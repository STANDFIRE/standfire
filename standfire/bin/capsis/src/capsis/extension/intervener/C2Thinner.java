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

package capsis.extension.intervener;

import java.util.Arrays;
import java.util.Collection;

import jeeb.lib.util.Alert;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.NumberableTree;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.TreeDbhComparator;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.HdomProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.RDIProvider;
import capsis.util.methodprovider.RdiProviderEnhanced.RdiTool;
import capsis.util.methodprovider.SHBProvider;
import capsis.util.methodprovider.TreeVProvider;
import capsis.util.methodprovider.VProvider;

// ---------------------------------------------------------------------------------------------------------------------------->
// 											C2Thinner														>
// 											C2Thinner														>
// 											C2Thinner														>
// 											C2Thinner														>
// ---------------------------------------------------------------------------------------------------------------------------->
/**
 * Create a C2Thinner, similar to CAPSIS 2.x thinner by F.-R. Bonnet
 * Algorithms are by F.-R. Bonnet
 * For interactive mode, use constructor with ExtensionStarter (containing stand to
 * thin and mode CUT/MARK trees). A dialog box is showed to get user choices.
 * For console mode, use the other constructor with specific paramater
 * C2ThinnerStarter.
 *
 * @author Ph. Dreyfus - March 2001 - October 2004
 */
public class C2Thinner implements Intervener {

	public static final String NAME = "C2Thinner";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "Ph. Dreyfus";
	public static final String DESCRIPTION = "C2Thinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	private boolean constructionCompleted =false;		// if cancel in interactive mode, false
	private int mode;				// CUT or MARK
	private GScene stand;			// Reference stand: will be altered by apply ()
	private GModel model;			// Associated model

	private	TreeCollection tc;

	private Object[] trees;

	private double targetKg;  	// target Kg
	private double targetStocking;		// target N or G ...
	private int Sm; 				// Stocking measure chosen : N, G, V, R(DI), or S(HB)

	private double Kg; // current Kg during process
	private double miniKg;

	private int nbClasses;
	private double pointCritique;

	private double Area_ha;

	private int NhaBefore;
	private double GhaBefore;
	private double VhaBefore;
	private double SHBBefore;

	private int NhaAfter;
	private double GhaAfter;
	private double VhaAfter;
	private double RDIAfter;
	private double SHBAfter;

	private int [] mem;
	private double [] memha;
	private double [] cut_rate;
	private double [] diam;
	private double [] height;

	protected MethodProvider mp;

	static {
		Translator.addBundle ("capsis.extension.intervener.C2Thinner");
	}


	// ---------------------------------------------------------------------------------------------------------------------------->
	// 						Constructor for Interactive Mode														>
	// 						Constructor for Interactive Mode														>
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public C2Thinner () {}

	/**
	 *
	 * @param tKg
	 * @param tStocking : N or G
	 * @param psm  : Stocking measure chosen : N, G, V, R(DI), or S(HB)
	 */
	public C2Thinner (double tKg, double tStocking, int psm) {
		targetKg = tKg;
		targetStocking = tStocking;
		Sm = psm;
		constructionCompleted = true;

	}



	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		model = m;
		stand = scene;

		tc = (TreeCollection) stand;

		nbClasses = tc.getTrees ().size(); // Number of "trees" (i.e. classes) in the stand
		//nbClasses = ((TreeCollection) stand).getTrees ().size(); // Number of "trees" (i.e. classes) in the stand
		Area_ha = stand.getArea() / 10000d;

		mem = new int [nbClasses];
		memha = new double [nbClasses];
		cut_rate = new double [nbClasses];
		diam = new double [nbClasses];
		height = new double [nbClasses];

		miniKg = 0;
		//Kg = 1;
		Kg = 0.9999;		// PhD 2004-10-14

		// Retrieve method provider
		//mp = MethodProviderFactory.getMethodProvider (model);
		mp = model.getMethodProvider ();

		// 0. Define mode : ask model
		if (model.isMarkModel ()) {
			mode = MARK;
		} else {
			mode = CUT;
		}

		// Sorting is necessary
		trees = tc.getTrees ().toArray ();
		Arrays.sort (trees, new TreeDbhComparator (true));  // sort in ascending order
		//(only in order to have a loop on trees starting with the smallest (not the case with an Iterator))

		// Keep initial Numbers :
		for(int k = 0; k < trees.length; k++) {
			NumberableTree t = (NumberableTree) trees[k];
			mem[k] = (int) t.getNumber();	// fc - 22.8.2006 - Numberable returns double
			memha[k] = ((int) t.getNumber()) / Area_ha;	// fc - 22.8.2006 - Numberable returns double
			diam[k] = t.getDbh();
			height[k] = t.getHeight();
			cut_rate[k] = 0;
		}

