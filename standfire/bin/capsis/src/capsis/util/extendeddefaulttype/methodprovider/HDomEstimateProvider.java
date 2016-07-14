/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2015 Mathieu Fortin
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
package capsis.util.extendeddefaulttype.methodprovider;

import java.util.Collection;

import repicea.stats.estimates.Estimate;
import capsis.kernel.GScene;

/**
 * This interface ensures the stand can provide an estimate of its dominant height. This interface is 
 * required for confidence interval calculation.
 * @author Mathieu Fortin - June 2015
 */
public interface HDomEstimateProvider {

	/**	
	 * This method returns an Estimate of the dominant height.
	 * @param compositeStand a GScene instance
	 * @param trees a collection of Tree instances
	 * @return an Estimate instance
	 */
	public Estimate<?> getHdomEstimate(GScene compositeStand, Collection trees);

}
