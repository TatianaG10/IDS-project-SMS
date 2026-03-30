import java.awt.Point;
import java.io.*;
import java.util.ArrayList;

public class Message implements Serializable {

    private String senderId;
    private String receiverId;
    private Point  senderCoordinate;
    private String content;
    public  String originalAntennaId;
    public  String previousAntennaId;
    public  int    hopCount = 0;        // ADD THIS — counts how many antennas we've visited

    public Message(String senderId, String receiverId, String content,
                   Point senderPositionPoint, String originalAntennaId) {
        this.senderId          = senderId;
        this.receiverId        = receiverId;
        this.content           = content;
        this.senderCoordinate  = senderPositionPoint;
        this.originalAntennaId = originalAntennaId;
        this.previousAntennaId = originalAntennaId;
    }

    // Turns this object into bytes so RabbitMQ can send it
    // Think of it like putting a letter in an envelope
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    // Turns bytes back into a Message object when we receive it
    // Think of it like opening the envelope and reading the letter
    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream    ois = new ObjectInputStream(bis);
        return (Message) ois.readObject();
    }

    // All your existing getters stay exactly the same
    public String getSenderId()        { return senderId; }
    public String getReceiverId()      { return receiverId; }
    public String getContent()         { return content; }
    public String getOriginalAntennaId() { return originalAntennaId; }
    public String getPreviousAntennaId() { return previousAntennaId; }
    public Point  getSenderCoordinate()  { return senderCoordinate; }
    public int    getHopCount()          { return hopCount; }
}