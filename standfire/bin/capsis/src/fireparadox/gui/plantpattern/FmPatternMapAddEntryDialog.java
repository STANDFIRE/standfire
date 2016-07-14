package fireparadox.gui.plantpattern;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.plant.fmgeom.FmGeom;
import fireparadox.model.plant.fmgeom.FmGeomMap;

/**	Dialog to add an entry in the pattern map
*
*	@author S. Griffon - May 2007
*/
public class FmPatternMapAddEntryDialog extends AmapDialog implements ActionListener {

	private FmModel model;

	private JButton ok;
	private JButton cancel;
	private JButton help;

	private JButton editPatternFrom;
	private JButton editPatternNew;

	private JComboBox specieComboBox;
	private JTextField heightMinTextField;
	private JTextField heightMaxTextField;
	private JComboBox enviComboBox;

	private JComboBox existingPatternComboBox;
	private JComboBox fromPatternComboBox;

	private ButtonGroup buttonGroup;
	private JRadioButton existingPatternRadio;
	private JRadioButton newPatternRadio;
	private JRadioButton fromPatternRadio;

	private String currentCriteria;
	private String currentPattern;
	private Collection speciesList;

	private FmPattern2DPanel pattern2DPanel;


	/**	Constructor.
	 */
	public FmPatternMapAddEntryDialog (FmModel fm) {
		super ();
		model = fm;
        speciesList = new ArrayList<FiSpecies> ();
        try {
			speciesList = model.getSpeciesList();
		} catch (Exception e) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.couldNotGetSpeciesList") + " : " + e.getMessage ());
		}

		createComboBox ();
		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	/**	Constructor for modification of an entry of the pattern map
	 */
	public FmPatternMapAddEntryDialog (FmModel fm, String modifCriteria, FmGeom modifPattern) {
		super ();
		model = fm;
        speciesList = new ArrayList<FiSpecies> ();
        try {
			speciesList = model.getSpeciesList();
		} catch (Exception e) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.couldNotGetSpeciesList") + " : " + e.getMessage ());
		}

