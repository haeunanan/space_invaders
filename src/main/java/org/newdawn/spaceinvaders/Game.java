package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	/** The list of all the entities that exist in our game */
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	/** The list of entities that need to be removed from the game this loop */
	private ArrayList<Entity> removeList = new ArrayList<Entity>();
	/** The entity representing the player */
	private Entity ship;
	/** The speed at which the player's ship should move (pixels/sec) */
	private double moveSpeed = 300;
	/** The time at which last fired a shot */
	private long lastFire = 0;
	/** The interval between our players shot (ms) */
	private long firingInterval = 500;
	/** The number of missiles fired per shot */
	private int missileCount = 1;
	/** The number of coins player has to spend in shop */
	private int coins = 0;
	/** Whether the shop UI is currently open */
	private boolean shopOpen = false;
	/** Cost for each upgrade (same price) */
	private final int UPGRADE_COST = 10;
	/** Max times an individual upgrade can be purchased */
	private final int MAX_UPGRADES = 6;
	/** Levels for each upgrade */
	private int attackLevel = 0;
	private int moveLevel = 0;
	private int missileLevel = 0;
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
		
		// 윈도우 창 닫는 리스너 
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// add a key input system (defined below) to our canvas so we can respond to key pressed
		addKeyListener(new KeyInputHandler());

		// add mouse listener for shop clicks
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
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
		
		// request the focus so key events come to us
		requestFocus();

		// create the buffering strategy which will allow AWT to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		// initialise the entities in our game so there's something
		// to see at startup
		initEntities();
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
		waitingForKeyPress = true; //죽었을땐 코인 ㄴㄴ 다시시작해야대
	}
	
	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
		message = "Well done! You Win!";
		waitingForKeyPress = true;
		coins += 10;// 라운드 클리어 > 코인 
		
	}

	/** Purchase attack speed upgrade: reduce firing interval by 10% */
	private void purchaseAttackSpeed() {
		if (attackLevel >= MAX_UPGRADES) return;
		if (coins < UPGRADE_COST) return;
		coins -= UPGRADE_COST;
		attackLevel++;
		// reduce interval but clamp to a minimum
		firingInterval = Math.max(100, (long)(firingInterval * 0.9));
	}

	/** Purchase move speed upgrade: increase move speed by 10% */
	private void purchaseMoveSpeed() {
		if (moveLevel >= MAX_UPGRADES) return;
		if (coins < UPGRADE_COST) return;
		coins -= UPGRADE_COST;
		moveLevel++;
		moveSpeed = moveSpeed * 1.1;
	}

	/** Purchase missile count upgrade: increment missiles by 1 up to a reasonable cap */
	private void purchaseMissileCount() {
		if (missileLevel >= MAX_UPGRADES) return;
		if (coins < UPGRADE_COST) return;
		coins -= UPGRADE_COST;
		missileLevel++;
		missileCount = Math.min(5, missileCount + 1);
	}

	/** Helper to draw wrapped text within a max width. */
	private void drawWrappedString(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
		if (text == null || text.length() == 0) return;
		java.awt.FontMetrics fm = g.getFontMetrics();
		String[] words = text.split(" ");
		StringBuilder line = new StringBuilder();
		int curY = y;
		for (int i=0;i<words.length;i++) {
			String word = words[i];
			String test = line.length() == 0 ? word : line + " " + word;
			int width = fm.stringWidth(test);
			if (width > maxWidth) {
				// draw current line and start new
				g.drawString(line.toString(), x, curY);
				curY += lineHeight;
				line = new StringBuilder(word);
			} else {
				if (line.length() > 0) line.append(" ");
				line.append(word);
			}
		}
		if (line.length() > 0) {
			g.drawString(line.toString(), x, curY);
		}
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
        
		// if we waited long enough, create shot(s) according to missileCount, and record the time.
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

			// update the frame counter
			lastFpsTime += delta;
			fps++;
			
			// update our FPS counter if a second has passed since
			// we last recorded
			if (lastFpsTime >= 1000) {
				container.setTitle(windowTitle+" (FPS: "+fps+")");
				lastFpsTime = 0;
				fps = 0;
			}
			
			// Get hold of a graphics context for the accelerated 
			// surface and blank it out
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.black);
			g.fillRect(0,0,800,600);
			
			// cycle round asking each entity to move itself
			if (!waitingForKeyPress) {
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					
					entity.move(delta);
				}
			}
			
			// cycle round drawing all the entities we have in the game
			for (int i=0;i<entities.size();i++) {
				Entity entity = (Entity) entities.get(i);
				
				entity.draw(g);
			}
			
			// brute force collisions, compare every entity against
			// every other entity. If any of them collide notify 
			// both entities that the collision has occured
			for (int p=0;p<entities.size();p++) {
				for (int s=p+1;s<entities.size();s++) {
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
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					entity.doLogic();
				}
				
				logicRequiredThisLoop = false;
			}
			
			// if we're waiting for an "any key" press then draw the 
			// current message 
			if (waitingForKeyPress) {
				g.setColor(Color.white);
				g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
				g.drawString("Press any key",(800-g.getFontMetrics().stringWidth("Press any key"))/2,300);
			}

			// 코인뱃지 왼쪽상단 
			int badgeX = 20;
			int badgeY = 20;
			int badgeW = 120;
			int badgeH = 40;
			g.setColor(new Color(255,165,0)); // orange
			g.fillRoundRect(badgeX,badgeY,badgeW,badgeH,20,20);
			// 코인 아이콘
			g.setColor(new Color(255,215,0)); // gold
			g.fillOval(badgeX+6,badgeY+6,28,28);
			g.setColor(Color.black);
			g.drawString("★", badgeX+12, badgeY+28);
			g.setColor(Color.black);
			g.drawString(String.valueOf(coins), badgeX+46, badgeY+26);

			// 오른쪽 상단 샵버튼
			g.setColor(Color.gray);
			g.fillRect(720,10,60,30);
			g.setColor(Color.white);
			g.drawString("Shop",732,30);

			// if 샵 오픈 >> overlay with three panels 표시
			if (shopOpen) {
				int overlayX = 40;
				int overlayY = 40;
				int overlayW = 720;
				int overlayH = 520;
				// dark rounded background
				g.setColor(new Color(40,42,45));
				g.fillRoundRect(overlayX,overlayY,overlayW,overlayH,10,10);

				// top coin badge inside overlay (mirror) optional - skip

				// 3개의 light panel
				int pad = 20;
				int panelW = (overlayW - pad*4)/3;
				int panelH = overlayH - 120;
				int panelY = overlayY + 60;
				g.setColor(new Color(220,220,220));
				g.setFont(g.getFont().deriveFont(14f));
				String[] titles = {"공격 속도 증가","이동 속도 증가","미사일 개수 증가"};
				String[] desc = {
					"공격 속도가 증가합니다",
					"플레이어의 이동속도가 증가합니다",
					"한 번에 발사할 수 있는 미사일의 개수가 1개 추가됩니다"
				};
				for (int i=0;i<3;i++) {
					int px = overlayX + pad + i*(panelW + pad);
					int py = panelY;
					// determine if this upgrade is maxed or affordable
					boolean maxed = false;
					int level = 0;
					if (i==0) { level = attackLevel; maxed = attackLevel>=MAX_UPGRADES; }
					if (i==1) { level = moveLevel; maxed = moveLevel>=MAX_UPGRADES; }
					if (i==2) { level = missileLevel; maxed = missileLevel>=MAX_UPGRADES; }

					g.fillRect(px,py,panelW,panelH);
					// draw wrapped title and description
					g.setColor(Color.black);
					int textX = px + 12;
					int textY = py + 20;
					int innerWidth = panelW - 24;
					g.setFont(g.getFont().deriveFont(16f));
					drawWrappedString(g, titles[i], textX, textY, innerWidth, 20);
					g.setFont(g.getFont().deriveFont(12f));
					drawWrappedString(g, desc[i], textX, textY + 36, innerWidth, 16);
					// price and level display
					String levelText = "Lv "+level;
					String priceText = maxed ? "MAX" : ("Price: "+UPGRADE_COST);
					// color price red if not enough coins
					if (!maxed && coins < UPGRADE_COST) g.setColor(Color.red);
					else g.setColor(Color.darkGray);
					g.setFont(g.getFont().deriveFont(12f));
					drawWrappedString(g, priceText, px+12, py+panelH-40, innerWidth, 14);
					g.drawString(levelText, px+panelW-60, py+panelH-20);
					g.setColor(new Color(220,220,220));
				}
				// 왼쪽 상단에 코인 개수 표시 
				g.setColor(Color.white);
				g.drawString("Coins: "+coins, overlayX+20, overlayY+30);
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
			
			// fire를 누르면 fire를 시도 
			if (firePressed) {
				tryToFire();
			}
			
			
			// to this and then factor in the current time to give  us our final value to wait for 
			SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
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
