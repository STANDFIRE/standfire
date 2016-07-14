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

import java.awt.Dimension;

import javax.swing.JTextField;

/**
 * JWidthTextField is a JTextField with preferred size chosen.
 * The width is the max of the field's length in the current font and the value passed as parameter.
 * The field text is always completely visible. The height is the one of the component font.
 * 
 * @author F. de Coligny - november 2003
 */
public class JWidthTextField extends JTextField {

	/**	
	 * Main constructor
	 */
	public JWidthTextField (int size, int wid, boolean fixedMin, boolean fixedMax) {
		super (size);	// approx in characters, ex : 5 means 5 chars
		StringBuffer buffer = new StringBuffer ();
		for (int i = 0; i < size; i++) {
			buffer.append ("8");
		}
		
		int labLen = getFontMetrics (getFont ()).stringWidth (buffer.toString ());  // length of the label in the font in pixels
		int height = getPreferredSize ().height;
		setPreferredSize (new Dimension (wid >= labLen?wid:labLen, height));	// better
		if (fixedMin) {setMinimumSize (getPreferredSize ());}
		if (fixedMax) {setMaximumSize (getPreferredSize ());}
	}

	/**	Constructor 2.
	*/
	public JWidthTextField (int size, int wid) {
		this (size, wid, false, false);
	}
	
	/**	Constructor 3.
	*/
//	public JWidthTextField (int wid) {
//		this (1, wid);
//	}
	public JWidthTextField (int width) {
		super ();
		int height = getPreferredSize ().height;
		setPreferredSize (new Dimension (width, height));
		setMinimumSize (getPreferredSize ());
		setMaximumSize (getPreferredSize ());
		
	}
	
}