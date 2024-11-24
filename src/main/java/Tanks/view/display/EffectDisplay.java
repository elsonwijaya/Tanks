package Tanks.view.display;

import Tanks.model.effects.*;
import processing.core.PApplet;

public class EffectDisplay extends BaseDisplay {
    private ParticleSystem particleSystem;

    public EffectDisplay(PApplet applet, ParticleSystem particleSystem) {
        super(applet);
        this.particleSystem = particleSystem;
    }

    @Override
    public void render() {
        applet.blendMode(PApplet.ADD);

        for (ParticleEffect effect : particleSystem.getActiveEffects()) {
            for (Particle particle : effect.getParticles()) {
                renderParticle(particle);
            }
        }

        applet.blendMode(PApplet.BLEND);
    }

    private void renderParticle(Particle particle) {
        float alpha = 255 * particle.getLifespanRatio();
        int[] color = particle.getColor();

        applet.noStroke();
        applet.fill(color[0], color[1], color[2], alpha);
        applet.ellipse(particle.getX(), particle.getY(), particle.getSize(), particle.getSize());
    }
}