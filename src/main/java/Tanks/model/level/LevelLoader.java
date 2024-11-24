package Tanks.model.level;

import processing.data.JSONArray;
import processing.data.JSONObject;
import java.util.*;

public class LevelLoader {
    public static class LoadedConfig {
        public final List<Level> levels;
        public final Map<Character, int[]> playerColors;

        public LoadedConfig(List<Level> levels, Map<Character, int[]> playerColors) {
            this.levels = levels;
            this.playerColors = playerColors;
        }
    }

    public static LoadedConfig loadConfig(JSONObject config) {
        List<Level> levels = loadLevels(config.getJSONArray("levels"));
        Map<Character, int[]> playerColors = loadPlayerColors(config.getJSONObject("player_colours"));
        return new LoadedConfig(levels, playerColors);
    }

    private static List<Level> loadLevels(JSONArray levelsJson) {
        List<Level> levels = new ArrayList<>();

        for (int i = 0; i < levelsJson.size(); i++) {
            JSONObject levelJson = levelsJson.getJSONObject(i);

            String layout = levelJson.getString("layout");
            String background = levelJson.getString("background");
            String foregroundColor = levelJson.getString("foreground-colour");
            String trees = levelJson.hasKey("trees") ? levelJson.getString("trees") : null;

            levels.add(new Level(layout, background, foregroundColor, trees));
        }

        return levels;
    }

    private static Map<Character, int[]> loadPlayerColors(JSONObject playerColorsJson) {
        Map<Character, int[]> playerColors = new HashMap<>();
        Random random = new Random();

        for (Object key : playerColorsJson.keys()) {
            String keyStr = (String) key;
            char playerChar = keyStr.charAt(0);
            String colorStr = playerColorsJson.getString(keyStr);

            int[] rgb;
            if (colorStr.equals("random")) {
                rgb = new int[]{random.nextInt(256), random.nextInt(256), random.nextInt(256)};
            } else {
                String[] parts = colorStr.split(",");
                rgb = new int[]{
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                };
            }

            playerColors.put(playerChar, rgb);
        }

        return playerColors;
    }
}