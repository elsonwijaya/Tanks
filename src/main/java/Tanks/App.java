package Tanks;

import Tanks.model.engine.GameEngine;
import Tanks.view.GameWindow;
import Tanks.view.keyboard.KeyboardInputHandler;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class App extends PApplet {
    private GameEngine gameEngine;
    private GameWindow gameWindow;
    private KeyboardInputHandler inputHandler;

    @Override
    public void settings() {
        size(864, 640);
    }

    @Override
    public void setup() {
        frameRate(30);
        gameEngine = new GameEngine(this, "config.json");  // Pass 'this' as PApplet
        gameWindow = new GameWindow(this, gameEngine);
        inputHandler = new KeyboardInputHandler(gameEngine);

        gameEngine.initialize(loadJSONObject("config.json"));
        gameWindow.initialize();
    }

    @Override
    public void draw() {
        gameEngine.update();
        gameWindow.render();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        inputHandler.handleKeyPress(event);
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }
}