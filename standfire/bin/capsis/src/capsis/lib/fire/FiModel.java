package capsis.lib.fire;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jeeb.lib.util.TicketDispenser;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;

/**
 * A super class for the model classes relying on the fire lib.
 * 
 * @author F. de Coligny - 2014
 */
public class FiModel extends GModel {

	// fc-2.2.2015 removed static
	public Set<String> particleNames;
//	public static Set<String> particleNames;
	
	// A way to get unique ids for the trees
	protected TicketDispenser treeIdDispenser;
	
	// this random generator number was added by FP nov 2015 to deal better with stochasticity.
	// If the seed provided in initial parameters is negative (-1), then no seed is used for the simulation (not determnistic)
	// if a positive long seed is provided, then random number generation is deterministic. 
	public Random rnd;

	public FiModel() {
		super();
		particleNames = new HashSet<String>();
		treeIdDispenser = new TicketDispenser();
	}
	/**
	 * Convenient method.
	 */
	public FiInitialParameters getSettings() {
		return (FiInitialParameters) settings;
	}
	
	protected MethodProvider createMethodProvider() {
		return new FiMethodProvider();
	}

	public TicketDispenser getTreeIdDispenser() {
		return treeIdDispenser;
	}

}
