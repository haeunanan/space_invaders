package org.newdawn.spaceinvaders;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
public class Game extends Canvas 
{
	public enum GameState {
		START_MENU, // 시작 메뉴
		LOGIN,      // 로그인 화면
		PLAYING     // 게임 중
	}
	private GameState currentState;

	private StartMenuPanel startMenuPanel;
	// private LoginPanel loginPanel;
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
	
	/**
	 * Construct our game and set it running.
	 */
	public Game() {

		currentState = GameState.START_MENU;

		FirebaseInitializer firebase = new FirebaseInitializer();
		firebase.initialize();

		// create a frame to contain our game
		container = new JFrame("Space Invaders 102");
		
		// get hold the content of the frame and set up the resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(new BorderLayout());

		startMenuPanel = new StartMenuPanel(this);
		panel.add(startMenuPanel, BorderLayout.CENTER);

		// loginPanel = new LoginPanel(this); // 나중에 만들 로그인 패널
		// panel.add(loginPanel, BorderLayout.CENTER);

		changeState(GameState.START_MENU);
		// setup our canvas size and put it into the content of the frame
		//setBounds(0,0,800,600);
		//panel.add(this);
		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		setIgnoreRepaint(true);
		
		// finally make the window visible 
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		// add a listener to respond to the user closing the window. If they
		// do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		addKeyListener(new KeyInputHandler());
		
		// request the focus so key events come to us
		requestFocus();

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		// initialise the entities in our game so there's something
		// to see at startup
		initEntities();
	}

	// Game.java에 아래 메소드 추가

	public void changeState(GameState newState) {
		currentState = newState;

		// 일단 모든 패널을 숨깁니다.
		if (startMenuPanel != null) startMenuPanel.setVisible(false);
		// if (loginPanel != null) loginPanel.setVisible(false);

		// 현재 상태에 맞는 패널만 보여줍니다.
		switch (currentState) {
			case START_MENU:
				if (startMenuPanel != null) startMenuPanel.setVisible(true);
				break;
			case LOGIN:
				// if (loginPanel != null) loginPanel.setVisible(true);
				break;
			case PLAYING:
				// 게임 플레이 상태가 되면 모든 UI 패널을 숨기고,
				// 게임 로직이 직접 화면을 그리기 시작합니다.
				startGame(); // 게임 판 초기화
				requestFocus(); // Game Canvas가 키 입력을 받을 수 있도록 포커스 요청
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
		ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",ship.getX()+10,ship.getY()-30);
		entities.add(shot);
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

				// Get hold of a graphics context for the accelerated
				// surface and blank it out
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.black);
				g.fillRect(0, 0, 800, 600);

				// cycle round asking each entity to move itself
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);

						entity.move(delta);
					}
				}

				// cycle round drawing all the entities we have in the game
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);

					entity.draw(g);
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

				// if we're waiting for an "any key" press then draw the
				// current message
				if (waitingForKeyPress) {
					g.setColor(Color.white);
					g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 250);
					g.drawString("Press any key", (800 - g.getFontMetrics().stringWidth("Press any key")) / 2, 300);
				}

				// finally, we've completed drawing so clear up the graphics
				// and flip the buffer over
				g.dispose();
				strategy.show();

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
			
			// we want each frame to take 10 milliseconds, to do this
			// we've recorded when we started the frame. We add 10 milliseconds
			// to this and then factor in the current time to give 
			// us our final value to wait for
			SystemTimer.sleep(10); // 루프가 너무 빨리 돌지 않도록 잠시 대기
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
					return;
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

			if (e.getKeyCode() == KeyEvent.VK_L) {
				System.out.println("'L' 키가 눌렸습니다. Firebase 인증을 테스트합니다.");

				// 1. AuthService 객체를 생성합니다.
				AuthService authService = new AuthService();

				// 2. 클라이언트(게임)에서 실제로 받아와야 할 ID 토큰입니다.
				//    지금은 테스트를 위해 임시 더미 값을 넣습니다.
				String dummyToken = "여기에_클라이언트에서_받은_ID_토큰을_넣으세요";

				// 3. 토큰 검증을 요청하고 결과를 받습니다.
				String uid = authService.verifyIdToken(dummyToken);

				if (uid != null) {
					// 인증 성공 시 로직
					System.out.println("로그인 검증 성공! 게임을 시작할 수 있습니다. User ID: " + uid);
				} else {
					// 인증 실패 시 로직
					System.out.println("로그인 검증 실패! 게임을 시작할 수 없습니다.");
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
			// if we're waiting for a "any key" type then
			// check if we've recieved any recently. We may
			// have had a keyType() event from the user releasing
			// the shoot or move keys, hence the use of the "pressCount"
			// counter.
			if (waitingForKeyPress) {
				if (pressCount == 1) {
					// since we've now recieved our key typed
					// event we can mark it as such and start 
					// our new game
					waitingForKeyPress = false;
					startGame();
					pressCount = 0;
				} else {
					pressCount++;
				}
			}
			
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
		Game g = new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
