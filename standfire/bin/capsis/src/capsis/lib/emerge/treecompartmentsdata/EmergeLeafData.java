package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

/* convenience class for data concerning tree leaf */

public class EmergeLeafData extends EmergeCompartmentData {

	public Short cohortAge;
	public Byte crownHeigthRangeLowerBound;
	public Byte crownHeigthRangeUpperBound;

	public EmergeLeafData( short treeAge, int treeId, Short componentId, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients,
			Short cohortAge, Byte crownHeigthRangeLowerBound, Byte crownHeigthRangeUpperBound) {
		super( treeAge, treeId, componentId, null, alive,  thinAge, biomass, nutrients);
		this.cohortAge = cohortAge;
		this.crownHeigthRangeLowerBound = crownHeigthRangeLowerBound;
		this.crownHeigthRangeUpperBound = crownHeigthRangeUpperBound;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		EmergeLeafData clone = new EmergeLeafData( treeAge, treeId, componentId, alive,  thinAge, biomass, new HashMap<String, Double>(), cohortAge, crownHeigthRangeLowerBound, crownHeigthRangeUpperBound);
		for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
			String nutName = (String) it.next();
			clone.nutrients.put(nutName, nutrients.get(nutName));
		}
		return clone;
	}
}
