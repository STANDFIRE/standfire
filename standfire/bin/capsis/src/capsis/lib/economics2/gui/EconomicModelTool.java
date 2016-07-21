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

package capsis.lib.economics2.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

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
import capsis.commongui.util.Helper;
import capsis.extension.DialogModelTool;
import capsis.extension.modeltool.economicbalance.EconomicTable1Popup;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.economics2.EconomicCustomOperation;
import capsis.lib.economics2.EconomicModel;
import capsis.lib.economics2.EconomicOperation;
import capsis.lib.economics2.EconomicPriceRecord;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicScenario.EconomicCase;
import capsis.lib.economics2.EconomicScene;
import capsis.lib.economics2.EconomicSettings;
import capsis.lib.economics2.EconomicSettingsLoader;
import capsis.util.JSmartFileChooser;

/**
 * Configure the economic Scenario
 * 
 * @author Gauthier Ligot - 24 june 2016
 */
public class EconomicModelTool extends DialogModelTool implements ActionListener, MouseListener, TableModelListener {

	static {
		Translator.addBundle ("capsis.lib.economics2.gui.EconomicTranslator");
	}

	static public final String AUTHOR = "Gauthier Ligot";
	static public final String VERSION = "1.0";

	//first combo box
	private JComboBox economicCase; 

	//most upper line
	private JTextField inputEconomicFile;
	private JButton browse;
	private JButton load;

	//first text fields
	private JTextField discountRate;
	private JTextField land;
	private JTextField firstDate;
	private JTextField intermediateDate;
	private JTextField lastDate;

	//first table
	private EconomicOperationTableModel manualOperationTableModel;
	private JTable manualOperationTable;

	private JButton manualOpAdd;
	private JButton manualOpRemove;

	//second table
	private EconomicCustomOperationTableModel automaticOperationTableModel;
	private JTable automaticOperationTable;

	//third table
	private EconomicPriceTableModel priceTableModel;
	private JTable priceTable;
	private TableRowSorter<EconomicPriceTableModel> priceSorter;

	private JButton priceAdd;
	private JButton priceRemove;
	private JButton priceSort;
	
	//control button
	private JButton ok;
	private JButton close;
	private JButton help;
	private JButton save;

	private ExtensionManager extMan;

	private Step step;
	private GModel model;
	private EconomicSettings tmpSettings; //temporary settings that is not connected to the scenario

	/**
	 * Default constructor.
	 */
	public EconomicModelTool () {
		super ();
	}

