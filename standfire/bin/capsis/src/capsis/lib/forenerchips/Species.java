package capsis.lib.forenerchips;

/**
 * A tree species
 * 
 * @author N. Bilot - February 2013
 */
public class Species {

	private String name;

	private boolean softwood; // bois résineux

	// C:index 0, N:index 1, S:index 2, P:index 3, K:index 4
	private double[][] concentrations; // C N S P K Ca Mg Mn concentrations for 6 compartments in the trees
	
	/**
	 * Constructor for the initial resource, to be used by the modules compatible with Forenerchips.
	 * The 8 methods must be called just after to set the concentrations for C, N, S, P, K, Ca, Mg and Mn
	 */
	public Species (String name, boolean softwood) {
		this.name = name;
		this.softwood = softwood;
		concentrations = new double[8][7]; // table with the 8 nutrients in lines and the 7 compartments in columns
	}

	/**
	 * Sets the C concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setCConcentrations (double[] CConcentrations) {
		concentrations[0] = CConcentrations;
	}

	/**
	 * Sets the N concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setNConcentrations (double[] NConcentrations) {
		concentrations[1] = NConcentrations;
	}

	/**
	 * Sets the S concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setSConcentrations (double[] SConcentrations) {
		concentrations[2] = SConcentrations;
	}

	/**
	 * Sets the P concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setPConcentrations (double[] PConcentrations) {
		concentrations[3] = PConcentrations;
	}

	/**
	 * Sets the K concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setKConcentrations (double[] KConcentrations) {
		concentrations[4] = KConcentrations;
	}
	
	/**
	 * Sets the Ca concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setCaConcentrations (double[] CaConcentrations) {
		concentrations[5] = CaConcentrations;
	}
	
	/**
	 * Sets the Mg concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setMgConcentrations (double[] MgConcentrations) {
		concentrations[6] = MgConcentrations;
	}
	
	/**
	 * Sets the Mn concentrations for the six compartments in this order: Br0_4, Br4_7, Br7_more,
	 * Stem0_7, Stem7_more_top, Stem7_more_bole, Leaves.
	 */
	public void setMnConcentrations (double[] MnConcentrations) {
		concentrations[7] = MnConcentrations;
	}

	public double[] getCConcentrations () {return concentrations[0];}
	public double[] getNConcentrations () {return concentrations[1];}
	public double[] getSConcentrations () {return concentrations[2];}
	public double[] getPConcentrations () {return concentrations[3];}
	public double[] getKConcentrations () {return concentrations[4];}
	public double[] getCaConcentrations () {return concentrations[5];}
	public double[] getMgConcentrations () {return concentrations[6];}
	public double[] getMnConcentrations () {return concentrations[7];}

	
	public String getName () {
		return name;
	}

	public String toString () {
		return "Species name: "+name+" softwood: "+softwood;
	}

}
