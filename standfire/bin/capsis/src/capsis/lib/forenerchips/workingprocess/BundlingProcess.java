package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;

import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A bundling working process (Fagoteuse)
 * 
 * @author N. Bilot - April 2013
 */
public class BundlingProcess extends WorkingProcess {

  private String name;
	private double bundlingPerf; // t/h
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
	private double contractorDistance_km; // km

	/**
	 * Constructor
	 */
	public BundlingProcess (String name, double bundlingPerf, double efficiency, double hourlyConsumption, double machineWheight, double machineLifetime, double machineToHumanTimeRatio,	boolean machineCarrierNeeded, double meanCarConsumption_kWperkm, double meanCarrierConsumption_kWperkm, double humanWorkingDayDuration_h, double contractorDistance_km) throws Exception {
		super ("BundlingProcess");

		// Check throws an exception if the condition is false
		check ("bundlingPerf", bundlingPerf >= 0);
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
		this.bundlingPerf = bundlingPerf;
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
		addCompatibleStatusOrSite (ResourceSite.PLOT);
		addCompatibleStatusOrSite (ResourceSite.ROADSIDE);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public BundlingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");

    String wpName = name;
		double bundlingPerf = doubleValue (st.nextToken ());
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
		return new BundlingProcess (name, bundlingPerf, efficiency, hourlyConsumption, machineWheight, machineLifetime, machineToHumanTimeRatio, machineCarrierNeeded, meanCarConsumption_kWperkm, meanCarrierConsumption_kWperkm, humanWorkingDayDuration_h, contractorDistance_km);
	}

	/**
	 * Run the bundling process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: status BUNDLE
		Resource output = input.copy ();
    output.processName = name ;

		output.status = ResourceStatus.BUNDLE;

		// Efficiency
		double efficiency = 1;

		// Consumption
		// 1. Fuel consumption
    double engineEfficiency = 0.35;
		double bundlingTime_h = input.wetBiomass / bundlingPerf;

		double fuelConsumption =  hourlyConsumption * bundlingTime_h;
    
    // 2. Oil consumption
    double oilCoefficient = 0.033;
    double oilConsumption = fuelConsumption * oilCoefficient;
    
    // 3. Life Cycle consumption equivalent
    double lcConsumption_pert = 16556;
    double lcConsumption_perh = machineWheight * lcConsumption_pert / machineLifetime;
    double lcConsumption = lcConsumption_perh * bundlingTime_h;
        
    // 4. Logistics consumption
    //  a. operatorTravelConsumption
		double humanProductiveWorkTime = bundlingTime_h * machineToHumanTimeRatio;
		int humanProductiveWorkTime_day = (int) Math.ceil (humanProductiveWorkTime / humanWorkingDayDuration_h);
		double operatorTravelConsumption = contractorDistance_km * 2d * meanCarConsumption_kWperkm * humanProductiveWorkTime_day; // kWh

		//  b. machineTravelConsumption
		double machineTravelConsumption = machineCarrierNeeded ? contractorDistance_km * 2d * meanCarrierConsumption_kWperkm : 0d; // kWh

    double logisticsConsumption = operatorTravelConsumption + machineTravelConsumption;
    
    // PROCESS CONSUMPTION
		double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;

		// Update the output resource
		output.machineWorkTime = bundlingTime_h;
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
		return "BundlingProcess" 
        + "name :" + name 
        + " bundlingPerf:" + bundlingPerf 
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
