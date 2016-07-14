package capsis.lib.crobas;

import java.io.Serializable;

/**	The PipeQual Model 
*	by Annikki Makela.
*	@author R. Schneider - 20.5.2008
*/
public interface PipeQualTree extends Serializable {
	
	public void whorlInteraction (CTree tree);
	
	public void createWhorls ();
	
}


