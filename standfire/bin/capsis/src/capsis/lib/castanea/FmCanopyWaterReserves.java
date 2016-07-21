package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import java.util.Collection;

/**
 * FmLeaf : leaves of Dynaclim model.
 *
 * @author Hendrik Davi - october 2009
 */
public class FmCanopyWaterReserves implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	private double[] rleaf;
	private double[] rbark;
	private double[] rcanop;
	private double[] rleafmax;
	private double[] rbarkmax;
	private double[] rcanmax;
	private double[] egt;
	private double[] ps;
	private double[] psd;
	private double[] ec;

	/**
	 * Constructor for new logical FmCanopyLayer.
	 */
	public FmCanopyWaterReserves (double[] rleaf, double[] rbark, double[] rcanop, double[] rleafmax,
			double[] rbarkmax, double[] rcanmax, double[] egt, double[] ps, double[] psd, double[] ec) {

		this.rleaf = rleaf;
		this.rbark = rbark;
		this.rcanop = rcanop;
		this.rleafmax = rleafmax;
		this.rbarkmax = rbarkmax;
		this.rcanmax = rcanmax;
		this.egt = egt;
		this.ps = ps;
		this.psd = psd;
		this.ec = ec;

	}

	// /////////////////
	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmCanopyWaterReserves r = (FmCanopyWaterReserves) super.clone (); // calls protected
																				// Object
																				// Object.clone ()
																				// {}

			r.rleaf = AmapTools.getCopy (rleaf);
			r.rbark = AmapTools.getCopy (rbark);
			r.rcanop = AmapTools.getCopy (rcanop);
			r.rleafmax = AmapTools.getCopy (rleafmax);
			r.rbarkmax = AmapTools.getCopy (rbarkmax);
			r.rcanmax = AmapTools.getCopy (rcanmax);
			r.egt = AmapTools.getCopy (egt);
			r.ps = AmapTools.getCopy (ps);
			r.psd = AmapTools.getCopy (psd);
			r.ec = AmapTools.getCopy (ec);

			return r;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FmCanopyWaterReserves ()", "Error while cloning", e);
			return null;
		}
	}

	// /////////////////

	public double getRleaf (int sp) {
		return rleaf[sp];
	}

	public double getRbark (int sp) {
		return rbark[sp];
	}

	public double getRcanop (int sp) {
		return rcanop[sp];
	}

	public double getRleafmax (int sp) {
		return rleafmax[sp];
	}

	public double getRbarkmax (int sp) {
		return rbarkmax[sp];
	}

	public double getRcanmax (int sp) {
		return rcanmax[sp];
	}

	public double getEgt (int sp) {
		return egt[sp];
	}

	public double getEc (int sp) {
		return ec[sp];
	}

	public double getPs (int sp) {
		return ps[sp];
	}

	public double getPsd (int sp) {
		return psd[sp];
	}

	public void setRleaf (int sp, double v) {
		rleaf[sp] = v;
	}

	public void setRbark (int sp, double v) {
		rbark[sp] = v;
	}

	public void setRcanop (int sp, double v) {
		rcanop[sp] = v;
	}

	public void setRleafmax (int sp, double v) {
		rleafmax[sp] = v;
	}

	public void setRbarkmax (int sp, double v) {
		rbarkmax[sp] = v;
	}

	public void setRcanmax (int sp, double v) {
		rcanmax[sp] = v;
	}

	public void setEgt (int sp, double v) {
		egt[sp] = v;
	}

	public void setEc (int sp, double v) {
		ec[sp] = v;
	}

	public void setPs (int sp, double v) {
		ps[sp] = v;
	}

	public void setPsd (int sp, double v) {
		psd[sp] = v;
	}

	public double getChangeRleafmax (double LAI, FmSpecies species) {
		return LAI * species.Tleaf;
	};

	// ****************************************************************************************
	// processInterception: calculate daily water interception (Dufrene et al., 2005).

	public void waterInterception (FmCell cell, FmClimateDay climateDay, FmSettings settings) {


		double PRI = climateDay.getDailyPrecipitation();

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];

		int fmSpeciesNumber = fmSpeciesList.length;
		double CIleaf = 0;
		double Ileaf = 0;
		double egtleaf = 0;
		double Iwood = 0;
		double ecSp = 0;
		double psdSp = 0;
		double egtbark = 0;

		double rleafmaxSp = 0; // re-calculated in the process to be improved
		double rcanmaxSp = 0;

		double[] L = cell.getCanopy ().getLAI ();
		double[] WAI = cell.getCanopy ().getWAI ();
		double[] agreg = cell.getCanopy ().getClumping ();

		int sp = 0;

		double rleafSp = this.rleaf[sp];
		double rbarkSp = this.rbark[sp];
		double rcanopSp = this.rcanop[sp];
		// double rbarkmaxSp= this.rbarkmax[sp];
		double rbarkmaxSp = 2 * WAI[sp] * species.Tbark;

		double Leq = L[sp] * agreg[sp]; // calculation of equivalent LAI that intercept water
										// (true LAI corrected by clumping)
		double CIwood = WAI[sp] / (WAI[sp] * species.CIA + species.CIB);

		if (species.decidu == 1) {
			CIleaf = L[sp] / (L[sp] * species.CIA + species.CIB);
			rleafmaxSp = L[sp] * species.Tleaf;
			rcanmaxSp = rbarkmaxSp + rleafmaxSp;
		} else {
			CIleaf = Leq / (Leq * species.CIA + species.CIB);
			rleafmaxSp = Leq * species.Tleaf;
			rcanmaxSp = rbarkmaxSp + rleafmaxSp;
		}

		if (L[sp] > WAI[sp]) {
			Ileaf = CIleaf * PRI;
		} else {
			Ileaf = 0;
		}

		rleafSp += Ileaf;

		if (rleafSp > rleafmaxSp) {
			egtleaf = rleafSp - rleafmaxSp;
		} else {
			egtleaf = 0;
		}

		if (L[sp] == 0) {
			egtleaf = 0;
		}

		rleafSp -= egtleaf;
		if (L[sp] > WAI[sp]) {
			Iwood = CIwood * egtleaf; // stem and branches water interception
		} else {
			Iwood = CIwood * PRI;
		}
		rbarkSp = rbarkSp + Iwood;

		if (rbarkSp > rbarkmaxSp) {
			ecSp = species.propec * (rbarkSp - rbarkmaxSp); // water reaching the soil after
															// stem throughfall
			egtbark = (1 - species.propec) * (rbarkSp - rbarkmaxSp); //
		} else {
			ecSp = 0;
			egtbark = 0;
		}

		rbarkSp = rbarkSp - ecSp - egtbark;

		double egtSp = egtleaf * (1 - CIwood) + egtbark; // water reaching the ground by drain
		double IN = (Ileaf - egtleaf) + (Iwood - ecSp - egtbark); // net interception in mm/day
		double psSp = PRI - IN; // total water reaching th soil
		psdSp = psSp - egtSp - ecSp; // water reaching directly the ground
		rcanopSp = rbarkSp + rleafSp; // canopy water reserve

		this.setRleaf (sp, rleafSp);
		this.setRbark (sp, rbarkSp);
		this.setRcanop (sp, rcanopSp);
		this.setRleafmax (sp, rleafmaxSp);
		this.setRbarkmax (sp, rleafmaxSp);
		this.setRcanmax (sp, rcanmaxSp);
		this.setEgt (sp, egtSp);
		this.setEc (sp, ecSp);
		this.setPs (sp, psSp);
		this.setPsd (sp, psdSp);

//			 Log.println(settings.logPrefix+"WaterReserve", getRleaf(sp)+";"+
//			 getRbark(sp)+";"+getEgt(sp)+";"+getPs(sp)+";"+getPsd(sp)+";"+PRI+";"+rbarkmaxSp+";"+WAI[sp]);


	}// end of processInterception

}// end of class