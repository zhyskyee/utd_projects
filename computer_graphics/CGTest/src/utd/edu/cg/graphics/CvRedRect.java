package utd.edu.cg.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date May 25, 2018 10:47:44 AM
* 
***********************************************/
public class CvRedRect extends Canvas{
	public void paint(Graphics g) {
		Dimension dimension = getSize();
		int maxX = dimension.width - 1;
		int maxY = dimension.height - 1;
		g.drawString("d.width="+dimension.width, 10, 30);
		g.drawString("d.height="+dimension.height, 10, 60);
		
		g.setColor(Color.RED);
		
		g.drawLine(0, 0, maxX, 0);
		Component c = getComponentAt(maxX, maxY);
		
		g.drawLine(maxX, 0, maxX, maxY);
		g.drawLine(maxX, maxY, 0, maxY);
		g.drawLine(0, maxY, 0, 0);
		
		g.setXORMode(Color.BLACK);
		g.fillRect(10, 10, 50, 50);
		g.fillRect(20, 30, 50, 50);
		g.setPaintMode();
		
	}
}
