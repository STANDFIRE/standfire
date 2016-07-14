package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A transporting working process (transport)
 * 
 * @author N. Bilot - February 2013
 */
public class TransportingProcess extends WorkingProcess {

  private String name;
	private double volumeCapacity; // m3
	private double weightCapacity; // t
  private double hundredkmConsumptionFull_kWh; // kWh/100km
	private double hundredkmConsumptionEmpty_kWh; // kW/100km
  private double unladenWheight; // t
  private double truckLifeDistance; // km
  private double averageSpeed; // km
	private String resourceDestination; // ResourceSite.PLATFORM or ResourceSite.HEATING_PLANT only
  
  //General parameters of the scenario
  private double deliveryDistance_km;

	/**
	 * Constructor.
	 */
	public TransportingProcess (String name, double volumeCapacity, double weightCapacity, double hundredkmConsumptionFull_kWh, double hundredkmConsumptionEmpty_kWh, double unladenWheight, double truckLifeDistance, double averageSpeed, String resourceDestination, double deliveryDistance_km) throws Exception {
		super ("TransportingProcess");

		// Check throws an exception if the condition is false
		check ("volumeCapacity", volumeCapacity >= 0);
		check ("weightCapacity", weightCapacity >= 0);
		check ("hundredkmConsumptionFull_kWh", hundredkmConsumptionFull_kWh >= 0);
		check ("hundredkmConsumptionEmpty_kWh", hundredkmConsumptionEmpty_kWh >= 0);
    check ("unladenWheight", unladenWheight >= 0);
    check ("truckLifeDistance", truckLifeDistance >= 0);
		check ("hundredkmConsumptionEmpty_kWh", hundredkmConsumptionEmpty_kWh <= hundredkmConsumptionFull_kWh);
    check ("averageSpeed", averageSpeed >= 0);    
		check ("resourceDestination", resourceDestination.equals (ResourceSite.PLATFORM)
				|| resourceDestination.equals (ResourceSite.HEATING_PLANT));
		check ("deliveryDistance_km", deliveryDistance_km >= 0);

		this.name = name;
    this.volumeCapacity = volumeCapacity;
		this.weightCapacity = weightCapacity;
    this.hundredkmConsumptionFull_kWh = hundredkmConsumptionFull_kWh;
		this.hundredkmConsumptionEmpty_kWh = hundredkmConsumptionEmpty_kWh;
    this.unladenWheight = unladenWheight;
    this.truckLifeDistance = truckLifeDistance;
    this.averageSpeed = averageSpeed;
		this.resourceDestination = resourceDestination;
		this.deliveryDistance_km = deliveryDistance_km;

		// What resource can be processed
		addCompatibleStatusOrSite (ResourceStatus.FALLEN_TREE);
		addCompatibleStatusOrSite (ResourceStatus.LOG);
		addCompatibleStatusOrSite (ResourceStatus.RESIDUAL);
		addCompatibleStatusOrSite (ResourceStatus.BUNDLE);
		addCompatibleStatusOrSite (ResourceStatus.BRANCH);
		addCompatibleStatusOrSite (ResourceStatus.CHIP);
		addCompatibleStatusOrSite (ResourceSite.ROADSIDE);
		addCompatibleStatusOrSite (ResourceSite.PLATFORM);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public TransportingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");
		
    String wpName = name;
    double volumeCapacity = doubleValue (st.nextToken ());
		double weightCapacity = doubleValue (st.nextToken ());
    double hundredkmConsumptionFull_kWh = doubleValue (st.nextToken ());
		double hundredkmConsumptionEmpty_kWh = doubleValue (st.nextToken ());
    double unladenWheight = doubleValue (st.nextToken ());
    double truckLifeDistance = doubleValue (st.nextToken ());
    double averageSpeed = doubleValue (st.nextToken ());
		String resourceDestination = st.nextToken ();
		if (resourceDestination.equals ("PLATFORM")) 
			resourceDestination = ResourceSite.PLATFORM;
		else if (resourceDestination.equals ("HEATING_PLANT")) 
			resourceDestination = ResourceSite.HEATING_PLANT;
		else 
			throw new Exception ("TransportingProcess: wrong value for resourceDestination: "+resourceDestination+", must be PLATFORM or HEATING_PLANT");
		
    // 4 scenario parameters unused for this class
    double meanCarConsumption_kWperkm  = doubleValue (st.nextToken ());
    double meanCarrierConsumption_kWperkm = doubleValue (st.nextToken ());
    double humanWorkingDayDuration_h = doubleValue (st.nextToken ());
		double contractorDistance_km = doubleValue (st.nextToken ());
		
    double deliveryDistance_km = doubleValue (st.nextToken ());
    
		return new TransportingProcess (name, volumeCapacity, weightCapacity, hundredkmConsumptionFull_kWh,hundredkmConsumptionEmpty_kWh, unladenWheight, truckLifeDistance, averageSpeed, resourceDestination, deliveryDistance_km);
	}

