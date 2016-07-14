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

import java.io.Serializable;

/**
 * SLTreeLight - light related properties of a tree.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLTreeLight implements Serializable {

	private SLLightableTree connectedTree; // The tree which we are the light
											// properties

	private int impactNumber;

	/**
	 * Constructor.
	 */
	public SLTreeLight(SLLightableTree connectedTree) {
		this.connectedTree = connectedTree;
	}

	/**
	 * Sometimes, the light is not calculated each year to save time and copies
	 * are used instead.
	 */
	public SLTreeLight getCopy(SLLightableTree newConnectedTree) {
		SLTreeLight tl = new SLTreeLight(newConnectedTree);
		tl.impactNumber = impactNumber;

		return tl;
	}

	public double getTrunkDirectEnergy() {
		return connectedTree.getTrunk().getDirectEnergy();
	}

	public double getTrunkDiffuseEnergy() {
		return connectedTree.getTrunk().getDiffuseEnergy();
	}

	public double getTrunkPotentialEnergy() {
		return connectedTree.getTrunk().getPotentialEnergy();
	}

	public double getTrunkEnergy() {
		return getTrunkDirectEnergy() + getTrunkDiffuseEnergy();
	}

	public double getCrownDirectEnergy() {
		double sum = 0;
		for (SLCrownPart p : connectedTree.getCrownParts()) {
			sum += p.getDirectEnergy();
		}
		return sum;
	}

	public double getCrownDiffuseEnergy() {
		double sum = 0;
		for (SLCrownPart p : connectedTree.getCrownParts()) {
			sum += p.getDiffuseEnergy();
		}
		return sum;
	}

	public double getCrownPotentialEnergy() {
		double sum = 0;
		for (SLCrownPart p : connectedTree.getCrownParts()) {
			sum += p.getPotentialEnergy();
		}
		return sum;
	}

	public double getCrownEnergy() {
		return getCrownDirectEnergy() + getCrownDiffuseEnergy();
	}

	public int getImpactNumber() {
		return impactNumber;
	}

	public void setImpactNumber(int n) {
		impactNumber = n;
	}

	public void incrementImpactNumber() {
		impactNumber += 1;
	}

	public void resetImpactNumber() {
		impactNumber = 0;
	}

}
