package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Particle extends SpaceObject {
    private int life;
    private final Color color;

    // particles are used for simple explosion effects
    public Particle(double x, double y, Color color) {
        super(x, y, 2);
        Random r = new Random();
        this.velocity.x = -3 + r.nextDouble() * 6;
        this.velocity.y = -3 + r.nextDouble() * 6;
        this.life = 20 + r.nextInt(20);
        this.color = color;
    }

    @Override
    // particles drift out and fade away quickly
    public void update(GamePanel game) {
        position.add(velocity);
        velocity.multiply(0.95);
        life--;
        if (life <= 0) alive = false;
    }

    @Override
    // draws one small particle
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int) position.x, (int) position.y, 3, 3);
    }
}
