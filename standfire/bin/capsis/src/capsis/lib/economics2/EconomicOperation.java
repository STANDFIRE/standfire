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
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import jeeb.lib.util.autoui.annotations.AutoUI;
import jeeb.lib.util.autoui.annotations.Editor;

/**
 * An economic operation in the economics2 package.
 * @author G. Ligot, F. de Coligny - January 2012
 */
@AutoUI(title="EconomicOperation", translation="EconomicOperation")
public class EconomicOperation {
	static {
		Translator.addBundle ("capsis.lib.economics2.Economic");
	}

	public enum Type {
		FIXED, M3, DBH_CLASS, TREE_NUMBER // ...
	}
	public enum Trigger {
		ON_DATE, ON_INTERVENTION, ON_FREQUENCY, YEARLY // ...
	}
	
	//variable as it is in the file
	private int givenDate;
	private int givenStartDate;
	private int givenEndDate;
	private int givenFrequency; 
	
	//transform above entries to validity dates using setValidityDates 
	private List<Integer> validityDates=new ArrayList(); 
	
	//other properties
	private String label;
	@Ignore
	protected Type typeValue=Type.FIXED;
	@Ignore
	protected Trigger trigger=Trigger.ON_DATE;
	@Ignore
	protected boolean income=false;
	@Editor (group="EconomicValues")
	protected double price=0; // Euros / unit (/ha, /stem, /m3...)
	
	protected List<EconomicTree> economicTrees; //This need to be defined for operation types M3, DBH_CLASS and TREE_NUMBER (default is all living trees)

//	/**	
//	 * Constructor for fixed Type operation
//	 */
//	public EconomicOperation (String label, Type type, Trigger trigger, boolean income, double price) {
//		this.label = label;
//		this.typeValue = type;
//		this.trigger = trigger;
//		this.income = income;
//		this.price = price;
//		
//		try{
//			if(this.typeValue != Type.FIXED){
//				throw new Exception("This constructor can only be used for economicOperation of typeValue = FIXED");
//			}
//		}catch(Exception e){
//			Log.println (Log.ERROR,"EconomicOperation.EconomicOperation()",e.toString ());
//		}
//		
//	}
	/**
	 * empty economic operation used in gui 
	 */
	public EconomicOperation (){
		
	}
	
	/**	
	 * Constructor
	 */
	public EconomicOperation (String label, Type type, Trigger trigger, boolean income, double price) { //gl 17/06/2016 - moved the list of trees from scene to operation
		this.label = label;
		this.typeValue = type;
		this.trigger = trigger;
		this.income = income;
		this.price = price;
	}
	
	/**	
	 * Second Constructor
	 */
	public EconomicOperation (String label, Type type, Trigger trigger, boolean income, double price, List<EconomicTree> trees) { //gl 17/06/2016 - moved the list of trees from scene to operation
		this.label = label;
		this.typeValue = type;
		this.trigger = trigger;
		this.income = income;
		this.price = price;
		this.economicTrees = trees;
		 
	}
	
//	/**
//	 * create a vector of dates corresponding to income/expanse dates.
//	 * needed for repeated economic operations 
//	 * @param frequency
//	 */
//	public void setValidityDates (List<Integer> validityYears) {
//		// SQ 12/02/2012
//		if (trigger==Trigger.ON_FREQUENCY){
//			if (validityYears.size()<3){
//				Log.println (Log.ERROR,"EconomicOperation.setValidityDates()","Unable to compute validity dates with intervention with trigger = frequency");
//				return;
//				} 
//			
//			int tmpDate = validityYears.get(0); //starting date
//			int frequency = validityYears.get(2);
//			
//			//add possible dates
//			do {
//				this.validityDates.add(tmpDate);
//				tmpDate = tmpDate + frequency;
//			}while(tmpDate<=validityYears.get(1)); //ending date
//		}else if(trigger==Trigger.YEARLY){
//			this.validityDates=validityYears; //not necessary to define the dates for yearly intervention!
//		}else{ 
//			this.validityDates=validityYears;
//		}
//	}

