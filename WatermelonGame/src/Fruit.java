// Fruit.java
import javax.swing.*;
import java.awt.*;


public class Fruit {
    private final String name;
    private final float radius;
    private final Image image;
    private final int picWidth;
    private final int picHeight;

    public Fruit(String name, float radius, String imagePath, int picWidth, int picHeight) {
        this.name = name;
        this.radius = radius;
        this.image = new ImageIcon(getClass().getResource(imagePath)).getImage();
        this.picWidth = picWidth;
        this.picHeight = picHeight;
    }

    public String getName() { return name; }
    public float getRadius() { return radius; }
    public Image getImage() { return image; }
    public int getPicWidth() { return picWidth; }
    public int getPicHeight() { return picHeight; }
}