import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class AireDeDessin extends JPanel {
    // We use Maps to store positions so we can update them dynamically
    private Map<String, Point> antennas = new ConcurrentHashMap<>();
    private Map<String, Point> users = new ConcurrentHashMap<>();
    private Map<String, String> userConnections = new ConcurrentHashMap<>(); // UserID -> AntennaID
    private Map<String, String> popups = new ConcurrentHashMap<>(); // UserID -> Popup message

    private Image antennaImg;
    private Image userImg;


    public void showPopup(String userId, String text) {
        popups.put(userId, text);
        repaint();
        // Remove the popup after 10 seconds
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                popups.remove(userId);
                repaint();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }

    public AireDeDessin() {
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(800, 600));

        try {
            userImg = ImageIO.read(new File("Images/cow.png"));
            antennaImg = ImageIO.read(new File("Images/antenna.png"));
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    public void updateAntenna(String id, Point p) {
        antennas.put(id, p);
        repaint();
    }

    public void updateUser(String id, Point p, String connectedTo) {
        users.put(id, p);
        if (connectedTo != null) userConnections.put(id, connectedTo);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Antennas
        g2.setColor(Color.BLUE);
        for (Map.Entry<String, Point> entry : antennas.entrySet()) {
            Point p = entry.getValue();
            
            // Draw the coverage radius circle 
            g2.setColor(new Color(0, 102, 204, 20));
            g2.fillOval(p.x - 120, p.y - 120, 240, 240);

            if (antennaImg != null) {
                g2.drawImage(antennaImg, p.x - 20, p.y - 20, 40, 40, this);
            } else {
                g2.setColor(new Color(0, 102, 204));
                g2.fillOval(p.x - 12, p.y - 12, 24, 24);
            }
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), p.x - 15, p.y + 35);
        }

        // Draw Users
        g2.setColor(Color.RED);
        for (Map.Entry<String, Point> entry : users.entrySet()) {
            Point p = entry.getValue();
            if (userImg != null) {
                g2.drawImage(userImg, p.x - 16, p.y - 16, 32, 32, this);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(p.x - 8, p.y - 8, 16, 16);
            }
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), p.x - 15, p.y + 30);

        }

        for (Map.Entry<String, String> entry : popups.entrySet()) {
            String userId = entry.getKey();
            if (users.containsKey(userId)) {
                Point p = users.get(userId);
                String text = entry.getValue();
                
                // Draw a small speech bubble
                g2.setColor(new Color(255, 255, 200)); // Pale yellow
                int width = g2.getFontMetrics().stringWidth(text) + 10;
                g2.fillRoundRect(p.x - width/2, p.y - 40, width, 25, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(p.x - width/2, p.y - 40, width, 25, 10, 10);
                g2.drawString(text, p.x - width/2 + 5, p.y - 23);
            }

        }
    }
}