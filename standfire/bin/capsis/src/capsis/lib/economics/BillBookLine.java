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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Translator;
import capsis.kernel.GScene;

/**	A line in the BillBook.
*	@author O. Pain - november 2007
*/
public abstract class BillBookLine extends jeeb.lib.util.Node implements Serializable, Comparable {

	static {
		Translator.addBundle("capsis.extension.standviewer.BillBook");
	}
	
	// CONSTANTS AND TYPOLOGIES HERE
	// Bill Book line types (type column in the Bill Book)
	public static final String EXPENSE_TYPE_INPUT_LOCATION = "EXPENSE_TYPE_INPUT_LOCATION";	// fertilization, herbicide ("intrant")
	public static final String EXPENSE_TYPE_MECHANIZATION = "EXPENSE_TYPE_MECHANIZATION";	// Application of input location or soil ploughing
	public static final String EXPENSE_TYPE_PURCHASE = "EXPENSE_TYPE_PURCHASE";	// 
	public static final String EXPENSE_TYPE_COST = "EXPENSE_TYPE_COST";	// used for Annual or variable costs
	public static final String EXPENSE_TYPE_INCOME = "EXPENSE_TYPE_INCOME";	// used for Annual or variable incomes
	
	// Bill Book line units
	public static final String HA = "HA";
	public static final String M3 = "M3";
	public static final String UNIT = "UNIT";	// "unité de plantation"
	public static final String KG = "KG";
	public static final String L = "L";	// liter
	public static final String DAY = "DAY";		// OP 16/10/07
	public static final String HOUR = "HOUR";	// OP 16/10/07
	public static final String NONE = "NONE";	// fc - 14.5.2008
	
	// fc - 14.5.2008
	private static Map billBookLineTypesMap;
	private static Map billBookLineUnitsMap;
	
	// fc - 14.5.2008
	static {
		billBookLineTypesMap = new HashMap<String,String> ();
		billBookLineTypesMap.put (Translator.swap ("EXPENSE_TYPE_INPUT_LOCATION"), EXPENSE_TYPE_INPUT_LOCATION);
		billBookLineTypesMap.put (Translator.swap ("EXPENSE_TYPE_MECHANIZATION"), EXPENSE_TYPE_MECHANIZATION);
		billBookLineTypesMap.put (Translator.swap ("EXPENSE_TYPE_PURCHASE"), EXPENSE_TYPE_PURCHASE);
		billBookLineTypesMap.put (Translator.swap ("EXPENSE_TYPE_COST"), EXPENSE_TYPE_COST);
		billBookLineTypesMap.put (Translator.swap ("EXPENSE_TYPE_INCOME"), EXPENSE_TYPE_INCOME);
		
		billBookLineUnitsMap = new HashMap<String,String> ();
		billBookLineUnitsMap.put (Translator.swap ("HA"), HA);
		billBookLineUnitsMap.put (Translator.swap ("M3"), M3);
		billBookLineUnitsMap.put (Translator.swap ("UNIT"), UNIT);
		billBookLineUnitsMap.put (Translator.swap ("KG"), KG);
		billBookLineUnitsMap.put (Translator.swap ("L"), L);
		billBookLineUnitsMap.put (Translator.swap ("DAY"), DAY);
		billBookLineUnitsMap.put (Translator.swap ("HOUR"), HOUR);
		billBookLineUnitsMap.put (Translator.swap ("NONE"), NONE);
	}
	
	private boolean cost;
	
	public abstract GScene getStand ();	// fc + op - 3.12.2007
	
	// fc - 14.5.2008
	static public Map<String,String> getBillBookLineTypesMap () {return billBookLineTypesMap;}
	static public Map<String,String> getBillBookLineUnitsMap () {return billBookLineUnitsMap;}

	public abstract int getBillBookRotationOrder ();	// fc + op - 3.12.2007
	public abstract int getBillBookYear ();
	public abstract String getBillBookOperation ();
	public abstract String getBillBookDetail ();
	public abstract String getBillBookType ();
	public abstract double getBillBookQuantity ();
	public abstract String getBillBookQuantityUnit ();
	public abstract double getBillBookUnitPrice ();
	public abstract double getBillBookTotalFuelConsumption ();
	public boolean isCost () {return cost;}
	
	public void setCost (boolean b) {this.cost = b;}
	
	public int compareTo (Object other) {
		if (!(other instanceof BillBookLine)) {return -1;}
		BillBookLine o = (BillBookLine) other;
		if (getBillBookYear () < o.getBillBookYear ()) {
			return -1;
		} else if (getBillBookYear () > o.getBillBookYear ()) {
			return +1;
		} else {
			return 0;
		}
	}
	
}

