package Tanks.model.entity;

import Tanks.model.terrain.TerrainManager;

public class Projectile extends Entity {
    private static final float GRAVITY = 0.12f;
    private static final int EXPLOSION_RADIUS = 30;

    private float dx, dy;
    private Tank owner;
    private float speedProjectile;
    private int wind;
    private boolean exploded;
    private TerrainManager terrainManager;

    public Projectile(float x, float y, float angle, int[] color, int power, int wind, Tank owner, TerrainManager terrainManager) {
        super(x, y, color);
        this.owner = owner;
        this.wind = wind;
        this.terrainManager = terrainManager;
        this.exploded = false;

        // Initialize velocity
        this.speedProjectile = 1 + 0.08f * power;
        this.dx = (float)Math.cos(-angle) * speedProjectile;
        this.dy = (float)Math.sin(-angle) * speedProjectile;
    }

    @Override
    public void update() {
        if (!exploded) {
            dy -= GRAVITY;
            dx += wind * 0.001f;
            x += dx;
            y -= dy;

            if (checkCollision()) {
                exploded = true;
            }
        }
    }

    private boolean checkCollision() {
        int newX = Math.round(x);
        int newY = Math.round(y);
        return terrainManager.isPositionSolid(newX, newY);
    }

    public Tank getOwner() { return owner; }
    public boolean hasExploded() { return exploded; }

    public void explode() {
        exploded = true;
        damageNearbyTanks();
        destroyTerrain();
    }

    private void damageNearbyTanks() {
        // Will be implemented when we add tank management
        // Damages tanks within explosion radius
    }

    private void destroyTerrain() {
        terrainManager.createExplosion(Math.round(x), Math.round(y), EXPLOSION_RADIUS);
    }
}