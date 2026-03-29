import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Antenna {

    // Physical properties
    private String id;
    private Point position;
    private int radius = 120; // coverage radius
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>(); // concurrent for thread safety

    // Network properties
    private String rightNeighborId; 
    private final Connection connection;
    private final Channel channel;
    private final String exchangeName = "antenna_ring_exchange";
    private final String masterExchange = "master_broadcast_exchange";

    public Antenna(String id, int x, int y, Connection connection, String rightNeighborId) throws Exception {
            this.id = id;
            this.position = new Point(x, y);
            this.connection = connection;
            this.rightNeighborId = rightNeighborId; 
            
            this.channel = connection.createChannel();
            this.channel.exchangeDeclare(exchangeName, "direct");
            this.channel.exchangeDeclare(masterExchange, "fanout"); 
            
            String queueName = "antenna_queue_" + id;
            this.channel.queueDeclare(queueName, false, false, false, null);
            this.channel.queueBind(queueName, exchangeName, "antenna." + id);


            listenForMessages(queueName);
        }

    private void listenForMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            Message msg = Message.deserialize(delivery.getBody());
            handleIncomingMessage(msg);
        }, consumerTag -> {});
    }

    
    public void sendRight(Message msg) throws Exception {

        String routingKey = "antenna." + rightNeighborId;

        byte[] data = msg.serialize(); 
        
        channel.basicPublish(exchangeName, routingKey, null, data);
        System.out.println("Antenna " + id + " forwarded message to " + rightNeighborId);
    }


    public void sendToMaster(Message msg) throws Exception {
        System.out.println("User offline. Sending to Master Antenna storage.");
        channel.basicPublish(masterExchange, "", null, msg.serialize());
    }


    private void handleIncomingMessage(Message msg) throws Exception {

        // Case 1: User is connected to THIS antenna
        if (connectedUsers.containsKey(msg.getReceiverId())) {
            deliverToUser(msg);
        } 
        // Case 2: Message has circled the entire ring (Loop Detection)
        else if (msg.getOriginalAntennaId().equals(this.id)) {
            sendToMaster(msg);
        } 
        // Case 3: User not here, send to neighbor
        else {
            sendRight(msg);
        }
    }


    // To calculate distance to a user for connection purposes
    public double euclideanDistance(Point otherPos) {
        return Math.sqrt(Math.pow(this.position.x - otherPos.x, 2) + 
                         Math.pow(this.position.y - otherPos.y, 2));
    }

    public void deliverToUser(Message msg) {
        User user = connectedUsers.get(msg.getReceiverId());
        if (user != null) {
            System.out.println("Delivering locally to user: " + user.getId());
        }
    }

    public String getId() { return id; }
    public Point getPosition() { return position; }
}