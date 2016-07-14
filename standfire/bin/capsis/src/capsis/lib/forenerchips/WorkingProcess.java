package capsis.lib.forenerchips;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.DefaultNumberFormat;

/**
 * A superclass for all working processes in the Forenerchips library (e.g. FellingProcess,
 * Forwarding).
 * 
 * @author N. Bilot - February 2013
 */
public abstract class WorkingProcess {

	protected String name;
	protected List<String> compatibleStatusAndSites;
	protected Resource input; // input of this working process

	// protected double consumption; // kWh, energy consumed by the working process
	protected List<Resource> outputs; // output(s) of this working process: 1 or more

	/**
	 * Constructor
	 */
	public WorkingProcess (String name) {
		this.name = name;
		compatibleStatusAndSites = new ArrayList<String> ();
		outputs = new ArrayList<Resource> ();
	}

	public String getName () {
		return name;
	}

	public void addCompatibleStatusOrSite (String s) {
		compatibleStatusAndSites.add (s);
	}

	public void setInputResource (Resource r) {
		input = r;
	}

	protected void checkInputCompatibility () throws Exception {
		if (input == null)
			throw new Exception (this.getClass ().getName () + ": could not check input compatibility, input is null");
		if (compatibleStatusAndSites == null || compatibleStatusAndSites.isEmpty ())
			throw new Exception (this.getClass ().getName ()
					+ ": could not check input compatibility, compatibleStatusAndSites is null or empty: "
					+ compatibleStatusAndSites);
		if (!compatibleStatusAndSites.contains (input.status))
			throw new Exception (this.getClass ().getName () + ": input resource status is not in compatibility list: "
					+ input);
		if (!compatibleStatusAndSites.contains (input.site))
			throw new Exception (this.getClass ().getName () + ": input resource site is not in compatibility list: "
					+ input);
	}

	/**
	 * Performs the process
	 */
	abstract public void run () throws Exception;

	// public double getConsumption () {
		// return consumption;
	// }

	public List<Resource> getOutputs () {
		return outputs;
	}

	/**
	 * A tool method to help checking the parameters in the constructors of the wp subclasses.
	 */
	protected void check (String parameterName, boolean correctExpression) throws Exception {
		if (!correctExpression)
			throw new Exception ("Error in " + getName () + ": the check method found a wrong value for "
					+ parameterName);
	}

	/**
	 * Returns the double value of the String parameter, exception if trouble.
	 */
	protected static double doubleValue (String s) throws Exception {
		try {
			return Double.valueOf (s.trim ()).doubleValue ();
		} catch (Exception e) {
			throw new Exception ("Expected a number: "+s);
		}
	}

	/**
	 * Returns the boolean value of the String parameter, exception if trouble.
	 */
	protected static boolean booleanValue (String s) throws Exception {
		try {
			if (!(s.equals ("true") || s.equals ("false"))) throw new Exception ();
			return Boolean.valueOf (s.trim ()).booleanValue ();
		} catch (Exception e) {
			throw new Exception ("Expected a boolean: "+s);
		}
	}

	public String chainingString () {
//		NumberFormat f = DefaultNumberFormat.getInstance ();
		String in = "" + input.market;
		String out = "";
		int i = 1;
		for (Resource res : outputs) {
			out += "" + res.market;
			if (i++ < outputs.size ()) out += ",";
		}

		return "" + name + " " + in + " -> " + out;
	}

}
