package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.newdawn.spaceinvaders.entity.*;
import org.newdawn.spaceinvaders.entity.AlienShotEntity;

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
    private int score = 0;
    private RankingManager rankingManager;
    private int currentLevel = 1; // ADDED: 현재 레벨을 추적하는 변수
    private static final int BOSS_LEVEL = 5; // ADDED: 보스가 나타날 레벨 정의
	
	/** The message to display which waiting for a key press */
	private String message = "";
	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = false;
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
		// create a frame to contain our game
		container = new JFrame("Space Invaders 102");
		
		// get hold the content of the frame and set up the resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);
		
		// setup our canvas size and put it into the content of the frame
		setBounds(0,0,800,600);
		panel.add(this);
		
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

        rankingManager = new RankingManager();
        score = 0;

        // initialise the entities in our game so there's something
		// to see at startup
        startGame();
    }
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
    private void startGame() {
        // 1. 모든 기존 엔티티(우주선, 외계인, 총알)를 깨끗하게 제거합니다.
        entities.clear();

        // 2. 점수 및 키 입력을 초기화합니다.
        leftPressed = false;
        rightPressed = false;
        firePressed = false;

        // 3. 새로운 엔티티들을 생성하고 배치합니다.
        initEntities();
    }
	
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
    private void initEntities() {
        // 1. 플레이어 우주선을 생성하고 entities 리스트에 추가합니다.
        // 이 부분이 누락되면 우주선이 나타나지 않습니다.
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        // 2. 외계인 수를 초기화합니다.
        alienCount = 0;

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
        message = "Oh no! They got you...";
        waitingForKeyPress = true;

        // ADDED: 랭킹 확인 및 저장 로직
        if (rankingManager.isHighScore(score)) {
            String name = JOptionPane.showInputDialog(container, "New High Score! Enter your name:", "Ranking", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                rankingManager.addScore(score, name);
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
    public void notifyBossKilled() {
        notifyWin();
        score += 5000;// 보스를 이기면 게임에서 승리
    }
	
	/**
	 * Notification that an alien has been killed
	 */
    public void notifyAlienKilled() {
        alienCount--;
        score += 100;

        if (alienCount == 0) {
            notifyWin(); // 일반 스테이지 클리어
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

        while (gameRunning) {
            // 1. 프레임 시간 계산
            long delta = SystemTimer.getTime() - lastLoopTime;
            lastLoopTime = SystemTimer.getTime();

            // FPS 카운터 업데이트
            lastFpsTime += delta;
            fps++;
            if (lastFpsTime >= 1000) {
                container.setTitle(windowTitle + " (FPS: " + fps + ")");
                lastFpsTime = 0;
                fps = 0;
            }

            // 2. 그리기 준비: 그래픽 객체를 얻고 화면을 검은색으로 지웁니다.
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600);

            // 3. 게임 플레이 로직 (일시정지 상태가 아닐 때)
            if (!waitingForKeyPress) {
                // 모든 엔티티를 움직입니다.
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.move(delta);
                }

                // 모든 엔티티를 화면에 그립니다.
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.draw(g);
                }

                // 모든 엔티티 간의 충돌을 확인합니다.
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
            }

            // 4. 제거할 엔티티들을 목록에서 삭제합니다.
            entities.removeAll(removeList);
            removeList.clear();

            // 5. 특별한 로직이 필요한 엔티티들의 로직을 실행합니다. (외계인 방향 전환 등)
            if (logicRequiredThisLoop) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.doLogic();
                }
                logicRequiredThisLoop = false;
            }

            // 6. UI(점수 등)를 항상 위에 그립니다.
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Score: " + score, 10, 20);

            // 7. 게임 오버/랭킹 화면 로직 (일시정지 상태일 때)
            if (waitingForKeyPress) {
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 150);

                g.setFont(new Font("Arial", Font.BOLD, 20));
                g.drawString("--- RANKING ---", (800 - g.getFontMetrics().stringWidth("--- RANKING ---")) / 2, 220);

                List<ScoreEntry> topScores = rankingManager.getScores();
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                for (int i = 0; i < topScores.size(); i++) {
                    ScoreEntry entry = topScores.get(i);
                    String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
                    g.drawString(rankString, 280, 260 + i * 25);
                }

                g.setFont(new Font("Arial", Font.BOLD, 20));
                g.drawString("Press any key to continue", (800 - g.getFontMetrics().stringWidth("Press any key to continue")) / 2, 550);
            }

            // 8. 모든 그리기가 끝난 후, 최종적으로 화면에 보여주고 리소스를 정리합니다.
            g.dispose();
            strategy.show();

            // 9. 플레이어 입력 처리 (일시정지 상태가 아닐 때)
            if (!waitingForKeyPress) {
                ship.setHorizontalMovement(0);
                if ((leftPressed) && (!rightPressed)) {
                    ship.setHorizontalMovement(-moveSpeed);
                } else if ((rightPressed) && (!leftPressed)) {
                    ship.setHorizontalMovement(moveSpeed);
                }

                if (firePressed) {
                    tryToFire();
                }
            }

            // 10. 프레임 속도 조절
            SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
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
