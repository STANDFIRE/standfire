package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.MaterialMarketDestination;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A felling working process
 * 
 * @author N. Bilot - February 2013
 */
public class FellingProcess extends WorkingProcess {

  private String name;
	private boolean LeavesOn; // boolean
  private double efficiency; // ratio
  private double fellingPerf; // t/h
  private double hourlyConsumption; // kW
  private String wholeTreeMarketDestination; // "ND", "ENERGY", "INDUSTRY", or "LUMBER
  private double machineWheight; // t
  private double machineLifetime; // h
  private double machineToHumanTimeRatio; // e.g. 1.5
	private boolean machineCarrierNeeded;
  
  //General parameters of the scenario
  private double meanCarConsumption_kWperkm;
  private double meanCarrierConsumption_kWperkm;
  private double humanWorkingDayDuration_h;
	private double contractorDistance_km;

  
	/**
	 * Constructor (Abatteuse simple, Tronconneur ou Abatteuse groupeuse). 
	 */
	public FellingProcess (String name, boolean LeavesOn, double efficiency, double fellingPerf, double hourlyConsumption, String wholeTreeMarketDestination, double machineWheight, double machineLifetime, double machineToHumanTimeRatio, boolean machineCarrierNeeded, double meanCarConsumption_kWperkm, double meanCarrierConsumption_kWperkm, double humanWorkingDayDuration_h, double contractorDistance_km) throws Exception {
		super ("FellingProcess");

		// Check throws an exception if the condition is false
    check ("efficiency", efficiency >= 0);
    check ("efficiency", efficiency <= 1);
    check ("fellingPerf", fellingPerf >= 0);
		check ("hourlyConsumption", hourlyConsumption >= 0);
    check ("wholeTreeMarketDestination", wholeTreeMarketDestination.equals (MaterialMarketDestination.ND) || wholeTreeMarketDestination.equals (MaterialMarketDestination.ENERGY) || wholeTreeMarketDestination.equals (MaterialMarketDestination.INDUSTRY) || wholeTreeMarketDestination.equals (MaterialMarketDestination.LUMBER));
    check ("machineWheight", machineWheight >= 0);
    check ("machineLifetime", machineLifetime >= 0);
		check ("machineToHumanTimeRatio", machineToHumanTimeRatio >= 0);
    check ("meanCarConsumption_kWperkm", meanCarConsumption_kWperkm >= 0);
    check ("meanCarrierConsumption_kWperkm", meanCarrierConsumption_kWperkm >= 0);
    check ("humanWorkingDayDuration_h", humanWorkingDayDuration_h >= 0);
		check ("contractorDistance_km", contractorDistance_km >= 0);

		this.name = name;
		this.LeavesOn = LeavesOn;
    this.fellingPerf = fellingPerf;
    this.efficiency = efficiency;
		this.hourlyConsumption = hourlyConsumption;
    this.wholeTreeMarketDestination = wholeTreeMarketDestination;
    this.machineWheight = machineWheight;
    this.machineLifetime = machineLifetime;
		this.machineToHumanTimeRatio = machineToHumanTimeRatio;
		this.machineCarrierNeeded = machineCarrierNeeded;
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
	static public FellingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");

    String wpName = name;
		boolean LeavesOn = booleanValue (st.nextToken ());
    double efficiency = doubleValue (st.nextToken ());
    double fellingPerf = doubleValue (st.nextToken ());
		double hourlyConsumption = doubleValue (st.nextToken ());
    String wholeTreeMarketDestination = st.nextToken ();
    double machineWheight = doubleValue (st.nextToken ());
    double machineLifetime = doubleValue (st.nextToken ());
		double machineToHumanTimeRatio = doubleValue (st.nextToken ());
		boolean machineCarrierNeeded = booleanValue (st.nextToken ());
    double meanCarConsumption_kWperkm  = doubleValue (st.nextToken ());
    double meanCarrierConsumption_kWperkm = doubleValue (st.nextToken ());
    double humanWorkingDayDuration_h = doubleValue (st.nextToken ());
		double contractorDistance_km = doubleValue (st.nextToken ());

		return new FellingProcess (name, LeavesOn, efficiency, fellingPerf, hourlyConsumption, wholeTreeMarketDestination, machineWheight, machineLifetime, machineToHumanTimeRatio,	machineCarrierNeeded, meanCarConsumption_kWperkm, meanCarrierConsumption_kWperkm, humanWorkingDayDuration_h, contractorDistance_km);
	}

