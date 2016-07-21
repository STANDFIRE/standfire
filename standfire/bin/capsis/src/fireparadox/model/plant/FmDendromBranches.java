package fireparadox.model.plant;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

// ----------------------------------------------------------------------------------------------------------------------------->
// 								  FIREPARADOX    D E N D R O M E T R I E   	2	>
// 								  FIREPARADOX    D E N D R O M E T R I E   	2	>
// 								  FIREPARADOX    D E N D R O M E T R I E   	2	>
// 								  FIREPARADOX    D E N D R O M E T R I E   	2	>
// ----------------------------------------------------------------------------------------------------------------------------->
/**
 * FiDendromBranches : static relations for branching,
 * Compute branches distribution of the trees
 * Also contains method to compute bulk density based on allometric relashionship
 * between diameter of
 *
 * @author Ph. Dreyfus - July 2009
 */
public class FmDendromBranches {


	

	/** ----------------------------------------------------------------------------------------------------------------------------------->
	* ------ contact : Ph.Dreyfus INRA URFM -------------------------------------------------------------------------------------------------------------->
	* 							S L U R P ! !									>
	*																	>
	* Get the successive age and height values of this tree, from its birth to current year				>
	* ATTENTION : ... supposing that the simulation step is 1 year								>
	*/
	public static Vector slurp (Tree tree) {
		System.out.println("Slurp");
		Vector curves = new Vector ();
		double h;
		int ag;
		Vector c1 = new Vector ();  // age
		Vector c2 = new Vector ();  // height
		try {
			Step step = (tree.getScene()).getStep ();

			if (step.isRoot()) {
				throw new Exception();
			}
			Vector steps = step.getProject ().getStepsFromRoot (step);
			int nb_steps = steps.size();

			for (int kont = 0; kont < nb_steps; kont++) { // ATTENTION : passés en revue dans l'ordre chronologique, depuis l'étape-racine
				Step stp = (Step) steps.get(kont);
				GScene stnd = stp.getScene ();
				Tree t = ((TreeCollection) stnd).getTree (tree.getId());

				if(stnd.isInterventionResult()) continue;

				if (stp.isRoot()) { // au 1er passage, on est à l'étape-racine, et on n'a pas de hauteur correspondant à une année antérieure
					for(ag = 1; ag < t.getAge(); ag++) {
						h = ag * t.getHeight() / t.getAge();
						c1.add (new Integer (ag));
						c2.add (new Double (h));
					}
				}

				ag = t.getAge();
				h = t.getHeight();
				c1.add (new Integer (ag));
				c2.add (new Double (h));
			}
		} catch (Exception e) { // càd création ou lecture
			for(ag = 1; ag <= tree.getAge(); ag++) {
				h = ag * tree.getHeight() / tree.getAge();
				c1.add (new Integer (ag));
				c2.add (new Double (h));
			}
		}

		curves.clear ();
		curves.add (c1);
		curves.add (c2);

		// Trace pour vérifier les séries de valeurs stockées :
		/*Vector ageSuccession = (Vector) curves.get (0);
		Vector heightSuccession = (Vector) curves.get (1);
		for(int kontt=0; kontt <= ageSuccession.size()-1; kontt++) {
			Double value = (Double) heightSuccession.get(kontt);
			double truc = value.doubleValue ();
			//Log.println("_FiPa","age "+kontt+" : "+ageSuccession.get(kontt)+" h "+kontt+" : "+R1(truc));
			//Log.println("age "+kontt+" : "+ageSuccession.get(kontt)+" h "+kontt+" : "+heightSuccession.get(kontt));
		}
		System.out.println("SLURP ageSuccession.size() : " + ageSuccession.size());
		*/

		return curves;
	}

