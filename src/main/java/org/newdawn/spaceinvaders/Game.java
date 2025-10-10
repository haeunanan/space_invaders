package org.newdawn.spaceinvaders;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.*;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

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
		PLAYING     // 게임 중
	}
	private GameState currentState;

	private StartMenuPanel startMenuPanel;
	private SignInPanel signInPanel;
	private SignUpPanel signUpPanel;
	private GamePlayPanel gamePlayPanel;
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	/** The list of all the entities that exist in our game */
	private ArrayList entities = new ArrayList();
	/** The list of entities that need to be removed from the game this loop */
	private ArrayList removeList = new ArrayList();
	/** The entity representing the player */
	private Entity ship;
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
	/** The last time at which we recorded the frame rate */
	private long lastFpsTime;
	/** The current number of frames recorded */
	private int fps;
	/** The normal title of the game window */
	private String windowTitle = "Space Invaders 102";
	/** The game window that we'll update with the frame count */
	private JFrame container;
	private CardLayout cardLayout;
	private JPanel mainPanel;
	
	/**
	 * Construct our game and set it running.
	 */
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

		mainPanel.add(startMenuPanel, "START");
		mainPanel.add(signInPanel, "SIGN_IN");
		mainPanel.add(signUpPanel, "SIGN_UP");
		mainPanel.add(gamePlayPanel, "PLAYING");

		container.getContentPane().add(mainPanel);

		changeState(GameState.START_MENU);

		gamePlayPanel.addKeyListener(new KeyInputHandler());
		gamePlayPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println("Mouse clicked at: " + e.getX() + ", " + e.getY());

				int mx = e.getX();
				int my = e.getY();
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

	// Game.java에 아래 메소드 추가

	public void changeState(GameState newState) {
		currentState = newState;

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
			case PLAYING:
				cardLayout.show(mainPanel, "PLAYING");
				gamePlayPanel.requestFocusInWindow();
				startGame();
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
		initEntities();
		
		// blank out any keyboard settings we might currently have
		leftPressed = false;
		rightPressed = false;
		firePressed = false;
	}
	
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
	private void initEntities() {
		// create the player ship and place it roughly in the center of the screen
		ship = new ShipEntity(this,"sprites/ship.gif",370,550);
		entities.add(ship);
		
		// create a block of aliens (5 rows, by 12 aliens, spaced evenly)
		alienCount = 0;
		for (int row=0;row<5;row++) {
			for (int x=0;x<12;x++) {
				Entity alien = new AlienEntity(this,100+(x*50),(50)+row*30);
				entities.add(alien);
				alienCount++;
			}
		}
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
		message = "Oh no! They got you, try again?";
		waitingForKeyPress = true;
	}
	
	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
		message = "Well done! You Win!";
		waitingForKeyPress = true;
		coins += 10;// 라운드 클리어 -> 코인
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
		
		if (alienCount == 0) {
			notifyWin();
		}
		
		// if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens
		for (int i=0;i<entities.size();i++) {
			Entity entity = (Entity) entities.get(i);
			
			if (entity instanceof AlienEntity) {
				// speed up by 2%
				entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02);
			}
		}
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
		int baseX = ship.getX()+10;
		int baseY = ship.getY()-30;
		// spread shots slightly when multiple missiles
		for (int i=0;i<missileCount;i++) {
			int offset = (i - (missileCount-1)/2) * 10; // centers the spread
			ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",baseX + offset,baseY);
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
		
		// keep looping round til the game ends
		while (gameRunning) {
			// work out how long its been since the last update, this
			// will be used to calculate how far the entities should
			// move this loop
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();


			if (currentState == GameState.PLAYING) {
				// update the frame counter
				lastFpsTime += delta;
				fps++;

				// update our FPS counter if a second has passed since
				// we last recorded
				if (lastFpsTime >= 1000) {
					container.setTitle(windowTitle + " (FPS: " + fps + ")");
					lastFpsTime = 0;
					fps = 0;
				}

				// cycle round asking each entity to move itself
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);

						entity.move(delta);
					}
				}

				// brute force collisions, compare every entity against
				// every other entity. If any of them collide notify
				// both entities that the collision has occured
				for (int p = 0; p < entities.size(); p++) {
					for (int s = p + 1; s < entities.size(); s++) {
						Entity me = (Entity) entities.get(p);
						Entity him = (Entity) entities.get(s);

						if (me.collidesWith(him)) {
							me.collidedWith(him);
							him.collidedWith(me);
						}
					}
				}

				// remove any entity that has been marked for clear up
				entities.removeAll(removeList);
				removeList.clear();

				// if a game event has indicated that game logic should
				// be resolved, cycle round every entity requesting that
				// their personal logic should be considered.
				if (logicRequiredThisLoop) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);
						entity.doLogic();
					}

					logicRequiredThisLoop = false;
				}

				gamePlayPanel.repaint();

				if(ship!=null) {
					// resolve the movement of the ship. First assume the ship
					// isn't moving. If either cursor key is pressed then
					// update the movement appropraitely
					ship.setHorizontalMovement(0);

					if ((leftPressed) && (!rightPressed)) {
						ship.setHorizontalMovement(-moveSpeed);
					} else if ((rightPressed) && (!leftPressed)) {
						ship.setHorizontalMovement(moveSpeed);
					}

					// if we're pressing fire, attempt to fire
					if (firePressed) {
						tryToFire();
					}
				}
			}
			
			// we want each frame to take 10 milliseconds, to do this
			// we've recorded when we started the frame. We add 10 milliseconds
			// to this and then factor in the current time to give 
			// us our final value to wait for
			SystemTimer.sleep(10); // 루프가 너무 빨리 돌지 않도록 잠시 대기
		}
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
	private class KeyInputHandler extends KeyAdapter {
		/** The number of key presses we've had while waiting for an "any key" press */
		private int pressCount = 1;
		
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed 
		 */
		public void keyPressed(KeyEvent e) {
			if (currentState == GameState.PLAYING) {
				// if we're waiting for an "any key" typed then we don't
				// want to do anything with just a "press"
				if (waitingForKeyPress) {
					waitingForKeyPress = false; // "Press any key" 상태를 해제
					startGame();
					return; // 게임 시작 처리 후 다른 키 입력은 무시
				}


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
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released 
		 */
		public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "released"
			if (waitingForKeyPress) {
				return;
			}
			
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

		/**
		 * Notification from AWT that a key has been typed. Note that
		 * typing a key means to both press and then release it.
		 *
		 * @param e The details of the key that was typed. 
		 */
		public void keyTyped(KeyEvent e) {
			// if we hit escape, then quit the game
			if (e.getKeyChar() == 27) {
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
