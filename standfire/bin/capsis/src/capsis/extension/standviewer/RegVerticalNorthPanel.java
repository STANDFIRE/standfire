/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
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
package capsis.extension.standviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Translator;
import capsis.util.Drawer;
import capsis.util.Panel2D;

/**
 * RegVerticalNorthPanel - A component to see North as a vertical arrow.
 *
 * @author A. Piboule - November 2003
 * simplication for SVRegeLight of MountCompass from B. Courbaud
 */
public class RegVerticalNorthPanel extends JPanel implements Drawer {

	protected Panel2D panel2D;
	private Font tableFont = new JTable ().getFont ();


	public RegVerticalNorthPanel () {

		Rectangle.Double r2 = new Rectangle.Double (-1, -1, 2, 2);

		panel2D = new Panel2D (this,
				r2,
				0, 	// x margin
				0);	// y margin
		panel2D.setPreferredSize (new Dimension (40, 40));
		panel2D.setZoomEnabled (false);
		panel2D.setSelectionEnabled (false);
		panel2D.setMoveEnabled (false);

		setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
		add (panel2D);

		JLabel n = new JLabel (Translator.swap ("RegVerticalNorthPanel.North"));
		n.setFont (tableFont);
		n.setForeground (Color.black);
		ColumnPanel l = new ColumnPanel (2, 0);
		l.add (n);
		l.addGlue ();
		l.setBackground (Color.white);

		add (l);

	}


	/**
	 * From Drawer interface.
	 * This method draws in the Panel2D each time this one must be
	 * repainted.
	 */

	public void draw (Graphics g, Rectangle.Double r) {

		Graphics2D g2 = (Graphics2D) g;

		// North arrow
		double alpha = Math.PI/2d;

		double x = 0;
		double y = 1d;

		double beta = Math.toRadians (30);
		double l = 0.3d;

		double x1 = x - l*Math.cos (alpha+beta);
		double y1 = y - l*Math.sin (alpha+beta);

		double x2 = x - l*Math.cos (beta-alpha);
		double y2 = y + l*Math.sin (beta-alpha);

		g2.setColor (Color.black);

		Shape north = new Line2D.Double (0, 0, x, y);
		Shape nBranch1 = new Line2D.Double (x, y, x1, y1);

		Shape nBranch2 = new Line2D.Double (x, y, x2, y2);

		g2.draw (north);
		g2.draw (nBranch1);
		g2.draw (nBranch2);

	}


	/**
	 * From Drawer interface.
	 * We may receive (from Panel2D) a selection rectangle (in user space i.e. meters)
	 * and return a JPanel containing information about the objects (trees) inside
	 * the rectangle.
	 * If no objects are found in the rectangle, return null.
	 */

	public JPanel select (Rectangle.Double r, boolean ctrlIsDown) {

		return null;	// no selection allowed here
	}


}







