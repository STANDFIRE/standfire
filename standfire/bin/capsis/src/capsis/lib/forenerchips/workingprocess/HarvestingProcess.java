package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.MaterialMarketDestination;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A harvesting working process
 * 
 * @author N. Bilot - February 2013
 */
public class HarvestingProcess extends WorkingProcess {

  private String name;
  private boolean LeavesOn; // boolean
	private double harvestingPerf; // t/h
  private double efficiency; // t/h
  private double hourlyConsumption; // kW
  private double machineWheight; // t
  private double machineLifetime; // h
  private double machineToHumanTimeRatio; // e.g. 1.5
  private boolean machineCarrierNeeded;
  private String stemMarketDestination; // "ND", "ENERGY", "INDUSTRY", or "LUMBER
  private boolean branchesLogging;
  private String branchesMarketDestination; // "ND", "ENERGY" or "INDUSTRY"
  private String smallWoodMarketDestination; // "ND" or "ENERGY"
  
  //General parameters of the scenario
  private double meanCarConsumption_kWperkm;
  private double meanCarrierConsumption_kWperkm;
  private double humanWorkingDayDuration_h;
	private double contractorDistance_km; // km

	/**
	 * Constructor (Abatteuse faconeuse ou bucheron à pied). 
	 * Choices in parameter inspired by Lortz (1991): 
	 * Manual felling time and productivity in southern pine forests
	 */
	public HarvestingProcess (String name, boolean LeavesOn, double efficiency, double harvestingPerf, double hourlyConsumption, double machineWheight, double machineLifetime, double machineToHumanTimeRatio, boolean machineCarrierNeeded, String stemMarketDestination, boolean branchesLogging, String branchesMarketDestination, String smallWoodMarketDestination, double meanCarConsumption_kWperkm, double meanCarrierConsumption_kWperkm, double humanWorkingDayDuration_h, double contractorDistance_km) throws Exception {
		super ("HarvestingProcess");

    
		// Check throws an exception if the condition is false
    check ("efficiency", efficiency >= 0);
    check ("harvestingPerf", harvestingPerf >= 0);
    check ("hourlyConsumption", hourlyConsumption >= 0);
    check ("machineWheight", machineWheight >= 0);
    check ("machineLifetime", machineLifetime >= 0);
		check ("machineToHumanTimeRatio", machineToHumanTimeRatio >= 0);
		
    check ("stemMarketDestination", stemMarketDestination.equals (MaterialMarketDestination.ND) || stemMarketDestination.equals (MaterialMarketDestination.ENERGY) || stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) || stemMarketDestination.equals (MaterialMarketDestination.LUMBER));
    check ("branchesMarketDestination", branchesMarketDestination.equals (MaterialMarketDestination.ND) || branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) || branchesMarketDestination.equals (MaterialMarketDestination.INDUSTRY) || branchesMarketDestination.equals (MaterialMarketDestination.LUMBER));
    check ("smallWoodMarketDestination", smallWoodMarketDestination.equals (MaterialMarketDestination.ND) || smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) || smallWoodMarketDestination.equals (MaterialMarketDestination.INDUSTRY) || smallWoodMarketDestination.equals (MaterialMarketDestination.LUMBER));
    
		check ("meanCarConsumption_kWperkm", meanCarConsumption_kWperkm >= 0);
    check ("meanCarrierConsumption_kWperkm", meanCarrierConsumption_kWperkm >= 0);
    check ("humanWorkingDayDuration_h", humanWorkingDayDuration_h >= 0);
		check ("contractorDistance_km", contractorDistance_km >= 0);

		this.name = name;
		this.LeavesOn = LeavesOn;
    this.efficiency = efficiency;
    this.harvestingPerf = harvestingPerf;
    this.hourlyConsumption = hourlyConsumption;
    this.machineWheight = machineWheight;
    this.machineLifetime = machineLifetime;
		this.machineToHumanTimeRatio = machineToHumanTimeRatio;
    this.machineCarrierNeeded = machineCarrierNeeded;
    
		this.stemMarketDestination = stemMarketDestination;
    this.branchesLogging = branchesLogging;
		this.branchesMarketDestination = branchesMarketDestination;
		this.smallWoodMarketDestination = smallWoodMarketDestination;
    
		this.meanCarConsumption_kWperkm = meanCarConsumption_kWperkm;
    this.meanCarrierConsumption_kWperkm = meanCarrierConsumption_kWperkm;
    this.humanWorkingDayDuration_h = humanWorkingDayDuration_h;
		this.contractorDistance_km = contractorDistance_km;

		// What resource can be processed
		addCompatibleStatusOrSite (ResourceStatus.STANDING_TREE);
		addCompatibleStatusOrSite (ResourceSite.PLOT);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public HarvestingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");
		
    String wpName = name;
		boolean LeavesOn = booleanValue (st.nextToken ());
    double efficiency = doubleValue (st.nextToken ());
    double harvestingPerf = doubleValue (st.nextToken ());
		double hourlyConsumption = doubleValue (st.nextToken ());
    double machineWheight = doubleValue (st.nextToken ());
    double machineLifetime = doubleValue (st.nextToken ());
		double machineToHumanTimeRatio = doubleValue (st.nextToken ());
    boolean machineCarrierNeeded = booleanValue (st.nextToken ());
		
		String stemMarketDestination = st.nextToken ();
    if (stemMarketDestination.equals ("LUMBER")) 
			stemMarketDestination = MaterialMarketDestination.LUMBER;
		else if (stemMarketDestination.equals ("INDUSTRY")) 
			stemMarketDestination = MaterialMarketDestination.INDUSTRY;
    else if (stemMarketDestination.equals ("ENERGY")) 
			stemMarketDestination = MaterialMarketDestination.ENERGY;
    else if (stemMarketDestination.equals ("ND")) 
			stemMarketDestination = MaterialMarketDestination.ND;
		else 
			throw new Exception ("MaterialMarketDestination: wrong value for stemMarketDestination: "+stemMarketDestination+", must be LUMBER, INDUSTRY, ENERGY or ND");
    
		boolean branchesLogging = booleanValue (st.nextToken ());
		
    String branchesMarketDestination = st.nextToken ();
    if (branchesMarketDestination.equals ("INDUSTRY")) 
			branchesMarketDestination = MaterialMarketDestination.INDUSTRY;
    else if (branchesMarketDestination.equals ("ENERGY")) 
			branchesMarketDestination = MaterialMarketDestination.ENERGY;
    else if (branchesMarketDestination.equals ("ND")) 
			branchesMarketDestination = MaterialMarketDestination.ND;
    else 
			throw new Exception ("MaterialMarketDestination: wrong value for branchesMarketDestination: "+branchesMarketDestination+", must be LUMBER, INDUSTRY, ENERGY or ND");
    
    String smallWoodMarketDestination = st.nextToken ();
    if (smallWoodMarketDestination.equals ("ENERGY")) 
			smallWoodMarketDestination = MaterialMarketDestination.ENERGY;
    else if (smallWoodMarketDestination.equals ("ND")) 
			smallWoodMarketDestination = MaterialMarketDestination.ND;
		else 
			throw new Exception ("MaterialMarketDestination: wrong value for smallWoodMarketDestination: "+smallWoodMarketDestination+", must be LUMBER, INDUSTRY, ENERGY or ND");
			
    double meanCarConsumption_kWperkm  = doubleValue (st.nextToken ());
    double meanCarrierConsumption_kWperkm = doubleValue (st.nextToken ());
    double humanWorkingDayDuration_h = doubleValue (st.nextToken ());
		double contractorDistance_km = doubleValue (st.nextToken ());

    
		return new HarvestingProcess (name, LeavesOn, efficiency, harvestingPerf, hourlyConsumption, machineWheight, machineLifetime, machineToHumanTimeRatio, machineCarrierNeeded, stemMarketDestination, branchesLogging, branchesMarketDestination, smallWoodMarketDestination, meanCarConsumption_kWperkm, meanCarrierConsumption_kWperkm, humanWorkingDayDuration_h, contractorDistance_km);
	}
  

	/**
	 * Run the harvesting process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		Resource output = input.copy ();
		
    // Do the trees have leaves at the moment of the felling ?
    if(!LeavesOn){
      output.wetBiomassLeaves = 0;
      output.updateBiomasses (1);
      output.updateMineralMasses ();
    }
		
    // Time
    double harvestingTime_h = output.wetBiomass / harvestingPerf ;
    
		// Consumptions
		// 1. fuelConsumption
      double fuelConsumption = hourlyConsumption * output.wetBiomass / harvestingPerf ;
      
    // 2. Oil consumption
			double oilCoefficient = 0d;
			if(machineCarrierNeeded){
				oilCoefficient = 0.058;
			} else {
				oilCoefficient = 0.322;
			}
			double oilConsumption = fuelConsumption * oilCoefficient;

    // 3. Life Cycle consumption equivalent
      double lcConsumption_pert = 16556;
      double lcConsumption_perh = machineWheight * lcConsumption_pert / machineLifetime;
      double lcConsumption = lcConsumption_perh * harvestingTime_h;
      

    // 4. Logistics consumption
      // a. operatorTravelConsumption
      double humanProductiveWorkTime = harvestingTime_h * machineToHumanTimeRatio;
      int humanWorkTime_day = (int) Math.ceil (humanProductiveWorkTime / humanWorkingDayDuration_h);
      double operatorTravelConsumption = contractorDistance_km * 2d * meanCarConsumption_kWperkm * humanWorkTime_day; // kWh

      //  b. machineTravelConsumption
      double machineTravelConsumption = machineCarrierNeeded ? contractorDistance_km * 2d * meanCarrierConsumption_kWperkm : 0d; // kWh
      
    double logisticsConsumption = operatorTravelConsumption + machineTravelConsumption;

      
    // PROCESS CONSUMPTION
    double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;
		
		// Update the output resource
		output.machineWorkTime = harvestingTime_h;
    output.humanWorkTime = humanProductiveWorkTime;
    output.fuelConsumption = fuelConsumption;
    output.oilConsumption = oilConsumption;
    output.lcConsumption = lcConsumption;
    output.logisticsConsumption = logisticsConsumption;
    output.processConsumption = processConsumption;
    output.chainConsumption += processConsumption;
		
		// TRANSFORMATION
		// 15 possible scenarios of bucking and market distribution :
    //                    1 2 3 4 5 6   7 8 9 0 1 2   3 4 5
    //  stemMarket	      L	L	L	L	L	L		I	I	I	I	I	I		E	E	E
    //  branchesLogging		T	T	T	T	F	F		T	T	T	T	F	F		T	T	F
    //  branchesMarket	  I	I	E	E	E	N		I	I	E	E	E	N		E	E	E
    //  smallsMarket	    N	E	N	E	E	N		N	E	N	E	E	N		N	E	E
    // L = lumber market, I = industry, E = energy, N = ND, T = "TRUE", F = "FALSE"


    if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // lumber log are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
      
    // industry logs are made in branches and stem top >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      // industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // energy costs are spread between lumber and industry
      double totalMass = lumber.wetBiomass + industry.wetBiomass;
      double lumberPart = lumber.wetBiomass / totalMass;
      double industryPart = industry.wetBiomass / totalMass;
      
      energySpread(lumber, lumberPart);
      energySpread(industry, industryPart);
      
    // lumber and industry are output
      outputs.add (lumber);
      outputs.add (industry);
      
      
    } else if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ){
    // lumber log are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
      
    // industry logs are made in branches and stem top >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      // industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // <7 compartments are exploited energy
      Resource energy = output.copy ();
      energy.processName = name ;
      // energy.wetBiomassBr0_4 = 0;
      // energy.wetBiomassBr4_7 = 0;
      energy.wetBiomassBr7_more = 0;
      // energy.wetBiomassStem0_7 = 0;
      energy.wetBiomassStem7_more_top = 0;
      energy.wetBiomassStem7_more_bole = 0;
      // energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.RESIDUAL;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy costs are spread between lumber, industry and energy
      double totalMass = lumber.wetBiomass + industry.wetBiomass + energy.wetBiomass;
      double lumberPart = lumber.wetBiomass / totalMass;
      double industryPart = industry.wetBiomass / totalMass;
      double energyPart = energy.wetBiomass / totalMass;
      
      energySpread(lumber, lumberPart);
      energySpread(industry, industryPart);
      energySpread(energy, energyPart);
      
    // lumber, industry and energy are output
      outputs.add (lumber);
      outputs.add (industry);
      outputs.add (energy);

      
    } else if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // lumber log are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
    // energy are made in branches and stem top >7cm
      Resource energy = output.copy ();
      energy.processName = name ;
      energy.wetBiomassBr0_4 = 0;
      energy.wetBiomassBr4_7 = 0;
      // energy.wetBiomassBr7_more = 0;
      energy.wetBiomassStem0_7 = 0;
      // energy.wetBiomassStem7_more_top = 0;
      energy.wetBiomassStem7_more_bole = 0;
      energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.LOG;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy costs are spread between lumber and energy
      double totalMass = lumber.wetBiomass + energy.wetBiomass;
      double lumberPart = lumber.wetBiomass / totalMass;
      double energyPart = energy.wetBiomass / totalMass;
      
      energySpread(lumber, lumberPart);
      energySpread(energy, energyPart);
      
    // lumber and energy are output
      outputs.add (lumber);
      outputs.add (energy);

      
    } else if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) &&   branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // lumber log are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
      
    // energyLogs are made in branches and stem top >7cm
      Resource energyLogs = output.copy ();
      energyLogs.processName = name ;
      energyLogs.wetBiomassBr0_4 = 0;
      energyLogs.wetBiomassBr4_7 = 0;
      // energyLogs.wetBiomassBr7_more = 0;
      energyLogs.wetBiomassStem0_7 = 0;
      // energyLogs.wetBiomassStem7_more_top = 0;
      energyLogs.wetBiomassStem7_more_bole = 0;
      energyLogs.wetBiomassLeaves = 0;
      energyLogs.status = ResourceStatus.LOG;
      energyLogs.market = MaterialMarketDestination.ENERGY;
      energyLogs.updateBiomasses (efficiency);
      energyLogs.updateMineralMasses ();
      energyLogs.addProcessInHistory (this);
      
    // energySmalls are made in branches and stem top <7cm
      Resource energySmalls = output.copy ();
      energySmalls.processName = name ;
      // energySmalls.wetBiomassBr0_4 = 0;
      // energySmalls.wetBiomassBr4_7 = 0;
      energySmalls.wetBiomassBr7_more = 0;
      // energySmalls.wetBiomassStem0_7 = 0;
      energySmalls.wetBiomassStem7_more_top = 0;
      energySmalls.wetBiomassStem7_more_bole = 0;
      // energySmalls.wetBiomassLeaves = 0;
      energySmalls.status = ResourceStatus.RESIDUAL;
      energySmalls.market = MaterialMarketDestination.ENERGY;
      energySmalls.updateBiomasses (efficiency);
      energySmalls.updateMineralMasses ();
      energySmalls.addProcessInHistory (this);

    // energy costs are spread between lumber, energyLogs and energySmalls
      double totalMass = lumber.wetBiomass + energyLogs.wetBiomass + energySmalls.wetBiomass;
      double lumberPart = lumber.wetBiomass / totalMass;
      double energyLogsPart = energyLogs.wetBiomass / totalMass;
      double energySmallsPart = energySmalls.wetBiomass / totalMass;
      
      energySpread(lumber, lumberPart);
      energySpread(energyLogs, energyLogsPart);
      energySpread(energySmalls, energySmallsPart);
      
    // lumber energyLogs and energySmalls are output
      outputs.add (lumber);
      outputs.add (energyLogs);
      outputs.add (energySmalls);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) && !branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // lumber logs are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
      
    // whole branches are exploited for energy
      Resource branches = output.copy ();
      branches.processName = name ;
      // branches.wetBiomassBr0_4 = 0;
      // branches.wetBiomassBr4_7 = 0;
      // branches.wetBiomassBr7_more = 0;
      branches.wetBiomassStem0_7 = 0;
      branches.wetBiomassStem7_more_top = 0;
      branches.wetBiomassStem7_more_bole = 0;
      // branches.wetBiomassLeaves = 0;
      branches.status = ResourceStatus.BRANCH;
      branches.market = MaterialMarketDestination.ENERGY;
      branches.updateBiomasses (efficiency);
      branches.updateMineralMasses ();
      branches.addProcessInHistory (this);
      
    // energy costs are spread between lumber and branches
      double totalMass = lumber.wetBiomass + branches.wetBiomass;
      double lumberPart = lumber.wetBiomass / totalMass;
      double branchesPart = branches.wetBiomass / totalMass;
      
      energySpread(lumber, lumberPart);
      energySpread(branches, branchesPart);
      
    // lumber and branches for energy are output
      outputs.add (lumber);
      outputs.add (branches);
     
     
    } else if (stemMarketDestination.equals (MaterialMarketDestination.LUMBER) && !branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ND) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // lumber logs are made in the stem bole
      Resource lumber = output.copy ();
      lumber.processName = name ;
      lumber.wetBiomassBr0_4 = 0;
      lumber.wetBiomassBr4_7 = 0;
      lumber.wetBiomassBr7_more = 0;
      lumber.wetBiomassStem0_7 = 0;
      lumber.wetBiomassStem7_more_top = 0;
      // lumber.wetBiomassStem7_more_bole = 0;
      lumber.wetBiomassLeaves = 0;
      lumber.status = ResourceStatus.LOG;
      lumber.market = MaterialMarketDestination.LUMBER;
      lumber.updateBiomasses (efficiency);
      lumber.updateMineralMasses ();
      lumber.addProcessInHistory (this);
      
    // energy costs do not have to be spread
    // lumber resource is output
      outputs.add (lumber);
      

    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // industry logs are made in the stem and branches >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      // industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // energy costs do not have to be spread
    // industry resource is output
      outputs.add (industry);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // industry logs are made in the stem and branches >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      // industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // smallwoods are exploited for energy
      Resource energy = output.copy ();
      energy.processName = name ;
      // energy.wetBiomassBr0_4 = 0;
      // energy.wetBiomassBr4_7 = 0;
      energy.wetBiomassBr7_more = 0;
      // energy.wetBiomassStem0_7 = 0;
      energy.wetBiomassStem7_more_top = 0;
      energy.wetBiomassStem7_more_bole = 0;
      // energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.RESIDUAL;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy costs are spread between industry and energy
      double totalMass = industry.wetBiomass + energy.wetBiomass;
      double industryPart = industry.wetBiomass / totalMass;
      double energyPart = energy.wetBiomass / totalMass;
      
      energySpread(industry, industryPart);
      energySpread(energy, energyPart);
      
    // industry and energy are output
      outputs.add (industry);
      outputs.add (energy);



    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // industry logs are made in the stem >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // branches >7cm logs are exploited for energy
      Resource energy = output.copy ();
      energy.processName = name ;
      energy.wetBiomassBr0_4 = 0;
      energy.wetBiomassBr4_7 = 0;
      // energy.wetBiomassBr7_more = 0;
      energy.wetBiomassStem0_7 = 0;
      energy.wetBiomassStem7_more_top = 0;
      energy.wetBiomassStem7_more_bole = 0;
      energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.LOG;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy costs are spread between industry and energy
      double totalMass = industry.wetBiomass + energy.wetBiomass;
      double industryPart = industry.wetBiomass / totalMass;
      double energyPart = energy.wetBiomass / totalMass;
      
      energySpread(industry, industryPart);
      energySpread(energy, energyPart);
      
    // industry and energy are output
      outputs.add (industry);
      outputs.add (energy);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // industry logs are made in the stem >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // branches logs >7cm are exploited for energy
      Resource energyLogs = output.copy ();
      energyLogs.processName = name ;
      energyLogs.wetBiomassBr0_4 = 0;
      energyLogs.wetBiomassBr4_7 = 0;
      // energyLogs.wetBiomassBr7_more = 0;
      energyLogs.wetBiomassStem0_7 = 0;
      energyLogs.wetBiomassStem7_more_top = 0;
      energyLogs.wetBiomassStem7_more_bole = 0;
      energyLogs.wetBiomassLeaves = 0;
      energyLogs.status = ResourceStatus.LOG;
      energyLogs.market = MaterialMarketDestination.ENERGY;
      energyLogs.updateBiomasses (efficiency);
      energyLogs.updateMineralMasses ();
      energyLogs.addProcessInHistory (this);
      
    // smallwoods are exploited separately for energy
      Resource energySmalls = output.copy ();
      energySmalls.processName = name ;
      // energySmalls.wetBiomassBr0_4 = 0;
      // energySmalls.wetBiomassBr4_7 = 0;
      energySmalls.wetBiomassBr7_more = 0;
      // energySmalls.wetBiomassStem0_7 = 0;
      energySmalls.wetBiomassStem7_more_top = 0;
      energySmalls.wetBiomassStem7_more_bole = 0;
      // energySmalls.wetBiomassLeaves = 0;
      energySmalls.status = ResourceStatus.RESIDUAL;
      energySmalls.market = MaterialMarketDestination.ENERGY;
      energySmalls.updateBiomasses (efficiency);
      energySmalls.updateMineralMasses ();
      energySmalls.addProcessInHistory (this);
      
    // energy costs are spread between industry energyLogs and energySmalls
      double totalMass = industry.wetBiomass + energyLogs.wetBiomass + energySmalls.wetBiomass;
      double industryPart = industry.wetBiomass / totalMass;
      double energyLogsPart = energyLogs.wetBiomass / totalMass;
      double energySmallsPart = energySmalls.wetBiomass / totalMass;
      
      energySpread(industry, industryPart);
      energySpread(energyLogs, energyLogsPart);
      energySpread(energySmalls, energySmallsPart);
     
    // industry energyLogs and energySmalls are output
      outputs.add (industry);
      outputs.add (energyLogs);
      outputs.add (energySmalls);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && !branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // industry logs are made in the stem >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
     
    // whole branches and stem<7 are exploited for energy
      Resource energy = output.copy ();
      energy.processName = name ;
      // energy.wetBiomassBr0_4 = 0;
      // energy.wetBiomassBr4_7 = 0;
      // energy.wetBiomassBr7_more = 0;
      // energy.wetBiomassStem0_7 = 0;
      energy.wetBiomassStem7_more_top = 0;
      energy.wetBiomassStem7_more_bole = 0;
      // energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.BRANCH;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy costs are spread between industry and energy
      double totalMass = industry.wetBiomass + energy.wetBiomass;
      double industryPart = industry.wetBiomass / totalMass;
      double energyPart = energy.wetBiomass / totalMass;
      
      energySpread(industry, industryPart);
      energySpread(energy, energyPart);
        
    // industry and energy are output
      outputs.add (industry);
      outputs.add (energy);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.INDUSTRY) && !branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ND) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // industry logs are made in the stem >7cm
      Resource industry = output.copy ();
      industry.processName = name ;
      industry.wetBiomassBr0_4 = 0;
      industry.wetBiomassBr4_7 = 0;
      // industry.wetBiomassBr7_more = 0;
      industry.wetBiomassStem0_7 = 0;
      // industry.wetBiomassStem7_more_top = 0;
      // industry.wetBiomassStem7_more_bole = 0;
      industry.wetBiomassLeaves = 0;
      industry.status = ResourceStatus.LOG;
      industry.market = MaterialMarketDestination.INDUSTRY;
      industry.updateBiomasses (efficiency);
      industry.updateMineralMasses ();
      industry.addProcessInHistory (this);
      
    // energy do not have to be spread
    // industry resource is output
      outputs.add (industry);


    } else if (stemMarketDestination.equals (MaterialMarketDestination.ENERGY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ND) ) {
    // energy logs are made in the stem and branches >7cm
      Resource energy = output.copy ();
      energy.processName = name ;
      energy.wetBiomassBr0_4 = 0;
      energy.wetBiomassBr4_7 = 0;
      // energy.wetBiomassBr7_more = 0;
      energy.wetBiomassStem0_7 = 0;
      // energy.wetBiomassStem7_more_top = 0;
      // energy.wetBiomassStem7_more_bole = 0;
      energy.wetBiomassLeaves = 0;
      energy.status = ResourceStatus.LOG;
      energy.market = MaterialMarketDestination.ENERGY;
      energy.updateBiomasses (efficiency);
      energy.updateMineralMasses ();
      energy.addProcessInHistory (this);
      
    // energy do not have to be spread
    // energy resource is output
      outputs.add (energy);

      
    } else if (stemMarketDestination.equals (MaterialMarketDestination.ENERGY) && branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // energy logs are made in the stem and branches >7 cm
      Resource energyLogs = output.copy ();
      energyLogs.processName = name ;
      energyLogs.wetBiomassBr0_4 = 0;
      energyLogs.wetBiomassBr4_7 = 0;
      // energyLogs.wetBiomassBr7_more = 0;
      energyLogs.wetBiomassStem0_7 = 0;
      // energyLogs.wetBiomassStem7_more_top = 0;
      // energyLogs.wetBiomassStem7_more_bole = 0;
      energyLogs.wetBiomassLeaves = 0;
      energyLogs.status = ResourceStatus.LOG;
      energyLogs.market = MaterialMarketDestination.ENERGY;
      energyLogs.updateBiomasses (efficiency);
      energyLogs.updateMineralMasses ();
      energyLogs.addProcessInHistory (this);
      
    // smallWoods are exploited separately as energy
      Resource energySmalls = output.copy ();
      energySmalls.processName = name ;
      // energySmalls.wetBiomassBr0_4 = 0;
      // energySmalls.wetBiomassBr4_7 = 0;
      energySmalls.wetBiomassBr7_more = 0;
      // energySmalls.wetBiomassStem0_7 = 0;
      energySmalls.wetBiomassStem7_more_top = 0;
      energySmalls.wetBiomassStem7_more_bole = 0;
      // energySmalls.wetBiomassLeaves = 0;
      energySmalls.status = ResourceStatus.RESIDUAL;
      energySmalls.market = MaterialMarketDestination.ENERGY;
      energySmalls.updateBiomasses (efficiency);
      energySmalls.updateMineralMasses ();
      energySmalls.addProcessInHistory (this);
      
    // energy costs are spread between energyLogs and energySmalls
      double totalMass = energyLogs.wetBiomass + energySmalls.wetBiomass;
      double energyLogsPart = energyLogs.wetBiomass / totalMass;
      double energySmallsPart = energySmalls.wetBiomass / totalMass;
      
      energySpread(energyLogs, energyLogsPart);
      energySpread(energySmalls, energySmallsPart);
      
    // energyLogs and energySmalls are output
      outputs.add (energyLogs);
      outputs.add (energySmalls);

      
    } else if (stemMarketDestination.equals (MaterialMarketDestination.ENERGY) && !branchesLogging && branchesMarketDestination.equals (MaterialMarketDestination.ENERGY) && smallWoodMarketDestination.equals (MaterialMarketDestination.ENERGY) ) {
    // energy logs are made in the stem >7cm
      Resource energyLogs = output.copy ();
      energyLogs.processName = name ;
      energyLogs.wetBiomassBr0_4 = 0;
      energyLogs.wetBiomassBr4_7 = 0;
      energyLogs.wetBiomassBr7_more = 0;
      energyLogs.wetBiomassStem0_7 = 0;
      // energyLogs.wetBiomassStem7_more_top = 0;
      // energyLogs.wetBiomassStem7_more_bole = 0;
      energyLogs.wetBiomassLeaves = 0;
      energyLogs.status = ResourceStatus.LOG;
      energyLogs.market = MaterialMarketDestination.ENERGY;
      energyLogs.updateBiomasses (efficiency);
      energyLogs.updateMineralMasses ();
      energyLogs.addProcessInHistory (this);
      
    // branches and stem <7cm are exploited separately as energy
      Resource branches = output.copy ();
      branches.processName = name ;
      // branches.wetBiomassBr0_4 = 0;
      // branches.wetBiomassBr4_7 = 0;
      // branches.wetBiomassBr7_more = 0;
      // branches.wetBiomassStem0_7 = 0;
      branches.wetBiomassStem7_more_top = 0;
      branches.wetBiomassStem7_more_bole = 0;
      // branches.wetBiomassLeaves = 0;
      branches.status = ResourceStatus.BRANCH;
      branches.market = MaterialMarketDestination.ENERGY;
      branches.updateBiomasses (efficiency);
      branches.updateMineralMasses ();
      branches.addProcessInHistory (this);
      
    // energy costs are spread between energyLogs and branches
      double totalMass = energyLogs.wetBiomass + branches.wetBiomass;
      double energyLogsPart = energyLogs.wetBiomass / totalMass;
      double branchesPart = branches.wetBiomass / totalMass;
      
      energySpread(energyLogs, energyLogsPart);
      energySpread(branches, branchesPart);
      
    // energyLogs and branches are output
      outputs.add (energyLogs);
      outputs.add (branches);


    } else {
      throw new Exception (this.getClass ().getName () + ": Parameters do not correspond to a realistic scenario, consider to modify either the branchesLogging (= " + branchesLogging + ") or the market destination for branches and smallWood (= " + branchesMarketDestination + " and " + smallWoodMarketDestination + ")");
    }
    
     
  }

  
  // a function to allocate an energy part
  public void energySpread (Resource r, double part) {

    r.machineWorkTime = r.machineWorkTime * part;
    r.humanWorkTime = r.humanWorkTime * part;
    r.fuelConsumption = r.fuelConsumption * part;
    r.oilConsumption = r.oilConsumption * part;
    r.lcConsumption = r.lcConsumption * part;
    r.logisticsConsumption = r.logisticsConsumption * part;
    r.processConsumption = r.processConsumption * part;
    r.chainConsumption += r.processConsumption * part;
        
  }
 
	public String toString () {
		return "HarvestingProcess" 
        + "name :" + name 
        + " LeavesOn:" + LeavesOn
        + " efficiency:" + efficiency
        + " harvestingPerf:" + harvestingPerf
        + " hourlyConsumption:" + hourlyConsumption
        + " machineWheight:" + machineWheight
        + " machineLifetime:" + machineLifetime
				+ " machineToHumanTimeRatio:" + machineToHumanTimeRatio
				+ " machineCarrierNeeded:" + machineCarrierNeeded
        
				+ " stemMarketDestination:" + stemMarketDestination
        + " branchesLogging:" + branchesLogging
        + " branchesMarketDestination:" + branchesMarketDestination
        + " smallWoodMarketDestination:" + smallWoodMarketDestination
        
				+ " meanCarConsumption_kWperkm:" + meanCarConsumption_kWperkm
        + " meanCarrierConsumption_kWperkm:" + meanCarrierConsumption_kWperkm
        + " humanWorkingDayDuration_h:" + humanWorkingDayDuration_h
				+ " contractorDistance_km:" + contractorDistance_km;
	}

}
