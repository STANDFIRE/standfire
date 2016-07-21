package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;

import java.util.Collection;

/**
 * FmCanopyEvergreen
 *
 * @author Hendrik Davi - october 2012
 */
public class FmCanopyEvergreen implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	private double[] leafArea;  // leaf area number of cohorts 0 represents the current year
	private double[] LMAy;  // number of cohorts 0 represents the current year

	private double[] coefOfLeafMass; // number of cohorts 0 represents the current year
	private double[] coefOfLeafNitrogen; // number of cohorts 0 represents the current year
	private double[] coefOfPhotosynthesis; // number of cohorts 0 represents the current year

	private double[] coefOfLeafMassOld; // number of cohorts 0 represents the current year
	private double[] coefOfLeafNitrogenOld; // number of cohorts 0 represents the current year
	private double[] coefOfPhotosynthesisOld; // number of cohorts 0 represents the current year


	private double [] LMA0evergreen;
	private double [] nitrogenEvergreen;
	private double [] photoEvergreen;

	private double[] coefOfLeafFall; // number of cohorts 0 represents the current year
	private double [] ratioPerCohorts;

		// variables for evergreen species

	private double [] ly;  // number of cohorts
	private double [] lhivy;  // number of cohorts
	private double [] lmy;  //number of cohorts

	private double lmaxYear;

	private FmSettings settings; // fc-11.6.2014 for logPrefix

	/**
	 * Constructor for new logical FmCanopyEvergreen.
	 */
	public FmCanopyEvergreen (int cohortesOfLeaves, FmSettings settings) {

		this.settings = settings; // fc-11.6.2014 for logPrefix

		this.leafArea =  new double[cohortesOfLeaves];
		this.LMAy =  new double[cohortesOfLeaves];
		this.coefOfLeafMass =  new double[cohortesOfLeaves];
		this.coefOfLeafNitrogen =  new double[cohortesOfLeaves];
		this.coefOfPhotosynthesis =  new double[cohortesOfLeaves];
		this.coefOfLeafMassOld =  new double[cohortesOfLeaves];
		this.coefOfLeafNitrogenOld =  new double[cohortesOfLeaves];
		this.coefOfPhotosynthesisOld =  new double[cohortesOfLeaves];
		this.coefOfLeafFall =  new double[cohortesOfLeaves];
		this.ly =  new double[cohortesOfLeaves];
		this.lhivy =  new double[cohortesOfLeaves];
		this.lmy =  new double[cohortesOfLeaves];
		this.LMA0evergreen=  new double[cohortesOfLeaves];
		this.nitrogenEvergreen=  new double[cohortesOfLeaves];
		this.photoEvergreen=  new double[cohortesOfLeaves];
		this.ratioPerCohorts = new double [cohortesOfLeaves];



	}

	// /////////////////
	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmCanopyEvergreen r = (FmCanopyEvergreen) super.clone (); // calls protected
																				// Object
																				// Object.clone ()
																				// {}
			r.leafArea = AmapTools.getCopy (leafArea);
			r.LMAy = AmapTools.getCopy (LMAy);
			r.coefOfLeafMass = AmapTools.getCopy (coefOfLeafMass);
			r.coefOfLeafNitrogen = AmapTools.getCopy (coefOfLeafNitrogen);
			r.coefOfPhotosynthesis = AmapTools.getCopy (coefOfPhotosynthesis);
			r.coefOfLeafMassOld = AmapTools.getCopy (coefOfLeafMassOld);
			r.coefOfLeafNitrogenOld = AmapTools.getCopy (coefOfLeafNitrogenOld);
			r.coefOfPhotosynthesisOld = AmapTools.getCopy (coefOfPhotosynthesisOld);

			r.coefOfLeafFall = AmapTools.getCopy (coefOfLeafFall);
			r.ly = AmapTools.getCopy (ly);
			r.lhivy = AmapTools.getCopy (lhivy);
			r.lmy = AmapTools.getCopy (lmy);

			//r.lmaxYear= AmapTools.getCopy (lmaxYear);

			return r;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FmCanopyEvergreen ()", "Error while cloning", e);
			return null;
		}
	}

	/*******************************************************************************************/

	public double getLeafArea (int year) {
		return leafArea[year];
	}

	public void setLeafArea (int year, double v) {
		this.leafArea[year] = v;
	}


	public double getLMAy (int year) {
		return LMAy[year];
	}

	public void setLMAy (int year, double v) {
		this.LMAy[year] = v;
	}

	public double getLmaxYear() {
		return lmaxYear;
	}

	public void setLmaxYear (double v) {
		this.lmaxYear = v;
	}


	public double getCoefOfLeafMass(int year) {
		return coefOfLeafMass[year];
	}

	public double getCoefOfLeafMassOld(int year) {
		return coefOfLeafMassOld[year];
	}

	public void setCoefOfLeafMass (int year, double v) {
		this.coefOfLeafMass[year] = v;
	}

	public double getCoefOfLeafNitrogen(int year) {
		return coefOfLeafNitrogen[year];
	}

	public void setCoefOfLeafNitrogen(int year, double v) {
		this.coefOfLeafNitrogen[year] = v;
	}

	public double getCoefOfLeafNitrogenOld(int year) {
		return coefOfLeafNitrogenOld[year];
	}


	public double getCoefOfPhotosynthesis(int year) {
		return coefOfPhotosynthesis[year];
	}

	public double getCoefOfPhotosynthesisOld(int year) {
		return coefOfPhotosynthesisOld[year];
	}

	public void setCoefOfPhotosynthesis(int year, double v) {
		this.coefOfPhotosynthesis[year] = v;
	}

	public double getCoefOfLeafFall(int year) {
		return coefOfLeafFall[year];
	}

	public void setCoefOfLeafFall(int year, double v) {
		this.coefOfLeafFall[year] = v;
	}

	public double getLy(int year) {
		return ly[year];
	}

	public void setLy(int year, double v) {
		this.ly[year] = v;
	}

	public double getLhivy(int year) {
		return lhivy[year];
	}

	public void setLhivy(int year, double v) {
			this.lhivy[year] = v;
	}

	public double getLmy(int year) {
		return lmy[year];
	}

	public void setLmy(int year, double v) {
			this.lmy[year] = v;
	}



	public void setLMA0evergreen(int year, double v) {
			this.LMA0evergreen[year] = v;
	}


	public void setNitrogenEvergreen(int year, double v) {
			this.nitrogenEvergreen[year] = v;
	}

	public void setPhotoEvergreen(int year, double v) {
			this.photoEvergreen[year] = v;
	}


	public double getRatioPerCohorts(int year) {
		return ratioPerCohorts[year];
	}

	public void setRatioPerCohorts(int year, double v) {
		this.ratioPerCohorts[year] = v;
	}

