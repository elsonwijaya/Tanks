package Tanks.model.effects;

public class Particle {
    private float x, y;
    private float dx, dy;
    private float lifespan;
    private float maxLifespan;
    private int[] color;
    private float size;

    public Particle(float x, float y, float dx, float dy, int[] color, float maxLifespan, float size) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.color = color;
        this.maxLifespan = maxLifespan;
        this.lifespan = maxLifespan;
        this.size = size;
    }

    public void update() {
        x += dx;
        y += dy;
        dy += 0.1f; // Gravity
        lifespan--;
    }

    public boolean isDead() {
        return lifespan <= 0;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getLifespanRatio() { return lifespan / maxLifespan; }
    public int[] getColor() { return color; }
    public float getSize() { return size; }
}