	/**
	 * ------------------------------------------------------------------------
	 * -----------------------------------------------------------> ------
	 * contact : Ph.Dreyfus INRA URFM
	 * --------------------------------------------
	 * ------------------------------------------------------------------>
	 * -------- Setting tree BRANCHES for this tree------------------
	 * 
	 * @throws Exception
	 */
	public static Vector setTreeBranches(FiPlant tree, Random rnd) throws Exception { // each
																			// element
																			// of
																			// the
																			// returned
																			// vector
																			// will
																			// be
																			// one
																			// branch
		System.out.println("setTreeBranches");

		Vector branchesOfThisTree = new Vector ();
		double LAsum = 0;
		double LBsum = 0;

		// Each branch has 8 (0 to 7) characteristics :
		// 0 : age of the tree when the branch was born
		// 1 : total height of the tree when the branch was born = branch height = bottomOfAnnualShoot
		// 2 : branch diameter
		// 3 : insertion angle (between the - vertical - stem and the branch near the trunk
		// 4 : angle between horiz and segment from branch insertion to branch end
		// 5 : horizontal extent of the branch between the trunk and the terminal bud of the branch
		// 8 : max horizontal extent of the branch between the trunk and the farthest bud
		// 6 : length of chord of the branch (=distance between its insertion on the stem and its end
		// 7 : wanted ! dead (0) ? or alive (1) ? In fact, we set only live branches inserted higher than crown base
		// add 8th characteristic ? : horizontal azimuth :  ... at random (at least until now) => not set

		double[] branch = new double[9];

		// Vectors of the succession of age & height of this tree :
		Vector curves = slurp(tree);
		Vector ageSuccession = (Vector) curves.get (0);
		Vector heightSuccession = (Vector) curves.get (1);
		int nbh = heightSuccession.size();

		double treeDbh = tree.getDbh();
		double treeHeight = tree.getHeight();
		double treeHeightToCrownBase = tree.getCrownBaseHeight ();

		double lpa;
		int nbbr;
		double topOfAnnualShoot, bottomOfAnnualShoot;
		double dist_cime_m;

		int orderOfLowestLivingWhorl=0;
		// Recherche préalable de orderOfLowestLivingWhorl
		for(int kont = nbh-1; kont >= 1; kont--) {				// pour chaque pousse annuelle, au-dessus de la base du houppier
			bottomOfAnnualShoot = ((Double) heightSuccession.get(kont - 1)).doubleValue();
			orderOfLowestLivingWhorl++;
			//Log.println("_FiPa",   "  bottomOfAnnualShoot: "	+R1(bottomOfAnnualShoot) + "  kont: "+ kont	+ "  nbh: "+nbh + "  treeHeightToCrownBase: " +treeHeightToCrownBase);
			if(bottomOfAnnualShoot < treeHeightToCrownBase) break;
		}
		Log.println("_FiPa",   "Tree id : "+tree.getId () + "_______________ orderOfLowestLivingWhorl : "	+ orderOfLowestLivingWhorl);

		for(int kont=nbh-1; kont >= 1; kont--) {	// POUR CHAQUE POUSSE ANNUELLE, AU-DESSUS DE LA BASE DU HOUPPIER
			bottomOfAnnualShoot = ((Double) heightSuccession.get(kont - 1)).doubleValue();
			topOfAnnualShoot = ((Double) heightSuccession.get(kont)).doubleValue();
			if(bottomOfAnnualShoot < treeHeightToCrownBase) continue; // ou break ? TBC on est encore sous la base actuelle du houppier
			//if(bottomOfAnnualShoot < HeightOfPruning) continue;
			lpa = 100.0 * (topOfAnnualShoot - bottomOfAnnualShoot);	// longueur de la pousse en cm
			nbbr = (int) (1.02 + 0.104 * lpa + 0.5); // nombre de branches dans
														// la pousse (en
			// général sur 2 ou 3
														// pseudo-verticilles)

			for (int kontt = 1; kontt <= nbbr; kontt++) { // GÉNÉRATION DES
															// "nbbr" BRANCHES
				// À CE NIVEAU DE
															// L'ARBRE, par
															// tirage entre la
															// plus grosse
															// possible et 0 (en
															// fait, il peut y
															// en avoir de plus
															// grosse que la
															// plus grosse)

				branch [0] = ((Integer) ageSuccession.get(kont - 1)).intValue();

				//branch[1] = bottomOfAnnualShoot;
				branch[1] = bottomOfAnnualShoot + 0.01*lpa * rnd.nextDouble();

				branch[2] = get1BranchDiameter (branch[1], treeDbh, treeHeight, rnd);

				//branch[3] = calc_aimPNN(nbh - kont);
				//branch[4] = calc_abmPNN(nbh - kont, orderOfLowestLivingWhorl);
				//branch[6] = calc_oblique_mPNN(treeHeight, treeHeightToCrownBase, bottomOfAnnualShoot);
				//branch[5] = calc_extension_mPNN(branch[6], branch[4]);

				dist_cime_m  = treeHeight - branch[1];
				branch[5] = calc_ET_ALEP(dist_cime_m, branch[2]);
				branch[8] = calc_EX_ALEP(dist_cime_m, branch[2]);

				branch[4] = calc_AB_ALEP(branch[5], branch[2]);

				branch[6] = branch[5] / Math.sin(Math.PI * branch[4]/200d) ;	// Oblique

				if (nbh - kont < orderOfLowestLivingWhorl) branch[7] = 1;

				//Log.println("_FiPa",   "FiDendromBranches speciesName : "+tree.getFuel().getSpeciesName ());
				double[] branchProperties = FmLocalPlantBiomass.branchProperties(
						tree.getSpeciesName(), branch[1] / treeHeight,
						branch[2]); // 0 : m² leaves 1 :
															// kg biomass leaves
				LAsum += branchProperties[0];
				LBsum += branchProperties[1];
				Log.println("_FiPa",   "  age arbre / naiss brch : "	+branch[0] //
				                                                    	        + "  haut (m): "		+R1(branch[1]) + "	bottomOfAnnualShoot : " + R1(bottomOfAnnualShoot)	//
				                                                    	        //+ "  kont: "		+kont	//
				                                                    	        + "  kontt: "		+kontt	//
				                                                    	        + "  diam (mm): "		+R0(branch[2])	//
				                                                    	        //+ "  ai: "		+R1(branch[3])
				                                                    	        + "  ab (grd): "		+R0(branch[4])
				                                                    	        + "  horiz bt (cm): "		+R0(branch[5])	//
				                                                    	        + "  horiz max (cm): "		+R0(branch[8])	//
				                                                    	        + "  obliq bt (cm): "		+R0(branch[6])
				                                                    	        + "  Dead/Alive: "		+branch[7]
				                                                    	                          		        //+ " Leaf Area: "	+R1(branchProperties[0])
				                                                    	                          		        //+ " Leaf Biomass: "	+R1(branchProperties[1])
				);
			}
		}

		//TODO should be modified with LAsum and LBsum when branchproperties will be corrected
		double crownDiameter = tree.getCrownDiameter();
		double crownPerpendicularDiameter = tree.getCrownPerpendicularDiameter();
		
		//TODO double[] crownProperties = FmLocalPlantBiomass
////				.crownPropertiesWithoutPruning(tree, FiPlant.ALIVE); // 0 : m²
//																	// leaves 1
//																	// : kg
//																	// biomass
//																	// leaves
//		double leafArea = crownProperties[0];
//		double leafBiomass = crownProperties[1];
//		double crownProjectedArea = FiPlant
//				.computeCrownProjectedArea(crownDiameter,
//						crownPerpendicularDiameter);
//		tree.getMass ().live[0] = leafBiomass;
//		tree.setLai(0.5*leafArea/crownProjectedArea);

		/*Log.println("_FiPa",   "Tree id : "+tree.getId ()
					//+ "\n" +
					+ "  LAglobal : " + LAglobal
					+ "  LAsum : " + LAsum
					//+ "\n" +
					+ "  LBglobal : " + LBglobal
					+ "  LBsum : " + LBsum
							);*/

		return branchesOfThisTree;
	}



