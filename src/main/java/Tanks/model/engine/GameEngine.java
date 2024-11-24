package Tanks.model.engine;

import Tanks.model.level.Level;
import Tanks.model.level.LevelManager;
import Tanks.model.terrain.TerrainManager;
import Tanks.model.utils.FileLoader;
import processing.core.PApplet;
import processing.data.JSONObject;

public class GameEngine {
    private LevelManager levelManager;
    private TerrainManager terrainManager;
    private EntityManager entityManager;
    private WindEngine windEngine;
    private FileLoader fileLoader;
    private boolean isGameOver;
    private boolean isLevelOver;
    private long turnStartTime;

    public GameEngine(PApplet applet, String configPath) {
        System.out.println("Initializing GameEngine with config: " + configPath);
        this.levelManager = new LevelManager(configPath);
        this.terrainManager = new TerrainManager();
        this.windEngine = new WindEngine();
        this.entityManager = new EntityManager(terrainManager, windEngine);
        this.fileLoader = new FileLoader(applet);
        this.isGameOver = false;
        this.isLevelOver = false;
    }

    public void initialize(JSONObject config) {
        System.out.println("Loading game configuration...");
        levelManager.loadConfig(config);
        loadCurrentLevel();
    }

    private void loadCurrentLevel() {
        System.out.println("Loading current level...");
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) {
            System.err.println("Error: Current level is null");
            return;
        }

        String layoutPath = currentLevel.getLayoutFile();
        System.out.println("Loading layout from: " + layoutPath);

        String[] layoutData = fileLoader.loadLayoutFile(layoutPath);
        if (layoutData == null) {
            System.err.println("Error: Failed to load layout data");
            return;
        }

        System.out.println("Initializing terrain...");
        terrainManager.loadTerrain(layoutData);
        terrainManager.smoothTerrain();
        terrainManager.placeTrees();

        System.out.println("Initializing entities...");
        entityManager.initializeTanks(
                terrainManager.getOriginalLayout(),
                levelManager.getPlayerColors()
        );

        entityManager.resetTankStats();
        windEngine.generateNewWindForNextTurn();

        System.out.println("Level loading complete");
    }

    public void update() {
        if (!isGameOver) {
            entityManager.update();
            terrainManager.update();
            windEngine.update();
            checkLevelEnd();
        }
    }

    private void checkLevelEnd() {
        if (entityManager.getRemainingPlayerCount() <= 1 && !isLevelOver) {
            isLevelOver = true;
            handleLevelEnd();
        }
    }

    private void handleLevelEnd() {
        if (levelManager.hasNextLevel()) {
            levelManager.nextLevel();
            loadCurrentLevel();
            isLevelOver = false;
        } else {
            isGameOver = true;
        }
    }

    public void nextTurn() {
        entityManager.nextTurn();
        turnStartTime = System.currentTimeMillis();
    }

    public long getTurnStartTime() {
        return turnStartTime;
    }

    // Getters for the view layer
    public LevelManager getLevelManager() { return levelManager; }
    public TerrainManager getTerrainManager() { return terrainManager; }
    public EntityManager getEntityManager() { return entityManager; }
    public WindEngine getWindEngine() { return windEngine; }
    public boolean isGameOver() { return isGameOver; }
}