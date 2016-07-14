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
public interface FishAdditionInterface {

	public float getSexRatio1 () ;
	public int getInitFishNumber1 ();
	public float getInitMinForkLength1 ();
	public float getInitMaxForkLength1 ();
	public byte getMinAge1 () ;
	public byte getMaxAge1 () ;
	public float getSexRatio2 () ;
	public int getInit0plus () ;
	public int getInit1plus () ;
	public int getInit2plus () ;
	public int getInit3plus () ;
	public int getInit4plus () ;
	public boolean getPerHectare () ;
	public boolean getByAge1 () ;
	public boolean getByForkLength1 () ;
	public boolean getStructuredPop () ;
	public boolean getNonStructuredPop () ;




}