	/**----------------------------------------------------------------------------------------------------------------------------------->
	* ------ contact : Ph.Dreyfus INRA URFM -------------------------------------------------------------------------------------------------------------->
	* A FEW BRANCHING RELATIONSHIPS					PhD 2009-07-07
	*
	* BRANCH DIAMETER (in mm) - Pinus halepensis (see Ph.Dreyfus)*/
	public static double get1BranchDiameter (double heightOfThisBranchInM, double treeDbh, double treeHeight, Random rnd) {

		double [] prob_cum_dr = {0.15, 0.29, 0.41, 0.51, 0.61, 0.68, 0.75, 0.81, 0.86, 0.90, 0.93, 0.95, 0.97, 0.99, 1.00}; // cf. simul_br.sas (et cdr_n_b.sas)
		double dbx5x, prdbx5x, b, a, dist_cm, dbx, dr, diambr, tirage;

		dbx5x = 1.984 * treeDbh; // diamètre de la 5ème plus grosse branche de
									// tout l'arbre
		prdbx5x  =  0.1 * (5.59 + 0.00994 * (100*treeHeight / treeDbh));	// position relative (entre la souche et la cime) de cette plus grosse branche (cf. carar.sas et an5_n_b.sas)
		b = 1.0 / (treeHeight * 100 * (1.0 - prdbx5x)); // b et a : paramètres
														// de la relation qui
		// donne le diamètre de
														// la plus grosse
		// branche possible à
														// tout niveau de
														// l'arbre :
		a = b * Math.exp(1) * dbx5x;

		dist_cm = 100*(treeHeight - heightOfThisBranchInM);	// -> distance en cm entre la cime et la base de la pousse
		dbx = a * Math.exp(-b * dist_cm) * dist_cm; // diamètre de la plus
													// grosse branche possible
		// à ce niveau de l'arbre

		tirage = rnd.nextDouble(); // les probas sont "fixes" (non conditionnées
								// par le niveau dans l'arbre, tout au moins
								// pour partie vive du houppier)
		int i = 0;
		while(tirage > prob_cum_dr[i]) {
			i++;
		}
		dr = ( i+1) * 0.1;
		diambr = dr * dbx;		// en mm

		return Math.max(2, diambr); // pas de branches observée de diam < 2 mm
	}



