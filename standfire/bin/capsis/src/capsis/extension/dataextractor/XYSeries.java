package capsis.extension.dataextractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.Vertex2d;

/**
 * A list of points with a name.
 * 
 * @author F. de Coligny - September 2015
 */
public class XYSeries {

	private String name;
	private Color color;
	private List<Vertex2d> points;

	/**
	 * Constructor
	 */
	public XYSeries (String name, Color color) {
		this.name = name;
		this.color = color;
		points = new ArrayList<> ();
	}
	
	public void addPoint (double x, double y) {
		points.add (new Vertex2d (x, y));
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public List<Vertex2d> getPoints () {
		return points;
	}

	public int size () {
		return points == null ? 0 : points.size ();
	}
	
	public String toString () {
		StringBuffer b = new StringBuffer ("XYSeries name: "+name+" #points: "+points.size ());
		for (Vertex2d p : points) {
			b.append(" ");
			b.append(p);
		}
		return b.toString ();
	}

}
