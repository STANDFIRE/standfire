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

import jeeb.lib.util.Log;
import capsis.kernel.Project;


/**
 * Economic settings.
 * 
 * @author dethier.o, gl - July 2013
 */
public class EconomicSettings{
	
	private Double discountRate; 
	private Double land;		
	private List<EconomicOperation> operations;
	private Map<Integer, List<Double[]>> pricesByDbhAndSpecies; //ordered by dbh, double[0] = dbh, double[1] = price
	private static Map<Integer, String> speciesList; 
	private String fileName;
	
	public EconomicSettings(){
		operations = new ArrayList<EconomicOperation>();
		pricesByDbhAndSpecies = new HashMap<Integer, List<Double[]>>();
	}
	
	public void addEconomicOperation(EconomicOperation eo){
		operations.add (eo);
	}
	
	/**
	 * Return a list of EconomicPriceRecord (used for eg. in EconomicModelTool)
	 */
	public List<EconomicPriceRecord> getPriceRecords(){
		List<EconomicPriceRecord> records = new ArrayList<EconomicPriceRecord>();
		for(Integer sp : pricesByDbhAndSpecies.keySet()){
			List<Double[]> prices = pricesByDbhAndSpecies.get(sp);
			for(int i = 0; i<prices.size(); i++){
				Double[] d = prices.get(i);
				EconomicPriceRecord r = new EconomicPriceRecord();
				r.species = sp;
				r.dbh = d[0];
				r.price = d[1];
				records.add(r);
			}
		}
		return records;
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
	
	/**convert species code in species name
	 */
	public static String getSpeciesName(Integer speciesCode){
		if(speciesList.containsKey(speciesCode)){
			return speciesList.get(speciesCode);
		}else{
			Log.println ("economics2", "EconomicScenario.getSpeciesName() - species code not available"); 
			System.out.println("EconomicScenario.getSpeciesName() - species code not available");
			return Integer.toString(speciesCode); 
		}
	}
	
	/**convert species name in species code
	 */
	public static Integer getSpeciesCode(String speciesName){
		for(Integer spCode : speciesList.keySet()){
			String name = speciesList.get(spCode);
			if(name.equals(speciesName)) return spCode;
		}
		Log.println ("economics2", "EconomicScenario.getSpeciesCode() - species name not found"); 
		System.out.println("EconomicScenario.getSpeciesCode() - species name not found");
		return null;
	}
	
	/**
	 * This method provide a map with the list of species code (key) and name (value)
	 * This method was intended to be used on initial scene so to capture the greatest diversity?
	 * Depending on model this method might not be sufficient... Then the speciesList should be given using the setter!
	 */
	public void createSpeciesList(EconomicScene scene){
		Map<Integer, String> speciesList = new HashMap<Integer, String>();
		
		List<EconomicTree> trees = scene.getLivingEconomicTrees();
		
		for(EconomicTree t : trees){
			if(!speciesList.containsKey(t.getSpeciesValue())){
				speciesList.put(t.getSpeciesValue(), t.getSpeciesName());
			}			
		}
		this.speciesList = speciesList;
	}

	public List<EconomicOperation> getOperations(){return operations;}
	public void setOperations(List<EconomicOperation> operations){this.operations = operations;}

	public Map<Integer, List<Double[]>> getPricesByDbhAndSpecies() {return pricesByDbhAndSpecies;}
	public void setPricesByDbhAndSpecies(Map<Integer, List<Double[]>> pricesByDbhAndSpecies) {this.pricesByDbhAndSpecies = pricesByDbhAndSpecies;}
	
	public List<Double[]> getPricesForSpecies(int species){return pricesByDbhAndSpecies.get(species);}
	public void setDiscountRate(Double dr) {discountRate = dr;}
	public Double getDiscountRate(){return discountRate;}
	public void setLand(double f){land = f;}
	public Double getLand() {return land;}
	public String getFileName () {return fileName;}
	public void setFileName (String fileName) {this.fileName = fileName;}
	
	public Map<Integer, String> getSpeciesList() {return speciesList;}
	public void setSpeciesList(Map<Integer, String> speciesList) {this.speciesList = speciesList;}
}
