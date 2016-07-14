package capsis.lib.crobas;

import java.util.Iterator;


/**	Crobas Growth Engine, based on A. Makela 1997 Forest Science, A. Makela 1999 Functional Ecology.
*	Relies heavily on Fortran code given by A. Makela, May 2006
*	@author R. Schneider - 22.5.2008
*/
public class GrowthEngine {
	
	// Initialize variables used for the growth
	private int age;
	private double[] W;
	private double[] h;
	private double[] A;
	private double BA;
	private double N;
	private double rhos;
	private double rhot;
	private double rhob;
	private double zeta;
	private double ksi;
	private double z;
	private double etas;
	private double etab;
	private double etat;
	private double dsd;
	private double dbd;
	private double db0;
	private double ds0;
	private double selfPrun;
	private double alphas0;
	private double psic0;
	private double psic1;
	private double phic;
	private double psis0;
	private double psis1;
	private double phis;
	private double Htot;
	private double dhp;
	private double GRO;
	private double alfar;
	private double phib;
	private double phib0;
	private double psib0;
	private double psib;
	private double psib1;
	private double phit;
	private double phit1;
	private double psit;
	private double psit1;
	private double phib1;
	private double Cr;
	private double Cs;
	private double Cbr;
	private double Ctr;
	private double CH;
	private double rf0;
	private double sf;
	private double sr;
	private double dt0;
	private double dt1;
	private double gammab;
	private double gammat;

	private double denom0;
	private double denom1;
	private double denom;
	private double Ss0;
	private double Sb0;
	private double St0;
	private double sums;
	private double sumb;
	private double sumt;
	private double Fnum;

	private double Gf0;
	private double Gr0;
	private double Gs0;
	private double Gb0;
	private double Gt0;

	private double rf;
	private double us;
	private double usmax;
	private double ucmax;
	private double ds;
	private double db;
	private double dt;

	private double[] dW;
	private double[] dh;
	private double[] dA;

	
	public void GrowthEngine () {}

