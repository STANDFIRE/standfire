package fireparadox.model.database;

import java.awt.Color;
import java.io.Serializable;

import jeeb.lib.defaulttype.CrownProfileUtil;
import jeeb.lib.util.Log;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.lib.fire.fuelitem.FuelMatrix;

/**
 * FiPlant is a tree built from the database in the fireparadox module.
 * 
 * @author O. Vigy, E. Rigaud - september 2006 and F. Pimont
 */
public class FmPlantFromDB extends FiPlant implements Serializable {

	// fc - september 2009 - review

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	// These properties may become immutable
	// private Color crownColor;
	// private FiSpecies species;
//	private String fileId; // fc + ov - 6.6.2007
//	private int pop; // PhD 2008-09-18

	// PlantSyntheticData description
	private long shapeId; // UUID of the plant in the database
	// private String speciesName; //species name
	private double voxelDx; // size of voxel in m (X axe)
	private double voxelDy; // size of voxel in m (X axe)
	private double voxelDz; // size of voxel in m (X axe)
	// ~ private double height; // height of the fuel in m (trunk+crown)
	// private double crownBaseHeight; // crown base height in m
	// private double crownBaseHeightBeforePruning; // used for biomass computation
	// and crownProfile
	// assessment
	// private double crownDiameter; // crown diameter in m
	// private double crownPerpendicularDiameter; // crown perpendicular diameter in m
	// private double maxDiameterHeight; // max diameter height in m
	// private double meanThinBulkDensity;
	// private double lai;
	private String teamName; // team name (owner)
	private boolean checked; // plant is validated true/false

	// TreeWithCrownProfile interface
	// This crown profile (description in percentage of crown heights / crown radius)
	// is always the same for a given tree during the simulation
//	private double[][] crownProfile;

	private String patternName; // name of the current associated pattern

	// private double liveMoisture;
	// private double liveTwigMoisture;
	// private double deadMoisture;
	private boolean closedEnvironment = true; // default closed environment

	// Fire variables
	// private FiFireParameters fire;
	// Damage variables
	// private FiSeverity severity;

	// To be removed (I think) voxel matrix is retrieved in the database
	// by Export (FiretecFeeder) for the shapeId trees and calculated in the
	// VoxelMatrixClass (special constructor) for the local trees.
	// ~ private FiVoxelMatrix crownMatrix;