/*******************************************************************************************/

	public void initCanopyEvergreen (FmSpecies species, int cohortesOfLeaves, FmCell cell, int sp){

		FmCanopy canopy= cell.getCanopy();
		double sumCoef=0;


		if (species.castaneaCode==7) {  //abies alba

			this.leafArea[0] = 1;
			this.leafArea[1] = 1;
			this.leafArea[2] = 1;
			this.leafArea[3] = 1;
			this.leafArea[4] = 1;
			this.leafArea[5] = 1;
			this.leafArea[6] = 1;
			this.leafArea[7] = 1;
			this.leafArea[8] = 1;
			this.leafArea[9] = 1;
			this.leafArea[10] = 1;

			this.coefOfLeafMass[0] = 1;
			this.coefOfLeafMass[1] = 1.20;
			this.coefOfLeafMass[2] = 1.35;
			this.coefOfLeafMass[3] = 1.45;
			this.coefOfLeafMass[4] = 1.51;
			this.coefOfLeafMass[5] = 1.56;
			this.coefOfLeafMass[6] = 1.59;
			this.coefOfLeafMass[7] = 1.62;
			this.coefOfLeafMass[8] = 1.63;
			this.coefOfLeafMass[9] = 1.64;
			this.coefOfLeafMass[10] =1.65;

			this.coefOfLeafNitrogen[0] = 1 ;
			this.coefOfLeafNitrogen[1] = 1 ;
			this.coefOfLeafNitrogen[2] = 0.969;
			this.coefOfLeafNitrogen[3] = 0.961;
			this.coefOfLeafNitrogen[4] = 0.961;
			this.coefOfLeafNitrogen[5] = 0.954;
			this.coefOfLeafNitrogen[6] = 0.926;
			this.coefOfLeafNitrogen[7] = 0.861;
			this.coefOfLeafNitrogen[8] = 0.746;
			this.coefOfLeafNitrogen[9] = 0.564;
			this.coefOfLeafNitrogen[10] =0.301;

			this.coefOfLeafFall[0] = 0;
			this.coefOfLeafFall[1] = 0;
			this.coefOfLeafFall[2] = 0;
			this.coefOfLeafFall[3] = 0;
			this.coefOfLeafFall[4] = 0;
			this.coefOfLeafFall[5] = 0.05;
			this.coefOfLeafFall[6] = 0.1;
			this.coefOfLeafFall[7] = 0.15;
			this.coefOfLeafFall[8] = 0.20;
			this.coefOfLeafFall[9] = 0.25;
			this.coefOfLeafFall[10] = 0.25;

			this.coefOfPhotosynthesis[0] = 1;
			this.coefOfPhotosynthesis[1] = 0.78;
			this.coefOfPhotosynthesis[2] = 0.57;
			this.coefOfPhotosynthesis[3] = 0.51;
			this.coefOfPhotosynthesis[4] = 0.45;
			this.coefOfPhotosynthesis[5] = 0.41;
			this.coefOfPhotosynthesis[6] = 0.38;
			this.coefOfPhotosynthesis[7] = 0.38;
			this.coefOfPhotosynthesis[8] = 0.38;
			this.coefOfPhotosynthesis[9] = 0.38;
			this.coefOfPhotosynthesis[10] = 0.38;


	}


	if (species.castaneaCode==1) {  //quercus ilex
// litlle variation for leaf area and leaf mass found in Gratalli & Bombelli 2000 entre 211 gDM/mï¿½ et 199
	    this.leafArea[0] = 1;
	    this.leafArea[1] = 1;
	    this.leafArea[2] = 1;


	    this.coefOfLeafMass[0] = 1;
	    this.coefOfLeafMass[1] = 1;
	    this.coefOfLeafMass[2] = 1;


	    this.coefOfLeafNitrogen[0] = 1 ;
	    this.coefOfLeafNitrogen[1] = 1 ;
	    this.coefOfLeafNitrogen[2] = 1;


	    this.coefOfLeafFall[0] = 0;
	    this.coefOfLeafFall[1] = 0;
	    this.coefOfLeafFall[2] = 1;

	    this.coefOfPhotosynthesis[0] = 1;
	    this.coefOfPhotosynthesis[1] = 1;
	    this.coefOfPhotosynthesis[2] = 0.8;
	}

	if (species.castaneaCode==6) {  //pinus pinaster
	        this.leafArea[0] = 1;
	        this.leafArea[1] = 1;
	        this.leafArea[2] = 1;
        
        
	        this.coefOfLeafMass[0] = 1;
	        this.coefOfLeafMass[1] = 1;
	        this.coefOfLeafMass[2] = 1;
        
        
	        this.coefOfLeafNitrogen[0] = 1 ; // few variation see Porte & Lousteau 1998
	        this.coefOfLeafNitrogen[1] = 1 ;
	        this.coefOfLeafNitrogen[2] = 1;
        
        
	        this.coefOfLeafFall[0] = 0;
	        this.coefOfLeafFall[1] = 0;
	        this.coefOfLeafFall[2] = 1;
        
	        this.coefOfPhotosynthesis[0] = 1; // Porte & Lousteau 1998, OgŽe et al., 2003
	        this.coefOfPhotosynthesis[1] = 1; //0.66
	        this.coefOfPhotosynthesis[2] = 0.7; //0.42
	}

	if (species.castaneaCode==10) {  //pinus nigra
	        this.leafArea[0] = 1;
	        this.leafArea[1] = 1;
	        this.leafArea[2] = 1;
        
        
	        this.coefOfLeafMass[0] = 1;
	        this.coefOfLeafMass[1] = 1;
	        this.coefOfLeafMass[2] = 1;
        
        
	        this.coefOfLeafNitrogen[0] = 1 ; 
	        this.coefOfLeafNitrogen[1] = 1 ;
	        this.coefOfLeafNitrogen[2] = 1;
        
        
	        this.coefOfLeafFall[0] = 0;
	        this.coefOfLeafFall[1] = 0;
	        this.coefOfLeafFall[2] = 1;
        
	        this.coefOfPhotosynthesis[0] = 1; // Freeland, R.O., 1952. EFFECT OF AGE OF LEAVES UPON THE RATE OF PHOTOSYNTHESIS IN SOME CONIFERS. Plant Physiol 27, 685–690.
	        this.coefOfPhotosynthesis[1] = 0.66; //0.66
	        this.coefOfPhotosynthesis[2] = 0.42; //0.42
        }




	for (int k = 0; k < cohortesOfLeaves; k++) {
		ratioPerCohorts[k]=1;
		for (int j = k; j> 0; j--) {
		        		ratioPerCohorts[k]=ratioPerCohorts[k]*(1-coefOfLeafFall[j]);
		}
		sumCoef=sumCoef+ratioPerCohorts[k];
		//Log.println(settings.logPrefix+"sumCoef", k+";"+";"+ratioPerCohorts[k]+";"+sumCoef);

	}



	for (int k = 0; k < cohortesOfLeaves; k++) {
		ratioPerCohorts[k]=ratioPerCohorts[k]/sumCoef;
		this.coefOfPhotosynthesisOld[k]= this.coefOfPhotosynthesis[k];
		this.coefOfLeafNitrogenOld[k]= this.coefOfLeafNitrogen[k];
		this.coefOfLeafMassOld[k]= this.coefOfLeafMass[k];
		//this.lhivy[k]= canopy.getLAImax ()[sp]/(cohortesOfLeaves);
		this.lhivy[k]= canopy.getLAImax ()[sp]*ratioPerCohorts[k];
		//Log.println(settings.logPrefix+"getLAImax", k+";"+";"+ratioPerCohorts[k]+";"+canopy.getLAImax ()[sp]+";"+this.lhivy[k]);
		this.ly[k]=lhivy[k];

	}


}
	public double [] getLMA0evergreen (FmCell cell, FmSettings settings, int cohortesOfLeaves, int sp) {
		//double [] LMA0evergreen= new double[cohortesOfLeaves];
		double LMA0newLeaves= cell.getLMAcell()[sp];
		int numberOfDays = cell.getNumberOfDays (cell.currentYear);

		double Lnew=0;

		for (int k = 0; k < cohortesOfLeaves-1; k++) {
			Lnew= Lnew+ this.getLy(k);
			double LMAk1= LMA0newLeaves*this.coefOfLeafMass[k];
			double LMAk2= LMA0newLeaves*this.coefOfLeafMass[k+1];

			LMA0evergreen[k]= LMAk1+cell.currentDay*(LMAk2-LMAk1)/numberOfDays;
		}
		// no change for the last cohortes of leaves
		LMA0evergreen[cohortesOfLeaves-1]= LMA0newLeaves*this.coefOfLeafMass[cohortesOfLeaves-1];

		return LMA0evergreen;

	}

	public double [] getNitrogenEvergreen(FmCell cell, FmSettings settings, FmSpecies species, int cohortesOfLeaves) {
		//double [] nitrogenEvergreen= new double[cohortesOfLeaves];
		int numberOfDays = cell.getNumberOfDays (cell.currentYear);

		double Lnew=0;

		for (int k = 0; k < cohortesOfLeaves-1; k++) {
			Lnew= Lnew+ this.getLy(k);
			double Nk1= species.leafNitrogen*this.coefOfLeafNitrogen[k];
			double Nk2= species.leafNitrogen*this.coefOfLeafNitrogen[k+1];

			this.nitrogenEvergreen[k]= Nk1+cell.currentDay*(Nk2-Nk1)/numberOfDays;
		}
		// no change for the last cohortes of leaves
		this.nitrogenEvergreen[cohortesOfLeaves-1]= species.leafNitrogen*this.coefOfLeafNitrogen[cohortesOfLeaves-1];
		return nitrogenEvergreen;

	}

	// coeficient of correction accounting for a decrease of photosynthetic capacities withe leaf ageing
	public double [] getPhotoEvergreen(FmCell cell, FmSettings settings, FmSpecies species, int cohortesOfLeaves) {
		//double [] photoEvergreen= new double[cohortesOfLeaves];
		int numberOfDays = cell.getNumberOfDays (cell.currentYear);

		//double Lnew=0;

		for (int k = 0; k < cohortesOfLeaves-1; k++) {
			//Lnew= Lnew+ this.getLy(k);
			double Pk1= this.coefOfPhotosynthesis[k];
			double Pk2= this.coefOfPhotosynthesis[k+1];

			this.photoEvergreen[k]= Pk1+cell.currentDay*(Pk2-Pk1)/numberOfDays;
		//	Log.println(settings.logPrefix+"understanding2", k+";"+";"+photoEvergreen[k]+";"+Pk1+";"+Pk2);

		}
		// no change for the last cohortes of leaves
		photoEvergreen[cohortesOfLeaves-1]= this.coefOfPhotosynthesis[cohortesOfLeaves-1];

		return photoEvergreen;

	}

}