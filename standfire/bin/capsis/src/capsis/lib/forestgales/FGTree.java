package capsis.lib.forestgales;

import java.io.Serializable;
import java.text.NumberFormat;

import jeeb.lib.util.DefaultNumberFormat;

/**
 * The tree description for ForestGales.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGTree implements Serializable {

	static private NumberFormat nf = DefaultNumberFormat.getInstance (); // to print numbers with 3 decimal digits

	// In standLevel mode, all these properties are mean values

	private double dbh_m; // m
	private double height; // m
	private double crownWidth; // m, optional (-1)
	private double crownDepth; // m, optional (-1)
	private double stemVolume; // m3, optional (-1)
	private double stemWeight; // kg, optional (-1)
	private double crownVolume; // m3, optional (-1)
	private double crownWeight; // kg, optional (-1)
	private double [] z;// m optionnal (-1)
	private double [] mass;// kg optionnal (-1)
	private double [] diam;// cm optionnal (-1)

	private String speciesName;

	// Results
	private double cwsForBreakage = -1; // m/s
	private double cwsForOverturning = -1; // m/s

	private double probabilityOfBreakage = -1; // optional (-1)
	private double probabilityOfOverturning = -1; // optional (-1)

	/**
	 * Constructor.
	 */
	public FGTree (double dbh_m, double height, double crownWidth, double crownDepth, double stemVolume,
			double stemWeight, double crownVolume, double crownWeight, double [] diam, double [] z, double [] mass, String speciesName) {
		super ();
		this.dbh_m = dbh_m;
		this.height = height;
		this.crownWidth = crownWidth;
		this.crownDepth = crownDepth;
		this.stemVolume = stemVolume;
		this.stemWeight = stemWeight;
		this.crownVolume = crownVolume;
		this.crownWeight = crownWeight;
		this.diam = diam  ;
		this.z = z ;
		this.mass = mass ;
		this.speciesName = speciesName;
	}

	public double getDbh_m () {
		return dbh_m;
	}

	public double getHeight () {
		return height;
	}

	public double getCrownWidth () {
		return crownWidth;
	}

	public void setCrownWidth (double crownWidth) {
		this.crownWidth = crownWidth;
	}

	public double getCrownDepth () {
		return crownDepth;
	}

	public void setCrownDepth (double crownDepth) {
		this.crownDepth = crownDepth;
	}

	public double getStemVolume () {
		return stemVolume;
	}

	public double getStemWeight () {
		return stemWeight;
	}

	public void setStemWeight (double stemWeight) {
		this.stemWeight = stemWeight;
	}

	public double getCrownVolume () {
		return crownVolume;
	}

	public void setCrownVolume (double crownVolume) {
		this.crownVolume = crownVolume;
	}

	public double getCrownWeight () {
		return crownWeight;
	}

	public void setCrownWeight (double crownWeight) {
		this.crownWeight = crownWeight;
	}

	public double [] getDiam () {
		return diam;
	}

	public void setDiam (double [] diam) {
		this.diam = diam;
	}

	public double [] getH () {
		return z;
	}

	public void setH (double [] z) {
		this.z = z;
	}

	public double [] getMass () {
		return mass;
	}

	public void setMass (double [] mass) {
		this.mass = mass;
	}

	public String getSpeciesName () {
		return speciesName;
	}

	public void setCwsForBreakage (double cwsForBreakage) {
		this.cwsForBreakage = cwsForBreakage;
	}

	public void setCwsForOverturning (double cwsForOverturning) {
		this.cwsForOverturning = cwsForOverturning;
	}

	public void setProbabilityOfBreakage (double probabilityOfBreakage) {
		this.probabilityOfBreakage = probabilityOfBreakage;
	}

	public void setProbabilityOfOverturning (double probabilityOfOverturning) {
		this.probabilityOfOverturning = probabilityOfOverturning;
	}

	public double getCwsForBreakage () {
		return cwsForBreakage;
	}

	public double getCwsForOverturning () {
		return cwsForOverturning;
	}

	public double getProbabilityOfBreakage () {
		return probabilityOfBreakage;
	}

	public double getProbabilityOfOverturning () {
		return probabilityOfOverturning;
	}

	public String toString () {
		//StringBuffer b = new StringBuffer ("FGTree " + nf.format (dbh_m) + "/" + nf.format (height) + "\n");
		StringBuffer b = new StringBuffer ("FGTree \n ");
		b.append ("  dbh_m: " + dbh_m + "\n");
		b.append ("  diam[0]: " + diam + "\n");
		b.append ("  height: " + height + "\n");
		b.append ("  crownWidth: " + crownWidth + "\n");
		b.append ("  crownDepth: " + crownDepth + "\n");
		b.append ("  stemVolume: " + stemVolume + "\n");
		b.append ("  stemWeight: " + stemWeight + "\n");
		b.append ("  crownVolume: " + crownVolume + "\n");
		b.append ("  crownWeight: " + crownWeight + "\n");
		b.append ("  speciesName: " + speciesName + "\n");

		b.append ("  cwsForBreakage:           " + cwsForBreakage + "\n");
		b.append ("  cwsForOverturning:        " + cwsForOverturning + "\n");
		b.append ("  probabilityOfBreakage:    " + probabilityOfBreakage + "\n");
		b.append ("  probabilityOfOverturning: " + probabilityOfOverturning);

		return b.toString ();
	}

}