	// ANGLE D'INSERTION (en degrés) - Pinus nigra nigricans (see Ph.Dreyfus)
	// Angle d'Insertion de la branche moyenne du verticille  ------------>
	//public static double calc_aimPNN(int numero) {
	//	return (46.5 + 47.6 * (1 - Math.exp(-0.0825 * (double) numero)));
	// ALEP :
	// Angle d’insertion (en grades) = 18.2 - 19.5 • V + 5.46 • V ²
	//
	// avec V = numéro d’ordre du verticille (compté depuis la cime), donc
	// équivalent à l’âge de la branche.
	//
	//}



	// ANGLE GENERAL (en degrés) - Pinus nigra nigricans (see Ph.Dreyfus)
	// Angle Général (insertion->bourgeon terminal) de la branche moyenne
	// ------------>
	public static double calc_abmPNN(int numero,int orderOfLowestLivingWhorl) {
		if(orderOfLowestLivingWhorl == 0) { /*MessageBox(0,"div.0","calc_abm",MB_OK);*/ return 35;}
		return (35.0 + 50.0 * (numero) / (orderOfLowestLivingWhorl));
	}
	// Angle Général (insertion->bourgeon terminal) de la + grosse branche du
	// verticille ------------>
	public static double calc_abxPNN(int numero,int orderOfLowestLivingWhorl) {
		return calc_abmPNN(numero,orderOfLowestLivingWhorl);
	}

