package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

import capsis.lib.emerge.EmergeTreeSubComponent;

/* convenience class for data concerning tree stump */

public class EmergeStumpData extends EmergeCompartmentData {

	public EmergeStumpData(short treeAge, int treeId, EmergeTreeSubComponent subComponent, boolean alive,Short thinAge,  double biomass, HashMap<String, Double> nutrients) {
		super(treeAge, treeId, null, subComponent, alive,thinAge, biomass, nutrients);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		EmergeStumpData clone = new EmergeStumpData(treeAge, treeId, subComponent, alive, thinAge, biomass, new HashMap<String, Double>());
		for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
			String nutName = (String) it.next();
			clone.nutrients.put(nutName, nutrients.get(nutName));
		}
		return clone;
	}
}
