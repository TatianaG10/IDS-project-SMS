import java.util.ArrayList;
import java.awt.*;
import java.io.Serializable;


public class Message implements Serializable {


    // A completer en fonction des besoins
    private String senderId;
    private String receiverId;
    private Point senderCoordinate;
    private String content;
    public String originalAntennaId;
    public String previousAntennaId;

    // private ArrayList<String> route = new ArrayList<>();


    public Message(String senderId, String receiverId, String content, Point senderPositionPoint, String originalAntennaId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.senderCoordinate = senderPositionPoint;
        this.originalAntennaId = originalAntennaId; // Store the original antenna ID in the message
        this.previousAntennaId = originalAntennaId; // Initialize previousAntennaId to originalAntennaId


    }



    // Getters
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getOriginalAntennaId() { return originalAntennaId; }
    public String getPreviousAntennaId() { return previousAntennaId; }
    public Point getSenderCoordinate() { return senderCoordinate; }


}