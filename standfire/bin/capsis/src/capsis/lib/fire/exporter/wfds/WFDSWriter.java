/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

// This extension is for export to the Marc Jaeger Blob Format
package capsis.lib.fire.exporter.wfds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.util.FortranBinaryOutputStream;

/**
 * WFDSWriter
 * 
 * @author F. de Coligny - january 2008
 */
public class WFDSWriter {

	private ArrayList<Grid> grids; // used only for constantBulkDensity mode
	// private File dir;
	// private String dirName;
	// private String fileName;
	private Set<String> canopyParticles;// set of particles present in the
										// exported scene. The String is just
										// the name here

	private List<String> surfaceLines;
	private List<String> canopyParticleLines;
	private List<String> canopyLines;
	private FortranBinaryOutputStream outCBD; // output bulk densities : TODO
												// find a way to make this work
	private int treeNumber;
	private WFDSParam param;

	/**
	 * Constructor
	 */
	public WFDSWriter(WFDSParam param, ArrayList<Grid> grids) throws Exception {
		this.param = param;
		this.grids = grids;
		// this.fileName = param.fileName;
		this.canopyParticles = new HashSet<String>();
		this.surfaceLines = new ArrayList<String>();
		this.canopyLines = new ArrayList<String>();
		this.canopyParticleLines = new ArrayList<String>();
		treeNumber = 0;
		
		if (param.canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) { //we create a file "Bd" that will be moved later
		  this.outCBD= new FortranBinaryOutputStream (new FileOutputStream("Bd"),true, param.format);
		}
	}

