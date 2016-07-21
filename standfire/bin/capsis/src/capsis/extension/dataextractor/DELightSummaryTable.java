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

package capsis.extension.dataextractor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.RectangularPlot;
import capsis.defaulttype.SquareCell;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFTables;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.samsaralight.SLCellLight;
import capsis.lib.samsaralight.SLLightableCell;
import capsis.lib.samsaralight.SLLightableModel;
import capsis.lib.samsaralight.SLLightableScene;
import capsis.lib.samsaralight.SLModel;
import capsis.lib.samsaralight.SLSensor;
import capsis.lib.samsaralight.SLSettings;

/**
 * A summary light table compatible with instance of samsaralight
 * @author G. Ligot - 16-02-2012
 */
public class DELightSummaryTable extends PaleoDataExtractor implements DFTables {
	
	public static final String AUTHOR = "G. Ligot";
	public static final String VERSION = "1.0";

	protected Collection<String [][]> tables;
	protected Collection<String> titles;
	
	protected MethodProvider methodProvider;
	private GenericExtensionStarter starter;
	
	protected NumberFormat f;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DELightSummaryTable"); 
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DELightSummaryTable () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DELightSummaryTable (GenericExtensionStarter s) {

		this.starter = s;

		try {
			tables = new ArrayList ();
			titles = new ArrayList ();

			// Used to format decimal part with 2 digits only
			f = NumberFormat.getInstance (Locale.ENGLISH);
			f.setGroupingUsed (false);
			f.setMaximumFractionDigits (1);
			f.setMinimumFractionDigits (1);

		} catch (Exception e) {
			Log.println (Log.ERROR, "DELightSummaryTable.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;} //should be changed
			GModel m = (GModel) referent;
						
			if (!(m instanceof SLLightableModel)) {return false;} 
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "DELightSummaryTable.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}
	
	/** 
	 * This method is called by superclass DataExtractor.
	 */
	@Override
	public void setConfigProperties () {

		// observation
		String[] tab = {"T_cell","T_sensor"};
		addRadioProperty (tab);
		
		// statistics
		addBooleanProperty("S_mean",true);
		addBooleanProperty("S_min",true);
		addBooleanProperty("S_max",true);
		addBooleanProperty("S_stddev",true);
			
		// Variable
		addBooleanProperty("V_PACLTOT",true);
		addBooleanProperty("V_PACLDIF",true);
		addBooleanProperty("V_PACLDIR",true);
		
		addBooleanProperty("V_IRRADIANCETOT",true);
		addBooleanProperty("V_IRRADIANCEDIF",true);
		addBooleanProperty("V_IRRADIANCEDIR",true);
		
		addBooleanProperty("V_IRRADIANCETOTHORIZ",true);
		addBooleanProperty("V_IRRADIANCEDIFHORIZ",true);
		addBooleanProperty("V_IRRADIANCEDIRHORIZ",true);
		
		addBooleanProperty("V_IRRADIANCEABOVECANOPY",true);

		
	}

	/**
	 * From DataExtractor Interface.
	 * Effectively process the extraction.
	 * Should only be called if needed (time consuming).
	 * One solution is to trigger real extraction by data renderer 
	 * paintComponent (). So, the work will only be done when needed.
	 * Return false if trouble.
	 */
	public boolean doExtraction () {
		
		if (upToDate) {return true;}
		if (step == null) {return false;}
		
		RectangularPlot plot = (RectangularPlot) step.getScene().getPlot();
		methodProvider = step.getProject ().getModel ().getMethodProvider ();
		
		Log.println ("DELightSummaryTable : extraction being made");
		
		SLLightableScene lightScene = (SLLightableScene) step.getScene();
		SLModel slModel = ((SLLightableModel) step.getProject ().getModel ()).getSLModel ();
		SLSettings slSettings = slModel.getSettings ();
		Collection<SquareCell> targetCells = new ArrayList<SquareCell> ();			
		targetCells = lightScene.getCellstoEnlight ();
		if (targetCells == null) targetCells = plot.getCells ();
		List<SLSensor> sensors = lightScene.getSensors ();
		
		tables.clear ();
		
		boolean noData = true;

		try {
			
			int nRow = 0;
			nRow ++; // the title
			int nCol = 0;
			nCol ++; // the title
			
			if (isSet("V_PACLTOT")) nRow ++;
			if (isSet("V_PACLDIF")) nRow ++;
			if (isSet("V_PACLDIR")) nRow ++;
			
			if (isSet("V_IRRADIANCETOT")) nRow ++;
			if (isSet("V_IRRADIANCEDIF")) nRow ++;
			if (isSet("V_IRRADIANCEDIR")) nRow ++;
			
			if (isSet("V_IRRADIANCETOTHORIZ")) nRow ++;
			if (isSet("V_IRRADIANCEDIFHORIZ")) nRow ++;
			if (isSet("V_IRRADIANCEDIRHORIZ")) nRow ++;
			
			if (isSet("V_IRRADIANCEABOVECANOPY")) nRow ++;
			
			if (isSet("S_mean")) nCol ++;
			if (isSet("S_min")) nCol ++;
			if (isSet("S_max")) nCol ++;
			if (isSet("S_stddev")) nCol ++;
			
			
			DescriptiveStatistics  paclTotal = new DescriptiveStatistics ();
			DescriptiveStatistics  paclDiffus = new DescriptiveStatistics ();
			DescriptiveStatistics  paclDirect = new DescriptiveStatistics ();
			
			DescriptiveStatistics  irradianceTotal = new DescriptiveStatistics ();
			DescriptiveStatistics  irradianceDiffus = new DescriptiveStatistics ();
			DescriptiveStatistics  irradianceDirect = new DescriptiveStatistics ();
			
			DescriptiveStatistics  irradianceTotalHoriz = new DescriptiveStatistics ();
			DescriptiveStatistics  irradianceDiffusHoriz = new DescriptiveStatistics ();
			DescriptiveStatistics  irradianceDirectHoriz = new DescriptiveStatistics ();
			
			DescriptiveStatistics  irradianceAboveCanopy = new DescriptiveStatistics ();

			
			if (isSet("T_cell") && ! targetCells.isEmpty ()) {
				
				int n = targetCells.size ();
				
				for(Iterator i = targetCells.iterator (); i.hasNext ();){
					SLCellLight c = ((SLLightableCell) i.next ()).getCellLight ();
					paclTotal.addValue (c.getRelativeHorizontalEnergy ());
					paclDiffus.addValue (c.getRelativeDiffuseHorizontalEnergy ());
					paclDirect.addValue (c.getRelativeDirectHorizontalEnergy ());
					irradianceTotal.addValue (c.getTotalSlopeEnergy ());
					irradianceDiffus.addValue (c.getDiffuseSlopeEnergy ());
					irradianceDirect.addValue (c.getDirectSlopeEnergy ());
					irradianceTotalHoriz.addValue(c.getTotalHorizontalEnergy());
					irradianceDiffusHoriz.addValue(c.getDiffuseHorizontalEnergy());
					irradianceDirectHoriz.addValue(c.getDirectHorizontalEnergy());
					irradianceAboveCanopy.addValue(c.getAboveCanopyHorizontalEnergy());
				}
				
				// Tables titles
				titles.clear ();
				titles.add (Translator.swap ("DELightSummaryTable.cellSummary")); 
				noData = false;
			}	
			
			if(isSet("T_sensor") && (sensors!=null)) {
				int n = sensors.size ();
				for(Iterator i = sensors.iterator (); i.hasNext ();){
					SLSensor s = (SLSensor) i.next ();
					paclTotal.addValue (s.getRelativeHorizontalTotalEnergy ());
					paclDiffus.addValue (s.getRelativeHorizontalDiffuseEnergy ());
					paclDirect.addValue (s.getRelativeHorizontalDirectEnergy ());
					irradianceTotal.addValue (s.getTotalSlopeEnergy ());
					irradianceDiffus.addValue (s.getDiffuseSlopeEnergy ());
					irradianceDirect.addValue (s.getDirectSlopeEnergy ());
					irradianceTotalHoriz.addValue(s.getTotalHorizontalEnergy());
					irradianceDiffusHoriz.addValue(s.getDiffuseHorizontalEnergy());
					irradianceDirectHoriz.addValue(s.getDirectHorizontalEnergy());
					irradianceAboveCanopy.addValue(s.getAboveCanopyHorizontalEnergy());
				}
				
				// Tables titles
				titles.clear ();
				titles.add (Translator.swap ("DELightSummaryTable.sensorSummary")); 
				noData = false;
			}
			
			if (noData) {throw new Exception (" There is no cell or no sensor with light values");}
			
			String [][] tabLight = new String[nRow][nCol];
			
			// Columns
			String[] colNames = new String[nCol];
			int c = 0;
			colNames[c++] = "Variable";
			if (isSet("S_mean")) colNames[c++] = "mean";
			if (isSet("S_min")) colNames[c++] = "min";
			if (isSet("S_max")) colNames[c++] = "max";
			if (isSet("S_stddev")) colNames[c++] = "stddev";
		
			// Rows
			String[] rowNames = new String[nRow];
			int r = 0;
			rowNames[r++] = "Variable";
			if (isSet("V_PACLTOT")) rowNames[r++] = "PACLTOT";
			if (isSet("V_PACLDIF")) rowNames[r++] = "PACLDIF";
			if (isSet("V_PACLDIR")) rowNames[r++] = "PACLDIR";
			
			if (isSet("V_IRRADIANCETOT")) rowNames[r++] = "IRRADIANCETOT";
			if (isSet("V_IRRADIANCEDIF")) rowNames[r++] = "IRRADIANCEDIF";
			if (isSet("V_IRRADIANCEDIR")) rowNames[r++] = "IRRADIANCEDIR";
			
			if (isSet("V_IRRADIANCETOTHORIZ")) rowNames[r++] = "IRRADIANCETOTHORIZ";
			if (isSet("V_IRRADIANCEDIFHORIZ")) rowNames[r++] = "IRRADIANCEDIFHORIZ";
			if (isSet("V_IRRADIANCEDIRHORIZ")) rowNames[r++] = "IRRADIANCEDIRHORIZ";
			
			if (isSet("V_IRRADIANCEABOVECANOPY")) rowNames[r++] = "IRRADIANCEABOVECANOPY";
			
			
			//initialization
			DescriptiveStatistics  x = new DescriptiveStatistics ();
			double value = -1;
			
			for(int rnum = 0; rnum<nRow; rnum++){
				for(int cnum = 0; cnum<nCol; cnum++){
					if (rnum==0) tabLight[rnum][cnum] = colNames[cnum];			// First line => insert sub-title
					else if (cnum==0) tabLight[rnum][cnum] = rowNames[rnum];	// First column => insert sub-title
					else{
						//get the variable
						if (rowNames[rnum].equals ("PACLTOT")) x = paclTotal;
						else if (rowNames[rnum].equals ("PACLDIF")) x = paclDiffus;
						else if (rowNames[rnum].equals ("PACLDIR")) x = paclDirect;
						
						else if (rowNames[rnum].equals ("IRRADIANCETOT")) x = irradianceTotal;
						else if (rowNames[rnum].equals ("IRRADIANCEDIF")) x = irradianceDiffus;
						else if (rowNames[rnum].equals ("IRRADIANCEDIR")) x = irradianceDirect;
						
						else if (rowNames[rnum].equals ("IRRADIANCETOTHORIZ")) x = irradianceTotalHoriz;
						else if (rowNames[rnum].equals ("IRRADIANCEDIFHORIZ")) x = irradianceDiffusHoriz;
						else if (rowNames[rnum].equals ("IRRADIANCEDIRHORIZ")) x = irradianceDirectHoriz;
						
						else if (rowNames[rnum].equals ("IRRADIANCEABOVECANOPY")) x = irradianceAboveCanopy;
						
						//get the statistics
						if (colNames[cnum].equals("mean")) value=x.getMean ();
						else if (colNames[cnum].equals("min")) value=x.getMin ();
						else if (colNames[cnum].equals("max")) value=x.getMax ();
						else if (colNames[cnum].equals("stddev")) value=x.getStandardDeviation ();
						
						tabLight[rnum][cnum] = f.format (value); 
					}
				}
			}
			
			tables.add(tabLight);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "DELightSummaryTable.doExtraction ()", "Exception caught : ",e);
			return false;
		}	
		
		upToDate = true;
		return true;
		
	}
	

	
	/**
	 * From DFTables interface.
	 */
	public Collection getTables () {return tables;}
	public Collection getTitles () {return titles;}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public String getAuthor () {return AUTHOR;}
	public String getDescription () {return Translator.swap ("DELightSummaryTable.description");}
	public String getName(){return Translator.swap ("DELightSummaryTable");}

}


