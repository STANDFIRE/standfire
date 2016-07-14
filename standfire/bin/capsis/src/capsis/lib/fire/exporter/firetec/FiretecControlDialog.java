/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package capsis.lib.fire.exporter.firetec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.SetMap;
import jeeb.lib.util.Translator;
import jeeb.lib.util.gui.NorthPanel;
import capsis.commongui.util.Helper;
import capsis.gui.DialogWithClose;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.fuelitem.FuelMatrix;
import capsis.util.Drawer;
import capsis.util.Panel2D;
import capsis.util.Vertex3f;

/**
 * NOT OPERATIONAL Control dialog for physmodels
 * 
 * @author F. de Coligny - january 2008
 */
public class FiretecControlDialog extends AmapDialog implements ActionListener, Drawer, ListSelectionListener {

	// An enum for the view point
	private enum ViewPoint {
		FRONT, TOP, RIGHT
	}

	// Current viewPoint
	private ViewPoint viewPoint;

	// current selection
	private Object[] selection;

	private FiStand stand;

	private Grid grid;
	private SetMap<CKey, CValue> contributionMap;

	// key = voxelMatrix source
	private Map<String, FuelMatrix> voxelMatrixMap;

	private JList list;
	private JScrollPane display;
	private Panel2D panel2D;

	private Vertex3f[][] crownVoxels; // we draw these crown voxels (min, max
										// points)
	private Collection firetecVoxels; // we draw also these firetec voxels (min,
										// max points)

	private JButton front;
	private JButton top;
	private JButton right;

	private JButton seeCMap;

	private JButton continueExport;
	private JButton abortExport;
	private JButton help;

	/**
	 * Dialog construction.
	 */
	public FiretecControlDialog(PhysExporter builder, PhysDataOF physData) {
		this.stand = builder.stand;
		this.grid = builder.grids.get(0);
		this.contributionMap = physData.contributionMap;
		this.voxelMatrixMap = physData.voxelMatrixMap;

		viewPoint = ViewPoint.FRONT; // default view point

		setTitle(Translator.swap("FiretecControlDialog.title"));

		// ~ System.out.println ("FiretecControlDialog.c ()");
		// ~ System.out.println
		// ("FiretecControlDialog contributionMap: "+contributionMap.toString
		// ());
		// ~ System.out.println
		// ("FiretecControlDialog voxelMatrixMap: "+Tools.toString
		// (voxelMatrixMap));

		createUI();

		setModal(true);
		pack();
		show();
	}

	public void draw(Graphics g, Rectangle.Double r) {
		Graphics2D g2 = (Graphics2D) g;
		if (crownVoxels == null || crownVoxels.length == 0) {
			return;
		} // nothing to draw

		g2.setColor(Color.RED);
		for (int i = 0; i < crownVoxels.length; i++) {
			Vertex3f min = crownVoxels[i][0];
			Vertex3f max = crownVoxels[i][1];

			drawVoxel(g2, min, max, Color.RED, viewPoint, r);

		}

		if (firetecVoxels == null || firetecVoxels.isEmpty()) {
			return;
		} // nothing more to draw

		g2.setColor(Color.BLACK);
		for (Iterator i = firetecVoxels.iterator(); i.hasNext();) {
			CValue cvalue = (CValue) i.next();
			int fi = cvalue.fi;
			int fj = cvalue.fj;
			int fk = cvalue.fk;

			Vertex3f min = grid.coor[fi][fj][fk];
			Vertex3f max = grid.coor[fi + 1][fj + 1][fk + 1];

			drawVoxel(g2, min, max, Color.BLACK, viewPoint, r);
		}

		// Origin in Green
		g2.setColor(Color.GREEN);
		double cx = r.x + r.width / 2;
		double cy = r.y + r.height / 2;
		Shape lineToZero = new Line2D.Double(0, 0, cx, cy);
		g2.draw(lineToZero);
		Rectangle2D.Double origin = new Rectangle2D.Double(-0.125, -0.125, 0.25, 0.25);
		g2.fill(origin);

	}

