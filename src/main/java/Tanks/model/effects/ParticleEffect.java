package Tanks.model.effects;

import java.util.*;

public abstract class ParticleEffect {
    protected List<Particle> particles;
    protected float x, y;
    protected int[] color;

    public ParticleEffect(float x, float y, int[] color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.particles = new ArrayList<>();
        init();
    }

    protected abstract void init();

    public void update() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update();
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    public boolean isFinished() {
        return particles.isEmpty();
    }

    public List<Particle> getParticles() {
        return particles;
    }
}