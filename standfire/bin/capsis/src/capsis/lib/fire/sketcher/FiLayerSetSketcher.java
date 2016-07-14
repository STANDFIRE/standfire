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

package capsis.lib.fire.sketcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.gl2.GLUgl2;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.sketch.extension.Sketcher;
import jeeb.lib.sketch.scene.item.Line;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.kernel.DigitalTerrainModel;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.util.SmartColor;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vector3d;
import jeeb.lib.util.Vertex3d;
import capsis.lib.fire.fuelitem.FiLayerSet;

/**
 * FiLayerSetSketcher: draw simple contours in the Sketch tool.
 * 
 * @author F. de Coligny - march, december 2006, february 2007
 */
public class FiLayerSetSketcher extends Sketcher {

	static {
		Translator.addBundle("capsis.lib.fire.sketcher.FiLayerSetSketcher");
	}

	private InstantPanel instantPanel;

	// Sketcher parameters
	protected boolean filled = false;
	protected boolean extrude = true;
	protected boolean reverseNormalsOrientation = false;
	protected SmartColor lineColor = new SmartColor(51, 102, 0); // default:
																	// some blue

	// Sketcher parameters

	/**
	 * Default constructor.
	 */
	public FiLayerSetSketcher() {
	}

	/**
	 * Standard constructor.
	 */
	public FiLayerSetSketcher(Object referent) throws Exception {
		super(referent);
		restore();
		reset();
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 */
	@Override
	public boolean matchWith(Object referent) {
		try {
			if (referent instanceof Collection) {
				Collection c = (Collection) referent;
				if (c.isEmpty()) {
					return false;
				}
				Collection reps = AmapTools.getRepresentatives(c);
				for (Iterator i = reps.iterator(); i.hasNext();) {
					Object o = i.next();
					if (!(o instanceof FiLayerSet)) {
						return false;
					}
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiLayerSetSketcher.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	@Override
	public void restore() {
		filled = Settings.getProperty("FiLayerSetSketcher.filled", true); // with
																			// default
																			// value
		extrude = Settings.getProperty("FiLayerSetSketcher.extrude", true);
		reverseNormalsOrientation = Settings.getProperty("FiLayerSetSketcher.reverseNormalsOrientation", false);
		lineColor = new SmartColor(Settings.getProperty("FiLayerSetSketcher.lineColor", lineColor));
	}

	@Override
	public void store() {
		Settings.setProperty("FiLayerSetSketcher.filled", filled);
		Settings.setProperty("FiLayerSetSketcher.extrude", extrude);
		Settings.setProperty("FiLayerSetSketcher.reverseNormalsOrientation", reverseNormalsOrientation);
		Settings.setProperty("FiLayerSetSketcher.lineColor", lineColor);
		try {
			Settings.savePropertyFile();
		} catch (Exception e) {
		} // may cause an error if default.property.file was not set before
	}

	/**
	 * Extension interface.
	 */
	@Override
	public String getName() {
		return Translator.swap("FiLayerSetSketcher");
	}

	/**
	 * Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.0";

	/**
	 * Extension interface.
	 */
	public String getAuthor() {
		return "F. de Coligny";
	}

	/**
	 * Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("FiLayerSetSketcher.description");
	}

	/**
	 * Scene display list.
	 */
	public int createSceneDisplayList(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();
		GLUgl2 glu = new GLUgl2();

		int scene = gl.glGenLists(1);
		gl.glNewList(scene, GL2.GL_COMPILE); // begin of the whole scene list

		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix();

		gl.glPointSize(5);
		int frontFace = reverseNormalsOrientation ? GL.GL_CW : GL.GL_CCW;
		gl.glFrontFace(frontFace);

		Collection<Line> subjects = new ArrayList(getItems());

		// The filled polygon will be slightly under to render correctly
		// contours over it
		gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_POINT);
		gl.glPolygonOffset(1.0f, -50.0f);

		// Get the terrain for altitude issues when triangulating the filled
		// polygons
		SceneModel sceneModel = (SceneModel) model;
		DigitalTerrainModel terrain = sceneModel.getTerrain();

		for (Line subject : subjects) {
			Item subjectI = subject;

			FiLayerSet layer = (FiLayerSet) subject;

			// Prepare selection
			loadNameForSelection(drawable, subject);

			SmartColor color = lineColor; // default

			if (layer.getColor() != null) { // fc+fp-7.11.2014
				color = new SmartColor(layer.getColor());
			}
			//System.out.println (color+"/"+lineColor);

			gl.glColor3d(color.getRedf(), color.getGreenf(), color.getBluef());

			float[] color4f = { color.getRedf(), color.getGreenf(), color.getBluef(), 1.0f };

			// 1. Polygon on the ground
			gl.glDisable(GL2.GL_LIGHTING);
			if (!extrude && filled) {
				gl.glPolygonMode(GL.GL_FRONT, GL2.GL_FILL);
			} else {
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
			}
			gl.glBegin(GL2.GL_POLYGON);
			ArrayList vertices = new ArrayList(subject.getVertices());
			for (Iterator i = vertices.iterator(); i.hasNext();) {
				Vertex3d v = (Vertex3d) i.next();
				gl.glVertex3d(v.x, v.y, v.z); // one vertex of the contour, fc
												// - 22.3.2007 - relative coords
			}
			gl.glEnd();

			if (extrude) {
				// 2. Top polygon at layer height
				if (filled) {
					gl.glPolygonMode(GL.GL_FRONT, GL2.GL_FILL);

					gl.glEnable(GL2.GL_LIGHTING);
					gl.glEnable(GL2.GL_NORMALIZE);
					gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, color4f, 0);
					gl.glShadeModel(GL2.GL_FLAT);

				} else {
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
				}

				Iterator ni = vertices.iterator();
				Vertex3d p1 = (Vertex3d) ni.next();
				Vertex3d p2 = (Vertex3d) ni.next();
				Vertex3d p3 = (Vertex3d) ni.next(); // polygon : at least 3
													// points
				Vector3d n1 = new Vector3d(p1, p2);
				Vector3d n2 = new Vector3d(p2, p3);
				Vector3d n = Vector3d.normalVector(n1, n2);

				if (filled) {
					// Triangulate the polygon (may be convex OR concave, must
					// be simple (no cross edges)
					Polygon2 polygon2 = buildPolygon2((Polygon) subject);
					boolean counter_clockwise = true;
					List<Polygon2> triangles = polygon2.triangulate(counter_clockwise);
					// Fill all the polygon's triangles
					for (int k = 0; k < triangles.size(); k++) {
						Polygon2 triangle = triangles.get(k);
						gl.glBegin(GL2.GL_POLYGON);

						gl.glNormal3d(n.x, n.y, n.z);

						for (int q = 0; q < triangle.size(); q++) {
							double x = triangle.getX(q);
							double y = triangle.getY(q);
							// z is asked to the terrain
							double z = terrain.getZ(x, y) + layer.getHeight();
							gl.glVertex3d(x, y, z); // one vertex of the
													// contour, fc -
													// 22.3.2007 - relative
													// coords
						}
						gl.glEnd();
					}

				}

				// 3. Edge polygons
				gl.glDisable(GL.GL_CULL_FACE); // needed

				Iterator k = vertices.iterator();
				Vertex3d v1 = (Vertex3d) k.next();
				Vertex3d v0 = v1;
				while (k.hasNext()) {
					Vertex3d v2 = (Vertex3d) k.next();
					p1 = new Vertex3d(v1.x, v1.y, v1.z);
					p2 = new Vertex3d(v2.x, v2.y, v2.z);
					p3 = new Vertex3d(v2.x, v2.y, v2.z + layer.getHeight()); // polygon
																				// :
																				// at
																				// least
																				// 3
																				// points
					n1 = new Vector3d(p1, p2);
					n2 = new Vector3d(p2, p3);
					n = Vector3d.normalVector(n1, n2);
					gl.glNormal3d(n.x, n.y, n.z);
					gl.glBegin(GL2.GL_POLYGON);
					gl.glVertex3d(v1.x, v1.y, v1.z);
					gl.glVertex3d(v2.x, v2.y, v2.z);
					gl.glVertex3d(v2.x, v2.y, v2.z + layer.getHeight());
					gl.glVertex3d(v1.x, v1.y, v1.z + layer.getHeight());
					gl.glEnd();
					v1 = v2;
				}
				// last edge polygon
				p1 = new Vertex3d(v1.x, v1.y, v1.z);
				p2 = new Vertex3d(v0.x, v0.y, v0.z);
				p3 = new Vertex3d(v0.x, v0.y, v0.z + layer.getHeight()); // polygon
																			// :
																			// at
																			// least
																			// 3
																			// points
				n1 = new Vector3d(p1, p2);
				n2 = new Vector3d(p2, p3);
				n = Vector3d.normalVector(n1, n2);
				gl.glNormal3d(n.x, n.y, n.z);
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex3d(v1.x, v1.y, v1.z);
				gl.glVertex3d(v0.x, v0.y, v0.z);
				gl.glVertex3d(v0.x, v0.y, v0.z + layer.getHeight());
				gl.glVertex3d(v1.x, v1.y, v1.z + layer.getHeight());
				gl.glEnd();
				gl.glDisable(GL2.GL_LIGHTING);
			}

			// 4. Points on the ground
			gl.glBegin(GL.GL_POINTS);
			vertices = new ArrayList(subject.getVertices());
			for (Iterator i = vertices.iterator(); i.hasNext();) {
				Vertex3d v = (Vertex3d) i.next();
				gl.glVertex3d(v.x, v.y, v.z); // one vertex of the contour, fc
												// - 22.3.2007 - relative coords
			}
			gl.glEnd();

		}

		gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
		gl.glDisable(GL2.GL_POLYGON_OFFSET_POINT);

		gl.glFrontFace(GL.GL_CCW);

		gl.glPopMatrix();
		gl.glPopAttrib();

		gl.glEndList(); // end of the scene display list

		return scene;
	}

	/**
	 * Fast scene display list.
	 */
	protected int createFastSceneDisplayList(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();
		GLUgl2 glu = new GLUgl2();

		fastScene = gl.glGenLists(1);
		gl.glNewList(fastScene, GL2.GL_COMPILE); // begin of the whole scene
													// list
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix();

		gl.glDisable(GL2.GL_LIGHTING); // color is only the current color
		gl.glColor3d(0.9d, 0.9d, 0.9d); // contour color
		gl.glPolygonMode(GL.GL_FRONT, GL2.GL_LINE);

		int frontFace = reverseNormalsOrientation ? GL.GL_CW : GL.GL_CCW;
		gl.glFrontFace(frontFace);

		gl.glDisable(GL2.GL_NORMALIZE);

		Collection<Line> subjects = new ArrayList(getItems());

		for (Line subject : subjects) {
			Item subjectI = subject;

			gl.glColor3d(0.0d, 0.0d, 0.0d); // normal contour color

			// For polyline
			if (subject instanceof Polygon && ((Polygon) subject).isClosed()) {
				gl.glBegin(GL.GL_LINE_LOOP);
			} else {
				gl.glBegin(GL.GL_LINE_STRIP);
			}
			ArrayList vertices = new ArrayList(subject.getVertices()); // fc -
																		// 3.6.2007
																		// -
																		// added
																		// absolute
			for (Iterator i = vertices.iterator(); i.hasNext();) {
				Vertex3d v = (Vertex3d) i.next();
				gl.glVertex3d(v.x, v.y, v.z); // one vertex of the contour, fc
												// - 22.3.2007 - relative coords
			}
			gl.glEnd();
		}
		gl.glFrontFace(GL.GL_CCW);

		gl.glPopMatrix();
		gl.glPopAttrib();

		gl.glEndList(); // end of the scene display list

		return fastScene;
	}

	private Polygon2 buildPolygon2(Polygon polygon) {
		List<Double> xs = new ArrayList<Double>();
		List<Double> ys = new ArrayList<Double>();
		ArrayList vertices = new ArrayList(polygon.getVertices());
		for (Iterator i = vertices.iterator(); i.hasNext();) {
			Vertex3d v = (Vertex3d) i.next();
			xs.add(v.x);
			ys.add(v.y);
		}
		Polygon2 polygon2 = new Polygon2(xs, ys);
		return polygon2;
	}

	/**
	 * Get the config panel for the subject
	 */
	public InstantPanel getInstantPanel() {
		if (instantPanel == null) {
			instantPanel = new FiLayerSetSketcherPanel(this);
		}
		return instantPanel;
	}

}
