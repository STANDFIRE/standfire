/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.dataextractor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFTables;
import capsis.extension.economicfunction.Expense;
import capsis.extension.economicfunction.Income;
import capsis.extensiontype.EconomicFunction;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.CommonEconFunctions;
import capsis.lib.economics.EconModel;
import capsis.lib.economics.EconStand;
import capsis.lib.economics.RegularExpenseOrIncome;
import capsis.util.GrouperManager;

/**
 * Econ data report.
 * 
 * @author C. Orazio - December 2005 From DEStandTable author F.de Coligny
 * 
 */
public class DEEconTable extends PaleoDataExtractor implements DFTables {
	public static final int MAX_FRACTION_DIGITS = 2;
	
	private static class EconLine implements Comparable {

		public String startingDate;
		public String endingDate;
		public String label;
		public String income;
		public String expense;
		public String balance;
		public String actualizedBalance;
		
		public EconLine () {}

		public void validate () {
			if (startingDate == null) startingDate = "";
			if (endingDate == null) endingDate = "";
			if (label == null) label = "";
			if (income == null) income = "";
			if (expense == null) expense = "";
			if (balance == null) balance = "";
			if (actualizedBalance == null) actualizedBalance = "";
		}
		
		/**	
		 * A method to compare this EconLine with another.
		 * If the Econ lines are in a TreeSet, they will be sorted.
		 */
		@Override
		public int compareTo (Object o) {
			if (!(o instanceof EconLine)) return -1;
			EconLine other = (EconLine) o;
			try {
				int a = Check.intValue (startingDate);
				int b = Check.intValue (other.startingDate);
				
				return a - b;
			} catch (Exception e) {
				return 1; // if text in col 0, we arrive here: at the top
			}
		}
		
		public String toString () {
			return startingDate+", "+endingDate+", "+label+", "+income+", "+expense+", "+balance+", "+actualizedBalance;
		}
		
	}

	protected Collection tables;
	protected Collection titles;
	protected MethodProvider methodProvider;

	protected NumberFormat formater;
	protected NumberFormat formater3;

	static {
		Translator.addBundle ("capsis.extension.dataextractor.DEEconTable");
	}

