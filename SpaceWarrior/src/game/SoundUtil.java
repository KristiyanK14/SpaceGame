package game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SoundUtil {
    private static Clip menuClip;
    private static Clip gameClip;

    private static void playTone(int hz, int ms) {
        new Thread(() -> {
            try {
                float sampleRate = 8000f;
                int samples = (int) (ms * sampleRate / 1000.0);
                byte[] data = new byte[samples];
                for (int i = 0; i < data.length; i++) {
                    double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
                    data[i] = (byte) (Math.sin(angle) * 70);
                }

                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(data);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                Clip clip = AudioSystem.getClip();
                clip.open(format, out.toByteArray(), 0, out.size());
                clip.start();
            } catch (Exception ignored) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

    // helper methods for the main game sounds
    public static void playShoot() {
        playTone(700, 50);
    }

    public static void playEnemyShoot() {
        playTone(400, 70);
    }

    public static void playExplosion() {
        playTone(180, 120);
    }

    public static void playPickup() {
        playTone(900, 90);
    }

    // menu music loops while the player is on the main menu
    public static void playMenuMusic() {
        stopGameMusic();
        if (menuClip != null && menuClip.isRunning()) {
            return;
        }
        menuClip = startLoop("/game/sounds/menu.wav", -8.0f);
    }

    public static void stopMenuMusic() {
        stopClip(menuClip);
        menuClip = null;
    }

    // in game music loops quietly during play
    public static void playGameMusic() {
        stopMenuMusic();
        if (gameClip != null && gameClip.isRunning()) {
            return;
        }
        gameClip = startLoop("/game/sounds/game.wav", -18.0f);
    }

    public static void stopGameMusic() {
        stopClip(gameClip);
        gameClip = null;
    }

    private static Clip startLoop(String path, float volume) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(SoundUtil.class.getResource(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            try {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(volume);
            } catch (Exception e) {
                // ignore if not supported
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            return clip;
        } catch (Exception e) {
            System.out.println("Music failed to load: " + path);
            return null;
        }
    }

    private static void stopClip(Clip clip) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            System.out.println("Could not stop clip");
        }
    }
}
