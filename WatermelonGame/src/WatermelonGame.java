import java.awt.*;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WatermelonGame extends JFrame {
    private static final int ORIGINAL_WIDTH = 1080;
    private static final int ORIGINAL_HEIGHT = 800;
    private double scaleFactor = 1.0;
    
    public WatermelonGame() {
        setTitle("Watermelon Game");
        setSize(ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel containerPanel = new BackgroundPanel();
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GamePanel gamePanel = new GamePanel();
        
        // 크기 변경 리스너 추가
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleFactor = Math.min(
                    getWidth() / (double)ORIGINAL_WIDTH,
                    getHeight() / (double)ORIGINAL_HEIGHT
                );
                
                // GamePanel 크기 조정
                Dimension newSize = new Dimension(
                    (int)(520 * scaleFactor),
                    (int)(685 * scaleFactor)
                );
                gamePanel.setPreferredSize(newSize);
                gamePanel.setScale(scaleFactor);
                
                // ScorePanel 크기 조정
                gamePanel.scorePanel.setScale(scaleFactor);
                
                containerPanel.revalidate();
            }
        });

        // ScorePanel을 GamePanel의 오른쪽에 배치
        GridBagConstraints gbc = new GridBagConstraints();

        // GamePanel 제약조건
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;  // 수평 확장 허용
        gbc.weighty = 0;  // 수직 확장 허용
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 10);  // 오른쪽 여백 추가
        containerPanel.add(gamePanel, gbc);

        // ScorePanel 제약조건
        gbc.gridx = 1;
        gbc.weightx = 0;  // 수평 확장 없음
        gbc.weighty = 0;  // 수직 확장 없음
        gbc.fill = GridBagConstraints.NONE;  // 크기 고정
        gbc.insets = new Insets(0, 0, 0, 0);  // 여백 초기화
        gbc.anchor = GridBagConstraints.NORTH;  // 상단 정렬
        containerPanel.add(gamePanel.scorePanel, gbc);
        
        add(containerPanel);
        setLocationRelativeTo(null);
        setResizable(true); // 크기 조정 가능하도록 설정
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // 스윙 이벤트 디스패치 스레드에서 실행
            WatermelonGame game = new WatermelonGame(); // 게임 인스턴스 생성
            game.setVisible(true); // 창을 보이게 설정
        });
    }
}

