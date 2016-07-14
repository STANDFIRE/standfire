package capsis.lib.phenofit;

/**
 * A phenology object: dates and index values.
 * 
 * @author Isabelle Chuine, Yassine Motie, F. de Coligny - May 2015, August 2015
 */
public abstract class FitlibPhenology {

	public static final byte LEAF_MODE = 1;
	public static final byte FLOWER_MODE = 2;
	public static final byte FRUIT_MODE = 3;
	public static final byte SENESCENCE_MODE = 4;

	public static final double DATE_NOT_SET = -999; // fc-10.11.2015
	
	// fc-18.6.2015 date of creation of this phenology object (leaf and flower
	// cohort)
	public int birthYear;
	public boolean deciduous; // fc-18.6.2015
	
	public double leafUnfoldingDate = DATE_NOT_SET; // day
	public double floweringDate = DATE_NOT_SET; // day

	public double fruitGrowthInitDate = DATE_NOT_SET; // Day

	public double fruitMaturationDate = DATE_NOT_SET; // day
	public double leafSenescenceDate = DATE_NOT_SET; // day

	public double leafDormancyBreakDate = DATE_NOT_SET; // day
	public double flowerDormancyBreakDate = DATE_NOT_SET; // day

	public double fruitIndex; // [0, 1]
	public double leafIndex; // [0, 1]
	public double maturationIndex; // [0, 1]

	private int mode; // LEAF_MODE...

	
	/**
	 * Constructor.
	 */
	public FitlibPhenology(int birthYear, boolean deciduous) {
		this.birthYear = birthYear;
		this.deciduous = deciduous;
	}

	/**
	 * Constructor 2, during evolution.
	 */
	public FitlibPhenology(FitlibPhenology pheno0) {
		this(pheno0.birthYear, pheno0.deciduous);

		flowerDormancyBreakDate = pheno0.flowerDormancyBreakDate;
		leafUnfoldingDate = pheno0.leafUnfoldingDate;
		floweringDate = pheno0.floweringDate;

		fruitGrowthInitDate = pheno0.fruitGrowthInitDate;

		fruitMaturationDate = pheno0.fruitMaturationDate;
		leafSenescenceDate = pheno0.leafSenescenceDate;

		leafDormancyBreakDate = pheno0.leafDormancyBreakDate;
		flowerDormancyBreakDate = pheno0.flowerDormancyBreakDate;

		fruitIndex = pheno0.fruitIndex;
		leafIndex = pheno0.leafIndex;
		maturationIndex = pheno0.maturationIndex;

	}

	/**
	 * Trying to save space: set the states and phases to null (at the end of
	 * the year).
	 */
	public void resetMemory() { // fc-22.6.2015

	}

	/**
	 * Returns the approx number of days from the beginning of this phenology
	 * object to the given year/day. Does not consider bissextile years, used
	 * only for securities.
	 */
//	public int getApproxPhenoAge(int year, int day) {
//		int age = day - 244;
//		if (year > birthYear) {
//			int n = year - birthYear;
//			age += n * 365;
//		}
//		return age;
//	}

	/**
	 * Returns true if the given day is 1st September of the given year.
	 */
	public static boolean isPhenoStartDay(int year, int d) {
		int sept1 = 244;
		if (FitlibClimate.isBissextile(year))
			sept1++;

		return d == sept1;
	}

	public double getLeafUnfoldingDate() {
		return leafUnfoldingDate;
	}

	public void setLeafUnfoldingDate(double leafUnfoldingDate) {
		this.leafUnfoldingDate = leafUnfoldingDate;
	}

	public double getFloweringDate() {
		return floweringDate;
	}

	public void setFloweringDate(double floweringDate) {
		this.floweringDate = floweringDate;
	}

	public double getFruitGrowthInitDate() {
		return fruitGrowthInitDate;
	}

	public void setFruitGrowthInitDate(double fruitGrowthInitDate) {
		this.fruitGrowthInitDate = fruitGrowthInitDate;
	}

	public double getFruitMaturationDate() {
		return fruitMaturationDate;
	}

	public void setFruitMaturationDate(double fruitMaturationDate) {
		this.fruitMaturationDate = fruitMaturationDate;
	}

	public double getLeafSenescenceDate() {
		return leafSenescenceDate;
	}

	public void setLeafSenescenceDate(double leafSenescenceDate) {
		this.leafSenescenceDate = leafSenescenceDate;
	}

	public double getLeafDormancyBreakDate() {
		return leafDormancyBreakDate;
	}

	public void setLeafDormancyBreakDate(double leafDormancyBreakDate) {
		this.leafDormancyBreakDate = leafDormancyBreakDate;
	}

	public double getFlowerDormancyBreakDate() {
		return flowerDormancyBreakDate;
	}

	public void setFlowerDormancyBreakDate(double flowerDormancyBreakDate) {
		this.flowerDormancyBreakDate = flowerDormancyBreakDate;
	}

	public double getFruitIndex() {
		return fruitIndex;
	}

	public void setFruitIndex(double fruitIndex) {
		this.fruitIndex = fruitIndex;
	}

	public double getLeafIndex() {
		return leafIndex;
	}

	public void setLeafIndex(double leafIndex) {
		this.leafIndex = leafIndex;
	}

	public double getMaturationIndex() {
		return maturationIndex;
	}

	public void setMaturationIndex(double maturationIndex) {
		this.maturationIndex = maturationIndex;
	}

	public boolean isDeciduous() {
		return deciduous;
	}

