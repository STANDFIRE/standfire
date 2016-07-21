package capsis.lib.forestgales;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeSet;

import capsis.kernel.PathManager;

/**
 * The ForestGales configuration object.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 * update cm tl 02 2014
 */
public class FGConfiguration implements Serializable {

	private double snowDensity = 150; // kg/m3
	private double vonKarmanConstant = 0.4; // unitless
	private double airDensity = 1.2226; // kg/m3
	private double gravityAcceleration = 9.81; // m/s2
	private double resolutionOfCalculation = 0.01; // m/s
	private double elementDragCoefficient = 0.3; // CR, unitless
	private double surfaceDragCoefficient = 0.003; // CS, unitless
	private double roughnessConstant = 2; // CW, unitless
	private double heightOfCalculation = 10; // m
	//private double fieldZ0 = 0.06;//BARRY You must check the label and the value!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	private double surroundingLandRoughness = 0.06; // m = fieldZ0
	private double Ua = 5;
	private double U_C1 = -0.5903;
	private double U_C2 = 4.4345;
	private double U_C3 = -11.8633;
	private double U_C4 = 13.569;
	private double DAMStoWeibullA1 = -0.9626;
	private double DAMStoWeibullA2 = 0.4279;

	private boolean tmcInterventionsOccurredInThe5PastYears;
	private int tmcHowManyYearsInThePast;
	private double tmcMeanDbhBeforeIntervention;
	private double tmcMeanHeightBeforeIntervention;
	private double tmcNhaBeforeIntervention;

	private Map<String,FGSpecies> speciesMap;

	/**
	 * Constructor
	 */
	public FGConfiguration () throws Exception {
	}

	public double getSnowDensity () {
		return snowDensity;
	}

	public void setSnowDensity (double snowDensity) {
		this.snowDensity = snowDensity;
	}

	public double getVonKarmanConstant () {
		return vonKarmanConstant;
	}

	public void setVonKarmanConstant (double vonKarmanConstant) {
		this.vonKarmanConstant = vonKarmanConstant;
	}

	public double getAirDensity () {
		return airDensity;
	}

	public void setAirDensity (double airDensity) {
		this.airDensity = airDensity;
	}

	public double getGravityAcceleration () {
		return gravityAcceleration;
	}

	public void setGravityAcceleration (double gravityAcceleration) {
		this.gravityAcceleration = gravityAcceleration;
	}

	public double getResolutionOfCalculation () {
		return resolutionOfCalculation;
	}

	public void setResolutionOfCalculation (double resolutionOfCalculation) {
		this.resolutionOfCalculation = resolutionOfCalculation;
	}

	public double getElementDragCoefficient () {
		return elementDragCoefficient;
	}

	public void setElementDragCoefficient (double elementDragCoefficient) {
		this.elementDragCoefficient = elementDragCoefficient;
	}

	public double getSurfaceDragCoefficient () {
		return surfaceDragCoefficient;
	}

	public void setSurfaceDragCoefficient (double surfaceDragCoefficient) {
		this.surfaceDragCoefficient = surfaceDragCoefficient;
	}

	public double getRoughnessConstant () {
		return roughnessConstant;
	}

	public void setRoughnessConstant (double roughnessConstant) {
		this.roughnessConstant = roughnessConstant;
	}

	public double getHeightOfCalculation () {
		return heightOfCalculation;
	}

	public void setHeightOfCalculation (double heightOfCalculation) {
		this.heightOfCalculation = heightOfCalculation;
	}

	public double getSurroundingLandRoughness () {
		return surroundingLandRoughness;
	}

	public void setSurroundingLandRoughness (double surroundingLandRoughness) {
		this.surroundingLandRoughness = surroundingLandRoughness;
	}



	public double getUa () {
		return Ua;
	}

	public void setUa (double ua) {
		Ua = ua;
	}

	public double getU_C1 () {
		return U_C1;
	}

	public void setU_C1 (double u_C1) {
		U_C1 = u_C1;
	}

	public double getU_C2 () {
		return U_C2;
	}

	public void setU_C2 (double u_C2) {
		U_C2 = u_C2;
	}

	public double getU_C3 () {
		return U_C3;
	}

	public void setU_C3 (double u_C3) {
		U_C3 = u_C3;
	}

	public double getU_C4 () {
		return U_C4;
	}

	public void setU_C4 (double u_C4) {
		U_C4 = u_C4;
	}

	public double getDAMStoWeibullA1 () {
		return DAMStoWeibullA1;
	}

	public void setDAMStoWeibullA1 (double dAMStoWeibullA1) {
		DAMStoWeibullA1 = dAMStoWeibullA1;
	}

	public double getDAMStoWeibullA2 () {
		return DAMStoWeibullA2;
	}

	public void setDAMStoWeibullA2 (double dAMStoWeibullA2) {
		DAMStoWeibullA2 = dAMStoWeibullA2;
	}

	public Map<String,FGSpecies> getSpeciesMap () {
		return speciesMap;
	}

