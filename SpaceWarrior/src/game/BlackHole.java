package game;

import java.awt.Color;
import java.awt.Graphics2D;

public class BlackHole extends SpaceObject {
    private int pulse = 0;

    // black holes sit in the map and can warp the player
    public BlackHole(double x, double y) {
        super(x, y, 24);
    }

    @Override
    public void update(GamePanel game) {
        pulse++;
    }

    @Override
    // draws a simple black hole effect
    public void draw(Graphics2D g) {
        int wobble = pulse % 16;
        g.setColor(new Color(110, 0, 180, 130));
        g.fillOval((int) position.x - 28 - wobble / 4, (int) position.y - 28 - wobble / 4, 56 + wobble / 2, 56 + wobble / 2);
        g.setColor(Color.BLACK);
        g.fillOval((int) position.x - 22, (int) position.y - 22, 44, 44);
        g.setColor(new Color(200, 120, 255));
        g.drawOval((int) position.x - 28, (int) position.y - 28, 56, 56);
        g.drawOval((int) position.x - 32, (int) position.y - 32, 64, 64);
    }
}
