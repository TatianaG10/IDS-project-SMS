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
    private long lastAckTime = System.currentTimeMillis(); 

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
        
        new Thread(() -> {
            try {
                receiveMessages(queueName);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        // Separate thread for periodic connection checks
        startHeartbeatThread();

        listenForInput();
    }

    private void receiveMessages(String queueName) throws Exception {
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            try {
                Message msg = Message.deserialize(delivery.getBody());
                
                // Rule 2 : We get a reply from an antenna that want to connect to this user
                if (msg.getType() == Message.Type.ANTENNA_REPLY_CONNECT) {
                    antennaReplies.add(msg);
                    
                    if (!isSelecting) {
                        isSelecting = true;
                        new Thread(() -> {
                            try {
                                Thread.sleep(500); // Wait to collect more replies
                                handleSelection();
                                isSelecting = false;
                            } catch (Exception e) { e.printStackTrace(); }
                        }).start();
                    }
                } 
                // Receiving acknowledgement from antenna
                else if (msg.getType() == Message.Type.CONNEXION_OK) {
                    this.connected = true;
                    this.lastAckTime = System.currentTimeMillis();
                }
                // Rule 4 (Part 2) : Receiving a message
                else if (msg.getType() == Message.Type.MESSAGE) {
                    System.out.println("\n[" + id + "] Received SMS from " + 
                                    msg.getSenderId() + ": " + msg.getContent());
                    System.out.print("Target User ID (or 'move X Y'): ");
                }

            } catch (Exception e) { e.printStackTrace(); }
        }, tag -> {});
    }

    private void startHeartbeatThread() {
        new Thread(() -> {
            while (true) {
                try {
                    // Rule 7 : When the delay for which we wait is not expired yet...
                    Thread.sleep(5000); 

                    if (connected && connectedAntennaId != null) {
                        // Rule 8 : When the delay is expired, we send a test message
                        Message hb = new Message(Message.Type.CHECK_CONNEXION, id, connectedAntennaId, "", userPoss);
                        channel.basicPublish(exchangeRing, "antenna." + connectedAntennaId, null, hb.serialize());

                        // Rule 9 : If delay for receiving acknowledgement message is reached (12s)
                        if (System.currentTimeMillis() - lastAckTime > 12000) {
                            System.out.println("\n[System] Lost connection to antenna " + connectedAntennaId);
                            connected = false;
                            connectToAntenna(); 
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
    }
        
    // Rule 3 : We connect to the closest one, specifying to all other antennas that we are connected
    private void handleSelection() throws Exception {
        if (antennaReplies.isEmpty()) return;

        Message best = antennaReplies.stream()
            .min(Comparator.comparingDouble(m -> userPoss.distance(m.getSenderCoordinate())))
            .get();

        this.connectedAntennaId = best.getSenderId();
        this.connected = true;
        this.lastAckTime = System.currentTimeMillis();
        System.out.println("Connected to antenna: " + connectedAntennaId);

        // Notify system - CONNECT_TO(id, connected_to)
        Message confirm = new Message(Message.Type.CONNECT_TO, id, "", connectedAntennaId, userPoss);
        channel.basicPublish(exchangeBroadcast, "", null, confirm.serialize());
    }

    public void connectToAntenna() throws Exception {
        // Rule 1 : We have a new user, so this user want to connect to an antenna
        this.connected = false;
        this.antennaReplies.clear(); 
        System.out.println("Broadcasting WANT_TO_CONNECT...");
        
        Message want = new Message(Message.Type.WANT_TO_CONNECT, id, "", "", userPoss);
        channel.basicPublish(exchangeBroadcast, "", null, want.serialize());
    }

    public void listenForInput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("Target User ID (or 'move X Y'):");
                String input = reader.readLine();

                // Manual trigger for movement
                if (input.startsWith("move")) {
                    String[] parts = input.split(" ");
                    int newX = Integer.parseInt(parts[1]);
                    int newY = Integer.parseInt(parts[2]);
                    this.userPoss.setLocation(newX, newY);
                    System.out.println("Moved to (" + newX + ", " + newY + ")");
                    continue;
                }

                String to = input;
                System.out.println("Message:");
                String content = reader.readLine();

                // Rule 4 : This user want to send a message... and is currently connected
                if (connected) {
                    Message sms = new Message(Message.Type.MESSAGE, id, to, content, userPoss);
                    channel.basicPublish(exchangeRing, "antenna." + connectedAntennaId, null, sms.serialize());
                } 
                // Rule 5 : Want to send a message, but currently not connected
                else {
                    System.out.println("Waiting for connection...");
                    connectToAntenna();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}