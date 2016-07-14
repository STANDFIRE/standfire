/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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

package fireparadox.extension.sketcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.TreeMap;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;

import jeeb.lib.sketch.scene.item.TreeWithCrownProfileItem;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;


/**	ColorRenderer: a cell for the edition table color
 *	@author S. Griffon - june 2007
 */
class ColorRenderer extends JLabel
	implements TableCellRenderer {
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;

	public ColorRenderer (boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque (true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent (
		JTable table, Object color,
		boolean isSelected, boolean hasFocus,
		int row, int column) {
		Color newColor = (Color)color;
		setBackground (newColor);
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder (2,5,2,5,
						table.getSelectionBackground ());
				}
				setBorder (selectedBorder);
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder (2,5,2,5,
						table.getBackground ());
				}
				setBorder (unselectedBorder);
			}
		}

		setToolTipText ("RGB : " + newColor.getRed () + ", "
			+ newColor.getGreen () + ", "
			+ newColor.getBlue ());
		return this;
	}
}

/**	ColorEditor: the color editor to choose and retreive a color
 *	@author S. Griffon - june 2007
 */
class ColorEditor extends AbstractCellEditor
	implements TableCellEditor,
	ActionListener {
	Color currentColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;
	protected static final String EDIT = "edit";

	public ColorEditor () {
		//Set up the editor (from the table's point of view),
		//which is a button.
		//This button brings up the color chooser dialog,
		//which is the editor from the user's point of view.
		button = new JButton ();
		button.setActionCommand (EDIT);
		button.addActionListener (this);
		button.setBorderPainted (false);

		//Set up the dialog that the button brings up.
		colorChooser = new JColorChooser ();
		dialog = JColorChooser.createDialog (button,
			"Pick a Color",
			true,  //modal
			colorChooser,
			this,  //OK button handler
			null); //no CANCEL button handler
	}

	/**
	 * Handles events from the editor button and from
	 * the dialog's OK button.
	 */
	public void actionPerformed (ActionEvent e) {
		if (EDIT.equals (e.getActionCommand ())) {
			//The user has clicked the cell, so
			//bring up the dialog.
			button.setBackground (currentColor);
			colorChooser.setColor (currentColor);
			dialog.setVisible (true);

			//Make the renderer reappear.
			fireEditingStopped ();

		} else { //User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor ();
		}
	}

	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue () {
		return currentColor;
	}

	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent (JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {
		currentColor = (Color)value;
		return button;
	}

	public JButton getButton () {
		return button;
	}


}

/**	FirePatternSketcherPanel: a configuration panel
 *	for FirePatternSketcher
 *	@author S. Griffon - june 2007
 */
public class FirePatternSketcherPanel extends InstantPanel {
	private FirePatternSketcher sketcher;

	private JCheckBox trunkEnabled;

	private JCheckBox crownEnabled;

	private JCheckBox renderFlat;
	private JCheckBox renderLight;
	private JRadioButton renderOutline;
	private JRadioButton renderFilled;

	private ButtonGroup group2;
//	private ButtonGroup group3;
	private ButtonGroup group4;

	private JRadioButton renderColorSpecies;
	private JRadioButton renderColorStratum;
	private JRadioButton renderColorDamage;
	private JRadioButton renderColorGlobal;

	private JScrollPane scrollColor;

	private JTextField threshold;

	private boolean damageAvailable;

	private JTextField qualityNote; // rendering quality

	private boolean qualityModified = false; //boolean to know if quality has been modified by user


	/**	Constructor.
	 */
	public FirePatternSketcherPanel (FirePatternSketcher sketcher, double qualityNote) {
		super (sketcher);
		this.sketcher = sketcher;
		// FP add default value for 
		// fc - 9.9.2009 - Test if the damage data are available
		// Look in the first plant if one of the numerical damage properties is -1
		//~ TreeWithCrownProfileItem item = (TreeWithCrownProfileItem) sketcher.getItems ().iterator ().next ();
		//~ FiPlant ft = (FiPlant) item.getTree ();
		//~ damageAvailable = ft.getSeverity() != null;
		createUI ();
		this.qualityNote.setText ((int) (100 * qualityNote) + "");		
		update ();
	}

	/**	May be called by the sketcher if the context changed, e.g. trees
	*	were added, removed or updated. We can here update the options
	*	e.g. damageAvailable.
	*/
	public void update () {
		// Test if the damage data are available
		// Look in the first plant if one of the numerical damage properties is -1
		damageAvailable = false;
		if (sketcher.getItems () != null && !sketcher.getItems ().isEmpty ()) {
			TreeWithCrownProfileItem item = (TreeWithCrownProfileItem) sketcher.getItems ().iterator ().next ();
			FiPlant ft = (FiPlant) item.getTree ();
			damageAvailable = (ft.getSeverity() != null);
		}
		
		// If no damage available, disable button
		renderColorDamage.setEnabled (damageAvailable);
		
		// If damage button is selected and no damage available, change selection to global
		if (renderColorDamage.isSelected () && !renderColorDamage.isEnabled ()) {
			renderColorDamage.setSelected (false);
			renderColorGlobal.setSelected (true);
			// Update the Sketcher
			isCorrect ();
		}
		
	}
	
	/**	InstantPanel interface.
	 */
	@Override
	public boolean isCorrect () {
		// 1. Controls
		if(threshold != null) {
			if(threshold.getText ().length ()==0) {
				threshold.setText (String.valueOf (sketcher.stratumThreshold));
			}
			if (!Check.isDouble (threshold.getText ().trim ())) {
				MessageDialog.print (this, Translator.swap ("FirePatternSketcher.stratumThresholdShouldBeADouble"));
				return false ;
			}
			sketcher.stratumThreshold = Check.doubleValue (threshold.getText ().trim ());
		}
		if (!Check.isDouble (qualityNote.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FirePatternSketcher.qualityNoteShouldBeADouble"));
			return false ;
		}
		
		if (Check.doubleValue (qualityNote.getText ().trim ()) < 0d) {
			MessageDialog.print (this, Translator.swap ("FirePatternSketcher.qualityNoteShouldBePositive"));
			return false ;
		}
		if (Check.doubleValue (qualityNote.getText ().trim ()) > 100d) {
			MessageDialog.print (this, Translator.swap ("FirePatternSketcher.qualityNoteShouldBeLowerToHundred"));
			return false ;
		}
		sketcher.trunkEnabled = trunkEnabled.isSelected ();

		sketcher.crownEnabled = crownEnabled.isSelected ();

		sketcher.renderOutline = renderOutline.isSelected ();

		sketcher.renderFilled = renderFilled.isSelected ();

		sketcher.renderFilledLight = renderLight.isSelected ();

		sketcher.labelColor = new Color (51, 0, 102);
		sketcher.trunkColor = Color.BLACK;
		sketcher.cellColor = Color.GRAY;
		sketcher.selectionColor = new Color (207, 74, 7);

		sketcher.colorSpecies = renderColorSpecies.isSelected ();
		sketcher.colorStratum = renderColorStratum.isSelected ();
		sketcher.colorDamage = renderColorDamage.isSelected ();
		sketcher.colorGlobal = renderColorGlobal.isSelected ();
		sketcher.renderFilledFlat = renderFlat.isSelected ();

		return true;
	}

	/**	Called when something changes in config
	 *	(e.g.: a check box was changed...)
	 *	It will notify the Drawer listener.
	 */
	@Override
	public void actionPerformed (ActionEvent e) {

		if (e.getSource ().equals (renderColorGlobal)) {
			sketcher.refreshShapePattern = true;
			createColorGlobalPanel ();
		} else if (e.getSource ().equals (renderColorSpecies)) {
			sketcher.refreshShapePattern = true;
			createColorSpeciesPanel ();
		} else if (e.getSource ().equals (renderColorStratum)) {
			sketcher.refreshShapePattern = true;
			createColorStratumPanel ();
		} else if (e.getSource ().equals (renderColorDamage)) {
			sketcher.refreshShapePattern = true;
			createColorDamagePanel ();
		} else if (e.getSource ().equals (threshold)) {
			sketcher.refreshShapePattern = true;
			renderColorStratum.setSelected (true);
			createColorStratumPanel ();
		} else if (e.getSource ().equals (qualityNote)) {
			qualityModified  = true;
			sketcher.refreshShapePattern = true;
		} else if(e.getSource ().equals (renderOutline) || e.getSource ().equals (renderFilled)) {
			sketcher.refreshShapePattern = true;
		} else if (e.getSource ().equals (renderFlat) ) {
			sketcher.renderFilledFlat = renderFlat.isSelected ();
		}
		super.actionPerformed (e);
		sketcher.store ();

	}

	/**	Initializes the GUI.
	 */
	private void createUI () {
		Border etched = BorderFactory.createEtchedBorder ();
		//~ ColumnPanel part1 = new ColumnPanel (Translator.swap ("FirePatternSketcher.title"), 0, 0);
		//~ Border b = BorderFactory.createTitledBorder (etched, Translator.swap ("FirePatternSketcher.labels"));

		// Lighter design, underlining
		ColumnPanel part1 = new ColumnPanel (0, 0);
		part1.setMargin (5);
		part1.add (LinePanel.getTitle1 (Translator.swap ("FirePatternSketcher.title")));

		part1.newLine ();

		ColumnPanel l10 = new ColumnPanel (0,0);
		group4 = new ButtonGroup ();
		LinePanel lcl0 = new LinePanel (0,0);
		renderColorGlobal = new JRadioButton (Translator.swap ("FirePatternSketcherPanel.renderColorGlobal"));
		renderColorGlobal.setSelected (sketcher.colorGlobal);
		lcl0.add (renderColorGlobal);
		lcl0.addGlue ();
		LinePanel lcl1 = new LinePanel (0,0);
		renderColorSpecies = new JRadioButton (Translator.swap ("FirePatternSketcherPanel.renderColorSpecies"));
		renderColorSpecies.setSelected (sketcher.colorSpecies);
		lcl1.add (renderColorSpecies);
		lcl1.addGlue ();
		LinePanel lcl2 = new LinePanel (0,0);
		renderColorStratum = new JRadioButton (Translator.swap ("FirePatternSketcherPanel.renderColorStratum")+" ");
		renderColorStratum.setSelected (sketcher.colorStratum);
		lcl2.add (renderColorStratum);
		lcl2.add (new JLabel (Translator.swap ("FirePatternSketcherPanel.threshold")));
		threshold = new JTextField ();
		threshold.setText (String.valueOf (sketcher.stratumThreshold));
		threshold.addActionListener (this);
		lcl2.add (threshold);
		lcl2.addGlue ();

		LinePanel lcl3 = new LinePanel (0,0);
		renderColorDamage =  new JRadioButton (Translator.swap ("FirePatternSketcherPanel.renderColorDamage"));
		renderColorDamage.setSelected (sketcher.colorDamage);
		lcl3.add (renderColorDamage);
		lcl3.addGlue ();

		l10.add (lcl0);
		l10.add (lcl1);
		l10.add (lcl2);
		l10.add (lcl3);

		renderColorSpecies.addActionListener (this);
		renderColorDamage.addActionListener (this);
		renderColorStratum.addActionListener (this);
		renderColorGlobal.addActionListener (this);

		group4.add (renderColorGlobal);
		group4.add (renderColorSpecies);
		group4.add (renderColorStratum);
		group4.add (renderColorDamage);

		LinePanel lScroll1 = new LinePanel (0,0);
		scrollColor = new JScrollPane ();
		scrollColor.setPreferredSize (new Dimension (100,120));
		lScroll1.add (scrollColor);
		lScroll1.addStrut0 ();

		TitledBorder tborder1 = new TitledBorder (l10.getBorder (), Translator.swap ("FirePatternSketcher.colors"));
		l10.setBorder (tborder1);
		l10.add (lScroll1);

		part1.add (l10);

		// Crown
		ColumnPanel p2 = new ColumnPanel (0, 0);
		Border b = BorderFactory.createTitledBorder (etched, Translator.swap ("FirePatternSketcher.rendering"));
		p2.setBorder (b);

		LinePanel l19 = new LinePanel ();
		l19.add(new JWidthLabel (Translator.swap ("FirePatternSketcher.sceneQualityNote") + " : ", 200));
		qualityNote = new JTextField (5);
		qualityNote.addActionListener (this);
		l19.add (qualityNote);
		l19.addStrut0 ();
		p2.add (l19);
		
		LinePanel l20 = new LinePanel ();
		trunkEnabled = new JCheckBox (Translator.swap ("FirePatternSketcher.trunkEnabled"));
		trunkEnabled.setSelected (sketcher.trunkEnabled);
		trunkEnabled.addActionListener (this);
		l20.add (trunkEnabled);
		l20.addGlue ();
		p2.add (l20);

		LinePanel l21 = new LinePanel ();
		crownEnabled = new JCheckBox (Translator.swap ("FirePatternSketcher.crownEnabled"));
		crownEnabled.setSelected (sketcher.crownEnabled);
		crownEnabled.addActionListener (this);
		l21.add (crownEnabled);
		l21.addGlue ();
		p2.add (l21);

		LinePanel l4 = new LinePanel ();
		renderOutline = new JRadioButton (Translator.swap ("FirePatternSketcher.renderOutline"));
		renderOutline.setSelected (sketcher.renderOutline);
		renderOutline.addActionListener (this);
		l4.add (renderOutline);
		l4.addGlue ();
		p2.add (l4);

		LinePanel l5 = new LinePanel ();
		renderFilled = new JRadioButton (Translator.swap ("FirePatternSketcher.renderFilled"));
		renderFilled.setSelected (sketcher.renderFilled);
		renderFilled.addActionListener (this);
		l5.add (renderFilled);
		l5.addGlue ();
		p2.add (l5);

		group2 = new ButtonGroup ();
		group2.add (renderOutline);
		group2.add (renderFilled);

		LinePanel l6 = new LinePanel ();
		renderFlat = new JCheckBox (Translator.swap ("FirePatternSketcher.renderFlat"));
		renderFlat.setSelected (sketcher.renderFilledFlat);
		renderFlat.addActionListener (this);
		l6.add (renderFlat);
		l6.addGlue ();
		p2.add (l6);

		LinePanel l7 = new LinePanel ();
		renderLight = new JCheckBox (Translator.swap ("FirePatternSketcher.renderLight"));
		renderLight.setSelected (sketcher.renderFilledLight);
		renderLight.addActionListener (this);
		l7.add (renderLight);
		l7.addGlue ();
		p2.add (l7);

		part1.add (p2);

		setLayout (new BorderLayout ());
		add (part1, BorderLayout.NORTH);	// was CENTER

		refreshColorTable();

	}

	public void setQualityNote(double value) {
		qualityNote.setText ((int) (100 * value) + "");
	}
	
	public double getQualityNote() {
		return(0.01 * Check.doubleValue (qualityNote.getText ().trim ()));
	}
	
	public boolean isQualityModified() {
		return qualityModified;
	}
	
	public void refreshColorTable () {

		if(sketcher.colorSpecies) {
			createColorSpeciesPanel ();
		} else if (sketcher.colorStratum) {
			createColorStratumPanel ();
		} else if (sketcher.colorDamage) {
			createColorDamagePanel ();
		} else if (sketcher.colorGlobal) {
			createColorGlobalPanel ();
		}
		
	}



	// Color Panels  and Table color model
	class ColorTableModel extends AbstractTableModel {
		protected String[] columnNames;
		protected Object[][] data;

		public int getColumnCount () {
			return columnNames.length;
		}

		public int getRowCount () {
			return data.length;
		}

		@Override
		public String getColumnName (int col) {
			return columnNames[col];
		}

		public Object getValueAt (int row, int col) {
			return data[row][col];
		}

		public void setData (Object [][] data) {
			this.data = data;
		}

		public void setColumnNames (String [] cname) {
			this.columnNames = cname;
		}

		/**
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		@Override
		public Class getColumnClass (int c) {
			try {
				return getValueAt (0, c).getClass ();
			} catch (Exception e) {
				return Object.class;	// bug, nullPointerException
			}
		}

		@Override
		public boolean isCellEditable (int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public void setValueAt (Object value, int row, int col) {

			//tips to refresh the 3D view when color value change
			int id = 0;	//unused
			String command = "option changed";
			ActionEvent e2 = new ActionEvent (this, id, command);
			actionPerformed (e2);

			fireTableCellUpdated (row, col);
		}
	}


	class ColorSpeciesTableModel extends ColorTableModel {
		@Override
		public void setValueAt (Object value, int row, int col) {
			data[row][col] = value;
			sketcher.speciesColor.put ((String)data[row][0], (Color)value);
			sketcher.store ();
			super.setValueAt (value, row, col);
		}
	}

	
	class ColorStratumTableModel extends ColorTableModel {
		@Override
		public void setValueAt (Object value, int row, int col) {
			data[row][col] = value;
			sketcher.stratumColor.set (row,(Color)value);
			sketcher.store ();
			super.setValueAt (value, row, col);
		}
	}

	class ColorFireDamageTableModel extends ColorTableModel {
		@Override
		public void setValueAt (Object value, int row, int col) {
			data[row][col] = value;
			sketcher.fireDamageColor.set (row,(Color)value);
			sketcher.refreshShapePattern = true;
			sketcher.store ();
			super.setValueAt (value, row, col);
		}
	}

	class ColorGlobalTableModel extends ColorTableModel {
		@Override
		public void setValueAt (Object value, int row, int col) {
			data[row][col] = value;
			sketcher.crownColor = (Color)value;
			sketcher.refreshShapePattern = true;
			sketcher.store ();
			super.setValueAt (value, row, col);
		}
	}

	private void createColorGlobalPanel () {

		ColorGlobalTableModel colGlobalTableModel = new ColorGlobalTableModel ();

		Object [][] data = new Object [1] [];

		data [0] = new Object [2];
		data [0][0] = Translator.swap ("FirePatternSketcher.colorsGlobal");
		data [0][1] = sketcher.crownColor;

		colGlobalTableModel.setData (data);
		String [] name = {Translator.swap ("FirePatternSketcherPanel.allSpecies"),
		Translator.swap ("FirePatternSketcher.colors")};
		colGlobalTableModel.setColumnNames (name);

		JTable tableColorGlobam = new JTable (colGlobalTableModel);
		// Set up renderer and editor for the Favorite Color column.
		tableColorGlobam.setDefaultRenderer (Color.class,
			new ColorRenderer (true));
		tableColorGlobam.setDefaultEditor (Color.class,
			new ColorEditor ());

		scrollColor.getViewport ().setView (tableColorGlobam);
	}

	private void createColorDamagePanel () {

		ColorFireDamageTableModel colDamageTableModel = new ColorFireDamageTableModel ();

		String [] nameVar = {Translator.swap ("FirePatternSketcher.colorCrown"),
		Translator.swap ("FirePatternSketcher.crownScorchedHeight"),
		Translator.swap ("FirePatternSketcher.crownKilledHeight"),
		Translator.swap ("FirePatternSketcher.colorTrunk"),
		Translator.swap ("FirePatternSketcher.hCMax"),
		Translator.swap ("FirePatternSketcher.hCMin")
		};

		Object [][] data = new Object [6] [];

		for (int i=0; i<6; i++) {
			data [i] = new Object [2];
			data [i][0] = nameVar[i];
			data [i][1] = sketcher.fireDamageColor.get (i);
		}

		colDamageTableModel.setData (data);
		String [] name = {Translator.swap ("FirePatternSketcherPanel.height"),
		Translator.swap ("FirePatternSketcher.colors")};
		colDamageTableModel.setColumnNames (name);

		JTable tableColorDamage = new JTable (colDamageTableModel);
		// Set up renderer and editor for the Favorite Color column.
		tableColorDamage.setDefaultRenderer (Color.class,
			new ColorRenderer (true));
		tableColorDamage.setDefaultEditor (Color.class,
			new ColorEditor ());

		scrollColor.getViewport ().setView (tableColorDamage);
	}

	private void createColorStratumPanel () {

		ColorStratumTableModel colStratumTableModel = new ColorStratumTableModel ();

		Object [][] data = new Object [2] [];

		data [0] = new Object [2];
		data [0][0] = "< "+ sketcher.stratumThreshold;
		data [0][1] = sketcher.stratumColor.get (0);

		data [1] = new Object [2];
		data [1][0] = ">= "+ sketcher.stratumThreshold;
		data [1][1] = sketcher.stratumColor.get (1);

		colStratumTableModel.setData (data);
		String [] name = {Translator.swap ("FirePatternSketcherPanel.stratum"),
		Translator.swap ("FirePatternSketcher.colors")};
		colStratumTableModel.setColumnNames (name);

		JTable tableColorStratum = new JTable (colStratumTableModel);
		// Set up renderer and editor for the Favorite Color column.
		tableColorStratum.setDefaultRenderer (Color.class,
			new ColorRenderer (true));
		tableColorStratum.setDefaultEditor (Color.class,
			new ColorEditor ());

		scrollColor.getViewport ().setView (tableColorStratum);

	}

	private void createColorSpeciesPanel () {

		ColorSpeciesTableModel colSpeciesTableModel = new ColorSpeciesTableModel ();

		TreeWithCrownProfileItem item = (TreeWithCrownProfileItem) sketcher.getItems ().iterator ().next ();
		FiPlant ft = (FiPlant) item.getTree ();
		FmStand stand = (FmStand) ft.getScene ();

		Collection<FiSpecies> standSpeciesList = FmStand.getStandSpeciesList (stand);
//		FiModel model = stand.getModel ();
//		Collection<FiSpecies> standSpeciesList=model.getStandSpeciesList ();

		TreeMap<String, Color> speciesColor = sketcher.speciesColor;
		Object [][] data = new Object [standSpeciesList.size ()] [];

		int specCount = 0;
		for (FiSpecies fs : standSpeciesList) {
			//~ System.out.println("RENDER species trait ="+fs.getTrait ()+" genus ="+fs.getGenus ()+" name ="+fs.getName ());
			data [specCount] = new Object [2];
			data [specCount][0] = fs.getName ();
			data [specCount][1] = speciesColor.get (fs.getName ());
			
			specCount++;

		}

		colSpeciesTableModel.setData (data);
		String [] name = {Translator.swap ("FirePatternSketcherPanel.species"),
		Translator.swap ("FirePatternSketcher.colors")};
		colSpeciesTableModel.setColumnNames (name);

		JTable tableColorSpecies = new JTable (colSpeciesTableModel);
		// Set up renderer and editor for the Favorite Color column.
		tableColorSpecies.setDefaultRenderer (Color.class,
			new ColorRenderer (true));
		tableColorSpecies.setDefaultEditor (Color.class,
			new ColorEditor ());
		scrollColor.getViewport ().setView (tableColorSpecies);
	}

}

