import com.rabbitmq.client.*;
import java.util.*;

public class MasterAntennaNode {
    // Storage for offline users: Map<ReceiverID, List of Messages>
    private Map<String, List<Message>> messageStorage = new HashMap<>();
    
    private final Connection connection;
    private final Channel channel;
    
    // Exchanges from your design
    private final String masterExchange = "master_broadcast_exchange";
    private final String ringExchange = "antenna_ring_exchange";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Master Antenna Node...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        // Create connection and pass to constructor
        Connection connection = factory.newConnection();
        new MasterAntennaNode(connection);
    }

    public MasterAntennaNode(Connection connection) throws Exception {
        this.connection = connection;
        this.channel = connection.createChannel();

        // Declare the exchanges
        this.channel.exchangeDeclare(masterExchange, "fanout");
        this.channel.exchangeDeclare(ringExchange, "direct");

        // Set up the queue for the Master node
        String queueName = "master_storage_queue";
        this.channel.queueDeclare(queueName, false, false, false, null);
        this.channel.queueBind(queueName, masterExchange, "");

        System.out.println("[Master] Waiting for undelivered messages or user reconnections...");
        listenForMessages(queueName);
    }

    private void listenForMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            try {
                // Deserialize the incoming message
                Message msg = Message.deserialize(delivery.getBody());
                handleIncomingMessage(msg);
            } catch (Exception e) {
                System.err.println("[Master] Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }, consumerTag -> {});
    }

    private void handleIncomingMessage(Message msg) throws Exception {
        // CASE 1: It is a standard message that made a full tour of the ring
        if (msg.getType() == Message.Type.MESSAGE) {
            System.out.println("[Master] Storing undelivered message for user: " + msg.getReceiverId());
            
            // Add to list of stored messages for this specific user
            messageStorage.computeIfAbsent(msg.getReceiverId(), k -> new ArrayList<>()).add(msg);
        } 
        
        // CASE 2: A user has reconnected to an antenna (Rule 3/Algorithm)
        // Note: In your algo, users broadcast CONNECT_TO when they pick an antenna
        else if (msg.getType() == Message.Type.CONNECT_TO) {
            String userId = msg.getSenderId();
            String antennaId = msg.getContent(); // The antenna the user chose

            if (messageStorage.containsKey(userId)) {
                System.out.println("[Master] User " + userId + " reconnected to Antenna " + antennaId + ". Pushing stored messages...");
                
                List<Message> pendingMessages = messageStorage.remove(userId);
                for (Message storedMsg : pendingMessages) {
                    // Send it back to the specific antenna the user is now connected to
                    String routingKey = "antenna." + antennaId;
                    channel.basicPublish(ringExchange, routingKey, null, storedMsg.serialize());
                }
                System.out.println("[Master] All pending messages sent for user " + userId);
            }
        }
    }
}