/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2011  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.dataextractor;

import jeeb.lib.util.Translator;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePart;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.CrobasBiomassProvider;

/**	Evolution of crobas compartments over Time in a MultipartScene.
*	@author F. de Coligny - february 2011
*/
public class Mu2TimeCrobasBiomass extends Mu2TimeGraph {

	static {
		Translator.addBundle("capsis.extension.dataextractor.Mu2TimeCrobasBiomass");
	}
	
	
	
	/**	Default constructor.
	 */
	public Mu2TimeCrobasBiomass () {}

	/**	Constructor.
	 */
	public Mu2TimeCrobasBiomass (GenericExtensionStarter s) {super (s);}
	
	//-------------------
	
	/**	Returns true if the 'hectare' option should be available 
	 * 	for this tool.
	 */
	public boolean acceptsPerHectare () {return false;}
	
	/**	Returns true if the 'show increments' option should be made available for this tool.
	 */
	public boolean acceptsIncrements () {return true;}
	
	/**	Returns true if the given Scene part can be accepted by this tool  
	 * 	(e.g. if we draw basal area over time, the part should contain a method
	 * 	to get the basal area...).
	 */
	public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		return mp != null && mp instanceof CrobasBiomassProvider && part instanceof TreeCollection;
	}

	
	
	/**	Returns the number of values to be drawn for each part (over time).
	 */
	public int numberOfValues () {return 5;}
	
	/**	Returns the value that must appear in the graph for the given ScenePart 
	 * 	(e.g. basal area, dominant height...).
	 */
	@Override
	public double[] getValues (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		// mp can not be null, was tested in matchWithPart (), see upper
		
		CrobasBiomassProvider m = (CrobasBiomassProvider) mp;
		double[] r = new double[5];
		r[0] = m.getWb (((TreeCollection) part).getTrees ());
		r[1] = m.getWf (((TreeCollection) part).getTrees ());
		r[2] = m.getWr (((TreeCollection) part).getTrees ());
		r[3] = m.getWs (((TreeCollection) part).getTrees ());
		r[4] = m.getWt (((TreeCollection) part).getTrees ());
		
		return r;
	}
	
	/**	Returns the names of the values returned by getValues () (e.g. "ddom", "dg").
	 */
	public String[] getNames () {
		return new String[] {"Wb", "Wf", "Wr", "Ws", "Wt"};
	}

	
	
	/**	Graph name.
	 */
	public String getGraphName () {
		return Translator.swap ("Mu2TimeCrobasBiomass");
	}
	
	public String getYAxisName () {return Translator.swap ("Mu2TimeCrobasBiomass.yLabel");}

	/**	Extension version.
	 */
	public String getVersion () {return "1.0";}

	/**	Extension author.
	 */
	public String getAuthor () {return "F. de Coligny";}

	/**	Extension description.
	 */
	public String getDescription () {return Translator.swap ("Mu2TimeCrobasBiomass.description");}
	
	//-------------------	

}
