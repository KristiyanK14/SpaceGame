package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameLauncher {
    // starts the window and launches the game panel
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Space Warrior");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.add(new GamePanel());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
