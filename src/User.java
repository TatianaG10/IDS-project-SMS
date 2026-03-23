import java.awt.*;
import java.util.ArrayList;

public class User {
    private String id;
    private Antenna connectedAntenna;
    private Point initialPoss;
    private Point userPoss;

    public User(String id, Integer x, Integer y) {
        this.id = id;
        this.initialPoss = new Point(x, y);
        this.userPoss = initialPoss;
    }

    public void connect(Antenna antenna) {
        this.connectedAntenna = antenna;
        antenna.registerUser(this);
    }

    public void sendMessage(String receiverId, String content) {
        Message msg = new Message(id, receiverId, content, userPoss);
        // sending msg to antenna responsible to relay msg to receiver 
    }

    public void receiveMessage(Message msg) {
        System.out.println("[" + id + "] Received from " +
            msg.getSenderId() + ": " + msg.getContent());
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