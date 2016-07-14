/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2010  Francois de Coligny
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

package capsis.extension.intervener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.ProjectFileAccessory;
import capsis.commongui.util.Helper;
import capsis.defaulttype.Tree;
import capsis.kernel.PathManager;

/**	This dialog box is used to set MarteloThinner parameters 
 * 	in GUI mode.
 * @author F. de Coligny - september 2010
 */
public class MarteloThinnerDialog extends AmapDialog implements ActionListener, RowSorterListener {
	
	/**	A table model for the MarteloThinnerDialog
	 * 	The column 0 contains Integer instances (matching tree ids are ints)
	 */
	private static class MarteloTableModel extends AbstractTableModel {
		private String[] columnNames;
		private Object[][] data;

		public MarteloTableModel (String[] columnNames, Object[][] data) {
			this.columnNames = columnNames;
			this.data = data;
		}

		public int getColumnCount () {
			return columnNames.length;
		}

		public int getRowCount () {
			return data.length;
		}

		public String getColumnName (int col) {
			return columnNames[col];
		}

		public Object getValueAt (int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass (int c) {
			return getValueAt(0, c).getClass();
		}

		/**	Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears on screen.
			return false;
		}

		/**	Don't need to implement this method unless your table's data can
		 *	change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

	}
	/* end-of-MarteloTableModel */
	
	private NumberFormat nf;  // To tune the number of decimals of decimal numbers
	private MarteloThinner thinner;  // The thinner being tuned
	
	private MarteloTableModel tableModel;
	private JTable table;
	
	private JTextField fileName;
	private JButton browse;
	private JButton updateTable;
	
	private JRadioButton cutIdsInFirstColumn;
	private JRadioButton cutIdsInSpecificColumn;
	private ButtonGroup group1;
	private JComboBox columnName;
	private JTextField cutInfo;
	private JTextField statusBar;
	
	private List<Integer> idList;  // of the trees to be cut
		
	protected JButton ok;
	protected JButton cancel;
	protected JButton help;


	/**	Constructor.
	 */
	public MarteloThinnerDialog (MarteloThinner thinner) {
		super ();
		
		this.thinner = thinner;
		
		nf = NumberFormat.getInstance (Locale.ENGLISH);
		nf.setGroupingUsed (false);
		nf.setMaximumFractionDigits (3);
		
		createUI ();
		setTitle (Translator.swap ("MarteloThinnerDialog"));
		
		activateSizeMemorization (this.getClass ().getName ());
		activateLocationMemorization (this.getClass ().getName ());
		setModal (true);
		pack ();	// uses component's preferredSize
		show ();
		
	}
	
	
	/**	The ids of the trees to be cut.
	 */
	public List<Integer> getTreeIdsToBeCut () {
		return idList;
	}

	
	private void browseAction () {

		JFileChooser chooser = new JFileChooser (Settings.getProperty ("capsis.inventory.path",  PathManager.getDir ("data")));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		//chooser.setFileSelectionMode ();
		int v = chooser.showOpenDialog (this);
		if (v == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile ().toString ();
			Settings.setProperty ("capsis.inventory.path", name);
		
			fileName.setText (name);
			
			updateTableAction ();
		}
	}
	
	private void updateTableAction () {
		try {
			String name = fileName.getText ().trim ();
			MarteloFileReader r = new MarteloFileReader (name);
//			r.setCommentString (";");  // default is "#"
//			r.setSeparator ("\t");  // default is ";"
			r.interpret ();
			
			String[] columnNames = r.getColumnNames ();
			Object[][] lines = r.getLines ();
			
			tableModel = new MarteloTableModel(columnNames, lines);
			table.setModel(tableModel);
			table.getRowSorter().addRowSorterListener (this);  // to update when sort occurs

		} catch (Exception e) {
			Log.println (Log.ERROR, "MarteloThinnerDialog.updateTableAction ()", "Error", e);
			MessageDialog.print(this, Translator.swap ("MarteloThinnerDialog.couldNotReadFile"), e);
		}
	}


	/**	Synchronize the user interface.
	 */ 
	private void synchro () {
		try {
			int c = tableModel.getColumnCount ();
			// Selection
			if (c == 1) {cutIdsInFirstColumn.setSelected (true);}
			// Enabling
			cutIdsInSpecificColumn.setEnabled (c > 1);
			columnName.setEnabled (c > 1 && cutIdsInSpecificColumn.isSelected ());
			updateColumnName ();
			
		} catch (Exception e) {
			// do nothing, maybe file not loaded yet
		}
		
	}

	/**	Updates the combo box containing all the column names
	 * 	except for column 0.
	 */
	private void updateColumnName () {
		try {
			int n = tableModel.getColumnCount();
			Vector v = new Vector ();
			for (int i = 1; i < n; i++) {  // skip the first column name (kind of "Tree id")
				v.add (tableModel.getColumnName(i));
			}
			ComboBoxModel m = new DefaultComboBoxModel(v);
			columnName.setModel(m);
		} catch (Exception e) {
			// Empty list (there may be only one column...)
			ComboBoxModel m = new DefaultComboBoxModel();
			columnName.setModel(m);
		}
		
	}

	/**	Update the user interface when the sort order changes.
	 */
	@Override
	public void sorterChanged(RowSorterEvent e) {
		updateList ();
		
	}

	/**	Update the id list
	 */ 
	private void updateList () {
		
		try {
			idList = new ArrayList<Integer> ();
			
			// One column only -> all ids
			if (cutIdsInFirstColumn.isSelected ()) {
				// Get all ids in column 0
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					// First column contains ids (ints)
					int id = (Integer) tableModel.getValueAt(i, 0);
					idList.add (id);
					
				}
				
			} else {

				// Several columns -> check the chosen column for ids selection
				// Get column index
				int j = 0;
				for (int k = 0; k < tableModel.getColumnCount(); k++) {
					if (tableModel.getColumnName(k).equals (columnName.getSelectedItem())) {
						j = k;
						break;
					}
				}
				
				// Read all lines, retain only the ids (col 0) if something is written in col j
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					// Next line: trim () to remove spaces and tabs (PhD said so)
					String s = ((String) tableModel.getValueAt(i, j)).trim ();  // considered column
					if (s.length() == 0) {continue;}  // skip id if nothing written in the considered column (not to be cut)
					int id = ((Integer) tableModel.getValueAt(i, 0));  // column of the ids
					idList.add (id);
					
				}
				
			}
			
			// Create the little message with the ids to be cut, same order than 
			// the tableModel (may be sorted)
			String s = "";
			Set set = new HashSet (idList);  // contains is faster on a Set (see below)
			
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				
				int k = table.convertRowIndexToModel(i);  // convert view -> model
				int id = ((Integer) tableModel.getValueAt (k, 0));
				if (set.contains (id)) {
					s += ""+id+" ";
				}
			}
			
