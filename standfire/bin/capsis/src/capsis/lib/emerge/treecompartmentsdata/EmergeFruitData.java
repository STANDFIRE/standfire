package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

/* convenience class for data concerning tree fruit */

public class EmergeFruitData extends EmergeCompartmentData {

	public EmergeFruitData(short treeAge, int treeId, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients) {
		super(treeAge, treeId, null, null, alive, thinAge, biomass, nutrients);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		EmergeFruitData clone =new EmergeFruitData(treeAge, treeId, alive, thinAge, biomass, new HashMap<String, Double>());
		for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
			String nutName = (String) it.next();
			clone.nutrients.put(nutName, nutrients.get(nutName));
		}
		return clone;
	}
}
