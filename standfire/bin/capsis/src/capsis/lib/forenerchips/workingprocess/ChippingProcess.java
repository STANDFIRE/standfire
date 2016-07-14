
package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A chipping working process (broyeuse)
 * 
 * @author N. Bilot - April 2013
 */
public class ChippingProcess extends WorkingProcess {

  private String name;
	private double chippingPerf; // t/h
  private double efficiency; // ratio
  private double hourlyConsumption; // kW
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
	 * Constructor
	 */
	public ChippingProcess (String name, double chippingPerf, double efficiency, double hourlyConsumption, double machineWheight, double machineLifetime, double machineToHumanTimeRatio, boolean machineCarrierNeeded, double meanCarConsumption_kWperkm, double meanCarrierConsumption_kWperkm, double humanWorkingDayDuration_h, double contractorDistance_km) throws Exception {
		super ("ChippingProcess");

		// Check throws an exception if the condition is false
		check ("chippingPerf", chippingPerf >= 0);
    check ("efficiency", efficiency >= 0);
    check ("hourlyConsumption", hourlyConsumption >= 0);
    check ("machineWheight", machineWheight >= 0);
    check ("machineLifetime", machineLifetime >= 0);
		check ("machineToHumanTimeRatio", machineToHumanTimeRatio >= 0);
    check ("meanCarConsumption_kWperkm", meanCarConsumption_kWperkm >= 0);
    check ("meanCarrierConsumption_kWperkm", meanCarrierConsumption_kWperkm >= 0);
    check ("humanWorkingDayDuration_h", humanWorkingDayDuration_h >= 0);
		check ("contractorDistance_km", contractorDistance_km >= 0);

		this.name = name;
		this.chippingPerf = chippingPerf;
    this.efficiency = efficiency;
    this.hourlyConsumption = hourlyConsumption;
    this.machineWheight = machineWheight;
    this.machineLifetime = machineLifetime;		
		this.machineToHumanTimeRatio = machineToHumanTimeRatio;
		this.machineCarrierNeeded = machineCarrierNeeded;
    this.meanCarConsumption_kWperkm = meanCarConsumption_kWperkm;
    this.meanCarrierConsumption_kWperkm = meanCarrierConsumption_kWperkm;
    this.humanWorkingDayDuration_h = humanWorkingDayDuration_h;
		this.contractorDistance_km = contractorDistance_km;
		
		// What resource can be processed
		addCompatibleStatusOrSite (ResourceStatus.FALLEN_TREE);
		addCompatibleStatusOrSite (ResourceStatus.BRANCH);
		addCompatibleStatusOrSite (ResourceStatus.RESIDUAL);
		addCompatibleStatusOrSite (ResourceStatus.LOG);
		addCompatibleStatusOrSite (ResourceStatus.BUNDLE);
		addCompatibleStatusOrSite (ResourceSite.PLOT);
		addCompatibleStatusOrSite (ResourceSite.ROADSIDE);
		addCompatibleStatusOrSite (ResourceSite.PLATFORM);
		addCompatibleStatusOrSite (ResourceSite.HEATING_PLANT);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public ChippingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");

    String wpName = name;
		double chippingPerf = doubleValue (st.nextToken ());
    double efficiency = doubleValue (st.nextToken ());
    double hourlyConsumption = doubleValue (st.nextToken ());
    double machineWheight = doubleValue (st.nextToken ());
    double machineLifetime = doubleValue (st.nextToken ());
		double machineToHumanTimeRatio = doubleValue (st.nextToken ());
		boolean machineCarrierNeeded = booleanValue (st.nextToken ());
    double meanCarConsumption_kWperkm  = doubleValue (st.nextToken ());
    double meanCarrierConsumption_kWperkm = doubleValue (st.nextToken ());
    double humanWorkingDayDuration_h = doubleValue (st.nextToken ());
		double contractorDistance_km = doubleValue (st.nextToken ());
		
		return new ChippingProcess (name, chippingPerf, efficiency, hourlyConsumption, machineWheight, machineLifetime, machineToHumanTimeRatio, machineCarrierNeeded, meanCarConsumption_kWperkm, meanCarrierConsumption_kWperkm, humanWorkingDayDuration_h, contractorDistance_km);
	}

	/**
	 * Run the felling process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: status CHIP
		Resource output = input.copy ();
    output.processName = name ;

		output.status = ResourceStatus.CHIP;

		// Consumptions
		// 1. Fuel consumption
    double engineEfficiency = 0.35;
		double chippingTime_h = input.wetBiomass / chippingPerf; 
		double fuelConsumption = hourlyConsumption * chippingTime_h;

    // 2. Oil consumption
    double oilCoefficient = 0.02;
    double oilConsumption = fuelConsumption * oilCoefficient;
    
    // 3. Life Cycle consumption equivalent
    double lcConsumption_pert = 16556;
    double lcConsumption_perh = machineWheight * lcConsumption_pert / machineLifetime;
    double lcConsumption = lcConsumption_perh * chippingTime_h;
        
    // 4. Logistics consumption
      //  a. operatorTravelConsumption
      double humanProductiveWorkTime = chippingTime_h * machineToHumanTimeRatio;
      int humanProductiveWorkTime_day = (int) Math.ceil (humanProductiveWorkTime / humanWorkingDayDuration_h);
      double operatorTravelConsumption = contractorDistance_km * 2d * meanCarConsumption_kWperkm * humanProductiveWorkTime_day; // kWh

      //  b. machineTravelConsumption
      double machineTravelConsumption = machineCarrierNeeded ? contractorDistance_km * 2d * meanCarrierConsumption_kWperkm : 0d; // kWh

    double logisticsConsumption = operatorTravelConsumption + machineTravelConsumption;
    
    // PROCESS CONSUMPTION
		double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;

		// Update the output resource
    output.machineWorkTime = chippingTime_h;
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
		return "ChippingProcess" 
        + "name :" + name 
        + " chippingPerf:" + chippingPerf 
        + " efficiency:" + efficiency
        + " hourlyConsumption:" + hourlyConsumption
        + " machineWheight:" + machineWheight
        + " machineLifetime:" + machineLifetime
				+ " machineToHumanTimeRatio:" + machineToHumanTimeRatio 
        + " machineCarrierNeeded:" + machineCarrierNeeded 
        + " meanCarConsumption_kWperkm:" + meanCarConsumption_kWperkm
        + " meanCarrierConsumption_kWperkm:" + meanCarrierConsumption_kWperkm
        + " humanWorkingDayDuration_h:" + humanWorkingDayDuration_h
        + " contractorDistance_km:" + contractorDistance_km;
	}

}
