import javax.swing.*;
import java.awt.*;

public class WatermelonGame extends JFrame {
    private static final int ORIGINAL_WIDTH = 1680;
    private static final int ORIGINAL_HEIGHT = 1050;
    private BackgroundPanel backgroundPanel;

    public WatermelonGame() {
        setTitle("Watermelon Game");
        setSize(ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(ORIGINAL_WIDTH/2, ORIGINAL_HEIGHT/2));
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        backgroundPanel = new BackgroundPanel();
        add(backgroundPanel, gbc);

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WatermelonGame game = new WatermelonGame();
            game.setVisible(true);
        });
    }
}