			cutInfo.setText ("n = "+idList.size ()+", ids = "+s);
			cutInfo.setCaretPosition (0);
			
			// Check ids
			checkIfTrouble (idList);
			
		} catch (Exception e){
			cutInfo.setText ("n = "+0);
		}
		
	}

	/**	Checks if all the trees to be cut exist, writes 
	 * 	a note in the status bar if not.
	 */
	private void checkIfTrouble (List<Integer> idList) {
		Set<Integer> set = new HashSet<Integer> ();
		for (Object t : thinner.concernedTrees) {
			set.add (((Tree) t).getId ());
		}
		
		String report = "";
		int cpt = 0;
		for (int id : idList) {
			if (!set.contains (id)) {
				report += (id + " ");
				cpt++;
			}
		}
		if (report.length () != 0) {
			report = Translator.swap ("MarteloThinnerDialog.warningTreeNotFound")+" (n="+cpt+") : "+report;
		}
		statusBar.setText (report);
		statusBar.setCaretPosition (0);

	}

	/**	Action on ok button.
	 */ 
	private void okAction () {
		// Check the user values here
		try {
			updateList ();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MarteloThinnerDialog.okAction ()", "Error on ok", e);
			MessageDialog.print(this, Translator.swap ("MarteloThinnerDialog.errorCheckLogForDetails"));
			return;
		}
		
		setValidDialog (true);
	}
	
	/**	Action on cancel button.
	 */ 
	private void cancelAction () {setValidDialog (false);}

	/** Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals(browse)) {
			browseAction ();
			synchro ();
			updateList ();
		} else if (evt.getSource ().equals (cutIdsInFirstColumn)) {
			synchro ();
			updateList ();
		} else if (evt.getSource ().equals (cutIdsInSpecificColumn)) {
			synchro ();
			updateList ();
		} else if (evt.getSource ().equals (updateTable)) {
			updateTableAction ();
			synchro ();
			updateList ();
		} else if (evt.getSource ().equals (columnName)) {
			updateList ();
		} else if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			cancelAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}
	
	/**	Create the dialog box user interface.
	*/ 
	private void createUI () {
		
		// 1. Top panel
		ColumnPanel top = new ColumnPanel ();
		
		// At opening time: an empty table
		table = new JTable ();
		table.setAutoCreateRowSorter (true);  // can be sorted
		table.getRowSorter().addRowSorterListener (this);  // to update when sort occurs
		top.add (new JScrollPane (table));
		
		// 1. Bottom panel
		ColumnPanel bottom = new ColumnPanel ();
		
		// Import file
		LinePanel importPanel = new LinePanel (Translator.swap ("MarteloThinnerDialog.import"));
		importPanel.add (new JLabel (Translator.swap ("MarteloThinnerDialog.fileName")+" : "));
		fileName = new JTextField ();
		importPanel.add (fileName);
		browse = new JButton (Translator.swap ("MarteloThinnerDialog.browse"));
		browse.addActionListener(this);
		importPanel.add (browse);
//		updateTable = new JButton (Translator.swap ("MarteloThinnerDialog.updateTable"));
//		updateTable.addActionListener(this);
//		importPanel.add (updateTable);
		
		bottom.add (importPanel);
		
		// Cut mode
		ColumnPanel cutModePanel = new ColumnPanel (Translator.swap ("MarteloThinnerDialog.cutMode"));
		LinePanel l1 = new LinePanel ();
		cutIdsInFirstColumn = new JRadioButton (Translator.swap ("MarteloThinnerDialog.cutIdsInFirstColumn"));
		cutIdsInFirstColumn.addActionListener (this);
		l1.add (cutIdsInFirstColumn);
		l1.addGlue ();
		cutModePanel.add (l1);
		cutModePanel.add (l1);
		LinePanel l2 = new LinePanel ();
		cutIdsInSpecificColumn = new JRadioButton (Translator.swap ("MarteloThinnerDialog.cutIdsInSpecificColumn"));
		cutIdsInSpecificColumn.addActionListener (this);
		l2.add (cutIdsInSpecificColumn);
		columnName = new JComboBox ();
		columnName.addActionListener (this);
		l2.add (columnName);
		l2.addGlue ();
		
		cutModePanel.add (l2);
		
		
		LinePanel l3 = new LinePanel ();
		l3.addGlue ();
		l3.add (new JLabel (Translator.swap ("MarteloThinnerDialog.cutInfo")+" : "));
		cutInfo = new JTextField ();
		cutInfo.setEditable (false);
		l3.add (cutInfo);
		l3.addGlue ();
		
		cutModePanel.add (l3);
		
		
		group1 = new ButtonGroup ();
		group1.add (cutIdsInFirstColumn);
		group1.add (cutIdsInSpecificColumn);
		cutIdsInFirstColumn.setSelected (true);
		
		bottom.add (cutModePanel);
		
		// 2. control panel (ok cancel help);
		LinePanel controlPanel = new LinePanel ();
		statusBar = new JTextField ();
		statusBar.setEditable (false);
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.addStrut0 ();
		controlPanel.add (statusBar);
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		controlPanel.addStrut0 ();
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
	
		bottom.add (controlPanel);
		
		// General layout
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (top, BorderLayout.CENTER);
		getContentPane ().add (bottom, BorderLayout.SOUTH);
		
		// Set ok as default button
		setDefaultButton(ok);
		
	}


}

