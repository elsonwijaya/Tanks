package Tanks;

import processing.core.PApplet;
import java.util.Arrays;

public class Projectile{

    protected PApplet p;
    public App app;

    /**
     * Size of a cell in pixels
     */
    public static final int CELLSIZE = 32; //8;
    /**
     * Height of a cell in pixels
     */
    public static final int CELLHEIGHT = 32;
    /**
     * Width of the board
     */
    public static int WIDTH = 864;
    /**
     * Height of the board
     */
    public static int HEIGHT = 640; 
    /**
     * FPS of the game
     */
    public static final int FPS = 30;


    protected float rx, ry, dx, dy; // Position of the projectile
    protected int[] color; // Color of the projectile which is equal to tank's color
    protected float angle; // Direction the turret is facing
    protected int power; //power of tank
    protected int wind; //wind external factor
    protected Tank owner; // the owner/tank who shot the projectile
    protected float speedProjectile; //speed of the projectile
    protected float gravity = (float) 0.12; // external factor gravity
    protected int explosionRadius = 30; // explosion radius
    protected boolean exploded = false; // check whether the projectile has hit a terrain and exploded
    
    /**
     * The constructor of the Projectile Class
     * @param rx, x coordinate of the projectile
     * @param ry, y coordinate of the projectile
     * @param angle, angle at which the projectile will be shot out from (corresponds with turret angle)
     * @param color, color of the projectile
     * @param power, power of the tank
     * @param wind, an external factor wind, which could affect the projectile's trajectory
     * @param owner, the owner/tank who shot the projectile
     */
    public Projectile(float rx, float ry, float angle, int[] color, int power, int wind, Tank owner) {
        this.rx = rx;
        this.ry = ry;
        this.color = color;
        this.angle = angle;
        this.power = power;
        this.wind = wind;
        this.owner = owner;
        this.speedProjectile = (float) 1 + (float) (0.08 * power);
        this.dx = (float) Math.cos(-angle) * (float) speedProjectile;
        this.dy = (float) Math.sin(-angle) * (float) speedProjectile;
    }

    /**
     * Draws out the projectile
     * @param p, App.java
     */
    public void drawProjectile(App p) {
        if (!exploded) {
            dy -= gravity;
            dx += wind * 0.001;
            rx += dx;
            ry -= dy;
            p.fill(color[0], color[1], color[2]);
            p.ellipse(rx, ry, 10, 10);
        
            if (checkCollisionWithTerrain(rx, ry)) {
                explode(p);
            }
        }
    }

    /**
     * Detect whether the projectile has collide with terrain
     * @param rx, x coordinate of projectile
     * @param ry, y coordinate of projectile
     * @return true if projectile collided with terrain, otherwise false
     */
    public boolean checkCollisionWithTerrain(float rx, float ry) {
        int newX = Math.round(rx);
        int newY = Math.round(ry);
        boolean status = newX >= 0 && newX < App.layoutScaled[0].length && 
        newY >= 0 && newY < App.layoutScaled.length && App.layoutScaled[newY][newX] == 'X'; 
        return status;
    }
    
    /**
     * Animate explosion
     * @param p, App.java
     * @param rx, projectile x coordinate
     * @param ry, projectile y coordinate
     * @param explosionRadius, explosion radius
     */
    public void drawExplosion(App p, int rx, int ry, int explosionRadius) {
        p.fill(255, 255, 0); //yellow
        p.ellipse(rx, ry, (explosionRadius - 24) * 2, (explosionRadius - 24) * 2);

        p.fill(255, 165, 0); //orange
        p.ellipse(rx, ry, (explosionRadius - 15) * 2, (explosionRadius - 15) * 2);

        p.fill(255, 0, 0); //red
        p.ellipse(rx, ry, explosionRadius * 2, explosionRadius * 2);
    }

    /**
     * Handles damage to tanks due to projectile explosion
     */
    public void damage() {
        boolean otherTanksAlive = false;
        for (Tank tank : App.tanks) {
            float tankX = tank.startingPositionX + ((CELLSIZE / 5) / 2);
            float tankY = tank.startingPositionY;
            float distance = (float) Math.sqrt(Math.pow(rx - tankX, 2) + Math.pow(ry - tankY, 2));

            if (distance <= explosionRadius) {
                float damage = 2 * (explosionRadius - distance);
                tank.setTankHealth(tank.getTankHealth() - (int) damage);

                if (this.owner != tank) {
                    this.owner.addScore((int) damage);
                }
                tank.fallExplosion();
            }
            // Check if there are any other tanks still alive
            if (tank != owner && tank.getTankHealth() > 0) {
                otherTanksAlive = true;
            }
        }

        // Only end level if no other tanks are alive
        if (!otherTanksAlive) {
            App.setLevelOver();
        }
    }
    
