package capsis.lib.fire.fuelitem;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jeeb.lib.util.Log;
import capsis.lib.fire.exporter.Grid;
import capsis.util.Vertex3f;

/**
 * FuelMatrix is the detailed 3D structure of each fuel that can be build from
 * any item to export them in a fire model
 * 
 * 
 * @author F. Pimont - septembre 2013
 */
public class FuelMatrix implements Cloneable {

	/**
	 * This class contains the option required to build a fuelMatrix
	 * 
	 * @author pimont
	 * 
	 */
	public static class FuelMatrixOptions {
		// public boolean thinTwigsIncluded = true;

		public double fiPlantDiscretization = 0.05; // ratio of discretization
													// of FiPlant in x, y, z
													// direction
		public double fiLayerSetHorizontalDistributionDx = 0.5; // in m
		public double horizontalDistribVoxelNumberMaximum = 20000000d; // for
																		// horizontal
																		// Discretization
																		// of
																		// the
																		// layerSet
		public double fiLayerSetVerticalDiscretization = 0.1; // ratio of
																// discretization
																// of FiLayerSet
		public double fiLayerSetMinDz = 0.05; // in m

		public boolean verbose = false; // tell is log print check in the log
		public Set<String> particleNames; // list of particleName that we want
											// to export
	}

	/**
	 * 
	 * internal distribution within the FuellMatrix of a FiLayerSet The internal
	 * distribution is based on a 2d (xy) distribution d, that contains a map.
	 * The keySet contains layerTypes, and the values are float[nx][ny]. Each
	 * value in the array is the fraction (between 0 and 1) of the layer in the
	 * cell i, j of the 2d grid
	 * 
	 * @author pimont
	 * 
	 */
	public static class HorizontalDistribution {

		// (x0,y0) is the min of the layerSets' bounding box
		public double x0;
		public double y0;
		// (x1,y1) is the max of the layerSets' bounding box
		public double x1;
		public double y1;
		// numbers of voxels in 2d grid
		public int nx;
		public int ny;
		// horizontal resolution
		public double dx;
		public double dy;
		// map that contains the fraction of layer in cell i, j
		private Map<String, float[][]> d = new HashMap<String, float[][]>(); // <
		// layerType,horizontalDistrib[i][j]>

		private boolean cellIsOccupied[][];// tell if a given position is
											// occupied by the fuel for the
											// current spatial group (see
											// layers)
		int emptyCellsInPoly;// number of cells in the polygon with no patchy
								// fuel for the current spatial group

		public int cellsInPoly = 0;// number of cells in the polygon
		private Random rnd;
		private boolean verbose;

		/**
		 * constructor
		 * 
		 * @param verbose2
		 */
		public HorizontalDistribution(double x0, double y0, double x1, double y1, double dx, double dy, Random rnd, boolean verbose) {
			this.rnd = rnd;
			this.x0 = x0;
			this.y0 = y0;

			this.x1 = x1;
			this.y1 = y1;
			this.dx = dx;
			this.dy = dy;
			this.nx = (int) Math.ceil((x1 - x0) / dx);
			this.ny = (int) Math.ceil((y1 - y0) / dy);
			this.verbose = verbose;
			cellIsOccupied = new boolean[nx][ny];
			// for (int i = 0; i < nx; i++) {
			// for (int j = 0; j < ny; j++) {
			// cellIsOccupied[i][j] = false;
			// }
			// }
			if (verbose) {
				System.out.println("Horizontal distribution extension:x0=" + x0 + " x1=" + x1 + " y0=" + y0 + " y1="
						+ y1);
			}

		}

