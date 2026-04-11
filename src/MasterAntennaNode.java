import com.rabbitmq.client.*;
import java.util.*;

public class MasterAntennaNode {
    private Map<String, List<Message>> messageStorage = new HashMap<>();
    
    private final Connection connection;
    private final Channel channel;
    
    private final String masterExchange = "master_broadcast_exchange";
    private final String userBroadcastExchange = "user_broadcast_exchange";
    private final String ringExchange = "antenna_ring_exchange";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Master Antenna Node...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = factory.newConnection();
        new MasterAntennaNode(connection);
    }

    public MasterAntennaNode(Connection connection) throws Exception {
        this.connection = connection;
        this.channel = connection.createChannel();

        this.channel.exchangeDeclare(masterExchange, "fanout");
        this.channel.exchangeDeclare(userBroadcastExchange, "fanout");
        this.channel.exchangeDeclare(ringExchange, "direct");

        String queueName = "master_storage_queue";
        this.channel.queueDeclare(queueName, false, false, false, null);
        
        // Listen for messages that made a full tour OR user reconnections
        this.channel.queueBind(queueName, masterExchange, "");
        this.channel.queueBind(queueName, userBroadcastExchange, "");

        System.out.println("[Master] Waiting for undelivered messages or user reconnections...");
        listenForMessages(queueName);
    }

    private void listenForMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            try {
                Message msg = Message.deserialize(delivery.getBody());
                handleIncomingMessage(msg);
            } catch (Exception e) {
                System.err.println("[Master] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }, consumerTag -> {});
    }

    private void handleIncomingMessage(Message msg) throws Exception {
        // CASE: Undelivered message reached master
        if (msg.getType() == Message.Type.MESSAGE) {
            System.out.println("[Master] Storing message for " + msg.getReceiverId());
            messageStorage.computeIfAbsent(msg.getReceiverId(), k -> new ArrayList<>()).add(msg);
        } 
        // CASE: Re-delivery trigger - User broadcasts their new antenna connection
        else if (msg.getType() == Message.Type.CONNECT_TO) {
            String userId = msg.getSenderId();
            String antennaId = msg.getContent(); 

            if (messageStorage.containsKey(userId)) {
                System.out.println("[Master] User " + userId + " found on " + antennaId + ". Relaying messages...");
                List<Message> pendingMessages = messageStorage.remove(userId);
                for (Message storedMsg : pendingMessages) {
                    channel.basicPublish(ringExchange, "antenna." + antennaId, null, storedMsg.serialize());
                }
            }
        }
    }
}