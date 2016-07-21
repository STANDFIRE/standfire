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

package capsis.lib.castanea;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;



/**	A format description to read a simulation file for transpoprege script mode.
*	One line = one simulation.
*
*	@author F. de Coligny,H. Davi -october 2006
*/
public class FmSpeciesReader extends RecordSet {
	private String fileName;
	private Map speciesMap;
	private FmSettings fmSets;

	// Every SpeciesRecord contains one line per simulation to be processed
	@Import
	static public class SpeciesRecord extends Record {
		public SpeciesRecord () {super ();}
		public SpeciesRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public int castaneaCode;

		public double CRF;
		public double CRRG;
		public double CRRF;
		public double CRBV;
		public double tronviv;
		public double branviv;
		public double rgviv;
		public double Lignroots;
		public double LIGNrl;
		public double LIGNll;
		public double LIGNfb;
		public double LIGNcb;
		public double LIGNcr;
		public double TGSS;
		public double leafNitrogen;
		public double coarseRootsNitrogen;
		public double fineRootsNitrogen;
		public double branchesNitrogen;
		public double stemNitrogen;
		public double reservesNitrogen;
		public double potsoilToWood;
		public double GBVmin;
		public double TMRF;
		public double ratioBR;
		public double RS;
		public double coefrac;
		public double ratioG;
		public double TMBV;
		public double SF;
		public double LMA0;
		public double KLMA;
		public double alphal;
		public double alphab;
		public double CrownArea1;
		public double CrownArea2;
		public double CoefLAI1;
		public double CoefLAI2;
		public double CoefLAI3;
		public double aGF;
		public double bGF;
		public double Phi;
		public double ros;
		public double clumping;
		public double RauwPIR;
		public double RauwPAR;
		public double RaufPIR;
		public double TaufPIR;
		public double RaufPAR;
		public double TaufPAR;
		public double emsf;
		public double Tleaf;
		public double Tbark;
		public double CIA ;
		public double CIB;
		public double propec;
		public double g0;
		public double g1;
		public double RSoilToleaves;
		public double CapSoilToleaves;
		public double EaVJ;
		public double ETT;
		public double JMT;
		public double NC;
		public double teta;
		public double TBASEA;
		public double TBASEB;
		public double TBASEC;
		public double NSTART;
		public double NSTART3;
		public double TSUMBB;
		public double HSUMFL;
		public double HSUMLMA;
		public double TSUMLFAL;
		public double t0;
		public double Vb;
		public double d;
		public double e;
		public double Fcrit;
		public double Ccrit;
		public double a1;
		public double a2;
		public double a3;
		public double C50;
		public double woodStop;
		public double TminEffect;
		public double decidu;
		public double cohortesOfLeaves;
		public double Rec0;
		public double frec0;
		public double T1rec;
		public double T2rec;
		public double T3rec;
		public double T6rec;
		public double fd0;
		public double ffrost;
		public double fa0spg;
		public double fa0aut;
		public double powRec;
		public double reservesToReproduce;  //  indiv reproduce
		public double reservesToMortality;
		public double BSSminCrit;
		public double costOfOneSeed;
		public double rateOfSeedProduction;

	}


	/**	Constructor 1: read thr given file, update the given FmSettings.
	*/
	public FmSpeciesReader (String fileName, FmSettings fmSets) throws Exception {
		super ();
		this.fileName = fileName;
		this.fmSets = fmSets;
		createRecordSet (fileName);
		interpret ();
	}

	/**	Constructor 2: saves the given parameters
	*	Usage: new FmSpeciesReader (fmSets).save (fileName);
	*/
	public FmSpeciesReader (FmSettings fmSets) throws Exception {
		super ();
		setHeaderEnabled (false);
		createRecordSet (fmSets);
	}

