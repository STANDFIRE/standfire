/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */
package capsis.app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jeeb.lib.defaulttype.Extension;
import jeeb.lib.defaulttype.ObjectViewer;
import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.sketch.kernel.SketchExtensionManager;
import jeeb.lib.util.Log;
import jeeb.lib.util.ParamMap;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.extensiontype.DataExtractor;
import capsis.extensiontype.DataRenderer;
import capsis.extensiontype.EconomicFunction;
import capsis.extensiontype.ExtractorGroup;
import capsis.extensiontype.Filter;
import capsis.extensiontype.GenericTool;
import capsis.extensiontype.GrouperDisplay;
import capsis.extensiontype.Lollypop;
import capsis.extensiontype.ModelTool;
import capsis.extensiontype.Spatializer;
import capsis.extensiontype.StandViewer;
import capsis.extensiontype.TreeLoggerImpl;
import capsis.extensiontype.WorkingProcess;
import capsis.kernel.PathManager;
import capsis.kernel.extensiontype.IFormat;
import capsis.kernel.extensiontype.Intervener;
import capsis.kernel.extensiontype.Memorizer;
import capsis.kernel.extensiontype.OFormat;

/**
 * The Capsis extension Manager
 * 
 * @author S. Dufour - february 2010
 */
public class CapsisExtensionManager extends ExtensionManager {

	public static final String EXTENSION_LIST_FILE_NAME = "extension.list";

	// fc-26.6.2015 try to disable non existing settings restoration (wrong
	// Serialization)
	public static final String EXTENSION_SETTINGS_FILE_NAME = "extension.settings";
	// public static final String EXTENSION_SETTINGS_FILE_NAME = "";

	// The extension types managed by this extension manager
	public static final String UNKNOWN = "Unknown";
	public static final String GENERIC_TOOL = PaleoExtension.GENERIC_TOOL;
	public static final String OBJECT_VIEWER = PaleoExtension.OBJECT_VIEWER;

	public static final String STAND_VIEWER = "StandViewer";
	public static final String DATA_EXTRACTOR = "DataExtractor";
	public static final String DATA_RENDERER = "DataRenderer";

	public static final String MODEL_TOOL = "ModelTool";
	public static final String FILTER = "Filter";
	public static final String INTERVENER = "Intervener";
	public static final String IO_FORMAT = "IOFormat";

	public static final String ECONOMIC_FUNCTION = "EconomicFunction";
	public static final String MEMORIZER = "Memorizer";
	public static final String GROUPER_DISPLAY = "GrouperDisplay";
	public static final String LOLLYPOP = "Lollypop";
	public static final String SPATIALIZER = "Spatializer";
	public static final String WORKING_PROCESS = "WorkingProcess";
	public static final String TREELOGGER = "TreeLogger";

	public static final String EXTRACTOR_GROUP = "ExtractorGroup";

	protected static boolean initOk = false;

	/**
	 * Singleton pattern: CapsisExtensionManager e =
	 * CapsisExtensionManager.getInstance ()
	 */
	static synchronized public ExtensionManager getInstance() {
		if (instance == null) {
			instance = new CapsisExtensionManager();
		}
		return instance;
	}

	/**
	 * Constructor
	 */
	protected CapsisExtensionManager() {
		super(PathManager.getDir("etc") + "/" + EXTENSION_SETTINGS_FILE_NAME);
		init(this);
	}

	/**
	 * Inits the extension manager
	 */
	static public void init(ExtensionManager e) {

		// init() must be executed only once
		if (initOk) {
			return;
		}
		initOk = true;

		// Add the Sketch extensions
		SketchExtensionManager.init(e);

		// Declare the Capsis extension types
		initTypes(e);

		// Load the Capsis extensions
		String extFile = getExtensionListFileName();
		try {
			e.readExtensionListFile(extFile);
		} catch (Exception ex) {
			// Too early, the ModelManager was not yet created
			// e.findNewExtensions (ModelManager.getInstance ().getPackageNames
			// (), extFile);
		}

	}

	/**
	 * Returns the extension list file name
	 */
	public static String getExtensionListFileName() {
		return PathManager.getDir("etc") + "/" + EXTENSION_LIST_FILE_NAME;
	}