	/**
	 * Run the process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: same status, site: ROADSIDE
		Resource output = input.copy ();

    output.processName = name ;
		output.site = resourceDestination;

		// Efficiency
		double efficiency = 1;

		double bulkDensity = 0;
		if (input.status.equals (ResourceStatus.FALLEN_TREE)) {
			bulkDensity = 0.4;
		} else if (input.status.equals (ResourceStatus.LOG)) {
			bulkDensity = 0.8;
		} else if (input.status.equals (ResourceStatus.RESIDUAL)) {
			bulkDensity = 0.3;
		} else if (input.status.equals (ResourceStatus.BUNDLE)) {
			bulkDensity = 0.8;
		} else if (input.status.equals (ResourceStatus.BRANCH)) {
			bulkDensity = 0.4;
		} else if (input.status.equals (ResourceStatus.CHIP)) {
			bulkDensity = 0.3;
		}

		double oneTripBiomass = 0;
		if (bulkDensity > weightCapacity / volumeCapacity) {
			oneTripBiomass = weightCapacity;
		} else {
			oneTripBiomass = volumeCapacity * bulkDensity;
		}
		int numberOfJourneys = (int) Math.ceil (input.wetBiomass / oneTripBiomass);
    double transportTime_h = (numberOfJourneys * 2 * deliveryDistance_km) / averageSpeed  ;
    
		// Consumptions
    // fuel consumption
		double fuelConsumption  = ( hundredkmConsumptionFull_kWh * numberOfJourneys * deliveryDistance_km / 100 ) + ( hundredkmConsumptionEmpty_kWh * numberOfJourneys * deliveryDistance_km / 100 );
    
    // 2. Oil consumption
    double oilConsumption = 0.0008 * fuelConsumption;
    
    // 3. Life cycle consumption equivalent
    double lcConsumption_pert = 16556;
    double lcConsumption_perkm = unladenWheight * lcConsumption_pert / truckLifeDistance;
    double lcConsumption = lcConsumption_perkm * deliveryDistance_km;
    
    // 4. Logistics' consumption
    // In this case, the operator comes to the site with the machine, so there is no logistics comsumption
    double logisticsConsumption = 0;
    
    // 5. TOTAL consumption for the process
    double processConsumption = fuelConsumption + oilConsumption + lcConsumption + logisticsConsumption;

		
    

		// Update the output resource
		output.machineWorkTime = transportTime_h;
    output.humanWorkTime = transportTime_h;
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
		return "TransportingProcess" 
        + "name :" + name 
        + " volumeCapacity:" + volumeCapacity 
				+ " weightCapacity:" + weightCapacity
				+ " hundredkmConsumptionFull_kWh:" + hundredkmConsumptionFull_kWh
        + " hundredkmConsumptionEmpty_kWh:" + hundredkmConsumptionEmpty_kWh
        + " unladenWheight:" + unladenWheight
        + " truckLifeDistance:" + truckLifeDistance
        + " averageSpeed:" + averageSpeed
				+ " resourceDestination:" + resourceDestination
				+ " deliveryDistance_km:" + deliveryDistance_km;
	}

}
