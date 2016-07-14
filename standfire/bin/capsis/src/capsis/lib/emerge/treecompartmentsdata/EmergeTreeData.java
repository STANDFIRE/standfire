package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;

import capsis.lib.emerge.EmergeTreeSubComponent;

/* convenience class for data concerning tree */
public class EmergeTreeData extends EmergeCompartmentData {

    public Boolean commercialWood;

    public EmergeTreeData(short treeAge, int treeId, EmergeTreeSubComponent subComponent, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients, Boolean commercialWood) {
        super(treeAge, treeId, null, subComponent, alive, thinAge, biomass, nutrients);
        this.commercialWood = commercialWood;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EmergeTreeData clone = new EmergeTreeData(treeAge, treeId, subComponent, alive, thinAge, biomass, new HashMap<String, Double>(), commercialWood);
        for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
            String nutName = (String) it.next();
            clone.nutrients.put(nutName, nutrients.get(nutName));
        }
        return clone;
    }
}
