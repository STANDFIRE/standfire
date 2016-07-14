/* 
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
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
package capsis.lib.samsaralight;

import java.util.List;

/**
 * SLLightableTree - a tree with a SLTreeLight light properties object.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public interface SLLightableTree {

	public SLTreeLight getTreeLight();

	public void setTreeLight(SLTreeLight t);

	// The crown parts of the tree (fraction of crown)
	public List<SLCrownPart> getCrownParts();

	// The trunk (a cylinder so far)
	public SLTrunk getTrunk();

	// [0,1], MJ/MJ
	public double getCrownTransmissivity();

	// fc-15.5.2014 MJ found double energy in Heterofor after an intervention
	public void resetEnergy () ;
	
}
