public class User {
    private String id;
    private Antenna connectedAntenna;

    public User(String id) {
        this.id = id;
    }

    public void connect(Antenna antenna) {
        this.connectedAntenna = antenna;
        antenna.registerUser(this);
    }

    public void sendMessage(String receiverId, String content) {
        Message msg = new Message(id, receiverId, content);
        // sending msg to antenna responsible to relay msg to receiver 
    }

    public void receiveMessage(Message msg) {
        System.out.println("[" + id + "] Received from " +
            msg.getSenderId() + ": " + msg.getContent());
    }

    public String getId() {
        return id;
    }
}