package capsis.lib.quest.knotviewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestKnot: a knot in a stem.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestKnot {

	public String id;
//	public int gu; // growth unit
	public double zr; // relative height within the growth unit [0,1]
	public double z0_mm;
	public double azimut; // deg
	private List<QuestKnotDiameter> diameters;

	/**
	 * Constructor.
	 */
	public QuestKnot(String id, /* int gu, */ double zr, double z0_mm, double azimut) {
		this.id = id;
//		this.gu = gu;
		this.zr = zr;
		this.z0_mm = z0_mm;
		this.azimut = azimut;
		
		diameters = new ArrayList<>();
		boolean alive = true; // first diameter is always alive
		int ringNumber = 0;
		
		QuestKnotDiameter d0 = new QuestKnotDiameter(ringNumber, 0, 0, z0_mm, 0, alive);
		d0.setColor(Color.BLACK);
		addDiameter(d0);
	}

	public void addDiameter(QuestKnotDiameter d) {
		diameters.add(d);
	}

	public List<QuestKnotDiameter> getDiameters() {
		return diameters;
	}

	public String toString() {
		return "Knot " + id /* + " gu: " + gu */ + " zr: " + zr + " z0_mm: " + z0_mm + " azimut: " + azimut + " #diameters: "
				+ diameters.size();
	}
}
