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
package capsis.lib.fire.exporter;

// checked 23/09
/*
 * TODO FP: check use of fuelMassControl and bulkVolumeTotal use to be checked below line 134
 * 
 * TODO FP: low priority : - voxelMatrixMap and contributionMap - loudScene
 */

import java.util.ArrayList;
import java.util.Set;

import jeeb.lib.util.Translator;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.PhysData.PhysDataOptions;
import capsis.lib.fire.fuelitem.FuelItem;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.util.NativeBinaryInputStream;

/**
 * Exporter that compute for each physics based model the appropriate physData
 * structure PhysData (PhysDataWFDS or PhysDataOf). Contains options object
 * grids and stand This is the superclass of Firetec and WFDS.
 * 
 * @author F. de Coligny, F. Pimont - september 2009, refactored 2013
 */

public class PhysExporter {

	static {
		Translator.addBundle("capsis.lib.fire.exporter.PhysExporter");
	}

	public static String BIGENDIAN = NativeBinaryInputStream.SPARC; // machine
																	// format
	public static String LITTLEENDIAN = NativeBinaryInputStream.X86;

	public ArrayList<Grid> grids; // physically based model grid(s)
	public PhysData physData; // physical data (PhysDataWFDS or PhysDataOf)
	public FiStand stand; // The stand contains plants and layerSets, the latter
							// contains layers.
	
	public FiModel model; // added FiModel, contains the particle names // fc-6.5.2015
	public Set<String> modelParticleNames; // always made available // fc-6.5.2015
	
	public FuelMatrixOptions fmo;
	public PhysDataOptions pdo;
	
	public double[] exportedBiomass = {0d,0d,0d}; // use for control: initial, in physData, really exported

	/**
	 * basic constructor, for the gui
	 */
	public PhysExporter() {
		this.fmo = new FuelMatrixOptions();
		this.pdo = new PhysDataOptions();
		this.grids = new ArrayList<Grid>();

	}

	/**
	 * Constructor. when all properties are already known. Script mode
	 */
	public PhysExporter(FiStand stand, FuelMatrixOptions fmo, PhysDataOptions pdo) {
		this.stand = stand;

		this.model = (FiModel) stand.getStep ().getProject ().getModel (); // fc-6.5.2015
		this.modelParticleNames = model.particleNames; // fc-6.5.2015

		this.grids = new ArrayList<Grid>();
		this.fmo = fmo;
		this.pdo = pdo;
	}

	/**
	 * method to known is a fuelItem has an intersection with the grid set
	 * 
	 * @param fi
	 * @return
	 */
	public boolean isInExportedZone(FuelItem fi) {
		for (int gridNumber = 0; gridNumber < grids.size(); gridNumber++) {
			Grid grid = grids.get(gridNumber);
			if (fi.isInRectangle(grid.getX0(), grid.getX1(), grid.getY0(), grid.getY1())) {
				return true;
			}
		}
		return false;
	}

	// Accessors below
	public void setOverlappingPermitted(boolean overlappingPermitted) {
		this.pdo.overlappingPermitted = overlappingPermitted;
	}

	public boolean isOverlappingPermitted() {
		return pdo.overlappingPermitted;
	}

	public void setProduceTreeCrownVoxel(boolean produceTreeCrownVoxel) {
		this.pdo.produceTreeCrownVoxel = produceTreeCrownVoxel;
	}

	public boolean isProduceTreeCrownVoxel() {
		return pdo.produceTreeCrownVoxel;
	}

}
