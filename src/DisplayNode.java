import com.rabbitmq.client.Channel;    // Fixes the "cannot find symbol" errors for channel
import com.rabbitmq.client.Connection; // Fixes the "incompatible types" error
import com.rabbitmq.client.ConnectionFactory;
import javax.swing.*;
import java.awt.*;
public class DisplayNode {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("SMS System Monitor - MoSIG");
        AireDeDessin canvas = new AireDeDessin();
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Exchanges to monitor
        channel.exchangeDeclare("user_broadcast_exchange", "fanout");
        channel.exchangeDeclare("antenna_ring_exchange", "direct");

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "user_broadcast_exchange", "");
        // Sniff all antenna traffic using wildcards
        channel.queueBind(queueName, "antenna_ring_exchange", "antenna.#");

        System.out.println("Monitor Active. Waiting for nodes...");

channel.basicConsume(queueName, true, (tag, del) -> {
    try {
        // You must wrap this because deserialize throws Exception
        Message msg = Message.deserialize(del.getBody());
        
                    switch (msg.getType()) {
                        case WANT_TO_CONNECT:
                            canvas.updateUser(msg.getSenderId(), msg.getSenderCoordinate(), null);
                            break;
                        case CONNECT_TO:
                            canvas.updateUser(msg.getSenderId(), msg.getSenderCoordinate(), msg.getContent());
                            canvas.showPopup(msg.getSenderId(), "Connected to " + msg.getContent());
                            break;
                        case ANTENNA_REPLY_CONNECT: 
                            canvas.updateAntenna(msg.getSenderId(), msg.getSenderCoordinate());
                            break;
                        case MESSAGE:
                            canvas.showPopup(msg.getSenderId(), "📤 Sending...");
                            new Thread(() -> {
                                try { 
                                    Thread.sleep(800); 
                                    canvas.showPopup(msg.getReceiverId(), "📩 Received!"); 
                                } catch (Exception e) { e.printStackTrace(); }
                            }).start();
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error deserializing message in DisplayNode: " + e.getMessage());
                    e.printStackTrace();
                }
            }, tag -> {});
    }
}