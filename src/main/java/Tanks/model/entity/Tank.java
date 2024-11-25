package Tanks.model.entity;

import Tanks.model.terrain.TerrainManager;

public class Tank extends Entity {
    private static final int TANKWIDTH = 20;
    private static final int TANKHEIGHT = 4;
    private static final float MOVE_SPEED = 1.5f;
    private static final float MIN_ANGLE = -(float)Math.PI + 0.21f;
    private static final float MAX_ANGLE = -0.21f;

    private char name;
    private float angle;
    private int health;
    private int power;
    private int fuel;
    private int parachute;
    private int score;
    private float turretLength;
    private boolean isFalling;
    private float fallSpeed;
    private boolean fallDueToExplosion;
    private boolean parachuteDeployed;
    private TerrainManager terrainManager;

    public Tank(float x, float y, int[] color, char name, TerrainManager terrainManager) {
        super(x, y, color);
        this.name = name;
        this.terrainManager = terrainManager;
        initializeStats();
    }

    private void initializeStats() {
        this.angle = -(float)(Math.PI / 2);
        this.health = 100;
        this.power = 50;
        this.fuel = 250;
        this.parachute = 3;
        this.score = 0;
        this.turretLength = 15;
        this.isFalling = false;
        this.fallSpeed = 0;
        this.fallDueToExplosion = false;
        this.parachuteDeployed = false;
    }

    @Override
    public void update() {
        handleFalling();
    }

    private void handleFalling() {
        boolean wasOnGround = !isFalling;
        boolean nowOnGround = isOnGround();

        if (!nowOnGround) {
            isFalling = true;
            if (fallDueToExplosion && parachute > 0) {
                fallSpeed = 2;
                parachuteDeployed = true;
            } else {
                fallSpeed = 4;
                if (fallDueToExplosion) {
                    health -= 1;
                    fuel += 1;
                }
            }
            y += fallSpeed;
        } else {
            if (isFalling || !wasOnGround) {
                if (parachuteDeployed) {
                    parachute--;
                    parachuteDeployed = false;
                }
                isFalling = false;
                fallSpeed = 0;
                fallDueToExplosion = false;
                float groundLevel = terrainManager.getGroundLevelAt((int)x);
                y = groundLevel;  // Snap to exact ground level when landing
            }
        }
    }

    public void moveLeft() {
        if (fuel > 0) {
            float newX = x - MOVE_SPEED;
            // Check if new position would be valid
            float groundLevel = terrainManager.getGroundLevelAt((int)newX);
            if (groundLevel < terrainManager.getScaledHeight()) {
                x = newX;
                y = groundLevel;
                fuel -= Math.abs(MOVE_SPEED);
            }
        }
    }

    public void moveRight() {
        if (fuel > 0) {
            float newX = x + MOVE_SPEED;
            // Check if new position would be valid
            float groundLevel = terrainManager.getGroundLevelAt((int)newX);
            if (groundLevel < terrainManager.getScaledHeight()) {
                x = newX;
                y = groundLevel;
                fuel -= Math.abs(MOVE_SPEED);
            }
        }
    }

    public void turnTurret(float amount) {
        angle += amount;
        // Clamp angle between min and max values
        angle = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, angle));
    }

    private float getGroundLevel(int x) {
        return terrainManager.getGroundLevelAt(x);
    }

    public boolean isOnGround() {
        int tankX = Math.round(x);
        float groundLevel = terrainManager.getGroundLevelAt(tankX);
        return y >= groundLevel - 1 && y <= groundLevel + 1;
    }

    public boolean isBelowMap() {
        return y >= terrainManager.getScaledHeight() - 32;
    }

    public void fallExplosion() {
        this.fallDueToExplosion = true;
    }

    // Power-ups
    public void repair() {
        if (score >= 20) {
            health = Math.min(100, health + 20);
            score -= 20;
        }
    }

    public void refuel() {
        if (score >= 10) {
            fuel += 200;
            score -= 10;
        }
    }

    public void addParachute() {
        if (score >= 15) {
            parachute++;
            score -= 15;
        }
    }

    public void resetStats() {
        health = 100;
        fuel = 250;
    }

    // Getters and setters
    public char getName() { return name; }
    public float getAngle() { return angle; }
    public int getHealth() { return health; }
    public int getPower() { return power; }
    public int getFuel() { return fuel; }
    public int getParachute() { return parachute; }
    public int getScore() { return score; }
    public boolean isParachuteDeployed() { return parachuteDeployed; }
    public float getTurretX() { return x + TANKWIDTH/2 + turretLength * (float)Math.cos(angle); }
    public float getTurretY() { return y + TANKHEIGHT/2 + turretLength * (float)Math.sin(angle); }

    public void setHealth(int health) { this.health = health; }
    public void addScore(int points) { this.score += points; }
    public void setPower(int power) { this.power = Math.max(0, Math.min(100, power)); }
}