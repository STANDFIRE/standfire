package capsis.lib.quest.knotviewer;

import java.awt.Color;
import java.text.NumberFormat;

import jeeb.lib.util.DefaultNumberFormat;

/**
 * QuestKnot: a diameter of a knot.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestKnotDiameter {

	private static NumberFormat nf = DefaultNumberFormat.getInstance();

	public int ringNumber;
	public double x; // mm
	public double y; // mm
	public double z; // mm
	public double diameter; // mm
	public boolean alive;

	public Color color; // optional, see getColor ()
	
	/**
	 * Constructor.
	 */
	public QuestKnotDiameter(int ringNumber, double x, double y, double z, double diameter, boolean alive) {
		this.ringNumber = ringNumber;
		this.x = x;
		this.y = y;
		this.z = z;
		this.diameter = diameter;
		this.alive = alive;
	}

	/**
	 * Optional, to set a specific color on the diameter.
	 */
	public void setColor (Color c) {
		color = c;
	}
	
	public Color getColor () {
		if (color != null) {
			return color;
		} else {
			return alive ? Color.GREEN : Color.RED;
		}
	}
	
	public String toString() {
		return "QuestKnotDiameter z: " + nf.format(z) + " diameter: " + nf.format(diameter) + " alive: " + alive;
	}
}
