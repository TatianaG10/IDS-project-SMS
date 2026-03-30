import java.awt.Point;
import java.io.*;

public class Message implements Serializable {
    public enum Type { PING, PONG, BROADCAST_PING, BROADCAST_PONG, CONNECT, MSG }

    private static final long serialVersionUID = 1L;

    private Type type;
    private String senderId;
    private String receiverId;
    private Point senderCoordinate;
    private String content;
    public String originalAntennaId;
    public String previousAntennaId;

    public Message(Type type, String senderId, String receiverId,
                   String content, Point senderCoordinate, String originalAntennaId) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.senderCoordinate = senderCoordinate;
        this.originalAntennaId = originalAntennaId;
        this.previousAntennaId = originalAntennaId;
    }

    // Serialization helpers
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (Message) ois.readObject();
        }
    }

    // Getters
    public Type getType()               { return type; }
    public String getSenderId()         { return senderId; }
    public String getReceiverId()       { return receiverId; }
    public String getContent()          { return content; }
    public String getOriginalAntennaId(){ return originalAntennaId; }
    public String getPreviousAntennaId(){ return previousAntennaId; }
    public Point getSenderCoordinate()  { return senderCoordinate; }

    public void setPreviousAntennaId(String id) { this.previousAntennaId = id; }
}