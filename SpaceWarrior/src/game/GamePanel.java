package game;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int width = 1000;
    private final int height = 700;
    private final int worldWidth = 2400;
    private final int worldHeight = 1800;

    private final Timer timer;
    private final Random random = new Random();

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Asteroid> asteroids = new ArrayList<>();
    private final List<EnemyShip> enemies = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<BlackHole> blackHoles = new ArrayList<>();
    private final List<Vector2D> starPositions = new ArrayList<>();

    private final HighScoreTable highScoreTable = new HighScoreTable("highscores.txt");
    private final GameView view;

    private PlayerShip player;
    private BufferedImage heartImage;

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean spacePressed;
    private boolean shiftPressed;
    private boolean controlPressed;
    private boolean shootPressed;

    private int score;
    private int lives;
    private int level;
    private int pickupSpawnTimer;
    private int blackHoleSpawnTimer;
    private int blackHoleTeleportCooldown;
    private int menuChoice;
    private int pauseChoice;
    private int damageFlashTimer;
    private boolean scoreSaved;

    private GameState gameState = GameState.MENU;

    public GamePanel() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        view = new GameView(this);
        makeStars();
        loadImages();
        resetRoundValues();

        SoundUtil.playMenuMusic();
        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        try {
            heartImage = ImageIO.read(getClass().getResource("/game/images/heart.png"));
        } catch (Exception e) {
            heartImage = null;
            System.out.println("Could not load heart image");
        }
        view.setHeartImage(heartImage);
    }

    // makes a set of background stars for the world
    private void makeStars() {
        starPositions.clear();
        for (int i = 0; i < 260; i++) {
            starPositions.add(new Vector2D(random.nextInt(worldWidth), random.nextInt(worldHeight)));
        }
    }

    private void resetRoundValues() {
        score = 0;
        lives = 3;
        level = 1;
        pickupSpawnTimer = 500;
        blackHoleSpawnTimer = 850;
        blackHoleTeleportCooldown = 0;
        damageFlashTimer = 0;
        scoreSaved = false;
    }

    private void clearObjects() {
        bullets.clear();
        asteroids.clear();
        enemies.clear();
        pickups.clear();
        particles.clear();
        blackHoles.clear();
    }

    // resets everything ready for a fresh game
    private void startNewGame() {
        clearObjects();
        resetRoundValues();
        player = new PlayerShip(worldWidth / 2.0, worldHeight / 2.0);
        pauseChoice = 0;
        gameState = GameState.PLAYING;
        spawnLevel();
        SoundUtil.playGameMusic();
    }

    // sends the player back to the main menu
    private void goToMenu() {
        gameState = GameState.MENU;
        menuChoice = 0;
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        spacePressed = false;
        shiftPressed = false;
        controlPressed = false;
        shootPressed = false;
        SoundUtil.playMenuMusic();
    }

    // sets up a new level with asteroids and enemies
    private void spawnLevel() {
        int asteroidCount = 4 + level * 2;
        int enemyCount = Math.max(0, level - 1);


        if (level >= 3) {
            enemyCount++;
        }

        for (int i = 0; i < asteroidCount; i++) {
            spawnAsteroidAwayFromPlayer(3);
        }
        for (int i = 0; i < enemyCount; i++) {
            enemies.add(new EnemyShip(random.nextInt(worldWidth), random.nextInt(worldHeight)));
        }

        if (random.nextBoolean()) {
            spawnBlackHole();
        }
    }

    // spawns an asteroid a bit away from the player
    private void spawnAsteroidAwayFromPlayer(int size) {
        double x;
        double y;
        do {
            x = random.nextInt(worldWidth);
            y = random.nextInt(worldHeight);
        } while (player != null && wrappedDistance(player.getPosition().x, player.getPosition().y, x, y) < 240);
        asteroids.add(new Asteroid(x, y, size));
    }

    // spawns a black hole somewhere safe in the map
    private void spawnBlackHole() {
        double x;
        double y;
        do {
            x = 80 + random.nextInt(worldWidth - 160);
            y = 80 + random.nextInt(worldHeight - 160);
        } while (player != null && wrappedDistance(player.getPosition().x, player.getPosition().y, x, y) < 260);
        blackHoles.add(new BlackHole(x, y));
    }

    @Override
    // timer loop that keeps the game updating
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updateGame();
        }
        repaint();
    }

    // updates the main game logic each frame
    private void updateGame() {
        if (damageFlashTimer > 0) {
            damageFlashTimer--;
        }
        if (blackHoleTeleportCooldown > 0) {
            blackHoleTeleportCooldown--;
        }

        updateObjects();
        handleCollisions();
        removeDeadObjects();
        spawnPickupsSometimes();
        spawnBlackHoleSometimes();

        if (asteroids.isEmpty() && enemies.isEmpty()) {
            level++;
            spawnLevel();
        }

        // only shoot once per key press
        shootPressed = false;
    }

    private void updateObjects() {
        if (player != null) {
            player.update(this);
        }
        for (Bullet bullet : bullets) {
            bullet.update(this);
        }
        for (Asteroid asteroid : asteroids) {
            asteroid.update(this);
        }
        for (EnemyShip enemy : enemies) {
            enemy.update(this);
        }
        for (Pickup pickup : pickups) {
            pickup.update(this);
        }
        for (Particle particle : particles) {
            particle.update(this);
        }
        for (BlackHole blackHole : blackHoles) {
            blackHole.update(this);
        }
    }

    // occasionally drops pickups into the world
    private void spawnPickupsSometimes() {
        pickupSpawnTimer--;
        if (pickupSpawnTimer > 0) {
            return;
        }

        int roll = random.nextInt(3);
        Pickup.Type type = Pickup.Type.SCORE;
        if (roll == 1) {
            type = Pickup.Type.SHIELD;
        } else if (roll == 2) {
            type = Pickup.Type.LIFE;
        }

        pickups.add(new Pickup(60 + random.nextInt(worldWidth - 120), 60 + random.nextInt(worldHeight - 120), type));
        pickupSpawnTimer = 550 + random.nextInt(300);
    }

    // occasionally adds another black hole
    private void spawnBlackHoleSometimes() {
        blackHoleSpawnTimer--;
        if (blackHoleSpawnTimer <= 0 && blackHoles.size() < 3) {
            spawnBlackHole();
            blackHoleSpawnTimer = 900 + random.nextInt(500);
        }
    }

    // handles the main collision checks
    private void handleCollisions() {
        handleBulletCollisions();
        handleShipCollisions();
        handlePickupCollisions();
        handleBlackHoleCollisions();
    }

    private void handleBulletCollisions() {
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) {
                continue;
            }

            if (bullet.isFromPlayer()) {
                for (Asteroid asteroid : asteroids) {
                    if (asteroid.isAlive() && bullet.collidesWith(asteroid, this)) {
                        bullet.setAlive(false);
                        breakAsteroid(asteroid);
                        break;
                    }
                }
                for (EnemyShip enemy : enemies) {
                    if (enemy.isAlive() && bullet.collidesWith(enemy, this)) {
                        bullet.setAlive(false);
                        enemy.setAlive(false);
                        score += 25;
                        makeExplosion(enemy.getPosition().x, enemy.getPosition().y, Color.RED, 18);
                        SoundUtil.playExplosion();
                    }
                }
            } else {
                if (player != null && player.canBeHit() && bullet.collidesWith(player, this)) {
                    bullet.setAlive(false);
                    playerHit();
                } else if (player != null && player.isShieldOn() && bullet.collidesWith(player, this)) {
                    bullet.setAlive(false);
                }
            }
        }
    }

    private void handleShipCollisions() {
        for (Asteroid asteroid : asteroids) {
            if (!asteroid.isAlive()) {
                continue;
            }
            if (player != null && player.canBeHit() && asteroid.collidesWith(player, this)) {
                asteroid.setAlive(false);
                playerHit();
                makeExplosion(asteroid.getPosition().x, asteroid.getPosition().y, Color.GRAY, 16);
            } else if (player != null && player.isShieldOn() && asteroid.collidesWith(player, this)) {
                asteroid.setAlive(false);
                score += 5;
                makeExplosion(asteroid.getPosition().x, asteroid.getPosition().y, Color.CYAN, 12);
            }
        }

        for (EnemyShip enemy : enemies) {
            if (enemy.isAlive() && player != null && player.canBeHit() && enemy.collidesWith(player, this)) {
                enemy.setAlive(false);
                playerHit();
                makeExplosion(enemy.getPosition().x, enemy.getPosition().y, Color.RED, 18);
            }
        }
    }

    private void handlePickupCollisions() {
        for (Pickup pickup : pickups) {
            if (pickup.isAlive() && player != null && pickup.collidesWith(player, this)) {
                pickup.setAlive(false);
                applyPickup(pickup);
            }
        }
    }

    private void handleBlackHoleCollisions() {
        for (BlackHole blackHole : blackHoles) {
            if (player != null && blackHoleTeleportCooldown == 0 && blackHole.collidesWith(player, this)) {
                teleportPlayer();
                blackHoleTeleportCooldown = 120;
                break;
            }
        }
    }

    // teleports the player after touching a black hole
    private void teleportPlayer() {
        if (player == null) {
            return;
        }

        makeExplosion(player.getPosition().x, player.getPosition().y, new Color(170, 80, 255), 20);
        player.getPosition().x = 100 + random.nextInt(worldWidth - 200);
        player.getPosition().y = 100 + random.nextInt(worldHeight - 200);
        makeExplosion(player.getPosition().x, player.getPosition().y, new Color(170, 80, 255), 20);
        SoundUtil.playPickup();
    }

    // breaks large asteroids into smaller ones
    private void breakAsteroid(Asteroid asteroid) {
        asteroid.setAlive(false);

        int size = asteroid.getSize();
        if (size == 3) {
            score += 10;
        } else if (size == 2) {
            score += 20;
        } else {
            score += 30;
        }

        makeExplosion(asteroid.getPosition().x, asteroid.getPosition().y, Color.LIGHT_GRAY, 12);
        SoundUtil.playExplosion();

        if (size > 1) {
            asteroids.add(new Asteroid(asteroid.getPosition().x + 5, asteroid.getPosition().y + 5, size - 1));
            asteroids.add(new Asteroid(asteroid.getPosition().x - 5, asteroid.getPosition().y - 5, size - 1));
        }

        if (random.nextDouble() < 0.18) {
            pickups.add(new Pickup(asteroid.getPosition().x, asteroid.getPosition().y, Pickup.Type.SCORE));
        }
    }

    // makes a small burst of particles
    private void makeExplosion(double x, double y, Color color, int amount) {
        for (int i = 0; i < amount; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    // applies the effect of the pickup collected
    private void applyPickup(Pickup pickup) {
        if (pickup.getType() == Pickup.Type.SCORE) {
            score += 50;
        } else if (pickup.getType() == Pickup.Type.SHIELD) {
            for (int i = 0; i < 40; i++) {
                particles.add(new Particle(pickup.getPosition().x, pickup.getPosition().y, Color.CYAN));
            }
            if (player != null) {
                player.addShieldEnergy(45);
                player.addBoostEnergy(45);
            }
        } else if (pickup.getType() == Pickup.Type.LIFE) {
            lives++;
        }
        SoundUtil.playPickup();
    }

    // handles damage taken by the player
    private void playerHit() {
        if (player == null) {
            return;
        }

        lives--;
        damageFlashTimer = 10; // quick flash so damage taken is more noticable
        makeExplosion(player.getPosition().x, player.getPosition().y, Color.ORANGE, 24);
        SoundUtil.playExplosion();

        if (lives <= 0) {
            gameState = GameState.GAME_OVER;
            checkHighScore();
            SoundUtil.stopGameMusic();
        } else {
            player.respawn(worldWidth, worldHeight);
        }
    }

    // clears out objects that are no longer active
    private void removeDeadObjects() {
        bullets.removeIf(b -> !b.isAlive());
        asteroids.removeIf(a -> !a.isAlive());
        enemies.removeIf(e -> !e.isAlive());
        pickups.removeIf(p -> !p.isAlive());
        particles.removeIf(p -> !p.isAlive());
        blackHoles.removeIf(b -> !b.isAlive());
    }

    // checks whether the score should go into the table
    private void checkHighScore() {
        if (scoreSaved) {
            return;
        }
        highScoreTable.addScore(score);
        scoreSaved = true;
    }

    @Override
    // hands the drawing over to the game view
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        view.draw((Graphics2D) g);
    }

    // wrapped distance is used because the world loops around
    public double wrappedDistance(double x1, double y1, double x2, double y2) {
        double dx = getWrappedDX(x1, x2);
        double dy = getWrappedDY(y1, y2);
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getWrappedDX(double targetX, double originX) {
        double dx = targetX - originX;
        if (dx > worldWidth / 2.0) {
            dx -= worldWidth;
        }
        if (dx < -worldWidth / 2.0) {
            dx += worldWidth;
        }
        return dx;
    }

    public double getWrappedDY(double targetY, double originY) {
        double dy = targetY - originY;
        if (dy > worldHeight / 2.0) {
            dy -= worldHeight;
        }
        if (dy < -worldHeight / 2.0) {
            dy += worldHeight;
        }
        return dy;
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public boolean consumeShootPress() {
        if (shootPressed) {
            shootPressed = false;
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    // handles the main keyboard input
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState == GameState.MENU) {
            handleMenuInput(key);
            return;
        }
        if (gameState == GameState.PAUSED) {
            handlePauseInput(key);
            return;
        }
        if (gameState == GameState.GAME_OVER) {
            handleGameOverInput(key);
            return;
        }

        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            if (!spacePressed) {
                shootPressed = true;
            }
            spacePressed = true;
        }
        if (key == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        }
        if (key == KeyEvent.VK_CONTROL) {
            controlPressed = true;
        }
        if (key == KeyEvent.VK_R) {
            startNewGame();
        }
        if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
            gameState = GameState.PAUSED;
            pauseChoice = 0;
            SoundUtil.stopGameMusic();
            leftPressed = false;
            rightPressed = false;
            upPressed = false;
            spacePressed = false;
            shiftPressed = false;
            controlPressed = false;
            shootPressed = false;
        }
    }

    private void handleMenuInput(int key) {
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            menuChoice--;
            if (menuChoice < 0) {
                menuChoice = 1;
            }
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            menuChoice++;
            if (menuChoice > 1) {
                menuChoice = 0;
            }
        }
        if (key == KeyEvent.VK_ENTER) {
            if (menuChoice == 0) {
                startNewGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void handlePauseInput(int key) {
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            pauseChoice--;
            if (pauseChoice < 0) {
                pauseChoice = 2;
            }
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            pauseChoice++;
            if (pauseChoice > 2) {
                pauseChoice = 0;
            }
        }
        if (key == KeyEvent.VK_ENTER) {
            if (pauseChoice == 0) {
                gameState = GameState.PLAYING;
                SoundUtil.playGameMusic();
            } else if (pauseChoice == 1) {
                goToMenu();
            } else {
                System.exit(0);
            }
        }
        if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
            gameState = GameState.PLAYING;
            SoundUtil.playGameMusic();
        }
    }

    private void handleGameOverInput(int key) {
        if (key == KeyEvent.VK_R) {
            startNewGame();
        }
        if (key == KeyEvent.VK_ESCAPE) {
            goToMenu();
        }
    }

    @Override
    // clears key states when keys are released
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        if (key == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (key == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
        if (key == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
        if (key == KeyEvent.VK_CONTROL) {
            controlPressed = false;
        }
    }

    int getScreenWidth() {
        return width;
    }

    int getScreenHeight() {
        return height;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isUpPressed() {
        return upPressed;
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }

    public boolean isControlPressed() {
        return controlPressed;
    }

    GameState getGameState() {
        return gameState;
    }

    int getScore() {
        return score;
    }

    int getLives() {
        return lives;
    }

    int getLevel() {
        return level;
    }

    int getMenuChoice() {
        return menuChoice;
    }

    int getPauseChoice() {
        return pauseChoice;
    }

    int getDamageFlashTimer() {
        return damageFlashTimer;
    }

    int getTopScore() {
        return highScoreTable.getTopScore();
    }

    List<Integer> getHighScores() {
        return highScoreTable.getScores();
    }

    List<Bullet> getBullets() {
        return bullets;
    }

    List<Asteroid> getAsteroids() {
        return asteroids;
    }

    List<EnemyShip> getEnemies() {
        return enemies;
    }

    List<Pickup> getPickups() {
        return pickups;
    }

    List<Particle> getParticles() {
        return particles;
    }

    List<BlackHole> getBlackHoles() {
        return blackHoles;
    }

    List<Vector2D> getStarPositions() {
        return starPositions;
    }
}
