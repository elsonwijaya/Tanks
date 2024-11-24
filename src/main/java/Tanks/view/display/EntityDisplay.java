package Tanks.view.display;

import Tanks.model.entity.Tank;
import Tanks.model.entity.Projectile;
import processing.core.PApplet;
import processing.core.PImage;
import java.util.List;

public class EntityDisplay extends BaseDisplay {
    private static final int TANKWIDTH = 20;
    private static final int TANKHEIGHT = 4;

    private List<Tank> tanks;
    private List<Projectile> projectiles;
    private PImage parachuteImage;

    public EntityDisplay(PApplet applet, PImage parachuteImage) {
        super(applet);
        this.parachuteImage = parachuteImage;
    }

    public void setEntities(List<Tank> tanks, List<Projectile> projectiles) {
        this.tanks = tanks;
        this.projectiles = projectiles;
    }

    @Override
    public void render() {
        if (tanks != null) renderTanks();
        if (projectiles != null) renderProjectiles();
    }

    private void renderTanks() {
        for (Tank tank : tanks) {
            drawTank(tank);
        }
    }

    private void drawTank(Tank tank) {
        applet.pushMatrix();

        float x = tank.getX();
        float y = tank.getY();
        int[] color = tank.getColor();

        // Draw tank body
        applet.fill(color[0], color[1], color[2]);
        applet.noStroke();

        // Main body
        applet.rect(x - TANKWIDTH/2, y + 24, TANKWIDTH, TANKHEIGHT);

        // Second body layer
        applet.rect(x - TANKWIDTH/2 + 3, y + 21, TANKWIDTH - 5, TANKHEIGHT);

        // Draw turret
        applet.stroke(0);
        applet.strokeWeight(4);
        applet.line(x - TANKWIDTH/2 + 7, y + 20,
                tank.getTurretX(), tank.getTurretY());
        applet.noStroke();

        // Draw parachute if deployed
        if (tank.isParachuteDeployed()) {
            applet.image(parachuteImage, x - 12, y - 10);
        }

        applet.popMatrix();
    }

    private void renderProjectiles() {
        for (Projectile projectile : projectiles) {
            drawProjectile(projectile);
        }
    }

    private void drawProjectile(Projectile projectile) {
        int[] color = projectile.getColor();
        applet.fill(color[0], color[1], color[2]);
        applet.ellipse(projectile.getX(), projectile.getY(), 10, 10);
    }
}