/* 
 * The Regeneration library for Capsis4
 * 
 * Copyright (C) 2008  N. Donès, Ph. Balandier, N. Gaudio
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
package capsis.lib.regeneration;

import jeeb.lib.util.Identifiable;
import capsis.defaulttype.Speciable;

/**
 * RGTree is a Tree with regeneration description.
 * 
 * @author N. Donès, Ph. Balandier, N. Gaudio - october 2008
 */
public interface RGTree extends Speciable, Identifiable, RGLightable {

	// getSpecies ()
	// getId ()

	/**
	 * Returns the energy in MJ intercepted by this tree at last radiative
	 * balance time.
	 * Updated in processLighting () or by an external light model.
	 */
	public double getEnergy_MJ(); // MJ

//	public void processGrowth();

}
