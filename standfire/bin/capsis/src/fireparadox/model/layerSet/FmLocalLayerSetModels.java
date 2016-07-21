package fireparadox.model.layerSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/** Contains the property needed to define a local layerSet model (Garrigue, maquis, etc.), read in file with 
 * the FiLocalLayerSetModelLoader (extension.ioformat)
 * These models can be used as default layerSet for the user that wants a typical fuel type
 *	@author F.Pimont - sept 2009
 */

public class FmLocalLayerSetModels {
	private class SyntheticProperties {
		public String speciesName;
		public int spatialGroup;
		public double height;
		public double coverFraction;
		public double characteristicSize;
		public double liveMoisture;
		public double deadMoisture;
		public double liveBulkDensity;
		public double deadBulkDensity;
		public double svr;
		public double mvr;
		
		public SyntheticProperties (String speciesName, int spatialGroup, double height, 
				double coverFraction,
				double characteristicSize, double liveMoisture, double deadMoisture, double liveBulkDensity,
				double deadBulkDensity, double svr, double mvr) {
		this.speciesName=speciesName;
		this.spatialGroup = spatialGroup;
		this.height=height;
		this.coverFraction = coverFraction;
		this.characteristicSize=characteristicSize;
		this.liveMoisture=liveMoisture;
		this.deadMoisture=deadMoisture;
		this.liveBulkDensity = liveBulkDensity;
		this.deadBulkDensity = deadBulkDensity;
		this.svr = svr;
		this.mvr = mvr;
		}
	}

	public Map <String, Map <Integer, SyntheticProperties> > map;
	public Map <String, FmLayerSetFieldProperties > fieldPropertyMap;
	

	public FmLocalLayerSetModels() {
		map = new HashMap <String, Map <Integer, SyntheticProperties> > ();
		fieldPropertyMap = new HashMap <String,  FmLayerSetFieldProperties>();		
	}

	public void addLayer(String layerSetName, String speciesName, int spatialGroup, double height,
			double percentage, double characteristicSize, double aliveMoisture, double deadMoisture,double liveBulkDensity, double deadBulkDensity, double svr, double mvr) {
		SyntheticProperties sp = new SyntheticProperties (speciesName, spatialGroup, height, percentage, characteristicSize, aliveMoisture, deadMoisture, liveBulkDensity, deadBulkDensity, svr, mvr);
		//System.out.println("layerSetModel "+layerSetName+":"+speciesName+" "+height+" "+percentage+" "+characteristicSize+" "+aliveMoisture+" "+deadMoisture);
		Map <Integer,SyntheticProperties> layers;
		Integer number=0;
		if (map.containsKey(layerSetName)) {
			layers = map.get(layerSetName);
			number = layers.size();
		} else {
			layers = new HashMap <Integer,SyntheticProperties>();
		}
		layers.put(number, sp);
		map.put(layerSetName, layers);
	}
	public void addFieldProperty(String layerSetName, FmLayerSetFieldProperties fp) {
		fieldPropertyMap.put(layerSetName, fp);
	}
	
/*	
	public void fill() {
		Map <Integer,SyntheticProperties> layers = new HashMap <Integer,SyntheticProperties>();
		layers.put(0,new SyntheticProperties(FiModel.QUERCUS_COCCIFERA, 0.75d, 70d, 3d,70d,10d));
		layers.put(1,new SyntheticProperties("Grass", 0.25d, 30d, 0d,15d,15d));
		map.put("Q. Coccifera Garrigue", layers);
		Map <Integer,SyntheticProperties> layers1 = new HashMap <Integer,SyntheticProperties>();
		layers1.put(0,new SyntheticProperties(FiModel.AUSTRALIAN_GRASSLAND, 0.7d, 100d, 0d,5d,5d));
		map.put("Australian grasslands", layers1);
	
	}
*/	
	public Set getNames() {
		return map.keySet();
	}
	
	public int getLayerNumber(String layerSetName) {
		return map.get(layerSetName).size();		
	}
	public String getLayerName(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).speciesName;		
	}
	public double getLayerHeight(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).height;		
	}
	public double getCoverFraction(String layerSetName, int layerNumber) {
		return map.get (layerSetName).get (layerNumber).coverFraction;		
	}
	public double getCharacteristicSize(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).characteristicSize;		
	}
	public double getAliveMoisture(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).liveMoisture;		
	}
	public double getDeadMoisture(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).deadMoisture;		
	}
	public double getLiveBulkDensity(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).liveBulkDensity;		
	}
	public double getDeadBulkDensity(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).deadBulkDensity;
	}
	public double getSvr(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).svr;		
	}
	public double getMvr(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).mvr;		
	}

	public int getSpatialGroup(String layerSetName, int layerNumber) {
		return map.get(layerSetName).get(layerNumber).spatialGroup;
	}

}
