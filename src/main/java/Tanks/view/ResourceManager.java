// Tanks/view/ResourceManager.java
package Tanks.view;

import Tanks.model.level.Level;
import processing.core.PApplet;
import processing.core.PImage;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static final int FUEL_IMAGE_SIZE = 20;
    private static final int WIND_IMAGE_SIZE = 48;
    private static final int PARACHUTE_SIZE = 32;

    private final PApplet applet;
    private final Map<String, PImage> images;
    private final String resourcePath = "src/main/resources/Tanks/";

    public ResourceManager(PApplet applet) {
        this.applet = applet;
        this.images = new HashMap<>();
    }

    public void loadAllResources() {
        // Load backgrounds
        loadImage("basic", "basic.png");
        loadImage("desert", "desert.png");
        loadImage("forest", "forest.png");
        loadImage("hills", "hills.png");
        loadImage("snow", "snow.png");

        // Load game elements
        loadImage("fuel", "fuel.png");
        loadImage("parachute", "parachute.png");
        loadImage("tree1", "tree1.png");
        loadImage("tree2", "tree2.png");

        // Load wind indicators
        loadImage("windLeft", "wind-1.png");
        loadImage("windRight", "wind.png");

        // Resize images
        resizeGameImages();
    }

    private void loadImage(String key, String filename) {
        try {
            PImage img = applet.loadImage(resourcePath + filename);
            if (img == null) {
                System.err.println("Failed to load image: " + filename);
                return;
            }
            images.put(key, img);
        } catch (Exception e) {
            System.err.println("Error loading image " + filename + ": " + e.getMessage());
        }
    }

    private void resizeGameImages() {
        // Resize utility images
        if (images.containsKey("fuel")) {
            images.get("fuel").resize(FUEL_IMAGE_SIZE, FUEL_IMAGE_SIZE);
        }
        if (images.containsKey("parachute")) {
            images.get("parachute").resize(PARACHUTE_SIZE, PARACHUTE_SIZE);
        }
        if (images.containsKey("windLeft")) {
            images.get("windLeft").resize(WIND_IMAGE_SIZE, WIND_IMAGE_SIZE);
        }
        if (images.containsKey("windRight")) {
            images.get("windRight").resize(WIND_IMAGE_SIZE, WIND_IMAGE_SIZE);
        }
    }

    public PImage getImage(String key) {
        PImage image = images.get(key);
        if (image == null) {
            System.err.println("Image not found: " + key);
            // Return a default image or placeholder
            return createPlaceholderImage();
        }
        return image;
    }

    public PImage getBackgroundForLevel(Level level) {
        String backgroundFile = level.getBackground();
        String key = backgroundFile.replace(".png", "");
        return getImage(key);
    }

    public PImage getTreeImage(String treeFile) {
        if (treeFile == null) return null;
        String key = treeFile.replace(".png", "");
        return getImage(key);
    }

    private PImage createPlaceholderImage() {
        // Create a simple placeholder image (pink rectangle)
        PImage placeholder = applet.createImage(32, 32, PApplet.RGB);
        placeholder.loadPixels();
        for (int i = 0; i < placeholder.pixels.length; i++) {
            placeholder.pixels[i] = applet.color(255, 0, 255);
        }
        placeholder.updatePixels();
        return placeholder;
    }

    // Helper methods to check resource existence
    public boolean hasResource(String key) {
        return images.containsKey(key);
    }

    public int getResourceCount() {
        return images.size();
    }

    // Get resource path
    public String getResourcePath() {
        return resourcePath;
    }
}