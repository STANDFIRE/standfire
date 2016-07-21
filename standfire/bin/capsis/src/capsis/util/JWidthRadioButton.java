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

import javax.swing.JRadioButton;

/**
 * JWidthRadioButton is a JRadioButton with preferred size chosen.
 * The width is the max of the radio's length in the current font and the value passed as parameter.
 * The radio text is always completely visible. The height is the one of the component font.
 * 
 * @author F. de Coligny - october 2003
 */
public class JWidthRadioButton extends JRadioButton {

	public JWidthRadioButton (String lab, int wid) {
		super (lab);
		int labLen = getFontMetrics (getFont ()).stringWidth (lab);  // length of the label in the font in pixels
		int fontHeight = getFontMetrics (getFont ()).getHeight ();
		wid+=50;	// additional space for selector
		labLen+=50;	// additional space for selector
		setPreferredSize (new Dimension (wid >= labLen?wid:labLen, fontHeight));	// better
	}

	public JWidthRadioButton (int wid) {
		this ("", wid);
	}
}