package capsis.lib.forenerchips;

import java.util.ArrayList;
import java.util.List;

/**
 * A Forenerchips methode
 * 
 * @author N. Bilot - February 2013
 */
public class FecMethod {


	/**
	 * A step in the method
	 */
	private static class Step {
		public WorkingProcess previousProcess;
		public String materialMarketDestination;
    public String materialStatus;
		public WorkingProcess nextProcess;
		public Step (WorkingProcess previousProcess, String materialMarketDestination, String materialStatus, WorkingProcess nextProcess) {
			super ();
			this.previousProcess = previousProcess;
			this.materialMarketDestination = materialMarketDestination;
      this.materialStatus = materialStatus;
      this.nextProcess = nextProcess;
		}
	}
	
	private String name;
	
	private WorkingProcess initialProcess;
	
	private List<Step> list;
	
	/**
	 * Constructor.
	 */
	public FecMethod (String name) {
		this.name = name;
		list = new ArrayList<Step> ();
	}

	public void setInitialProcess (WorkingProcess initialProcess) {
		this.initialProcess = initialProcess;
	}
	
	
	/**
	 * Add a process in the ForEnerChips method. Next process will be run on the given output (status) of the given previous process. This is the way to chain processes in ForEnerChips.
	 */
	public void addProcess (WorkingProcess previousProcess, String materialMarketDestination, String materialStatus, WorkingProcess nextProcess) {
		list.add (new Step (previousProcess, materialMarketDestination, materialStatus, nextProcess));
	}

	
	/**
	 * Returns the next process to be run on the given output (status) of the given previous
	 * process. If nothing to be run, returns null.
	 */
	public WorkingProcess getProcess (WorkingProcess previousProcess, String materialMarketDestination, String materialStatus) throws Exception {

		for (Step step : list) {
			if (step.previousProcess.equals (previousProcess) 
					&& step.materialMarketDestination.equals (materialMarketDestination) && step.materialStatus.equals (materialStatus)) {
				return step.nextProcess;
			}
		}
		
		return null;
		
	}

	
	public String getName () {
		return name;
	}

	
	public WorkingProcess getInitialProcess () {
		return initialProcess;
	}

	public String toString () {
		List<WorkingProcess> wps = new ArrayList<WorkingProcess> ();
		wps.add (initialProcess);
		StringBuffer b = new StringBuffer ("Method information: "+name+"\n");
		
		b.append ("  WorkingProcess sequence: \n    Initial process: "+initialProcess.name+"\n");
		for (Step step : list) {
			b.append ("    " + step.previousProcess.name+" > " + step.materialMarketDestination + " (" + step.materialStatus+") > " + step.nextProcess.name + "\n");
			wps.add (step.nextProcess);
		}
		
		b.append ("  WorkingProcesses configuration: \n");
		for (WorkingProcess wp : wps) {
			b.append ("    " + wp + "\n");
		}
		
		return b.toString ();
	}
}
