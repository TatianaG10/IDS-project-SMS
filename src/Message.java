import java.util.ArrayList;
import java.awt.*;


public class Message {
    // A completer en fonction des besoins
    private String senderId;
    private String receiverId;
    private Point senderCoordinate;
    private String content;
    // private String currentAntenna;
    // private String previousAntenna;
    private ArrayList<String> route = new ArrayList<>();


    public Message(String senderId, String receiverId, String content, Point senderPositionPoint) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.senderCoordinate = senderPositionPoint;

    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }


    public void updateHeader(String sourceAntenna) {
        this.route.add(sourceAntenna);
    }
}