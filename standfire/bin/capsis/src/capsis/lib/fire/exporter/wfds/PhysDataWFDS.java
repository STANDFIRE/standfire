package capsis.lib.fire.exporter.wfds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeb.lib.util.Log;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysData;
import capsis.lib.fire.fuelitem.FiMassVoxel;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FuelMatrix;
import capsis.util.Vertex3f;

/**
 * the physical data for wfds (extends PhysData) is attached to a given fuel
 * item. Any fuelMatrix can be added to it though the addFuelMatrix method.
 * 
 * @author pimont
 * 
 */

public class PhysDataWFDS extends PhysData {

	public Map<FiParticle, List<BulkDensityVoxel>> bulkDensities; // kg/m3, for
																	// each
																	// particle,
																	// a list of
																	// voxels

	public Vertex3f p0; // southwest corner on wfds grid
	public Vertex3f p1; // northeast corner on wfds grid
	public double actualFuelDepth = 0; // usefull for surface fuels...
	public double dz1 = 0; // cell size of grid (1,1,1).z usefull for surface
							// fuels...
	public boolean surfaceFuel; // tell that this physdata is a surface fuel
	public FiPlant plant; // only required to get access to tree extension for
							// the WFDS CYLINDER format and to know if the
							// PhysData comes from a FiPlant or FiLayerSet
	public Color color;
	

	/**
	 * Constructor
	 */
	public PhysDataWFDS() {
		super();
		bulkDensities = new HashMap<FiParticle, List<BulkDensityVoxel>>();
		p0 = new Vertex3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		p1 = new Vertex3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
	}
	