	/**
	 * Constructor with a synthetic data description from the fire fuel database.
	 */
	public FmPlantFromDB (FmPlantSyntheticData data, Color crownColor, FiSpecies species, FiModel model) throws Exception {

		// id=0, stand=null, age=0, height=0, dbh=0, marked=false, x=0, y=0, z=0 (explicitly set
		// below)
		// id, stand,age,x,y,z, fileId, dbh
		super (0, null, model, 0, 0d, 0d, 0d, 0d,
				// height, CBH,CD,CLD
				data.getHeight (), data.getCrownBaseHeight (), data.getCrownDiameter (), 
				species);
		this.maxDiameterHeight = data.getMaxDiameterHeight ();
		this.crownColor = crownColor;

		this.shapeId = data.getShapeId ();
		this.voxelDx = data.getVoxelDx ();
		this.voxelDy = data.getVoxelDy ();
		this.voxelDz = data.getVoxelDz ();
		this.crownPerpendicularDiameter = data.getCrownPerpendicularDiameter ();
		this.totalThinMass = data.getMeanBulkdensity0_2mm () * this.voxelDx * this.voxelDy * this.voxelDz;

		//this.lai = data.getLai ();
		this.teamName = data.getTeamName ();
		this.checked = data.isChecked ();

		try {
			// Create a crown profile for the tree (spheric crown)
			crownGeometry = CrownProfileUtil.createRelativeCrownProfile (new double[] {0, 0, 50, 100, 100, 0}); // '50'
																												// could
																												// be
																												// recalculated
																												// with
																												// maxDiameterHeight
			this.setCrownDiameterHeightFromCrownGeometry ();
		} catch (Exception e) {
			Log.println (Log.WARNING, "FiPlant.c ()", "could not create a crown profile, passed", e);
		}

		this.patternName = null;
		// ~ this.crownMatrix = null;

		// fc - 9.9.2009 (from PhD 2008-09-15 in other constructor)
		if (getDbh () <= 0) {
			setDbh (Math.max (1, 100d * crownDiameter / 20d));
		}

	}

	
	/**
	 * Builds a FuelMatrix for a given FiPlant from data base.
	 */
	public FuelMatrix buildFuelMatrix (String sourceName, boolean thinTwigsIncluded) throws Exception {

		// May be adapted to process plants in groups (e.g. 10 x 10)
		FuelMatrix fm = new FuelMatrix ();
		
//		FmDBCommunicator com = FmDBCommunicator.getInstance ();
//		
//		Collection<Long> shapeIds = new ArrayList<Long> ();
//		shapeIds.add (shapeId);
//		Collection voxelDatas = com.getVoxelData (shapeIds);
//
//		FmPlantVoxelData voxelData = (FmPlantVoxelData) voxelDatas.iterator ().next ();
//
//		fm.setUniqueSourceName (sourceName);
//
//		//fm.setThinTwigsIncluded (thinTwigsIncluded);
//		fm.dx = voxelData.getVoxelDx ();
//		fm.dy = voxelData.getVoxelDy ();
//		fm.dz = voxelData.getVoxelDz ();
//		//fm.crownVoxelVolume = fm.dx *fm. dy * fm.dz;
//		int iMin = Integer.MAX_VALUE;
//		int jMin = Integer.MAX_VALUE;
//		int kMin = Integer.MAX_VALUE;
//		int iMax = 0;
//		int jMax = 0;
//		int kMax = 0;
//		Collection<FmLTVoxel> voxels = voxelData.getVoxels ();
//		// get dimensions of the voxelData
//		for (FmLTVoxel voxel : voxels) {
//			iMin = Math.min (iMin, voxel.getI ());
//			iMax = Math.max (iMax, voxel.getI ());
//			jMin = Math.min (jMin, voxel.getJ ());
//			jMax = Math.max (jMax, voxel.getJ ());
//			kMin = Math.min (kMin, voxel.getK ());
//			kMax = Math.max (kMax, voxel.getK ());
//		}
//
//		double crownDiameter = ((iMax - iMin) + 1) * fm.dx;
//		double crownPerpendicularDiameter = ((jMax - jMin) + 1) * fm.dy;
//		double crownBaseHeight = kMin * fm.dz;
//		double treeHeight = (kMax + 1) * fm.dz;
//		fm.x0 = -crownDiameter / 2;
//		fm.x1 = crownDiameter / 2;
//		fm.y0 = -crownPerpendicularDiameter / 2;
//		fm.y1 = crownPerpendicularDiameter / 2;
//		fm.z0 = crownBaseHeight;
//		fm.z1 = treeHeight;
//		fm.nx = (int) Math.ceil ((fm.x1 - fm.x0) / fm.dx);
//		fm.ny = (int) Math.ceil ((fm.y1 - fm.y0) / fm.dy);
//		fm.nz = (int) Math.ceil ((fm.z1 - fm.z0) / fm.dz);
//
//		fm.voxelIsInitialized = new boolean[fm.nx][fm.ny][fm.nz];
//		fm.center = new Vertex3f[fm.nx][fm.ny][fm.nz];
//		fm.min = new Vertex3f[fm.nx][fm.ny][fm.nz];
//		fm.max = new Vertex3f[fm.nx][fm.ny][fm.nz];
//		fm.distributions = new FireMatrixProperties[fm.nx][fm.ny][fm.nz];
//
//		double xHalfSize = fm.dx / 2;
//		double yHalfSize = fm.dy / 2;
//		double zHalfSize = fm.dz / 2;
//
//		voxels = voxelData.getVoxels ();
//
//		// ITERATION OF VOXELS:
//
//		for (FmLTVoxel voxel : voxels) {
//			int i = voxel.getI ();
//			int j = voxel.getJ ();
//			int k = voxel.getK ();
//
//			double cx = fm.x0 + i * fm.dx + xHalfSize;
//			double cy = fm.y0 + j * fm.dy + yHalfSize;
//			double cz = fm.z0 + k * fm.dz + zHalfSize;
//
//			// 1. ALIVE MATERIAL
//			FmLTFamilyProperty aliveBiomasses = voxel.getAliveBiomasses ();
//			if (aliveBiomasses != null) {
//				Set<String> familyNames = aliveBiomasses.getFamilyNames ();
//				for (String familyName : familyNames) {
//					if (fm.familyToBeSelected (familyName)) {
//						double biomass = voxel.getAliveBiomass (familyName);
//						double mvr = voxelData.getMVR (familyName);
//						double svr = voxelData.getSVR (familyName);
//						// this moisture will be set separately with setMoisture
//						// for shapeIdPlant
//						double liveMoisture = 0.0;
//						// double liveTwigMoisture = 0.0;
//						// double defaultValue_MC = 0.0;
//						fm.updateVoxel (i, j, k, biomass, mvr, svr, liveMoisture,
//						// defaultValue_MVR,defaultValue_SVR,
//						// defaultValue_MC,
//						cx, cy, cz, Double.MAX_VALUE);
//					}
//				}
//			}
//			// 2. DEAD MATERIAL
//			FmLTFamilyProperty deadBiomasses = voxel.getDeadBiomasses ();
//			if (deadBiomasses != null) {
//				Set<String> familyNames = deadBiomasses.getFamilyNames ();
//				for (String familyName : familyNames) {
//					if (fm.familyToBeSelected (familyName)) {
//						double biomass = voxel.getDeadBiomass (familyName);
//						double mvr = voxelData.getMVR (familyName);
//						double svr = voxelData.getSVR (familyName);
//						// this moisture will be set separately with setMoisture
//						// for shapeIdPlant
//						double deadMoisture = 0.0;
//						// this moisture will be set separately with setMoisture
//						fm.updateVoxel (i, j, k, biomass, mvr, svr, deadMoisture,
//						// deadMC, defaultValue_MVR, defaultValue_SVR,
//						cx, cy, cz, Double.MAX_VALUE);
//					}
//				}
//			}
//
//			// System.out.println("Control DBVoxelData "+voxelData.getShapeId()+
//			// "exported biomass="+biomassSum+"kg");
//		}
		return fm;
	}

	// TreeWithCrownProfile interface
	// Location of the tree on the scene (in m.)
	// ~ public double getX () {return x;}
	// ~ public double getY () {return y;}
	// ~ public double getZ () {return z;}

	// Main dimensions
	// ~ public int getAge () {return 0;}
	// ~ public double getDbh () {return 1;} // cm
	// ~ public double getHeight () {return height;} // m

	// Crown (simple, may be upgraded later)

	public long getShapeId () {
		return shapeId;
	}

	public double getVoxelDx () {
		return voxelDx;
	}

	public double getVoxelDy () {
		return voxelDy;
	}

	public double getVoxelDz () {
		return voxelDz;
	}

	public boolean isClosedEnvironment () {
		return closedEnvironment;
	}

	@Override
	public String toString () {
		return "FiPLant id=" + getId () + " shapeId=" + getShapeId ();
	}

}
