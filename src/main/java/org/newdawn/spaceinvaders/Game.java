package org.newdawn.spaceinvaders;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.newdawn.spaceinvaders.entity.*;
import org.newdawn.spaceinvaders.stage.Stage;
import org.newdawn.spaceinvaders.stage.MarsStage;
import org.newdawn.spaceinvaders.stage.JupiterStage;
import org.newdawn.spaceinvaders.stage.SaturnStage;
import org.newdawn.spaceinvaders.stage.UranusStage;
import org.newdawn.spaceinvaders.stage.NeptuneStage;
import org.newdawn.spaceinvaders.stage.BlackHoleBossStage;


/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 *
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 *
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 *
 * @author Kevin Glass
 */
public class Game
{
    public enum GameState {
        START_MENU, // 시작 메뉴
        SIGN_IN,    // 로그인 화면
        SIGN_UP,    // 회원가입 화면
        PVP_MENU,   // PvP 모드 선택 화면
        PVP_LOBBY,  // 매치메이킹 대기 화면
        PLAYING_SINGLE,    // 기존 혼자하기 모드
        PLAYING_PVP, // PvP 게임 플레이 중
        MY_PAGE
    }
    private GameState currentState;

    private StartMenuPanel startMenuPanel;
    private SignInPanel signInPanel;
    private SignUpPanel signUpPanel;
    private GamePlayPanel gamePlayPanel;
    private PvpMenuPanel pvpMenuPanel;
    private PvpLobbyPanel pvpLobbyPanel;
    private MyPagePanel myPagePanel;
    /** The stragey that allows us to use accelerate page flipping */
    private BufferStrategy strategy;
    /** True if the game is currently "running", i.e. the game loop is looping */
    private boolean gameRunning = true;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> removeList = new ArrayList<>();
    /** The entity representing the player */
    private Entity ship;
    private Entity opponentShip;
    /** The speed at which the player's ship should move (pixels/sec) */
    private double moveSpeed = 300;
    /** The time at which last fired a shot */
    private long lastFire = 0;
    /** The interval between our players shot (ms) */
    private long firingInterval = 500;
    /** The number of aliens left on the screen */
    private int alienCount;
    // shop 브랜치에서 복사해올 변수들
    public int missileCount = 1; // 다중 미사일 기능
    public int coins = 0;
    public boolean shopOpen = false;
    public final int UPGRADE_COST = 10;
    public final int MAX_UPGRADES = 6;
    public int attackLevel = 0;
    public int moveLevel = 0;
    public int missileLevel = 0;
    public int score = 0;
    public RankingManager rankingManager;
    public int currentLevel = 1;
    public static final int BOSS_LEVEL = 5;
    private Thread matchmakingThread;
    private String currentMatchId;
    private volatile String player1_uid;
    private volatile String player2_uid;
    private Thread networkThread;
    /** The message to display which waiting for a key press */
    private String message = "";
    /** True if we're holding up game play until a key has been pressed */
    private boolean waitingForKeyPress = true;
    /** True if the left cursor key is currently pressed */
    private boolean leftPressed = false;
    /** True if the right cursor key is currently pressed */
    private boolean rightPressed = false;
    /** True if we are firing */
    private boolean firePressed = false;
    /** True if game logic needs to be applied this loop, normally as a result of a game event */
    private boolean logicRequiredThisLoop = false;
    private JFrame container;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // ===== Stage System =====
    private int stageIndex = 1;
    private Stage currentStage;
    private final int MAX_STAGE = 6;   // 총 스테이지 수 (Mars~BlackHole)   // Mars~BlackHole 총 6개라고 가정
    private boolean transitionRequested = false;
    private long slowTimer = 0;
    public boolean reverseControls = false; // 조작 반전 상태 플래그
    private boolean upPressed = false;
    private boolean downPressed = false;


    /**
     * Construct our game and set it running.
     */
    public GameState getCurrentState() {
        return currentState;
    }
    public Entity getShip() {
        return this.ship;
    }
    public Entity getOpponentShip() {
        return this.opponentShip;
    }
    public boolean amIPlayer1() {
        // 현재 로그인한 사용자의 UID를 가져옵니다.
        String myUid = CurrentUserManager.getInstance().getUid();

        // player1_uid나 myUid가 아직 설정되지 않은 경우(null)를 대비한 안전장치입니다.
        // 두 값 모두 유효할 때만 비교를 수행합니다.
        if (myUid != null && player1_uid != null) {
            return myUid.equals(player1_uid);
        }

        // 정보가 불완전하면 기본값으로 false를 반환하여 오류를 방지합니다.
        return false;
    }
    // Game.java 파일 내부에 추가

