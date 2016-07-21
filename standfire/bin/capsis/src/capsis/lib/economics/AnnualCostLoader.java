package capsis.lib.economics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**	A format description to read Annual costs files
*
*	@author O. Pain - may 2008
*/
public class AnnualCostLoader extends RecordSet {
	private String fileName;

	private Collection<AnnualCost> annualCosts;

	// A description for a line in the file for a AnnualCost
	@Import
	static public class AnnualCostRecord extends Record {
		public AnnualCostRecord () {super ();}
		public AnnualCostRecord (String line) throws Exception {super (line);}
		//~ public String getSeparator () {return ";";}	// to change default "\t" separator
		public boolean isCost;		// translation key for the species name
		public String label;
		public double amount;
	}


	/**	Constructor 1: reads the given file 	*/
	public AnnualCostLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
		interpret ();
	}

	/**	Constructor 2: saves the given parameters.
	*	Usage: new AnnualCostLoader (salePrices).save (fileName);
	*/
	public AnnualCostLoader (Collection<AnnualCost> annualCosts) throws Exception {
		// fc - 5.12.2007
		super ();
		setHeaderEnabled (false);
		createRecordSet (annualCosts);
	}

	//	Export mode: Before saving, create the record set
	//
	private void createRecordSet (Collection<AnnualCost> annualCosts) throws Exception {
		// fc - 5.12.2007
		add (new CommentRecord ("isCost	label	amount"));
		add (new EmptyRecord ());

		for (AnnualCost c : annualCosts) {
			AnnualCostRecord r = new AnnualCostRecord ();
			r.isCost = c.isCost ();
			r.label = c.getLabel ();
			r.amount = c.getAmount ();

			add (r);
		}

	}

	//	Import mode: Interpret
	//
	private void interpret () throws Exception {
		annualCosts = new ArrayList<AnnualCost> ();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof AnnualCostRecord) {
				AnnualCostRecord r = (AnnualCostRecord) record;

				AnnualCost c = new AnnualCost (
					r.isCost,
					r.label,
					r.amount);

				annualCosts.add (c);

			} else {
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}

		}

	}

	public Collection<AnnualCost> getAnnualCosts () {return annualCosts;}

}


