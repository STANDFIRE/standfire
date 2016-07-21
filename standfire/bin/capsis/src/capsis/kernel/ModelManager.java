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
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

/**
 * Manages models. Adapted for models outside Capsis. This class is a almost a
 * singleton: the constructor must be called once and only once, then
 * getInstance () returns the ModelManager reference.
 * 
 * @author S. Dufour, F. de Coligny - december 2008, september 2010
 */
public class ModelManager {

	/**
	 * Almost a Singleton pattern. The constructor must be called once (and only
	 * once). Then (and only then), the instance can be retrieved with
	 * ModelManager.getInstance ().
	 */
	private static ModelManager instance;
	/** The name of the file containing the list of available models */
	private String modelsFileName;
	/** Model package names, e.g. 'mountain', 'lerfob.abial' */
	private List<String> packageNames;
	private Map<String, String> packageMap;
	/** Model IdCards contain info on each model */
	private List<IdCard> idCards;
	private Map<String, IdCard> idcardMap;

	/**
	 * Used to get an instance of ModelManager. Use the constructor once before
	 * to build the instance.
	 */
	public synchronized static ModelManager getInstance() {
		if (instance == null) {
			throw new Error(
					"Design error: Constructor must be CALLED ONCE before calling ModelManager.getInstance (), aborted.");
		}
		return instance;
	}

	/**
	 * Constructor
	 */
	public ModelManager(String modelsFileName) throws Exception {
		// The constructor can only be run once
		if (instance != null) {
			throw new Error(
					"Design error: ModelManager constructor must be called ONLY ONCE, then use getInstance (), aborted.");
		}
		instance = this;

		this.modelsFileName = modelsFileName;

		// WARNING the following code prevents the labels to be loaded correctly
		// in Simeo (ok in Capsis). This is tricky and postponed
		// Please do nnot uncomment -fc-20.4.2011
		//
		// // Write the known models in the Log
		// StringBuffer b = new StringBuffer
		// ("ModelManager knows "+getPackageNames ().size ()+" models:");
		// b.append ("\n   \'package.name\' / \'Model name\'");
		// for (String packageName : getPackageNames ()) {
		// try {
		// IdCard idCard = getIdCard (packageName);
		// b.append ("\n   "+packageName);
		// b.append (" / "+idCard.getModelName ());
		// } catch (Exception e) {
		// b.append ("\n   "+packageName+" / could not find idcard.properties");
		// }
		// }
		// Log.println (b.toString ());
	}

	/**
	 * Reset all cached data.
	 */
	public void reset() {
		this.packageNames = null;
		this.idcardMap = null;
		this.idCards = null;
		this.packageMap = null;

	}

