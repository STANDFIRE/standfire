package capsis.lib.forenerchips;

import java.util.ArrayList;
//import java.util.List;

/**
 * A Forenerchips scenario
 * 
 * @author N. Bilot - October 2014
 */
public class FecScenario {

	/**
	 * Several general parameters for the scenario
	 */
  public double plotArea_ha;
  public double meanCarConsumption_kWperkm;
  public double meanCarrierConsumption_kWperkm;
  public double humanWorkingDayDuration_h;
  public double contractorDistance_km;
  public double deliveryDistance_km;
  public String scenarioParameters;
  
  
	/**
	 * A phase in the scenario
	 */
	public static class Phase {
    public int phaseNumber;
    public double beginDg;
    public double finalDg;
    public int numberOfFirstMethodUses;
		public String firstMethodName;
    public String secondMethodName;
    
		public Phase (
      int phaseNumber, 
      double beginDg, 
      double finalDg, 
      int numberOfFirstMethodUses, 
      String firstMethodName, 
      String secondMethodName
    ) {
		  super ();
			this.phaseNumber = phaseNumber;
      this.beginDg = beginDg;
      this.finalDg = finalDg;
      this.numberOfFirstMethodUses = numberOfFirstMethodUses;
			this.firstMethodName = firstMethodName;
      this.secondMethodName = secondMethodName;
		}
	}
	
	private String name;
	
	private Phase initialPhase;
	
	public ArrayList<Phase> phases;
	
	/**
	 * Constructor.
	 */
	public FecScenario (String name) {
		this.name = name;
		phases = new ArrayList<Phase> ();
	}

	public void setInitialPhase (Phase initialPhase) {
		this.initialPhase = initialPhase;
	}
	
	
	/**
	 * Add a phase in the ForEnerChips scenario. 
   * Next phase will be run when Dg reaches finalDg value.
   * This is the way to chain phases in ForEnerChips.
	 */
	public void addPhase (
    int phaseNumber, 
    double beginDg, 
    double finalDg, 
    int numberOfFirstMethodUses, 
    String firstMethodName, 
    String secondMethodName
  ) {
		phases.add (new Phase (phaseNumber, beginDg, finalDg, numberOfFirstMethodUses, firstMethodName, secondMethodName));
	}

	
	
	// public String toString () {
		// List<WorkingProcess> wps = new ArrayList<WorkingProcess> ();
		// wps.add (initialProcess);
		// StringBuffer b = new StringBuffer ("Method information: "+name+"\n");
		// b.append ("  Initial process: "+initialProcess.name+"\n");
		// for (Step step : list) {
			// b.append ("    "+step.previousProcess.name+" > "+step.materialMarketDestination+" > "+step.process.name+"\n");
			// wps.add (step.process);
		// }
		// b.append ("  WorkingProcesses configuration: \n");
		// for (WorkingProcess wp : wps) {
			// b.append ("    "+wp+"\n");
		// }
		// return b.toString ();
	// }
}