	public FGSpecies getSpecies (String speciesName) throws Exception {
		FGSpecies sp = speciesMap.get (speciesName);
		if (sp == null) throw new Exception ("Unknown species: "+speciesName+", should be one of: "+getSpeciesNames ()+".");
		return sp;
	}

	public void loadSpeciesMap (String fileName) throws Exception {
		FGSpeciesLoader loader = new FGSpeciesLoader (fileName);
		this.speciesMap = loader.interpret ();

	}


	public boolean isTmcInterventionsOccurredInThe5PastYears () {
		return tmcInterventionsOccurredInThe5PastYears;
	}


	public void setTmcInterventionsOccurredInThe5PastYears (boolean tmcInterventionsOccurredInThe5PastYears) {
		this.tmcInterventionsOccurredInThe5PastYears = tmcInterventionsOccurredInThe5PastYears;
	}


	public int getTmcHowManyYearsInThePast () {
		return tmcHowManyYearsInThePast;
	}


	public void setTmcHowManyYearsInThePast (int tmcHowManyYearsInThePast) {
		this.tmcHowManyYearsInThePast = tmcHowManyYearsInThePast;
	}


	public double getTmcMeanDbhBeforeIntervention () {
		return tmcMeanDbhBeforeIntervention;
	}


	public void setTmcMeanDbhBeforeIntervention (double tmcMeanDbhBeforeIntervention) {
		this.tmcMeanDbhBeforeIntervention = tmcMeanDbhBeforeIntervention;
	}


	public double getTmcMeanHeightBeforeIntervention () {
		return tmcMeanHeightBeforeIntervention;
	}


	public void setTmcMeanHeightBeforeIntervention (double tmcMeanHeightBeforeIntervention) {
		this.tmcMeanHeightBeforeIntervention = tmcMeanHeightBeforeIntervention;
	}


	public double getTmcNhaBeforeIntervention () {
		return tmcNhaBeforeIntervention;
	}


	public void setTmcNhaBeforeIntervention (double tmcNhaBeforeIntervention) {
		this.tmcNhaBeforeIntervention = tmcNhaBeforeIntervention;
	}

	public String getSpeciesNames () {
		StringBuffer b = new StringBuffer ();
		for (String spName : new TreeSet<String> (speciesMap.keySet ())) {
			b.append (spName);
			b.append (", ");
		}
		// remove the last ", "
		b.deleteCharAt (b.length ()-1);
		b.deleteCharAt (b.length ()-1);
		return b.toString ();
	}

	public String toString () {
		StringBuffer b = new StringBuffer ("FGConfiguration\n");
		b.append ("  snowDensity: " + snowDensity + "\n");
		b.append ("  vonKarmanConstant: " + vonKarmanConstant + "\n");
		b.append ("  airDensity: " + airDensity + "\n");
		b.append ("  gravityAcceleration: " + gravityAcceleration + "\n");
		b.append ("  resolutionOfCalculation: " + resolutionOfCalculation + "\n");
		b.append ("  elementDragCoefficient: " + elementDragCoefficient + "\n");
		b.append ("  surfaceDragCoefficient: " + surfaceDragCoefficient + "\n");
		b.append ("  roughnessConstant: " + roughnessConstant + "\n");
		b.append ("  heightOfCalculation: " + heightOfCalculation + "\n");
		b.append ("  surroundingLandRoughness: " + surroundingLandRoughness + "\n");
		b.append ("  Ua: " + Ua + "\n");
		b.append ("  U_C1: " + U_C1 + "\n");
		b.append ("  U_C2: " + U_C2 + "\n");
		b.append ("  U_C3: " + U_C3 + "\n");
		b.append ("  U_C4: " + U_C4 + "\n");
		b.append ("  DAMStoWeibullA1: " + DAMStoWeibullA1 + "\n");
		b.append ("  DAMStoWeibullA2: " + DAMStoWeibullA2 + "\n");

		b.append ("  tmcInterventionsOccurredInThe5PastYears: " + tmcInterventionsOccurredInThe5PastYears + "\n");
		b.append ("  tmcHowManyYearsInThePast: " + tmcHowManyYearsInThePast + "\n");
		b.append ("  tmcMeanDbhBeforeIntervention: " + tmcMeanDbhBeforeIntervention + "\n");
		b.append ("  tmcMeanHeightBeforeIntervention: " + tmcMeanHeightBeforeIntervention + "\n");
		b.append ("  tmcNhaBeforeIntervention: " + tmcNhaBeforeIntervention + "\n");

		b.append ("  speciesMap: \n");
		if (speciesMap == null) {
			b.append ("  null \n");
		} else {
			for (FGSpecies sp : speciesMap.values ()) {
				b.append (sp);
			}
		}

		return b.toString ();
	}

	public static void main (String[] args) throws Exception { // Testing the species loader
		FGConfiguration config = new FGConfiguration ();
		config.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");
		System.out.println (config);
	}

}
