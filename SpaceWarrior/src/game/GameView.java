package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class GameView {
    private final GamePanel game;
    private BufferedImage heartImage;

    public GameView(GamePanel game) {
        this.game = game;
    }

    public void setHeartImage(BufferedImage heartImage) {
        this.heartImage = heartImage;
    }

    // draws whichever screen should be shown at the time
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (game.getGameState() == GameState.MENU) {
            drawMenu(g2);
            return;
        }

        drawWorld(g2);
        drawHud(g2);
        drawMiniMap(g2);

        if (game.getGameState() == GameState.PAUSED) {
            drawPauseOverlay(g2);
        }
        if (game.getGameState() == GameState.GAME_OVER) {
            drawGameOver(g2);
        }
        if (game.getDamageFlashTimer() > 0) {
            g2.setColor(new Color(255, 0, 0, 70));
            g2.fillRect(0, 0, game.getScreenWidth(), game.getScreenHeight());
        }
    }

    // draws the main menu and score list
    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, game.getScreenWidth(), game.getScreenHeight());
        drawBackgroundStars(g2, game.getScreenWidth() / 2.0, game.getScreenHeight() / 2.0, 1.0);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 44));
        g2.drawString("SPACE WARRIOR", game.getScreenWidth() / 2 - 150, 120);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Arrow keys move   Space shoots   Shift uses shield , Ctrl uses boost", game.getScreenWidth() / 2 - 215, 175);
        g2.drawString("Black holes can warp you somewhere else in the map", game.getScreenWidth() / 2 - 220, 205);

        g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.setColor(game.getMenuChoice() == 0 ? Color.CYAN : Color.WHITE);
        g2.drawString("Start Game", game.getScreenWidth() / 2 - 78, 285);
        g2.setColor(game.getMenuChoice() == 1 ? Color.CYAN : Color.WHITE);
        g2.drawString("Quit", game.getScreenWidth() / 2 - 28, 330);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("High Scores", game.getScreenWidth() / 2 - 70, 410);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        for (int i = 0; i < game.getHighScores().size(); i++) {
            g2.drawString((i + 1) + ".  " + game.getHighScores().get(i), game.getScreenWidth() / 2 - 35, 450 + (i * 28));
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Use up/down and press Enter", game.getScreenWidth() / 2 - 110, 640);
    }

    // draws the pause overlay on top of the game
    private void drawPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, game.getScreenWidth(), game.getScreenHeight());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("PAUSED", game.getScreenWidth() / 2 - 90, 220);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(game.getPauseChoice() == 0 ? Color.CYAN : Color.WHITE);
        g2.drawString("Resume", game.getScreenWidth() / 2 - 55, 320);
        g2.setColor(game.getPauseChoice() == 1 ? Color.CYAN : Color.WHITE);
        g2.drawString("Main Menu", game.getScreenWidth() / 2 - 72, 370);
        g2.setColor(game.getPauseChoice() == 2 ? Color.CYAN : Color.WHITE);
        g2.drawString("Quit", game.getScreenWidth() / 2 - 28, 420);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Use up/down and Enter", game.getScreenWidth() / 2 - 85, 490);
    }

    // draws the game over screen
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 185));
        g2.fillRect(0, 0, game.getScreenWidth(), game.getScreenHeight());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("GAME OVER", game.getScreenWidth() / 2 - 130, game.getScreenHeight() / 2 - 40);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.drawString("Press R to restart", game.getScreenWidth() / 2 - 90, game.getScreenHeight() / 2 + 5);
        g2.drawString("Press ESC for menu", game.getScreenWidth() / 2 - 98, game.getScreenHeight() / 2 + 35);
    }

    // draws the scrolling game world around the player
    private void drawWorld(Graphics2D g2) {
        if (game.getPlayer() == null) {
            return;
        }

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, game.getScreenWidth(), game.getScreenHeight());

        drawBackgroundStars(g2, game.getPlayer().getPosition().x, game.getPlayer().getPosition().y, 0.35);
        drawSectorLines(g2);

        for (BlackHole blackHole : game.getBlackHoles()) {
            drawObjectRelative(g2, blackHole);
        }
        for (Pickup pickup : game.getPickups()) {
            drawObjectRelative(g2, pickup);
        }
        for (Particle particle : game.getParticles()) {
            drawObjectRelative(g2, particle);
        }
        for (Asteroid asteroid : game.getAsteroids()) {
            drawObjectRelative(g2, asteroid);
        }
        for (EnemyShip enemy : game.getEnemies()) {
            drawObjectRelative(g2, enemy);
        }
        for (Bullet bullet : game.getBullets()) {
            drawObjectRelative(g2, bullet);
        }
        drawObjectRelative(g2, game.getPlayer());
    }

    // simple star background with a small effect
    private void drawBackgroundStars(Graphics2D g2, double camX, double camY, double parallax) {
        g2.setColor(Color.WHITE);
        for (Vector2D star : game.getStarPositions()) {
            double dx = game.getWrappedDX(star.x, camX) * parallax;
            double dy = game.getWrappedDY(star.y, camY) * parallax;
            int sx = (int) (game.getScreenWidth() / 2.0 + dx);
            int sy = (int) (game.getScreenHeight() / 2.0 + dy);
            if (sx >= -2 && sx <= game.getScreenWidth() + 2 && sy >= -2 && sy <= game.getScreenHeight() + 2) {
                g2.fillRect(sx, sy, 2, 2);
            }
        }
    }

    // draws faint sector lines so the world feels less empty
    private void drawSectorLines(Graphics2D g2) {
        g2.setColor(new Color(50, 50, 90, 80));
        g2.setStroke(new BasicStroke(1f));

        int sectorSize = 300;
        for (int x = 0; x <= game.getWorldWidth(); x += sectorSize) {
            double dx = game.getWrappedDX(x, game.getPlayer().getPosition().x);
            int sx = (int) (game.getScreenWidth() / 2.0 + dx);
            if (sx >= -sectorSize && sx <= game.getScreenWidth() + sectorSize) {
                g2.drawLine(sx, 0, sx, game.getScreenHeight());
            }
        }

        for (int y = 0; y <= game.getWorldHeight(); y += sectorSize) {
            double dy = game.getWrappedDY(y, game.getPlayer().getPosition().y);
            int sy = (int) (game.getScreenHeight() / 2.0 + dy);
            if (sy >= -sectorSize && sy <= game.getScreenHeight() + sectorSize) {
                g2.drawLine(0, sy, game.getScreenWidth(), sy);
            }
        }
    }

    // draws world objects relative to the player position
    private void drawObjectRelative(Graphics2D g2, SpaceObject obj) {
        if (game.getPlayer() == null) {
            return;
        }

        double dx = game.getWrappedDX(obj.getPosition().x, game.getPlayer().getPosition().x);
        double dy = game.getWrappedDY(obj.getPosition().y, game.getPlayer().getPosition().y);
        double screenX = game.getScreenWidth() / 2.0 + dx;
        double screenY = game.getScreenHeight() / 2.0 + dy;

        if (screenX < -90 || screenX > game.getScreenWidth() + 90 || screenY < -90 || screenY > game.getScreenHeight() + 90) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        copy.translate(screenX - obj.getPosition().x, screenY - obj.getPosition().y);
        obj.draw(copy);
        copy.dispose();
    }

    // draws the on screen info such as score, lives and shield
    private void drawHud(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Score: " + game.getScore(), 15, 25);
        g2.drawString("Lives:", 15, 52);
        drawHearts(g2, 78, 32);
        g2.drawString("Level: " + game.getLevel(), 15, 85);
        g2.drawString("High Score: " + game.getTopScore(), 15, 110);

        if (game.getPlayer() != null) {
            int barX = game.getScreenWidth() - 170;
            int shieldY = 15;
            int boostY = 42;
            int barW = 140;
            int barH = 18;

            g2.drawString("Shield", game.getScreenWidth() - 230, 30);
            g2.drawRect(barX, shieldY, barW, barH);
            g2.setColor(Color.CYAN);
            g2.fillRect(barX + 1, shieldY + 1, (int) (game.getPlayer().getShieldEnergy() * 1.38), 16);

            g2.setColor(Color.WHITE);
            g2.drawString("Boost", game.getScreenWidth() - 226, 57);
            g2.drawRect(barX, boostY, barW, barH);
            g2.setColor(new Color(255, 140, 0));
            g2.fillRect(barX + 1, boostY + 1, (int) (game.getPlayer().getBoostEnergy() * 1.38), 16);

            g2.setColor(Color.WHITE);
        }
    }

    // draws the heart icons for the lives display
    private void drawHearts(Graphics2D g2, int startX, int y) {
        int size = 22;
        for (int i = 0; i < Math.min(game.getLives(), 6); i++) {
            int x = startX + i * 26;
            if (heartImage != null) {
                g2.drawImage(heartImage, x, y, size, size, null);
            } else { // incase the image doesnt work
                g2.setColor(Color.RED);
                g2.fillOval(x, y, 10, 10);
                g2.fillOval(x + 8, y, 10, 10);
                int[] xs = {x - 1, x + 19, x + 9};
                int[] ys = {y + 7, y + 7, y + 20};
                g2.fillPolygon(xs, ys, 3);
            }
        }
        if (game.getLives() > 6) {
            g2.setColor(Color.WHITE);
            g2.drawString("x" + game.getLives(), startX + 6 * 26, y + 17);
        }
    }

    // draws the mini map in the corner
    private void drawMiniMap(Graphics2D g2) {
        int mapW = 200;
        int mapH = 140;
        int mapX = game.getScreenWidth() - mapW - 18;
        int mapY = game.getScreenHeight() - mapH - 18;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(mapX, mapY, mapW, mapH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(mapX, mapY, mapW, mapH, 12, 12);
        g2.drawString("Mini Map", mapX + 60, mapY - 8);

        for (Asteroid asteroid : game.getAsteroids()) {
            drawMiniMapDot(g2, asteroid.getPosition().x, asteroid.getPosition().y, Color.LIGHT_GRAY, mapX, mapY, mapW, mapH, 4);
        }
        for (EnemyShip enemy : game.getEnemies()) {
            drawMiniMapDot(g2, enemy.getPosition().x, enemy.getPosition().y, Color.RED, mapX, mapY, mapW, mapH, 4);
        }
        for (Pickup pickup : game.getPickups()) {
            Color c = Color.GREEN;
            if (pickup.getType() == Pickup.Type.SHIELD) {
                c = Color.CYAN;
            }
            if (pickup.getType() == Pickup.Type.LIFE) {
                c = Color.PINK;
            }
            drawMiniMapDot(g2, pickup.getPosition().x, pickup.getPosition().y, c, mapX, mapY, mapW, mapH, 4);
        }
        for (BlackHole blackHole : game.getBlackHoles()) {
            drawMiniMapDot(g2, blackHole.getPosition().x, blackHole.getPosition().y, new Color(190, 100, 255), mapX, mapY, mapW, mapH, 5);
        }
        if (game.getPlayer() != null) {
            drawMiniMapDot(g2, game.getPlayer().getPosition().x, game.getPlayer().getPosition().y, Color.YELLOW, mapX, mapY, mapW, mapH, 6);
        }
    }

    // draws one marker on the mini map
    private void drawMiniMapDot(Graphics2D g2, double x, double y, Color color, int mapX, int mapY, int mapW, int mapH, int size) {
        int dotX = mapX + (int) ((x / game.getWorldWidth()) * mapW);
        int dotY = mapY + (int) ((y / game.getWorldHeight()) * mapH);
        g2.setColor(color);
        g2.fillOval(dotX - size / 2, dotY - size / 2, size, size);
    }
}
