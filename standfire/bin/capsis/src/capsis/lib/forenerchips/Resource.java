package capsis.lib.forenerchips;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.DefaultNumberFormat;

/**
 * A superclass for all resources in the forenerchips library (e.g. Chips)
 * 
 * @author N. Bilot - February 2013
 */
public class Resource implements Cloneable {

	private List<WorkingProcess> processHistory;

	public String processName;
	public Species species;
	public double plotArea_ha; // ha
	public double treeNumber;
	public double Hg;
	public double Hdom;
	public double Dg; // cm
	public double Ddom; // cm
	public double dMax; // cm
	public double G; // cm
	public double RDI; // cm
	public double VStem; // m3
	public double VMerchant; // m3
  public int numberOfPotentialLumberBoles;
  public double averagePotentialLumberBoleVolume; // m3
  public double cumulatedBoleVolume; // m3
  
  public String methodName; // name of the ForEnerChips method

	public String site; // One of ResourceSite.PLOT, ROADSIDE, PLATFORM, HEATING_PLANT, can be
						// changed by WPs
	public String status; // One of ResourceStatus.STANDING_TREE..., can be changed by WPs

  public String market; // One of ResourceStatus.STANDING_TREE..., can be changed by WPs
  
  public double machineWorkTime; // h, time needed for the machine in the last WorkingProcess to provide this Resource
  public double humanWorkTime; // h, time needed for the human operator in the last WorkingProcess to provide this Resource
    
	public double fuelConsumption; // kWh, energy needed in fuel in the last WorkingProcess to provide this Resource
  public double oilConsumption; // kWh, energy needed in fuel in the last WorkingProcess to provide this Resource
  public double lcConsumption; // kWh, energy needed in fuel in the last WorkingProcess to provide this Resource
  public double logisticsConsumption; // kWh, energy needed in fuel in the last WorkingProcess to provide this Resource
  public double processConsumption; // kWh, energy needed in the last WorkingProcess to provide this Resource
	public double chainConsumption; // kWh, energy needed so far by the chain to provide this Resource

  public double moistureContent; // [0, 1] gross moisture content, 0: dry, 1: water only

	// The total wet biomass (t), sum of the 6 biomass per compartment below
	public double wetBiomass;

	// The wet biomass (t) of the branches with a diameter within [0, 4[ cm
	public double wetBiomassBr0_4;

	// The wet biomass (t) of the branches with a diameter within [4, 7[ cm
	public double wetBiomassBr4_7;

	// The wet biomass (t) of the branches with a diameter greater than 7 cm
	public double wetBiomassBr7_more;

	// The wet biomass (t) of the stem with a diameter within [0, 7[ cm
	public double wetBiomassStem0_7;

	// The wet biomass (t) of the stem with a diameter greater than 7 cm
	public double wetBiomassStem7_more_top;
  
  // The wet biomass (t) of the stem with a diameter greater than 7 cm
	public double wetBiomassStem7_more_bole;

	// The wet biomass (t) of the leaves
	public double wetBiomassLeaves;

	// The quantities of C in biomass by compartments and finally in total (kg)
	public double massCBr0_4; // kg
	public double massCBr4_7; // kg
	public double massCBr7_more; // kg
	public double massCStem0_7; // kg
	public double massCStem7_more_top; // kg
  public double massCStem7_more_bole; // kg
	public double massCLeaves; // kg
	public double massCTot; // kg
	

	// The quantities of N in biomass by compartments and finally in total (kg)
	public double massNBr0_4; // kg
	public double massNBr4_7; // kg
	public double massNBr7_more; // kg
	public double massNStem0_7; // kg
	public double massNStem7_more_top; // kg
	public double massNStem7_more_bole; // kg
	public double massNLeaves; // kg
	public double massNTot; // kg
	
	// The quantities of S in biomass by compartments and finally in total (kg)
	public double massSBr0_4; // kg
	public double massSBr4_7; // kg
	public double massSBr7_more; // kg
	public double massSStem0_7; // kg
	public double massSStem7_more_top; // kg
	public double massSStem7_more_bole; // kg
	public double massSLeaves; // kg
	public double massSTot; // kg
	
	// The quantities of P in biomass by compartments and finally in total (kg)
	public double massPBr0_4; // kg
	public double massPBr4_7; // kg
	public double massPBr7_more; // kg
	public double massPStem0_7; // kg
	public double massPStem7_more_top; // kg
	public double massPStem7_more_bole; // kg
	public double massPLeaves; // kg
	public double massPTot; // kg
	
	// The quantities of K in biomass by compartments and finally in total (Kg)
	public double massKBr0_4; // kg
	public double massKBr4_7; // kg
	public double massKBr7_more; // kg
	public double massKStem0_7; // kg
	public double massKStem7_more_top; // kg
	public double massKStem7_more_bole; // kg
	public double massKLeaves; // kg
	public double massKTot; // kg
	
