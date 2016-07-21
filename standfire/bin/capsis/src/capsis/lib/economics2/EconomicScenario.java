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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Log;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;

/**
 * An economic scenario: a list of economic operations applied to a given Capsis
 * project.
 * 
 * @author G. Ligot, F. de Coligny - January 2012
 */
public class EconomicScenario {

	private Project project;
	private List<EconomicOperation> operations;
	private List<EconomicStandardizedOperation> standardizedEconomicOperations; //list of operation date by date that can be sorted by date
	//	private List<SummarizedEconomicOperation> summarizedEconomicOperations;	//summary of operations by years
	private EconomicSettings settings;
	private EconomicModel model;
	private Double tir; //those are french notations...
	private Double tirf;
	private double bt;
	private double bm;
	private double rt;
	private double rm;
	private Double basi; //critère de Faustmann, land expectation value
	private Double basf;
	private double bas; //net present value
	private double vm; //m3/ha/year
	private double vt; //m3/ha
	private Double initialFEV;
	private Double finalFEV;
	private Double forwardAnnuity;
	private Double backwardAnuity;
//	private Double discountRate; // % or [0,1] ???
//	private Double land; //land value	
	private boolean discountRateGiven;
	private boolean landGiven;
	private Double estimatedDiscountRate;
	private int nYears; 
	private int firstDate;
	private int lastDate;
	private Integer firstDateInfiniteCycle; //date of the beginning of what could be considered an infinite cycle
//	private boolean landIsEstimated = true;
	private EconomicScene sceneAtFirstDate;
	private EconomicStandDescription standDescriptionAtFirstDate;
	private EconomicScene sceneAtLastDate;
	private EconomicStandDescription standDescriptionAtLastDate;
	private EconomicScene sceneAtFirstDateInfiniteCycle;
	private EconomicStandDescription standDescriptionAtFirstDateInfiniteCycle;


	public enum EconomicCase {
		INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE(0,"infinity_cycle_with_land_observation"), 
		INFINITY_CYCLE_WITHOUT_LAND_OBSERVATION(1,"infinity_cycle_without_land_observation"),
		TRANSITORY_PERIOD(2,"transitory_period"),
		TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE(3,"transitory_period_plus_infinity_cycle");

		private int value;
		private String name;

		EconomicCase(int value, String name){
			this.value = value;
			this.name = name;
		}

		public String getName () {return name;}
		public int getValue () {return value;}
	}


	private EconomicCase economicCase;

	/**
	 * default constructor
	 * Economic settings need to be set afterward
	 */
	public EconomicScenario (Project project, EconomicModel model) {
		settings = new EconomicSettings ();
		this.project = project;
		this.model = model;
		model.setEconomicScenario (this);

	}

	/**
	 * a first script constructor that creates a simplified instance of EconomicSettings
	 */
	public EconomicScenario (Project project, EconomicModel model, double discountRate, double land) {
		settings = new EconomicSettings ();
		settings.setDiscountRate (discountRate);
		settings.setLand (land);
		this.project = project;
		this.model = model;
		model.setEconomicScenario (this);

	}

