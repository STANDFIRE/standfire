package capsis.extension.treelogger.geolog;

import java.util.Collection;

import repicea.simulation.treelogger.LoggableTree;
import capsis.kernel.Step;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**
 * This interface serves to check whether or not the tree can be processed by GeoLog tree logger.
 * @author Mathieu Fortin - November 2011
 */
public interface GeoLogLoggableTree extends LoggableTree {
	
	/**
	 * This method returns a GeoLogTreeData instance that describes the tree history as well as
	 * the different profiles in the tree (juvenile wood, heartwood, lowest dead branch, etc...)
	 * @param stepsFromRoot a Collection of Step instances that goes from the root to the current step
	 * @param mp a TreeRadius_cmProvider instance
	 * @param isCrownExpansionFactorEnabled a boolean (true to enable the crown expansion factor DEFAULT value)
	 * @return a GeoLogTreeData instance
	 */
	public GeoLogTreeData getTreeData(Collection<Step> stepsFromRoot, TreeRadius_cmProvider mp, boolean isCrownExpansionFactorEnabled);
	

}
