package capsis.lib.forenerchips;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import capsis.lib.forenerchips.workingprocess.WorkingProcessFactory;
import capsis.lib.forenerchips.MaterialMarketDestination;
import capsis.lib.forenerchips.ResourceStatus;


/**
 * Loads a ForEnerChips methode from a text file.
 * 
 * @author N. Bilot, F. de Coligny - April 2013
 */
public class FecMethodLoader extends RecordSet {

	// A record for a ForEnerChips WorkingProcess
	@Import
	static public class WorkingProcessRecord extends Record {

		public WorkingProcessRecord () {
			super ();
		}

		public WorkingProcessRecord (String line) throws Exception {
			super (line);
		}

		public String name;
		public String klass;
		public String parameters;
	}


	// A record for a FecMethod step
	@Import
	static public class MethodStepRecord extends Record {

		public MethodStepRecord () {
			super ();
		}

		public MethodStepRecord (String line) throws Exception {
			super (line);
		}

		public String methodName;
		public String previousProcess;
		public String materialMarketDestination;
    public String materialStatus;
		public String nextProcess;
	}

	private String fileName;
//	private String methodName;
	private Map<String,WorkingProcess> wpMap; // name -> WorkingProcess

	/**
	 * Constructor
	 */
	public FecMethodLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
		wpMap = new HashMap<String,WorkingProcess> ();
	}

	/**
	 * File interpretation
	 */
	public FecMethod interpret (String methodName, String scenarioParameters) throws Exception {

		FecMethod method = null;
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof WorkingProcessRecord) {
				WorkingProcessRecord r = (WorkingProcessRecord) record;
					
				// Create and store the wps
				WorkingProcess wp = WorkingProcessFactory.createWorkingProcess (r.name, r.klass, r.parameters + scenarioParameters);
				wpMap.put (r.name, wp);

			} else if (record instanceof MethodStepRecord) {
				MethodStepRecord r = (MethodStepRecord) record;

				// First step, if the InitialProcess has not been set: initialize the method
				if (method == null){
					method = new FecMethod (methodName);
					WorkingProcess wp = findWorkingProcess (r.methodName, r.previousProcess);
					method.setInitialProcess (wp);
				}

				// Then, the steps are added to form the method
				WorkingProcess wp1 = findWorkingProcess (r.methodName, r.previousProcess);
				String materialMarketDestination = MaterialMarketDestination.getMarketDestination (r.materialMarketDestination);
				String materialStatus = ResourceStatus.getStatus (r.materialStatus);
        WorkingProcess wp2 = findWorkingProcess (r.methodName, r.nextProcess);

				method.addProcess (wp1, materialMarketDestination, materialStatus, wp2);

			} else {
				throw new Exception ("wrong format in " + fileName + " near record " + record);
			}

		}
		return method;
	}

	/**
	 * Looks in the working process map loaded from file for a given working process. If trouble,
	 * throws an exception.
	 */
	private WorkingProcess findWorkingProcess (String methodName, String previousProcess) throws Exception {
		WorkingProcess wp = wpMap.get (previousProcess);
		if (wp == null)
			throw new Exception ("Could not find working process for method: " + methodName
					+ ", wrong wp name: " + previousProcess);
		return wp;
	}

}
