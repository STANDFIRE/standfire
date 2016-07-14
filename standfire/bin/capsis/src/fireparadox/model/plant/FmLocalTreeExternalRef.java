package fireparadox.model.plant;

import fireparadox.model.FmModel;



/**	FiPlantExternalRef contains data to instantiate a FiPlant.
*	@author O. Vigy, F. de Coligny - july 2007
*/
public class FmLocalTreeExternalRef implements Cloneable {

	private String speciesName;
	private double dominantHeight;
	private double meanAge;
	private double ageStandardDeviation;
	private double stemDensity;
	private double liveMoisture;
	private double deadMoisture;
	private double liveTwigMoisture;
	private FmModel model;
	

	/**	Constructor for FireParadox externalRef, used to
	*	create - local - FireTrees (not from database).
	*/
	public FmLocalTreeExternalRef (
			String speciesName,
			double dominantHeight,
			double meanAge,
			double ageStandardDeviation, double stemDensity,
			double liveMoisture, double deadMoisture, double liveTwigMoisture,
			FmModel model) {
		
		
		this.speciesName = speciesName;
		this.dominantHeight = dominantHeight;
		this.meanAge = meanAge;
		this.ageStandardDeviation = ageStandardDeviation;
		this.stemDensity = stemDensity;
		this.liveMoisture = liveMoisture;
		this.deadMoisture = deadMoisture;
		this.liveTwigMoisture = liveTwigMoisture;
		this.model = model;
	}

	public String getSpeciesName () {return speciesName;}
	public double getDominantHeight() {
		return dominantHeight;
	}
	public double getMeanAge () {return meanAge;}
	public double getAgeStandardDeviation () {return ageStandardDeviation;}
	public double getStemDensity() {
		return stemDensity;
	}

	public double getLiveMoisture() {
		return liveMoisture;
	}

	public double getDeadMoisture() {
		return deadMoisture;
	}

	public double getLiveTwigMoisture() {
		return liveTwigMoisture;
	}
	public FmModel getModel () {return model;}

	/**	Can be redefined in subclasses
	*/
	@Override
	public Object clone () {
		FmLocalTreeExternalRef o = null;
		try {
			o = (FmLocalTreeExternalRef) super.clone ();
		} catch (Exception exc) {}
		return o;
	}
}