	/**
	 * a second script constructor that will load a file to create an instance of EconomicSettings
	 * and load the file
	 */
	public EconomicScenario (Project project, EconomicModel model, String filename) {
		settings = new EconomicSettings ();
		settings.setFileName (filename);
		this.project = project;
		this.model = model;
		model.setEconomicScenario (this);

		try {
			this.loadSettingsFromFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public List<EconomicOperation> searchAfterCustomEconomicOperation (Project project, EconomicModel model, Step selectedStep){

		List<EconomicOperation> ops = new ArrayList<EconomicOperation>();
		Vector<Step> steps = project.getStepsFromRoot(selectedStep);

		for(Step step : steps){
			GScene scene = step.getScene();
			if(scene.isInterventionResult()){
				// vérifier que l'intervention est une instance de EconomicOperation

				// ajouter l'opération à la liste ops
			}
		}

		return ops;
	}


	public void addEconomicOperation (EconomicOperation op) {
		if (operations == null) operations = new ArrayList<EconomicOperation> ();
		operations.add (op);
	}

	public List<EconomicOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<EconomicOperation> operations) {
		this.operations = operations;
	}

	public Project getProject() {
		return project;
	}

	public List<EconomicOperation> getCustomOperations(){
		List<EconomicOperation> cops = new ArrayList<EconomicOperation>();
		if(operations!=null){
			for(EconomicOperation op : operations){
				if(op instanceof EconomicCustomOperation){
					cops.add(op);
				}
			}
		}
		return cops;
	} 

	/**
	 * Load the settings from an economic file
	 * Evaluates all economic operations
	 * Computes economic indicators
	 * Produces summary tables
	 * @param operations
	 * @author GL - 01/02/12
	 * @throws Exceptions can be generated at file loading
	 */
	public void evaluate (int firstDateScenario, int lastDateScenario, Integer firstDateInfiniteCylce, EconomicCase economicCase) {

		this.firstDate = firstDateScenario;
		this.lastDate = lastDateScenario;
		this.firstDateInfiniteCycle = firstDateInfiniteCylce; //
		this.nYears = lastDateScenario - firstDateScenario; 
		this.economicCase = economicCase;

		//add the economic operations recorded in the settings to update the list of EconomicOperation in This economicScenario
		List<EconomicOperation> fileOperations = settings.getOperations ();

		for(EconomicOperation op : fileOperations){
			op.computeValidityDates(firstDateScenario, lastDateScenario);
			addEconomicOperation (op);
		}

		createStandardizedEconomicOperation(operations, this.project.getLastStep());
		//		createSummarizedEconomicOperation(getStandardizedEconomicOperations());
		calcStandDescriptionAtkeyPoints(this.settings); 

		//get or compute land/discount rate
		Double discountRate = getSettings().getDiscountRate();
		Double land = getSettings().getLand();

		if(economicCase.equals(EconomicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE)){
			//then we can compute missing land or rate value
			discountRateGiven = false;
			if(discountRate!=null && discountRate >= 0 && discountRate <= 1){discountRateGiven = true;}
			landGiven = false;
			if(land != null && land >= 0){landGiven = true;}
			if(landGiven && !discountRateGiven){
				discountRate = calcDiscountRateFromLand(land, firstDateScenario, firstDateInfiniteCylce, lastDateScenario, this.standardizedEconomicOperations, economicCase);
				this.estimatedDiscountRate = discountRate;
			}else if(!landGiven && discountRateGiven){
				land = calcBASI(discountRate, this.standardizedEconomicOperations, firstDateScenario, firstDateInfiniteCylce, lastDateScenario, economicCase);
			}
		}
		
		calcStaticIndicators(discountRate, land);
	}

	/**
	 * To keep compatibility with previous version
	 */
	public void evaluate (int firstDateScenario, int lastDateScenario) {
		evaluate (firstDateScenario, lastDateScenario, firstDateScenario, economicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE);
	}



	/**
	 * This method load a file with economic feature 
	 * update the list of economic operation of economic scenario
	 * do some checks about the values found in the file
	 */
	public void loadSettingsFromFile () throws Exception{

		if(settings.getFileName ()!=null){ //if an economic file was recorded
			EconomicSettingsLoader csl;
			csl = new EconomicSettingsLoader (settings.getFileName ());
			csl.loadSettings (settings);

			Log.println ("economics2", "EconomicScenario.init() - the scenario was initialized with an economic file"); 
		}else{
			Log.println ("economics2", "EconomicScenario.init() - no economic file was recorded before loading file"); 
		}
	}

	/**find the scene corresponding to first and last date (as defined in this economic scénario)
	 * Note : these scene are not always found!
	 * @param step
	 */
	private void findkeyScenes(Step step){

		Vector<Step> steps = this.getProject().getStepsFromRoot(step);
		for(Step s : steps){
			EconomicScene es = (EconomicScene) s.getScene();
			int date = es.getDate();

			if(date == this.firstDate){ //first scene before any intervention
				sceneAtFirstDate = es;
			}else if(date == this.firstDateInfiniteCycle){ //first scene before any intervention ??
				sceneAtFirstDateInfiniteCycle = es;
			}else if(date == this.lastDate){ //last scene, could be after intervention
				sceneAtLastDate = es;
			}
		}
	}

	private void calcStandDescriptionAtkeyPoints(EconomicSettings settings){
		findkeyScenes(this.project.getLastStep());

		if(sceneAtFirstDate != null){
			EconomicStandDescription fs = new EconomicStandDescription(sceneAtFirstDate, settings);
			standDescriptionAtFirstDate  = fs;
			fs.toPrint();
		}else{
			System.out.println("EconomicScenario : No stand description at year " + this.firstDate);
		}

		if(sceneAtFirstDateInfiniteCycle != null){
			EconomicStandDescription fs = new EconomicStandDescription(sceneAtFirstDateInfiniteCycle, settings);
			standDescriptionAtFirstDateInfiniteCycle  = fs;
			fs.toPrint();
		}else{
			System.out.println("EconomicScenario : No stand description at year " + this.firstDateInfiniteCycle);
		}

		if(sceneAtLastDate != null){
			EconomicStandDescription fs = new EconomicStandDescription(sceneAtLastDate, settings);
			standDescriptionAtLastDate = fs;
			fs.toPrint();
		}else{
			System.out.println("EconomicScenario : No stand description at year " + this.lastDate);
		}
	}


	//	/** compute by species and total number of trees, quadratic mean diameter, basal area, volume
	//	 * 
	//	 * @param scene
	//	 */
	//	private void describeEconomicScene(EconomicScene scene){
	//		List<EconomicTree> trees = scene.getEconomicTrees();
	//		Map<int sp, double[]> sceneDescription
	//	}

	/**
	 * create comparable list of economic operation
	 * @param scene : start scene from where to evaluate 
	 * @author GL - 01/02/12
	 */
	private void createStandardizedEconomicOperation(List<EconomicOperation> operations, Step step){
		// create a list per year of expanse and income
		List<EconomicStandardizedOperation> arrayOperations = new ArrayList<EconomicStandardizedOperation>();

		// set right income/expanse with right scene
		// We need the scene to compute the economic value
		// check whether there are operations and steps
		Vector<Step> steps = this.getProject().getStepsFromRoot(step);
		//if(arrayOperations.size()==0 || steps.size()==0) return;

		// initialization
		int sceneIndex=0;
		int initialDate=steps.get(0).getScene().getDate();
		int lastSceneIndex = steps.size();

		// go through every economic operations and add them date by date
		for (EconomicOperation operation : operations){
			String label = operation.getLabel();
			double expanse = 0;
			double income = 0;
			double harvestedVolume = 0;

			if(operation.getTrigger()==EconomicOperation.Trigger.ON_INTERVENTION){ // in this particular case we have the associated scene

				if (operation.isIncome()){
					income = operation.getValue((EconomicScene)((EconomicCustomOperation) operation).getScene(),this);
				}else{
					expanse = operation.getValue((EconomicScene)((EconomicCustomOperation) operation).getScene(),this);
				}

				int date = ((EconomicScene)((EconomicCustomOperation) operation).getScene()).getDate();

				if(date >= this.firstDate && date <= this.lastDate){
					EconomicScene s = ((EconomicScene)((EconomicCustomOperation) operation).getScene());

					harvestedVolume = 0;
					if(s.getHarvestedEconomicTrees() != null){
						for(EconomicTree t : s.getHarvestedEconomicTrees()){harvestedVolume += t.getEconomicVolume_m3() * 10000 / s.getArea ();}
					}

					arrayOperations.add(new EconomicStandardizedOperation(date,label,income,expanse,harvestedVolume,operation));
				}

			}else{

				for (int date : operation.getValidityDates()){
					// find the scene to compute the economic value
					if (date - initialDate < 0) {
						sceneIndex = 0;
					}else{
						//take the first scene with the same date
						//the following ones can be intervention results
						for(int i = 0; i < lastSceneIndex; i++){
							EconomicScene es = (EconomicScene) steps.get(sceneIndex).getScene();
							if(es.getDate () == date){
								sceneIndex = i;
								break;
							}
						}
					}

					//get the value
					if (operation.isIncome()){
						income= operation.getValue((EconomicScene)steps.get(sceneIndex).getScene(),this);
					}else{
						expanse = operation.getValue((EconomicScene)steps.get(sceneIndex).getScene(),this);
					}

					if(date >= this.firstDate && date <= this.lastDate){
						EconomicScene s = ((EconomicScene)steps.get(sceneIndex).getScene());
						harvestedVolume = 0;
						if(s.getHarvestedEconomicTrees() != null){
							for(EconomicTree t : s.getHarvestedEconomicTrees()){harvestedVolume += t.getEconomicVolume_m3() * 10000 / s.getArea ();}
						}

						arrayOperations.add(new EconomicStandardizedOperation(date,label,income,expanse,harvestedVolume,operation));
					}
				}
			}

			//			System.out.println ("EconomicScenario.StdOperation - label = " + label + " income =" + income + " expense = " + expanse + "volume " + harvestedVolume + " Trigger " + operation.trigger);

		}

		Collections.sort(arrayOperations);
		setStandardizedEconomicOperations(arrayOperations);

	}

	/**
	 * Compute economic indicators from summary tables
	 * This method use the class variables : discountRate, dates, ... and set the indicator variables of this class (vt,bt,...,tir,...) 
	 * @author GL - 01/02/2012
	 */
	private void calcStaticIndicators(double discountRate, Double land){

		int n = nYears; //number of years

		vt = calcHarvestedVolume(standardizedEconomicOperations);
		vm = vt/n;
		bt = calcNetPresentValue(0d, standardizedEconomicOperations, firstDate);
		bm = bt/n;
		rt = calcRt(standardizedEconomicOperations);
		rm = rt/n;
		bas = calcNetPresentValue (discountRate, standardizedEconomicOperations, firstDate);

		// indicators for which computations depends on the chosen economic case
		basi = calcBASI(discountRate, standardizedEconomicOperations, firstDate, firstDateInfiniteCycle, lastDate, economicCase);

		initialFEV = basi; //Basi can be null (TRANSITORY_PERIOD)

		if(economicCase.equals(EconomicCase.TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE)){
			double bas2 = calcNetPresentValueInfiniteCycle (discountRate, standardizedEconomicOperations,firstDate,firstDateInfiniteCycle,lastDate);
			int n2 = lastDate - firstDateInfiniteCycle;
			double basi2 = bas2 * Math.pow(1+discountRate,n2)/(Math.pow(1+discountRate,n2)-1);
			finalFEV = basi2;
		}else{
			finalFEV = basi; //can be null (TRANSITORY_PERIOD)
		}

		basf = calcBASF(discountRate, initialFEV, finalFEV, standardizedEconomicOperations, firstDate, lastDate, economicCase);

		tir = calcIRR(n, standardizedEconomicOperations, "TIR", firstDate, economicCase);
		tirf = calcIRR2(initialFEV, finalFEV, n, standardizedEconomicOperations, "TIRF", firstDate, economicCase);
		forwardAnnuity = calcForwardAnnuity(initialFEV, finalFEV, discountRate, standardizedEconomicOperations, firstDate, lastDate);

		printEconomicResults();
		printStandardizedOperations(); //this must always run after evaluate!		
	}


	/**
	 * Method to compute forest/land expectation value at first year (Faustmann formula)
	 * @param r = discount rate
	 * @param ops = list of considered economic operations
	 * @param firstDate = first date of economic scenario
	 * @param intermediateDate = first year of (optionnally) infinite cycle
	 * @param lastDate = last year of economic scenario
	 * @param economicCase = economic case (see enum)
	 * @return
	 */
	public Double calcBASI(double r, List<EconomicStandardizedOperation> ops, int firstDate, Integer intermediateDate, int lastDate, EconomicCase economicCase){

		int n = lastDate - firstDate;

		//checks
		try {
			if(r<0 | r>1){throw new Exception("EconomicScenario.calcBASI() - the discount rate must be within 0-1 range");}

			if (n == 0){
				Log.println ("economics2", "EconomicScenario.calcBASI() - number of years = 0, BASI will be equal to infinity"); 
				System.out.println("EconomicScenario.calcBASI() - number of years = 0, BASI will be equal to infinity"); 
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Double basi = null;

		if(economicCase.equals(EconomicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE) | economicCase.equals(EconomicCase.INFINITY_CYCLE_WITHOUT_LAND_OBSERVATION)){

			double bas = calcNetPresentValue (r, standardizedEconomicOperations, firstDate);
			basi = bas * Math.pow(1+r,n)/(Math.pow(1+r,n)-1);

		}else if (economicCase.equals(EconomicCase.TRANSITORY_PERIOD)){
			// Impossible to compute an infinite sequence of cost and incomes
			basi = null;

		}else if (economicCase.equals(EconomicCase.TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE)){
			int n2 = lastDate - intermediateDate; //period of the infinity cycle
			double bas1 = calcNetPresentValueTransitoryPeriod (r, ops,firstDate,intermediateDate,lastDate);
			double bas2 = calcNetPresentValueInfiniteCycle (r, ops,firstDate,intermediateDate,lastDate);
			basi = bas1 + (bas2 * Math.pow(1+r,n2)/(Math.pow(1+r,n2)-1))/Math.pow(1+r,intermediateDate-firstDate);
		}

		return basi;
	}

	/**
	 * Compute the net present value considering an initial investment equals to land or forest expectation value
	 * @param r = discount rate [0,1]
	 * @param ops = list of considered economic operations
	 * @param firstDate = first date of economic scenario
	 * @param lastDate = last year of economic scenario
	 * @param economicCase = economic case (see enum)
	 * @return
	 */
	public Double calcBASF(double r, Double initialFEV, Double finalFEV, List<EconomicStandardizedOperation> ops, int firstDate, int lastDate, EconomicCase economicCase){

		Double basf = null;

		int n = lastDate - firstDate;
		double bas = calcNetPresentValue (r, ops, firstDate);

		if(initialFEV != null & finalFEV != null){	
			basf = - initialFEV + bas + finalFEV / Math.pow(1+r,n) ;
		}

		return basf;
	}




	/**
	 * Compute forest value at year a for even-aged (?) stands
	 * @param a = measurement year
	 * @param n = number of years between plantation and clear-cut
	 * @param r = discount rate
	 * @param summary = summary of the economic operation
	 */
	public double calcBlocValue (int a, int n, double r, List<EconomicStandardizedOperation> ops){
		double v1 = 0;
		double v2 = 0;

		for (EconomicStandardizedOperation op : ops){
			int i = op.getDate(); //current date
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(i>=a){
				v1 += (income - expanse) * Math.pow(1 + r,n+a-i);
			}else{
				v2 += (income - expanse) * Math.pow(1 + r,a-i);
			}
		}
		return (v1 + v2)/(Math.pow (1+r,n)-1);
	}

	public double calcBlocValuePr (int a, int n, double r, double f, List<EconomicStandardizedOperation> ops){
		double v1 = 0;

		for (EconomicStandardizedOperation op : ops){
			int i = op.getDate(); //current date
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(i<a){
				v1 += (expanse - income) * Math.pow(1 + r,a-i);
			}
		}
		return f * Math.pow (1+r,a) + v1;
	}

	public double calcBlocValueAtt (int a, int n, double r, double f, List<EconomicStandardizedOperation> ops){
		double v1 = 0;

		for (EconomicStandardizedOperation op : ops){
			int i = op.getDate(); //current date
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(i>=a){
				v1 += (income - expanse) * Math.pow(1 + r,a-i);
			}
		}
		return f * Math.pow (1+r,a-n) + v1;
	}

	public void printEconomicResults(){

		System.out.println ("--------------");
		System.out.println ("land = " + getLand() + " EUR");
		System.out.println ("Discount rate [0-1] = " + getDiscountRate());
		System.out.println ("Number of years = " + nYears);
		System.out.println ("--------------");

		Double tirpc = getTir () == null ? null : 100d*getTir ();
		Double tirfpc = getTirf () == null ? null : 100d*getTirf ();

		System.out.println("Bt = " + Math.round(getBt()*100d)/100d
				+ "; Bm = " + Math.round(getBm()*100d)/100d
				+ "; Rt = " + Math.round(getRt()*100d)/100d
				+ "; Rm = " + Math.round(getRm()*100d)/100d
				+ "; BAS = " + Math.round(getBas()*100d)/100d
				+" ; BASF = "+ toStringVariable(getBasf())
				+" ; BASI = "+ toStringVariable(getBasi())
				+" ; TIR = "+ toStringVariable(tirpc) + "%"
				+" ; TIRF = "+ toStringVariable(tirfpc) + "%"
				+" ; VT = " + Math.round(getVt()*100d)/100d 
				+" ; VM = " + Math.round(getVm()*100d)/100d
				+" ; annuity = " + toStringVariable(getForwardAnnuity())
				//				+" ; ba = " + Math.round(getBackwardAnuity()*100d)/100d
				);
	}

	public static String storeEconomicResultsInALineHeader(String sep){
		String l = "Bt" + sep
				+ "Bm" + sep
				+ "Rt" + sep
				+ "Rm" + sep
				+ "BAS" + sep
				+ "BASF" + sep
				+ "BASI" + sep
				+ "TIR" + sep
				+ "TIRF" + sep
				+ "VT" + sep
				+ "VM" + sep
				+ "annuity"  //+sep
				//				+ "BA"
				;
		return l;
	}

	public String storeEconomicResultsInALine(String sep){
		String l =  getBt() + sep
				+ getBm() + sep
				+ getRt() + sep
				+ getRm() + sep
				+ getBas() + sep
				+ toStringVariable(getBasf()) + sep
				+ toStringVariable(getBasi()) + sep
				+ toStringVariable(getTir ()) + sep
				+ toStringVariable(getTirf ()) + sep
				+ getVt() + sep
				+ getVm() + sep
				+ getForwardAnnuity() //+ sep
				//				+ getBackwardAnuity()
				;
		return l;
	}

	public void storeEconomicResults(String filename){
		String sep = ";";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(createResultBuffer(sep).toString());
			bw.flush();
			bw.close();

		} catch (IOException e) {
			Log.println (Log.ERROR, "EconomicScenario.storeEconomicResults()","Could not write in file: " + filename, e);
			e.printStackTrace();
		}
		System.out.println ("EconomicScenario.storeEconomicResults() - the economic results have been writen in " + filename);
	}

	public StringBuffer createBillBookBuffer(String sep){
		StringBuffer sb = new StringBuffer ();

		sb.append ("Date" + sep + "Label" + sep + "Income" + sep + "Expanse" + sep + "Harvested volume");
		sb.append(System.getProperty("line.separator"));

		List<EconomicStandardizedOperation> listOfOperations = getStandardizedEconomicOperations();

		if(listOfOperations != null){
			for (EconomicStandardizedOperation co : listOfOperations){
				sb.append(co.toString(sep));
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb;
	}

	public StringBuffer createResultBuffer(String sep){
		StringBuffer sb = new StringBuffer ();
		sb.append("ECONOMIC2 - A capsis library - simulation results");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append("land" + sep + getLand());
		sb.append(System.getProperty("line.separator"));
		sb.append("discount rate" + sep + getDiscountRate());
		sb.append(System.getProperty("line.separator"));
		sb.append ("nb years" + sep + this.nYears);
		sb.append(System.getProperty("line.separator"));
		sb.append("Bt (net total value)" + sep + this.bt);
		sb.append(System.getProperty("line.separator"));
		sb.append("Bm (net annual value)" + sep + this.bm);
		sb.append(System.getProperty("line.separator"));
		sb.append("Rt (total income)" + sep + this.bt);
		sb.append(System.getProperty("line.separator"));
		sb.append("Rm (annual income)" + sep + this.bm);
		sb.append(System.getProperty("line.separator"));
		sb.append("BAS (net present value)" + sep + this.bas);
		sb.append(System.getProperty("line.separator"));
		sb.append("BASF (net present value with land)" + sep + toStringVariable(this.basf));
		sb.append(System.getProperty("line.separator"));
		sb.append ("BASI (land expectation value)" + sep + toStringVariable(this.basi));
		sb.append(System.getProperty("line.separator"));
		sb.append("TIR (internal rate of return)" + sep + toStringVariable(this.tir));
		sb.append(System.getProperty("line.separator"));
		sb.append("TIRF (internal rate of return with land)" + sep + toStringVariable(this.tirf));
		sb.append(System.getProperty("line.separator"));
		sb.append("Total harvested volume" + sep + this.vt);
		sb.append(System.getProperty("line.separator"));
		sb.append("Mean annual harvested volume" + sep + this.vm);
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));

		sb.append(System.getProperty("line.separator"));
		sb.append("Annual economic operations");
		sb.append(System.getProperty("line.separator"));

		sb.append(createBillBookBuffer(sep));

		return sb;
	}

	/**
	 * Compute the net present value (BAS) 
	 * @param r = discount rate [0,1]
	 * @param ops = list of std operation from an economic scenario
	 * @param refYear = first date for economic computation
	 * @author GL 
	 */ 
	public double calcNetPresentValue(double r, List<EconomicStandardizedOperation> ops, int refYear){
		double bas = 0;
		for (EconomicStandardizedOperation op : ops){
			int date = op.getDate();
			int nbYears = date - refYear;
			double income = op.getIncome();
			double expanse = op.getExpanse();

			bas += (income - expanse) / Math.pow(1 + r,nbYears); 

			//			System.out.println("calcNetPresentValue - income " + income + " - rate " + r + " - VA " + ( (income - expanse) / Math.pow(1 + r,nbYears)) + " - BAS " + bas);
		}
		return bas;
	}

	/**
	 * Compute the net present value (BAS) associated to the scenario
	 * fluxes are actualized at first date
	 * 
	 * gl removed static modifier 
	 */ 
	private double calcNetPresentValueTransitoryPeriod(double r, List<EconomicStandardizedOperation> ops, int firstDate, int intermediateDate, int lastDate){
		double bas = 0;
		for (EconomicStandardizedOperation op : ops){
			int date = op.getDate();
			//gl--- the previous formula was not correct whenever the initial date was not zero
			int nbYears = date - firstDate;
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(date >= firstDate & date <= intermediateDate){
				bas += (income - expanse) / Math.pow(1 + r,nbYears); 
			}
			//			System.out.println("calcNetPresentValue - income " + income + " - rate " + r + " - VA " + ( (income - expanse) / Math.pow(1 + r,nbYears)) + " - BAS " + bas);
		}
		return bas;
	}

	/**
	 * Compute the net present value (BAS) of the infinity cycle
	 * Economics fluxes are actualized at first date of the inifinite cycle
	 * 
	 * gl removed static modifier 
	 */ 
	private double calcNetPresentValueInfiniteCycle(double r, List<EconomicStandardizedOperation> ops, int firstDate, int intermediateDate, int lastDate){
		double bas = 0;
		for (EconomicStandardizedOperation op : ops){
			int date = op.getDate();
			int nbYears = date - intermediateDate;
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(date > intermediateDate & date <= lastDate){// on ne compte pas les recettes et dépenses de l'année 0 du cycle infini
				bas += (income - expanse) / Math.pow(1 + r,nbYears); 
			}
		}
		return bas;
	}

	private static double calcHarvestedVolume(List<EconomicStandardizedOperation> stdops){
		double v = 0;
		for(EconomicStandardizedOperation op : stdops){v+=op.getHarvestedVolume ();}
		return v;
	}

//	private double calcIRR(double land, int n, List<EconomicStandardizedOperation> ops, String method, int firstDate) {
//		int nIter = 0;
//		double maxIter = 1000;
//		double precision = 0.00001;	
//		double lowGuessRate = 0.0001;
//		double highGuessRate = 0.15;	
//		double guessRate = (lowGuessRate+highGuessRate)/2;
//
//		double x =  calcNetPresentValue(guessRate, ops, firstDate);
//		if (method.equals("calcDiscountRateFromLand()")) x = -land + x * Math.pow (1+guessRate, n) / (Math.pow (1+guessRate, n) - 1);
//		else if (method.equals("calcTIRF()")) x = -land + x + land / Math.pow(1+guessRate,n);
//
//		while (Math.abs(x) > precision)	{
//
//			if (x < 0) {highGuessRate = guessRate;} else { lowGuessRate = guessRate;}
//
//			if ((highGuessRate-lowGuessRate) < precision){
//				System.out.println ("EconomicScenario." + method + " - Convergence stopped with low limit to " + lowGuessRate + " and high limit to " + highGuessRate);
//				return guessRate;
//			}
//
//			guessRate = (lowGuessRate+highGuessRate)/2;
//			x = calcNetPresentValue(guessRate, ops, firstDate);
//			if (method.equals("calcDiscountRateFromLand()")) x = -land + x * Math.pow (1+guessRate, n) / (Math.pow (1+guessRate, n) - 1);
//			else if (method.equals("calcTIRF()")) x = -land + x + land / Math.pow(1+guessRate,n);
//
//			//			System.out.println ("EconomicScenario." + method + " - No iter = " + nIter + " - net present value = " + x + " - guess internal rate of return = " + guessRate );
//
//			if (nIter >= maxIter){
//				Log.println (Log.WARNING,"EconomicScenario." + method,"False convergence in TIR computation. I returned -1, the guessed last value was " + x);
//				System.out.println ("EconomicScenario." + method + " - False convergence in TIR computation. I returned -1, the guessed last value was " + x);
//				return -1d;
//			}
//			nIter ++;
//		}
//
//		System.out.println ("EconomicScenario." + method + " - Normal convergence achieved of internal rate of return  " + guessRate + " with No iter = " + nIter);
//		return guessRate;		
//	}


//	/**
//	 * Compute the internal rate of return 
//	 * @param land = initial investment
//	 * @param n = number of years
//	 * @param ops = list of considered economic operations
//	 * @param method = method name ("TIRF", "TIR", "rateFromLand")
//	 * @param refDate = date of reference
//	 * @TODO ce n'est pas très élégant d'utiliser des string comme option de calcul!!!
//	 * @return
//	 */
//	private Double calcIRRBruteForceAlgo(Double initialFEV, Double finalFEV, int n, List<EconomicStandardizedOperation> ops, String method, int refDate, EconomicCase economicCase) {
//		double precision = 0.001;	
//		double lowGuessRate = 0.001;
//		double highGuessRate = 1.000; //10 000 iterations	
//		
//		//initialization
//		double estimatedPrecision = 9999;
//		double precisionMin = 9999;
//		double guessRate = - 999;
//
//		if((method.equals("TIRF") & (initialFEV==null | finalFEV==null)){return null;}
//
//		for(double r = lowGuessRate; r < highGuessRate + precision; r = r + precision){
//			double x = calcBASF(r, initialFEV, finalFEV, ops, n, refDate, economicCase); 
//			estimatedPrecision = Math.abs(0-x);
//			if(estimatedPrecision<precisionMin){
//				guessRate=r;
//				precisionMin = estimatedPrecision;
//			}
//		}
//
//		System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - estimated TIR = " + guessRate + " with precision = " + precisionMin);
//
//		if(guessRate == lowGuessRate | guessRate == highGuessRate){
//			System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - out of bound estimated TIR : guess rate = " + guessRate);
//			return null;
//		}
//
//		return guessRate; 
//
//	}
	
	/**
	 * Compute the internal rate of return
	 * method = brute force algorithm
	 */
	private Double calcIRR(int n, List<EconomicStandardizedOperation> ops, String method, int refDate, EconomicCase economicCase){
		double precision = 0.001;	
		double lowGuessRate = 0.001;
		double highGuessRate = 1.000; //10 000 iterations	
		
		//initialization
		double estimatedPrecision = 9999;
		double precisionMin = 9999;
		double guessRate = - 999;

		for(double r = lowGuessRate; r < highGuessRate + precision; r = r + precision){
			double x = calcNetPresentValue(r, ops, refDate);
			estimatedPrecision = Math.abs(0-x);
			if(estimatedPrecision<precisionMin){
				guessRate=r;
				precisionMin = estimatedPrecision;
			}
		}
		System.out.println("EconomicScenario.calcIRR() - estimated TIR = " + guessRate + " with precision = " + precisionMin);
		if(guessRate == lowGuessRate | guessRate == highGuessRate){
			System.out.println("EconomicScenario.calcIRR() - out of bound estimated TIR : guess rate = " + guessRate);
			return null;
		}
		return guessRate;
	}
	
	/** Compute the internal rate of return taking into account an initial investion of FEVt=0 and final income of discounted FEVt=n
	 * 	method = brute force algorithm
	 */
	private Double calcIRR2(Double initialFEV, Double finalFEV, int n, List<EconomicStandardizedOperation> ops, String method, int refDate, EconomicCase economicCase){
		double precision = 0.001;	
		double lowGuessRate = 0.001;
		double highGuessRate = 1.000; //10 000 iterations	
		
		//initialization
		double estimatedPrecision = 9999;
		double precisionMin = 9999;
		double guessRate = - 999;

		if(initialFEV==null | finalFEV==null){return null;}

		for(double r = lowGuessRate; r < highGuessRate + precision; r = r + precision){
			double x = -999;

			if (method.equals("TIRF")) {
				x = calcBASF(r, initialFEV, finalFEV, ops, n, refDate, economicCase); //refDate???
			}else if (method.equals("rateFromLand")){
				x = calcBASI(r, ops, refDate, refDate, refDate, economicCase) - initialFEV;  //il faut les dates!!! 
			}else {
				x = calcNetPresentValue(r, ops, refDate);
			}

			//			System.out.println("CalcIRRBrutalIteration() - estimated TIR = " + r + " for BAS or BASF = " + x);
			estimatedPrecision = Math.abs(0-x);
			if(estimatedPrecision<precisionMin){
				guessRate=r;
				precisionMin = estimatedPrecision;
			}
		}

		System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - estimated TIR = " + guessRate + " with precision = " + precisionMin);

		if(guessRate == lowGuessRate | guessRate == highGuessRate){
			System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - out of bound estimated TIR : guess rate = " + guessRate);
			return null;
		}

		return guessRate;
	}
	
	
	/** brute force algorithm to search the discount rate so that BASI - land = 0
	 */
	private Double calcDiscountRateFromLand(double land, int firstDate, Integer intermediateDate, int lastDate, List<EconomicStandardizedOperation> ops, EconomicCase economicCase){
		int n = lastDate - firstDate;
		
		double precision = 0.0001;	
		double lowGuessRate = 0.001;
		double highGuessRate = 1.000; //100 000 iterations	
		
		//initialization
		double estimatedPrecision = 9999;
		double precisionMin = 9999;
		double guessRate = - 999;

		for(double r = lowGuessRate; r < highGuessRate + precision; r = r + precision){
			double x = calcBASI(r, ops, firstDate, intermediateDate, lastDate, economicCase) - land;  
			estimatedPrecision = Math.abs(0-x);
			if(estimatedPrecision<precisionMin){
				guessRate=r;
				precisionMin = estimatedPrecision;
			}
		}

		System.out.println("EconomicScenario.calcDiscountRateFromLand() - estimated TIR = " + guessRate + " with precision = " + precisionMin);

		try{
			if(guessRate == lowGuessRate | guessRate == highGuessRate){
				System.out.println("EconomicScenario.calcDiscountRateFromLand() - out of bound estimated discount rate : guess rate = " + guessRate);
				throw new Exception("EconomicScenario.calcDiscountRateFromLand() - Out of bound estimated discount rate : guess rate = " + guessRate);
			}
		}catch (Exception e) {
				e.printStackTrace();
		}

		return guessRate; 
		//		return calcIRR(land, n, summary, "calcDiscountRateFromLand()");	

	}

	//	/**
	//	 * Compute the internal rate of return with land (TIRF) with an iterative process
	//	 * @author GL 29/07/2013
	//	 */
	//	private Double calcTIRF(List<EconomicStandardizedOperation> ops, int n, int firstDate){
	//		
	//		Double tir = null;
	//		
	//		if (economicCase.equals(EconomicCase.TRANSITORY_PERIOD)){
	//			// Impossible to compute an infinite sequence of cost and incomes
	//			tir = null;
	//		}else {
	//			Double basi = this.basi; //!!!!!
	//			tir = calcIRRBruteForceAlgo(basi, n, ops, "calcTIRF()", firstDate);
	//		}
	//		return tir;
	//	}

	//	/**
	//	 * Compute the internal rate of return (TIR) with an iterative process
	//	 * Taking into account no initial investment of LEV/FEV
	//	 * @author GL 29/07/2013
	//	 */
	//	private double calcTIR(List<EconomicStandardizedOperation> ops, int firstDate){
	//		return calcIRRBruteForceAlgo(0, 0, ops, "calcTIR()", firstDate);
	//		//		return calcIRR(0, 0, summary, "calcTIR()");
	//	}

	/**
	 * Compute the sum of all revenues
	 * @param list of summarized operations
	 * @return a double value with the sum of all revenue
	 * @author GL 15/06/2016
	 */
	private double calcRt(List<EconomicStandardizedOperation> ops){
		double rt = 0;
		for (EconomicStandardizedOperation op : ops){
			double income = op.getIncome();
			rt += income; 
		}
		return rt;
	}

	/**
	 * Compute forward annuity according to Hanewinkel 2014
	 * 
	 */
	private Double calcForwardAnnuity(Double initialFEV, Double finalFEV, double r, List<EconomicStandardizedOperation> ops, int firstDate, int lastDate){

		Double a = null;
		int n = lastDate - firstDate;


		if(initialFEV != null & finalFEV!=null){
			double SVtb = initialFEV;
			double SVte = finalFEV;

			double sum = 0;

			for (EconomicStandardizedOperation op : ops){
				double income = op.getIncome();
				double expanse = op.getExpanse();
				sum += (income - expanse) / Math.pow(1+r,op.getDate()-r); 
			}
			a = (SVte / Math.pow(1+r,n) - SVtb + sum)*(r*Math.pow(1+r,n))/(Math.pow(1+r, n)-1);
		}
		return a;
	}

	//	/**
	//	 * Compute backward annuity according to Hanewinkel 2014 ????? Error in published formula??? always the same as forward annuity
	//	 * 
	//	 */
	//	private double calcBackwardAnnuity(List<EconomicStandardizedOperation> ops){
	//		double a=0;
	//		double SVtb = 0;
	//		double SVte = 0;
	//		if(standDescriptionAtFirstDate != null){
	//			SVtb = this.standDescriptionAtFirstDate.getMarketValue();
	//		}else{
	//			System.out.println("EconomicScenario.calcBackwardAnnuity() - Makert value was not computed for initial scene and then set to 0");
	//		}
	//		if(standDescriptionAtLastDate != null){
	//			SVte = this.standDescriptionAtLastDate.getMarketValue();
	//		}else{
	//			System.out.println("EconomicScenario.calcBackwardAnnuity() - Makert value was not computed for last scene and then set to 0");
	//		}
	//
	//		double sum = 0;
	//		for (EconomicStandardizedOperation op : ops){
	//			double income = op.getIncome();
	//			double expanse = op.getExpanse();
	//			sum += (income - expanse) / Math.pow(1+this.discountRate,op.getDate()-this.firstDate); 
	//		}
	//		a = (SVte / Math.pow(1+discountRate,nYears) - SVtb + sum)*(discountRate*Math.pow(1+discountRate,nYears))/(Math.pow(1+discountRate, nYears)-1);
	//		return a;
	//	}


	/**
	 * Print the summary table in console
	 * GL - 22/02/2012
	 */
	public void printStandardizedOperations(){
		String sep = "\t";
		System.out.println("---");
		System.out.println("-printStandardizedOperations()-");
		System.out.println("---");
		System.out.println("Date" + sep + "Label" + sep + "Income" + sep + "Expanse" + sep + "volume");
		System.out.println("---");	

		List<EconomicStandardizedOperation> listOfOperations = getStandardizedEconomicOperations();

		if(listOfOperations != null){
			for (EconomicStandardizedOperation co : listOfOperations){
				System.out.println(co.toString(sep));
			}
		}
		System.out.println("---");

	}

	//	/**
	//	 * A summary of all operations that occurs the same year
	//	 * those object can be sorted
	//	 * 
	//	 * This object is now useless. Try not using it!
	//	 * 
	//	 * @author GL - 01/02/12
	//	 *
	//	 */
	//	protected class SummarizedEconomicOperation  implements java.lang.Comparable{
	//
	//		private int date;
	//		private double expanse;
	//		private double income;
	//
	//		//empty constructor
	//		SummarizedEconomicOperation(){}
	//
	//		//full constructor
	//		SummarizedEconomicOperation(int date, double income, double expanse, boolean isWithinInfiniteCycle){
	//			this.date = date;
	//			this.expanse = expanse;
	//			this.income = income;
	//		}
	//
	//		@Override
	//		public int compareTo(Object summarizedOperations) {
	//			int date1 = ((SummarizedEconomicOperation) summarizedOperations).getDate();
	//			int date2 = this.getDate();
	//			if (date1 < date2) return -1;
	//			else if(date1 == date2) return 0;
	//			else return 1;
	//		}
	//
	//		/**
	//		 * To String method that could be used to produce a txt file
	//		 * GL - 22/02/2012
	//		 */
	//		public String toString(String separator){
	//			String msg;
	//			msg = getDate() + separator + Math.round(getIncome()*100d)/100d + separator + Math.round(getExpanse()*100d)/100d ;
	//			return msg;
	//		}
	//
	//		// --- Accessors
	//		public int getDate() {return this.date;}
	//		public double getExpanse() {return expanse;}
	//		public double getIncome() {return income;}
	//		public void setDate(int date) {this.date = date;}
	//		public void addExpanse(double expanse) {this.expanse += expanse;}
	//		public void addIncome(double income) {this.income += income;}
	//	}


	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Method to print with two decimals the Double variable. Write NaN if no value was computed
	 * @param d : Double value
	 */
	public String toStringVariable(Double d){
		if(tirf == null){
			return "NaN";
		}else{
			return Double.toString(Math.round(d*100d)/100d);
		}
	}


	// --- Accessors --- GL 01/02/12
	public double getBt () {return bt;}
	public void setBt (double b) {this.bt = b;}
	public double getBm() {return bm;}
	public void setBm(double bm) {this.bm = bm;}
	public double getRt() {return rt;}
	public void setRt(double rt) {this.rt = rt;}
	public double getRm() {return rm;	}
	public void setRm(double rm) {this.rm = rm;}
	public Double getTir() {return tir;}
	public void setTir(double tir) {this.tir = tir;}
	public Double getTirf() {return tirf;}

	public void setTirf(double tirf) {this.tirf = tirf;}
	public Double getBasi() {return basi;}
	public void setBasi(double basi) {this.basi = basi;}
	public Double getBasf() {return basf;}
	public void setBasf(double basf) {this.basf = basf;}
	public double getBas() {return bas;}
	public void setBas(double bas) {this.bas = bas;}
	public double getVm() {return vm;}
	public void setVm(double vm) {this.vm = vm;}
	public double getVt() {return vt;}
	public void setVt(double vt) {this.vt = vt;}
	public Double getForwardAnnuity() {return forwardAnnuity;}
	public void setForwardAnnuity(double forwardAnnuity) {this.forwardAnnuity = forwardAnnuity;}
	public Double getBackwardAnuity() {return backwardAnuity;}
	public void setBackwardAnuity(double backwardAnuity) {this.backwardAnuity = backwardAnuity;}
	public List<EconomicStandardizedOperation> getStandardizedEconomicOperations() {return standardizedEconomicOperations;}
	//	public List<SummarizedEconomicOperation> getSummarizedEconomicOperations() {return summarizedEconomicOperations;}
	public void setStandardizedEconomicOperations(List<EconomicStandardizedOperation> comparableOperation) {
		this.standardizedEconomicOperations = comparableOperation;
	}
	//	public void setSummarizedEconomicOperations(List<SummarizedEconomicOperation> summarizeOperations) {
	//		this.summarizedEconomicOperations = summarizeOperations;
	//	}
	public Double getDiscountRate() {return getSettings().getDiscountRate();}
	//	public void setDiscountRate(double r) {this.discountRate = r;}
	public Double getLand() {return getSettings().getLand();}
	//	public void setLand(double f) {this.land = f;} // land and discount rate must be set in the settings!
	public EconomicModel getModel () {return model;}
	public void setModel (EconomicModel model) {this.model = model;}
	public EconomicSettings getSettings () {return settings;}
	public void setSettings (EconomicSettings settings) {this.settings = settings;}

	public int getnYears() {return nYears;}
	public int getFirstDate() {return firstDate;}
	public int getLastDate() {return lastDate;}
	public int getFirstDateInfiniteCycle() {return firstDateInfiniteCycle;}

	public EconomicStandDescription getStandDescriptionAtFirstDate() {return standDescriptionAtFirstDate;}
	public EconomicStandDescription getStandDescriptionAtLastDate() {return standDescriptionAtLastDate;}
	public EconomicStandDescription getStandDescriptionAtFirstDateInfiniteCycle() {return standDescriptionAtFirstDateInfiniteCycle;}
//	public boolean isLandIsEstimated() {return landIsEstimated;}

	public EconomicCase getEconomicCase(){return this.economicCase;}
	public void setEconomicCase(EconomicCase economicCase){this.economicCase = economicCase;}

	public Double getInitialFEV() {return initialFEV;}
	public void setInitialFEV(Double initialFEV) {this.initialFEV = initialFEV;}
	public Double getFinalFEV() {return finalFEV;}
	public void setFinalFEV(Double finalFEV) {this.finalFEV = finalFEV;}

	public boolean isLandgiven() {return landGiven;}
	public void setLandgiven(boolean landgiven) {this.landGiven = landgiven;}

	public boolean isDiscountRateGiven() {return discountRateGiven;}
	public void setDiscountRateGiven(boolean discountRateGiven) {this.discountRateGiven = discountRateGiven;}

	public Double getEstimatedDiscountRate() {return estimatedDiscountRate;}
	public void setEstimatedDiscountRate(Double estimatedDiscountRate) {this.estimatedDiscountRate = estimatedDiscountRate;}

}
