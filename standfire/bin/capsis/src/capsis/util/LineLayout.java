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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Lays out components on lines inside the container. Each line contents
 * up to lineSize components. When a line is full, a new one is created.
 * Note: 1.1 version.  For the 1.2 version, you would probably
 *       change getSize() to getWidth() or getHeight().
 * Note: to be used by capsis outputBox to lay out outputs.
 * Note: Original is DiagonalLayout from Swing tutorial.
 * 
 * @author ???
 */
public class LineLayout implements LayoutManager {
    private int lineSize;
    private int minWidth = 0, minHeight = 0;
    private int preferredWidth = 0, preferredHeight = 0;
    private boolean sizeUnknown = true;

    public LineLayout() {
        this(5);
    }

    public LineLayout(int n) {
        lineSize = n;
    }

    /** Required by LayoutManager. */
    public void addLayoutComponent(String name, Component comp) {
    }

// may replace preceding method for j2se 1.3
/*	public void addLayoutComponent (Component comp, Object obj) {} */

    /** Required by LayoutManager. */
    public void removeLayoutComponent(Component comp) {
    }

    private void setSizes(Container parent) {
        //int nComps = parent.getComponentCount();
		Component c = null;
        Dimension d = null;
		int l = 0;
		int localWidth = 0;
		int localHeight = 0;

        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;

		// load components in a Vector to enumerate them
		Component comp[] = parent.getComponents ();
		Vector components = new Vector ();
		for (int i = 0; i < comp.length; i++) {
			components.add (comp [i]);
		}
		Enumeration enu = components.elements ();

		c = readNextComponent (enu);

		while (c != null) {
			l = 1;
			localWidth = 0;
			localHeight = 0;
			while ((c != null) && (l <= lineSize)) {
				d = c.getPreferredSize();
				localWidth += d.width;
				localHeight = Math.max(localHeight, d.height);
				minWidth = Math.max(minWidth, d.width);
				minHeight = Math.max(minHeight, d.height);

				c = readNextComponent (enu);
				l += 1;
			}
			preferredWidth = Math.max(localWidth, preferredWidth);
			preferredHeight += localHeight;
		}
    }


    /** Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        //int nComps = parent.getComponentCount();

        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth
                    + insets.left + insets.right;
        dim.height = preferredHeight
                     + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

    /** Required by LayoutManager. */
    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        //int nComps = parent.getComponentCount();

        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = minWidth
                    + insets.left + insets.right;
        dim.height = minHeight
                     + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

	/** Read next component in Enumeration. */
	private Component readNextComponent (Enumeration enu) {
		Component c = null;
		if (enu.hasMoreElements ()) {
			c = (Component) enu.nextElement ();
		}
		return c;
	}

	/**
	 * Required by LayoutManager.
	 *
	 * This is called when the panel is first displayed,
	 * and every time its size changes.
	 * Note: You CAN'T assume preferredLayoutSize or
	 * minimumLayoutSize will be called -- in the case
	 * of applets, at least, they probably won't be.
	 */
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width
                       - (insets.left + insets.right);
        int maxHeight = parent.getSize().height
                        - (insets.top + insets.bottom);
        //int nComps = parent.getComponentCount();
        int x = 0, y = 0;
		Component c = null;
        Dimension d = null;
		int l = 0;
		int localWidth = insets.left;
		int localHeight = insets.top;

        // Go through the components' sizes, if neither
        // preferredLayoutSize nor minimumLayoutSize has
        // been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

		// load components in a Vector to enumerate them
		Component comp[] = parent.getComponents ();
		Vector components = new Vector ();
		for (int i = 0; i < comp.length; i++) {
			components.add (comp [i]);
		}
		Enumeration enu = components.elements ();

		c = readNextComponent (enu);

		while (c != null) {
			l = 1;
			localWidth = insets.left;
			y += localHeight;
			while ((c != null) && (l <= lineSize)) {
				d = c.getPreferredSize();
				x = localWidth;
				localWidth += d.width;
				localHeight = Math.max(localHeight, d.height);

                // Set the component's size and position.
                c.setBounds(x, y, d.width, d.height);

				c = readNextComponent (enu);
				l += 1;
			}
		}
    }

    public String toString() {
		StringBuffer b = new StringBuffer ();
		b.append (getClass().getName());
		b.append ("[lineSize=");
		b.append (lineSize);
		b.append ("]");
		return b.toString ();
	}
}

/* MEMENTO
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width
                       - (insets.left + insets.right);
        int maxHeight = parent.getSize().height
                        - (insets.top + insets.bottom);
        int nComps = parent.getComponentCount();
        int previousWidth = 0, previousHeight = 0;
        int x = 0, y = insets.top;
        int rowh = 0, start = 0;
        int xFudge = 0, yFudge = 0;
        boolean oneColumn = false;

        // Go through the components' sizes, if neither
        // preferredLayoutSize nor minimumLayoutSize has
        // been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

        if (maxWidth <= minWidth) {
            oneColumn = true;
        }

        if (maxWidth != preferredWidth) {
            xFudge = (maxWidth - preferredWidth)/(nComps - 1);
        }

        if (maxHeight > preferredHeight) {
            yFudge = (maxHeight - preferredHeight)/(nComps - 1);
        }

        for (int i = 0 ; i < nComps ; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();

                 // increase x and y, if appropriate
                if (i > 0) {
                    if (!oneColumn) {
                        x += previousWidth/2 + xFudge;
                    }
                    y += previousHeight + vgap + yFudge;
                }

                // If x is too large,
                if ((!oneColumn) &&
                    (x + d.width) >
                    (parent.getSize().width - insets.right)) {
                    // reduce x to a reasonable number.
                    x = parent.getSize().width
                        - insets.bottom - d.width;
                }

                // If y is too large,
                if ((y + d.height)
                    > (parent.getSize().height - insets.bottom)) {
                    // do nothing.
                    // Another choice would be to do what we do to x.
                }

                // Set the component's size and position.
                c.setBounds(x, y, d.width, d.height);

                previousWidth = d.width;
                previousHeight = d.height;
            }
        }
    }
*/