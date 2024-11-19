import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;

import org.jbox2d.dynamics.*;
import org.jbox2d.common.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;

public class WatermelonGame extends JFrame {
    public WatermelonGame() {
        setTitle("Watermelon Game"); // 창 제목 설정
        setSize(620, 850); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 동작 설정
        setLocationRelativeTo(null); // 창을 화면 중앙에 배치
        setBackground(new Color(0xF7F4C8)); // 배경색 설정
        setResizable(false); // 창 크기 조정 불가 설정

       
        
        // JBox2D World 설정
        World world = new World(new Vec2(0.0f, 20.0f)); // 중력 설정

        // 패널 추가
        GamePanel panel = new GamePanel(world); // 게임 패널 생성
        add(panel); // 패널을 프레임에 추가
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // 스윙 이벤트 디스패치 스레드에서 실행
            WatermelonGame game = new WatermelonGame(); // 게임 인스턴스 생성
            game.setVisible(true); // 창을 보이게 설정
        });
    }
}

class GamePanel extends JPanel {
    private World world; // JBox2D 월드
    private static final Color WALL_COLOR = new Color(0xE6B143); // 벽 색상
    private Body leftWall; // 왼쪽 벽 바디
    private Body rightWall; // 오른쪽 벽 바디
    private Body ground; // 바닥 바디
    private Body topLine; // 탑 라인 바디
    private Timer timer; // 타이머
    private Body currentBody; // 현재 조작중인 과일 바디
    private Fruit currentFruit; // 현재 조작중인 과일 정보

