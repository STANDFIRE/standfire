package capsis.lib.phelib;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Phelib dll interface.
 *
 * @author F. de Coligny - June 2015
 */
public class Phelib {

//	static {
//		// Set jna.library.path
//		System.setProperty("jna.library.path", System.getProperty("java.library.path"));
//	}

	// This is the standard, stable way of mapping, which supports extensive
	// customization and mapping of Java to native types.

	public interface PhelibInterface extends Library {
		PhelibInterface INSTANCE = (PhelibInterface) Native.loadLibrary("libPhelib", PhelibInterface.class);

		String getVersion();

/*		void init(String xmlFileName);

		int getPhaseCount();

		void f(int site, int year, int doy, float latitude, int nVariables, float[] climateDoy,
				float[] climateDayAfter, int nPhases, int[] prevIds, float[] prevValues, int[] phaseIds,
				float[] phaseValues);
*/
	}

	public String getVersion() {
		return PhelibInterface.INSTANCE.getVersion();
	}

/*	public void init(String xmlFileName) {
		PhelibInterface.INSTANCE.init(xmlFileName);
	}

	public int getPhaseCount() {
		return PhelibInterface.INSTANCE.getPhaseCount();
	}

	public void f(int site, int year, int doy, float latitude, int nVariables, float[] climateDoy,
			float[] climateDayAfter, int nPhases, int[] prevIds, float[] prevValues, int[] phaseIds, float[] phaseValues) {

		PhelibInterface.INSTANCE.f(site, year, doy, latitude, nVariables, climateDoy, climateDayAfter, nPhases,
				prevIds, prevValues, phaseIds, phaseValues);

	}
*/
	/**
	 * A test method
	 * @param args: expected first arg is the Phelib xmlFileName
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

//		String xmlFileName = null;
//		try {
//			xmlFileName = args[0];
//		} catch (Exception e) {
//			throw new Exception("Phelib: missing xml file name, aborted");
//		}

		Phelib p = new Phelib();

		System.out.println("Phelib calling getVersion ()...");
		String v = p.getVersion();
		System.out.println("Phelib version: " + v);

//		System.out.println("Phelib calling init ()...");
//		p.init(xmlFileName);
//		System.out.println("Phelib ...init () done");

//		System.out.println("Phelib calling getPhaseCount ()...");
//		int n = p.getPhaseCount();
//		System.out.println("Phelib #Phases: " + n);


	}

}