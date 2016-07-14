/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 1999-2010 INRA
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
package capsis.app;

import java.awt.HeadlessException;
import java.awt.SplashScreen;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.commongui.util.Tools;
import capsis.kernel.Engine;
import capsis.kernel.Options;
import capsis.kernel.PathManager;

/**
 * Capsis Starter. To launch Capsis: new Starter (commandLineArgs).start ();
 * 
 * @author F. de Coligny - september 2001
 */
public class Starter {

	static public final String CAPSIS_VERSION = "4.2.4"; // fc-15.10.2012 changed
															// version (graphs/colors)
	static private boolean wasCalled;

	/**
	 * Deals with main options, then creates the capsis.kernel.Engine. start ()
	 * must be called just afterward.
	 */
	public Starter(String[] args) {
		if (Starter.wasCalled) {
			return;
		} // can be called only once (could
			// happen in script mode)
		Starter.wasCalled = true;

		try {

			// fc-15.9.2014
			detectOsArchMem();

			// This line should be removed, only here for profiling tests -
			// fc-18.4.2011
			// PathManager.setInstallDir ("/home/coligny/workspace/capsis4");

			// Options and properties files
			String optionsFileName = PathManager.getDir("etc") + "/capsis.options";
			String propertiesFileName = PathManager.getDir("etc") + "/capsis.properties";

			// Options inits the Log and loads the properties files
			Options options = new Options("Capsis", CAPSIS_VERSION, optionsFileName, propertiesFileName, args);

			// Encoding test
			Charset charset = Charset.defaultCharset();
			Log.println("Default encoding: " + charset + " (Aliases: " + charset.aliases() + ")");

			printShortNotice();

			String pilotname = options.getPilotName();
			String[] pilotArguments = options.getPilotArguments();

			// Models file
			String modelsFileName = PathManager.getDir("etc") + "/capsis.models";

			// Extension manager
			ExtensionManager extMan = CapsisExtensionManager.getInstance();
			Log.println(extMan.toString());

			// Creates the application Engine
			// "Capsis" is the application name, "capsis" is the package name
			// where
			// the pilots can be found, e.g. for a Pilot named "gui" ->
			// capsis.gui.Pilot
			// Engine creates the ModelManager and the Translator
			new Engine("Capsis", "capsis", pilotname, pilotArguments,
			/*
			 * optionsFileName, propertiesFileName,
			 */
			modelsFileName, extMan, CAPSIS_VERSION);
			System.out.println("Working dir: " + PathManager.getInstallDir());

			// Searches for Extensions
			if (options.isSearchExtension()) {
				List<String> packages = new ArrayList<String>();
				packages.add("capsis");
				packages.addAll(Engine.getInstance().getModelManager().getPackageNames());
				String filename = CapsisExtensionManager.getExtensionListFileName();
				extMan.findNewExtensions(packages, filename);
				System.exit(0);
			}

			// Try to close splash screen
			try {
				SplashScreen sc = SplashScreen.getSplashScreen();
				if (sc != null) {
					sc.close();
				}
			} catch (HeadlessException e) {
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "Starter.c ()", "Exception in Starter", e);
			System.out.println("Starter: could not start, details may be found in var/capsis.log");
			System.exit(1); // stop
		}
	}

	/**
	 * To be called after the constructor, starts the pilot selected in the app
	 * launch process.
	 */
	public void start() {

		try {
			Engine.getInstance().getPilot().start();

		} catch (Exception e) {
			Log.println(Log.ERROR, "Starter.start ()", "Exception in Starter", e);
			System.out.println("Starter.start (): could not start, see var/capsis.log");
			System.exit(2); // stop
		}
	}

	/**
	 * Prints a short notice about the app, authors and licence in the terminal.
	 */
	private void printShortNotice() {
		System.out.println("Capsis " + CAPSIS_VERSION
				+ ", (c) 2000-2015 F. de Coligny, S. Dufour-Kowalski (INRA-AMAP) and the Capsis modellers");
		System.out.println("Capsis comes with ABSOLUTELY NO WARRANTY");
		System.out.println("The core of the Capsis platform (packages capsis.*) is free software ");
		System.out.println("and you are welcome to redistribute it under certain conditions. ");
		System.out.println("Some components in other packages may not be free. See licence files.");
		System.out.println();
	}

	/**
	 * Capsis main entry point.
	 */
	public static void main(String[] args) {

		Starter s = new Starter(args);
		s.start();

	}

	/**
	 * Detection of the os, java data model architecture and max memory
	 * available.
	 */
	static public void detectOsArchMem() { // fc-17.9.2014

		// Detect java architecture (32 or 64 bits)
		int javaArch = AmapTools.getJavaArchitecture();

		String os = "unknown";
		if (AmapTools.isWindowsPlatform())
			os = "windows";
		if (AmapTools.isMacPlatform())
			os = "mac";
		if (AmapTools.isLinuxPlatform())
			os = "linux";

		String arch = "unknown";
		if (javaArch == 32)
			arch = "32";
		if (javaArch == 64)
			arch = "64";

		// Detect -Xmx value passed to th JVM
		String maxMem = AmapTools.getJVMParam("-Xmx");

		System.out.println("-> OS/JVM/memory: " + os + "/" + arch + "/" + maxMem);

	}

}
