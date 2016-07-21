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

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.NumberableTree;
import capsis.defaulttype.Tree;
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
import capsis.util.methodprovider.TreeVProvider;

/**
 * Calculate volume of removed wood during an intervention using getV function before and after intervention
 * A price is given to the wood removed : value
 *
 * @author C. Orazio - january 2003
 */
public class IncomePerDiameterRange extends Income {

	protected SortedMap downLimitAndPrice;

	static {
		Translator.addBundle("capsis.extension.economicfunction.IncomePerDiameterRange");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public IncomePerDiameterRange () {}

	/**
	 * Official constructor. Uses an ExtensionStarter.
	 */
	public IncomePerDiameterRange (GenericExtensionStarter s) throws Exception {
		try {
			stand = s.getScene ();	// stand is defined in EconomicFunction
			model = s.getModel ();	// model is defined in EconomicFunction

			// 1. Start mode according to context : interactive or not
			if (s instanceof IncomePerDiameterRangeStarter) {

				// 2. Script mode starter
				IncomePerDiameterRangeStarter p = (IncomePerDiameterRangeStarter) s;
				downLimitAndPrice = p.downLimitAndPrice;

			} else {
				Log.println ("Try to open IncomePerDiameterRangedialog");
				// 3. Interactive start
				IncomePerDiameterRangeDialog dlg = new IncomePerDiameterRangeDialog ();

				if (dlg.isValidDialog ()) {
					// valid -> ok was hit and check were ok
					downLimitAndPrice = dlg.getValue ();
				} else {
					throw new CancelException ();
				}
				dlg.dispose ();

			}

		} catch (Exception exc) {
			if (! (exc instanceof CancelException)) {	// do not log if cancel
				Log.println (Log.ERROR, "IncomePerDiameterRange.c ()", exc.toString (), exc);
			}
			throw exc;
		}

	}

	/**
	 * String constructor. Uses an ExtensionStarter+ string
	 */
	public IncomePerDiameterRange (GenericExtensionStarter s, String stringParameters) throws Exception {
		try {
			stand = s.getScene ();	// stand is defined in EconomicFunction
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
		if (! (s instanceof TreeList)) {return false;}

		MethodProvider mp = m.getMethodProvider ();
		if (mp == null) {return false;}
		if (! (mp instanceof TreeVProvider)) {return false;}


		return true;
	}

	/**
	 * From EconomicFunction.
	 * Computation of the expense using getV function of model
	 */
	public double getResult () {

		String econTreesStatus = "cut";
		Collection trees = ((TreeList) stand).getTrees (econTreesStatus);

		double total = 0;
		SortedMap sousTable;
		
	
		if (trees.isEmpty()){
			Log.println (Log.WARNING, "IncomePerDiameterRange", "No tree collection");
			return 0;
		}
			for (Iterator i =  trees.iterator (); i.hasNext ();){
				Tree t = (Tree) i.next();
				// calcultate tree volume
				double tV =  ((TreeVProvider) model.getMethodProvider ()).getTreeV( t.getDbh(), t.getHeight(), stand); // Tree volume
				double n = 1;
				if (t instanceof Numberable){
					n = 0;
					n = ((NumberableTree) t).getNumber();
					tV = n*tV; // prise en compte des arbres � effectif
					}
				// calculate volume price
				double tp = 0; // tree price
				double tDbh = t.getDbh(); // tree diametre
				if (downLimitAndPrice.containsKey(tDbh)){
					tp = (Double) downLimitAndPrice.get(tDbh);
				} else {
					sousTable = downLimitAndPrice.headMap(tDbh);
					if (!sousTable.isEmpty()){
						tp = (Double) downLimitAndPrice.get(sousTable.lastKey());
						//Log.println (Log.INFO, "IncomePerDiameterRange", id_tree+" Diametre "+tDbh+" Price:"+ tp);
					} else {
						tp = (Double) downLimitAndPrice.get(downLimitAndPrice.firstKey());
						Log.println (Log.INFO, "IncomePerDiameterRange", "subtable is null Affect lower price:"+tp);
					}
				}
				// calculate total price
			total += tV*tp; 
			Log.println (Log.INFO,"IncomePerDiameterRange Volume ","Tree "+ t.getId()+ "DbH:"+tDbh+" prix :"+ tp +"Volume:"+tV+ " cumul : "+total);
			}			
		return total;	
		
				
		/*
		Step stepfather = (Step) stand.getStep().getFather();
		GStand standfather = stepfather.getStand();
		TreeHashtable tcf = null;
		TreeHashtable tc = null;
		GTree tree;
		GTree treeFather;
		double V = 0;
		Double p = new Double(0);
		double total = 0;
		SortedMap sousTable;
			try {
				tcf = (TreeHashtable) ((GTCStand) standfather).getTreeCollection();// Avant coupe
				tc = (TreeHashtable) ((GTCStand) stand).getTreeCollection();// Apr�s coupe
			} catch (Exception e) {
				Log.println (Log.ERROR, "IncomePerDiameterRange.getResult ()",
						"This method requires a GStand implementing TreeCollection interface.");
				return new Double("NEGATIVE_INFINITY").doubleValue();
			}

			if (tcf.isEmpty ()) {return 0d;}	// if no elements, V = 0
			try {
				for (Iterator i = tcf.getIds ().iterator (); i.hasNext ();) {

					int id_tree = ((Integer) i.next()).intValue();
					treeFather = tcf.getTree(id_tree);
					tree = tc.getTree(id_tree);
					Double treeFatherDbh = new Double(treeFather.getDbh());
					if (treeFather instanceof Numberable){
						int numberOfTrees = 0;
						int numberOfDeadTrees = 0;
						if (tree!=null) {
							numberOfTrees = ((GMaidTree) tree).getNumber();
							//numberOfDeadTrees = ((GMaidTree) tree).getNumberOfDead();
						}
						V = ((TreeVProvider) model.getMethodProvider ()).getTreeV(treeFather.getDbh(), treeFather.getHeight(), stand) * (((GMaidTree) treeFather).getNumber()-numberOfTrees);// dead trees are cut trees in PP3
						//V = ((TreeVProvider) model.getMethodProvider ()).getTreeV(treeFather.getDbh(), treeFather.getHeight(), stand) * (((GMaidTree) treeFather).getNumber()-numberOfTrees-numberOfDeadTrees);
						//System.out.println("nfather:"+((GMaidTree) treeFather).getNumber()+"n:"+numberOfTrees+" ndead:"+numberOfDeadTrees);
					} else {
						//V = ((TreeVProvider) model.getMethodProvider ()).getTreeV(tree.getDbh(), tree.getHeight(), standfather);
						V=0d; // � v�rifier pour d'autre types de mod�les
						//check the possibility of using 'contains' method on the map stand.getTrees("cut");
						Log.println (Log.INFO, "IncomePerDiameterRange", "Non instance of Numerable");
					}
					p=new Double(0d);
					if (downLimitAndPrice.containsKey(treeFatherDbh)){
						p = (Double) downLimitAndPrice.get(treeFatherDbh);
					} else {
						sousTable = downLimitAndPrice.headMap(treeFatherDbh);
						if (!sousTable.isEmpty()){
							p = (Double) downLimitAndPrice.get(sousTable.lastKey());
							Log.println (Log.INFO, "IncomePerDiameterRange", id_tree+" Diametre "+treeFatherDbh+" Price:"+ p);
						} else {
							p = (Double) downLimitAndPrice.get(downLimitAndPrice.firstKey());
							Log.println (Log.WARNING, "IncomePerDiameterRange", "subtable is null Affect lower price:"+p);
						}
					}

					total += V*p.doubleValue(); // voir comment faire la diff�rence avec volume apr�s coupe.
					Log.println (Log.INFO,"IncomePerDiameterRange Volume ", "Volume:"+V+" prix :"+ p + " cumul : "+total);
				}
				return total;		// V*p
			} catch (Exception e) {
				Log.println (Log.ERROR, "IncomePerDiameterRange.getResult ()", "Error while computing V*p", e);
				return 0d;
		}*/
	}

	/**
	 * From EconomicFunction.
	 */
	public String getFunctionParameters () {
		return getName ()
				+", "+Translator.swap ("IncomePerDiameterRangeDialog.valueList")+"="
				+ downLimitAndPrice.toString()
				;
	}
	/**
	* From EconomicFunction.
	*/
	public String getParametersList (){
		return Translator.swap ("IncomePerDiameterRangeDialog.valueList")+separator;
	}

	/**
	 * From Extension interface.
	 */
	public String getName () {
		return Translator.swap ("IncomePerDiameterRange");
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
	public String getDescription () {return Translator.swap ("IncomePerDiameterRange.description");}

}