		createComboBox ();
		createUI ();
		initFromCurrentCriteria (modifCriteria, modifPattern);
		// location is set by AmapDialog
		pack ();
		show ();
	}

	public String getCurrentCriteria () {
		return currentCriteria;
	}

	public String getCurrentPattern () {
		return currentPattern;
	}

	//	Ok was hit
	//
	private void validateAction () {

		if(controlValues ()) {
			if(checkCoherence () && checkPattern ()) {
				setValidDialog (true);
			}
		}

	}

	private boolean checkPattern () {

		FmGeom selPattern = (FmGeom)fromPatternComboBox.getSelectedItem ();

		if(selPattern != null) {

			currentPattern = String.valueOf (selPattern.getId ());
			return true;

		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.needAPattern"));
			return false;
		}

	}

	private boolean checkCoherence () {
		String selectSpecie = "";
		String selectEnvi = "";
		double heightMin, heightMax;
		selectSpecie = (String) specieComboBox.getSelectedItem ();

		if(heightMinTextField.getText ().length ()== 0) {
			heightMin = - Double.MAX_VALUE;
			//heightMin = -1;
		} else {
			heightMin = Double.parseDouble (heightMinTextField.getText ());
		}


		if(heightMaxTextField.getText ().length ()== 0) {
			heightMax = Double.MAX_VALUE;
			//heightMax = -1;
		} else {
			heightMax = Double.parseDouble (heightMaxTextField.getText ());
		}

		selectEnvi= String.valueOf (enviComboBox.getSelectedIndex ());

		try {
			model.getPatternMap ().checkCoherence (selectSpecie, heightMin, heightMax, selectEnvi);
			currentCriteria=FmGeomMap.createCriteria (selectSpecie, heightMin, heightMax, selectEnvi,model.getPatternMap ().isAdmin ());


		} catch (Exception e) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.conflictCoherence") + " : " + e.getMessage ());
			return false;
		}

		return true;
	}

	//Control the value in the ComboBoxes and the TextFields
	private boolean controlValues ()   {
		String selectSpecie = "";
		String selectEnvi = "";
		double heightMin, heightMax;
		heightMin = heightMax = -1;

		selectSpecie = (String)  specieComboBox.getSelectedItem ();

		if(selectSpecie.equals ("")) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.needSpeciesCriteria"));
			return false;
		}

		if(!heightMinTextField.getText ().equals ("") || !heightMaxTextField.getText ().equals ("") ) {
			if(Check.isDouble (heightMinTextField.getText ()) && Check.isDouble (heightMaxTextField.getText ())) {
				heightMin= Double.parseDouble (heightMinTextField.getText ());
				heightMax= Double.parseDouble (heightMaxTextField.getText ());
				if(heightMin>=heightMax) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.heightMinSupHeightMax"));
					return false;
				}
			} else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternMapAddEntryDialog.heightTextFieldAreNotDouble"));
				return false;
			}
		}

		return true;
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource ().equals (editPatternNew)) {
			editNewPatterns (true);
		} else if (evt.getSource ().equals (editPatternFrom)) {
			editNewPatterns (false);
		} else if (evt.getSource ().equals (existingPatternComboBox) || evt.getSource ().equals (fromPatternComboBox)) {
			refreshPreviewer ();
		}
	}

	private void refreshPreviewer () {
		FmGeom selPattern = (FmGeom)fromPatternComboBox.getSelectedItem ();
		pattern2DPanel.setPattern (selPattern);
	}

	// newPattern = true if we edit a new pattern, false if we edit an existing pattern
	private void editNewPatterns (boolean newPattern) {
		FmPatternEditor fEditor = null;

		if(checkCoherence ()) {

			model.getPatternList ().setNextPatternId ();

			if(newPattern) {

				FmGeom fpatternew = new FmGeom ();

				System.out.println ("Criteria no format : " + currentCriteria);
				fEditor = new FmPatternEditor (currentCriteria , fpatternew);


			} else if (checkPattern ()) {

				FmGeom selPattern = ((FmGeom)fromPatternComboBox.getSelectedItem ()).clone ();
				//The new pattern is clone from the selected pattern but has a different ID !
				selPattern.setId (FmGeom.idFactory.getNext ());
				fEditor = new FmPatternEditor (currentCriteria , selPattern);
			}

			if(fEditor != null && fEditor.isValidDialog ()) {
				try {
					model.getPatternList ().addPattern (fEditor.getCurrentPattern ());
					model.getPatternList ().save ();
					existingPatternComboBox.getModel ().setSelectedItem (fEditor.getCurrentPattern ());
				} catch (Exception e) {
					JOptionPane.showMessageDialog (this,  Translator.swap ("FiPatternMapAddEntryDialog.errorOnWritingPatternList"));
				}
			}
		}
	}

	//	Initialize the GUI.
	private void createUI () {

		//CRITERIA PANEL
		JPanel criteriaAuxPanel = new JPanel ( new BorderLayout ());
		ColumnPanel criteriaPanel = new ColumnPanel ();

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiPatternMapAddEntryDialog.species")+" :", 180));
		l1.add (specieComboBox);
		l1.addStrut0 ();

		LinePanel heightPanel = new LinePanel ();
		heightMinTextField = new JTextField (5);
		heightMaxTextField = new JTextField (5);
		heightPanel.add (new JWidthLabel (Translator.swap ("FiPatternMapAddEntryDialog.intervalHeight")+" :", 180));
		heightPanel.add (new JLabel (" [ "));
		heightPanel.add (heightMinTextField);
		heightPanel.addStrut0 ();
		heightPanel.add (new JLabel (" , "));
		heightPanel.add (heightMaxTextField);
		heightPanel.addStrut0 ();
		heightPanel.add (new JLabel (" [ "));


		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FiPatternMapAddEntryDialog.environment")+" :", 180));
		l3.add (enviComboBox);
		l3.addStrut0 ();

		criteriaPanel.add (l1);
		criteriaPanel.addGlue ();
		criteriaPanel.add (heightPanel);
		criteriaPanel.addGlue ();
		criteriaPanel.add (l3);

		TitledBorder tborder1 = new TitledBorder (criteriaPanel.getBorder (), Translator.swap ("FiPatternMapAddEntryDialog.criteriaLabel"));
		criteriaPanel.setBorder (tborder1);

		criteriaAuxPanel.add (criteriaPanel, BorderLayout.NORTH);

		//PATTERN PANEL
		ColumnPanel patternPanel = new ColumnPanel ();

		buttonGroup = new ButtonGroup ();

		LinePanel l4 = new LinePanel ();
		existingPatternRadio = new JRadioButton (Translator.swap ("FiPatternMapAddEntryDialog.existingPatternRadio"), false);
		existingPatternRadio.addActionListener (this);
		buttonGroup.add (existingPatternRadio);
		l4.add (existingPatternRadio);
		l4.addGlue ();
		l4.add (existingPatternComboBox);
		l4.addStrut0 ();

		LinePanel l5 = new LinePanel ();
		newPatternRadio = new JRadioButton (Translator.swap ("FiPatternMapAddEntryDialog.newPatternRadio"), false);
		newPatternRadio.addActionListener (this);
		editPatternNew = new JButton (Translator.swap ("FiPatternMapAddEntryDialog.newPattern"));
		editPatternNew.addActionListener (this);
		buttonGroup.add (newPatternRadio);
		l5.add (newPatternRadio);
		l5.addGlue ();
		l5.add (editPatternNew);
		l5.addStrut0 ();

		LinePanel l6 = new LinePanel ();
		fromPatternRadio = new JRadioButton (Translator.swap ("FiPatternMapAddEntryDialog.fromPatternRadio"), false);
		fromPatternRadio.addActionListener (this);
		editPatternFrom = new JButton (Translator.swap ("FiPatternMapAddEntryDialog.modifPattern"));
		editPatternFrom.addActionListener (this);
		buttonGroup.add (fromPatternRadio);
		l6.add (fromPatternRadio);
		l6.addGlue ();
		l6.add (fromPatternComboBox);
		l6.addStrut0 ();
		l6.add (editPatternFrom);
		l6.addGlue ();

		patternPanel.add (l4);
		patternPanel.add (l5);
		patternPanel.add (l6);
		TitledBorder tborder2 = new TitledBorder (patternPanel.getBorder (), Translator.swap (
				"FiPatternMapAddEntryDialog.patternLabel"));
		patternPanel.setBorder (tborder2);

		ColumnPanel leftPanel = new ColumnPanel ();
		leftPanel.add (criteriaAuxPanel);
		leftPanel.addStrut0 ();
		leftPanel.add (patternPanel);
		leftPanel.addStrut0 ();

		pattern2DPanel = new FmPattern2DPanel (null);
		TitledBorder tborder4 = new TitledBorder (pattern2DPanel.getBorder (), Translator.swap (
				"FiPatternMapAddEntryDialog.previewer"));
		pattern2DPanel.setBorder (tborder4);
		pattern2DPanel.setPreferredSize (new Dimension (250,350));

		//Control Panel
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("FiPatternMapAddEntryDialog.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		// ov - 26.9.2007
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, pattern2DPanel);
		split.setResizeWeight (0.5);

		//add cancel...
		getContentPane ().setLayout (new BorderLayout ());
		//getContentPane ().add (leftPanel, BorderLayout.WEST);		// ov - 26.9.2007
		//getContentPane ().add (pattern2DPanel, BorderLayout.EAST);// ov - 26.9.2007
		getContentPane ().add (split, BorderLayout.CENTER);			// ov - 26.9.2007
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPatternMapAddEntryDialog.patternDialogTitle"));

		setModal (true);
	}

	//Create the species and environment list displayed in the combo box
	private void createComboBox () {

		String [] enviValue = {Translator.swap ("FiPatternMapAddEntryDialog.openEnv"),
				Translator.swap ("FiPatternMapAddEntryDialog.closeEnv")};
		enviComboBox = new JComboBox (enviValue);
		enviComboBox.setSelectedIndex (1);

		specieComboBox = new JComboBox ();
		if(speciesList == null) {
			Log.println (Log.ERROR,"FiPatternMapAddEntryDialog.updateComboBox", "No firespecies");

		} else {

			for (Iterator i = speciesList.iterator(); i.hasNext ();) {
				FiSpecies sp = (FiSpecies)  i.next();
				String name = sp.getName ();
				specieComboBox.addItem (name);

			}
		}

		DefaultComboBoxModel dataModel = new DefaultComboBoxModel () {

			FmGeom selectedPattern = null;

			@Override
			public Object getElementAt (int index) {
				Object [] entry = model.getPatternList ().entrySet ().toArray ();
				FmGeom value = (FmGeom)((Map.Entry)entry[index]).getValue ();
				return value;
			}

			@Override
			public int getSize () {
				return model.getPatternList ().size ();
			}

		};

		existingPatternComboBox = new JComboBox (dataModel);
		fromPatternComboBox = new JComboBox (dataModel);

		existingPatternComboBox.addActionListener (this);
		fromPatternComboBox.addActionListener (this);

	}

	//Init current value for modification
	private void initFromCurrentCriteria (String modifCriteria, FmGeom modiftPattern) {

		buttonGroup.setSelected (existingPatternRadio.getModel (), true);
		String [] criteriaTok = FmGeomMap.formatCriteria (modifCriteria);


		for (Iterator i = speciesList.iterator(); i.hasNext ();) {
			FiSpecies sp = (FiSpecies) i.next();
			String name = sp.getName();
			if(name.equals (criteriaTok[0]))
				specieComboBox.setSelectedItem (name);
		}

		//If environment == 0 -> select "Opened" in the combo box, 'Closed' otherwise
		if(criteriaTok[3].equals ("0"))
			enviComboBox.setSelectedIndex (0);
		else
			enviComboBox.setSelectedIndex (1);

		heightMinTextField.setText (criteriaTok[1]);

		heightMaxTextField.setText (criteriaTok[2]);

		existingPatternComboBox.getModel ().setSelectedItem (modiftPattern);

	}


}
