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

package fireparadox.extension.sketcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;

import jeeb.lib.sketch.extension.Sketcher;
import jeeb.lib.sketch.kernel.SketchEvent;
import jeeb.lib.sketch.scene.item.TreeWithCrownProfileItem;
import jeeb.lib.sketch.util.SmartColor;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSeverity;
import capsis.lib.fire.fuelitem.FiSpecies;

import com.jogamp.common.nio.Buffers;

import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmPlant;
import fireparadox.model.plant.fmgeom.FmGeom;
import fireparadox.model.plant.fmgeom.FmGeomDiameter;

/**
 * FirePatternSketcher: draw FiPlant according FiPattern data in 3D.
 * 
 * @author S. Griffon - june 2007
 */
public class FirePatternSketcher extends Sketcher {

	static {
		Translator.addBundle ("fireparadox.extension.sketcher.FirePatternSketcher");
	}

	public static final int RENDERING_THRESHOLD_1 = 20000; // 150000; // 20000;
	// below RENDERING_THRESHOLD_1: render 1, decreasing number of segment
	public static final int RENDERING_THRESHOLD_2 = 150000; // 800000; //
	// 150000;
	// below RENDERING_THRESHOLD_2: render 2, decreasing number of trees with
	// render 2, the others in render 3
	// above RENDERING_THRESHOLD_2: render 3 for all trees

	private FirePatternSketcherPanel instantPanel;

	public double qualityNote = 1d; // decrease with render and tree fraction in
	// renders, from 1 to 0
	// for render 1 quality varies between 1 and 0.5 depending on the number of
	// segments
	// for render 2 quality varies between 0 and 0.5 depending on the number of
	// trees with 3D render
	// for render 3 quality is 0

	// public boolean improvedRendering; //when user wants a temporary better
	// rendering
	public double forcedQualityNote; // when user wants a temporary better
										// rendering

	private Collection<TreeWithCrownProfileItem> items;

	private int trunk = -1;
	// private int selectionMark=-1;
	// private int scene=-1;
	// private int fastScene=-1;

	// ArrayList array vertex
	private HashMap<FiPlant,DoubleBuffer> verticesBuf = new HashMap<FiPlant,DoubleBuffer> ();
	private HashMap<FiPlant,DoubleBuffer> normalBuf = new HashMap<FiPlant,DoubleBuffer> ();
	private HashMap<FiPlant,IntBuffer> indiceBuf = new HashMap<FiPlant,IntBuffer> ();
	private HashMap<FiPlant,FloatBuffer> colorsBuf = new HashMap<FiPlant,FloatBuffer> ();

	// Sketcher parameters
	protected boolean refreshShapePattern = true; // the geometry shape of the
													// pattern must be
													// recomputed
	protected boolean trunkEnabled = true; // trunk visible or not
	protected boolean crownEnabled = true; // crown visible or not
	protected boolean renderOutline = false; // fast drawing: drawings not
												// Filled
	protected boolean renderFilled = true; // fill the lollypop
	protected boolean renderFilledFlat = false; // fill with flat color
	protected boolean renderFilledLight = true; // fill with light aspect
	protected boolean renderFilledTransparent = false; // fill with transparency
	protected boolean colorSpecies = true; // true : show each specie have a
											// specific color
	protected boolean colorStratum = false; // true : show each stratum have a
											// specific color
	protected boolean colorDamage = false; // true : show fire damage with
											// specific color
	protected boolean colorGlobal = false; // true : show all firetree with the
											// same specific color
	protected int renderAlphaValue = 200; // transparency parameter [0..255]

	protected Color labelColor = new Color (51, 0, 102);
	protected Color trunkColor = Color.BLACK;
	protected Color crownColor = new Color (0, 102, 0);
	protected Color cellColor = Color.GRAY;
	protected Color selectionColor = new Color (207, 74, 7);

	protected TreeMap<String,Color> speciesColor = new TreeMap<String,Color> (); // a
																					// map
																					// of
																					// color
																					// for
																					// species
	protected ArrayList<Color> stratumColor = new ArrayList<Color> (); // a
																		// collection
																		// of
																		// color
																		// for
																		// stratum
	protected double stratumThreshold = 2.0;
	protected ArrayList<Color> fireDamageColor = new ArrayList<Color> ();

	protected Collection<FiSpecies> fireSpeciesList = new ArrayList<FiSpecies> ();
	// Sketcher parameters

	/**
	 * Default constructor.
	 */
	public FirePatternSketcher () {

		// Create the instant panel in any case to avoid errors
		instantPanel = getInstantPanel ();

	}

