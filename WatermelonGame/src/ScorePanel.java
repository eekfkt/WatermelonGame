import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.*;

class ScorePanel extends JPanel {
    @SuppressWarnings("unused")
    private int currentScore = 0;
    private JLabel scoreLabel;
    private JLabel titleLabel;
    private Image bubbleImage;
    private double scale = 1.0;
    private static Font customFont;
    // static 블록에서 폰트 초기화
    static {
        try {
            InputStream is = ScorePanel.class.getResourceAsStream("/resources/fonts/CookieRun Regular.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("폰트 로드 실패: " + e.getMessage());
            customFont = new Font("Dialog", Font.BOLD, 36);
        }
    }
    
    public ScorePanel() {
        setLayout(null); // 절대 위치 사용
        setOpaque(false); // 패널을 투명하게 설정
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 이미지 로드
        try {
            bubbleImage = new ImageIcon(getClass().getResource("/resources/base/bubble.png")).getImage();
        } catch (Exception e) {
            System.err.println("이미지를 로드할 수 없습니다: " + e.getMessage());
        }
        
        titleLabel =  new BorderedLabel("점수", new Color(0xFFFEF0), new Color(0x8D6219), 
        new Color(0xD8AA65), null, null); 
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        scoreLabel = new BorderedLabel("0", null, new Color(0x8D6219), new Color(0xD8AA65), 
        new Color(0xFFFEF0), new Color(0xF8C951)); 
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(titleLabel);
        add(scoreLabel);
    }

    public void setScale(double scale) {
        this.scale = scale;
        updateLayout();
    }
    
    private void updateLayout() {
        // 컴포넌트 크기와 위치 조정
        int width = (int) (200 * scale);
        
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, (int) (40 * scale)));
        titleLabel.setBounds(0, 00, width, (int) (50 * scale));
        
        scoreLabel.setFont(customFont.deriveFont(Font.BOLD, (int) (50 * scale)));
        scoreLabel.setBounds(0, (int) (50 * scale), width, (int) (130 * scale));

        
        revalidate();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 배경 이미지 그리기
        if (bubbleImage != null) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(bubbleImage, 0, 0, getWidth(), getHeight(), null);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)(200 * scale), (int)(200 * scale));
    }
    
    public void updateScore(int score) {
        currentScore = score;
        scoreLabel.setText(String.valueOf(score));
    }
}