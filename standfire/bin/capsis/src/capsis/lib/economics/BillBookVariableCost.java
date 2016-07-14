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

import jeeb.lib.util.Translator;
import capsis.kernel.GScene;

/**	A variable cost line in the BillBook.
*	@author O. Pain - may 2008
*/
public class BillBookVariableCost extends BillBookLine {

	static {
		Translator.addBundle("capsis.lib.economics.BillBook");
	}
	
	private GScene stand;	// fc + op - 3.12.2007
	private String productName;
	
	private int billBookRotationOrder;
	private int billBookYear;
	private String billBookOperation;
	private String billBookDetail;
	private String billBookType;
	private double billBookQuantity;
	private String billBookQuantityUnit;
	private double billBookUnitPrice;
	private double billBookTotalFuelConsumption;
	
	public BillBookVariableCost (VariableCost cost, GScene stand, int year) {
		this.stand = stand;
		
		this.billBookRotationOrder = stand.getDate ();
		this.billBookYear = year;
		if (cost.isCost ()) {
			this.billBookOperation = Translator.swap ("BillBookVariableCost.variableCost");
		} else {
			this.billBookOperation = Translator.swap ("BillBookVariableCost.variableIncome");
		}
		this.billBookDetail = cost.getLabel (); 
		this.billBookType = cost.isCost () ? EXPENSE_TYPE_COST : EXPENSE_TYPE_INCOME;
		this.billBookQuantity = 1;
		this.billBookQuantityUnit = BillBookLine.HA;
		this.billBookUnitPrice = cost.getAmount ();
		this.billBookTotalFuelConsumption = 0;
		
		setCost (cost.isCost ());
	}
	
	public void setBillBookUnitPrice (double p) {this.billBookUnitPrice = p;}
	
	public String getProductName () {return productName;}
	// BillBookLine interface
	public GScene getStand () {return stand;}	// fc + op - 3.12.2007
	public int getBillBookRotationOrder () {return stand.getDate ();}	// fc + op - 3.12.2007
	public int getBillBookYear () {return billBookYear;}
	public String getBillBookOperation () {return billBookOperation;}
	public String getBillBookDetail () {return billBookDetail;}
	public String getBillBookType () {return billBookType;}
	public double getBillBookQuantity () {return billBookQuantity;}
	public String getBillBookQuantityUnit () {return billBookQuantityUnit;}
	public double getBillBookUnitPrice () {return billBookUnitPrice;}
	public double getBillBookTotalFuelConsumption () {return billBookTotalFuelConsumption;}
	// BillBookLine interface
	
	
}

