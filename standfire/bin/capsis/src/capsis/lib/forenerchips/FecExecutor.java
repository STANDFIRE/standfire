package capsis.lib.forenerchips;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Executes a given ForEnerChips method on the given initial resource (i.e. cut trees).
 * 
 * @author N. Bilot, F. de Coligny - February 2013
 */
public class FecExecutor {

	private static class Job {

		public Resource inputResource;
		public WorkingProcess process;
	
		/**
		 * Constructor
		 */
		public Job (Resource inputResource, WorkingProcess process) {
			this.inputResource = inputResource;
			this.process = process;
		}
	}

	private Resource initialResource;
	private FecMethod method;

	private Stack<Job> jobs;

	private List<Resource> allResources;
	private List<WorkingProcess> allProcesses;

	/**
	 * Constructor
	 */
	public FecExecutor (Resource initialResource, FecMethod method) {
		this.initialResource = initialResource;
		this.method = method;
		jobs = new Stack<Job> ();
		allResources = new ArrayList<Resource> ();
		allProcesses = new ArrayList<WorkingProcess> ();
	}

	public void execute () throws Exception {
		System.out.println ("FecExecutor executing...");
		int cpt = 0;
		
		WorkingProcess p0 = method.getInitialProcess ();
		jobs.add (new Job (initialResource, p0));

		allResources.add (initialResource);

		while (!jobs.isEmpty ()) {

			Job job = jobs.pop ();
			Resource ir = job.inputResource;
			WorkingProcess p = job.process;

			p.setInputResource (ir);
			p.run ();
			allProcesses.add (p);
			cpt++;

			// Which ouput(s) ?
			List<Resource> ouputs = p.getOutputs ();

			// Do we have wps to run on these outputs in the Method ?
			for (Resource res : ouputs) {
				allResources.add (res);
				WorkingProcess p2 = method.getProcess (p, res.market, res.status);
				if (p2 != null) { // found a wp for this resource
					// Add a job to be run
					jobs.push (new Job (res, p2));

				}
			}
		}
		System.out.println ("FecExecutor ended, have run "+cpt+" processes");

	}

	public List<Resource> getAllResources () {
		return allResources;
	}

	public List<WorkingProcess> getAllProcesses () {
		return allProcesses;
	}

	public String report () {
		StringBuffer b = new StringBuffer ();
		b.append ("Executor report: \n");
		b.append (method);
		b.append ("After execution: \n");
		b.append ("  Executed all these working processes: \n");
		for (WorkingProcess p : allProcesses) {
			b.append ("    " + p.chainingString () + "\n");
		}
		b.append ("  Managed these resources: \n");
		for (Resource r : allResources) {
			b.append ("    " + r + "\n");
		}
		return b.toString ();
	}

}
