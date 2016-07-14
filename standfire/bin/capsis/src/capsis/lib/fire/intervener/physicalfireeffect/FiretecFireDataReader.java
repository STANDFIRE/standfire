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
package capsis.lib.fire.intervener.physicalfireeffect;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import capsis.util.FortranBinaryInputStream;

/**
 * Read a firedata fortran (big endian our small endian)
 * 
 * @author F. Pimont - december 2009
 */
public class FiretecFireDataReader {
	// private String fileName;
	public int nx;
	public int ny;
	public int nz;

	public double[][][] tGas;// temperature (K)
	public double[][][] radFlux; // radiative flux kW/m2
	public double[][][] horizRadFlux; // horizontal radiative flux kW/m2
	public double[][][] velocity; // gas velocity (m/s)
	public double[][][] moistureFraction; // fraction
	public double fuelMass[][][]; // (kg) total fuelMass in cell for intensity
	// computation around tree

	String format; //NativeBinaryOutputStream.X86=littleEndian    NativeBinaryOutputStream.sparc=bigEndian
	

	public FiretecFireDataReader(int nx, int ny, int nz,
			String format) throws Exception {
		//this.dir = dir;		// the directory where the file must be readed
		//dirName = dir.getAbsolutePath ();
		// this.fileName = fileName;
		this.nx=nx;
		this.ny=ny;
		this.nz = nz;
		this.tGas = new double[nx][ny][nz];
		this.radFlux = new double[nx][ny][nz];
		this.horizRadFlux = new double[nx][ny][nz];
		this.velocity = new double[nx][ny][nz];
		this.moistureFraction = new double[nx][ny][nz];
		this.fuelMass = new double[nx][ny][nz];
		this.format=format;
	}

	// check the file format
	public void check(String fileName) throws Exception {
		// Check file size
		long fileSize = new File (fileName).length () * 8;	// in bits
		// should be 6
		long fileTheoriticalSize = (nx * ny * nz + 2) * 32 * 6; // in bits
		if (fileSize != fileTheoriticalSize) {
			throw new Exception(
					"FiretecFireDataReader error, wrong file size: "
					+fileSize+" bits, expected: "+fileTheoriticalSize+" bits");
		}
	}

	// read the file
	public void read(String fileName) throws Exception {		
		InputStream _in = new FileInputStream (fileName);
		FortranBinaryInputStream in = new FortranBinaryInputStream(_in, true, format);
		in.readRecord();
		// readInteger()
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					tGas[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					radFlux[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					horizRadFlux[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					velocity[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					moistureFraction[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
		for (int k = 0; k < nz; k++) {
			for (int j = 0; j < ny; j++) {
				for (int i = 0; i < nx; i++) {
					fuelMass[i][j][k] = in.readReal();
					if (in.isFinFichier()) {
						throw new Exception(
								"FiretecDataReader error, reached file end too early: "
										+ fileName);
					}
				}
			}
		}
	}


	public static void main (String[] args) {
		//String userDir = Settings.getProperty ("user.dir", null);
		//System.out.println ("user.dir="+userDir);

		try {
			new FiretecFireDataReader(0, 0, 0, "86")
			.read("fileName");
		} catch (Exception e) {
			e.printStackTrace (System.out);
		}
	}	
}

