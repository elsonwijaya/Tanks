package Tanks.model.engine;

import Tanks.model.effects.ParticleSystem;
import Tanks.model.entity.Tank;
import Tanks.model.entity.Projectile;
import Tanks.model.terrain.TerrainManager;
import java.util.*;

public class EntityManager {
    private static final int EXPLOSION_RADIUS = 30;
    private static final float DAMAGE_MULTIPLIER = 2.0f;
    private static final int CELLSIZE = 32;
    private static final int CELLHEIGHT = 32;

    private List<Tank> tanks;
    private List<Tank> tanksCopy;
    private List<Projectile> projectiles;
    private Tank currentTank;
    private int currentTurnIndex;
    private TerrainManager terrainManager;
    private WindEngine windEngine;
    private ParticleSystem particleSystem;

    public EntityManager(TerrainManager terrainManager, WindEngine windEngine) {
        this.terrainManager = terrainManager;
        this.windEngine = windEngine;
        this.tanks = new ArrayList<>();
        this.tanksCopy = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.particleSystem = new ParticleSystem();
    }

    public void initializeTanks(char[][] layout, Map<Character, int[]> playerColors) {
        tanks.clear();
        tanksCopy.clear();
        currentTurnIndex = 0;

        System.out.println("Initializing tanks...");
        char[][] scaledLayout = terrainManager.getLayoutScaled();

        // Find ground level for each tank
        for (int x = 0; x < scaledLayout.length; x++) {
            for (int y = 0; y < scaledLayout[0].length; y++) {
                char ch = scaledLayout[x][y];
                if (ch != ' ' && ch >= 'A' && ch <= 'Z' && ch != 'X' && playerColors.containsKey(ch)) {
                    System.out.println("Found tank " + ch + " at position (" + y + "," + x + ")");

                    // Find actual ground level for this tank's x position
                    float groundLevel = terrainManager.getGroundLevelAt(y);  // Use x=y for ground level check
                    if (groundLevel >= terrainManager.getScaledHeight()) {
                        System.out.println("Warning: No ground found for tank " + ch);
                        continue;  // Skip this tank if no ground found
                    }

                    int[] currentColor = playerColors.get(ch);
                    Tank tank = new Tank(
                            y,              // x coordinate
                            groundLevel,    // y coordinate - use actual ground level
                            currentColor,
                            ch,
                            terrainManager
                    );

                    tanks.add(tank);
                    tanksCopy.add(tank);
                    System.out.println("Created tank " + ch + " at (" + y + "," + groundLevel + ")");
                }
            }
        }

        System.out.println("Created " + tanks.size() + " tanks");

        if (!tanks.isEmpty()) {
            Collections.sort(tanks, Comparator.comparing(Tank::getName));
            Collections.sort(tanksCopy, Comparator.comparing(Tank::getName));
            currentTank = tanks.get(0);
            System.out.println("Set current tank to: " + currentTank.getName());
        }
    }

    public void update() {
        updateTanks();
        updateProjectiles();
        removeDestroyedTanks();
        particleSystem.update();
    }

    private void updateTanks() {
        for (Tank tank : tanks) {
            tank.update();
            if (tank.isBelowMap()) {
                tank.setHealth(0);
                handleTankExplosion(tank, 15);
            }
        }
    }

    private void updateProjectiles() {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update();

            if (projectile.hasExploded()) {
                handleProjectileExplosion(projectile);
                iterator.remove();
            }
        }
    }

    private void removeDestroyedTanks() {
        Iterator<Tank> iterator = tanks.iterator();
        while (iterator.hasNext()) {
            Tank tank = iterator.next();
            if (tank.getHealth() <= 0) {
                handleTankExplosion(tank, 30);
                iterator.remove();

                if (currentTank == tank) {
                    nextTurn();
                } else if (tanks.indexOf(tank) < currentTurnIndex) {
                    currentTurnIndex--;
                }
            }
        }
    }

    public void fireProjectile(Tank tank) {
        if (tank != currentTank) return;

        Projectile projectile = new Projectile(
                tank.getTurretX(),
                tank.getTurretY(),
                tank.getAngle(),
                tank.getColor(),
                tank.getPower(),
                windEngine.getCurrentWind(),
                tank,
                terrainManager
        );
        projectiles.add(projectile);
    }

    private void handleProjectileExplosion(Projectile projectile) {
        for (Tank tank : tanks) {
            float distance = calculateDistance(
                    projectile.getX(), projectile.getY(),
                    tank.getX() + 10,
                    tank.getY() + 16
            );

            if (distance <= EXPLOSION_RADIUS) {
                int damage = calculateDamage(distance);
                tank.setHealth(tank.getHealth() - damage);

                if (projectile.getOwner() != tank) {
                    projectile.getOwner().addScore(damage);
                }

                tank.fallExplosion();
            }
        }

        terrainManager.createExplosion(
                Math.round(projectile.getX()),
                Math.round(projectile.getY()),
                EXPLOSION_RADIUS
        );

        particleSystem.createExplosion(
                projectile.getX(),
                projectile.getY(),
                projectile.getColor()
        );
    }

    private void handleTankExplosion(Tank tank, int radius) {
        for (Tank otherTank : tanks) {
            if (otherTank != tank) {
                float distance = calculateDistance(
                        tank.getX() + 10, tank.getY() + 16,
                        otherTank.getX() + 10, otherTank.getY() + 16
                );

                if (distance <= radius) {
                    int damage = calculateDamage(distance);
                    otherTank.setHealth(otherTank.getHealth() - damage);
                }
            }
        }

        terrainManager.createExplosion(
                Math.round(tank.getX() + 10),
                Math.round(tank.getY() + 16),
                radius
        );

        particleSystem.createExplosion(
                tank.getX() + 10,
                tank.getY() + 16,
                tank.getColor()
        );
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private int calculateDamage(float distance) {
        return (int)(DAMAGE_MULTIPLIER * (EXPLOSION_RADIUS - distance));
    }

    public void nextTurn() {
        if (tanks.isEmpty()) return;
        currentTurnIndex = (currentTurnIndex + 1) % tanks.size();
        currentTank = tanks.get(currentTurnIndex);
        windEngine.generateNewWindForNextTurn();
    }

    // Getters and utility methods
    public Tank getCurrentTank() { return currentTank; }
    public List<Tank> getTanks() { return tanks; }
    public List<Tank> getTanksCopy() { return tanksCopy; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public ParticleSystem getParticleSystem() { return particleSystem; }
    public int getRemainingPlayerCount() {
        System.out.println("Remaining players: " + tanks.size());
        return tanks.size();
    }

    public boolean isGameOver() {
        return tanks.size() <= 1;
    }

    public Tank getWinner() {
        if (tanks.size() != 1) return null;
        return tanks.get(0);
    }

    public void resetTankStats() {
        for (Tank tank : tanks) {
            tank.resetStats();
        }
    }
}