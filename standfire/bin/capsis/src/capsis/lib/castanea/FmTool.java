package capsis.lib.castanea;


/**	FmTool is a class with some usuals tool
*	@author Hendrik Davi - sept 2009
*/


public class FmTool {



	//===================================================== max
	public static double max(double[] t) {
	    double maximum = t[0];   // start with the first value
	    for (int i=0; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];   // new maximum
	        }
	    }
	    return maximum;
	}//end method max







}