	/**
	 * Returns the list of available model packages.
	 */
	synchronized public List<String> getPackageNames() {

		if (this.packageNames != null) {
			return packageNames;
		}

		this.packageNames = new LinkedList<String>();

		// Read the models file
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(modelsFileName));

		} catch (Exception e) {
			Log.println(Log.ERROR, "ModelManager.getPackageNames ()", "Could not load models file: " + modelsFileName,
					e);
			throw new Error("ModelManager: aborted (see Log), could not load models file: " + modelsFileName);
		}

		// Parse each entry of the models file
		for (Entry<Object, Object> pair : properties.entrySet()) {
			String v = (String) pair.getValue();

			if (v.equalsIgnoreCase("true")) {
				String k = (String) pair.getKey();

				this.packageNames.add(k);
			}
		}

		return this.packageNames;
	}

	/**
	 * Creates a Collection containing the identity cards of the models by
	 * dynamic instanciation of the classes <packageName>.IdCard
	 */
	synchronized private Collection<IdCard> getIdCards() {

		IdCard card = null;
		if (this.idCards != null) {
			return idCards;
		} // buffered

		this.idCards = new ArrayList<IdCard>();
		Collection<String> packages = this.getPackageNames();

		// Load IdCard for each module
		for (String pkg : packages) {

//			boolean trace = pkg.contains("fagacees"); // fc-TRACE

			try {
				pkg = pkg.replaceAll("\\.", "/");

//				if (trace)
//					System.out.println("ModelManager getIdCards: pkg: " + pkg);

				// fc-14.10.2014 TRYING TO FIX a Windows network drive UNC
				// protocol error / Xavier Morin / CEFE cluster
				// URL f = getClass().getClassLoader().getResource (pkg +
				// "/idcard.properties");
				//
				// if (trace)
				// System.out.println("ModelManager getIdCards: URL: "+f);
				//
				// File idFile;
				// try {
				// idFile = new File (f.toURI ());
				//
				// if (trace)
				// System.out.println("ModelManager getIdCards: f.toURI () idFile: "+idFile);
				//
				// } catch (URISyntaxException e) {
				// idFile = new File (f.getPath ());
				//
				// if (trace)
				// System.out.println("ModelManager getIdCards: f.getPath () () idFile: "+idFile);
				//
				// }
				// fc-14.10.2014 TRYING TO FIX a Windows network drive UNC
				// protocol error / Xavier Morin / CEFE cluster

				// PROPOSED this new line to replace the block above
				// SAW that in = new BufferedReader(new FileReader(fileName));
				// works with relative fileName: data/forceps/raul/cmd1.txt

				URL f = getClass().getClassLoader().getResource(pkg + "/idcard.properties"); // fc-24.11.2014
				String buildDir = "bin/"; // fc-24.11.2014 bin/ in AMAPstudio
				if (f.toString().contains("class/")) // fc-24.11.2014
					buildDir = "class/"; // fc-24.11.2014 class/ in Capsis

				String idFile = buildDir + pkg + "/idcard.properties"; // fc-24.11.2014
				// String idFile = "class/" + pkg + "/idcard.properties"; //
				// fc-14.10.2014

//				if (trace)
//					System.out.println("ModelManager getIdCards: idFile: " + idFile);

				// fc-14.10.2014 TRYING TO FIX a Windows network drive UNC
				// protocol error / Xavier Morin / CEFE cluster

				Properties properties = new Properties();
				properties.load(new FileInputStream(idFile));
				card = new IdCard(properties);

			} catch (Exception e) {
				Log.println(Log.WARNING, "ModelManager.getIdCards ()", "Could not load idcard.properties for " + pkg, e);
				card = null;

			}

			if (card != null) {
				// Language bundle (internationalization)
				// Here, we load early the internationalized label files for
				// the modules. In case of trouble during new project or
				// open project, error labels will be translated.
				String bundleBaseName = card.getModelBundleBaseName();
				Translator.addBundle(bundleBaseName);
				idCards.add(card);
			}

		}

		return idCards;
	}

	/**
	 * Builds idCard and package maps.
	 */
	private void initMaps() {

		this.idcardMap = new HashMap<String, IdCard>();
		this.packageMap = new HashMap<String, String>();

		for (IdCard card : this.getIdCards()) {
			this.idcardMap.put(card.getModelPackageName(), card);
			this.packageMap.put(card.getModelName(), card.getModelPackageName());
		}
	}

	/**
	 * Returns the idCard map.
	 */
	synchronized private Map<String, IdCard> getIdCardMap() {

		if (this.idcardMap == null) {
			this.initMaps();
		}
		return this.idcardMap;
	}

	/**
	 * Returns the package map.
	 */
	synchronized private Map<String, String> getPackageMap() {

		if (this.packageMap == null) {
			this.initMaps();
		}
		return this.packageMap;
	}

	/**
	 * Returns an IdCard given a packageName.
	 */
	public IdCard getIdCard(String pkgName) throws Exception {

		Map<String, IdCard> map = getIdCardMap();
		IdCard ret = map.get(pkgName);
		if (ret == null) {
			throw new Exception("Unknown package: " + pkgName);
		}

		return ret;

	}

	/**
	 * Returns a packageName given a modelName.
	 */
	public String getPackageName(String modelName) throws Exception {

		Map<String, String> map = getPackageMap();
		String ret = map.get(modelName);

		if (ret == null) {
			throw new Exception("Unknown package : " + modelName);
		}

		return ret;
	}

	/**
	 * Creates a list with the model names to be presented to the user in a GUI
	 * list widget.
	 */
	public Collection<String> getModelNames() {

		Collection<IdCard> idCards = getIdCards();
		ArrayList<String> modelNames = new ArrayList<String>();

		for (IdCard card : idCards) {
			modelNames.add(card.getModelName());
		}

		return modelNames;
	}

	/**
	 * Loads the 'Model class' of a module given its package name (e.g.
	 * 'mountain').
	 */
	public GModel loadModel(String pkgName) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		GModel model = null;

		try {
			IdCard card = this.getIdCardMap().get(pkgName);
			String className = card.modelMainClassName;

			// Class<?> c = urlclassloader.loadClass (className);
			Engine.initGroovy();
			Class<?> c = Engine.loadClass(className);

			// Instantiation using the default constructor
			model = (GModel) c.newInstance();

			// Add the IdCard
			model.init(card);

			Log.println("Model [" + className + "] was correctly loaded");

		} catch (Exception e) {
			Log.println(Log.ERROR, "ModelManager.loadModel ()", "Model [" + pkgName + "]: load failed", e);
			// Returns null
		}

		return model;
	}

	/**
	 * Creates the relay object for the given model and the current pilot. The
	 * relay is built from conventions. The relay class is
	 * <modelPackage>.<pilotName>.<modelPrefix>Relay. This method is used at the
	 * end of the model loading process and when a project is re-opened (the
	 * project was saved without its relay classes).
	 */
	public Relay createRelay(GModel model, String pilotName, AbstractPilot pilot) throws Exception {
		// Convert pilotName (?)
		if (!pilotName.equals("gui")) {
			pilotName = "script";
		}

		// Try to load the Relay
		String modelPackage = model.getIdCard().getModelPackageName();
		String modelPrefix = model.getIdCard().getModelPrefix();

		String className = modelPackage + "." + pilotName + "." + modelPrefix + "Relay"; // relays
																							// names
																							// are
																							// normalized

		Relay relay = null;
		try {
			Class<?> c = getClass().getClassLoader().loadClass(className);
			Constructor<?> ctr = c.getConstructor(new Class[] { GModel.class });
			relay = (Relay) ctr.newInstance(new Object[] { model });

		} catch (ClassNotFoundException e) {

			// Load default relay
			relay = pilot.getDefaultRelay(model);

		} catch (Throwable e) {

			Log.println(Log.ERROR, "ModelManager.createRelay ()", "Error while creating relay: " + className, e);
			throw new Exception("Error while creating relay: " + className, e);
		}

		return relay;
	}

}
