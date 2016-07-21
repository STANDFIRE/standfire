package fireparadox.sketch;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.sketch.extension.ItemChooser;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayerSet;
import fireparadox.model.database.FmLayerSetFromDB;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.layerSet.FmLayerSetExternalRef;


/**	FiLayerSetType is the type of a FiLayerSet Item.
*	@author F. de Coligny - march 2009
*/
public class FmLayerSetType extends jeeb.lib.sketch.scene.kernel.CustomType {
	
	/**	Constructor.
	*/
	public FmLayerSetType () {
		// name, preferredSketcherClassName
		super (Translator.swap ("FiLayerSet.itemType"), 
				capsis.lib.fire.sketcher.FiLayerSetSketcher.class);	
	}
	
	/**	Return a Sketch Item for the given external reference
	*/
	@Override
	public Item getItem (Object externalRef) throws Exception {
		try {
			FmLayerSetExternalRef extRef = (FmLayerSetExternalRef) externalRef;
			
			if (extRef.isFromDataBase ()) {
				return new FmLayerSetFromDB (0, extRef.getLayers ());	// id = 0, will be set when added in FiStand
			} else {
				return new FmLayerSet (0, extRef.getLayers ());	// id = 0, will be set when added in FiStand
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayerSetType.getItem ()", "Exception", e);
			throw new Exception ("Exception in FiLayerSetType.getItems ()", e);
		}
	}
	
	/**	Returns true if items of this type can be selected by the given chooser.
	*/
	@Override
	public boolean accepts (ItemChooser chooser) {
		return chooser instanceof fireparadox.extension.itemchooser.FiLayerSetChooser
				|| chooser instanceof fireparadox.extension.itemchooser.FiLocalLayerSetChooser;
	}
	
	
}