	public double addPhysDataWFDS(PhysDataWFDS data) throws Exception {
		double exportedBiomass = 0d;
		if (data.surfaceFuel) { // extract litter from the surface fuel to put
								// it in the surface fuel of wfds
			WFDSSurfaceFuel sf = new WFDSSurfaceFuel(data, param);
			if (!sf.isEmpty()) {
				surfaceLines.add(sf.getLines());
			}
			exportedBiomass = sf.exportedMass;
		} else { // "canopy fuel"
			String canopyFuelRepresentation = param.canopyFuelRepresentation;
			if (canopyFuelRepresentation.equals(WFDSParam.CYLINDER) && (data.plant == null)) {
				// a layerSet can not be represented as a cylinder=> rectangle
				// is more appropriate
				canopyFuelRepresentation = WFDSParam.RECTANGLE;
			}
			// add the canopy lines and the particle to the canopyParticleLines
			// if required
			// System.out.println("tree "+treeNumber+"isCanopyFuel="+data.isCanopyFuel);
			try {
				boolean outputTree = true;
				WFDSCanopyFuel cf = new WFDSCanopyFuel(data, treeNumber, outputTree, outCBD, canopyFuelRepresentation,
						param.bulkDensityAccuracy, grids);
				// System.out.println("tree "+treeNumber+" empty="+cf.isEmpty
				// ());
				treeNumber = cf.treeNumber;

				if (!cf.isEmpty()) {
					canopyLines.add(cf.getLines());
					for (FiParticle fp : data.bulkDensities.keySet()) { // particles
						if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_TEXT)) { // we
																								// have
																								// to
																								// create
																								// as
																								// many
																								// particles
																								// as
																								// rounded
																								// bd
																								// and
																								// particles
							for (BulkDensityVoxel bdv : data.bulkDensities.get(fp)) {
								double bulkDensity = Math.round(bdv.bulkDensity / param.bulkDensityAccuracy)
										* param.bulkDensityAccuracy;
								if (bulkDensity > 0.5 * param.bulkDensityAccuracy) {
									String name = fp.getFullName() + "_" + bulkDensity;
									if (!canopyParticles.contains(name)) {
										canopyParticles.add(name);
										WFDSCanopyParticle cp = new WFDSCanopyParticle(fp, name, param, bulkDensity, data.color);
										if (!cp.isEmpty())
											canopyParticleLines.add(cp.getLines());
									}
									exportedBiomass += bulkDensity * grids.get(bdv.gridNumber).getVolume(bdv.i, bdv.j, bdv.k);
								}
							}
						} else { // HET_RECTANGLE_BIN, RECTANGLE and CYLINDERS
							String nameHRB = fp.getFullName();
							if (!canopyParticles.contains(nameHRB)) {
								canopyParticles.add(nameHRB);
							}
						    double fuelMass = 0d;
							for (BulkDensityVoxel bdv : data.bulkDensities.get(fp)) {
								double volume = grids.get(bdv.gridNumber).getVolume(bdv.i, bdv.j, bdv.k);
								fuelMass += bdv.bulkDensity * volume;
							}
							exportedBiomass += fuelMass;
							 
							if (canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) {
								WFDSCanopyParticle cp = new WFDSCanopyParticle(fp, nameHRB, param, -1d, data.color);
								if (!cp.isEmpty()) {
									canopyParticleLines.add(cp.getLines());
								}
							} else { // RECTANGLE and CYLINDERS
								double bulkDensity = fuelMass / cf.plantVolume; // divided by crown volume (for cylinders and rectangle)
                                                                if (bulkDensity < 3.0) {
		                                                        double bulkDensityBinned = Math.round(bulkDensity / param.bulkDensityAccuracy) * param.bulkDensityAccuracy;
		                                                        if (bulkDensityBinned > 0.5 * param.bulkDensityAccuracy) {
		                                                                String name = fp.getFullName() + "_" + bulkDensityBinned;
		                                                                if (!canopyParticles.contains(name)) {
		                                                                        canopyParticles.add(name);
											if (bulkDensity > 0d) {
												WFDSCanopyParticle cp = new WFDSCanopyParticle(fp, name, param, bulkDensityBinned, data.color);
												if (!cp.isEmpty()) {
													canopyParticleLines.add(cp.getLines());
												}
											}
		                                                                }
		                                                        }
                                                                }
							}
						}
					}
				}
			} catch (Exception e) {
				Log.println(Log.ERROR, "WFDSWrite.addPhysDataWFDS ()", "Exception", e);
				throw e;
			}
		}
		return exportedBiomass;
	}

	public double computeMeanBulkDensity(List<BulkDensityVoxel> voxels) {
		double fuelMass = 0d;
		double fuelVolume = 0d;
		for (BulkDensityVoxel bdv : voxels) {
			double volume = grids.get(bdv.gridNumber).getVolume(bdv.i, bdv.j, bdv.k);
			fuelMass += bdv.bulkDensity * volume;
			fuelVolume += volume;
		}
		if (fuelVolume > 0d)
			return fuelMass / fuelVolume;
		return 0d;
	}

	// Write the file
	public void save(File dir) throws Exception {

		try {
			System.out.println("WFDSWriter. saving files...");
			// System.out.println ("dirName:  " + dirName);

			String fileName = new File(dir, param.fileName).getCanonicalPath();
			System.out.println("fileName: " + fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

			out.write("!========================= / " + "\n");
			out.write("!SURFACE FUEL MODEL DEFINITIONS" + "\n");
			out.write("!========================= /" + "\n");
			for (String line : surfaceLines) {
				out.write(line);
			}

			out.write("!========================= / " + "\n");
			out.write("! Canopy Fuel Definitions/" + "\n");
			out.write("!========================= /" + "\n");
			for (String line : canopyParticleLines) {
				out.write(line);
			}
			for (String line : canopyLines) {
				out.write(line);
			}

			out.close();
			// writing the binary bulk density file
			if (param.canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN)) {
				// String dirName = dir.getAbsolutePath ();
				//
				// if
				// (param.canopyFuelRepresentation.equals(WFDSParam.HET_RECTANGLE_BIN))
				// {
				// this.outCBD= new FortranBinaryOutputStream (
				// new FileOutputStream
				// (dirName+File.separator+param.fileName+"Bd"),
				// true, param.format);
				// }
				outCBD.writeRecord();
				outCBD.flush();
				outCBD.close();
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "WFDSWriter.save ()", "Exception", e);
			throw e;
		}

	}

	public static void main(String[] args) {

		String userDir = Settings.getProperty("user.dir", (String) null);
		System.out.println("user.dir=" + userDir);

		// try {
		// new WFDSWriter (new File (userDir), "fileName1", "fileName2",
		// "fileName3", "fileName4", "topofile", false,
		// null, "86").save ();
		// } catch (Exception e) {
		// e.printStackTrace (System.out);
		// }
	}
}