	/**	Function to calculate tree growth from biomasses with acclimation interaction
	*/
	public void treeGrowth (CTree tree, int numberOfStepsInYear) {

		CSpecies sp = tree.getCSpecies ();

		W = new double[5];
		h = new double[5];
		A = new double[3];
		
//		System.out.println ("GrowthEngine - début;");
//		System.out.println ("Tree id"+tree.getId ()+" age "+tree.getAge()+" ksi "+tree.ksi+" z "+tree.z);


		// Variable definition
			age = tree.getAge ();
			W[0] = tree.Wf;
			W[1] = tree.Wr;
			W[2] = tree.Ws;
			W[3] = tree.Wb;
			W[4] = tree.Wt;
			h[0] = tree.Hc;
			h[1] = tree.Hs;			// tree.getHeight () - tree.Hc;
			h[2] = tree.Hb;
			h[3] = tree.Htrans;
			h[4] = tree.getHeight ();
			A[0] = tree.As;
			A[1] = tree.Ab;
			A[2] = tree.At;
			BA = tree.StemBasalArea;
			N = tree.getNumber ();
			rhos = sp.rhos;
//			rhos = tree.raus;
			rhot = sp.rhot;
			rhob = sp.rhob;
			z = tree.z;
			etat = 1/sp.alphat;
			phit1 = sp.phit;
			psit1 = sp.psit;
			phib1 = sp.phib;
			selfPrun = tree.selfPrun;
			Htot = tree.getHeight ();
			dhp = tree.getDbh ();
			sf = sp.sf;
			sr = sp.sr;
			dt0 = sp.dt0;
			dt1 = sp.dt1;
			ksi = tree.ksi;
			gammab = tree.gammab;
			gammat = sp.gammat;

			// Parameters calculated through Whorl-tree interaction
			etab = 1/tree.alphab;
			etas = 1/tree.alphas;
			alphas0 = tree.alphas;
			dsd = tree.dsd;
			dbd = tree.dbd;
			psic0 = tree.psic;
			psic1 = tree.psic1;
			psis0 = tree.psis;
			psis1 = tree.psis1;
			psib0 = tree.psib;
			psib1 = tree.psib1;
			phic = tree.phic;
			phis = tree.phis;
			ds0 = tree.ds0;
			db0 = tree.db0;
			
			// Net growth
			GRO = tree.G;

			// Root turnover rate
			// ratio of fine roots to foliage
			alfar = sp.alphar;

			// define form factors
			if (h[0] > 0.) {
				phib = h[2] * phib1 / h[0];
				psib = h[2] * psib1 / h[0];

			}
			phit = sp.gammat * phit1;
			psit = sp.gammat * psit1;

			// set control functions Ci (see Makela 1999 Functional Ecology)
			if (W[1] > 0) {
				Cr = Math.pow (alfar * W[0] / W[1], sp.taur);
			}

			if ((A[0] > 0) && (etas != 0)) {
				Cs = Math.pow (W[0] / etas / A[0], sp.tauw);
			}

			if ((A[1] > 0) && (etab !=0)) {
				Cbr = Math.pow (W[0] / etab / A[1], sp.tauw);
			}

			if ((A[2] > 0) && (etat != 0)) {
				Ctr = Math.pow (W[0] / etat / A[2], sp.tauw);
			}

			if (h[0] > 0) {
				CH = Math.pow (Math.pow(W[0] / ksi, 0.5/z) / h[0], sp.tauh);
			}

			// calculate foliage biomass growth if no foliage shedding (rf0)
			rf0 = -0.01;
			calculateRF0 (tree);

			if ( (rf0 < 0) && (CH > 0) ) {
				CH = 0;
				calculateRF0 (tree);
			}
			if ( (rf0 < -dsd) && (Cs > 0) ) {
				Cs = 0;
				calculateRF0 (tree);
			}
			if ( (rf0 < -dbd) && (Cbr > 0) ) {
				Cbr = 0;
				calculateRF0 (tree);
			}
			if ( (rf0 < -dt1) && (Ctr > 0) ) {
				Ctr = 0;
				calculateRF0 (tree);
			}
			if ( (rf0 < -sr) && (Cr > 0) ) {
				Cr = 0;
				calculateRF0 (tree);
			}


			// check for non-negative growth and calculate specific growth rate of foliage with shedding from crown rise
			if (rf0 <= 0) {
				rf = rf0;
			} else {
				rf = selfPrun * rf0;
			}

			// crown rise (us) when rf < rf0 and us > 0

			us = 0;
			
			double Ss00 = rhos*psis0*h[1]*A[0] + rhos*psic0*h[0]*A[0];			// disused pipe biomass in stem
			double Sb00 = rhob*psib0*h[2]*A[1];									// disused pipe biomass in branches
			double St00 = rhot*psit1*h[3]*A[2];									// disused pipe biomass in transport roots
			double sums0 = (Cs-1.)*W[2] + Ss00;									// adjust for system out of equilibrium
			double sumb0 = (Cbr-1.)*W[3] + Sb00;								// adjust for system out of equilibrium
			double sumt0 = (Ctr-1.)*W[4] + St00;								// adjust for system out of equilibrium
			double gammau1 = ds0*sums0 + db0*sumb0 + dt0*sumt0;					// disused pipe biomass from turnover rates of each compartment
			double gammau = 0;
			
			if (rf < rf0) {
				
				if (sp.phic > 0) {
					gammau = gammau1 + denom1 + (phis/phic-1)*rhos*phic*A[0]*h[0] - W[3];
				}
				if (gammau > 0) {
					us = h[0] * (Fnum - rf*denom)/gammau;						// crown rise
				}
				if (gammau > 0) {
					usmax = h[0]*Fnum/gammau;									// maximum crown rise, i.e all of previous foliage biomass is shed
				}
				if (us > usmax) {
					us = usmax;
				}
				if (us < 0) {
					us = 0;
				}
			}

			if (z > 0) {
				ucmax = rf0*(0.5/z)*h[0];										// crown length increment
			}

			// Disused pipe formation due to crown rise
			if (h[0] > 0) {
				if (psic1 > 0) {
					ds = ds0*us/h[0] + dsd;
					db = db0*us/h[0] + dbd;
				} else {
					ds = ds0*us/h[0];
					db = db0*us/h[0];
				}
				dt = dt0*us/h[0] + sp.dt1;
			}

			// Increments of state variables
			dW = new double[6];
			dh = new double[5];
			dA = new double[3];

			dW[0] = rf * W[0];
			dW[1] = Cr*(rf+sr)*W[1] - sr*W[1];

			if ( (h[0] > 0) && (z > 0) ) {
				dh[0] = CH*(rf/(2*z) + us/h[0])*h[0] - us;
			}
			if (dh[0] > ucmax) {
				dh[0] = ucmax;
			}
			if (dh[0] < (-us)) {
				dh[0] = -us;
			}
			dh[1] = us;
			dh[2] = gammab * dh[0];
			dh[3] = gammat * (dh[0] + dh[1]);
			dh[4] = dh[0] + dh[1];
			dA[0] = Cs*(rf+ds)*A[0] - ds*A[0];
			dA[1] = Cbr*(rf+db)*A[1] - db*A[1];
			dA[2] = Ctr*(rf+dt)*A[2] - dt*A[2];

			double uc = dh[0];
			dW[2] = rhos * (dA[0]*(phic*h[0] + phis*h[1]) + A[0]*(phic*uc + phis*us));
			dW[3] = rhob*phib*(dA[1]*h[0] + A[1]*uc);
			dW[4] = rhot*phit*((us+uc)*A[2] + (h[0] + h[1])*dA[2]);

			double W61 = 0;
			if (h[0] > 0) {
				W61 = (dsd*psis1+ds0*us*psis0/h[0]) * h[1];
			}

			double W60 = 0;
			if (h[0] > 0) {
				W60 = (dsd*psic1+ds0*us*psic0/h[0]) * h[0];
			}

			dW[5] = rhos*A[0]*(W60 + W61);

			// heartwood formation in branches and transport roots
			double Sb = 0;
			if (h[0] > 0) {
				Sb = rhob * A[1]*(psib0*db0*us/h[0] + psib1*dbd) * h[2];
			}
			double St = rhot * A[2] * psit * dt * (h[0] + h[1]);
			double Ss = rhos * A[0] * (W60 + W61);

			// allocation parameters
			double[] fl = new double[5];
			double test = dW[0]+dW[1]+dW[2]+dW[3]+dW[4]+Sb+St+Ss+sf*W[0]+sr*W[1];
			if (test > 0.) {
				fl[0] = (dW[0] + sf*W[0])/test;
				fl[1] = (dW[1] + sr*W[1])/test;
				fl[2] = (dW[2] + Ss)/test;
				fl[3] = (dW[3] + Sb)/test;
				fl[4] = (dW[4] + St)/test;
			}

			// check if breast height age a13 is large enough to prompt senescence due to ageing
			double ds13 = 0;
			if ((age > 10) && (h[0] > 0)) {
				ds13 = ds0*us/h[0] + dsd;
			} else {
				if (h[0] > 0) {
					ds13 = ds0*us/h[0];
				}
			}

			double dBApot = Math.max (0., Cs*(rf+ds13)*A[0]);
			double dBA = 0;
			if (h[1] >= 1.3) {
				dBA =  dBApot;
			} else {
				if ((h[4] > 1.3) && ((h[4]-h[1]) > 0)) {
					dBA = (h[4]-1.3)/(h[4]-h[1]) * dBApot;
				}
			}
		
		// Format growth increments
		double iWf = dW[0] / numberOfStepsInYear;
		double iWr = dW[1] / numberOfStepsInYear;
		double iWs = dW[2] / numberOfStepsInYear;
		double iWds = dW[5] / numberOfStepsInYear;
		double iWb = dW[3] / numberOfStepsInYear;
		double iWt = dW[4] / numberOfStepsInYear;
		double iHc = dh[0] / numberOfStepsInYear;
		double iHs = dh[1] / numberOfStepsInYear;
		double iHb = dh[2] / numberOfStepsInYear;
		double iHtrans = dh[3] / numberOfStepsInYear;
		double iHeight = iHc + iHs;
		double iStemBasalArea = dBA / numberOfStepsInYear;
		double iVtot = ((dW[2] + dW[5]) / rhos) / numberOfStepsInYear;

		double iAs = dA[0] / numberOfStepsInYear;
		double iAb = dA[1] / numberOfStepsInYear;
		double iAt = dA[2] / numberOfStepsInYear;
		
		tree.dBApot = dBApot / numberOfStepsInYear;

		tree.growth (iWf, iWr, iWs, iWb, iWt, iHc, iHs, iHb, iHeight, iStemBasalArea, iVtot, iAs, iAb, iAt, iHtrans, iWds);

//		System.out.println ("GrowthEngine - fin;");
//		System.out.println ("Tree id"+tree.getId ()+" age "+tree.getAge()+" ksi "+tree.ksi+" z "+tree.z);

		// commented code doesn't yield same results as code above
//		tree.iWf = dW[0] / numberOfStepsInYear;
//		tree.iWr = dW[1] / numberOfStepsInYear;
//		tree.iWs = dW[2] / numberOfStepsInYear;
//		tree.iWds = dW[5] / numberOfStepsInYear;
//		tree.iWb = dW[3] / numberOfStepsInYear;
//		tree.iWt = dW[4] / numberOfStepsInYear;
//		tree.iHc = dh[0] / numberOfStepsInYear;
//		tree.iHs = dh[1] / numberOfStepsInYear;
//		tree.iHb = dh[2] / numberOfStepsInYear;
//		tree.iHtrans = dh[3] / numberOfStepsInYear;
//		tree.iHeight = tree.iHc + tree.iHs;
//		tree.iStemBasalArea = dBA / numberOfStepsInYear;
//		tree.iVtot = ((dW[2] + dW[5]) / rhos) / numberOfStepsInYear;
//
//		tree.iAs = dA[0] / numberOfStepsInYear;
//		tree.iAb = dA[1] / numberOfStepsInYear;
//		tree.iAt = dA[2] / numberOfStepsInYear;
//			
//		tree.dBApot = dBApot / numberOfStepsInYear;
//		
//		tree.growth(tree);
		
	}

