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

import java.util.Hashtable;
import java.util.List;

/**
 * Economic description of a stand at one point of time
 * @author GL - 15/06/2016
 * TODO : add values of trees if land and interest rate are known
 *
 */
public class EconomicStandDescription {

		private EconomicScene scene;
		private EconomicSettings settings;
		private double treeNumber;
		private double meanDiameter;
		private double basalArea;
		private double volume;
		private double marketValue;
		private Hashtable<Integer, String> speciesNames;
		private Hashtable<Integer,Double> treeNumberBySpecies;
		private Hashtable<Integer,Double> meanDiameterBySpecies;
		private Hashtable<Integer,Double> basalAreaBySpecies;
		private Hashtable<Integer,Double> volumeBySpecies;
		private Hashtable<Integer,Double> marketValueBySpecies;


		EconomicStandDescription(EconomicScene scene, EconomicSettings settings){
			this.scene = scene;
			this.settings = settings;

			//initialization
			treeNumber = 0;

			speciesNames = new Hashtable<Integer,String>();
			treeNumberBySpecies = new Hashtable<Integer,Double>();
			meanDiameterBySpecies = new Hashtable<Integer,Double>(); 
			basalAreaBySpecies = new Hashtable<Integer,Double>();
			volumeBySpecies = new Hashtable<Integer,Double>();
			marketValueBySpecies = new Hashtable<Integer, Double>();

			List<EconomicTree> trees = scene.getLivingEconomicTrees();

			for(EconomicTree t : trees){
				treeNumber += 1;
				meanDiameter += t.getDbh();
				basalArea += Math.PI * Math.pow(t.getDbh()/200,2);
				volume += t.getEconomicVolume_m3();
				marketValue += EconomicOperation.calcValueOfOneTree(t, settings);

				if(speciesNames.get(t.getSpeciesValue()) == null){
					speciesNames.put(t.getSpeciesValue(), t.getSpeciesName());
				}

				if(treeNumberBySpecies.get(t.getSpeciesValue()) == null){
					treeNumberBySpecies.put(t.getSpeciesValue(), 1d);
				}else{
					treeNumberBySpecies.put(t.getSpeciesValue(), treeNumberBySpecies.get(t.getSpeciesValue())+1);
				}

				if(meanDiameterBySpecies.get(t.getSpeciesValue()) == null){
					meanDiameterBySpecies.put(t.getSpeciesValue(), t.getDbh());
				}else{
					meanDiameterBySpecies.put(t.getSpeciesValue(), meanDiameterBySpecies.get(t.getSpeciesValue())+ t.getDbh());
				}

				if(basalAreaBySpecies.get(t.getSpeciesValue()) == null){
					basalAreaBySpecies.put(t.getSpeciesValue(), Math.PI * Math.pow(t.getDbh()/200,2));
				}else{
					basalAreaBySpecies.put(t.getSpeciesValue(), basalAreaBySpecies.get(t.getSpeciesValue())+ Math.PI * Math.pow(t.getDbh()/200,2));
				}

				if(volumeBySpecies.get(t.getSpeciesValue()) == null){
					volumeBySpecies.put(t.getSpeciesValue(), t.getEconomicVolume_m3());
				}else{
					volumeBySpecies.put(t.getSpeciesValue(), volumeBySpecies.get(t.getSpeciesValue())+t.getEconomicVolume_m3());
				}

				if(marketValueBySpecies.get(t.getSpeciesValue()) == null){
					marketValueBySpecies.put(t.getSpeciesValue(), t.getEconomicVolume_m3());
				}else{
					marketValueBySpecies.put(t.getSpeciesValue(), marketValueBySpecies.get(t.getSpeciesValue()) + EconomicOperation.calcValueOfOneTree(t,settings));
				}
			}

			// division by number of tree to get averages
			meanDiameter = meanDiameter / treeNumber;
			for (int key : meanDiameterBySpecies.keySet()) {
				double n = treeNumberBySpecies.get(key);
				meanDiameterBySpecies.put(key,meanDiameterBySpecies.get(key)/n);
			}

		}