	private void drawVoxel(Graphics2D g2, Vertex3f min, Vertex3f max, Color color, ViewPoint viewPoint,
			Rectangle.Double r) {

		Rectangle2D.Double face0 = null;

		if (viewPoint.equals(ViewPoint.FRONT)) {
			double w = max.x - min.x;
			double h = max.z - min.z;
			face0 = new Rectangle2D.Double(min.x, min.z, w, h);
		} else if (viewPoint.equals(ViewPoint.TOP)) {
			double w = max.x - min.x;
			double h = max.y - min.y;
			face0 = new Rectangle2D.Double(min.x, min.y, w, h);
		} else if (viewPoint.equals(ViewPoint.RIGHT)) {
			double w = max.y - min.y;
			double h = max.z - min.z;
			face0 = new Rectangle2D.Double(min.y, min.z, w, h);
		}

		Rectangle2D bBox = face0.getBounds2D();
		if (r.intersects(bBox)) {
			g2.draw(face0);
		}

	}

	public JPanel select(Rectangle.Double r, boolean ctrlIsDown) {
		return null; // selection not implemented
	}

	public void valueChanged(ListSelectionEvent evt) {
		selection = list.getSelectedValues();
		update();
	}

	private void update() {
		if (selection == null) {
			return;
		}

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		double maxZ = Double.MIN_VALUE;

		// for each crown voxel to be drawn: min, max points
		crownVoxels = new Vertex3f[selection.length][2];

		firetecVoxels = new ArrayList();

		System.out.println("*** new selection");
		int cpt = 0;
		for (int i = 0; i < selection.length; i++) {
			CKey ckey = (CKey) selection[i];

			// source
			String source = ckey.source;

			FuelMatrix cm = voxelMatrixMap.get(source);

			// crown voxel
			int ci = ckey.ci;
			int cj = ckey.cj;
			int ck = ckey.ck;

			// crownVoxels[cpt][0] = cm.coor[ci][cj][ck]; // min
			// crownVoxels[cpt][1] = cm.coor[ci + 1][cj + 1][ck + 1]; // max

			crownVoxels[cpt][0] = new Vertex3f((float) cm.getX(ci), (float) cm.getY(cj), (float) cm.getZ(ck, ci, cj));
			crownVoxels[cpt][1] = new Vertex3f((float) cm.getX(ci + 1), (float) cm.getY(cj + 1), (float) cm.getZ(
					ck + 1, ci, cj));

			minX = Math.min(minX, crownVoxels[cpt][0].x);
			minY = Math.min(minY, crownVoxels[cpt][0].y);
			minZ = Math.min(minZ, crownVoxels[cpt][0].z);
			maxX = Math.max(maxX, crownVoxels[cpt][1].x);
			maxY = Math.max(maxY, crownVoxels[cpt][1].y);
			maxZ = Math.max(maxZ, crownVoxels[cpt][1].z);

			// Consider firetec voxels in contributionMap SetMap
			Collection items = contributionMap.getObjects(ckey);
			firetecVoxels.addAll(items);

			cpt++; // next crown voxel;
		}

		Rectangle.Double userBounds = null;

		if (viewPoint.equals(ViewPoint.FRONT)) {
			userBounds = new Rectangle.Double(minX, minZ, maxX - minX, maxZ - minZ);
		} else if (viewPoint.equals(ViewPoint.TOP)) {
			userBounds = new Rectangle.Double(minX, minY, maxX - minX, maxY - minY);
		} else if (viewPoint.equals(ViewPoint.RIGHT)) {
			userBounds = new Rectangle.Double(minY, minZ, maxY - minY, maxZ - minZ);
		}
		panel2D = new Panel2D(this, userBounds, 5, 5);
		display.getViewport().setView(panel2D);
	}

	// key: t2-c(15-28-8) -> return 15 28 8
	// if error, return null
	// ~ private int[] getCiCjCk (String key) {
	// ~ try {
	// ~ int[] result = new int[3];

	// ~ int i0 = key.indexOf ('(');
	// ~ String cut = key.substring (i0+1);

	// ~ int i1 = cut.indexOf ('-');
	// ~ String ci = cut.substring (0, i1);
	// ~ cut = cut.substring (i1+1);

	// ~ i1 = cut.indexOf ('-');
	// ~ String cj = cut.substring (0, i1);
	// ~ cut = cut.substring (i1+1);

	// ~ i1 = cut.indexOf (')');
	// ~ String ck = cut.substring (0, i1);

	// ~ result[0] = Check.intValue (ci);
	// ~ result[1] = Check.intValue (cj);
	// ~ result[2] = Check.intValue (ck);
	// ~ return result;

	// ~ } catch (Exception e) {
	// ~ MessageDialog.promptError
	// ("Error in FiretecControlDialog.getCiCjCk (), "+e);
	// ~ return null;
	// ~ }
	// ~ }

