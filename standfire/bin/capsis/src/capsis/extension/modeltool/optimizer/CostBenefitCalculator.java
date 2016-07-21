/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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
package capsis.extension.modeltool.optimizer;

import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import repicea.app.SettingMemory;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.Resettable;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.javacsv.CSVReader;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.stats.LinearStatisticalExpression;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.modeltool.optimizer.CostRecord.CostType;
import capsis.extension.modeltool.optimizer.CostRecord.CostUnit;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.VMerchantProviderWithName;


class CostBenefitCalculator implements IOUserInterfaceableObject, REpiceaShowableUIWithParent, Memorizable, Resettable {

	protected static class CostBenefitCalculatorFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".cbc";
		
		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) { 
				return true; 
			} else {
				return file.getPath().toLowerCase().endsWith(getExtension());
			}
		}

		@Override
		public String getDescription() {
			return CostBenefitCalculatorDialog.MessageID.CostBenefitCalculatorFileFilter.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

	protected static enum PriceFunction implements TextableEnum {
		Heshmatol("Heshmatol price function", "Fonction de prix de Heshmatol"), 
		Chavet("Chavet price function", "Fonction de prix de Chavet"), 
		CustomizedDiameter("Diameter-based costumized function", "Fonction bas\u00E9e sur le diam\u00E8tre"),
		CustomizedVolume("Volume-based costumized function", "Fonction bas\u00E9e sur le volume"),
		;

		PriceFunction(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	
	
	private final static CostBenefitCalculatorFileFilter MyFileFilter = new CostBenefitCalculatorFileFilter();

	private double discount_rate;

	private final double hehsmatol_pbase = 72.1;
	private final double hehsmatol_k = 0.0397;
	private final double heshmatol_Dg0 = 26.4;

	private final double chavet_dseuil = 32;
	private final double chavet_pref = 14;
	private final double chavet_p0 = 41.58;
	private final double chavet_a = 1.71;

	protected PriceFunction selectedPriceFunction;
	protected final ArrayList<CostRecord> costs;
	protected double annualFixedCosts;
	
	private String filename;
	
	private transient CostBenefitCalculatorDialog guiInterface;
	protected transient final SettingMemory settings;
	
	protected LinearStatisticalExpression diameterBasedFunction;
	protected LinearStatisticalExpression volumeBasedFunction;
	
	/**
	 * Constructor.
	 */
	protected CostBenefitCalculator(OptimizerTool tool) {
//		optimizer = tool;
		costs = new ArrayList<CostRecord>();
		diameterBasedFunction = new LinearStatisticalExpression();
		diameterBasedFunction.setVariableValue(0, 1d);		// intercept
		volumeBasedFunction = new LinearStatisticalExpression();
		volumeBasedFunction.setVariableValue(0, 1d);		// intercept
		
		settings = tool.getSettingMemory();
		filename = settings.getProperty(getClass().getSimpleName() + ".lastSavedFile", System.getProperty("user.home") + "");
		File file = new File(filename);
		boolean correctlyLoaded = false;
		if (file.exists() && file.isFile() && getFileFilter().accept(file)) {
			try {
				load(filename);
				correctlyLoaded = true;
			} catch (Exception e) {}
		} 
		
		if (!correctlyLoaded) {
			reset();
		} 
	}
	
	protected void setDiscountRate(double discount_rate) {this.discount_rate = discount_rate;}
	
	protected double getDiscountRate() {return discount_rate;}
	
	/** 
	 * This method returns the Heshmatol-Barkaoui-Peyron price function for evaluating the income of the harvested trees.
	 * @param dg the mean quadratic diameter
	 * @return the price in euro per cubic meters?
	 */
	private double getHeshmatolPrice(double dg){
		double price = hehsmatol_pbase;
		price *= 1. - Math.exp(-hehsmatol_k * (dg - heshmatol_Dg0)) ;
		return price;
	}

	/** 
	 * This method returns the Chavet & Chavet price function for evaluating the income of the harvested trees.
	 * @param dg the mean quadratic diameter  
	 * @param the price in euro per cubic meters?
	 */
	private double getChavetPrice(double dg){
		double price = chavet_pref;
		if (dg > chavet_dseuil) {
			price = chavet_a * dg - chavet_p0;
		}
		return price;
	}

	/**
	 * This method returns the actualized value.
	 * @param originalValue the value to be actualized
	 * @param referenceDate the date to which it has to be actualized
	 * @param originalDate the date at which the original value was observed
	 * @return the actualized value
	 */
	private double getActualizeValue(double originalValue, double referenceDate, double originalDate) {
		double actualizedValue = originalValue * Math.pow(1 + discount_rate, referenceDate - originalDate); 
		return actualizedValue;
	}

	private double getActualizedValuePerHaForThisStep(Step step, double referenceDate) {
		double[] output = calculateCostAndIncomesForThisStep(step);
		int ageForThisStep = step.getScene().getDate();
		double actualizedValue = getActualizeValue (output[0] - output[1], referenceDate, ageForThisStep);	// output[0] : incomes, output[1] : costs
		return actualizedValue;
	}
	
	private double getActualizedMaintenanceCosts(Step rootStep, double referenceDate) {
		GScene stand = rootStep.getScene();
		double actualizedCost = 0;
		double originalCost;
		
		for (CostRecord cr : costs) {
			if (cr.getType() == CostType.maintenance && cr.isEnabled()) {
				double originalDate = cr.getAge();
				originalCost = cr.getCost() * ((AreaHaProvider) stand).getAreaHa ();
				actualizedCost += this.getActualizeValue (originalCost, referenceDate, originalDate);
			}
		}
		return actualizedCost;
	}
	
	protected double getNetPresentValue(Vector<Step> steps) {
		double referenceDate = steps.firstElement().getScene().getDate();
		double presentValue = 0;
		Step currentStep;
		for (int i = steps.size() - 1; i >= 0; i--) {
			currentStep = steps.get(i);
			presentValue += getActualizedValuePerHaForThisStep(currentStep, referenceDate);
		} 
		presentValue -= getActualizedMaintenanceCosts(steps.firstElement(), referenceDate);		// actualized maintenance costs are subtracted from present value
		return presentValue;
	}
	
	
	/**
	 * This method returns the non actualized incomes for a particular step on a hectare basis.
	 * @param step a Step instance
	 * @return an array of double with two slots, the first is the incomes while the second is the cost per hectare
	 */
	private double[] calculateCostAndIncomesForThisStep(Step step) {
		double[] output = new double[2];
		MethodProvider mp = step.getProject().getModel().getMethodProvider();
		
		double totalVolumeHa = 0;
		double totalNumberOfStemsHa = 0;
		GScene stand = step.getScene();
		double standAreaHa = ((AreaHaProvider) stand).getAreaHa();
		double areaFactor = 1d / standAreaHa;
		
		if (stand.isInterventionResult()) {
			List<Tree> harvestedTrees = new ArrayList<Tree>();
			Collection<Tree> trees = ((TreeList) stand).getTrees(StatusClass.cut.name());
			for (Tree tree : trees) {
				harvestedTrees.clear();
				harvestedTrees.add(tree);
				double volumeForThisTree = ((VMerchantProviderWithName) mp).getVMerchant(stand, harvestedTrees);
				double nbStemsForThisTree = ((NProvider) mp).getN (stand, harvestedTrees);
				totalVolumeHa += volumeForThisTree * areaFactor;
				totalNumberOfStemsHa += nbStemsForThisTree * areaFactor;
				output[0] += getPrice(tree.getDbh(), volumeForThisTree) * volumeForThisTree * areaFactor;
			}
			for (CostRecord cr : costs) {
				if (cr.getType() == CostType.harvest && cr.isEnabled()) {
					switch(cr.getUnit()) {
					case ha:
						output[1] += cr.getCost() * standAreaHa;
						break;
					case m3:
						output[1] += cr.getCost() * totalVolumeHa;
						break;
					case stem:
						output[1] += cr.getCost() * totalNumberOfStemsHa;
						break;
					}
				}
			}
		}
		return output;
	}
		
	
	private double getPrice(double treeDbh, double treeVolume) {
		switch(selectedPriceFunction) {
		case Heshmatol:
			return getHeshmatolPrice(treeDbh);
		case Chavet:
			return getChavetPrice(treeDbh);
		case CustomizedDiameter:
			diameterBasedFunction.setVariableValue(1, treeDbh);
			diameterBasedFunction.setVariableValue(2, treeDbh * treeDbh);
			return diameterBasedFunction.getValue();
		case CustomizedVolume:
			volumeBasedFunction.setVariableValue(1, treeVolume);
			volumeBasedFunction.setVariableValue(2, treeVolume * treeVolume);
			return volumeBasedFunction.getValue();
		default:
			return -999999;
		}
	}

	private void readDefaultHarvestMaintenanceCosts() {
		costs.clear();
		String path = ObjectUtility.getPackagePath(getClass());
		String filename = path + "EconomicParameters.csv";
		try {
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while((record = reader.nextRecord ()) != null) {
				CostRecord er = new CostRecord(record);
				if (er.getType() == CostType.maintenance && er.getUnit() != CostUnit.ha) {
					throw new InvalidParameterException("Maintenance costs are to be expressed by ha!");
				}
				costs.add(er);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CostBenefitCalculatorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new CostBenefitCalculatorDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public void showUI(Window parent) {getUI(parent).setVisible(true);}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(discount_rate);
		mp.add(costs);
		mp.add(annualFixedCosts);
		mp.add(selectedPriceFunction);
		mp.add(diameterBasedFunction);
		mp.add(volumeBasedFunction);
		return mp;
	}

	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		discount_rate = (Double) wasMemorized.get(0);
		costs.clear();
		costs.addAll((ArrayList<CostRecord>) wasMemorized.get(1));
		annualFixedCosts = (Double) wasMemorized.get(2);
		selectedPriceFunction = (PriceFunction) wasMemorized.get(3);
		diameterBasedFunction = (LinearStatisticalExpression) wasMemorized.get(4);
		volumeBasedFunction = (LinearStatisticalExpression) wasMemorized.get(5);
	}

	@Override
	public void save(String filename) throws IOException {
		this.filename = filename;
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		Object obj = deserializer.readObject();
		if (obj instanceof CostBenefitCalculator) {
			CostBenefitCalculator calc = (CostBenefitCalculator) obj;
			calc.filename = filename;
			unpackMemorizerPackage(calc.getMemorizerPackage());
		} else {
			throw new IOException("Deserialized object is not a CostBenefitCalculator instance!");
		}
		this.filename = filename; 
	}

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}

	@Override
	public String getFilename() {return filename;}

	protected Double getAnnualFixedCosts() {return annualFixedCosts;}

	protected void setAnnualFixedCosts(double annualFixedCosts) {this.annualFixedCosts = annualFixedCosts;}

	@Override
	public void reset() {
		filename = "";
		selectedPriceFunction = PriceFunction.Heshmatol;
		discount_rate = 0.03;
		annualFixedCosts = 0d;
		readDefaultHarvestMaintenanceCosts();
		diameterBasedFunction.setParameterValue(0, 0d);
		diameterBasedFunction.setParameterValue(1, 0d);
		diameterBasedFunction.setParameterValue(2, 0d);
		volumeBasedFunction.setParameterValue(0, 0d);
		volumeBasedFunction.setParameterValue(1, 0d);
		volumeBasedFunction.setParameterValue(2, 0d);
	}

	protected void saveProperties() {
		settings.setProperty(getClass().getSimpleName() + ".lastSavedFile", filename);
	}
	
	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}


//	/*
//	 * For debugging
//	 */
//	@SuppressWarnings ("unused")
//	public static void main(String[] args) {
//		CostBenefitCalculator cc = new CostBenefitCalculator();
//		for (int dg = 20; dg < 70; dg++) {
//			double hValue = cc.getHeshmatolPrice(dg);
//			double cValue = cc.getChavetPrice (dg);
//			System.out.println("Prices for dg = " + dg + " cm, H = " + hValue + "; C = " + cValue);
//		}
//		double actVal = cc.getActualizeValue(2000, 25, 35);
//		int u = 0;
//	}
	
	
	
}
