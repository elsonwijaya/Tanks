package Tanks.model.effects;

public class ExplosionEffect extends ParticleEffect {
    private static final int NUM_PARTICLES = 50;
    private static final float MAX_VELOCITY = 3.0f;
    private static final float MAX_LIFESPAN = 60;
    private static final float MIN_SIZE = 2.0f;
    private static final float MAX_SIZE = 6.0f;
    private static final float[] COLORS = {
            255, 255, 0,  // Yellow
            255, 165, 0,  // Orange
            255, 0, 0     // Red
    };

    public ExplosionEffect(float x, float y, int[] color) {
        super(x, y, color);
    }

    @Override
    protected void init() {
        // Create initial burst of particles
        for (int i = 0; i < NUM_PARTICLES; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float velocity = (float) (Math.random() * MAX_VELOCITY);
            float dx = (float) Math.cos(angle) * velocity;
            float dy = (float) Math.sin(angle) * velocity;
            float lifespan = (float) (Math.random() * MAX_LIFESPAN);
            float size = (float) (Math.random() * (MAX_SIZE - MIN_SIZE) + MIN_SIZE);

            // Randomly select explosion colors
            int colorIndex = (int)(Math.random() * 3) * 3;
            int[] particleColor = new int[] {
                    (int)COLORS[colorIndex],
                    (int)COLORS[colorIndex + 1],
                    (int)COLORS[colorIndex + 2]
            };

            particles.add(new Particle(x, y, dx, dy, particleColor, lifespan, size));
        }
    }
}