		/**
		 * This method entail to set cellIsOccupied to false (for a new spatial
		 * group)
		 */
		public void resetCellOccupation() {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					cellIsOccupied[i][j] = false;
				}
			}
			emptyCellsInPoly = cellsInPoly;
		}

		/**
		 * method to add a layer ll, knowing if the cell is in polygon
		 * 
		 * @param ll
		 * @param inPoly
		 *            : boolean array
		 * @param cellsInPoly
		 *            : total number of cell in the polygon
		 * @param emptyCellsInPoly
		 *            : when emptyCellsInPoly==0, no other cell can be
		 *            attributed to this spatialGroup
		 * @throws Exception
		 */
		public void addPatches(FiLayer ll, boolean[][] inPoly) throws Exception {
			double cover = 0.0;
			int patchNumber = 0;
			if (verbose) {
				System.out.println("	ray=" + ll.getCharacteristicSize() + " cover=" + ll.getCoverFraction());
			}
			while (cover < ll.getCoverFraction()) {
				patchNumber++;
				// System.out.println("patchNumber="+patchNumber+" covertemp="+cover);
				boolean isPatchEmpty = true;
				// ray of the patch
				double ray = 0.5 * ll.getCharacteristicSize();
				// center of the path position
				//double patchCenterX = Math.random() * (x1 - x0);
				//double patchCenterY = Math.random() * (y1 - y0);
				double patchCenterX = rnd.nextDouble() * (x1 - x0);
				double patchCenterY = rnd.nextDouble() * (y1 - y0);
				
				// bounding box of the patch
				int nxmin = Math.max((int) Math.ceil((patchCenterX - ray) / dx) - 1, 0);
				int nxmax = Math.min((int) Math.ceil((patchCenterX + ray) / dx), nx);
				int nymin = Math.max((int) Math.ceil((patchCenterY - ray) / dy) - 1, 0);
				int nymax = Math.min((int) Math.ceil((patchCenterY + ray) / dy), ny);
				// System.out.println("patch "+patchNumber+" x,y="+patchCenterX+" "+patchCenterY+" nxminmax="+nxmin+" "+nxmax+" nyminmax="+nymin+" "+nymax);
				for (int i = nxmin; i < nxmax; i++) {
					for (int j = nymin; j < nymax; j++) {
						// System.out.println("i,j "+i+" "+j+" distrib="+horizontalDistribution[i][j][lnumb]+" in polygone="+inPoly[i][j]);
						// if there is spaced in this cell and it is in the
						// polygon
						if (this.get(ll, i, j) < 1d && inPoly[i][j]) {
							double distanceToCenter = Math.sqrt(Math.pow((i + 0.5) * dx - patchCenterX, 2)
									+ Math.pow((j + 0.5) * dy - patchCenterY, 2));
							// if within the patch and not occupied
							// System.out.println("distanceTocenter="+distanceToCenter+" ray="+ray+" cellOcuppied="+cellIsOccupied[i][j]);
							if (distanceToCenter < ray && !cellIsOccupied[i][j]) {
								cellIsOccupied[i][j] = true;
								this.set(ll, i, j, 1d);
								cover += 1.0 / cellsInPoly;
								isPatchEmpty = false;
								emptyCellsInPoly -= 1;
								if (emptyCellsInPoly == 0) {
									throw new Exception(
											"HorizontalDistribution.addPatches() : impossible to add all patches in layer line "
													+ ll.getLayerType());
								}
							}
						}
						// to increase accuracy...
						if (cover >= ll.getCoverFraction()) {
							// System.out.println("		Patch number:"+patchNumber+" FinalCover="+cover);
							break;
						}
					}
				}
				// System.out.println("isPatchEmpty "+ isPatchEmpty);
			}
			if (verbose) {
				System.out.println("		Patch number:" + patchNumber + " FinalCover=" + cover);
			}
		}

		public void put(FiLayer layer) {
			d.put(layer.getLayerType(), new float[nx][ny]);
		}

		public double get(FiLayer layer, int i, int j) {
			// if (verbose) {
			// System.out.println ("horizontalDistrib.get(" + layer.getLayerType
			// () + ",i=" + i +
			// ",j=" + j);
			//
			// for (FiLayer l : d.keySet ()) {
			// System.out.println ("	horizontal distrib :" + l.getLayerType ());
			// }
			// for (FiLayer l : d.keySet ()) {
			// System.out.println ("	props :" + d.get (layer).length);
			// System.out.println ("	props :" + d.get (layer).getClass ());
			// System.out.println ("	props :" + d.get (layer).toString ());
			// }
			// }
			float[][] temp = (float[][]) d.get(layer.getLayerType());
			return (double) temp[i][j];
		}

		public void set(FiLayer layer, int i, int j, double value) {
			float[][] temp = (float[][]) d.get(layer.getLayerType());
			temp[i][j] = (float) value;
		}

		public void add(FiLayer layer, int i, int j, double value) {
			float[][] temp = (float[][]) d.get(layer.getLayerType());
			temp[i][j] += (float) value;
		}

		public boolean isCellOccupied(int i, int j) {
			return cellIsOccupied[i][j];
		}
	}

	/*****************************************************************************************************
	 * ******************* End of sub class
	 * horizontalDistribution***************************************
	 * ********************************
	 * *****************************************************************
	 */

	private static NumberFormat formater;
	static {
		formater = NumberFormat.getInstance(Locale.ENGLISH);
		formater.setGroupingUsed(false);
		formater.setMaximumFractionDigits(2);
	}

	// Absolute position of the fuelMatrix (fc + fp - 23.9.2009)
	// position can be reset in case of shapeIdPlant with
	// getCopyAtOtherPosition: to plant
	// the same object with only on access to database
	// private Vertex3f position;

	public int nx; // number of voxels in x, y, z
	public int ny;
	public int nz;

	public double dx; // sizes of voxels (m) in x, y, z (default: 0.25, 0.25,
						// 0.25)
	public double dy;
	public double dz;

	final double minDz = 0.05; // minimum resolution of vertical grid

	// absolute extension of the crown in three directions
	public double x0;
	public double x1;
	public double y0;
	public double y1;
	public double z0;
	public double z1;

	// 3D matrix indices:
	// considering that trees are located on the ground in (x, y), and z is the
	// altitude (height of
	// the tree)
	// first index: i grows like x
	// second index: j grows like y
	// third index: z grows like z (altitude)
	public List<FiParticle> particles;
	public List<FiMassVoxel> massVoxels;
	public boolean surfaceFuel = false; // when surfaceFuel, nz=1;

	// for topo
	public double[][] topo; // [i][j] array of topo

	// for controls
	public double totalBiomass; // kg
	public double totalCrownVolume; // m3

	// public double effectiveHeight = 0d; // m
	// private boolean thinTwigsIncluded;
	// public FiLayer TREES = new FiLayer (); // used for
	// lanl
	// litter
	// that is
	// below
	// trees and
	// lanl
	// grass on
	// the side

	// Source name must be a unique key of the object for which we build this
	// voxel matrix
	static private Set sourceNames; // to check the sourceNames are unique (keys
									// in maps)
	//static private Random random; removed by fp, now from model.rnd
	private Random rnd; 
	private String sourceName;

	// internal distribution in layerSet
	public HorizontalDistribution horizontalDistribution;

	public FiPlant plant; // only used for WFDS export!
	public Color color; //only used for WFDS export!

	// TODO FP : DB : fuelMatrix constructor to build
	public FuelMatrix() {

	}

	/**
	 * Default constructor
	 * 
	 * @param sourceName
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param x1
	 * @param y1
	 * @param z1
	 */
	public FuelMatrix(String sourceName, double dx, double dy, double dz, double x0, double y0, double z0, double x1,
			double y1, double z1, FuelMatrixOptions fmo, Color color, Random rnd) {
		massVoxels = new ArrayList<FiMassVoxel>();
		particles = new ArrayList<FiParticle>();
		this.rnd = rnd;
		this.setUniqueSourceName(sourceName);

		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
        this.color = color;
		// compute number of voxel and build mesh
		this.nx = (int) Math.ceil((this.x1 - this.x0) / this.dx);
		this.ny = (int) Math.ceil((this.y1 - this.y0) / this.dy);
		this.nz = (int) Math.ceil((this.z1 - this.z0) / this.dz);
		if (fmo.verbose) {
			System.out.println("	x0=" + x0 + ", y0=" + y0 + ", z0=" + z0);
			System.out.println("	x1=" + x1 + ", y1=" + y1 + ", z1=" + z1);
			System.out.println("	nx=" + nx + ", ny=" + ny + ", nz=" + nz);
			System.out.println("	dx=" + dx + ", dy=" + dy + ", dz=" + dz);
		}
		this.topo = new double[nx][ny];
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				topo[i][j] = 0d;
			}
		}

		// this.coor = new Vertex3f[nx + 1][ny + 1][nz + 1];
		// for (int i = 0; i < nx + 1; i++) {
		// for (int j = 0; j < ny + 1; j++) {
		// for (int k = 0; k < nz + 1; k++) {
		// coor[i][j][k] = new Vertex3f ((float) (x0 + i * dx), (float) (y0 + j
		// * dy), (float) (z0 + k * dz));
		// }
		// }
		// }
		//
	}

	/**
	 * Get x : i is left boundary of cell i, i+1 is right boundary of cell i,
	 * i+0.5 is center of cell i
	 */
	public double getX(double i) {
		return x0 + i * dx;
	}

	/**
	 * Get y : j is left boundary of cell j, j+1 is right boundary of cell j,
	 * j+0.5 is center of cell j
	 */
	public double getY(double j) {
		return y0 + j * dy;
	}

	/**
	 * Get z : k is low boundary of cell k, k+1 is up boundary of cell k, k+0.5
	 * is center of cell k
	 */
	public double getZ(double k, int i, int j) {
		return z0 + k * dz + topo[i][j];
	}

	/**
	 * Get crown Volume
	 */
	public double getVoxelVolume() {
		return dx * dy * dz;
	}

	public String getSourceName() {
		return sourceName;
	}

	/**
	 * Method for control in the log file
	 */
	public void printData() {
		this.computeTotalBiomassAndVolume();
		System.out.println("FuelMatrix.printData () source " + sourceName);
		System.out.println("	nx=" + nx + ", ny=" + ny + ", nz=" + nz + ",np=" + particles.size());
		System.out.println("	dx=" + dx + ", dy=" + dy + ", dz=" + dz);
		System.out.println("	FuelMatrix biomass=" + totalBiomass + " kg; plantVolume(m3)=" + totalCrownVolume);
		System.out.println("	 xmin=" + getX(0) + "	 xmax=" + getX(nx));
		System.out.println("	 ymin=" + getY(0) + "	 ymax=" + getY(ny));
		System.out.println("	 zmin=" + getZ(0, 0, 0) + "	 zmax=" + getZ(nz, nx - 1, ny - 1));
	}

	/**
	 * Sets the name of the source. The source is the object this VoxelMatrix
	 * belongs to. The sourceName must be unique because it is the key in some
	 * control maps. This method ensures the source is unique by appending a
	 * number to it if needed.
	 */
	public String setUniqueSourceName(String candidate) { // fc - 6.10.2009, modified by FP to use rnd from model
		if (sourceNames == null) {
			sourceNames = new HashSet(); // a set to check unicity
			//random = new Random(); // a random number generator
		}
		if (!sourceNames.contains(candidate)) { // candidate is unknown -> ok
			this.sourceName = candidate;
			sourceNames.add(candidate);
		} else {
			// If candidate is known (not unique), append some nmuber +until it
			// is unique
			String aux = candidate;
			while (sourceNames.contains(aux)) {
				int suffix = rnd.nextInt(1000000);
				aux = candidate + "_" + suffix;
			}
			this.sourceName = aux;
			sourceNames.add(aux); // must not be used for another source
		}
		return this.sourceName;
//		if (sourceNames == null || random == null) {
//			sourceNames = new HashSet(); // a set to check unicity
//			random = new Random(); // a random number generator
//		}
//		if (!sourceNames.contains(candidate)) { // candidate is unknown -> ok
//			this.sourceName = candidate;
//			sourceNames.add(candidate);
//		} else {
//			// If candidate is known (not unique), append some nmuber +until it
//			// is unique
//			String aux = candidate;
//			while (sourceNames.contains(aux)) {
//				int suffix = random.nextInt(1000000);
//				aux = candidate + "_" + suffix;
//			}
//			this.sourceName = aux;
//			sourceNames.add(aux); // must not be used for another source
//		}
//		return this.sourceName;

	}

	/**
	 * update values of totalBiomass and totalCrownVolume for controls
	 */
	public void computeTotalBiomassAndVolume() {
		this.totalBiomass = 0d;
		this.totalCrownVolume = 0d;
		for (FiMassVoxel massVoxel : massVoxels) { // sum on all voxels
			double bmInVox = 0d;
			for (FiParticle fp : massVoxel.masses.keySet()) {
				bmInVox += massVoxel.masses.get(fp);
			}
			if (bmInVox > 0d) {
				this.totalBiomass += bmInVox;
				this.totalCrownVolume += dx * dy * dz;
			}
		}
	}

	/**
	 * Clone method for FuelMatrix
	 */
	@Override
	public FuelMatrix clone() {
		try {
			FuelMatrix copy = (FuelMatrix) super.clone();
			copy.particles = new ArrayList<FiParticle>();
			copy.massVoxels = new ArrayList<FiMassVoxel>();
			for (FiParticle fp : particles) {
				copy.particles.add(fp.copy());
			}
			// for all voxel
			for (FiMassVoxel massVoxel : massVoxels) {
				copy.massVoxels.add(massVoxel.copy());
				;
			}
			copy.topo = topo.clone();
			return copy;
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiVoxelMatrix.clone ()", "Error while cloning FiVoxelMatrix: " + this, e);
		}
		return null;
	}

	/**
	 * this method temporary set the elevation of the topo array of the matrix
	 * from the topofile of the firetecMatrix depending on the i,j of the
	 * firetecmatrix where the center of the voxel is
	 */
	public void setElevation(Grid gr) {
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				int[] ind = gr.getIndicesXY(getX(i + 0.5), getY(j + 0.5),0d);
				if (ind[0] != -1 && ind[1] != -1) {
					topo[i][j] = (float) gr.coor[ind[0]][ind[1]][0].z;
				}
			}
		}
	}

	/**
	 * Only used for the first constructor (FiPlantVoxelData voxelData). The
	 * voxelMatrix is located at a given absolute position (position of the tree
	 * OR reference position of the layer). This method translates the
	 * voxelMatrix to the given new position. This method returns a copy of this
	 * FiVoxelMatrix (6.10.2009).
	 * 
	 */
	public FuelMatrix getCopyAtOtherPosition(String candidateSourceName, Vertex3f newPosition) {
		FuelMatrix copy = this.clone();

		// The voxelMatrix is used for another source: set new source name
		copy.setUniqueSourceName(candidateSourceName);
		double xShift = newPosition.x - 0.5 * (copy.x0 + copy.x1);
		double yShift = newPosition.y - 0.5 * (copy.y0 + copy.y1);
		double zShift = newPosition.z - 0.5 * (copy.z0 + copy.z1);

		copy.x0 = x0 + xShift;
		copy.x1 = x1 + xShift;
		copy.y0 = y0 + yShift;
		copy.y1 = y1 + yShift;
		copy.z0 = z0 + zShift;
		copy.z1 = z1 + zShift;

		// See new sourceName management in constructors and
		// getCopyAtOtherPosition ()
		// ~ source = formater.format(newPosition.x) + "_"
		// ~ + formater.format(newPosition.y) + "_"
		// ~ + formater.format(newPosition.z);
		return copy;
	}

	/**
	 * test is a given i,j cell center of the FuelMatrix is in or out the
	 * polygon of the layerset
	 * 
	 * @param i
	 * @param j
	 * @param poly
	 * @return
	 */
	public boolean isCellCenterInPoly(int i, int j, FiLayerSet poly) {
		double cx = x0 + (i + 0.5) * dx;
		double cy = y0 + (j + 0.5) * dy;
		return poly.contains(cx, cy);
	}

	/**
	 * test is a given i,j cell center of the FuelMatrix is on the edge of the
	 * polygon of the layerset
	 * 
	 * @param i
	 * @param j
	 * @param poly
	 * @return
	 */
	public boolean isPointOnEdge(int i, int j, FiLayerSet ls) {
		double cx = x0 + (i + 0.5) * dx;
		double cy = y0 + (j + 0.5) * dy;
		return ls.isPointOnEdge(cx, cy);
	}

	/*
	 * fc-29.1.2015 
	 * // TODO FP : DB addLayerFromDBToVoxel public void
	 * addLayerFromDBToVoxel(FmLayerFromDB layer, Map<FiLayer, FmLayerVoxelData>
	 * voxelDataMap, int i, int j, int k, double cx, double cy, double cz,
	 * double horizontalDistribution // double deadMC, double defaultValue_MC,
	 * double defaultValue_MVR, // double defaultValue_SVR ) { //
	 * FmLayerVoxelData voxelData = voxelDataMap.get (layer); // double
	 * dxDBVoxel = voxelData.getVoxelDx (); // double dyDBVoxel =
	 * voxelData.getVoxelDy (); // double dzDBVoxel = voxelData.getVoxelDz ();
	 * // double correctionVolumeRatio = dx * dy * dz / (dxDBVoxel * dyDBVoxel
	 * // * dzDBVoxel); // Collection<FmLTVoxel> voxels =
	 * voxelData.getCenterVoxels (); // // for (FmLTVoxel voxel : voxels) { //
	 * int kDBVoxel = voxel.getK (); // double zminDBVoxel = kDBVoxel *
	 * dzDBVoxel; // double zmaxDBVoxel = (kDBVoxel + 1) * dzDBVoxel; // // if
	 * the center of the FiVoxelMatrix is // // between bottom and top // // of
	 * cell "voxel", then biomass can be // // added // if (cz < zmaxDBVoxel &&
	 * cz >= zminDBVoxel) { // FmLTFamilyProperty aliveBiomasses =
	 * voxel.getAliveBiomasses (); // if (aliveBiomasses != null) { //
	 * Set<String> familyNames = aliveBiomasses.getFamilyNames (); // for
	 * (String familyName : familyNames) { // if (familyToBeSelected
	 * (familyName)) { // double biomass = voxel.getAliveBiomass (familyName) *
	 * // horizontalDistribution // * correctionVolumeRatio; // double mvr =
	 * voxelData.getMVR (familyName); // double svr = voxelData.getSVR
	 * (familyName); // // double moisture = layer.getLiveMoisture (); // //
	 * updateVoxel (i, j, k, biomass, mvr, svr, moisture, cx, cy, cz, //
	 * layer.getHeight ()); // // //
	 * System.out.println("		in voxel :"+i+" "+j+" "
	 * +k+":biomass of "+familyName+"="+biomass); // } // } // } //
	 * FmLTFamilyProperty deadBiomasses = voxel.getDeadBiomasses (); // if
	 * (deadBiomasses != null) { // Set<String> familyNames =
	 * deadBiomasses.getFamilyNames (); // for (String familyName : familyNames)
	 * { // if (familyToBeSelected (familyName)) { // double biomass =
	 * voxel.getAliveBiomass (familyName) * // horizontalDistribution // *
	 * correctionVolumeRatio; // double mvr = voxelData.getMVR (familyName); //
	 * double svr = voxelData.getSVR (familyName); // double moisture =
	 * layer.getDeadMoisture (); // updateVoxel (i, j, k, biomass, mvr, svr,
	 * moisture, cx, cy, cz, // layer.getHeight ()); // } // } // } // } // }
	 * 
	 * } fc-29.1.2015
	 */
}