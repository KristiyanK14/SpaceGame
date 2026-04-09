package game;

import java.awt.Graphics2D;

public abstract class SpaceObject {
    protected Vector2D position;
    protected Vector2D velocity;
    protected double radius;
    protected boolean alive = true;

    // base object class shared by ships, bullets and other world objects
    public SpaceObject(double x, double y, double radius) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.radius = radius;
    }

    public abstract void update(GamePanel game);

    public abstract void draw(Graphics2D g);

    // radius collision check using wrapped distance
    public boolean collidesWith(SpaceObject other, GamePanel game) {
        double dx = position.x - other.position.x;
        double dy = position.y - other.position.y;

        double worldWidth = game.getWorldWidth();
        double worldHeight = game.getWorldHeight();

        if (dx > worldWidth / 2.0) dx -= worldWidth;
        if (dx < -worldWidth / 2.0) dx += worldWidth;
        if (dy > worldHeight / 2.0) dy -= worldHeight;
        if (dy < -worldHeight / 2.0) dy += worldHeight;

        return Math.sqrt(dx * dx + dy * dy) < radius + other.radius;
    }

    public void wrap(int width, int height) {
        if (position.x < 0) position.x += width;
        if (position.x >= width) position.x -= width;
        if (position.y < 0) position.y += height;
        if (position.y >= height) position.y -= height;
    }

    public Vector2D getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
