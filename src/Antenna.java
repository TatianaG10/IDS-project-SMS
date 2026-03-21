import java.util.HashMap;
import java.util.Map;

public class Antenna {
    private String id;
    // private Server server; => in case we decide to go for a central server design
    private Map<String, User> connectedUsers = new HashMap<>();

    public Antenna(String id) {
        this.id = id;
    }

    public void registerUser(User user) {
        connectedUsers.put(user.getId(), user);
    }

    public void deliverToUser(Message msg) {
        User user = connectedUsers.get(msg.getReceiverId());
        if (user != null) {
            // find user to deliver msg
        }
    }

    public String getId() {
        return id;
    }
}