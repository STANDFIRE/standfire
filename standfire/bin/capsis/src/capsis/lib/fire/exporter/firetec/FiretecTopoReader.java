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
import java.io.FileInputStream;
import java.io.InputStream;

import capsis.util.FortranBinaryInputStream;

/**
 * Read the topography in a fortran topofile (big endian our small endian), but
 * other format (text file could be added)
 * 
 * @author F. Pimont - november 2009
 */
public class FiretecTopoReader {
	private String fileName;
	private int nx;
	private int ny;
	private float[][] topo;
	String format; // NativeBinaryOutputStream.X86=littleEndian
					// NativeBinaryOutputStream.sparc=bigEndian

	public FiretecTopoReader(String fileName, int nx, int ny, String format) throws Exception {
		// this.dir = dir; // the directory where the file must be readed
		// dirName = dir.getAbsolutePath ();
		this.fileName = fileName;
		this.nx = nx;
		this.ny = ny;
		this.topo = new float[nx][ny];
		this.format = format;
	}

	// read the file
	public void read() throws Exception {
		// Check file size
		long fileSize = new File(fileName).length() * 8; // in bits
		long fileTheoriticalSize = (nx * ny + 2) * 32; // in bits
		if (fileSize != fileTheoriticalSize) {
			throw new Exception("FiretecTopoReader error, wrong file size: " + fileSize + " bits, expected: "
					+ fileTheoriticalSize + " bits");
		}

		boolean flatTopo = true;
		System.out.println("FiretecTopoReader. read topofile...");
		InputStream _in = new FileInputStream(fileName);
		FortranBinaryInputStream in = new FortranBinaryInputStream(_in, true, format);
		in.readRecord();

		// ~ System.out.println ("FiretecTopoReader theoritical file size: " +
		// (nx * ny * 32) + " bits");
		// ~ File f = new File (fileName);
		// ~ System.out.println ("FiretecTopoReader file length () : " +
		// f.length () + " bytes");
		// ~ System.out.println ("FiretecTopoReader file length () : " +
		// f.length () * 8 + " bits");

		for (int j = 0; j < ny; j++) {
			for (int i = 0; i < nx; i++) {
				topo[i][j] = in.readReal();
				// Once flatTopo is false, it must stay false
				if (flatTopo && topo[i][j] != 0.0) {
					flatTopo = false;
				}
				// System.out.println ("topo[i][j]:"+i+" "+j+" :"+topo[i][j]);
				if (in.isFinFichier()) {
					throw new Exception("FiretecTopoReader error, reached file end too early: " + fileName);
				}
				// if (j==0 & (i % 20 == 0)) {
				// System.out.println("z("+i+","+j+")="+topo[i][j]);
				// }
			}
		}

		if (!flatTopo) {
			System.out.println("	This topo is not flat");
		}
		System.out.println("	Topofile was read correctly");
	}

	public float[][] getTopoMatrix() {
		return topo;
	}

	public static void main(String[] args) {
		// String userDir = Settings.getProperty ("user.dir", null);
		// System.out.println ("user.dir="+userDir);

		try {
			// new FiretecTopoReader (new File (userDir), "fileName", 0, 0).read
			// ();
			new FiretecTopoReader("fileName", 0, 0, "86").read();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
