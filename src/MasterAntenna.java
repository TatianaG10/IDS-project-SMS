import com.rabbitmq.client.*;          // RIGHT import — not java.sql or java.nio
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MasterAntenna {

    // receiverId → list of messages waiting for that user
    private Map<String, List<Message>> storage = new ConcurrentHashMap<>();

    private final Connection connection;
    private final Channel    channel;
    private final String masterExchange = "master_broadcast_exchange";
    private final String storageQueue   = "master_storage_queue";

    public MasterAntenna(Connection connection) throws Exception {
        this.connection = connection;
        this.channel    = connection.createChannel();

        // We declare these — antennas use them but don't own them
        this.channel.exchangeDeclare(masterExchange, "fanout");
        this.channel.queueDeclare(storageQueue, false, false, false, null);
        // Bind the storage queue to the fanout exchange so we receive messages sent to it
        this.channel.queueBind(storageQueue, masterExchange, "");
    }

    public void start() throws Exception {
        channel.basicConsume(storageQueue, true, (tag, delivery) -> {
            try {
                Message msg = Message.deserialize(delivery.getBody());
                handleMessage(msg);
            } catch (Exception e) { e.printStackTrace(); }
        }, tag -> {});

        System.out.println("[Master] Ready — listening for undelivered messages");
    }

    private void handleMessage(Message msg) throws Exception {
        // When a message arrives here it means no antenna found the receiver
        // We just store it and wait for the user to come back online
        storeMessage(msg);
    }

    // Saves a message for an offline user
    private void storeMessage(Message msg) {
        // computeIfAbsent: if the list doesn't exist yet, create it, then add
        storage.computeIfAbsent(msg.getReceiverId(), k -> new ArrayList<>()).add(msg);
        System.out.println("[Master] Stored message for offline user: " + msg.getReceiverId());
    }

    // Called by an Antenna when a user reconnects to it
    // antennaId = which antenna the user connected to (so we know where to send)
    public void flushMessages(String userId, String antennaId, Channel antennaChannel)
            throws Exception {

        List<Message> messages = storage.remove(userId); // remove = take them out of storage
        if (messages == null || messages.isEmpty()) {
            System.out.println("[Master] No stored messages for " + userId);
            return;
        }

        // Send each stored message to the antenna the user is now at
        String antennaQueue = "antenna_queue_" + antennaId;
        for (Message msg : messages) {
            antennaChannel.basicPublish("", antennaQueue, null, msg.serialize());
        }
        System.out.println("[Master] Flushed " + messages.size()
            + " messages to " + userId + " via antenna " + antennaId);
    }

    // Broadcasts a message to ALL antennas at once (via fanout exchange)
    // Useful for system messages like "user X reconnected"
    public void broadcastToAll(Message msg) throws Exception {
        channel.basicPublish(masterExchange, "", null, msg.serialize());
        System.out.println("[Master] Broadcast sent to all antennas");
    }
}