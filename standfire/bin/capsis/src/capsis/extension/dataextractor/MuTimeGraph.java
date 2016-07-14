/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2011 Francois de Coligny
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
package capsis.extension.dataextractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.projectmanager.ColorManager;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePart;
import capsis.defaulttype.ScenePartMap;
import capsis.defaulttype.SpeciesPropsExplorer;
import capsis.defaulttype.SpeciesPropsOwner;
import capsis.defaulttype.SpeciesPropsOwnerList;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFColoredCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;

/**
 * Evolution of some value over Time in a MultipartScene.
 * 
 * This graph draws -one single value- for each ScenePart (e.g. hdom), with optionally average and
 * total, possibly per hectare.
 * 
 * This class must be subclassed for each needed graph (e.g. basal area over time, dominant height
 * over time...).
 * 
 * @author F. de Coligny - january 2011
 */
public abstract class MuTimeGraph extends PaleoDataExtractor implements DFColoredCurves {

	// fc-7.6.2013 Removed the Simmem specific code related to TB2012 and the group choice
	// Created instead a set of Simmem specific extractors, see SimTimeGraph
	
	static {
		Translator.addBundle ("capsis.extension.dataextractor.MuTimeGraph");
	}
	private List<List<? extends Number>> curves;
	private List<List<String>> labels;

	/**
	 * Default constructor.
	 */
	public MuTimeGraph () {}

