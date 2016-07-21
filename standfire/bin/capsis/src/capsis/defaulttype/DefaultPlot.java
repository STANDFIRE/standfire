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
package capsis.defaulttype;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Vertex3d;
import capsis.kernel.AbstractPlot;
import capsis.kernel.Cell;

/**
 * Default implementation of a plot (without cell)
 * 
 * @author sdufour
 * 
 */
public class DefaultPlot<T extends Cell> extends AbstractPlot<T> implements
		Cloneable {

	private static final long serialVersionUID = 1L;

	private Shape shape;

	@Override
	/** Do nothing */
	public void addCell(T cell) {
	}

	@Override
	public String bigString() {
		return "DefaultPlot";
	}

	@Override
	/** Do Nothing */
	public Shape getShape() {
		if (shape == null) {
			double x = getOrigin().x;
			double y = getOrigin().y;
			double w = getXSize();
			double h = getYSize();
			shape = new Rectangle2D.Double(x, y, w, h);
		}
		return shape;
	}

	@Override
	/** Return Empty list */
	public Collection<Vertex3d> getVertices() {
		return new ArrayList<Vertex3d>();
	}

	@Override
	/** Return null */
	public T matchingCell(Spatialized t) {

		return null;
	}

	@Override
	/** Do Nothing */
	public void setVertices(Collection<Vertex3d> c) {
	}

}
