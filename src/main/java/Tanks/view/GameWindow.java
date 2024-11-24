package Tanks.view;

import Tanks.model.engine.GameEngine;
import Tanks.model.entity.Tank;
import Tanks.view.display.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class GameWindow {
    private final PApplet applet;
    private final GameEngine gameEngine;
    private final ResourceManager resourceManager;

    private TerrainDisplay terrainDisplay;
    private EntityDisplay entityDisplay;
    private HUDDisplay hudDisplay;
    private EffectDisplay effectDisplay;
    private PGraphics terrainBuffer;
    private boolean terrainNeedsUpdate;

    public GameWindow(PApplet applet, GameEngine gameEngine) {
        this.applet = applet;
        this.gameEngine = gameEngine;
        this.resourceManager = new ResourceManager(applet);
        this.terrainNeedsUpdate = true;
    }

    public void initialize() {
        resourceManager.loadAllResources();

        // Create terrain buffer
        terrainBuffer = applet.createGraphics(applet.width, applet.height);

        // Initialize display components
        terrainDisplay = new TerrainDisplay(applet, gameEngine.getTerrainManager(), terrainBuffer);
        gameEngine.getTerrainManager().setTerrainDisplay(terrainDisplay);
        entityDisplay = new EntityDisplay(applet, resourceManager.getImage("parachute"));
        hudDisplay = new HUDDisplay(
                applet,
                resourceManager.getImage("fuel"),
                resourceManager.getImage("parachute"),
                resourceManager.getImage("windLeft"),
                resourceManager.getImage("windRight")
        );
        effectDisplay = new EffectDisplay(applet, gameEngine.getEntityManager().getParticleSystem());

        updateCurrentLevel();
    }

    public void render() {
        // Game over state
        if (gameEngine.isGameOver()) {
            drawGameOver();
            return;
        }

        // Background
        PImage background = resourceManager.getBackgroundForLevel(
                gameEngine.getLevelManager().getCurrentLevel()
        );
        applet.background(background);

        // If terrain changed, update buffer
        if (terrainNeedsUpdate) {
            terrainDisplay.updateTerrainBuffer();
            terrainNeedsUpdate = false;
        }

        // Draw terrain from buffer
        applet.image(terrainBuffer, 0, 0);

        // Update and render entities
        entityDisplay.setEntities(
                gameEngine.getEntityManager().getTanks(),
                gameEngine.getEntityManager().getProjectiles()
        );
        entityDisplay.render();

        // Render effects
        effectDisplay.render();

        // Update and render HUD
        Tank currentTank = gameEngine.getEntityManager().getCurrentTank();
        if (currentTank != null) {
            hudDisplay.update(
                    currentTank,
                    gameEngine.getWindEngine().getCurrentWind()
            );
            hudDisplay.render();

            // Show turn indicator for first 2 seconds of turn
            if (gameEngine.getTurnStartTime() > 0 &&
                    System.currentTimeMillis() - gameEngine.getTurnStartTime() < 2000) {
                drawTurnIndicator(currentTank);
            }
        }
    }

    private void drawTurnIndicator(Tank tank) {
        float tankX = tank.getX();
        float tankY = tank.getY() - 40;  // Position above tank

        applet.stroke(0);
        applet.strokeWeight(2);
        applet.fill(255);

        // Draw arrow pointing to current tank
        applet.beginShape();
        applet.vertex(tankX, tankY);
        applet.vertex(tankX - 10, tankY - 20);
        applet.vertex(tankX + 10, tankY - 20);
        applet.endShape(PApplet.CLOSE);
    }

    private void updateCurrentLevel() {
        terrainDisplay.setLevel(
                gameEngine.getLevelManager().getCurrentLevel(),
                resourceManager.getImage("tree1"),
                resourceManager.getImage("tree2")
        );
        terrainNeedsUpdate = true;
    }

    private void drawGameOver() {
        applet.background(0, 150);
        applet.fill(255);
        applet.textAlign(PApplet.CENTER);

        Tank winner = gameEngine.getEntityManager().getWinner();
        if (winner != null) {
            // Winner announcement
            applet.textSize(32);
            applet.text("Player " + winner.getName() + " wins!",
                    applet.width/2, applet.height/2 - 50);

            // Draw score box
            drawFinalScores();
        }
    }

    private void drawFinalScores() {
        float y = applet.height/2;
        float delay = 700; // 0.7s delay between scores as per spec
        long startTime = System.currentTimeMillis();

        // Get sorted scores
        List<Tank> sortedTanks = new ArrayList<>(gameEngine.getEntityManager().getTanksCopy());
        sortedTanks.sort((t1, t2) -> Integer.compare(t2.getScore(), t1.getScore()));

        for (Tank tank : sortedTanks) {
            if (System.currentTimeMillis() - startTime > delay * sortedTanks.indexOf(tank)) {
                int[] color = tank.getColor();
                applet.fill(color[0], color[1], color[2], 200);
                applet.rect(applet.width/2 - 100, y, 200, 40);

                applet.fill(255);
                applet.textAlign(PApplet.LEFT, PApplet.CENTER);
                applet.text("Player " + tank.getName() + ": " + tank.getScore(),
                        applet.width/2 - 80, y + 20);

                y += 50;
            }
        }
    }

    public void notifyTerrainChanged() {
        terrainNeedsUpdate = true;
    }
}