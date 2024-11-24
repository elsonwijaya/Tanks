package Tanks.model.utils;

import processing.core.PApplet;

public class FileLoader {
    private final PApplet applet;

    public FileLoader(PApplet applet) {
        this.applet = applet;
    }

    public String[] loadLayoutFile(String filename) {
        try {
            String[] lines = applet.loadStrings(filename);
            System.out.println("Loading layout: " + filename);  // Debug
            System.out.println("Lines loaded: " + (lines != null ? lines.length : 0));  // Debug
            return lines;
        } catch (Exception e) {
            System.err.println("Error loading layout file: " + filename);
            e.printStackTrace();
            return new String[]{"X X X"};
        }
    }
}