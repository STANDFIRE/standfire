package capsis.lib.phenofit;

/**
 * A phenology object for Phenofit4: extends the FitlibPhenology with its dates
 * and index values and adds phases and states memories for one year.
 * 
 * @author F. de Coligny - August 2015
 */
public class Fit4Phenology extends FitlibPhenology {

	public FitlibStates leafDevelopmentStates;
	public FitlibPhases leafDevelopmentPhases;
	public FitlibStates flowerDevelopmentStates;
	public FitlibPhases flowerDevelopmentPhases;

	public FitlibStates leafFrostStates;
	public FitlibStates flowerFrostStates;

	/**
	 * Constructor.
	 */
	public Fit4Phenology(int birthYear, boolean deciduous) {
		super(birthYear, deciduous); // fc-31.8.2015
	}

	/**
	 * Constructor 2, during evolution.
	 */
	public Fit4Phenology(Fit4Phenology pheno0) {
		super(pheno0); // fc-31.8.2015
	}

	/**
	 * Trying to save space: set the states and phases to null (at the end of
	 * the year).
	 */
	public void resetMemory() { // fc-22.6.2015

		super.resetMemory(); // fc-31.8.2015

		leafDevelopmentStates = null;
		leafDevelopmentPhases = null;
		flowerDevelopmentStates = null;
		flowerDevelopmentPhases = null;

		leafFrostStates = null;
		flowerFrostStates = null;

	}

	// /**
	// * Returns the approx number of days from the beginning of this phenology
	// * object to the given year/day. Does not consider bissextile years, used
	// * only for securities.
	// */
	// public int getApproxPhenoAge(int year, int day) {
	// int age = day - 244;
	// if (year > birthYear) {
	// int n = year - birthYear;
	// age += n * 365;
	// }
	// return age;
	// }

	// /**
	// * Returns true if the given day is 1st September of the given year.
	// */
	// public static boolean isPhenoStartDay(int year, int d) {
	// int sept1 = 244;
	// if (FitlibClimate.isBissextile(year))
	// sept1++;
	//
	// return d == sept1;
	// }

	// public double getLeafUnfoldingDate() {
	// return leafUnfoldingDate;
	// }
	//
	// public void setLeafUnfoldingDate(double leafUnfoldingDate) {
	// this.leafUnfoldingDate = leafUnfoldingDate;
	// }
	//
	// public double getFloweringDate() {
	// return floweringDate;
	// }
	//
	// public void setFloweringDate(double floweringDate) {
	// this.floweringDate = floweringDate;
	// }
	//
	// public double getFruitGrowthInitDate() {
	// return fruitGrowthInitDate;
	// }
	//
	// public void setFruitGrowthInitDate(double fruitGrowthInitDate) {
	// this.fruitGrowthInitDate = fruitGrowthInitDate;
	// }
	//
	// public double getFruitMaturationDate() {
	// return fruitMaturationDate;
	// }
	//
	// public void setFruitMaturationDate(double fruitMaturationDate) {
	// this.fruitMaturationDate = fruitMaturationDate;
	// }
	//
	// public double getLeafSenescenceDate() {
	// return leafSenescenceDate;
	// }
	//
	// public void setLeafSenescenceDate(double leafSenescenceDate) {
	// this.leafSenescenceDate = leafSenescenceDate;
	// }
	//
	// public double getLeafDormancyBreakDate() {
	// return leafDormancyBreakDate;
	// }
	//
	// public void setLeafDormancyBreakDate(double leafDormancyBreakDate) {
	// this.leafDormancyBreakDate = leafDormancyBreakDate;
	// }
	//
	// public double getFlowerDormancyBreakDate() {
	// return flowerDormancyBreakDate;
	// }
	//
	// public void setFlowerDormancyBreakDate(double flowerDormancyBreakDate) {
	// this.flowerDormancyBreakDate = flowerDormancyBreakDate;
	// }
	//
	// public double getFruitIndex() {
	// return fruitIndex;
	// }
	//
	// public void setFruitIndex(double fruitIndex) {
	// this.fruitIndex = fruitIndex;
	// }
	//
	// public double getLeafIndex() {
	// return leafIndex;
	// }
	//
	// public void setLeafIndex(double leafIndex) {
	// this.leafIndex = leafIndex;
	// }
	//
	// public double getMaturationIndex() {
	// return maturationIndex;
	// }
	//
	// public void setMaturationIndex(double maturationIndex) {
	// this.maturationIndex = maturationIndex;
	// }
	//
	// public boolean isDeciduous() {
	// return deciduous;
	// }

