/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 1999-2014 INRA
 *
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 *
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;

/**
 * This tool class detects which path should be added to the java.library.path
 * system property to run the dlls on this system. It checks which os is running
 * (linux/windows/mac) and which java data model architecture is used (32/64
 * bits). A chosen directory is added at the beginning of the path accordingly.
 * The final path is written in a file in the user directory, named
 * javalibrarypath.
 * 
 * @author F. de Coligny - September 2014
 */
public class JavaLibraryPathDetector {

	/**
	 * Detection of jdk data model 32 or 64 bits, completion of the
	 * java.library.path to find the dynamic libraries like Organon dlls. This
	 * can not be changed after Capsis boot (ClassLoader can not be reset). This
	 * program is run before, it gets the new path and writes the complete
	 * java.library.path in a file for Capsis launch time.
	 */
	public static void main(String[] args) {

		// Detect java architecture (32 or 64 bits)
		int javaArch = AmapTools.getJavaArchitecture();

		// Get user dir
		String userDir = System.getProperty("user.dir");

		String os = "";
		String newPath = "";

		// MEMO fc-13.5.2016
		// Some installers may copy a javalibrarypath file to another machine...
		// -> If trouble with javalibrarypath when trying to connect to a .dll /
		// .so, try to remove the javalibrarypath file and relaunch capsis.bat /
		// capsis.sh to rebuild it

		if (javaArch == 32) { // 32 bits

			if (AmapTools.isWindowsPlatform()) { // Windows
				os = "windows";
				newPath = userDir + "\\ext\\windows";
			} else if (AmapTools.isMacPlatform()) { // Mac
				os = "mac";
				newPath = userDir + "/ext/macosx";
			} else { // supposed to be Linux
				os = "linux";
				newPath = userDir + "/ext/linux";
			}

		} else { // 64 bits

			if (AmapTools.isWindowsPlatform()) { // Windows
				os = "windows";
				newPath = userDir + "\\ext\\windows64";
			} else if (AmapTools.isMacPlatform()) { // Mac
				os = "mac";
				newPath = userDir + "/ext/macosx";
			} else { // supposed to be Linux
				os = "linux";
				newPath = userDir + "/ext/linux64";
			}

		}
		String jlp = newPath + File.pathSeparatorChar + System.getProperty("java.library.path");
		System.setProperty("java.library.path", jlp);

		System.out.println("-> JavaLibraryPathDetector added this path to java.library.path: " + newPath);

		// Write jlp to a file
		String fileName = "javalibrarypath";
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(jlp);
			out.newLine();
			out.close();
		} catch (Exception e) {
			Log.println(Log.ERROR, "JavaLibraryPathDetector.main ()", "Could not write in file: " + fileName, e);
		}

	}

}
