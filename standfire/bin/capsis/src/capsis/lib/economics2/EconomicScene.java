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

/**
 * A scene in the economics2 package.
 * 
 * @author G. Ligot, F. de Coligny - January 2012
 */
public interface EconomicScene {
	
	/**
	 * Get the date of the current scene
	 */
	public int getDate ();
	
	/**
	 * Get the plot area in m2
	 */
	public double getArea (); // m2
	
	/**
	 * This list includes every living trees
	 */
	public 	List<EconomicTree> getLivingEconomicTrees ();
	
	/**
	 * This list includes every harvested trees; The list is empty if no cutting occurred
	 */
	public 	List<EconomicTree> getHarvestedEconomicTrees ();

	/**
	 * Convenient method to store a  list of cut trees in XScene
	 * Its use is optional!
	 */
	public void setHarvestedEconomicTrees(List<EconomicTree> trees);
	
	/**
	 * method that return an economicOperation. = null if any
	 */
	public EconomicOperation getEconomicOperation();
	
//	/**
//	 * Convenient method to store a particular list of affected trees in XScene
//	 * Its use is optional!
//	 */
//	public void setLivingEconomicTrees(List<EconomicTree> trees); 
	
//	 * This list includes only cut trees
//	 */
//	public 	List<EconomicTree> getCutEconomicTrees ();
//
//		/**
//	 * Convenient method to store a particular list of cut trees in XScene
//	 * Its use is optional!
//	 */
//	public void setCutEconomicTrees(List<EconomicTree> trees);
//	
//	/**
//	 * Convenient method to store a particular list of affected trees in XScene
//	 * Its use is optional!
//	 */
//	public void setLivingEconomicTrees(List<EconomicTree> trees); 
//	
//	/**get the number of affected trees.
//	 * It is usually the total number of all trees except after intervention (thinning, tree damage, ...)
//	 * @return the number of trees per hectare
//	 */
//	public double getAffectedTreeNumber_ha ();
//	
//	/**
//	 * Convenient method that might be used in XThinner to store tree number in XScene
//	 * Its use is optional!
//	 */
//	public void setAffectedTreeNumber (int treeNumber); 
//	
//	/**get the volume of affected trees.
//	 * It is usually the total volume of all trees except after intervention (thinning, tree damage, ...)
//	 * @return total affected volume in m3
//	 */
//	public double getAffectedVolume ();
//	
//	/**
//	 * Convenient method that might be used in XThinner to store affected timber volume in XScene
//	 * Its use is optional!
//	 */
//	public void setAffectedVolume (double volume);
//	
//	
//	/**get the volume of harvested trees.
//	 * It is zero except after cutting trees
//	 * @return total volume of harvested timber
//	 */
//	public double getHarvestedVolume ();
//	
//	/**
//	 * Convenient method that might be used in XThinner to store harvested timber volume in XScene
//	 * Its use is optional!
//	 */
//	public void setHarvestedVolume (double volume);
	
	
}
