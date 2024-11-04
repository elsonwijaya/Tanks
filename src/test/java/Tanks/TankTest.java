package Tanks;

import org.junit.jupiter.api.Test;

import processing.core.PApplet;
import processing.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import javax.lang.model.type.NullType;

public class TankTest {

    private Tank tank;
    private PApplet p;

    @Test
    public void testCreateTank() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        assertEquals(50, tank.getTankX());
        assertEquals(50, tank.getTankY());
        assertEquals('A', tank.getName());
        assertEquals(100, tank.getTankHealth());
        assertEquals(250, tank.getTankFuel());
        assertEquals(3, tank.getTankParachute());
        assertArrayEquals(color, tank.getColor());
    }

    @Test
    public void testMovementRight() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.setSpeed(1.5f);
        tank.moveRight();
        assertEquals(51.5f, tank.getTankX());
        tank.moveLeft();
        assertEquals(50f, tank.getTankX());
    }

    @Test
    public void testMovementLeft() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.setSpeed(1.5f);
        tank.moveLeft();
        assertEquals(48.5f, tank.getTankX());
    }

    @Test
    public void Parachute() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        assertEquals(3, tank.getTankParachute());
        tank.addParachute();
        assertEquals(3, tank.getTankParachute());
        tank.setScore(100);
        tank.addParachute();
        assertEquals(4, tank.getTankParachute());
        assertEquals(85, tank.getScore());
    }

    @Test
    public void testTurnTurretLeft() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.turnTurretLeft((float) 0.1);
        assertEquals(-Math.PI / 2 - (float) 0.1, tank.getTurretAngle());
    }

    @Test
    public void testTurnTurretRight() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.turnTurretRight((float) 0.1);
        assertEquals(-Math.PI / 2 + (float) 0.1, tank.getTurretAngle());
    }

    @Test
    public void testResetStats() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');
        
        tank.setTankFuel(0);
        tank.setTankHealth(50);
        tank.resetStats();
        assertEquals(250, tank.getTankFuel());
        assertEquals(100, tank.getTankHealth());
    }

    @Test
    public void testScores() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.addScore(10);
        assertEquals(10, tank.getScore());
    }

    @Test
    public void testPowerUps() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.addScore(100);
        tank.refuel();
        assertEquals(450, tank.getTankFuel());
        assertEquals(90, tank.getScore());
        tank.setTankHealth(70);
        tank.repair();
        assertEquals(90, tank.getTankHealth());
        assertEquals(70, tank.getScore());        
    }

    @Test
    public void testPower() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        assertEquals(50, tank.getTankPower());
        tank.increasePower(1);
        assertEquals(51, tank.getTankPower());
        tank.decreasePower(50);
        assertEquals(1, tank.getTankPower());
        tank.decreasePower(1);
        assertEquals(0, tank.getTankPower());
        tank.decreasePower(1);
        assertEquals(0, tank.getTankPower());
    }

    @Test
    public void testGetY() {
        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        App.layoutScaled = new char[App.HEIGHT][App.WIDTH];

        for (int x = 0; x < App.HEIGHT; x++) {
            for (int y = 0; y < App.WIDTH; y++) {
                if (x > 20) {
                    App.layoutScaled[x][y] = 'X';
                }
                else {
                    App.layoutScaled[x][y] = ' ';
                }
            }
        }
    int x = 10; //x coordinate
    float getY = 20 * Tank.CELLHEIGHT - Tank.CELLSIZE;
    assertEquals(getY, tank.getY(x));
    }

    @Test
    public void testDisplay() {
        PApplet p = new PApplet();

        int[] color = {255, 0, 0};
        tank = new Tank(50, 50, color, 'A');

        tank.display(p);

        p.fill(color[0], color[1], color[2]);
        float angle = (float) -(Math.PI / 2);
        float turretLength = 15;
        int startX = (50 - 32 / 5);
        int startY = (50 + 24);
        int TANKWIDTH = 20;
        float TANKHEIGHT = 32 / 8;
        float turretX = (startX + 3) + (TANKWIDTH - 5) / 2 + turretLength * PApplet.cos(angle);
        float turretY = (startY - 3) + TANKHEIGHT / 2 + turretLength * PApplet.sin(angle);


        p.fill(255, 0, 0);
        p.rect(startX, startY, TANKWIDTH, TANKHEIGHT);
        p.rect(startX + 3, startY, TANKWIDTH - 5, TANKHEIGHT);
        p.stroke(0);
        p.line(startX + 10, startY - 4, turretX, turretY);
        p.strokeWeight(4);
        p.noStroke();
    }
}
