/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Philippe Dreyfus
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
package capsis.extension.intervener;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import jeeb.lib.util.annotation.RecursiveParam;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.TreeDbhThenIdComparator;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.RDIProvider;
import capsis.util.methodprovider.RandomGeneratorProvider;
import capsis.util.methodprovider.RdiProviderEnhanced.RdiTool;

/**
 * Create a Thinner using the rdi, and a objective rdi
 * Algorithm is by J.-F. Dh�te
 *
 * @author P. Vallet - April 2003
 */
public class RdiThinner implements Intervener, Automatable {

	private static final long serialVersionUID = 20100223L;

	static public String NAME = "RdiThinner";
	static public String VERSION = "1.1";
	static public String AUTHOR =  "P. Vallet";
	static public String DESCRITPION = "RdiThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	static {
		Translator.addBundle ("capsis.extension.intervener.RdiThinner");
	}

	static public final double DEFAULT_THINNING_COEF = 0.7;
	static public final double HIGH_THINNING_COEF = 2.;
	public static class Data implements Serializable, Automatable {
		private static final long serialVersionUID = 20100223L;

		// Default values for the dialog :
		public double objectiveRdi = -1.;
		public double thinningCoef = DEFAULT_THINNING_COEF;

		// TODO : obsolete field :
		public String typeOfThinning = null;
	}

	@Ignore
	private GModel model;
	@Ignore
	private GScene stand;

	/**
	 * Thinning parameters
	 * (public for Automation only, should be private).
	 */
	@RecursiveParam
	public Data m_data = null;	// should be private
	@Ignore
	protected boolean ok = false;

	@SuppressWarnings("rawtypes")
	public static class Couple implements Comparable {

		private double prob;
		private int id;

		public Couple (double p, int n) {
			prob = p;
			id = n;
		}

		public double getProb () {return prob;}
		public int getId () {return id;}
		public void setProb (double x) {prob = x;}
		public void setId (int n) {id = n;}

		// for Comparable interface
		public int compareTo (Object o1) {

			if (! (o1 instanceof Couple)) {
				throw new ClassCastException ("Object is not a GTree : "+o1);
			}
			if (((Couple) o1).getProb () > prob) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public RdiThinner () {}

	public RdiThinner (Data data) {
		this.m_data = data;
		ok = true;
	}

	/**
	 * From Intervener.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		model = m;
		stand = scene;
	}

	/**
	 * From Intervener.
	 */
	@Override
	public boolean initGUI() {
		RdiThinnerDialog box = new RdiThinnerDialog (stand, model);
		ok = false;
		if( box.isValidDialog () ) {
			m_data = box.getData ();
			ok = true;
		}
		box.dispose ();
		return ok;
	}

	@Override
	public void activate() {}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (! (referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (! (s instanceof TreeCollection)) {return false;}
//			TreeCollection tc = (TreeCollection) s;
			//if (! (tc.getTrees ().iterator ().next () instanceof GMaidTree)) {return false;}

			MethodProvider mp = m.getMethodProvider ();
			if (! (mp instanceof NProvider)) {return false;}
			if (! (mp instanceof RDIProvider)) {return false;}
			if (! (mp instanceof DgProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "RdiThinner.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * From Intervener.
	 * Control input parameters.
	 */
	@Override
	public boolean isReadyToApply () {
		return ok;
	}

	/**
	 * From Intervener.
	 * Makes the action : thinning.
	 */
	@Override
	public Object apply () throws Exception {
		thin (stand, model, m_data);
		return stand;
	}

	/**
	 * Makes the action : thinning.
	 * (public static method used in Fagacees module)
	 */
	public static void thin (GScene stand, GModel model, Data data) {
		if (data != null) {
			double thinningCoef = data.thinningCoef;
			if ("High".equals (data.typeOfThinning)) {
				thinningCoef = HIGH_THINNING_COEF;
			}
			thin (stand, model, data.objectiveRdi, thinningCoef);
		}
	}

//	/**
//	 * This private method returns the rdi. The rdi calculation is based on the instantiation of the method provider.
//	 * @param stand a GScene instance
//	 * @param mp a MethodProvider object
//	 * @param nbTrees a double
//	 * @param mdq a double
//	 * @return the relative density index (rdi)
//	 */
//	private static double getRdiDependingOnMethodProviderInstance(GScene stand, GModel model, double nbTreesHa, double mdq) {
//		try {
//			double partialRdi;
//			MethodProvider mp = model.getMethodProvider();
//			if (mp instanceof RdiProviderEnhanced) {
//				partialRdi = ((RdiProviderEnhanced) mp).getRDI((Speciable) stand, nbTreesHa, mdq);
//			} else {
//				partialRdi = ((RDIProvider) mp).getRDI (model, nbTreesHa, mdq, null);
//			}
//			return partialRdi;
//		} catch (Exception e) {
//			return -1d;
//		}
//	}
	
	
	/**
	 * Makes the action : thinning.
	 * (public static method used in Fagacees module)
	 * @param targetObjectiveRatio = 0 to 1 for standard thinning (1 = pure low thinning,
	 * 0 = thinning decreases linearly with diameter), 2 for highThinning (thinning does not
	 * depend on diameter)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void thin (GScene stand, GModel model, double objectiveRdi,
			double targetObjectiveRatio)
	{
		if (targetObjectiveRatio < 0.) {
			targetObjectiveRatio = DEFAULT_THINNING_COEF;
		}
		boolean standardThinning = targetObjectiveRatio <= 1.;

		// 1: To calculate the population that can be thinned.
		TreeCollection tc = (TreeCollection) stand;
		if (tc.getTrees ().size () <= 0) {return;}

		// fc - 9.4.2004 - method providers now need (stand, trees)
		Collection auxTrees = tc.getTrees ();

		Object [] trees = auxTrees.toArray ();
		Arrays.sort (trees, new TreeDbhThenIdComparator (false));  // sort in descending order
		MethodProvider mp = model.getMethodProvider ();

		// if the thinning type is "standard", we must calculate the population that can be thinned
		// supThDiameter is the diameter of the the bigger tree of the thinned population
		double supThDiameter = ((Tree) trees [0]).getDbh ();
		if (standardThinning) {

			double targetRdi = targetObjectiveRatio * objectiveRdi;

			int nbTree = 1; // nbTree is the number of trees over supThDiameter
			double partialDg = supThDiameter;
			
			double partialRdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
					model, 
					nbTree * 10000 / stand.getArea(), 
					partialDg);
//			if (mp instanceof RdiProviderEnhanced) {
//				partialRdi = ((RdiProviderEnhanced) mp).getRDI((Speciable) stand, nbTree * 10000 / stand.getArea(), partialDg);
//			} else {
//				partialRdi = ((RDIProvider) mp).getRDI (model, nbTree / (stand.getArea ()/10000d), partialDg, null);
//			}

			while (partialRdi < targetRdi && nbTree < trees.length) {
				nbTree++;
				supThDiameter = ((Tree) trees [nbTree-1]).getDbh (); // trees first index is 0 !
				partialDg = Math.sqrt ((1.0/nbTree)* ((nbTree-1.0)*partialDg*partialDg + supThDiameter*supThDiameter) );
				partialRdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
						model, 
						nbTree * 10000 / stand.getArea(), 
						partialDg);
//				partialRdi = ((RDIProvider) mp).getRDI (model, nbTree/ (stand.getArea ()/10000d), partialDg, null);
			}
		}

		// if the thinning type is "High", every tree can be thinned
		// --> so nothing to do.

		// 2: To make the thinning in the population defined above.
		// Initialization of the variables
		double dg = ((DgProvider) mp).getDg (stand, auxTrees);
		double N = ((NProvider) mp).getN (stand, auxTrees);	// fc - 22.8.2006 - Numberable is double
		double rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
				model, 
				N * 10000 / stand.getArea(), 
				dg);
//		if (mp instanceof RdiProviderEnhanced) {
//			rdi = ((RdiProviderEnhanced) mp).getRDI((Speciable) stand, N * 10000 / stand.getArea(), dg);
//		} else  {
//			rdi = ((RDIProvider) mp).getRDI (model, N / (stand.getArea() / 10000d), dg, null);
//		}

		Random randomGenerator;
		if (mp instanceof RandomGeneratorProvider) {
			randomGenerator = ((RandomGeneratorProvider) mp).getRandomGenerator (model);
		} else {
			randomGenerator = new Random ();
		}

		Couple [] prob_id = new Couple [tc.getTrees ().size ()];

		// Note : we use trees (sorted array) in place of tc.getTrees (unsorted) to get repeatable results
		// (=> two simulations with the same random seed should give the same results)
		int compt = 0;
		for (int n = 0; n < trees.length; ++ n) {
			Tree t = (Tree) trees [n];
			double prob = randomGenerator.nextDouble ();
			if (standardThinning) {
				prob *= supThDiameter - t.getDbh ();
			}
			prob_id [compt] = new Couple (prob, t.getId ());
			compt ++;
		}

		Arrays.sort (prob_id);

		// This loop starts with the 1st tree to be thinned and count the number of tree that will be : compt
		compt=0;
		while (rdi > objectiveRdi && compt < prob_id.length) {
			Tree t = tc.getTree (prob_id [compt].getId ());
			dg = Math.sqrt ((1.0/ (N-1))* (N*dg*dg - t.getDbh ()*t.getDbh ()) );
			N--;
			rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
					model, 
					N * 10000 / stand.getArea(), 
					dg);
//			rdi = ((RDIProvider) mp).getRDI (model, N/ (stand.getArea ()/10000d), dg, null);
			compt++;
		}

		// This loop goes from the first tree to be thinned to the last to be, and then remove them from tc.
		for (int i = 0; i<compt; i++) {
			Tree t = (Tree) tc.getTree (prob_id [i].getId ());
			// fc - 18.3.2004		// changed 2011-03-08 was called after the removeTree method. Better have it here.
			if (! (t instanceof Numberable)) { ((TreeList) stand).storeStatus (t, "cut");}
			
			tc.removeTree (t);
		}

		stand.setInterventionResult (true);

		//~ System.out.println ("RdiThinner: " + typeOfThinning + " thinning of " + compt
				//~ + " trees at age " + stand.getDate ());
	}



	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString () {
		return getClass ().getName ();
	}

	/**
	 * Human readable text info.
	 */
	public String getInfo () {
		String s = "rdi thinning";
		if (m_data != null) {
			s += " to rdi=" + m_data.objectiveRdi +
				" coef=" + m_data.thinningCoef;
		} else {
			s += "(null data)";
		}
		return s;
	}

}

