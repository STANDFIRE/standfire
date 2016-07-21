package capsis.lib.regeneration;

/**
 * Defines a method to calculate the energy intercepted by this object.
 * 
 * @author N. Dones - April 2012
 */
public interface RGLightable {

	/**
	 * Calculates the energy intercepted by this object and returns the
	 * remaining energy.
	 * @param cell : the cell that contains this object
	 * @param energy
	 *            : available above the object
	 * 
	 * @param incidentEnergy: total energy above canopy
	 * @return: energy available below the object
	 */
	public double processLighting(RGCell cell, double incidentEnergy, double energy);

}
