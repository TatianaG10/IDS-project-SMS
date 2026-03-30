import com.rabbitmq.client.*;
import java.awt.Point;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Antenna {

    private String id;
    private Point  position;
    private int    radius = 120;
    private Map<String, User> connectedUsers = new ConcurrentHashMap<>();

    private String     rightNeighborId;
    private int        ringSize;           // ADD THIS — needed for hop count check
    private Connection connection;
    private Channel    channel;

    private final String exchangeName  = "antenna_ring_exchange";
    private final String masterExchange = "master_broadcast_exchange";
    private final String masterQueue    = "master_storage_queue";

    public Antenna(String id, int x, int y, Connection connection,
                   String rightNeighborId, int ringSize) throws Exception {
        this.id              = id;
        this.position        = new Point(x, y);
        this.connection      = connection;
        this.rightNeighborId = rightNeighborId;
        this.ringSize        = ringSize;           // store it

        this.channel = connection.createChannel();
        this.channel.exchangeDeclare(exchangeName,  "direct");
        this.channel.exchangeDeclare(masterExchange, "fanout");

        String queueName = "antenna_queue_" + id;
        this.channel.queueDeclare(queueName, false, false, false, null);
        this.channel.queueBind(queueName, exchangeName,   "antenna." + id);
        this.channel.queueBind(queueName, masterExchange, "antenna." + id);

        listenForMessages(queueName);
        System.out.println("[Antenna " + id + "] Ready at (" + x + "," + y + ")");
    }

    private void listenForMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            try {
                Message msg = Message.deserialize(delivery.getBody());
                handleIncomingMessage(msg);
            } catch (Exception e) { e.printStackTrace(); }
        }, consumerTag -> {});
    }

    // Called when a User's keepalive reaches us and they aren't registered yet
    // Adds them to our local map so we can deliver messages to them
    public void connectUser(User user) {
        connectedUsers.put(user.getId(), user);
        System.out.println("[Antenna " + id + "] User " + user.getId() + " connected");
    }

    // Called when a User's keepalive stops (they moved away or disconnected)
    public void disconnectUser(String userId) {
        connectedUsers.remove(userId);
        System.out.println("[Antenna " + id + "] User " + userId + " disconnected");
    }

    private void handleIncomingMessage(Message msg) throws Exception {

        // Case 1: receiver is connected to THIS antenna → deliver directly
        if (connectedUsers.containsKey(msg.getReceiverId())) {
            deliverToUser(msg);

        // Case 2: message has visited every antenna → receiver is offline
        // FIX: use hopCount instead of comparing antenna IDs
        } else if (msg.hopCount >= ringSize) {
            sendToMaster(msg);

        // Case 3: keep forwarding around the ring
        } else {
            sendRight(msg);
        }
    }

    public void sendRight(Message msg) throws Exception {
        msg.hopCount++;                              // increment before forwarding
        msg.previousAntennaId = this.id;            // track where it came from

        channel.basicPublish(exchangeName, "antenna." + rightNeighborId,
                             null, msg.serialize());
        System.out.println("[Antenna " + id + "] Forwarded to " + rightNeighborId
                           + " (hop " + msg.hopCount + ")");
    }

    public void sendToMaster(Message msg) throws Exception {
        System.out.println("[Antenna " + id + "] Receiver offline, sending to master");
        channel.basicPublish(masterExchange, "", null, msg.serialize());
    }

    public void deliverToUser(Message msg) {
        User user = connectedUsers.get(msg.getReceiverId());
        if (user != null) {
            System.out.println("[Antenna " + id + "] Delivered \""
                + msg.getContent() + "\" to " + user.getId());
        }
    }

    public double euclideanDistance(Point otherPos) {
        return Math.sqrt(Math.pow(this.position.x - otherPos.x, 2) +
                         Math.pow(this.position.y - otherPos.y, 2));
    }

    public boolean isInRange(Point userPos) {
        return euclideanDistance(userPos) <= radius;
    }

    public String getId()       { return id; }
    public Point  getPosition() { return position; }
    public Channel getChannel() { return channel; }
}