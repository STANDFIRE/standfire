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

package capsis.extension.economicfunction;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extensiontype.EconomicFunction;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.CommonEconFunctions;
import capsis.lib.economics.EconModel;
import capsis.lib.economics.EconStand;
import capsis.util.CancelException;

/**
 *
 *
 * @author C. Orazio - january 2003
 */
public class ExpensePerHectare extends Expense {

	protected double cost;

	static {
		Translator.addBundle("capsis.extension.economicfunction.ExpensePerHectare");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public ExpensePerHectare () {}

	/**
	 * Official constructor. Uses an ExtensionStarter.
	 */
	public ExpensePerHectare (GenericExtensionStarter s) throws Exception {
		try {
			stand = s.getScene ();	// stand is defined in EconomicFunction
			model = s.getModel ();	// model is defined in EconomicFunction

			// 1. Start mode according to context : interactive or not
			if (s instanceof ExpensePerHectareStarter) {

				// 2. Script mode starter
				ExpensePerHectareStarter p = (ExpensePerHectareStarter) s;
				cost = p.cost;

			} else {

				// 3. Interactive start
				ExpensePerHectareDialog dlg = new ExpensePerHectareDialog ();

				if (dlg.isValidDialog ()) {
					// valid -> ok was hit and check were ok
					cost = dlg.getCost ();
				} else {
					throw new CancelException ();
				}
				dlg.dispose ();

			}

		} catch (Exception exc) {
			if (! (exc instanceof CancelException)) {	// do not log if cancel
				Log.println (Log.ERROR, "ExpensePerHectare.c ()", exc.toString (), exc);
			}
			throw exc;
		}

	}
	/**
	 * String constructor. Uses an ExtensionStarter+ string
	 */
	public ExpensePerHectare (GenericExtensionStarter s, String stringParameters) throws Exception {
		try {
			stand = s.getScene ();	// stand is defined in EconomicFunction
			model = s.getModel ();	// model is defined in EconomicFunction
			cost = new Double (CommonEconFunctions.getValueFromString(stringParameters, 1, EconomicFunction.separator)).doubleValue();

		} catch (Exception exc) {
			if (! (exc instanceof CancelException)) {	// do not log if cancel
				Log.println (Log.ERROR,"String constructor "+"ExpenseConstant.c ()", exc.toString (), exc);
			}
			throw exc;
		}

	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		if (! (referent instanceof GModel)) {return false;}
		if (! (referent instanceof EconModel)) {return false;}

		GModel m = (GModel) referent;
		Step root = (Step) m.getProject ().getRoot ();
		GScene s = root.getScene ();
		if (! (s instanceof EconStand)) {return false;}

		return true;
	}

	/**
	 * From EconomicFunction.
	 * Computation of the expense.
	 */
	public double getResult () {
		try {
			return stand.getArea () / 10000 * cost;
		} catch (Exception e) {
			Log.println (Log.ERROR, "EconommicFunction.ExpensePerHectare.getResult ()"," Could not return Result due to exception", e);
			return -1;
		}	
	}

	/**
	 * From EconomicFunction.
	 */
	public String getFunctionParameters () {
		return getName ()
				+", "+Translator.swap ("ExpensePerHectareDialog.cost")+"="+cost
				;
	}
	/**
	* From EconomicFunction.
	*/
	public String getParametersList (){
		return Translator.swap ("ExpensePerHectareDialog.cost")+separator;
	}

	/**
	 * From Extension interface.
	 */
	public String getName () {
		return Translator.swap ("ExpensePerHectare");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "C. Orazio";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("ExpensePerHectare.description");}

}

