import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Random;
import java.util.List;
import java.util.ArrayList; 
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.common.*;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.*;

public class WatermelonGame extends JFrame {
    
    public WatermelonGame() {
        setTitle("Watermelon Game"); // 창 제목 설정
        setSize(1080, 800); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 동작 설정

        // 메인 컨테이너 패널 생성 (여백을 위한)
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new GridBagLayout()); // GridBagLayout으로 변경
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // GamePanel 생성 및 크기 고정
        GamePanel gamePanel = new GamePanel();
        Dimension fixedSize = new Dimension(460, 685);
        gamePanel.setPreferredSize(fixedSize);
        gamePanel.setMinimumSize(fixedSize);
        gamePanel.setMaximumSize(fixedSize);
        
        containerPanel.add(gamePanel); // GridBagLayout은 자동으로 중앙 정렬
        add(containerPanel);
        
        setLocationRelativeTo(null);
        setResizable(false); // 창 크기 조정 불가 설정
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // 스윙 이벤트 디스패치 스레드에서 실행
            WatermelonGame game = new WatermelonGame(); // 게임 인스턴스 생성
            game.setVisible(true); // 창을 보이게 설정
        });
    }
}

class GamePanel extends JPanel implements ActionListener {
    // 충돌 정보를 저장할 클래스
    private class CollisionInfo {
        Body bodyA;
        Body bodyB;
        Fruit fruit;
        Vec2 position;
        
        CollisionInfo(Body a, Body b, Fruit f, Vec2 pos) {
            bodyA = a;
            bodyB = b;
            fruit = f;
            position = pos;
        }
    }
    
    // 처리할 충돌 목록
    private List<CollisionInfo> pendingCollisions = new ArrayList<>();
    
    private World world;
    private Body leftWall; // 왼쪽 벽 바디
    private Body rightWall; // 오른쪽 벽 바디
    private Body ground; // 바닥 바디
    private Body topLine; // 탑 라인 바디
    private Body diagonalWall1; // 대각선 벽 바디
    private Body diagonalWall2; // 대각선 벽 바디
    private Body topWall; // 탑 벽 바디
    private Timer timer; // 타이머
    private Body currentBody; // 현재 조작중인 과일 바디
    private Fruit currentFruit; // 현재 조작중인 과일 정보
    private Vec2 lastSKeyPosition = new Vec2(230 / 30f, 50 / 30f); // 기본 위치 설정
    private Random random = new Random();
    private Timer leftMoveTimer;
    private Timer rightMoveTimer;

