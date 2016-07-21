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

import java.util.Arrays;
import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.CommonEconFunctions;
import capsis.lib.economics.EconModel;
import capsis.lib.economics.EconStand;
import capsis.util.CancelException;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.TreeVProvider;
import capsis.util.methodprovider.VProvider;

/**
 * Calculate volume of removed wood during an intervention using getV function before and after intervention
 * A price is given to the wood removed : value
 *
 * @author C. Orazio - Decemeber 2005
 */
public class IncomeFunction extends Income {

	protected Double A, B, C, D;
	protected String[] selectedTrees = new String[CommonEconFunctions.possibleTreesStatus]; // []cut, dead, ....
	protected String type;// Equation type 1, 2, ....
	//private GStand stand;	stand is defined in EconomicFunction
	//private GModel model;

	static {
		Translator.addBundle("capsis.extension.economicfunction.IncomeFunction");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public IncomeFunction () {}

	/**
	 * Official constructor. Uses an ExtensionStarter.
	 */
	public IncomeFunction (GenericExtensionStarter s) throws Exception {
		try {
			stand = s.getScene ();	// stand is defined in EconomicFunction
			model = s.getModel ();	// model is defined in EconomicFunction
			IncomeFunctionStarter p =null;
			// 1. Start mode according to context : interactive or not
			if (s instanceof IncomeFunctionStarter) {

				// 2. Script mode starter
				p = (IncomeFunctionStarter) s;
				

			} else {
				Log.println ("Try to open IncomeFunctiondialog");
				// 3. Interactive start
				IncomeFunctionDialog dlg = new IncomeFunctionDialog (model);

				if (dlg.isValidDialog ()) {
					// valid -> ok was hit and check were ok
					p = dlg.getValue ();
				} else {
					throw new CancelException ();
				}
				dlg.dispose ();

			}
			A = p.A;
			B = p.B;
			C = p.C;
			D = p.D;
			type = p.type;
			selectedTrees = p.selectedTrees;
			
		} catch (Exception exc) {
			if (! (exc instanceof CancelException)) {	// do not log if cancel
				Log.println (Log.ERROR, "IncomeFunction.c ()", exc.toString (), exc);
			}
			throw exc;
		}

	}

	/**
	 * String constructor. Uses an ExtensionStarter+ string
	 **
	public IncomeFunction (ExtensionStarter s, String stringParameters) throws Exception {
		try {
			stand = s.getStand ();	// stand is defined in EconomicFunction
			model = s.getModel ();	// model is defined in EconomicFunction
			//value = new Double (CommonEconFunctions.getValueFromString(stringParameters, 1, EconomicFunction.separator)).doubleValue();
			int i = 1;
			while (CommonEconFunctions.getValueFromString(stringParameters, i, separator)!=null && CommonEconFunctions.getValueFromString(stringParameters, i+1, separator)!=null){
				downLimitAndPrice.put (CommonEconFunctions.getValueFromString(stringParameters, i, separator),CommonEconFunctions.getValueFromString(stringParameters, i+1, separator));
				i+=2;
			}
		} catch (Exception exc) {
			if (! (exc instanceof CancelException)) {	// do not log if cancel
				Log.println (Log.ERROR,"String constructor "+"ExpenseConstant.c ()", exc.toString (), exc);
			}
			throw exc;
		}

	}/*

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
		if (! (s instanceof TreeList)) {return false;}

		MethodProvider mp = m.getMethodProvider ();
		if (mp == null) {return false;}
		if (! (mp instanceof TreeVProvider)) {return false;}
		//if (! (mp instanceof TreeNProvider)) {return false;}


		return true;
	}

	/**
	 * From EconomicFunction.
	 * Computation of the expense using getV function of model
	 */
	public static String TYPE1 ="A + BV + CV^2 + DV^3";
	public static String TYPE2 ="V * (A + B log10(C * V/n + D))";//A=29; B=14; C=1; D=0; VolTot * (14.039 * Log(VolUnit) + 29.481)
	public double getResult () {

		double result=0;
		double V = 0;
		double n = 0;	// fc - 22.8.2006 - Numberable is double
		Collection trees = null;
		if (type == TYPE1){
			try {trees = ((TreeList) stand).getTrees (selectedTrees);} catch (Exception e) {}
			V = ((VProvider) model.getMethodProvider ()).getV (stand, trees);			
			result =  A + B * V + C * Math.pow(V,2) + D * Math.pow(V,3);
					
		}else if (type == TYPE2 && (model.getMethodProvider () instanceof NProvider)){
			try {trees = ((TreeList) stand).getTrees (selectedTrees);} catch (Exception e) {}
			V = ((VProvider) model.getMethodProvider ()).getV (stand, trees);
			n = ((NProvider) model.getMethodProvider ()).getN (stand, trees);
			if (n!=0){ result =    V * (A + B * Math.log10(C * V/n + D));};
		
		} else {
			result = 0;
		}
		
		Log.println (Log.INFO, "IncomeFunction", "result="+result +" for Type : "+type + " with  A="+ A +" B="+ B+" C="+ C +" D="+ D + " V ="+V + " n="+n + " selectedTrees="+Arrays.toString(selectedTrees) + " VProvider="+( model.getMethodProvider () instanceof VProvider)+ " NProvider="+(model.getMethodProvider () instanceof NProvider) );
		return result;
	}

	/**
	 * From EconomicFunction.
	 */
	public String getFunctionParameters () {
		return getName ()
				+", Type="+type+" A="+A + " B="+B+" C="+C +" D=" +D + " selected="+ Arrays.toString(selectedTrees);//CommonEconFunctions.createStringFromArray (selectedTrees, CommonEconFunctions.possibleTreesStatus);
	}
	/**
	* From EconomicFunction.
	*/
	public String getParametersList (){
		return Translator.swap ("IncomeFunctionDialog.valueList")+separator;
	}

	/**
	 * From Extension interface.
	 */
	public String getName () {
		return Translator.swap ("IncomeFunction");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "C. Orazio";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("IncomeFunction.description");}

}

