package capsis.lib.fire.exporter.firetec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Log;
import jeeb.lib.util.SetMap;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysData;
import capsis.lib.fire.fuelitem.FiMassVoxel;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FuelMatrix;

/**
 * This is the PhysData for the firetec export, it contains the 3D arrays of
 * properties on the firetec grid (fuelVolume, fuelMass, fuelSurface, waterMass,
 * massAveragedHeight, fuelDepth...)
 * 
 * It implements the method addFuelMatrix (see PhysData) that add any fuel
 * matrix to this.
 * 
 * @author pimont
 * 
 */
public class PhysDataOF extends PhysData {

	public double maxHeight;

	public int nx;
	public int ny;
	public int nz;

	public float[][][] fuelVolume; // m3
	public float[][][] fuelMass; // kg
	public float[][][] fuelSurface; // m2
	public float[][][] waterMass; // kg
	public float[][][] massAveragedHeight; // m
	public float[][][] fuelDepth; // m

	// This list contain treeId and a map <cellNumber, FiretecTreeListElement>
	// to be able to rebuild the crown of all tree from firetecMatrix
	public Map<Integer, Map> treeCrownMap = new HashMap<Integer, Map>();

	// Controls of export : These two arrays are common to all models to avoid
	// crown overlappings resulting in a too high
	// densities or cover fractions
	public float[][][] fuelMassControl; // kg/m3 maximum possible mass in a
										// FuelMatrix, if
	// reached, the voxel is "full"
	public float[][][] bulkVolumeTotal; // added by FP- 20.4.2009- m3 total bulk
										// volume added in a
	// FuelMatrix voxel if it reaches firetecVoxelVolume, the voxel is "full"

	// Control maps:
	public Map<String, FuelMatrix> voxelMatrixMap; // key = voxelMatrix source
	public SetMap<CKey, CValue> contributionMap;
	private boolean loudScene; // do not calculate voxelMatrix and contribution
								// maps if too loud

	public PhysDataOF(int nx, int ny, int nz, Grid grid) {
		super();
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;

		// System.out.println ("PhysDataOF nx: "+nx+" ny: "+ny+" nz: "+nz);
		fuelVolume = new float[nx][ny][nz];
		fuelMass = new float[nx][ny][nz];
		fuelSurface = new float[nx][ny][nz];
		waterMass = new float[nx][ny][nz];
		massAveragedHeight = new float[nx][ny][nz];
		fuelDepth = new float[nx][ny][nz];

		// initialization of controls:
		fuelMassControl = new float[nx][ny][nz];
		bulkVolumeTotal = new float[nx][ny][nz];

		// totalNumberOfVoxels = 0;
		voxelMatrixMap = new HashMap<String, FuelMatrix>();
		contributionMap = new SetMap<CKey, CValue>();
	}

