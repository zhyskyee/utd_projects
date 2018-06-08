package utd.edu.cg.graphics;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date May 28, 2018 1:30:55 PM
* 
***********************************************/
public class hexGrid extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new hexGrid();
	}

	public hexGrid() {
		super("Click on the panel to set up a proper radius");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(600, 400);
		setLocationRelativeTo(null);
		add("Center", new hexagons());
		setVisible(true);
		//show();
	}
}

class hexagons extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int maxX, maxY;
	float pixelSize, rWidth = 10.0F, rHeight = 10.0F, xP = -1, yP;
	FontMetrics metrics;

	public hexagons() {
		// TODO Auto-generated constructor stub
		super();
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				xP = evt.getX();
				yP = evt.getY();
				repaint();
			}
		});
	}

	void initgr() {
		Dimension d = getSize();
		maxX = d.width - 1;
		maxY = d.height - 1;
		rWidth = maxX;
		rHeight = maxY;
		pixelSize = Math.max(rWidth / maxX, rHeight / maxY);
	}

	int iX(float x) {
		return Math.round(x / pixelSize);
	}

	int iY(float y) {
		return Math.round(maxY - y / pixelSize);
	}

	float fx(int x) {
		return x * pixelSize;
	}

	float fy(int y) {
		return (maxY - y) * pixelSize;
	}

	public void paint(Graphics g) {
		initgr();

		if (xP != -1) {
			double value1 = Math.pow(xP, 2);
			double value2 = Math.pow(yP, 2);
			int radius = (int) Math.sqrt(value1 + value2);
			drawHexagons(g, radius);
		}
	}

	// Method to generate the hexagonal loop
	private void drawHexagons(Graphics g, int radius) {
		double cos30rad = Math.sqrt(3.0) * (radius) / 2.0;
		double sin30rad = radius / 2;

		int rows = (int) ((maxY - 10) / (2 * cos30rad));
		int offsetY = (int) ((maxY - rows * (2 * cos30rad)) / 2);

		int columns = (int) ((maxX - 10 - radius / 2.0) / (3.0 / 2.0 * radius));
		int offsetX = (int) ((maxX - columns * (3.0 / 2.0 * radius) - radius / 2.0) / 2);

		if (rows == 0 || columns == 0) {
			return;
		}

		if (rows == 1) {
			drawHex(g, maxX / 2, maxY / 2, radius);
			return;
		}

		int rowNum = 2 * rows + 1;
		for (int row = 1; row <= rowNum; row++) {
			int colNum;
			double startOfsX, startOfsY;

			if (row % 2 == 0) { // even
				colNum = columns / 2;
				startOfsX = offsetX + 2 * radius;
				startOfsY = offsetY + cos30rad;
			} else {
				colNum = columns - columns / 2;
				startOfsX = offsetX + sin30rad;
				startOfsY = offsetY;
			}

			for (int col = 1; col <= colNum; col++) {
				double x1 = startOfsX + 3 * radius * (col - 1);
				double y1 = startOfsY + 2 * cos30rad * (row - row / 2 - 1);
				double x2 = x1 + 2 * sin30rad;
				double y2 = y1;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
			}
		}

		for (int col = 1; col <= columns - columns / 2; col++) {
			double x1, y1, x2, y2;
			for (int row = 0; row < rows; row++) {
				x1 = offsetX + sin30rad + 3 * (col - 1) * radius;
				y1 = offsetY + 2 * cos30rad * row;
				x2 = x1 - sin30rad;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
				x1 = x2;
				y1 = y2;
				x2 = x1 + sin30rad;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));

				x1 = offsetX + sin30rad + radius + 3 * (col - 1) * radius;
				y1 = offsetY + 2 * cos30rad * row;
				x2 = x1 + sin30rad;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
				x1 = x2;
				y1 = y2;
				x2 = x1 - sin30rad;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
			}
		}

		if (columns % 2 == 0 && columns != 0) { // even number needs to fix the right-most line
			double lastX = offsetX + 3 * radius * (columns / 2);
			double x1, y1, x2, y2;
			for (int row = 0; row < rows - 1; row++) {
				x1 = lastX;
				y1 = offsetY + cos30rad + 2 * row * cos30rad;
				x2 = x1 + sin30rad;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
				x1 = x2;
				y1 = y2;
				x2 = lastX;
				y2 = y1 + cos30rad;
				g.drawLine(iX((float) x1), iY((float) y1), iX((float) x2), iY((float) y2));
			}
		}
	}

	private void drawHex(Graphics g, int x, int y, int r) {
		int[] xpoints = new int[6];
		int[] ypoints = new int[6];

		xpoints[0] = x - r / 2;
		ypoints[0] = (int) (y - Math.sqrt(3.0) * r / 2);

		xpoints[1] = x + r / 2;
		ypoints[1] = ypoints[0];

		xpoints[2] = x + r;
		ypoints[2] = y;

		xpoints[3] = xpoints[1];
		ypoints[3] = (int) (y + Math.sqrt(3.0) * r / 2);

		xpoints[4] = xpoints[0];
		ypoints[4] = ypoints[3];

		xpoints[5] = x - r;
		ypoints[5] = y;

		g.drawPolygon(new Polygon(xpoints, ypoints, 6));
	}
}
