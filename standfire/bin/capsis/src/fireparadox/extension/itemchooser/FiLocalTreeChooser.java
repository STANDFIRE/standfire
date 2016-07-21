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

import jeeb.lib.defaulttype.Item;
import jeeb.lib.sketch.extension.ChooserException;
import jeeb.lib.sketch.extension.ItemChooser;
import jeeb.lib.sketch.extension.ItemPattern;
import jeeb.lib.sketch.kernel.SketchController;
import jeeb.lib.sketch.kernel.SketchExtensionManager;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.kernel.CustomType;
import jeeb.lib.sketch.scene.kernel.ItemType;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;


/**	FiLocalTreeChooser selects Ph. dreyfus trees (not in the database).
*	@author F. Pimont, F. de Coligny - june2009
*/
public class FiLocalTreeChooser implements ItemChooser, SketchController {
	// Note: ItemChooser implements InstantConfigurable
	static {
		Translator.addBundle("fireparadox.extension.itemchooser.FiLocalTreeChooser");
	}
	
	private InstantPanel instantPanel;
	// fc - 15.10.2008 - ItemChooser parameters
	public SketchModel sketchModel;
	protected Object externalRef;	// FireExternalRef, FireLayerExternalRef
	// fc - 15.10.2008 - ItemChooser parameters


	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public FiLocalTreeChooser () {}

	/**	Official constructor. It uses the standard Extension params.
	*/
	public FiLocalTreeChooser (Object referent) throws Exception {
		this.sketchModel = (SketchModel) referent;
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
			Log.println (Log.ERROR, "FiLocalTreeChooser.matchWith ()", 
					"Exception, returned false", e);
			return false;
		}
	}

	/**	Returns true if this ItemChooser needs an ItemPattern and this one is compatible.
	*/
	public boolean accepts (ItemPattern pattern) {
		return true;
	}
	// fc - 5.12.2008

	/**	Namable
	*/
	public String getName () {
			return Translator.swap ("FiLocalTreeChooser");}

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
	public String getVersion () {return "1.0";}

	/**	Return author name.
	*/
	public String getAuthor () {return "F. Pimont, F. de Coligny";}

	/**	Return short description.
	*/
	public String getDescription () {
			return Translator.swap ("FiLocalTreeChooser.description");}

	/**	From InstantConfigurable
	*/
	public void actionPerformed (ActionEvent evt) {}
	
	/**	New framework ItemChooser / ItemPattern. 
	*	Returns items respecting the given number.
	*/
	public Collection<Item> getItems (CustomType itemType, int number) throws ChooserException {

		try {
	System.out.println ("FiLocalTreeChooser getItems () begins...");
	System.out.println ("FiLocalTreeChooser extRef="+externalRef);

			Collection<Item> result = new ArrayList<Item> ();

			for (int i = 0; i < number; i++) {
				Item tree = itemType.getItem (externalRef);
				result.add (tree);
			}
			
	System.out.println ("FLTC returned collection="+AmapTools.toString (result));
	System.out.println ("FLTC back to planter...");
			
			return result;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLocalTreeChooser.getItems ()", "Exception", e);
			throw new ChooserException (Translator.swap ("FiLocalTreeChooser.couldNotGetItems"), e);
		}
		
	}
	
	public InstantPanel getInstantPanel () {
		if (instantPanel == null) {
				instantPanel = new FiLocalTreeChooserPanel (this);}
		return instantPanel;
	}
	
}




