package Tanks.view.keyboard;

public class InputAction {
    public enum ActionType {
        MOVE_LEFT,
        MOVE_RIGHT,
        TURRET_UP,
        TURRET_DOWN,
        FIRE,
        INCREASE_POWER,
        DECREASE_POWER,
        REPAIR,
        REFUEL,
        PARACHUTE
    }

    private final ActionType type;
    private final float value;

    public InputAction(ActionType type) {
        this(type, 0);
    }

    public InputAction(ActionType type, float value) {
        this.type = type;
        this.value = value;
    }

    public ActionType getType() { return type; }
    public float getValue() { return value; }
}
