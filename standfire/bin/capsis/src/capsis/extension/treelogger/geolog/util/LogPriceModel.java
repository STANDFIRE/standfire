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

package capsis.extension.treelogger.geolog.util;

import java.io.Serializable;

/**	
 * LogPriceModel : a simple model of log price in Euro / m3
 * based on median diameter
 *	@author F. Mothe - july 2006
 */
public class LogPriceModel implements Serializable {
	
	public double slope;
	public double intercept;
	
	public LogPriceModel (double intercept, double slope) {
		this.intercept = intercept;
		this.slope = slope;
	}
	public double getPrice_Epm3 (double medianDiam_cm) {
		double price = slope * medianDiam_cm + intercept;
		return Math.max (price, 0.);
	}
	
	@Override
	public boolean equals(Object obj) {
		LogPriceModel refCategory = (LogPriceModel) obj;
		
		if (refCategory.slope != this.slope) {return false;}
		if (refCategory.intercept != this.intercept) {return false;}
		
		return true;
	}
	
}

