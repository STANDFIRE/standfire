/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
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

package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import jeeb.lib.defaulttype.Extension;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.extension.AbstractStandViewer;
import capsis.extension.PaleoWorkingProcess;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.economics.AnnualCost;
import capsis.lib.economics.BillBookCompatible;
import capsis.lib.economics.BillBookIncome;
import capsis.lib.economics.BillBookLine;
import capsis.lib.economics.BillBookSpecies;
import capsis.lib.economics.CropExpense;
import capsis.lib.economics.CurrencyRenderer;
import capsis.lib.economics.FinancialTools;
import capsis.lib.economics.Producer;
import capsis.lib.economics.Product;
import capsis.lib.economics.VariableCost;

/**	SVFinancialReport builds a billbook of the projects with BillBookCompatible stands
*
*	@author F. de Coligny, O. Pain - november 2007
*/
public class SVFinancialReport extends AbstractStandViewer implements ActionListener {
	
	static public String AUTHOR = "F. de Coligny, O. Pain";
	static public String VERSION = "1.0";
	

	private static class ProjectReportLine {
		public double quantity;	// rounded
		public double gazoleQuantity;
		public double cropExpense;
		public double wpExpense;
		public double income;
		// candidate second income including annual and variable costs
		// candidate second income including annual and variable costs
		// candidate second income including annual and variable costs
		public double IRR;
		public double NPV;
		public double NPVI;
		public double constantAnnuity;
	}

	private static class ProductReportLine {
		public int rotationOrder;
		public String productName;
		public double quantity;	// rounded
		public double gazoleQuantity;
		public double wpExpense;
		public double income;
		public double balance;
		public ProductReportLine () {}
		public ProductReportLine (ProductReportLine model) {
			this.rotationOrder = model.rotationOrder;
			this.productName = model.productName;
			this.quantity = model.quantity;
			this.gazoleQuantity = model.gazoleQuantity;
			this.wpExpense = model.wpExpense;
			this.income = model.income;
			this.balance = model.balance;
		}
	}

	private static class WPReportLine {
		public String wpName;
		public double quantity;
		public double gazoleQuantity;
		public double wpExpense;
	}

	private static class ProjectReportTableModel extends AbstractTableModel {
		private ArrayList data;
		private String[] columnNames = {
				Translator.swap ("SVFinancialReport.Quantity"),
				Translator.swap ("SVFinancialReport.gazoleQuantity"),
				Translator.swap ("SVFinancialReport.cropExpense"),
				Translator.swap ("SVFinancialReport.wpExpense"),
				Translator.swap ("SVFinancialReport.income"),
				Translator.swap ("SVFinancialReport.IRR"),
				Translator.swap ("SVFinancialReport.NPV"),
				Translator.swap ("SVFinancialReport.NPVI"),
				Translator.swap ("SVFinancialReport.constantAnnuity")};
		public ProjectReportTableModel (Collection<ProjectReportLine> lines) {
			data = new ArrayList (lines);
		}
		public Class getColumnClass (int col) {
			if (col != 0 && col != 1 && col != 5) {return Currency.class;}
			return data == null ? null : getValueAt (0, col).getClass ();
		}
		public int getColumnCount () {return columnNames.length;}
		public String getColumnName (int col) {
			return columnNames[col];
		}
		public int getRowCount () {return data == null ? 0 : data.size ();}
		public Object getValueAt (int row, int col) {
			ProjectReportLine line = (ProjectReportLine) data.get (row);
			switch (col) {
				case 0: return line.quantity;
				case 1: return line.gazoleQuantity;
				case 2: return line.cropExpense;
				case 3: return line.wpExpense;
				case 4: return line.income;
				case 5: return line.IRR;
				case 6: return line.NPV;
				case 7: return line.NPVI;
				case 8: return line.constantAnnuity;
			}
			return "";
		}
	}

