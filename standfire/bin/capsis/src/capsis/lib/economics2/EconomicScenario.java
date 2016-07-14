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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Log;
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
	private double tir; //those are french notations...
	private double tirf;
	private double bt;
	private double bm;
	private double rt;
	private double rm;
	private double basi; //critère de Faustmann, land expectation value
	private double basf;
	private double bas; //net present value
	private double vm; //m3/ha/year
	private double vt; //m3/ha
	private double forwardAnnuity;
	private double backwardAnuity;
	private Double discountRate; // % or [0,1] ???
	private Double land; //land value	
	private int nYears; 
	private int firstDate;
	private int lastDate;
	private boolean dateAfterIntervention;
	private int firstDateInfiniteCycle; //date of the beginning of what could be considered an infinite cycle
	private boolean landIsEstimated = true;
	private EconomicScene sceneAtFirstDate;
	private EconomicStandDescription standDescriptionAtFirstDate;
	private EconomicScene sceneAtLastDate;
	private EconomicStandDescription standDescriptionAtLastDate;
	private EconomicScene sceneAtFirstDateInfiniteCycle;
	private EconomicStandDescription standDescriptionAtFirstDateInfiniteCycle;
	//	private int EconomicCase; 

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

	public EconomicScenario getCleanCopy (){
		List<EconomicOperation> cleanOperationList = new ArrayList<EconomicOperation>();
		if(operations!=null){
			for(EconomicOperation op : operations){
				if(op instanceof EconomicCustomOperation){
					cleanOperationList.add(op);
				}
			}
		}

		EconomicScenario cleanES = new EconomicScenario(project, model);
		cleanES.setOperations(cleanOperationList);

		return cleanES;
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
	public void evaluate (int firstDateScenario, int lastDateScenario, int firstDateInfiniteCylce, boolean dateAfterIntervention) {

		this.firstDate = firstDateScenario;
		this.lastDate = lastDateScenario;
		this.firstDateInfiniteCycle = firstDateInfiniteCylce; //
		this.nYears = lastDateScenario - firstDateScenario; 
		this.dateAfterIntervention = dateAfterIntervention;
		
		// load discount rate from the settings
		try {
			if(settings.getLand ()>0){
				this.land= new Double (settings.getLand ()); //then the discount rate remain null and must be computed

				if (settings.getDiscountRate ()>0){
					Log.println (Log.WARNING,"EconomicScenario.evaluate()","You cannot fixed both the discount rate and land value. The discount rate has been estimated with the land.");
					System.out.println ("EconomicScenario.evaluate() - You cannot fixed both the discount rate and land value. The discount rate has been estimated with the land.");
				}

			}else if((settings.getDiscountRate ()>0)) {
				this.discountRate= new Double (settings.getDiscountRate ()); //then the land (fond in french) will be computed lately (for even-aged stand only)
				if (this.discountRate>1) {
					throw new Exception ("EconomicScenario.evaluate - the discount rate is greater than 1, i.e. greater than 100%");
				}
			}else{
				throw new Exception ("EconomicScenario.evaluate - the discount rate nor the land have been properly definied in the EconomicSettinfs");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//add the economic operations recorded in the settings to update the list of EconomicOperation in This economicScenario
		List<EconomicOperation> fileOperations = settings.getOperations ();
		
		for(EconomicOperation op : fileOperations){
			op.computeValidityDates(firstDateScenario, lastDateScenario);
			addEconomicOperation (op);
		}

		createStandardizedEconomicOperation(operations, this.project.getLastStep());
		//		createSummarizedEconomicOperation(getStandardizedEconomicOperations());
		calcStandDescriptionAtkeyPoints(this.settings); 
		CalcEconomicIndicators();
	}

	/**
	 * To keep compatibility with previous version
	 */
	public void evaluate (int firstDateScenario, int lastDateScenario) {
		evaluate (firstDateScenario, lastDateScenario, firstDateScenario, true);
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
			
			if(dateAfterIntervention == false){
				if(date == this.firstDate & s.getScene().isInterventionResult() == false){ //first scene before any intervention
					sceneAtFirstDate = es;
				}else if(date == this.firstDateInfiniteCycle & s.getScene().isInterventionResult() == false){ //first scene before any intervention ??
					sceneAtFirstDateInfiniteCycle = es;
				}else if(date == this.lastDate & s.getScene().isInterventionResult() == false){ //last scene, could be after intervention
					sceneAtLastDate = es;
				}
			}else{
				if(date == this.firstDate){ //first scene before any intervention
					sceneAtFirstDate = es;
				}else if(date == this.firstDateInfiniteCycle){ //first scene before any intervention ??
					sceneAtFirstDateInfiniteCycle = es;
				}else if(date == this.lastDate){ //last scene, could be after intervention
					sceneAtLastDate = es;
				}
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

	//	/**
	//	 * create a list of economic operation summarized by years
	//	 * @author GL - 01/02/12
	//	 * @param list of comparable operations
	//	 */
	//	private void createSummarizedEconomicOperation(List<EconomicStandardizedOperation> operations){
	//
	//		//sort comparable operation
	//		Collections.sort(operations);
	//
	//		//initialization
	//		List<SummarizedEconomicOperation> listSumOps = new ArrayList<SummarizedEconomicOperation>();
	//		if(operations.size()==0)return; //exit on empty list
	//		int tmpDate = operations.get(0).getDate();//first date
	//		int date = tmpDate;
	//		double expanse = 0;
	//		double income = 0;
	//		boolean infiniteCycle = false;
	//		if(date >= this.firstDateInfiniteCycle & date < this.lastDate){
	//			infiniteCycle = true;
	//		}else{
	//			infiniteCycle = false;
	//		}
	//		SummarizedEconomicOperation summarizedOp = new SummarizedEconomicOperation(date,income,expanse,infiniteCycle);
	//
	//		for (int i=0;i<operations.size();i++){//   ComparableOperation op : operations){
	//			date = operations.get(i).getDate();
	//			expanse = operations.get(i).getExpanse();
	//			income = operations.get(i).getIncome();
	//
	//			//first operation for the given date - new instance of SummarizedOperation
	//			if (date > tmpDate){
	//				//add the previous summary
	//				listSumOps.add(summarizedOp);
	//				//System.out.println("sum="+(summarizedOp.income-summarizedOp.expanse)+" date="+summarizedOp.date);
	//				if(date >= this.firstDateInfiniteCycle & date < this.lastDate){
	//					infiniteCycle = true;
	//				}else{
	//					infiniteCycle = false;
	//				}
	//				//new instance of summarizedOps
	//				summarizedOp =  new SummarizedEconomicOperation(date,income,expanse,infiniteCycle);
	//
	//				//second or later operation
	//			}else{
	//				summarizedOp.addIncome(income);
	//				summarizedOp.addExpanse(expanse);
	//			}
	//
	//			tmpDate=date;
	//		}
	//
	//		//add the last summary
	//		listSumOps.add(summarizedOp);
	//
	//		//set summarized ops
	//		setSummarizedEconomicOperations(listSumOps);
	//
	//	}


	/**
	 * Compute economic indicators from summary tables
	 * 
	 * @param n : simulation length (number of years)
	 * @param r : discount rate
	 * @param f : land value
	 * @param list of summarized operations
	 * @author GL - 01/02/2012
	 */
	private void CalcEconomicIndicators(){

		int n = nYears; //number of years

		//start computing either the land or the discount rate
		if (discountRate == null && land > 0){
			discountRate = calcDiscountRateFromLand(land,n,standardizedEconomicOperations); //iterative process that can fail to converge
			landIsEstimated = false;
		}else if (land == null && discountRate > 0){
			double npv = calcNetPresentValue (discountRate, standardizedEconomicOperations, firstDate);
			land =npv * Math.pow(1+discountRate,n) / (Math.pow(1+discountRate,n) - 1); //BASI
			landIsEstimated = true;
		}else{
			Log.println (Log.WARNING,"EconomicScenario.CalcEconomicIndicator()", "land or discount rate not correctly specified...");
		}

		double vt = calcHarvestedVolume(standardizedEconomicOperations);
		double vm = vt/n;
		double bt = calcNetPresentValue(0d, standardizedEconomicOperations, firstDate);
		double bm = bt/n;
		double rt = calcRt(standardizedEconomicOperations);
		double rm = rt/n;
		double bas1 = calcNetPresentValueTransitoryPeriod (discountRate, standardizedEconomicOperations, firstDate, firstDateInfiniteCycle, lastDate);
		double bas2 = calcNetPresentValueInfiniteCycle (discountRate, standardizedEconomicOperations, firstDate, firstDateInfiniteCycle, lastDate);
		double bas = calcNetPresentValue (discountRate, standardizedEconomicOperations, firstDate);
//		double basi = bas1 + (bas2 * Math.pow(1+discountRate,n)/(Math.pow(1+discountRate,n)-1))/Math.pow(1+discountRate,firstDateInfiniteCycle-firstDate);
		double basi = calcBASI(discountRate, standardizedEconomicOperations, firstDate, firstDateInfiniteCycle, lastDate);
		double basf = -land + bas + land / Math.pow(1+discountRate,n) ;
		double tir = calcTIR (standardizedEconomicOperations, this.firstDate);
		double tirf = calcTIRF (standardizedEconomicOperations, n, this.firstDate);
		double fa = calcForwardAnnuity(standardizedEconomicOperations);
		double ba = calcBackwardAnnuity(standardizedEconomicOperations);

		setBt(bt);
		setBm(bm);
		setRt(rt);
		setRm(rm);
		setVt (vt);
		setVm (vm);
		setBas(bas); //net present value
		setBasi(basi);//critère de Faustmann, land expectation value
		setBasf(basf);
		setTir (tir);
		setTirf(tirf);
		this.forwardAnnuity = fa;
		this.backwardAnuity = ba;

		printEconomicResults();
		printStandardizedOperations(); //this must always run after evaluate!		
	}
	
	public double calcBASI(double r, List<EconomicStandardizedOperation> ops, int firstDate, int intermediateDate, int lastDate){
		int n = lastDate - firstDate;
		double bas1 = calcNetPresentValueTransitoryPeriod (r, ops,firstDate,intermediateDate,lastDate);
		double bas2 = calcNetPresentValueInfiniteCycle (r, ops,firstDate,intermediateDate,lastDate);
		double basi = bas1 + (bas2 * Math.pow(1+r,n)/(Math.pow(1+r,n)-1))/Math.pow(1+r,intermediateDate-firstDate);
		return basi;
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
		System.out.println ("land = " + land + " EUR");
		System.out.println ("Discount rate = " + discountRate * 100 + " %");
		System.out.println ("Number of years = " + nYears);
		System.out.println ("--------------");

		System.out.println("Bt = " + Math.round(getBt()*100d)/100d
				+ "; Bm = " + Math.round(getBm()*100d)/100d
				+ "; Rt = " + Math.round(getRt()*100d)/100d
				+ "; Rm = " + Math.round(getRm()*100d)/100d
				+ "; BAS = " + Math.round(getBas()*100d)/100d
				+" ; BASF = "+ Math.round(getBasf()*100d)/100d
				+" ; BASI = "+ Math.round(getBasi()*100d)/100d
				+" ; TIR = "+ Math.round(getTir ()*10000d)/100d + "%"
				+" ; TIRF = "+ Math.round(getTirf ()*10000d)/100d + "%"
				+" ; VT = " + Math.round(getVt()*100d)/100d 
				+" ; VM = " + Math.round(getVm()*100d)/100d
				+" ; annuity = " + Math.round(getForwardAnnuity()*100d)/100d
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
				+ getBasf() + sep
				+ getBasi() + sep
				+ getTir () + sep
				+ getTirf () + sep
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
		sb.append("land" + sep + this.land);
		sb.append(System.getProperty("line.separator"));
		sb.append("discount rate" + sep + this.discountRate);
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
		sb.append("BASF (net present value with land)" + sep + this.basf);
		sb.append(System.getProperty("line.separator"));
		sb.append ("BASI (land expectation value)" + sep + this.basi);
		sb.append(System.getProperty("line.separator"));
		sb.append("TIR (internal rate of return)" + sep + this.tir);
		sb.append(System.getProperty("line.separator"));
		sb.append("TIRF (internal rate of return with land)" + sep + this.tirf);
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
	 * @param firstDate = first date for economic computation
	 * @author GL 
	 */ 
	public double calcNetPresentValue(double r, List<EconomicStandardizedOperation> ops, int firstDate){
		double bas = 0;
		for (EconomicStandardizedOperation op : ops){
			int date = op.getDate();
			//gl--- the previous formula was not correct whenever the initial date was not zero
			int nbYears = date - firstDate;
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

			if(firstDate != intermediateDate & date >= firstDate & date < intermediateDate){
				bas += (income - expanse) / Math.pow(1 + r,nbYears); 
			}
			//			System.out.println("calcNetPresentValue - income " + income + " - rate " + r + " - VA " + ( (income - expanse) / Math.pow(1 + r,nbYears)) + " - BAS " + bas);
		}
		return bas;
	}

	/**
	 * Compute the net present value (BAS) associated to the scenario
	 * Economics fluxes are actualized at first date of the inifinite cycle
	 * 
	 * gl removed static modifier 
	 */ 
	private double calcNetPresentValueInfiniteCycle(double r, List<EconomicStandardizedOperation> ops, int firstDate, int intermediateDate, int lastDate){
		double bas = 0;
		for (EconomicStandardizedOperation op : ops){
			int date = op.getDate();
			//gl--- the previous formula was not correct whenever the initial date was not zero
			int nbYears = date - intermediateDate;
			double income = op.getIncome();
			double expanse = op.getExpanse();

			if(date >= intermediateDate & date <= lastDate){
				bas += (income - expanse) / Math.pow(1 + r,nbYears); 
			}
			//			System.out.println("calcNetPresentValue - income " + income + " - rate " + r + " - VA " + ( (income - expanse) / Math.pow(1 + r,nbYears)) + " - BAS " + bas);
		}
		return bas;
	}

	private static double calcHarvestedVolume(List<EconomicStandardizedOperation> stdops){
		double v = 0;
		for(EconomicStandardizedOperation op : stdops){v+=op.getHarvestedVolume ();}
		return v;
	}

	private double calcIRR(double land, int n, List<EconomicStandardizedOperation> ops, String method, int firstDate) {
		int nIter = 0;
		double maxIter = 1000;
		double precision = 0.00001;	
		double lowGuessRate = 0.0001;
		double highGuessRate = 0.15;	
		double guessRate = (lowGuessRate+highGuessRate)/2;

		double x =  calcNetPresentValue(guessRate, ops, firstDate);
		if (method.equals("calcDiscountRateFromLand()")) x = -land + x * Math.pow (1+guessRate, n) / (Math.pow (1+guessRate, n) - 1);
		else if (method.equals("calcTIRF()")) x = -land + x + land / Math.pow(1+guessRate,n);

		while (Math.abs(x) > precision)	{

			if (x < 0) {highGuessRate = guessRate;} else { lowGuessRate = guessRate;}

			if ((highGuessRate-lowGuessRate) < precision){
				System.out.println ("EconomicScenario." + method + " - Convergence stopped with low limit to " + lowGuessRate + " and high limit to " + highGuessRate);
				return guessRate;
			}

			guessRate = (lowGuessRate+highGuessRate)/2;
			x = calcNetPresentValue(guessRate, ops, firstDate);
			if (method.equals("calcDiscountRateFromLand()")) x = -land + x * Math.pow (1+guessRate, n) / (Math.pow (1+guessRate, n) - 1);
			else if (method.equals("calcTIRF()")) x = -land + x + land / Math.pow(1+guessRate,n);

			//			System.out.println ("EconomicScenario." + method + " - No iter = " + nIter + " - net present value = " + x + " - guess internal rate of return = " + guessRate );

			if (nIter >= maxIter){
				Log.println (Log.WARNING,"EconomicScenario." + method,"False convergence in TIR computation. I returned -1, the guessed last value was " + x);
				System.out.println ("EconomicScenario." + method + " - False convergence in TIR computation. I returned -1, the guessed last value was " + x);
				return -1d;
			}
			nIter ++;
		}

		System.out.println ("EconomicScenario." + method + " - Normal convergence achieved of internal rate of return  " + guessRate + " with No iter = " + nIter);
		return guessRate;		
	}

	private double calcIRRBruteForceAlgo(double land, int n, List<EconomicStandardizedOperation> ops, String method, int firstDate) {
		double precision = 0.0001;	
		double lowGuessRate = 0.001;
		double highGuessRate = 1.000; //100 000 iterations	
		//		double npvMin = 100;
		double npv = 0;
		double estimatedPrecision = 9999;
		double precisionMin = 9999;
		//		double precisionTolerance = 500; //50 euro/dollar/... //the longer the scenario the greater the NPV?
		double tir = - 999;

		for(double r = lowGuessRate; r < highGuessRate + precision; r = r + precision){
			npv=calcNetPresentValue(r, ops, firstDate);	
			double x = -999;
			if (method.equals("calcDiscountRateFromLand()")) x = -land + npv * Math.pow (1+r, n) / (Math.pow (1+r, n) - 1);
			else if (method.equals("calcTIRF()")) x = -land + npv + land / Math.pow(1+r,n);
			else x = npv;

			//			System.out.println("CalcIRRBrutalIteration() - estimated TIR = " + r + " for BAS or BASF = " + x);
			estimatedPrecision = Math.abs(0-x);
			if(estimatedPrecision<precisionMin){
				tir=r;
				precisionMin = estimatedPrecision;
			}
		}

		System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - estimated TIR = " + tir + " with precision = " + precisionMin);

		if(tir == lowGuessRate | tir == highGuessRate){
			System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - out of bound estimated TIR : tir = " + tir);
			return -0.999d;
		}

		//		if(precisionMin > precisionTolerance){
		//			System.out.println("EconomicScenario.calcIRRBruteForceAlgo() - estimated tir precision greater than tolerated : precision = " + precisionMin + " tir = " + tir);
		//			return -0.999d;
		//		}

		return tir; 

	}

	private double calcDiscountRateFromLand(double land, int n, List<EconomicStandardizedOperation> ops){
		return calcIRRBruteForceAlgo(land, n, ops, "calcDiscountRateFromLand()", this.firstDate);	
		//		return calcIRR(land, n, summary, "calcDiscountRateFromLand()");	

	}

	/**
	 * Compute the internal rate of return with land (TIRF) with an iterative process
	 * @author GL 29/07/2013
	 */
	private double calcTIRF(List<EconomicStandardizedOperation> ops, int n, int firstDate){
		return calcIRRBruteForceAlgo(land, n, ops, "calcTIRF()", firstDate);
		//		return calcIRR(land, n, summary, "calcTIRF()");
	}

	/**
	 * Compute the internal rate of return (TIR) with an iterative process
	 * @author GL 29/07/2013
	 */
	private double calcTIR(List<EconomicStandardizedOperation> ops, int firstDate){
		return calcIRRBruteForceAlgo(0, 0, ops, "calcTIR()", firstDate);
		//		return calcIRR(0, 0, summary, "calcTIR()");
	}

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
	private double calcForwardAnnuity(List<EconomicStandardizedOperation> ops){
		double a=0;
		double SVtb = 0;
		double SVte = 0;
		if(standDescriptionAtFirstDate != null){
			SVtb = this.standDescriptionAtFirstDate.getMarketValue();
		}else{
			System.out.println("EconomicScenario.calcForwardAnnuity() - Makert value was not computed for initial scene and then set to 0");
		}
		if(standDescriptionAtLastDate != null){
			SVte = this.standDescriptionAtLastDate.getMarketValue();
		}else{
			System.out.println("EconomicScenario.calcForwardAnnuity() - Makert value was not computed for last scene and then set to 0");
		}

		double sum = 0;
		for (EconomicStandardizedOperation op : ops){
			double income = op.getIncome();
			double expanse = op.getExpanse();
			sum += (income - expanse) / Math.pow(1+this.discountRate,op.getDate()-this.firstDate); 
		}
		a = (SVte / Math.pow(1+discountRate,nYears) - SVtb + sum)*(discountRate*Math.pow(1+discountRate,nYears))/(Math.pow(1+discountRate, nYears)-1);
		return a;
	}

	/**
	 * Compute backward annuity according to Hanewinkel 2014
	 * 
	 */
	private double calcBackwardAnnuity(List<EconomicStandardizedOperation> ops){
		double a=0;
		double SVtb = 0;
		double SVte = 0;
		if(standDescriptionAtFirstDate != null){
			SVtb = this.standDescriptionAtFirstDate.getMarketValue();
		}else{
			System.out.println("EconomicScenario.calcBackwardAnnuity() - Makert value was not computed for initial scene and then set to 0");
		}
		if(standDescriptionAtLastDate != null){
			SVte = this.standDescriptionAtLastDate.getMarketValue();
		}else{
			System.out.println("EconomicScenario.calcBackwardAnnuity() - Makert value was not computed for last scene and then set to 0");
		}

		double sum = 0;
		for (EconomicStandardizedOperation op : ops){
			double income = op.getIncome();
			double expanse = op.getExpanse();
			sum += (income - expanse) / Math.pow(1+this.discountRate,op.getDate()-this.firstDate); 
		}
		a = (SVte / Math.pow(1+discountRate,nYears) - SVtb + sum)*(discountRate*Math.pow(1+discountRate,nYears))/(Math.pow(1+discountRate, nYears)-1);
		return a;
	}
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


	// --- Accessors --- GL 01/02/12
	public double getBt () {return bt;}
	public void setBt (double b) {this.bt = b;}
	public double getBm() {return bm;}
	public void setBm(double bm) {this.bm = bm;}
	public double getRt() {return rt;}
	public void setRt(double rt) {this.rt = rt;}
	public double getRm() {return rm;	}
	public void setRm(double rm) {this.rm = rm;}
	public double getTir() {return tir;}
	public void setTir(double tir) {this.tir = tir;}
	public double getTirf() {return tirf;}
	public void setTirf(double tirf) {this.tirf = tirf;}
	public double getBasi() {return basi;}
	public void setBasi(double basi) {this.basi = basi;}
	public double getBasf() {return basf;}
	public void setBasf(double basf) {this.basf = basf;}
	public double getBas() {return bas;}
	public void setBas(double bas) {this.bas = bas;}
	public double getVm() {return vm;}
	public void setVm(double vm) {this.vm = vm;}
	public double getVt() {return vt;}
	public void setVt(double vt) {this.vt = vt;}
	public double getForwardAnnuity() {return forwardAnnuity;}
	public void setForwardAnnuity(double forwardAnnuity) {this.forwardAnnuity = forwardAnnuity;}
	public double getBackwardAnuity() {return backwardAnuity;}
	public void setBackwardAnuity(double backwardAnuity) {this.backwardAnuity = backwardAnuity;}
	public List<EconomicStandardizedOperation> getStandardizedEconomicOperations() {return standardizedEconomicOperations;}
	//	public List<SummarizedEconomicOperation> getSummarizedEconomicOperations() {return summarizedEconomicOperations;}
	public void setStandardizedEconomicOperations(List<EconomicStandardizedOperation> comparableOperation) {
		this.standardizedEconomicOperations = comparableOperation;
	}
	//	public void setSummarizedEconomicOperations(List<SummarizedEconomicOperation> summarizeOperations) {
	//		this.summarizedEconomicOperations = summarizeOperations;
	//	}
	public double getDiscountRate() {return discountRate;}
	//	public void setDiscountRate(double r) {this.discountRate = r;}
	public double getLand() {return land;}
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
	public boolean isLandIsEstimated() {return landIsEstimated;}
	public boolean isDateAfterIntervention() {return dateAfterIntervention;}

}
