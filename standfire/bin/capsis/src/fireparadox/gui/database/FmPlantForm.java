package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Range;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmPlantSyntheticData;


/**	A form to select fuel individuals in the database.
*	The form is based on buffers (see model class), no access to the database here.
*	A listener can register with addSelectionListener (ListSelectionListener l)
*	and it will be told any time the selection changes. It can get the 
*	selection with List<FiPlantFormLine> getSelection ().
*	@author Oana Vigy & Eric Rigaud - july 2007
*/
public class FmPlantForm extends JPanel implements ActionListener{
// fc - sept 2009 review

	private FmModel model;
	private FmInitialParameters settings;
	private FmPlantFormTableModel tableModel;

    private JCheckBox speciesNameEnabled;
    private JCheckBox heightEnabled;
    private JCheckBox crownDiameterEnabled;

    private JComboBox speciesCombo;
    private JComboBox heightCombo;
    private JComboBox crownDiameterCombo;

	private JTable resultTable;


    /**	Creates a new instance of FiPlantForm.
	*/
    public FmPlantForm (FmModel model) {

		this.model = model;
		settings = model.getSettings();

		createComboBoxes ();

		// Create the tableModel
		tableModel = new FmPlantFormTableModel ();

		createUI();
 		synchro ();
 		updateTable ();
	}
	
	/**	Create the combo boxes with criteria to refine the table.
	*/
	public void createComboBoxes () {

		// speciesCombo
		Set<String> speciesList = new TreeSet<String> ();	// TreeSet: sorted
		try {
			speciesList.addAll (model.getGenusNames ());	// instances of String
			speciesList.addAll (model.getSpeciesNames ());	// instances of String

		} catch (Exception e) {
			Log.println (Log.WARNING, "FiPlantForm ()", "Could not get speciesList, passed", e);
		} finally {
			speciesCombo = new JComboBox (new Vector<String> (speciesList));
		}
		
		// heightCombo
		Vector v = new Vector ();
		try {
			v.add (new Range (0, 0.5));
			v.add (new Range (0.5, 1));
			v.add (new Range (1, 2));
			v.add (new Range (2, 5));
			v.add (new Range (5, 10));
			v.add (new Range (10, 15));
			v.add (new Range (15, 20));
			v.add (new Range (20, 25));
			v.add (new Range (25, 30));
			v.add (new Range (30, 35));
			v.add (new Range (35, 40));
			v.add (new Range (40, 45));
			v.add (new Range (45, 50));
			v.add (new Range (50, 100));
		} catch (Exception e) {
			Log.println (Log.WARNING, "FiPlantForm ()", 
					"Trouble while creating height ranges, passed", e);
		}
		heightCombo = new JComboBox (v);

		// crownDiameterCombo
		v = new Vector ();
		try {
			v.add (new Range (0, 1));
			v.add (new Range (1, 2));
			v.add (new Range (2, 3));
			v.add (new Range (3, 4));
			v.add (new Range (4, 5));
			v.add (new Range (5, 6));
			v.add (new Range (6, 7));
			v.add (new Range (7, 8));
			v.add (new Range (8, 9));
			v.add (new Range (9, 10));
			v.add (new Range (10, 50));
		} catch (Exception e) {
			Log.println (Log.WARNING, "FiPlantForm ()", 
					"Trouble while creating crownDiameter ranges, passed", e);
		}
		crownDiameterCombo = new JComboBox (v);

	}

	/**	Update the table according to the current values of selection criteria
	*	in the combo boxes.
	*/
	public void updateTable () {
		try {
			String selectedSpeciesName = (String) speciesCombo.getSelectedItem ();
			Range selectedHeight = (Range) heightCombo.getSelectedItem ();
			Range selectedCrownDiameter = (Range) crownDiameterCombo.getSelectedItem ();
			
			// Get the FiPlantSyntheticData instances
			Map<Long,FmPlantSyntheticData> allPlants = model.getPlantSyntheticMap ();
			
			// Manage FiPlantFormLine instances (FiPlantSyntheticData + moisture)
			List<FmPlantFormLine> plants = new ArrayList<FmPlantFormLine> ();
			
			for (FmPlantSyntheticData candidate : allPlants.values ()) {
				if (speciesNameEnabled.isSelected ()
					&& !candidate.getSpeciesName ().startsWith (selectedSpeciesName)) {continue;}
				if (heightEnabled.isSelected ()
					&& !selectedHeight.contains (candidate.getHeight ())) {continue;}
				if (crownDiameterEnabled.isSelected ()
					&& !selectedCrownDiameter.contains (candidate.getCrownDiameter ())) {continue;}
					
				// FiPlantSyntheticData -> FiPlantFormLine (additinal moisture set to default value)
				plants.add (new FmPlantFormLine (candidate));
				
			}
			tableModel.setPlants (plants);
			
		} catch (Exception e) {
			Log.println (Log.WARNING, "FiPlantForm ()", 
					"Trouble while updating table, passed", e);
		}
		
	}
	