	private static class ProductReportTableModel extends AbstractTableModel {
		private ArrayList data;
		private String[] columnNames = {
				Translator.swap ("SVFinancialReport.productName"),
				Translator.swap ("SVFinancialReport.quantity"),
				Translator.swap ("SVFinancialReport.gazoleQuantity"),
				Translator.swap ("SVFinancialReport.wpExpense"),
				Translator.swap ("SVFinancialReport.income"),
				Translator.swap ("SVFinancialReport.balance")};
		public ProductReportTableModel (Collection<ProductReportLine> lines) {
			data = new ArrayList (lines);
		}
		public Class getColumnClass (int col) {
			if (col >= 3) {return Currency.class;}
			return data == null ? null : getValueAt (0, col).getClass ();
		}
		public int getColumnCount () {return columnNames.length;}
		public String getColumnName (int col) {
			return columnNames[col];
		}
		public int getRowCount () {return data == null ? 0 : data.size ();}
		public Object getValueAt (int row, int col) {
			ProductReportLine line = (ProductReportLine) data.get (row);
			switch (col) {
				case 0: return line.productName;
				case 1: return line.quantity;
				case 2: return line.gazoleQuantity;
				case 3: return line.wpExpense;
				case 4: return line.income;
				case 5: return line.balance;
			}
			return "";
		}
	}

	private static class ProductReportTableModel3 extends AbstractTableModel {
		private ArrayList data;
		private String[] columnNames = {
				Translator.swap ("SVFinancialReport.rotationOrder"),
				Translator.swap ("SVFinancialReport.productName"),
				Translator.swap ("SVFinancialReport.quantity"),
				Translator.swap ("SVFinancialReport.gazoleQuantity"),
				Translator.swap ("SVFinancialReport.wpExpense"),
				Translator.swap ("SVFinancialReport.income"),
				Translator.swap ("SVFinancialReport.balance")};
		public ProductReportTableModel3 (Collection<ProductReportLine> lines) {
			data = new ArrayList (lines);
		}
		public Class getColumnClass (int col) {
			if (col >= 4) {return Currency.class;}
			return data == null ? null : getValueAt (0, col).getClass ();
		}
		public int getColumnCount () {return columnNames.length;}
		public String getColumnName (int col) {
			return columnNames[col];
		}
		public int getRowCount () {return data == null ? 0 : data.size ();}
		public Object getValueAt (int row, int col) {
			ProductReportLine line = (ProductReportLine) data.get (row);
			switch (col) {
				case 0: return line.rotationOrder;
				case 1: return line.productName;
				case 2: return line.quantity;
				case 3: return line.gazoleQuantity;
				case 4: return line.wpExpense;
				case 5: return line.income;
				case 6: return line.balance;
			}
			return "";
		}
	}

	private static class WPReportTableModel extends AbstractTableModel {
		private ArrayList data;
		private String[] columnNames = {
				Translator.swap ("SVFinancialReport.wpName"),
				Translator.swap ("SVFinancialReport.quantity"),
				Translator.swap ("SVFinancialReport.gazoleQuantity"),
				Translator.swap ("SVFinancialReport.wpExpense")};
		public WPReportTableModel (Collection<WPReportLine> lines) {
			data = new ArrayList (lines);
		}
		public Class getColumnClass (int col) {
			if (col >= 3) {return Currency.class;}
			return data == null ? null : getValueAt (0, col).getClass ();
		}
		public int getColumnCount () {return columnNames.length;}
		public String getColumnName (int col) {
			return columnNames[col];
		}
		public int getRowCount () {return data == null ? 0 : data.size ();}
		public Object getValueAt (int row, int col) {
			WPReportLine line = (WPReportLine) data.get (row);
			switch (col) {
				case 0: return line.wpName;
				case 1: return line.quantity;
				case 2: return line.gazoleQuantity;
				case 3: return line.wpExpense;
			}
			return "";
		}
	}

