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
package capsis.util;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;

/**
 * JWidthCheckBox is a JCheckBox with preferred size chosen.
 * The width is the max of the checkbox's length in the current font and the value passed as parameter.
 * The text is always completely visible. The height is the one of the component font.
 * 
 * @author F. de Coligny - november 2003
 */
public class JWidthCheckBox extends JCheckBox {

	public JWidthCheckBox (String lab, int wid) {
		super (lab);
		int labLen = getFontMetrics (getFont ()).stringWidth (lab);  // length of the label in the font in pixels
		int fontHeight = getFontMetrics (getFont ()).getHeight ();
		wid+=50;	// additional space for selector
		labLen+=50;	// additional space for selector
		setPreferredSize (new Dimension (wid >= labLen?wid:labLen, fontHeight));	// better
	}

	public JWidthCheckBox (int wid) {
		this ("", wid);
	}
	
	// Test method, unused in normal mode
	// fc - 26.11.2003
	public static void main (String[] args) {
		JFrame f = new JFrame ("JWidthCheckBox test");
		JPanel content = (JPanel) f.getContentPane ();
		
		ColumnPanel p = new ColumnPanel ();
		
		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthCheckBox ("Small label", 200));
		l0.add (new JTextField (5));
		l0.addStrut0 ();
		
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthCheckBox ("Very large label", 200));
		l1.add (new JTextField (5));
		l1.addStrut0 ();
		
		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthRadioButton ("Case 1", 200));
		l2.add (new JTextField (5));
		l2.addStrut0 ();
		
		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthRadioButton ("Case 2 and more", 200));
		l3.add (new JTextField (5));
		l3.addStrut0 ();
		
		p.add (l0);
		p.add (l1);
		p.add (l2);
		p.add (l3);
		p.addGlue ();
		
		content.setLayout (new BorderLayout ());
		content.add (p, BorderLayout.NORTH);
		
		
		f.setSize (500, 400);
		f.setVisible (true);
		
	}
}