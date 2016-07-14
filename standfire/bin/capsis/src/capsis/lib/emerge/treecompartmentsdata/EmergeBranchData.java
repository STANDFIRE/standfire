package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

import capsis.lib.emerge.EmergeTreeSubComponent;

/* convenience class for data concerning tree branch */
public class EmergeBranchData extends EmergeCompartmentData {

    public Short diameterClassLowerBound;
    public Short diameterClassUpperBound;
    public Byte crownHeigthRangeLowerBound;
    public Byte crownHeigthRangeUpperBound;
    public Boolean commercialWood;

    public EmergeBranchData(short treeAge, int treeId, Short componentId, EmergeTreeSubComponent subComponent, boolean alive, Short thinAge, double biomass,
            HashMap<String, Double> nutrients, Short diameterClassLowerBound, Short diameterClassUpperBound, Byte crownHeigthRangeLowerBound, Byte crownHeigthRangeUpperBound, Boolean commercialWood) {
        super(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, nutrients);
        this.diameterClassLowerBound = diameterClassLowerBound;
        this.diameterClassUpperBound = diameterClassUpperBound;
        this.crownHeigthRangeLowerBound = crownHeigthRangeLowerBound;
        this.crownHeigthRangeUpperBound = crownHeigthRangeUpperBound;
        this.commercialWood = commercialWood;
    }

    public Object clone() throws CloneNotSupportedException {
        EmergeBranchData clone = new EmergeBranchData(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, new HashMap<String, Double>(), diameterClassLowerBound,
                diameterClassUpperBound, crownHeigthRangeLowerBound, crownHeigthRangeUpperBound, commercialWood);
        for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
            String nutName = (String) it.next();
            clone.nutrients.put(nutName, nutrients.get(nutName));
        }
        return clone;
    }
}
