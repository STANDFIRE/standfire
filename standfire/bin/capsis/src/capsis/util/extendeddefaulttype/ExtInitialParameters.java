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

import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.Log;
import jeeb.lib.util.TicketDispenser;
import capsis.gui.MainFrame;
import capsis.kernel.AbstractSettings;
import capsis.kernel.GModel;
import capsis.kernel.InitialParameters;
import capsis.util.EnumProperty;

/**
 * The AbstractInitialParameters class handles all the settings of a simulation with many plots.
 * @author Mathieu Fortin - October 2013
 *
 * @param <C> a CompositeTreeList type
 * @param <S> an AbstractSimulationSettings type
 * @param <CP> an AbstractConstantInitialParameters type
 */
public abstract class ExtInitialParameters<C extends ExtCompositeStand, 
										S extends ExtSimulationSettings,
										CP extends ExtConstantInitialParameters> extends AbstractSettings implements InitialParameters {

	private static final long serialVersionUID = 20100804L;

	public enum UseMode {GUI_MODE, ASSISTED_SCRIPT_MODE, PURE_SCRIPT_MODE}

	private String projectName = "Unnamed";	// default value			
	private UseMode useMode;
	private String filename;
	private int iInitialSimulationYear;
	private int iNumberOfIterations;
	private TicketDispenser treeIdDispenser;

	private transient ExtRecordReader recordReader;
	private S generalSettings;
	private CP constantInitialParameters;

	protected transient C initStand;
    protected boolean bForceResidualStand;

	private boolean isStochastic = false; // default value


	/**
	 * General constructor
	 */
	protected ExtInitialParameters() {
		treeIdDispenser = new TicketDispenser();
	}

	/**
	 * This method returns the project name as specified in the initial parameter dialog.
	 * @return a String
	 */
	public String getProjectName() {return projectName;}
	
	protected void setProjectName(String projectName) {this.projectName = projectName;}
	
	/**
	 * This method returns the initial composite stand for any simulation.
	 * @return a CompositeTreeList-derived  instance
	 */
	public C getInitScene() {return initStand;}

	/**
	 * This method forces the initial stand to be an intervention results. The implementation depends on 
	 * the model.
	 * @param b a boolean
	 */
	public void setForceInterventionResultEnabled(boolean b) {bForceResidualStand = b;}

	public TicketDispenser getTreeIdDispenser() {return treeIdDispenser;}

	public S getGeneralSettings() {return generalSettings;}
	public void setGeneralSettings(S generalSettings) {this.generalSettings = generalSettings;}

	public void setFilename(String filename) {this.filename = filename;}
	public String getFilename() {return this.filename;}


	/**
	 * This method loads the initial stratum (or stand) that may have one or many plots.
	 * @param stratumId the id of the stratum (or stand) that is to be loaded
	 * @throws Exception
	 */
	protected abstract void loadInitCompositeStand(int stratumId) throws Exception;

	/**
	 * This method initializes some parameters before making the first step visible.
	 * @throws Exception
	 */
	protected void initializeModel() throws Exception {
		ExtCompositeStand initCompositeStand = getInitScene();
		if (initCompositeStand == null) {
			throw new NullPointerException();
		}

		initCompositeStand.setSpeciesGroupTag(getConstantInitialParameters().groupTagMap);
	}

	public void setInitialSimulationYear(int i) {iInitialSimulationYear = i;}
	public int getInitialSimulationYear() {return iInitialSimulationYear;}

	/**
	 * This method returns the constant initial parameters for this simulation.
	 * @return a ConstantInitialParameters-derived instance
	 */
	public CP getConstantInitialParameters() {return constantInitialParameters;}

	/**
	 * This method sets the constant initial parameters of the simulation.
	 * @param constantInitialParameters a QuebecMRNFConstantInitialParameters-derived instance
	 */
	public void setConstantInitialParameters(CP constantInitialParameters) {
		this.constantInitialParameters = constantInitialParameters;
	}


//	public boolean isGuiEnabled() {return (useMode == UseMode.GUI_MODE);}

	/**
	 * This method set the number of Monte Carlo iterations.
	 * @param i an Integer
	 */
	public void setNumberOfIterations(int i) {iNumberOfIterations = i;}

	/**
	 * This method returns the number of Monte Carlo iterations.
	 * @return an Integer
	 */
	public int getNumberOfIterations() {return iNumberOfIterations;}

	/**
	 * This method returns true if the simulation mode is set to stochastic or false
	 * if the default deterministic mode is enabled.
	 * @return a boolean
	 */
	public boolean isStochastic() {return isStochastic;}

	/**
	 * This method sets the simulation mode. 
	 * @param isStochastic true to use the stochastic simulation mode or false to use the 
	 * deterministic mode (default)
	 */
	public void setStochastic(boolean isStochastic) {this.isStochastic = isStochastic;}

	public String getInventoryPath() {return getGeneralSettings().getInventoryPath();}


	/**
	 * This method requests the initialization of the RecordInstantiator. NOTE: To be called in GUI mode only.
	 */
	public void initImport() throws Exception {
		getRecordReader().initGUIMode(MainFrame.getInstance(), getFilename());
	}


	/**
	 * This method returns a RecordReader instance that can read the user-specified file.
	 * @return a RecordReader instance
	 */
	public ExtRecordReader getRecordReader() {return recordReader;}
	protected void setRecordReader(ExtRecordReader<?> recordReader) {this.recordReader = recordReader;}

	
	public EnumProperty getSpeciesGroupTagSpecimen() {return getConstantInitialParameters().groupTagSpecimen;}
	
	public EnumProperty getSpeciesGroupTag(int speciesID) {return getConstantInitialParameters().groupTagMap.get(speciesID);}

	/**
	 * This method returns a vector of simulation dates.
	 * @return a Vector of integer instances that represent the simulation step dates
	 */
	public List<Integer> getSimulationDates() {
		List<Integer> outputVec = new ArrayList<Integer>();
		int maxYear = getInitialSimulationYear() + getGeneralSettings().getMaxNumberOfSteps() * getGeneralSettings().getTimeStep();
		for (int year = getInitialSimulationYear(); year <= maxYear; year += getGeneralSettings().getTimeStep()) {
			outputVec.add(year);
		}
		return outputVec;
	}

	/**
	 * This method returns true if the module is used in GUI mode.
	 * @return a boolean
	 */
	public boolean isGuiEnabled() {return (useMode == UseMode.GUI_MODE);}

	public UseMode getUseMode() {return useMode;}
	
	public void setUseMode(UseMode useMode) {this.useMode = useMode;}

	/** 
	 * This method loads initial scene using a RecordReader instance
	 * 
	 */
	public void buildInitScene(GModel model) throws Exception {
		try {
			int stratumId = getRecordReader().getSelectedGroupId();
			loadInitCompositeStand(stratumId);
			initializeModel();
		} catch (Exception exc) {
			Log.println (Log.ERROR, "AbstractInitialParameters ()", "Error during inventory load", exc);
			initStand = null;	// to free the memory from this stand
			throw exc;
		}		
	}
	
	protected void clear() {initStand = null;}

}
