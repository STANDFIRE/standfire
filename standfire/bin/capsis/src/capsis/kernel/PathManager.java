/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.kernel;

import java.io.File;
import java.util.StringTokenizer;

import jeeb.lib.util.Log;

/**
 * PathManager can find the installDir. It can return directories based on the
 * installDir.
 * 
 * @author F. de Coligny - september 2010
 */
public class PathManager {

	/** The application install directory */
	private static String installDir;

	/**
	 * Optional method, in case getInstallDir () does not return the good path.
	 * If so, use this method to set the install directory in a custom way.
	 */
	public static void setInstallDir(String absolutePath) {
		PathManager.installDir = absolutePath;
		if (!new File(absolutePath).exists()) {
			Log.println(Log.ERROR, "PathManager.setInstallDir ()", "Path does not exist: " + absolutePath);
		}
	}

	/**
	 * This method returns the install directory where the app is installed It
	 * can be called at any time. e.g.
	 * capsis4/bin/capsis/kernel/PathManager.class -> installDir = capsis4/ In
	 * case of wrong path returned, see optional setInstallDir ().
	 */
	public static String getInstallDir() {
		if (PathManager.installDir == null) {

			File f = null;
			
			try {
				// The bin/ directory, containing the java packages
				String binDir = PathManager.class.getResource("/").getPath();
	
				// Bug (detected by O. Taugourdeau, sep 2010) related to
				// D:\Mes%20Donnes\... %20 is a blank
				// We need to do that trick to get a correct binPath
				if (binDir.contains("%20")) {
					binDir = binDir.replaceAll("%20", " ");
				}
				if (!new File(binDir).exists()) {
					Log.println(Log.ERROR, "PathManager.getInstallDir ()", "PathManager error, binDir does not exist: "
							+ binDir);
				}
				
				// The parent directory is supposed to be the install directory
				// (otherwise, see optional setInstallDir ())
				f = new File(binDir).getParentFile();
			
			} catch (Exception e) {
			
				try {
					// fc-16.11.2015 added this line in case this class is in a jar file: returns the jar path
					PathManager pm = new PathManager ();
					f = new File (pm.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
					
					// The parent directory is supposed to be the install directory
					f = f.getParentFile();
					
				} catch (Exception e2) {
					throw new RuntimeException ("PathManager could not find installDir", e);
				}
				
			}
			
			PathManager.installDir = f.getPath();

			if (!new File(PathManager.installDir).exists()) {
				Log.println(Log.ERROR, "PathManager.getInstallDir ()",
						"PathManager error, install dir does not exist: " + PathManager.installDir);
			}

		}
		return PathManager.installDir;

	}

	/**
	 * Returns a directory based on the install dir e.g. : getDir("var") ->
	 * "installDir/var"
	 */
	static public String getDir(String name) {
		return getInstallDir() + File.separator + name;
	}

	// Windows network drive error, X. Morin 14.10.2014
	public static final void main (String[] args) {
		
		test ("//SERVER/some/path");
		test ("\\\\10.8.4.252\\shared\\Morinx\\Capsis");
		
		// problematic file path: \\10.8.4.252\shared\Morinx\Capsis
		
	}

	static private void test(String path) {
		System.out.println("PathManager testing path: "+path);
		
		File f = new File(path);
		System.out.println("  f.toString ():          "+f.toString ());
		System.out.println("  f.toURI ().toString (): "+f.toURI ().toString ());
		
	}

}
