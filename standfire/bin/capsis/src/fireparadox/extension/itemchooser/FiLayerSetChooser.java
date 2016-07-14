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

import capsis.lib.fire.fuelitem.FiLayerSet;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.sketch.extension.ChooserException;
import jeeb.lib.sketch.extension.ItemChooser;
import jeeb.lib.sketch.extension.ItemPattern;
import jeeb.lib.sketch.kernel.SketchController;
import jeeb.lib.sketch.kernel.SketchExtensionManager;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.kernel.CustomType;
import jeeb.lib.sketch.scene.kernel.ItemType;
import jeeb.lib.sketch.util.SketchTools;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

/**	FiLayerSetChooser SceneItem chooser.
*	Chooses FiLayerSetChoosers from the FireParadox fueldb (using buffers, 
*	no access to the database)
*	@author F. de Coligny , F Pimont- december 2008
*/
public class FiLayerSetChooser implements ItemChooser, SketchController {
	// fc sept 2009 review

	// Note: ItemChooser implements InstantConfigurable
	static {
		Translator.addBundle ("fireparadox.extension.itemchooser.FiLayerSetChooser");
	}
	
	private InstantPanel instantPanel;
	// fc - 15.10.2008 - ItemChooser parameters
	public SketchModel sketchModel;
	public Polygon polygon;
	protected Object externalRef;	// FireExternalRef, FireLayerExternalRef
	// fc - 15.10.2008 - ItemChooser parameters


	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public FiLayerSetChooser () {}

	/**	Official constructor. It uses the standard Extension params.
	*/
	public FiLayerSetChooser (Object referent) throws Exception {		// fc - 15.10.2008
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
			Log.println (Log.ERROR, "FiLayerSetChooser.matchWith ()", 
					"Exception, returned false", e);
			return false;
		}
	}

	/**	Returns true if this ItemChooser needs an ItemPattern and this one is compatible.
	*/
	public boolean accepts (ItemPattern pattern) {
		return false;	// no pattern needed for layers
	}
	// fc - 5.12.2008

	/**	Namable
	*/
	public String getName () {
			return Translator.swap ("FiLayerSetChooser");}

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
	public String getAuthor () {return "F. de Coligny";}

	/**	Return short description.
	*/
	public String getDescription () {
			return Translator.swap ("FiLayerSetChooser.description");}

	/**	From InstantConfigurable
	*/
	public void actionPerformed (ActionEvent evt) {}
	
	/**	New framework ItemChooser / ItemPattern. 
	*	Returns items respecting the given number.
	*/
	public Collection<Item> getItems (CustomType itemType, int number) throws ChooserException {
		
		try {
			// Number is -1 (ignored here: we return a layer based on the selected polygon in the SceneModel
			Item l = itemType.getItem (externalRef);
			FiLayerSet layerSet = (FiLayerSet) l;
			
			// The layer will match the selected polygon (which will be replaced by the layer)
			layerSet.updateToMatch (polygon);
			
			// Remove the basic polygon
			sketchModel.getUndoManager ().undoableRemoveItems (this, SketchTools.inSet (polygon));
			
			// The caller will add the returned item into the SceneModel
			Collection<Item> c = new ArrayList<Item> ();
			c.add (layerSet);
			
			return c;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayerSetChooser.getItems ()", "Exception", e);
			throw new ChooserException (Translator.swap ("FiLayerSetChooser.couldNotGetItems"), e);
		}
			
	}
	
	public InstantPanel getInstantPanel () {
		if (instantPanel == null) {
				instantPanel = new FiLayerSetChooserPanel (this);}
		return instantPanel;
	}
	
}