	/**
	 * Run the felling process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: status FALLEN_TREE
		Resource output = input.copy ();
    output.processName = name ;

		output.status = ResourceStatus.FALLEN_TREE;
    // If the whole tree is destinated to energy, the market value is actualized. Else, this value is let to its default value ("ND")
    if (wholeTreeMarketDestination.equals (MaterialMarketDestination.ENERGY)){
      output.market = MaterialMarketDestination.ENERGY;
    }

		// Do the trees have leaves at the moment of the felling ?
    if(!LeavesOn){
      output.wetBiomassLeaves = 0;
      output.updateBiomasses (efficiency);
      output.updateMineralMasses ();
    }

    // Time
    double fellingTime_h = output.wetBiomass / fellingPerf ;
    
		// Consumptions
		// 1. Fuel consumption
    double fuelConsumption = hourlyConsumption * fellingTime_h ;
		
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
    double lcConsumption = lcConsumption_perh * fellingTime_h;
    
    // 4. Logistics consumption
      // a. operatorTravelConsumption
      double humanProductiveWorkTime = fellingTime_h * machineToHumanTimeRatio;
      int humanProductiveWorkTime_day = (int) Math.ceil (humanProductiveWorkTime / humanWorkingDayDuration_h);
      double operatorTravelConsumption = contractorDistance_km * 2d * meanCarConsumption_kWperkm * humanProductiveWorkTime_day; // kWh

      //  b. machineTravelConsumption
      double machineTravelConsumption = machineCarrierNeeded ? contractorDistance_km * 2d * meanCarrierConsumption_kWperkm : 0d; // kWh
      
    double logisticsConsumption = operatorTravelConsumption + machineTravelConsumption;
    
    // PROCESS CONSUMPTION
    double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;
    
		// Update the output resource
		output.machineWorkTime = fellingTime_h;
    output.humanWorkTime = humanProductiveWorkTime;
    output.fuelConsumption = fuelConsumption;
    output.oilConsumption = oilConsumption;
    output.lcConsumption = lcConsumption;
    output.logisticsConsumption = logisticsConsumption;
    output.processConsumption = processConsumption;
    output.chainConsumption += processConsumption;
    
		output.updateBiomasses (efficiency);
		output.updateMineralMasses ();
		output.addProcessInHistory (this);

		outputs.add (output);

	}

	public String toString () {
		return "FellingProcess\n" 
        + " name :" + name 
        + "; LeavesOn:" + LeavesOn 
        + "; efficiency:" + efficiency
        + "; fellingPerf:" + fellingPerf
				+ "; hourlyConsumption:" + hourlyConsumption
        + "; wholeTreeMarketDestination:" + wholeTreeMarketDestination
        + "; machineWheight:" + machineWheight
        + "; machineLifetime:" + machineLifetime
				+ "; machineToHumanTimeRatio:" + machineToHumanTimeRatio
				+ "; machineCarrierNeeded:" + machineCarrierNeeded
        + "; meanCarConsumption_kWperkm:" + meanCarConsumption_kWperkm
        + "; meanCarrierConsumption_kWperkm:" + meanCarrierConsumption_kWperkm
        + "; humanWorkingDayDuration_h:" + humanWorkingDayDuration_h
				+ "; contractorDistance_km:" + contractorDistance_km + ".";
	}

}
