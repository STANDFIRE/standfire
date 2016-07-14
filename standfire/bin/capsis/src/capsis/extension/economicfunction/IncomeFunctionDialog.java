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
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.extensiontype.EconomicFunction;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.PathManager;
import capsis.lib.economics.CommonEconFunctions;
import capsis.util.JSmartFileChooser;
import capsis.util.methodprovider.NProvider;


/**
 * This dialog box is used to set ExpensePerHectare parameters in interactive context.
 *
 * @author C. Orazio - january 2003
 */
public class IncomeFunctionDialog extends AmapDialog implements ActionListener {

	
	
	
	
	protected LinePanel top = new LinePanel ();;
	protected JLabel labelTop = new JLabel();
	protected JButton load = new JButton(new ImageIcon("capsis/extension/modeltool/economicbalance/images/open.gif"));
	protected JButton save = new JButton(new ImageIcon("capsis/extension/modeltool/economicbalance/images/save.gif"));
	
	//protected Panel leftSideFunction = new Panel(new LeftSideLayout (LeftSideLayout.TOP, 5, false));
	protected ColumnPanel leftSideFunction = new ColumnPanel();
	//protected GroupBoxPanel groupBoxFunction = new GroupBoxPanel (Translator.swap ("IncomeFunctionDialog.functions"), new BorderLayout());
	protected ButtonGroup functions = new ButtonGroup();
	protected JRadioButton type1 = new JRadioButton();
	protected JRadioButton type2 = new JRadioButton();
	
	protected ColumnPanel leftSideTree = new ColumnPanel();
	//protected Panel leftSideTree = new Panel(new LeftSideLayout (LeftSideLayout.TOP, 5, false));
	//protected GroupBoxPanel groupBoxTree = new GroupBoxPanel (Translator.swap ("IncomeFunctionDialog.trees"), new BorderLayout());
	protected JCheckBox cut = new JCheckBox();
	protected JCheckBox dead = new JCheckBox();

	protected LinePanel bottom = new LinePanel ();
	protected JButton ok= new JButton(Translator.swap ("Shared.ok"));
	protected JButton cancel= new JButton(Translator.swap ("Shared.cancel"));
	protected JButton help= new JButton(Translator.swap ("Shared.help"));
	
	protected Panel matrix = new Panel(new GridLayout (4,2,5,5));
	protected JLabel ALabel = new JLabel();
	protected JTextField AText = new JTextField("1", 10);
	protected JLabel BLabel = new JLabel();
	protected JTextField BText = new JTextField("1", 10);
	protected JLabel CLabel = new JLabel();
	protected JTextField CText = new JTextField("1", 10);
	protected JLabel DLabel = new JLabel();
	protected JTextField DText = new JTextField("1", 10);
	
	protected Panel border = new Panel (new BorderLayout(5,5));			
	
	private static final String separator = EconomicFunction.separator;

	private static final int DIAMETER = 0;
	private static final int PRICE = 1;
	public Double A, B, C, D;
	public String[]  treesSelected = new String[10]; 
	public String type;
	public 	HashMap DialogMap;
	private GModel model;

	public IncomeFunctionDialog (GModel m) {
		super ();
		model = m;
		createUI ();
		setTitle (Translator.swap ("IncomeFunctionDialog"));

		
		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();
		//fileName="emptyfile";

	}

	public void Calculate ()  {
		A = Check.doubleValue (AText.getText ().trim ());
		B = Check.doubleValue (BText.getText ().trim ());
		C = Check.doubleValue (CText.getText ().trim ());
		D = Check.doubleValue (DText.getText ().trim ());
		if (type1.isSelected()){type=IncomeFunction.TYPE1;}
		if (type2.isSelected()){type=IncomeFunction.TYPE2;}
		treesSelected=new String[CommonEconFunctions.possibleTreesStatus];
		int i = 0;
		if (cut.isSelected()){treesSelected[i++]="cut";}
		if (dead.isSelected()){treesSelected[i++]="dead";}
		}
	
	/**
	 * Return Map With expected parameters
	 */
	public IncomeFunctionStarter getValue () {
		
		IncomeFunctionStarter s = new IncomeFunctionStarter();
		Calculate();
		s.A = A;
		s.B = B;
		s.C = C;
		s.D = D;
		
		s.selectedTrees =  treesSelected;
		s.type = type;
		/*
		DialogMap.put("A", A);
		DialogMap.put("B", B);
		DialogMap.put("C", C);
		DialogMap.put("D", D);
		DialogMap.put("Type", type);
		DialogMap.put("treesSelected", treesSelected);*/	
		
		return s;
	}

	//
	// Action on ok button.
	//
	private void okAction () {

		// Checks...
		Calculate();
		
		/*if (!Check.isDouble (AText.getText ().trim ())) {
			MessageDialog.promptError (Translator.swap ("IncomeFunctionDialog.youMustProvideData"));
			return;
		}
		*/
		setValidDialog (true);
		return;
	}

