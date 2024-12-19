import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

class SelectPanel extends JPanel {
    private BorderedLabel titleLabel2P;
    private BorderedLabel titleLabelMulti;
    private static Font customFont;
    private SelectButton button2P;
    private SelectButton buttonMulti;  

    static {
        try {
            InputStream is = SelectPanel.class.getResourceAsStream("/resources/fonts/CookieRun Regular.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("폰트 로드 실패: " + e.getMessage());
            customFont = new Font("Dialog", Font.BOLD, 36);
        }
    }

    public SelectPanel() {
        setLayout(null);
        setOpaque(false);

        // 라벨 초기화
        titleLabel2P = new BorderedLabel("2인 매칭", new Color(0xFFFEF0), new Color(0x8D6219),
                new Color(0x9c6304), null, null);
        titleLabelMulti = new BorderedLabel("다인 매칭", new Color(0xFFFEF0), new Color(0x8D6219),
                new Color(0x9c6304), null, null);

        // 버튼 초기화 및 스타일링
        button2P = createStyledButton("");
        buttonMulti = createStyledButton("");

        // 컴포넌트 추가
        add(titleLabel2P);
        add(titleLabelMulti);
        add(button2P);
        add(buttonMulti);

        // 버튼 이벤트 핸들러
        button2P.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "2인 매칭을 시작합니다.");
        });

        buttonMulti.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "다인 매칭을 시작합니다.");
        });
    }

    private SelectButton createStyledButton(String text) {
        // 적당한 보라색 계열 색상 사용
        SelectButton button = new SelectButton(text, new Color(0xffc30b));
        return button;
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
        
        double scaleX = (double)panelWidth / baseWidth;
        double scaleY = (double)panelHeight / baseHeight;
        double scale = Math.min(scaleX, scaleY);
        
        double offsetX = (panelWidth - (baseWidth * scale)) / 2.0;

        // 버튼과 라벨 크기/위치 계산
        int buttonSize = (int)(200 * scale);
        int verticalGap = (int)(50 * scale);
        
        // 첫 번째 버튼 영역
        int firstButtonY = (int)(panelHeight * 0.25);
        button2P.setBounds(
            (int)(offsetX + (buttonSize - buttonSize*0.8)/2),  // 버튼 크기를 80%로 조정
            (int)(firstButtonY + (buttonSize - buttonSize*0.8)/2),
            (int)(buttonSize*0.8),
            (int)(buttonSize*0.8)
        );
        titleLabel2P.setFont(customFont.deriveFont((float)(30.0 * scale)));
        titleLabel2P.setBounds(
            (int)(offsetX + (buttonSize - titleLabel2P.getPreferredSize().width) / 2),
            firstButtonY + (buttonSize - titleLabel2P.getPreferredSize().height) / 2,
            titleLabel2P.getPreferredSize().width,
            titleLabel2P.getPreferredSize().height
        );

         // 두 번째 버튼 영역
         int secondButtonY = firstButtonY + buttonSize + verticalGap;
         buttonMulti.setBounds(
             (int)(offsetX + (buttonSize - buttonSize*0.8)/2),  // 버튼 크기를 80%로 조정
             (int)(secondButtonY + (buttonSize - buttonSize*0.8)/2),
             (int)(buttonSize*0.8),
             (int)(buttonSize*0.8)
         );
        titleLabelMulti.setFont(customFont.deriveFont((float)(30.0 * scale)));
        titleLabelMulti.setBounds(
            (int)(offsetX + (buttonSize - titleLabelMulti.getPreferredSize().width) / 2),
            secondButtonY + (buttonSize - titleLabelMulti.getPreferredSize().height) / 2,
            titleLabelMulti.getPreferredSize().width,
            titleLabelMulti.getPreferredSize().height
        );
    }
}
