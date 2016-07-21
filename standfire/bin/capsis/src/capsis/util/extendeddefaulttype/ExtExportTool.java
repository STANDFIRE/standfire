/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.util.extendeddefaulttype;

import java.awt.Container;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import repicea.io.GExportRecord;
import repicea.io.REpiceaRecordSet;
import repicea.io.tools.REpiceaExportTool;
import repicea.io.tools.REpiceaExportToolDialog;
import repicea.util.MemoryWatchDog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.kernel.Step;

/**
 * This class implements the REpiceaExportTool in Capsis.
 * @author Mathieu Fortin - May 2016
 */
public abstract class ExtExportTool extends REpiceaExportTool {


	/**
	 * This enum variable represents the different export formats available for 
	 * the QuebecMRNF modules.
	 * @author Mathieu Fortin - April 2011
	 */
	public static enum ExportType implements TextableEnum {
		TREE("Tree level", "\u00C9chelle de l'arbre"),		
		STAND("Plot level", "\u00C9chelle de la placette"), 
		COMPOSITE_STAND("Stand level", "\u00C9chelle de la strate"), 
		COMPOSITE_STAND_WOODSTOCK("Stand level in column format", "\u00C9chelle de la strate (format en colonnes)"),
		TREE_QUALITY("Tree level with quality", "\u00C9chelle de l'arbre avec qualit\u00e9 "),
		TREELOG("Tree log level", "\u00C9chelle du billon"),
		STANDLOG ("Stand log level", "\u00C9chelle du billon au niveau de la placette"),		
		STAND_DHP_CATEGORY("Stand dhp category", "\u00C9chelle de la placette par cat\u00e9gorie de diam\u00e8tre"),				
		SCENARIO("Scenario", "Sc\u00E9nario");
		
		ExportType(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

	};

	@SuppressWarnings("serial")
	protected abstract static class InternalSwingWorkerForRecordSet<P extends ExtExportTool> extends REpiceaExportTool.InternalSwingWorkerForRecordSet {
		
		protected final P caller;
		
		protected InternalSwingWorkerForRecordSet(P caller, Enum exportOption, REpiceaRecordSet recordSet) {
			super(exportOption, recordSet);
			this.caller = caller;
		}
		
		/**
		 * Export scenario
		 */
		protected void createScenario() throws Exception {
			GExportRecord r;
			List<ExtEvolutionParameters> oVec = ((ExtCompositeStand) caller.getStepVector().lastElement().getScene()).getEvolutionTracking();
			double progressFactor = 100d / oVec.size();
			int l = 0;
			
			if (!oVec.isEmpty()) {
				outerLoop:
				for (int i = 0; i < oVec.size(); i++) {
					
					if (isCancelled) {
						break outerLoop;
					}
					MemoryWatchDog.checkAvailableMemory();

					ExtEvolutionParameters evolParams = (ExtEvolutionParameters) oVec.get(i);
					r = evolParams.getRecord();	
					addRecord(r);
					setProgress((int) ((++l) * progressFactor));
				}
			} else {
				throw new Exception();
			}
		}
	}
 

	
	private Vector<Step> steps;
	private ExtModel model;
	protected String stratumID;


	/**
	 * General constructor with multiple selection enabled 
	 * @throws Exception
	 */
	protected ExtExportTool() {
		super(true);
	}


	@Override
	public REpiceaExportToolDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new ExtExportToolDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	
	/**
	 * This method is a short initializer for the export tool. It implies there is no additional parameters inherited
	 * from a parent.
	 * @param model a QuebecMRNFModel instance
	 * @param step a Step object
	 * @throws Exception
	 */
	public void init(ExtModel model, Step step) {
		init(model, step, null);
	}
	
	/**
	 * This method initializes the export tool.
	 * @param model a QuebecMRNFModel instance
	 * @param step a Step object
	 * @param parentDBFTool a QuebecMRNFExportDBFTool object from which additional parameters can be inherited
	 */
	public void init(ExtModel model, Step step, ExtExportTool parentDBFTool) {
		this.model = model;
		if (step != null) {
			steps = step.getProject().getStepsFromRoot(step);
			ExtCompositeStand stand = (ExtCompositeStand) step.getScene();
			stratumID = stand.getStratumName();
		}
	}

	protected ExtModel getModel() {return model;}
	protected ExtMethodProvider getMethodProvider() {return (ExtMethodProvider) getModel().getMethodProvider();} 
	protected Vector<Step> getStepVector() {return steps;}
	
	
	protected Map<Enum, REpiceaRecordSet> justGetRecordSetsForSelectedExportOptions() {
		Map<Enum, REpiceaRecordSet> outputMap = new HashMap<Enum, REpiceaRecordSet>();
		for (Enum option : getSelectedExportFormats()) {
			REpiceaRecordSet recordSet = new REpiceaRecordSet();
			REpiceaExportTool.InternalSwingWorkerForRecordSet worker = this.instantiateInternalSwingWorkerForRecordSet(option, recordSet);
			worker.run();
			outputMap.put(option, recordSet);
		}
		return outputMap;
	}
	
	/*
	 * Just to make it public (non-Javadoc)
	 * @see quebecmrnfutility.ioformat.exportdbf.ExportDBFTool#setRecordSet(java.lang.Enum)
	 */
	@SuppressWarnings("rawtypes")
	public Set<Enum> getSelectedExportFormats() {return super.getSelectedExportOptions();}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Vector<Enum> defineAvailableExportOptions() {
		Vector<Enum> exportOptions = new Vector<Enum>();
		for (ExportType exportType : ExportType.values()) {
			exportOptions.add(exportType);
		}
		return exportOptions;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List<Enum> getAvailableExportOptions() {return super.getAvailableExportOptions();}

	@Override
	protected String getFilename() {return super.getFilename();}
	
	
}

