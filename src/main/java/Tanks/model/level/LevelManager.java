package Tanks.model.level;

import processing.data.JSONArray;
import processing.data.JSONObject;
import java.util.*;

public class LevelManager {
    private List<Level> levels;
    private Map<Character, int[]> playerColors;
    private int currentLevelIndex;
    private String configPath;
    private Random random;

    public LevelManager(String configPath) {
        this.configPath = configPath;
        this.levels = new ArrayList<>();
        this.playerColors = new HashMap<>();
        this.currentLevelIndex = 0;
        this.random = new Random();
    }

    public void loadConfig(JSONObject config) {
        loadLevels(config.getJSONArray("levels"));
        loadPlayerColors(config.getJSONObject("player_colours"));
    }

    private void loadLevels(JSONArray levelsJson) {
        levels.clear();
        for (int i = 0; i < levelsJson.size(); i++) {
            JSONObject levelJson = levelsJson.getJSONObject(i);

            String layout = levelJson.getString("layout");
            String background = levelJson.getString("background");
            String foregroundColor = levelJson.getString("foreground-colour");

            // Trees are optional in level definition
            String trees = null;
            if (levelJson.hasKey("trees")) {
                trees = levelJson.getString("trees");
            }

            levels.add(new Level(layout, background, foregroundColor, trees));
        }
    }

    private void loadPlayerColors(JSONObject playerColorsJson) {
        playerColors.clear();
        for (Object key : playerColorsJson.keys()) {
            String keyStr = (String) key;
            char playerChar = keyStr.charAt(0);
            String colorValue = playerColorsJson.getString(keyStr);

            int[] rgb;
            if ("random".equals(colorValue)) {
                rgb = generateRandomColor();
            } else {
                rgb = parseColorString(colorValue);
            }

            playerColors.put(playerChar, rgb);
        }
    }

    private int[] generateRandomColor() {
        return new int[] {
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        };
    }

    private int[] parseColorString(String colorStr) {
        String[] parts = colorStr.split(",");
        return new int[] {
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
        };
    }

    // Level management methods
    public Level getCurrentLevel() {
        if (currentLevelIndex >= levels.size()) {
            return null;
        }
        return levels.get(currentLevelIndex);
    }

    public void nextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    public void reset() {
        currentLevelIndex = 0;
    }

    // Getters
    public Map<Character, int[]> getPlayerColors() {
        return Collections.unmodifiableMap(playerColors);
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getTotalLevels() {
        return levels.size();
    }

    // Color helper methods
    public int[] getPlayerColor(char playerChar) {
        return playerColors.getOrDefault(playerChar, new int[]{0, 0, 0});
    }

    public boolean isValidPlayer(char playerChar) {
        return playerColors.containsKey(playerChar);
    }
}