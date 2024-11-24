package Tanks.model.entity;

public abstract class Entity {
    protected float x, y;
    protected int[] color;

    public Entity(float x, float y, int[] color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int[] getColor() { return color; }

    public abstract void update();
}
