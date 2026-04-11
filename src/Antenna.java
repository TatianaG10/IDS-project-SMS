import com.rabbitmq.client.*;
import java.awt.Point;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Antenna {
    private String id;
    private Point position;
    private int radius = 120;
    private Set<String> connectedUserIds = ConcurrentHashMap.newKeySet();

    private String leftNeighborId; 
    private final Connection connection;
    private final Channel channel;
    
    private final String exchangeRing = "antenna_ring_exchange";
    private final String exchangeUser = "antenna_user_exchange";
    private final String exchangeBroadcast = "user_broadcast_exchange";
    private final String masterExchange = "master_broadcast_exchange";

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: Antenna <id> <x> <y> <leftNeighborId>");
            return;
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        new Antenna(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), 
                    factory.newConnection(), args[3]);
    }

    public Antenna(String id, int x, int y, Connection connection, String leftNeighborId) throws Exception {
        this.id = id;
        this.position = new Point(x, y);
        this.connection = connection;
        this.leftNeighborId = leftNeighborId; 
        
        this.channel = connection.createChannel();
        this.channel.exchangeDeclare(exchangeRing, "direct");
        this.channel.exchangeDeclare(exchangeUser, "direct");
        this.channel.exchangeDeclare(exchangeBroadcast, "fanout");
        this.channel.exchangeDeclare(masterExchange, "fanout"); 
        
        String queueName = "antenna_queue_" + id;
        this.channel.queueDeclare(queueName, false, false, false, null);
        this.channel.queueBind(queueName, exchangeRing, "antenna." + id);
        this.channel.queueBind(queueName, exchangeBroadcast, "");

        System.out.println("Antenna " + id + " started.");
        listenForMessages(queueName);
    }

    private void listenForMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            try {
                Message msg = Message.deserialize(delivery.getBody());
                handleIncomingMessage(msg);
            } catch (Exception e) {
                System.err.println("Error processing message in Antenna: " + e.getMessage());
                e.printStackTrace();
            }
        }, consumerTag -> {});
    }

    private void handleIncomingMessage(Message msg) throws Exception {
        // Rule 1 : Request for connection from user and it is closed enough
        if (msg.getType() == Message.Type.WANT_TO_CONNECT) {
            if (this.position.distance(msg.getSenderCoordinate()) < radius) {
                Message reply = new Message(Message.Type.ANTENNA_REPLY_CONNECT, id, msg.getSenderId(), "", position);
                channel.basicPublish(exchangeUser, "user." + msg.getSenderId(), null, reply.serialize());
            }
        } 
        // Rule 2 : A user confirms us that he/she connect to that antenna
        else if (msg.getType() == Message.Type.CONNECT_TO) {
            if (msg.getContent().equals(id)) {
                connectedUserIds.add(msg.getSenderId());
                System.out.println("User " + msg.getSenderId() + " connected to " + id);
            } 
            // Rule 3 : A user confirms us that he/she connect to another antenna
            else {
                if (connectedUserIds.remove(msg.getSenderId())) {
                    System.out.println("User " + msg.getSenderId() + " moved to antenna " + msg.getContent());
                }
            }
        }
        // MSG handling rules
        else if (msg.getType() == Message.Type.MESSAGE) {
            // Receiver is in our list
            if (connectedUserIds.contains(msg.getReceiverId())) {
                System.out.println("Delivering message locally to " + msg.getReceiverId());
                channel.basicPublish(exchangeUser, "user." + msg.getReceiverId(), null, msg.serialize());
            } 
            // Loop detected (Receiver was not found in the ring)
            else if (id.equals(msg.getOriginalAntennaId())) {
                sendToMaster(msg);
            } 
            // Receiver not here, send to left
            else {
                if (msg.getOriginalAntennaId() == null) msg.setOriginalAntennaId(id);
                sendLeft(msg);
            }
        }
        // Rule 4 : A user want to confirm that it is indeed connected to us
        else if (msg.getType() == Message.Type.CHECK_CONNEXION) {
            if (this.position.distance(msg.getSenderCoordinate()) < radius) {
                Message ok = new Message(Message.Type.CONNEXION_OK, id, msg.getSenderId(), "", position);
                channel.basicPublish(exchangeUser, "user." + msg.getSenderId(), null, ok.serialize());
            } else {
                connectedUserIds.remove(msg.getSenderId());
            }
        }
    }

    public void sendLeft(Message msg) throws Exception {
        String routingKey = "antenna." + leftNeighborId;
        channel.basicPublish(exchangeRing, routingKey, null, msg.serialize());
    }

    public void sendToMaster(Message msg) throws Exception {
        System.out.println("User offline. Sending to Master Antenna storage.");
        channel.basicPublish(masterExchange, "", null, msg.serialize());
    }
}