		public StringBuffer toStringBuffer(){
			StringBuffer sb = new StringBuffer();
			String sep = "\t";
			
			//title
			sb.append("SpCode" + sep + "SpName" + sep + "N" + sep + "Dm" + sep + "BA" + sep + "volume" + sep + "market value");
			sb.append(System.getProperty("line.separator"));
			sb.append("---");
			sb.append(System.getProperty("line.separator"));

			for (int key : treeNumberBySpecies.keySet()) {
				sb.append(key + sep +
						speciesNames.get(key) + sep +
						treeNumberBySpecies.get(key) + sep +
						Math.round(meanDiameterBySpecies.get(key)) + sep +
						Math.round(basalAreaBySpecies.get(key)) + sep +
						Math.round(volumeBySpecies.get(key)) + sep + 
						Math.round(marketValueBySpecies.get(key))
						);
				sb.append(System.getProperty("line.separator"));
			}
			sb.append("---");
			sb.append(System.getProperty("line.separator"));

			sb.append("Total" + sep + "" + sep +
					treeNumber + sep +
					Math.round(meanDiameter) + sep +
					Math.round(basalArea) + sep +
					Math.round(volume) + sep + 
					Math.round(marketValue)
					);
			sb.append(System.getProperty("line.separator"));
			sb.append("---");
			sb.append(System.getProperty("line.separator"));
			
			return sb;
		}
		
		public void toPrint(){
			String sep = "\t";
			System.out.println("-----------------------------------------------------");
			System.out.println(" EconomicScenario.StandDescription.toPrint()");
			System.out.println("Stand description at year : " + scene.getDate());
			System.out.println("---");
			System.out.println("SpCode" + sep + "SpName" + sep + "N" + sep + "Dm" + sep + "BA" + sep + "volume" + sep + "market value");
			System.out.println("---");	


			for (int key : treeNumberBySpecies.keySet()) {
				System.out.println(key + sep +
						speciesNames.get(key) + sep +
						treeNumberBySpecies.get(key) + sep +
						Math.round(meanDiameterBySpecies.get(key)) + sep +
						Math.round(basalAreaBySpecies.get(key)) + sep +
						Math.round(volumeBySpecies.get(key)) + sep + 
						Math.round(marketValueBySpecies.get(key))
						);
			}

			System.out.println("---");
			System.out.println("Total" + sep + "" + sep +
					treeNumber + sep +
					Math.round(meanDiameter) + sep +
					Math.round(basalArea) + sep +
					Math.round(volume) + sep + 
					Math.round(marketValue)
					);

			System.out.println("-----------------------------------------------------");
		}

		public double getTreeNumber() {return treeNumber;}
		public void setTreeNumber(double treeNumber) {this.treeNumber = treeNumber;}
		public double getMeanDiameter() {return meanDiameter;}
		public void setMeanDiameter(double meanDiameter) {this.meanDiameter = meanDiameter;}
		public double getBasalArea() {return basalArea;}
		public void setBasalArea(double basalArea) {this.basalArea = basalArea;}
		public double getVolume() {return volume;}
		public void setVolume(double volume) {this.volume = volume;}
		public double getMarketValue() {return marketValue;}
		public void setMarketValue(double marketValue) {this.marketValue = marketValue;}
		public Hashtable<Integer, Double> getTreeNumberBySpecies() {return treeNumberBySpecies;}
		public void setTreeNumberBySpecies(Hashtable<Integer, Double> treeNumberBySpecies) {this.treeNumberBySpecies = treeNumberBySpecies;}
		public Hashtable<Integer, Double> getMeanDiameterBySpecies() {return meanDiameterBySpecies;}
		public void setMeanDiameterBySpecies(Hashtable<Integer, Double> meanDiameterBySpecies) {this.meanDiameterBySpecies = meanDiameterBySpecies;}
		public Hashtable<Integer, Double> getBasalAreaBySpecies() {return basalAreaBySpecies;}
		public void setBasalAreaBySpecies(Hashtable<Integer, Double> basalAreaBySpecies) {this.basalAreaBySpecies = basalAreaBySpecies;}
		public Hashtable<Integer, Double> getVolumeBySpecies() {return volumeBySpecies;}
		public void setVolumeBySpecies(Hashtable<Integer, Double> volumeBySpecies) {this.volumeBySpecies = volumeBySpecies;}
		public Hashtable<Integer, Double> getMarketValueBySpecies() {return marketValueBySpecies;}
		public void setMarketValueBySpecies(Hashtable<Integer, Double> marketValueBySpecies) {this.marketValueBySpecies = marketValueBySpecies;}
	}
