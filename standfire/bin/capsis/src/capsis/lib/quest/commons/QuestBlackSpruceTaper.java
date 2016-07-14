package capsis.lib.quest.commons;

import nz1.model.NZ1Settings;
import nz1.model.NZ1Tree;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**
 * A taper equation for Black spruce by Sharma & Zhang 2004.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public class QuestBlackSpruceTaper implements QuestTaper {

	/**
	 * This method returns the radius of a cross section at any height along the
	 * tree bole.
	 * 
	 * @param t
	 *            the Tree instance that serves as subject
	 * @param h
	 *            the height of the cross section
	 * @param overBark
	 *            true to obtain the overbark radius or false otherwise
	 * @return the radius of the cross section (cm)
	 */
	public double getTreeRadius_cm(Tree t, double h, boolean overBark) {
		return getTreeRadius_cm(t.getDbh(), t.getHeight(), h, overBark);
	}

	public double getTreeRadius_cm(double treeDbh, double treeHeight, double h, boolean overBark) {

		// fc+ed-18.3.2015 this taper equation does not accept h = 0 (would return Infinity)
		if (h == 0) h = 0.01;
		
//		System.out.println("QuestBlackSpruceTaper: getTreeRadius_cm() treeDbh: " + treeDbh + " treeHeight: " + treeHeight + " h: " + h + " overBark: "
//				+ overBark);

		double d0 = 0.9256;
		double d1 = 2.1177;
		double d2 = -0.5137;
		double d3 = 0.8377;

		double D = treeDbh;
		double hD = 1.3;
		double H = treeHeight;
		double z = h / H;

		double d = D * Math.sqrt(d0 * Math.pow(h / hD, 2 - (d1 + d2 * z + d3 * z * z)) * ((H - h) / (H - hD)));

//		System.out.println("  -> returned " + (d / 2d));

		return d / 2d;

	}

	/**
	 * A test method.
	 */
	public static void main(String[] args) {

		int id = 1;
		GScene stand = null;
		double height = 20;
		double dbh = 25;
		double number = 1;
		NZ1Settings nz1Settings = null;
		NZ1Tree t = new NZ1Tree(id, stand, height, dbh, number, nz1Settings);

		QuestBlackSpruceTaper taper = new QuestBlackSpruceTaper();

		System.out.println("QuestBlackSpruceTaper...");
		System.out.println("tree dbh: " + dbh + " height: " + height);
		System.out.println("h: 1   d: " + taper.getTreeRadius_cm(t, 1, true) * 2);
		System.out.println("h: 1.3 d: " + taper.getTreeRadius_cm(t, 1.3, true) * 2);
		System.out.println("h: 10  d: " + taper.getTreeRadius_cm(t, 10, true) * 2);
		System.out.println("h: 18  d: " + taper.getTreeRadius_cm(t, 18, true) * 2);
		System.out.println("h: 20  d: " + taper.getTreeRadius_cm(t, 20, true) * 2);

	}

}
