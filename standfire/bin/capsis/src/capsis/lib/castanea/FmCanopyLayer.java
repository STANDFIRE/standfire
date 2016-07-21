package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.Log;

/**	FmCanopyLayer : a layer of leaves of Dynaclim model.
*
*	@author Hendrik Davi - march 2006
*/
public class FmCanopyLayer implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	protected int layerRank;					// 0  is top
	protected double[] thickness;			// m

	protected double[] LMA;				// Leaf Mass per Area
	//private double[] photosynthesis;	// µmol co2 m-2 s-1
	//private double[] respiration;		// µmol co2 m-2 s-1

	// other properties here...


	/**	Constructor for new logical FmCanopyLayer.
	*/
	public FmCanopyLayer (int layerRank,
						double[] thickness) {

		this.layerRank = layerRank;
		this.thickness = thickness;
	}

	/**	Clone method.
	*/
	public Object clone () {
		try {
			FmCanopyLayer l = (FmCanopyLayer) super.clone ();	// calls protected Object Object.clone () {}

			l.LMA = null;

			return l;
		} catch (CloneNotSupportedException e) {
			Log.println (Log.ERROR, "FmCanopyLayer.clone ()",
					"Error while cloning "
					+" "+e.toString (), e);
			return null;
		}
	}

	// creation of accessors

	public int getLayerRank() {return layerRank;}					// 0  is top
	public double[] getThickness() {return thickness;}


//*******************************************************************************************************
	// processLMALAI: Calculate LMALAI of leaves and Leaf surface

	public double getLMAdecidous (FmSettings s, FmCell cell, FmSpecies species, double lai, int sp) {
		double LMALAI= cell.getLMAcell()[sp] *Math.exp(-species.KLMA *(lai));
		return LMALAI;
	}

	public double getLMAevergreen (FmCell cell, FmSettings settings, FmSpecies species, double lai, int cohortesOfLeaves, double strat, int sp) {
		 double sumMass= 0;
		 FmCanopyEvergreen canopyEvergreen = cell.getCanopy ().getCanopyEvergreen ();
		 //double Lnew= canopyEvergreen.getLy(0);
		 double Lnew= 0;

		 double [] LMA0evergreen= canopyEvergreen.getLMA0evergreen(cell, settings,cohortesOfLeaves,sp); 
		 double LMA0average=0;
		 
		 for (int k = 0; k < cohortesOfLeaves; k++) {
			Lnew= Lnew+ canopyEvergreen.getLy(k);
			// average LMA0 => hypothezize that all leaf age are equally distributed in canopy
			// to be improved
			LMA0average= LMA0average+LMA0evergreen[k]*canopyEvergreen.getLy(k);			

		  }
		 double LMA0= LMA0average/Lnew;	
		 double LMALAI=LMA0*Math.exp(-species.KLMA *(lai));

			//double LMALAI= 
		//System.out.println ("LMALAI"+ LMALAI+"  "+LMA0+" "+Lnew+"  "+LMA0evergreen[0]+" "+LMA0evergreen[1]+" "+LMA0evergreen[2]);
		return LMALAI;
	}
	
	public double getNitrogenAvEvergreen(FmCell cell, FmSettings settings, FmSpecies species, double lai, int cohortesOfLeaves, double strat) {
		FmCanopyEvergreen canopyEvergreen = cell.getCanopy ().getCanopyEvergreen ();
		double Lnew= 0;

		double [] nitrogenEvergreen= canopyEvergreen.getNitrogenEvergreen(cell, settings, species,cohortesOfLeaves); 
		double nitrogenAverage=0;
		for (int k = 0; k < cohortesOfLeaves; k++) {
			Lnew= Lnew+ canopyEvergreen.getLy(k);
			// average LMA0 => hypothezize that all leaf age are equally distributed in canopy
			// to be improved
			nitrogenAverage= nitrogenAverage+nitrogenEvergreen[k]*canopyEvergreen.getLy(k);			

		  }
		nitrogenAverage=nitrogenAverage/Lnew;
		return nitrogenAverage;
	}

	public double getRb(FmSpecies sp, double lai) {

		double rb= 25; // to be improved change with leaf size
		return rb;
	}

	public double getG1(FmSpecies species, double lai, FmCell cell, int sp) {
		//double getR

		double g1max=  cell.getG1cell()[sp];
		double g1min= 1;
		FmSoil soil= cell.getSoil();
		double g1= (g1max - g1min) * soil.getStomatalControl() + g1min;
		//System.out.println ("g1"+ g1);

		return g1;  // to be improved change with time
	}





// accesseurs
//	public double getLMAc () {return LMA;}
	//~ public void setPhotosynthesis (double[] n) {photosynthesis = n;}


}