	/**
	 * Constructor.
	 */
	public MuTimeGraph (GenericExtensionStarter s) {
		super (s);

		try {
			curves = new ArrayList<List<? extends Number>> ();
			labels = new ArrayList<List<String>> ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "MuTimeGraph.c ()", "Exception in constructor: ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This method checks if the extension can deal (i.e.
	 * is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
			GModel model = (GModel) referent;

			Step root = model.getProject ().getRoot ();
			GScene scene = root.getScene ();

			// MultipartScenes only
			if (!(scene instanceof MultipartScene)) { return false; }

			// Check the parts compatibility
			MultipartScene ms = (MultipartScene) scene;
			for (ScenePart part : ms.getParts ()) {
				if (!matchWithPart (model, (MultipartScene) scene, part)) { return false; }
			}
			return true;

		} catch (Exception e) {
			Log.println (Log.ERROR, "MuTimeGraph.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties

		if (acceptsPerHectare ()) {
			addBooleanProperty ("MuTimeGraph.perHectare", true);
		}
		if (acceptsShowSpecies ()) {
			addBooleanProperty ("MuTimeGraph.showSpecies", false);
		}
		if (acceptsShowParts ()) {
			addBooleanProperty ("MuTimeGraph.showParts", true);
		}
		if (acceptsIncrements ()) {
			addBooleanProperty ("MuTimeGraph.showIncrements", false);
		}
		if (acceptsShowMean ()) {
			addBooleanProperty ("MuTimeGraph.showMean", true);
		}
		if (acceptsShowTotal ()) {
			addBooleanProperty ("MuTimeGraph.showTotal", true);
		}

	}

	/**
	 * From DataExtractor SuperClass.
	 * 
	 * Computes the data series. This is the real output building. It needs a particular Step.
	 * 
	 * Returns false if trouble while extracting.
	 */
	public boolean doExtraction () {

		if (upToDate) { return true; }
		if (step == null) { return false; }

		try {

			// Retrieve Steps from root to this step
			Vector<Step> steps = step.getProject ().getStepsFromRoot (step);

			GModel model = step.getProject ().getModel ();

			MultipartScene scene0 = (MultipartScene) step.getScene ();
			int nParts = scene0.getParts ().size ();

			ScenePartMap partMap = new ScenePartMap (scene0); // name -> id

			Vector xs = new Vector (); // x coordinates
			Vector y1 = new Vector (); // MuTimeGraph.showTotal
			Vector y2 = new Vector (); // MuTimeGraph.showMean
			Vector[] ys = new Vector[nParts]; // MuTimeGraph.showParts
			for (int i = 0; i < nParts; i++) {
				ys[i] = new Vector ();
			}

			// System.out.println("MuTimeGraph doExtraction ()...");
			// Species level (optional)
			Vector[] sps = null;
			SpeciesPropsExplorer speciesExplorer = null;
			if (isSet ("MuTimeGraph.showSpecies")) {
				speciesExplorer = new SpeciesPropsExplorer ((SpeciesPropsOwnerList) scene0);
				int speciesN = speciesExplorer.getSpeciesNames ().size ();
				// System.out.println("MuTimeGraph showSpecies: true speciesN: "+speciesN);
				sps = new Vector[speciesN];
				for (int i = 0; i < speciesN; i++) { // prepare vectors
					sps[i] = new Vector ();
				}

			}

			curves.clear ();
			labels.clear ();

			double[] prevValue = new double[nParts]; // for increments
			// For species level
			Map<String,Double> speciesValues = new HashMap<String,Double> (); // map for the current
																				// step
			Map<String,Double> speciesAreas = new HashMap<String,Double> (); // area per species

			for (Step step : steps) {

				// MultipartScenes only
				MultipartScene scene = (MultipartScene) step.getScene ();

				xs.add (scene.getDate ());

				double cumValue = 0d;
				double sceneArea = 0d;

				// Prepare species level
				speciesValues.clear ();
				speciesAreas.clear ();

				// Loop on parts
				for (ScenePart part : scene.getParts ()) {
					int partId = partMap.getId (part.getName ());

					// per Ha computation
					double k0 = 1;
					if (isSet ("MuTimeGraph.perHectare")) {
						k0 = 10000d / part.getArea_m2 ();
					}

					double value = getValue (model, scene, part);

					sceneArea += part.getArea_m2 ();

					double v = isSet ("MuTimeGraph.showIncrements") ? value - prevValue[partId] : value;

					cumValue += v;
					v *= k0;

					if (isSet ("MuTimeGraph.showParts")) {
						int i = partMap.getId (part.getName ());
						ys[i].add (v);
					}

					prevValue[partId] = value;

					// Species level
					if (isSet ("MuTimeGraph.showSpecies")) {

						SpeciesPropsOwner owner = (SpeciesPropsOwner) part;
						for (String speciesName : speciesExplorer.getSpeciesNames ()) {

							String prop = getShowSpeciesProp (); // e.g. "BasalArea", asked to
																	// subclass

							double spValue = owner.getValue (speciesName, prop); // k0: for hectare
							if (spValue == 0) {
								continue; // no value, jump to next speciesName
							}
							Double V = speciesValues.get (speciesName); // already a value for this
																		// species ?
							if (V == null) {
								speciesValues.put (speciesName, spValue); // no: memo
								speciesAreas.put (speciesName, part.getArea_m2 ());

							} else {
								speciesValues.put (speciesName, V.doubleValue () + spValue); // yes:
																								// sum
								// Cumulate parts areas per species for hectare calculation below
								Double A = speciesAreas.get (speciesName);
								speciesAreas.put (speciesName, A.doubleValue () + part.getArea_m2 ());
							}
						}

					}

				} // end of scene parts loop

				// per Ha computation
				double k1 = 1;
				if (isSet ("MuTimeGraph.perHectare")) {
					k1 = 10000d / sceneArea;
				}

				if (isSet ("MuTimeGraph.showTotal")) {
					y1.add (cumValue * k1);
				}

				if (isSet ("MuTimeGraph.showMean")) {
					y2.add (cumValue * k1 / nParts);
				}

				// Species level
				if (isSet ("MuTimeGraph.showSpecies")) {
					for (String speciesName : speciesValues.keySet ()) {
						int k = speciesExplorer.getSpeciesId (speciesName);

						double k2 = 1d;
						if (isSet ("MuTimeGraph.perHectare")) {
							k2 = 10000d / speciesAreas.get (speciesName);
						}

						Double V = speciesValues.get (speciesName);
						sps[k].add (V != null ? V * k2 : 0);
					}

					// TRACE
					// for (int i = 0; i < sps.length; i++) {
					// System.out.println("   i: "+i+" sps[i]: "+sps[i]);
					// }

				}

			} // end of steps loop

			curves.add (xs);
			labels.add (Collections.EMPTY_LIST); // no labels for xs

			if (isSet ("MuTimeGraph.showTotal")) {
				curves.add (y1);
				labels.add (Collections.singletonList (Translator.swap ("MuTimeGraph.total")));

			}

			if (isSet ("MuTimeGraph.showMean")) {
				curves.add (y2);
				labels.add (Collections.singletonList (Translator.swap ("MuTimeGraph.mean")));
			}

			if (isSet ("MuTimeGraph.showParts")) {
				for (int i = 0; i < nParts; i++) {
					curves.add (ys[i]);
					labels.add (Collections.singletonList (partMap.getName (i)));
				}
			}

			// If increments, remove the first step values (except if one single step)
			if (isSet ("MuTimeGraph.showIncrements") && steps.size () > 1) {
				for (List<? extends Number> l : curves) {
					l.remove (0);
				}
			}

			if (isSet ("MuTimeGraph.showSpecies")) {
				for (int i = 0; i < sps.length; i++) {
					curves.add (sps[i]);
					labels.add (Collections.singletonList (speciesExplorer.getSpeciesName (i)));
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "MuTimeGraph.doExtraction ()", "Exception : ", e);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * Extension name.
	 */
	public String getName () {
		return getNamePrefix () + getGraphName ();
	}

	/**
	 * Graph X axis name (should be translated by Translator.swap ()).
	 */
	public String getXAxisName () {
		return Translator.swap ("MuTimeGraph.xLabel");
	}

	/**
	 * DFCurves
	 */
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**
	 * DFCurves
	 */
	public List<List<String>> getLabels () {
		return labels;
	}

	/**
	 * DFCurves
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (getXAxisName ());
		String yLab;
		yLab = getYAxisName ();
		if (isSet ("MuTimeGraph.perHectare")) {
			yLab = yLab + " (ha)";
		}
		if (isSet ("MuTimeGraph.showIncrements")) {
			yLab = yLab + "(" + Translator.swap ("MuTimeGraph.increments") + ")";
		}
		v.add (yLab);
		return v;
	}

	/**
	 * DFCurves
	 */
	public int getNY () {
		return curves == null ? 0 : curves.size () - 1;
	}

	/**
	 * DFClloredCurves: returns a color per curve: getCurves ().size () - 1.
	 */
	public Vector getColors () {
		Vector v = new Vector ();
		float n = getNY () + 1; // try to avoid blank
		float hue = 0.75f;
		for (int i = 1; i <= getNY (); i++) {
			float gradient = i / n;
			Color c = ColorManager.getColor (hue, gradient);
			v.add (c);
		}
		return v;
	}

	// -------------------
	/**
	 * Returns true if the 'hectare' option should be made available for this tool.
	 */
	abstract public boolean acceptsPerHectare ();

	/**
	 * Returns true if the 'show total' option should be made available for this tool.
	 */
	abstract public boolean acceptsShowTotal ();

	/**
	 * Returns true if the 'show mean' option should be made available for this tool.
	 */
	abstract public boolean acceptsShowMean ();

	/**
	 * Returns true if the 'show parts' option should be made available for this tool.
	 */
	abstract public boolean acceptsShowParts ();

	/**
	 * Returns true if the 'show increments' option should be made available for this tool.
	 */
	abstract public boolean acceptsIncrements ();

	/**
	 * Returns true if the 'show species' option should be made available for this tool. Subclasses
	 * may redefine getShowSpeciesProp () to become compatible.
	 */
	public boolean acceptsShowSpecies () {

		// Returns true if the MultiPart Scene implements SpeciesPropsOwnerList
		// and if the available properties contain "BasalArea"

		// fc-18.1.2012 added species level curves
		Step st = getStep ();
		GScene sc = st.getScene ();
		if (!(sc instanceof SpeciesPropsOwnerList)) { return false; }

		if (getShowSpeciesProp () == null) { return false; }

		SpeciesPropsOwnerList l = (SpeciesPropsOwnerList) sc;
		SpeciesPropsExplorer explorer = new SpeciesPropsExplorer (l);

		return explorer.getProps ().contains (getShowSpeciesProp ()); // getShowSpeciesProp (), e.g.
																		// "BasalArea"

	}

	/**
	 * Returns the name of the species prop this extractor is for. E.g. "BasalArea".
	 */
	protected String getShowSpeciesProp () {
		return null;
	} // see acceptsShowSpecies ()

	/**
	 * Returns true if the given Scene part can be accepted by this tool (e.g. if we draw basal area
	 * over time, the part should provide a way to get the basal area...).
	 */
	abstract public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part);

	/**
	 * Returns the value that must appear in the graph for the given ScenePart (e.g. basal area,
	 * dominant height...).
	 */
	abstract public double getValue (GModel model, MultipartScene scene, ScenePart part);

	/**
	 * Graph name (should be translated by Translator.swap ()).
	 */
	abstract public String getGraphName ();

	/**
	 * Graph Y axis name (should be translated by Translator.swap ()).
	 */
	abstract public String getYAxisName ();

	/**
	 * Extension version.
	 */
	abstract public String getVersion ();

	/**
	 * Extension author.
	 */
	abstract public String getAuthor ();

	/**
	 * Extension description (should be translated by Translator.swap ()).
	 */
	abstract public String getDescription ();
	// -------------------
}