	@Override
	public void init (GModel m, Step s) {

		try {
			step = s;
			model = m;

			setTitle (Translator.swap ("EconomicModelTool") + " - " + VERSION + " - " + step.getCaption ());
			extMan = CapsisExtensionManager.getInstance ();

			createUI ();

			pack (); // sets the size
			setVisible (true);
			setModal (false);
			setResizable (true);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "EconomicModelTool.c ()", exc.toString (), exc);
		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {

			if (!(referent instanceof GModel)) { return false; }
			if (!(referent instanceof EconomicModel)) { return false; }

			GModel m = (GModel) referent;
			Step root = (Step) m.getProject ().getRoot ();
			GScene s = root.getScene ();

			if (!(s instanceof EconomicScene)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "EconomicModelTool.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
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
	 * Called on window closing (escape, title bar or close button) 
	 */
	public void closeAction () {
		try {
			dispose ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap("EconomicModelTool.canNotCloseDueToExceptionSeeLog"), e);
			return;
		}
	}


	/**
	 * From ActionListener interface.
	 * @TODO
	 */
	public void actionPerformed (ActionEvent evt) {

		// events from the popup menu (right click on table)
		if (evt.getSource () instanceof JMenuItem){
			// TODO
//			JMenuItem item = (JMenuItem) evt.getSource ();
//			
//			int code = item.getMnemonic ();
//
//			if (code == EconomicTablePopUp.INSERT_LINE_AFTER) { // table 1
//				manualOperationTableModel.addOperation(new EconomicOperation());
//
//			}else if (code == EconomicTablePopUp.REMOVE_LINE) { 
//
//				int[] selection = manualOperationTable.getSelectedRows();
//
//				for(int i = selection.length - 1; i >= 0; i--){
//					manualOperationTableModel.removeOperation(selection[i]);
//				}
//			}
		}

		if(evt.getSource().equals(economicCase)){
			if(economicCase.getSelectedItem().equals(EconomicScenario.EconomicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE)){
				intermediateDate.setEnabled(false);
			}else if(economicCase.getSelectedItem().equals(EconomicScenario.EconomicCase.INFINITY_CYCLE_WITHOUT_LAND_OBSERVATION)){
				intermediateDate.setEnabled(false);
				land.setEnabled(false);
			}else if(economicCase.getSelectedItem().equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE)){
				intermediateDate.setEnabled(true);
				land.setEnabled(false);
			}else if(economicCase.getSelectedItem().equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD)){
				intermediateDate.setEnabled(false);
				land.setEnabled(false);
			}
		}
		
		// evt are buttons
		if(evt.getSource().equals(manualOpAdd)){
			manualOperationTableModel.addOperation(new EconomicOperation());
		}

		if(evt.getSource().equals(manualOpRemove)){
			int[] selection = manualOperationTable.getSelectedRows();
			for(int i = selection.length - 1; i >= 0; i--){
				manualOperationTableModel.removeOperation(selection[i]);
			}
		}

		if(evt.getSource().equals(priceAdd)){
			priceTableModel.addRecords(new EconomicPriceRecord());
		}

		if(evt.getSource().equals(priceRemove)){
			int[] selection = priceTable.getSelectedRows();
			for(int i = selection.length - 1; i >= 0; i--){
				priceTableModel.removeRecord(selection[i]);
			}
		}
		
		if(evt.getSource().equals(priceSort)){
			priceSorter.sort();
		}

		// control buttons
		if (evt.getSource ().equals (ok)) {

			//create a new scenario
			EconomicScenario es = new EconomicScenario(this.model.getProject(), (EconomicModel) this.model);

			// set or correct the settings of the economic scenario
			EconomicSettings settings = es.getSettings();
			
			// set the fileName even though this file might have been corrected in the gui
			settings.setFileName(inputEconomicFile.getText ());

			// load the discount rate and land as it appears in the Gui interface
			if(!discountRate.getText().equals("") && Double.parseDouble(discountRate.getText().trim()) > 0){
				settings.setDiscountRate(Double.parseDouble(discountRate.getText().trim()));
			}
			if(!land.getText().equals("") && Double.parseDouble(land.getText().trim()) > 0){
				settings.setLand(Double.parseDouble(land.getText().trim()));
			}
			
			//load user-defined operations in the settings			
			settings.setOperations(manualOperationTableModel.getOperations());

			//load model defined operations in the scenario
			es.setOperations(automaticOperationTableModel.getOperations());
			
			
			// load the user-modified price list
			List<EconomicPriceRecord> priceList = priceTableModel.getPriceList();
			for(EconomicPriceRecord p : priceList){
				settings.addPrice(p.dbh, p.price, p.species);
			}
			
			// load and check the defined period (type and dates)
			//check that some valid dates have been written...
			//TODO improve the checks
			if(intermediateDate.getText () == null || intermediateDate.getText ().equals ("")){
				es.evaluate(Integer.parseInt(firstDate.getText().trim()), Integer.parseInt(lastDate.getText().trim()));
			}else{
				es.evaluate(Integer.parseInt(firstDate.getText().trim()), 
						Integer.parseInt(lastDate.getText().trim()), 
						Integer.parseInt(intermediateDate.getText().trim()), 
						(EconomicCase) economicCase.getSelectedItem());
			}
			
			// Store user values for next opening
			Settings.setProperty("EconomicModelTool.inputEconomicFile", inputEconomicFile.getText ());
			Settings.setProperty("EconomicModelTool.discountRate", discountRate.getText().trim());
			Settings.setProperty("EconomicModelTool.land", land.getText().trim());
			Settings.setProperty("EconomicModelTool.firstDate", firstDate.getText().trim());
			Settings.setProperty("EconomicModelTool.intermediateDate", intermediateDate.getText().trim());
			Settings.setProperty("EconomicModelTool.lastDate", lastDate.getText().trim());


			//open EconomicTextViewer
			//???

			closeAction ();
		}

		if (evt.getSource ().equals (browse)) {
			browseAction (inputEconomicFile, Translator.swap ("defineInputFile"));
		}

		if (evt.getSource ().equals (close)) {
			closeAction ();
		}

		if (evt.getSource ().equals (save)) {
			//todo
		}

		if (evt.getSource ().equals (load)) {

			try {

				EconomicSettingsLoader csl;
				csl = new EconomicSettingsLoader (inputEconomicFile.getText());
				tmpSettings = new EconomicSettings();
				csl.loadSettings(tmpSettings);

				//set the value found in the text file in the corresponding jtextfield
				discountRate.setText(Double.toString(tmpSettings.getDiscountRate()));
				land.setText( Double.toString(tmpSettings.getLand()));

				//report all economic operation recorded in the economic file
				List<EconomicOperation> manualOps = tmpSettings.getOperations();

				updateManualOperationTable(manualOps);

				EconomicScenario es = ((EconomicModel) model).getEconomicScenario();
				if(es != null){
					List<EconomicOperation> cops = es.getCustomOperations();
					updateAutomaticOperationTable(cops);
				}

				// this need a list of species
				tmpSettings.createSpeciesList((EconomicScene) this.model.getProject().getRoot().getScene()); 
				updatePriceTable(tmpSettings.getPriceRecords());

			} catch (Exception e) {
				Log.println (Log.ERROR, "EconomicModelTool.actionPerformed ()",
						"Error while loading text file into economics2 scenario ", e);
				MessageDialog.print (this, Translator.swap ("EconomicModelTool.errorWhileLoadingSettings"), e);
			}
		}

		if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}

	}


	/**
	 * Called on Escape. Redefinition of method in AmapDialog : ask for user
	 * confirmation.
	 */
	@Override
	protected void escapePressed () {
		if (Question.ask (this, Translator.swap ("EconomicModelTool.confirm"), Translator
				.swap ("EconomicModelTool.confirmClose"))) {
			closeAction ();
		}
	}

	public Step getStep () {
		return step;
	}


	//	private List<EconomicOperation> createOperationList(){
	//		EconomicTableModel model = (EconomicTableModel) manualOperationTable.getModel();
	//		List<EconomicOperation> operations = new ArrayList<EconomicOperation>();
	//		for (int r = 0; r < model.getRowCount(); r++){
	//			EconomicOperation.Trigger trigger = (EconomicOperation.Trigger) model.getValueAt(r, 6);
	//			
	//			EconomicOperation op = new EconomicOperation((String) model.getValueAt(r, 4), //label
	//					(EconomicOperation.Type) model.getValueAt(r, 5), //type, 
	//					trigger, //trigger, 
	//					(boolean) model.getValueAt(r, 7), //income, 
	//					(double) model.getValueAt(r, 8) //price);
	//					); 
	//		// set the dates and frequency
	//		if(trigger.equals(EconomicOperation.Trigger.ON_DATE)){
	//			op.setGivenDate((int) model.getValueAt(r, 0)); 
	//		}else if(trigger.equals(EconomicOperation.Trigger.ON_FREQUENCY)){
	//			op.setGivenStartDate((int) model.getValueAt(r, 1)); 
	//			op.setGivenEndDate((int) model.getValueAt(r, 2)); 
	//			op.setGivenFrequency((int) model.getValueAt(r, 3)); 
	//		}else {
	//			// do nothing so far
	//		}
	//			
	//			operations.add(op);
	//		}
	//		return operations;
	//		
	//	}

	/**
	 * User interface definition.
	 * To be completed.....
	 */
	private void createUI () {
		ColumnPanel g1 = new ColumnPanel (Translator.swap ("EconomicModelTool.scenarioDates")); 
		//------------------------------------------------------------//
		// first Step = define economic case and scenario dates

		//Create the combo box, select item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		economicCase = new JComboBox(EconomicCase.values());
		economicCase.setSelectedIndex(0);
		economicCase.addActionListener(this);
		g1.add(economicCase);
		g1.addStrut0();

		LinePanel datePanel = new LinePanel ();

		LinePanel l01 = new LinePanel ();
		l01.add (new JWidthLabel (Translator.swap ("firstDate") + " :", 80));
		firstDate = new JTextField (10);
		firstDate.setToolTipText(Translator.swap ("EconomicModelTool.dateExplanation1"));
		l01.add (firstDate);
		l01.addGlue ();
		datePanel.add (l01);

		LinePanel l02 = new LinePanel ();
		l02.add (new JWidthLabel (Translator.swap ("intermediateDate") + " :", 80));
		intermediateDate = new JTextField (10);
		intermediateDate.setToolTipText(Translator.swap ("EconomicModelTool.dateExplanation2"));
		l02.add (intermediateDate);
		l02.addGlue ();
		datePanel.add (l02);

		LinePanel l03 = new LinePanel ();
		l03.add (new JWidthLabel (Translator.swap ("lastDate") + " :", 80));
		lastDate = new JTextField (10);
		lastDate.setToolTipText(Translator.swap ("EconomicModelTool.dateExplanation3"));
		l03.add (lastDate);
		l03.addGlue ();
		datePanel.add (l03);
		g1.add(datePanel);
		g1.addStrut0();

		//------------------------------------------------------------//
		
		ColumnPanel g2 = new ColumnPanel();
		
		//input economic file
		LinePanel filePanel = new LinePanel (Translator.swap ("EconomicModelTool.scenarioFile"));
		load = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/load.gif"));
		load.addActionListener (this);
		browse = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/open.gif"));
		browse.addActionListener (this);
		inputEconomicFile = new JTextField (10);
		filePanel.add (inputEconomicFile);
		filePanel.add (browse);
		filePanel.add (load);
		g2.add(filePanel);
		g2.addStrut0();
		
		//------------------------------------------------------------//
		
		LinePanel DiscountRateAndLand = new LinePanel (Translator.swap ("EconomicModelTool.scenarioDiscount")); 

		LinePanel l21 = new LinePanel ();
		l21.add (new JWidthLabel (Translator.swap ("discountRate") + " :", 80));
		discountRate = new JTextField (10);
		l21.add (discountRate);
		l21.addGlue ();
		DiscountRateAndLand.add (l21);

		LinePanel l22 = new LinePanel ();
		l22.add (new JWidthLabel (Translator.swap ("land") + " :", 80));
		land = new JTextField (10);
		l22.add (land);
		l22.addGlue ();
		DiscountRateAndLand.add (l22);
		g2.add(DiscountRateAndLand);

		//------------------------------------------------------------//
		// table with operations from file and that can manually be corrected
		createManualOperationTable(new ArrayList<EconomicOperation>());		

		ColumnPanel manualOpPanel = new ColumnPanel(Translator.swap ("EconomicModelTool.manualOperation"));

		JScrollPane manualOpScrollPane = new JScrollPane (manualOperationTable);

		LinePanel manualOpBoutonPanel = new LinePanel();
		manualOpAdd = new JButton(Translator.swap ("EconomicModelTool.addAction"));
		manualOpAdd.addActionListener(this);
		manualOpRemove = new JButton(Translator.swap ("EconomicModelTool.removeAction"));
		manualOpRemove.addActionListener(this);

		manualOpBoutonPanel.add(manualOpAdd);
		manualOpBoutonPanel.add(manualOpRemove);

		manualOpPanel.add(manualOpScrollPane);
		manualOpPanel.add(manualOpBoutonPanel);

		g2.add(manualOpPanel);


		//-------------------------------------------//
		// table with model-defined operations from model
		createAutomaticOperationTable(new ArrayList<EconomicOperation>());
		
		ColumnPanel autoOpPanel = new ColumnPanel(Translator.swap ("EconomicModelTool.modelOperation"));

		JScrollPane autoScrollPane = new JScrollPane (automaticOperationTable);
			
		autoOpPanel.add(autoScrollPane);
		
		g2.add(autoOpPanel);
		

		//------------------------------------------------------------//
		// table with the price by dbh and by species
		
		createPriceTable(new ArrayList<EconomicPriceRecord>());		
		
		ColumnPanel priceOpPanel = new ColumnPanel(Translator.swap ("EconomicModelTool.scenarioPrice"));

		JScrollPane priceScrollPane = new JScrollPane (priceTable);
		
		LinePanel priceBoutonPanel = new LinePanel();
		priceAdd = new JButton(Translator.swap ("EconomicModelTool.addAction"));
		priceAdd.addActionListener(this);
		priceRemove = new JButton(Translator.swap ("EconomicModelTool.removeAction"));
		priceRemove.addActionListener(this);
		priceSort = new JButton(Translator.swap ("EconomicModelTool.sortAction"));
		priceSort.addActionListener(this);

		priceBoutonPanel.add(priceAdd);
		priceBoutonPanel.add(priceRemove);
		priceBoutonPanel.add(priceSort);
		
		priceOpPanel.add(priceScrollPane);
		priceOpPanel.add(priceBoutonPanel);
		
		g2.add(priceOpPanel);
		
		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//
		// default values
		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//
		// default if nothing found
		String inputEconomicFileDefault = "D:\\workspace\\capsis4\\data\\samsara2\\economics2\\economicOperations2.txt";
		double discountRateDefault = 0.02;
		double landDefault = -1;

		int firstDateDefault = model.getProject().getStepsFromRoot(step).get(0).getScene().getDate();
		int intermediateDateDefault = firstDateDefault;
		int LastDateDefault = model.getProject().getStepsFromRoot(step).get(model.getProject().getStepsFromRoot(step).size()-1).getScene().getDate();


		// from file
		economicCase.setSelectedIndex(Settings.getProperty("EconomicModelTool.economicCase", 0)); //TODO seems not working
		inputEconomicFile.setText(""+Settings.getProperty("EconomicModelTool.inputEconomicFile", inputEconomicFileDefault));
		discountRate.setText(""+Settings.getProperty("EconomicModelTool.discountRate", discountRateDefault));
		land.setText(""+Settings.getProperty("EconomicModelTool.land", landDefault));
		//		firstDate.setText("" +  Settings.getProperty("EconomicModelTool.firstDate", firstDateDefault));
		//		intermediateDate.setText("" +  Settings.getProperty("EconomicModelTool.intermediateDate", intermediateDateDefault));
		//		lastDate.setText("" +  Settings.getProperty("EconomicModelTool.lastDate", LastDateDefault));

		firstDate.setText("" +  firstDateDefault);
		intermediateDate.setText("" +  intermediateDateDefault);
		lastDate.setText("" +  LastDateDefault);

		// from current scenario
		// not sure this is necessary

		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//
		// control buttons
		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//
		//-----------------------------------------------------------------------//

		// 3. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/ok.gif"));
		ok.addActionListener (this);

		save = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/save.gif"));
		save.addActionListener (this);

		close = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/close.gif"));
		close.addActionListener (this);

		help = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/help.gif"));
		help.addActionListener (this);

		pControl.add (ok);
		pControl.add (save);
		pControl.add (close);
		pControl.add (help);

		// Layout parts
		getContentPane ().add (g1, BorderLayout.NORTH);
		getContentPane ().add (g2, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

	}

	/** 
	 * Create the table of user-defined operations
	 * @param ops = list of operations (can be empty)
	 */
	private void createManualOperationTable (List<EconomicOperation> ops){
		manualOperationTableModel = new EconomicOperationTableModel();
		manualOperationTableModel.setData(ops);
		
		manualOperationTable = new JTable(manualOperationTableModel);

		manualOperationTable.setDefaultEditor(EconomicOperation.Type.class, new TypeCellEditor());
		manualOperationTable.setDefaultEditor(EconomicOperation.Trigger.class, new TriggerCellEditor());

		manualOperationTable.addMouseListener (this);
		//		manualOperationTable.setSelectionMode (ListSelectionModel.SINGLE_SELECTION); //default can select multiple lines 
		manualOperationTableModel.addTableModelListener (this);
	}

	/** 
	 * Create the table of model-defined operations
	 * @param ops = list of operations (can be empty)
	 */
	private void createAutomaticOperationTable (List<EconomicOperation> ops){

		automaticOperationTableModel = new EconomicCustomOperationTableModel();
		automaticOperationTableModel.setData(ops);
		
		automaticOperationTable = new JTable (automaticOperationTableModel);
		
		automaticOperationTable.setDefaultEditor(EconomicOperation.Type.class, new TypeCellEditor());
		automaticOperationTable.setDefaultEditor(EconomicOperation.Trigger.class, new TriggerCellEditor());
		
		automaticOperationTable.addMouseListener(this);
		automaticOperationTableModel.addTableModelListener(this);
	}

	/**
	 * create the table with the list of prices
	 */
	private void createPriceTable (List<EconomicPriceRecord> records){
		priceTableModel = new EconomicPriceTableModel();
		priceTableModel.setData(records);
		
		priceTable = new JTable (priceTableModel);
		
		priceTable.addMouseListener(this);
		priceTableModel.addTableModelListener(this);
		
		priceSorter = new TableRowSorter<>(priceTableModel);
		priceTable.setRowSorter(priceSorter);
		
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		 
		int columnIndexForspecies = 0;
		sortKeys.add(new RowSorter.SortKey(columnIndexForspecies, SortOrder.ASCENDING));
		 
		int columnIndexFordbh = 1;
		sortKeys.add(new RowSorter.SortKey(columnIndexFordbh, SortOrder.ASCENDING));
		 
		priceSorter.setSortKeys(sortKeys);
	}
	
	/**update the table with a list of user-defined operations
	 */
	private void updateManualOperationTable (List<EconomicOperation> ops){
		manualOperationTableModel.setData(ops);
	}

	/** update the list of model-defined operation
	 */
	private void updateAutomaticOperationTable(List<EconomicOperation> ops){
		automaticOperationTableModel.setData(ops);
	}

	/** update the entire price list
	 * 
	 */
	private void updatePriceTable(List<EconomicPriceRecord> records){
		priceTableModel.setData(records);
	}


	@Override
	public void mouseClicked(MouseEvent e) {

		Object obj = e.getSource ();
		JTable table = (JTable) obj;

		int x = e.getX ();
		int y = e.getY ();
		int row = table.rowAtPoint (new Point (x, y));


		if(e.getButton() == MouseEvent.BUTTON1) //left click
		{

		}	    
		else if(e.getButton() == MouseEvent.BUTTON3) //right click
		{
			if (table.equals (manualOperationTable)) {
				JPopupMenu popup = new EconomicTablePopUp (table, this);
				popup.show (e.getComponent (), e.getX (), e.getY ());
			
			}else if (table.equals (priceTable)) {
				JPopupMenu popup = new EconomicTablePopUp (table, this);
				popup.show (e.getComponent (), e.getX (), e.getY ());
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO FP Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO FP Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO FP Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO FP Auto-generated method stub

	}

	//tableModelListener interface

	@Override
	public void tableChanged(TableModelEvent evt) {
		if ((evt.getSource () == manualOperationTable)){
			System.out.println("tableChanged() - I detect a change in this table...");
		}	
		//		} & !(evt.getColumn () == TABLE1_EXPENSES
		//				|| evt.getColumn () == TABLE1_INCOMES || evt.getColumn () == TABLE1_BENEFIT))
		//				|| (evt.getSource () == tableModel2 & !(evt.getColumn () == TABLE2_BENEFIT))) {
		//			System.out.println ("INFO EconomicBalance Evènement dans la table " + evt.getSource () + " at column "
		//					+ table1.getColumnName (evt.getColumn ()) + " de Type: " + evt.getType ());
		//			hasbeenmodified = true;
		//			if (evt.getSource () == tableModel2) {
		//				calculateAction ();
		//			}

	}

	//inner class
	public class TriggerCellEditor extends DefaultCellEditor {
		public TriggerCellEditor() {
			super(new JComboBox(EconomicOperation.Trigger.values()));
		}
	}
	public class TypeCellEditor extends DefaultCellEditor {
		public TypeCellEditor() {
			super(new JComboBox(EconomicOperation.Type.values()));
		}
	}

}
