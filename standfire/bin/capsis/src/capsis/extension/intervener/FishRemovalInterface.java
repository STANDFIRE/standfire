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

package capsis.extension.intervener;


/**
 * Can be used to create a FishAddition intervener without interactive dialog:
 * gives the requested data to the intervener.
 *
 * @author J. Labonne - may 2005
 */
public interface FishRemovalInterface {


	public float getMinFishingSize () ;
	public float getMaxFishingSize () ;
	public int getTotalFishing () ;
	public int getOrder1Fishing () ;
	public int getOrder2Fishing () ;
	public int getOrder3Fishing () ;
	public int getOrder4Fishing () ;
	public int getOrder5Fishing () ;
	public int getOrder6Fishing () ;
	public boolean getPerHectareFishing () ;
	public boolean getHomogeneousFishing ();
	public boolean getPerOrderFishing () ;




}
