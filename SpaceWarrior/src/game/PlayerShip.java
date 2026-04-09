package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PlayerShip extends SpaceObject {
    private double angle;
    private int fireCooldown;
    private int respawnTimer;
    private int invincibleTimer;
    private double shieldEnergy = 100;
    private boolean shieldOn;
    private double boostEnergy = 100;
    private boolean boostOn;
    private BufferedImage shipImage;

    // sets up the player ship and loads its sprite
    public PlayerShip(double x, double y) {
        super(x, y, 16);
        angle = -Math.PI / 2;

        try {
            shipImage = ImageIO.read(getClass().getResource("/game/images/player_ship.png"));
        } catch (Exception e) {
            shipImage = null;
            System.out.println("Could not load player ship image");
        }
    }

    @Override
    // updates movement, shield use and shooting
    public void update(GamePanel game) {
        if (respawnTimer > 0) {
            respawnTimer--;
            invincibleTimer = 90;
            return;
        }

        if (game.isLeftPressed()) {
            angle -= 0.09;
        }
        if (game.isRightPressed()) {
            angle += 0.09;
        }
        double thrustAmount = 0.20;
        double maxSpeed = 6.0;

        if (boostOn) {
            thrustAmount = 0.30;
            maxSpeed = 9.0;
        }

        if (game.isUpPressed()) {
            Vector2D thrust = Vector2D.fromAngle(angle, thrustAmount);
            velocity.add(thrust);
        }

        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
        if (speed > maxSpeed) {
            velocity.x = (velocity.x / speed) * maxSpeed;
            velocity.y = (velocity.y / speed) * maxSpeed;
        }

        position.add(velocity);
        velocity.multiply(0.992);
        wrap(game.getWorldWidth(), game.getWorldHeight());

        if (fireCooldown > 0) {
            fireCooldown--;
        }
        if (invincibleTimer > 0) {
            invincibleTimer--;
        }

        shieldOn = game.isShiftPressed() && shieldEnergy > 1;
        boostOn = game.isControlPressed() && boostEnergy > 1;
        if (shieldOn) {
            shieldEnergy -= 0.7;
            if (shieldEnergy < 0) {
                shieldEnergy = 0;
            }
        } else {
            shieldEnergy += 0.18;
            if (shieldEnergy > 100) {
                shieldEnergy = 100;
            }
        }

        if (game.consumeShootPress() && fireCooldown == 0) {
            shoot(game);
            fireCooldown = 12;
        }
        if (boostOn) {
            boostEnergy -= 0.7;
            if (boostEnergy < 0) {
                boostEnergy = 0;
            }
        } else {
            boostEnergy += 0.18;
            if (boostEnergy > 100) {
                boostEnergy = 100;
            }
        }
    }

    // spawns a bullet from the front of the ship
    public void shoot(GamePanel game) {
        Vector2D dir = Vector2D.fromAngle(angle, 9);
        Bullet bullet = new Bullet(
                position.x + Math.cos(angle) * 20,
                position.y + Math.sin(angle) * 20,
                dir.x + velocity.x * 0.4,
                dir.y + velocity.y * 0.4,
                true
        );
        game.addBullet(bullet);
        SoundUtil.playShoot();
    }

    @Override
    // draws the ship sprite (incase image fails) and shield and boost effect
    public void draw(Graphics2D g) {
        if (respawnTimer > 0) {
            return;
        }
        if (invincibleTimer > 0 && invincibleTimer % 10 < 5) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(position.x, position.y);
        g2.rotate(angle + Math.PI / 2);

        if (shipImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int width = 62;
            int height = 62;
            g2.drawImage(shipImage, -width / 2, -height / 2, width, height, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(0, -18, -12, 12);
            g2.drawLine(-12, 12, 0, 6);
            g2.drawLine(0, 6, 12, 12);
            g2.drawLine(12, 12, 0, -18);
        }

        if (shieldOn) {
            g2.setColor(new Color(80, 200, 255, 140));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(-24, -24, 48, 48);
        }
        if (boostOn) {
            g2.setColor(new Color(255, 140, 0, 180));
            g2.fillOval(-6, 18, 12, 18);
        }
        g2.dispose();
    }

    public boolean canBeHit() {
        return respawnTimer == 0 && invincibleTimer == 0 && !shieldOn;
    }

    public boolean isShieldOn() {
        return shieldOn;
    }

    public double getShieldEnergy() {
        return shieldEnergy;
    }

    public void addShieldEnergy(double amount) {
        shieldEnergy += amount;
        if (shieldEnergy > 100) {
            shieldEnergy = 100;
        }
    }
    public double getBoostEnergy() {
        return boostEnergy;
    }

    public void addBoostEnergy(double amount) {
        boostEnergy += amount;
        if (boostEnergy > 100) {
            boostEnergy = 100;
        }
    }

    // puts the player back in the middle after losing a life
    public void respawn(int worldWidth, int worldHeight) {
        position.x = worldWidth / 2.0;
        position.y = worldHeight / 2.0;
        velocity.x = 0;
        velocity.y = 0;
        angle = -Math.PI / 2;
        respawnTimer = 45;
        invincibleTimer = 120;
        shieldEnergy = 100;
        boostEnergy = 100;
    }
}