	// ANGLE GENERAL (en GRADES) - Pinus halepensis (see Ph.Dreyfus)
	public static double calc_AB_ALEP(double et_cm, double diam_brch_mm) {	//AB_2_2009.sas
		// Only branches verticillaires;
		//double a  =    123.4;
		//double b  =   0.1317;
		//double aa = -85.3815;
		//double bb = -30.5932;

		// Y compris branches inter-verticillaires;
		double a  =  95.7496;
		double b  =   0.1634;
		double aa = -51.2206;
		double bb = -33.9328;

		double ab_grades = a * Math.pow(et_cm,b) + aa + bb * Math.log(diam_brch_mm);
		return Math.max(5, ab_grades);
	}



	// HORIZONTALE (en m) - Pinus nigra nigricans (see Ph.Dreyfus)
	// Extension Horizontale de la branche moyenne du verticille ------------------------------>
	public static double calc_extension_mPNN(double oblique_m,double abm) {
		return (oblique_m * Math.sin(abm * Math.PI / 200.0));
	}

	// Extension Horizontale de la branche la + grosse du verticille ------------------------------->
	public static double calc_extension_xPNN(double oblique_x,double abx) {
		return (oblique_x * Math.sin(abx * Math.PI / 200.0));
	}



	// HORIZONTALE (en m) - Pinus halepensis (see Ph.Dreyfus)
	// Extension horizontale depuis le tronc jusqu'au bourgeon terminal ------------------------------>
	public static double calc_ET_ALEP(double dist_cime_m, double diam_brch_mm) {	//ET_2_2009.sas
		double dist_cime_cm = dist_cime_m * 100d;
		double a  =                0.6663;
		double b  =                0.2474;
		double c   =               0.4907;
		double et_cm = Math.exp(a * Math.pow(dist_cime_cm, b) + c * Math.log(diam_brch_mm));
		return et_cm;
	}

	// Extension horizontale maximale, depuis le tronc jusqu'au bourgeon le plus
	// éloigné ------------------------------>
	public static double calc_EX_ALEP(double dist_cime_m, double diam_brch_mm) {	//EX_2_2009.sas
		double dist_cime_cm = dist_cime_m * 100d;
		double a  =                0.8788;
		double b  =                0.1973;
		double c   =               0.5588;
		double ex_cm = Math.exp(a * Math.pow(dist_cime_cm, b) + c * Math.log(diam_brch_mm));
		return ex_cm;
	}




