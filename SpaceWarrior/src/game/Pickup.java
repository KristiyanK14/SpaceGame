package game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Pickup extends SpaceObject {
    public enum Type {
        SCORE,
        SHIELD,
        LIFE
    }

    private final Type type;
    private int timeLeft = 600;

    // pickups give score, shield or an extra life
    public Pickup(double x, double y, Type type) {
        super(x, y, 10);
        this.type = type;
    }

    @Override
    // pickups spin slowly and stay in the world
    public void update(GamePanel game) {
        timeLeft--;
        if (timeLeft <= 0) alive = false;
    }

    @Override
    // draws the pickup using a colour based on its type
    public void draw(Graphics2D g) {
        if (type == Type.SCORE) g.setColor(Color.GREEN);
        if (type == Type.SHIELD) g.setColor(Color.CYAN);
        if (type == Type.LIFE) g.setColor(Color.PINK);
        g.fillOval((int) position.x - 10, (int) position.y - 10, 20, 20);
        g.setColor(Color.WHITE);
        if (type == Type.SCORE) g.drawString("$", (int) position.x - 4, (int) position.y + 4);
        if (type == Type.SHIELD) g.drawString("S", (int) position.x - 4, (int) position.y + 4);
        if (type == Type.LIFE) g.drawString("+", (int) position.x - 4, (int) position.y + 4);
    }

    public Type getType() {
        return type;
    }
}
