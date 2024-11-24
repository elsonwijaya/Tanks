package Tanks.view.display;

import Tanks.model.entity.Tank;
import processing.core.PApplet;
import processing.core.PImage;

public class HUDDisplay extends BaseDisplay {
    private Tank currentTank;
    private int windStrength;
    private PImage fuelImage;
    private PImage parachuteImage;
    private PImage windLeftImage;
    private PImage windRightImage;

    public HUDDisplay(PApplet applet, PImage fuelImage, PImage parachuteImage,
                      PImage windLeftImage, PImage windRightImage) {
        super(applet);
        this.fuelImage = fuelImage;
        this.parachuteImage = parachuteImage;
        this.windLeftImage = windLeftImage;
        this.windRightImage = windRightImage;
    }

    public void update(Tank currentTank, int windStrength) {
        this.currentTank = currentTank;
        this.windStrength = windStrength;
    }

    @Override
    public void render() {
        if (currentTank == null) return;

        applet.fill(0);
        applet.textSize(18);

        drawPlayerInfo();
        drawHealthBar();
        drawPowerIndicator();
        drawWindIndicator();
        drawResources();
    }

    private void drawPlayerInfo() {
        applet.text("Player " + currentTank.getName() + "'s turn", 20, 26);
    }

    private void drawHealthBar() {
        applet.fill(0);
        float currentHealth = currentTank.getHealth();
        applet.text("Health:", 320, 26);

        float healthWidth = 200 * (currentHealth/100f);
        applet.fill(0, 0, 255);
        applet.rect(385, 12, healthWidth, 14);

        applet.fill(0);
        applet.text((int)currentHealth, 590, 26);
    }

    private void drawPowerIndicator() {
        int power = currentTank.getPower();
        applet.text("Power:", 320, 55);
        applet.text(power, 385, 55);

        applet.noFill();
        applet.stroke(128);
        applet.strokeWeight(4);
        applet.rect(385, 12, -3 + power * 2, 14);

        applet.stroke(255, 0, 0);
        applet.strokeWeight(3);
        applet.line(385 + power * 2, 8, 385 + power * 2, 26);
    }

    private void drawWindIndicator() {
        PImage windImage = windStrength >= 0 ? windRightImage : windLeftImage;
        applet.image(windImage, 740, 10);
        applet.text(Math.abs(windStrength), 795, 44);
    }

    private void drawResources() {
        // Fuel
        applet.image(fuelImage, 165, 10);
        applet.fill(0);
        applet.text(currentTank.getFuel(), 190, 26);

        // Parachutes
        applet.image(parachuteImage, 160, 40);
        applet.fill(0);
        applet.text(currentTank.getParachute(), 190, 62);
    }
}