	static {
		Translator.addBundle("capsis.extension.standviewer.SVFinancialReport");
	}
	private JScrollPane scroll0;
	private JScrollPane scroll1;
	private JScrollPane scroll2;
	private JScrollPane scroll3;
	private JScrollPane scroll4;
	private JTextField discountRate;	// "Taux d'actualisation"
	private JCheckBox applyDiscountRateToIncomeAndExpense;
	private JComboBox quantityUnit;		// unit of different report tables
	private JCheckBox includeAnnualAndVariableCosts;	// fc + op - 7.5.2008


	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
		try {
			createUI ();
			update ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVFinancialReport.c ()", "Error in constructor", e);
			throw e;	// propagate
		}
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			Step rootStep = (Step) m.getProject ().getRoot ();
			GScene rootStand = rootStep.getScene ();
			if (!(rootStand instanceof BillBookCompatible)) {return false;}
			return true;
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVFinancialReport.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}


	/**	Update the viewer on a given step button
	*/
	public void update (StepButton sb) {
		super.update (sb);		// computes boolean sameStep
		update ();	// for example
	}

	/**	Update the viewer with the current step button
	*/
	public void update () {
		super.update ();
		if (sameStep) {return;}

		// Retrieve Steps from root to this step
		Collection<CropExpense> cropExpenses = new ArrayList<CropExpense> ();
		Collection<Product> products = new ArrayList<Product> ();	// Product costs
		Collection<BillBookIncome> incomes = new ArrayList<BillBookIncome> ();
		String report = null;	// if not null at the end, show report to the user
		Map<Integer,String> macroWPNames = new HashMap<Integer,String> ();
		
		Vector steps = step.getProject ().getStepsFromRoot (step);
		Collection<BillBookLine> productsAndIncomes = new ArrayList<BillBookLine> ();
		for (Iterator i = steps.iterator (); i.hasNext ();) {
			Step stp = (Step) i.next ();

			BillBookCompatible stand = (BillBookCompatible) stp.getScene ();
			
			// 0. rotation -> name of the macroWP
			int rotationOrder = ((GScene) stand).getDate ();
			String macroWPName = stand.getMacroWPName ();
			macroWPNames.put (rotationOrder, macroWPName);
			
			// 1. crop expenses
			cropExpenses.addAll (stand.getCropExpenses ());

			// 2. primary products of the stand
			//~ Collection<Product> products = stand.getProducts ();	// ex: STAND_TREE
			//~ lines.addAll (products);

			// 3. products of the working processes + incomes
			Collection<BillBookLine> stepProductsAndIncomes = stand.getWPOutputProducts ();
			productsAndIncomes.addAll (stepProductsAndIncomes);
			for (BillBookLine l : stepProductsAndIncomes) {
				if (l instanceof Product) {
					products.add ((Product) l);
				} else if (l instanceof BillBookIncome) {
					incomes.add ((BillBookIncome) l);
				} else {
					Log.println (Log.ERROR, "SVFinancialReport.update ()",
							"stepProductsAndIncomes contains a wrong line, should be Product or BillBookIncome:"
							+l.getClass ().getName ()+" "+l);
					report = Translator.swap ("SVFinancialReport.wrongLineInstandGetWPOutputProductsSeeLog");
				}
			}
		}

		// Show report if not null
		if (report != null) {MessageDialog.print (this, report);}

		double discountRateValue = Check.doubleValue (discountRate.getText ().trim ())/100;

		BillBookCompatible stand = (BillBookCompatible) step.getScene ();
		BillBookSpecies species = stand.getSpecies ();
		double area_ha = stand.getArea () / 10000;

		// 0. macroWPNames table
		Vector columnNames = new Vector ();
		columnNames.add (Translator.swap ("SVFinancialReport.rotationOrder"));
		columnNames.add (Translator.swap ("SVFinancialReport.macroWPName"));
		Vector data = new Vector ();
		for (Integer i : new TreeSet<Integer> (macroWPNames.keySet ())) {
			String name = macroWPNames.get (i);
			Vector v = new Vector ();
			v.add (i);
			v.add (name);
			data.add (v);
		}
		JTable table0 = new JTable (data, columnNames);
		table0.setDefaultEditor (Object.class, null);
		//~ table0.setPreferredScrollableViewportSize (new Dimension (300, 40));
			int h0 = table0.getRowHeight () * Math.min (3, table0.getModel ().getRowCount ());	// up to 3 visible rows
			table0.setPreferredScrollableViewportSize (new Dimension (300, h0));

		table0.setBackground (Color.WHITE);
		table0.setDefaultRenderer (Currency.class, new CurrencyRenderer ());

		table0.getColumnModel ().getColumn (0).setPreferredWidth (30);
		table0.getColumnModel ().getColumn (1).setPreferredWidth (100);

		scroll0.getViewport ().setView (table0);



		// 1. Project table
		Collection<Integer> years = new ArrayList<Integer> ();
		Collection<Double> cashFlows = new ArrayList<Double> ();

		Collection<ProjectReportLine> lines = new ArrayList<ProjectReportLine> ();
		ProjectReportLine l = new ProjectReportLine ();
		lines.add (l);	// one single line in lines

		for (CropExpense e : cropExpenses) {
			int year = e.getBillBookYear ();
			double v = e.getQuantity () * e.getUnitPrice () * area_ha;
			double v1 = v;
			if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
				v1 = v1 / Math.pow ((1+discountRateValue), year);
			}
			l.cropExpense += v1;

			years.add (year);
			cashFlows.add (v * -1);	// cost

			v = e.getFuelConsumption () * area_ha;
			l.gazoleQuantity += v;
		}

		for (Product p : products) {
			int year = p.getBillBookYear ();
			double v = p.getQuantityInPreferredUnit () * p.getUnitPrice () * area_ha;
			double v1 = v;
			if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
				v1 = v1 / Math.pow ((1+discountRateValue), year);
			}
			l.wpExpense += v1;

			years.add (year);
			cashFlows.add (v * -1);	// cost

			v = p.getTotalFuelConsumption () * area_ha;
			l.gazoleQuantity += v;
		}

		String targetUnitKey = (String) quantityUnit.getSelectedItem ();
		String targetUnit = Product.getProductUnitsMap ().get (targetUnitKey);
		for (BillBookIncome i : incomes) {
			int year = i.getBillBookYear ();
			double v = i.getBillBookQuantity () * i.getBillBookUnitPrice () * area_ha;
			double v1 = v;
			if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
				v1 = v1 / Math.pow ((1+discountRateValue), year);
			}
			l.income += v1;

			double convertedQuantity = species.convert (i.getBillBookQuantity (),
					i.getBillBookQuantityUnit (), targetUnit);
			double v2 = convertedQuantity * area_ha;
			l.quantity += v2;

			years.add (year);
			cashFlows.add (v);	// income

		}
		// quantity is rounded
		l.quantity = Math.round (l.quantity);

		// If requested, include annual and variable costs - fc + op - 7.5.2008
		if (includeAnnualAndVariableCosts.isSelected ()) {
			Collection<AnnualCost> annualCosts = stand.getAnnualCosts ();
			Collection<VariableCost> variableCosts = stand.getVariableCosts ();
			int plantationAge = stand.getPlantationAge ();
			double totalAnnualAndVariableCosts = 0;
			
			for (AnnualCost cost : annualCosts) {
				for (int i = 1; i <= plantationAge; i++) {
					years.add (i);
					double v = cost.getAmount ();
					double m = cost.isCost () ? v*-1 : v;
					cashFlows.add (m);
					totalAnnualAndVariableCosts += m;
				}
			}
			
			for (VariableCost cost : variableCosts) {
				int dateMin = cost.getDateMin ();
				int dateMax = cost.getDateMax ();
				for (int i = dateMin; i <= Math.min (dateMax, plantationAge); i++) {
					years.add (i);
					double v = cost.getAmount ();
					double m = cost.isCost () ? v*-1 : v;
					cashFlows.add (m);
					totalAnnualAndVariableCosts += m;
				}
			}
			l.income += totalAnnualAndVariableCosts;
		}
		
		double NPV = FinancialTools.getNetPresentValue (discountRateValue, years, cashFlows);
		double NPVI = FinancialTools.getNetPresentValueI (discountRateValue, years, cashFlows);
		double IRR = FinancialTools.getInternalRateOfReturn (years, cashFlows);
		double CA = FinancialTools.getConstantAnnuity (discountRateValue, years, cashFlows);

		l.NPV = NPV;
		l.NPVI = NPVI;
		l.IRR = IRR * 100;	// in percent
		l.constantAnnuity = CA;

		ProjectReportTableModel model1 = new ProjectReportTableModel (lines);
		JTable table1 = new JTable (model1);
		table1.setDefaultEditor (Object.class, null);
		//~ table1.setPreferredScrollableViewportSize (new Dimension (300, 40));
			int h1 = table1.getRowHeight () * Math.min (1, model1.getRowCount ());	// up to 1 visible rows
			table1.setPreferredScrollableViewportSize (new Dimension (300, h1));


		table1.setDefaultRenderer (Currency.class, new CurrencyRenderer ());

		table1.getColumnModel ().getColumn (0).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (1).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (2).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (3).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (4).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (5).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (6).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (7).setPreferredWidth (30);
		table1.getColumnModel ().getColumn (8).setPreferredWidth (30);

		scroll1.getViewport ().setView (table1);

		// 2. Prepare product tables and wp table
		Map<String,ProductReportLine> cumulatedProductReportLines = new HashMap<String,ProductReportLine> ();
		Collection<ProductReportLine> productReportLines = new ArrayList<ProductReportLine> ();
		
		Map<String,WPReportLine> wpReportLines = new HashMap<String,WPReportLine> ();
		
		
		double totalCost = 0;
		double totalFuel = 0;	// fc + op - 19.3.2008
		for (BillBookLine p : productsAndIncomes) {
			if (p instanceof Product) {
				Product product = (Product) p;
				
				totalCost += p.getBillBookQuantity () * p.getBillBookUnitPrice ()
						* p.getStand ().getArea () / 10000;
				
				// cumulate the fuel of each intermediate product, see lower
				totalFuel += product.getTotalFuelConsumption ();
				
				// fc + op - 19.3.2008 - table 4: Working processes report
				Producer producer = ((Product) p).getProducer ();
				if (producer instanceof PaleoWorkingProcess) {
					String wpName = ExtensionManager.getName ((Extension)producer);
					
					// wp quantity
					double convertedQuantity = species.convert (p.getBillBookQuantity (),
							p.getBillBookQuantityUnit (), targetUnit);
					double v2 = convertedQuantity * area_ha;
					
					// wp fuel
					//~ Map wpFuel = ((BillBookCompatible) p.getStand ()).getWpFuel ();
					//~ double fuel = (Double) wpFuel.get (wpName);
					
					double fuel = product.getTotalFuelConsumption ();
					
					// wp expense
					int year = product.getBillBookYear ();
					double v = product.getQuantityInPreferredUnit () * product.getUnitPrice () * area_ha;
					double v1 = v;
					if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
						v1 = v1 / Math.pow ((1+discountRateValue), year);
					}
			
					WPReportLine line2 = wpReportLines.get (wpName);
					if (line2 == null) {
						// create a line
						line2 = new WPReportLine ();
						///
						line2.wpName = wpName;
						line2.quantity = v2;
						line2.gazoleQuantity = fuel;
						line2.wpExpense = v1;

						wpReportLines.put (wpName, line2);
					} else {
						// cumulate in the previous line for this wp
						line2.quantity += v2;
						line2.gazoleQuantity += fuel;
						line2.wpExpense += v1;
						
					}
				}
				
			} else if (p instanceof BillBookIncome) {
				// creation of table 2 and 3
				BillBookIncome i = (BillBookIncome) p;
				int year = i.getBillBookYear ();

				ProductReportLine candidate = new ProductReportLine ();
				candidate.rotationOrder = i.getStand ().getDate ();
				candidate.productName = Translator.swap (i.getProductName ());

				double convertedQuantity = species.convert (i.getBillBookQuantity (),
						i.getBillBookQuantityUnit (), targetUnit);
				candidate.quantity = convertedQuantity * area_ha;
				
				// fc + op - 19.3.2008
				//~ Map productFuel = ((BillBookCompatible) i.getStand ()).getProductFuel ();
				//~ double fuelForThisRotationAndThisProduct = (Double) productFuel.get (i.getProductName ());
					//~ candidate.gazoleQuantity = fuelForThisRotationAndThisProduct;
					//~ candidate.gazoleQuantity = i.getBillBookTotalFuelConsumption ();
					candidate.gazoleQuantity = totalFuel;

				if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
					totalCost = totalCost / Math.pow ((1+discountRateValue), year);
				}
				candidate.wpExpense = totalCost;

				double v = i.getBillBookQuantity () * i.getBillBookUnitPrice () * area_ha;
				if (applyDiscountRateToIncomeAndExpense.isSelected ()) {
					v = v / Math.pow ((1+discountRateValue), year);
				}
				candidate.income = v;

				candidate.balance = candidate.income - candidate.wpExpense;

				updateProductReportLines (candidate, cumulatedProductReportLines, productReportLines);
				
				totalCost = 0;	// for next branch in stand.productTree
				totalFuel = 0;
			}
		}

		// 2.1 Cumulated product table
		Collection<ProductReportLine> productLines = cumulatedProductReportLines.values ();
		ProductReportTableModel model2 = new ProductReportTableModel (productLines);
		JTable table2 = new JTable (model2);
		table2.setDefaultEditor (Object.class, null);
		//~ table2.setPreferredScrollableViewportSize (new Dimension (300, 150));
			int h2 = table2.getRowHeight () * Math.min (5, model2.getRowCount ());	// up to 5 visible rows
			table2.setPreferredScrollableViewportSize (new Dimension (300, h2));

		table2.setDefaultRenderer (Currency.class, new CurrencyRenderer ());

		table2.getColumnModel ().getColumn (0).setPreferredWidth (30);
		table2.getColumnModel ().getColumn (1).setPreferredWidth (30);
		table2.getColumnModel ().getColumn (2).setPreferredWidth (30);
		table2.getColumnModel ().getColumn (3).setPreferredWidth (30);
		table2.getColumnModel ().getColumn (4).setPreferredWidth (30);
		table2.getColumnModel ().getColumn (5).setPreferredWidth (30);

		scroll2.getViewport ().setView (table2);

		// 2.2 Product table
		ProductReportTableModel3 model3 = new ProductReportTableModel3 (productReportLines);
		JTable table3 = new JTable (model3);
		table3.setDefaultEditor (Object.class, null);
		//~ table3.setPreferredScrollableViewportSize (new Dimension (300, 150));
			int h3 = table3.getRowHeight () * Math.min (10, model3.getRowCount ());	// up to 10 visible rows
			table3.setPreferredScrollableViewportSize (new Dimension (300, h3));

		table3.setDefaultRenderer (Currency.class, new CurrencyRenderer ());

		table3.getColumnModel ().getColumn (0).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (1).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (2).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (3).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (4).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (5).setPreferredWidth (30);
		table3.getColumnModel ().getColumn (6).setPreferredWidth (30);

		scroll3.getViewport ().setView (table3);

		// 4. wp table
		Collection<WPReportLine> wpLines = wpReportLines.values ();
		WPReportTableModel model4 = new WPReportTableModel (wpLines);
		JTable table4 = new JTable (model4);
		table4.setDefaultEditor (Object.class, null);
		
			int h4 = table4.getRowHeight () * Math.min (5, model4.getRowCount ());	// up to 5 visible rows
			table4.setPreferredScrollableViewportSize (new Dimension (300, h4));

		table4.setDefaultRenderer (Currency.class, new CurrencyRenderer ());

		table4.getColumnModel ().getColumn (0).setPreferredWidth (30);
		table4.getColumnModel ().getColumn (1).setPreferredWidth (30);
		table4.getColumnModel ().getColumn (2).setPreferredWidth (30);
		table4.getColumnModel ().getColumn (3).setPreferredWidth (30);

		scroll4.getViewport ().setView (table4);

	}

	//
	public void updateProductReportLines (ProductReportLine candidate,
			Map<String,ProductReportLine> cumulatedProductReportLines,
			Collection<ProductReportLine> productReportLines) {
		// map
		String productName = candidate.productName;
		ProductReportLine line = cumulatedProductReportLines.get (productName);
		if (line == null) {
			cumulatedProductReportLines.put (productName, new ProductReportLine (candidate));
		} else {
			line.quantity += candidate.quantity;
			line.gazoleQuantity += candidate.gazoleQuantity;
			line.wpExpense += candidate.wpExpense;
			line.income += candidate.income;
			line.balance = line.income - line.wpExpense;
		}

		// collection
		productReportLines.add (candidate);
	}

	/**	Create the user interface
	*/
	public void createUI () {
		//~ setLayout (new GridLayout (1, 1));		// important for viewer appearance
		setLayout (new BorderLayout ());		// important for viewer appearance

		ColumnPanel part1 = new ColumnPanel (0, 0);
		part1.setMargin (5);

		double discountRateValue ;
		discountRateValue = Settings.getProperty ("financial.report.discount.rate", 4d);
		
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("SVFinancialReport.discountRate")+" : ", 120));
		discountRate = new JTextField (5);
		discountRate.setText (""+discountRateValue);
		discountRate.addActionListener (this);
		l1.add (discountRate);

		boolean apply;
		
		apply = Settings.getProperty ("financial.report.apply.discount.rate.to.income.and.expense", false);
		
		applyDiscountRateToIncomeAndExpense = new JCheckBox (
				Translator.swap ("SVFinancialReport.applyDiscountRateToIncomeAndExpense"), apply);
		applyDiscountRateToIncomeAndExpense.addActionListener (this);
		l1.add (applyDiscountRateToIncomeAndExpense);
		l1.addStrut0 ();
		part1.add (l1);

		add (part1, BorderLayout.NORTH);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("SVFinancialReport.quantityUnit")+" : ", 120));
		Vector v = new Vector (Product.getProductUnitsMap ().keySet ());
		quantityUnit = new JComboBox (v);
		String defaultSelection ;
		
		defaultSelection = Settings.getProperty ("financial.report.quantity.unit", Translator.swap (Product.MWH));
				
		quantityUnit.setSelectedItem (defaultSelection);
		quantityUnit.addActionListener (this);
		l2.add (quantityUnit);
		l2.addGlue ();
		part1.add (l2);
		part1.addStrut0 ();


		ColumnPanel part2 = new ColumnPanel (0, 0);
		part2.setMargin (5);

		LinePanel l5 = new LinePanel ();
		l5.add (new JLabel (Translator.swap ("SVFinancialReport.projectReportTable")+" : "));

		boolean includeCosts;
		includeCosts = Settings.getProperty ("financial.report.include.annual.and.variable.costs", false);

		includeAnnualAndVariableCosts = new JCheckBox (
				Translator.swap ("SVFinancialReport.includeAnnualAndVariableCosts"), includeCosts);
		includeAnnualAndVariableCosts.addActionListener (this);
		l5.add (includeAnnualAndVariableCosts);

		l5.addGlue ();
		part2.add (l5);
		scroll0 = new JScrollPane ();
		part2.add (scroll0);
		scroll1 = new JScrollPane ();
		part2.add (scroll1);

		LinePanel l6 = new LinePanel ();
		l6.add (new JLabel (Translator.swap ("SVFinancialReport.productReportTable1")+" : "));
		l6.addGlue ();
		part2.add (l6);
		scroll2 = new JScrollPane ();
		part2.add (scroll2);

		LinePanel l7 = new LinePanel ();
		l7.add (new JLabel (Translator.swap ("SVFinancialReport.productReportTable2")+" : "));
		l7.addGlue ();
		part2.add (l7);
		scroll3 = new JScrollPane ();
		part2.add (scroll3);
		part2.addStrut0 ();

		// fc + op - 19.3.2008
		LinePanel l8 = new LinePanel ();
		l8.add (new JLabel (Translator.swap ("SVFinancialReport.wpReport")+" : "));
		l8.addGlue ();
		part2.add (l8);
		scroll4 = new JScrollPane ();
		part2.add (scroll4);
		part2.addGlue ();

		JScrollPane s = new JScrollPane (part2);
		s.setBorder (null);
		add (s, BorderLayout.CENTER);
		//~ add (part2, BorderLayout.SOUTH);

	}

	/**	Used for the settings buttons.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (discountRate)) {
			discountRateAction ();
		} else if (evt.getSource ().equals (applyDiscountRateToIncomeAndExpense)) {
			applyDiscountRateToIncomeAndExpenseAction ();
		} else if (evt.getSource ().equals (includeAnnualAndVariableCosts)) {
			includeAnnualAndVariableCostsAction ();
		} else if (evt.getSource ().equals (quantityUnit)) {
			quantityUnitAction ();
		}
	}

	private void discountRateAction () {
		if (!Check.isDouble (discountRate.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SVFinancialReport.discountRateIsNotADouble"));
			return;
		}

		double v = Check.doubleValue (discountRate.getText ().trim ());
		Settings.setProperty ("financial.report.discount.rate", ""+v);

		update ();
	}

	private void applyDiscountRateToIncomeAndExpenseAction () {
		Settings.setProperty ("financial.report.apply.discount.rate.to.income.and.expense", ""
				+applyDiscountRateToIncomeAndExpense.isSelected ());
		update ();
	}

	private void includeAnnualAndVariableCostsAction () {
		Settings.setProperty ("financial.report.include.annual.and.variable.costs", ""
				+includeAnnualAndVariableCosts.isSelected ());
		update ();
	}

	private void quantityUnitAction () {
		String v = (String) quantityUnit.getSelectedItem ();
		Settings.setProperty ("financial.report.quantity.unit", v);
		update ();
	}

	/**	From Pilotable interface.
	*/
	//~ public JComponent getPilot () {
		//~ ImageIcon icon = new IconLoader ().getIcon ("help_16.png");
		//~ helpButton = new JButton (icon);
		//~ Tools.setSizeExactly (helpButton, 23, 23);
		//~ helpButton.setToolTipText (Translator.swap ("Shared.help"));
		//~ helpButton.addActionListener (this);

		//~ JToolBar toolbar = new JToolBar ();
		//~ toolbar.add (helpButton);
		//~ toolbar.setVisible (true);

		//~ return toolbar;
	//~ }

}

