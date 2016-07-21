package fireparadox.model.database;

import java.util.ArrayList;
import java.util.Collection;

import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FuelMatrix;
import capsis.lib.fire.fuelitem.FuelMatrix.HorizontalDistribution;
import fireparadox.model.layerSet.FmLayer;


/**
 * A layerSet in the FuelManager, built fro the database.
 * 
 * @author F. Pimont, F. de Coligny
 */
public class FmLayerSetFromDB extends FiLayerSet {
	
	/**
	 * Constructor.
	 */
	public FmLayerSetFromDB (int id) {
		super (id);
		this.setType ();

	}

	/**
	 * Constructor 2.
	 */
	public FmLayerSetFromDB (int id, Collection<FiLayer> layers) {
		super (id, layers);
		
	}

	/**
	 * Return a collection of FMLayers.
	 */
	public Collection<FmLayer> getFmLayers () {
		Collection<FmLayer> fmLayers = new ArrayList<FmLayer> ();
		for (FiLayer fiLayer : getLayers ()) {
			fmLayers.add ((FmLayer) fiLayer);
		}
		return fmLayers;
	}

	/**
	 * Builds a FuelMatrix. Requires an HorizontalDistribution.
	 */
	public FuelMatrix buildFuelMatrix (String sourceName, 
			boolean thinTwigsIncluded,
			HorizontalDistribution hd) throws Exception {

		FuelMatrix fm = new FuelMatrix ();

//		
//		FmDBCommunicator com = FmDBCommunicator.getInstance ();
//		
//		// Here we build a map (voxelDataMap) to link layer and their
//		// FiLayerVoxelData
//		// 1. compute the shapeIdGroup of the layer set and a map to link
//		// shapeId and Layer
//		Set<Long> shapeIds = new HashSet <Long> ();
//		Map<Long, FmLayerFromDB> layerMap = new HashMap<Long, FmLayerFromDB>();
//		for (FiLayer layer : this.getLayers ()) {
//			shapeIds.add(((FmLayerFromDB) layer).getShapeId());
//			layerMap.put(((FmLayerFromDB) layer).getShapeId(),
//					(FmLayerFromDB) layer);
//		}
//		// 2. build the voxelDataMap (with request to the base to get the
//		// voxelData
//		Map <FiLayer,FmLayerVoxelData> voxelDataMap = new HashMap <FiLayer,FmLayerVoxelData> ();
//		Collection voxelDatas = com.getVoxelData (shapeIds);
//		for (Object o : voxelDatas) {
//			FmLayerVoxelData voxelData = (FmLayerVoxelData) o;
//			long shapeId = voxelData.getShapeId ();
//			FiLayer layer = layerMap.get(shapeId);
//			voxelDataMap.put(layer, voxelData);
//		}
//
//		
//		
//		double xref = hd.xref;
//		double yref = hd.yref;
//		double zref = hd.zref;
//
//		fm.setUniqueSourceName (sourceName);
//
//		//TODO fm.setThinTwigsIncluded (thinTwigsIncluded);
//
//		fm.dx = hd.dx;
//		fm.dy = hd.dy;
//		// this.dy = polygonResolution;
//		// this.dz = Math.max(FiInitialParameters.Z_CUBE_SIZE,0.1 * (layerSet.getHeight() -
//		// layerSet.getBottomHeight()));
//		fm.dz = 0.1 * (this.getHeight () - this.getBottomHeight ());
//
//		//fm.crownVoxelVolume = fm.dx * fm.dy * fm.dz;
//
//		Vertex3d southWest = this.getMin ();
//
//		// Computation accounting for xref and yref
//		fm.x0 = xref + fm.dx * Math.floor ((southWest.x - xref) / hd.dx);
//		fm.y0 = yref + fm.dy * Math.floor ((southWest.y) / hd.dy);
//
//		Vertex3d northEast = this.getMax ();
//		fm.x1 = northEast.x;
//		fm.y1 = northEast.y;
//		fm.z0 = this.getBottomHeight ();
//		fm.z1 = this.getHeight ();
//
//		fm.nx = (int) Math.ceil ((fm.x1 - fm.x0) / fm.dx);
//		fm.ny = (int) Math.ceil ((fm.y1 - fm.y0) / fm.dy);
//		fm.nz = (int) Math.ceil ((fm.z1 - fm.z0) / fm.dz);
//		// System.out.println("	layerSet:x0,x1,y0,y1,z0,z1:" + x0 + " " + x1 + " "
//		// + y0 + " " + y1 + " " + z0 + " " + z1 + "; nx,ny,nz=" + nx
//		// + " " + ny + " " + nz);
//		double xHalfSize = fm.dx / 2;
//		double yHalfSize = fm.dy / 2;
//		double zHalfSize = fm.dz / 2;
//
//		fm.coor = new Vertex3f[fm.nx][fm.ny][fm.nz + 1];
//		fm.min = new Vertex3f[fm.nx][fm.ny][fm.nz];
//		fm.max = new Vertex3f[fm.nx][fm.ny][fm.nz];
//		fm.distributions = new FireMatrixProperties[fm.nx][fm.ny][fm.nz];
//
//		int layerNumber = this.getLayers ().size ();
//
//		boolean inPoly[][] = new boolean[fm.nx][fm.ny];
//		// number of cells in the polygon
//		int cellsInPoly = 0;
//		// number of cells in the polygon with no patchy fuel
//		int emptyCellsInPoly;
//
//		fm.horizontalDistribution = hd;
//
//		//
//		for (int i = 0; i < fm.nx; i++) {
//			for (int j = 0; j < fm.ny; j++) {
//				double cx = fm.x0 + i * fm.dx + xHalfSize;
//				double cy = fm.y0 + j * fm.dy + yHalfSize;
//				// System.out.println("cell i,j:"+cx+","+cy);
//				if (this.contains (cx, cy)) {// within the polygon
//					inPoly[i][j] = true;
//					cellsInPoly++;
//				} else {
//					inPoly[i][j] = false;
//				}
//			}
//		}
//		if (cellsInPoly == 0) { throw new Exception ("FiVoxelMatrix : no cells in the polygon of layerSet "
//				+ this.getId ()); }
//		emptyCellsInPoly = cellsInPoly;
//
//		// building of "distribution" for all filayerlines
//
//		for (int i = 0; i < fm.nx; i++) {
//			for (int j = 0; j < fm.ny; j++) {
//				for (int k = 0; k < fm.nz; k++) {
//					if (inPoly[i][j]) {
//						double cx = fm.x0 + i * fm.dx + xHalfSize;
//						double cy = fm.y0 + j * fm.dy + yHalfSize;
//						double cz = fm.z0 + k * fm.dz + zHalfSize;
//						double edgeCoef = 1d;
//						// this edgecoeff is to divide biomasse in two when the
//						// egde of the poly is exactly on the center of the
//						// voxel
//						if (this.isPointOnEdge (cx, cy)) {
//							edgeCoef = 0.5;
//						}
//
//						// coordinate of the voxel on the reference grid (whole polygon)
//						int iref = (int) Math.round ((cx - xref - xHalfSize) / fm.dx);
//						int jref = (int) Math.round ((cy - yref - yHalfSize) / fm.dy);
//						int kref = (int) Math.round ((cz - zref - zHalfSize) / fm.dz);
//
//						int lnumb = 0;
////						boolean fromDB = this.isFromDB ();
//						double totalLoad = 0d;
//						fm.effectiveHeight = 0d;
//						for (FiLayer layer : this.getLayers ()) {
//							// this edgecoeff is to divide biomasse in two when
//							// the egde of the poly is exactly on the center of
//							// the voxel
//							double horizDistrib = hd.get (layer, iref, jref) * edgeCoef;
//
//							// double defaultValue_MC = defaultValue_treeMC;
//							// if (layer.getHeight() < treeHeightThreshold)
//							// defaultValue_MC = defaultValue_shrubMC;
//
////							if (fromDB) {
//							
//							// shapeID layers
//							fm.addLayerFromDBToVoxel ((FmLayerFromDB) layer, voxelDataMap, i, j, k, cx, cy, cz, horizDistrib);
//
//
////							} else {
////								// System.out.println("BEFORE ADDLOCALLAYERTOVOXEL");
////								fm.addLocalLayerToVoxel (layer, i, j, k, cx, cy, cz, horizDistrib);
////
////								// defaultValue_MC, defaultValue_MVR,
////								// defaultValue_SVR);
////								fm.effectiveHeight += layer.getHeight () * layer.computeInternalLoad ();
////								totalLoad += layer.computeInternalLoad ();
////
////							}
//							lnumb++;
//						}
//						fm.effectiveHeight = fm.effectiveHeight / totalLoad;
//					}
//				}
//			}
//		}
//
		return fm;

	}


}