    public GamePanel(World world) {
        this.world = world; // 월드 설정
        setBackground(new Color(0xF7F4C8)); // 패널 배경색 설정
        createWalls(); // 벽 생성

        addFruit(); // 과일 추가

        // 타이머 설정 (60 FPS)
        timer = new Timer(1000 / 60, new ActionListener() { // 60 FPS로 타이머 설정
            @Override
            public void actionPerformed(ActionEvent e) {
                world.step(1.0f / 60.0f, 6, 2); // 물리 엔진 스텝
                repaint(); // 패널 다시 그리기
            }
        });
        timer.start(); // 타이머 시작

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!currentBody.isAwake()) {  // 현재 과일이 멈춰있을 때만
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:  // A키
                            if (currentBody.getPosition().x > 30f/30.0f)
                                currentBody.setTransform(new Vec2(currentBody.getPosition().x - 1/30.0f, currentBody.getPosition().y), 0);
                            break;
                        case KeyEvent.VK_RIGHT: // D키
                            if (currentBody.getPosition().x < 590f/30.0f)
                                currentBody.setTransform(new Vec2(currentBody.getPosition().x + 1/30.0f, currentBody.getPosition().y), 0);
                            break;
                        case KeyEvent.VK_DOWN:  // S키
                            currentBody.setAwake(true);  // 과일 떨어뜨리기
                            
                            // 1초 후 새 과일 생성
                            Timer dropTimer = new Timer(1000, e1 -> addFruit());
                            dropTimer.setRepeats(false);
                            dropTimer.start();
                            break;
                    }
                }
            }
        });
        setFocusable(true);  // 키 이벤트 받을 수 있도록 설정
    }

    private void createWalls() {
        // 왼쪽 벽 생성
        BodyDef leftWallDef = new BodyDef(); // 바디 정의 생성
        leftWallDef.position.set(15 / 30.0f, 395 / 30.0f); // 위치 설정 (미터 단위)
        leftWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        leftWall = world.createBody(leftWallDef); // 월드에 바디 생성
        PolygonShape leftWallShape = new PolygonShape(); // 폴리곤 모양 생성
        leftWallShape.setAsBox(30 / 30.0f, 790 / 30.0f); // 박스 모양 설정
        leftWall.createFixture(leftWallShape, 0.0f); // 바디에 모양 추가

        // 오른쪽 벽 생성
        BodyDef rightWallDef = new BodyDef(); // 바디 정의 생성
        rightWallDef.position.set(605 / 30.0f, 395 / 30.0f); // 위치 설정 (미터 단위)
        rightWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        rightWall = world.createBody(rightWallDef); // 월드에 바디 생성
        PolygonShape rightWallShape = new PolygonShape(); // 폴리곤 모양 생성
        rightWallShape.setAsBox(30 / 30.0f, 790 / 30.0f); // 박스 모양 설정
        rightWall.createFixture(rightWallShape, 0.0f); // 바디에 모양 추가

        // 바닥 생성
        BodyDef groundDef = new BodyDef(); // 바디 정의 생성
        groundDef.position.set(310 / 30.0f, 820 / 30.0f); // 위치 설정 (미터 단위)
        groundDef.type = BodyType.STATIC; // 정적 바디로 설정
        ground = world.createBody(groundDef); // 월드에 바디 생성
        PolygonShape groundShape = new PolygonShape(); // 폴리곤 모양 생성
        groundShape.setAsBox(620 / 30.0f, 60 / 30.0f); // 박스 모양 설정
        ground.createFixture(groundShape, 0.0f); // 바디에 모양 추가

        // 탑 라인 생성
        BodyDef topLineDef = new BodyDef(); // 바디 정의 생성
        topLineDef.position.set(310 / 30.0f, 150 / 30.0f); // 위치 설정 (미터 단위)
        topLineDef.type = BodyType.STATIC; // 정적 바디로 설정
        topLine = world.createBody(topLineDef); // 월드에 바디 생성
        PolygonShape topLineShape = new PolygonShape(); // 폴리곤 모양 생성
        topLineShape.setAsBox(620 / 30.0f, 2 / 30.0f); // 박스 모양 설정
        topLine.createFixture(topLineShape, 0.0f); // 바디에 모양 추가
    }

    private void addFruit() {
        // 랜덤 과일 생성
        Fruit fruit = FruitType.getRandomFruit();
        
        // 과일 바디 정의
        BodyDef fruitDef = new BodyDef();
        fruitDef.position.set(300 / 30.0f, 50 / 30.0f); // 초기 위치
        fruitDef.type = BodyType.DYNAMIC; // 동적 바디
        
        // 과일 바디 생성
        Body fruitBody = world.createBody(fruitDef);
        
        // 원형 모양 생성
        CircleShape circle = new CircleShape();
        circle.setRadius(fruit.getRadius() / 30.0f); // 미터 단위로 변환
        
        // 물리 속성 설정
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.2f; // 반발력
        
        // 바디에 fixture 추가
        fruitBody.createFixture(fixtureDef);
        
        // 현재 과일 설정
        currentBody = fruitBody;
        currentFruit = fruit;
        
        // 초기에는 잠든 상태 (중력 영향 X)
        currentBody.setAwake(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 부모 클래스의 메서드 호출

        // JBox2D 월드의 객체 그리기
        drawBody(g, leftWall, WALL_COLOR); // 왼쪽 벽 그리기
        drawBody(g, rightWall, WALL_COLOR); // 오른쪽 벽 그리기
        drawBody(g, ground, WALL_COLOR); // 바닥 그리기
        drawBody(g, topLine, WALL_COLOR); // 탑 라인 그리기

        // 현재 과일 그리기
        if (currentBody != null && currentFruit != null) {
            Vec2 position = currentBody.getPosition();
            int x = (int)(position.x * 30.0f);
            int y = (int)(position.y * 30.0f);
            
            Image fruitImage = currentFruit.getImage();
            int radius = (int)(currentFruit.getRadius());
            g.drawImage(fruitImage, 
                       x - radius, // 이미지 중심점 조정
                       y - radius, 
                       radius * 2, 
                       radius * 2, 
                       null);
        }
    }

    private void drawBody(Graphics g, Body body, Color color) {
        g.setColor(color); // 그리기 색상 설정
        
        // Body의 모든 Fixture를 순회
        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
            Shape shape = fixture.getShape(); // Fixture의 형태 가져오기
            
            // 다각형(벽, 바닥 등) 그리기
            if (shape instanceof PolygonShape) {
                PolygonShape polygon = (PolygonShape) shape;
                int vertexCount = polygon.getVertexCount();
                int[] xPoints = new int[vertexCount];
                int[] yPoints = new int[vertexCount];
                
                // 각 꼭지점의 위치를 화면 좌표로 변환
                for (int i = 0; i < vertexCount; i++) {
                    Vec2 vertex = polygon.getVertex(i);
                    Vec2 worldPoint = body.getWorldPoint(vertex);
                    // JBox2D의 미터 단위를 픽셀 단위로 변환 (1미터 = 30픽셀)
                    xPoints[i] = (int) (worldPoint.x * 30.0f);
                    yPoints[i] = (int) (worldPoint.y * 30.0f);
                }
                
                // 다각형 채우기
                g.fillPolygon(xPoints, yPoints, vertexCount);
            }
        }
    }
}
