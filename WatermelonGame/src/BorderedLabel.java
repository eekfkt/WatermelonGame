import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

class BorderedLabel extends JLabel {
    private final Color textColor;
    private final Color outerBorder;
    private final Color innerBorder;
    private final Color gradientStart;
    private final Color gradientEnd;
    private final boolean useGradient;
    private static Font customFont;

    // static 블록에서 폰트 초기화
    static {
        try {
            InputStream is = BorderedLabel.class.getResourceAsStream("/resources/fonts/CookieRun Regular.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("폰트 로드 실패: " + e.getMessage());
            customFont = new Font("Dialog", Font.BOLD, 36);
        }
    }

    // 기본 생성자
    public BorderedLabel(String text) {
        this(text, new Color(0xFFFEF0), new Color(0x8D6219), new Color(0xD8AA65), null, null);
    }

    // 모든 색상을 지정하는 생성자
    public BorderedLabel(String text, Color textColor, Color outerBorder, Color innerBorder, 
                        Color gradientStart, Color gradientEnd) {
        super(text);
        this.textColor = textColor;
        this.outerBorder = outerBorder;
        this.innerBorder = innerBorder;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
        this.useGradient = (gradientStart != null && gradientEnd != null);
        setFont(customFont); // 기본 폰트 설정
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String text = getText();
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        // 외부 테두리
        g2d.setColor(outerBorder);
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(i) == 2 || Math.abs(j) == 2) {
                    g2d.drawString(text, x + i, y + j);
                }
            }
        }

        // 내부 테두리
        g2d.setColor(innerBorder);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (Math.abs(i) == 1 || Math.abs(j) == 1) {
                    g2d.drawString(text, x + i, y + j);
                }
            }
        }

        // 텍스트 (그라데이션 또는 단색)
        if (useGradient) {
            GradientPaint gradient = new GradientPaint(
                    x, y - fm.getAscent(), gradientStart,
                    x, y, gradientEnd);
            g2d.setPaint(gradient);
        } else {
            g2d.setColor(textColor);
        }
        g2d.drawString(text, x, y);

        g2d.dispose();
    }
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // 여백을 포함한 크기 반환
        return new Dimension(
            size.width + (5 * 2),
            size.height + (5 * 2)
        );
    }
}