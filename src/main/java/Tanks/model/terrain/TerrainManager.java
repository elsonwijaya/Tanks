package Tanks.model.terrain;

import Tanks.view.display.TerrainDisplay;
import processing.core.PApplet;
import java.util.Arrays;
import java.util.Random;

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
        width = 0;
        for (String line : layout) {
            width = Math.max(width, line.length());
        }
        height = layout.length;

        // Initialize and store original layout first
        originalLayout = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(originalLayout[y], ' ');
            if (y < layout.length) {
                String line = layout[y];
                for (int x = 0; x < line.length(); x++) {
                    originalLayout[y][x] = line.charAt(x);
                }
            }
        }

        // Calculate scaled dimensions
        scaledWidth = width * CELLSIZE;
        scaledHeight = height * CELLHEIGHT;

        // Initialize scaled layout
        layoutScaled = new char[scaledHeight][scaledWidth];
        for (char[] row : layoutScaled) {
            Arrays.fill(row, ' ');
        }

        // Initial terrain placement
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length(); x++) {
                char ch = layout[y].charAt(x);
                if (ch == 'X') {
                    for (int i = y * CELLSIZE; i < scaledHeight; i++) {
                        layoutScaled[i][x * CELLSIZE] = 'X';
                    }
                }
            }
        }
    }

    public void smoothTerrain() {
        // First pass
        smoothingPass();
        // Second pass
        smoothingPass();
    }

    private void smoothingPass() {
        char[][] smooth = new char[scaledHeight][scaledWidth];

        // For each column
        for (int x = 0; x < scaledWidth; x++) {
            // Calculate average height from surrounding points
            int startX = Math.max(0, x - 16);
            int endX = Math.min(scaledWidth - 1, x + 16);
            int sum = 0;
            int count = 0;

            for (int wx = startX; wx <= endX; wx++) {
                int height = findGroundLevel(wx);
                if (height < scaledHeight) {
                    sum += height;
                    count++;
                }
            }

            // Calculate average and fill column
            if (count > 0) {
                int avgHeight = sum / count;
                for (int y = avgHeight; y < scaledHeight; y++) {
                    smooth[y][x] = 'X';
                }
            }
        }

        // Copy back
        layoutScaled = smooth;
    }

    private int findGroundLevel(int x) {
        for (int y = 0; y < scaledHeight; y++) {
            if (layoutScaled[y][x] == 'X') {
                return y;
            }
        }
        return scaledHeight;
    }

    public void placeTrees() {
        // Place trees from original layout
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
        // Add some randomness to tree position
        int treeOffset = random.nextInt(CELLSIZE);
        int finalX = baseX + treeOffset;

        // Find ground level at this position
        int groundY = findGroundLevel(finalX);

        // Place tree if position is valid
        if (groundY > 0 && groundY < scaledHeight) {
            layoutScaled[groundY - 1][finalX] = 'T';
        }
    }

    public void update() {
        // Handle any falling trees or terrain
        handleFallingObjects();
    }

    private void handleFallingObjects() {
        // Process from bottom to top to handle falling correctly
        for (int y = scaledHeight - 2; y >= 0; y--) {
            for (int x = 0; x < scaledWidth; x++) {
                // Check for floating trees
                if (layoutScaled[y][x] == 'T' && layoutScaled[y + 1][x] == ' ') {
                    // Move tree down
                    layoutScaled[y][x] = ' ';
                    layoutScaled[y + 1][x] = 'T';
                }
            }
        }
    }

    public void createExplosion(int centerX, int centerY, int radius) {
        // Bounds for the explosion area
        int minX = Math.max(0, centerX - radius);
        int maxX = Math.min(scaledWidth, centerX + radius);
        int minY = Math.max(0, centerY - radius);
        int maxY = Math.min(scaledHeight, centerY + radius);

        // Clear terrain within explosion radius
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (distance(x, y, centerX, centerY) <= radius) {
                    layoutScaled[y][x] = ' ';
                }
            }
        }

        // Apply physics after explosion
        applyPhysicsAfterExplosion(minX, maxX, minY, maxY);

        if (terrainDisplay != null) {
            terrainDisplay.notifyTerrainChanged();
        }
    }

    private void applyPhysicsAfterExplosion(int minX, int maxX, int minY, int maxY) {
        boolean changed;
        do {
            changed = false;
            // Process from bottom to top
            for (int y = maxY - 1; y >= minY; y--) {
                for (int x = minX; x < maxX; x++) {
                    // Handle falling terrain
                    if (y + 1 < scaledHeight && layoutScaled[y][x] == 'X' && layoutScaled[y + 1][x] == ' ') {
                        layoutScaled[y + 1][x] = 'X';
                        layoutScaled[y][x] = ' ';
                        changed = true;
                    }
                    // Handle falling trees
                    else if (y + 1 < scaledHeight && layoutScaled[y][x] == 'T' && layoutScaled[y + 1][x] == ' ') {
                        layoutScaled[y + 1][x] = 'T';
                        layoutScaled[y][x] = ' ';
                        changed = true;
                    }
                }
            }
        } while (changed); // Continue until no more changes occur
    }

    private float distance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // Helper methods used by other classes
    public boolean isPositionSolid(int x, int y) {
        if (x < 0 || x >= scaledWidth || y < 0 || y >= scaledHeight) {
            return false;
        }
        return layoutScaled[y][x] == 'X';
    }

    public float getGroundLevelAt(int x) {
        if (x < 0 || x >= scaledWidth) {
            return scaledHeight;
        }
        int y = findGroundLevel(x);
        return y - CELLHEIGHT;
    }

    // Getters
    public char[][] getLayoutScaled() { return layoutScaled; }
    public int getScaledWidth() { return scaledWidth; }
    public int getScaledHeight() { return scaledHeight; }
    public char[][] getOriginalLayout() {
        return originalLayout;
    }
}