	//	Before saving, create the record set
	//
	private void createRecordSet (FmSettings fmSets) throws Exception {

		add (new CommentRecord ("Constant and species specific parameter of CASTANEA model for different species"));
		add (new CommentRecord ("1: Holm oak, 2: white oak, 3: oak, 4: Beech, 5: Scots Pine, 6: Maritime pine, 7: silver fir, 8: spruce"));
		add (new EmptyRecord ());

		add (new KeyRecord ("MRN", ""+fmSets.MRN));
		add (new KeyRecord ("C", ""+fmSets.QDIX));
		add (new KeyRecord ("tc", ""+fmSets.tc));
		add (new KeyRecord ("Tbase", ""+fmSets.Tbase));
		add (new KeyRecord ("Oi0", ""+fmSets.Oi0));
		add (new KeyRecord ("Ko0", ""+fmSets.Ko0));
		add (new KeyRecord ("Kc0", ""+fmSets.Kc0));
		add (new KeyRecord ("cVc", ""+fmSets.cVc));
		add (new KeyRecord ("cVo", ""+fmSets.cVo));
		add (new KeyRecord ("cKc", ""+fmSets.cKc));
		add (new KeyRecord ("cKo", ""+fmSets.cKo));
		add (new KeyRecord ("cgama", ""+fmSets.cgama));
		add (new KeyRecord ("EaKc", ""+fmSets.EaKc));
		add (new KeyRecord ("EaKo", ""+fmSets.EaKo));
		add (new KeyRecord ("EaVc", ""+fmSets.EaVc));
		add (new KeyRecord ("EaVo", ""+fmSets.EaVo));
		add (new KeyRecord ("Eagama", ""+fmSets.Eagama));
		add (new KeyRecord ("rdtq", ""+fmSets.rdtq));
		add (new KeyRecord ("coefbeta", ""+fmSets.coefbeta));
		add (new KeyRecord ("KAR", ""+fmSets.KAR));
		add (new KeyRecord ("Cd", ""+fmSets.Cd));
		add (new KeyRecord ("Zosol", ""+fmSets.Zosol));
		add (new KeyRecord ("K1", ""+fmSets.K1));
		add (new KeyRecord ("K2", ""+fmSets.K2));
		add (new KeyRecord ("K3", ""+fmSets.K3));
		add (new KeyRecord ("K4", ""+fmSets.K4));
		add (new KeyRecord ("K5", ""+fmSets.K5));
		add (new KeyRecord ("K6", ""+fmSets.K6));
		add (new KeyRecord ("K7", ""+fmSets.K7));
		add (new KeyRecord ("K8", ""+fmSets.K8));
		add (new KeyRecord ("Cph", ""+fmSets.Cph));
		add (new KeyRecord ("R", ""+fmSets.R));
		add (new KeyRecord ("IO", ""+fmSets.IO));
		add (new KeyRecord ("gsolmax", ""+fmSets.gsolmax));
		add (new KeyRecord ("gsolmin", ""+fmSets.gsolmin));
		add (new KeyRecord ("emsg", ""+fmSets.emsg));
		add (new KeyRecord ("Alit", ""+fmSets.Alit));
		add (new KeyRecord ("Asoil", ""+fmSets.Asoil));

		add (new EmptyRecord ());
		add (new CommentRecord ("Respiration													    Allometric					Radiatif budget									Hydrology							Photosynthesis					Phenology								"));
		add (new CommentRecord (" species code	CRF	CRRG	CRRF	CRBV	tronviv	branviv	rgviv Lignroots	LIGNrl	LIGNll	LIGNfb	LIGNcb	LIGNcr TGSS Nitrogen	ratioBR	RS coefrac ratioG	TMBV	SF	LMA0 KLMA	alphal	alphab CrownArea1 CrownArea2 CoefLAI1 CoefLAI2 clumping RauwPIR	RauwPAR	RaufPIR	TaufPIR	RaufPAR	TaufPAR	emsf	Tleaf	Tbark	CIA 	CIB	propec	g0	g1	EaVJ	ETT	JMT	NC	teta	TBASEA	TBASEB	TBASEC	NSTART	NSTART3	TSUMBB	HSUMFL	HSUMLMA	TSUMLFAL t0 Vb d e Fcrit Ccrit a1 a2 a3 C50 woodStop TminEffect"));
		add (new CommentRecord ("	leaf construction cost	coarse roots construction cost	Fine roots construction cost	woody construction cost	rate of alive cells in trunk	rate of alive cells in branches	rate of alive cells in coarse roots	Roots lignin	lignin fraction in fine roots	lignin fraction in leaf litter	lignin fraction in fine branches	lignin fraction in coarse branches	lignin fraction in corase roots	ratio of branches	rootshoot	wood mortality	leaf area	Leaf Mass per Area of sunlit	leaf angle	wood angle	Wood PIR reflectance	Wood PAR reflectance	Leaf PIR reflectance	Leaf PIR transmittance	Leaf PAR reflectance	Leaf PAR transmittance	leaf emissivity	Leaf water storage	Bark water storage	water interception coef 1	water interception coef 2	water stem flow	cuticular conductance	ball slope	Activation Energy	Temperature effect on J	Temperature effect on J	ration between vcmax and NF	curvature of J curve	critical temperature for budburst	critical temperature for LAI growth	critical temperature for leaf fall	Date of beginning of sum for BB	Date of beginning of sum for LF	Temperature sum for BB	Temperature sum for LAImax	Temperature sum for LMA	Temperature sum for LF Type of phenologie"));
		add (new EmptyRecord ());

		Map speciesMap = fmSets.castaneaSpeciesMap;
		for (Iterator i = speciesMap.values ().iterator (); i.hasNext ();) {
			FmSpecies species = (FmSpecies) i.next ();

			SpeciesRecord r = new SpeciesRecord ();
			r.castaneaCode = species.castaneaCode;
			r.CRF = species.CRF;
			r.CRRG = species.CRRG;
			r.CRRF = species.CRRF;
			r.CRBV = species.CRBV;
			r.tronviv = species.tronviv;
			r.branviv = species.branviv;
			r.rgviv = species.rgviv;
			r.Lignroots = species.Lignroots;
			r.LIGNrl = species.LIGNrl;
			r.LIGNll = species.LIGNll;
			r.LIGNfb = species.LIGNfb;
			r.LIGNcb = species.LIGNcb;
			r.LIGNcr = species.LIGNcr;
			r.TGSS= species.TGSS;
			r.leafNitrogen = species.leafNitrogen;
			r.coarseRootsNitrogen = species.coarseRootsNitrogen;
			r.fineRootsNitrogen = species.fineRootsNitrogen;
			r.branchesNitrogen = species.branchesNitrogen;
			r.stemNitrogen = species.stemNitrogen;
			r.reservesNitrogen = species.reservesNitrogen;
			r.potsoilToWood = species.potsoilToWood;
			r.GBVmin = species.GBVmin;
			r.TMRF = r.TMRF;
			r.ratioBR = species.ratioBR;
			r.RS = species.RS;
			r.coefrac=species.coefrac;
			r.ratioG = species.ratioG;
			r.TMBV = species.TMBV;
			r.SF = species.SF;
			r.LMA0 = species.LMA0;
			r.KLMA = species.KLMA;
			r.alphal = species.alphal;
			r.alphab = species.alphab;
			r.CrownArea1= species.CrownArea1;
			r.CrownArea2= species.CrownArea2;
			r.CoefLAI1= species.CoefLAI1;
			r.CoefLAI2= species.CoefLAI2;
			r.CoefLAI3= species.CoefLAI3;
			r.aGF= species.aGF;
			r.bGF=species.bGF;
			r.Phi=species.Phi;
			r.ros= species.ros;
			r.clumping= species.defaultClumping;
			r.RauwPIR = species.RauwPIR;
			r.RauwPAR = species.RauwPAR;
			r.RaufPIR = species.RaufPIR;
			r.TaufPIR = species.TaufPIR;
			r.RaufPAR = species.RaufPAR;
			r.TaufPAR = species.TaufPAR;
			r.emsf = species.emsf;
			r.Tleaf = species.Tleaf;
			r.Tbark = species.Tbark;
			r.CIA = species.CIA;
			r.CIB = species.CIB;
			r.propec = species.propec;
			r.g0 = species.g0;
			r.g1 = species.g1;
			r.RSoilToleaves = species.RSoilToleaves;
			r.CapSoilToleaves = species.CapSoilToleaves;
			r.EaVJ = species.EaVJ;
			r.ETT = species.ETT;
			r.JMT = species.JMT;
			r.NC = species.NC;
			r.teta = species.teta;
			r.TBASEA = species.TBASEA;
			r.TBASEB = species.TBASEB;
			r.TBASEC = species.TBASEC;
			r.NSTART = species.NSTART;
			r.NSTART3 = species.NSTART3;
			r.TSUMBB = species.TSUMBB;
			r.HSUMFL = species.HSUMFL;
			r.HSUMLMA = species.HSUMLMA;
			r.TSUMLFAL = species.TSUMLFAL;
			r.t0 = species.t0;
			r.Vb = species.Vb;
			r.d = species.d;
			r.e = species.e;
			r.Fcrit = species.Fcrit;
			r.Ccrit = species.Ccrit;
			r.a1 = species.a1;
			r.a2 = species.a2;
			r.a3 = species.a3;
			r.C50 = species.C50;
			r.woodStop = species.woodStop;
			r.TminEffect = species.TminEffect;
			r.decidu = species.decidu;
			r.cohortesOfLeaves= species.cohortesOfLeaves;
			r.Rec0 = species.Rec0;
			r.frec0 = species.frec0;
			r.T1rec = species.T1rec;
			r.T2rec = species.T2rec;
			r.T3rec = species.T3rec;
			r.T6rec = species.T6rec;
			r.fd0 = species.fd0;
			r.ffrost = species.ffrost;
			r.fa0spg = species.fa0spg;
			r.fa0aut = species.fa0aut;
			r.powRec = species.powRec;
			r.reservesToReproduce= species.reservesToReproduce;  //  indiv reproduce
			r.reservesToMortality= species.reservesToMortality;
			r.BSSminCrit= species.BSSminCrit;
			r.costOfOneSeed=species.costOfOneSeed;
			r.rateOfSeedProduction= species.rateOfSeedProduction;

			add (r);
		}

	}
	//	File Interpretation
	//
	public void interpret () throws Exception {
		speciesMap = new HashMap();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof KeyRecord) {
				FmSpeciesReader.KeyRecord r = (FmSpeciesReader.KeyRecord) record;	// cast to precise type

				if (r.hasKey ("MRN")) {
					try {
						fmSets.MRN = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with MRN", e);
						throw e;
					}
				} else if (r.hasKey ("QDIX")) {
					try {
						fmSets.QDIX = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with QDIX", e);
						throw e;
					}
				} else if (r.hasKey ("tc")) {
					try {
						fmSets.tc = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with tc", e);
						throw e;
					}
				} else if (r.hasKey ("Tbase")) {
					try {
						fmSets.Tbase = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Tbase", e);
						throw e;
					}

				} else if (r.hasKey ("Oi0")) {
					try {
						fmSets.Oi0 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Oi0", e);
						throw e;
					}

				} else if (r.hasKey ("Ko0")) {
					try {
						fmSets.Ko0 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Ko0", e);
						throw e;
					}

				} else if (r.hasKey ("Kc0")) {
					try {
						fmSets.Kc0 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Kc0", e);
						throw e;
					}

				} else if (r.hasKey ("cVc")) {
					try {
						fmSets.cVc = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with cVc", e);
						throw e;
					}

				} else if (r.hasKey ("cVo")) {
					try {
						fmSets.cVo = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with cVo", e);
						throw e;
					}

				} else if (r.hasKey ("cKc")) {
					try {
						fmSets.cKc = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with cKc", e);
						throw e;
					}

				} else if (r.hasKey ("cKo")) {
					try {
						fmSets.cKo = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with cKo", e);
						throw e;
					}

				} else if (r.hasKey ("cgama")) {
					try {
						fmSets.cgama = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with cgama", e);
						throw e;
					}

				} else if (r.hasKey ("EaKc")) {
					try {
						fmSets.EaKc = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with EaKc", e);
						throw e;
					}

				} else if (r.hasKey ("EaKo")) {
					try {
						fmSets.EaKo = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with EaKo", e);
						throw e;
					}

				} else if (r.hasKey ("EaVc")) {
					try {
						fmSets.EaVc = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with EaVc", e);
						throw e;
					}

				} else if (r.hasKey ("EaVo")) {
					try {
						fmSets.EaVo = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with EaVo", e);
						throw e;
					}

				} else if (r.hasKey ("Eagama")) {
					try {
						fmSets.Eagama = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Eagama", e);
						throw e;
					}

				} else if (r.hasKey ("rdtq")) {
					try {
						fmSets.rdtq = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with rdtq", e);
						throw e;
					}

				} else if (r.hasKey ("coefbeta")) {
					try {
						fmSets.coefbeta = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with coefbeta", e);
						throw e;
					}

				} else if (r.hasKey ("KAR")) {
					try {
						fmSets.KAR = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with KAR", e);
						throw e;
					}

				} else if (r.hasKey ("Cd")) {
					try {
						fmSets.Cd = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Cd", e);
						throw e;
					}

				} else if (r.hasKey ("Zosol")) {
					try {
						fmSets.Zosol = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Zosol", e);
						throw e;
					}

				} else if (r.hasKey ("K1")) {
					try {
						fmSets.K1 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K1", e);
						throw e;
					}

				} else if (r.hasKey ("K2")) {
					try {
						fmSets.K2 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K2", e);
						throw e;
					}

				} else if (r.hasKey ("K3")) {
					try {
						fmSets.K3 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K3", e);
						throw e;
					}

				} else if (r.hasKey ("K4")) {
					try {
						fmSets.K4 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K4", e);
						throw e;
					}

				} else if (r.hasKey ("K5")) {
					try {
						fmSets.K5 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K5", e);
						throw e;
					}

				} else if (r.hasKey ("K6")) {
					try {
						fmSets.K6 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K6", e);
						throw e;
					}

				} else if (r.hasKey ("K7")) {
					try {
						fmSets.K7 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K7", e);
						throw e;
					}

				} else if (r.hasKey ("K8")) {
					try {
						fmSets.K8 = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with K8", e);
						throw e;
					}
				} else if (r.hasKey ("Cph")) {
					try {
						fmSets.Cph = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Cph", e);
						throw e;
					}

				} else if (r.hasKey ("R")) {
					try {
						fmSets.R = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with R", e);
						throw e;
					}
				} else if (r.hasKey ("IO")) {
					try {
						fmSets.IO = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with IO", e);
						throw e;
					}
				} else if (r.hasKey ("gsolmax")) {
					try {
						fmSets.gsolmax = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with gsolmax", e);
						throw e;
					}
				} else if (r.hasKey ("gsolmin")) {
					try {
						fmSets.gsolmin = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with gsolmin", e);
						throw e;
					}
				} else if (r.hasKey ("emsg")) {
					try {
						fmSets.emsg = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with emsg", e);
						throw e;
					}
				} else if (r.hasKey ("Alit")) {
					try {
						fmSets.Alit = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Alit", e);
						throw e;
					}
				} else if (r.hasKey ("Asoil")) {
					try {
						fmSets.Asoil = r.getDoubleValue ();
					} catch (Exception e) {
						Log.println (Log.ERROR,	"FmSpeciesReader.interpret ()", "Trouble with Asoil", e);
						throw e;
					}


			}

		} else if (record instanceof SpeciesRecord) {
				FmSpeciesReader.SpeciesRecord r = (FmSpeciesReader.SpeciesRecord) record;	// cast to precise type
				FmSpecies s = new FmSpecies(r.castaneaCode);

				s.CRF = r.CRF;
				s.CRRG = r.CRRG;
				s.CRRF = r.CRRF;
				s.CRBV = r.CRBV;
				s.tronviv = r.tronviv;
				s.branviv = r.branviv;
				s.rgviv = r.rgviv;
				s.Lignroots = r.Lignroots;
				s.LIGNrl = r.LIGNrl;
				s.LIGNll = r.LIGNll;
				s.LIGNfb = r.LIGNfb;
				s.LIGNcb = r.LIGNcb;
				s.LIGNcr = r.LIGNcr;
				s.TGSS = r.TGSS;
				s.leafNitrogen= r.leafNitrogen;
				s.coarseRootsNitrogen= r.coarseRootsNitrogen;
				s.fineRootsNitrogen= r.fineRootsNitrogen;
				s.branchesNitrogen= r.branchesNitrogen;
				s.stemNitrogen= r.stemNitrogen;
				s.reservesNitrogen= r.reservesNitrogen;
				s.potsoilToWood = r.potsoilToWood;
				s.GBVmin = r.GBVmin;
				s.TMRF = r.TMRF;
				s.ratioBR = r.ratioBR;
				s.RS = r.RS;
				s.coefrac=r.coefrac;
				s.ratioG= r.ratioG;
				s.TMBV = r.TMBV;
				s.SF = r.SF;
				s.LMA0 = r.LMA0;
				s.KLMA = r.KLMA;
				s.alphal = r.alphal;
				s.alphab = r.alphab;
				s.CrownArea1= r.CrownArea1;
				s.CrownArea2= r.CrownArea2;
				s.CoefLAI1= r.CoefLAI1;
				s.CoefLAI2= r.CoefLAI2;
				s.CoefLAI3= r.CoefLAI3;
				s.aGF= r.aGF;
				s.bGF=r.bGF;
				s.Phi=r.Phi;
				s.ros= r.ros;
				s.defaultClumping= r.clumping;
				s.RauwPIR = r.RauwPIR;
				s.RauwPAR = r.RauwPAR;
				s.RaufPIR = r.RaufPIR;
				s.TaufPIR = r.TaufPIR;
				s.RaufPAR = r.RaufPAR;
				s.TaufPAR = r.TaufPAR;
				s.emsf = r.emsf;
				s.Tleaf = r.Tleaf;
				s.Tbark = r.Tbark;
				s.CIA = r.CIA;
				s.CIB = r.CIB;
				s.propec = r.propec;
				s.g0= r.g0;
				s.g1 = r.g1;
				s.RSoilToleaves = r.RSoilToleaves;
				s.CapSoilToleaves = r.CapSoilToleaves;
				s.EaVJ = r.EaVJ;
				s.ETT = r.ETT;
				s.JMT = r.JMT;
				s.NC = r.NC;
				s.teta = r.teta;
				s.TBASEA = r.TBASEA;
				s.TBASEB = r.TBASEB;
				s.TBASEC = r.TBASEC;
				s.NSTART = r.NSTART;
				s.NSTART3 = r.NSTART3;
				s.TSUMBB = r.TSUMBB;
				s.HSUMFL = r.HSUMFL;
				s.HSUMLMA = r.HSUMLMA;
				s.TSUMLFAL = r.TSUMLFAL;
				s.t0 = r.t0;
				s.Vb = r.Vb;
				s.d = r.d;
				s.e = r.e;
				s.Fcrit = r.Fcrit;
				s.Ccrit = r.Ccrit;
				s.a1 = r.a1;
				s.a2 = r.a2;
				s.a3 = r.a3;
				s.C50 = r.C50;
				s.woodStop = r.woodStop;
				s.TminEffect = r.TminEffect;
				s.decidu = r.decidu;
				s.cohortesOfLeaves =r.cohortesOfLeaves;
				s.Rec0 = r.Rec0;
				s.frec0 = r.frec0;
				s.T1rec = r.T1rec;
				s.T2rec = r.T2rec;
				s.T3rec = r.T3rec;
				s.T6rec = r.T6rec;
				s.fd0 = r.fd0;
				s.ffrost = r.ffrost;
				s.fa0spg = r.fa0spg;
				s.fa0aut = r.fa0aut;
				s.powRec = r.powRec;
				s.reservesToReproduce= r.reservesToReproduce;  //  indiv reproduce
				s.reservesToMortality= r.reservesToMortality;
				s.BSSminCrit= r.BSSminCrit;
				s.costOfOneSeed= r.costOfOneSeed;
				s.rateOfSeedProduction= r.rateOfSeedProduction;

				speciesMap.put(r.castaneaCode, s);

			} else {
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}
		}

	}

	//public static String getParameterFileName() {return (String) parameterFileName ; }
	public Map getSpeciesMap(){return speciesMap;}
}


