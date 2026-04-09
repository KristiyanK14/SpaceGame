package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;

public class EnemyShip extends SpaceObject {
    private int changeDirTimer = 0;
    private int shootTimer = 80;
    private final Random random = new Random();
    private BufferedImage shipImage;

    // creates an enemy ship and loads its sprite
    public EnemyShip(double x, double y) {
        super(x, y, 18);
        velocity.x = 2;
        velocity.y = 1.5;

        try {
            shipImage = ImageIO.read(getClass().getResource("/game/images/enemy_ship.png"));
        } catch (Exception e) {
            shipImage = null;
            System.out.println("Could not load enemy ship image");
        }
    }

    @Override
    public void update(GamePanel game) {
        position.add(velocity);
        wrap(game.getWorldWidth(), game.getWorldHeight());

        changeDirTimer--;
        if (changeDirTimer <= 0) {
            velocity.x = -2.5 + random.nextDouble() * 5;
            velocity.y = -2.5 + random.nextDouble() * 5;
            changeDirTimer = 50 + random.nextInt(70);
        }

        shootTimer--;
        if (shootTimer <= 0 && game.getPlayer() != null) {
            PlayerShip player = game.getPlayer();
            double dx = game.getWrappedDX(player.getPosition().x, position.x);
            double dy = game.getWrappedDY(player.getPosition().y, position.y);
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len != 0) {
                dx /= len;
                dy /= len;
            }
            game.addBullet(new Bullet(position.x, position.y, dx * 5, dy * 5, false));
            SoundUtil.playEnemyShoot();
            shootTimer = 70 + random.nextInt(50);
        }
    }

    @Override
    // draws the enemy sprite (just incase the image of the enemy ship doesnt work)
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        double angle = Math.atan2(velocity.y, velocity.x) + Math.PI / 2;
        g2.translate(position.x, position.y);
        g2.rotate(angle);

        if (shipImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int width = 54;
            int height = 54;
            g2.drawImage(shipImage, -width / 2, -height / 2, width, height, null);
        } else {
            g2.setColor(new Color(255, 100, 100));
            g2.drawRect(-16, -8, 32, 16);
            g2.drawLine(-22, 0, -16, 0);
            g2.drawLine(16, 0, 22, 0);
            g2.drawOval(-6, -5, 12, 10);
        }
        g2.dispose();
    }
}