	// key: t2-c(15-28-8) -> return treeId: 2
	// if error, return -1
	// ~ private int getTreeId (String key) {
	// ~ try {
	// ~ int i0 = 1;
	// ~ int i1 = key.indexOf ('-');
	// ~ String treeId = key.substring (i0, i1);
	// ~ return Check.intValue (treeId);

	// ~ } catch (Exception e) {
	// ~ MessageDialog.promptError
	// ("Error in FiretecControlDialog.getTree (), "+e);
	// ~ return -1;
	// ~ }
	// ~ }

	/**
	 * Show the contribution map in an inspector.
	 */
	private void seeCMapAction() {
		JPanel inspector = AmapTools.createInspectorPanel(contributionMap.toArray());
		String title = Translator.swap("FiretecControlDialog.inspectionOfCMap");
		boolean modal = true;
		new DialogWithClose(this, inspector, title, modal);
	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(front)) {
			viewPoint = ViewPoint.FRONT;
			update();
		} else if (evt.getSource().equals(top)) {
			viewPoint = ViewPoint.TOP;
			update();
		} else if (evt.getSource().equals(right)) {
			viewPoint = ViewPoint.RIGHT;
			update();
		} else if (evt.getSource().equals(continueExport)) {
			setValidDialog(true);
		} else if (evt.getSource().equals(seeCMap)) {
			seeCMapAction();
		} else if (evt.getSource().equals(abortExport)) {
			setValidDialog(false);
		} else if (evt.getSource().equals(abortExport)) {
			setValidDialog(false);
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Called on escape : ask for confirmation.
	 */
	@Override
	protected void escapePressed() {
		// ~ if (Question.isTrue (Translator.swap
		// ("FiretecControlDialog.confirm"),
		// ~ Translator.swap ("FiretecControlDialog.confirmClose"))) {
		// ~ dispose ();
		// ~ }
	}

	/**
	 * Initializes the GUI.
	 */
	private void createUI() {

		ColumnPanel main = new ColumnPanel(Translator.swap("FiretecControlDialog.crownVoxels"));

		// Scene characteristics
		// ~ LinePanel part0 = new LinePanel ();
		// ~ main.add (part0);

		// 1. a list of crown voxels
		Vector v = new Vector(contributionMap.keySet());
		Collections.sort(v);
		list = new JList(v);
		list.addListSelectionListener(this);

		main.add(new JScrollPane(list));
		main.addStrut0();

		// 2. a panel2D in a JScrollPane, to show a drawing of the list
		// selection
		display = new JScrollPane();

		// A lateral panel for view point
		ColumnPanel lateral = new ColumnPanel();
		front = new JButton(Translator.swap("FiretecControlDialog.front"));
		front.addActionListener(this);
		lateral.add(LinePanel.addWithStrut0(front));
		top = new JButton(Translator.swap("FiretecControlDialog.top"));
		top.addActionListener(this);
		lateral.add(LinePanel.addWithStrut0(top));
		right = new JButton(Translator.swap("FiretecControlDialog.right"));
		right.addActionListener(this);
		lateral.add(LinePanel.addWithStrut0(right));
		lateral.addStrut0();

		// 2. Control panel (ok cancel help);
		JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		seeCMap = new JButton(Translator.swap("FiretecControlDialog.seeCMap"));
		continueExport = new JButton(Translator.swap("FiretecControlDialog.continueExport"));
		abortExport = new JButton(Translator.swap("FiretecControlDialog.abortExport"));
		help = new JButton(Translator.swap("Shared.help"));
		pControl.add(seeCMap);
		pControl.add(continueExport);
		pControl.add(abortExport);
		pControl.add(help);
		seeCMap.addActionListener(this);
		continueExport.addActionListener(this);
		abortExport.addActionListener(this);
		help.addActionListener(this);

		// sets ok as default (see AmapDialog)
		// ~ ok.setDefaultCapable (true);
		// ~ getRootPane ().setDefaultButton (ok);

		JLabel explanation = new JLabel("<html>" + Translator.swap("FiretecControlDialog.explanation") + "</html>");

		getContentPane().add(explanation, BorderLayout.NORTH);
		getContentPane().add(main, BorderLayout.WEST);
		getContentPane().add(display, BorderLayout.CENTER);
		getContentPane().add(new NorthPanel(lateral), BorderLayout.EAST);
		getContentPane().add(pControl, BorderLayout.SOUTH);
	}

}
