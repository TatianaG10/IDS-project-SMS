import javax.imageio.ImageIO;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Antenna {

    private String id;
    private Point position;
    private int radius;
    private Image img;
    private Map<String, User> connectedUsers = new HashMap<>();

    public Antenna(String id, int x, int y) {
        this.id = id;
        this.position = new Point(x, y);
        this.radius = 120;

        try {
            InputStream in = new FileInputStream("Images/antenna.png");
            img = ImageIO.read(in);
        } catch (Exception e) {
            System.err.println("Cannot load antenna image");
            System.exit(1);
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Filled translucent circle
        g2d.setColor(new Color(55, 138, 221, 40));
        g2d.fillOval(position.x - radius, position.y - radius, radius * 2, radius * 2);

        // Dashed border
        float[] dash = {6f, 4f};
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2d.setColor(new Color(55, 138, 221, 160));
        g2d.drawOval(position.x - radius, position.y - radius, radius * 2, radius * 2);

        // Reset stroke, draw icon and label
        g2d.setStroke(new BasicStroke(1f));
        if (img != null) {
            g2d.drawImage(img, position.x - 20, position.y - 20, 40, 40, null);
        }
        g2d.setColor(new Color(24, 95, 165));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.drawString(id, position.x - 8, position.y + 34);
    }

    public void moveTo(int x, int y) {
        this.position = new Point(x, y);
    }

    public void registerUser(User user) {
        connectedUsers.put(user.getId(), user);
    }

    public void deregisterUser(User user) {
        connectedUsers.remove(user.getId());
    }

    public void deliverToUser(Message msg) {
        User user = connectedUsers.get(msg.getReceiverId());
        if (user != null) {
            // deliver msg to user
        }
    }

    public String getId()      { return id; }
    public Point getPosition() { return position; }
    public int getRadius()     { return radius; }
}