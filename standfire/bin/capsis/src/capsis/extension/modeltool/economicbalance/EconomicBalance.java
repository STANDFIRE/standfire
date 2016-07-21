/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
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

package capsis.extension.modeltool.economicbalance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.ButtonColorer;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.projectmanager.ProjectManager;
import capsis.commongui.util.Helper;
import capsis.defaulttype.TreeCollection;
import capsis.extension.DialogModelTool;
import capsis.extension.economicfunction.Expense;
import capsis.extension.economicfunction.Income;
import capsis.extensiontype.EconomicFunction;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.CommonEconFunctions;
import capsis.lib.economics.EconModel;
import capsis.lib.economics.EconStand;
import capsis.lib.economics.RegularExpenseOrIncome;
import capsis.util.JSmartFileChooser;
import capsis.util.methodprovider.NProvider;

/**
 * Makes the economic balance of your project associating incomes and expenses
 * to interventions
 * 
 * @author C. Orazio - january 2003
 */
public class EconomicBalance extends DialogModelTool implements ActionListener, MouseListener, TableModelListener,
		ListSelectionListener {

	static {
		Translator.addBundle ("capsis.extension.modeltool.economicbalance.EconomicBalance");
	}

	static public final String AUTHOR = "C. Orazio";
	static public final String VERSION = "1.2.0";

	private boolean hasbeenmodified;

	private JTable table1;
	private JTable table2;
	private TableModel tableModel1;
	private TableModel tableModel2;

	private JScrollPane scroll1;

	private Vector table2Rows; // a vector of vectors (lines), displayed in
								// table 2
	private Vector emptyRow2; // an empty line for table 2

	private String none;

	private Map expenseFunctions;
	private Map incomeFunctions;

	private JTextField b;
	private JTextField bao;
	private JTextField basio;
	private JTextField tir;
	private JTextField ace;
	private JTextField actualizationRate;
	private JTextField startingDate;

	private JTextField status;
	private JLabel mainStatus;
	private JTextField fileNameTable2;

	private JButton calculate;
	private JButton close;
	private JButton help;
	private JButton saveTable2;
	private JButton loadTable2;

	private ExtensionManager extMan;
	private NumberFormat formater;

	private Step step;
	private GModel model;
	// constants more explicit than column numbers
	private static final int TABLE1_STEP = 0;
	private static final int TABLE1_YEAR = 1;
	private static final int TABLE1_LABEL = 2;
	private static final int TABLE1_FUNCTIONS_EXPENSES = 3;
	private static final int TABLE1_FUNCTIONS_INCOMES = 4;
	private static final int TABLE1_EXPENSES = 5;
	private static final int TABLE1_INCOMES = 6;
	private static final int TABLE1_BENEFIT = 7;
	private static final int TABLE1_FUNCTIONS_EXPENSES_REFERENCE = 8;
	private static final int TABLE1_BENEFIT_INCOMES_REFERENCE = 9;
	private static final int TABLE1_COLUMN_NUMBER = 10;

	private static final int TABLE2_YEAR_BEGIN = 0;
	private static final int TABLE2_YEAR_END = 1;
	private static final int TABLE2_LABEL = 2;
	private static final int TABLE2_EXPENSES = 3;
	private static final int TABLE2_INCOMES = 4;
	private static final int TABLE2_BENEFIT = 5;
	private static final int TABLE2_COLUMN_NUMBER = 6;

	private static final String separator = EconomicFunction.separator;

	/**
	 * Default constructor.
	 */
	public EconomicBalance () {
		super ();
	}

	@Override
	public void init (GModel m, Step s) {

		try {
			step = s;
			model = m;

			setTitle (Translator.swap ("EconomicBalance") + " - " + VERSION + " - " + step.getCaption ());
			none = "[" + Translator.swap ("EconomicBalance.none") + "]";
			extMan = CapsisExtensionManager.getInstance ();

			makeFunctions ();

			hasbeenmodified = false;// Become true if some econ data are
									// modified

			formater = NumberFormat.getNumberInstance (Locale.ENGLISH);
			formater.setMinimumFractionDigits (2);
			formater.setMaximumFractionDigits (2);
			formater.setMinimumIntegerDigits (0);
			formater.setGroupingUsed (false);

			createUI ();
			updateMainStatus ();

			// Managed by AmapDialog superclass (these lines result in asking 2
			// confirmations)
			// setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
			// addWindowListener (new WindowAdapter () {
			// public void windowClosing (WindowEvent evt) {
			// escapePressed ();
			// }
			// });

			pack (); // sets the size
			setVisible (true);
			setModal (false);
			setResizable (true);

			readModelParameters ();// get the parameters from EconModel
			calculateAction ();// Calculate economic indicators

		} catch (Exception exc) {
			Log.println (Log.ERROR, "EconomicBalance.c ()", exc.toString (), exc);
		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {

			if (!(referent instanceof GModel)) { return false; }
			if (!(referent instanceof EconModel)) { return false; }

			GModel m = (GModel) referent;
			Step root = (Step) m.getProject ().getRoot ();
			GScene s = root.getScene ();

			// ~ fc - 29.3.2004 - if (! (s instanceof EconStand) && (s!=null))
			// {return false;}
			if (!(s instanceof EconStand)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "EconomicBalance.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	// Get the compatible functions for menus
	private void makeFunctions () {
		expenseFunctions = new HashMap ();
		incomeFunctions = new HashMap ();

		Collection functions = extMan.getExtensionClassNames (CapsisExtensionManager.ECONOMIC_FUNCTION, model);
		for (Iterator i = functions.iterator (); i.hasNext ();) {
			String className = (String) i.next ();
			try {
				boolean compatible = extMan.isCompatible (className, model);
				String name = ExtensionManager.getName (className);
				if (compatible) {// I don't know why it doesn't work to be
									// checked
					if (ExtensionManager.isInstanceOf (className, Expense.class)) {
						expenseFunctions.put (name, className);
					} else {
						incomeFunctions.put (name, className);
					}
				}
			} catch (Exception e) {
				Log.println (Log.ERROR, "EconomicBalance.makeFunctions ()", "Could not load phantom extension "
						+ className + " due to exception", e);
			}
		}
	}

	/****************************************************************************************
	 * Compute indicators of the balance
	 ****************************************************************************************/
	public void calculateAction () {
		// Update EconModel (table1 is saved at each change!)
		saveTable1 ();
		saveTable2 ();
		saveModelParameters ();
		updateMainStatus ();
		// Calculate economic parameters
		b.setText (formater.format (CommonEconFunctions.standBenefit (step)));
		bao.setText (formater.format (CommonEconFunctions.standPNV (step,
				((EconModel) model).getActualizationRate () / 100)));
		basio.setText (formater.format (CommonEconFunctions.standPNVIS (step, ((EconModel) model)
				.getActualizationRate () / 100)));
		tir.setText (formater.format (CommonEconFunctions.standIRR (step)));
		ace.setText (formater.format (CommonEconFunctions.standPNVISbyD (step, ((EconModel) model)
				.getActualizationRate () / 100)));

		// Update display column in tables
		TableModel m1 = table1.getModel ();
		for (int i = 0; i < m1.getRowCount (); i++) {
			table1.setValueAt (formater.format (CommonEconFunctions
					.standBenefit ((Step) m1.getValueAt (i, TABLE1_STEP))), i, TABLE1_BENEFIT);
		}
		TableModel m2 = table2.getModel ();
		for (int i = 0; i < m2.getRowCount (); i++) {
			double sum = 0;
			if (!((String) m2.getValueAt (i, TABLE2_YEAR_BEGIN) == null
					|| (String) m2.getValueAt (i, TABLE2_YEAR_BEGIN) == ""
					|| (String) m2.getValueAt (i, TABLE2_YEAR_END) == null || (String) m2.getValueAt (i,
					TABLE2_YEAR_END) == "")) {
				int date1 = new Integer ((String) m2.getValueAt (i, TABLE2_YEAR_BEGIN)).intValue () + 0;
				int date2 = new Integer ((String) m2.getValueAt (i, TABLE2_YEAR_END)).intValue () + 0; // co
																										// 21/07/03
																										// ERREUR!!!!!!!
																										// trace
																										// dans
																										// la
																										// log?????
																										// je
																										// ne
																										// sais
																										// pas
																										// pourquoi
				date1 = Math.max (date1, CommonEconFunctions.StartingDate (step));
				date2 = Math.min (date2, CommonEconFunctions.EndingDate (step));
				for (int a = (int) Math.round (date1); a <= Math.round (date2); a++) {
					double inc = new Double ((String) m2.getValueAt (i, TABLE2_INCOMES)).doubleValue () + 0d;
					double exp = new Double ((String) m2.getValueAt (i, TABLE2_EXPENSES)).doubleValue () + 0d;
					// double ar = new Double
					// (actualizationRate.getText()).doubleValue();
					// sum += (inc-exp)/Math.pow(1+ar,a) ;
					sum += inc - exp;
					// System.out.println ( "inc:"+inc+"  exp:"+exp+
					// "  sum:"+sum);
				}
			}
			table2.setValueAt (formater.format (sum), i, TABLE2_BENEFIT);
		}
		
		// Force data extractors update
		Step root = (Step) Current.getInstance ().getProject ().getRoot ();
		ButtonColorer.getInstance ().moveColor (ProjectManager.getInstance ().getStepButton (root));
		ButtonColorer.getInstance ().moveColor (ProjectManager.getInstance ().getStepButton (Current.getInstance ().getStep ()));
		
	}

	/**
	 * Insert an expense in table1
	 */
	public void expenseAction (String key) {

		try {
			int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());
			Step s = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);
			GScene std = s.getScene ();

			if (key.equals (none)) {
				EconomicFunction f = (EconomicFunction) table1.getModel ().getValueAt (numRow,
						TABLE1_FUNCTIONS_EXPENSES_REFERENCE);
				((EconStand) std).removeEconomicFunction (f);

				table1.getModel ().setValueAt (none, numRow, TABLE1_FUNCTIONS_EXPENSES);
				table1.getModel ().setValueAt (formater.format (0), numRow, TABLE1_EXPENSES); // overwrite
																								// result
				table1.getModel ().setValueAt (null, numRow, TABLE1_FUNCTIONS_EXPENSES_REFERENCE);

			} else {
				String className = (String) expenseFunctions.get (key);

				try {
					EconomicFunction f = (EconomicFunction) extMan.loadInitData (className,
							new GenericExtensionStarter ("model", s.getProject ().getModel (), "step", s));
					f.init (s.getProject ().getModel (), s);

					double result = f.getResult ();
					Log.println (Log.INFO, "EconomicBalance.expenseAction ()", " result=" + result);
					table1.getModel ().setValueAt (key, numRow, TABLE1_FUNCTIONS_EXPENSES);

					String r = formater.format (result);
					table1.getModel ().setValueAt (r, numRow, TABLE1_EXPENSES);

					EconomicFunction f0 = (EconomicFunction) table1.getModel ().getValueAt (numRow,
							TABLE1_FUNCTIONS_EXPENSES_REFERENCE);
					((EconStand) std).removeEconomicFunction (f0);
					table1.getModel ().setValueAt (f, numRow, TABLE1_FUNCTIONS_EXPENSES_REFERENCE);
					((EconStand) std).addEconomicFunction (f);
					hasbeenmodified = true;
					// calculateAction ();

				} catch (Exception e) {
					Log.println (Log.WARNING, "EconomicBalance.expenseAction ()",
							"Could not load extension (maybe canceled?) " + className + " due to exception: " + e);
					status.setText (Translator.swap ("EconomicBalance.interventionAborted"));
					table1.getModel ().setValueAt (none, numRow, TABLE1_FUNCTIONS_EXPENSES);
					table1.getModel ().setValueAt (formater.format (0), numRow, TABLE1_EXPENSES); // delete
																									// result
					table1.getModel ().setValueAt (null, numRow, TABLE1_FUNCTIONS_EXPENSES_REFERENCE);
					readTable1 ();
				}
			}

		} catch (Exception e) {
			return;
		} // unexpected trouble : abort

		valueChanged (null);
	}

	/**
	 * Insert an income in table1
	 */
	public void incomeAction (String key) {

		try {
			int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());
			Step s = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);
			GScene std = s.getScene ();

			if (key.equals (none)) {
				EconomicFunction f = (EconomicFunction) table1.getModel ().getValueAt (numRow,
						TABLE1_BENEFIT_INCOMES_REFERENCE);
				((EconStand) std).removeEconomicFunction (f);

				table1.getModel ().setValueAt (none, numRow, TABLE1_FUNCTIONS_INCOMES);
				table1.getModel ().setValueAt (formater.format (0), numRow, TABLE1_INCOMES); // overwrite
																								// result
				table1.getModel ().setValueAt (null, numRow, TABLE1_BENEFIT_INCOMES_REFERENCE);

			} else {
				String className = (String) incomeFunctions.get (key);

				try {
					EconomicFunction f = (EconomicFunction) extMan.loadInitData (className,
							new GenericExtensionStarter ("model", s.getProject ().getModel (), "step", s));
					f.init (s.getProject ().getModel (), s);

					double result = f.getResult ();

					table1.getModel ().setValueAt (key, numRow, TABLE1_FUNCTIONS_INCOMES);

					String r = formater.format (result);
					table1.getModel ().setValueAt (r, numRow, TABLE1_INCOMES);

					EconomicFunction f0 = (EconomicFunction) table1.getModel ().getValueAt (numRow,
							TABLE1_BENEFIT_INCOMES_REFERENCE);
					((EconStand) std).removeEconomicFunction (f0);
					table1.getModel ().setValueAt (f, numRow, TABLE1_BENEFIT_INCOMES_REFERENCE);
					((EconStand) std).addEconomicFunction (f);
					hasbeenmodified = true;
					// calculateAction ();

				} catch (Exception e) {
					Log.println (Log.ERROR, "EconomicBalance.incomeAction ()",
							"Could not load extension (maybe canceled?) " + className + " due to exception", e);
					status.setText (Translator.swap ("EconomicBalance.interventionAborted"));
					table1.getModel ().setValueAt (none, numRow, TABLE1_FUNCTIONS_INCOMES);
					table1.getModel ().setValueAt (formater.format (0), numRow, TABLE1_INCOMES); // delete
																									// result
					table1.getModel ().setValueAt (null, numRow, TABLE1_BENEFIT_INCOMES_REFERENCE);
					readTable1 ();
				}
				// calculateAction ();
			}

		} catch (Exception e) {
			return;
		} // unexpected trouble : abort

		valueChanged (null);

	}

	/**
	 * Update status will take into account date correction
	 */
	public void updateMainStatus () {
		Step steproot = (Step) step.getProject ().getRoot ();
		mainStatus.setText (Translator.swap ("EconomicBalance.area") + "(ha) : "
				+ (step.getScene ().getArea () / 10000) + " - " + Translator.swap ("EconomicBalance.period") + " : "
				+ CommonEconFunctions.StartingDate (step) + "-" + CommonEconFunctions.EndingDate (step));
	}

	/**
	 * select file dialog box from coligny
	 */
	private void browseAction (JTextField fileName, String label) {
		// FileName ? -> get it with a JFileChooser
		String name = "";
		String defaultExportPath = System.getProperty ("capsis.export.path");
		if ((defaultExportPath == null) || defaultExportPath.equals ("")) {
			defaultExportPath = PathManager.getInstallDir ();
		}

		JFileChooser chooser = new JSmartFileChooser (label, Translator.swap ("Shared.select"), defaultExportPath);

		int returnVal = chooser.showDialog (MainFrame.getInstance (), null); // null
																				// :
																				// approveButton
																				// text
																				// was
																				// already
																				// set

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.export.path", chooser.getSelectedFile ().toString ());
			name = chooser.getSelectedFile ().toString ();

			fileName.setText (name);

		} else {
			return; // cancel on file chooser -> do nothing
		}

	}

	/**
	 * Save table 2 in a text file
	 * 
	 * @Version 2.0
	 */
	public void saveTable2InAFile (String fileName) throws Exception {
		saveTable2 ();
		if (!Check.isFile (fileName)) {
			try {
				CommonEconFunctions.createEconParametersFile (fileName);
			} catch (Exception e) {
				throw new Exception ("File name " + fileName + " causes error : " + e.toString ());
			}
		}
		// Write the lines content
		Vector out = new Vector ();
		out.add ("FromDate" + separator + "ToDate" + separator + "Label" + separator + "Expense" + separator + "Income"
				+ separator);
		Collection lines = ((EconModel) model).getRegularExpenseOrIncomes ();
		if (!(lines == null && lines.isEmpty ())) {
			for (Iterator i = lines.iterator (); i.hasNext ();) {
				RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();
				// if (r.getFromDate ()!= null && r.getToDate()!=null &&
				// (r.getExpense()!=null || r.getIncome()!=null)){
				out.add (r.getFromDate () + separator + r.getToDate () + separator + r.getLabel () + separator
						+ formater.format (r.getExpense ()) + separator + formater.format (r.getIncome ()) + separator);
				// }
			}
		}
		// out.add(emptyRow2);
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "Table2", out);

		// below facultative part made for L. Guenneguez, can be removed in case
		// of trouble
		calculateAction ();
		out.clear ();
		out.add ("TABLE1_YEAR" + separator + "TABLE1_LABEL" + separator + "TABLE1_FUNCTIONS_EXPENSES" + separator
				+ "TABLE1_FUNCTIONS_INCOMES" + separator + "TABLE1_EXPENSES " + separator + "TABLE1_INCOMES"
				+ separator + "N Before" + separator + "N After" + separator);
		TableModel m1 = table1.getModel ();
		for (int numRow = 0; numRow < m1.getRowCount (); numRow++) {
			String s = (String) table1.getModel ().getValueAt (numRow, TABLE1_YEAR) + separator;
			s += (String) table1.getModel ().getValueAt (numRow, TABLE1_LABEL) + separator;
			s += (String) table1.getModel ().getValueAt (numRow, TABLE1_FUNCTIONS_EXPENSES) + separator;
			s += (String) table1.getModel ().getValueAt (numRow, TABLE1_FUNCTIONS_INCOMES) + separator;
			s += (String) table1.getModel ().getValueAt (numRow, TABLE1_EXPENSES) + separator;
			s += (String) table1.getModel ().getValueAt (numRow, TABLE1_INCOMES) + separator;
			Step localStep = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);
			MethodProvider mp = getStep ().getProject ().getModel ().getMethodProvider ();

			System.out.println ("numRow" + numRow);
			if (localStep.getFather () != null && mp instanceof NProvider) {
				GScene fatherStand = ((Step) localStep.getFather ()).getScene ();// fc
																					// -
																					// 8.4.2004
																					// +
																					// c.o-
																					// 6.8.2004
				Collection fatherTrees = null;
				try {
					fatherTrees = ((TreeCollection) fatherStand).getTrees ();
				} catch (Exception e) {
				}
				s += ((NProvider) mp).getN (fatherStand, fatherTrees) + separator;
			} else {
				s += separator;
			}
			if (localStep.getScene () != null && mp instanceof NProvider) {
				GScene localStand = localStep.getScene ();// fc - 8.4.2004 +
															// c.o- 6.8.2004
				Collection localTrees = null;
				try {
					localTrees = ((TreeCollection) localStand).getTrees ();
				} catch (Exception e) {
				}
				s += ((NProvider) mp).getN (localStand, localTrees) + separator;
			} else {
				s += separator;
			}
			out.add (s);
		}
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "Table1", out);
		out.clear ();
		out.add (b.getText ());
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "B", out);
		out.clear ();
		out.add (bao.getText ());
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "bao", out);
		out.clear ();
		out.add (tir.getText ());
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "tir", out);
		out.clear ();
		out.add (actualizationRate.getText ());
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "actualizationRate", out);
		out.clear ();
		out.add (mainStatus.getText ());
		CommonEconFunctions.updateEconParametersFile (fileName, "EconomicBalance", "mainStatus", out);
		// end of facultative part
	}

	/**
	 * Load text file in the table2
	 * 
	 * @Version 1.2
	 */
	public void readTable2InFile (String fileName) throws Exception {
		Vector fileContent = new Vector ();
		if (!Check.isFile (fileName)) { throw new Exception ("File name " + fileName
				+ " causes error trying to read it in readtable2InFile class"); }
		fileContent = CommonEconFunctions.getValueSubTagEconParametersFile (fileName, "EconomicBalance", "Table2");
		if (fileContent.size () != 0) {
			table2Rows.clear ();
			for (int j = 1; j <= fileContent.size () - 1; j++) {
				Vector row = new Vector ();
				System.out.println ("Adding to table2: " + fileContent.get (j).toString ());
				for (int i = 1; i <= 5; i++) { // the number 5 comes from save
												// in a file function parameters
					System.out.println ("Extract " + i + " value: "
							+ CommonEconFunctions.getValueFromString (fileContent.get (j).toString (), i, separator));
					row.add (CommonEconFunctions.getValueFromString (fileContent.get (j).toString (), i, separator));
				}
				row.add ("0"); // for the column number 6 that has not been
								// saved
				table2Rows.add (row);
				table2.revalidate ();
			}
		} else {
			Log.println (Log.WARNING, "EconomicBalance.loadingfromtext ()", "No compatible data in text file "
					+ fileName);
		}
	}

	/**
	 * update EconModel (save data from economicbalance to econmodel)
	 */

	public void saveTable1 () {
		// save table1 in EconModel (put the econLabel on steps)
		TableModel m1 = table1.getModel ();
		int nRow = m1.getRowCount ();

		for (int i = 0; i < nRow; i++) {
			String newReason = (String) m1.getValueAt (i, TABLE1_LABEL);
			Step stp = (Step) m1.getValueAt (i, TABLE1_STEP);
			String line = stp.getReason ();
			int begin = line.indexOf ("econLabel=\"");
			if (begin == -1) {
				newReason = line + " econLabel=\"" + newReason + "\"";
			} else {
				begin += 11;
				int end = line.indexOf ("\"", begin + 1); // I think we need
															// plus 1
				if (end == -1) {
					end = line.length ();
				}
				newReason = line.substring (0, begin) + newReason + line.substring (end, line.length ());
				System.out.println ("EconomicBalance.saveTable1 ()  saving table1, begin=" + begin + " end=" + end
						+ " begin=" + line.length ());
			}
			stp.setReason (newReason);
		}
	}

	public void saveTable2 () {
		// save table2 in EconModel
		TableModel m2 = table2.getModel ();
		int nRow = m2.getRowCount ();

		// 1. memorize regular exp and inc from table 2 in a temporary memo
		Collection memo = new ArrayList ();
		for (int i = 0; i < nRow; i++) {
			RegularExpenseOrIncome r = new RegularExpenseOrIncome ();

			try {
				String s1 = (String) m2.getValueAt (i, TABLE2_YEAR_BEGIN);
				String s2 = (String) m2.getValueAt (i, TABLE2_YEAR_END);
				String s3 = (String) m2.getValueAt (i, TABLE2_LABEL);
				String s4 = (String) m2.getValueAt (i, TABLE2_EXPENSES);
				String s5 = (String) m2.getValueAt (i, TABLE2_INCOMES);

				if ((s1 == "" && s2 == "" && s3 == "" && s4 == "" && s5 == "")
						|| (s1 == null && s2 == null && s3 == null && s4 == null && s5 == null)
						|| (s1 == "0" && s2 == "0" && s3 == "0" && s4 == "0" && s5 == "0")) {
					continue; // blank line
				}
				if (s1.trim ().length () == 0 && s2.trim ().length () == 0 && s3.trim ().length () == 0
						&& s4.trim ().length () == 0 && s5.trim ().length () == 0) {
					continue; // blank line
				}
				// set 0 if one of income or expense is null
				if (s1 == "" && s3 != "") {
					s1 = "0";
				}
				if (s2 == "" && s3 != "") {
					s2 = "0";
				}
				if (s4 == "" && s3 != "") {
					s4 = "0";
				}
				if (s5 == "" && s3 != "") {
					s5 = "0";
				}

				int v1 = new Integer (s1).intValue ();
				r.setFromDate (v1);

				int v2 = new Integer (s2).intValue ();
				r.setToDate (v2);

				r.setLabel (s3);

				double v4 = new Double (s4).doubleValue ();
				r.setExpense (v4);

				double v5 = new Double (s5).doubleValue ();
				r.setIncome (v5);

				memo.add (r);
				// ~ ((EconModel) model).TABLE1 (r);
			} catch (Exception e) {
				Log.println (Log.ERROR, "EconomicBalance.closeAction ()", "Error while saving table2, row=" + i
						+ "(from 0) due to exception", e);
				MessageDialog.print (this, Translator.swap ("EconomicBalance.errorDuringTable2SavingOnLine") + " "
						+ (i + 1), e);
				return;
			}
		}

		// 2. clear destination, then add regular exp or inc from memo to
		// EconModel
		try {
			((EconModel) model).getRegularExpenseOrIncomes ().clear ();
		} catch (Exception e) {
			Log.println (Translator.swap ("EconomicBalance.canNotSavetTable2DueToExceptionSeeLog") + " ("
					+ e.toString () + ")");
		}
		for (Iterator i = memo.iterator (); i.hasNext ();) {
			RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();
			((EconModel) model).addRegularExpenseOrIncomes (r);
		}
	}

	public void saveModelParameters () {
		if (actualizationRate.getText ().trim ().length () != 0) {
			double ar = new Double (actualizationRate.getText ()).doubleValue ();
			((EconModel) model).setActualizationRate (ar);
		}
		if (startingDate.getText ().trim ().length () != 0) {
			int sd = new Integer (startingDate.getText ()).intValue ();
			((EconModel) model).setEconomicModelStartingDate (sd);
		}
	}

	/**
	 * Called on window closing (escape, title bar or close button) -> update
	 * EconModel and EconStands
	 */
	public void closeAction () {
		try {
			// save table1 in EconStands Expenses and incomes are saved during
			// table1 management
			saveTable1 ();

			// save table2 in EconStand
			saveTable2 ();

			// save actualization rate in EconModel
			saveModelParameters ();

			// Note that project has been modified
			if (hasbeenmodified) {
				step.getProject ().setSaved (false);
			}

			dispose ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("EconomicBalance.canNotCloseDueToExceptionSeeLog"), e);
			return;
		}
	}

	/**
	 * FromListSelectionListener interface. Table1 row selection come here :
	 * update status textfield below table1.
	 */
	public void valueChanged (ListSelectionEvent evt) {
		if (evt != null && evt.getValueIsAdjusting ()) { return; }

		int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());
		EconomicFunction e = (EconomicFunction) table1.getModel ().getValueAt (numRow,
				TABLE1_FUNCTIONS_EXPENSES_REFERENCE); // expense function
		EconomicFunction i = (EconomicFunction) table1.getModel ()
				.getValueAt (numRow, TABLE1_BENEFIT_INCOMES_REFERENCE); // income
																		// function
		StringBuffer text = new StringBuffer ();
		if (e != null) {
			text.append (e.getFunctionParameters ());
		}
		if (i != null) {
			if (text.length () != 0) {
				text.append (" / ");
			}
			text.append (i.getFunctionParameters ());
		}
		status.setText (text.toString ());
	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource () instanceof JMenuItem) { // meni items
			JMenuItem item = (JMenuItem) evt.getSource ();
			int code = item.getMnemonic ();
			if (code == EconomicTable1Popup.EXPENSE) { // table 1
				expenseAction (item.getText ());
				calculateAction ();// Calculate economic indicators
			} else if (code == EconomicTable1Popup.INCOME) {
				incomeAction (item.getText ());
				calculateAction ();// Calculate economic indicators
			} else if (code == EconomicTable1Popup.INSERT_INTERVENTION_BEFORE) { // insert
																					// an
																					// economic
																					// intervention
																					// before
																					// a
																					// given
																					// row
				int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());
				Step a = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);

				try {
					int beforeDate = new Integer (item.getText ()).intValue ();

					while (a != null && a.getScene ().getDate () != beforeDate) {
						a = (Step) a.getFather ();
					}

					Step r = (Step) a.getFather ();

					CommonEconFunctions.insertEconomicIntervention (r, a);

					readTable1 ();
					ProjectManager.getInstance ().update ();
					hasbeenmodified = true;
					calculateAction ();// Calculate economic indicators
				} catch (Exception e) {
					Log.println (Log.ERROR, "EconomicBalance.actionPerformed ()",
							"Error while inserting intervention before row, row=" + numRow
									+ "(from 0) due to exception ", e);
					MessageDialog.print (this, Translator
							.swap ("EconomicBalance.errorWhileInsertingInterventionBefore")
							+ " " + (numRow + 1), e);
					return;
				}

			} else if (code == EconomicTable1Popup.INSERT_INTERVENTION_AFTER) { // temporary
																				// disabled

			} else if (code == EconomicTable1Popup.REMOVE_INTERVENTION) {
				int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());
				Step a = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);

				CommonEconFunctions.removeEconomicIntervention (a);

				readTable1 ();
				ProjectManager.getInstance ().update ();
				hasbeenmodified = true;
				calculateAction ();// Calculate economic indicators
			} else if (code == EconomicTable1Popup.INSERT_LINE) {

			} else if (code == EconomicTable1Popup.REMOVE_LINE) {

			} else if (code == EconomicTable2Popup.INSERT_LINE_BEFORE) { // table
																			// 2
				Vector emptyRow = new Vector ();
				for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
					emptyRow.add ("0");
				}
				int numRow = table2.convertRowIndexToModel (table2.getSelectedRow ());
				table2Rows.insertElementAt (emptyRow, numRow);
				table2.revalidate ();

				hasbeenmodified = true;
				calculateAction ();// Calculate economic indicators
			} else if (code == EconomicTable2Popup.INSERT_LINE_AFTER) {
				Vector emptyRow = new Vector ();
				for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
					emptyRow.add ("0");
				}

				int numRow = table2.convertRowIndexToModel (table2.getSelectedRow ());
				table2Rows.insertElementAt (emptyRow, numRow + 1);
				table2.revalidate ();

				hasbeenmodified = true;
				calculateAction ();// Calculate economic indicators
			} else if (code == EconomicTable2Popup.REMOVE_LINE) {
				int numRow = table2.convertRowIndexToModel (table2.getSelectedRow ());
				table2Rows.removeElementAt (numRow);
				table2.revalidate ();

				hasbeenmodified = true;
				calculateAction ();// Calculate economic indicators
			}

		} else if (evt.getSource ().equals (close)) {
			escapePressed ();
			// if (Question.ask (MainFrame.getInstance (),
			// Translator.swap ("EconomicBalance.confirm"), Translator.swap
			// ("EconomicBalance.confirmClose"))) {
			// closeAction ();
			// }

		}

		if (evt.getSource ().equals (saveTable2)) {
			if (fileNameTable2.getText () == null || fileNameTable2.getText ().equals ("")
					|| !Check.isFile (fileNameTable2.getText ())) {
				browseAction (fileNameTable2, Translator.swap ("EconomicBalance.saveInATextFile"));
			}
			try {
				saveTable2InAFile (fileNameTable2.getText ());
			} catch (Exception e) {
				Log.println (Log.ERROR, "EconomicBalance.actionPerformed ()",
						"Error while saving table2 into text file due to exception ", e);
				MessageDialog.print (this, Translator.swap ("EconomicBalance.errorWhileSavingIntoTextFileTable2"), e);
			}
			// hasbeenmodified = true;
		}

		if (evt.getSource ().equals (loadTable2)) {
			if (fileNameTable2.getText () == null || fileNameTable2.getText ().equals ("")) {
				browseAction (fileNameTable2, Translator.swap ("EconomicBalance.loadFromATextFile"));
			}
			try {
				readTable2InFile (fileNameTable2.getText ());
			} catch (Exception e) {
				Log.println (Log.ERROR, "EconomicBalance.actionPerformed ()",
						"Error while loading text file into table 2 due to exception ", e);
				MessageDialog.print (this, Translator.swap ("EconomicBalance.errorWhileLoadingFromTextFileTable2"), e);
			}
			hasbeenmodified = true;
		}

		if (evt.getSource ().equals (calculate)) {
			calculateAction ();
			updateMainStatus ();
		}

		if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}

	}

	/**
	 * From tableModelListener : detect changes in tables
	 */
	public void tableChanged (TableModelEvent evt) {
		if ((evt.getSource () == tableModel1 & !(evt.getColumn () == TABLE1_EXPENSES
				|| evt.getColumn () == TABLE1_INCOMES || evt.getColumn () == TABLE1_BENEFIT))
				|| (evt.getSource () == tableModel2 & !(evt.getColumn () == TABLE2_BENEFIT))) {
			System.out.println ("INFO EconomicBalance Evènement dans la table " + evt.getSource () + " at column "
					+ table1.getColumnName (evt.getColumn ()) + " de Type: " + evt.getType ());
			hasbeenmodified = true;
			if (evt.getSource () == tableModel2) {
				calculateAction ();
			}
		}
	}

	/**
	 * Called on Escape. Redefinition of method in AmapDialog : ask for user
	 * confirmation.
	 */
	@Override
	protected void escapePressed () {
		if (Question.ask (this, Translator.swap ("EconomicBalance.confirm"), Translator
				.swap ("EconomicBalance.confirmClose"))) {
			closeAction ();
		}
	}

	public Step getStep () {
		return step;
	}

	private void readModelParameters () {
		actualizationRate.setText (((EconModel) model).getActualizationRate () + "");
		startingDate.setText (((EconModel) model).getEconomicModelStartingDate () + "");
	}

	/*********************
	 * /* Get data from models structure and display them in table1 /
	 *********************/

	private void readTable1 () {

		Vector rows = new Vector ();
		Vector row = new Vector ();
		Vector columnNames = new Vector ();
		columnNames.add ("Step reference"); // C0, not visible
		columnNames.add (Translator.swap ("EconomicBalance.year")); // C1
		columnNames.add (Translator.swap ("EconomicBalance.intervention")); // C2
		columnNames.add (Translator.swap ("EconomicBalance.expenseFunction")); // C3
		columnNames.add (Translator.swap ("EconomicBalance.incomeFunction")); // C4
		columnNames.add (Translator.swap ("EconomicBalance.expense")); // C5
		columnNames.add (Translator.swap ("EconomicBalance.income")); // C6
		columnNames.add (Translator.swap ("EconomicBalance.result")); // C7
		columnNames.add ("expenseFunctionReference"); // C8 not visible
		columnNames.add ("incomeFunctionReference"); // C9 not visible

		Vector steps = step.getProject ().getStepsFromRoot (step);
		String line = "";
		for (Iterator i = steps.iterator (); i.hasNext ();) {
			Step stp = (Step) i.next ();
			GScene std = stp.getScene ();
			//if (std.isInitialScene () || std.isInterventionResult ()) {
			if(std.isInitialScene () || std.isInterventionResult () || ((EconStand) std).getEconomicFunctions () != null) {
				row = new Vector ();
				row.add (stp); // C0
				row.add ("" + std.getDate ()); // C1

				// NOTE:
				// "hamburger".substring(4, 8) returns "urge"
				// "smiles".substring(1, 5) returns "mile"
				line = CommonEconFunctions.getEconLabel (stp);
				if (std.isInitialScene () && line == "") {
					line = Translator.swap ("EconomicBalance.initialStep");
				}
				row.add (line); // C2

				Expense expF = null;
				Income incF = null;
				EconStand es = (EconStand) std;
				Collection functions = es.getEconomicFunctions ();
				if (functions != null) {
					for (Iterator j = functions.iterator (); j.hasNext ();) {
						EconomicFunction f = (EconomicFunction) j.next ();
						if (f instanceof Expense) {
							expF = (Expense) f;
						}
						if (f instanceof Income) {
							incF = (Income) f;
						}
					}
				}

				row.add ((expF == null) ? none : expF.getName ()); // C3
				row.add ((incF == null) ? none : incF.getName ()); // C4
				row.add ((expF == null) ? formater.format (0) : formater.format (expF.getResult ())); // C5
				row.add ((incF == null) ? formater.format (0) : formater.format (incF.getResult ())); // C6
				row.add (formater.format (0)); // C7

				row.add (expF); // C8
				row.add (incF); // C9

				rows.add (row);
			}

		}
		table1 = new JTable (rows, columnNames);

		TableColumn c0 = table1.getColumnModel ().getColumn (TABLE1_STEP);
		c0.setWidth (0);
		c0.setMinWidth (0);
		c0.setMaxWidth (0);

		TableColumn c8 = table1.getColumnModel ().getColumn (TABLE1_FUNCTIONS_EXPENSES_REFERENCE);
		c8.setWidth (0);
		c8.setMinWidth (0);
		c8.setMaxWidth (0);

		TableColumn c9 = table1.getColumnModel ().getColumn (TABLE1_BENEFIT_INCOMES_REFERENCE);
		c9.setWidth (0);
		c9.setMinWidth (0);
		c9.setMaxWidth (0);

		// set columns sizes
		table1.getColumnModel ().getColumn (TABLE1_YEAR).setPreferredWidth (20);
		table1.getColumnModel ().getColumn (TABLE1_LABEL).setPreferredWidth (100);
		table1.getColumnModel ().getColumn (TABLE1_FUNCTIONS_EXPENSES).setPreferredWidth (50);
		table1.getColumnModel ().getColumn (TABLE1_FUNCTIONS_INCOMES).setPreferredWidth (50);
		table1.getColumnModel ().getColumn (TABLE1_EXPENSES).setPreferredWidth (40);
		table1.getColumnModel ().getColumn (TABLE1_INCOMES).setPreferredWidth (40);
		table1.getColumnModel ().getColumn (TABLE1_BENEFIT).setPreferredWidth (40);

		scroll1.getViewport ().setView (table1);

		table1.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		table1.addMouseListener (this);
		tableModel1 = table1.getModel ();
		tableModel1.addTableModelListener (this);
		table1.getSelectionModel ().addListSelectionListener (this);

	}

	/*********************
	 * /* Get data from models structure and display them in table2 /
	 *********************/

	private void readTable2 () {

		table2Rows = new Vector ();
		Vector row = new Vector ();
		Vector columnNames = new Vector ();
		columnNames.add (Translator.swap ("EconomicBalance.begin"));
		columnNames.add (Translator.swap ("EconomicBalance.end"));
		columnNames.add (Translator.swap ("EconomicBalance.constantCharges"));
		columnNames.add (Translator.swap ("EconomicBalance.expense"));
		columnNames.add (Translator.swap ("EconomicBalance.income"));
		columnNames.add (Translator.swap ("EconomicBalance.result"));

		Collection lines = ((EconModel) model).getRegularExpenseOrIncomes ();
		if (lines == null) {
			row = new Vector ();
			for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
				row.add ("0");
			}
			table2Rows.add (row);
			row = new Vector ();
			for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
				row.add ("0");
			}
			table2Rows.add (row);
			row = new Vector ();
			for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
				row.add ("0");
			}
			table2Rows.add (row);
			table2 = new JTable (table2Rows, columnNames);
		} else {
			for (Iterator i = lines.iterator (); i.hasNext ();) {
				RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();
				row = new Vector ();

				row.add ("" + r.getFromDate ());
				row.add ("" + r.getToDate ());
				row.add ("" + r.getLabel ());
				row.add ("" + r.getExpense ());
				row.add ("" + r.getIncome ());
				row.add ("0");
				table2Rows.add (row);
			}
			// Insert an empty line after having loaded the others
			emptyRow2 = new Vector ();
			for (int i = 1; i <= TABLE2_COLUMN_NUMBER; i++) {
				row.add ("0");
			}
			// table2Rows.add (emptyRow2);
			table2 = new JTable (table2Rows, columnNames);
		}
		table2.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		table2.addMouseListener (this);
		tableModel2 = table2.getModel ();
		tableModel2.addTableModelListener (this);

	}

	/**
	 * User interface definition.
	 */
	private void createUI () {

		// 0. Main panel
		JPanel mainPanel = new JPanel (new BorderLayout ());
		mainStatus = new JLabel ();
		LinePanel l10 = new LinePanel ();
		l10.addGlue ();
		l10.add (mainStatus);
		l10.addGlue ();
		mainPanel.add (l10, BorderLayout.NORTH);
		// 1. Split pane with tables

		// 1.1 left component
		JPanel left1 = new JPanel (new BorderLayout ());
		scroll1 = new JScrollPane ();
		readTable1 ();
		status = new JTextField (5);
		left1.add (scroll1, BorderLayout.CENTER);
		left1.add (status, BorderLayout.SOUTH);

		ColumnPanel c1 = new ColumnPanel (Translator.swap ("EconomicBalance.table1"));
		c1.add (LinePanel.addWithStrut0 (left1));
		c1.addStrut0 ();

		// 1.2 right component
		JPanel right1 = new JPanel (new BorderLayout ());
		readTable2 ();

		right1.add (new JScrollPane (table2), BorderLayout.CENTER);

		LinePanel l20 = new LinePanel ();
		// saveTable2 = new JButton (Translator.swap
		// ("EconomicBalance.saveFileTable2"));
		// saveTable2 = new JButton (new
		// ImageIcon("capsis/extension/modeltool/economicbalance/images/save.gif"));
		saveTable2 = new JButton (new ImageIcon ("src/capsis/extension/modeltool/economicbalance/images/save.gif"));
		saveTable2.addActionListener (this);
		// loadTable2 = new JButton (new
		// ImageIcon("capsis/extension/modeltool/economicbalance/images/open.gif"));
		loadTable2 = new JButton (new ImageIcon ("src/capsis/extension/modeltool/economicbalance/images/open.gif"));
		loadTable2.addActionListener (this);
		fileNameTable2 = new JTextField (10);
		l20.add (fileNameTable2);
		l20.add (loadTable2);
		l20.add (saveTable2);
		right1.add (l20, BorderLayout.SOUTH);

		ColumnPanel c2 = new ColumnPanel (Translator.swap ("EconomicBalance.table2"));
		c2.add (LinePanel.addWithStrut0 (right1));
		c2.addStrut0 ();

		JSplitPane split = new JSplitPane (JSplitPane.VERTICAL_SPLIT, true, c1, c2);
		split.setPreferredSize (new Dimension (800, 400));
		split.setOneTouchExpandable (true);
		// split.setDividerLocation (500);
		split.setResizeWeight (0.5);

		// 2. Calculation results
		JPanel part2 = new JPanel (new BorderLayout ());

		ColumnPanel left2 = new ColumnPanel ();

		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("EconomicBalance.b") + " :", 80));
		b = new JTextField (10);
		l0.add (b);
		l0.addGlue ();
		left2.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("EconomicBalance.bao") + " :", 80));
		bao = new JTextField (10);
		l1.add (bao);
		l1.addGlue ();
		left2.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("EconomicBalance.basio") + " :", 80));
		basio = new JTextField (10);
		l2.add (basio);
		l2.addGlue ();
		left2.add (l2);

		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("EconomicBalance.tir") + " :", 80));
		tir = new JTextField (10);
		l3.add (tir);
		l3.addGlue ();
		left2.add (l3);
		left2.addStrut0 ();

		LinePanel l6 = new LinePanel ();
		l6.add (new JWidthLabel (Translator.swap ("EconomicBalance.ace") + " :", 80));
		ace = new JTextField (10);
		l6.add (ace);
		l6.addGlue ();
		left2.add (l6);
		left2.addStrut0 ();

		JPanel right2 = new JPanel (new BorderLayout ());

		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("EconomicBalance.actualizationRate") + " :", 80));
		actualizationRate = new JTextField (10);
		l4.add (actualizationRate);
		l4.addGlue ();
		LinePanel l5 = new LinePanel ();
		l5.add (new JWidthLabel (Translator.swap ("EconomicBalance.stratingDate") + " :", 80));
		startingDate = new JTextField (10);
		l5.add (startingDate);
		l5.addGlue ();
		ColumnPanel aux1 = new ColumnPanel ();
		aux1.add (l4);
		aux1.add (l5);
		right2.add (aux1, BorderLayout.NORTH);

		LinePanel aux2 = new LinePanel ();
		aux2.add (left2);
		aux2.add (right2);
		aux2.addStrut0 ();

		part2.add (aux2, BorderLayout.EAST);

		// 3. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		calculate = new JButton (Translator.swap ("EconomicBalance.calculate"));
		calculate.addActionListener (this);
		close = new JButton (Translator.swap ("Shared.close"));
		close.addActionListener (this);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (calculate);
		pControl.add (close);
		pControl.add (help);

		// Set close as default (see AmapDialog)
		close.setDefaultCapable (true);
		getRootPane ().setDefaultButton (calculate);

		// Layout parts
		mainPanel.add (split, BorderLayout.CENTER);
		mainPanel.add (part2, BorderLayout.SOUTH);

		getContentPane ().add (mainPanel, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseClicked (MouseEvent mouseEvent) {
	}

	/**
	 * From MouseListener interface.
	 */
	public void mousePressed (MouseEvent m) {
		Object obj = m.getSource ();
		if (obj instanceof JTable) {
			JTable table = (JTable) obj;

			// if (m.isPopupTrigger ()) { // fc - failed under windows
			if ((m.getModifiers () & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				int x = m.getX ();
				int y = m.getY ();
				int row = table.rowAtPoint (new Point (x, y));

				// Ensure some selection was made
				if (!table.isRowSelected (row)) {
					table.getSelectionModel ().setSelectionInterval (row, row);
				}

				if (table.equals (table1)) {
					int numRow = table1.convertRowIndexToModel (table1.getSelectedRow ());

					boolean economicInterventionLine = false;
					Step stp = (Step) table1.getModel ().getValueAt (numRow, TABLE1_STEP);

					// ~ GStand std = step.getStand ();
					String line = stp.getReason ();
					if (line.indexOf ("className=\"EconomicIntervention\"") != -1) {
						economicInterventionLine = true;
					}

					// create the list of candidate dates for
					// "Insert intervention before"
					Set candidateDates = new TreeSet ();
					if (numRow == 0) {
						candidateDates.add ("" + stp.getScene ().getDate ()); // before
																				// root
																				// :
																				// no
																				// alternative
					} else {
						int prevRow = numRow - 1;
						Step prevStep = (Step) table1.getModel ().getValueAt (prevRow, TABLE1_STEP);
						Step s = stp;
						while (s != null && s != prevStep) {
							candidateDates.add ("" + s.getScene ().getDate ()); // if
																				// 2
																				// steps
																				// with
																				// same
																				// date,
																				// no
																				// dupplicate
																				// (Set)
							s = (Step) s.getFather ();
						}
					}

					JPopupMenu popup = new EconomicTable1Popup (table, this, economicInterventionLine, none,
							new Vector (expenseFunctions.keySet ()), new Vector (incomeFunctions.keySet ()),
							candidateDates);
					popup.show (m.getComponent (), m.getX (), m.getY ());

				} else {
					// int numRow = table2.getSelectedRow ();
					JPopupMenu popup = new EconomicTable2Popup (table, this);
					popup.show (m.getComponent (), m.getX (), m.getY ());

				}
			}
		}

		// ~ if (m.isControlDown ()) {
		// ~ }
	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseReleased (MouseEvent mouseEvent) {
	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseEntered (MouseEvent mouseEvent) {
	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseExited (MouseEvent mouseEvent) {
	}

}
