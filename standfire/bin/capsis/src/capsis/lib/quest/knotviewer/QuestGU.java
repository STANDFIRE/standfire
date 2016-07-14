package capsis.lib.quest.knotviewer;

import java.util.ArrayList;
import java.util.List;

/**
 * QuestGU: a growth unit in a stem.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestGU {

	public int id;
	public double z0_m; // m
	public double length_m; // m
	public int n; // number of knots
	public double treeDbh_cm; // cm
	public double treeHeight_m; // m
	private List<QuestKnot> knots;

	/**
	 * Constructor.
	 */
	public QuestGU(int id, double z0_m, double length_m, int n, double treeDbh_cm, double treeHeight_m) {
		this.id = id;
		this.z0_m = z0_m;
		this.length_m = length_m;
		this.n = n;
		this.treeDbh_cm = treeDbh_cm;
		this.treeHeight_m = treeHeight_m;
		
		knots = new ArrayList<>();
	}

	public void addKnot(QuestKnot k) {
		knots.add(k);
	}

	public List<QuestKnot> getKnots() {
		return knots;
	}

	public String toString() {
		return "GU " + id + " z0_m: " + z0_m + " length_m: " + length_m + " n: " + n + " treeDbh_cm: " + treeDbh_cm + " treeHeight_m: "
				+ treeHeight_m + " #knots: "+knots.size ();
	}
}