	// public void setDate1(int year, double d) throws Exception {
	// if (year == birthYear) {
	// d = d - FitlibClimate.getNbDays(birthYear);
	// } else if (year == birthYear + 1) {
	// // ok
	// } else if (year == birthYear + 2) {
	// d = d + FitlibClimate.getNbDays(birthYear + 1);
	// } else {
	// throw new Exception("Fit4Phenology.setDate1 (): Wrong year: " + year
	// + ", can not be greater than birthYear + 2");
	// }
	//
	// if (mode == LEAF_MODE) {
	// setLeafDormancyBreakDate(d);
	// } else if (mode == FLOWER_MODE) {
	// setFlowerDormancyBreakDate(d);
	// } else if (mode == FRUIT_MODE) {
	// setFruitGrowthInitDate(d);
	// } else if (mode == SENESCENCE_MODE) {
	// setLeafSenescenceDate(d);
	// } else {
	// throw new Exception("Fit4Phenology.setDate1 (): Wrong mode: " + mode);
	// }
	// }
	//
	// public double getDate1() throws Exception {
	// if (mode == LEAF_MODE) {
	// return getLeafDormancyBreakDate();
	// } else if (mode == FLOWER_MODE) {
	// return getFlowerDormancyBreakDate();
	// } else if (mode == FRUIT_MODE) {
	// return getFruitGrowthInitDate();
	// } else if (mode == SENESCENCE_MODE) {
	// return getLeafSenescenceDate();
	// } else {
	// throw new Exception("Fit4Phenology.getDate1 (): Wrong mode: " + mode);
	// }
	// }
	//
	// public void setDate2(int year, double d) throws Exception {
	// if (year == birthYear) {
	// d = d - FitlibClimate.getNbDays(birthYear);
	// } else if (year == birthYear + 1) {
	// // ok
	// } else if (year == birthYear + 2) {
	// d = d + FitlibClimate.getNbDays(birthYear + 1);
	// } else {
	// throw new Exception("Fit4Phenology.setDate2 (): Wrong year: " + year
	// + ", can not be greater than birthYear + 2");
	// }
	// if (mode == LEAF_MODE) {
	// setLeafUnfoldingDate(d);
	// } else if (mode == FLOWER_MODE) {
	// setFloweringDate(d);
	// } else if (mode == FRUIT_MODE) {
	// setFruitMaturationDate(d);
	// } else if (mode == SENESCENCE_MODE) {
	// throw new
	// Exception("Fit4Phenology.setDate2 (): no date 2 can be set for SENESCENCE_MODE");
	// } else {
	// throw new Exception("Fit4Phenology.setDate2 (): Wrong mode: " + mode);
	// }
	// }
	//
	// public double getDate2() throws Exception {
	// if (mode == LEAF_MODE) {
	// return getLeafUnfoldingDate();
	// } else if (mode == FLOWER_MODE) {
	// return getFloweringDate();
	// } else if (mode == FRUIT_MODE) {
	// return getFruitMaturationDate();
	// } else if (mode == SENESCENCE_MODE) {
	// throw new
	// Exception("Fit4Phenology.getDate2 (): no date 2 can be returned for SENESCENCE_MODE");
	// } else {
	// throw new Exception("Fit4Phenology.getDate2 (): Wrong mode: " + mode);
	// }
	// }
	//
	// public void setMode(int mode) {
	// this.mode = mode;
	// }
	//
	// public int getMode() {
	// return mode;
	// }

	public String toString() {
		return "Fit4Phenology birthYear: " + birthYear + " leafUnfoldingDate: " + leafUnfoldingDate + " (...)";
	}
}
