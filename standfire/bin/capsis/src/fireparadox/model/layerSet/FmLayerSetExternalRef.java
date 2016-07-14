package fireparadox.model.layerSet;

import java.util.ArrayList;
import java.util.Collection;

import capsis.lib.fire.fuelitem.FiLayer;
import fireparadox.model.database.FmLayerFromDB;


/**	FiLayerSetExternalRef contains data to instantiate a FiLayerSet.
*	@author F. de Coligny - march 2009
*/
public class FmLayerSetExternalRef implements Cloneable {

	//~ private String dbFuelId;
	//~ private double height;

	private Collection<FiLayer> layers;


	/**	Constructor.
	*/
	public FmLayerSetExternalRef () {
		layers = new ArrayList<FiLayer> ();
	}

	//~ public String getDbFuelId () {return dbFuelId;}
	//~ public double getHeight () {return height;}

	
	public void addLayer (FiLayer layer) {
		if (layer == null) {return;}
		layers.add (layer);
	}
	
	public void addLayers(Collection<? extends FiLayer> layers) {
		if (layers == null) {return;}
		this.layers.addAll (layers);
	}

	public Collection<FiLayer> getLayers () {
		return layers;
	}

	public boolean isFromDataBase () {
		if (layers.isEmpty ()) return false;
		for (FiLayer l : layers) {
			if (l instanceof FmLayerFromDB) return true;
		}
		return false;
	}
	
	//~ public Object clone () {	// to be redefined in subclasses
		//~ FiLayerSetExternalRef o = null;
		//~ try {
			//~ o = (FiLayerSetExternalRef) super.clone ();
		//~ } catch (Exception exc) {}
		//~ return o;
	//~ }
	
	@Override
	public String toString () {
		return "FiLayerSetExternalRef_#layers="+(layers==null?"null":layers.size ());
	}
}

