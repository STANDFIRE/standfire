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
package capsis.extension.economicfunction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.extensiontype.EconomicFunction;
import capsis.gui.MainFrame;
import capsis.kernel.PathManager;
import capsis.lib.economics.CommonEconFunctions;
import capsis.util.JSmartFileChooser;


/**
 * This dialog box is used to set ExpensePerHectare parameters in interactive context.
 *
 * @author C. Orazio - january 2003
 */
public class IncomePerDiameterRangeDialog extends AmapDialog implements ActionListener {

	private SortedMap downDiameterAndPrice;
	private Vector columnNames;
	private Vector row;
	private Vector tableRows;
	private JTable table;
	private JButton saveTable;
	private JButton loadTable;
	private String fileName;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	private static final String separator = EconomicFunction.separator;

	private static final int DIAMETER = 0;
	private static final int PRICE = 1;


	public IncomePerDiameterRangeDialog () {
		super ();
		createUI ();
		setTitle (Translator.swap ("IncomePerDiameterRangeDialog"));

		
		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();
		fileName="emptyfile";

	}

	/**
	 *
	 */
	public SortedMap getValue () {
		downDiameterAndPrice = new TreeMap();
		TableModel t = table.getModel ();
		int nRow = t.getRowCount ();
		for (int i = 0; i < nRow; i++) {
			String diametre = (String) t.getValueAt (i, DIAMETER);
			String price = (String) t.getValueAt (i, PRICE);
			if (Check.isDouble(diametre) && Check.isDouble(price)){
				//System.out.println ("IncomePerDiameterRangeData : "+ diametre+";"+price);
				downDiameterAndPrice.put (new Double(diametre),new Double(price));

			}
		}
		return downDiameterAndPrice;
	}

	//
	// Action on ok button.
	//
	private void okAction () {

		// Checks...
		downDiameterAndPrice = new TreeMap();
		TableModel t = table.getModel ();
		int nRow = t.getRowCount ();
		for (int i = 0; i < nRow; i++) {
			String diametre = (String) t.getValueAt (i, DIAMETER);
			String price = (String) t.getValueAt (i, PRICE);
			if (Check.isDouble(diametre) && Check.isDouble(price)){
				//System.out.println ("IncomePerDiameterRangeData : "+ (new Double(t.getValueAt (i, DIAMETER).toString())+";"+new Double(t.getValueAt (i, PRICE).toString())));
				downDiameterAndPrice.put (new Double(diametre),new Double(price));
			}
		}
		if (downDiameterAndPrice.isEmpty ()) {
			MessageDialog.print (this, Translator.swap ("IncomePerDiameterRangeDialog.youMustProvideData"));
			return;
		}

		setValidDialog (true);
	}

	//
	// Action on cancel button.
	//
	private void cancelAction () {setValidDialog (false);}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		fileName="";
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			cancelAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
		if (evt.getSource ().equals (saveTable)) {
		//if (fileNameTable2.getText()==null || fileNameTable2.getText().equals("") || !Check.isFile(fileNameTable2.getText())){

			fileName=browseAction ( Translator.swap ("IncomePerDiameterRangeDialog.saveInATextFile"));
		//	}
			System.out.println ("fileName: "+fileName);
			try{
				saveTableInAFile (fileName);
			} catch (Exception e) {
					Log.println (Log.ERROR, "IncomePerDiameterRangeDialog.actionPerformed ().SAveInATextFile",
							"Error while saving table into text file due to exception ", e);
					MessageDialog.print (this, Translator.swap ("IncomePerDiameterRangeDialog.errorWhileSavingIntoTextFileTable2"));
			}
			//hasbeenmodified = true;
		}