	/**
	 * Phantom constructor. Only to ask for extension properties (authorName,
	 * version...).
	 */
	public DEEconTable () {
	}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEEconTable (GenericExtensionStarter s) {
		super (s);
		try {
			tables = new ArrayList ();
			titles = new ArrayList ();
			// Used to format decimal part with 2 digits only

			// add cm + tl 10032005 : US format to export to excel
			formater = NumberFormat.getInstance (Locale.US); // to impose
			// decimal dot
			// instead of
			// "," for
			// french number
			// format
			// formater = NumberFormat.getInstance ();
			formater.setMaximumFractionDigits (MAX_FRACTION_DIGITS);
			formater.setMinimumFractionDigits (MAX_FRACTION_DIGITS);
			formater.setGroupingUsed (false);
			formater3 = NumberFormat.getInstance (Locale.US); // to impose
			// decimal dot
			// instead of
			// "," for
			// french number
			// format
			// formater3 = NumberFormat.getInstance ();
			formater3.setMaximumFractionDigits (3);
			formater3.setMinimumFractionDigits (3);
			formater3.setGroupingUsed (false);

		} catch (Exception e) {
			Log.println (Log.ERROR, "DEEconTable.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
			GModel m = (GModel) referent;
			if (!(m instanceof EconModel)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "DEEconTable.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);

		// CHANGED: these options are not any more options (simplifies a lot)
//		addBooleanProperty ("ActualizedBalance", true);
//		addBooleanProperty ("Balance", true);
//		addBooleanProperty ("Expense", true);
//		addBooleanProperty ("Income", true);
//		addBooleanProperty ("Label", true);
//		addBooleanProperty ("EndingDate", true);
	}

	/**
	 * From DataExtractor SuperClass.
	 * 
	 * Computes the data series. This is the real output building. It needs a
	 * particular Step. This output display in a teble format data hidden in economic variables
	 * date from the root Step to this one.
	 * 
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) { return true; }
		if (step == null) { return false; }

		// Retrieve method provider
		methodProvider = step.getProject ().getModel ().getMethodProvider ();

		try {
			// Per Ha computation
			double coefHa = 1;
			if (settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);
			Collection regularLines = ((EconModel) step.getProject ().getModel ()).getRegularExpenseOrIncomes ();
			
			List<EconLine> econLines = new ArrayList<EconLine> ();

			// Data extraction************************************************

//			int line = 1; // line account*******
			// Read data from steps
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step step = (Step) i.next ();
				
				GScene stand = step.getScene ();

				// REPLACED the lines higher by an alternative: if no economics data available, jump to next
				if (((EconStand) stand).getEconomicFunctions () == null) {continue;}
				
				// Create a line in the table for this step
				EconLine li = new EconLine ();
				econLines.add (li);
				
				// Econ variables part from steps
				int date = stand.getDate ();
				
				li.startingDate = "" + date;
				li.endingDate = "" + date;
				li.label = CommonEconFunctions.getEconLabel (step);

				// Get the economic functions
				Expense expF = null;
				Income incF = null;
				EconStand es = (EconStand) stand;

				// Note: each step may have one expense and one income at most
				Collection functions = es.getEconomicFunctions ();
				if (functions != null) {
					for (Iterator j = functions.iterator (); j.hasNext ();) {
						EconomicFunction f = (EconomicFunction) j.next ();
						if (f instanceof Expense) expF = (Expense) f;
						if (f instanceof Income) incF = (Income) f;
					}
				}

				double inc = 0;
				if (incF != null) inc = incF.getResult ();

				double exp = 0;
				if (expF != null) exp = expF.getResult ();
				// ERROR below, fixed just higher - fc-8.11.2011
				// if (expF!=null){exp = incF.getResult ();};

				if (incF != null) li.income = "" + formater.format (inc * coefHa);
				if (expF != null) li.expense = "" + formater.format (exp * coefHa);
				li.balance = "" + formater.format ((inc - exp) * coefHa);
				li.actualizedBalance = ""
							+ formater.format (CommonEconFunctions.actualizedValueOnAStep (inc - exp, date, date, step)
									* coefHa);
				
			}

			// Read regular incomes / expenses from EconModel------
			if (regularLines != null) {
				for (Iterator i = regularLines.iterator (); i.hasNext ();) {
					
					RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();

					double date1 = Math.max (r.getFromDate (), CommonEconFunctions.StartingDate (step));
					double date2 = Math.min (r.getToDate (), CommonEconFunctions.EndingDate (step));
					
					EconLine li = new EconLine ();
					econLines.add (li);

					li.startingDate = "" + r.getFromDate ();
					li.endingDate = "" + r.getToDate ();
					li.label = "" + r.getLabel ();
					li.income = "" + formater.format (r.getIncome () * coefHa);
					li.expense = "" + formater.format (r.getExpense () * coefHa);
					li.balance = ""
						+ formater.format ((r.getIncome () - r.getExpense ()) * (date2 - date1 + 1) * coefHa);
					li.actualizedBalance = ""
						+ formater.format (CommonEconFunctions.actualizedValueOnAStep (r.getIncome ()
								- r.getExpense (), r.getFromDate (), r.getToDate (), step)
								* coefHa);
					
				}
			}

			System.out.println ("DEEconTable, doExtraction ()...");

			String[][] t = new String[econLines.size () + 1][7]; // +1 line for the header, see below

			// Column headers
			t[0][0] = "Start";
			t[0][1] = "End";
			t[0][2] = "Label";
			t[0][3] = (settings.perHa) ? "Income/ha" : "Income";
			t[0][4] = (settings.perHa) ? "Expense/ha" : "Expense";
			t[0][5] = (settings.perHa) ? "Balance/ha" : "Balance";
			t[0][6] = (settings.perHa) ? "Net Value/ha" : "Net Value";
			
			// Sort on starting date
			Collections.sort (econLines);
			
			// Copy lines to array
			int k = 1; // line 0 is for header, see higher
			for (EconLine l : econLines) {
				l.validate ();
				t[k][0] = l.startingDate;
				t[k][1] = l.endingDate;
				t[k][2] = l.label;
				t[k][3] = l.income;
				t[k][4] = l.expense;
				t[k][5] = l.balance;
				t[k][6] = l.actualizedBalance;
				k++;
			}
			
//			System.out.println ("EconLines:-----------: "+t.length);
//			for (int i = 0; i < t.length; i++) {
//				for (int j = 0; j < t[0].length; j++) {
//					System.out.println ("-> "+t[i][j]);
//				}
//				System.out.println ("---");
//			}
//			System.out.println ("---------------------");
			
			// Tables titles
			titles.clear ();
			titles.add ("Economy");
			
			// Tables contents
			tables.clear ();
			tables.add (t);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DEEconTable.doExtraction ()", "Exception caught : ", exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * This prefix is built depending on current settings. ex: "+ 25 years /ha"
	 */
	protected String getNamePrefix () {
		String prefix = "";
		try {
			if (isCommonGrouper () && isGrouperMode ()
					&& GrouperManager.getInstance ().getGrouperNames ().contains (getGrouperName ())) {
				prefix += getGrouperName () + " - ";
			}
			if (settings.perHa) {
				prefix += "/ha - ";
			}
		} catch (Exception e) {
		} // if trouble, prefix is empty
		return prefix;
	}

	/**
	 * From DataFormat interface. From Extension interface.
	 */
	public String getName () {
		return getNamePrefix () + Translator.swap ("DEEconTable");
	}

	/**
	 * DFTables interface.
	 */
	public Collection getTables () {
		return tables;
	}

	/**
	 * DFTables interface.
	 */
	public Collection getTitles () {
		return titles;
	}

	/**
	 * Extension interface.
	 */
	public String getVersion () {
		return VERSION;
	}

	public static final String VERSION = "1.1.1";

	/**
	 * Extension interface.
	 */
	public String getAuthor () {
		return "C. Orazio";
	}

	/**
	 * Extension interface.
	 */
	public String getDescription () {
		return Translator.swap ("DEEconTable.description");
	}

}
