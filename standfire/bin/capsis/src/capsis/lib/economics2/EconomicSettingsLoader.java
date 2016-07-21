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
import java.util.Iterator;
import java.util.List;

import jeeb.lib.util.Log;
import jeeb.lib.util.RecordSet;
import capsis.lib.economics2.EconomicOperation.Trigger;
import capsis.lib.economics2.EconomicOperation.Type;
import capsis.lib.economics2.EconomicOperation;

/**
 * Read a *.txt file containing the settings for an EconomicScenario.
 * 
 * @author dethier.o - July 2013
 */
public class EconomicSettingsLoader extends RecordSet
{
	private String filename;

	public EconomicSettingsLoader(String filename) throws Exception
	{	
		this.filename = filename;
		addAdditionalClass(EconomicOperationRecord.class);
		addAdditionalClass(EconomicPriceRecord.class);
		createRecordSet(filename);
	}
	
	
	public void loadSettings(EconomicSettings es) throws Exception
	{
		for (Iterator i = this.iterator(); i.hasNext();) 
		{
			Object record = i.next();

			if (record instanceof EconomicOperationRecord) 
			{
				EconomicOperationRecord eor = (EconomicOperationRecord) record;
				//~ Retrieve enumerations ~//
				Type type = Type.valueOf (eor.type);
				Trigger trigger = Trigger.valueOf (eor.trigger);
				
				//~ Create and add the new EconomicOperation ~//
				EconomicOperation eo = new EconomicOperation(eor.label,type,trigger, eor.income, eor.price);
				
				//manage the dates
				if(trigger == Trigger.ON_DATE){
					eo.setGivenDate(eor.date);
				}else if (trigger == Trigger.ON_FREQUENCY){
					List<Integer> dateIntVector = new ArrayList<Integer>();
					String dateStringVector = eor.frequency.replace ("{", "").replace ("}", "");
					if (dateStringVector.length () > 0){
						for (String date : dateStringVector.split (",")){
							if (date.length () != 0) dateIntVector.add (Integer.parseInt (date));
						}
					}
					
					try{
						if (dateIntVector.size()<3){
							throw new Exception("EconomicSettingLoader.loadSettings() - Unable to compute validity dates with intervention with trigger = frequency");
						}
					} catch (Exception e){
						System.err.println("Caught IOException: " + e.getMessage());
						Log.println (Log.ERROR,"EconomicSettingLoader.loadSettings()","Unable to compute validity dates with intervention with trigger = frequency");
					}
					
					eo.setGivenStartDate(dateIntVector.get(0));
					eo.setGivenEndDate(dateIntVector.get(1));
					eo.setGivenFrequency(dateIntVector.get(2));
					
				}else if (trigger == Trigger.YEARLY){
					//do nothing ... the validity years will be computed later on	
					
				}else if (trigger == Trigger.ON_INTERVENTION){	
					//do nothing ... the validity years will be computed later on			
				}
				
//				List<Integer> vd = new ArrayList<Integer>();
//				if(trigger == Trigger.ON_DATE){
//					vd.add(eor.date);
//					eo.setValidityDates (vd);
//				}else if (trigger == Trigger.ON_FREQUENCY){
//				
//					String dates = eor.frequency.replace ("{", "").replace ("}", "");
//					if (dates.length () > 0){
//						for (String date : dates.split (",")){
//							if (date.length () != 0) vd.add (Integer.parseInt (date));
//						}
//					}
//					eo.setValidityDates (vd);
//					
//				}else if (trigger == Trigger.YEARLY){
//					//do not set the validy date (that is optional and useless so far)	
//					
//				}else if (trigger == Trigger.ON_INTERVENTION){	
//					//do not set the validy date (that is optional and useless so far)					
//				}

				es.addEconomicOperation (eo);
			}
			else if (record instanceof EconomicPriceRecord)
			{
				EconomicPriceRecord epr = (EconomicPriceRecord) record;
				es.addPrice (epr.dbh, epr.price, epr.species);
			}
			else if (record instanceof KeyRecord)
			{
				KeyRecord r = (KeyRecord) record;
					
				if (r.hasKey ("discountRate"))
				{
					try
					{
						es.setDiscountRate (r.getDoubleValue ());
					}
					catch (Exception e)
					{
						Log.println (Log.ERROR, "EconomicLoader.load ()",
								"Trouble with discountRate", e);
						throw e;
					}	
				}
				else if (r.hasKey ("land"))
				{
					try
					{
						es.setLand (r.getDoubleValue ());
					}
					catch (Exception e)
					{
						Log.println (Log.ERROR, "EconomicLoader.load ()",
								"Trouble with land", e);
						throw e;
					}	
				}
				else 
				{
					throw new Exception("wrong format in " + filename
							+ " near record " + record);
				}		
			}
		}
	}
}
