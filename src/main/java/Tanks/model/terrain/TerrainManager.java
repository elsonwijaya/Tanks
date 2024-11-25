package Tanks.model.terrain;

import Tanks.view.display.TerrainDisplay;
import processing.core.PApplet;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.HashMap;

public class TerrainManager {
    private static final int CELLSIZE = 32;
    private static final int CELLHEIGHT = 32;

    private char[][] layoutScaled;
    private String[] rawLayout;
    private char[][] originalLayout;
    private int width;
    private int height;
    private int scaledWidth;
    private int scaledHeight;
    private Random random;
    private TerrainDisplay terrainDisplay;

    public TerrainManager() {
        this.random = new Random();
    }

    public void setTerrainDisplay(TerrainDisplay display) {
        this.terrainDisplay = display;
    }

    public void loadTerrain(String[] layout) {
        if (layout == null) {
            System.err.println("Error: Layout data is null");
            return;
        }

        this.rawLayout = layout;

        // Calculate dimensions
        height = 0;
        for (String line : layout) {
            height = Math.max(height, line.length());
        }
        width = layout.length;

        // Store original layout
        originalLayout = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(originalLayout[x], ' ');
            if (x < layout.length) {
                String line = layout[x];
                for (int y = 0; y < line.length(); y++) {
                    originalLayout[x][y] = line.charAt(y);
                }
            }
        }

        // Calculate scaled dimensions
        scaledWidth = width * CELLSIZE;
        scaledHeight = height * CELLHEIGHT;
        System.out.println("Scaled dimensions will be: " + scaledWidth + "x" + scaledHeight);

        // Initialize scaled layout
        layoutScaled = new char[scaledWidth][scaledHeight];
        for (char[] row : layoutScaled) {
            Arrays.fill(row, ' ');
        }

