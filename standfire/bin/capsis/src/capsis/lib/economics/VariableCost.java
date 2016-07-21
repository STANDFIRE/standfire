package capsis.lib.economics;


/**	VariableCost
*
*	@author O. Pain - may 2008
*/
public class VariableCost extends AnnualCost implements Comparable {

	private int dateMin;
	private int dateMax;

	// from RegixAnnualCost: 
	//~ private boolean cost;
	//~ private String label;
	//~ private double amount;
	
	public VariableCost (int dateMin, int dateMax, boolean cost, String label, double amount) {
		super (cost, label, amount);
		this.dateMin = dateMin;
		this.dateMax = dateMax;
	}
	public int getDateMin () {return dateMin;}
	public int getDateMax () {return dateMax;}
	
	public void setDateMin (int v) {dateMin = v;}
	public void setDateMax (int v) {dateMax = v;}
	
	public int compareTo (Object other) {
		if (!(other instanceof VariableCost)) {return -1;}
		VariableCost o = (VariableCost) other;
		
		if (getDateMin () < o.getDateMin ()) {
			return -1;
		} else if (getDateMin () > o.getDateMin ()) {
			return 1;
		} else {
			if (getDateMax () < o.getDateMax ()) {
				return -1;
			} else if (getDateMax () > o.getDateMax ()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
