package capsis.lib.forestgales;

import java.io.Serializable;

/**
 * A superclass for all the ForestGales methods.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public abstract class FGMethod implements Serializable {

	protected FGStand stand;
	protected FGConfiguration configuration;

	// To see the report during the process
	protected boolean writeInTerminal = false;
	// To get the report at the end
	protected boolean writeInReport = true;
	protected StringBuffer report;

	/**
	 * Constructor. Must be called by all subclasses.
	 * 
	 */
	public FGMethod (FGStand stand, FGConfiguration configuration) {
		this.stand = stand;
		this.configuration = configuration;
		
		report = new StringBuffer ();
	}

	
	public FGStand getStand () {
		return stand;
	}

	
	public FGConfiguration getConfiguration () {
		return configuration;
	}

	abstract public String getName (); // e.g. "ForestGales stand level roughness method" 
	
	/**
	 * Optional, to ask the report to be printed in the terminal during the process.
	 */
	public void setWriteInTerminal (boolean writeInTerminal) {
		this.writeInTerminal = writeInTerminal;
	}

	/**
	 * Optional, to ask the report to be printed in a String to be returned at the end with
	 * getReport ().
	 */
	public void setWriteInReport (boolean writeInReport) {
		this.writeInReport = writeInReport;
	}

	/**
	 * Optional, the methods subclassing FGMethod may use report () in their run () method to report
	 * the progress.
	 */
	protected void report (String message) {
		if (writeInTerminal) System.out.println (message);
		if (writeInReport) report.append (message);
	}

	/**
	 * Returns the report containing the messages sent to report ().
	 */
	public String getReport () {
		return report.toString ();
	}

	abstract public void run () throws Exception;

}
