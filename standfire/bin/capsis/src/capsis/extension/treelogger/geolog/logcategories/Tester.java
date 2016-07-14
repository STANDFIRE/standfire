package capsis.extension.treelogger.geolog.logcategories;

import capsis.extension.treelogger.geolog.GeoLogTreeData;

/**
 * This abstract class implements a method that checks if the length of the log is valid.
 * @author F. Mothe - January 2006
 * @author Mathieu Fortin - November 2011 (refactoring)
 */
public abstract class Tester {
	
	private GeoLogTreeData td;
	private double botHeight_m;

	/**
	 * Protected constructor for derived classes.
	 * @param td a GeoLogTreeData instance
	 * @param botHeight_m the bottom height of the log to be tested (m)
	 */
	protected Tester (GeoLogTreeData td, double botHeight_m) {
		this.td = td;
		this.botHeight_m = botHeight_m;
	}
	
	/**
	 * This method returns the GeoLogTreeData instance of this tester.
	 * @return a GeoLogTreeData instance
	 */
	protected GeoLogTreeData getTreeData() {return td;}
	
	/**
	 * This method returns the bottom height of the log to be tested.
	 * @return the bottom height (m)
	 */
	protected double getBottomHeight() {return botHeight_m;}
	
	public abstract boolean isValid(double length_m);

}
