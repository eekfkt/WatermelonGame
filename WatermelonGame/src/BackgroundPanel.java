import java.awt.*;
import javax.swing.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private GamePanel gamePanel;
    private ScorePanel scorePanel;
    private double scale = 1.0;

    public BackgroundPanel() {
        setLayout(new GridBagLayout());
        backgroundImage = new ImageIcon(getClass().getResource("/resources/base/background.png")).getImage();
        
        gamePanel = new GamePanel();
        scorePanel = new ScorePanel();
        scorePanel.setGamePanel(gamePanel);
        // GamePanel의 점수 업데이트를 ScorePanel에 연결
        gamePanel.setScoreUpdateListener(score -> {
            scorePanel.updateScore(score);
        });
        
        // GamePanel 배치
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);
        add(gamePanel, gbc);
        
        // ScorePanel 배치
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 20, 20, 0);
        add(scorePanel, gbc);

        // 빈 패널
        gbc.gridx = 2;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gbc);
    }

    public void setScale(double scale) {
        this.scale = scale;
        repaint();
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(scale, scale);

            
            
        // 배경 이미지 그리기
        int width = (int)(getWidth() / scale);
        int height = (int)(getHeight() / scale);
        g2d.drawImage(backgroundImage, 0, 0, width, height, null);

    }
}
