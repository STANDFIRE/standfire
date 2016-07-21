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
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.MuDg;
import capsis.util.methodprovider.MuMerchantVolume;

/**	Evolution of Merchant Volume over Time in a MultipartScene.
 *	@author F. de Coligny - february 2011
 */
public class MuTimeMerchantVolume extends MuTimeGraph {

	static {
		Translator.addBundle("capsis.extension.dataextractor.MuTimeMerchantVolume");
	}
	
	
	
	/**	Default constructor.
	 */
	public MuTimeMerchantVolume () {}

	/**	Constructor.
	 */
	public MuTimeMerchantVolume (GenericExtensionStarter s) {super (s);}
	
	//-------------------
	
	/**	Returns true if the 'hectare' option should be available 
	 * 	for this tool.
	 */
	public boolean acceptsPerHectare () {return true;}
	public boolean acceptsShowTotal () {return true;}
	public boolean acceptsShowMean () {return true;}
	public boolean acceptsShowParts () {return true;}
	public boolean acceptsIncrements() {return true;}
	
	/**	Returns true if the given Scene part can be accepted by this tool  
	 * 	(e.g. if we draw basal area over time, the part should contain a method
	 * 	to get the basal area...).
	 */
	public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		return mp != null && mp instanceof MuMerchantVolume;
	}
	
	/**	Returns the value that must appear in the graph for the given ScenePart 
	 * 	(e.g. basal area, dominant height...).
	 */
	public double getValue (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		// mp can not be null, was tested in matchWithPart (), see upper
		
		MuMerchantVolume m = (MuMerchantVolume) mp;
		return m.getMerchantVolume (scene, part);
	}
	
	/**	Graph name.
	 */
	public String getGraphName () {
		return Translator.swap ("MuTimeMerchantVolume");
	}
	
	public String getYAxisName () {return Translator.swap ("MuTimeMerchantVolume.yLabel");}

	/**	Extension version.
	 */
	public String getVersion () {return "1.0";}

	/**	Extension author.
	 */
	public String getAuthor () {return "F. de Coligny";}

	/**	Extension description.
	 */
	public String getDescription () {return Translator.swap ("MuTimeMerchantVolume.description");}
	
	//-------------------
	
	
	


}


