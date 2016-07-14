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
package capsis.lib.fire.exporter.firetec;

import java.io.File;
import java.io.FileOutputStream;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import capsis.lib.fire.exporter.Grid;
import capsis.util.FortranBinaryOutputStream;

//TODO FP : check what is done with actualFuelDepth

/**
 * FiretecWriter is the class that is used to write a PhysDataOF within the
 * appropriate files All arrays begin with 0 index.
 * 
 * @author F. de Coligny - january 2008
 */
public class FiretecWriter {
	private File dir;
	private String dirName;
	private String fileName1; // treesrhof.dat
	private String fileName2; // sizescale.dat
	private String fileName3; // moisture.dat
	private String fileName4; // actualfueldepth.dat
	private String topofileName;
	private String treeCrownFileName;
	private PhysDataOF data;
	private String format;
	private boolean produceTreeCrownVoxel;
	private Grid grid;

	public FiretecWriter(File dir, String baseFileName, String topofileName, boolean produceTreeCrownVoxel,
			PhysDataOF data, String format, Grid grid) throws Exception {
		this.dir = dir; // the directory where the files must be created
		dirName = dir.getAbsolutePath();
		this.fileName1 = baseFileName + Firetec.RHOF;
		this.fileName2 = baseFileName + Firetec.SS;
		this.fileName3 = baseFileName + Firetec.MOIST;
		this.fileName4 = baseFileName + Firetec.FUELDEPTH;
		this.topofileName = topofileName;
		this.produceTreeCrownVoxel = produceTreeCrownVoxel;
		this.treeCrownFileName = "treesCrownVoxel.txt";
		this.data = data;
		this.format = format;
		this.grid = grid;
	}

	// Write the 4 files
	public void save() throws Exception {
		try {

			System.out.println("FiretecMatrixWriter. saving files...");

			System.out.println("dirName=" + dirName);
			System.out.println("nx,ny,nz=" + data.nx + "," + data.ny + "," + data.nz);

			// format NativeBinaryInputStream.X86=littleEndian
			// NativeBinaryInputStream.sparc=bigEndian

			FortranBinaryOutputStream out1 = new FortranBinaryOutputStream(new FileOutputStream(dirName
					+ File.separator + fileName1), true, format);
			FortranBinaryOutputStream out2 = new FortranBinaryOutputStream(new FileOutputStream(dirName
					+ File.separator + fileName2), true, format);
			FortranBinaryOutputStream out3 = new FortranBinaryOutputStream(new FileOutputStream(dirName
					+ File.separator + fileName3), true, format);
			FortranBinaryOutputStream out4 = new FortranBinaryOutputStream(new FileOutputStream(dirName
					+ File.separator + fileName4), true, format);
			FortranBinaryOutputStream topoOut = new FortranBinaryOutputStream(new FileOutputStream(dirName
					+ File.separator + topofileName), true, format);

			// the files are builded one by one to avoid memory problems...
			int cpt = 0;
			for (int k = 0; k < data.nz; k++) {
				for (int j = 0; j < data.ny; j++) {
					for (int i = 0; i < data.nx; i++) {
						cpt++;
						float treesrhof = 0f;
						float firetecVoxelVolume = grid.getVolume(i, j, k);
						// Maximum possible mass in a Firetec voxel, if reached,
						// the voxel is "full"
						treesrhof = data.fuelMass[i][j][k] / firetecVoxelVolume; // kg/m3
						out1.writeReal(treesrhof);

						if (treesrhof < 0d)
							System.out.println("i,j=" + i + "," + j + ";rhof=" + treesrhof);
					}
				}
			}
			out1.writeRecord();
			out1.flush();
			out1.close();

			cpt = 0;
			for (int k = 0; k < data.nz; k++) {
				for (int j = 0; j < data.ny; j++) {
					for (int i = 0; i < data.nx; i++) {
						cpt++;
						float sizescale = 0f;
						// if (k < data.nzveg) {
						if (data.fuelSurface[i][j][k] != 0) {
							sizescale = 2 * data.fuelVolume[i][j][k] / data.fuelSurface[i][j][k]; // m
						}
						// }
						out2.writeReal(sizescale);

						if (sizescale < 0d)
							System.out.println("i,j=" + i + "," + j + ",ss=" + sizescale);
					}
				}
			}
			out2.writeRecord();
			out2.flush();
			out2.close();
			cpt = 0;
			for (int k = 0; k < data.nz; k++) {
				for (int j = 0; j < data.ny; j++) {
					for (int i = 0; i < data.nx; i++) {
						cpt++;
						float moisture = 0f;
						// if (k < data.nzveg) {
						if (data.fuelMass[i][j][k] != 0) {
							moisture = data.waterMass[i][j][k] / data.fuelMass[i][j][k]; // fraction
						}
						// }
						out3.writeReal(moisture);

						if (moisture < 0d)
							System.out.println("i,j=" + i + "," + j + ",mc=" + moisture);
					}
				}
			}
			out3.writeRecord();
			out3.flush();
			out3.close();
			cpt = 0;
			for (int k = 0; k < data.nz; k++) {
				for (int j = 0; j < data.ny; j++) {
					for (int i = 0; i < data.nx; i++) {
						cpt++;

						float actualfueldepth = 0f;
						// mass average height: FP and chema may 2012
						if (k == 0 && data.fuelMass[i][j][k] > 0) {
							// After discussions we decided with Rod to use the max,
							// because in terms of drag effect, the litter should not play a role if not alone
							
							//actualfueldepth = data.massAveragedHeight[i][j][k] / data.fuelMass[i][j][k];
							actualfueldepth = data.fuelDepth[i][j][k];
						}
						// } // m
						// if ((k < fm.nz) && (fm.fuelMass[i][j][k] != 0)) {
						// actualfueldepth = fm.effectiveHeight[i][j][k]
						// / fm.fuelMass[i][j][k];
						// } // m
						out4.writeReal(actualfueldepth);

						if (actualfueldepth < 0d)
							// || actualfueldepth > 1.6)
							System.out.println("i,j=" + i + "," + j + "fd=" + actualfueldepth);

					}
				}
			}

			out4.writeRecord();
			out4.flush();
			out4.close();

			// topo
			cpt = 0;
			for (int j = 0; j < data.ny; j++) {
				for (int i = 0; i < data.nx; i++) {
					cpt++;
					float zs = grid.coor[i][j][0].z; // m
					topoOut.writeReal(zs);
				}
			}
			topoOut.writeRecord();
			topoOut.flush();
			topoOut.close();

			// tree crown voxels
			if (this.produceTreeCrownVoxel) {
				// TODO FP: TreeCrownVoxel temporary disconnected
				// FiretecTreeLoaderWriter wr = new FiretecTreeLoaderWriter();
				// wr.createRecordSet(fm);
				// wr.save(dirName + File.separator + treeCrownFileName);
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiretecMatrixWriter.save ()", "Exception", e);
			throw e;

		}
	}

	public static void main(String[] args) {

		String userDir = Settings.getProperty("user.dir", (String) null);
		System.out.println("user.dir=" + userDir);

		try {
			new FiretecWriter(new File(userDir), "baseFileName", "topofile", false, null, "86", null).save();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
