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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

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
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.economics2.EconomicModel;
import capsis.lib.economics2.EconomicOperation;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicScene;
import capsis.lib.economics2.EconomicSettings;
import capsis.lib.economics2.EconomicSettingsLoader;
import capsis.util.JSmartFileChooser;

/**
 * Configure the economic Scenario
 * 
 * @author Gauthier Ligot - 24 june 2016
 */
public class EconomicModelTool extends DialogModelTool implements ActionListener {

	static {
		Translator.addBundle ("capsis.lib.economics2.gui.EconomicTranslator");
	}

	static public final String AUTHOR = "Gauthier Ligot";
	static public final String VERSION = "0";

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
	
	private JRadioButton dateBeforeIntervention;
	private JRadioButton dateAfterIntervention;


	//first table
	private EconomicOperationTable manualOperationTableModel;
	private JTable manualOperationTable;
	
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
	 */
	public void actionPerformed (ActionEvent evt) {

		if (evt.getSource ().equals (ok)) {
			
			EconomicScenario es = ((EconomicModel) this.model).getEconomicScenario();
			if(es==null){es = new EconomicScenario(this.model.getProject(), (EconomicModel) this.model);}
			
			//reset the economic scenario
			es = es.getCleanCopy();
			
			// set or correct the settings of the economic scenario
			EconomicSettings settings = es.getSettings();
			settings.setFileName(inputEconomicFile.getText ());
			
			//so far the file is loaded
			//TODO operations and price list should be loaded from the Gui Interface!
			try {
				es.loadSettingsFromFile();
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
			}
			//correct the discount rate and land as it appears in the Gui interface
			settings.setDiscountRate(Double.parseDouble(discountRate.getText().trim()));
			settings.setLand(Double.parseDouble(land.getText().trim()));
			
			//check that some valid dates have been written...
			//TODO improve the checks
			if(intermediateDate.getText () == null || intermediateDate.getText ().equals ("")){
				es.evaluate(Integer.parseInt(firstDate.getText().trim()), Integer.parseInt(lastDate.getText().trim()));
			}else{
				es.evaluate(Integer.parseInt(firstDate.getText().trim()), Integer.parseInt(lastDate.getText().trim()), Integer.parseInt(intermediateDate.getText().trim()), dateAfterIntervention.isSelected() );
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
				
				
//				EconomicScenario es = ((EconomicModel) model).getEconomicScenario();
//				if(es == null){es = new EconomicScenario(this.model.getProject(), (EconomicModel) this.model);}
				
				EconomicSettingsLoader csl;
				csl = new EconomicSettingsLoader (inputEconomicFile.getText());
				tmpSettings = new EconomicSettings();
				csl.loadSettings(tmpSettings);
				
				//set the value found in the text file in the corresponding jtextfield
				discountRate.setText(Double.toString(tmpSettings.getDiscountRate()));
				land.setText( Double.toString(tmpSettings.getLand()));
				
				//report all economic operation recorded in the economic file
				List<EconomicOperation> manualOps = tmpSettings.getOperations();
				updateOperationTable(manualOps, Integer.parseInt(firstDate.getText().trim()), Integer.parseInt(lastDate.getText().trim()));

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



	/**
	 * User interface definition.
	 * To be completed.....
	 */
	private void createUI () {
		ColumnPanel g1 = new ColumnPanel (); 
		//------------------------------------------------------------//
		// first Step = define scenario dates
		LinePanel datePanel = new LinePanel ();
		
		LinePanel l01 = new LinePanel ();
		l01.add (new JWidthLabel (Translator.swap ("firstDate") + " :", 80));
		firstDate = new JTextField (10);
		l01.add (firstDate);
		l01.addGlue ();
		datePanel.add (l01);

		LinePanel l02 = new LinePanel ();
		l02.add (new JWidthLabel (Translator.swap ("intermediateDate") + " :", 80));
		intermediateDate = new JTextField (10);
		l02.add (intermediateDate);
		l02.addGlue ();
		datePanel.add (l02);

		LinePanel l03 = new LinePanel ();
		l03.add (new JWidthLabel (Translator.swap ("lastDate") + " :", 80));
		lastDate = new JTextField (10);
		l03.add (lastDate);
		l03.addGlue ();
		datePanel.add (l03);
		g1.add(datePanel);
		g1.addStrut0();
		
		LinePanel l04 = new LinePanel();
		ButtonGroup bg = new ButtonGroup();
		dateBeforeIntervention = new JRadioButton(Translator.swap ("EconomicModelTool.dateBeforeIntervention"));
		dateAfterIntervention = new JRadioButton(Translator.swap ("EconomicModelTool.dateAfterIntervention"));
		dateAfterIntervention.setSelected(true); //default
		bg.add(dateBeforeIntervention);
		bg.add(dateAfterIntervention);
		l04.add(dateBeforeIntervention);
		l04.add(dateAfterIntervention);
		g1.add(l04);
		g1.addStrut0();
		
		//------------------------------------------------------------//
		//input economic file
		LinePanel inputEconomicFilePanel = new LinePanel ();
		load = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/load.gif"));
		load.addActionListener (this);
		browse = new JButton (new ImageIcon ("src/capsis/lib/economics2/gui/images/open.gif"));
		browse.addActionListener (this);
		inputEconomicFile = new JTextField (10);
		inputEconomicFilePanel.add (inputEconomicFile);
		inputEconomicFilePanel.add (browse);
		inputEconomicFilePanel.add (load);
		g1.add(inputEconomicFilePanel);

		//------------------------------------------------------------//
		ColumnPanel g2 = new ColumnPanel();
		// second bloc of JTextField
		LinePanel DiscountRateAndLand = new LinePanel (); 

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
		writeEmptyManualOperationTable();		
		JScrollPane scrollPane = new JScrollPane(manualOperationTable);
//		manualOperationTable.setFillsViewportHeight(true);
		g2.add(scrollPane);
		//------------------------------------------------------------//

		
		// Add default values
		// default if nothing found
 		String inputEconomicFileDefault = "D:\\workspace\\capsis4\\data\\samsara2\\economics2\\economicOperations2.txt";
		double discountRateDefault = 0.02;
		double landDefault = -1;

		int firstDateDefault = model.getProject().getStepsFromRoot(step).get(0).getScene().getDate();
		int intermediateDateDefault = firstDateDefault;
		int LastDateDefault = model.getProject().getStepsFromRoot(step).get(model.getProject().getStepsFromRoot(step).size()-1).getScene().getDate();
		
		
		// from file
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
	
	private void writeEmptyManualOperationTable (){
		Vector table = new Vector();
		Vector emptyRow = new Vector();
		for (int i = 1; i <= 6; i++) {
			emptyRow.add("0");
		}
		table.add (emptyRow);
		
		manualOperationTableModel = new EconomicOperationTable();
		manualOperationTableModel.setHeader(headerOfManualOperationTable());
		manualOperationTable = new JTable (manualOperationTableModel);
	}
	
	private String[] headerOfManualOperationTable (){
		String[] colNames = new String[]{ Translator.swap ("dates") ,
			Translator.swap ("trigger"),
	//		colNames.add (Translator.swap ("frequency"));
			Translator.swap ("label"),
			Translator.swap ("type"),
			Translator.swap ("income"),
			Translator.swap ("price")
		};
		return colNames;
	}
	
	private void clearOperationTable(){
		Object[][] table2 = new Object[0][6]; 
		manualOperationTableModel.setData(table2);
	}
	
	private void updateOperationTable (List<EconomicOperation> ops, int firstDate, int lastDate){
		
		Vector table = new Vector();
		
		//full fill table		
		for(EconomicOperation op : ops){
			op.computeValidityDates(firstDate, lastDate);
			for(int date : op.getValidityDates()){ //as-t'on vraimment chaque fois le validity date de défini (annual, on intervention...)
				Vector row = new Vector();
				row.add(date);
				row.add(op.getLabel());
				row.add(op.getTrigger());
				row.add(op.getType());
				row.add(op.isIncome());
				row.add(op.getPrice());
				
				table.add(row);
			}
		}
		
		Object[][] table2 = new Object[table.size()][6]; 
		
		for(int i = 0 ; i<table.size(); i++){
			for(int j= 0 ;j<6;j++){
				Vector row = (Vector) table.get(i); 
				table2[i][j] = row.get(j);
			}
		}
		
		manualOperationTableModel.setData(table2);
	}



}