		double N = 0, G = 0, V = 0;
		double pi_40000 = Math.PI/40000;
		int i = -1; // for access memha[0]
		while(++i < nbClasses)  {
			N += memha[i];
			G += memha[i] * Math.pow(diam[i],2) * pi_40000;  // Pi/4  / 10000 (100 x 100 because diam is in cm)
			V += memha[i] * ((TreeVProvider) mp).getTreeV(diam[i],height[i],stand);				// N.B. : will use the dominant (or main) species of the stand
		}
		setNhaBefore(N);
		setGhaBefore(G);
		setVhaBefore(V);
		setSHBBefore(10746.0/( ((HdomProvider) mp).getHdom(stand, tc.getTrees ()) * Math.pow(N,0.5) ));

	}

	@Override
	public boolean initGUI() throws Exception{

		constructionCompleted = false;
		// 1. Create dialog box to catch the thinning parameters
		C2DThinParameters thinningParameters = new C2DThinParameters ( stand, model, this );

		targetKg = thinningParameters.getTargetKg();
		targetStocking = thinningParameters.getTargetStocking ();
		Sm = thinningParameters.getSm();

		//Log.println ("targetKg = "+targetKg+" targetStocking = "+targetStocking+" Sm = "+Sm);

		if (thinningParameters.isValidDialog ()) {
			constructionCompleted = true;
		}
		thinningParameters.dispose ();
		return constructionCompleted;

	}


	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	@SuppressWarnings("rawtypes")
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeCollection)) {return false;}
			TreeCollection tc = (TreeCollection) s;
			//~ if (!(tc.getTrees ().iterator ().next () instanceof GMaidTree)) {return false;}

			// Check we have only Numberable Gtree instances in collection
			// fc - 18.3.2004
			//
			Collection reps = Tools.getRepresentatives (tc.getTrees ());	// one object for each class in collection
			if (!(	reps != null
					&& reps.size () == 1
					&& reps.iterator ().next () instanceof Tree
					&& reps.iterator ().next () instanceof Numberable)) {return false;}

			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof NProvider)) {return false;}
			if (!(mp instanceof GProvider)) {return false;}
			if (!(mp instanceof VProvider)) {return false;}
			//if (!(mp instanceof RDIProvider)) {return false;}	// seems to be optional (fc)
			if (!(mp instanceof SHBProvider)) {return false;}
			if (!(mp instanceof DgProvider)) {return false;}
			if (!(mp instanceof TreeVProvider)) {return false;}
			if (!(mp instanceof HdomProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "C2Thinner.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------------------->
	// 						assertionsAreOk																		>
	// 						assertionsAreOk																		>
	// 						assertionsAreOk																		>
	// ---------------------------------------------------------------------------------------------------------------------------->
	//
	// These assertions must be checked before apply.
	//
	private boolean assertionsAreOk () {
		if (mode != CUT && mode != MARK) {
			Log.println (Log.ERROR, "C2Thinner.assertionsAreOk ()", "Wrong mode="+mode
					+", should be "+CUT+" (CUT) or "+MARK+" (MARK). C2Thinner is not appliable.");
			return false;
		}
		if (model == null) {
			Log.println (Log.ERROR, "C2Thinner.assertionsAreOk ()",
			"model is null. C2Thinner is not appliable.");
			return false;
		}
		if (stand == null) {
			Log.println (Log.ERROR, "C2Thinner.assertionsAreOk ()",
			"stand is null. C2Thinner is not appliable.");
			return false;
		}
		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------------------->
	// 						isReadyToApply																		>
	// 						isReadyToApply																		>
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * From Intervener.
	 * Control input parameters.
	 */
	public boolean isReadyToApply () {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk ()) { return true;}
		return false;
	}

	// ---------------------------------------------------------------------------------------------------------------------------->
	// 									apply																		>
	// 									 ppl 																		>
	// 									  p  																		>
	// 									 ppl 																		>
	// 									apply																		>
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * From Intervener.
	 * Makes the action : thinning.
	 */
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {
			throw new Exception ("C2Thinner.apply () - Wrong input parameters, see Log");
		}

		computeThinning ();

		UpdateTC();

		// Status for the trees which number was just reduced - fc - 5.7.2005
		for (int k = 0; k < mem.length; k++) {
			Numberable n = (Numberable) trees[k];
			double n0 = mem[k];			// fc - 22.8.2006 - Numberable returns double
			double n1 = n.getNumber ();	// fc - 22.8.2006 - Numberable returns double
			double c = n0 - n1;			// fc - 22.8.2006 - Numberable returns double
			if (c > 0) {
				((TreeList) stand).storeStatus (n, "cut", c);
			}
		}

		stand.setInterventionResult (true);
		return stand;
	}

	public void computeThinning () {
		System.out.println ("targetKg = "+targetKg+" targetStocking = "+targetStocking+" Sm = "+Sm);
		System.out.println ("Avant CalcKg");
		Kg = CalcKg();

		System.out.println ("Avant CalcMiniKg");
		miniKg = CalcMiniKg(false);

		System.out.println ("C2Thinner D�but apply - Kg = "+Kg+" miniKg = "+miniKg);
		ConvergeWithKg();

		System.out.println ("APRES ConvergeWithKg  - Kg = "+Kg+" miniKg = "+miniKg);

		double N = 0, G = 0, V = 0;
		double pi_40000 = Math.PI/40000;
		int i = -1;
		while(++i < nbClasses)  {
			N += memha[i] * ((100d - cut_rate[i]) / 100);
			G += memha[i] * ((100d - cut_rate[i]) / 100) * Math.pow(diam[i],2) * pi_40000;  // Pi/4  / 10000 (100 x 100 because diam is in cm)
			V += memha[i] * ((100d - cut_rate[i]) / 100) * ((TreeVProvider) mp).getTreeV(diam[i],height[i],stand);  // N.B. : will use the dominant (or main) species of the stand
		}
		setNhaAfter(N);
		setGhaAfter(G);
		setVhaAfter(V);
		setSHBAfter(10746.0/( ((HdomProvider) mp).getHdom(stand, tc.getTrees ()) * Math.pow(N,0.5) ));
		System.out.println (" NhaAfter : "+getNhaAfter()+" GhaAfter : "+getGhaAfter()+" NhaAfter : "+getVhaAfter()+" SHNAfter : "+getSHBAfter());

	}




	// Update numbers for each tree according to the current cutting rates ------------------------------------->
	public void UpdateTC() {
		for (int i = 0; i < nbClasses; i++) {
			NumberableTree t = (NumberableTree) trees[i];
			t.setNumber( (int) (mem[i] * (100d - cut_rate[i]) / 100 + 0.5) );
		}
	}

	// Convergence vers une valeur de Kg ------------------------------------->
	@SuppressWarnings("rawtypes")
	public void ConvergeWithKg() {
		double Erreur = targetKg - Kg + 0.05; // +0.05 sinon le calcul ne se fait pas, les cut_rate ne sont pas mis � jour PhD 2004-10-14
		double pasA = 0.01, eps = 0.001, a = 2 * targetKg - 0.2;
		double anteErreur = 1, doubleAnteErreur = 1, roundKg = (long)(miniKg * 100.0) / 100.0;
		int cpt = -1;
		if(targetKg < roundKg) {
			//			JOptionPane.showMessageDialog (MainFrame.getInstance (), Translator.swap ("C2Thinner.targetKgSetToKgMini"),
			//					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			Log.println (Log.WARNING, "C2Thinner.ConvergeWithKg ()",
					Translator.swap ("C2Thinner.targetKgSetToKgMini"));
			Alert.print (Translator.swap ("C2Thinner.targetKgSetToKgMini"));

			targetKg = roundKg;
		}
		if(targetKg == roundKg) { AjustSurKMini(); return; }
		//if(targetKg >= 1) { AjustSurKMaxi(); return; }
		if(targetKg >= 0.9995) { AjustSurKMaxi(); return; }			// PhD 2004-10-14
		if(targetKg < 0.9) pasA = 0.05;
		while(Math.abs(Erreur) > eps) {
			//(Erreur >= 0) ? a += pasA : a -= pasA;		// non accept� par Java !?
			if (Erreur >= 0) { a += pasA; } else { a -= pasA; }
			CourbeAdjust(a);
			UpdateTC();

			// fc - 9.4.2004 - method providers now need (stand, trees)
			Collection trees = tc.getTrees ();

			NhaAfter = (int) (((NProvider) mp).getN(stand, trees) / Area_ha);
			GhaAfter = ((GProvider) mp).getG(stand, trees) / Area_ha;	// fc - 24.3.2004
			VhaAfter = ((VProvider) mp).getV(stand, trees) / Area_ha;
			
//			if(mp instanceof RDIProvider) RDIAfter = ((RDIProvider) mp).getRDI(model,
//					((NProvider) mp).getN(stand, trees)/Area_ha,
//					((DgProvider) mp).getDg(stand, trees),
//					null);
			
			if (mp instanceof RDIProvider) {
				RDIAfter = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
						model,
						((NProvider) mp).getN(stand, trees)/Area_ha,
						((DgProvider) mp).getDg(stand, trees));
			}
			
			
			SHBAfter = ((SHBProvider) mp).getSHB(stand, trees);
			Converge();
			Erreur = targetKg - CalcKg();
			if((Erreur == doubleAnteErreur) | (signe_de(Erreur) != signe_de(anteErreur))) if(pasA > 0.0001) pasA /= 2d;
			doubleAnteErreur = anteErreur;
			anteErreur = Erreur;
			if(++cpt > 20) eps += 0.005;
		}
		Kg = CalcKg();
		//Log.println("Kg : "+Kg + "\t targetKg : "+targetKg);
		if((Kg > (targetKg + 0.01)) | (Kg < (targetKg - 0.01))) {
			//			JOptionPane.showMessageDialog (MainFrame.getInstance (), Translator.swap ("C2Thinner.targetKgWasNotReached"),
			//					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			Log.println (Log.WARNING, "C2Thinner.ConvergeWithKg ()",
					Translator.swap ("C2Thinner.targetKgWasNotReached"));
			Alert.print (Translator.swap ("C2Thinner.targetKgWasNotReached"));
		}
	}

	// Converge vers l'targetStocking ---------------------------------------------->
	void Converge() {
		if(Sm == 0) ConvergeN();
		if(Sm == 1) ConvergeG();
		if(Sm == 2) ConvergeV();
		if(Sm == 3) ConvergeRDI();
		if(Sm == 4) ConvergeSHB();
	}
	// Calcule le Kg minimal ------------------------------------------------->
	double CalcMiniKg(boolean set) {
		if(Sm == 0) return CalcMiniKgN(set);
		if(Sm == 1) return CalcMiniKgG(set);
		if(Sm == 2) return CalcMiniKgV(set);
		if(Sm == 3) return CalcMiniKgRDI(set);
		if(Sm == 4) return CalcMiniKgSHB(set);
		return 0;
	}

	// Calcule la nature de l'�claircie -------------------------------------->
	public double CalcKg() {
		int i = -1;
		double DgPeuSqr = 0, DgEclSqr = 0, nPeu = 0, nEcl = 0, registre, registre2;
		while(++i < nbClasses) {
			nPeu += mem[i];
			registre = mem[i] * cut_rate[i] / 100.0;
			registre2 = Math.pow(diam[i],2);
			nEcl += registre;
			DgPeuSqr += registre2 * mem[i];
			DgEclSqr += registre2 * registre;
		}
		if(nPeu==0 | nEcl==0) return 0;
		DgPeuSqr /= nPeu;
		DgEclSqr /= nEcl;
		if(DgPeuSqr != 0) return Math.min(1.0, DgEclSqr / DgPeuSqr);
		return 0;
	}

	// Converge vers N ------------------------------------------------------->
	@SuppressWarnings("rawtypes")
	public void ConvergeN() {
		double tauxEps = 1, erreur = NhaAfter - targetStocking;
		double erreurPrecedente;
		int i = -1, cpt = 1;
		while(Math.abs(erreur) > 0.5) {
			while(++i < nbClasses) {
				//(erreur >= 0) ? cut_rate[i] += tauxEps : cut_rate[i] -= tauxEps;		// non accept� par Java !?
				if (erreur >= 0) { cut_rate[i] += tauxEps; } else { cut_rate[i] -= tauxEps; }
				if(cut_rate[i] > 100) cut_rate[i] = 100;
				if(cut_rate[i] < 0) cut_rate[i] = 0;
			}
			erreurPrecedente = erreur;
			UpdateTC();

			// fc - 9.4.2004 - method providers now need (stand, trees)
			Collection trees = tc.getTrees ();

			NhaAfter = (int) (((NProvider) mp).getN(stand, trees) / Area_ha);
			erreur =  NhaAfter - targetStocking; i = -1;
			if(signe_de(erreurPrecedente) != signe_de(erreur)) tauxEps /= 2;
			if(++cpt > 1000) break;
		}
	}

	// Converge vers G ------------------------------------------------------->
	public void ConvergeG() {
		double tauxEps = 1, erreur = GhaAfter - targetStocking;
		double erreurPrecedente;
		int i = -1, cpt = 1;
		while(Math.abs(erreur) > 0.05) {
			while(++i < nbClasses) {
				if (erreur >= 0) { cut_rate[i] += tauxEps; } else { cut_rate[i] -= tauxEps; }
				if(cut_rate[i] > 100) cut_rate[i] = 100;
				if(cut_rate[i] < 0) cut_rate[i] = 0;
			}
			erreurPrecedente = erreur;
			UpdateTC();
			GhaAfter = ((GProvider) mp).getG(stand, tc.getTrees ()) / Area_ha;	// fc - 24.3.2004
			erreur =  GhaAfter - targetStocking; i = -1;
			if(cpt != 0 && (signe_de(erreurPrecedente) != signe_de(erreur))) tauxEps /= 2;
			if(++cpt > 1000) break;
		}
	}

	// Converge vers V ------------------------------------------------------->
	@SuppressWarnings("rawtypes")
	public void ConvergeV()	{
		double tauxEps = 1, erreur = VhaAfter - targetStocking;
		double erreurPrecedente;
		int i = -1, cpt = 1;
		while(Math.abs(erreur) > 0.05) {
			while(++i < nbClasses) {
				if (erreur >= 0) { cut_rate[i] += tauxEps; } else { cut_rate[i] -= tauxEps; }
				if(cut_rate[i] > 100) cut_rate[i] = 100;
				if(cut_rate[i] < 0) cut_rate[i] = 0;
			}
			erreurPrecedente = erreur;
			UpdateTC();

			// fc - 9.4.2004 - method providers now need (stand, trees)
			Collection trees = tc.getTrees ();

			VhaAfter = ((VProvider) mp).getV(stand, trees) / Area_ha;
			erreur =  VhaAfter - targetStocking; i = -1;
			if(signe_de(erreurPrecedente) != signe_de(erreur)) tauxEps /= 2;
			if(++cpt > 1000) break;
		}
	}

	// Converge vers RDI ----------------------------------------------------->
	@SuppressWarnings("rawtypes")
	public void ConvergeRDI() {
		double tauxEps = 1, erreur = RDIAfter - targetStocking;
		double erreurPrecedente;
		int i = -1, cpt = 1;
		while(Math.abs(erreur) > 0.005) {
			while(++i < nbClasses) {
				if (erreur >= 0) { cut_rate[i] += tauxEps; } else { cut_rate[i] -= tauxEps; }
				if(cut_rate[i] > 100) cut_rate[i] = 100;
				if(cut_rate[i] < 0) cut_rate[i] = 0;
			}
			erreurPrecedente = erreur;
			UpdateTC();

			// fc - 9.4.2004 - method providers now need (stand, trees)
			Collection trees = tc.getTrees ();

//			RDIAfter = ((RDIProvider) mp).getRDI(model,
//					((NProvider) mp).getN(stand, trees)/Area_ha,
//					((DgProvider) mp).getDg(stand, trees),
//					null);
			
			RDIAfter = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
					model, 
					((NProvider) mp).getN(stand, trees)/Area_ha,
					((DgProvider) mp).getDg(stand, trees));
			
			erreur = RDIAfter - targetStocking; i = -1;
			if(signe_de(erreurPrecedente) != signe_de(erreur)) tauxEps /= 2;
			if(++cpt > 1000) break;
		}
	}

	// Converge vers S ------------------------------------------------------->
	@SuppressWarnings("rawtypes")
	public void ConvergeSHB() {
		double tauxEps = 1, erreur = SHBAfter - targetStocking;
		double erreurPrecedente;
		int i = -1, cpt = 1;
		while(Math.abs(erreur) > 0.05) {
			//Log.println("ConvergeSHB cpt:"+cpt +" erreur:"+erreur + "\t SHBAfter:"+SHBAfter+ "\t targetStocking:"+targetStocking);
			while(++i < nbClasses) {
				if (erreur >= 0) { cut_rate[i] -= tauxEps; } else { cut_rate[i] += tauxEps; }
				if(cut_rate[i] > 100) cut_rate[i] = 100;
				if(cut_rate[i] < 0) cut_rate[i] = 0;
			}
			erreurPrecedente = erreur;
			UpdateTC();

			// fc - 9.4.2004 - method providers now need (stand, trees)
			Collection trees = tc.getTrees ();

			SHBAfter = ((SHBProvider) mp).getSHB(stand, trees);
			erreur = SHBAfter - targetStocking; i = -1;
			if(signe_de(erreurPrecedente) != signe_de(erreur)) tauxEps /= 2;
			if(++cpt > 1000) break;
		}
	}

	// Calcule le Kg minimal sur le nombre de tiges -------------------------->
	public double CalcMiniKgN(boolean set) {
		int i = -1;
		double DgPeuSqr = 0, DgEclSqr = 0, nPeu = 0, nEcl, registre;
		while(++i < nbClasses)  {
			nPeu += memha[i];
		}
		nEcl = nPeu - targetStocking;
		if(nEcl <= 0) return 0;
		i = -1;
		while(++i < nbClasses) {
			registre = Math.pow(diam[i],2);
			DgPeuSqr += registre * memha[i];
			if(set) {
				cut_rate[i] = 0;
			}
			if(nEcl==0 || (memha[i] <= 0)) continue;
			if(memha[i] <= nEcl) {
				DgEclSqr += registre * memha[i];
				nEcl -= memha[i];
				if(set && mem[i] != 0) cut_rate[i] = 100.0;
			}
			else {
				DgEclSqr += registre * nEcl;
				if(set) cut_rate[i] = nEcl / memha[i] * 100.0;
				pointCritique = i;
				nEcl = 0;
			}
		}
		DgPeuSqr /= nPeu;
		DgEclSqr /= (nPeu - targetStocking);
		if(DgPeuSqr != 0) return DgEclSqr / DgPeuSqr;
		return 0;
	}

	// Calcule le Kg minimal sur la surface terri�re ------------------------->
	public double CalcMiniKgG(boolean set) {
		int i = -1;
		double DgPeuSqr = 0, DgEclSqr = 0, nPeu = 0, nEcl = 0, gEcl, G = 0, registre;
		double pi_40000 = Math.PI/40000;
		while(++i < nbClasses)  {
			G += memha[i] * Math.pow(diam[i],2) * pi_40000;  // Pi/4  / 10000 (100 x 100 because diam is in cm)
		}
		gEcl = G - targetStocking;
		if(gEcl <= 0) return 0;
		i = -1;
		while(++i < nbClasses) {
			registre = Math.pow(diam[i],2);
			DgPeuSqr += registre * memha[i];
			nPeu += memha[i];
			G = registre * memha[i] * pi_40000;
			if(set) { cut_rate[i] = 0; }
			if(gEcl==0) continue;
			if(G <= gEcl) { DgEclSqr = DgPeuSqr; nEcl = nPeu; gEcl -= G; if(set && mem[i] != 0) cut_rate[i] = 100.0; continue; }
			registre = gEcl / pi_40000;
			DgEclSqr += registre;
			registre /= Math.pow(diam[i],2);
			if(registre > memha[i]) return 0;
			if(set) cut_rate[i] = registre / memha[i] * 100.0;
			nEcl += registre;
			pointCritique = i;
			gEcl = 0;
		}
		if(nPeu==0 | nEcl==0) return 0;
		DgPeuSqr /= nPeu;
		DgEclSqr /= nEcl;
		if(DgPeuSqr != 0) return DgEclSqr / DgPeuSqr;
		return 0;
	}
	// Calcule le Kg minimal sur le volume ----------------------------------->
	public double CalcMiniKgV(boolean set) {
		int i = -1;
		double DgPeuSqr = 0, DgEclSqr = 0, nPeu = 0, nEcl = 0, vEcl, V = 0, registre;
		while(++i < nbClasses)  {
			V += memha[i] * ((TreeVProvider) mp).getTreeV(diam[i],height[i],stand);	 // N.B. : will use the dominant (or main) species of the stand
		}
		vEcl = V - targetStocking;
		if(vEcl <= 0) return 0;
		i = -1;
		while(++i < nbClasses) {
			DgPeuSqr += Math.pow(diam[i],2) * memha[i];
			nPeu += memha[i];
			V = memha[i] * ((TreeVProvider) mp).getTreeV(diam[i],height[i],stand);      // N.B. : will use the dominant (or main) species of the stand
			if(set) {
				cut_rate[i] = 0;
			}
			if(vEcl==0) continue;
			if(V <= vEcl) { DgEclSqr = DgPeuSqr; nEcl = nPeu; vEcl -= V; if(set && mem[i] != 0) cut_rate[i] = 100.0; continue; }
			V = ((TreeVProvider) mp).getTreeV(diam[i],height[i],stand);     // N.B. : will use the dominant (or main) species of the stand
			if(V==0) return 0;
			registre = vEcl / V;
			if(registre > memha[i]) return 0;
			DgEclSqr += Math.pow(diam[i],2) * registre;
			if(set) cut_rate[i] = registre / memha[i] * 100.0;
			nEcl += registre;
			pointCritique = i;
			vEcl = 0;
		}
		if(nPeu==0 | nEcl==0) return 0;
		DgPeuSqr /= nPeu;
		DgEclSqr /= nEcl;
		if(DgPeuSqr != 0) return DgEclSqr / DgPeuSqr;
		return 0;
	}

	// Calcule le Kg minimal sur le CCF -------------------------->
	/*public double CalcMiniKgCCF(boolean set)	{  // NO MORE USED
		int i = -1;
		double DgPeuSqr = 0, DgEclSqr = 0, nPeu = 0, nEcl = 0, ccfEcl, CCF = 0, registre;
		while(++i < nbClasses) CCF += memha[i] * ((CCFProvider) mp).GetCCF(diam[i],thinClasses[i].houppier);
		ccfEcl = CCF - targetStocking * (100 * Area_ha);
		if(ccfEcl <= 0) return 0;
		i = -1;
		while(++i < nbClasses) {
			DgPeuSqr += Math.pow(diam[i],2) * memha[i];
			nPeu += memha[i];
			CCF = memha[i] * ((CCFProvider) mp).GetCCF(diam[i],thinClasses[i].houppier);
			if(set) {
				cut_rate[i] = 0;
			}
			if(!ccfEcl) continue;
			if(CCF <= ccfEcl) { DgEclSqr = DgPeuSqr; nEcl = nPeu; ccfEcl -= CCF; if(set && mem[i] != 0) cut_rate[i] = 100.0; continue; }
			CCF = ((CCFProvider) mp).GetCCF(diam[i],thinClasses[i].houppier);
			if(!CCF) return 0;
			registre = ccfEcl / CCF;
			if(registre > memha[i]) return 0;
			DgEclSqr += Math.pow(diam[i],2) * registre;
			if(set) cut_rate[i] = registre / memha[i] * 100.0;
			nEcl += registre;
			pointCritique = i;
			ccfEcl = 0;
		}
		if(!nPeu | !nEcl) return 0;
		DgPeuSqr /= nPeu;
		DgEclSqr /= nEcl;
		if(DgPeuSqr != 0) return DgEclSqr / DgPeuSqr;
		return 0;
	}   */

	// Calcule le Kg minimal sur le RDI -------------------------->
	public double CalcMiniKgRDI(boolean set) {
		int i = -1, j= -1;
		double DgAvSqr = 0, DgPeuSqr = 0, DgEclSqr = 0, nAv = 0, nPeu = 0, nEcl = 0, rdiEcl, RDIAv, RDIAp;
		while(++i < nbClasses) { DgAvSqr += memha[i] * Math.pow(diam[i],2); nAv += memha[i]; }
		
//		RDIAv = ((RDIProvider) mp).getRDI(model,nAv, Math.sqrt(DgAvSqr/nAv), null);
		RDIAv = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
				model, 
				nAv, 
				Math.sqrt(DgAvSqr/nAv));
		
		rdiEcl = RDIAv - targetStocking;
		if(RDIAv <= targetStocking) return 0;
		i = -1;
		while(++i < nbClasses) {
			DgPeuSqr += Math.pow(diam[i],2) * memha[i];
			nPeu += memha[i];
//			RDIAp = ((RDIProvider) mp).getRDI(model,
//					nAv - nPeu, Math.sqrt((DgAvSqr - DgPeuSqr)/(nAv - nPeu)), null);
			RDIAp = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
					model, 
					nAv - nPeu, 
					Math.sqrt((DgAvSqr - DgPeuSqr)/(nAv - nPeu)));
			if(set) {
				cut_rate[i] = 0;
			}
			if(rdiEcl==0) continue;
			if(RDIAp >= targetStocking) { DgEclSqr = DgPeuSqr; nEcl = nPeu; if(set && mem[i] != 0) cut_rate[i] = 100.0; continue; }
			DgPeuSqr -= Math.pow(diam[i],2) * memha[i];
			nPeu -= memha[i];
			while(++j < (int)(memha[i])) {
				DgPeuSqr += Math.pow(diam[i],2);
				nPeu++;
//				RDIAp = ((RDIProvider) mp).getRDI(model,
//						nAv - nPeu, Math.sqrt((DgAvSqr - DgPeuSqr)/(nAv - nPeu)), null);
				RDIAp = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
						model,
						nAv - nPeu, 
						Math.sqrt((DgAvSqr - DgPeuSqr)/(nAv - nPeu)));
				if(RDIAp <= targetStocking) break;
			}
			if(set) cut_rate[i] = j / memha[i] * 100.0;
			nEcl = nPeu;
			DgEclSqr = DgPeuSqr;
			pointCritique = i;
			rdiEcl = 0;
		}
		if(nAv==0 | nEcl==0) return 0;
		DgAvSqr /= nAv;
		DgEclSqr /= nEcl;
		if(DgAvSqr != 0) return DgEclSqr / DgAvSqr;
		return 0;
	}

	// Calcule le Kg minimal sur le facteur d'espacement -------------------------->
	@SuppressWarnings("rawtypes")
	public double CalcMiniKgSHB(boolean set) {

		// fc - 9.4.2004 - method providers now need (stand, trees)
		Collection trees = tc.getTrees ();

		double objectifOld = targetStocking, registre = Math.pow((10746.0/(((HdomProvider) mp).getHdom(stand, trees) * targetStocking)),2);
		if(registre >= ((NProvider) mp).getN(stand, trees)/Area_ha) return 0;
		targetStocking = registre;
		registre = CalcMiniKgN(set);
		targetStocking = objectifOld;
		return registre;
	}

	// Propose la valeur minimale de Kg -------------------------------------->
	@SuppressWarnings("rawtypes")
	public void AjustSurKMini() {
		if(targetStocking == 0) return;
		CalcMiniKg(true);
		UpdateTC();

		// fc - 9.4.2004 - method providers now need (stand, trees)
		Collection trees = tc.getTrees ();

		NhaAfter = (int) (((NProvider) mp).getN(stand, trees) / Area_ha);			/// USELESS ??????
		GhaAfter = ((GProvider) mp).getG(stand, trees) / Area_ha;
		VhaAfter = ((VProvider) mp).getV(stand, trees) / Area_ha;
		
//		if(mp instanceof RDIProvider) RDIAfter = ((RDIProvider) mp).getRDI(model,
//				((NProvider) mp).getN(stand, trees)/Area_ha,
//				((DgProvider) mp).getDg(stand, trees),
//				null);
		
		if (mp instanceof RDIProvider) {
			RDIAfter = RdiTool.getRdiDependingOnMethodProviderInstance(stand, 
					model, 
					((NProvider) mp).getN(stand, trees)/Area_ha, 
					((DgProvider) mp).getDg(stand, trees));
		}
		
		SHBAfter = ((SHBProvider) mp).getSHB(stand, trees);
		Kg = miniKg;			/// USELESS ??????
	}

	// Propose la valeur maximale de Kg -------------------------------------->
	public void AjustSurKMaxi() {
		if(targetStocking == 0) return;
		int i = -1;
		while(++i < nbClasses) cut_rate[i] = 0;
		Converge();
	}

	// Ajuste les taux de pr�l�vement sur les courbes de K ------------------->
	public void CourbeAdjust(double a) {
		int i = -1;
		double gain = 2.0 / (double)(nbClasses), x;
		double Ka = (double)(pointCritique + 1.5) * gain;
		if(Ka >= 2) Ka = 1;
		if(a < Ka) a = Ka;
		double b = Math.abs(20 * Math.pow((2*(a - Ka)/(2 - Ka) -1),15)) + 1;
		while(++i < nbClasses) {
			x = gain * (double)(i + 1);
			if(x > 2) x = 2;
			cut_rate[i] = 100.0 * Courbes(a,b,x) + 0.5;
		}
	}

	// Calcul des courbes de K ------------------------------------------------>
	public double Courbes(double a,double b,double x) {
		if(a == 0) a = 1;
		if(a <= 1) {
			if((x >= 0)&(x <= a)) return 1 - 0.5 * Math.pow(x / a, b);
			return 0.5 * Math.pow((2 - x)/(2 - a), b * (2 - a) / a);
		}
		if(a >= 2) a = 1;
		if((x >= 0)&(x <= a)) return 1 - 0.5 * Math.pow(x / a, b * a / (2 - a));
		return 0.5 * Math.pow((2 - x)/(2 - a), b);
	}

	// Retourne le signe d'un double --------------------------------------->
	double signe_de(double d) {
		if(d == 0) return 0;
		if(d < 0)  return -1;
		return 1;
	}

	public void setTargetKg (double x) { targetKg = x;	}
	public void setTargetStocking (double x) { targetStocking = x;	}
	public void setSm (int i) { Sm = i;	}

	public void setNhaBefore (double x) { NhaBefore = (int) (x+0.5);	}
	public void setGhaBefore (double x) { GhaBefore = x;	}
	public void setVhaBefore (double x) { VhaBefore = x;	}
	public void setSHBBefore (double x) { SHBBefore = x;	}

	public void setNhaAfter (double x) { NhaAfter = (int) (x+0.5);	}
	public void setGhaAfter (double x) { GhaAfter = x;	}
	public void setVhaAfter (double x) { VhaAfter = x;	}
	public void setSHBAfter (double x) { SHBAfter = x;	}

	public int getNhaBefore () { return NhaBefore;	}
	public double getGhaBefore () { return GhaBefore;	}
	public double getVhaBefore () { return VhaBefore;	}
	public double getSHBBefore () { return SHBBefore;	}

	public int getNhaAfter () { return NhaAfter;	}
	public double getGhaAfter () { return GhaAfter;	}
	public double getVhaAfter () { return VhaAfter;	}
	public double getSHBAfter () { return SHBAfter;	}

	public double getKg () { return Kg;	}
	public double getMiniKg () { return miniKg;	}


	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString () {
		return "class="+getClass().getName ()
		+" name=\""+NAME;
	}


	@Override
	public void activate() {}


}