    public void resetSinglePlayerState() {
        this.currentLevel = 1;
        this.score = 0;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }


    public Game() {

        currentState = GameState.START_MENU;

        FirebaseInitializer firebase = new FirebaseInitializer();
        firebase.initialize();

        // create a frame to contain our game
        container = new JFrame("Space Invaders 102");

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setPreferredSize(new Dimension(800, 600));

        startMenuPanel = new StartMenuPanel(this);
        signInPanel = new SignInPanel(this);
        signUpPanel = new SignUpPanel(this);
        gamePlayPanel = new GamePlayPanel(this);
        pvpMenuPanel = new PvpMenuPanel(this);
        pvpLobbyPanel = new PvpLobbyPanel(this);
        myPagePanel = new MyPagePanel(this);
        // dev 브랜치의 Game() 생성자 안에 추가
        rankingManager = new RankingManager();
        score = 0;

        mainPanel.add(startMenuPanel, "START");
        mainPanel.add(signInPanel, "SIGN_IN");
        mainPanel.add(signUpPanel, "SIGN_UP");
        mainPanel.add(gamePlayPanel, "PLAYING_SINGLE");
        mainPanel.add(pvpMenuPanel, "PVP_MENU");
        mainPanel.add(pvpLobbyPanel, "PVP_LOBBY");
        mainPanel.add(myPagePanel, "MY_PAGE");

        container.getContentPane().add(mainPanel);

        changeState(GameState.START_MENU);

        gamePlayPanel.addKeyListener(new KeyInputHandler());
        gamePlayPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // ▼▼▼ '메뉴로 돌아가기' 버튼 클릭 처리 추가 ▼▼▼
                // 게임이 끝나고(waitingForKeyPress) 싱글 플레이 상태일 때
                if (Game.this.isWaitingForKeyPress() && Game.this.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
                    if (mx >= 325 && mx <= 475 && my >= 550 && my <= 590) {
                        int choice = JOptionPane.showConfirmDialog(
                                container,
                                "진행 상황이 저장되지 않습니다. 메뉴로 돌아가시겠습니까?\n(레벨 1부터 다시 시작합니다)",
                                "메뉴로 돌아가기",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            Game.this.resetSinglePlayerState(); // '예'를 눌렀을 때만 초기화
                            Game.this.changeState(GameState.PVP_MENU);
                        }
                        // '아니오'를 누르면 아무것도 하지 않고 게임 종료 화면에 남아있습니다.
                        return;
                    }
                }

                // simple shop button at top-right
                if (mx >= 720 && mx <= 780 && my >= 10 && my <= 40) {
                    shopOpen = !shopOpen;
                    return;
                }
                // if shop open, check option clicks
                if (shopOpen) {
                    // calculate panel positions consistent with rendering
                    int overlayX = 40;
                    int overlayY = 40;
                    int overlayW = 720;
                    int overlayH = 520;
                    int pad = 20;
                    int panelW = (overlayW - pad*4)/3; // space for 3 panels and paddings
                    int panelH = overlayH - 120;
                    int panelY = overlayY + 60;
                    for (int i=0;i<3;i++) {
                        int px = overlayX + pad + i*(panelW + pad);
                        int py = panelY;
                        if (mx >= px && mx <= px + panelW && my >= py && my <= py + panelH) {
                            if (i == 0) purchaseAttackSpeed();
                            if (i == 1) purchaseMoveSpeed();
                            if (i == 2) purchaseMissileCount();
                        }
                    }
                }
            }
        });
        gamePlayPanel.setFocusable(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setLocationRelativeTo(null);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                gameRunning = false;
                System.exit(0);
            }
        });

        Thread gameThread = new Thread(()-> {
            this.gameLoop();
        });
        gameThread.start();
    }

    public void changeState(GameState newState) {
        currentState = newState;

        if (matchmakingThread != null && matchmakingThread.isAlive()) {
            matchmakingThread.interrupt();
        }
        if (networkThread != null && networkThread.isAlive()) {
            networkThread.interrupt();
        }

        switch (currentState) {
            case START_MENU:
                cardLayout.show(mainPanel, "START");
                break;
            case SIGN_IN:
                cardLayout.show(mainPanel, "SIGN_IN");
                break;
            case SIGN_UP:
                cardLayout.show(mainPanel, "SIGN_UP");
                break;
            case PVP_MENU:
                cardLayout.show(mainPanel, "PVP_MENU");
                break;
            case PVP_LOBBY:
                cardLayout.show(mainPanel, "PVP_LOBBY");
                startMatchmakingLoop();
                break;
            case PLAYING_SINGLE:
                cardLayout.show(mainPanel, "PLAYING_SINGLE");
                gamePlayPanel.requestFocusInWindow();
                startGame();
                break;
            case PLAYING_PVP:
                waitingForKeyPress = false;
                cardLayout.show(mainPanel, "PLAYING_SINGLE");
                SwingUtilities.invokeLater(() -> {
                    gamePlayPanel.requestFocusInWindow();
                });
                startPvpGame();
                break;
            case MY_PAGE:
                cardLayout.show(mainPanel, "MY_PAGE");
                myPagePanel.updateUser();
                break;
        }
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    private void startGame() {
        // 모든 엔티티 초기화
        entities.clear();
        removeList.clear();

        slowTimer = 0;
        reverseControls = false;

        // 스테이지 기본값 초기화
        stageIndex = 1;
        currentStage = loadStage(stageIndex);

        // 플레이어 배 초기화 (스테이지보다 먼저 생성)
        initPlayer();

        // 스테이지 적/보스 생성
        if (currentStage != null) {
            currentStage.init();
        }

        leftPressed = rightPressed = firePressed = false;
        waitingForKeyPress = false;
    }
    /**
     * 다음 스테이지로 안전하게 전환하는 메소드
     */
    /**
     * 다음 스테이지로 안전하게 전환하는 메소드
     */
    // Game.java 내부

    public void nextStage() {
        waitingForKeyPress = false;

        // [중요] 엔티티 리스트뿐만 아니라 '삭제 대기 목록'도 반드시 비워야 합니다.
        entities.clear();
        removeList.clear();

        // [수정] 스테이지 전환/재시작 시 조작 반전 및 키 입력 상태 초기화
        reverseControls = false;
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
        upPressed = false;   // 혹시 눌린 상태로 시작되는 것 방지
        downPressed = false;

        // 스테이지 제한 확인 (예: 6단계)
        if (stageIndex > MAX_STAGE) {
            message = "ALL STAGES CLEAR! Returning to Menu...";
            waitingForKeyPress = true;
            changeState(GameState.PVP_MENU);
            return;
        }

        currentStage = loadStage(stageIndex);
        initPlayer();

        if (currentStage != null) {
            currentStage.init();
        } else {
            changeState(GameState.PVP_MENU);
        }
    }

    private void startMatchmakingLoop() {
        matchmakingThread = new Thread(() -> {
            FirebaseClientService clientService = new FirebaseClientService();
            String myUid = CurrentUserManager.getInstance().getUid();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("상대방 찾는 중...");
                    String opponentUid = clientService.findOpponent(myUid);

                    if (opponentUid != null) {
                        // === 방장(Creator) 로직 ===
                        if (myUid.compareTo(opponentUid) < 0) {
                            System.out.println("내가 방장! 게임 방 생성 시도.");
                            String matchId = clientService.createMatch(myUid, opponentUid);
                            if (matchId != null) {
                                this.currentMatchId = matchId;
                                this.player1_uid = myUid;
                                this.player2_uid = opponentUid;
                                clientService.deleteFromQueue(myUid);
                                clientService.deleteFromQueue(opponentUid);

                                SwingUtilities.invokeLater(() -> Game.this.changeState(GameState.PLAYING_PVP));
                                break;
                            }
                        }
                    } else {
                        // === 참가자(Follower) 로직 ▼▼▼ 수정 ▼▼▼ ===
                        if (!clientService.isUserInQueue(myUid)) {
                            System.out.println("큐에서 사라짐! 내 게임 방을 찾습니다.");
                            String matchId = clientService.findMyMatch(myUid);

                            if (matchId != null) {
                                this.currentMatchId = matchId;
                                System.out.println("매치 찾음! ID: " + matchId);

                                // 매치 데이터를 가져와서 player1, player2 uid 설정
                                Map<String, Object> matchData = clientService.getMatchData(matchId);
                                if (matchData != null) {
                                    this.player1_uid = (String) matchData.get("player1");
                                    this.player2_uid = (String) matchData.get("player2");

                                    // 모든 정보가 설정되었으므로 PVP 상태로 전환
                                    SwingUtilities.invokeLater(() -> Game.this.changeState(GameState.PLAYING_PVP));
                                    break;
                                }
                            }
                        }
                    }

                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    System.out.println("매치메이킹 중단됨.");
                    Thread.currentThread().interrupt();
                }
            }
        });
        matchmakingThread.start();
    }

    private void startPvpGame() {
        entities.clear();
        waitingForKeyPress = false;

        // 언제나 '나'는 아래쪽에 생성
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        ((ShipEntity) ship).setHealth(3);
        entities.add(ship);

        // 언제나 '상대방'은 위쪽에 생성
        opponentShip = new ShipEntity(this, "sprites/opponent_ship.gif", 370, 50);
        ((ShipEntity) opponentShip).setHealth(3);
        entities.add(opponentShip);

        startNetworkLoop();
    }

    private void startNetworkLoop() {
        networkThread = new Thread(() -> {
            FirebaseClientService clientService = new FirebaseClientService();
            String myUid = CurrentUserManager.getInstance().getUid();

            while (currentState == GameState.PLAYING_PVP && !Thread.currentThread().isInterrupted()) {
                try {
                    // --- 내 상태 전송 ---
                    Map<String, Object> myState = new java.util.HashMap<>();
                    myState.put("x", ship.getX());
                    myState.put("y", ship.getY());
                    myState.put("health", ((ShipEntity) ship).getCurrentHealth());

                    // 내가 쏜 총알들의 좌표 목록 생성
                    java.util.List<Map<String, Integer>> myShotsData = new ArrayList<>();
                    for (Entity entity : entities) {
                        if (entity instanceof ShotEntity) {
                            ShotEntity shot = (ShotEntity) entity;
                            if (myUid.equals(shot.getOwnerUid())) {
                                Map<String, Integer> shotData = new HashMap<>();
                                shotData.put("x", shot.getX());
                                shotData.put("y", shot.getY());
                                myShotsData.add(shotData);
                            }
                        }
                    }
                    myState.put("shots", myShotsData);

                    String myPlayerNode = amIPlayer1() ? "player1_state" : "player2_state";
                    clientService.updatePlayerState(currentMatchId, myPlayerNode, myState);

                    // --- 상대 상태 수신 ---
                    String opponentNode = amIPlayer1() ? "player2_state" : "player1_state";
                    Map<String, Object> opponentState = clientService.getOpponentState(currentMatchId, opponentNode);

                    if (opponentState != null) {
                        if (opponentState.get("x") != null) {
                            Object xObj = opponentState.get("x");
                            Object yObj = opponentState.get("y");
                            if (xObj instanceof Number && yObj instanceof Number) {
                                double opponentX = ((Number) xObj).doubleValue();
                                double opponentY = ((Number) yObj).doubleValue();
                                opponentShip.setLocation((int) opponentX, (int) opponentY);
                            }
                        }
                        String opponentUid = amIPlayer1() ? player2_uid : player1_uid;

                        if (opponentState.get("health") instanceof Number) {
                            int opponentHealth = ((Number) opponentState.get("health")).intValue();
                            ((ShipEntity)opponentShip).setCurrentHealth(opponentHealth);

                            // 만약 상대방 체력이 0 이하면, 내가 승리!
                            if (opponentHealth <= 0) {
                                SwingUtilities.invokeLater(this::notifyWinPVP);
                                break; // 네트워크 루프 종료
                            }
                        }

                        // 2-1. 기존의 상대방 총알들을 모두 제거 목록에 추가
                        for (Entity entity : entities) {
                            if (entity instanceof ShotEntity && opponentUid.equals(((ShotEntity) entity).getOwnerUid())) {
                                removeEntity(entity);
                            }
                        }
                        // 2-2. 새로 받은 총알 정보로 다시 생성
                        if (opponentState.get("shots") instanceof java.util.List) {
                            java.util.List<Map<String, Double>> opponentShotsData =
                                    (java.util.List<Map<String, Double>>) opponentState.get("shots");

                            for (Map<String, Double> shotData : opponentShotsData) {

                                double shotDX = 0;
                                double shotDY = 300; // 상대 탄환은 내 화면에서는 아래→위 방향

                                ShotEntity shot = new ShotEntity(
                                        this,
                                        "sprites/shot.gif",
                                        shotData.get("x").intValue(),
                                        shotData.get("y").intValue(),
                                        shotDX,
                                        shotDY
                                );

                                addEntity(shot);
                            }
                        }

                    }

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        networkThread.start();
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entitiy will be added to the overall list of entities in the game.
     */
    private void initPlayer() {
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        ((ShipEntity) ship).setHealth(3);
        this.slowTimer = 0;
        entities.add(ship);
    }

    private void initStandardStage() {
        double moveSpeed = 100;
        int alienRows = 3;
        double firingChance = 0;
        int startY = 50;

        switch (currentLevel) {
            case 1:
                moveSpeed = 100;
                alienRows = 3;
                firingChance = 0;
                break;
            case 2:
                moveSpeed = 130;
                alienRows = 4;
                firingChance = 0.0002;
                break;
            case 3:
                moveSpeed = 160;
                alienRows = 5;
                firingChance = 0.0005;
                break;
            case 4:
                moveSpeed = 200;
                alienRows = 5;
                firingChance = 0.0008;
                startY = 80;
                break;
        }

        // 설정된 값으로 외계인을 생성하고 entities 리스트에 추가합니다.
        // 이 부분이 누락되면 외계인이 나타나지 않습니다.
        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(this, "sprites/alien.gif", 100 + (x * 50), startY + row * 30, moveSpeed, firingChance);
                entities.add(alien);
                alienCount++;
            }
        }
    }
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Notification from a game entity that the logic of the game
     * should be run at the next opportunity (normally as a result of some
     * game event)
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
     * Remove an entity from the game. The entity removed will
     * no longer move or be drawn.
     *
     * @param entity The entity that should be removed
     */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    /**
     * Notification that the player has died.
     */
    public void notifyDeath() {
        if (currentState == GameState.PLAYING_PVP) {
            if (waitingForKeyPress) return; // 중복 호출 방지
            message = "You Lose...";
            waitingForKeyPress = true;
            // TODO: PVP 결과 화면을 보여준 뒤 PVP_MENU로 돌아가는 로직 추가
            return;
        }

        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;

        // CurrentUserManager를 통해 로그인 상태 확인
        if (CurrentUserManager.getInstance().isLoggedIn()) {
            // 로그인 상태이면, 현재 닉네임을 가져와서 바로 랭킹에 추가
            String nickname = CurrentUserManager.getInstance().getNickname();
            rankingManager.addScore(score, nickname);
            // "New High Score!" 같은 메시지는 notifyWin/notifyDeath 메시지에 포함시키거나,
            // GamePlayPanel에서 점수를 그릴 때 특별 효과를 주는 식으로 개선할 수 있습니다.
        } else {
            // 비로그인 상태일 때만 이름을 물어봄 (기존 방식)
            if (rankingManager.isHighScore(score)) {
                String name = JOptionPane.showInputDialog(container, "New High Score! Enter your name:", "Ranking", JOptionPane.PLAIN_MESSAGE);
                if (name != null && !name.trim().isEmpty()) {
                    rankingManager.addScore(score, name);
                }
            }
        }

        currentLevel = 1;// 죽으면 레벨 1로 리셋
        score = 0;
    }

    /**
     * Notification that the player has won since all the aliens
     * are dead.
     */
    public void notifyWin() {
        coins += 10;// 라운드 클리어 -> 코인
        currentLevel++;
        if (currentLevel > BOSS_LEVEL) {
            message = "Congratulations! You have defeated the final boss!";
            waitingForKeyPress = true;
            currentLevel = 1;
        } else if (currentLevel == BOSS_LEVEL) {
            message = "Final Stage! The Boss is approaching!";
            waitingForKeyPress = true;
        } else {
            message = "Stage " + (currentLevel - 1) + " Cleared! Prepare for the next stage.";
            waitingForKeyPress = true;
        }
    }
    // ▼▼▼ PVP 승리/패배 처리 메소드 추가 ▼▼▼
    public void notifyWinPVP() {
        if (currentState != GameState.PLAYING_PVP) return; // 중복 호출 방지
        coins += 30;
        message = "You win! 30 reward coins";
        waitingForKeyPress = true;
        // TODO: PVP 결과 화면을 보여준 뒤 PVP_MENU로 돌아가는 로직 추가
    }

    public void bossKilled() {
        message = "BOSS DEFEATED!";
        waitingForKeyPress = true;
        stageIndex++;
    }

    // 상점 옵션1: 공격 속도 증가 >> firing interval 10% 감소
    private void purchaseAttackSpeed() {
        if (attackLevel >= MAX_UPGRADES) return;
        if (coins < UPGRADE_COST) return;
        coins -= UPGRADE_COST;
        attackLevel++;
        // reduce interval but clamp to a minimum
        firingInterval = Math.max(100, (long)(firingInterval * 0.9));
    }

    // 상점 옵션2: 이동 속도 증가 >> move speed 10% 증가
    private void purchaseMoveSpeed() {
        if (moveLevel >= MAX_UPGRADES) return;
        if (coins < UPGRADE_COST) return;
        coins -= UPGRADE_COST;
        moveLevel++;
        moveSpeed = moveSpeed * 1.1;
    }

    // 상점 옵션3: 미사일 개수 증가 increment missiles by 1 up to a reasonable cap
    private void purchaseMissileCount() {
        if (missileLevel >= MAX_UPGRADES) return;
        if (coins < UPGRADE_COST) return;
        coins -= UPGRADE_COST;
        missileLevel++;
        missileCount = Math.min(5, missileCount + 1);
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        score += 100; // 기존 점수 획득
        coins += 1;  // [수정] 적 처치 시 10 코인 획득 (원하는 금액으로 조절 가능)
    }


    /**
     * Attempt to fire a shot from the player. Its called "try"
     * since we must first check that the player can fire at this
     * point, i.e. has he/she waited long enough between shots
     */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        lastFire = System.currentTimeMillis();

        // [추가] 발사 효과음 재생
        SoundManager.get().playSound("sounds/shoot.wav");

        int baseX = ship.getX() + 10;
        int baseY = ship.getY() - 30;

        double shotDX = 0;

        // [수정] 스테이지별 탄속 적용 (기본 -300, 화성 -150 등)
        double shotDY = -300; // 기본값
        if (currentStage != null) {
            shotDY = currentStage.getPlayerShotVelocity();
        }

        // 여러 발 미사일 처리
        for (int i = 0; i < missileCount; i++) {
            int offset = (i - (missileCount - 1) / 2) * 10;
            // ... (나머지 코드는 동일) ...
            ShotEntity shot = new ShotEntity(
                    this,
                    "sprites/shot.gif",
                    baseX + offset,
                    baseY,
                    shotDX,
                    shotDY // 수정된 속도 변수 사용
            );
            entities.add(shot);
        }
    }
    public void applySlow(long duration) {
        this.slowTimer = duration;

    }



    /**
     * The main game loop. This loop is running during all game
     * play as is responsible for the following activities:
     * <p>
     * - Working out the speed of the game loop to update moves
     * - Moving the game entities
     * - Drawing the screen contents (entities, text)
     * - Updating game events
     * - Checking Input
     * <p>
     */
    /**
     * The main game loop.
     */
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        while (gameRunning) {
            long now = System.currentTimeMillis();
            long delta = now - lastLoopTime;
            lastLoopTime = now;

            if (slowTimer > 0) {
                slowTimer -= delta;
            }

            if (transitionRequested) {
                nextStage();
                transitionRequested = false; // 요청 처리 완료 후 초기화
                // 전환 직후에는 델타 타임이 튀거나 로직이 꼬일 수 있으므로 이번 루프는 건너뜁니다.
                continue;
            }

            // ===========================
            //   1. PVP 모드 처리
            // ===========================
            if (currentState == GameState.PLAYING_PVP) {
                updatePvpGameplay(delta);
                gamePlayPanel.repaint(); // 화면 갱신 요청
                try { Thread.sleep(10); } catch (Exception e) {}
                continue;
            }

            // ===========================
            //   2. 싱글 플레이 로직
            // ===========================
            if (currentState == GameState.PLAYING_SINGLE) {

                // (1) 플레이어 이동 속도 설정 (키 입력 반영) - [누락되었던 부분 추가]
                if (!waitingForKeyPress && ship != null) {
                    ship.setHorizontalMovement(0); // 키를 안 누르면 멈춤
                    ship.setDY(0); // [추가] 수직 속도 초기화 (안 하면 계속 미끄러짐)

                    // 1. 속도 계산 (기존 슬로우 로직 유지)
                    double currentSpeed = moveSpeed;
                    if (slowTimer > 0) currentSpeed *= 0.5;

                    // [수정] 조작 반전 여부에 따른 입력 처리
                    boolean moveLeft = leftPressed;
                    boolean moveRight = rightPressed;

                    if (reverseControls) {
                        // 조작이 반대로 바뀜
                        moveLeft = rightPressed;
                        moveRight = leftPressed;
                    }

                    if (moveLeft && !moveRight) {
                        ship.setHorizontalMovement(-currentSpeed);
                    } else if (moveRight && !moveLeft) {
                        ship.setHorizontalMovement(currentSpeed);
                    }

                    // 4. [추가] 상하 이동 처리
                    boolean moveUp = upPressed;
                    boolean moveDown = downPressed;

                    if (reverseControls) {
                        moveUp = downPressed;
                        moveDown = upPressed;
                    }

                    if (moveUp && !moveDown) {
                        ship.setDY(-currentSpeed); // 위로 이동
                    } else if (moveDown && !moveUp) {
                        ship.setDY(currentSpeed);  // 아래로 이동
                    } else {
                        ship.setDY(0); // [중요] 키를 안 누르면 멈춰야 합니다!
                    }

                    if (currentStage instanceof NeptuneStage) {
                        NeptuneStage ns = (NeptuneStage) currentStage;
                        double wind = ns.getCurrentWindForce();

                        // 부스터가 꺼져있고, 바람이 불고 있다면
                        if (wind != 0 && !((ShipEntity)ship).isBoosterActive()) {
                            // 기존 움직임에 바람 속도를 더함 (밀려나는 효과)
                            // 키를 안 눌러도 바람 때문에 움직이게 됨
                            ship.setHorizontalMovement(ship.getDX() + wind);
                        }
                    }

                    if (firePressed) {
                        tryToFire();
                    }
                }

                // (2) 외계인 방향 전환 로직 (벽에 닿았을 때) - [누락되었던 부분 추가]
                if (logicRequiredThisLoop) {
                    for (Entity entity : entities) {
                        entity.doLogic();
                    }
                    logicRequiredThisLoop = false;
                }

                // (3) 스테이지 기믹 업데이트
                if (!waitingForKeyPress && currentStage != null) {
                    currentStage.update(delta);

                    if (currentStage.isCompleted()) {
                        waitingForKeyPress = true;

                        // [수정] 메시지를 먼저 설정하고, 그 다음에 단계를 올립니다.
                        message = "Stage " + stageIndex + " Clear!";
                        stageIndex++;

                        // (참고) notifyWin()은 사용하지 않고 이 로직으로 통일하는 것이 깔끔합니다.
                    }
                }
                // (4) "Press any key" 대기 중이 아닐 때만 이동 및 충돌 처리
                if (!waitingForKeyPress) {
                    // 엔티티 이동
                    for (int i = 0; i < entities.size(); i++) {
                        Entity entity = entities.get(i);
                        entity.move(delta);
                    }

                    // 충돌 체크
                    for (int p = 0; p < entities.size(); p++) {
                        for (int s = p + 1; s < entities.size(); s++) {
                            Entity me = entities.get(p);
                            Entity him = entities.get(s);

                            if (me.collidesWith(him)) {
                                me.collidedWith(him);
                                him.collidedWith(me);
                            }
                        }
                    }

                    // 삭제된 엔티티 제거
                    entities.removeAll(removeList);
                    removeList.clear();

                    // 다음 스테이지 로딩 로직 (엔티티가 다 사라졌을 때 등)
                    if (currentStage == null) { // 스테이지가 없으면 로드 시도
                        currentStage = loadStage(stageIndex);
                        if (currentStage != null) {
                            currentStage.init();
                        }
                    }
                }

                // (5) 화면 그리기 (repaint 호출)
                gamePlayPanel.repaint();
            }

            // 메뉴 화면 등에서도 repaint 필요 시 호출
            if (currentState != GameState.PLAYING_SINGLE && currentState != GameState.PLAYING_PVP) {
                mainPanel.repaint();
            }

            try { Thread.sleep(10); } catch (Exception e) {}
        }
    }

    private Rectangle getVisualBounds(Entity entity) {
        if (entity == null) return new Rectangle(0,0,0,0);
        int drawX = entity.getX();
        int drawY = entity.getY();
        String myUid = CurrentUserManager.getInstance().getUid();

        boolean isOpponentEntity = false;
        if (currentState == GameState.PLAYING_PVP) {
            if (entity == opponentShip ||
                    (entity instanceof ShotEntity && myUid != null && !((ShotEntity)entity).isOwnedBy(myUid))) {
                isOpponentEntity = true;
            }
        }

        if (isOpponentEntity) {
            drawY = container.getHeight() - entity.getY() - entity.getSpriteHeight();
        }

        return new Rectangle(drawX, drawY, entity.getSpriteWidth(), entity.getSpriteHeight());
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public boolean isWaitingForKeyPress() {
        return waitingForKeyPress;
    }

    public String getMessage() {
        return message;
    }

    private Stage loadStage(int index) {
        switch (index) {
            case 1: return new MarsStage(this);      // 1: 화성
            case 2: return new JupiterStage(this);   // 2: 목성
            case 3: return new SaturnStage(this);    // 3: 토성 (고리)
            case 4: return new UranusStage(this);    // 4: 천왕성
            case 5: return new NeptuneStage(this);   // 5: 해왕성
            case 6: return new BlackHoleBossStage(this); // 6: 보스
            default: return null;
        }
    }

    public String getBackgroundForLevel() {
        switch(currentLevel) {
            case 1: return "sprites/bg_mars.png";
            case 2: return "sprites/bg_jupiter.png";
            case 3: return "sprites/bg_saturn.png";
            case 4: return "sprites/bg_uranus.png";
            case 5: return "sprites/bg_neptune.png";
            case 6: return "sprites/bg_blackhole.png";
        }
        return "sprites/bg_default.png";
    }
    private void updatePvpGameplay(long delta) {
        // 이 안에는 PVP 모드에서 매 프레임 실행되는 내용만 넣음.
        // 현재는 네 기존 PvP move/shot/network 코드를 그대로 유지하면 됨.

        // 예시:
        // 상대방 위치 업데이트는 networkThread가 하고 있으므로
        // 여기서는 단순히 플레이어 이동/총알 이동만 하면 됨.

        for (Entity e : entities) {
            if (e != opponentShip) {
                e.move(delta);
            }
        }
    }




    /**
     * A class to handle keyboard input from the user. The class
     * handles both dynamic input during game play, i.e. left/right
     * and shoot, and more static type input (i.e. press any key to
     * continue)
     *
     * This has been implemented as an inner class more through
     * habbit then anything else. Its perfectly normal to implement
     * this as seperate class if slight less convienient.
     *
     * @author Kevin Glass
     */
    // Game.java 파일의 KeyInputHandler 클래스 전체를 아래 코드로 교체

    private class KeyInputHandler extends KeyAdapter {

        // Game.java 내부의 KeyInputHandler 클래스 -> keyPressed 메소드

        @Override
        public void keyPressed(KeyEvent e) {
            // 1. ESC -> 게임 종료
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }

            // 2. 대기 상태에서 키 입력 -> 다음 스테이지 또는 메뉴로
            if (waitingForKeyPress) {
                if (currentState == GameState.PLAYING_SINGLE) {
                    transitionRequested = true; // [수정] 직접 호출 대신 플래그 설정
                    return;
                }
                else if (currentState == GameState.PLAYING_PVP) {
                    waitingForKeyPress = false;
                    changeState(GameState.PVP_MENU);
                    return;
                }
            }

            // 3. 게임 플레이 키 입력
            if (currentState == GameState.PLAYING_SINGLE ||
                    currentState == GameState.PLAYING_PVP) {

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;

                if (e.getKeyCode() == KeyEvent.VK_SPACE) firePressed = true;

                // [삭제] 테스트용 'S' 키 코드 제거됨
                // 아이템을 먹어야만 효과가 발동됩니다.
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (waitingForKeyPress) return;

            if (currentState == GameState.PLAYING_SINGLE ||
                    currentState == GameState.PLAYING_PVP) {

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;

                if (e.getKeyCode() == KeyEvent.VK_SPACE) firePressed = false;

            }
        }

    }

    /**
     * The entry point into the game. We'll simply create an
     * instance of class which will start the display and game
     * loop.
     *
     * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        SwingUtilities.invokeLater(() -> {
            new Game();
        });
    }
}