	//
	// Action on cancel button.
	//
	private void cancelAction () {setValidDialog (false);}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		String fileName="";
		if (evt.getSource ().equals (ok)) {
			Calculate();
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			cancelAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
		if (evt.getSource ().equals (save)) {
		//if (fileNameTable2.getText()==null || fileNameTable2.getText().equals("") || !Check.isFile(fileNameTable2.getText())){

			fileName=browseAction ( Translator.swap ("IncomeFunctionDialog.saveInATextFile"));
		//	}
			System.out.println ("fileName: "+fileName);
			try{
				Calculate();
				saveParametersInFile (fileName);
			} catch (Exception e) {
					Log.println (Log.ERROR, "IncomeFunctionDialog.actionPerformed ().SAveInATextFile",
							"Error while saving table into text file due to exception ", e);
					MessageDialog.print (this, Translator.swap ("IncomeFunctionDialog.errorWhileSavingIntoTextFileTable2"));
			}
			//hasbeenmodified = true;
		}

		if (evt.getSource ().equals (load)) {
			//if (fileNameTable2.getText()==null || fileNameTable2.getText().equals("")){
				fileName=browseAction (Translator.swap ("IncomeFunctionDialog.loadFromATextFile"));
			//}
			try{
				readParametersInFile (fileName);
				Calculate();
			} catch (Exception e) {
					Log.println (Log.ERROR, "IncomeFunctionDialog.actionPerformed ()",
							"Error while loading text file into table 2 due to exception ", e);
					MessageDialog.print (this, Translator.swap ("IncomeFunctionDialog.errorWhileLoadingFromTextFileTable"));
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
		public void saveParametersInFile (String fileName) throws Exception {
			if (! Check.isFile(fileName)){
				try {
					CommonEconFunctions.createEconParametersFile (fileName);
				} catch (Exception e) {
				throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
				}
			}
			Vector v = new Vector();
			v.add(A);	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "A", v ); v.clear();
			v.add(B);	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "B", v ); v.clear();
			v.add(C);	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "C", v ); v.clear();
			v.add(D);	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "D", v ); v.clear();
			v.add(type);	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "type", v ); v.clear();
			v.add(Arrays.toString(treesSelected));	CommonEconFunctions.updateEconParametersFile (fileName, "IncomeFunctionDialog", "treesSelected", v); v.clear();
			
			
	    }


	/**
	* Load text file in the table
	* @Version 1.2
	*/
	public void readParametersInFile (String fileName) throws Exception {
			Vector fileContent = new Vector();
			if (! Check.isFile(fileName)){
				throw new Exception ("File name "+fileName+" causes error trying to read it in readtableInFile class");
			}
			AText.setText (CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "A").toString().replace("[","").replace("]",""));
			BText.setText (CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "B").toString().replace("[","").replace("]",""));
			CText.setText (CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "C").toString().replace("[","").replace("]",""));
			DText.setText (CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "D").toString().replace("[","").replace("]",""));
			type = CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "type").toString().replace("[","").replace("]","");
			String selected = CommonEconFunctions.getValueSubTagEconParametersFile(fileName, "IncomeFunctionDialog", "treesSelected").toString();
			//type = type.trim();
			type1.setSelected(type.indexOf(IncomeFunction.TYPE1)!=-1);
			type2.setSelected(type.indexOf(IncomeFunction.TYPE2)!=-1);
			System.out.println(type+ " TYPE2:" +IncomeFunction.TYPE2);
			selected = selected.trim();
			cut.setSelected(selected.indexOf("cut")!=-1);
			dead.setSelected(selected.indexOf("dead")!=-1);
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

	

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		top.add(labelTop);
		top.add(load);
		top.add(save);
		type1.setText("prix = "+ IncomeFunction.TYPE1);
		type2.setText("prix = "+ IncomeFunction.TYPE2);
		if (!(model.getMethodProvider () instanceof NProvider)){
		    type2.setEnabled(false);	
		}
		type1.setSelected(true);
		functions.add(type1);
		functions.add(type2);
		leftSideFunction.add(type1);
		leftSideFunction.add(type2);
		cut.setText(Translator.swap ("IncomeFunctionDialog.cut"));
		cut.setSelected(true);
		dead.setText(Translator.swap ("IncomeFunctionDialog.dead"));
		leftSideTree.add(cut);
		leftSideTree.add(dead);
		//groupBoxFunction.add("Center", leftSideFunction);
		ALabel.setText("A");
		BLabel.setText("B");
		CLabel.setText("C");
		DLabel.setText("D");
		matrix.add(ALabel);
		matrix.add(AText);
		matrix.add(BLabel);
		matrix.add(BText);
		matrix.add(CLabel);
		matrix.add(CText);
		matrix.add(DLabel);
		matrix.add(DText);
		bottom.add(ok);
		bottom.add(cancel);
		bottom.add(help);
		border.add("North",  top );
		border.add("East",  leftSideTree );
		border.add("South",  bottom );
		border.add("West",  leftSideFunction);
		border.add("Center",  matrix );
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
		load.addActionListener (this);
		save.addActionListener (this);
		add("Center", border);
		getRootPane ().setDefaultButton (ok);

	}

}

