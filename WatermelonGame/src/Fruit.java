// Fruit.java
import javax.swing.*;
import java.awt.*;


public class Fruit {
    private final String name;
    private final float radius;
    private final Image image;

    public Fruit(String name, float radius, String imagePath) {
        this.name = name;
        this.radius = radius;
        this.image = new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    public String getName() { return name; }
    public float getRadius() { return radius; }
    public Image getImage() { return image; }
}