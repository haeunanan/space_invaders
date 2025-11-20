package org.newdawn.spaceinvaders;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.newdawn.spaceinvaders.entity.*;

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
		MY_PAGE,
		COOP_LOBBY,
		PLAYING_COOP
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
			case COOP_LOBBY:
				cardLayout.show(mainPanel, "PVP_LOBBY"); // UI는 PVP 로비 재사용 (텍스트만 바꾸면 됨)
				startCoopMatchmakingLoop(); // 협동 매칭 루프 시작
				break;
			case PLAYING_COOP: // 협동 게임 시작
				waitingForKeyPress = false;
				cardLayout.show(mainPanel, "PLAYING_SINGLE"); // 게임 화면 재사용
				SwingUtilities.invokeLater(() -> gamePlayPanel.requestFocusInWindow());
				startCoopGame(); // 협동 게임 초기화
				break;
		}
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame() {
		// clear out any existing entities and intialise a new set
		entities.clear();
		alienCount = 0;
		initEntities();
		
		// blank out any keyboard settings we might currently have
		leftPressed = false;
		rightPressed = false;
		firePressed = false;

		waitingForKeyPress = false;
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

			while ((currentState == GameState.PLAYING_PVP || currentState == GameState.PLAYING_COOP) && !Thread.currentThread().isInterrupted()) {
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
							java.util.List<Map<String, Double>> opponentShotsData = (java.util.List<Map<String, Double>>) opponentState.get("shots");
							for (Map<String, Double> shotData : opponentShotsData) {
								ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", shotData.get("x").intValue(), shotData.get("y").intValue(), opponentUid);
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

	// ▼▼▼ 협동용 게임 시작 메소드 ▼▼▼
	private void startCoopGame() {
		entities.clear();

		// 내 우주선 (아래쪽)
		ship = new ShipEntity(this, "sprites/ship.gif", 300, 550); // X좌표를 약간 왼쪽으로
		((ShipEntity) ship).setHealth(3);
		entities.add(ship);

		// 상대방 우주선 (같은 편! 아래쪽)
		opponentShip = new ShipEntity(this, "sprites/ship.gif", 500, 550); // X좌표를 약간 오른쪽으로, 이미지는 ship.gif 사용
		((ShipEntity) opponentShip).setHealth(3);
		entities.add(opponentShip);

		// 외계인 생성 (싱글 플레이처럼 적들도 생성해야 함!)
		alienCount = 0;
		initStandardStage(); // 1단계 적들 생성

		startNetworkLoop(); // 네트워크 동기화 시작 (PVP와 같은 루프 사용해도 됨)

		waitingForKeyPress = false;

	}

	private void startCoopMatchmakingLoop() {
		matchmakingThread = new Thread(() -> {
			FirebaseClientService clientService = new FirebaseClientService();
			String myUid = CurrentUserManager.getInstance().getUid();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					System.out.println("협동 상대 찾는 중...");
					String opponentUid = clientService.findCoopOpponent(myUid); // Coop 메소드 사용

					if (opponentUid != null) {
						// 방장 로직
						if (myUid.compareTo(opponentUid) < 0) {
							String matchId = clientService.createMatch(myUid, opponentUid);
							if (matchId != null) {
								this.currentMatchId = matchId;
								this.player1_uid = myUid;
								this.player2_uid = opponentUid;
								clientService.deleteFromCoopQueue(myUid);       // Coop 큐에서 삭제
								clientService.deleteFromCoopQueue(opponentUid); // Coop 큐에서 삭제
								SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_COOP));
								break;
							}
						}
					} else {
						// 참가자 로직
						if (!clientService.isUserInCoopQueue(myUid)) { // Coop 큐 확인
							String matchId = clientService.findMyMatch(myUid); // 매치 찾는건 동일 (matches 경로는 공유)
							if (matchId != null) {
								this.currentMatchId = matchId;
								Map<String, Object> matchData = clientService.getMatchData(matchId);
								if (matchData != null) {
									this.player1_uid = (String) matchData.get("player1");
									this.player2_uid = (String) matchData.get("player2");
									SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_COOP));
									break;
								}
							}
						}
					}
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		matchmakingThread.start();
	}
	
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
	private void initEntities() {
		alienCount = 0;

		// 1. 플레이어 우주선을 생성하고 entities 리스트에 추가합니다.
		// 이 부분이 누락되면 우주선이 나타나지 않습니다.
		ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
		((ShipEntity)ship).setHealth(1);
		entities.add(ship);

		// 3. 현재 레벨에 맞는 스테이지를 설정합니다.
		if (currentLevel >= BOSS_LEVEL) {
			initBossStage();
		} else {
			initStandardStage();
		}
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
	private void initBossStage() {
		Entity boss = new BossEntity(this, "sprites/boss.gif", 350, 50);
		entities.add(boss);
		// 보스 스테이지에서는 alienCount가 아닌 다른 방식으로 승리 조건을 관리합니다.
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
			return;
		}

		message = "Oh no! They got you, try again?";
		waitingForKeyPress = true;

		// CurrentUserManager를 통해 로그인 상태 확인
		if (CurrentUserManager.getInstance().isLoggedIn()) {
			// 로그인 상태이면, 현재 닉네임을 가져와서 바로 랭킹에 추가
			String nickname = CurrentUserManager.getInstance().getNickname();
			rankingManager.addScore(score, nickname);
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

	public void notifyBossKilled() {
		notifyWin();
		score += 5000;// 보스를 이기면 게임에서 승리
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
		// reduce the alient count, if there are none left, the player has won!
		alienCount--;
		score += 100;
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
		
		// if we waited long enough, create the shot entity, and record the time.
		lastFire = System.currentTimeMillis();
		String myUid = CurrentUserManager.getInstance().getUid(); // 내 UID 가져오기
		int baseX = ship.getX()+10;
		int baseY = ship.getY()-30;
		// spread shots slightly when multiple missiles
		for (int i=0;i<missileCount;i++) {
			int offset = (i - (missileCount-1)/2) * 10; // centers the spread
			ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",baseX + offset,baseY,myUid);
			entities.add(shot);
		}
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
	public void gameLoop() {
		long lastLoopTime = SystemTimer.getTime();
		while (gameRunning) {
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();

			// --- 1. 키 입력 처리 ---
			// 'waiting' 상태가 아닐 때만 키 입력을 처리합니다.
			if (!waitingForKeyPress && ship != null) {
				ship.setHorizontalMovement(0);
				if (leftPressed && !rightPressed) ship.setHorizontalMovement(-moveSpeed);
				else if (rightPressed && !leftPressed) ship.setHorizontalMovement(moveSpeed);
				if (firePressed) tryToFire();
			}

			// --- 2. 로직 업데이트 (엔티티 이동) ---
			if (!waitingForKeyPress) {
				for (Entity entity : new ArrayList<>(entities)) {
					// PVP 모드에서는 상대방 우주선은 네트워크로만 위치가 설정되므로 move() 생략
					if (currentState == GameState.PLAYING_PVP && entity == opponentShip) {
						continue;
					}
					entity.move(delta);
				}
			}

			// --- 3. 상태별 로직 처리 ---
			if (currentState == GameState.PLAYING_SINGLE) {
				if (!waitingForKeyPress) {
					// 외계인 방향 전환 로직
					if (logicRequiredThisLoop) {
						for (Entity entity : entities) {
							entity.doLogic();
						}
						logicRequiredThisLoop = false;
					}

					// --- 싱글 플레이 충돌 판정 ---
					for (int p = 0; p < entities.size(); p++) {
						for (int s = p + 1; s < entities.size(); s++) {
							Entity me = entities.get(p);
							Entity him = entities.get(s);

							if (me.collidesWith(him)) {
								// 플레이어 총알 vs 적
								if (me instanceof ShotEntity && (him instanceof AlienEntity || him instanceof BossEntity)) {
									if (him instanceof AlienEntity) { removeEntity(him); notifyAlienKilled(); }
									else { ((BossEntity) him).takeDamage(); }
									removeEntity(me);
								} else if (him instanceof ShotEntity && (me instanceof AlienEntity || me instanceof BossEntity)) {
									if (me instanceof AlienEntity) { removeEntity(me); notifyAlienKilled(); }
									else { ((BossEntity) me).takeDamage(); }
									removeEntity(him);
								}
								// 적 총알 vs 플레이어
								else if ((me instanceof AlienShotEntity || me instanceof BossShotEntity) && him == ship) {
									((ShipEntity) him).takeDamage();
									removeEntity(me);
								} else if ((him instanceof AlienShotEntity || him instanceof BossShotEntity) && me == ship) {
									((ShipEntity) me).takeDamage();
									removeEntity(him);
								}
							}
						}
					}
					int aliensRemaining = 0;
					for (Entity e : entities) {
						if (e instanceof AlienEntity) aliensRemaining++;
					}

					if (aliensRemaining == 0 && !waitingForKeyPress && currentLevel < BOSS_LEVEL) {
						notifyWin();
					}
				}
			}
			else if (currentState == GameState.PLAYING_PVP) {
				// --- PVP 충돌 판정 (시각적 위치 기준) ---
				String myUid = CurrentUserManager.getInstance().getUid();
				for (int p = 0; p < entities.size(); p++) {
					for (int s = p + 1; s < entities.size(); s++) {
						Entity me = entities.get(p);
						Entity him = entities.get(s);

						// 두 엔티티의 '시각적' 위치를 기준으로 충돌 사각형 생성
						Rectangle meRect = getVisualBounds(me);
						Rectangle himRect = getVisualBounds(him);

						if (meRect.intersects(himRect)) {
							// Case 1: 내 총알 vs 상대 우주선
							if (me instanceof ShotEntity && ((ShotEntity)me).isOwnedBy(myUid) && him == opponentShip) {
								removeEntity(me); // 내 총알 제거
							} else if (him instanceof ShotEntity && ((ShotEntity)him).isOwnedBy(myUid) && me == opponentShip) {
								removeEntity(him);
							}

							// Case 2: 상대방 총알 vs 내 우주선
							else if (me instanceof ShotEntity && !((ShotEntity)me).isOwnedBy(myUid) && him == ship) {
								removeEntity(me); // 상대방 총알 제거
								((ShipEntity)him).takeDamage(); // 내 우주선 데미지
							} else if (him instanceof ShotEntity && !((ShotEntity)him).isOwnedBy(myUid) && me == ship) {
								removeEntity(him);
								((ShipEntity)me).takeDamage();
							}
						}
					}
				}
			}
			else if (currentState == GameState.PLAYING_COOP) {
				if (ship == null || opponentShip == null) {
					try { Thread.sleep(10); } catch (InterruptedException e) {}
					continue;
				}

				// 1. 내 입력 처리
				ship.setHorizontalMovement(0);
				if ((leftPressed) && (!rightPressed)) ship.setHorizontalMovement(-moveSpeed);
				else if ((rightPressed) && (!leftPressed)) ship.setHorizontalMovement(moveSpeed);
				if (firePressed) tryToFire();

				// 2. 엔티티 이동 (적군 + 내 우주선 + 총알들)
				// 상대방 우주선(opponentShip)은 네트워크 스레드가 움직이므로 여기서 건너뜁니다.
				for (Entity entity : new ArrayList<>(entities)) {
					if (entity == opponentShip) continue;
					entity.move(delta);
				}

				// 3. 외계인 방향 전환 로직 (싱글 플레이와 동일)
				if (logicRequiredThisLoop) {
					for (Entity entity : entities) {
						entity.doLogic();
					}
					logicRequiredThisLoop = false;
				}

				// 4. 협동 모드 충돌 판정
				String myUid = CurrentUserManager.getInstance().getUid();

				// 충돌 계산을 위한 복사본 생성
				ArrayList<Entity> currentEntities = new ArrayList<>(entities);

				for (int p = 0; p < currentEntities.size(); p++) {
					for (int s = p + 1; s < currentEntities.size(); s++) {
						Entity me = currentEntities.get(p);
						Entity him = currentEntities.get(s);

						if (me.collidesWith(him)) {
							// (1) 아군(나 또는 동료)의 총알이 적(외계인/보스)을 맞췄을 때
							if (me instanceof ShotEntity && (him instanceof AlienEntity || him instanceof BossEntity)) {
								removeEntity(me); // 총알 제거
								if (him instanceof AlienEntity) {
									removeEntity(him);
									notifyAlienKilled(); // 점수 증가
								} else if (him instanceof BossEntity) {
									((BossEntity) him).takeDamage();
								}
							}
							else if (him instanceof ShotEntity && (me instanceof AlienEntity || me instanceof BossEntity)) {
								removeEntity(him); // 총알 제거
								if (me instanceof AlienEntity) {
									removeEntity(me);
									notifyAlienKilled();
								} else if (me instanceof BossEntity) {
									((BossEntity) me).takeDamage();
								}
							}

							// (2) 적의 총알이 '나(ship)'에게 맞았을 때 -> 내 체력 감소
							else if ((me instanceof AlienShotEntity || me instanceof BossShotEntity) && him == ship) {
								removeEntity(me);
								((ShipEntity) him).takeDamage();
							}
							else if ((him instanceof AlienShotEntity || him instanceof BossShotEntity) && me == ship) {
								removeEntity(him);
								((ShipEntity) me).takeDamage();
							}

							// (3) 적의 총알이 '동료(opponentShip)'에게 맞았을 때 -> 시각적으로 총알만 제거
							else if ((me instanceof AlienShotEntity || me instanceof BossShotEntity) && him == opponentShip) {
								removeEntity(me);
							}
							else if ((him instanceof AlienShotEntity || him instanceof BossShotEntity) && me == opponentShip) {
								removeEntity(him);
							}

							// (4) 적 몸체 충돌
							else if ((me instanceof AlienEntity || me instanceof BossEntity) && him == ship) {
								((ShipEntity) him).takeDamage();
							}
							else if ((him instanceof AlienEntity || him instanceof BossEntity) && me == ship) {
								((ShipEntity) me).takeDamage();
							}
						}
					}
				}

				// 5. 스테이지 클리어 확인
				if (currentLevel < BOSS_LEVEL) {
					int aliensRemaining = 0;
					for (Entity e : entities) {
						if (e instanceof AlienEntity) aliensRemaining++;
					}
					if (aliensRemaining == 0 && !waitingForKeyPress) {
						notifyWin();
					}
				}
			}
			// 6. 공통 정리 및 그리기
			entities.removeAll(removeList);
			removeList.clear();
			gamePlayPanel.repaint();

			try { SystemTimer.sleep(10); } catch (Exception e) {}
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
		/**
		 * 키가 눌렸을 때 호출됩니다.
		 */
		public void keyPressed(KeyEvent e) {
			// 게임 플레이 중(싱글 또는 PVP)일 때만 키 입력을 처리합니다.
			if (currentState == GameState.PLAYING_SINGLE || currentState == GameState.PLAYING_PVP || currentState == GameState.PLAYING_COOP) {

				// "Press any key" 상태일 때의 특별 처리
				if (waitingForKeyPress) {
					// 싱글 플레이 종료 후에는 게임 재시작
					if (currentState == Game.GameState.PLAYING_SINGLE) {
						waitingForKeyPress = false;
						startGame();
					}
					// PVP 종료 후에는 PVP 메뉴로 이동
					else if (currentState == Game.GameState.PLAYING_PVP) {
						// waitingForKeyPress는 다음 화면에서 false로 설정될 것이므로 여기서는 상태만 변경
						changeState(GameState.PVP_MENU);
					}
					else if (currentState == Game.GameState.PLAYING_COOP) {
						changeState(GameState.PVP_MENU);
					}
					return; // 키 입력 처리를 여기서 마침
				}


				// 공통 키 입력 처리
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					leftPressed = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					rightPressed = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					firePressed = true;
				}
			}
		}
		/**
		 * 키에서 손을 뗐을 때 호출됩니다.
		 */
		public void keyReleased(KeyEvent e) {
			// 게임 플레이 중(싱글 또는 PVP)일 때만 키 입력을 처리합니다.
			if (currentState == GameState.PLAYING_SINGLE || currentState == GameState.PLAYING_PVP || currentState == GameState.PLAYING_COOP) {

				// waitingForKeyPress 상태에서는 키를 떼는 동작은 무시합니다.
				if (waitingForKeyPress) {
					return;
				}

				// 공통 키 입력 처리
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					leftPressed = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					rightPressed = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					firePressed = false;
				}
			}
		}
		/**
		 * 키를 타이핑했을 때 호출됩니다. (ESC 키 종료 등)
		 */
		public void keyTyped(KeyEvent e) {
			// ESC 키는 언제든지 게임을 종료할 수 있도록 합니다.
			if (e.getKeyChar() == 27) { // 27은 ESC 키의 아스키 코드
				System.exit(0);
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
