package game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends SpaceObject {
    private int life = 75;
    private final boolean fromPlayer;

    // bullets disappear after a short time
    public Bullet(double x, double y, double vx, double vy, boolean fromPlayer) {
        super(x, y, 3);
        this.velocity.x = vx;
        this.velocity.y = vy;
        this.fromPlayer = fromPlayer;
    }

    @Override
    // moves the bullet and removes it after its life runs out
    public void update(GamePanel game) {
        position.add(velocity);
        wrap(game.getWorldWidth(), game.getWorldHeight());
        life--;
        if (life <= 0) alive = false;
    }

    @Override
    // draws a small bullet
    public void draw(Graphics2D g) {
        g.setColor(fromPlayer ? Color.YELLOW : Color.RED);
        g.fillOval((int) (position.x - radius), (int) (position.y - radius), (int) radius * 2, (int) radius * 2);
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }
}
