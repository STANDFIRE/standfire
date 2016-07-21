package capsis.lib.phenofit;

/**
 * Original survival and fitness variables of Phenofit 4 as published in Chuine & Beaubien 2001,
 * Ecology Letters.
 * 
 * @author Isabelle Chuine, Yassine Motie - June 2015
 */
public class FitlibFitness {
	
	public double droughtSurvival; // [0, 1]
	public double tempSurvival; // [0, 1]
	public double carbonSurvival; // [0, 1]
	public double survival; // [0, 1]

	public double fitness; // [0, 1]

	public double getDroughtSurvival() {
		return droughtSurvival;
	}

	public void setDroughtSurvival(double droughtSurvival) {
		this.droughtSurvival = droughtSurvival;
	}

	public double getTempSurvival() {
		return tempSurvival;
	}

	public void setTempSurvival(double tempSurvival) {
		this.tempSurvival = tempSurvival;
	}

	public double getCarbonSurvival() {
		return carbonSurvival;
	}

	public void setCarbonSurvival(double leafSurvival) {
		this.carbonSurvival = leafSurvival;
	}

	public double getSurvival() {
		return survival;
	}

	public void setSurvival(double survival) {
		this.survival = survival;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

}
