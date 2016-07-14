package capsis.lib.economics;

import java.io.Serializable;

/**	AnnualCost
*
*	@author O. Pain - may 2008
*/
public class AnnualCost implements Serializable {

	private boolean cost;
	private String label;
	private double amount;

	public AnnualCost (boolean cost, String label, double amount) {
		this.cost = cost;
		this.label = label;
		this.amount = amount;
	}
	
	public boolean isCost () {return cost;}
	public String getLabel () {return label;}
	public double getAmount () {return amount;}
	
	public void setCost (boolean v) {cost = v;}
	public void setLabel (String v) {label = v;}
	public void setAmount (double v) {amount= v;}
	
}
