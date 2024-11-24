package Tanks.view.keyboard;

public class KeyBindings {
    // Movement
    public static final int MOVE_LEFT = 37;      // LEFT arrow
    public static final int MOVE_RIGHT = 39;     // RIGHT arrow
    public static final int TURRET_UP = 38;      // UP arrow
    public static final int TURRET_DOWN = 40;    // DOWN arrow

    // Actions
    public static final int FIRE = 32;           // SPACE
    public static final int INCREASE_POWER = 87; // W
    public static final int DECREASE_POWER = 83; // S
    public static final int REPAIR = 82;         // R
    public static final int REFUEL = 70;         // F
    public static final int PARACHUTE = 80;      // P

    // Movement constants
    public static final float TURRET_ROTATION_SPEED = 0.05f;
    public static final float POWER_CHANGE_RATE = 1.0f;
}