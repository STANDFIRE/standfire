package capsis.lib.castanea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.gui.NorthPanel;
import capsis.commongui.util.Helper;
import capsis.gui.MainFrame;
import capsis.kernel.PathManager;
import capsis.util.JSmartFileChooser;

/**
 * FmCastaneaEditor - Dialog box to view / edit castanea files.
 *
 * @author Hendrik Davi - April 2008, added in capsis.lib.castanea in February 2013
 */
public class FmCastaneaEditor extends AmapDialog implements ActionListener {

	static {
		Translator.addBundle ("capsis.lib.castanea.Castanea");
	}

	private FmSettings fmSets;
	private Map<Integer,FmSpecies> tabIndex_speciesMap;
	private JTabbedPane tabs;

	private JTextField MRN;
	private JTextField QDIX;
	private JTextField tc;
	private JTextField Tbase;
	private JTextField Oi0;
	private JTextField Ko0;
	private JTextField Kc0;
	private JTextField cVc;
	private JTextField cVo;
	private JTextField cKc;
	private JTextField cKo;
	private JTextField cgama;
	private JTextField EaKc;
	private JTextField EaKo;
	private JTextField EaVc;
	private JTextField EaVo;
	private JTextField Eagama;
	private JTextField rdtq;
	private JTextField coefbeta;
	private JTextField KAR;
	private JTextField Cd;
	private JTextField Zosol;
	private JTextField K1;
	private JTextField K2;
	private JTextField K3;
	private JTextField K4;
	private JTextField K5;
	private JTextField K6;
	private JTextField K7;
	private JTextField K8;
	private JTextField Cph;
	private JTextField R;
	private JTextField IO;
	private JTextField gsolmax;
	private JTextField gsolmin;
	private JTextField emsg;
	private JTextField Alit;
	private JTextField Asoil;
	private JTextField[] CRF;
	private JTextField[] CRRG;
	private JTextField[] CRRF;
	private JTextField[] CRBV;
	private JTextField[] tronviv;
	private JTextField[] branviv;
	private JTextField[] rgviv;
	private JTextField[] Lignroots;
	private JTextField[] LIGNrl;
	private JTextField[] LIGNll;
	private JTextField[] LIGNfb;
	private JTextField[] LIGNcb;
	private JTextField[] LIGNcr;
	private JTextField[] TGSS;
	private JTextField[] leafNitrogen;
	private JTextField[] coarseRootsNitrogen;
	private JTextField[] fineRootsNitrogen;
	private JTextField[] branchesNitrogen;
	private JTextField[] stemNitrogen;
	private JTextField[] reservesNitrogen;
	private JTextField[] potsoilToWood;
	private JTextField[] GBVmin;
	private JTextField[] TMRF;
	private JTextField[] ratioBR;
	private JTextField[] RS;
	private JTextField[] ratioG;
	private JTextField[] coefrac;
	private JTextField[] TMBV;
	private JTextField[] SF;
	private JTextField[] LMA0;
	private JTextField[] KLMA;
	private JTextField[] alphal;
	private JTextField[] alphab;
	private JTextField[] CrownArea1;
	private JTextField[] CrownArea2;
	private JTextField[] CoefLAI1;
	private JTextField[] CoefLAI2;
	private JTextField[] CoefLAI3;
	private JTextField[] aGF;
	private JTextField[] bGF;
	private JTextField[] Phi;
	private JTextField[] ros;
	private JTextField[] clumping;
	private JTextField[] RauwPIR;
	private JTextField[] RauwPAR;
	private JTextField[] RaufPIR;
	private JTextField[] TaufPIR;
	private JTextField[] RaufPAR;
	private JTextField[] TaufPAR;
	private JTextField[] emsf;
	private JTextField[] Tleaf;
	private JTextField[] Tbark;
	private JTextField[] CIA;
	private JTextField[] CIB;
	private JTextField[] propec;
	private JTextField[] g0;
	private JTextField[] g1;
	private JTextField[] RSoilToleaves;
	private JTextField[] CapSoilToleaves;
	private JTextField[] EaVJ;
	private JTextField[] ETT;
	private JTextField[] JMT;
	private JTextField[] NC;
	private JTextField[] teta;
	private JTextField[] TBASEA;
	private JTextField[] TBASEB;
	private JTextField[] TBASEC;
	private JTextField[] NSTART;
	private JTextField[] NSTART3;
	private JTextField[] TSUMBB;
	private JTextField[] HSUMFL;
	private JTextField[] HSUMLMA;
	private JTextField[] TSUMLFAL;
	private JTextField[] t0;
	private JTextField[] Vb;
	private JTextField[] d;
	private JTextField[] e;
	private JTextField[] Fcrit;
	private JTextField[] Ccrit;
	private JTextField[] a1;
	private JTextField[] a2;
	private JTextField[] a3;
	private JTextField[] C50;
	private JTextField[] woodStop;
	private JTextField[] TminEffect;
	private JTextField[] decidu;
	private JTextField[] cohortesOfLeaves;
	private JTextField[] Rec0;
	private JTextField[] frec0;
	private JTextField[] T1rec;
	private JTextField[] T2rec;
	private JTextField[] T3rec;
	private JTextField[] T6rec;
	private JTextField[] fd0;
	private JTextField[] ffrost;
	private JTextField[] fa0spg;
	private JTextField[] fa0aut;
	private JTextField[] powRec;
	private JTextField[] reservesToReproduce;  //  indiv reproduce
	private JTextField[] reservesToMortality;
	private JTextField[] BSSminCrit;
	private JTextField[] costOfOneSeed;
	private JTextField[] rateOfSeedProduction;