	/**	Actions (buttons...)
	*/
    public void actionPerformed (ActionEvent e) {
		if (e.getSource () instanceof JCheckBox) {
			synchro ();
			updateTable ();
		} else if (e.getSource ().equals (speciesCombo)) {
			updateTable ();
		} else if (e.getSource ().equals (heightCombo)) {
			updateTable ();
		} else if (e.getSource ().equals (crownDiameterCombo)) {
			updateTable ();

		}
	}

	/**	Ensures the combo boxes are enabled accordingly to 
	*	the checkboxes
	*/
	private void synchro () {
		speciesCombo.setEnabled (speciesNameEnabled.isSelected ());
		heightCombo.setEnabled (heightEnabled.isSelected ());
		crownDiameterCombo.setEnabled (crownDiameterEnabled.isSelected ());
		
	}
	
	/**	Add a listener listening to the selection in our table
	*/
    public void addSelectionListener (ListSelectionListener l) {
		resultTable.getSelectionModel ().addListSelectionListener (l);
    }

    /**	An accessor for the selection from outside
    */
    public List<FmPlantFormLine> getSelection () {
		// Stop current edition if needed
		if (resultTable.isEditing ()) {
		    resultTable.getCellEditor ().stopCellEditing ();
		}
		
		List<FmPlantFormLine> selection = new ArrayList<FmPlantFormLine> ();
		
		int[] selectedRows = resultTable.getSelectedRows ();
		for (int i = 0; i < selectedRows.length; i++) {
			// consider sorting
			selectedRows[i] = resultTable.convertRowIndexToModel (selectedRows[i]);
			// selection is now in terms of the underlying TableModel
			selection.add (tableModel.getPlant (selectedRows[i]));
		}
System.out.println ("FiPlantForm getSelection: "+AmapTools.toString (selectedRows));
		
		return selection;
    }

	/**	Initialize the GUI.
	*/
    public void createUI() {

		// 1. Criteria
		ColumnPanel criteriaPanel = new ColumnPanel (Translator.swap ("FiPlantForm.criteria"), 0, 2);

		// Species
		LinePanel l1 = new LinePanel ();
		speciesNameEnabled = new JCheckBox (Translator.swap ("FiPlantForm.speciesName")+" : ", false);
		speciesNameEnabled.addActionListener (this);
		l1.add (speciesNameEnabled);
		speciesCombo.addActionListener (this);
		l1.add (speciesCombo);
		l1.addStrut0 ();
		criteriaPanel.add (l1);

		// Height
		LinePanel l2 = new LinePanel ();
		heightEnabled = new JCheckBox (Translator.swap ("FiPlantForm.height")+" : ", false);
		heightEnabled.addActionListener (this);
		l2.add (heightEnabled);
		heightCombo.addActionListener(this);
		l2.add (heightCombo);
		l2.addStrut0 ();
		criteriaPanel.add (l2);

		// Crown diameter
		LinePanel l3 = new LinePanel ();
		crownDiameterEnabled = new JCheckBox (Translator.swap ("FiPlantForm.crownDiameter")+" : ", false);
		crownDiameterEnabled.addActionListener (this);
		l3.add (crownDiameterEnabled);
		crownDiameterCombo.addActionListener(this);
		l3.add (crownDiameterCombo);
		l3.addStrut0 ();
		criteriaPanel.add (l3);

		criteriaPanel.addStrut0 ();

		// 2. Results
		JPanel tablePanel = new JPanel (new BorderLayout ());
		tablePanel.setBorder (BorderFactory.createTitledBorder (Translator.swap (
				"FiPlantForm.results")));

		// 2.1 Result Table
		resultTable = new JTable (tableModel);
		// Table may be sorted
		resultTable.setAutoCreateRowSorter (true);
		// Single line selection only
		TableColumn col = resultTable.getColumnModel ().getColumn (0);
		col.setPreferredWidth (150);
		resultTable.getSelectionModel ().setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		tablePanel.add (new JScrollPane (resultTable), BorderLayout.CENTER);
		
		setLayout (new BorderLayout ());
		add (criteriaPanel, BorderLayout.NORTH);
		add (tablePanel, BorderLayout.CENTER);

		setPreferredSize (new Dimension (200, 200));
    }


}
