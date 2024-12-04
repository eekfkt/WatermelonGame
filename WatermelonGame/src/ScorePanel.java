import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.awt.geom.AffineTransform;

class ScorePanel extends JPanel {
    private BorderedLabel scoreLabel;
    private BorderedLabel titleLabel;
    private static Font customFont;
    private Image bubbleImage;
    private Image evolutionImage;

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
        setLayout(null); // null 레이아웃 사용
        //setOpaque(false);

        // 이미지 로드
        try {
            bubbleImage = new ImageIcon(getClass().getResource("/resources/base/bubble.png")).getImage();
            evolutionImage = new ImageIcon(getClass().getResource("/resources/base/evolution.png")).getImage();
        } catch (Exception e) {
            System.err.println("이미지를 로드할 수 없습니다: " + e.getMessage());
        }

         // 라벨 초기화 시 여백 추가
        titleLabel = new BorderedLabel("점수", new Color(0xFFFEF0), new Color(0x8D6219),
        new Color(0xD8AA65), null, null) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // 좌우 여백 20픽셀씩 추가
                return new Dimension(d.width + 40, d.height + 30);
            }
        };

        scoreLabel = new BorderedLabel("0", null, new Color(0x8D6219), new Color(0xD8AA65),
            new Color(0xFFFEF0), new Color(0xF8C951)) {
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            // 좌우 여백 20픽셀씩 추가
            return new Dimension(d.width + 40, d.height + 30);
        }
        };

        // 폰트 크기 설정
        titleLabel.setFont(customFont.deriveFont(24f));
        scoreLabel.setFont(customFont.deriveFont(24f));

        // 라벨 기본 크기 설정
        titleLabel.setSize(titleLabel.getPreferredSize());
        scoreLabel.setSize(scoreLabel.getPreferredSize());

        // 라벨 가시성 설정
        titleLabel.setVisible(true);
        scoreLabel.setVisible(true);

        add(titleLabel);
        add(scoreLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        float baseWidth = 200f;
        float baseHeight = 685f;
        
        // 스케일 계산
        double scaleX = (double)panelWidth / baseWidth;
        double scaleY = (double)panelHeight / baseHeight;
        double scale = Math.min(scaleX, scaleY);
        
        // 중앙 정렬 오프셋
        double offsetX = (panelWidth - (baseWidth * scale)) / 2.0;
        double offsetY = (panelHeight - (baseHeight * scale)) / 2.0;

        // 상하단 여백 설정 (전체 높이의 10%를 여백으로 설정)
        float verticalPadding = baseHeight * 0.05f;
        // 실제 이미지들이 차지할 수 있는 높이
        float availableHeight = baseHeight - (verticalPadding * 2);

        // 이미지 관련 계산
        float totalElements = 3.0f;
        float gapRatio = 0.03f;
        float imageRatio = (1.0f - (totalElements - 1) * gapRatio) / totalElements;
        int bubbleSize = (int)(availableHeight * imageRatio);
        int verticalGap = (int)(availableHeight * gapRatio);
        int imageX = (int)((baseWidth - bubbleSize) / 2);

        // 이미지 그리기 - 상단 여백을 고려한 Y 위치 계산
        AffineTransform originalTransform = g2d.getTransform();
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // verticalPadding을 시작 Y 위치에 추가
        float startY = verticalPadding;
        g2d.drawImage(bubbleImage, imageX, (int)startY, bubbleSize, bubbleSize, null);
        g2d.drawImage(bubbleImage, imageX, (int)(startY + bubbleSize + verticalGap), bubbleSize, bubbleSize, null);
        g2d.drawImage(evolutionImage, imageX, (int)(startY + (bubbleSize + verticalGap) * 2), bubbleSize, bubbleSize, null);

        // 라벨 위치 계산 조정
        double titleY = startY + bubbleSize * -0.17;
        double scoreY = startY + bubbleSize * 0.33;
        
        titleLabel.setFont(customFont.deriveFont((float)(40.0 * scale)));
        scoreLabel.setFont(customFont.deriveFont((float)(40.0 * scale)));
        
        Dimension titleSize = titleLabel.getPreferredSize();
        Dimension scoreSize = scoreLabel.getPreferredSize();
        
        titleLabel.setBounds(
            (int)(offsetX + scale * (imageX + bubbleSize/2 - titleSize.width/(2*scale))), 
            (int)(offsetY + scale * titleY),
            titleSize.width,
            titleSize.height
        );
        
        scoreLabel.setBounds(
            (int)(offsetX + scale * (imageX + bubbleSize/2 - scoreSize.width/(2*scale))), 
            (int)(offsetY + scale * scoreY),
            scoreSize.width,
            scoreSize.height
        );

        g2d.setTransform(originalTransform);
    }

    // 점수 업데이트 메서드
    public void updateScore(int score) {
        scoreLabel.setText(String.valueOf(score));
        repaint();
    }
}