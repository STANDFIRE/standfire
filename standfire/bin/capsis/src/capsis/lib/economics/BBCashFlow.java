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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import capsis.kernel.GScene;
import capsis.kernel.Step;

/**	BBCashFlow is a collection of BBCashFlowLine.
*	@author O. Pain - march 2008
*/
public class BBCashFlow {

	private Collection<BBCashFlowLine> lines;
	private Collection<String> soldProductNames;
	private Collection<String> wpNames;

	
	public BBCashFlow () {}

	public BBCashFlow (Collection steps, boolean includeAnnualAndVariableCosts) {
		lines = new ArrayList<BBCashFlowLine> ();
		soldProductNames = new ArrayList<String> ();
		wpNames = new ArrayList<String> ();
		
		for (Iterator i = steps.iterator (); i.hasNext ();) {
			Step stp = (Step) i.next ();
			
			BillBookCompatible stand = (BillBookCompatible) stp.getScene ();
			
			// 1. crop expenses
			Collection<CropExpense> cropExpenses = stand.getCropExpenses ();
			
			//~ lines.addAll (cropExpenses);
			for (CropExpense ce : cropExpenses) {
				BBCashFlowLine line = new BBCashFlowLine (ce);
				lines.add (line);
			}
			
			// 2. primary products of the stand
			//~ Collection<Product> products = stand.getProducts ();	// ex: STAND_TREE
			//~ lines.addAll (products);
			
			// 3. products of the working processes + incomes
			Collection<BillBookLine> wpOutputProducts = stand.getWPOutputProducts ();
			
			//~ lines.addAll (wpOutputProducts);
			for (BillBookLine l : wpOutputProducts) {
				BBCashFlowLine line = new BBCashFlowLine (l);
				lines.add (line);
				
				if (l instanceof BillBookIncome) {
					soldProductNames.add (l.getBillBookOperation ());
				} else {
					wpNames.add (l.getBillBookOperation ());
				}
			}
			
			// If requested, include annual and variable costs
			if (includeAnnualAndVariableCosts) {
				Collection<AnnualCost> annualCosts = stand.getAnnualCosts ();
				Collection<VariableCost> variableCosts = stand.getVariableCosts ();
				int plantationAge = stand.getPlantationAge ();
				int rotationAge = stand.getRotationAge ();
				
				for (AnnualCost cost : annualCosts) {
					for (int k = plantationAge - rotationAge + 1; k <= plantationAge; k++) {
						BillBookAnnualCost line = new BillBookAnnualCost (cost, (GScene) stand, k);
						lines.add (new BBCashFlowLine (line));
					}
				}
				
				// variable costs area not repeated every year
				if (!i.hasNext ()) {
					for (VariableCost cost : variableCosts) {
						int dateMin = cost.getDateMin ();
						int dateMax = cost.getDateMax ();
						for (int k = dateMin; k <= Math.min (dateMax, plantationAge); k++) {
							BillBookVariableCost line = new BillBookVariableCost (cost, (GScene) stand, k);
							lines.add (new BBCashFlowLine (line));
						}
					}
				}
			}
			
		}
		
	}
	
	private void copy (BBCashFlow from, BBCashFlow to) {
		to.lines = new ArrayList<BBCashFlowLine> ();
		to.soldProductNames = new ArrayList<String> ();
		to.wpNames = new ArrayList<String> ();
		
		for (BBCashFlowLine l : from.getLines ()) {
			to.lines.add (l.clone ());
		}
		for (String s : from.getSoldProductNames ()) {
			to.soldProductNames.add (s);
		}
		for (String s : from.getWpNames ()) {
			to.wpNames.add (s);
		}
	}

	public BBCashFlow applyVariation (String name, double variation) {	// ex: "Abbatage mécanisé", 0.95 = -5%, 1.05 = +5%
		// memorize the current BBCashFlow into a tmp, work in the tmp
System.out.println ("BBCashFlow.applyVariation...");
		BBCashFlow tmp = new BBCashFlow ();
		copy (this, tmp);
		
		for (BBCashFlowLine line : tmp.getLines ()) {
System.out.println ("name="+name+", BillBookOperation="+line.getBillBookOperation ());
			if (line.getBillBookOperation ().equals (name)) {
System.out.println ("BBCashFlow.applyVariation, found "+name+", apply variation: "+variation);
				double pu = line.getBillBookUnitPrice ();
System.out.println ("cashFlow before="+line.getCashFlow ());
				line.setBillBookUnitPrice (pu * variation);
				line.calculateCashFlow ();
System.out.println ("cashFlow after="+line.getCashFlow ());
System.out.println ("end-of-BBCashFlow.applyVariation");
			}
		}
		
		return tmp;
	}
	
	public Collection<Integer> getYears () {
		Collection<Integer> years = new ArrayList<Integer> ();
		for (BBCashFlowLine line : lines) {
			years.add (line.getBillBookYear ());
		}
		return years;
	}

	public Collection<Double> getCashFlows () {
		Collection<Double> cashFlows = new ArrayList<Double> ();
		for (BBCashFlowLine line : lines) {
			cashFlows.add (line.getCashFlow ());
		}
		return cashFlows;
	}

	public Collection<BBCashFlowLine> getLines () {return lines;}
	
	public Collection<String> getSoldProductNames () {return soldProductNames;}
	
	public Collection<String> getWpNames () {return wpNames;}
	
	
}

