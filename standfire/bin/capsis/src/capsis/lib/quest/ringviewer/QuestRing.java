package capsis.lib.quest.ringviewer;

import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex2d;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;

/**
 * A ring in a Disc in a Piece for the Quest library.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public class QuestRing extends GPieceRing {

	public double radius_mm;
	public double width_mm;
	public int cambialAge;
	public Vertex2d v1;
	public Vertex2d v2;
	public Vertex2d v3;
	public Vertex2d v4;
	public Vertex2d center;
//	public double width; //

	/**
	 * Constructor.
	 */
	public QuestRing(int id, int discId, double radius_mm, double width_mm, int cambialAge, Vertex2d v1, Vertex2d v2, Vertex2d v3, Vertex2d v4) {
		super(id, discId, 0, 0, radius_mm); // centreX_mm, centreZ_mm = 0

		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;

		double cx = (v1.x + v2.x + v3.x + v4.x) / 4d; 
		double cy = (v1.y + v2.y + v3.y + v4.y) / 4d; 
		center = new Vertex2d (cx, cy);
		
		this.radius_mm = radius_mm;
		this.width_mm = width_mm;
		this.cambialAge = cambialAge;
	}

	public double getCenterAltitude() {
		return center.y;
	}

	public static double getDiscRadius_mm(GPieceDisc disc) {
		try {
			Collection<GPieceRing> rings = disc.getRings();

			if (rings == null || rings.isEmpty()) {
				return 0;
			}

			double maxR_mm = 0;
			for (GPieceRing ring : rings) {
				QuestRing qRing = (QuestRing) ring;
				if (qRing.radius_mm > maxR_mm) {
					maxR_mm = qRing.radius_mm;
				}
			}

			return maxR_mm;
		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRing.getDiscRadius_mm ()", "Could not find the radius of this Disc", e);
			return 0;
		}
	}


	/**
	 * A representation of the ring in a String to check its structure.
	 */
	public String toString () {
		StringBuffer b = new StringBuffer ("  GPieceRing: "+getRingId()+" radius_mm: "+getMeanRadius_mm()+" cambialAge: "+cambialAge);
		return b.toString ();
		
	}

	
}
