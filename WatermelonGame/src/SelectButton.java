import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

public class SelectButton extends JButton {
    private Color mainColor;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public SelectButton(String text, Color mainColor) {
        super(text);
        this.mainColor = mainColor;
        setup();
    }

    private void setup() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        
        // 기본 색상에서 밝은/어두운 변형 생성
        Color lighterColor = new Color(
            Math.min(mainColor.getRed() + 30, 255),
            Math.min(mainColor.getGreen() + 30, 255),
            Math.min(mainColor.getBlue() + 30, 255)
        );
        Color darkerColor = new Color(
            Math.max(mainColor.getRed() - 30, 0),
            Math.max(mainColor.getGreen() - 30, 0),
            Math.max(mainColor.getBlue() - 30, 0)
        );

        // 그라데이션 설정
        GradientPaint gradient;
        if (isPressed) {
            gradient = new GradientPaint(0, 0, darkerColor, 0, height, mainColor);
        } else if (isHovered) {
            gradient = new GradientPaint(0, 0, lighterColor, 0, height, lighterColor);
        } else {
            gradient = new GradientPaint(0, 0, lighterColor, 0, height, mainColor);
        }

        // 버튼 본체 그리기
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 3, width, height-3, 10, 10);

        // 상단 하이라이트
        if (!isPressed) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRoundRect(0, 0, width, height/2, 10, 10);
        }

        // 하단 그림자
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(0, height-5, width, 5, 10, 10);

        // 텍스트 그리기
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(getText(), g2);
        int x = (width - (int) r.getWidth()) / 2;
        int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
        
        if (isPressed) {
            x += 1;
            y += 1;
        }

        g2.setColor(Color.WHITE);
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }
}
