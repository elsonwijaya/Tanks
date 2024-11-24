package Tanks.model.level;

public class Level {
    private final String layoutFile;
    private final String background;
    private final String foregroundColor;
    private final String trees;

    public Level(String layoutFile, String background, String foregroundColor, String trees) {
        this.layoutFile = layoutFile;
        this.background = background;
        this.foregroundColor = foregroundColor;
        this.trees = trees;
    }

    public String getLayoutFile() { return layoutFile; }
    public String getBackground() { return background; }
    public String getForegroundColor() { return foregroundColor; }
    public String getTreeFile() { return trees; }
}