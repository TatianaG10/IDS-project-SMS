import com.rabbitmq.client.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class User {
    private String id;
    private String connectedAntennaId = null;
    private Point userPoss;
    private boolean connected = false;
    private List<Message> antennaReplies = new ArrayList<>();

    private final Connection connection;
    private final Channel channel;
    private final String exchangeUser = "antenna_user_exchange";
    private final String exchangeBroadcast = "user_broadcast_exchange";
    private final String exchangeRing = "antenna_ring_exchange";
    private boolean isSelecting = false;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java User <id> <x> <y>");
            return;
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        new User(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), factory.newConnection());
    }

    public User(String id, Integer x, Integer y, Connection connection) throws Exception {
        this.id = id;
        this.userPoss = new Point(x, y);
        this.connection = connection;
        this.channel = connection.createChannel();

        String queueName = "user_queue_" + id;
        this.channel.queueDeclare(queueName, false, false, false, null);
        this.channel.queueBind(queueName, exchangeUser, "user." + id);

        connectToAntenna();
        
        // Use a thread for receive to prevent blocking
        new Thread(() -> {
            try {
                receiveMessages(queueName);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        listenForInput();
    }

    private void receiveMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (tag, del) -> {
            try {
                Message msg = Message.deserialize(del.getBody());
                
                // Case 1: Antenna replying to broadcast
                if (msg.getType() == Message.Type.ANTENNA_REPLY_CONNECT) {
                    antennaReplies.add(msg);
                    
                    if (!isSelecting) {
                        isSelecting = true;
                        new Thread(() -> {
                            try {
                                Thread.sleep(500); // Collection window
                                handleSelection();
                                isSelecting = false;
                            } catch (Exception e) { e.printStackTrace(); }
                        }).start();
                    }
                } 
                // Case 2: Actual SMS message from another user
                else if (msg.getType() == Message.Type.MESSAGE) {
                    System.out.println("\n[" + id + "] Received SMS from " + 
                                    msg.getSenderId() + ": " + msg.getContent());
                    System.out.print("Target User ID: ");
                }

            } catch (Exception e) { e.printStackTrace(); }
        }, tag -> {});
    }
        
    // Rule 3: Pick the closest antenna and confirm the connection
    private void handleSelection() throws Exception {
        if (antennaReplies.isEmpty()) return;

        // Find the antenna with the minimum distance to the user
        Message best = antennaReplies.stream()
            .min(Comparator.comparingDouble(m -> userPoss.distance(m.getSenderCoordinate())))
            .get();

        this.connectedAntennaId = best.getSenderId();
        this.connected = true;
        System.out.println("Connected to antenna: " + connectedAntennaId);

        // Notify the system which antenna we chose so others can ignore us
        Message confirm = new Message(Message.Type.CONNECT_TO, id, "", connectedAntennaId, userPoss);
        channel.basicPublish(exchangeBroadcast, "", null, confirm.serialize());
    }

    // Rule 1: Broadcast a request to find all antennas in range
    public void connectToAntenna() throws Exception {
        this.connected = false;
        this.antennaReplies.clear(); // Clear previous search results
        System.out.println("Broadcasting WANT_TO_CONNECT...");
        
        // Send a broadcast message that all antennas will hear
        Message want = new Message(Message.Type.WANT_TO_CONNECT, id, "", "", userPoss);
        channel.basicPublish(exchangeBroadcast, "", null, want.serialize());
    }

    // Rules 4-6: Handle terminal input to send messages
    public void listenForInput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("Target User ID:");
                String to = reader.readLine();
                System.out.println("Message:");
                String content = reader.readLine();

                // Rule 4: Only send if we are currently connected to an antenna
                if (connected) {
                    Message sms = new Message(Message.Type.MESSAGE, id, to, content, userPoss);
                    // Send directly to our chosen antenna's routing key
                    channel.basicPublish(exchangeRing, "antenna." + connectedAntennaId, null, sms.serialize());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}