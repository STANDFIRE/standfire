package fireparadox.model.plant;

import fireparadox.model.FmModel;

//~ import jeeb.lib.sketch.item.factory.*;


/**	FiPlantExternalRef contains data to instantiate a FiPlant.
*	@author O. Vigy, F. de Coligny - july 2007
*/
public class FmPlantExternalRef implements Cloneable {

	private long fuelId;
	private double height;				// m. tree height
	private double crownBaseHeight;		// m. "hauteur de la base du houppier / hmin" Note: hmax = tree height
	private double crownDiameter;		// m. the maximum diameter of the crown
	private double crownDiameterHeight;	// m. height of crownDiameter
	private double moisture;
		private boolean openess;	// openess true / false
	private int speciesValue; 			// int, from fueldb
	private String speciesCode; 		// ex: Qi
	private String speciesName; 		// ex: Quercus ilex

	//sg 16/07/07
	private String speciesTrait;		//ex : resineous
	private String speciesGenus;		//ex : quercus
	private int speciesTaxonLevel;
	private FmModel model;

	/**	Constructor for FireParadox externalRef, used by FireFactory to
	*	create FireTrees with data coming from the fueldb.
	*/
	public FmPlantExternalRef (
			long 	fuelId,
			double 	height,
			double 	crownBaseHeight,
			double 	crownDiameter,
			double 	crownDiameterHeight,
			boolean openess,
			int 	speciesValue,
			String 	speciesCode,
			String 	speciesName,
			String speciesTrait,
			String speciesGenus,
			int speciesTaxonLevel,
			double moisture,
			FmModel model) {
		this.fuelId = fuelId;
		this.height = height;
		this.crownBaseHeight = crownBaseHeight;
		this.crownDiameter = crownDiameter;

		this.crownDiameterHeight = crownDiameterHeight;
		this.openess = openess;
		this.speciesValue = speciesValue;
		this.speciesCode = speciesCode;
		this.speciesName = speciesName;
		this.speciesTrait = speciesTrait;
		this.speciesGenus = speciesGenus;
		this.speciesTaxonLevel = speciesTaxonLevel;
		this.moisture = moisture;
		this.model=model;
	}

	public long getFuelId () {return fuelId;}
	public double getHeight () {return height;}
	public double getCrownBaseHeight () {return crownBaseHeight;}
	public double getCrownDiameter () {return crownDiameter;}
	public double getCrownDiameterHeight () {return crownDiameterHeight;}
	public double getMoisture() {
		return moisture;
	}
		public boolean isOpen () {return openess;}
	public int getSpeciesValue () {return speciesValue;}
	public String getSpeciesCode () {return speciesCode;}
	public String getSpeciesName () {return speciesName;}
	public String getSpeciesTrait () {return speciesTrait;}
	public String getSpeciesGenus () {return speciesGenus;}
	public int getSpeciesTaxonLevel () {return speciesTaxonLevel;}
	public FmModel getModel () {return model;}

	@Override
	public Object clone () {	// to be redefined in subclasses
		FmPlantExternalRef o = null;
		try {
			o = (FmPlantExternalRef) super.clone ();
		} catch (Exception exc) {}
		return o;
	}
}