    /**
     * Handles destroying the terrain due to projectile explosion 
     */
    public void destroyTerrain() {
        char[][] tempBuffer = new char[App.layoutScaled.length][App.layoutScaled[0].length];
        for (int i = 0; i < App.layoutScaled.length; i++) {
            tempBuffer[i] = Arrays.copyOf(App.layoutScaled[i], App.layoutScaled[i].length);
        }

        // Create smoother circular destruction using finer angle steps
        for (float angle = 0; angle < 2 * Math.PI; angle += 0.05) {
            for (float r = 0; r <= explosionRadius; r += 0.25) {
                int x = Math.round(rx + r * (float)Math.cos(angle));
                int y = Math.round(ry + r * (float)Math.sin(angle));

                // Add slight randomness at explosion edge for natural look
                float distFromCenter = PApplet.dist(rx, ry, x, y);
                if (x >= 0 && x < App.layoutScaled[0].length &&
                        y >= 0 && y < App.layoutScaled.length) {
                    if (distFromCenter < explosionRadius - 2) {
                        tempBuffer[y][x] = ' ';
                    }
                    else if (distFromCenter <= explosionRadius) {
                        // Smoother edge transition
                        if (Math.random() > (distFromCenter - (explosionRadius - 2)) / 2) {
                            tempBuffer[y][x] = ' ';
                        }
                    }
                }
            }
        }

        // Secondary pass to smooth jagged edges
        char[][] smoothedBuffer = new char[App.layoutScaled.length][App.layoutScaled[0].length];
        for (int i = 0; i < App.layoutScaled.length; i++) {
            smoothedBuffer[i] = Arrays.copyOf(tempBuffer[i], tempBuffer[i].length);
        }

        // Smooth edges using 3x3 window
        for (int y = 1; y < App.layoutScaled.length - 1; y++) {
            for (int x = 1; x < App.layoutScaled[0].length - 1; x++) {
                int emptyCount = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (tempBuffer[y + dy][x + dx] == ' ') {
                            emptyCount++;
                        }
                    }
                }
                if (emptyCount >= 6) {  // If majority of neighbors are empty
                    smoothedBuffer[y][x] = ' ';
                }
            }
        }

        // Copy smoothed changes back
        for (int i = 0; i < App.layoutScaled.length; i++) {
            for (int j = 0; j < App.layoutScaled[0].length; j++) {
                App.layoutScaled[i][j] = smoothedBuffer[i][j];
            }
        }

        // Handle terrain falling after destruction
        boolean changed;
        do {
            changed = false;
            for (int x = 0; x < App.layoutScaled[0].length; x++) {
                int emptySpaceY = -1;
                for (int y = App.layoutScaled.length - 1; y >= 0; y--) {
                    if (App.layoutScaled[y][x] == ' ') {
                        emptySpaceY = y;
                        break;
                    }
                }

                if (emptySpaceY != -1) {
                    for (int y = emptySpaceY - 1; y >= 0; y--) {
                        if (App.layoutScaled[y][x] != ' ') {
                            App.layoutScaled[emptySpaceY][x] = App.layoutScaled[y][x];
                            App.layoutScaled[y][x] = ' ';
                            emptySpaceY--;
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        handleTrees(0, App.layoutScaled[0].length - 1, 0, App.layoutScaled.length - 1);
    }

    private void handleTrees(int minX, int maxX, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            int groundY = -1;
            boolean hasTree = false;
            int treeY = -1;

            // Find highest tree and ground level in this column
            for (int y = 0; y < App.layoutScaled.length; y++) {
                if (App.layoutScaled[y][x] == 'T') {
                    hasTree = true;
                    treeY = y;
                }
                if (App.layoutScaled[y][x] == 'X') {
                    groundY = y;
                    break;
                }
            }

            // If there's a tree and ground, place tree on ground
            if (hasTree && groundY > 0) {
                App.layoutScaled[treeY][x] = ' ';
                App.layoutScaled[groundY - 1][x] = 'T';
            }
        }
    }

    /**
     * Check
     * @param rx, x coordinate of projectile
     * @param ry, y coordinate of projectile
     * @return true if tree is there, otherwise false
     */
    public boolean checkTree(int rx, int ry) {
        return App.layoutScaled[ry][rx] == 'T';
    }

    /**
     * Handles everything that happens if a projectile explodes (change terrain, deal damage, draw out animation)
     */
    public void explode(App p) {
        exploded = true;
        drawExplosion(p, (int) rx, (int) ry, explosionRadius);
        damage();
        destroyTerrain();
    }

    public boolean isOutOfBounds() {
        return rx < 0 || rx > App.WIDTH || ry < 0 || ry > App.HEIGHT;
    }

}