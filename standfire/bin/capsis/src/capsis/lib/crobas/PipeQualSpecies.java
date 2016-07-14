package capsis.lib.crobas;

import java.io.Serializable;

import jeeb.lib.util.Translator;
import jeeb.lib.util.autoui.annotations.AutoUI;
import jeeb.lib.util.autoui.annotations.Editor;

/**	Species specific parameters used in PipeQual (A. Makela, and H. Makeninen 2003. For. Eco. and Manag.). 
* 	Values are set to those estimated through empirical relations for jack pine when possible, or those proposed by Makela and Makinen 2003.
*	@author R. Schneider - 20.5.2008
*/
@AutoUI(title="PipeQualSpecies.name")  // this annotation is needed for the AutoPanel, all variables with @Editor will be in it
public class PipeQualSpecies implements Serializable {
	
	// Parameter definitions
	// Beta function parameters for vertical foliage distribution
	//Nodal Whorls
	
	@Editor
	public double pBeta=2;
	@Editor
	public double qBeta=1;

	@Editor
	public double amin2 = 100; 
	@Editor
	public double amax2 = 400; 
	
	//Active pipe area in branches relative to stem
	@Editor
	public double alpha=1.0275;
	@Editor
	public double alphaSBr = 485.33;
	@Editor
	public double alpha1 = 0.00093821;
	@Editor
	public double alpha2 = 2.8693;

	//Empirical coefficient relating branch length in whorl to distance
	//of whorl from tree top
	@Editor
	public double gamma=0.386;
	@Editor
	public double zbr=0.60959;

	//Age-dependent specific rate of disuse in active pipes
	@Editor
	public double s=0.015;

	// Height interpolation parameter
	@Editor
	public double p1 = -0.4;
	
	// Taper parameters for initialisation procedure
	@Editor
	public double b2 = 0.08032484;
	@Editor
	public double b3 = 0.29589870;
	
	
	
	/**	The toString method is used by the AutoUIs (@AutoUI, @Editor...)
	 */
	public String toString () {return Translator.swap ("PipeQualSpecies.name");}
	
	
}


