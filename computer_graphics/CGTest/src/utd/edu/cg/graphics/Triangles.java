package utd.edu.cg.graphics;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date May 25, 2018 12:15:39 PM
* 
***********************************************/
public class Triangles extends Frame{
	public static void main(String[] args) {
		new Triangles();
	}

	Triangles() {
		super("Click anywhere on screen to generate hexagons of appropriate radius!");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(600, 400);
		//add("Center", new CvRedRect());
		//add("Center", new CvTriangles());
		add("Center", new hexGrid());
		show();

	}
}