	private JButton save;
	private JButton saveAs;
	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**
	 * Constructor.
	 */
	public FmCastaneaEditor (FmSettings fmSets) {
		super ();
		this.fmSets = fmSets;
		tabIndex_speciesMap = new HashMap<Integer,FmSpecies> ();

		init ();

		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	private void init () {
		int n = fmSets.castaneaSpeciesMap.size ();
		CRF = new JTextField[n];
		CRRG = new JTextField[n];
		CRRF = new JTextField[n];
		CRBV = new JTextField[n];
		tronviv = new JTextField[n];
		branviv = new JTextField[n];
		rgviv = new JTextField[n];
		Lignroots = new JTextField[n];
		LIGNrl = new JTextField[n];
		LIGNll = new JTextField[n];
		LIGNfb = new JTextField[n];
		LIGNcb = new JTextField[n];
		LIGNcr = new JTextField[n];
		TGSS = new JTextField[n];
		leafNitrogen = new JTextField[n];
		coarseRootsNitrogen = new JTextField[n];
		fineRootsNitrogen = new JTextField[n];
		branchesNitrogen = new JTextField[n];
		stemNitrogen = new JTextField[n];
		reservesNitrogen = new JTextField[n];
		potsoilToWood = new JTextField[n];
		GBVmin = new JTextField[n];
		TMRF = new JTextField[n];
		ratioBR = new JTextField[n];
		RS = new JTextField[n];
		ratioG = new JTextField[n];
		coefrac = new JTextField[n];
		TMBV = new JTextField[n];
		SF = new JTextField[n];
		LMA0 = new JTextField[n];
		KLMA = new JTextField[n];
		alphal = new JTextField[n];
		alphab = new JTextField[n];
		CrownArea1 = new JTextField[n];
		CrownArea2 = new JTextField[n];
		CoefLAI1 = new JTextField[n];
		CoefLAI2 = new JTextField[n];
		CoefLAI3 = new JTextField[n];
		aGF = new JTextField[n];
		bGF = new JTextField[n];
		Phi = new JTextField[n];
		ros = new JTextField[n];
		clumping = new JTextField[n];
		RauwPIR = new JTextField[n];
		RauwPAR = new JTextField[n];
		RaufPIR = new JTextField[n];
		TaufPIR = new JTextField[n];
		RaufPAR = new JTextField[n];
		TaufPAR = new JTextField[n];
		emsf = new JTextField[n];
		Tleaf = new JTextField[n];
		Tbark = new JTextField[n];
		CIA = new JTextField[n];
		CIB = new JTextField[n];
		propec = new JTextField[n];
		g0 = new JTextField[n];
		g1 = new JTextField[n];
		RSoilToleaves = new JTextField[n];
		CapSoilToleaves = new JTextField[n];
		EaVJ = new JTextField[n];
		ETT = new JTextField[n];
		JMT = new JTextField[n];
		NC = new JTextField[n];
		teta = new JTextField[n];
		TBASEA = new JTextField[n];
		TBASEB = new JTextField[n];
		TBASEC = new JTextField[n];
		NSTART = new JTextField[n];
		NSTART3 = new JTextField[n];
		TSUMBB = new JTextField[n];
		HSUMFL = new JTextField[n];
		HSUMLMA = new JTextField[n];
		TSUMLFAL = new JTextField[n];
		t0 = new JTextField[n];;
		Vb= new JTextField[n];
		d= new JTextField[n];
		e= new JTextField[n];
		Fcrit= new JTextField[n];
		Ccrit= new JTextField[n];
		a1= new JTextField[n];
		a2= new JTextField[n];
		a3= new JTextField[n];
		C50= new JTextField[n];
		woodStop = new JTextField[n];
		TminEffect = new JTextField[n];
		decidu = new JTextField[n];
		cohortesOfLeaves= new JTextField[n];
		Rec0 = new JTextField[n];
		frec0 = new JTextField[n];
		T1rec = new JTextField[n];
		T2rec = new JTextField[n];
		T3rec = new JTextField[n];
		T6rec = new JTextField[n];
		fd0 = new JTextField[n];
		ffrost = new JTextField[n];
		fa0spg = new JTextField[n];
		fa0aut = new JTextField[n];
		powRec = new JTextField[n];
		reservesToReproduce = new JTextField[n];  //  indiv reproduce
		reservesToMortality = new JTextField[n];
		BSSminCrit = new JTextField[n];
		costOfOneSeed = new JTextField[n];
		rateOfSeedProduction = new JTextField[n];
	}

	private boolean checkEverything () {
		// Check general tab
		if (!Check.isDouble (MRN.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.MRNShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (QDIX.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.QDIXShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (tc.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.tcShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Tbase.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TbaseShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Oi0.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.Oi0ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Ko0.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.Ko0ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Kc0.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.Kc0ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (cVc.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cVcShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (cVo.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cVoShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (cKc.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cKcShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (cKo.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cKoShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (cgama.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cgamaShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (EaKc.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EaKcShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (EaKo.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EaKoShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (EaVc.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EaVcShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (EaVo.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EaVoShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Eagama.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EagamaShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (rdtq.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.rdtqShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (coefbeta.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.coefbetaShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (KAR.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.KARShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Cd.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CdShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Zosol.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.ZosolShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K1.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K1ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K2.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K2ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K3.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K3ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K4.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K4ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K5.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K5ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K6.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K6ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K7.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K7ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (K8.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.K8ShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (gsolmax.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.gsolmaxShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (gsolmin.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.gsolminShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (emsg.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.emsgShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Alit.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.AlitShouldBeANumber"));
			return false;
		}
		if (!Check.isDouble (Asoil.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.AsoilShouldBeANumber"));
			return false;
		}

		// Check all values on the species tabs
		int n = fmSets.castaneaSpeciesMap.size ();

		for (int i = 0; i < n; i++) {

			if (!Check.isDouble (CRF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CRFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CRRG[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CRRGShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CRRF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CRRFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CRBV[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CRBVShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (tronviv[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.tronvivShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (branviv[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.branvivShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (rgviv[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.rgvivShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Lignroots[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LignrootsShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LIGNrl[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LIGNrlShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LIGNll[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LIGNllShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LIGNfb[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LIGNfbShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LIGNcb[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LIGNcbShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LIGNcr[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LIGNcrShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TGSS[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TGSSShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (leafNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.leafNitrogenShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (coarseRootsNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.coarseRootsNitrogenShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (fineRootsNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.fineRootsNitrogenShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (branchesNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.branchesNitrogenShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (stemNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.stemNitrogenShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (reservesNitrogen[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.reservesNitrogenShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (potsoilToWood[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.potsoilToWoodShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (GBVmin[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.GBVminShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TMRF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TMRFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (ratioBR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.ratioBRShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RS[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RSShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (ratioG[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.ratioGShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (coefrac[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.coefracShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TMBV[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TMBVShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (SF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.SFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CrownArea1[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CrownArea1ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CrownArea2[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CrownArea2ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CoefLAI1[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CoefLAI1ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CoefLAI2[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CoefLAI2ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CoefLAI3[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CoefLAI3ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (aGF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.aGFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (bGF[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.bGFShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Phi[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.PhiShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (ros[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.rosShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (clumping[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.clumpingShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (LMA0[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.LMA0ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (KLMA[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.KLMAShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (alphal[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.alphalShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (alphab[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.alphabShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RauwPIR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RauwPIRShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RauwPAR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RauwPARShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RaufPIR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RaufPIRShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TaufPIR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TaufPIRShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RaufPAR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RaufPARShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TaufPAR[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TaufPARShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (emsf[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.emsfShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Tleaf[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TleafShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Tbark[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TbarkShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CIA[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CIAShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CIB[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CIBShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (propec[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.propecShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (g0[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.g0ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (g1[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.g1ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (RSoilToleaves[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.RSoilToleavesShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (CapSoilToleaves[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CapSoilToleavesShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (EaVJ[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.EaVJShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (ETT[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.ETTShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (JMT[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.JMTShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (NC[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.NCShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (teta[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.tetaShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TBASEA[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TBASEAShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TBASEB[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TBASEBShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TBASEC[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TBASECShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (NSTART[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.NNSTARTShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (NSTART3[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.NSTART3ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TSUMBB[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TSUMBBShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (HSUMFL[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.HSUMFLShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (HSUMLMA[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.HSUMLMAShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TSUMLFAL[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TSUMLFALShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (t0[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.t0ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Vb[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.VbShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (d[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.dShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (e[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.eShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (Ccrit[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.CcritShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (Fcrit[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.FcritShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (a1[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.a1ShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (a2[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.a2ShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (a3[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.a3ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (C50[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.a3ShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (decidu[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.deciduShouldBeANumber"));
				return false;
			}

			if (!Check.isDouble (cohortesOfLeaves[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.cohortesOfLeavesShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (woodStop[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.woodStopShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (TminEffect[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.TminEffectShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (reservesToReproduce[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.reservesToReproduceShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (reservesToMortality[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.reservesToMortalityShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (BSSminCrit[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.BSSminCritShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (costOfOneSeed[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.costOfOneSeedShouldBeANumber"));
				return false;
			}
			if (!Check.isDouble (rateOfSeedProduction[i].getText ().trim ())) {
				tabs.setSelectedIndex (i + 1);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.rateOfSeedProductionShouldBeANumber"));
				return false;
			}

		}

		// All checks are ok
		fmSets.MRN = Check.doubleValue (MRN.getText ().trim ());
		fmSets.QDIX = Check.doubleValue (QDIX.getText ().trim ());
		fmSets.tc = Check.doubleValue (tc.getText ().trim ());
		fmSets.Tbase = Check.doubleValue (Tbase.getText ().trim ());
		fmSets.Oi0 = Check.doubleValue (Oi0.getText ().trim ());
		fmSets.Ko0 = Check.doubleValue (Ko0.getText ().trim ());
		fmSets.Kc0 = Check.doubleValue (Kc0.getText ().trim ());
		fmSets.cVc = Check.doubleValue (cVc.getText ().trim ());
		fmSets.cVo = Check.doubleValue (cVo.getText ().trim ());
		fmSets.cKc = Check.doubleValue (cKc.getText ().trim ());
		fmSets.cKo = Check.doubleValue (cKo.getText ().trim ());
		fmSets.cgama = Check.doubleValue (cgama.getText ().trim ());
		fmSets.EaKc = Check.doubleValue (EaKc.getText ().trim ());
		fmSets.EaKo = Check.doubleValue (EaKo.getText ().trim ());
		fmSets.EaVc = Check.doubleValue (EaVc.getText ().trim ());
		fmSets.EaVo = Check.doubleValue (EaVo.getText ().trim ());
		fmSets.Eagama = Check.doubleValue (Eagama.getText ().trim ());
		fmSets.rdtq = Check.doubleValue (rdtq.getText ().trim ());
		fmSets.coefbeta = Check.doubleValue (coefbeta.getText ().trim ());
		fmSets.KAR = Check.doubleValue (KAR.getText ().trim ());
		fmSets.Cd = Check.doubleValue (Cd.getText ().trim ());
		fmSets.Zosol = Check.doubleValue (Zosol.getText ().trim ());
		fmSets.K1 = Check.doubleValue (K1.getText ().trim ());
		fmSets.K2 = Check.doubleValue (K2.getText ().trim ());
		fmSets.K3 = Check.doubleValue (K3.getText ().trim ());
		fmSets.K4 = Check.doubleValue (K4.getText ().trim ());
		fmSets.K5 = Check.doubleValue (K5.getText ().trim ());
		fmSets.K6 = Check.doubleValue (K6.getText ().trim ());
		fmSets.K7 = Check.doubleValue (K7.getText ().trim ());
		fmSets.K8 = Check.doubleValue (K8.getText ().trim ());
		fmSets.Cph = Check.doubleValue (Cph.getText ().trim ());
		fmSets.R = Check.doubleValue (R.getText ().trim ());
		fmSets.IO = Check.doubleValue (IO.getText ().trim ());
		fmSets.gsolmax = Check.doubleValue (gsolmax.getText ().trim ());
		fmSets.gsolmin = Check.doubleValue (gsolmin.getText ().trim ());
		fmSets.emsg = Check.doubleValue (emsg.getText ().trim ());
		fmSets.Alit = Check.doubleValue (Alit.getText ().trim ());
		fmSets.Asoil = Check.doubleValue (Asoil.getText ().trim ());

		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CRF = Check.doubleValue (CRF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CRRG = Check.doubleValue (CRRG[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CRRF = Check.doubleValue (CRRF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CRBV = Check.doubleValue (CRBV[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.tronviv = Check.doubleValue (tronviv[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.branviv = Check.doubleValue (branviv[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.rgviv = Check.doubleValue (rgviv[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Lignroots = Check.doubleValue (Lignroots[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LIGNrl = Check.doubleValue (LIGNrl[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LIGNll = Check.doubleValue (LIGNll[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LIGNfb = Check.doubleValue (LIGNfb[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LIGNcb = Check.doubleValue (LIGNcb[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LIGNcr = Check.doubleValue (LIGNcr[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TGSS = Check.doubleValue (TGSS[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.leafNitrogen = Check.doubleValue (leafNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.coarseRootsNitrogen = Check.doubleValue (coarseRootsNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.fineRootsNitrogen = Check.doubleValue (fineRootsNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.branchesNitrogen = Check.doubleValue (branchesNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.stemNitrogen = Check.doubleValue (stemNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.reservesNitrogen = Check.doubleValue (reservesNitrogen[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.potsoilToWood = Check.doubleValue (potsoilToWood[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.GBVmin = Check.doubleValue (GBVmin[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TMRF = Check.doubleValue (TMRF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.ratioBR = Check.doubleValue (ratioBR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RS = Check.doubleValue (RS[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TMBV = Check.doubleValue (TMBV[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.SF = Check.doubleValue (SF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CrownArea1 = Check.doubleValue (CrownArea1[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CrownArea2 = Check.doubleValue (CrownArea2[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CoefLAI1 = Check.doubleValue (CoefLAI1[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CoefLAI2 = Check.doubleValue (CoefLAI2[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CoefLAI3 = Check.doubleValue (CoefLAI3[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.aGF = Check.doubleValue (aGF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.bGF = Check.doubleValue (bGF[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Phi = Check.doubleValue (Phi[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.ros = Check.doubleValue (ros[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.defaultClumping = Check.doubleValue (clumping[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.LMA0 = Check.doubleValue (LMA0[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.KLMA = Check.doubleValue (KLMA[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.alphal = Check.doubleValue (alphal[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.alphab = Check.doubleValue (alphab[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RauwPIR = Check.doubleValue (RauwPIR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RauwPAR = Check.doubleValue (RauwPAR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RaufPIR = Check.doubleValue (RaufPIR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TaufPIR = Check.doubleValue (TaufPIR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RaufPAR = Check.doubleValue (RaufPAR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TaufPAR = Check.doubleValue (TaufPAR[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.emsf = Check.doubleValue (emsf[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Tleaf = Check.doubleValue (Tleaf[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Tbark = Check.doubleValue (Tbark[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CIA = Check.doubleValue (CIA[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CIB = Check.doubleValue (CIB[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.propec = Check.doubleValue (propec[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.g0 = Check.doubleValue (g0[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.g1 = Check.doubleValue (g1[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.RSoilToleaves = Check.doubleValue (RSoilToleaves[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.CapSoilToleaves = Check.doubleValue (CapSoilToleaves[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.EaVJ = Check.doubleValue (EaVJ[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.ETT = Check.doubleValue (ETT[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.JMT = Check.doubleValue (JMT[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.NC = Check.doubleValue (NC[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.teta = Check.doubleValue (teta[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TBASEA = Check.doubleValue (TBASEA[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TBASEB = Check.doubleValue (TBASEB[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TBASEC = Check.doubleValue (TBASEC[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.NSTART = Check.doubleValue (NSTART[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.NSTART3 = Check.doubleValue (NSTART3[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TSUMBB = Check.doubleValue (TSUMBB[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.HSUMFL = Check.doubleValue (HSUMFL[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.HSUMLMA = Check.doubleValue (HSUMLMA[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.TSUMLFAL = Check.doubleValue (TSUMLFAL[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.t0 = Check.doubleValue (t0[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Vb = Check.doubleValue (Vb[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.d = Check.doubleValue (d[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.e = Check.doubleValue (e[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Ccrit = Check.doubleValue (Ccrit[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.Fcrit = Check.doubleValue (Fcrit[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.a1 = Check.doubleValue (a1[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.a2 = Check.doubleValue (a2[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.a3 = Check.doubleValue (a3[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.decidu = Check.doubleValue (decidu[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.cohortesOfLeaves = Check.doubleValue (cohortesOfLeaves[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.woodStop = Check.doubleValue (woodStop[i].getText ().trim ());
		}

		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.reservesToReproduce = Check.doubleValue (reservesToReproduce[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.reservesToMortality = Check.doubleValue (reservesToMortality[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.BSSminCrit = Check.doubleValue (BSSminCrit[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.rateOfSeedProduction = Check.doubleValue (costOfOneSeed[i].getText ().trim ());
		}
		for (int i = 0; i < n; i++) {
			FmSpecies species = tabIndex_speciesMap.get (i);
			species.rateOfSeedProduction = Check.doubleValue (rateOfSeedProduction[i].getText ().trim ());
		}


		// Canceling is no more possible
		cancel.setEnabled (false);

		return true;
	}

	private void saveAction () {
		String fileName = fmSets.castaneaFile;
		if (!Question.ask (MainFrame.getInstance (), Translator.swap ("FmCastaneaEditor.confirm"), Translator
				.swap ("FmCastaneaEditor.areYouSureYouWantToOverwriteTheFile") + " : " + fileName)) { return; }

		if (!checkEverything ()) { return; }

		try {

			new FmSpeciesReader (fmSets).save (fileName);
		} catch (Exception e) {
			Log.println (Log.ERROR, "FmCastaneaEditor.saveAction ()", "Exception", e);
			MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.errorWhileSavingCastaneaFile"));
		}
	}

	private void saveAsAction () {

		boolean trouble = false;
		JFileChooser chooser = null;
		int returnVal = 0;
		do {
			trouble = false;
			chooser = new JSmartFileChooser (Translator.swap ("FmCastaneaEditor.saveParameters"),
					Translator.swap ("FmCastaneaEditor.save"), Translator.swap ("FmCastaneaEditor.save"),
					Settings.getProperty ("capsis.inventory.path", PathManager.getDir ("data")), false); // DIRECTORIES_ONLY=false
			returnVal = chooser.showDialog (MainFrame.getInstance (), null); // null : approveButton
																				// text was already
																				// set

			if (returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile ().exists ()) {
				if (!Question.ask (MainFrame.getInstance (), Translator.swap ("FmCastaneaEditor.confirm"), ""
						+ chooser.getSelectedFile ().getPath () + "\n"
						+ Translator.swap ("FmCastaneaEditor.fileExistsPleaseConfirmOverwrite"))) {
					trouble = true;
				}
			}
		} while (trouble);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (!checkEverything ()) { return; }

			String fileName = chooser.getSelectedFile ().toString ();
			Settings.setProperty ("capsis.inventory.path", fileName);

			try {
				new FmSpeciesReader (fmSets).save (fileName);
			} catch (Exception e) {
				Log.println (Log.ERROR, "FmCastaneaEditor.saveAction ()", "Exception", e);
				MessageDialog.print (this, Translator.swap ("FmCastaneaEditor.errorWhileSavingCastaneaFile"));
			}
		}

	}

	private void okAction () {
		if (!checkEverything ()) { return; }

		// checks...
		// ~ if (!Check.isFile (standInventory.getText ().trim ())) {
		// ~ MessageDialog.promptError (Translator.swap (
		// ~ "FmCastaneaEditor.standInventoryIsNotFile"));
		// ~ return;
		// ~ }

		// ~ if (!Check.isFile (castaneaFile.getText ().trim ())) {
		// ~ MessageDialog.promptError (Translator.swap (
		// ~ "FmCastaneaEditor.castaneaFileIsNotFile"));
		// ~ return;
		// ~ }

		// ~ if (Check.isEmpty (plotName.getText ().trim ())) {
		// ~ MessageDialog.promptError (Translator.swap (
		// ~ "FmCastaneaEditor.plotNameIsEmpty"));
		// ~ return;
		// ~ }

		// ~ if (!Check.isInt (numberOfSpecies.getText ().trim ())) {
		// ~ MessageDialog.promptError (Translator.swap (
		// ~ "FmCastaneaEditor.numberOfSpeciesIsNotInt"));
		// ~ return;
		// ~ }

		// ~ fmSets.standInventory = standInventory.getText ().trim ();
		// ~ fmSets.castaneaFile = castaneaFile.getText ().trim ();
		// ~ fmSets.plotName = plotName.getText ().trim ();
		// ~ fmSets.numberOfSpecies = Check.intValue (numberOfSpecies.getText ().trim ());

		setValidDialog (true);
	}

	/**
	 * Some button was hit...
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (save)) {
			saveAction ();
		} else if (evt.getSource ().equals (saveAs)) {
			saveAsAction ();
		} else if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**
	 * Inits the GUI.
	 */
	private void createUI () {
		tabs = new JTabbedPane ();
		// The following line enables to use scrolling tabs.
		tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);

		// General
		ColumnPanel col1 = new ColumnPanel ();

		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.MRN") + " :", 120));
		MRN = new JTextField (5);
		MRN.setText ("" + fmSets.MRN);
		MRN.setToolTipText (Translator.swap ("FmCastaneaEditor.MRN_Help"));
		l0.add (MRN);
		l0.addStrut0 ();
		col1.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.QDIX") + " :", 120));
		QDIX = new JTextField (5);
		QDIX.setText ("" + fmSets.QDIX);
		QDIX.setToolTipText (Translator.swap ("FmCastaneaEditor.QDIX_Help"));
		l1.add (QDIX);
		l1.addStrut0 ();
		col1.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.tc") + " :", 120));
		tc = new JTextField (5);
		tc.setText ("" + fmSets.tc);
		tc.setToolTipText (Translator.swap ("FmCastaneaEditor.tc_Help"));
		l2.add (tc);
		l2.addStrut0 ();
		col1.add (l2);

		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Tbase") + " :", 120));
		Tbase = new JTextField (5);
		Tbase.setText ("" + fmSets.Tbase);
		Tbase.setToolTipText (Translator.swap ("FmCastaneaEditor.Tbase_Help"));
		l3.add (Tbase);
		l3.addStrut0 ();
		col1.add (l3);

		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Oi0") + " :", 120));
		Oi0 = new JTextField (5);
		Oi0.setText ("" + fmSets.Oi0);
		Oi0.setToolTipText (Translator.swap ("FmCastaneaEditor.Oi0_Help"));
		l4.add (Oi0);
		l4.addStrut0 ();
		col1.add (l4);

		LinePanel l5 = new LinePanel ();
		l5.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Ko0") + " :", 120));
		Ko0 = new JTextField (5);
		Ko0.setText ("" + fmSets.Ko0);
		Ko0.setToolTipText (Translator.swap ("FmCastaneaEditor.Ko0_Help"));
		l5.add (Ko0);
		l5.addStrut0 ();
		col1.add (l5);

		LinePanel l6 = new LinePanel ();
		l6.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Kc0") + " :", 120));
		Kc0 = new JTextField (5);
		Kc0.setText ("" + fmSets.Kc0);
		Kc0.setToolTipText (Translator.swap ("FmCastaneaEditor.Kc0_Help"));
		l6.add (Kc0);
		l6.addStrut0 ();
		col1.add (l6);

		LinePanel l7 = new LinePanel ();
		l7.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cVc") + " :", 120));
		cVc = new JTextField (5);
		cVc.setText ("" + fmSets.cVc);
		cVc.setToolTipText (Translator.swap ("FmCastaneaEditor.cVc_Help"));
		l7.add (cVc);
		l7.addStrut0 ();
		col1.add (l7);

		LinePanel l8 = new LinePanel ();
		l8.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cVo") + " :", 120));
		cVo = new JTextField (5);
		cVo.setText ("" + fmSets.cVo);
		cVo.setToolTipText (Translator.swap ("FmCastaneaEditor.cVo_Help"));
		l8.add (cVo);
		l8.addStrut0 ();
		col1.add (l8);

		LinePanel l9 = new LinePanel ();
		l9.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cKc") + " :", 120));
		cKc = new JTextField (5);
		cKc.setText ("" + fmSets.cKc);
		cKc.setToolTipText (Translator.swap ("FmCastaneaEditor.cKc_Help"));
		l9.add (cKc);
		l9.addStrut0 ();
		col1.add (l9);

		LinePanel l10 = new LinePanel ();
		l10.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cKo") + " :", 120));
		cKo = new JTextField (5);
		cKo.setText ("" + fmSets.cKo);
		cKo.setToolTipText (Translator.swap ("FmCastaneaEditor.cKo_Help"));
		l10.add (cKo);
		l10.addStrut0 ();
		col1.add (l10);

		LinePanel l11 = new LinePanel ();
		l11.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cgama") + " :", 120));
		cgama = new JTextField (5);
		cgama.setText ("" + fmSets.cgama);
		cgama.setToolTipText (Translator.swap ("FmCastaneaEditor.cgama_Help"));
		l11.add (cgama);
		l11.addStrut0 ();
		col1.add (l11);

		LinePanel l12 = new LinePanel ();
		l12.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.EaKc") + " :", 120));
		EaKc = new JTextField (5);
		EaKc.setText ("" + fmSets.EaKc);
		EaKc.setToolTipText (Translator.swap ("FmCastaneaEditor.EaKc_Help"));
		l12.add (EaKc);
		l12.addStrut0 ();
		col1.add (l12);

		LinePanel l13 = new LinePanel ();
		l13.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.EaKo") + " :", 120));
		EaKo = new JTextField (5);
		EaKo.setText ("" + fmSets.EaKo);
		EaKo.setToolTipText (Translator.swap ("FmCastaneaEditor.EaKo_Help"));
		l13.add (EaKo);
		l13.addStrut0 ();
		col1.add (l13);

		LinePanel l14 = new LinePanel ();
		l14.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.EaVc") + " :", 120));
		EaVc = new JTextField (5);
		EaVc.setText ("" + fmSets.EaVc);
		EaVc.setToolTipText (Translator.swap ("FmCastaneaEditor.EaVc_Help"));
		l14.add (EaVc);
		l14.addStrut0 ();
		col1.add (l14);

		LinePanel l15 = new LinePanel ();
		l15.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.EaVo") + " :", 120));
		EaVo = new JTextField (5);
		EaVo.setText ("" + fmSets.EaVo);
		EaVo.setToolTipText (Translator.swap ("FmCastaneaEditor.EaVo_Help"));
		l15.add (EaVo);
		l15.addStrut0 ();
		col1.add (l15);

		LinePanel l16 = new LinePanel ();
		l16.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Eagama") + " :", 120));
		Eagama = new JTextField (5);
		Eagama.setText ("" + fmSets.Eagama);
		Eagama.setToolTipText (Translator.swap ("FmCastaneaEditor.Eagama_Help"));
		l16.add (Eagama);
		l16.addStrut0 ();
		col1.add (l16);

		LinePanel l17 = new LinePanel ();
		l17.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.rdtq") + " :", 120));
		rdtq = new JTextField (5);
		rdtq.setText ("" + fmSets.rdtq);
		rdtq.setToolTipText (Translator.swap ("FmCastaneaEditor.rdtq_Help"));
		l17.add (rdtq);
		l17.addStrut0 ();
		col1.add (l17);

		LinePanel l18 = new LinePanel ();
		l18.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.coefbeta") + " :", 120));
		coefbeta = new JTextField (5);
		coefbeta.setText ("" + fmSets.coefbeta);
		coefbeta.setToolTipText (Translator.swap ("FmCastaneaEditor.coefbeta_Help"));
		l18.add (coefbeta);
		l18.addStrut0 ();
		col1.add (l18);

		LinePanel l19 = new LinePanel ();
		l19.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.KAR") + " :", 120));
		KAR = new JTextField (5);
		KAR.setText ("" + fmSets.KAR);
		KAR.setToolTipText (Translator.swap ("FmCastaneaEditor.KAR_Help"));
		l19.add (KAR);
		l19.addStrut0 ();
		col1.add (l19);

		col1.addGlue ();
		ColumnPanel col2 = new ColumnPanel ();

		LinePanel l20 = new LinePanel ();
		l20.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Cd") + " :", 120));
		Cd = new JTextField (5);
		Cd.setText ("" + fmSets.Cd);
		Cd.setToolTipText (Translator.swap ("FmCastaneaEditor.Cd_Help"));
		l20.add (Cd);
		l20.addStrut0 ();
		col2.add (l20);

		LinePanel l21 = new LinePanel ();
		l21.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Zosol") + " :", 120));
		Zosol = new JTextField (5);
		Zosol.setText ("" + fmSets.Zosol);
		Zosol.setToolTipText (Translator.swap ("FmCastaneaEditor.Zosol_Help"));
		l21.add (Zosol);
		l21.addStrut0 ();
		col2.add (l21);

		LinePanel l22 = new LinePanel ();
		l22.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K1") + " :", 120));
		K1 = new JTextField (5);
		K1.setText ("" + fmSets.K1);
		K1.setToolTipText (Translator.swap ("FmCastaneaEditor.K1_Help"));
		l22.add (K1);
		l22.addStrut0 ();
		col2.add (l22);

		LinePanel l23 = new LinePanel ();
		l23.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K2") + " :", 120));
		K2 = new JTextField (5);
		K2.setText ("" + fmSets.K2);
		K2.setToolTipText (Translator.swap ("FmCastaneaEditor.K2_Help"));
		l23.add (K2);
		l23.addStrut0 ();
		col2.add (l23);

		LinePanel l24 = new LinePanel ();
		l24.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K3") + " :", 120));
		K3 = new JTextField (5);
		K3.setText ("" + fmSets.K3);
		K3.setToolTipText (Translator.swap ("FmCastaneaEditor.K3_Help"));
		l24.add (K3);
		l24.addStrut0 ();
		col2.add (l24);

		LinePanel l25 = new LinePanel ();
		l25.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K4") + " :", 120));
		K4 = new JTextField (5);
		K4.setText ("" + fmSets.K4);
		K4.setToolTipText (Translator.swap ("FmCastaneaEditor.K4_Help"));
		l25.add (K4);
		l25.addStrut0 ();
		col2.add (l25);

		LinePanel l26 = new LinePanel ();
		l26.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K5") + " :", 120));
		K5 = new JTextField (5);
		K5.setText ("" + fmSets.K5);
		K5.setToolTipText (Translator.swap ("FmCastaneaEditor.K5_Help"));
		l26.add (K5);
		l26.addStrut0 ();
		col2.add (l26);

		LinePanel l27 = new LinePanel ();
		l27.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K6") + " :", 120));
		K6 = new JTextField (5);
		K6.setText ("" + fmSets.K6);
		K6.setToolTipText (Translator.swap ("FmCastaneaEditor.K6_Help"));
		l27.add (K6);
		l27.addStrut0 ();
		col2.add (l27);

		LinePanel l28 = new LinePanel ();
		l28.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K7") + " :", 120));
		K7 = new JTextField (5);
		K7.setText ("" + fmSets.K7);
		K7.setToolTipText (Translator.swap ("FmCastaneaEditor.K7_Help"));
		l28.add (K7);
		l28.addStrut0 ();
		col2.add (l28);

		LinePanel l29 = new LinePanel ();
		l29.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.K8") + " :", 120));
		K8 = new JTextField (5);
		K8.setText ("" + fmSets.K8);
		K8.setToolTipText (Translator.swap ("FmCastaneaEditor.K8_Help"));
		l29.add (K8);
		l29.addStrut0 ();
		col2.add (l29);

		LinePanel l30 = new LinePanel ();
		l30.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Cph") + " :", 120));
		Cph = new JTextField (5);
		Cph.setText ("" + fmSets.Cph);
		Cph.setToolTipText (Translator.swap ("FmCastaneaEditor.Cph_Help"));
		l30.add (Cph);
		l30.addStrut0 ();
		col2.add (l30);

		LinePanel l31 = new LinePanel ();
		l31.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.R") + " :", 120));
		R = new JTextField (5);
		R.setText ("" + fmSets.R);
		R.setToolTipText (Translator.swap ("FmCastaneaEditor.R_Help"));
		l31.add (R);
		l31.addStrut0 ();
		col2.add (l31);

		LinePanel l32 = new LinePanel ();
		l32.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.IO") + " :", 120));
		IO = new JTextField (5);
		IO.setText ("" + fmSets.IO);
		IO.setToolTipText (Translator.swap ("FmCastaneaEditor.IO_Help"));
		l32.add (IO);
		l32.addStrut0 ();
		col2.add (l32);

		LinePanel l33 = new LinePanel ();
		l33.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.gsolmax") + " :", 120));
		gsolmax = new JTextField (5);
		gsolmax.setText ("" + fmSets.gsolmax);
		gsolmax.setToolTipText (Translator.swap ("FmCastaneaEditor.gsolmax_Help"));
		l33.add (gsolmax);
		l33.addStrut0 ();
		col2.add (l33);

		LinePanel l34 = new LinePanel ();
		l34.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.gsolmin") + " :", 120));
		gsolmin = new JTextField (5);
		gsolmin.setText ("" + fmSets.gsolmin);
		gsolmin.setToolTipText (Translator.swap ("FmCastaneaEditor.gsolmin_Help"));
		l34.add (gsolmin);
		l34.addStrut0 ();
		col2.add (l34);

		LinePanel l35 = new LinePanel ();
		l35.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.emsg") + " :", 120));
		emsg = new JTextField (5);
		emsg.setText ("" + fmSets.emsg);
		emsg.setToolTipText (Translator.swap ("FmCastaneaEditor.emsg_Help"));
		l35.add (emsg);
		l35.addStrut0 ();
		col2.add (l35);

		LinePanel l36 = new LinePanel ();
		l36.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Alit") + " :", 120));
		Alit = new JTextField (5);
		Alit.setText ("" + fmSets.Alit);
		Alit.setToolTipText (Translator.swap ("FmCastaneaEditor.Alit_Help"));
		l36.add (Alit);
		l36.addStrut0 ();
		col2.add (l36);

		LinePanel l37 = new LinePanel ();
		l37.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Asoil") + " :", 120));
		Asoil = new JTextField (5);
		Asoil.setText ("" + fmSets.Asoil);
		Asoil.setToolTipText (Translator.swap ("FmCastaneaEditor.Asoil_Help"));
		l37.add (Asoil);
		l37.addStrut0 ();
		col2.add (l37);

		col2.addGlue ();

		LinePanel general = new LinePanel ();
		general.add (new NorthPanel (col1));
		general.add (new NorthPanel (col2));
		general.addStrut0 ();

		general.addGlue ();
		tabs.addTab (Translator.swap ("FmCastaneaEditor.general"), new NorthPanel (general));

		// Species tabs
		Map speciesMap = fmSets.castaneaSpeciesMap;
		int k = 0;
		// fc + hd - 10.6.2008 - try to sort the species list
		Set sortedSpecies = new TreeSet (speciesMap.values ());
		for (Iterator i = sortedSpecies.iterator (); i.hasNext ();) {
			FmSpecies species = (FmSpecies) i.next ();
			tabIndex_speciesMap.put (k, species);
			int castaneaCode = species.castaneaCode;

			ColumnPanel column1 = new ColumnPanel ();

			LinePanel l50 = new LinePanel ();
			l50.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CRF") + " :", 120));
			CRF[k] = new JTextField (5);
			CRF[k].setText ("" + species.CRF);
			CRF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CRF_Help"));
			l50.add (CRF[k]);
			l50.addStrut0 ();
			column1.add (l50);

			LinePanel l51 = new LinePanel ();
			l51.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CRRG") + " :", 120));
			CRRG[k] = new JTextField (5);
			CRRG[k].setText ("" + species.CRRG);
			CRRG[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CRRG_Help"));
			l51.add (CRRG[k]);
			l51.addStrut0 ();
			column1.add (l51);

			LinePanel l52 = new LinePanel ();
			l52.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CRRF") + " :", 120));
			CRRF[k] = new JTextField (5);
			CRRF[k].setText ("" + species.CRRF);
			CRRF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CRRF_Help"));
			l52.add (CRRF[k]);
			l52.addStrut0 ();
			column1.add (l52);

			LinePanel l53 = new LinePanel ();
			l53.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CRBV") + " :", 120));
			CRBV[k] = new JTextField (5);
			CRBV[k].setText ("" + species.CRBV);
			CRBV[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CRBV_Help"));
			l53.add (CRBV[k]);
			l53.addStrut0 ();
			column1.add (l53);

			LinePanel l54 = new LinePanel ();
			l54.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.tronviv") + " :", 120));
			tronviv[k] = new JTextField (5);
			tronviv[k].setText ("" + species.tronviv);
			tronviv[k].setToolTipText (Translator.swap ("FmCastaneaEditor.tronviv_Help"));
			l54.add (tronviv[k]);
			l54.addStrut0 ();
			column1.add (l54);

			LinePanel l55 = new LinePanel ();
			l55.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.branviv") + " :", 120));
			branviv[k] = new JTextField (5);
			branviv[k].setText ("" + species.branviv);
			branviv[k].setToolTipText (Translator.swap ("FmCastaneaEditor.branviv_Help"));
			l55.add (branviv[k]);
			l55.addStrut0 ();
			column1.add (l55);

			LinePanel l56 = new LinePanel ();
			l56.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.rgviv") + " :", 120));
			rgviv[k] = new JTextField (5);
			rgviv[k].setText ("" + species.rgviv);
			rgviv[k].setToolTipText (Translator.swap ("FmCastaneaEditor.rgviv_Help"));
			l56.add (rgviv[k]);
			l56.addStrut0 ();
			column1.add (l56);

			LinePanel l57 = new LinePanel ();
			l57.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Lignroots") + " :", 120));
			Lignroots[k] = new JTextField (5);
			Lignroots[k].setText ("" + species.Lignroots);
			Lignroots[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Lignroots_Help"));
			l57.add (Lignroots[k]);
			l57.addStrut0 ();
			column1.add (l57);

			LinePanel l58 = new LinePanel ();
			l58.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LIGNrl") + " :", 120));
			LIGNrl[k] = new JTextField (5);
			LIGNrl[k].setText ("" + species.LIGNrl);
			LIGNrl[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LIGNrl_Help"));
			l58.add (LIGNrl[k]);
			l58.addStrut0 ();
			column1.add (l58);

			LinePanel l59 = new LinePanel ();
			l59.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LIGNll") + " :", 120));
			LIGNll[k] = new JTextField (5);
			LIGNll[k].setText ("" + species.LIGNll);
			LIGNll[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LIGNll_Help"));
			l59.add (LIGNll[k]);
			l59.addStrut0 ();
			column1.add (l59);

			LinePanel l60 = new LinePanel ();
			l60.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LIGNfb") + " :", 120));
			LIGNfb[k] = new JTextField (5);
			LIGNfb[k].setText ("" + species.LIGNfb);
			LIGNfb[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LIGNfb_Help"));
			l60.add (LIGNfb[k]);
			l60.addStrut0 ();
			column1.add (l60);

			LinePanel l61 = new LinePanel ();
			l61.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LIGNcb") + " :", 120));
			LIGNcb[k] = new JTextField (5);
			LIGNcb[k].setText ("" + species.LIGNcb);
			LIGNcb[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LIGNcb_Help"));
			l61.add (LIGNcb[k]);
			l61.addStrut0 ();
			column1.add (l61);

			LinePanel l62 = new LinePanel ();
			l62.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LIGNcr") + " :", 120));
			LIGNcr[k] = new JTextField (5);
			LIGNcr[k].setText ("" + species.LIGNcr);
			LIGNcr[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LIGNcr_Help"));
			l62.add (LIGNcr[k]);
			l62.addStrut0 ();
			column1.add (l62);

			LinePanel l63 = new LinePanel ();
			l63.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TGSS") + " :", 120));
			TGSS[k] = new JTextField (5);
			TGSS[k].setText ("" + species.TGSS);
			TGSS[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TGSS_Help"));
			l63.add (TGSS[k]);
			l63.addStrut0 ();
			column1.add (l63);

			LinePanel l64 = new LinePanel ();
			l64.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.leafNitrogen") + " :", 120));
			leafNitrogen[k] = new JTextField (5);
			leafNitrogen[k].setText ("" + species.leafNitrogen);
			leafNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.leafNitrogen_Help"));
			l64.add (leafNitrogen[k]);
			l64.addStrut0 ();
			column1.add (l64);

			LinePanel l64_2 = new LinePanel ();
			l64_2.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.coarseRootsNitrogen") + " :", 120));
			coarseRootsNitrogen[k] = new JTextField (5);
			coarseRootsNitrogen[k].setText ("" + species.coarseRootsNitrogen);
			coarseRootsNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.coarseRootsNitrogen_Help"));
			l64_2.add (coarseRootsNitrogen[k]);
			l64_2.addStrut0 ();
			column1.add (l64_2);

			LinePanel l64_3 = new LinePanel ();
			l64_3.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.fineRootsNitrogen") + " :", 120));
			fineRootsNitrogen[k] = new JTextField (5);
			fineRootsNitrogen[k].setText ("" + species.fineRootsNitrogen);
			fineRootsNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.fineRootsNitrogen_Help"));
			l64_3.add (fineRootsNitrogen[k]);
			l64_3.addStrut0 ();
			column1.add (l64_3);

			LinePanel l64_4 = new LinePanel ();
			l64_4.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.branchesNitrogen") + " :", 120));
			branchesNitrogen[k] = new JTextField (5);
			branchesNitrogen[k].setText ("" + species.branchesNitrogen);
			branchesNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.branchesNitrogen_Help"));
			l64_4.add (branchesNitrogen[k]);
			l64_4.addStrut0 ();
			column1.add (l64_4);

			LinePanel l64_5 = new LinePanel ();
			l64_5.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.stemNitrogen") + " :", 120));
			stemNitrogen[k] = new JTextField (5);
			stemNitrogen[k].setText ("" + species.stemNitrogen);
			stemNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.stemNitrogen_Help"));
			l64_5.add (stemNitrogen[k]);
			l64_5.addStrut0 ();
			column1.add (l64_5);

			LinePanel l64_6 = new LinePanel ();
			l64_6.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.reservesNitrogen") + " :", 120));
			reservesNitrogen[k] = new JTextField (5);
			reservesNitrogen[k].setText ("" + species.leafNitrogen);
			reservesNitrogen[k].setToolTipText (Translator.swap ("FmCastaneaEditor.reservesNitrogen_Help"));
			l64_6.add (reservesNitrogen[k]);
			l64_6.addStrut0 ();
			column1.add (l64_6);

			LinePanel l74 = new LinePanel ();
			l74.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.LMA0") + " :", 120));
			LMA0[k] = new JTextField (5);
			LMA0[k].setText ("" + species.LMA0);
			LMA0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.LMA0_Help"));
			l74.add (LMA0[k]);
			l74.addStrut0 ();
			column1.add (l74);

			LinePanel l75 = new LinePanel ();
			l75.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.KLMA") + " :", 120));
			KLMA[k] = new JTextField (5);
			KLMA[k].setText ("" + species.KLMA);
			KLMA[k].setToolTipText (Translator.swap ("FmCastaneaEditor.KLMA_Help"));
			l75.add (KLMA[k]);
			l75.addStrut0 ();
			column1.add (l75);
			column1.addGlue ();

			ColumnPanel column2 = new ColumnPanel ();

			LinePanel l130 = new LinePanel ();
			l130.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.potsoilToWood") + " :", 120));
			potsoilToWood[k] = new JTextField (5);
			potsoilToWood[k].setText ("" + species.potsoilToWood);
			potsoilToWood[k].setToolTipText (Translator.swap ("FmCastaneaEditor.potsoilToWood_Help"));
			l130.add (potsoilToWood[k]);
			l130.addStrut0 ();
			column2.add (l130);

			LinePanel l131 = new LinePanel ();
			l131.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.GBVmin") + " :", 120));
			GBVmin[k] = new JTextField (5);
			GBVmin[k].setText ("" + species.GBVmin);
			GBVmin[k].setToolTipText (Translator.swap ("FmCastaneaEditor.GBVmin_Help"));
			l131.add (GBVmin[k]);
			l131.addStrut0 ();
			column2.add (l131);

			LinePanel l132 = new LinePanel ();
			l132.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TMRF") + " :", 120));
			TMRF[k] = new JTextField (5);
			TMRF[k].setText ("" + species.TMRF);
			TMRF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TMRF_Help"));
			l132.add (TMRF[k]);
			l132.addStrut0 ();
			column2.add (l132);


			LinePanel l65 = new LinePanel ();
			l65.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.ratioBR") + " :", 120));
			ratioBR[k] = new JTextField (5);
			ratioBR[k].setText ("" + species.ratioBR);
			ratioBR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.ratioBR_Help"));
			l65.add (ratioBR[k]);
			l65.addStrut0 ();
			column2.add (l65);

			LinePanel l66 = new LinePanel ();
			l66.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RS") + " :", 120));
			RS[k] = new JTextField (5);
			RS[k].setText ("" + species.RS);
			RS[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RS_Help"));
			l66.add (RS[k]);
			l66.addStrut0 ();
			column2.add (l66);

			LinePanel l66_1 = new LinePanel ();
			l66_1.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.ratioG") + " :", 120));
			ratioG[k] = new JTextField (5);
			ratioG[k].setText ("" + species.ratioG);
			ratioG[k].setToolTipText (Translator.swap ("FmCastaneaEditor.ratioG_Help"));
			l66_1.add (ratioG[k]);
			l66_1.addStrut0 ();
			column2.add (l66_1);

			LinePanel l66_2 = new LinePanel ();
			l66_2.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.coefrac") + " :", 120));
			coefrac[k] = new JTextField (5);
			coefrac[k].setText ("" + species.coefrac);
			coefrac[k].setToolTipText (Translator.swap ("FmCastaneaEditor.coefrac_Help"));
			l66_2.add (coefrac[k]);
			l66_2.addStrut0 ();
			column2.add (l66_2);

			LinePanel l67 = new LinePanel ();
			l67.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TMBV") + " :", 120));
			TMBV[k] = new JTextField (5);
			TMBV[k].setText ("" + species.TMBV);
			TMBV[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TMBV_Help"));
			l67.add (TMBV[k]);
			l67.addStrut0 ();
			column2.add (l67);

			LinePanel l68 = new LinePanel ();
			l68.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.SF") + " :", 120));
			SF[k] = new JTextField (5);
			SF[k].setText ("" + species.SF);
			SF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.SF_Help"));
			l68.add (SF[k]);
			l68.addStrut0 ();
			column2.add (l68);


			LinePanel l69 = new LinePanel ();
			l69.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CrownArea1") + " :", 120));
			CrownArea1[k] = new JTextField (5);
			CrownArea1[k].setText ("" + species.CrownArea1);
			CrownArea1[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CrownArea1_Help"));
			l69.add (CrownArea1[k]);
			l69.addStrut0 ();
			column2.add (l69);

			LinePanel l70 = new LinePanel ();
			l70.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CrownArea2") + " :", 120));
			CrownArea2[k] = new JTextField (5);
			CrownArea2[k].setText ("" + species.CrownArea2);
			CrownArea2[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CrownArea2_Help"));
			l70.add (CrownArea2[k]);
			l70.addStrut0 ();
			column2.add (l70);

			LinePanel l71 = new LinePanel ();
			l71.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CoefLAI1") + " :", 120));
			CoefLAI1[k] = new JTextField (5);
			CoefLAI1[k].setText ("" + species.CoefLAI1);
			CoefLAI1[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CoefLAI1_Help"));
			l71.add (CoefLAI1[k]);
			l71.addStrut0 ();
			column2.add (l71);

			LinePanel l72 = new LinePanel ();
			l72.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CoefLAI2") + " :", 120));
			CoefLAI2[k] = new JTextField (5);
			CoefLAI2[k].setText ("" + species.CoefLAI2);
			CoefLAI2[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CoefLAI2_Help"));
			l72.add (CoefLAI2[k]);
			l72.addStrut0 ();
			column2.add (l72);

			LinePanel l72bis = new LinePanel ();
			l72bis.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CoefLAI3") + " :", 120));
			CoefLAI3[k] = new JTextField (5);
			CoefLAI3[k].setText ("" + species.CoefLAI3);
			CoefLAI3[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CoefLAI3_Help"));
			l72bis.add (CoefLAI3[k]);
			l72bis.addStrut0 ();
			column2.add (l72bis);

			LinePanel l72_3 = new LinePanel ();
			l72_3.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.aGF") + " :", 120));
			aGF[k] = new JTextField (5);
			aGF[k].setText ("" + species.aGF);
			aGF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.aGF_Help"));
			l72_3.add (aGF[k]);
			l72_3.addStrut0 ();
			column2.add (l72_3);

			LinePanel l72_4 = new LinePanel ();
			l72_4.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.bGF") + " :", 120));
			bGF[k] = new JTextField (5);
			bGF[k].setText ("" + species.bGF);
			bGF[k].setToolTipText (Translator.swap ("FmCastaneaEditor.bGF_Help"));
			l72_4.add (bGF[k]);
			l72_4.addStrut0 ();
			column2.add (l72_4);

			LinePanel l72_5 = new LinePanel ();
			l72_5.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Phi") + " :", 120));
			Phi[k] = new JTextField (5);
			Phi[k].setText ("" + species.Phi);
			Phi[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Phi_Help"));
			l72_5.add (Phi[k]);
			l72_5.addStrut0 ();
			column2.add (l72_5);

			LinePanel l72_6 = new LinePanel ();
			l72_6.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.ros") + " :", 120));
			ros[k] = new JTextField (5);
			ros[k].setText ("" + species.ros);
			ros[k].setToolTipText (Translator.swap ("FmCastaneaEditor.ros_Help"));
			l72_6.add (ros[k]);
			l72_6.addStrut0 ();
			column2.add (l72_6);


			LinePanel l73 = new LinePanel ();
			l73.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.clumping") + " :", 120));
			clumping[k] = new JTextField (5);
			clumping[k].setText ("" + species.defaultClumping);
			clumping[k].setToolTipText (Translator.swap ("FmCastaneaEditor.clumping_Help"));
			l73.add (clumping[k]);
			l73.addStrut0 ();
			column2.add (l73);

			LinePanel l76 = new LinePanel ();
			l76.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.alphal") + " :", 120));
			alphal[k] = new JTextField (5);
			alphal[k].setText ("" + species.alphal);
			alphal[k].setToolTipText (Translator.swap ("FmCastaneaEditor.alphal_Help"));
			l76.add (alphal[k]);
			l76.addStrut0 ();
			column2.add (l76);

			LinePanel l77 = new LinePanel ();
			l77.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.alphab") + " :", 120));
			alphab[k] = new JTextField (5);
			alphab[k].setText ("" + species.alphab);
			alphab[k].setToolTipText (Translator.swap ("FmCastaneaEditor.alphab_Help"));
			l77.add (alphab[k]);
			l77.addStrut0 ();
			column2.add (l77);


			column2.addGlue ();
			ColumnPanel column3 = new ColumnPanel ();




			LinePanel l78 = new LinePanel ();
			l78.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RauwPIR") + " :", 120));
			RauwPIR[k] = new JTextField (5);
			RauwPIR[k].setText ("" + species.RauwPIR);
			RauwPIR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RauwPIR_Help"));
			l78.add (RauwPIR[k]);
			l78.addStrut0 ();
			column3.add (l78);

			LinePanel l79 = new LinePanel ();
			l79.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RauwPAR") + " :", 120));
			RauwPAR[k] = new JTextField (5);
			RauwPAR[k].setText ("" + species.RauwPAR);
			RauwPAR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RauwPAR_Help"));
			l79.add (RauwPAR[k]);
			l79.addStrut0 ();
			column3.add (l79);

			LinePanel l80 = new LinePanel ();
			l80.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RaufPIR") + " :", 120));
			RaufPIR[k] = new JTextField (5);
			RaufPIR[k].setText ("" + species.RaufPIR);
			RaufPIR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RaufPIR_Help"));
			l80.add (RaufPIR[k]);
			l80.addStrut0 ();
			column3.add (l80);

			LinePanel l81 = new LinePanel ();
			l81.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TaufPIR") + " :", 120));
			TaufPIR[k] = new JTextField (5);
			TaufPIR[k].setText ("" + species.TaufPIR);
			TaufPIR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TaufPIR_Help"));
			l81.add (TaufPIR[k]);
			l81.addStrut0 ();
			column3.add (l81);

			LinePanel l82 = new LinePanel ();
			l82.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RaufPAR") + " :", 120));
			RaufPAR[k] = new JTextField (5);
			RaufPAR[k].setText ("" + species.RaufPAR);
			RaufPAR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RaufPAR_Help"));
			l82.add (RaufPAR[k]);
			l82.addStrut0 ();
			column3.add (l82);

			LinePanel l83 = new LinePanel ();
			l83.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TaufPAR") + " :", 120));
			TaufPAR[k] = new JTextField (5);
			TaufPAR[k].setText ("" + species.TaufPAR);
			TaufPAR[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TaufPAR_Help"));
			l83.add (TaufPAR[k]);
			l83.addStrut0 ();
			column3.add (l83);

			LinePanel l84 = new LinePanel ();
			l84.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.emsf") + " :", 120));
			emsf[k] = new JTextField (5);
			emsf[k].setText ("" + species.emsf);
			emsf[k].setToolTipText (Translator.swap ("FmCastaneaEditor.emsf_Help"));
			l84.add (emsf[k]);
			l84.addStrut0 ();
			column3.add (l84);


			LinePanel l85 = new LinePanel ();
			l85.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Tleaf") + " :", 120));
			Tleaf[k] = new JTextField (5);
			Tleaf[k].setText ("" + species.Tleaf);
			Tleaf[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Tleaf_Help"));
			l85.add (Tleaf[k]);
			l85.addStrut0 ();
			column3.add (l85);

			LinePanel l86 = new LinePanel ();
			l86.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Tbark") + " :", 120));
			Tbark[k] = new JTextField (5);
			Tbark[k].setText ("" + species.Tbark);
			Tbark[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Tbark_Help"));
			l86.add (Tbark[k]);
			l86.addStrut0 ();
			column3.add (l86);

			LinePanel l87 = new LinePanel ();
			l87.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CIA") + " :", 120));
			CIA[k] = new JTextField (5);
			CIA[k].setText ("" + species.CIA);
			CIA[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CIA_Help"));
			l87.add (CIA[k]);
			l87.addStrut0 ();
			column3.add (l87);

			LinePanel l88 = new LinePanel ();
			l88.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CIB") + " :", 120));
			CIB[k] = new JTextField (5);
			CIB[k].setText ("" + species.CIB);
			CIB[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CIB_Help"));
			l88.add (CIB[k]);
			l88.addStrut0 ();
			column3.add (l88);

			LinePanel l89 = new LinePanel ();
			l89.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.propec") + " :", 120));
			propec[k] = new JTextField (5);
			propec[k].setText ("" + species.propec);
			propec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.propec_Help"));
			l89.add (propec[k]);
			l89.addStrut0 ();
			column3.add (l89);

			LinePanel l90 = new LinePanel ();
			l90.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.g0") + " :", 120));
			g0[k] = new JTextField (5);
			g0[k].setText ("" + species.g0);
			g0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.g0_Help"));
			l90.add (g0[k]);
			l90.addStrut0 ();
			column3.add (l90);

			LinePanel l91 = new LinePanel ();
			l91.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.g1") + " :", 120));
			g1[k] = new JTextField (5);
			g1[k].setText ("" + species.g1);
			g1[k].setToolTipText (Translator.swap ("FmCastaneaEditor.g1_Help"));
			l91.add (g1[k]);
			l91.addStrut0 ();
			column3.add (l91);

			LinePanel l133 = new LinePanel ();
			l133.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.RSoilToleaves") + " :", 120));
			RSoilToleaves[k] = new JTextField (5);
			RSoilToleaves[k].setText ("" + species.RSoilToleaves);
			RSoilToleaves[k].setToolTipText (Translator.swap ("FmCastaneaEditor.RSoilToleaves_Help"));
			l133.add (RSoilToleaves[k]);
			l133.addStrut0 ();
			column3.add (l133);

			LinePanel l134 = new LinePanel ();
			l134.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.CapSoilToleaves") + " :", 120));
			CapSoilToleaves[k] = new JTextField (5);
			CapSoilToleaves[k].setText ("" + species.CapSoilToleaves);
			CapSoilToleaves[k].setToolTipText (Translator.swap ("FmCastaneaEditor.CapSoilToleaves_Help"));
			l134.add (CapSoilToleaves[k]);
			l134.addStrut0 ();
			column3.add (l134);

			LinePanel l92 = new LinePanel ();
			l92.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.EaVJ") + " :", 120));
			EaVJ[k] = new JTextField (5);
			EaVJ[k].setText ("" + species.EaVJ);
			EaVJ[k].setToolTipText (Translator.swap ("FmCastaneaEditor.EaVJ_Help"));
			l92.add (EaVJ[k]);
			l92.addStrut0 ();
			column3.add (l92);

			LinePanel l93 = new LinePanel ();
			l93.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.ETT") + " :", 120));
			ETT[k] = new JTextField (5);
			ETT[k].setText ("" + species.ETT);
			ETT[k].setToolTipText (Translator.swap ("FmCastaneaEditor.ETT_Help"));
			l93.add (ETT[k]);
			l93.addStrut0 ();
			column3.add (l93);

			LinePanel l94 = new LinePanel ();
			l94.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.JMT") + " :", 120));
			JMT[k] = new JTextField (5);
			JMT[k].setText ("" + species.JMT);
			JMT[k].setToolTipText (Translator.swap ("FmCastaneaEditor.JMT_Help"));
			l94.add (JMT[k]);
			l94.addStrut0 ();
			column3.add (l94);

			LinePanel l95 = new LinePanel ();
			l95.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.NC") + " :", 120));
			NC[k] = new JTextField (5);
			NC[k].setText ("" + species.NC);
			NC[k].setToolTipText (Translator.swap ("FmCastaneaEditor.NC_Help"));
			l95.add (NC[k]);
			l95.addStrut0 ();
			column3.add (l95);

			LinePanel l96 = new LinePanel ();
			l96.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.teta") + " :", 120));
			teta[k] = new JTextField (5);
			teta[k].setText ("" + species.teta);
			teta[k].setToolTipText (Translator.swap ("FmCastaneaEditor.teta_Help"));
			l96.add (teta[k]);
			l96.addStrut0 ();
			column3.add (l96);

			column3.addGlue ();
			ColumnPanel column4 = new ColumnPanel ();

			LinePanel l97 = new LinePanel ();
			l97.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TBASEA") + " :", 120));
			TBASEA[k] = new JTextField (5);
			TBASEA[k].setText ("" + species.teta);
			TBASEA[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TBASEA_Help"));
			l97.add (TBASEA[k]);
			l97.addStrut0 ();
			column4.add (l97);

			LinePanel l98 = new LinePanel ();
			l98.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TBASEB") + " :", 120));
			TBASEB[k] = new JTextField (5);
			TBASEB[k].setText ("" + species.TBASEB);
			TBASEB[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TBASEB_Help"));
			l98.add (TBASEB[k]);
			l98.addStrut0 ();
			column4.add (l98);

			LinePanel l99 = new LinePanel ();
			l99.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TBASEC") + " :", 120));
			TBASEC[k] = new JTextField (5);
			TBASEC[k].setText ("" + species.TBASEC);
			TBASEC[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TBASEC_Help"));
			l99.add (TBASEC[k]);
			l99.addStrut0 ();
			column4.add (l99);

			LinePanel l100 = new LinePanel ();
			l100.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.NSTART") + " :", 120));
			NSTART[k] = new JTextField (5);
			NSTART[k].setText ("" + species.NSTART);
			NSTART[k].setToolTipText (Translator.swap ("FmCastaneaEditor.NSTART_Help"));
			l100.add (NSTART[k]);
			l100.addStrut0 ();
			column4.add (l100);

			LinePanel l101 = new LinePanel ();
			l101.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.NSTART3") + " :", 120));
			NSTART3[k] = new JTextField (5);
			NSTART3[k].setText ("" + species.NSTART3);
			NSTART3[k].setToolTipText (Translator.swap ("FmCastaneaEditor.NSTART3_Help"));
			l101.add (NSTART3[k]);
			l101.addStrut0 ();
			column4.add (l101);

			LinePanel l102 = new LinePanel ();
			l102.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TSUMBB") + " :", 120));
			TSUMBB[k] = new JTextField (5);
			TSUMBB[k].setText ("" + species.TSUMBB);
			TSUMBB[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TSUMBB_Help"));
			l102.add (TSUMBB[k]);
			l102.addStrut0 ();
			column4.add (l102);

			LinePanel l103 = new LinePanel ();
			l103.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.HSUMFL") + " :", 120));
			HSUMFL[k] = new JTextField (5);
			HSUMFL[k].setText ("" + species.HSUMFL);
			HSUMFL[k].setToolTipText (Translator.swap ("FmCastaneaEditor.HSUMFL_Help"));
			l103.add (HSUMFL[k]);
			l103.addStrut0 ();
			column4.add (l103);

			LinePanel l104 = new LinePanel ();
			l104.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.HSUMLMA") + " :", 120));
			HSUMLMA[k] = new JTextField (5);
			HSUMLMA[k].setText ("" + species.HSUMLMA);
			HSUMLMA[k].setToolTipText (Translator.swap ("FmCastaneaEditor.THSUMLMA_Help"));
			l104.add (HSUMLMA[k]);
			l104.addStrut0 ();
			column4.add (l104);

			LinePanel l105 = new LinePanel ();
			l105.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TSUMLFAL") + " :", 120));
			TSUMLFAL[k] = new JTextField (5);
			TSUMLFAL[k].setText ("" + species.TSUMLFAL);
			TSUMLFAL[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TSUMLFAL_Help"));
			l105.add (TSUMLFAL[k]);
			l105.addStrut0 ();
			column4.add (l105);

			LinePanel l106 = new LinePanel ();
			l106.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.t0") + " :", 120));
			t0[k] = new JTextField (5);
			t0[k].setText ("" + species.t0);
			t0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.t0_Help"));
			l106.add (t0[k]);
			l106.addStrut0 ();
			column4.add (l106);

			LinePanel l107 = new LinePanel ();
			l107.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Vb") + " :", 120));
			Vb[k] = new JTextField (5);
			Vb[k].setText ("" + species.Vb);
			Vb[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Vb_Help"));
			l107.add (Vb[k]);
			l107.addStrut0 ();
			column4.add (l107);

			LinePanel l108 = new LinePanel ();
			l108.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.d") + " :", 120));
			d[k] = new JTextField (5);
			d[k].setText ("" + species.d);
			d[k].setToolTipText (Translator.swap ("FmCastaneaEditor.d_Help"));
			l108.add (d[k]);
			l108.addStrut0 ();
			column4.add (l108);

			LinePanel l109 = new LinePanel ();
			l109.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.e") + " :", 120));
			e[k] = new JTextField (5);
			e[k].setText ("" + species.e);
			e[k].setToolTipText (Translator.swap ("FmCastaneaEditor.eL_Help"));
			l109.add (e[k]);
			l109.addStrut0 ();
			column4.add (l109);

			LinePanel l110 = new LinePanel ();
			l110.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Ccrit") + " :", 120));
			Ccrit[k] = new JTextField (5);
			Ccrit[k].setText ("" + species.Ccrit);
			Ccrit[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Ccrit_Help"));
			l110.add (Ccrit[k]);
			l110.addStrut0 ();
			column4.add (l110);

			LinePanel l111 = new LinePanel ();
			l111.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Fcrit") + " :", 120));
			Fcrit[k] = new JTextField (5);
			Fcrit[k].setText ("" + species.Fcrit);
			Fcrit[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Fcrit_Help"));
			l111.add (Fcrit[k]);
			l111.addStrut0 ();
			column4.add (l111);

			LinePanel l112 = new LinePanel ();
			l112.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.a1") + " :", 120));
			a1[k] = new JTextField (5);
			a1[k].setText ("" + species.a1);
			a1[k].setToolTipText (Translator.swap ("FmCastaneaEditor.a1_Help"));
			l112.add (a1[k]);
			l112.addStrut0 ();
			column4.add (l112);

			LinePanel l113 = new LinePanel ();
			l113.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.a2") + " :", 120));
			a2[k] = new JTextField (5);
			a2[k].setText ("" + species.a2);
			a2[k].setToolTipText (Translator.swap ("FmCastaneaEditor.a2_Help"));
			l113.add (a2[k]);
			l113.addStrut0 ();
			column4.add (l113);

			LinePanel l114 = new LinePanel ();
			l114.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.a3") + " :", 120));
			a3[k] = new JTextField (5);
			a3[k].setText ("" + species.a3);
			a3[k].setToolTipText (Translator.swap ("FmCastaneaEditor.a3_Help"));
			l114.add (a3[k]);
			l114.addStrut0 ();
			column4.add (l114);

			LinePanel l115 = new LinePanel ();
			l115.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.C50") + " :", 120));
			C50[k] = new JTextField (5);
			C50[k].setText ("" + species.C50);
			C50[k].setToolTipText (Translator.swap ("FmCastaneaEditor.C50_Help"));
			l115.add (C50[k]);
			l115.addStrut0 ();
			column4.add (l115);

			LinePanel l116 = new LinePanel ();
			l116.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.woodStop") + " :", 120));
			woodStop[k] = new JTextField (5);
			woodStop[k].setText ("" + species.woodStop);
			woodStop[k].setToolTipText (Translator.swap ("FmCastaneaEditor.woodStop_Help"));
			l116.add (woodStop[k]);
			l116.addStrut0 ();
			column4.add (l116);

			LinePanel l117 = new LinePanel ();
			l117.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.TminEffect") + " :", 120));
			TminEffect[k] = new JTextField (5);
			TminEffect[k].setText ("" + species.TminEffect);
			TminEffect[k].setToolTipText (Translator.swap ("FmCastaneaEditor.TminEffect_Help"));
			l117.add (TminEffect[k]);
			l117.addStrut0 ();
			column4.add (l117);

			LinePanel l118 = new LinePanel ();
			l118.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.decidu") + " :", 120));
			decidu[k] = new JTextField (5);
			decidu[k].setText ("" + species.decidu);
			decidu[k].setToolTipText (Translator.swap ("FmCastaneaEditor.decidu_Help"));
			l118.add (decidu[k]);
			l118.addStrut0 ();
			column4.add (l118);

			LinePanel l119 = new LinePanel ();
			l119.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.cohortesOfLeaves") + " :", 120));
			cohortesOfLeaves[k] = new JTextField (5);
			cohortesOfLeaves[k].setText ("" + species.cohortesOfLeaves);
			cohortesOfLeaves[k].setToolTipText (Translator.swap ("FmCastaneaEditor.cohortesOfLeaves_Help"));
			l119.add (cohortesOfLeaves[k]);
			l119.addStrut0 ();
			column4.add (l119);

			/*

			LinePanel l115 = new LinePanel ();
			l115.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.Rec0") + " :", 120));
			Rec0[k] = new JTextField (5);
			Rec0[k].setText ("" + species.Rec0);
			Rec0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.Rec0_Help"));
			l115.add (Rec0[k]);
			l115.addStrut0 ();
			column4.add (l115);

			LinePanel l116 = new LinePanel ();
			l116.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.frec0") + " :", 120));
			frec0[k] = new JTextField (5);
			frec0[k].setText ("" + species.frec0);
			frec0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.frec0_Help"));
			l116.add (frec0[k]);
			l116.addStrut0 ();
			column4.add (l116);

			LinePanel l117 = new LinePanel ();
			l117.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.T1rec") + " :", 120));
			T1rec[k] = new JTextField (5);
			T1rec[k].setText ("" + species.T1rec);
			T1rec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.T1rec_Help"));
			l117.add (T1rec[k]);
			l117.addStrut0 ();
			column4.add (l117);

			LinePanel l118 = new LinePanel ();
			l118.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.T2rec") + " :", 120));
			T2rec[k] = new JTextField (5);
			T2rec[k].setText ("" + species.T2rec);
			T2rec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.T2rec_Help"));
			l118.add (T2rec[k]);
			l118.addStrut0 ();
			column4.add (l118);

			LinePanel l119 = new LinePanel ();
			l119.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.T3rec") + " :", 120));
			T3rec[k] = new JTextField (5);
			T3rec[k].setText ("" + species.T3rec);
			T3rec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.T3rec_Help"));
			l119.add (T3rec[k]);
			l119.addStrut0 ();
			column4.add (l119);

			LinePanel l120 = new LinePanel ();
			l120.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.T6rec") + " :", 120));
			T6rec[k] = new JTextField (5);
			T6rec[k].setText ("" + species.T6rec);
			T6rec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.T6rec_Help"));
			l120.add (T6rec[k]);
			l120.addStrut0 ();
			column4.add (l120);

			LinePanel l121 = new LinePanel ();
			l121.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.fd0") + " :", 120));
			fd0[k] = new JTextField (5);
			fd0[k].setText ("" + species.fd0);
			fd0[k].setToolTipText (Translator.swap ("FmCastaneaEditor.fd0_Help"));
			l121.add (fd0[k]);
			l121.addStrut0 ();
			column4.add (l121);

			LinePanel l122 = new LinePanel ();
			l122.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.ffrost") + " :", 120));
			ffrost[k] = new JTextField (5);
			ffrost[k].setText ("" + species.ffrost);
			ffrost[k].setToolTipText (Translator.swap ("FmCastaneaEditor.ffrost_Help"));
			l122.add (ffrost[k]);
			l122.addStrut0 ();
			column4.add (l122);

			LinePanel l123 = new LinePanel ();
			l123.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.fa0spg") + " :", 120));
			fa0spg[k] = new JTextField (5);
			fa0spg[k].setText ("" + species.decidu);
			fa0spg[k].setToolTipText (Translator.swap ("FmCastaneaEditor.fa0spg_Help"));
			l123.add (fa0spg[k]);
			l123.addStrut0 ();
			column4.add (l123);

			LinePanel l124 = new LinePanel ();
			l124.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.fa0aut") + " :", 120));
			fa0aut[k] = new JTextField (5);
			fa0aut[k].setText ("" + species.fa0aut);
			fa0aut[k].setToolTipText (Translator.swap ("FmCastaneaEditor.fa0aut_Help"));
			l124.add (fa0aut[k]);
			l124.addStrut0 ();
			column4.add (l124);

			LinePanel l125 = new LinePanel ();
			l125.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.powRec") + " :", 120));
			powRec[k] = new JTextField (5);
			powRec[k].setText ("" + species.powRec);
			powRec[k].setToolTipText (Translator.swap ("FmCastaneaEditor.powRecHelp"));
			l125.add (powRec[k]);
			l125.addStrut0 ();
			column4.add (l125);

			*/
			column4.addGlue ();
			ColumnPanel column5 = new ColumnPanel ();

			LinePanel l135 = new LinePanel ();
			l135.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.reservesToReproduce") + " :", 120));
			reservesToReproduce[k] = new JTextField (5);
			reservesToReproduce[k].setText ("" + species.reservesToReproduce);
			reservesToReproduce[k].setToolTipText (Translator.swap ("FmCastaneaEditor.reservesToReproduceHelp"));
			l135.add (reservesToReproduce[k]);
			l135.addStrut0 ();
			column5.add (l135);

			LinePanel l136 = new LinePanel ();
			l136.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.reservesToMortality") + " :", 120));
			reservesToMortality[k] = new JTextField (5);
			reservesToMortality[k].setText ("" + species.reservesToMortality);
			reservesToMortality[k].setToolTipText (Translator.swap ("FmCastaneaEditor.reservesToMortalityHelp"));
			l136.add (reservesToMortality[k]);
			l136.addStrut0 ();
			column5.add (l136);

			LinePanel l137 = new LinePanel ();
			l137.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.BSSminCrit") + " :", 120));
			BSSminCrit[k] = new JTextField (5);
			BSSminCrit[k].setText ("" + species.BSSminCrit);
			BSSminCrit[k].setToolTipText (Translator.swap ("FmCastaneaEditor.BSSminCritcHelp"));
			l137.add (BSSminCrit[k]);
			l137.addStrut0 ();
			column5.add (l137);

			LinePanel l138 = new LinePanel ();
			l138.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.costOfOneSeed") + " :", 120));
			costOfOneSeed[k] = new JTextField (5);
			costOfOneSeed[k].setText ("" + species.costOfOneSeed);
			costOfOneSeed[k].setToolTipText (Translator.swap ("FmCastaneaEditor.costOfOneSeedHelp"));
			l138.add (costOfOneSeed[k]);
			l138.addStrut0 ();
			column5.add (l138);

			LinePanel l139 = new LinePanel ();
			l139.add (new JWidthLabel (Translator.swap ("FmCastaneaEditor.rateOfSeedProduction") + " :", 120));
			rateOfSeedProduction[k] = new JTextField (5);
			rateOfSeedProduction[k].setText ("" + species.rateOfSeedProduction);
			rateOfSeedProduction[k].setToolTipText (Translator.swap ("FmCastaneaEditor.rateOfSeedProductionHelp"));
			l139.add (rateOfSeedProduction[k]);
			l139.addStrut0 ();
			column5.add (l139);


			column5.addGlue ();

			LinePanel aux = new LinePanel ();
			aux.add (new NorthPanel (column1));
			aux.add (new NorthPanel (column2));
			aux.add (new NorthPanel (column3));
			aux.add (new NorthPanel (column4));
			aux.add (new NorthPanel (column5));

			aux.addStrut0 ();

			tabs.addTab (Translator.swap ("FmCastaneaEditor.species") + " " + castaneaCode, new NorthPanel (aux));
			k++;
		}

		// Control panel
		LinePanel controlPanel = new LinePanel ();
		save = new JButton (Translator.swap ("FmCastaneaEditor.save"));
		saveAs = new JButton (Translator.swap ("FmCastaneaEditor.saveAs"));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.addGlue ();
		controlPanel.add (save);
		controlPanel.add (saveAs);
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		controlPanel.addStrut0 ();
		save.addActionListener (this);
		saveAs.addActionListener (this);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (tabs, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FmCastaneaEditor"));

		setModal (true);
	}

}
