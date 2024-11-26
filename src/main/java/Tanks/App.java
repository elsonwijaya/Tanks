package Tanks;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static App instance;
    /**
     * Size of a cell in pixels
     */
    public static final int CELLSIZE = 32; //8;
    /**
     * Height of a cell in pixels
     */
    public static final int CELLHEIGHT = 32;
    /**
     * Width of the board
     */
    public static int WIDTH = 864;
    /**
     * Height of the board
     */
    public static int HEIGHT = 640; 
    /**
     * Size to resize image (small)
     */
    public static final int imageResizer = 20;
    /**
     * FPS of the game
     */
    public static final int FPS = 30;
    /**
     * Size to resize image (large)
     */
    public static final int imageSize = 48;
    /**
     * Variable to store config path
     */
    public String configPath;
    /**
     * Random object
     */
    public static Random random = new Random();

    //Resource images variables
    public PImage basicBackgroundImage;
    public PImage desertBackgroundImage;
    public PImage forestBackgroundImage;
    public PImage hillsBackgroundImage;
    public PImage snowBackgroundImage;
    public PImage fuelImage;
    public PImage parachuteImage;
    public PImage tree1;
    public PImage tree2;
    public PImage windLeft;
    public PImage windRight;
    //Config variables
    /**
     * Variable to store layout extracted from config file
     */
    public java.util.List<String> layouts = new ArrayList<>();
    /**
     * Variable to store background extracted from config file
     */
    public java.util.List<String> backgrounds = new ArrayList<>();
    /**
     * variable to store foreground-colour extracted from config file
     */
    public java.util.List<String> foregroundColors = new ArrayList<>();
    /**
     * variable to store trees extracted from config file
     */
    public java.util.List<String> trees = new ArrayList<>();
    /**
     * variable to store players extracted from config file
     */
    public HashMap<Character, int[]> players = new HashMap<>();
    //Terrain
    /**
     * variable to store the txt file that has been read 
     */
    public String[] layout;
    /**
     * variable to store the accordingly scaled Layout
     */
    public static char[][] layoutScaled;
    public static int scaledWidth;
    public static int scaledHeight;
    //Players
    /**
     * variable to store Tank players for tracking players on battlefield
     */
    public static java.util.List<Tank> tanks = new ArrayList<>(); // For tracking players on battlefield
    public static java.util.List<Tank> tanksCopy = new ArrayList<>(); // For tracking player scores
    public HashMap<Character, Integer> playerScores = new HashMap<>(); // Keeps player scores
    public static Tank currentTank;
    private int currentTurnIndex = 0;
    //Projectile
    /**
     * variable to store Projectile objects
     */
    public static java.util.List<Projectile> projectiles = new ArrayList<>();
    private Projectile currentProjectile;
    //Wind
    private int windRandom = random.nextInt(71) - 35;
    //Level and game
    public int currentLevelIndex = 0;
    public boolean isLevelOver = false;
    public boolean isGameOver = false;
    private boolean isTransitioning = false;
    private int transitionTimer = 0;
    private static final int TRANSITION_DELAY = 30; // 0.5 seconds at 60 FPS
    

    /**
     * Initialise the constructor of the App class.
     */
    public App() {
        this.configPath = "config.json";
        instance = this;
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        loadJSONObject(configPath);
        obtainConfig(configPath);
		loadLevel(layouts.get(currentLevelIndex));
        smoothing(layoutScaled);
        smoothing(layoutScaled);
        putTree();
        initializeTanks();
        currentTank = tanks.get(currentTurnIndex);
        //Load Images from resources
        basicBackgroundImage = loadImage("src/main/resources/Tanks/basic.png");
        desertBackgroundImage = loadImage("src/main/resources/Tanks/desert.png");
        forestBackgroundImage = loadImage("src/main/resources/Tanks/forest.png");
        hillsBackgroundImage = loadImage("src/main/resources/Tanks/hills.png");
        snowBackgroundImage = loadImage("src/main/resources/Tanks/snow.png");
        fuelImage = loadImage("src/main/resources/Tanks/fuel.png");
        parachuteImage = loadImage("src/main/resources/Tanks/parachute.png");
        tree1 = loadImage("src/main/resources/Tanks/tree1.png");
        tree2 = loadImage("src/main/resources/Tanks/tree2.png");
        windRight = loadImage("src/main/resources/Tanks/wind.png");
        windLeft = loadImage("src/main/resources/Tanks/wind-1.png");
        fuelImage.resize(imageResizer, imageResizer);
        windLeft.resize(imageSize, imageSize);
        windRight.resize(imageSize, imageSize);
        parachuteImage.resize(CELLSIZE, CELLHEIGHT);
    }

    
    /**
     * Given a json file which contains information of the 
     * levels (layout, background, foreground-colour, trees) and players of the game,
     * it will retrieve the information and store them in the given variables.
     * @param configPath, Path to the config json file
     */
    public void obtainConfig(String configPath) { 
        JSONObject config = loadJSONObject(configPath);
        
        //levels Object 
        JSONArray levels = config.getJSONArray("levels");
        
        for (int i = 0; i < levels.size(); i++) {
            //Get Layout
            JSONObject currentLevel = levels.getJSONObject(i);
            String currentLayout = currentLevel.getString("layout");
            layouts.add(currentLayout);
            //Get Background
            String currentBackground = currentLevel.getString("background");
            backgrounds.add(currentBackground);
            //Get ForegroundColor
            String currentForegroundColor = currentLevel.getString("foreground-colour");
            foregroundColors.add(currentForegroundColor);
            //Get Trees
            if (currentLevel.hasKey("trees")) {
                String currentTree = currentLevel.getString("trees");
                trees.add(currentTree);
            } else {
                trees.add(null);
            }
        }
        // players object
        JSONObject allPlayerColors = config.getJSONObject("player_colours");
        for (Object objectedKey : allPlayerColors.keys()) {
            String key = (String) objectedKey;
            String current = allPlayerColors.getString(key);
            int[] rgb;
            if (current.equals("random")) {
                rgb = new int[]{(int) random(256), (int) random(256), (int) random(256)};
            }  else {
                rgb = PApplet.parseInt(split(current, ','));
            }
            players.put(key.charAt(0), rgb);
        }
    }

    
    /**
     * Given a txt file which contains the characters representing 
     * elements of the terrain, it loads the txt file 
     * and creates an upsized version of the terrain 
     * @param layoutFile, the path to the txt layout file
     */
    public void loadLevel(String layoutFile) {
        layout = loadStrings(layoutFile); // Retrieves the level.txt file into an array

        int width = 0;
        for (String line : layout) {
            if (line.length() > width) {
                width = line.length();
            }
        }

        height = layout.length;

        scaledWidth = width * CELLSIZE;
        scaledHeight = height * CELLHEIGHT;

        layoutScaled = new char[scaledHeight][scaledWidth]; // Create a new scaled terrain array

        // fills every pixel in 'layoutScaled' to be ' '
        for (char[] row : layoutScaled) {
            Arrays.fill(row, ' ');
        }

        // fills the scaled terrain array with 'X' based on the positions of 'X' in the original txt file
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length(); y++) {
                if (layout[x].charAt(y) == 'X') {
                    for (int i = x * CELLSIZE; i < layoutScaled.length; i++) {
                        for (int j = y * CELLHEIGHT; j < y * CELLHEIGHT + CELLHEIGHT; j++) {
                            layoutScaled[i][j] = 'X';
                        }
                    }
                }
            }
        }
    }

    /**
     * Given an unsmoothed version of the terrain, by moving average, the method smoothes the terrain
     * by iterating through each column pixel of the unsmoothed array and counts the average height
     * of the next 32 pixels. The average height of that iteration is then assigned to that column.
     * The final smoothed array is then copied to the original array.
     * @param unsmoothed, the unsmoothed layout that needs to be smoothed. The unsmoothed layout contains
     * 'X' in certain positions which indicates the terrain
     */
    public void smoothing(char[][] unsmoothed) {
        char[][] smoothed = new char[scaledHeight][scaledWidth];
    
        for (int y = 0; y + 31 < unsmoothed[0].length; y++) { // Moving average
            int sum = 0;
            for (int count = 0; count < 32; count++) {
                int x = 0;
                while (x < unsmoothed.length && unsmoothed[x][y + count] != 'X') {
                    x++;
                }
                sum += x;
            }
            int average = sum / 32;
            smoothed[average][y] = 'X';
            for (int a = average; a < smoothed.length; a++) { // Fills bottom of X
                smoothed[a][y] = 'X';
            }
        }
        for (int i = 0; i < smoothed.length; i++) { // Copy
            for (int j = 0; j + 31 < smoothed[i].length; j++) {
                unsmoothed[i][j] = smoothed[i][j];
            }
        }
    }
    

    /**
     * Puts the element 'T' and the players into the scaled layout 
     */
    public void putTree() {
        String layout[] = loadStrings(layouts.get(currentLevelIndex));
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length(); y++) {
                char ch = layout[x].charAt(y);
                if (ch == 'T') {
                    Random random = new Random();
                    int tree_random = random.nextInt(CELLSIZE);
                    
                    int tree_height = 0;
                    for (int i = 0; i < scaledHeight; i++) {
                        if (layoutScaled[i][CELLHEIGHT * y + tree_random] == 'X') {
                            tree_height = i;
                            break;
                        }
                    }
                    layoutScaled[tree_height - 1][CELLHEIGHT * y + tree_random] = 'T';
                }
                else if (ch != ' ' && ch != 'X') {
                    int tank_height = 0;
                    for (int i = 0; i < scaledHeight; i++) {
                        if (layoutScaled[i][CELLHEIGHT * y] == 'X') {
                            tank_height = i;
                            break;
                        }
                    }
                    layoutScaled[tank_height - 1][CELLHEIGHT * y] = ch;
                }
            }
        }
    }

    /**
     * Draws out the terrain
     */
    public void drawTerrain() {
        for (int y = 0; y < layoutScaled[0].length; y++) {
            for (int x = 0; x < layoutScaled.length; x++) {
                char ch = layoutScaled[x][y];
                if (ch == 'X') {
                    String currentForegroundColor = foregroundColors.get(currentLevelIndex);
                    int rgb[];
                    rgb = PApplet.parseInt(split(currentForegroundColor, ','));
                    fill(rgb[0], rgb[1], rgb[2]);
                    rect(y, x, 1, HEIGHT - x);
                    noStroke();
                    break;
                }
                else if (ch == 'T') {
                    int X = y * CELLSIZE;
                    int Y = x * CELLHEIGHT;
                    String currentTree = trees.get(currentLevelIndex);

                    if (currentTree.equals("tree1.png")) {
                        image(tree1, y - 16, x - CELLSIZE, CELLSIZE, CELLHEIGHT);
                    } 
                    else if (currentTree.equals("tree2.png")) {
                        image(tree2, y - 16, x - CELLSIZE , CELLSIZE, CELLHEIGHT);
                    }
                }
            }
        }
    }

    
    /**
     * Initializes the tanks to create new tank objects and add them to a list.
     */
    public void initializeTanks() {
        // Store previous scores before clearing tanks
        Map<Character, Integer> previousScores = new HashMap<>();
        for (Tank tank : tanks) {
            previousScores.put(tank.getName(), tank.getScore());
        }

        tanks.clear();
        tanksCopy.clear(); // Clear and rebuild tanksCopy too

        for (int x = 0; x < layoutScaled.length; x++) {
            for (int y = 0; y < layoutScaled[x].length; y++) {
                char ch = layoutScaled[x][y];
                if (ch != ' ' && players.containsKey(ch)) {
                    int[] currentColor = players.get(ch);
                    Tank object = new Tank(y, x - CELLSIZE, currentColor, ch);

                    // Restore previous score if it exists
                    if (previousScores.containsKey(ch)) {
                        object.setScore(previousScores.get(ch));
                    }

                    tanks.add(object);
                    tanksCopy.add(object);
                    if (!playerScores.containsKey(ch)) {
                        playerScores.put(ch, 0);
                    }
                }
            }
        }

        // Sort the tanks in order
        Collections.sort(tanks, new Comparator<Tank>() {
            public int compare(Tank firstTank, Tank secondTank) {
                int comparator = Character.compare(firstTank.getName(), secondTank.getName());
                if (comparator == 0) {
                    return Integer.compare(firstTank.getName(), secondTank.getName());
                } else {
                    return comparator;
                }
            }
        });
        Collections.sort(tanksCopy, new Comparator<Tank>() {
            public int compare(Tank firstTank, Tank secondTank) {
                int comparator = Character.compare(firstTank.getName(), secondTank.getName());
                if (comparator == 0) {
                    return Integer.compare(firstTank.getName(), secondTank.getName());
                } else {
                    return comparator;
                }
            }
        });
    }
    
    
    /**
     * Draws out the tanks in the terrain.
     */
    public void drawTank() {
        for (Tank tank : tanks) {
            tank.display(this);
            if (tank.parachuteDeployed) {
                image(parachuteImage, tank.x - 12, tank.y - 10);
            }
        }
    }

    /**
     * Draws out the projectile in the terrain.
     */
    public void drawProjectile() {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            if (projectile.exploded || projectile.checkCollisionWithTerrain(projectile.rx, projectile.ry) || projectile.isOutOfBounds()) {
                if (!projectile.isOutOfBounds()) {
                    projectile.explode(this);
                }
                iterator.remove();
                nextTurn(); // Move turn change here
                windRandom = random.nextInt(71) - 35;
            } else {
                projectile.drawProjectile(this);
            }
        }
    }

    public void nextTurn() {
        // Remember the score of dying tank
        boolean currentTankDied = false;
        if (currentTank.getTankHealth() <= 0 || currentTank.isOutOfBounds()) {
            currentTank.setScore(0); // Reset score if tank kills itself
            currentTankDied = true;
        }

        // Remove dead tanks
        Iterator<Tank> iterator = tanks.iterator();
        while (iterator.hasNext()) {
            Tank tank = iterator.next();
            if (tank.getTankHealth() <= 0 || tank.isOutOfBounds()) {
                // Update score in tanksCopy before removal
                for (Tank copyTank : tanksCopy) {
                    if (copyTank.getName() == tank.getName()) {
                        copyTank.setScore(tank.getScore());
                        break;
                    }
                }
                iterator.remove();
            }
        }

        // Move to next alive tank
        if (tanks.size() > 1) {
            if (!currentTankDied) {
                // Normal turn progression
                currentTurnIndex = (currentTurnIndex + 1) % tanks.size();
            } else {
                // If current tank died, don't increment turn index
                // This will effectively skip to the next tank in the list
                // since the current tank was removed
                currentTurnIndex = currentTurnIndex % tanks.size();
            }
            currentTank = tanks.get(currentTurnIndex);
        }
    }

    /**
     * Given a background, we place that background as the background of the terrain
     * @param background, The filename of the background
     */
    public void putBackground(String background) {
        PImage backgroundImage = loadImage("src/main/resources/Tanks/" + background);
        background(backgroundImage);
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        // Ignore key presses during transitions, when game is over, or when projectile is active
        if (isTransitioning || isGameOver || !projectiles.isEmpty()) {
            return;
        }

        int key = event.getKeyCode();
        Tank currentTank = tanks.get(currentTurnIndex);
        
        if (key == UP) {
            currentTank.turnTurretLeft((float) 0.05);
            //turret moves left +3 rad /s
        }
        else if (key == DOWN) {
            currentTank.turnTurretRight((float) 0.05);
            //Turret moves right - 3 rad /s
        }
        else if (key == LEFT) {
            currentTank.moveLeft();
            //tank moves to left - 60 pixels /sec
        }
        else if (key == RIGHT) {
            currentTank.moveRight();
            //tank moves to right + 60 pixels / sec
        }
        else if (key == 'w' || key == 'W') {
            //Increase turret power + 36 units / sec
            currentTank.increasePower(1);
        }
        else if (key == 's' || key == 'S') {
            currentTank.decreasePower(1);
            //Decrease turret power - 36 units / sec
        }
        else if (key == 32) { // Spacebar key
            if (currentTurnIndex < tanks.size()) {
                currentTank.fireProjectile(windRandom); // Remove turn change from here
            }
        }
        else if (key == 'r' || key == 'R') {
            currentTank.repair();
            //Repair tank by 20 health cost 20 pts
        }
        else if (key == 'f' || key == 'F') {
            currentTank.refuel();
            //Add fuel by 200 cost 10 pts
        }
        else if (key == 'p'|| key == 'P') {
            currentTank.addParachute();
        }
    }

    /**
     * Displays HUD elements 
     */
    private void displayHUD() {
        fill(0);
        textSize(18);
        // Player Turn
        // currentTank = tanks.get(currentTurnIndex);
        Character currentPlayerName = currentTank.getName();
        String currentPlayerNameString = Character.toString(currentPlayerName);
        String currentPlayer = "Player " + currentPlayerNameString + "'s turn";
        text(currentPlayer, 20, 26);

        // Health Bar
        fill(0);
        float currentHealth = Math.max(0, (float) currentTank.getTankHealth());
        text("Health:", 320, 26);

        float maxHealth = 100;
        float healthWidth = 200 * (currentHealth/maxHealth); // How much health is left to fill the colored bar

        // Power
        int currentPower = currentTank.getTankPower();
        text("Power:", 320, 55); // Power Text
        text(currentPower, 385, 55); // Power Number

        // Wind 
        if (windRandom >= 0) {
            image(windRight, 740, 10);
            text((int) windRandom, 795, 44); 
        }
        else if (windRandom < 0) {
            image(windLeft, 740, 10);
            text((int) windRandom, 795, 44); 
        }

        // Fuel
        image(fuelImage, 165, 10);
        int currentFuel = currentTank.getTankFuel();
        fill(0);
        text(currentFuel, 190, 26);

        // Parachutes
        int currentParachute = currentTank.getTankParachute();
        image(parachuteImage, 160, 40);
        fill(0);
        text(currentParachute, 190, 62);

        fill(0, 0, 255); // Blue HP bar
        rect(385, 12, healthWidth, 14); // HP bar based on current HP

        noFill();
        stroke(128); // Gray Border
        strokeWeight(4); // Thickness
        rect(385, 12, -3 + currentPower * 2, 14); // First half of the Border

        stroke(255, 0, 0); // Red
        strokeWeight(3); // Thickness
        line(385 + currentPower * 2, 8, 385 + currentPower * 2, 12 + 20); // Line in the middle of the HP bar

        noFill();
        stroke(0); // Black Border
        strokeWeight(4); // Thickness
        rect(387 + currentPower * 2, 12, 197 - currentPower * 2, 14); // Second half of the Border

        fill(0);
        noStroke();
        text((int) currentHealth, 590, 26); // Display health number
    }

    /**
     * Display Scoreboard
     */
    private void displayScoreboard() {
        textSize(16);
        int startX = 700;
        int startY = 70;

        pushStyle(); // Save all current things that have been drawn

        // Scores
        noFill();
        stroke(0); // Black Border
        strokeWeight(3); // Thickness
        rect(startX, startY, 150, 20);
        fill(0);
        text("Scores:", startX + 8, startY + 16);

        // Player Scores Border
        noFill();
        stroke(0); // Black Border
        strokeWeight(3); // Thickness
        rect(startX, startY + 20, 150, 100);

        int changeY = 0;
        Set<Character> displayedPlayers = new HashSet<>();

        for (Tank currentTank : tanksCopy) {
            if (!displayedPlayers.contains(currentTank.getName())) {
                displayedPlayers.add(currentTank.getName());

                // Find matching tank in active tanks list to get current score
                int currentScore = 0;
                for (Tank activeTank : tanks) {
                    if (activeTank.getName() == currentTank.getName()) {
                        currentScore = activeTank.getScore();
                        break;
                    }
                }

                Character currentPlayerName = currentTank.getName();
                String currentPlayerNameString = Character.toString(currentPlayerName);
                String currentPlayer = "Player " + currentPlayerNameString;

                fill(currentTank.color[0], currentTank.color[1], currentTank.color[2]);
                text(currentPlayer, startX + 5, startY + 40 + changeY);
                text(currentScore, startX + 115, startY + 40 + changeY);
                changeY += 25;
            }
        }

        popStyle(); // Restore all things that have been drawn
    }

    /**
     * Draw final scoreboard when the game ends
     */
    public void drawGameOver() {
        if (isGameOver) {
            background(0, 150);
            fill(255);
            textAlign(CENTER);
            textSize(32);

            // Sort tanks by score
            java.util.List<Tank> sortedTanks = new ArrayList<>(tanksCopy);
            Collections.sort(sortedTanks, (t1, t2) -> Integer.compare(t2.getScore(), t1.getScore()));

            // Check for tie (if highest score appears multiple times)
            int highestScore = sortedTanks.get(0).getScore();
            java.util.List<Tank> winners = sortedTanks.stream()
                .filter(t -> t.getScore() == highestScore)
                .collect(java.util.stream.Collectors.toList());

            // Display winner(s)
            if (winners.size() == 1) {
                String message = "Player " + winners.get(0).getName() + " wins!";
                text(message, WIDTH/2, HEIGHT/2 - 100);
            } else {
                String tieMessage = "Tie between Players ";
                for (int i = 0; i < winners.size(); i++) {
                    tieMessage += winners.get(i).getName();
                    if (i < winners.size() - 1) {
                        tieMessage += " & ";
                    }
                }
                text(tieMessage, WIDTH/2, HEIGHT/2 - 100);
            }

            // Display final scoreboard
            textSize(24);
            text("Final Scores:", WIDTH/2, HEIGHT/2 - 40);
            
            int yOffset = 0;
            for (Tank tank : sortedTanks) {
                fill(tank.getColor()[0], tank.getColor()[1], tank.getColor()[2]);
                String scoreText = String.format("Player %c: %d", tank.getName(), tank.getScore());
                text(scoreText, WIDTH/2, HEIGHT/2 + yOffset);
                yOffset += 30;
            }
        }
    }

    /**
     * Check whether the current level has ended
     */
    public void checkLevelEnd() {
        int aliveTanks = 0;
        for (Tank tank : tanks) {
            if (tank.getTankHealth() > 0) {
                aliveTanks++;
            }
        }
        if (aliveTanks <= 1 && !isLevelOver) {
            isLevelOver = true;
        }
    }
 
    /**
     * Move to the next level
     */
    public void changeLevel() {
        if (isLevelOver) {
            if (!isTransitioning) {
                // Start transition only if there are no active projectiles
                if (projectiles.isEmpty()) {
                    isTransitioning = true;
                    transitionTimer = TRANSITION_DELAY;
                }
            } else {
                // Count down the transition timer
                if (transitionTimer > 0) {
                    transitionTimer--;
                    return;
                }

                // Proceed with level change after delay
                if (tanks.size() <= 1) {
                    currentLevelIndex++;
                    if (currentLevelIndex < layouts.size()) {
                        loadLevel(layouts.get(currentLevelIndex));
                        smoothing(layoutScaled);
                        smoothing(layoutScaled);
                        putTree();
                        initializeTanks();
                        for (Tank tank : tanks) {
                            tank.resetStats();
                        }
                        currentTurnIndex = 0;
                        currentTank = tanks.get(currentTurnIndex);
                        isLevelOver = false;
                        isTransitioning = false;
                    } else {
                        isGameOver = true;
                    }
                }
            }
        }
    }

    public static void setLevelOver() {
        currentTank = tanks.get(0);
        instance.isLevelOver = true;
    }

    /**
     * Restart the game and re-initializes everything
     */
    public void restartGame() {
        currentLevelIndex = 0;
        isGameOver = false;
        isLevelOver = false;
        tanks.clear();
        loadLevel(layouts.get(currentLevelIndex));
        smoothing(layoutScaled);
        smoothing(layoutScaled);
        putTree();
        initializeTanks();
        for (Tank tank : tanks) {
            tank.resetStats();
        }
        currentTank = tanks.get(currentTurnIndex);
    }



    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        if (!isGameOver) {
            putBackground(backgrounds.get(currentLevelIndex));
            drawTerrain();
            drawTank();
            drawProjectile();

            displayHUD();

            displayScoreboard();

            checkLevelEnd(); //Check level end
            changeLevel(); // Called when level is ended
            drawGameOver();
        }
    }



    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}
