package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.MaterialMarketDestination;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A nothing working process (faconneuse)
 * 
 * @author N. Bilot - February 2013
 */
public class NothingProcess extends WorkingProcess {

  private String name;
  private String unUsedParam;


	/**
	 * Constructor.
	 */
   public NothingProcess (String name, String unUsedParam) throws Exception {
		super ("NothingProcess");

		this.name = name;
    this.unUsedParam = unUsedParam;
		
		// What resource can be processed
		addCompatibleStatusOrSite (ResourceStatus.STANDING_TREE);
    addCompatibleStatusOrSite (ResourceStatus.FALLEN_TREE);
    addCompatibleStatusOrSite (ResourceStatus.BRANCH);
    addCompatibleStatusOrSite (ResourceStatus.RESIDUAL);
    addCompatibleStatusOrSite (ResourceStatus.LOG);
    addCompatibleStatusOrSite (ResourceStatus.CHIP);
		addCompatibleStatusOrSite (ResourceSite.PLOT);
		addCompatibleStatusOrSite (ResourceSite.ROADSIDE);
    addCompatibleStatusOrSite (ResourceSite.PLATFORM);
    addCompatibleStatusOrSite (ResourceSite.HEATING_PLANT);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public NothingProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");
		
    String wpName = name;
    String unUsedParam = st.nextToken ();
		
		return new NothingProcess (name, unUsedParam);
	}

	/**
	 * Run the nothing process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		
  // Outputs material(s)
  // Consumptions for this process are null, any consumption variable is reduced to 0
    Resource output = input.copy ();
    output.processName = name ;
    output.addProcessInHistory (this);
    output.machineWorkTime = 0d;
    output.humanWorkTime = 0d;
    output.fuelConsumption = 0d;
    output.oilConsumption = 0d;
    output.lcConsumption = 0d;
    output.logisticsConsumption = 0d;
    output.processConsumption = 0d;
    outputs.add (output);
    
  }

	public String toString () {
		return "NothingProcess" 
        + "name :" + name 
        + "unUsedParam :" + unUsedParam 
        ;
	}

}