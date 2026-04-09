package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

public class Asteroid extends SpaceObject {
    private final int size;
    private final int[] xPoints;
    private final int[] yPoints;
    private double angle;
    private double rotateSpeed;

    // makes a rough asteroid shape with a random movement
    public Asteroid(double x, double y, int size) {
        super(x, y, size == 3 ? 34 : size == 2 ? 22 : 14);
        this.size = size;
        Random r = new Random();
        velocity.x = -2 + r.nextDouble() * 4;
        velocity.y = -2 + r.nextDouble() * 4;
        rotateSpeed = -0.04 + r.nextDouble() * 0.08;
        xPoints = new int[10];
        yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double a = (Math.PI * 2 / 10) * i;
            double dist = radius * (0.7 + r.nextDouble() * 0.5);
            xPoints[i] = (int) (Math.cos(a) * dist);
            yPoints[i] = (int) (Math.sin(a) * dist);
        }
    }

    @Override
    // moves and rotates the asteroid each frame
    public void update(GamePanel game) {
        position.add(velocity);
        wrap(game.getWorldWidth(), game.getWorldHeight());
        angle += rotateSpeed;
    }

    @Override
    // draws the asteroid as a polygon shape
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(position.x, position.y);
        g2.rotate(angle);
        g2.setColor(Color.LIGHT_GRAY);
        g2.draw(new Polygon(xPoints, yPoints, xPoints.length));
        g2.dispose();
    }

    public int getSize() {
        return size;
    }
}
