package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

import capsis.lib.emerge.EmergeTreeSubComponent;

/* convenience class for data concerning tree root */

public class EmergeRootData extends EmergeCompartmentData {

	public Short diameterClassLowerBound;
	public Short diameterClassUpperBound;
	public Short soilDepthRangeLowerBound;
	public Short soilDepthRangeUpperBound;

	public EmergeRootData(short treeAge, int treeId, Short componentId, EmergeTreeSubComponent subComponent, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients,
			Short diameterClassLowerBound, Short diameterClassUpperBound, Short soilDepthRangeLowerBound, Short soilDepthRangeUpperBound) {
		super(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, nutrients);
		this.diameterClassLowerBound = diameterClassLowerBound;
		this.diameterClassUpperBound = diameterClassUpperBound;
		this.soilDepthRangeLowerBound = soilDepthRangeLowerBound;
		this.soilDepthRangeUpperBound = soilDepthRangeUpperBound;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		EmergeRootData clone =new EmergeRootData(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, new HashMap<String, Double>(), diameterClassLowerBound, diameterClassUpperBound, soilDepthRangeLowerBound, soilDepthRangeUpperBound);
		for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
			String nutName = (String) it.next();
			clone.nutrients.put(nutName, nutrients.get(nutName));
		}
		return clone;
	}
}
