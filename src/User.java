import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class User {
    // Physical properties
    private String id;
    private Antenna connectedAntenna;
    private Point initialPoss;
    private Point userPoss;

    // Network properties
    private final Connection connection;
    private final Channel channel;
    private final String exchangeUserName = "antenna_user_exchange";
    private final String exchangeBroadcastUserName = "user_broadcast_exchange";


    public User(String id, Integer x, Integer y, Connection connection) {
        // Setup data
        this.id = id;
        this.initialPoss = new Point(x, y);
        this.userPoss = initialPoss;
        this.connection = connection;

        // And the actual connection
        String queueName = "user_queue_" + id;
        this.channel = connection.createChannel();
        this.channel.queueBind(queueName, exchangeUserName, "user." + id);
        this.channel.queueBind(queueName, exchangeBroadcastUserName, "user." + id);

        // Do standard action
        connectToAntenna();
        receiveMessage(queueName);
        listenForLoopedMessages(queueName);

    }

    public void listenForLoopedMessages(String queueName) {
        try (
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            for (;;)
            {
                // Get data from the user, to which we send it and also the content of the message
                String userInput;
                System.out.println("Message: ");
                if ((userInput = stdIn.readLine()) == null)
                    break;
                String toWho;
                System.out.println("Receiver: (Enter the id)");
                if ((toWho = stdIn.readLine()) == null)
                    break;

                // Send the message to the antenna of the user
                sendMessage(toWho, userInput);
            }
        } catch (Exception e) {
            System.out.println("Error when trying to read user's input");
        }
    }


    public void connectToAntenna() {
        // Setup the callback to connect to an antenna
        System.out.println("Try to connect to antenna");
        // TODO : We want to create a specific message to connect to an antenna
        sendMessageBroadcast("CONNECTION");
     }


    public void sendMessage(String receiverId, String content) {
        // TODO : Handle the case where we are not connected to an antenna but we are still trying to send a message
        String routingKey = "antenna." + connectedAntenna.getId();
        Message message = new Message(id, receiverId, content, userPoss);
        byte[] data = message.serialize();
        this.channel.basicPublish(exchangeUserName, routingKey, null, data);
        System.out.println("User " + id + " sent message to antenna " + connectedAntenna.getId());
    }


    public void sendMessageBroadcast(String content) {
        Message message = new Message(id, id, content, userPoss); // Don't have receivedID, so we set both as the same id as us
        byte[] data = message.serialize();
        this.channel.basicPublish(exchangeBroadcastUserName, null, null, data);
        System.out.println("User " + id + " sent message to antenna " + connectedAntenna.getId());
    }

    public void receiveMessage(String queueName) {
        this.channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            Message message = Message.deserialize(delivery.getBody());

            // Case 1 : We receive a message about an antenna replying to our CONNECT message

            // Case 2 : We receive a message that was for us

            System.out.println("[" + id + "] Received from " +
                message.getSenderId() + ": " + message.getContent());

        }, consumerTag -> {});
    }


    public String getId() {
        return id;
    }

    public Antenna getNearestAntenna(ArrayList<Antenna> antennasList) {

        Double minDistance = 0.0;

        for (Antenna antenna: antennasList) {
            Double distance = antenna.eucledianDistance(antenna);

            if (minDistance == 0.0) {
                minDistance = distance;
                this.connectedAntenna = antenna;
                return this.connectedAntenna;
            }

            else if (minDistance > distance) {
                minDistance = distance;

            this.connectedAntenna = antenna;
                }        
        }
        
        return this.connectedAntenna;
    
    }

}