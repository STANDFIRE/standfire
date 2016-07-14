package capsis.lib.emerge.treecompartmentsdata;

import java.util.HashMap;
import java.util.Iterator;
import capsis.lib.emerge.EmergeTreeSubComponent;

/* convenience class for data concerning tree stem */
public class EmergeStemData extends EmergeCompartmentData {

    public Short growthUnit;
    public Short ring;
    public Boolean commercialWood;

    public EmergeStemData(short treeAge, int treeId, Short componentId, EmergeTreeSubComponent subComponent, boolean alive, Short thinAge, double biomass, HashMap<String, Double> nutrients,
            Short growthUnit, Short ring, Boolean commercialWood) {
        super(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, nutrients);
        this.growthUnit = growthUnit;
        this.ring = ring;
        this.commercialWood = commercialWood;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EmergeStemData clone = new EmergeStemData(treeAge, treeId, componentId, subComponent, alive, thinAge, biomass, new HashMap<String, Double>(), growthUnit, ring,commercialWood);
        for (Iterator it = nutrients.keySet().iterator(); it.hasNext();) {
            String nutName = (String) it.next();
            clone.nutrients.put(nutName, nutrients.get(nutName));
        }
        return clone;
    }
}
