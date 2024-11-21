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
        setSize(620, 850); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 동작 설정
        setLocationRelativeTo(null); // 창을 화면 중앙에 배치
        //setResizable(false); // 창 크기 조정 불가 설정
        
        add(new GamePanel()); // 패널을 프레임에 추가
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
        Fruit fruitA;
        Fruit fruitB;
        Vec2 position;
        
        CollisionInfo(Body a, Body b, Fruit fa, Fruit fb, Vec2 pos) {
            bodyA = a;
            bodyB = b;
            fruitA = fa;
            fruitB = fb;
            position = pos;
        }
    }
    
    // 처리할 충돌 목록
    private List<CollisionInfo> pendingCollisions = new ArrayList<>();
    
    private World world;
    private static final Color WALL_COLOR = new Color(0xE6B143); // 벽 색상
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
    private Vec2 lastSKeyPosition = new Vec2(300 / 30f, 50 / 30f); // 기본 위치 설정
    private Random random = new Random();


    public GamePanel() {
        world = new World(new Vec2(0.0f, 100.0f)); // JBox2D 월드 생성
        setSize(460, 800);
        setBackground(new Color(0xF7F4C8)); // 패널 배경색 설정
        createWalls(); // 벽 생성
        setFocusable(true); // 키 이벤트 받을 수 있도록 설정

        // 타이머 설정 (60 FPS)
        timer = new Timer(1000 / 60, this);
        timer.start(); // 타이머 시작


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!currentBody.isAwake()) {  // 현재 과일이 멈춰있을 때만
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            if (currentBody.getPosition().x > 30f/30.0f+currentFruit.getRadius()/30.0f)
                                currentBody.setTransform(new Vec2(currentBody.getPosition().x - 10/30.0f, currentBody.getPosition().y), 0);
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (currentBody.getPosition().x < 590f/30.0f+currentFruit.getRadius()/30.0f)
                                currentBody.setTransform(new Vec2(currentBody.getPosition().x + 10/30.0f, currentBody.getPosition().y), 0);
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
                            Timer dropTimer = new Timer(100, e1 -> addFruit());
                            dropTimer.setRepeats(false);
                            dropTimer.start();
                            break;
                    }
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
        //                 pendingCollisions.add(new CollisionInfo(bodyA, bodyB, fruitA, fruitB, midPoint));
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
        world.step(1.0f / 60.0f, 6, 2);
        
        // 충돌 처리
        for (CollisionInfo collision : pendingCollisions) {
            processFruitCollision(collision);
        }
        pendingCollisions.clear();
        
        repaint();
    }

    private void processFruitCollision(CollisionInfo info) {
        Fruit nextFruit = FruitType.getNextFruit(info.fruitA);
        if (nextFruit != null) {
            // 새 과일 생성
            BodyDef fruitDef = new BodyDef();
            fruitDef.position.set(info.position.x, info.position.y);
            fruitDef.type = BodyType.DYNAMIC;
            Body newFruitBody = world.createBody(fruitDef);
            
            CircleShape circle = new CircleShape();
            circle.setRadius(nextFruit.getRadius() / 30.0f);
            
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = circle;
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0.05f;
            fixtureDef.restitution = 0.2f;
            
            newFruitBody.createFixture(fixtureDef);
            newFruitBody.setAngularDamping(0.05f);
            newFruitBody.setLinearDamping(0.0f);
            newFruitBody.setUserData(nextFruit);
            
            // 기존 과일 제거
            world.destroyBody(info.bodyA);
            world.destroyBody(info.bodyB);
        }
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
            if (body == leftWall || body == rightWall || body == ground || body == topLine || body == diagonalWall1 || body == diagonalWall2 || body == topWall) {
                // 벽과 바닥 그리기
                g2d.setColor(WALL_COLOR); // 그리기 색상 설정

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
            } else {
                // 과일 그리기
                Object userData = body.getUserData();
                if (userData instanceof Fruit) {
                    Fruit fruit = (Fruit) userData;

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

                    // 체리인 경우 Y-오프셋 추가
                    if (fruit.getName().equals("base/00_cherry")) {
                        g2d.translate(0, -14);
                    }
                    else if (fruit.getName().equals("base/_gyool")) {
                        g2d.translate(0, -10);
                    }
                    

                    // 이미지 그리기
                    g2d.drawImage(fruit.getImage(), 0, 0, radius*2, radius*2,null);

                    // 이전 변환 복원
                    g2d.setTransform(oldTransform);
                }
            }
        }
    }

    private void createWalls() {
        // 왼쪽 벽 생성
        BodyDef leftWallDef = new BodyDef(); // 바디 정의 생성
        leftWallDef.position.set(7.5f / 30.0f, 395 / 30.0f); // 위치 설정 (미터 단위)
        leftWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        leftWall = world.createBody(leftWallDef); // 월드에 바디 생성
        PolygonShape leftWallShape = new PolygonShape(); // 폴리곤 모양 생성
        leftWallShape.setAsBox(7.5f / 30.0f, 250f / 30.0f); // 박스 모양 설정
        leftWall.createFixture(leftWallShape, 0.0f); // 바디에 모양 추가

        // 오른쪽 벽 생성
        BodyDef rightWallDef = new BodyDef(); // 바디 정의 생성
        rightWallDef.position.set(452.5f / 30.0f, 395 / 30.0f); // 위치 설정 (미터 단위)
        rightWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        rightWall = world.createBody(rightWallDef); // 월드에 바디 생성
        PolygonShape rightWallShape = new PolygonShape(); // 폴리곤 모양 생성
        rightWallShape.setAsBox(7.5f / 30.0f, 250f / 30.0f); // 박스 모양 설정
        rightWall.createFixture(rightWallShape, 0.0f); // 바디에 모양 추가

        // 바닥 생성
        BodyDef groundDef = new BodyDef(); // 바디 정의 생성
        groundDef.position.set(230f / 30.0f, 645 / 30.0f); // 위치 설정 (미터 단위)
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
        topLineDef.position.set(230f / 30.0f, 150 / 30.0f); // 위치 설정 (미터 단위)
        topLineDef.type = BodyType.STATIC; // 정적 바디로 설정
        topLine = world.createBody(topLineDef); // 월드에 바디 생성
        PolygonShape topLineShape = new PolygonShape(); // 폴리곤 모양 생성
        topLineShape.setAsBox(230f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        topLine.createFixture(topLineShape, 0.0f); // 바디에 모양 추가

        // 대각선 벽1 생성
        BodyDef diagonalWallDef1 = new BodyDef();
        diagonalWallDef1.position.set(30 / 30.0f, 100f / 30.0f); // 위치 설정
        diagonalWallDef1.type = BodyType.STATIC;
        diagonalWallDef1.angle = (float) Math.toRadians(310); // 각도 설정
        diagonalWall1 = world.createBody(diagonalWallDef1);
        PolygonShape diagonalShape1 = new PolygonShape();
        diagonalShape1.setAsBox(50f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
        diagonalWall1.createFixture(diagonalShape1, 0.0f);

        // 대각선 벽2 생성
        BodyDef diagonalWallDef2 = new BodyDef();
        diagonalWallDef2.position.set(430 / 30.0f, 100f / 30.0f); // 위치 설정
        diagonalWallDef2.type = BodyType.STATIC;
        diagonalWallDef2.angle = (float) Math.toRadians(50);
        diagonalWall2 = world.createBody(diagonalWallDef2);
        PolygonShape diagonalShape2 = new PolygonShape();
        diagonalShape2.setAsBox(50f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
        diagonalWall2.createFixture(diagonalShape2, 0.0f);

        // 탑 벽 생성
        BodyDef topWallDef = new BodyDef(); // 바디 정의 생성
        topWallDef.position.set(230f / 30.0f, 5 / 30.0f); // 위치 설정 (미터 단위)
        topWallDef.type = BodyType.STATIC; // 정적 바디로 설정
        topWall = world.createBody(topWallDef); // 월드에 바디 생성
        PolygonShape topWallShape = new PolygonShape(); // 폴리곤 모양 생성
        topWallShape.setAsBox(185f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        topWall.createFixture(topWallShape, 0.0f); // 바디에 모양 추가

    }

    private void addFruit() {
        // 랜덤 과일 생성
        Fruit fruit = FruitType.getRandomFruit();
        
        // 과일 바디 정의
        BodyDef fruitDef = new BodyDef();
        fruitDef.position.set(lastSKeyPosition.x, lastSKeyPosition.y); // 초기 위치        
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
        fixtureDef.friction = 0.05f; // 마찰력 감소
        fixtureDef.restitution = 0.2f; // 반발력

        // 충돌 필터 설정 - 초기에는 충돌 비활성화
        fixtureDef.filter.categoryBits = 0x0002;
        fixtureDef.filter.maskBits = 0x0000; // 아무것과도 충돌하지 않음
        
        // 바디에 fixture 추가
        fruitBody.createFixture(fixtureDef);

        // 각 감쇠 감소
        fruitBody.setAngularDamping(0.05f);  
        fruitBody.setLinearDamping(0.0f); // 선형 감쇠 제거

        // 과일 바디에 userData 설정
        fruitBody.setUserData(fruit);
        
        // 현재 과일 설정
        currentBody = fruitBody;
        currentFruit = fruit;
        
        // 초기에는 잠든 상태 (중력 영향 X)
        currentBody.setAwake(false);
    }
}