        // Fill terrain
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length(); y++) {
                char ch = layout[x].charAt(y);
                if (ch == 'X') {
                    int baseX = x * CELLSIZE;
                    for (int i = baseX; i < layoutScaled.length; i++) {
                        int baseY = y * CELLHEIGHT;
                        for (int j = baseY; j < baseY + CELLHEIGHT && j < layoutScaled[0].length; j++) {
                            layoutScaled[i][j] = 'X';
                        }
                    }
                }
            }
        }

        // Place tank markers
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length(); y++) {
                char ch = layout[x].charAt(y);
                if (ch != ' ' && ch != 'X' && ch != 'T' && ch >= 'A' && ch <= 'Z') {
                    int tank_height = 0;
                    int scaledY = y * CELLHEIGHT;

                    for (int i = 0; i < layoutScaled.length; i++) {
                        if (scaledY < layoutScaled[0].length && layoutScaled[i][scaledY] == 'X') {
                            tank_height = i;
                            break;
                        }
                    }

                    if (tank_height > 0 && scaledY < layoutScaled[0].length) {
                        layoutScaled[tank_height - 1][scaledY] = ch;
                        System.out.println("Placed tank marker " + ch + " at " + scaledY + "," + (tank_height - 1));
                    }
                }
            }
        }
    }

    public void smoothTerrain() {
        smoothingPass();
        smoothingPass();
    }

    private void smoothingPass() {
        // Only store tank and tree markers, not terrain X's
        Map<Point, Character> markers = new HashMap<>();
        for (int x = 0; x < layoutScaled.length; x++) {
            for (int y = 0; y < layoutScaled[0].length; y++) {
                char ch = layoutScaled[x][y];
                if ((ch >= 'A' && ch <= 'Z' && ch != 'X') || ch == 'T') {  // Exclude 'X' markers
                    markers.put(new Point(x, y), ch);
                }
            }
        }

        // Perform smoothing
        char[][] smoothed = new char[scaledWidth][scaledHeight];
        for (char[] row : smoothed) {
            Arrays.fill(row, ' ');
        }

        for (int x = 0; x + 31 < smoothed[0].length; x++) {
            int sum = 0;
            for (int count = 0; count < 32 && (x + count) < smoothed[0].length; count++) {
                int y = 0;
                while (y < smoothed.length && layoutScaled[y][x + count] != 'X') {
                    y++;
                }
                sum += y;
            }
            int average = sum / 32;
            smoothed[average][x] = 'X';
            for (int y = average; y < smoothed.length; y++) {
                smoothed[y][x] = 'X';
            }
        }

        // Copy smoothed terrain
        for (int i = 0; i < smoothed.length; i++) {
            for (int j = 0; j + 31 < smoothed[i].length; j++) {
                layoutScaled[i][j] = smoothed[i][j];
            }
        }

        // Restore markers at their original positions
        for (Map.Entry<Point, Character> entry : markers.entrySet()) {
            Point p = entry.getKey();
            layoutScaled[p.x][p.y] = entry.getValue();

            // Only log tank markers (A-Z), not trees
            if (entry.getValue() >= 'A' && entry.getValue() <= 'Z' && entry.getValue() != 'X') {
                System.out.println("Restored tank marker " + entry.getValue() +
                        " at position " + p.x + "," + p.y);
            }
        }
    }

    private int findGroundLevel(int x) {
        // Bounds checking
        if (x < 0 || x >= layoutScaled[0].length) {
            return scaledHeight;
        }

        // Find first terrain block
        for (int y = 0; y < layoutScaled.length; y++) {
            if (layoutScaled[y][x] == 'X') {
                return y;
            }
        }
        return scaledHeight;
    }

    public boolean isPositionSolid(int x, int y) {
        // First check bounds
        if (x < 0 || x >= layoutScaled[0].length || y < 0 || y >= layoutScaled.length) {
            return false;
        }

        // Then check for terrain
        return layoutScaled[y][x] == 'X';
    }

    public float getGroundLevelAt(int x) {
        // Bounds checking
        if (x < 0 || x >= layoutScaled[0].length) {
            return scaledHeight;  // Return max height if out of bounds
        }

        // Find first solid ground from top
        for (int y = 0; y < layoutScaled.length; y++) {
            if (layoutScaled[y][x] == 'X') {
                // Return position just above ground
                return y - CELLHEIGHT;
            }
        }

        // If no ground found, return bottom of map
        return scaledHeight;
    }

    public void placeTrees() {
        for (int y = 0; y < rawLayout.length; y++) {
            for (int x = 0; x < rawLayout[y].length(); x++) {
                char cell = rawLayout[y].charAt(x);
                if (cell == 'T') {
                    placeTreeAtPosition(x * CELLSIZE);
                }
            }
        }
    }

    private void placeTreeAtPosition(int baseX) {
        if (baseX >= layoutScaled[0].length) return;

        int treeOffset = random.nextInt(CELLSIZE);
        int finalX = baseX + treeOffset;

        if (finalX >= layoutScaled[0].length) return;

        int groundY = findGroundLevel(finalX);
        if (groundY > 0 && groundY < layoutScaled.length) {
            layoutScaled[groundY - 1][finalX] = 'T';
        }
    }

    public void update() {
        handleFallingObjects();
    }

    private void handleFallingObjects() {
        for (int x = 0; x < layoutScaled.length; x++) {
            for (int y = layoutScaled[0].length - 2; y >= 0; y--) {
                if (layoutScaled[x][y] == 'T' && y + 1 < layoutScaled[0].length && layoutScaled[x][y + 1] == ' ') {
                    layoutScaled[x][y] = ' ';
                    layoutScaled[x][y + 1] = 'T';
                }
            }
        }
    }

    public void createExplosion(int centerX, int centerY, int radius) {
        // Store tree positions before explosion
        Map<Point, Character> treePositions = new HashMap<>();

        int minX = Math.max(0, centerX - radius);
        int maxX = Math.min(layoutScaled[0].length - 1, centerX + radius);
        int minY = Math.max(0, centerY - radius);
        int maxY = Math.min(layoutScaled.length - 1, centerY + radius);

        // Store tree positions before explosion
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (layoutScaled[y][x] == 'T') {
                    treePositions.put(new Point(x, y), 'T');
                }
            }
        }

        // Create explosion
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (distance(x, y, centerX, centerY) <= radius) {
                    layoutScaled[y][x] = ' ';
                }
            }
        }

        // Apply physics to terrain
        applyPhysicsAfterExplosion(minX, maxX, minY, maxY);

        // Reposition trees on new terrain
        for (Map.Entry<Point, Character> entry : treePositions.entrySet()) {
            Point p = entry.getKey();
            // Find new ground level for this tree
            int newGroundY = findGroundLevel(p.x);
            if (newGroundY > 0 && newGroundY < layoutScaled.length) {
                layoutScaled[newGroundY - 1][p.x] = 'T';
            }
        }

        if (terrainDisplay != null) {
            terrainDisplay.notifyTerrainChanged();
        }
    }

    private void applyPhysicsAfterExplosion(int minX, int maxX, int minY, int maxY) {
        boolean changed;
        do {
            changed = false;
            for (int x = minX; x < maxX; x++) {
                for (int y = maxY - 1; y >= minY; y--) {
                    // Handle terrain falling
                    if (y + 1 < layoutScaled.length) {
                        if (layoutScaled[y][x] == 'X' && layoutScaled[y + 1][x] == ' ') {
                            layoutScaled[y + 1][x] = layoutScaled[y][x];
                            layoutScaled[y][x] = ' ';
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        // Handle trees after terrain has settled
        for (int x = minX; x < maxX; x++) {
            for (int y = maxY - 1; y >= minY; y--) {
                if (layoutScaled[y][x] == 'T') {
                    int groundY = findGroundLevel(x);
                    if (groundY > y) {
                        layoutScaled[y][x] = ' ';
                        if (groundY - 1 < layoutScaled.length) {
                            layoutScaled[groundY - 1][x] = 'T';
                        }
                    }
                }
            }
        }
    }

    private float distance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public char[][] getLayoutScaled() { return layoutScaled; }
    public int getScaledWidth() { return scaledWidth; }
    public int getScaledHeight() { return scaledHeight; }
    public char[][] getOriginalLayout() { return originalLayout; }

    // Helper class for storing coordinates
    private static class Point {
        final int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public void printTerrainStatus(float x, float y) {
        int ix = (int)x;
        int iy = (int)y;
        System.out.println("Checking position: " + ix + "," + iy);
        if (ix >= 0 && ix < layoutScaled[0].length && iy >= 0 && iy < layoutScaled.length) {
            System.out.println("Terrain at position: " + layoutScaled[iy][ix]);
            System.out.println("Ground level at x=" + ix + ": " + getGroundLevelAt(ix));
        } else {
            System.out.println("Position out of bounds");
        }
    }
}

