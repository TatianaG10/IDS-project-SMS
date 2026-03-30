import java.awt.Point;

public class User {

    private String id;
    private Point  position;

    public User(String id, int x, int y) {
        this.id       = id;
        this.position = new Point(x, y);
    }

    // Call this when the user moves — Antenna will check distance after
    public void updatePosition(int x, int y) {
        this.position = new Point(x, y);
    }

    public String getId()       { return id; }
    public Point  getPosition() { return position; }
}