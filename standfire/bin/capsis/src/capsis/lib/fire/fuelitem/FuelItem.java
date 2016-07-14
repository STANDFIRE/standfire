package capsis.lib.fire.fuelitem;

import java.util.Set;

import capsis.lib.fire.exporter.PhysExporter;

/**
 * A fuel: possibly a FiPlant or a FiLayerSet that can added into a PhysData.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public interface FuelItem {

	/**
	 * Adds the fuel with PhysDataExporter.
	 */
	public double addFuelTo(PhysExporter exporter) throws Exception;

	/**
	 * get fuel biomass corresponding to PhysDataExporter.
	 */
	public double getFuelMass(PhysExporter exporter) throws Exception;

	/**
	 * a method to test if a FuelItem is in a given rectangle
	 * 
	 * @return
	 */
	public abstract boolean isInRectangle(double xMin, double xMax, double yMin, double yMax);

	/**
	 * 
	 * @return
	 */
	public abstract String getName();;

	/**
	 * print in the log some syntheticData of the FuelItem
	 */
	public abstract void printSyntheticData(Set<String> particleNames);;

}