	// The quantities of Ca in biomass by compartments and finally in total (Cag)
	public double massCaBr0_4; // kg
	public double massCaBr4_7; // kg
	public double massCaBr7_more; // kg
	public double massCaStem0_7; // kg
	public double massCaStem7_more_top; // kg
	public double massCaStem7_more_bole; // kg
	public double massCaLeaves; // kg
	public double massCaTot; // kg
	
	// The quantities of Mg in biomass by compartments and finally in total (Mgg)
	public double massMgBr0_4; // kg
	public double massMgBr4_7; // kg
	public double massMgBr7_more; // kg
	public double massMgStem0_7; // kg
	public double massMgStem7_more_top; // kg
	public double massMgStem7_more_bole; // kg
	public double massMgLeaves; // kg
	public double massMgTot; // kg
	
	// The quantities of Mn in biomass by compartments and finally in total (Mng)
	public double massMnBr0_4; // kg
	public double massMnBr4_7; // kg
	public double massMnBr7_more; // kg
	public double massMnStem0_7; // kg
	public double massMnStem7_more_top; // kg
	public double massMnStem7_more_bole; // kg
	public double massMnLeaves; // kg
	public double massMnTot; // kg
	
	
	/**
	 * Constructor for the initial resource, to be used by the modules compatible with Forenerchips.
	 */
	public Resource (Species species, double plotArea_ha, double treeNumber, double Hg, double Hdom, double Dg, double Ddom, double dMax, double G, double RDI, double VStem, double VMerchant, int numberOfPotentialLumberBoles, double averagePotentialLumberBoleVolume, double cumulatedBoleVolume, double moistureContent, double wetBiomassBr0_4, double wetBiomassBr4_7, double wetBiomassBr7_more, double wetBiomassStem0_7, double wetBiomassStem7_more_top, double wetBiomassStem7_more_bole, double wetBiomassLeaves) {

		processHistory = new ArrayList<WorkingProcess> ();

		this.methodName = "BeforeIntervention";
		this.processName = "Cut_trees";
    this.species = species;
		this.plotArea_ha = plotArea_ha;
		this.treeNumber = treeNumber;
		this.Hg = Hg;
		this.Hdom = Hdom;
		this.Dg = Dg;
		this.Ddom = Ddom;
		this.dMax = dMax;
    this.G = G;
    this.RDI = RDI;
    this.VStem = VStem;
    this.VMerchant = VMerchant;
    this.numberOfPotentialLumberBoles = numberOfPotentialLumberBoles;
    this.averagePotentialLumberBoleVolume = averagePotentialLumberBoleVolume;
    this.cumulatedBoleVolume = cumulatedBoleVolume;

    site = ResourceSite.PLOT;
		status = ResourceStatus.STANDING_TREE;
    market = MaterialMarketDestination.ND;
    machineWorkTime = 0;
    humanWorkTime = 0;
    fuelConsumption = 0;
    oilConsumption = 0;
    lcConsumption = 0;
    logisticsConsumption = 0;
    processConsumption = 0;
		chainConsumption = 0;

		this.moistureContent = moistureContent;

		this.wetBiomassBr0_4 = wetBiomassBr0_4;
		this.wetBiomassBr4_7 = wetBiomassBr4_7;
		this.wetBiomassBr7_more = wetBiomassBr7_more;
		this.wetBiomassStem0_7 = wetBiomassStem0_7;
		this.wetBiomassStem7_more_top = wetBiomassStem7_more_top;
    this.wetBiomassStem7_more_bole = wetBiomassStem7_more_bole;
		this.wetBiomassLeaves = wetBiomassLeaves;

		updateWetBiomass ();

		updateMineralMasses ();
	}

	/**
	 * Sums all compartments biomasses into total wetBiomass.
	 */
	public void updateWetBiomass () {
		wetBiomass = wetBiomassBr0_4 + wetBiomassBr4_7 + wetBiomassBr7_more + wetBiomassStem0_7 + wetBiomassStem7_more_top + wetBiomassStem7_more_bole + wetBiomassLeaves;
	}

	/**
	 * Recomputes the values of massC, massN, massS, massP and massK based on wetBiomasses per
	 * compartment and the mineral concentrations of the species.
	 */
	public void updateMineralMasses () {
		updateMassC ();
		updateMassN ();
		updateMassS ();
		updateMassP ();
		updateMassK ();
		updateMassCa ();
		updateMassMg ();
		updateMassMn ();
	}

