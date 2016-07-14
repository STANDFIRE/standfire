package fireparadox.model.plant.pattern;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.TreeSet;

import capsis.lib.fire.fuelitem.FiPlant;

import jeeb.lib.util.TicketDispenser;

/**
 * FiPattern is the a pattern for tree rendering. This pattern is composed of a collection of crown diameters.
 * @author S. Griffon - May 2007
 */
public class FmPattern implements Comparable, Cloneable, Serializable {
	
	private int id; //A single identifiant
	private String alias; //An alias to identify the patten
	private String info; //A string that represents the shape of the pattern : hDmax - diametersInferior[0] - diametersInferior[1] - ... - diametersSuperior[0] - diametersSuperior[1] - ...
	private TreeSet<FmPatternDiameter> diametersInferior;   //The array of diameters witch are below hDmax, in % of the distance between hBottom (the height of crown bottom) and hDmax
	private TreeSet<FmPatternDiameter> diametersSuperior;   //The array of diameters witch are above hDmax, in % of the distance between hDmax and hTop (the height of crown top)
	private double hDMax; //the height of the maximum crown diameter
	
	public static TicketDispenser idFactory = new TicketDispenser (); //usefull to generate a single identifiant for the object of this class
	
	/** Creates a new instance of FiPattern with default name and default diameter */
	public FmPattern () {
		super ();
		diametersInferior = new TreeSet<FmPatternDiameter> ();
		diametersSuperior = new TreeSet<FmPatternDiameter> ();
		info="";
		alias="";
		id = FmPattern.idFactory.getNext ();
		this.hDMax=50;
	}
	
	public FmPattern (int id) {
		this ();
		this.id = id;
		FmPattern.idFactory.setCurrentValue (id);
	}
	
	// this constructor try to build a FiPattern from plant crown profile and dimensions FP jan 2010
	public FmPattern (FiPlant plant) {
		this ();
		double[][] crownProfile = plant.getCrownProfile();
		if (crownProfile == null) {
			this.id = 0;
		} else {
			this.id = FmPattern.idFactory.getNext ();
			double h = plant.getHeight();
			double cbh = plant.getCrownBaseHeight();
			double maxdbhh = plant.getMaxDiameterHeight();
			this.setHDMax(maxdbhh);
			//double maxDiameter = plant.getCrownDiameter();
			for (int ndiam = 0; ndiam < crownProfile.length ; ndiam++ ) {
				if (crownProfile[ndiam][0]*0.01*(h-cbh)<=maxdbhh-cbh) {
					double factor = 1d;// (h - cbh) / (maxdbhh - cbh);
					this.addDiametersInferior(new FmPatternDiameter(
							crownProfile[ndiam][0] * factor,
							crownProfile[ndiam][1]));
				// System.out.println("inf:h,d=" + crownProfile[ndiam][0]
					// * factor + "," + crownProfile[ndiam][1]);
				} else {
					double factor = 1d;// (h - cbh) / (h - maxdbhh);
					this.addDiametersSuperior(new FmPatternDiameter(
							crownProfile[ndiam][0] * factor,
							crownProfile[ndiam][1]));
				// System.out.println("sup:h,d=" + crownProfile[ndiam][0]
					// * factor + "," + crownProfile[ndiam][1]);
				}
			}
			FmPattern.idFactory.setCurrentValue (id);
		}
	}
	
	
	@Override
	public FmPattern clone () {
		
		FmPattern fp = null;
		try {
			fp = (FmPattern) super.clone ();
		} catch(CloneNotSupportedException cnse) {
			// Should never be here because we implement Cloneable
		}
		fp.info = new String (info);
		
		fp.diametersInferior = new TreeSet<FmPatternDiameter> ();
		fp.diametersSuperior = new TreeSet<FmPatternDiameter> ();
		
		for (FmPatternDiameter fpd : diametersInferior)	{
			fp.diametersInferior.add (fpd.clone ());
		}
		
		for (FmPatternDiameter fpd : diametersSuperior)	{
			fp.diametersSuperior.add (fpd.clone ());
		}
		
		return fp;
		
	}
	
	public void setHDMax (double hDMax) {
		this.hDMax = hDMax;
	}
	
	public double getHDMax () {
		return hDMax;
	}
	
	public void addDiametersInferior (FmPatternDiameter diameterInferior) {
		this.diametersInferior.add (diameterInferior);
		getInfo ();
	}
	
	public void addDiametersSuperior (FmPatternDiameter diameterSuperior) {
		this.diametersSuperior.add (diameterSuperior);
		getInfo ();
	}
	
	public String getInfo () {
		NumberFormat f = NumberFormat.getIntegerInstance ();
		info=f.format (hDMax);
		for(FmPatternDiameter fdiam : diametersSuperior)  {
			info += " - s"+ f.format(fdiam.getWidth ());
		}
		for(FmPatternDiameter fdiam : diametersInferior) {
			info += " - i"+ f.format(fdiam.getWidth ());
		}
		return info;
	}

	public void setAlias (String alias) {
		this.alias = alias;
	}

	public String getAlias () {
		return alias;
	}

	public String getName () {
		if(alias.length () == 0) {			
			return String.valueOf (id)+" - "+getInfo ();
		} else {
			return alias+"-"+String.valueOf (id)+" - "+getInfo ();
		}
	}
	
	@Override
	public String toString () {
		return getName ();
	}
	
	public void setId (int id) {
		this.id = id;		
	}
	
	public int getId () {
		return id;
	}
	
	public TreeSet<FmPatternDiameter> getDiametersInferior () {
		return diametersInferior;
	}
	
	public TreeSet<FmPatternDiameter> getDiametersSuperior () {
		return diametersSuperior;
	}
	
	
	
	public int compareTo (Object o) {
		FmPattern fPattern = (FmPattern) o;
		return getName ().compareTo (fPattern.getName ());
	}
	
	
}
