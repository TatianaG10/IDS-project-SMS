import java.awt.Point;
import java.io.*;

public class Message implements Serializable {
    public enum Type { 
        WANT_TO_CONNECT, ANTENNA_REPLY_CONNECT, CONNECT_TO, 
        CHECK_CONNEXION, CONNEXION_OK, MESSAGE 
    }

    private static final long serialVersionUID = 1L;
    private Type type;
    private String senderId;
    private String receiverId;
    private Point senderCoordinate;
    private String content;
    private String originalAntennaId;

    public Message(Type type, String senderId, String receiverId, String content, Point senderCoordinate) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.senderCoordinate = senderCoordinate;
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static Message deserialize(byte[] data) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (Message) ois.readObject();
        }
    }

    // Getters and Setters
    public Type getType() { return type; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public Point getSenderCoordinate() { return senderCoordinate; }
    public String getOriginalAntennaId() { return originalAntennaId; }
    public void setOriginalAntennaId(String id) { this.originalAntennaId = id; }
}