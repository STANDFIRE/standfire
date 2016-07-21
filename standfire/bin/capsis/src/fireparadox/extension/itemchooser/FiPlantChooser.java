/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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
package fireparadox.extension.itemchooser;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.sketch.extension.ChooserException;
import jeeb.lib.sketch.extension.ItemChooser;
import jeeb.lib.sketch.extension.ItemPattern;
import jeeb.lib.sketch.kernel.SketchExtensionManager;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.kernel.CustomType;
import jeeb.lib.sketch.scene.kernel.ItemType;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;


/**	FiPlant Item chooser.
*	Chooses FirePlants from the FireParadox fueldb (using buffers, 
*	no access to the database)
*	@author O. Vigy, F. de Coligny - july 2007
*/
public class FiPlantChooser implements ItemChooser {
	// fc sept 2009 review
	
	// Note: ItemChooser implements InstantConfigurable
	static {
		Translator.addBundle("fireparadox.extension.itemchooser.FiPlantChooser");
	}
	
	private InstantPanel instantPanel;
	// fc - 15.10.2008 - ItemChooser parameters
	public SketchModel sketchModel;
	protected Object externalRef;	// FiPlantSyntheticData instance
	// fc - 15.10.2008 - ItemChooser parameters


	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public FiPlantChooser () {}

	/**	Official constructor. It uses the standard Extension params.
	*/
	public FiPlantChooser (Object referent) throws Exception {		// fc - 15.10.2008
		this.sketchModel = (SketchModel) referent;		// fc - 15.10.2008
	}

	/**	Extension dynamic compatibility mechanism.
	*/
	public boolean matchWith (Object referent) {
		try {
			ItemType type = (ItemType) referent;
			// this chooser is compatible if the type accepts it
			return (type instanceof CustomType 
					&& ((CustomType) type).accepts (this));
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantChooser.matchWith ()", 
					"Exception, returned false", e);
			return false;
		}
	}

	/**	Returns true if this ItemChooser needs an ItemPattern and this one is compatible.
	*/
	public boolean accepts (ItemPattern pattern) {
		return true;
	}

	/**	Namable
	*/
	public String getName () {
			return Translator.swap ("FiPlantChooser");}

	/**	Return extension type
	*/
	public String getType () {return SketchExtensionManager.ITEM_CHOOSER;}

	/**	Return getClass ().getName () : complete class name including package name.
	*/
	public String getClassName () {return getClass ().getName ();}

	/**	Optional initialization processing. Called after constructor.
	*/
	public void activate () {}

	/**	Return version.
	*/
	public String getVersion () {return "1.1";}

	/**	Return author name.
	*/
	public String getAuthor () {return "O. Vigy, F. de Coligny";}

	/**	Return short description.
	*/
	public String getDescription () {
			return Translator.swap ("FiPlantChooser.description");}
	
	/**	From InstantConfigurable
	*/
	public void actionPerformed (ActionEvent evt) {}
	
	/**	New framework ItemChooser / ItemPattern. 
	*	Returns items respecting the given number.
	*/
	public Collection<Item> getItems (CustomType itemType, int number) throws ChooserException {
		
		try {
			ArrayList<Item> list = new ArrayList<Item> ();
			
			for (int k = 0; k < number; k++) {
				// fc - 12.6.2007
				Item t = itemType.getItem (externalRef);
				
				list.add (t);
			}
			
			// Shuffle items - fc - 15.3.2007
			Collections.shuffle (list);
			
			return list;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantChooser.getItems ()", "Exception", e);
			throw new ChooserException (Translator.swap ("FiPlantChooser.couldNotGetItems"), e);
		}
			
	}
	
	public InstantPanel getInstantPanel () {
		if (instantPanel == null) {
				instantPanel = new FiPlantChooserPanel (this);}
		return instantPanel;
	}
	
}




