import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.common.*;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.*;

class GamePanel extends JPanel implements ActionListener {
    // 충돌 정보를 저장할 클래스
    private class CollisionInfo {
        Body bodyA;
        Body bodyB;
        Fruit fruit;
        Vec2 position;
        
        CollisionInfo(Body bodyA, Body bodyB, Fruit fruit, Vec2 position) {
            this.bodyA = bodyA;
            this.bodyB = bodyB;
            this.fruit = fruit;
            this.position = position;
        }
    }
    // 처리할 충돌 목록
    private CollisionInfo pendingCollision = null; 
    
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
    private Vec2 lastSKeyPosition = new Vec2(200 / 30f, 110 / 30f); // 기본 위치 설정
    private Random random = new Random();
    private Timer leftMoveTimer;
    private Timer rightMoveTimer;
    private Image backgroundImage;
    private Image cloudImage;
    private int score = 0;
    private ScoreUpdateListener scoreListener;
    private boolean isGameOver = false;
    private BorderedLabel gameOverLabel;
    private BorderedLabel finalScoreLabel;
    private static Font customFont;
    private Fruit nextFruit;

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


    public GamePanel() {
        setLayout(null);
        world = new World(new Vec2(0.0f, 60.0f)); // JBox2D 월드 생성
        setOpaque(false); // 패널을 투명하게 설정
        createWalls(); // 벽 생성
        setFocusable(true); // 키 이벤트 받을 수 있도록 설정

        // 이미지 로드
        backgroundImage = new ImageIcon(getClass().getResource("/resources/base/back.png")).getImage();
        cloudImage = new ImageIcon(getClass().getResource("/resources/base/cloud.png")).getImage();

        // 타이머 설정 (60 FPS)
        timer = new Timer(1000 / 60, this);
        timer.start(); // 타이머 시작

        // 왼쪽/오른쪽 이동 타이머 초기화
        leftMoveTimer = new Timer(5, e -> {
            if (currentBody != null && !currentBody.isAwake()) {
                if (currentBody.getPosition().x > 17f/30.0f + currentFruit.getRadius()/30.0f) {
                    currentBody.setTransform(new Vec2(currentBody.getPosition().x - 1/30.0f, currentBody.getPosition().y), 0);
                    lastSKeyPosition.x = currentBody.getPosition().x; // 구름 위치 업데이트 
                }
            }
        });
        leftMoveTimer.setRepeats(true);

        rightMoveTimer = new Timer(5, e -> {
            if (currentBody != null && !currentBody.isAwake()) {
                if (currentBody.getPosition().x < 443f/30.0f - currentFruit.getRadius()/30.0f) {
                    currentBody.setTransform(new Vec2(currentBody.getPosition().x + 1/30.0f, currentBody.getPosition().y), 0);
                    lastSKeyPosition.x = currentBody.getPosition().x; // 구름 위치 업데이트 
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
                            currentBody.applyTorque((random.nextFloat() - 0.5f) * 10.0f);

                            // 몇초 후 새 과일 생성
                            Timer dropTimer = new Timer(400, e1 -> addFruit());
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
        
        // 포커스 관련 설정 추가
        setFocusable(true);
        requestFocusInWindow();
        
        // 포커스 리스너 추가
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // 포커스를 잃으면 자동으로 다시 가져오기
                SwingUtilities.invokeLater(() -> requestFocusInWindow());
            }
        });

        // 마우스 리스너 추가
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                
                Object userDataA = bodyA.getUserData();
                Object userDataB = bodyB.getUserData();
                
                if (userDataA instanceof Fruit && userDataB instanceof Fruit) {
                    Fruit fruitA = (Fruit) userDataA;
                    Fruit fruitB = (Fruit) userDataB;
                    
                    if (fruitA.getName().equals(fruitB.getName())) {
                        Vec2 posA = bodyA.getPosition();
                        Vec2 posB = bodyB.getPosition();
                        Vec2 midPoint = new Vec2((posA.x + posB.x) / 2, (posA.y + posB.y) / 2);
                        
                        addScore(fruitA); // 점수 획득
                        // 충돌 정보 저장
                        pendingCollision = new CollisionInfo(bodyA, bodyB, fruitA, midPoint);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        addFruit(); // 과일 추가

        
    
        // 게임오버 라벨 초기화
        gameOverLabel = new BorderedLabel("게임 오버!", 
            new Color(0xFFFEF0),  // 텍스트 색상
            new Color(0x913E2E),  // 외부 테두리
            new Color(0xD8AA65),  // 내부 테두리
            new Color(0xFFA92E),  // 그라디언트 시작
            new Color(0xFA792A)   // 그라디언트 끝
        );
        gameOverLabel.setFont(gameOverLabel.getFont().deriveFont(60f));
        gameOverLabel.setVisible(false);
        add(gameOverLabel);

        finalScoreLabel = new BorderedLabel("최종 점수: 0",
            new Color(0xFFFEF0),  // 텍스트 색상
            new Color(0x8D6219),  // 외부 테두리
            new Color(0xD8AA65),  // 내부 테두리
            null,                 // 그라디언트 없음
            null
        );
        finalScoreLabel.setFont(finalScoreLabel.getFont().deriveFont(30f));
        finalScoreLabel.setVisible(false);
        add(finalScoreLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver) {
            world.step(1.0f / 60.0f, 3, 2); // 물리 시뮬레이션 업데이트
            checkGameOver();
        }
        
        // 충돌 처리
        if (pendingCollision != null) {
            Fruit nextFruit = FruitType.getNextFruit(pendingCollision.fruit);
            if (nextFruit != null) {
                createFruitBody(new Vec2(pendingCollision.position.x, pendingCollision.position.y), nextFruit);
            }
            world.destroyBody(pendingCollision.bodyA);
            world.destroyBody(pendingCollision.bodyB);
            pendingCollision = null; // 처리 후 초기화
        }
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        render(g2d);
    }

    private void render(Graphics2D g2d) {
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
            
        // 패널의 현재 크기 가져오기
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // 기준 크기 설정 (원본 디자인 기준)
        float baseWidth = 460f;
        float baseHeight = 685f;

        // 스케일 계산
        float scaleX = panelWidth / baseWidth;
        float scaleY = panelHeight / baseHeight;
        float scale = Math.min(scaleX, scaleY); // 더 작은 비율 사용

        // 변환 행렬 저장
        AffineTransform originalTransform = g2d.getTransform();

        // 스케일 적용
        g2d.scale(scale, scale);

        // 중앙 정렬을 위한 오프셋 계산
        float offsetX = (panelWidth/scale - baseWidth) / 2;
        float offsetY = (panelHeight/scale - baseHeight) / 2;
        g2d.translate(offsetX, offsetY);

        // 배경 이미지 그리기
        Composite originalComposite = g2d.getComposite();
        float alpha = 0.8f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(backgroundImage, 0, 125, (int)baseWidth, (int)(baseHeight-125), null);
        g2d.setComposite(originalComposite);

        // 구름 이미지 그리기 
        g2d.drawImage(cloudImage, 
        (int)(lastSKeyPosition.x * 30.0f - 20), 
        (int)(lastSKeyPosition.y * 30.0f - 90), 
        130, 90, null);

        //모든 바디 순회하여 그리기
        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            if (body == leftWall || body == rightWall || body == ground || body == diagonalWall1
                    || body == diagonalWall2 || body == topWall) {
                // 벽과 바닥 그리기
                g2d.setColor(new Color(0xf3d680)); // 그리기 색상 설정

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
        drawGuideLine(g2d);
        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            if (body.getUserData() instanceof Fruit) {
                Fruit fruit = (Fruit) body.getUserData();

                // 위치 계산
                Vec2 position = body.getPosition();
                int x = (int) (position.x * 30.0f);
                int y = (int) (position.y * 30.0f);
                float angle = body.getAngle(); // 각도 부호 반전

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
                } else {
                    width = (int) (radius * 2 * ((float) fruit.getPicWidth() / fruit.getPicHeight()));
                    height = radius * 2;
                }

                // 이미지 그리기
                g2d.drawImage(fruit.getImage(), 0, 0, width, height, null);

                // 이전 변환 복원
                g2d.setTransform(oldTransform);
            }
        }
        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            if (body == topLine) {
                // 벽과 바닥 그리기
                g2d.setColor(new Color(0xf3d680)); // 그리기 색상 설정

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

        if (isGameOver) {
            // 반투명 오버레이
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillRoundRect(0, 172, (int)baseWidth, (int)(baseHeight-172), 15, 15);        
            // 원래 변환으로 복원
            g2d.setTransform(originalTransform);
        
            // 게임오버 라벨 설정
            gameOverLabel.setFont(customFont.deriveFont((float)(60.0 * scale)));
            finalScoreLabel.setFont(customFont.deriveFont((float)(30.0 * scale)));
            
            Dimension gameOverSize = gameOverLabel.getPreferredSize();
            Dimension scoreSize = finalScoreLabel.getPreferredSize();
        
            // 오버레이 영역의 중심점 계산
            int overlayStartY = (int)(offsetY + 172 * scale);
            int overlayHeight = (int)((baseHeight-172) * scale);
            int overlayCenterY = overlayStartY + (overlayHeight / 2);
        
            // 라벨 위치 계산 (정확한 중앙 정렬)
            gameOverLabel.setBounds(
                (int)(panelWidth/2 - gameOverSize.width/2),
                overlayCenterY - gameOverSize.height - 10,
                gameOverSize.width,
                gameOverSize.height
            );
        
            finalScoreLabel.setBounds(
                (int)(panelWidth/2 - scoreSize.width/2),
                overlayCenterY + 10,
                scoreSize.width,
                scoreSize.height
            );
        
            gameOverLabel.setVisible(true);
            finalScoreLabel.setText("최종 점수: " + score);
            finalScoreLabel.setVisible(true);
        }
        g2d.setTransform(originalTransform);
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
        ground.createFixture(groundShape, 0.0f); // 바디에 모양 추가

        // 탑 라인 생성
        BodyDef topLineDef = new BodyDef(); // 바디 정의 생성
        topLineDef.position.set(230f / 30.0f, 178f / 30.0f); // 위치 설정 (미터 단위)
        topLineDef.type = BodyType.STATIC; // 정적 바디로 설정
        topLine = world.createBody(topLineDef); // 월드에 바디 생성
        PolygonShape topLineShape = new PolygonShape(); // 폴리곤 모양 생성
        topLineShape.setAsBox(220f / 30.0f, 6f / 30.0f); // 박스 모양 설정
        FixtureDef topLineFixtureDef = new FixtureDef();
        topLineFixtureDef.shape = topLineShape;
        topLineFixtureDef.isSensor = true; // 센서로 설정
        topLine.createFixture(topLineFixtureDef); // 바디에 모양 추가

        // 대각선 벽1 생성
        BodyDef diagonalWallDef1 = new BodyDef();
        diagonalWallDef1.position.set(24 / 30.0f, 156.5f / 30.0f); // 위치 설정
        diagonalWallDef1.type = BodyType.STATIC;
        diagonalWallDef1.angle = (float) Math.toRadians(304); // 각도 설정
        diagonalWall1 = world.createBody(diagonalWallDef1);
        PolygonShape diagonalShape1 = new PolygonShape();
        diagonalShape1.setAsBox(32f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
        FixtureDef diagonalFixture1 = new FixtureDef();
        diagonalFixture1.shape = diagonalShape1;
        diagonalFixture1.isSensor = true; // 센서로 설정
        diagonalWall1.createFixture(diagonalFixture1);

        // 대각선 벽2 생성
        BodyDef diagonalWallDef2 = new BodyDef();
        diagonalWallDef2.position.set(436 / 30.0f, 156.5f / 30.0f); // 위치 설정
        diagonalWallDef2.type = BodyType.STATIC;
        diagonalWallDef2.angle = (float) Math.toRadians(56);
        diagonalWall2 = world.createBody(diagonalWallDef2);
        PolygonShape diagonalShape2 = new PolygonShape();
        diagonalShape2.setAsBox(32f / 30.0f, 7.5f / 30.0f); // 길이와 두께 설정
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
        topWallShape.setAsBox(193.5f / 30.0f, 7.5f / 30.0f); // 박스 모양 설정
        FixtureDef topWallFixture = new FixtureDef(); 
        topWallFixture.shape = topWallShape;
        topWallFixture.isSensor = true; // 센서로 설정
        topWall.createFixture(topWallFixture);

    }

    private void addFruit() {
        // 저장된 다음 과일이 없으면 새로 생성
        if (nextFruit == null) {
            updateNextFruit();
        }

        Body fruitBody = createFruitBody(lastSKeyPosition, nextFruit);

        Filter fixtureFilter = fruitBody.getFixtureList().getFilterData();

        // 충돌 필터 설정 - 초기에는 충돌 비활성화
        fixtureFilter.categoryBits = 0x0002;
        fixtureFilter.maskBits = 0x0000; // 아무것과도 충돌하지 않음

        // 현재 과일 설정
        currentBody = fruitBody;
        currentFruit = nextFruit;

        // 초기에는 잠든 상태 (중력 영향 X)
        currentBody.setAwake(false);
        updateNextFruit();

        // ScorePanel 즉시 업데이트
        if (getParent() != null) {
            getParent().repaint();
        }
    }
    
    private Body createFruitBody(Vec2 position, Fruit fruit) {
        if (position.x < 16f / 30.0f + fruit.getRadius() / 30.0f) {
            position.x = 16f / 30.0f + fruit.getRadius() / 30.0f;
        } else if (position.x > 444f / 30.0f - fruit.getRadius() / 30.0f) {
            position.x = 444f / 30.0f - fruit.getRadius() / 30.0f;
        }

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
        fixtureDef.density = 0.1f; // 밀도
        fixtureDef.friction = 0.5f; // 마찰력
        fixtureDef.restitution = 0.15f; // 반발력
        fruitBody.createFixture(fixtureDef); // 바디에 모양 추가
        fruitBody.setAngularDamping(0.2f); // 각속도 감소
        fruitBody.setLinearDamping(0.01f); // 선속도 감소
        fruitBody.setUserData(fruit); // 사용자 데이터 설정

        return fruitBody;
    }

    private void drawGuideLine(Graphics2D g2d) {
        // 현재 과일이 있고 멈춰있을 때만 가이드 라인 표시
        if (currentBody != null && !currentBody.isAwake()) {
            // 가이드 라인 스타일 설정
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));

            // 현재 과일 위치의 x좌표
            int x = (int) (currentBody.getPosition().x * 30.0f);
            // 현재 과일 위치의 y좌표
            int y = (int) (currentBody.getPosition().y * 30.0f);

            // 세로선 그리기 (위에서 아래로)
            g2d.drawLine(x, y, x, 650);
        }
    }
    
    // ScoreUpdateListener 인터페이스 추가
    public interface ScoreUpdateListener {
        void onScoreUpdate(int score);
    }
    
    // 리스너 설정 메서드
    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        this.scoreListener = listener;
    }
    // 점수 획득 메서드
    private void addScore(Fruit fruit) {
        switch (fruit.getName()) {
            case "base/00_cherry":
                score += 1;
                break;
            case "base/01_strawberry":
                score += 3;
                break;
            case "base/02_grape":
                score += 6;
                break;
            case "base/03_gyool":
                score += 10;
                break;
            case "base/04_orange":
                score += 15;
                break;
            case "base/05_apple":
                score += 21;
                break;
            case "base/06_pear":
                score += 28;
                break;
            case "base/07_peach":
                score += 36;
                break;
            case "base/08_pineapple":
                score += 45;
                break;
            case "base/09_melon":
                score += 55;
                break;
            case "base/10_watermelon":
                score += 66;
                break;
        }
        if (scoreListener != null) {
            scoreListener.onScoreUpdate(score);
        }
    }


    // 게임오버 체크 메소드 추가
    private void checkGameOver() {
        if (isGameOver)
            return;

        for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
            // 현재 조작중인 과일은 제외
            if (body == currentBody)
                continue;

            // Fruit 객체를 가진 바디만 체크
            if (body.getUserData() instanceof Fruit) {
                // 과일의 중심점이 topLine보다 위에 있으면 게임오버
                if (body.getPosition().y < topLine.getPosition().y-0.5) {
                    gameOver();
                    break;
                }
            }
        }
    }
    // 게임오버 처리 메소드
    private void gameOver() {
        isGameOver = true;

        leftMoveTimer.stop();
        rightMoveTimer.stop();
        this.setFocusable(false);

        repaint();
    }
    
    public Fruit getNextFruit() {
        return nextFruit;
    }
    private void updateNextFruit() {
        nextFruit = FruitType.getRandomFruit();
        if (scoreListener != null) {
            scoreListener.onScoreUpdate(score); 
        }
    }
}