	private void calculateRF0 (CTree tree) {

		denom0 = W[0] + Cr*W[1] +  Cs*W[2] + Cbr*W[3] + Ctr*W[4];						// total living biomass
		denom1 = CH * (rhos * phic * A[0] * h[0] + rhot * phit * A[2] * h[0] + W[3]);

		if (z > 0) {
			denom = denom0 + denom1 / (2*z);										
		}
		Ss0 = rhos * psis1 * h[1] * A[0] + rhos * psic1 * h[0] * A[0];				// disused pipe biomass in the stem
		Sb0 = rhob * psib1 * h[2] * A[1];											// disused pipe in the branches
		St0 = rhot * psit1 * h[3] * A[2];											// disused pipe in the transport roots
		sums = (Cs - 1.) * W[2] + Ss0;												// Adjust for system which is out of equilibrium
		sumb = (Cbr - 1.) * W[3] + Sb0;												// Adjust for system which is out of equilibrium
		sumt = (Ctr - 1.) * W[4] + St0;												// Adjust for system which is out of equilibrium
		Fnum = GRO - sf*W[0] - sr*Cr*W[1] - dsd*sums - dbd*sumb - dt1*sumt;			// amount of carbon allocated to growth of the living parts = growth - turnover biomass 

		rf0 = 0;
		if ( (W[0] > 0) && (denom > 0) ) {
			rf0  =  Fnum / denom;													// specific growth rate of foliage without shedding
		}

		Gf0 = (rf0 + sf) * W[0];													// total foliage growth
		Gr0 = Cr * (rf0 + sr) * W[1];												// total growth of fine roots

		if (z > 0) {
			Gs0 = Cs*rf0*W[2]  + dsd*sums + CH*rf0*rhos*phic*A[0]*h[0]/2/z;			// total growth of the stem
			Gb0 = Cbr*rf0*W[3] + dbd*sumb + CH*rf0*W[3]/2/z;						// total growth of the branches 
			Gt0 = Ctr*rf0*W[4] + dt1*sumt + CH*rf0*rhot*phit*A[2]*h[0]/2/z;			// total growth of the transport roots
		}
	}

}


