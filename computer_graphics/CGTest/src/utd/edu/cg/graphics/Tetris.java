package utd.edu.cg.graphics;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date May 29, 2018 12:11:20 PM
* 
***********************************************/
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

public class Tetris extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Tetris() {
		super("Tertris Beta");
		Tetrisblock a = new Tetrisblock();
		addKeyListener(a);
		setSize(400, 440);
		add(a);
	}

	public static void main(String[] args) {
		Tetris tetris = new Tetris();
		tetris.setLocationRelativeTo(null);
		// tetris.setUndecorated(true);
		tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tetris.setVisible(true);
	}
}

class Tetrisblock extends JPanel implements KeyListener, MouseInputListener, MouseWheelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Timer timer;
	private Dimension panel;
	private int blockType, nextBlockType;
	private int turnState, nextTurnState;
	private int score = 0;
	private int lines = 0;
	private int level = 1;
	private int x = 4;
	private int y = 0;
	private int i = 0;
	JButton pause, quit;
	int j = 0;
	int flag = 0;
	boolean isPaused = false;
	int blockUnitSize;
	int GapX, GapY;
	Rectangle mainAreaRect, nextShapeRect;
	// int mainAreaX, mainAreaY, mainAreaW, mainAreaH;
	// wall + 2, 1 for no indexOutOfBounds;
	int[][] map = new int[13][23];

	private final Color blockColor[] = new Color[] { Color.CYAN, Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW,
			Color.ORANGE, Color.PINK };
	// First columns is shape type:S、Z、L、J、I、O、T
	// Second column is rotated shape
	// Third columns is a 4X4 matrix to indicate which blocks are selected.
	// content equals to its color index plus 1 in blockColor, 1 saves for wall;
	private final int shapes[][][] = new int[][][] {
			// I
			{ { 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			  { 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0 },
			  { 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0 } },
			// S
			{ { 0, 3, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			  { 3, 0, 0, 0, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0 },
			  { 0, 3, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 3, 0, 0, 0, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0 } },
			// Z
			{ { 4, 4, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			  { 0, 4, 0, 0, 4, 4, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0 },
			  { 4, 4, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 0, 4, 0, 0, 4, 4, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0 } },
			// J
			{ { 0, 5, 0, 0, 0, 5, 0, 0, 5, 5, 0, 0, 0, 0, 0, 0 }, 
			  { 5, 0, 0, 0, 5, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 5, 5, 0, 0, 5, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0 },
			  { 5, 5, 5, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
			// O
			{ { 6, 6, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			  { 6, 6, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 6, 6, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 6, 6, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
			// L
			{ { 7, 0, 0, 0, 7, 0, 0, 0, 7, 7, 0, 0, 0, 0, 0, 0 }, 
			  { 7, 7, 7, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 7, 7, 0, 0, 0, 7, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0 },
			  { 0, 0, 7, 0, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
			// T
			{ { 0, 8, 0, 0, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
			  { 0, 8, 0, 0, 8, 8, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0 },
			  { 8, 8, 8, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			  { 0, 8, 0, 0, 0, 8, 8, 0, 0, 8, 0, 0, 0, 0, 0, 0 } } };

	// create a new block
	private void createNewBlock() {
		blockType = (int) (Math.random() * 1000) % 7;
		turnState = (int) (Math.random() * 1000) % 4;
	}

	// create the next block
	private void createNextBlock() {
		nextBlockType = (int) (Math.random() * 1000) % 7;
		nextTurnState = (int) (Math.random() * 1000) % 4;
	}

	// get the next block
	private void getNextBlock() {
		blockType = nextBlockType;
		turnState = nextTurnState;
		x = 4;
		y = 0;

		if (isCollided(x, y, blockType, turnState) == 0) {
			initMap();
			initWall();
			score = 0;
			lines = 0;
			level = 1;
			JOptionPane.showMessageDialog(null, "GAME OVER");
		}
	}

	// draw the wall
	private void initWall() {
		for (i = 0; i < 12; i++) {
			map[i][21] = 1;
		}
		for (j = 0; j < 22; j++) {
			map[11][j] = 1;
			map[0][j] = 1;
		}
	}

	// initialize the map
	private void initMap() {
		for (i = 0; i < 12; i++) {
			for (j = 0; j < 22; j++) {
				map[i][j] = 0;
			}
		}
	}

	// initialize the class
	public Tetrisblock() {
		createNewBlock();
		createNextBlock();
		initMap();
		initWall();

		mainAreaRect = new Rectangle();
		nextShapeRect = new Rectangle();

		pause = new JButton("PAUSE");
		quit = new JButton("QUIT");

		this.add("Center", pause);
		this.add("Center", quit);

		blockUnitSize = 20;
		GapX = 20;
		GapY = 20;

		setLayout(new BorderLayout());
		this.pause.setEnabled(true);
		this.pause.setVisible(false);
		this.pause.addKeyListener(this);
		this.pause.setFont(new Font("Arial", Font.BOLD, blockUnitSize));
		this.pause.setContentAreaFilled(false);
		this.pause.setFocusable(false);
		this.pause.setBorder(BorderFactory.createLineBorder(new Color(9, 151, 247), 2));
		this.pause.setForeground(new Color(9, 151, 247));
		this.pause.setBorderPainted(true);
		this.pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (timer.isRunning()) {
					timer.stop();
					isPaused = true;
					pause.setText("RESUME");
				} else {
					timer.start();
					isPaused = false;
					pause.setText("PAUSE");
				}
			}
		});

		this.quit.setEnabled(true);
		this.quit.setVisible(true);
		this.quit.addKeyListener(this);
		this.quit.setFont(new Font("Arial", Font.BOLD, blockUnitSize));
		this.quit.setContentAreaFilled(false);
		this.quit.setFocusable(false);
		this.quit.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		this.quit.setForeground(Color.BLACK);
		this.quit.setBorderPainted(true);
		this.quit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int input = JOptionPane.showConfirmDialog(null, "Are you sure to quit the game?", "Select an Option...",
						JOptionPane.YES_NO_OPTION);
				// 0 = yes 1 = no
				if (input == 0) {
					System.exit(0);
				}

			}
		});

		this.timer = new Timer(1000, new TimerListener());
		timer.start();
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addMouseListener(this);
	}

	private void initgr() {
		Dimension d = this.getParent().getSize();
		int blockUnitWidthSize = d.width / 19;
		int blockUnitHeightSize = d.height / 22;
		blockUnitSize = Math.min(blockUnitHeightSize, blockUnitWidthSize);
		GapX = (d.width - 19 * blockUnitSize) / 2;
		GapY = (d.height - 22 * blockUnitSize) / 2;
		int pauseBtnX = GapX + (10 * blockUnitSize) / 4 + blockUnitSize;
		int pauseBtnY = GapY + (9 * blockUnitSize);

		if (pause.getFont().getSize() != blockUnitSize) {
			this.pause.setFont(new Font("Arial", Font.BOLD, blockUnitSize));
		}
		this.pause.setBounds(pauseBtnX, pauseBtnY, 5 * blockUnitSize, 2 * blockUnitSize);

		int quitBtnX = GapX + 12 * blockUnitSize;
		int quitBtnY = GapY + 19 * blockUnitSize;
		if (quit.getFont().getSize() != blockUnitSize) {
			this.quit.setFont(new Font("Arial", Font.BOLD, blockUnitSize));
		}
		this.quit.setBounds(quitBtnX, quitBtnY, 4 * blockUnitSize, 2 * blockUnitSize);

		this.mainAreaRect.x = GapX + blockUnitSize;
		this.mainAreaRect.y = GapY + blockUnitSize;
		this.mainAreaRect.width = blockUnitSize * 10;
		this.mainAreaRect.height = blockUnitSize * 20;

		this.nextShapeRect.x = GapX + 12 * blockUnitSize;
		this.nextShapeRect.y = GapY + blockUnitSize;
		this.nextShapeRect.width = 6 * blockUnitSize;
		this.nextShapeRect.height = 6 * blockUnitSize;
	}

	// rotate
	public void rotate(boolean bClockWise) {
		int tempturnState = turnState;
		turnState = (turnState + (bClockWise ? 3 : 1)) % 4;
		if (isCollided(x, y, blockType, turnState) == 0) {
			turnState = tempturnState;
		}
		repaint();
	}

	// move to left one block
	public void left() {
		if (isCollided(x - 1, y, blockType, turnState) == 1) {
			x = x - 1;
		}

		repaint();
	}

	// move to right one block
	public void right() {
		if (isCollided(x + 1, y, blockType, turnState) == 1) {
			x = x + 1;
		}
		repaint();
	}

	// move down one block
	public void down() {
		if (isCollided(x, y + 1, blockType, turnState) == 1) {
			y = y + 1;
			deletelines();
		}

		if (isCollided(x, y + 1, blockType, turnState) == 0) {
			add(x, y, blockType, turnState);
			getNextBlock();
			createNextBlock();
			deletelines();
		}

		repaint();
	}

	// if the current block touches other blocks
	public int isCollided(int x, int y, int blockType, int turnState) {
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				if (((shapes[blockType][turnState][a * 4 + b] > 1) && (map[x + b + 1][y + a] >= 1))) {
					return 0;
				}
			}
		}
		return 1;
	}

	// delete lines as many as possible
	public void deletelines() {
		int c = 0;
		for (int b = 0; b < 22; b++) {
			for (int a = 0; a < 12; a++) {
				if (map[a][b] > 1) {
					c = c + 1;
					if (c == 10) {
						score += 10;
						lines++;
						if (score / 100 >= level) {
							level = score / 100 + 1;
							int delay = (int) (1000 * Math.pow(0.9, level - 1));
							if (delay > 100) {
								timer.setDelay(delay);
							}
							//System.out.println("level:"+level+"  delay:"+delay);
						}
						for (int d = b; d > 0; d--) {
							for (int e = 0; e < 11; e++) {
								map[e][d] = map[e][d - 1];
							}
						}
					}
				}
			}
			c = 0;
		}
	}

	// add a new block into the map
	public void add(int x, int y, int blockType, int turnState) {
		int j = 0;
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				if (map[x + b + 1][y + a] == 0) {
					map[x + b + 1][y + a] = shapes[blockType][turnState][j];
				}
				j++;
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (panel == null || !panel.equals(this.getParent().getSize())) {
			initgr();
			panel = this.getParent().getSize();
		}

		// draw the new block
		for (j = 0; j < 16; j++) {
			if (shapes[blockType][turnState][j] > 1) {
				g.setColor(blockColor[blockType]);
				g.fillRect((j % 4 + x + 1) * blockUnitSize + GapX, (j / 4 + y) * blockUnitSize + GapY, blockUnitSize,
						blockUnitSize);
				g.setColor(Color.BLACK);
				g.drawRect((j % 4 + x + 1) * blockUnitSize + GapX, (j / 4 + y) * blockUnitSize + GapY, blockUnitSize,
						blockUnitSize);
			}
		}

		// draw the next block
		for (j = 0; j < 16; j++) {
			if (shapes[nextBlockType][nextTurnState][j] > 1) {
				g.setColor(blockColor[nextBlockType]);
				g.fillRect((j % 4 + 13) * blockUnitSize + GapX, (j / 4 + 2) * blockUnitSize + GapY, blockUnitSize,
						blockUnitSize);
				g.setColor(Color.BLACK);
				g.drawRect((j % 4 + 13) * blockUnitSize + GapX, (j / 4 + 2) * blockUnitSize + GapY, blockUnitSize,
						blockUnitSize);
			}
		}
		g.drawRect(nextShapeRect.x, nextShapeRect.y, nextShapeRect.width, nextShapeRect.height);

		// draw the blocks already there
		for (j = 0; j < 22; j++) {
			for (i = 0; i < 12; i++) {
				if (map[i][j] > 1) {
					g.setColor(blockColor[map[i][j] - 2]);
					g.fillRect(i * blockUnitSize + GapX, j * blockUnitSize + GapY, blockUnitSize, blockUnitSize);
					g.setColor(Color.BLACK);
					g.drawRect(i * blockUnitSize + GapX, j * blockUnitSize + GapY, blockUnitSize, blockUnitSize);
				}
			}
		}

		g.setColor(Color.BLACK);
		g.drawRect(mainAreaRect.x, mainAreaRect.y, mainAreaRect.width, mainAreaRect.height);

		g.setFont(new Font("Arial", Font.BOLD, blockUnitSize));
		g.drawString("Level:   " + level, 12 * blockUnitSize + GapX, 9 * blockUnitSize + GapY);
		g.drawString("Lines:   " + lines, 12 * blockUnitSize + GapX, 11 * blockUnitSize + GapY);
		g.drawString("Score:   " + score, 12 * blockUnitSize + GapX, 13 * blockUnitSize + GapY);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (isPaused) {
			return;
		}

		switch (e.getKeyCode()) {
		case KeyEvent.VK_DOWN:
			down();
			break;
		case KeyEvent.VK_UP:
			rotate(true);
			break;
		case KeyEvent.VK_RIGHT:
			right();
			break;
		case KeyEvent.VK_LEFT:
			left();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	// timer listener
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			repaint();
			if (isCollided(x, y + 1, blockType, turnState) == 1) {
				y = y + 1;
				deletelines();
			}

			if (isCollided(x, y + 1, blockType, turnState) == 0) {
				if (flag == 1) {
					add(x, y, blockType, turnState);
					deletelines();
					getNextBlock();
					createNextBlock();
					flag = 0;
				}
				flag = 1;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
		int y = e.getY();

		if (x >= mainAreaRect.x && x <= mainAreaRect.x + mainAreaRect.width && y >= mainAreaRect.y
				&& y <= mainAreaRect.x + mainAreaRect.height) {
			this.pause.setVisible(true);
		} else {
			this.pause.setVisible(false);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		/*
		 * 1 ({@code BUTTON1}) left btn 
		 * 2 ({@code BUTTON2}) mid btn 
		 * 3 ({@code BUTTON3}) right btn
		 */
		if (isPaused) {
			return;
		}

		int keyType = e.getButton();
		switch (keyType) {
		case 1:
			left();
			break;
		case 2:
			down();
			break;
		case 3:
			right();
		default:
			break;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		if (isPaused) {
			return;
		}

		if (e.getWheelRotation() >= 1) {
			// backward scroll
			rotate(false);
		} else {
			// forward scroll
			rotate(true);
		}
	}
}