package fireparadox.gui.plantpattern;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.plant.fmgeom.FmGeom;
import fireparadox.model.plant.fmgeom.FmGeomMap;

class PatternMapCellRenderer extends JLabel implements TableCellRenderer {

	private FmGeomMap currentFilteredMap;
	private RowSorter sorter;

	public PatternMapCellRenderer (FmGeomMap currentFilteredMap, RowSorter sorter) {
		this.currentFilteredMap=currentFilteredMap;
		this.sorter = sorter;
		setOpaque (true);
	}
	public Component getTableCellRendererComponent (JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {

		Object [] entry = currentFilteredMap.keySet ().toArray ();
		if(row < entry.length) {
			if( currentFilteredMap.isAdminEntry ((String)entry[sorter.convertRowIndexToModel (row)]) ) {
				setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.LIGHT_GRAY);
			} else {
				setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.white);
			}
		}  else setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.white);

		setForeground (isSelected ? Color.white : Color.black);

		this.setText ((String)value);

		return this;

	}

}




/**
 *
 * @author S. Griffon - May 2007
 */
public class FmPatternMapDialog extends AmapDialog
               implements ActionListener, ListSelectionListener {

	private FmModel model;

	private JButton ok;
	private JButton cancel;
	private JButton help;
	private JButton add;
	private JButton remove;
	private JButton modify;
	private JButton editPattern;
	private JButton reset;

	private JCheckBox specieCheckBox;
	private JCheckBox heigthCheckBox;
	private JCheckBox envCheckBox;
	private JCheckBox strictCheckBox;

	private JComboBox specieComboBox;
	private JTextField heightMinTextField;
	private JTextField heightMaxTextField;
	private JComboBox enviComboBox;


	private JScrollPane scroll1;

	//TODO remplacer scroll2 par la previewer
	private JScrollPane scroll2;

	private JTable tablePatternMap;

	private FmGeomMap currentFilteredMap;

	private FmPattern2DPanel pattern2DPanel;

	private int selectedRow; //the current selected



	/**	Constructor.
	 */
	public FmPatternMapDialog (FmModel fm) {
		super ();

		model = fm;

		selectedRow = 0;
		currentFilteredMap = (FmGeomMap)model.getPatternMap ().clone ();

		try {
			model.getSpeciesList ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPatternMapDialog.c ()", "Could not get the species list", e);
		}

		createUI ();

		updateTablePatternMap ();
		synchro ();
		refreshPreviewer ();

		// location is set by AmapDialog
		pack ();
		show ();
	}

	/**	Ok was hit
	 */
	private void okAction () {
		setValidDialog (true);
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource ().equals (add)) {
			addSpeciePatternEntry ();
		} else if (evt.getSource ().equals (modify)) {
			modifySpeciePatternEntry ();
		} else if (evt.getSource ().equals (remove)) {
			removeSpeciePatternEntry ();
		} else if (evt.getSource ().equals (editPattern)) {
			editPatterns ();
		} else if ((specieCheckBox.isSelected () && evt.getSource ().equals (specieComboBox)) || (envCheckBox.isSelected () && evt.getSource ().equals (enviComboBox)) ||  evt.getSource ().equals (heightMinTextField) || evt.getSource ().equals (heightMaxTextField)){
			filterPatternMap ();
		} else if (evt.getSource ().equals (strictCheckBox) || evt.getSource ().equals (specieCheckBox) || evt.getSource ().equals (heigthCheckBox) || evt.getSource ().equals (envCheckBox)) {
			synchro ();
			filterPatternMap ();
		} else if (evt.getSource ().equals (reset)) {
			reset ();
		}
	}

	private void reset () {

		int ret = JOptionPane.showConfirmDialog (this,Translator.swap ("FiPatternMapDialog.resetYesNo"));
		if (ret==0) {
			try {
				model.getPatternMap ().reset ();
				filterPatternMap ();
				model.getPatternMap ().firePatternChanged ();
			} catch (Exception e) {

			}
		}
	}

	private void synchro () {
		specieComboBox.setEnabled (specieCheckBox.isSelected ()) ;
		strictCheckBox.setEnabled (specieCheckBox.isSelected ());
		heightMinTextField.setEnabled (heigthCheckBox.isSelected ()) ;
		heightMaxTextField.setEnabled (heigthCheckBox.isSelected ()) ;
		enviComboBox.setEnabled (envCheckBox.isSelected ()) ;
	}

	private void editPatterns () {
		if(model.getPatternList().size () > 0) {
			int row = 0;
			if(model.getPatternMap().size () > 0) {
				Object [] entry = currentFilteredMap.entrySet ().toArray ();

				Map.Entry currentEntry =
					(Map.Entry)entry[ tablePatternMap.convertRowIndexToModel(selectedRow)];
				try {
					row = model.getPatternList().getIndexOf ((String)currentEntry.getValue ());
				} catch (Exception e) {
					JOptionPane.showMessageDialog (this, e.getMessage ());
				}
			}
			FmPatternListDialog fList = new FmPatternListDialog (model, row);
			refreshPreviewer ();
		} else {
			JOptionPane.showMessageDialog (this,Translator.swap ("FiPatternMapDialog.noPatternInTheList"));
		}

	}

	//Add a new entry in the pattern map associated and redraw the table
	private void addSpeciePatternEntry () {
		FmPatternMapAddEntryDialog addEntry = new FmPatternMapAddEntryDialog (model);
		if(addEntry.isValidDialog ())	{
			try {
				//Redraw a new JTable with the updated PatternMap
				model.getPatternMap ().put (addEntry.getCurrentCriteria (),  addEntry.getCurrentPattern ());
				model.getPatternMap ().save ();
				selectedRow = model.getPatternMap ().getIndexOf (addEntry.getCurrentCriteria ());
				selectedRow = tablePatternMap.convertRowIndexToView(selectedRow);

			} catch (Exception e) {
				JOptionPane.showMessageDialog (this,  e.getMessage ());
			}

			filterPatternMap ();
			updateComboBox ();
			model.getPatternMap ().firePatternChanged ();
		}
	}

	//Remove an entry of the pattern map and redraw the table.
	private void removeSpeciePatternEntry () {

		int [] selRow  = tablePatternMap.getSelectedRows ();
		Object [] arrayObj = currentFilteredMap.keySet ().toArray ();
		if(selRow.length>=1 && tablePatternMap.getRowCount ()>0 ) {
			try {
				selectedRow = tablePatternMap.convertRowIndexToModel (selRow[0]);
				for(int row : selRow) {
					if(model.getPatternMap ().isModifiable ((String)arrayObj[ tablePatternMap.convertRowIndexToModel(row)])){
						int ret = JOptionPane.showConfirmDialog (this,Translator.swap ("FiPatternMapDialog.removeYesNo")
								+ " : " + (String)arrayObj[ tablePatternMap.convertRowIndexToModel (row)]);
						if (ret==0) {
							try {
								model.getPatternMap ().remove ((String)arrayObj[ tablePatternMap.convertRowIndexToModel (row)]);
							} catch(Exception e) {
								JOptionPane.showMessageDialog (this, e.getMessage ());
								return;
							}
							model.getPatternMap ().save ();
						}
					} else {
						JOptionPane.showMessageDialog (this, Translator.swap ("FireParadox.removeAdminEntryForbidden"));
					}
				}

				if (selectedRow >= currentFilteredMap.size ()) {
					selectedRow=currentFilteredMap.size ()-1;
				}
				if(selectedRow<0) {
					selectedRow=0;
				}


				filterPatternMap ();
				updateComboBox ();
				model.getPatternMap ().firePatternChanged ();

			} catch (Exception e) {
				JOptionPane.showMessageDialog (this,  Translator.swap ("FiPatternMapDialog.errorOnWritingPatternMap"));
			}
		}
	}

	//Modify the map entry corresponding to the selected row in the JTable table
	private void modifySpeciePatternEntry () {
		int [] selRow  = tablePatternMap.getSelectedRows ();
		if(selRow.length>=1 && tablePatternMap.getRowCount ()>0) {
			Object [] arrayObj = currentFilteredMap.entrySet ().toArray ();

			Map.Entry modifEntry = (Map.Entry)arrayObj[ tablePatternMap.convertRowIndexToModel (selRow[0])];

			String selSpecie = (String)modifEntry.getKey ();
			String selPattern = (String)modifEntry.getValue ();
			FmGeom fpattern = model.getPatternMap ().getPattern (selSpecie);

			try {
				model.getPatternMap ().remove (selSpecie);
			} catch (Exception e) {
				JOptionPane.showMessageDialog (this, e.getMessage ());
				return;
			}

			//Open dialog to modify the pattern mapped to a species (if selectioned>1, take only the first entry) selectioned in the pattern map JTable
			FmPatternMapAddEntryDialog modEntry = new FmPatternMapAddEntryDialog (model, selSpecie , fpattern);

			if(modEntry.isValidDialog ())	{
				model.getPatternMap ().put (modEntry.getCurrentCriteria (),  modEntry.getCurrentPattern ());
				try {
					model.getPatternMap ().save ();
					selectedRow = model.getPatternMap ().getIndexOf (modEntry.getCurrentCriteria ());
					selectedRow = tablePatternMap.convertRowIndexToView (selectedRow);
				} catch (Exception e) {
					JOptionPane.showMessageDialog (this,  e.getMessage ());
				}

			} else {
				model.getPatternMap ().put (selSpecie,  selPattern);
			}


			filterPatternMap ();
			updateComboBox ();
			model.getPatternMap ().firePatternChanged ();

		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapDialog.selectAnEntryToModify"));
		}
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		//filterPanel.add (new JLabel (Translator.swap ("FiPatternMapDialog.filterLabel")));
		JPanel gl1_1 = new JPanel (new FlowLayout (FlowLayout.CENTER));
		specieCheckBox = new JCheckBox (Translator.swap ("FiPatternMapDialog.speciesLabel"), false);
		specieCheckBox.addActionListener (this);
		strictCheckBox =  new JCheckBox (Translator.swap ("FiPatternMapDialog.strictLabel"), false);
		strictCheckBox.addActionListener (this);
		heigthCheckBox = new JCheckBox (Translator.swap ("FiPatternMapDialog.heightIntervalLabel"),false);
		heigthCheckBox.addActionListener (this);
		envCheckBox = new JCheckBox (Translator.swap ("FiPatternMapDialog.environmentLabel"),false);
		envCheckBox.addActionListener (this);

		specieComboBox = new JComboBox ();
		specieComboBox.addActionListener (this);

		gl1_1.add (specieCheckBox);
		gl1_1.add (specieComboBox);
		gl1_1.add (strictCheckBox);

		//JPanel gl1_2 = new JPanel (new FlowLayout (FlowLayout.CENTER));

		heightMinTextField = new JTextField (5);
		heightMaxTextField = new JTextField (5);
		heightMinTextField.addActionListener (this);
		heightMaxTextField.addActionListener (this);
		gl1_1.add (heigthCheckBox);
		gl1_1.add (new JLabel (" [ "));
		gl1_1.add (heightMinTextField);
		gl1_1.add (new JLabel (" , "));
		gl1_1.add (heightMaxTextField);
		gl1_1.add (new JLabel (" [ "));

		//JPanel gl1_3 = new JPanel (new FlowLayout (FlowLayout.RIGHT));

		enviComboBox = new JComboBox ();
		enviComboBox.addActionListener (this);
		gl1_1.add (envCheckBox);
		gl1_1.add (enviComboBox);


		JPanel gl1 = new JPanel (new GridLayout (1,3));
		gl1.add (gl1_1);

		JPanel centerFilter = new JPanel (new BorderLayout ());
		centerFilter.add (gl1, BorderLayout.CENTER);
		TitledBorder tborder1 = new TitledBorder (centerFilter.getBorder (), Translator.swap ("FiPatternMapDialog.filterLabel"));
		centerFilter.setBorder (tborder1);

		//filterPanel.add (centerFilter);

		JPanel l1 = new JPanel (new BorderLayout ());
		scroll1 = new JScrollPane ();
		l1.add (scroll1, BorderLayout.CENTER);

		JPanel c2 = new JPanel (new GridLayout (4,1));
		add = new JButton (Translator.swap ("FiPatternMapDialog.add"));
		add.addActionListener (this);
		remove = new JButton (Translator.swap ("FiPatternMapDialog.remove"));
		remove.addActionListener (this);
		modify = new JButton (Translator.swap ("FiPatternMapDialog.modify"));
		modify.addActionListener (this);
		c2.add (add);
		c2.add (remove);
		c2.add (modify);
		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (c2,BorderLayout.NORTH);
		l1.add (aux, BorderLayout.EAST);

		TitledBorder tborder2 = new TitledBorder (l1.getBorder (), Translator.swap (
				"FiPatternMapDialog.patternsMap"));
		l1.setBorder (tborder2);

		pattern2DPanel = new FmPattern2DPanel (null);
		TitledBorder tborder4 = new TitledBorder (pattern2DPanel.getBorder (), Translator.swap (
				"FiPatternMapDialog.previewer"));
		pattern2DPanel.setBorder (tborder4);
		//pattern2DPanel.setPreferredSize (new Dimension (250,500));

		//scroll2

		//Control Panel
		LinePanel bottomPanel = new LinePanel ();
		JPanel resetPanel = new JPanel ();
		resetPanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		JLabel modeLabel = new JLabel ();
		if(	model.getPatternMap ().isAdmin ()) {
			modeLabel.setText (Translator.swap ("FiPatternMapDialog.adminMode"));
		} else {
			modeLabel.setText (Translator.swap ("FiPatternMapDialog.clientMode"));
		}
		resetPanel.add (modeLabel);
		reset = new JButton (Translator.swap ("FiPatternMapDialog.reset"));
		reset.addActionListener (this);
		resetPanel.add (reset);
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
		editPattern = new JButton (Translator.swap ("FiPatternMapDialog.editPattern"));
		editPattern.addActionListener (this);
		controlPanel.add (editPattern);
		ok = new JButton (Translator.swap ("Shared.ok"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);
		bottomPanel.add (resetPanel);
		bottomPanel.add (controlPanel);

		// fc + ov - 25.9.2007
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
				l1, pattern2DPanel);
		split.setResizeWeight (0.5);

		//add cancel...
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (centerFilter, BorderLayout.NORTH);
		//~ getContentPane ().add (l1, BorderLayout.WEST);					// fc + ov - 25.9.2007
		//~ getContentPane ().add (pattern2DPanel, BorderLayout.CENTER);	// fc + ov - 25.9.2007
		getContentPane ().add (split, BorderLayout.CENTER);		// fc + ov - 25.9.2007
		getContentPane ().add (bottomPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPatternMapDialog.patternDialogTitle"));

		setModal (true);
		updateComboBox ();

	}


	private void filterPatternMap () {

		currentFilteredMap = (FmGeomMap)model.getPatternMap ().clone ();
		Object [] entry = currentFilteredMap.entrySet ().toArray ();
		String selectSpecie = "";
		String selectEnvi = "";
		double heightMin, heightMax;
		heightMin = -Double.MAX_VALUE;
		heightMax = Double.MAX_VALUE;

		if(specieCheckBox.isSelected ()) {
			selectSpecie=(String)specieComboBox.getSelectedItem ();
		}

		if(heigthCheckBox.isSelected ()) {
			try {
				String heightMinStr = heightMinTextField.getText ();
				String heightMaxStr = heightMaxTextField.getText ();
				if (heightMinStr.length ()==0) heightMinStr = String.valueOf (-Double.MAX_VALUE);
				if (heightMaxStr.length ()==0) heightMaxStr = String.valueOf (Double.MAX_VALUE);
				if(Check.isDouble (heightMinStr) && Check.isDouble (heightMaxStr)) {
					heightMin= Double.parseDouble (heightMinStr);
					heightMax= Double.parseDouble (heightMaxStr);
					if(heightMax < heightMin) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapDialog.heightMin>heightMax"));
						return;
					}

				} else {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapDialog.heightTextFieldAreNotDouble"));
					return;
				}

			} catch (NumberFormatException e) {
				Log.println (Log.ERROR, "FiPatternMapDialog.isSelected ()",
					"Trouble with format number", e);
				return;
				//throw new Exception (Translator.swap ("FiPatternMapDialog.errorMessageFilterNumericValues"));
			}

		}

		if(envCheckBox.isSelected ()) {
			selectEnvi= String.valueOf (enviComboBox.getSelectedIndex ());
		}

		if(specieCheckBox.isSelected () || heigthCheckBox.isSelected () || envCheckBox.isSelected ()) {
			boolean remove = false;

			for (Object o : entry) {

				String criteria = (String)((Map.Entry)o).getKey ();
				String [] key = FmGeomMap.formatCriteria (criteria);
				if(selectSpecie == null) selectSpecie="";

				if(!selectSpecie.equals ("")) {





					//If not strict check for each inferior taxonomoc level
					//Not-strict filter : filter on the exact taxon name
					if(!strictCheckBox.isSelected ()) {
//						FiSpecies selectedSpecies = FiSpecies.getSpecies (selectSpecie);
						FiSpecies selectedSpecies = model.getSpeciesSpecimen ().getSpecies (selectSpecie);
						
						int selectedTaxonLevel = selectedSpecies.getTaxonomicLevel();

//						FiSpecies keySpecies = FiSpecies.getSpecies (key[0]);
						FiSpecies keySpecies = model.getSpeciesSpecimen ().getSpecies (key[0]);
						int keyTaxonLevel = keySpecies.getTaxonomicLevel();

						if (selectedTaxonLevel==FiSpecies.TRAIT_TAXON_LEVEL) {
							if (keyTaxonLevel==FiSpecies.TRAIT_TAXON_LEVEL) {
								if (!key[0].equals (selectSpecie)) remove = true;
							}
							else {
								String keyTrait = keySpecies.getTrait();

								if (!keyTrait.equals(selectSpecie)) remove = true;
							}
						}

						else if (selectedTaxonLevel==FiSpecies.GENUS_TAXON_LEVEL) {
							if (keyTaxonLevel==FiSpecies.GENUS_TAXON_LEVEL) {
								if (!key[0].equals (selectSpecie)) remove = true;
							}
							else {
								String keyGenus = keySpecies.getGenus();
								if (!keyGenus.equals(selectSpecie)) remove = true;
							}
						}

						else if (!key[0].equals (selectSpecie)) {
							remove = true;
						}

					} else if (!key[0].equals (selectSpecie)) {
						remove = true;
					}

				} else if (!key[1].equals ("") && Double.parseDouble (key[1]) < heightMin) {
					remove = true;
				} else if (!key[2].equals ("") && Double.parseDouble (key[2]) > heightMax) {
					remove = true;
				} else if (!selectEnvi.equals ("") && !key[3].equals (selectEnvi)) {
					remove = true;
				}
				if(remove) {
					try {
						currentFilteredMap.remove (criteria);
					} catch (Exception e) {
						JOptionPane.showMessageDialog (this, e.getMessage ());
					}
				}
				remove = false;
			}

		}

		if(selectedRow>=currentFilteredMap.size ()) {
			selectedRow = currentFilteredMap.size ()-1;
		}
		if(selectedRow<0) {
			selectedRow=0;
		}

		updateTablePatternMap ();
		refreshPreviewer ();
	}

