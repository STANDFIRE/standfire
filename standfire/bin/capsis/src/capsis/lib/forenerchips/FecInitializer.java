package capsis.lib.forenerchips;

//import capsis.kernel.GScene;

/**
 * Modules must provide a class implementing FecInitializer to be compatible with ForEnerChips.
 * 
 * @author N. Bilot - February 2013
 */
public interface FecInitializer {

	public Resource getInitResource (double plotArea_ha);
	public Resource getResidualStand (double plotArea_ha);

}
