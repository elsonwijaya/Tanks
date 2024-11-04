package Tanks;

import processing.core.PApplet;

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
        for (Tank tank : App.tanks) {
            float tankX = tank.startingPositionX + ((CELLSIZE / 5) / 2);
            float tankY = tank.startingPositionY;
            float distance = (float) Math.sqrt(Math.pow(rx - tankX, 2) + Math.pow(ry - tankY, 2));
            float damage = 0;

            if (distance <= explosionRadius) {
                damage = 2 * (explosionRadius - distance);
                tank.setTankHealth(tank.getTankHealth() - (int) damage);

                if (this.owner != tank) {
                    this.owner.addScore((int) damage);
                }

                tank.fallExplosion();
            } 
        }
    }
    
    /**
     * Handles destroying the terrain due to projectile explosion 
     */
    public void destroyTerrain() {
        int minX = Math.max(0, (int) rx - explosionRadius);
        int maxX = Math.min(App.layoutScaled[0].length, (int) rx + explosionRadius);
        int minY = Math.max(0, (int) ry - explosionRadius);
        int maxY = Math.min(App.layoutScaled.length, (int) ry + explosionRadius);
    
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                if (PApplet.dist(rx, ry, i, j) <= explosionRadius) {
                    App.layoutScaled[j][i] = ' ';
                }
            }
        }

        //Handles the trees 
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                if (checkTree(i, j)) {
                    int newJ = j;
                    while (newJ + 1 < App.layoutScaled.length && App.layoutScaled[newJ + 1][i] == ' ') {
                        newJ++;
                    } 
                    App.layoutScaled[j][i] = ' ';
                    App.layoutScaled[newJ][i] = 'T';
                }
            }
        }


        for (int y = minY + 1; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (App.layoutScaled[y][x] == ' ' && App.layoutScaled[y - 1][x] == 'X') {
                    int newY = y;
                    while (newY > 0 && App.layoutScaled[newY - 1][x] == 'X') {
                        App.layoutScaled[newY][x] = 'X';
                        App.layoutScaled[newY - 1][x] = ' ';
                        newY--;
                    }
                }
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

}