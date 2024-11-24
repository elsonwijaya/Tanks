package Tanks.view.keyboard;

import Tanks.model.engine.GameEngine;
import Tanks.model.entity.Tank;
import processing.event.KeyEvent;

public class KeyboardInputHandler {
    private final GameEngine gameEngine;

    public KeyboardInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void handleKeyPress(KeyEvent event) {
        if (gameEngine.isGameOver() ||
                gameEngine.getEntityManager().getRemainingPlayerCount() <= 1) {
            return;
        }

        Tank currentTank = gameEngine.getEntityManager().getCurrentTank();
        if (currentTank == null) return;

        InputAction action = mapKeyToAction(event.getKeyCode());
        if (action != null) {
            executeAction(action, currentTank);
        }
    }

    private InputAction mapKeyToAction(int keyCode) {
        switch (keyCode) {
            case KeyBindings.MOVE_LEFT:
                return new InputAction(InputAction.ActionType.MOVE_LEFT);
            case KeyBindings.MOVE_RIGHT:
                return new InputAction(InputAction.ActionType.MOVE_RIGHT);
            case KeyBindings.TURRET_UP:
                return new InputAction(InputAction.ActionType.TURRET_UP, KeyBindings.TURRET_ROTATION_SPEED);
            case KeyBindings.TURRET_DOWN:
                return new InputAction(InputAction.ActionType.TURRET_DOWN, -KeyBindings.TURRET_ROTATION_SPEED);
            case KeyBindings.FIRE:
                return new InputAction(InputAction.ActionType.FIRE);
            case KeyBindings.INCREASE_POWER:
                return new InputAction(InputAction.ActionType.INCREASE_POWER, KeyBindings.POWER_CHANGE_RATE);
            case KeyBindings.DECREASE_POWER:
                return new InputAction(InputAction.ActionType.DECREASE_POWER, -KeyBindings.POWER_CHANGE_RATE);
            case KeyBindings.REPAIR:
                return new InputAction(InputAction.ActionType.REPAIR);
            case KeyBindings.REFUEL:
                return new InputAction(InputAction.ActionType.REFUEL);
            case KeyBindings.PARACHUTE:
                return new InputAction(InputAction.ActionType.PARACHUTE);
            default:
                return null;
        }
    }

    private void executeAction(InputAction action, Tank tank) {
        switch (action.getType()) {
            case MOVE_LEFT:
                tank.moveLeft();
                break;
            case MOVE_RIGHT:
                tank.moveRight();
                break;
            case TURRET_UP:
            case TURRET_DOWN:
                tank.turnTurret(action.getValue());
                break;
            case FIRE:
                gameEngine.getEntityManager().fireProjectile(tank);
                break;
            case INCREASE_POWER:
            case DECREASE_POWER:
                tank.setPower(tank.getPower() + (int)action.getValue());
                break;
            case REPAIR:
                tank.repair();
                break;
            case REFUEL:
                tank.refuel();
                break;
            case PARACHUTE:
                tank.addParachute();
                break;
        }
    }
}