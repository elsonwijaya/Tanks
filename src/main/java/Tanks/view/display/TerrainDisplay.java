package Tanks.view.display;

import Tanks.model.level.Level;
import Tanks.model.terrain.TerrainManager;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class TerrainDisplay extends BaseDisplay {
    private TerrainManager terrainManager;
    private Level currentLevel;
    private PImage tree1;
    private PImage tree2;
    private PGraphics terrainBuffer;

    public TerrainDisplay(PApplet applet, TerrainManager terrainManager, PGraphics terrainBuffer) {
        super(applet);
        this.terrainManager = terrainManager;
        this.terrainBuffer = terrainBuffer;
    }

    public void setLevel(Level level, PImage tree1, PImage tree2) {
        this.currentLevel = level;
        this.tree1 = tree1;
        this.tree2 = tree2;
        updateTerrainBuffer(); // Update buffer when level changes
    }

    public void updateTerrainBuffer() {
        if (currentLevel == null) return;

        terrainBuffer.beginDraw();
        terrainBuffer.clear(); // Clear the buffer

        // Draw terrain to buffer
        char[][] layout = terrainManager.getLayoutScaled();
        String[] colorParts = currentLevel.getForegroundColor().split(",");
        int r = Integer.parseInt(colorParts[0]);
        int g = Integer.parseInt(colorParts[1]);
        int b = Integer.parseInt(colorParts[2]);

        terrainBuffer.fill(r, g, b);
        terrainBuffer.noStroke();

        // Draw terrain
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                if (layout[y][x] == 'X') {
                    terrainBuffer.rect(x, y, 1, applet.height - y);
                }
            }
        }

        // Draw trees
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                if (layout[y][x] == 'T') {
                    PImage treeImage = currentLevel.getTreeFile() != null &&
                            currentLevel.getTreeFile().equals("tree1.png") ? tree1 : tree2;
                    if (treeImage != null) {
                        terrainBuffer.image(treeImage, x - 16, y - 32, 32, 32);
                    }
                }
            }
        }

        terrainBuffer.endDraw();
    }

    @Override
    public void render() {
        // The actual rendering is now handled by GameWindow using the buffer
        applet.image(terrainBuffer, 0, 0);
    }

    // Used when terrain is modified (e.g., explosions)
    public void notifyTerrainChanged() {
        updateTerrainBuffer();
    }
}