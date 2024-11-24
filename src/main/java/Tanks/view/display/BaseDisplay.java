package Tanks.view.display;

import processing.core.PApplet;

public abstract class BaseDisplay {
    protected final PApplet applet;

    public BaseDisplay(PApplet applet) {
        this.applet = applet;
    }

    public abstract void render();
}