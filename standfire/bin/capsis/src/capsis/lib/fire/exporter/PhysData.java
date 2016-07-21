package capsis.lib.fire.exporter;

// checked 23/09

import java.util.ArrayList;

import capsis.lib.fire.fuelitem.FuelMatrix;

/**
 * data required for a physically-based model each export has his own data
 * structure that inheritates from PhysData (see PhysDataWFDS for WFDS and
 * PhysDataOF for Firetec) The physData must implement a method addFuelMatrix
 * that is able to add a FuelMatrix fm to a list of Grid (geometry of the
 * physically-based model mesh), according to some options pdo
 * 
 * @author pimont
 * 
 */
public abstract class PhysData {

	public static class PhysDataOptions {
		public boolean visualControl = false; // visualization of intersection
												// process between grid and
												// physdata
		public boolean overlappingPermitted; // For Firetec only, if overlapping
												// is permitted, trees that
												// overlapped will all be added
												// to the fuelmatrix (for a
												// respect of biomass)
		// however this might induce very high load at some points when the
		// stemdensity is very high. For that reason, it might be better
		// not to allow this overlapping
		// TODO FP : check if possible to implement this for WFDS (see
		// PhysDataWFDS)
		public boolean produceTreeCrownVoxel; // option for building a map that
												// connect a FiPlant to the grid
												// for post processing (physical
												// fire effects...)
		public boolean verbose = false; // tell is log print check in the log
	}

	/**
	 * Constructor
	 */
	public PhysData() {
	}

	/**
	 * Adds a FuelMatrix in the PhysData.
	 * 
	 * @param fm
	 *            : fuel matrix
	 * @param grids
	 *            : list of grid (for firetec, there is just one)
	 * @param pdo
	 *            : options, see internal class
	 * @throws Exception
	 */
	public abstract double addFuelMatrix(FuelMatrix fm, ArrayList<Grid> grids, PhysDataOptions pdo) throws Exception;
	
	
	//public abstract double getExportedFuel();
}
