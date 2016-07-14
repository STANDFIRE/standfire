package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

import capsis.lib.emerge.EmergeTreeSubComponent;

/*Abstract class for tree compartments
 * 
 * */

public abstract class EmergeCompartmentData {
	public short treeAge;
	public int treeId;
	public Short componentId;
	public EmergeTreeSubComponent subComponent;
	public Short thinAge;
	public boolean alive;
	public double biomass;
	public HashMap<String, Double> nutrients;

	EmergeCompartmentData(short treeAge, int treeId, Short componentId, EmergeTreeSubComponent subComponent, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients) {
		this.treeAge = treeAge;
		this.treeId = treeId;
		this.componentId = componentId;
		this.subComponent = subComponent;
		this.alive = alive;
		this.thinAge = thinAge;
		this.biomass = biomass;
		this.nutrients = nutrients;
	}

	public boolean isZeroMass() {
		if (biomass > 0)
			return false;
		if (nutrients != null) {
			for (Iterator it = nutrients.values().iterator(); it.hasNext();)
				if ((Double) it.next() > 0)
					return false;
		}
		return true;
	}

	public boolean hasNegMass() {
		if (biomass < 0d)
			return true;
		if (nutrients != null) {
			for (Iterator it = nutrients.values().iterator(); it.hasNext();)
				if ((Double) it.next() < 0d)
					return true;
		}
		return false;
	}

	/***
	 * set to 0 masses values below 0
	 */
	public void correctMasses() {
		if (biomass < 0d)
			biomass = 0d;
		if (nutrients != null) {
			for (Iterator it = nutrients.values().iterator(); it.hasNext();) {
				Double value = (Double) it.next();
				if (value < 0d)
					value = 0d;
			}
		}

	}
	
}
