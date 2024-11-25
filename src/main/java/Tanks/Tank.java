package Tanks;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class Tank{

    protected PApplet p;

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
    public static final int TANKWIDTH = 20;
    public static final float TANKHEIGHT = CELLHEIGHT / 8;



    protected char name;
    protected float x, y; // Position of the tank
    protected float startingPositionX, startingPositionY;
    protected int[] color; // Color of the tank
    protected float angle; // Direction the turret is facing
    protected float speed; // Speed of the tank
    protected int health;
    protected int power;
    protected int fuel;
    protected int parachute = 3;
    protected int score;
    protected float turretLength = 15;
    protected float turretX;
    protected float turretY;
    protected boolean isFalling;
    protected float fallSpeed;
    protected boolean fallDueToExplosion;
    protected boolean parachuteDeployed;

    /**
     * The constructor of the Tank class
     * @param x, x coordinate of the tank
     * @param y, y coordinate of the tank
     * @param color, a list of the rgb values for the tank's color
     * @param name, name of the tank 
     */
    public Tank(float x, float y, int[] color, char name) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.color = color;
        this.angle = (float) -(Math.PI / 2);
        this.speed = (float) 1.5; // Set a default speed
        this.health = 100;
        this.power = 50;
        this.fuel = 250;
        this.parachute = parachute;
        this.score = 0;
        this.isFalling = false;
        this.fallSpeed = 0;
        this.fallDueToExplosion = false;
        this.parachuteDeployed = false;
    }

    /**
     * Moving to next level, reset tanks attribute to default (except parachute)
     */
    public void resetStats() {
        this.health = 100;
        this.fuel = 250;
    }

    /**
     * Get tank's name
     * @return name
     */
    public char getName() {
        return name;
    }

    /**
     * Get the tank's color
     * @return color
     */
    public int[] getColor() {
        return color;
    }

    /**
     * Draws out the body and turret of the tank
     */
    public void display(PApplet p) {
        p.fill(color[0], color[1], color[2]);

        startingPositionX = x - (CELLSIZE / 5);
        startingPositionY =  y + 24;
        p.rect(startingPositionX, startingPositionY, TANKWIDTH, TANKHEIGHT); // Tank Body
        
        float secondBodyX = startingPositionX + 3; 
        float secondBodyY = startingPositionY - 3;
        p.rect(secondBodyX, secondBodyY, TANKWIDTH - 5, TANKHEIGHT); // Second body
        
        turretX = secondBodyX + (TANKWIDTH - 5) / 2 + turretLength * PApplet.cos(angle);
        turretY = secondBodyY + TANKHEIGHT / 2 + turretLength * PApplet.sin(angle);

        //Turret
        p.stroke(0);
        p.line(secondBodyX + 7, secondBodyY - 1, turretX, turretY);
        p.strokeWeight(4);
        p.noStroke();

        tankFall();
    }

    /**
     * Checks whether
     * @return true if tank is on ground otherwise false
     */
    public boolean isOnGround() {
        int tankX = Math.round(x);
        boolean status = tankX >= 0 && tankX < App.layoutScaled[0].length && y + CELLHEIGHT < App.layoutScaled.length && App.layoutScaled[Math.round(y) + CELLHEIGHT][tankX] == 'X';
        return status;
    }

    /**
     * Checks whether the tank is out of the map when falling off
     * @return true if tank is out of map when falling off otherwise false
     */
    public boolean isBelowMap() {
        return y >= App.scaledHeight;
    }

    /**
     * Sets fallDueToExplosion to true
     */
    public void fallExplosion() {
        fallDueToExplosion = true;
    }

    /**
     * Handles the tank falling due to explosion
     */
    public void tankFall() {
        if (!isOnGround()) {
            isFalling = true;
            if (fallDueToExplosion && parachute > 0) {
                fallSpeed = 2;
                parachuteDeployed = true;
            }
            else {
                fallSpeed = 4;
                if (fallDueToExplosion) { // Only reduce hp when it is due to explosion, not due to descending with move()
                    health -= 1;
                    fuel += 1;
                }
            }
        }
        else {
            if (isFalling) {
                if (parachuteDeployed) {
                    parachute--;
                    parachuteDeployed = false;
                }
                isFalling = false;
                fallSpeed = 0;
                if (fallDueToExplosion) {
                    fallDueToExplosion = false;
                }
            }
        }

        if (isFalling) {
            y += fallSpeed;
        }
    }

    /**
     * Handles the tank explosion if it reaches 0 health
     * @param explosionRadius, the radius of explosion
     */
    public void tankExplosion(int explosionRadius) {
        float tankX = startingPositionX;
        float tankY = startingPositionY;
        for (Tank tank : App.tanks) {
            float distance = PApplet.dist(x, y, tankX, tankY);
            if (distance <= explosionRadius) {
                float damage = 2 * (explosionRadius - distance);
                tank.setTankHealth(tank.getTankHealth() - (int) damage);
            } 
        }

        int minX = Math.max(0, (int) x - explosionRadius);
        int maxX = Math.min(App.layoutScaled[0].length, (int) x + explosionRadius);
        int minY = Math.max(0, (int) y - explosionRadius);
        int maxY = Math.min(App.layoutScaled.length, (int) y + explosionRadius);

        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                if (PApplet.dist(x, y, i, j) <= explosionRadius) {
                    App.layoutScaled[j][i] = ' ';
                }
            }
        }
    }


    /**
     * Moves the tank left when the correct keypress is detected
     */
    public void moveLeft() {
        if (fuel > 0) {
            x -= speed;
            y = getY((int) x);
            
            fuel -= Math.abs(speed);
        }
    }

    /**
     * Moves the tank right when the correct keypress is detected
     */
    public void moveRight() {
        if (fuel > 0) {
            x += speed;
            y = getY((int) x);
            
            fuel -= Math.abs(speed);
        }
    }

    /**
     * Get the updated Y coordinate at the current X coordinate
     * @param x, x coordinate of the tank
     */
    public float getY(int x) {
        int height = 0;
        for (int i = 0; i < HEIGHT; i++) {
            if (App.layoutScaled[i][x] == 'X') {
                height = i;
                break;
            }
        }
        return height - CELLHEIGHT;
    }

    /**
     * Turn the turret right when the correct keypress is detected
     * @param t, change in angle
     */
    public void turnTurretRight(float t) {
        angle += t;
        if (angle > 0 - (float) 0.21) {
            angle = 0 - (float) 0.21;
        }
    }

    /**
     * Turn the turret left when the correct keypress is detected
     */
    public void turnTurretLeft(float t) {
        angle -= t;
        if (angle < -((float) Math.PI - (float) 0.21)) {
            angle = -((float) Math.PI - (float) 0.21);
        }
    }
    
    /**
     * Set the tank's speed
     * @param s, new speed
     */
    public void setSpeed(float s) {
        speed = s;
    }

    /**
     * Get tank's X coordinate
     * @return x
     */
    public float getTankX() {
        return x;
    }

    /**
     * Get tank's Y coordinate
     * @return y
     */
    public float getTankY() {
        return y;
    }

    /**
     * Get angle of the turret
     * @return angle
     */
    public float getTurretAngle() {
        return angle;
    }

    /**
     * Get tank's health
     * @return health
     */
    public int getTankHealth() {
        return health;
    }

    /**
     * Set tank's health
     * @param health, tank's new health
     */
    public void setTankHealth(int health) {
        this.health = health;
    }

    /**
     * Get tank's power
     * @return power
     */
    public int getTankPower() {
        return power;
    }

    /**
     * Increase tank's power
     * @param powerIncrease, amount to increase power by
     */
    public void increasePower(float powerIncrease) {
        if (power < 100) {
            power += powerIncrease;    
        }
        else {
            power = power;
        }
    }

    /**
     * Decrease tank's power
     * @param powerDecrease, amount to decrease power by
     */
    public void decreasePower(float powerDecrease) {
        if (power > 0) {
            power -= powerDecrease;    
        }
        else {
            power = power;
        }
    }

    /**
     * Get tank's fuel
     * @return fuel
     */
    public int getTankFuel() {
        return fuel;
    }

    /**
     * Set tank's fuel
     * @param fuel, tank's new fuel
     */
    public void setTankFuel(int fuel) {
        this.fuel = fuel;
    }
    
    /**
     * Creates a new projectile object and add's them to a list
     * @param wind, current wind, which is randomized from App.java
     */
    public void fireProjectile(int wind) {
        Projectile projectile = new Projectile(turretX, turretY, angle, color, power, wind, this);
        App.projectiles.add(projectile);
    }

    /**
     * Increase tank health by 20 (cannot exceed 100), when r is pressed (power up)
     */
    public void repair() {
        if (score >= 20) {
            if (health < 80) {
                health += 20;
            } else if (health >= 80) {
                health = 100;
            }
            score -= 20;
        } 
    }

    /**
     * Increase tank fuel by 200 (no limit), when f is pressed (power up)
     */
    public void refuel() {
        if (score >= 10) {
            fuel += 200;
            score -= 10;
        }
    }

    /**
     * Get tank's number of parachute
     * @return parachute 
     */
    public int getTankParachute() {
        return parachute;
    }

    /*
     * Add 1 parachute (no limit) when p is pressed (power up)
     */
    public void addParachute() {
        if (score >= 15) {
            parachute++;
            score -= 15;
        }
    }

    /**
     * Get tank's score
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * Set score
     * @param score
     */
    public void setScore(int score) {
        this.score = score;
    }
    /**
     * Add score 
     */
    public void addScore(int score) {
        this.score += score;
    }
}