	/**
	 * This method give some default values in empty cells below maxTreeHeight
	 * in the PhysDataOF (none 0 value)
	 */
	public void putDefaultValuesInEmptyCells(Grid grid) {
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				for (int k = 0; k < nz; k++) {
					double height = grid.coor[i][j][k].z - grid.coor[i][j][0].z;
					if (height <= maxHeight) {
						if (fuelVolume[i][j][k] <= 0f || fuelMass[i][j][k] <= 0f) {
							fuelMass[i][j][k] = (float) (Firetec.RHOF_DEFAULT * grid.getVolume(i, j, k));
							fuelVolume[i][j][k] = grid.getVolume(i, j, k);
						}
						if (fuelSurface[i][j][k] <= 0f) {
							fuelSurface[i][j][k] = (float) (Firetec.RHOF_DEFAULT * Firetec.SVR_DEFAULT * fuelVolume[i][j][k]);
						}
						if (k == 0 && massAveragedHeight[i][j][k] == 0f) {
							//finally fueldepth is used
							massAveragedHeight[i][j][k] = (float) (0.5 * height * fuelMass[i][j][k]);
						}
						if (k == 0) {
							fuelDepth[i][j][k] = Math.max(fuelDepth[i][j][k], Firetec.MIN_FUELDEPTH_DEFAULT);
						}
					}
				}
			}
		}
	}

	public double getExportedFuel() {
		double exportedFuel=0d;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				for (int k = 0; k < nz; k++) {
					exportedFuel+=fuelMass[i][j][k];
				}
			}
		}
		return exportedFuel;
	}
	
	/**
	 * Adds a FuelMatrix in the PhysData.
	 */
	public double addFuelMatrix(FuelMatrix fm, ArrayList<Grid> grids, PhysDataOptions pdo) throws Exception {

		Grid grid = grids.get(0); // always just one grid in firetec...
		StringBuffer report = new StringBuffer();
		// for control
		double exportedBiomass = 0.0;
		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE;
		double zmax = -Double.MAX_VALUE;
		double crownVolume = 0.0;

		// int totalNumberOfVoxels = 0;

		// initialization of a map to know if a firetec voxel is already filled
		// (for controls of crownVolume)
		boolean[][][] isVoxelAlreadyFilled = new boolean[nx][ny][nz];
		for (int fmi = 0; fmi < nx; fmi++) {
			for (int fmj = 0; fmj < ny; fmj++) {
				for (int fmk = 0; fmk < nz; fmk++) {
					isVoxelAlreadyFilled[fmi][fmj][fmk] = false;
				}
			}
		}

		try {
			// TODO FP : real topo this should be done differently when there
			// will be a real topo
			// set the elevation of the fm according to topofile of
			// firetecmatrix
			fm.setElevation(grid);
			String sourceName = fm.getSourceName();
			if (pdo.visualControl) {
				voxelMatrixMap.put(sourceName, fm);
			}

			double crownVoxelVolume = fm.getVoxelVolume();
			// int nVoxels = 0;

			report.append("\nFuelId: " + sourceName + " processing fuelMatrix");
			for (FiMassVoxel vox : fm.massVoxels) {
				int i = vox.i;
				int j = vox.j;
				int k = vox.k;
				// for (int i = 0; i < fm.nx; i++) {
				// for (int j = 0; j < fm.ny; j++) {
				// for (int k = 0; k < fm.nz; k++) {

				report.append("\nCrown voxel [" + i + "][" + j + "][" + k + "]");
				// System.out.println ("crown voxel ["+i+"]["+j+"]["+k+"]");
				double[][] contributions = grid.intersection(fm, i, j, k, sourceName);
				// System.out.println ("contribution computation done");
				// if the crown voxel is outside the borders of the scene,
				// ignore it
				if (contributions == null) {
					continue;
				}
				// nVoxels++;

				// CKey cKey = new CKey (sourceName, i, j, k);
				for (int n = 0; n < contributions.length; n++) {
					// CValue cValue = new CValue (contributions[n][0],
					// contributions[n][1],
					// contributions[n][2], contributions[n][3]);
					// fc - 20.10.2009 - keep memory only if not loudScene
					// if (!physDataBuilder.isLoudScene ()) {
					// TODO FP : check why there is a problem here cKey
					// physDataBuilder.contributionMap.addObject (cKey, cValue);
					// }
					int fmi = (int) contributions[n][0];
					int fmj = (int) contributions[n][1];
					int fmk = (int) contributions[n][2];
					double contrib = contributions[n][3];
					report.append("\nFiretec voxel [" + fmi + "][" + fmj + "][" + fmk + "], contrib=" + contrib);

					float firetecVoxelVolume = grid.getVolume(fmi, fmj, fmk);
					// The following test is added by FP to avoid excessive
					// filling of
					// cells 20-04-2009
					// It entails to limit the bulkVolumeTotal inside a cell to
					// exceed
					// the firetecVoxelVolume
					if (!pdo.overlappingPermitted
							&& (bulkVolumeTotal[fmi][fmj][fmk] + contrib * crownVoxelVolume > firetecVoxelVolume)) {
						contrib = (float) (firetecVoxelVolume - bulkVolumeTotal[fmi][fmj][fmk]) / crownVoxelVolume;
						bulkVolumeTotal[fmi][fmj][fmk] = firetecVoxelVolume;
					} else {
						bulkVolumeTotal[fmi][fmj][fmk] += contrib * crownVoxelVolume;
					}

					for (FiParticle part : vox.masses.keySet()) {
						double fuelMassV = vox.masses.get(part);

						if (contrib * fuelMassV > 0.0) {
							xmin = Math.min(xmin, grid.coor[fmi][fmj][fmk].x);
							xmax = Math.max(xmax, grid.coor[fmi + 1][fmj][fmk].x);
							ymin = Math.min(ymin, grid.coor[fmi][fmj][fmk].y);
							ymax = Math.max(ymax, grid.coor[fmi][fmj + 1][fmk].y);
							zmin = Math.min(zmin, grid.coor[fmi][fmj][fmk].z);
							zmax = Math.max(zmax, grid.coor[fmi][fmj][fmk + 1].z);

							fuelVolume[fmi][fmj][fmk] += contrib * fuelMassV / part.mvr; // m3
							fuelMass[fmi][fmj][fmk] += contrib * fuelMassV; // kg
							fuelSurface[fmi][fmj][fmk] += contrib * fuelMassV / part.mvr * part.svr; // m2
							waterMass[fmi][fmj][fmk] += contrib * fuelMassV * part.moisture * 0.01; // kg
							if (fmk == 0 & fuelMass[fmi][fmj][fmk] > 0.0) {
								maxHeight = Math.max(maxHeight, fm.getZ(k + 1, i, j));
								// float cellHeight = fm.coor[i][j][k + 1].z -
								// fm.coor[i][j][k].z;
								// After discussions we decided with Rod to use the max,
								// because in terms of drag effect, the litter should not play a role if not alone
								massAveragedHeight[fmi][fmj][fmk] += contrib * fuelMassV * fm.dz; // not used
								fuelDepth[fmi][fmj][fmk] = Math.min(
										Math.max(fuelDepth[fmi][fmj][fmk], (float) fm.getZ(k + 1, i, j)),
										(float) grid.coor[fmi][fmj][fmk + 1].z);
								// if (fuelDepth[fmi][fmj][fmk]>1.6d) {
								// System.out.println("	fd:"+fuelDepth[fmi][fmj][fmk]+","+fm.getZ
								// (k + 1, i, j)+","+ grid.coor[fmi][fmj][fmk +
								// 1].z+";"+fmi+","+fmk);
								// }
							}
							if (pdo.produceTreeCrownVoxel) {
								// TODO FP treeCrownVoxel this id...
								// fc+fp-10.9.2013
								// treeList
								// if (id > 0) {
								// Map<Integer,FiretecTreeListElement>
								// crownListMap = treeCrownMap.get (id);
								// // unique identifier in matrix from
								// // 1 to
								// // nx*ny*nz
								// int voxelNumber = fmi + 1 + (fmj) * nx + fmk
								// * nx * ny;
								//
								// if (!crownListMap.containsKey (voxelNumber))
								// {
								// FiretecTreeListElement el = new
								// FiretecTreeListElement (fmi, fmj, fmk,
								// contrib * crownVoxelVolume, contrib *
								// fuelMassV);
								// crownListMap.put (voxelNumber, el);
								// } else {
								// FiretecTreeListElement el = crownListMap.get
								// (voxelNumber);
								// el.volume += contrib * crownVoxelVolume;
								// el.biomass += contrib * fuelMassV;
								// }
								//
								// // Voxel at ground level is added by
								// // default!
								// if (!(fmk == 0)) {
								// if (!crownListMap.containsKey (fmi + 1 +
								// (fmj) * nx)) {
								// FiretecTreeListElement el = new
								// FiretecTreeListElement (fmi, fmj,
								// 0, contrib * crownVoxelVolume, contrib *
								// fuelMassV);
								// crownListMap.put (fmi + 1 + (fmj) * nx, el);
								// }
								// }
								// }
							} // end produceTreeCrownVoxel
								// controls
							exportedBiomass += contrib * fuelMassV;

							if (!isVoxelAlreadyFilled[fmi][fmj][fmk]) {
								crownVolume += firetecVoxelVolume;
								isVoxelAlreadyFilled[fmi][fmj][fmk] = true;
							}
						}
					}
				}
				// }
				// }

			}
			// ~ fm.cumulatedContributions[fmi][fmj][fmk] += (float)contrib;

			// totalNumberOfVoxels += nVoxels;

			if (pdo.verbose) {
				System.out.println("Exported firetecMatrix: biomass=" + exportedBiomass + " kg; Fuel Volume (m3)="
						+ crownVolume);
				System.out.println("	 xmin=" + xmin + "	 xmax=" + xmax);
				System.out.println("	 ymin=" + ymin + "	 ymax=" + ymax);
				System.out.println("	 zmin=" + zmin + "	 zmax=" + zmax);
			}
			return exportedBiomass;

		} catch (Exception e) {
			Log.println(Log.ERROR, "PhysDataOF.addFuelMatrix ()", "Exception, " + report, e);
			throw e;
		}
	}

	// public String toString () {
	// String s =
	// "PhysDataOF, fuelVolume: "+fuelVolume.length+" fuelMass: "+fuelMass.length+"...";
	// s+= " fuelMass[0][0][0]: "+fuelMass[0][0][0];
	// return s;
	// }

}
