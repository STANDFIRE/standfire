package capsis.lib.economics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**	A format description to read Variable costs files
*
*	@author O. Pain - may 2008
*/
public class VariableCostLoader extends RecordSet {
	private String fileName;

	private Collection<VariableCost> variableCosts;

	// A description for a line in the file for a VariableCost
	@Import
	static public class VariableCostRecord extends Record {
		public VariableCostRecord () {super ();}
		public VariableCostRecord (String line) throws Exception {super (line);}
		//~ public String getSeparator () {return ";";}	// to change default "\t" separator
		public int dateMin;	
		public int dateMax;	
		public boolean isCost;
		public String label;
		public double amount;
	}


	/**	Constructor 1: reads the given file
	*/
	public VariableCostLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
		interpret ();
	}

	/**	Constructor 2: saves the given parameters.
	*	Usage: new VariableCostLoader (salePrices).save (fileName);
	*/
	public VariableCostLoader (Collection<VariableCost> variableCosts) throws Exception {
		// fc - 5.12.2007
		super ();
		setHeaderEnabled (false);
		createRecordSet (variableCosts);
	}

	//	Export mode: Before saving, create the record set
	//
	private void createRecordSet (Collection<VariableCost> variableCosts) throws Exception {
		// fc - 5.12.2007
		add (new CommentRecord ("dateMin	dateMax	isCost	label	amount"));
		add (new EmptyRecord ());

		for (VariableCost c : variableCosts) {
			VariableCostRecord r = new VariableCostRecord ();
			r.dateMin = c.getDateMin ();
			r.dateMax = c.getDateMax ();
			r.isCost = c.isCost ();

			// otherwise, error at read time (two tabs are considered one separator)
			String label = c.getLabel ();
			if (label == null || label.trim ().length () == 0) {label = " ";}
				
			r.label = label;
			r.amount = c.getAmount ();

			add (r);
		}

	}

	//	Import mode: Interpret
	//
	private void interpret () throws Exception {
		variableCosts = new ArrayList<VariableCost> ();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof VariableCostRecord) {
				VariableCostRecord r = (VariableCostRecord) record;

				VariableCost c = new VariableCost (
					r.dateMin,
					r.dateMax,
					r.isCost,
					r.label,
					r.amount);

				variableCosts.add (c);

			} else {
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}

		}

	}

	public Collection<VariableCost> getVariableCosts () {return variableCosts;}

}


