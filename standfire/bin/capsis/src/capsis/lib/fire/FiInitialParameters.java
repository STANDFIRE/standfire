package capsis.lib.fire;

import java.util.Map;

import capsis.kernel.AbstractSettings;
import capsis.lib.fire.fuelitem.FiSpecies;

public class FiInitialParameters extends AbstractSettings {
	public double sceneOriginX;
	public double sceneOriginY;
	public double sceneSizeX;
	public double sceneSizeY;

	protected Map<String,FiSpecies> speciesMap;
	
	public Map<String,FiSpecies> getSpeciesMap () {
		return speciesMap;
	}
}
