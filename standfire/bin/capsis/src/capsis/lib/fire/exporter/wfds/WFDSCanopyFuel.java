package capsis.lib.fire.exporter.wfds;

// FIX FP : WFDSCanopyFuel is still to be done...

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.DefaultNumberFormat;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.util.FortranBinaryOutputStream;
import capsis.util.Vertex3f;

/**
 * A canopy fuel in the WFDS export.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class WFDSCanopyFuel {

	private PhysDataWFDS data;
	private boolean empty;
	private StringBuffer lines;
	private NumberFormat nf;
	public int treeNumber;
	public double plantVolume = 0d; //volume of the shape

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public WFDSCanopyFuel(PhysDataWFDS data, int treeNumber, boolean outputTree, FortranBinaryOutputStream cbdOut,
			String canopyFuelRepresentation, double bulkDensityAccuracy, ArrayList<Grid> grids) throws IOException {
		this.data = data;
		this.treeNumber = treeNumber;
		this.empty = true;
		lines = new StringBuffer();

		nf = DefaultNumberFormat.getInstance();

		this.treeNumber += 1;
		String label = "FUEL" + this.treeNumber;
		if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) { // write
																			// name
																			// of
																			// label
																			// in
																			// unformated
																			// file
			cbdOut.writeCharacter(this.completeTo(label, 100));
			cbdOut.writeRecord();
		}
		lines.append(" ! " + label + "  -- specific instances /" + "\n");
		int nfam = 0;
		for (FiParticle fp : data.bulkDensities.keySet()) {
			if (data.bulkDensities.get(fp).size() > 0) {
				this.empty = false;
				nfam += 1;
			}
		}
		if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) { // write
																			// number
																			// of
																			// fuel
																			// family
																			// in
																			// unformated
																			// file
			cbdOut.writeInteger(nfam);
			cbdOut.writeRecord();
		}

		for (FiParticle fp : data.bulkDensities.keySet()) {
			List<BulkDensityVoxel> bds = data.bulkDensities.get(fp);
			double fuelMass = 0d;
			for (BulkDensityVoxel bdv : bds) {
				double volume = grids.get(bdv.gridNumber).getVolume(bdv.i, bdv.j, bdv.k);
				fuelMass += bdv.bulkDensity * volume;
			}
			if (fuelMass > 0d) {
				String output_tree = ".FALSE.";
				if (outputTree) {
					output_tree = ".TRUE.";
				}

				if (canopyFuelRepresentation.equals(WFDSParam.RECTANGLE)) {
					double x0, x1,y0,y1,z0,z1;
					if (data.plant==null) {// LayerSet
					  x0 = data.p0.x;
					  x1 = data.p1.x;
					  y0 = data.p0.y;
					  y1 = data.p1.y;
					  z0 = data.p0.z;
					  z1 = data.p1.z;
					} else { // Plant
					  double cr = 0.5 * data.plant.getCrownDiameter();
					  x0 = Math.max(data.plant.getX() - cr, data.p0.x); // max for scene contours (when a tree is cutted by scene edge)
					  x1 = Math.min(data.plant.getX() + cr, data.p1.x);
					  y0 = Math.max(data.plant.getY() - cr, data.p0.y);
					  y1 = Math.min(data.plant.getY() + cr, data.p1.y);
					  double zc = data.plant.getZ();
					  z0 = data.plant.getCrownBaseHeight() + zc;
					  z1 = data.plant.getHeight() + zc;
					}
					lines.append("&TREE XB=" + x0 + "," + x1 + "," + y0 + "," + y1 + ","+ z0 + "," + z1);
					lines.append(",PART_ID='" + fp.getFullName() + "',FUEL_GEOM='RECTANGLE',OUTPUT_TREE=" + output_tree
							+ ",LABEL='" + label + "_" + fp.getFullName() + "' / \n");
					plantVolume = (x1 - x0) * (y1 - y0) * (z1 - z0); 

				} else if (canopyFuelRepresentation.equals(WFDSParam.CYLINDER)) { // data
																					// issued
																					// from
																					// the
																					// "plant"
																					// properties
																					// (only
																					// call
																					// for
																					// a
																					// FiPlant,
																					// not
																					// a
																					// LayerSet)
					double xc = data.plant.getX();
					double yc = data.plant.getY();
					double zc = data.plant.getZ();
					double cw = data.plant.getCrownDiameter();
					double cbh = data.plant.getCrownBaseHeight();
					double height = data.plant.getHeight();
					plantVolume = 0.25 * Math.PI * cw *cw * (height - cbh);
					lines.append("&TREE XYZ=" + xc + "," + yc + "," + zc);
					lines.append(",PART_ID='" + fp.getFullName() + "',FUEL_GEOM='CYLINDER', CROWN_WIDTH=" + cw
							+ ", CROWN_BASE_HEIGHT=" + cbh + ", TREE_HEIGHT=" + height + ",OUTPUT_TREE=" + output_tree
							+ ",LABEL='" + label + "_" + fp.getFullName() + "' / \n");

				} else if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) { // write
																							// voxels
																							// in
																							// unformated
																							// file
					lines.append("&TREE XB=" + data.p0.x + "," + data.p1.x + "," + data.p0.y + "," + data.p1.y + ","
							+ data.p0.z + "," + data.p1.z);
					cbdOut.writeCharacter(this.completeTo(fp.getFullName(), 100));
					cbdOut.writeRecord();
					cbdOut.writeInteger(bds.size());
					cbdOut.writeRecord();
					// System.out.println("voxel number="+bds.size()+" for particle "+fp.name);
					for (BulkDensityVoxel bdv : bds) {
						cbdOut.writeInteger(bdv.gridNumber + 1);
						cbdOut.writeInteger(bdv.i + 1);
						cbdOut.writeInteger(bdv.j + 1);
						cbdOut.writeInteger(bdv.k + 1);
						cbdOut.writeReal((float) bdv.bulkDensity);
					}
					cbdOut.writeRecord();
					cbdOut.flush();
					lines.append(",PART_ID=" + fp.getFullName() + ",FUEL_GEOM='HET_RECTANGLE',OUTPUT_TREE="
							+ output_tree + ",LABEL='" + label + "_" + fp.getFullName() + "' / \n");

				} else if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_TEXT)) { // write
																							// voxels
																							// in
																							// formated
																							// standard
																							// WFDS
																							// file
					int voxNumber = 0; // voxel number for label
					System.out.println("voxel number=" + bds.size());
					for (BulkDensityVoxel bdv : bds) {
						Vertex3f[][][] coor = grids.get(bdv.gridNumber).coor;
						double bulkDensity = Math.round(bdv.bulkDensity / bulkDensityAccuracy) * bulkDensityAccuracy;
						// System.out.println(voxNumber+":"+coor[bdv.i][bdv.j][bdv.k].x+","+coor[bdv.i][bdv.j][bdv.k].y+","+coor[bdv.i][bdv.j][bdv.k].z+","+bulkDensity+","+bdv.bulkDensity+","+fp.name);
						if (bulkDensity > 0.5 * bulkDensityAccuracy) {
							String ptName = fp.getFullName() + "_" + bulkDensity;
							lines.append("&TREE XB=" + coor[bdv.i][bdv.j][bdv.k].x + ","
									+ coor[bdv.i + 1][bdv.j][bdv.k].x + "," + coor[bdv.i][bdv.j][bdv.k].y + ","
									+ coor[bdv.i][bdv.j + 1][bdv.k].y + "," + coor[bdv.i][bdv.j][bdv.k].z + ","
									+ coor[bdv.i][bdv.j][bdv.k + 1].z);
							lines.append(",PART_ID='" + ptName + "',FUEL_GEOM='RECTANGLE',OUTPUT_TREE=" + output_tree
									+ ",LABEL='" + label + "_" + fp.getFullName() + "_vox_" + voxNumber + "' / \n");
							voxNumber = voxNumber + 1;
						}
					}
				}
			}
		}
	}

	public boolean isEmpty() {
		return empty;
	}

	public String getLines() {
		return lines.toString();
	}

	private String completeTo(String str, int size) {
		while (str.length() < size) {
			str = str + " ";
		}
		return str;
	}

}