	private void updateMassC () {
		double[] conc = species.getCConcentrations ();
		// Dry material content is calculated as a ratio regarding moisture content
    double dryContent = 1 - moistureContent;
    // Masses of elements (kg) are calculated from the dry content material ratio, the wet biomass (kg), and the concentration of the element in the compartment (ratio).
		massCBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]);
		massCBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massCBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massCStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massCStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
    massCStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massCLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massCTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);

	}

	private void updateMassN () {
		double[] conc = species.getNConcentrations ();
		double dryContent = 1 - moistureContent;
		massNBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massNBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massNBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massNStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massNStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
    massNStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massNLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massNTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);
	}

	private void updateMassS () {
		double[] conc = species.getSConcentrations ();
		double dryContent = 1 - moistureContent;
		massSBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massSBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massSBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massSStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massSStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
    massSStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massSLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massSTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);

	}

	private void updateMassP () {
		double[] conc = species.getPConcentrations ();
		double dryContent = 1 - moistureContent;
		massPBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massPBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massPBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massPStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massPStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
    massPStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massPLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massPTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);

	}

	private void updateMassK () {
		double[] conc = species.getKConcentrations ();
		double dryContent = 1 - moistureContent;
		massKBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massKBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massKBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massKStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massKStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
		massKStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massKLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massKTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);
		
	}
	
	private void updateMassCa () {
		double[] conc = species.getCaConcentrations ();
		double dryContent = 1 - moistureContent;
		massCaBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massCaBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massCaBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massCaStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massCaStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
		massCaStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massCaLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massCaTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);
		
	}
	
	private void updateMassMg () {
		double[] conc = species.getMgConcentrations ();
		double dryContent = 1 - moistureContent;
		massMgBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massMgBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massMgBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massMgStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massMgStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
		massMgStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massMgLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massMgTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);
		
	}
	
	private void updateMassMn () {
		double[] conc = species.getMnConcentrations ();
		double dryContent = 1 - moistureContent;
		massMnBr0_4 = dryContent	* (wetBiomassBr0_4 * conc[0]); 
		massMnBr4_7 = dryContent *(wetBiomassBr4_7 * conc[1]);
		massMnBr7_more = dryContent * (wetBiomassBr7_more * conc[2]);
		massMnStem0_7 = dryContent * (wetBiomassStem0_7 * conc[3]);
		massMnStem7_more_top = dryContent * (wetBiomassStem7_more_top * conc[4]);
		massMnStem7_more_bole = dryContent * (wetBiomassStem7_more_bole * conc[5]);
		massMnLeaves = dryContent * (wetBiomassLeaves * conc[6]);
		massMnTot = dryContent * (wetBiomassBr0_4 * conc[0] + wetBiomassBr4_7 * conc[1] + wetBiomassBr7_more * conc[2] + wetBiomassStem0_7 * conc[3] + wetBiomassStem7_more_top * conc[4] + wetBiomassStem7_more_bole * conc[5] + wetBiomassLeaves * conc[6]);

	}

	public void updateBiomasses (double efficiency0_1) {

		wetBiomassBr0_4 *= efficiency0_1;
		wetBiomassBr4_7 *= efficiency0_1;
		wetBiomassBr7_more *= efficiency0_1;
		wetBiomassStem0_7 *= efficiency0_1;
		wetBiomassStem7_more_top *= efficiency0_1;
		wetBiomassStem7_more_bole *= efficiency0_1;
		wetBiomassLeaves *= efficiency0_1;

		updateWetBiomass ();
	}
  
  // (eventually) a methode to update biomasses regarding variable efficiencies depending on the compartments.
  public void updateBiomasses (double efficiencyBr0_4, double efficiencyBr4_7, double efficiencyBr7_more, double efficiencyStem0_7, double efficiencyStem7_more_top, double efficiencyStem7_more_bole, double efficiencyLeaves) {

		wetBiomassBr0_4 *= efficiencyBr0_4;
		wetBiomassBr4_7 *= efficiencyBr4_7;
		wetBiomassBr7_more *= efficiencyBr7_more;
		wetBiomassStem0_7 *= efficiencyStem0_7;
		wetBiomassStem7_more_top *= efficiencyStem7_more_top;
		wetBiomassStem7_more_bole *= efficiencyStem7_more_bole;
		wetBiomassLeaves *= efficiencyLeaves;

		updateWetBiomass ();
	}

	/**
	 * Returns a copy of this Resource
	 */
	public Resource copy () throws Exception {
		Resource copy = (Resource) super.clone ();
		// Copy the wp history
		copy.processHistory = new ArrayList<WorkingProcess> ();
		for (WorkingProcess wp : processHistory) {
			copy.processHistory.add (wp);
		}
		return copy;
	}

	public List<WorkingProcess> getProcessHistory () {
		return processHistory;
	}

	public String getResourceOrigin () {
		if (processHistory == null || processHistory.isEmpty ()) return "BeforeIntervention";
		int lastIndex = processHistory.size () - 1;
		WorkingProcess wp = processHistory.get (lastIndex);
		return wp.getName ();
	}

	public void addProcessInHistory (WorkingProcess wp) {
		this.processHistory.add (wp);
	}

	public String toString () {
		NumberFormat f = DefaultNumberFormat.getInstance ();

		return "Resource origin:" + getResourceOrigin () + " status:" + status + " site:" + site + " market:" + market +  " area_ha:" + plotArea_ha + " species:" + species.getName ()
				 + " wetBiomass:" + f.format (wetBiomass)+ " chainConsumption:" + f.format (chainConsumption);
	}

}
