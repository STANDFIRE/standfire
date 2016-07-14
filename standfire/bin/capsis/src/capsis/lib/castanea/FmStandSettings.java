package capsis.lib.castanea;

import java.util.Collection;
import java.util.Iterator;

import capsis.kernel.AbstractSettings;

/**	FmStandSettings - List of stand level settings.
*
*
*	@author Hendrik Davi - april 2009
*/
public class FmStandSettings extends AbstractSettings {

	public double[] treeNumber;
	public double[] meanDbh;
	public double[] meanAge;
	public double[] meanHeight;
	public double[] meanHcb;
	public double[] G;

	public double AsolPAR;
	public double AsolPIR;


 public FmStandSettings (Collection<FmSpecies> fmSpeciesList) {
		int nCastaneaSpecies=fmSpeciesList.size();
		treeNumber = new double[nCastaneaSpecies];
		meanDbh = new double[nCastaneaSpecies];
		meanAge = new double[nCastaneaSpecies];
		meanHeight = new double[nCastaneaSpecies];
		meanHcb = new double[nCastaneaSpecies];
		G= new double[nCastaneaSpecies];
		double AsolPAR;
		double AsolPIR;

		// g m-2

	    //Asoil;
		//Alit;


		//~ G = new double[nCastaneaSpecies];

		//~ soilHeights1 = new double[nCastaneaSpecies];
		//~ soilHeights2 = new double[nCastaneaSpecies];
		//~ soilHeights3 = new double[nCastaneaSpecies];
	}


	double [] getTreeNumber () {return treeNumber;}
	double[] getMeanDbh () {return meanDbh;}
	double[] getMeanAge() {return meanAge;}
	double[] getMeanHeight() {return meanHeight;}
	double[] getMeanHcb() {return meanHcb;}
	double[] getG() {return G;}



	public void setTreeNumber (Collection c) {
		int k = 0;
		for (Iterator i = c.iterator (); i.hasNext ();) {treeNumber[k++] = (int) ((Double) i.next ()).doubleValue ();}
	}

	public void setMeanDbh (Collection c) {
		int k = 0;
		for (Iterator i = c.iterator (); i.hasNext ();) {meanDbh[k++] = (Double) i.next ();}
	}

	public void setMeanAge (Collection c) {
		int k = 0;
		for (Iterator i = c.iterator (); i.hasNext ();) {meanAge[k++] = (Double) i.next ();}
	}

	public void setMeanHeight (Collection c) {
		int k = 0;
		for (Iterator i = c.iterator (); i.hasNext ();) {meanHeight[k++] = (Double) i.next ();}
	}




}