//	private void filterPatternMap () {
//
//		currentFilteredMap = (FiPatternMap)model.getPatternMap ().clone ();
//		Object [] entry = currentFilteredMap.entrySet ().toArray ();
//
//		String selectSpecie = "";
//		String selectEnvi = "";
//		double heightMin, heightMax;
//		heightMin = -Double.MAX_VALUE;
//		heightMax = Double.MAX_VALUE;
//
//		if(specieCheckBox.isSelected ()) {
//			selectSpecie=(String)specieComboBox.getSelectedItem ();
//		}
//
//		if(heigthCheckBox.isSelected ()) {
//			try {
//				String heightMinStr = heightMinTextField.getText ();
//				String heightMaxStr = heightMaxTextField.getText ();
//				if (heightMinStr.length ()==0) heightMinStr = String.valueOf (-Double.MAX_VALUE);
//				if (heightMaxStr.length ()==0) heightMaxStr = String.valueOf (Double.MAX_VALUE);
//				if(Check.isDouble (heightMinStr) && Check.isDouble (heightMaxStr)) {
//					heightMin= Double.parseDouble (heightMinStr);
//					heightMax= Double.parseDouble (heightMaxStr);
//					if(heightMax < heightMin) {
//						JOptionPane.showMessageDialog (this, Translator.swap ("filterPatternMap.heightMin>heightMax"));
//						return;
//					}
//
//				} else {
//					JOptionPane.showMessageDialog (this, Translator.swap ("filterPatternMap.heightTextFieldAreNotDouble"));
//					return;
//				}
//
//			} catch (NumberFormatException e) {
//				Log.println (Log.ERROR, "FiPatternMapDialog.isSelected ()",
//					"Trouble with format number", e);
//				return;
//				//throw new Exception (Translator.swap ("FiPatternMapDialog.errorMessageFilterNumericValues"));
//			}
//
//
//		}
//
//		if(envCheckBox.isSelected ()) {
//			selectEnvi= String.valueOf (enviComboBox.getSelectedIndex ());
//		}
//
//		if(specieCheckBox.isSelected () || heigthCheckBox.isSelected () || envCheckBox.isSelected ()) {
//			boolean remove = false;
//
//			for (Object o : entry) {
//
//				String criteria = (String)((Map.Entry)o).getKey ();
//
//				String [] key = FiPatternMap.parseCriteria (criteria);
//				if (!selectSpecie.equals ("") && !key[0].equals (selectSpecie)) {
//					remove = true;
//				} else if (!key[1].equals ("") && Double.parseDouble (key[1]) < heightMin) {
//					remove = true;
//				} else if (!key[2].equals ("") && Double.parseDouble (key[2]) > heightMax) {
//					remove = true;
//				} else if (!selectEnvi.equals ("") && !key[3].equals (selectEnvi)) {
//					remove = true;
//				}
//				if(remove) {
//					try {
//						currentFilteredMap.remove (criteria);
//					} catch (Exception e) {
//						JOptionPane.showMessageDialog (this, e.getMessage ());
//					}
//				}
//				remove = false;
//			}
//
//		}
//		updateTablePatternMap ();
//	}

	private void updateComboBox () {

		TreeSet <String> firespecies = new TreeSet <String> ();
		Object [] entry = model.getPatternMap ().entrySet ().toArray ();


		for (Object o : entry) {
			String criteria = (String)((Map.Entry)o).getKey ();
			String [] key = FmGeomMap.parseCriteria (criteria);
			FiSpecies speciesFound = model.getSpeciesSpecimen ().getSpecies (key[0]);
			System.out.println("criteria="+criteria+" key="+key[0]) ;

			if (speciesFound != null) {
				firespecies.add (key[0]);
			}

		}

		specieComboBox.removeAllItems ();
		for (String specieName :firespecies) {
			specieComboBox.addItem (new String (specieName));
		}
		if(!firespecies.isEmpty ())
			specieComboBox.setSelectedIndex (0);

		String [] enviValue = {Translator.swap ("FiPatternMapDialog.openEnv"),
					Translator.swap ("FiPatternMapDialog.closeEnv")};
		enviComboBox.removeAllItems ();
		enviComboBox.addItem (enviValue[0]);
		enviComboBox.addItem (enviValue[1]);
		enviComboBox.setSelectedIndex (1);

	}

	private void updateTablePatternMap () {
System.out.println ("MapDialog.updateTablePatternMap ()...");

		if (tablePatternMap != null){
			tablePatternMap.getSelectionModel ().removeListSelectionListener (this);
		}

		//  To display a TreeMap into a JTable of n rows and 2 columns:
		//      - the row represent a entry of the map and the first column is
		//      - the first column is the key of the entry
		//      - the second column is the value of the entry
		TableModel dataModel = new AbstractTableModel () {
			// an entry map is always composed of 2 value : a key and value
			public int getColumnCount () {return 4;}
			// The row count is the size of the pattern map
			public int getRowCount () {return currentFilteredMap.size ();}
			//Returns the name of the column at columnIndex.
			@Override
			public String getColumnName (int columnIndex) {
				switch (columnIndex) {
					case 0  : return Translator.swap ("FiPatternMapDialog.tableColumnNameSpecies");
					case 1  : return Translator.swap ("FiPatternMapDialog.tableColumnNameInterval");
					case 2  : return Translator.swap ("FiPatternMapDialog.tableColumnNameEnvironment");
					case 3  : return Translator.swap ("FiPatternMapDialog.tableColumnNamePattern");
					default : break;
				}
				return Translator.swap ("FiPatternMapDialog.tableColumnNameError");
			}

			//Return the key (col=0) or value (col=1) of the entry map at the position 'row'
			public Object getValueAt (int row, int col) {
				if (currentFilteredMap.size () > 0) {
					Object [] entry = currentFilteredMap.entrySet ().toArray ();
					String value = (String)((Map.Entry)entry[row]).getKey ();
					String [] key = FmGeomMap.formatCriteria (value);

					if(key[3].equals ("0")) {
						key[3] = Translator.swap ("FiPatternMapDialog.openEnv");
					} else {
						key[3] = Translator.swap ("FiPatternMapDialog.closeEnv");
					}

					switch (col) {
						case 0 : value = key[0]; break;
						case 1 : value = "[" + key[1] + " , " + key[2] + "["; break;
						case 2 : value = key[3]; break;
						case 3 : value = currentFilteredMap.getPattern (value).getName (); break;
						default : break;
					}
					return value;
				} else {
					return "";
				}

			}

			@Override
			public Class getColumnClass (int columnIndex) {
				return String.class;
			}
		};

		tablePatternMap = new JTable (dataModel);
		tablePatternMap.setAutoCreateRowSorter(true);

		//tablePatternMap.getSelectionModel ().addSelectionInterval (selectedRow,selectedRow);

		tablePatternMap.setDefaultRenderer (String.class,
			new PatternMapCellRenderer (currentFilteredMap, tablePatternMap.getRowSorter() ));

		tablePatternMap.getSelectionModel ().addListSelectionListener (this);

		scroll1.getViewport ().setView (tablePatternMap);

	}

	private void refreshPreviewer () {

		if(currentFilteredMap.size () != 0 || selectedRow < 0) {

			int locSelectedRow = tablePatternMap.convertRowIndexToModel(selectedRow);

			Object [] arrayObj = currentFilteredMap.entrySet ().toArray ();
			Map.Entry curEntry = (Map.Entry)arrayObj[locSelectedRow];
			String selSpecie = (String)curEntry.getKey ();
			String selPattern = (String)curEntry.getValue ();
			FmGeom fpattern = model.getPatternMap ().getPattern (selSpecie);
			pattern2DPanel.setPattern (fpattern);
		} else {
			pattern2DPanel.setPattern (null);
		}
	}

	public void valueChanged (ListSelectionEvent e) {
		int s = tablePatternMap.getSelectedRow ();	// fc + sg - 26.7.2007
		if (s < 0) {
			return;
		} else {
			selectedRow = s;
			refreshPreviewer ();
		}
		//~ selectedRow = tablePatternMap.getSelectedRow ();
		//~ refreshPreviewer ();

	}

}
