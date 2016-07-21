package capsis.lib.phenofit;

/**
 * A phenology object for Phenofit5: extends the FitlibPhenology with its dates
 * and index values and adds a 1 day memory needed by the phenology functions.
 *
 * @author F. de Coligny - August 2015
 */
public class Fit5Phenology extends FitlibPhenology {

	// 1 day memory

	public FitlibMemory leafMemory;
	public FitlibMemory flowerMemory;





	/**
	 * Constructor.
	 */
	public Fit5Phenology(int birthYear, boolean deciduous) {
		super(birthYear, deciduous); // fc-31.8.2015

		leafMemory = new FitlibMemory ();
		flowerMemory = new FitlibMemory ();
	}

	/**
	 * Constructor 2, during evolution.
	 */
	public Fit5Phenology(Fit5Phenology pheno0) {
		super(pheno0); // fc-31.8.2015

		// Memo the values of pheno0
		// fc-31.8.2015
		this.leafMemory = new FitlibMemory (pheno0.leafMemory);
		this.flowerMemory = new FitlibMemory (pheno0.flowerMemory);

	}

	/**
	 * Trying to save space: set the states and phases to null (at the end of
	 * the year).
	 */
	public void resetMemory() { // fc-22.6.2015

		super.resetMemory(); // fc-31.8.2015

	}

	public String toString() {
		return "Fit5Phenology birthYear: " + birthYear + " leafUnfoldingDate: " + leafUnfoldingDate + " (...)";
	}

}
