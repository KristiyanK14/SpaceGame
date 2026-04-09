package game;

public class Vector2D {
    public double x;
    public double y;

    // 2d vector used for position and movement
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D copy() {
        return new Vector2D(x, y);
    }

    public void add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void multiply(double amount) {
        this.x *= amount;
        this.y *= amount;
    }

    public double distanceTo(Vector2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // creates a vector from an angle and speed
    public static Vector2D fromAngle(double angle, double length) {
        return new Vector2D(Math.cos(angle) * length, Math.sin(angle) * length);
    }
}
