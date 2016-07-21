package capsis.lib.phenofit;

import java.io.Serializable;

import phenofit5.model.Fit5Location;

/**
 * FitlibLocation is a location in Phenofit.
 * 
 * @author Isabelle Chuine, Yassine Motie - May 2015
 */
public class FitlibLocation implements Serializable {
// fc-2.9.2015 static here was a wrong idea, Phenofit could not be launched twaice in Capsis
//	protected static int idCounter = 1;
	
	protected int id;
	protected double latitude; // decimal degrees
	protected double longitude; // decimal degrees
	protected double altitude; // (m)
	protected double whc; // water holding capacity, mm, (= wtmax-wtmin)
	//protected double waterMax; // field capacity, mm, maximum amount water in soil // ic 25/03/16 
	//protected double waterMin; // wilting point, mm, minimum amount water in soil (non extractible water) // ic 25/03/16 
	
	// In the arrays below, index 1 is 1st January, index 0 is unused
	public double[] pet; // potential evapotranspiration, mm
	public double[] aet; // actual evapotranspiration, mm
	public double[] droughtIndex; // unitless, [0, 1]
	public double[] water; // soil water content, mm, [0, whc]

	/**
	 * Default constructor.
	 */
	public FitlibLocation() {
	}

	/**
	 * Constructor.
	 */
	public FitlibLocation(int locId, double latitude, double longitude) {
		this.id = locId;
		this.latitude = latitude;
		this.longitude = longitude;

	}
	
	/**
	 * Constructor 2, during evolution.
	 */
	public FitlibLocation(FitlibLocation originalLocation) {
		this.id = originalLocation.getId ();
		this.latitude = originalLocation.getLatitude ();
		this.longitude = originalLocation.getLongitude ();
		this.altitude = originalLocation.getAltitude();
		this.whc = originalLocation.getWhc();
		//this.waterMax = originalLocation.getWaterMax(); // ic 25/03/16 
		//this.waterMin = originalLocation.getWaterMin();// ic 25/03/16 
	}
	
	public int getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getWhc() {
		return whc;
	}
	
	/*public double getWtmax() {  // ic 25/03/16 
		return wtmax;
	}
	
	public double getWtmin() {  // ic 25/03/16 
		return wtmin; 
	}*/

	public void setWhc(double whc) {
		this.whc = whc;
	}
	
	/*public void setWtmax(double whc) {  // ic 25/03/16 
		this.waterMax = waterMax;
	}

	
	public void setWtmin(double whc) {  // ic 25/03/16 
		this.waterMin = waterMin;
	} */


}