	/**
	 * Return the value (income or expense) of the operation for 1 ha
	 * @return operation value (EUR/ha)
	 */
	public double getValue(EconomicScene s, EconomicScenario scenario) { // Euros
		//by default if the list of economicTrees is empty, use the number of living trees
		if(economicTrees == null){
			System.out.println("EconomicOperation.getValue : the list of economicTrees was empty. By default, it takes the list of all living trees");
			Log.println ("economics2", "the list of economicTrees was empty. By default, it takes the list of all living trees");
			economicTrees = (List<EconomicTree>) s.getLivingEconomicTrees();
		}
		
		if (typeValue.equals(Type.FIXED)) { //price are given per hectare
			return price; // * s.getArea() / 10000d;

		} else if (typeValue.equals(Type.M3)) { //price are given per m3
			return getAffectedVolume() * price * 10000 / s.getArea (); //gl 29-07-2013

		} else if (typeValue.equals(Type.DBH_CLASS)) { //price are given per m3 and dbh class!
			EconomicSettings settings = scenario.getSettings();
			return calcValueOfTrees(economicTrees,s,settings);

		} else if (typeValue.equals(Type.TREE_NUMBER)) { //price are given per tree
			return getAffectedTreeNumber() * price; //corrected 10/01/2014 by gl as the number of trees should already per ha (* 10000 / s.getArea (); //gl 29-07-2013)

		} else {
			return 0; // error
		}
	}
	
	/**
	 * Compute the market value of a list of trees
	 * @author Gauthier Ligot 15/06/2016
	 */
	private static double calcValueOfTrees(List<EconomicTree> trees, EconomicScene s, EconomicSettings settings){
		double v = 0d;
		if(trees != null || !trees.isEmpty ()){
			for(EconomicTree t : trees){
				v += calcValueOfOneTree(t,settings);
			}
		}
		return v * 10000 / s.getArea (); 
	}
	
	public static double calcValueOfOneTree(EconomicTree tree, EconomicSettings settings){
		double pricebydbh = 0;
		try{
			pricebydbh = settings.getPriceByDbh (tree.getDbh(), tree.getSpeciesValue());
		}catch(Exception e){
			Log.println (Log.ERROR,"calcValueOfOneTree.getValue()",e.toString ());
		}
		return tree.getEconomicVolume_m3 () * pricebydbh;
	}
	
	public double getAffectedVolume(){
		double v=0;
		for (EconomicTree t : economicTrees){
			v+=t.getEconomicVolume_m3();
		}
		return v;
	}
	
	public double getAffectedTreeNumber(){
		double n=0;
		for (EconomicTree t : economicTrees){
			n+=1;
		}
		return n;
	}
	
	public void computeValidityDates(int firstDate, int lastDate) {
		
			if (trigger==Trigger.ON_FREQUENCY){
				List<Integer> vd = new ArrayList<Integer>();
				int tmpDate = givenStartDate;
				do {
					vd.add(tmpDate);
					tmpDate = tmpDate + givenFrequency;
				}while(tmpDate<=givenEndDate); //ending date
				
				this.validityDates = vd;
				
			}else if(trigger == Trigger.YEARLY){
				List<Integer> vd = new ArrayList<Integer>();
				for(Integer y = firstDate; y <= lastDate; y++){
					vd.add(y);
				}
				this.validityDates = vd;
				
			}else if(trigger == Trigger.ON_INTERVENTION){
				//TODO ... Trigger on intervention is not working so far with manual operation
				
			}else if(trigger == Trigger.ON_DATE){
				List<Integer> vd = new ArrayList<Integer>();
				vd.add(givenDate);
				this.validityDates = vd;
			}

		}
	

	//--- Accessors
	public String getLabel() {return label;}
	public Type getType() {	return typeValue;}
	public Trigger getTrigger() {return trigger;}
	public boolean isIncome() {return income;}
	public double getPrice() {return price;}	
	public void setLabel(String label) {this.label = label;}
	public void setTrigger(Trigger trigger) {this.trigger = trigger;}
	public void setIncome(boolean income) {this.income = income;}
	public void setPrice(double price) {this.price = price;}
	public void setType(Type type){this.typeValue = type;}
	public List<EconomicTree> getEconomicTrees() {return economicTrees;}
	public void setEconomicTrees(List<EconomicTree> economicTrees) {this.economicTrees = economicTrees;}

	public void setGivenDate(int givenDate) {this.givenDate = givenDate;}
	public int getGivenDate() {return givenDate;}
	public int getGivenStartDate() {return givenStartDate;}
	public void setGivenStartDate(int givenStartDate) {this.givenStartDate = givenStartDate;}
	public int getGivenEndDate() {return givenEndDate;}
	public void setGivenEndDate(int givenEndDate) {this.givenEndDate = givenEndDate;}
	public int getGivenFrequency() {return givenFrequency;}
	public void setGivenFrequency(int givenFrequency) {this.givenFrequency = givenFrequency;}
	
	public List<Integer> getValidityDates() {return validityDates;}
	public void setValidityDates(List<Integer> validityDates) {this.validityDates = validityDates;}
}