	/**
	 * Standard constructor.
	 */
	public FirePatternSketcher (Object referent) throws Exception {
		super (referent);
		try {
			// Create the instant panel in any case to avoid errors
			instantPanel = getInstantPanel ();

			restore ();
			reset ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "FirePatternSketcher.c ()", "Exception, ", e);
			throw e;
		}
	}

	@Override
	public void activate () {
		try {
			items = new ArrayList (getItems ());

			// fc - Errors in Log... if items is empty, we can not go further
			if (items == null || items.isEmpty ()) { return; }

			TreeWithCrownProfileItem item = items.iterator ().next ();
			FiPlant ft = (FiPlant) item.getTree ();

			FmStand stand = (FmStand) ft.getScene ();
			FmModel model = stand.getModel ();

			model.getPatternMap ().addActionListeners (this);

			setStandSpeciesList (FmStand.getStandSpeciesList (stand));
			// model.setInitStand (stand);
			// setStandSpeciesList (model.getStandSpeciesList ());

			// Restore a previous configuration if available
			initColors ();

			refreshShapePattern = true;

			if (instantPanel != null) {
				instantPanel.refreshColorTable ();
				instantPanel.repaint ();
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FirePatternSketcher.activate ()", "Exception, ", e);
		}
	}

	/**
	 * This is Extension dynamic compatibility mechanism.
	 * 
	 * This matchwith method must be redefined for each extension subclass. It
	 * must check if the extension can deal (i.e. is compatible) with the
	 * referent.
	 */
	@Override
	public boolean matchWith (Object referent) {

		try {
			if (referent instanceof Collection) {
				Collection c = (Collection) referent;
				if (c.isEmpty ()) { return false; }
				Collection reps = AmapTools.getRepresentatives (c);
				for (Iterator i = reps.iterator (); i.hasNext ();) {
					Object o = i.next ();
					if (!(o instanceof TreeWithCrownProfileItem)) { return false; }
					TreeWithCrownProfileItem item = (TreeWithCrownProfileItem) o;
					if (!(item.getTree () instanceof FiPlant)) { return false; }
				}
				// all TreeWithCrownProfileItem instances with FiPlant inside
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FirePatternSketcher.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**
	 * Restore some previously set options
	 */
	@Override
	public void restore () {
		refreshShapePattern = Settings.getProperty ("FirePatternSketcher.refreshShapePattern", refreshShapePattern); // with
																														// default
																														// value
		trunkEnabled = Settings.getProperty ("FirePatternSketcher.trunkEnabled", trunkEnabled); // with
																								// default
																								// value
		crownEnabled = Settings.getProperty ("FirePatternSketcher.crownEnabled", crownEnabled); // with
																								// default
																								// value
		renderOutline = Settings.getProperty ("FirePatternSketcher.renderOutline", renderOutline); // with
																									// default
																									// value
		renderFilled = Settings.getProperty ("FirePatternSketcher.renderFilled", renderFilled); // with
																								// default
																								// value
		renderFilledFlat = Settings.getProperty ("FirePatternSketcher.renderFilledFlat", renderFilledFlat); // with
																											// default
																											// value
		renderFilledLight = Settings.getProperty ("FirePatternSketcher.renderFilledLight", renderFilledLight); // with
																												// default
																												// value
		renderFilledTransparent = Settings.getProperty ("FirePatternSketcher.renderFilledTransparent",
				renderFilledTransparent); // with default value
		colorSpecies = Settings.getProperty ("FirePatternSketcher.colorSpecies", colorSpecies); // with
																								// default
																								// value
		colorStratum = Settings.getProperty ("FirePatternSketcher.colorStratum", colorStratum); // with
																								// default
																								// value
		colorDamage = Settings.getProperty ("FirePatternSketcher.colorDamage", colorDamage); // with
																								// default
																								// value
		colorGlobal = Settings.getProperty ("FirePatternSketcher.colorGlobal", colorGlobal); // with
																								// default
																								// value
		renderAlphaValue = Settings.getProperty ("FirePatternSketcher.renderAlphaValue", renderAlphaValue); // with
																											// default
																											// value

		labelColor = new SmartColor (Settings.getProperty ("FirePatternSketcher.labelColor", labelColor)); // with
																											// default
																											// value
		trunkColor = new SmartColor (Settings.getProperty ("FirePatternSketcher.trunkColor", trunkColor)); // with
																											// default
																											// value
		crownColor = new SmartColor (Settings.getProperty ("FirePatternSketcher.crownColor", crownColor)); // with
																											// default
																											// value
		cellColor = new SmartColor (Settings.getProperty ("FirePatternSketcher.cellColor", cellColor)); // with
																										// default
																										// value
		selectionColor = new SmartColor (Settings.getProperty ("FirePatternSketcher.selectionColor", selectionColor)); // with
																														// default
																														// value

		stratumThreshold = Settings.getProperty ("FirePatternSketcher.stratumThreshold", stratumThreshold); // with
																											// default
																											// value

		// fc - 10.2.2010
		initColors ();

		/*
		 * TO BE PROCESSED protected TreeMap<String,Color> speciesColor = new
		 * TreeMap<String,Color> (); // a map of color for species protected
		 * ArrayList<Color> stratumColor = new ArrayList<Color> (); // a
		 * collection of color for stratum protected ArrayList<Color>
		 * fireDamageColor = new ArrayList<Color> (); protected
		 * Collection<FiSpecies> fireSpeciesList = new ArrayList<FiSpecies> ();
		 */
	}

	/**
	 * Store some options for next time
	 */
	@Override
	public void store () {
		Settings.setProperty ("FirePatternSketcher.refreshShapePattern", refreshShapePattern);
		Settings.setProperty ("FirePatternSketcher.trunkEnabled", trunkEnabled);
		Settings.setProperty ("FirePatternSketcher.crownEnabled", crownEnabled);
		Settings.setProperty ("FirePatternSketcher.renderOutline", renderOutline);
		Settings.setProperty ("FirePatternSketcher.renderFilled", renderFilled);
		Settings.setProperty ("FirePatternSketcher.renderFilledFlat", renderFilledFlat);
		Settings.setProperty ("FirePatternSketcher.renderFilledLight", renderFilledLight);
		Settings.setProperty ("FirePatternSketcher.renderFilledTransparent", renderFilledTransparent);
		Settings.setProperty ("FirePatternSketcher.colorSpecies", colorSpecies);
		Settings.setProperty ("FirePatternSketcher.colorStratum", colorStratum);
		Settings.setProperty ("FirePatternSketcher.colorDamage", colorDamage);
		Settings.setProperty ("FirePatternSketcher.colorGlobal", colorGlobal);
		Settings.setProperty ("FirePatternSketcher.renderAlphaValue", renderAlphaValue);

		Settings.setProperty ("FirePatternSketcher.labelColor", labelColor);
		Settings.setProperty ("FirePatternSketcher.trunkColor", trunkColor);
		Settings.setProperty ("FirePatternSketcher.crownColor", crownColor);
		Settings.setProperty ("FirePatternSketcher.cellColor", cellColor);
		Settings.setProperty ("FirePatternSketcher.selectionColor", selectionColor);

		Settings.setProperty ("FirePatternSketcher.stratumThreshold", stratumThreshold);

		/*
		 * TO BE PROCESSED protected TreeMap<String,Color> speciesColor = new
		 * TreeMap<String,Color> (); // a map of color for species protected
		 * ArrayList<Color> stratumColor = new ArrayList<Color> (); // a
		 * collection of color for stratum protected ArrayList<Color>
		 * fireDamageColor = new ArrayList<Color> (); protected
		 * Collection<FiSpecies> fireSpeciesList = new ArrayList<FiSpecies> ();
		 */
		try {
			Settings.savePropertyFile ();
		} catch (Exception e) {
		} // may cause an error if default.property.file was not set before
	}

	/**
	 * From Extension interface.
	 */
	@Override
	public String getName () {
		return Translator.swap ("FirePatternSketcher");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {
		return VERSION;
	}

	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {
		return "S. Griffon";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {
		return Translator.swap ("FirePatternSketcher.description");
	}

	/**
	 * SketchListener interface
	 */
	@Override
	public void sketchHappening (SketchEvent evt) {
		super.sketchHappening (evt); // very significant

		// When trees are added / removed / updated, some options of the panel
		// may have to be refreshed (e.g. damage available only if some trees
		// have a severity != null)
		getInstantPanel ().update (); // may update this.colorDamage
	}


	@Override
	public void actionPerformed (ActionEvent evt) {
		super.actionPerformed (evt);
		// Catch event thrown by the pattern map to update patterns geometry if
		// necessary
		if (evt.getActionCommand ().equals ("PatternChanged")) {
			refreshShapePattern = true;
			reset ();
			panel3D.updateNow ();
		}

	}

	/**
	 * Scene display list.
	 * Create a scene with the items in getItems () with the current options.
	 */
	public int createSceneDisplayList (GLAutoDrawable drawable) {

		items = new ArrayList (getItems ());

		// Get a renderingLevel according to 2 thresholds t1 and t2
		// A level is returned depending on the number of items to be drawn
		// l1: less than t1, full details -> l2 : intermediate -> l3: more than
		// t2, raw rendering
		int renderingLevel = getRenderingLevel (RENDERING_THRESHOLD_1, RENDERING_THRESHOLD_2);

		if (instantPanel.isQualityModified ()) {
			forcedQualityNote = instantPanel.getQualityNote ();
			if (forcedQualityNote > 0.5) {
				renderingLevel = 1;
			} else if (forcedQualityNote > 0.01) {
				renderingLevel = 2;
			} else {
				renderingLevel = 3;
			}
			qualityNote = forcedQualityNote;
		}

		int numberOfItems = items.size ();

		// Crown only in renderingLevel 1 and 2
		crownEnabled = crownEnabled && renderingLevel != 3;
		trunkEnabled = trunkEnabled && renderingLevel == 1;

		double level2Height = 0d;
		if (renderingLevel == 2) {
			// For level 2, computation of a threshold in height for trees that
			// are
			// going to be represented with crown and trunk (level2Height):
			// the number of trees represented in mode 2 decrease linearly
			// between
			// RENDERING_THRESHOLD_1 and RENDERING_THRESHOLD_2
			double treesInMode2fraction;
			if (!instantPanel.isQualityModified ()) {
				treesInMode2fraction = (items.size () - RENDERING_THRESHOLD_2)
						/ (double) (RENDERING_THRESHOLD_1 - RENDERING_THRESHOLD_2);
			} else {
				treesInMode2fraction = 2d * forcedQualityNote;
			}
			double minHeight = Double.MAX_VALUE;
			double maxHeight = -Double.MAX_VALUE;

			for (TreeWithCrownProfileItem item : items) {
				minHeight = Math.min (minHeight, item.getHeight ());
				maxHeight = Math.max (maxHeight, item.getHeight ());
			}
			level2Height = minHeight;
			qualityNote = 0.5;
			// Median computation based on sampling in 10 classes
			int nclass = 10;
			int[] treeNumber = new int[nclass];
			for (TreeWithCrownProfileItem item : items) {
				// Index is an integer between 0 and nclass - 1
				// increasing linearly with item.getHeight()
				int index = (int) Math.round ((nclass - 1) * (item.getHeight () - minHeight)
						/ (maxHeight - minHeight + 1e-10));
				treeNumber[index]++;
			}
			int treeBelowMedian = 0;
			for (int index = 0; index < nclass; index++) {
				treeBelowMedian += treeNumber[index];
				if (treeBelowMedian > items.size () * (1d - treesInMode2fraction)) {
					level2Height = minHeight + (index + 0.5) / nclass * (maxHeight - minHeight);
					qualityNote = Math.max (0.5 * treesInMode2fraction, 0.01);
					break;
				}
			}
			System.out.println ("Height threshold for representation in mode 2: " + level2Height);
			System.out.println ("Pourcentage of trees in mode 2: " + treesInMode2fraction * 100d);
		} else if (renderingLevel == 3) {
			qualityNote = 0.01d;
		}
		// System.out.println("quality note of the scene: " + qualityNote*100d);
		instantPanel.setQualityNote (qualityNote);

		if (verticesBuf.isEmpty ()) {
			refreshShapePattern = true;
		}

		GL2 gl = drawable.getGL ().getGL2 ();
		GLUgl2 glu = new GLUgl2 ();

		// We compute the mesh of the patterns, trunk and box if needed
		if (refreshShapePattern) {

			if (crownEnabled) {
				// ~ System.out.println ("Compute mesh");
				verticesBuf.clear ();
				normalBuf.clear ();
				indiceBuf.clear ();
				colorsBuf.clear ();
				for (TreeWithCrownProfileItem item : items) {
					FmPlant subject = (FmPlant) item.getTree ();
					makePatternCrown (gl, subject, renderingLevel, numberOfItems);
				}
			}

			if (trunkEnabled) {
				trunk = makeTrunkList (gl);
				addDisplayList (trunk);
			}

		}

		// Display list creation
		int scene = gl.glGenLists (1);
		gl.glNewList (scene, GL2.GL_COMPILE); // begin of the whole scene list

		gl.glPushAttrib (GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix ();

		gl.glEnable (GL2.GL_CULL_FACE);
		gl.glEnable (GL2.GL_NORMALIZE);
		if (renderFilledLight)
			gl.glEnable (GL2.GL_LIGHTING);
		else
			gl.glDisable (GL2.GL_LIGHTING);

		if (renderOutline) {
			gl.glPolygonMode (GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
			gl.glDisable (GL.GL_CULL_FACE);
		} else {
			gl.glPolygonMode (GL.GL_FRONT, GL2.GL_FILL);
		}

		if (renderFilledFlat) {
			gl.glShadeModel (GL2.GL_FLAT);
		} else {
			gl.glShadeModel (GL2.GL_SMOOTH);
		}

		boolean trunkEnabledMemo = trunkEnabled;
		boolean crownEnabledMemo = crownEnabled;

		for (TreeWithCrownProfileItem item : items) {
			FiPlant subject = (FiPlant) item.getTree ();

			// These x, y and z are in the openGL frame
			double x = subject.getX ();
			double y = subject.getY ();
			double z = subject.getZ ();

			double treeDbh = subject.getDbh () / 100;
			double treeHeight = subject.getHeight ();
			double trunkHeight = treeHeight - treeHeight / 8.;

			// Prepare selection
			loadNameForSelection (drawable, item);

			// Level 2
			if (renderingLevel == 2 && subject.getHeight () < level2Height) {
				trunkEnabled = false;
				crownEnabled = false;
			} else {
				trunkEnabled = trunkEnabledMemo;
				crownEnabled = crownEnabledMemo;
			}

			double boundingRadius = subject.getCrownRadius ();
			double boundingHeight = treeHeight;

			// Draw trunk
			if (trunkEnabled) {
				gl.glPushMatrix ();

				// Draw at least a line (to avoid trunk disappearing when too
				// thin)
				gl.glPushAttrib (GL2.GL_ALL_ATTRIB_BITS);
				gl.glDisable (GL2.GL_LIGHTING);
				gl.glColor3d (0d, 0d, 0d);
				gl.glBegin (GL.GL_LINES);
				gl.glVertex3d (x, y, z);
				gl.glVertex3d (x, y, z + treeHeight);
				gl.glEnd ();
				gl.glPopAttrib ();

				// Change the treeHeight
				if (colorDamage) {
					gl.glEnable (GL2.GL_LIGHTING);
					traceTrunkDamage (gl, trunk, subject);
					if (renderFilledLight)
						gl.glEnable (GL2.GL_LIGHTING);
					else
						gl.glDisable (GL2.GL_LIGHTING);

				} else {
					gl.glTranslated (x, y, z);
					gl.glScaled (treeDbh, treeDbh, trunkHeight);
					float[] someColor = { 0.48f, 0.39f, 0.25f, 1.0f };
					gl.glMaterialfv (GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, someColor, 0);
					gl.glCallList (trunk);
				}

				gl.glPopMatrix ();
			}

			// Crown
			if (crownEnabled) {

				// Draw sphericCrown
				gl.glPushMatrix ();
				gl.glTranslated (x, y, z);
				traceRevolution (gl, subject);
				gl.glPopMatrix ();

			}

			// If many items, raw rendering
			if (!trunkEnabled && !crownEnabled) {

				gl.glDisable (GL2.GL_LIGHTING);
				int d = (int) Math.round (2 * subject.getCrownRadius ());
				d = Math.max (1, d);
				gl.glPointSize (d);

				// Color according to current options
				float[] c = getColor (subject);
				gl.glColor3d (c[0], c[1], c[2]);

				// A single point
				gl.glBegin (GL.GL_POINTS);
				gl.glVertex3d (x, y, z);
				gl.glEnd ();
			}

		}

		gl.glPopMatrix ();
		gl.glPopAttrib ();
		
		gl.glEndList (); // end of the scene display list

		return scene;
	}
	
	/**
	 * Fast scene display list.
	 */
	protected int createFastSceneDisplayList (GLAutoDrawable drawable) {
		
		GL2 gl = drawable.getGL ().getGL2 ();
		GLUgl2 glu = new GLUgl2 ();

		
		// Create the FAST scene display list
		int fastScene = gl.glGenLists (1);
		gl.glNewList (fastScene, GL2.GL_COMPILE); // begin of the whole scene
													// list

		gl.glPushAttrib (GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix ();

		gl.glPolygonMode (GL.GL_FRONT, GL2.GL_LINE);
		gl.glDisable (GL2.GL_LIGHTING);
		gl.glDisable (GL2.GL_NORMALIZE);
		gl.glColor3d (0d, 0d, 0d);

		items = new ArrayList (getItems ());

		for (TreeWithCrownProfileItem item : items) {
			FiPlant subject = (FiPlant) item.getTree ();

			// These x, y and z are in the openGL frame
			double x = subject.getX ();
			double y = subject.getY ();
			double z = subject.getZ ();

			double treeDbh = subject.getDbh () / 100;
			double treeHeight = subject.getHeight ();

			double boundingRadius = subject.getCrownRadius ();
			double boundingHeight = treeHeight;

			// Default color
			gl.glColor3d (0d, 0d, 0d);

			// Draw trunk
			if (trunkEnabled) {
				gl.glPushMatrix ();

				gl.glBegin (GL.GL_LINES);
				gl.glVertex3d (x, y, z);
				gl.glVertex3d (x, y, z + treeHeight);
				gl.glEnd ();

				gl.glPopMatrix ();
			}

			// Crown
			if (crownEnabled) {
				// Draw sphericCrown
				gl.glPushMatrix ();
				gl.glTranslated (x, y, z);
				traceRevolution (gl, subject);
				gl.glPopMatrix ();
			}

			// If many items, raw rendering
			if (!trunkEnabled && !crownEnabled) {

				gl.glDisable (GL2.GL_LIGHTING);
				int d = (int) Math.round (2 * subject.getCrownRadius ());
				d = Math.max (1, d);
				gl.glPointSize (d);

				// Color according to current options
				float[] c = getColor (subject);
				gl.glColor3d (c[0], c[1], c[2]);

				// A single point
				gl.glBegin (GL.GL_POINTS);
				gl.glVertex3d (x, y, z);
				gl.glEnd ();
			}
		}

		gl.glPopMatrix ();
		gl.glPopAttrib ();

		gl.glEndList (); // end of the FAST scene display list

		refreshShapePattern = false;
		return fastScene;
	}

	private void setStandSpeciesList (Collection<FiSpecies> fireSpeciesList) {
		this.fireSpeciesList = fireSpeciesList;
	}

	private void initColors () {
		//Color[] damageColor = { new Color (51, 151, 0), new Color (255, 153, 0), new Color (153, 153, 153),
				
		Color[] damageColor = { new Color (51, 151, 0), new Color (255, 153, 0), new Color (255, 153, 0),
				new Color (153, 51, 51), new Color (15, 15, 15), new Color (0, 0, 0) };
		// Create default damage colors

		if (fireDamageColor.isEmpty ()) {
			for (int i = 0; i < damageColor.length; i++) {
				fireDamageColor.add (damageColor[i]);
			}

			// store ();
		}

		// Create default stratum colors
		if (stratumColor.isEmpty ()) {
			// The Hue value is an angle in a HSB color model.
			int nbStratum = 2;
			float angleHue = (float) (1.0d / nbStratum);
			float currentHue = 0.3f;
			for (int i = 0; i < nbStratum; i++) {
				currentHue += angleHue;
				stratumColor.add (new Color (Color.HSBtoRGB (currentHue, 1.0f, 1.0f)));
			}

			// store ();
		}

		// Create default species colors
		// The Hue value is an angle in a HSB color model.
		float angleHue = (float) (1.0d / fireSpeciesList.size ());
		float currentHue = 0.f;
		for (FiSpecies fs : fireSpeciesList) {
			currentHue += angleHue;
			if (speciesColor.get (fs.getName ()) == null)
				speciesColor.put (fs.getName (), new Color (Color.HSBtoRGB (currentHue, 1.0f, 1.0f)));
			// ~ System.out.println ("fsParams.initColors: name="+fs.getName
			// ()+" color="+speciesColor.get (fs.getName ()));
		}

		store ();
	}

	// Display list: Trunk
	private int makeTrunkList (GL2 gl) {
		GLUgl2 glu = new GLUgl2 ();

		GLUquadric gluQuatric = glu.gluNewQuadric ();
		glu.gluQuadricOrientation (gluQuatric, GLU.GLU_OUTSIDE);

		// if (params.renderOutline) {
		// glu.gluQuadricDrawStyle (gluQuatric, GLU.GLU_LINE);
		// }

		int list = gl.glGenLists (1);
		gl.glNewList (list, GL2.GL_COMPILE);
		glu.gluCylinder (gluQuatric, 0.5, 0.5, 1, 8, 1); // a normalized cone
		gl.glEndList ();
		return list;
	}

	// Draw the trunk with color damage: hCMin , hCmax -> 3 cylinders
	private void traceTrunkDamage (GL2 gl, int trunkList, FiPlant ft) {

		double treeDbh = ft.getDbh () / 100 * 2;
		double treeHeight = ft.getHeight ();
		double trunkHeight = treeHeight - treeHeight / 8.;
		// FP 2009
		FiSeverity severity = ft.getSeverity ();
		double hCMin = severity.getMinBoleLengthCharred () * 0.01 * treeHeight;
		double hCMax = severity.getMaxBoleLengthCharred () * 0.01 * treeHeight;

		float[] someColor = fireDamageColor.get (5).getComponents (null);
		gl.glTranslated (ft.getX (), ft.getY (), ft.getZ ());
		gl.glPushMatrix ();
		// gl.glScaled (treeDbh, treeDbh, ft.getHCMin ());
		gl.glScaled (treeDbh, treeDbh, hCMin);// FP2009
		gl.glMaterialfv (GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, someColor, 0);
		gl.glCallList (trunk);
		gl.glPopMatrix ();
		// gl.glTranslated (0, 0, ft.getHCMin ());
		gl.glTranslated (0, 0, hCMin);// FP2009
		gl.glPushMatrix ();
		someColor = fireDamageColor.get (4).getComponents (null);
		gl.glMaterialfv (GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, someColor, 0);
		// gl.glScaled (treeDbh, treeDbh, ft.getHCMax ()-ft.getHCMin ());
		gl.glScaled (treeDbh, treeDbh, hCMax - hCMin);// FP2009
		gl.glCallList (trunk);
		gl.glPopMatrix ();
		// gl.glTranslated (0, 0, ft.getHCMax ()-ft.getHCMin ());
		gl.glTranslated (0, 0, hCMax - hCMin);
		gl.glPushMatrix ();
		someColor = fireDamageColor.get (3).getComponents (null);
		gl.glMaterialfv (GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, someColor, 0);
		gl.glScaled (treeDbh, treeDbh, trunkHeight - hCMax);
		gl.glCallList (trunk);
		gl.glPopMatrix ();
	}

	private int makePatternCrown (GL gl, FmPlant ft, int renderingLevel, int numberOfItems) {

		FmModel model = ((FmStand) ft.getScene ()).getModel ();
		FmGeom fp = model.getPatternMap ().findAlwaysPattern (ft);
		ArrayList<Vertex3d> points = computePatternSideShape (fp, ft);
		computeRevolution (ft, points, renderingLevel, numberOfItems);
		return 1;
	}

	private ArrayList<Vertex3d> computePatternSideShape (FmGeom fp, FiPlant ft) {

		double crownBaseHeight = ft.getCrownBaseHeight (); // m.
															// "hauteur de la base du houppier / hmin"
															// Note: hmax = tree
															// height
		double crownDiameter = ft.getCrownDiameter (); // m. the maximum
														// diameter of the crown

		double crownDiameterHeight = ft.getMaxDiameterHeight (); // m. height of
																	// crownDiameter
		double treeHeight = ft.getHeight ();
		double crownLength = treeHeight - crownBaseHeight;
		// double crownScorchedHeight = ft.getCrownScorchedHeight ();
		// double crownKilledHeight = ft.getCrownKilledHeight ();
		// remplaced by : FP 2009, initially defined as crownScorchHeight
		// (without additionning crownBaseHeight), but false
		// replaced by crownLength....

		// fc - 17.9.2009 - removed the 3 lines below, getSeverity () may return
		// null if no damage
		// ~ FiSeverity severity = ft.getSeverity();
		// ~ double crownLengthScorched = severity.getCrownLengthScorched();
		// ~ double crownLengthKilled = severity.getCrownLengthKilled();

		// TODO : calculer le % de la hauteur du diam max � partir des valeurs
		// de l'arbre, ou prendre le pourcentage de la hauteur du diam max
		// renseign�e du patron ?
		// double crownPatternDiameterMaxHeight = fp.getHDMax ();
		double crownPatternDiameterMaxHeight = ((crownDiameterHeight - crownBaseHeight) / crownLength) * 100;

		ArrayList<Vertex3d> pointPoly = new ArrayList<Vertex3d> ();

		pointPoly.add (new Vertex3d (0, 0, crownBaseHeight));
		for (FmGeomDiameter diam : fp.getDiametersInferior ()) {
			// double relativeHeight=diam.getHeight () *
			// crownPatternDiameterMaxHeight / 100.;
			double relativeHeight = diam.getHeight (); // FP 2011
			double relativeDiameter = diam.getWidth ();
			double scaledHeight = (crownLength * relativeHeight / 100.) + crownBaseHeight;
			double scaledRadius = (crownDiameter * relativeDiameter / 100.) / 2.;
			// System.out.println("inf pattern: sh,sr=" + scaledHeight + ","
			// + scaledRadius);
			pointPoly.add (new Vertex3d (-scaledRadius, 0, scaledHeight));

		}
		// Add the points for the max diameter
		// TODO si c en % calcul�
		pointPoly.add (new Vertex3d (-crownDiameter / 2., 0, (crownLength * crownPatternDiameterMaxHeight / 100.)
				+ crownBaseHeight));
		// pointPoly.add (new Vertex3d (-crownDiameter/2., 0,
		// crownDiameterHeight));

		// Draw the superior diameters
		for (FmGeomDiameter diam : fp.getDiametersSuperior ()) {
			// double relativeHeight=diam.getHeight () *
			// ((100-crownPatternDiameterMaxHeight) / 100.) +
			// crownPatternDiameterMaxHeight;
			double relativeHeight = diam.getHeight (); // FP 2011
			double relativeDiameter = diam.getWidth ();
			double scaledHeight = (crownLength * relativeHeight / 100.) + crownBaseHeight;
			double scaledRadius = (crownDiameter * relativeDiameter / 100.) / 2.;
			pointPoly.add (new Vertex3d (-scaledRadius, 0, scaledHeight));
			// System.out.println("sup pattern: sh,sr=" + scaledHeight + ","
			// + scaledRadius);
		}

		pointPoly.add (new Vertex3d (0, 0, treeHeight));

		return pointPoly;

	}

	/**
	 * Returns the color for a given plant according to current options.
	 */
	private float[] getColor (FiPlant p) {
		try {
			float someColor[] = new float[4];

			if (colorGlobal) {
				someColor = crownColor.getComponents (null);

			} else if (colorSpecies) {
				if (speciesColor == null || speciesColor.isEmpty ()) {
					FmStand stand = (FmStand) p.getScene ();
					setStandSpeciesList (FmStand.getStandSpeciesList (stand));
					initColors ();
				}
				someColor = speciesColor.get (p.getSpecies ().getName ()).getComponents (null);

			} else if (colorStratum) {
				if (p.getHeight () < stratumThreshold) {
					someColor = stratumColor.get (0).getComponents (null);

				} else {
					someColor = stratumColor.get (1).getComponents (null);
				}
			}
			return someColor;

		} catch (Exception e) {
			System.out.println (">>>>>>>>>>>>>FirePatternSketcher getColor (FiPlant p) p = " + p + " colorSpecies = "
					+ colorSpecies + " p.getSpecies ().getName () = " + p.getSpecies ().getName ());
			System.out.println (">>>>>>>>>>>>>>>>FirePatternSketcher speciesColor = "
					+ AmapTools.toString (speciesColor));
			return Color.GRAY.getComponents (null);
		}
	}

	private void traceRevolution (GL2 gl, FiPlant ft) {

		float someColor[] = new float[4];

		DoubleBuffer tmpVerticesBuf = verticesBuf.get (ft);
		DoubleBuffer tmpNormalBuf = normalBuf.get (ft);
		IntBuffer tmpIndiceBuf = indiceBuf.get (ft);
		FloatBuffer tmpColorsBuf = colorsBuf.get (ft);

		int nbIndices = tmpIndiceBuf.limit ();

		someColor = getColor (ft);
		// ~ if(colorGlobal) {
		// ~ someColor = crownColor.getComponents (null);
		// ~ } else if(colorSpecies) {
		// ~ someColor = speciesColor.get (ft.getSpecies ().getName
		// ()).getComponents (null);
		// ~ } else if(colorStratum) {

		// ~ if(ft.getHeight () < stratumThreshold)
		// ~ someColor = stratumColor.get (0).getComponents (null);
		// ~ else
		// ~ someColor = stratumColor.get (1).getComponents (null);
		// ~ }

		gl.glMaterialfv (GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, someColor, 0);

		// If we display the damage we need to enable GL_COLOR_MATERIAL
		// to allow GL_COLOR_ARRAY effects on the color material
		if (colorDamage) {
			// gl.glDisable (GL.GL_LIGHTING);
			gl.glEnable (GL2.GL_COLOR_MATERIAL);
			gl.glEnableClientState (GL2.GL_COLOR_ARRAY);
			gl.glColorPointer (4, GL.GL_FLOAT, 0, tmpColorsBuf);
		}

		//

		gl.glEnableClientState (GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState (GL2.GL_NORMAL_ARRAY);

		gl.glVertexPointer (3, GL2.GL_DOUBLE, 0, tmpVerticesBuf);
		gl.glNormalPointer (GL2.GL_DOUBLE, 0, tmpNormalBuf);

		// if (params.renderOutline) {
		// gl.glPolygonMode (GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		// } else {
		// gl.glPolygonMode (GL.GL_FRONT, GL.GL_FILL);
		// }

		gl.glDrawElements (GL.GL_TRIANGLES, nbIndices, GL2.GL_UNSIGNED_INT, tmpIndiceBuf);

		gl.glDisableClientState (GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState (GL2.GL_NORMAL_ARRAY);

		if (colorDamage) {
			// gl.glEnable (GL.GL_LIGHTING);
			gl.glDisableClientState (GL2.GL_COLOR_ARRAY);
			gl.glDisable (GL2.GL_COLOR_MATERIAL);
		}
		// gl.glShadeModel (gl.GL_SMOOTH);
	}

	private void addVertexForColorDamage (FiPlant ft, ArrayList<Vertex3d> points) {
		int ind1 = -1; // index of the first vertex to add : crownScorchedHeight
		int ind2 = -1; // index of the second vertex to add :
						// crownKilledHeightdouble crownScorchedHeight =
						// ft.getCrownScorchedHeight ();

		// TODO change if crownScorchedHeight and crownKilledHeight are %
		// => No they are in m (FP 09/2009)
		// remplaced by : FP 2009, initially defined as
		// crownScorchHeight+crownBaseHeight, but false

		FiSeverity severity = ft.getSeverity ();
		
		// fc+sg-6.1.2015 no damage -> no extra vertices
		if (!severity.containsDamages()) return;
		
		// double crownBaseHeight = ft.getCrownBaseHeight ();
		// crownScorchedHeight and crownKilledHeight form ground
		double crownScorchHeight = severity.getCrownScorchHeight ();
		double crownScorchedX = 0;
		double crownScorchedXDelta = 0;
		double crownKillHeight = severity.getCrownKillHeight ();
		double crownKilledX = 0;
		double crownKilledXDelta = 0;

		double delta = 0;
		double deltaX = 0;
		double deltaZ = 0;

		int currentIndice = 1;

		// sg+fc-17.11.2015 Don't add points if under crownBaseHeight 
		if (crownScorchHeight > ft.getCrownBaseHeight()) {
			while (currentIndice < points.size ()) { // don't iterate on the last
														// point'
				Vertex3d pointA = points.get (currentIndice - 1);
				Vertex3d pointB = points.get (currentIndice);
				if (ind1 == -1 && pointB.z >= crownScorchHeight) {
					ind1 = currentIndice;
					deltaX = pointA.x - pointB.x;
					if (deltaX == 0) {
						crownScorchedX = pointA.x;
						crownScorchedXDelta = pointA.x + 0.01;
					} else {
						deltaZ = pointA.z - pointB.z;
						delta = deltaZ / deltaX;
						double decalage = (pointA.x * pointB.z - pointB.x * pointA.z) / deltaX;
						crownScorchedX = (crownScorchHeight - decalage) / delta;
						crownScorchedXDelta = (crownScorchHeight + 0.01 - decalage) / delta;
					}
					break;
				}
				currentIndice++;
			}
	
			if (ind1 != -1) {
				points.add (ind1, new Vertex3d (crownScorchedX, 0, crownScorchHeight));
				// We add point to reduce the color interpolation zone
				points.add (ind1 + 1, new Vertex3d (crownScorchedXDelta, 0, crownScorchHeight + 0.01));
	
			}
		}

		// sg+fc-17.11.2015 Don't add points if under crownBaseHeight 
		if (crownKillHeight > ft.getCrownBaseHeight()) {
			currentIndice = 1;
			while (currentIndice < points.size ()) { // don't iterate on the last
														// point'
				Vertex3d pointA = points.get (currentIndice - 1);
				Vertex3d pointB = points.get (currentIndice);
				if (ind2 == -1 && pointB.z >= crownKillHeight) {
					ind2 = currentIndice;
					deltaX = pointA.x - pointB.x;
					if (deltaX == 0) {
						crownKilledX = pointA.x;
						crownKilledXDelta = pointA.x + 0.01;
					} else {
						deltaZ = pointA.z - pointB.z;
						delta = deltaZ / deltaX;
						double decalage = (pointA.x * pointB.z - pointB.x * pointA.z) / deltaX;
						crownKilledX = (crownKillHeight - decalage) / delta;
						crownKilledXDelta = (crownKillHeight + 0.01 - decalage) / delta;
					}
					break;
				}
	
				currentIndice++;
			}
	
			if (ind2 != -1) {
	
				points.add (ind2, new Vertex3d (crownKilledX, 0, crownKillHeight));
				// We add point to reduce the color interpolation zone
				points.add (ind2 + 1, new Vertex3d (crownKilledXDelta, 0, crownKillHeight + 0.01));
	
			}
		}
	}

	private void computeRevolution (FiPlant ft, ArrayList<Vertex3d> points, int renderingLevel, int numberOfItems) {

		if (colorDamage) {
			addVertexForColorDamage (ft, points);
		}

		int nSegments = 0;

		// Rendering level
		// level 1: full details, 16 segments
		// level 2: rawer, less segments
		// level 3: no crown at all, computeRevolution should not be called
		if (renderingLevel == 1) { // renderingLevel 1
			int nMax = 16;
			int nMin = 3;

			// nSegments between nMin and nMax depending on numberOfItems
			// nSegments = (int) Math.round (nMax - (nMax - nMin) *
			// numberOfItems / t1);
			if (!instantPanel.isQualityModified ()) {
				if (numberOfItems <= RENDERING_THRESHOLD_1 * nMin / (double) nMax) {
					nSegments = nMax;
					qualityNote = 1d;
				} else {
					nSegments = (int) Math.round (nMin * RENDERING_THRESHOLD_1 / (double) numberOfItems);
					qualityNote = (1d * (nSegments - nMin) + 0.5 * (nMax - nSegments)) / (double) (nMax - nMin);
				}
			} else { // quality modified
				qualityNote = forcedQualityNote;
				nSegments = (int) Math.round ((qualityNote * (nMax - nMin) + nMin - 0.5 * nMax) / 0.5);
				// System.out.println("segment number="+nSegments);
			}
			instantPanel.setQualityNote (qualityNote);

		} else { // renderingLevel 2
			nSegments = 4;
		}

		int nRings = points.size () - 1;
		float angle = 360.0f / nSegments;
		float current_angle;
		int nbVertices = (nRings + 1) * (nSegments + 1);
		int nbIndices = 6 * nRings * (nSegments + 1);
		int nbFaces = nbIndices / 3;

		// FP 2009, initially defined from ground
		// remplaced by : FP 2009, initially defined as
		// crownScorchHeight+crownBaseHeight, but false
		FiSeverity severity = ft.getSeverity ();
		double crownScorchHeight = severity == null ? 0 : severity.getCrownScorchHeight ();
		double crownKillHeight = severity == null ? 0 : severity.getCrownKillHeight ();

		// fc-29.8.2014 Replaced BufferUtil by Buffers (JOGL 2.0 -> 2.2 upgrade)
		DoubleBuffer tmpVerticesBuf = Buffers.newDirectDoubleBuffer (nbVertices * 3);
		DoubleBuffer tmpNormalBuf = Buffers.newDirectDoubleBuffer (nbVertices * 3);
		FloatBuffer tmpColorsBuf = Buffers.newDirectFloatBuffer (nbVertices * 4);
		IntBuffer tmpIndiceBuf = Buffers.newDirectIntBuffer (nbIndices);
//		DoubleBuffer tmpVerticesBuf = BufferUtil.newDoubleBuffer (nbVertices * 3);
//		DoubleBuffer tmpNormalBuf = BufferUtil.newDoubleBuffer (nbVertices * 3);
//		FloatBuffer tmpColorsBuf = BufferUtil.newFloatBuffer (nbVertices * 4);
//		IntBuffer tmpIndiceBuf = BufferUtil.newIntBuffer (nbIndices);
		
		Vertex3d pointDown = points.get (0);
		Vertex3d pointUp = points.get (points.size () - 1);
		short wVerticeIndex = 0;
		float colorScroched[];
		float colorKilled[];
		float colorOriginal[];

		// For each ring
		for (int ring = 0; ring <= nRings; ring++) {
			current_angle = 0f;
			for (int seg = 0; seg <= nSegments; seg++) {
				Vertex3d point = points.get (ring);
				point = rotationPoint (point, current_angle);
				tmpVerticesBuf.put (point.x);
				tmpVerticesBuf.put (point.y);
				tmpVerticesBuf.put (point.z);

				if (colorDamage) {
					colorScroched = fireDamageColor.get (1).getComponents (null);
					colorKilled = fireDamageColor.get (2).getComponents (null);
					colorOriginal = fireDamageColor.get (0).getComponents (null);
					// if this point is in the scroched zone of the crown
					if (point.z <= crownKillHeight) {
						tmpColorsBuf.put (colorKilled[0]);
						tmpColorsBuf.put (colorKilled[1]);
						tmpColorsBuf.put (colorKilled[2]);
						tmpColorsBuf.put (colorKilled[3]);

					} else if (point.z > crownKillHeight && point.z <= crownScorchHeight) { // if
																							// this
																							// point
						// is in the
						// killed zone
						// of the crown
						tmpColorsBuf.put (colorScroched[0]);
						tmpColorsBuf.put (colorScroched[1]);
						tmpColorsBuf.put (colorScroched[2]);
						tmpColorsBuf.put (colorScroched[3]);
					} else { // we are in the safe zone
						tmpColorsBuf.put (colorOriginal[0]);
						tmpColorsBuf.put (colorOriginal[1]);
						tmpColorsBuf.put (colorOriginal[2]);
						tmpColorsBuf.put (colorOriginal[3]);
					}

				}

				if (ring != nRings) {
					// each vertex (except the last) has six indices pointing to
					// it
					tmpIndiceBuf.put (wVerticeIndex + nSegments + 1);
					tmpIndiceBuf.put (wVerticeIndex);
					tmpIndiceBuf.put (wVerticeIndex + nSegments);
					tmpIndiceBuf.put (wVerticeIndex + 1);
					tmpIndiceBuf.put (wVerticeIndex);
					tmpIndiceBuf.put (wVerticeIndex + nSegments + 1);

					wVerticeIndex++;
				}
				current_angle += angle;
				current_angle %= 360.0;
			}

		}

		tmpVerticesBuf.rewind ();
		tmpIndiceBuf.rewind ();

		wVerticeIndex = 0;
		Vertex3d vNormal1;
		Vertex3d vNormal2;
		// Compute normal
		for (int ring = 0; ring <= nRings; ring++) {

			for (int seg = 0; seg <= nSegments; seg++) {

				double[] v1 = new double[3];
				double[] v2 = new double[3];
				double[] v3 = new double[3];
				double[] v4 = new double[3];
				int indiceV1 = wVerticeIndex; // the current vertice

				// Top vertex
				if (ring == nRings) {
					vNormal1 = new Vertex3d (0, 0, 1);

				} else if (ring == 0) {
					vNormal1 = new Vertex3d (0, 0, -1);

				} else {
					int indiceV2 = wVerticeIndex + nSegments + 1; // the vertice
																	// up
					int indiceV3;
					if (seg == 0) { // If we are not on the last vertice of this
									// ring
						indiceV3 = wVerticeIndex + nSegments - 1; // the vertice
																	// prec: ,
																	// because
																	// first
																	// vertice
																	// is at the
																	// same
																	// coord
																	// that the
																	// last
																	// vertice
					} else {
						indiceV3 = wVerticeIndex - 1; // the vertice prec
					}
					int indiceV4;
					if (seg == nSegments) { // If we are on the last vertice of
											// this ring
						indiceV4 = wVerticeIndex - nSegments + 1; // the vertice
																	// next: ,
																	// because
																	// first
																	// vertice
																	// is at the
																	// same
																	// coord
																	// that the
																	// last
																	// vertice
					} else {
						indiceV4 = wVerticeIndex + 1; // the vertice next
					}

					v1[0] = tmpVerticesBuf.get (3 * indiceV1);
					v1[1] = tmpVerticesBuf.get (3 * indiceV1 + 1);
					v1[2] = tmpVerticesBuf.get (3 * indiceV1 + 2);

					v2[0] = tmpVerticesBuf.get (3 * indiceV2);
					v2[1] = tmpVerticesBuf.get (3 * indiceV2 + 1);
					v2[2] = tmpVerticesBuf.get (3 * indiceV2 + 2);

					v3[0] = tmpVerticesBuf.get (3 * indiceV3);
					v3[1] = tmpVerticesBuf.get (3 * indiceV3 + 1);
					v3[2] = tmpVerticesBuf.get (3 * indiceV3 + 2);

					v4[0] = tmpVerticesBuf.get (3 * indiceV4);
					v4[1] = tmpVerticesBuf.get (3 * indiceV4 + 1);
					v4[2] = tmpVerticesBuf.get (3 * indiceV4 + 2);

					Vertex3d vCross1 = new Vertex3d (v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]);
					Vertex3d vCross2 = new Vertex3d (v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]);
					Vertex3d vCross3 = new Vertex3d (v4[0] - v1[0], v4[1] - v1[1], v4[2] - v1[2]);
					vNormal1 = crossProduct (vCross1, vCross2);
					vNormal2 = crossProduct (vCross2, vCross3);
					vNormal1.x = vNormal1.x + vNormal2.x;
					vNormal1.y = vNormal1.y + vNormal2.y;
					vNormal1.z = vNormal1.z + vNormal2.z;
					vNormal1 = normalize (vNormal1);
				}

				tmpNormalBuf.put (vNormal1.x);
				tmpNormalBuf.put (vNormal1.y);
				tmpNormalBuf.put (vNormal1.z);

				// if (params.renderOutline) {
				// float someColor2[] = { 1f, 0f, 0f, 1.0f };
				// //Display normal
				// gl.glPushMatrix ();
				// gl.glColor3f (someColor2[0], someColor2[1], someColor2[2]);
				// gl.glBegin (GL.GL_LINES);
				// gl.glVertex3d (v1[0], v1[1], v1[2]);
				// gl.glVertex3d (v1[0]+vNormal1.x, v1[1]+vNormal1.y,
				// v1[2]+vNormal1.z);
				// gl.glEnd ();
				// gl.glPopMatrix ();
				// }

				wVerticeIndex++;
			}
		}

		tmpVerticesBuf.rewind ();
		tmpNormalBuf.rewind ();
		tmpColorsBuf.rewind ();
		tmpIndiceBuf.rewind ();

		verticesBuf.put (ft, tmpVerticesBuf);
		normalBuf.put (ft, tmpNormalBuf);
		indiceBuf.put (ft, tmpIndiceBuf);
		colorsBuf.put (ft, tmpColorsBuf);

	}

	// //////// 3D Math functions /////////////
	// Rotation around Z of angle angle
	private Vertex3d rotationPoint (Vertex3d point, float angle) {

		Vertex3d point_rot = new Vertex3d (0, 0, 0);
		double n = normeXY (point);

		point_rot.x = Math.cos (Math.toRadians (angle)) * n;
		point_rot.y = -Math.sin (Math.toRadians (angle)) * n;
		point_rot.z = point.z;

		return point_rot;

	}

	boolean isFloatNulEpsilon (double x) {
		return (Math.abs (x) <= Double.MIN_VALUE);
	}

	// Returns the norm of the vector n in the horizontal plane
	private double normeXY (Vertex3d n) {
		double d = Math.sqrt ((n.x * n.x + n.y * n.y));
		// if (isFloatNulEpsilon(d))
		// throw new Exception (this,"vecteur nul pour la normalisation...");

		return d;
	}

	private Vertex3d normalize (Vertex3d n) {
		double d = Math.sqrt ((n.x * n.x + n.y * n.y + n.z * n.z));
		// if (isFloatNulEpsilon (d))
		// throw new Exception (this,"vecteur nul pour la normalisation...");
		Vertex3d normal = new Vertex3d (n.x / d, n.y / d, n.z / d);

		return normal;
	}

	private Vertex3d crossProduct (Vertex3d vCross1, Vertex3d vCross2) {
		Vertex3d vn = new Vertex3d (0, 0, 0);
		vn.x = vCross1.y * vCross2.z - vCross1.z * vCross2.y;
		vn.y = vCross1.z * vCross2.x - vCross1.x * vCross2.z;
		vn.z = vCross1.x * vCross2.y - vCross1.y * vCross2.x;
		return vn;
	}

	// ///////////////////////////////////

//	// Display list: selection mark
//	private int makeSelectionMarkList (GL2 gl) {
//		GLU glu = new GLU ();
//		float[] red = { 0.8f, 0.1f, 0.0f, 1.0f };
//		SmartColor selectionColor = panel3D.getSelectionColor ();
//
//		int list = gl.glGenLists (1);
//		// ~ displayListIds.add (list);
//		gl.glNewList (list, GL2.GL_COMPILE);
//		// ~ float memoColor[] = new float[4];
//		// ~ gl.glGetFloatv (GL.GL_CURRENT_COLOR, memoColor, 0);
//
//		// ~ byte[] boo = new byte[1];
//		// ~ gl.glGetBooleanv (GL.GL_LIGHTING, boo, 0);
//		// ~ System.out.println ("FirePatterneSketcher boo="+boo[0]);
//
//		// ~ byte[] isLightingOn = new byte[1];
//		// ~ gl.glGetBooleanv (GL.GL_LIGHTING, isLightingOn, 0);
//		// ~ System.out.println ("FirePatterneSketcher boo="+boo[0]);
//
//		boolean isLightingOn = gl.glIsEnabled (GL2.GL_LIGHTING);
//
//		gl.glDisable (GL2.GL_LIGHTING); // color is only the current color
//
//		// ~ gl.glMaterialfv (GL.GL_FRONT, GL.GL_AMBIENT, red, 0);
//
//		gl.glColor3d (selectionColor.getRedf (), selectionColor.getGreenf (), selectionColor.getBluef ()); // red
//		gl.glBegin (GL.GL_LINE_LOOP);
//		gl.glVertex3f (-0.5f, 0.5f, 0.0f); // Top Left
//		gl.glVertex3f (-0.5f, -0.5f, 0.0f); // Bottom Left
//		gl.glVertex3f (0.5f, -0.5f, 0.0f); // Bottom Right
//		gl.glVertex3f (0.5f, 0.5f, 0.0f); // Top Right
//		gl.glEnd ();
//		gl.glBegin (GL.GL_LINE_LOOP);
//		gl.glVertex3f (-0.5f, 0.5f, 1.0f); // Top Left
//		gl.glVertex3f (-0.5f, -0.5f, 1.0f); // Bottom Left
//		gl.glVertex3f (0.5f, -0.5f, 1.0f); // Bottom Right
//		gl.glVertex3f (0.5f, 0.5f, 1.0f); // Top Right
//		gl.glEnd ();
//		gl.glBegin (GL.GL_LINES);
//		gl.glVertex3f (-0.5f, 0.5f, 0.0f); // Top Left
//		gl.glVertex3f (-0.5f, 0.5f, 1.0f); // Top Left
//		gl.glEnd ();
//		gl.glBegin (GL.GL_LINES);
//		gl.glVertex3f (-0.5f, -0.5f, 0.0f); // Bottom Left
//		gl.glVertex3f (-0.5f, -0.5f, 1.0f); // Bottom Left
//		gl.glEnd ();
//		gl.glBegin (GL.GL_LINES);
//		gl.glVertex3f (0.5f, -0.5f, 0.0f); // Bottom Right
//		gl.glVertex3f (0.5f, -0.5f, 1.0f); // Bottom Right
//		gl.glEnd ();
//		gl.glBegin (GL.GL_LINES);
//		gl.glVertex3f (0.5f, 0.5f, 0.0f); // Top Right
//		gl.glVertex3f (0.5f, 0.5f, 1.0f); // Top Right
//		gl.glEnd ();
//
//		if (isLightingOn) {
//			gl.glEnable (GL2.GL_LIGHTING);
//		}
//		// ~ if (params.renderFilledLight) {
//		// ~ gl.glEnable (GL.GL_LIGHTING);
//		// ~ } else {
//		// ~ gl.glDisable (GL.GL_LIGHTING);
//		// ~ }
//
//		// ~ gl.glColor4f (memoColor[0], memoColor[1], memoColor[2],
//		// memoColor[3]);
//		gl.glEndList ();
//
//		return list;
//	}

	/**
	 * Get the config panel for the subject
	 */
	public FirePatternSketcherPanel getInstantPanel () {

		// OLD NOTE: Maybe useful if a bug arises:
		// Create the panel each time, needed for objectViewers when selection
		// changes - fc - 23.9.2009

		if (instantPanel == null) {
			instantPanel = new FirePatternSketcherPanel (this, qualityNote);
		}
		return instantPanel;
	}

}
