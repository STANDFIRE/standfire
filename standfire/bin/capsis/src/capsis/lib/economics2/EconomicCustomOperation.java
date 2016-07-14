/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2012  Francois de Coligny et al.
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
package capsis.lib.economics2;

import java.util.List;

import capsis.kernel.GScene;


/**
 * A custom economic operation in the economics2 package: is applied on the
 * underlying scene at construction time, can change some of its properties.
 * 
 * @author G. Ligot, F. de Coligny - January 2012
 */
public abstract class EconomicCustomOperation extends EconomicOperation {
	
	/**
	 * Constructor
	 */
	public EconomicCustomOperation(String label, Type type,
			Trigger trigger, boolean income, double price) {
		super (label, type, trigger, income, price);

	}

	abstract public GScene getScene();
	
	/**
	 * Link and initialize EconomicOperation to EconomicScenario. 
	 * It must add a full featured EconomicOperation to the EconomicScenario.
	 */
	abstract public void initEconomic();
}