	// ------ contact : Ph.Dreyfus INRA URFM -------------------------------------------------------------------------------------------------------------->
	// --------------------------------------------------
	public static String R1 (double x) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(1);
		nf.setGroupingUsed (false);
		return nf.format(x);
	}
	public static String R0 (double x) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(0);
		nf.setGroupingUsed (false);
		return nf.format(x);
	}

	/*// OBLIQUE (en m) - Pinus nigra nigricans (see Ph.Dreyfus)
	// Extension Oblique (insertion->bourgeon terminal) de la branche moyenne du verticille ---------------------------------------->
	public static double calc_oblique_mPNN(double hauteur_actuelle, double houppier_actuel,double hauteur) {
		if(hauteur_actuelle == houppier_actuel) { return 0.648;}
		double x = (hauteur_actuelle - hauteur) / (hauteur_actuelle - houppier_actuel);
		return (0.648 - (0.356 - 0.092 * x) * x) * (hauteur_actuelle - hauteur);
	}
	// Extension Oblique (insertion->bourgeon terminal) de la branche la + grosse du verticille ---------------------------------------->
	public static double calc_oblique_xPNN(double hauteur_actuelle, double houppier_actuel,double hauteur) {
		if(hauteur_actuelle == houppier_actuel) { return 0.648;}
		double x = (hauteur_actuelle - hauteur) / (hauteur_actuelle - houppier_actuel);
		return (0.739 - (0.434 - 0.108 * x) * x) * (hauteur_actuelle - hauteur);
	}*/
	// ----------------------------------------------------------------------------------------------------------------------------------->
	// ------ contact : Ph.Dreyfus INRA URFM
	// -------------------------------------------------------------------------------------------------------------->
	// Méthode de calcul des dimensions du Houppier --------->
	// ----------------------------------------------------------------------------------------------------------------------------------->
	public static boolean setCrownDimensions(FmStand s) {
		if (s.getTrees ().isEmpty())
			return false;
		FiPlant t;
		for (Iterator i = s.getTrees().iterator(); i.hasNext();) {
			t = (FiPlant) i.next();
			String speciesName = t.getSpeciesName();
			if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
				t.setCrownBaseHeight(calcHcb(t.getDbh(), t.getHeight(), t
						.getAge(), t.getCrownBaseHeight(), s));
				t.setCrownRadius(calcCrownR(t.getSpecies(), t.getDbh(), t
						.getHeight(), t.getCrownBaseHeight(),/*
															 * s.getHighestPruning
															 * ()
															 */0));
				// t.setH_D_(100.0 * (t.getHeight() -1.3) / t.getDbh());
			}
		}
		return true;
	}

	// ----------------------------------------------------------------------------------------------------------------------------------->
	// ------ contact : Ph.Dreyfus INRA URFM
	// -------------------------------------------------------------------------------------------------------------->
	// calcCrownR - PH1 (cf. at5.sas) >
	// calcCrownR - PH1 (cf. at5.sas) >
	// calcCrownR - PH1 (cf. at5.sas) >
	// calcCrownR - PH1 (cf. at5.sas) >
	// ----------------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Compute Diameter of tree crown
	 */
	// --------------------------------------------------------------------------------->
	public static double calcCrownR(FiSpecies sp, double dcm, double ht,
			double hcb, double hop) {
		if (dcm <= 0 || ht < 1.3)
			return ht * 0.33;
		double a = 0.3742, b = 0.2858, c = 7372.2; // cf. at5.sas
		double d = -2.5; // fixé;
		double h_d_ = 100.0 * (ht - 1.30) / dcm; // ht en m, dcm en cm;
		double emax_ht = a - b * Math.exp(-c * Math.pow(h_d_, d));

		if (hop > 0) {
			double cr_nat = (ht - hcb) / ht;
			double cr_art = (ht - Math.max(hcb, Math.min(0.9 * ht, hop))) / ht;
			return emax_ht * ht / Math.exp(2.2041 * cr_nat)
					* Math.exp(2.2041 * cr_art); // cf. couv_epi.sas (!!!!)
		} else {
			return emax_ht * ht; // en m
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------->
	// ------ contact : Ph.Dreyfus INRA URFM
	// -------------------------------------------------------------------------------------------------------------->
	// calcHcb >
	// calcHcb >
	// calcHcb >
	// calcHcb >
	// ----------------------------------------------------------------------------------------------------------------------------------->
	// Méthode d'estimation de la base du hcb
	// --------------------------------------->
	public static double calcHcb(double diameter, double height, double age,
			double prev_hbh, FmStand s) {
		if (age == 0)
			return 1;
		double hcb;
		if (height <= 1.3)
			hcb = height * 0.33; // 1/3
		else {
			double hou1 = 0.05129591, hou2 = 10.84080202; // Pin laricio
			double hou3 = 0.779; // Pin noir d'Autriche
			double d_h = diameter / height, terme = hou3 * (d_h - 0.77)
					/ Math.pow((3.33 - hou3 * d_h), 4);
			if (d_h <= 0.77) {
				d_h = 0.77;
				terme = 0;
			}
			if (d_h >= 3.33) {
				d_h = 3.33;
				terme = 3.991409;
			}
			double valeur = (hou1 + hou2 / age) * d_h;
			if (valeur < -200)
				return 1;
			// hcb = height * Math.exp( - valeur - terme); // Conformité CL
			// ("terme")
			hcb = height * Math.exp(-valeur); // rien (on ne prend pas la
			// "conformité Croissance Libre")
			if (hcb > 0.9 * height)
				hcb = 0.9 * height;
		}
		if (hcb < prev_hbh)
			hcb = prev_hbh;
		return hcb;
	}




} // end of FiDendromBranches