    public GamePanel() {
        world = new World(new Vec2(0.0f, 100.0f)); // JBox2D 월드 생성
        setBackground(new Color(0xF7F4C8)); // 패널 배경색 설정
        createWalls(); // 벽 생성
        setFocusable(true); // 키 이벤트 받을 수 있도록 설정

        // 타이머 설정 (60 FPS)
        timer = new Timer(1000 / 60, this);
        timer.start(); // 타이머 시작

        // 왼쪽/오른쪽 이동 타이머 초기화
        leftMoveTimer = new Timer(5, e -> {
            if (currentBody != null && !currentBody.isAwake()) {
                if (currentBody.getPosition().x > 16f/30.0f + currentFruit.getRadius()/30.0f) {
                    currentBody.setTransform(
                        new Vec2(currentBody.getPosition().x - 1/30.0f, currentBody.getPosition().y), 
                        0
                    );
                }
            }
        });
        leftMoveTimer.setRepeats(true);

        rightMoveTimer = new Timer(5, e -> {
            if (currentBody != null && !currentBody.isAwake()) {
                if (currentBody.getPosition().x < 444f/30.0f - currentFruit.getRadius()/30.0f) {
                    currentBody.setTransform(
                        new Vec2(currentBody.getPosition().x + 1/30.0f, currentBody.getPosition().y), 
                        0
                    );
                }
            }
        });
        rightMoveTimer.setRepeats(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!currentBody.isAwake()) {  // 현재 과일이 멈춰있을 때만
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            if (!leftMoveTimer.isRunning()) {
                                leftMoveTimer.start();
                            }
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (!rightMoveTimer.isRunning()) {
                                rightMoveTimer.start();
                            }
                            break;
                        case KeyEvent.VK_DOWN:
                            // 과일 위치 저장
                            if (currentBody != null) {
                                lastSKeyPosition.set(currentBody.getPosition());
                            }
                            // 과일 떨어뜨리기 전에 충돌 필터 업데이트
                            Fixture fixture = currentBody.getFixtureList();
                            Filter filter = fixture.getFilterData();
                            filter.maskBits = 0xFFFF; // 모든 카테고리와 충돌
                            fixture.setFilterData(filter);
                            currentBody.setAwake(true); // 과일 떨어뜨리기

                             // 약간의 랜덤 토크 추가
                            float randomTorque = (random.nextFloat() - 0.5f) * 10.0f;
                            currentBody.applyTorque(randomTorque);

                            // 몇초 후 새 과일 생성
                            Timer dropTimer = new Timer(200, e1 -> addFruit());
                            dropTimer.setRepeats(false);
                            dropTimer.start();
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        leftMoveTimer.stop();
                        break;
                    case KeyEvent.VK_RIGHT:
                        rightMoveTimer.stop();
                        break;
                }
            }
        });
        
        
        // world.setContactListener(new ContactListener() {
        //     @Override
        //     public void beginContact(Contact contact) {
        //         Body bodyA = contact.getFixtureA().getBody();
        //         Body bodyB = contact.getFixtureB().getBody();
                
        //         Object userDataA = bodyA.getUserData();
        //         Object userDataB = bodyB.getUserData();
                
        //         if (userDataA instanceof Fruit && userDataB instanceof Fruit) {
        //             Fruit fruitA = (Fruit) userDataA;
        //             Fruit fruitB = (Fruit) userDataB;
                    
        //             if (fruitA.getName().equals(fruitB.getName())) {
        //                 Vec2 posA = bodyA.getPosition();
        //                 Vec2 posB = bodyB.getPosition();
        //                 Vec2 midPoint = new Vec2((posA.x + posB.x) / 2, (posA.y + posB.y) / 2);
                        
        //                 // 충돌 정보 저장
        //                 pendingCollisions.add(new CollisionInfo(bodyA, bodyB, fruitA, midPoint));
        //             }
        //         }
        //     }

        //     @Override
        //     public void endContact(Contact contact) {}
        //     @Override
        //     public void preSolve(Contact contact, Manifold oldManifold) {}
        //     @Override
        //     public void postSolve(Contact contact, ContactImpulse impulse) {}
        // });

        addFruit(); // 과일 추가
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 물리 시뮬레이션 업데이트
        world.step(1.0f / 60.0f, 3, 2);
        
        // 충돌 처리
        for (CollisionInfo info : pendingCollisions) {
            Fruit nextFruit = FruitType.getNextFruit(info.fruit);
            if (nextFruit != null) {
                createFruitBody(new Vec2(info.position.x, info.position.y), nextFruit);
            }
            // 기존 과일 제거
            world.destroyBody(info.bodyA);
            world.destroyBody(info.bodyB);
        }
        pendingCollisions.clear();
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render((Graphics2D) g);
    }

    private void render(Graphics2D g2d) {
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 모든 바디 순회하여 그리기
        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            if (body == leftWall || body == rightWall || body == ground || body == topLine || body == diagonalWall1
                    || body == diagonalWall2 || body == topWall) {
                // 벽과 바닥 그리기
                g2d.setColor(new Color(0xf3d681)); // 그리기 색상 설정

                // 다각형 모양 가져오기
                PolygonShape shape = (PolygonShape) body.getFixtureList().getShape();
                int vertexCount = shape.getVertexCount();
                int[] xPoints = new int[vertexCount];
                int[] yPoints = new int[vertexCount];

                // 각 꼭지점의 위치를 화면 좌표로 변환
                for (int i = 0; i < vertexCount; i++) {
                    Vec2 vertex = shape.getVertex(i);
                    Vec2 worldPoint = body.getWorldPoint(vertex);
                    xPoints[i] = (int) (worldPoint.x * 30.0f);
                    yPoints[i] = (int) (worldPoint.y * 30.0f);
                }

                // 다각형 채우기
                g2d.fillPolygon(xPoints, yPoints, vertexCount);
            }
        }
        
        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            if (body.getUserData() instanceof Fruit) {
                Fruit fruit = (Fruit) body.getUserData();

                // 위치 계산
                Vec2 position = body.getPosition();
                int x = (int) (position.x * 30.0f);
                int y = (int) (position.y * 30.0f);
                float angle = -body.getAngle(); // 각도 부호 반전

                int radius = (int) fruit.getRadius();

                // 기존 변환 저장
                AffineTransform oldTransform = g2d.getTransform();

                // 변환 적용
                g2d.translate(x, y); // 1. 위치로 이동
                g2d.rotate(angle); // 2. 회전 적용
                g2d.translate(-radius, -radius); // 3. 이미지 중심으로 이동

                int width;
                int height;

                if (fruit.getPicHeight() > fruit.getPicWidth()) {
                    width = radius * 2;
                    height = (int) (radius * 2 * ((float) fruit.getPicHeight() / fruit.getPicWidth()));
                    g2d.translate(0, -(height - radius * 2));
                }
                else {
                    width = (int) (radius * 2 * ((float) fruit.getPicWidth() / fruit.getPicHeight()));
                    height = radius * 2;
                }
                
                // 이미지 그리기
                g2d.drawImage(fruit.getImage(), 0, 0, width, height,null);

                // 이전 변환 복원
                g2d.setTransform(oldTransform);
            }
        }
    }

    private void createWalls() {
        // 왼쪽 벽 생성
        BodyDef leftWallDef = new BodyDef(); // 바디 정의 생성
        leftWallDef.position.set(7.5f / 30.0f, 427.5f / 30.0f); // 위치 설정 (미터 단위)
        leftWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        leftWall = world.createBody(leftWallDef); // 월드에 바디 생성
        PolygonShape leftWallShape = new PolygonShape(); // 폴리곤 모양 생성
        leftWallShape.setAsBox(7.5f / 30.0f, 250f / 30.0f); // 박스 모양 설정
        leftWall.createFixture(leftWallShape, 0.0f); // 바디에 모양 추가

        // 오른쪽 벽 생성
        BodyDef rightWallDef = new BodyDef(); // 바디 정의 생성
        rightWallDef.position.set(452.5f / 30.0f, 427.5f / 30.0f); // 위치 설정 (미터 단위)
        rightWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        rightWall = world.createBody(rightWallDef); // 월드에 바디 생성
        PolygonShape rightWallShape = new PolygonShape(); // 폴리곤 모양 생성
        rightWallShape.setAsBox(7.5f / 30.0f, 250f / 30.0f); // 박스 모양 설정
        rightWall.createFixture(rightWallShape, 0.0f); // 바디에 모양 추가

        // 바닥 생성
        BodyDef groundDef = new BodyDef(); // 바디 정의 생성
        groundDef.position.set(230f / 30.0f, 677.5f / 30.0f); // 위치 설정 (미터 단위)
        groundDef.type = BodyType.STATIC; // 정적 바디로 설정
        ground = world.createBody(groundDef); // 월드에 바디 생성
        PolygonShape groundShape = new PolygonShape(); // 폴리곤 모양 생성
        groundShape.setAsBox(230f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        // 바닥의 물리 속성 설정
        FixtureDef groundFixtureDef = new FixtureDef();
        groundFixtureDef.shape = groundShape;
        groundFixtureDef.friction = 0.05f; // 마찰력 감소
        ground.createFixture(groundFixtureDef); // 바디에 모양 추가

        // 탑 라인 생성
        BodyDef topLineDef = new BodyDef(); // 바디 정의 생성
        topLineDef.position.set(230f / 30.0f, 182.5f / 30.0f); // 위치 설정 (미터 단위)
        topLineDef.type = BodyType.STATIC; // 정적 바디로 설정
        topLine = world.createBody(topLineDef); // 월드에 바디 생성
        PolygonShape topLineShape = new PolygonShape(); // 폴리곤 모양 생성
        topLineShape.setAsBox(230f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        FixtureDef topLineFixtureDef = new FixtureDef();
        topLineFixtureDef.shape = topLineShape;
        topLineFixtureDef.isSensor = true; // 센서로 설정
        topLine.createFixture(topLineFixtureDef); // 바디에 모양 추가

        // 대각선 벽1 생성
        BodyDef diagonalWallDef1 = new BodyDef();
        diagonalWallDef1.position.set(21 / 30.0f, 157.5f / 30.0f); // 위치 설정
        diagonalWallDef1.type = BodyType.STATIC;
        diagonalWallDef1.angle = (float) Math.toRadians(305); // 각도 설정
        diagonalWall1 = world.createBody(diagonalWallDef1);
        PolygonShape diagonalShape1 = new PolygonShape();
        diagonalShape1.setAsBox(35f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
        FixtureDef diagonalFixture1 = new FixtureDef();
        diagonalFixture1.shape = diagonalShape1;
        diagonalFixture1.isSensor = true; // 센서로 설정
        diagonalWall1.createFixture(diagonalFixture1);

        // 대각선 벽2 생성
        BodyDef diagonalWallDef2 = new BodyDef();
        diagonalWallDef2.position.set(438 / 30.0f, 157.5f / 30.0f); // 위치 설정
        diagonalWallDef2.type = BodyType.STATIC;
        diagonalWallDef2.angle = (float) Math.toRadians(55);
        diagonalWall2 = world.createBody(diagonalWallDef2);
        PolygonShape diagonalShape2 = new PolygonShape();
        diagonalShape2.setAsBox(35f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
        FixtureDef diagonalFixture2 = new FixtureDef();
        diagonalFixture2.shape = diagonalShape2;
        diagonalFixture2.isSensor = true; // 센서로 설정
        diagonalWall2.createFixture(diagonalFixture2);

        // 탑 벽 생성
        BodyDef topWallDef = new BodyDef(); // 바디 정의 생성
        topWallDef.position.set(230f / 30.0f, 132.5f / 30.0f); // 위치 설정 (미터 단위)
        topWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        topWall = world.createBody(topWallDef); // 월드에 바디 생성
        PolygonShape topWallShape = new PolygonShape(); // 폴리곤 모양 생성
        topWallShape.setAsBox(195f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        FixtureDef topWallFixture = new FixtureDef(); 
        topWallFixture.shape = topWallShape;
        topWallFixture.isSensor = true; // 센서로 설정
        topWall.createFixture(topWallFixture);

    }

    private void addFruit() {
        // 랜덤 과일 생성
        Fruit fruit = FruitType.getRandomFruit();

        Body fruitBody = createFruitBody(lastSKeyPosition, fruit);

        Filter fixtureFilter = fruitBody.getFixtureList().getFilterData();

        // 충돌 필터 설정 - 초기에는 충돌 비활성화
        fixtureFilter.categoryBits = 0x0002;
        fixtureFilter.maskBits = 0x0000; // 아무것과도 충돌하지 않음

        // 현재 과일 설정
        currentBody = fruitBody;
        currentFruit = fruit;

        // 초기에는 잠든 상태 (중력 영향 X)
        currentBody.setAwake(false);
    }
    
    private Body createFruitBody(Vec2 position, Fruit fruit) {
        // 바디 정의
        BodyDef fruitDef = new BodyDef();
        fruitDef.position.set(position.x, position.y);
        fruitDef.type = BodyType.DYNAMIC;
        Body fruitBody = world.createBody(fruitDef);
    
        // 형태 정의
        CircleShape circle = new CircleShape();
        circle.setRadius(fruit.getRadius() / 30.0f);
    
        // Fixture 정의
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.3f; // 밀도
        fixtureDef.friction = 0.8f; // 마찰력
        fixtureDef.restitution = 0.2f; // 반발력
        fruitBody.createFixture(fixtureDef); // 바디에 모양 추가
        fruitBody.setAngularDamping(0.1f); // 각속도 감소
        fruitBody.setLinearDamping(0.1f); // 선속도 감소
        fruitBody.setUserData(fruit); // 사용자 데이터 설정
    
        return fruitBody;
    }
}
