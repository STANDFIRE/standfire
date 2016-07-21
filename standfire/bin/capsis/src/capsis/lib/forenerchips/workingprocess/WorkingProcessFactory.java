package capsis.lib.forenerchips.workingprocess;

import capsis.lib.forenerchips.WorkingProcess;

/**
 * A factory to create WorkingProcesses when reading a file.
 * 
 * @author N. Bilot, F. de Coligny - April 2013
 */
public class WorkingProcessFactory {

	static public WorkingProcess createWorkingProcess (String name, String klass, String parameters) throws Exception {
		try {
			if (klass.equals ("BundlingProcess")) {
				return BundlingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("BundlingProcessBis")) {
				// return BundlingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("ChippingProcess")) {
				return ChippingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("ChippingProcessBis")) {
				// return ChippingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("DryingProcess")) {
				return DryingProcess.getInstance (name, parameters);

			} else if (klass.equals ("FellingProcess")) {
				return FellingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("FellingProcessBis")) {
				// return FellingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("ForwardingProcess")) {
				return ForwardingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("ForwardingProcessBis")) {
				// return ForwardingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("HarvestingProcess")) {
				return HarvestingProcess.getInstance (name, parameters);
			
			} else if (klass.equals ("HarvestingLortz")) {
				return HarvestingLortz.getInstance (name, parameters);

			// } else if (klass.equals ("HarvestingProcessBis")) {
				// return HarvestingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("LoadingProcess")) {
				return LoadingProcess.getInstance (name, parameters);

			} else if (klass.equals ("SeasoningProcess")) {
				return SeasoningProcess.getInstance (name, parameters);

			} else if (klass.equals ("BuckingProcess")) {
				return BuckingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("BuckingProcessBis")) {
				// return BuckingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("TransportingProcess")) {
				return TransportingProcess.getInstance (name, parameters);

			// } else if (klass.equals ("TransportingProcessBis")) {
				// return TransportingProcessBis.getInstance (name, parameters);

			} else if (klass.equals ("NothingProcess")) {
				return NothingProcess.getInstance (name, parameters);

			} else {
				throw new Exception ("Unexpected WorkingProcess: " + klass);
			}
		} catch (Exception e) {
			throw new Exception ("WorkingProcessFactory: could not create a working process for: " + klass, e);
		}
	}

}
