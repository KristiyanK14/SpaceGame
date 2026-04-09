package game;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class HighScoreTable {
    private final List<Integer> scores = new ArrayList<>();
    private final File file;

    // keeps the top scores and saves them to a text file
    public HighScoreTable(String fileName) {
        file = new File(fileName);
        load();
    }

    // loads scores from the text file
    public void load() {
        scores.clear();
        if (!file.exists()) {
            for (int i = 0; i < 5; i++) {
                scores.add(0);
            }
            save();
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextInt()) {
                scores.add(scanner.nextInt());
            }
        } catch (Exception e) {
            System.out.println("Could not load high scores");
        }

        while (scores.size() < 5) {
            scores.add(0);
        }
        Collections.sort(scores, Collections.reverseOrder());
    }

    // inserts a new score and trims the list
    public void addScore(int score) {
        scores.add(score);
        Collections.sort(scores, Collections.reverseOrder());
        while (scores.size() > 5) {
            scores.remove(scores.size() - 1);
        }
        save();
    }

    public int getTopScore() {
        if (scores.isEmpty()) {
            return 0;
        }
        return scores.get(0);
    }

    public List<Integer> getScores() {
        return scores;
    }

    private void save() {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (int value : scores) {
                writer.println(value);
            }
        } catch (Exception e) {
            System.out.println("Could not save high scores");
        }
    }
}
