package capsis.lib.quest.commons;

import capsis.defaulttype.Tree;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**
 * QuestTaper: computes a stem taper.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public interface QuestTaper extends TreeRadius_cmProvider {

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
	public double getTreeRadius_cm(Tree t, double h, boolean overBark);

	/**
	 * This method returns the radius of a cross section at any height along the
	 * tree bole.
	 * @param treeDbh cm
	 * @param treeHeight m
	 * @param h m
	 * @param overBark true/false
	 */
	public double getTreeRadius_cm(double treeDbh, double treeHeight, double h, boolean overBark);

	
}
