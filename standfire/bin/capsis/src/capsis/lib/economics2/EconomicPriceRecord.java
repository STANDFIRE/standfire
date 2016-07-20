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

import java.io.Serializable;

import jeeb.lib.util.Record;

/**
 * Recording containing a price for a dbh (upper) limit and by species.
 * 
 * @author dethier.o - July 2013
 */
public class EconomicPriceRecord extends Record implements Serializable 
{
	
	public EconomicPriceRecord()
	{
		super();
	}
	
	public EconomicPriceRecord(String line) throws Exception
	{
		super(line);
	}
	
	public double dbh;
	public double price;
	public int species;
}