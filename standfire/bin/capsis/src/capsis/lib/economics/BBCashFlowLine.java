/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.lib.economics;

import jeeb.lib.util.Log;
import capsis.kernel.GScene;

/**	A line in the BillBook, with the last cash flow column added.
*	@author O. Pain - march 2008
*/
public class BBCashFlowLine extends BillBookLine implements Cloneable {

	private GScene stand;
	
	private int billBookRotationOrder;
	private int billBookYear;
	private String billBookOperation;
	private String billBookDetail;
	private String billBookType;
	private double billBookQuantity;
	private String billBookQuantityUnit;
	private double billBookUnitPrice;
	private double billBookTotalFuelConsumption;
	private boolean cost;
	
	private double cashFlow;	// THIS IS THE difference with BillBookLine : the cash flow is calculated line by line

	
	public BBCashFlowLine (BillBookLine ref) {
		this.stand = ref.getStand ();
		
		// ref is an original scenario line, not modified here, copied into this
		this.billBookRotationOrder 	= ref.getBillBookRotationOrder ();
		this.billBookYear 			= ref.getBillBookYear ();
		this.billBookOperation 		= ref.getBillBookOperation ();
		this.billBookDetail 		= ref.getBillBookDetail ();
		this.billBookType 			= ref.getBillBookType ();
		this.billBookQuantity 		= ref.getBillBookQuantity ();
		this.billBookQuantityUnit 	= ref.getBillBookQuantityUnit ();
		this.billBookUnitPrice 		= ref.getBillBookUnitPrice ();
		this.billBookTotalFuelConsumption = ref.getBillBookTotalFuelConsumption ();
		this.cost = ref.isCost ();
		
		calculateCashFlow () ;
	}
	
	public void calculateCashFlow () {
		this.cashFlow = this.getBillBookQuantity () * this.getBillBookUnitPrice () 
				* this.getStand ().getArea ()/10000;
		if (this.isCost ()) {this.cashFlow *= -1;}
	}
	
	public BBCashFlowLine clone () {
		try {
			BBCashFlowLine l = (BBCashFlowLine) super.clone ();
			return l;
		} catch (Exception e) {
			Log.println (Log.ERROR, "BBCashFlowLine.clone ()", "exception during cloning", e);
			return null;
		}
	}
	
	
	public GScene getStand () {return stand;}
	
	public int getBillBookRotationOrder () {return billBookRotationOrder;}
	public int getBillBookYear () {return billBookYear;}
	public String getBillBookOperation () {return billBookOperation;}
	public String getBillBookDetail () {return billBookDetail;}
	public String getBillBookType () {return billBookType;}
	public double getBillBookQuantity () {return billBookQuantity;}
	public String getBillBookQuantityUnit () {return billBookQuantityUnit;}
	public double getBillBookUnitPrice () {return billBookUnitPrice;}
	public double getBillBookTotalFuelConsumption () {return billBookTotalFuelConsumption;}
	public boolean isCost () {return cost;}
	
	public double getCashFlow () {return cashFlow;}
	

	public void setBillBookUnitPrice (double v) {billBookUnitPrice = v;}

	//~ public void setCost (boolean b) {this.cost = b;}
	
	public int compareTo (Object other) {
		if (!(other instanceof BBCashFlowLine)) {return -1;}
		BBCashFlowLine o = (BBCashFlowLine) other;
		if (getBillBookYear () < o.getBillBookYear ()) {
			return -1;
		} else if (getBillBookYear () > o.getBillBookYear ()) {
			return +1;
		} else {
			return 0;
		}
	}
	
}