	/**
	 * Adds a FuelMatrix in the PhysData.
	 */
	public double addFuelMatrix(FuelMatrix fm, ArrayList<Grid> grids, PhysDataOptions pdo) throws Exception {

		surfaceFuel = fm.surfaceFuel;
		plant = fm.plant;
		color =  fm.color;
		Map<FiParticle, Integer> partN = new HashMap<FiParticle, Integer>(); // map
																				// des
																				// particles
		int npart = 0;
		for (FiParticle particle : fm.particles) {
			partN.put(particle, npart);
			npart += 1;
		}

		// StringBuffer report = new StringBuffer ();
		// for control
		double exportedBiomass = 0.0;
		double crownVolume = 0.0;

		// initialization of a map to know if a wfds voxel is already filled
		// (for controls of crownVolume)
		// boolean[][][] isVoxelAlreadyFilled = new boolean[nx][ny][nz];
		// for (int fmi = 0; fmi < nx; fmi++) {
		// for (int fmj = 0; fmj < ny; fmj++) {
		// for (int fmk = 0; fmk < nz; fmk++) {
		// isVoxelAlreadyFilled[fmi][fmj][fmk] = false;
		// }
		// }
		// }

		try {
			for (int gridNumber = 0; gridNumber < grids.size(); gridNumber++) {
				Grid grid = grids.get(gridNumber);
				// boolean intersection = false; // tell if there is an
				// intersection between grid and the fuel matrix

				int[] ind = grid.getIndicesXYZ(Math.max(fm.x0, grid.getX0()), Math.max(fm.y0, grid.getY0()), fm.z0,0d);

				int i0 = ind[0];
				int j0 = ind[1];
				int k0 = Math.max(ind[2], 0);
				ind = grid.getIndicesXYZ(Math.min(fm.x1, grid.getX1()), Math.min(fm.y1, grid.getY1()), fm.z1,1d);
				int i1 = ind[0];
				int j1 = ind[1];
				int k1 = ind[2];
				if (k1 == -1)
					k1 = grid.nz - 1;

				double[][][][] mass = new double[i1 - i0 + 1][j1 - j0 + 1][k1 - k0 + 1][npart]; // temporary
																								// array
																								// for
																								// mass
				boolean[][][] voxelFull = new boolean[i1 - i0 + 1][j1 - j0 + 1][k1 - k0 + 1]; // temporary
																								// array
																								// for
																								// volume
																								// computation
				dz1 = grid.coor[0][0][1].z;
				fm.setElevation(grid);
				String sourceName = fm.getSourceName();
				// if (pdo.visualControl) {
				// voxelMatrixMap.put (sourceName, fm);
				// }

				// report.append ("\nFuelId: " + sourceName +
				// " processing fuelMatrix");
				for (FiMassVoxel vox : fm.massVoxels) {
					int fmi = vox.i;
					int fmj = vox.j;
					int fmk = vox.k;
					actualFuelDepth = Math.max(actualFuelDepth, fm.getZ(fmk + 1, fmi, fmj));
					// report.append ("\nCrown voxel [" + fmi + "][" + fmj +
					// "][" + fmk + "]");
					// System.out.println ("crown voxel ["+i+"]["+j+"]["+k+"]");
					double[][] contributions = grid.intersection(fm, fmi, fmj, fmk, sourceName);
					if (contributions == null) {
						continue;
					}
					for (int n = 0; n < contributions.length; n++) {
						int i = (int) contributions[n][0];
						int j = (int) contributions[n][1];
						int k = (int) contributions[n][2];
						double contrib = contributions[n][3];
						if (contrib>0) {
						// report.append ("\nWFDS voxel [" + i + "][" + j + "]["
						// + k + "], contrib="
						// + contrib);
						 //System.out.println(" contrib="+contrib+","+i+","+j+","+k+","+i0+","+j0+","+k0+","+i1+","+j1+","+k1);
						for (FiParticle part : vox.masses.keySet()) {
							double fuelMassV = vox.masses.get(part);
							// System.out.println("	"+partN.get(part)+","+npart);
							mass[i - i0][j - j0][k - k0][partN.get(part)] += contrib * fuelMassV;
						}
						}
					}
				}
				for (FiParticle part : partN.keySet()) {
					int ipart = partN.get(part);
					ArrayList<BulkDensityVoxel> bds = new ArrayList<BulkDensityVoxel>();
					for (int i = i0; i < i1 + 1; i++) {
						for (int j = j0; j < j1 + 1; j++) {
							for (int k = k0; k < k1 + 1; k++) {
								double massInVoxel = mass[i - i0][j - j0][k - k0][ipart];
								if (massInVoxel > 0) {
									exportedBiomass += massInVoxel;
									voxelFull[i - i0][j - j0][k - k0] = true;
									// System.out.println(i+","+j+","+k+","+massInVoxel+","+i1+","+j1+","+k1);
									float wfdsVoxelVolume = grid.getVolume(i, j, k);
									bds.add(new BulkDensityVoxel(gridNumber, i, j, k, massInVoxel / wfdsVoxelVolume));
								}
							}
						}
					}
					bulkDensities.put(part, bds);
				}
				for (int i = i0; i < i1 + 1; i++) {
					for (int j = j0; j < j1 + 1; j++) {
						for (int k = k0; k < k1 + 1; k++) {
							if (voxelFull[i - i0][j - j0][k - k0]) {
								crownVolume += grid.getVolume(i, j, k);
								;
							}
						}
					}
				}
				Vertex3f p0tmp = grid.coor[i0][j0][k0];
				Vertex3f p1tmp = grid.coor[i1 + 1][j1 + 1][k1 + 1];
				// The below min and max are probably not required
				p0.x = Math.min(p0.x, p0tmp.x);
				p0.y = Math.min(p0.y, p0tmp.y);
				p0.z = Math.min(p0.z, p0tmp.z);
				//System.out.println("	 xmax=" + p1.x + "	 xmax=" + p1tmp.x+"	 i1=" + i1+"	 j1=" + j1);
				p1.x = Math.max(p1.x, p1tmp.x);
				p1.y = Math.max(p1.y, p1tmp.y);
				p1.z = Math.max(p1.z, p1tmp.z);

			}

			if (pdo.verbose) {
				System.out.println("Exported wfdsMatrix: biomass=" + exportedBiomass + " kg; Fuel Volume (m3)="
						+ crownVolume);
				System.out.println("	 xmin=" + p0.x + "	 xmax=" + p1.x);
				System.out.println("	 ymin=" + p0.y + "	 ymax=" + p1.y);
				System.out.println("	 zmin=" + p0.z + "	 zmax=" + p1.z);
				// int nbvalues = 0;
				// for (FiParticle particle: fm.particles) {
				// nbvalues += bulkDensities.get(particle).size();
				// }
				// System.out.println ("	 bulkDensity values=" + nbvalues);
			}
			return exportedBiomass;

		} catch (Exception e) {
			// Log.println (Log.ERROR, "PhysDataWFDS.addFuelMatrix ()",
			// "Exception, " + report, e);
			Log.println(Log.ERROR, "PhysDataWFDS.addFuelMatrix ()", "Exception, ", e);
			throw e;
		}
	}
}