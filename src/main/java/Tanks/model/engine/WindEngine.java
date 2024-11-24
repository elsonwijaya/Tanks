package Tanks.model.engine;

import java.util.Random;

public class WindEngine {
    private static final int MIN_WIND = -35;
    private static final int MAX_WIND = 35;
    private static final float WIND_EFFECT_MULTIPLIER = 0.001f;

    private Random random;
    private int currentWind;

    public WindEngine() {
        this.random = new Random();
        generateNewWindForNextTurn();
    }

    /**
     * Updates the wind state. Currently no continuous updates needed,
     * wind only changes between turns.
     */
    public void update() {
        // Wind remains constant during a turn
        // Changes are handled by generateNewWindForNextTurn()
    }

    /**
     * Generates a new random wind value for the next turn.
     * Wind ranges from -35 to 35
     */
    public void generateNewWindForNextTurn() {
        // Generate wind between MIN_WIND and MAX_WIND (inclusive)
        currentWind = random.nextInt(MAX_WIND - MIN_WIND + 1) + MIN_WIND;
    }

    /**
     * Get the current wind value
     * @return current wind strength and direction
     */
    public int getCurrentWind() {
        return currentWind;
    }

    /**
     * Get the wind effect multiplier for physics calculations
     * @return wind effect multiplier
     */
    public float getWindEffectMultiplier() {
        return WIND_EFFECT_MULTIPLIER;
    }
}