	/**
	 * Extensions will be searched in these packages.
	 */
	public List<String> getAppMainPackages() {
		List<String> list = new ArrayList<String>();
		list.add("capsis");
		return list;
	}

	/**
	 * Declaration of the extension types
	 */
	public static void initTypes(ExtensionManager em) {

		em.declareType(CapsisExtensionManager.GENERIC_TOOL, GenericTool.class);

		em.declareType(CapsisExtensionManager.DATA_EXTRACTOR, DataExtractor.class);
		em.declareType(CapsisExtensionManager.IO_FORMAT, IFormat.class);
		em.declareType(CapsisExtensionManager.IO_FORMAT, OFormat.class);

		em.declareType(CapsisExtensionManager.STAND_VIEWER, StandViewer.class);
		em.declareType(CapsisExtensionManager.DATA_RENDERER, DataRenderer.class);
		em.declareType(CapsisExtensionManager.INTERVENER, Intervener.class);

		em.declareType(CapsisExtensionManager.MODEL_TOOL, ModelTool.class);
		em.declareType(CapsisExtensionManager.FILTER, Filter.class);
		em.declareType(CapsisExtensionManager.TREELOGGER, TreeLoggerImpl.class);

		em.declareType(CapsisExtensionManager.ECONOMIC_FUNCTION, EconomicFunction.class);
		em.declareType(CapsisExtensionManager.MEMORIZER, Memorizer.class);

		em.declareType(CapsisExtensionManager.LOLLYPOP, Lollypop.class);
		em.declareType(CapsisExtensionManager.SPATIALIZER, Spatializer.class);
		em.declareType(CapsisExtensionManager.WORKING_PROCESS, WorkingProcess.class);

		em.declareType(CapsisExtensionManager.OBJECT_VIEWER, ObjectViewer.class);

		em.declareType(CapsisExtensionManager.GROUPER_DISPLAY, GrouperDisplay.class);

		em.declareType(CapsisExtensionManager.EXTRACTOR_GROUP, ExtractorGroup.class); // fc-23.9.2013

		em.declareType(PaleoExtension.class.getSimpleName(), null);

	}

	/**
	 * Returns true if the extension with the given className is an export
	 * extension
	 */
	static public boolean isExport(String className) {
		try {
			Class<?> c = getClass(className);
			Class<?> oformat = OFormat.class;
			// if not an OFormat -> return false
			if (!oformat.isAssignableFrom(c)) {
				return false;
			}

			// Try old method
			Method m = c.getMethod("isExport", (Class<?>[]) null);
			if (m != null) {
				Extension e = getExtensionPrototype(className);
				Boolean b = (Boolean) m.invoke(e, (Object[]) null);
				return b;
			} else {
				return true;
			}

		} catch (Exception e) {
			Log.println(Log.WARNING, "isExport()", "error : ", e);
			return true;
		}
	}

	/**
	 * Returns true if the extension with the given className is an import
	 * extension
	 */
	static public boolean isImport(String className) {
		try {
			Class<?> c = getClass(className);
			Class<?> iformat = IFormat.class;
			// if not an iFormat -> return false
			if (!iformat.isAssignableFrom(c)) {
				return false;
			}

			// Try old method
			Method m = c.getMethod("isImport", (Class<?>[]) null);
			if (m != null) {
				Extension e = getExtensionPrototype(className);
				Boolean b = (Boolean) m.invoke(e, (Object[]) null);
				return b;
			} else {
				return true;
			}

		} catch (Exception e) {
			Log.println(Log.WARNING, "isExport()", "error : ", e);
			return true;
		}
	}

	/**
	 * toString method for CapsisExtensionManager
	 */
	public String toString() {

		Set<String> extensions = getEnabledExtensions();
		SortedSet<String> sorted = new TreeSet<String>(extensions);

		StringBuffer b = new StringBuffer();
		b.append("CapsisExtensionManager knows ");
		b.append(sorted.size() + " active extensions:\n");
		for (String e : sorted) {
			b.append("   " + e + "\n");
		}
		return b.toString();

	}

}
