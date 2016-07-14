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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Economic settings.
 * 
 * @author dethier.o, gl - July 2013
 */
public class EconomicSettings{
	
	private double discountRate = -1; //either the discount rate
	private double land = -1;		//or land can be specified!
	private List<EconomicOperation> operations;
	private Map<Integer, List<Double[]>> pricesByDbhAndSpecies; //ordered by the first double in the array
	private String fileName;
	
	public EconomicSettings(){
		operations = new ArrayList<EconomicOperation>();
		pricesByDbhAndSpecies = new HashMap<Integer, List<Double[]>>();
	}
	
	public void addEconomicOperation(EconomicOperation eo){
		operations.add (eo);
	}
	
	public void addPrice(double dbh, double price, int species){
		// Create a new List for the species if doesn't exist
		if (!pricesByDbhAndSpecies.containsKey(species))
			pricesByDbhAndSpecies.put(species, new ArrayList<Double[]>());
		// Retrieve the list and add the prices for the Dbh
		List<Double[]> pricesByDbh = pricesByDbhAndSpecies.get(species);	
		pricesByDbh.add (new Double[] {dbh, price});
		// Check if the order is respected and reorder if not
		int size = pricesByDbh.size ();
		if (size > 1 && pricesByDbh.get (size - 1)[0] < pricesByDbh.get (size - 2)[0])
		{
			Collections.sort(pricesByDbh, new Comparator<Double[]>() {
				public int compare(Double[] d1, Double[] d2)
				{
					return (int) (d1[0] - d2[0]);
				}
			});	
		}
	}
	
	/**
	 * Return the price for the species and the dbh range where the input belongs.
	 */
	public double getPriceByDbh(double dbh, int species) throws Exception {
		if (!pricesByDbhAndSpecies.containsKey(species))
		{
			throw new Exception("Error with EconomicSetting.getPriceByDbh : unknown species");
		}
		else
		{
			List<Double[]> pricesByDbh = pricesByDbhAndSpecies.get(species);
			
			if ((pricesByDbh.size () == 0) || (dbh < 0) || 
					(dbh >= pricesByDbh.get (pricesByDbh.size () - 1)[0]))
			{
				throw new Exception("Error with EconomicSettings.getPrice : " + 
						"there is no existing dbh range");
			}
			else
			{
				int i = 0;
			
				while (i < pricesByDbh.size () && pricesByDbh.get (i)[0] <= dbh)
					i++;
				return pricesByDbh.get (i)[1];		
			}
		}
	}
		
	public List<EconomicOperation> getOperations(){return operations;}
	public List<Double[]> getPricesForSpecies(int species){return pricesByDbhAndSpecies.get(species);}
	public void setDiscountRate(double dr) {discountRate = dr;}
	public double getDiscountRate(){return discountRate;}
	public void setLand(double f){land = f;}
	public double getLand() {return land;}
	public String getFileName () {return fileName;}
	public void setFileName (String fileName) {this.fileName = fileName;}
}
