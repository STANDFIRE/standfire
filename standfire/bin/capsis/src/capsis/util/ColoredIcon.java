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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * A colored icon.
 * 
 * @author F. de Coligny - may 2003
 */
public class ColoredIcon extends ImageIcon {
	private Color color;
	private int width;
	private int height;
	
	/**
	 * Constructor
	 */
	public ColoredIcon (Color color, int width, int height) {
		super ();
		this.color = color;
		this.width = width;
		this.height = height;
		init ();
	}

	/**
	 * Default constructor, default width and height
	 */
	public ColoredIcon (Color color) {
		this (color, 10, 5);
	}
	
	/**
	 * Creates and sets the image (ImageIcon.paintIcon() will paint it when needed)
	 */
	private void init () {
		Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		createColoredImage(image.getGraphics(), width, height);
		setImage (image);
	}
	
	/**
	 * Draws a colored rectangle with the given size in the given graphics
	 */
	private void createColoredImage (Graphics g, int width, int height) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = new Rectangle (0, 0, width, height);
		Color memory = g2.getColor ();
		g2.setColor (color);
		g2.fill (r);
		g2.setColor (memory);
		
	}
	
//	public synchronized void paintIcon (Component c, Graphics g, int x, int y) {
//		Graphics2D g2 = (Graphics2D) g;
//		Rectangle r = new Rectangle (x, y, 10, 5);
//		Color memory = g2.getColor ();
//		g2.setColor (color);
//		g2.fill (r);
//		g2.setColor (memory);
//		
//	}
	
//	public int getIconWidth () {return 15;}
}





