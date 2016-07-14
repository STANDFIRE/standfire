package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A forwarding working process (débardeur, porteur)
 * 
 * @author N. Bilot - February 2013
 */
public class ForwardingProcess extends WorkingProcess {

  private String name;
	private double efficiency; // ratio
  private double forwardingPerf; // t/h
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
	 * Constructor.
	 */
	public ForwardingProcess (String name, double efficiency, double forwardingPerf, double hourlyConsumption, double machineWheight, double machineLifetime, double machineToHumanTimeRatio, boolean machineCarrierNeeded, double meanCarConsumption_kWperkm, double meanCarrierConsumption_kWperkm, double humanWorkingDayDuration_h, double contractorDistance_km) throws Exception {
		super ("ForwardingProcess");

		// Check throws an exception if the condition is false
		check ("efficiency", efficiency >= 0);
    check ("forwardingPerf", forwardingPerf >= 0);
		check ("hourlyConsumption", hourlyConsumption >= 0);
		check ("machineWheight", machineWheight >= 0);
    check ("machineLifetime", machineLifetime >= 0);
		check ("machineToHumanTimeRatio", machineToHumanTimeRatio >= 0);
    check ("meanCarConsumption_kWperkm", meanCarConsumption_kWperkm >= 0);
    check ("meanCarrierConsumption_kWperkm", meanCarrierConsumption_kWperkm >= 0);
    check ("humanWorkingDayDuration_h", humanWorkingDayDuration_h >= 0);
		check ("contractorDistance_km", contractorDistance_km >= 0);

		this.name = name;
		this.efficiency = efficiency;
    this.forwardingPerf = forwardingPerf;
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
		addCompatibleStatusOrSite (ResourceStatus.LOG);
		addCompatibleStatusOrSite (ResourceStatus.RESIDUAL);
		addCompatibleStatusOrSite (ResourceStatus.BUNDLE);
		addCompatibleStatusOrSite (ResourceStatus.BRANCH);
		addCompatibleStatusOrSite (ResourceSite.PLOT);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public ForwardingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");
		
    String wpName = name;
		double efficiency = doubleValue (st.nextToken ());
    double forwardingPerf = doubleValue (st.nextToken ());
    double hourlyConsumption = doubleValue (st.nextToken ());
		double machineWheight = doubleValue (st.nextToken ());
    double machineLifetime = doubleValue (st.nextToken ());
		double machineToHumanTimeRatio = doubleValue (st.nextToken ());
		boolean machineCarrierNeeded = booleanValue (st.nextToken ());
    
		double meanCarConsumption_kWperkm  = doubleValue (st.nextToken ());
    double meanCarrierConsumption_kWperkm = doubleValue (st.nextToken ());
    double humanWorkingDayDuration_h = doubleValue (st.nextToken ());
		double contractorDistance_km = doubleValue (st.nextToken ());
		
		return new ForwardingProcess (name, efficiency, forwardingPerf, hourlyConsumption, machineWheight, machineLifetime, machineToHumanTimeRatio, machineCarrierNeeded, meanCarConsumption_kWperkm, meanCarrierConsumption_kWperkm, humanWorkingDayDuration_h, contractorDistance_km);
	}

	/**
	 * Run the forwarding process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: same status, site: ROADSIDE
		Resource output = input.copy ();
    output.processName = name ;

		output.site = ResourceSite.ROADSIDE;

		// Efficiency taken into account before treatment because only the actually forwarded biomass is considered in the process
    output.updateBiomasses (efficiency);


    // Time
    double forwardingTime_h = output.wetBiomass / forwardingPerf ;
		
		// Consumptions
		// 1. fuelConsumption
      double engineEfficiency = 0.35;
    
      double fuelConsumption = hourlyConsumption * forwardingTime_h ;
      
    // 2. Oil consumption
      double oilCoefficient = 0.033;
      double oilConsumption = fuelConsumption * oilCoefficient;
      
    // 3. Life Cycle consumption equivalent
      double lcConsumption_pert = 16556; // kWh/t
      double lcConsumption_perh = machineWheight * lcConsumption_pert / machineLifetime;
      double lcConsumption = lcConsumption_perh * forwardingTime_h;
      
    // 4. Logistics consumption
      // a. operatorTravelConsumption
      double humanProductiveWorkTime = forwardingTime_h * machineToHumanTimeRatio;
      int humanProductiveWorkTime_day = (int) Math.ceil (humanProductiveWorkTime / humanWorkingDayDuration_h);
      double operatorTravelConsumption = contractorDistance_km * 2d * meanCarConsumption_kWperkm * humanProductiveWorkTime_day; // kWh

      //  b. machineTravelConsumption
		double machineTravelConsumption = machineCarrierNeeded ? contractorDistance_km * 2d * meanCarrierConsumption_kWperkm : 0d; // kWh

    double logisticsConsumption = operatorTravelConsumption + machineTravelConsumption;
    
    // PROCESS CONSUMPTION
    double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;
    
		// Update the output resource
		output.machineWorkTime = forwardingTime_h;
    output.humanWorkTime = humanProductiveWorkTime;
    output.fuelConsumption = fuelConsumption;
    output.oilConsumption = oilConsumption;
    output.lcConsumption = lcConsumption;
    output.logisticsConsumption = logisticsConsumption;
    output.processConsumption = processConsumption;
    output.chainConsumption += processConsumption;
		output.updateMineralMasses ();
		output.addProcessInHistory (this);

		outputs.add (output);

	}

	public String toString () {
		return "ForwardingProcess" 
        + " name :" + name 
        + " efficiency:" + efficiency
        + " forwardingPerf:" + forwardingPerf
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