	/**
	 * Interprets the given date in the Phenofit 4 time scale, depending on this phenology birthYear.
	 * E.g. birthYear = 2010, year = 2010, d = 244 -> -122
	 */
	public int getPhenofit4Date (int year, int d) throws Exception {
		return (int) getPhenofit4Date (year, (double) d);
	}
	public double getPhenofit4Date (int year, double d) throws Exception {
		// fc+ic+jg-10.11.2015
		if (year == birthYear) {
			return d - FitlibClimate.getNbDays(birthYear);
		} else if (year == birthYear + 1) {
			// ok
			return d;
		} else if (year == birthYear + 2) {
			return d + FitlibClimate.getNbDays(birthYear + 1);
		} else {
			throw new Exception("FitlibPhenology.getPhenofit4Date (): Wrong year: " + year
					+ ", should be in [birthYear ("+birthYear+"), birthYear + 2 ("+(birthYear + 2)+")]");
		}
	}
	
	/**
	 * Returns true if the date1 was already set.
	 */
	public boolean isSetDate1 () throws Exception {
		return getDate1() != DATE_NOT_SET; // fc+ic+jg-10.11.2015
	}
	
	/**
	 * Returns true if the date2 was already set.
	 */
	public boolean isSetDate2 () throws Exception {
		return getDate2() != DATE_NOT_SET; // fc+ic+jg-10.11.2015
	}
	
	public boolean isSetLeafUnfoldingDate () throws Exception {
		return leafUnfoldingDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetFloweringDate () throws Exception {
		return floweringDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetFruitGrowthInitDate () throws Exception {
		return fruitGrowthInitDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetFruitMaturationDate () throws Exception {
		return fruitMaturationDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetLeafSenescenceDate () throws Exception {
		return leafSenescenceDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetLeafDormancyBreakDate () throws Exception {
		return leafDormancyBreakDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}
	public boolean isSetFlowerDormancyBreakDate () throws Exception {
		return flowerDormancyBreakDate != DATE_NOT_SET; // fc+ic+jg-28.1.2015
	}


	public void setDate1(int year, double d) throws Exception {
//		if (year == birthYear) {
//			d = d - FitlibClimate.getNbDays(birthYear);
//		} else if (year == birthYear + 1) {
//			// ok
//		} else if (year == birthYear + 2) {
//			d = d + FitlibClimate.getNbDays(birthYear + 1);
//		} else {
//			throw new Exception("FitlibPhenology.setDate1 (): Wrong year: " + year
//					+ ", should be in [birthYear ("+birthYear+"), birthYear + 2 ("+(birthYear + 2)+")]");
//		}

		d = getPhenofit4Date(year, d); // fc+ic+jg-10.11.2015
		
		if (mode == LEAF_MODE) {
			setLeafDormancyBreakDate(d);
		} else if (mode == FLOWER_MODE) {
			setFlowerDormancyBreakDate(d);
		} else if (mode == FRUIT_MODE) {
			setFruitGrowthInitDate(d);
		} else if (mode == SENESCENCE_MODE) {
			setLeafSenescenceDate(d);
		} else {
			throw new Exception("FitlibPhenology.setDate1 (): Wrong mode: " + mode);
		}
	}

	public double getDate1() throws Exception {
		if (mode == LEAF_MODE) {
			return getLeafDormancyBreakDate();
		} else if (mode == FLOWER_MODE) {
			return getFlowerDormancyBreakDate();
		} else if (mode == FRUIT_MODE) {
			return getFruitGrowthInitDate();
		} else if (mode == SENESCENCE_MODE) {
			return getLeafSenescenceDate();
		} else {
			throw new Exception("FitlibPhenology.getDate1 (): Wrong mode: " + mode);
		}
	}
	
	public void setDate2(int year, double d) throws Exception {
//		if (year == birthYear) {
//			d = d - FitlibClimate.getNbDays(birthYear);
//		} else if (year == birthYear + 1) {
//			// ok
//		} else if (year == birthYear + 2) {
//			d = d + FitlibClimate.getNbDays(birthYear + 1);
//		} else {
//			throw new Exception("FitlibPhenology.setDate2 (): Wrong year: " + year
//					+ ", should be in [birthYear ("+birthYear+"), birthYear + 2 ("+(birthYear + 2)+")]");
//		}

		d = getPhenofit4Date(year, d); // fc+ic+jg-10.11.2015
		
		if (mode == LEAF_MODE) {
			setLeafUnfoldingDate(d);
		} else if (mode == FLOWER_MODE) {
			setFloweringDate(d);
		} else if (mode == FRUIT_MODE) {
			setFruitMaturationDate(d);
		} else if (mode == SENESCENCE_MODE) {
			throw new Exception("FitlibPhenology.setDate2 (): no date 2 can be set for SENESCENCE_MODE");
		} else {
			throw new Exception("FitlibPhenology.setDate2 (): Wrong mode: " + mode);
		}
	}

	public double getDate2() throws Exception {
		if (mode == LEAF_MODE) {
			return getLeafUnfoldingDate();
		} else if (mode == FLOWER_MODE) {
			return getFloweringDate();
		} else if (mode == FRUIT_MODE) {
			return getFruitMaturationDate();
		} else if (mode == SENESCENCE_MODE) {
			throw new Exception("FitlibPhenology.getDate2 (): no date 2 can be returned for SENESCENCE_MODE");
		} else {
			throw new Exception("FitlibPhenology.getDate2 (): Wrong mode: " + mode);
		}
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public String toString() {
		return "FitlibPhenology birthYear: " + birthYear + " leafUnfoldingDate: " + leafUnfoldingDate + " (...)";
	}
}
