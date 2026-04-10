import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AireDeDessin extends JPanel {
    // We use Maps to store positions so we can update them dynamically
    private Map<String, Point> antennas = new ConcurrentHashMap<>();
    private Map<String, Point> users = new ConcurrentHashMap<>();
    private Map<String, String> userConnections = new ConcurrentHashMap<>(); // UserID -> AntennaID
    private Map<String, String> popups = new ConcurrentHashMap<>(); // UserID -> Popup message

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
            g2.fillOval(p.x - 10, p.y - 10, 20, 20);
            g2.drawString("Antenna: " + entry.getKey(), p.x + 12, p.y);
            // Draw Radius
            g2.setColor(new Color(0, 0, 255, 30));
            g2.drawOval(p.x - 120, p.y - 120, 240, 240);
            g2.setColor(Color.BLUE);
        }

        // Draw Users
        g2.setColor(Color.RED);
        for (Map.Entry<String, Point> entry : users.entrySet()) {
            Point p = entry.getValue();
            g2.fillRect(p.x - 5, p.y - 5, 10, 10);
            g2.drawString("User: " + entry.getKey(), p.x + 10, p.y);
            
            // Draw connection line if connected
            String antId = userConnections.get(entry.getKey());
            if (antId != null && antennas.containsKey(antId)) {
                Point antP = antennas.get(antId);
                g2.setColor(Color.GREEN);
                g2.drawLine(p.x, p.y, antP.x, antP.y);
                g2.setColor(Color.RED);
            }
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