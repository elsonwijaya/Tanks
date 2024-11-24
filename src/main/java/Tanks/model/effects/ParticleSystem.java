package Tanks.model.effects;

import java.util.*;

public class ParticleSystem {
    private static final Random random = new Random();
    private List<ParticleEffect> activeEffects;

    public ParticleSystem() {
        this.activeEffects = new ArrayList<>();
    }

    public void update() {
        Iterator<ParticleEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            ParticleEffect effect = iterator.next();
            effect.update();
            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }

    public void createExplosion(float x, float y, int[] color) {
        activeEffects.add(new ExplosionEffect(x, y, color));
    }

    public List<ParticleEffect> getActiveEffects() {
        return activeEffects;
    }
}