		if (evt.getSource ().equals (loadTable)) {
			//if (fileNameTable2.getText()==null || fileNameTable2.getText().equals("")){
				fileName=browseAction (Translator.swap ("IncomePerDiameterRangeDialog.loadFromATextFile"));
			//}
			try{
				readTableInFile (fileName);
			} catch (Exception e) {
					Log.println (Log.ERROR, "IncomePerDiameterRangeDialog.actionPerformed ()",
							"Error while loading text file into table 2 due to exception ", e);
					MessageDialog.print (this, Translator.swap ("IncomePerDiameterRangeDialog.errorWhileLoadingFromTextFileTable"));
			}
			//hasbeenmodified = true;
		}
	}

	/**
	* select file dialog box from coligny
	*/
	private String browseAction (String label) {
			// FileName ? -> get it with a JFileChooser
			String f = "";
			String defaultExportPath = System.getProperty("capsis.export.path");
			if ((defaultExportPath == null) || defaultExportPath.equals ("")) {
				defaultExportPath = PathManager.getInstallDir();
			}

			JFileChooser chooser = new JSmartFileChooser (label,Translator.swap ("Shared.select"), defaultExportPath);

			int returnVal = chooser.showDialog (MainFrame.getInstance (),null);    // null : approveButton text was already set

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Settings.setProperty ("capsis.export.path", chooser.getSelectedFile ().toString ());
				f = chooser.getSelectedFile ().toString ();
			}
				//System.out.println ("fileName f: "+f);
			//} else {
			//	return;        // cancel on file chooser -> do nothing
			//}
			return f;
		}

		/**
		* Save table in a text file
		* @Version 2.0
		*/
		public void saveTableInAFile (String fileName) throws Exception {
			if (! Check.isFile(fileName)){
				try {
					CommonEconFunctions.createEconParametersFile (fileName);
				} catch (Exception e) {
				throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
				}
			}
			// Write the lines content
			Vector out = new Vector();
			out.add("diametre"+separator+"price");
			//downDiameterAndPrice = new TreeMap();
			TableModel t = table.getModel ();
			int nRow = t.getRowCount ();
			for (int i = 0; i < nRow; i++) {
				String diametre = (String) t.getValueAt (i, DIAMETER);
				String price = (String) t.getValueAt (i, PRICE);
				if (Check.isDouble(diametre) && Check.isDouble(price)){
					//System.out.println ("IncomePerDiameterRangeData : "+ diametre+";"+price);
					out.add (diametre+separator+price+separator);
				}
			}
			//out.add(emptyRow2);
			CommonEconFunctions.updateEconParametersFile (fileName, "IncomePerDiameterRangeDialog", "PriceTable", out);
	    }


	/**
	* Load text file in the table
	* @Version 1.2
	*/
	public void readTableInFile (String fileName) throws Exception {
			Vector fileContent = new Vector();
			if (! Check.isFile(fileName)){
				throw new Exception ("File name "+fileName+" causes error trying to read it in readtableInFile class");
			}
			fileContent = CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomePerDiameterRangeDialog", "PriceTable");
			if  (fileContent.size()!=0){
				tableRows.clear();
				for (int j=1; j<= fileContent.size()-1; j++){
						Vector row = new Vector ();
						System.out.println ("Adding to table: "+fileContent.get(j).toString());
						for (int i=1; i<=2 ;i++){  // the number comes from save in text file
							System.out.println ("Extract " +i+" value: "+CommonEconFunctions.getValueFromString(fileContent.get(j).toString(), i, separator));
							row.add (CommonEconFunctions.getValueFromString(fileContent.get(j).toString(), i, separator));
						}
				tableRows.add (row);
				table.revalidate ();
				}
			} else {
				Log.println (Log.WARNING, "IncomePerDiameterRangeDialog.loadingfromtext ()",
						"No compatible data in text file "+ fileName);
			}
	}


	/**
	 * Data in the table changes.
	 */
	/*public void tableChanged(TableModelEvent e) {
	        int r = e.getFirstRow();
	        int column = e.getColumn();
	        TableModel model = (TableModel)e.getSource();
	        String columnName = model.getColumnName(column);
	        Object data = model.getValueAt(r, column);
			if (!Check.isDouble(data.toString())){
				new DGeneral (Translator.swap ("IncomePerVolumeDialog.Alert"),new JLabel(Translator.swap ("IncomePerVolumeDialog.DataMustBeDouble")));
			}
			if (e.getType()==e.UPDATE && r==((TableModel)table).getRowCount()-1){
				tableRows.add (row.clone());
			}
 	}
	*/
	//
	// Create the dialog box user interface.
	//
	private void createUI () {

		// 1. Util panel
		//Create vector for titles
		columnNames = new Vector();
		columnNames.add (Translator.swap ("IncomePerDiameterRangeDialog.downDiameter"));
		columnNames.add (Translator.swap ("IncomePerDiameterRangeDialog.price"));

		// create vector for data
		row = new Vector();
		row.add ("0");
		row.add ("0");

		//l1.add (new JLabel (Translator.swap ("IncomePerVolumeDialog.value")+" :"));
		tableRows = new Vector();
		tableRows.add (row);
		row = new Vector();
		row.add (" ");
		row.add (" ");

		for (int i=1;i<=30;i++){
			tableRows.add (row.clone());
		}
		table = new JTable (tableRows, columnNames);
		//table.getModel().addTableModelListener(this);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(70, 70));
		//l1.addGlue ();
		//panel.add (l1);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (scrollPane, BorderLayout.CENTER);

		// 2. control panel (ok cancel help);
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

		//0.save and load buttons
		LinePanel l0 = new LinePanel ();
		saveTable = new JButton (new ImageIcon("capsis/extension/modeltool/economicbalance/images/save.gif"));
		saveTable.addActionListener (this);
		loadTable = new JButton (new ImageIcon("capsis/extension/modeltool/economicbalance/images/open.gif"));
		loadTable.addActionListener (this);
		//fileNameTable2 = new JTextField (10);
		//l0.add (fileNameTable2);
		l0.add (loadTable);
		l0.add (saveTable);
		getContentPane ().add (l0, BorderLayout.NORTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

	}

}

