package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.lib.fire.FiConstants;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmLayerFromDB;
import fireparadox.model.database.FmLayerSetFromDB;
import fireparadox.model.database.FmLayerSyntheticDataBaseData;
import fireparadox.model.layerSet.FmLayer;


/**	A form to select layers in the fuel database to create a layerSet, can
*	also be used to browse or edit an existing layerSet.
*	The form is based on buffers (see model class), no access to the database here.
*	A listener can register with addListener (Listener l) and it will
*	be told any time the somethingHappens. It can get the selection with
*	Collection<FiLayer> getLayers ().
*	@author F. de Coligny - march 2009
*/
public class FmLayerFormFromDB extends JPanel implements ActionListener,
		TableModelListener, ListenedTo {
	// fc sept 2009 review

	public static final String SHOW_EDITOR = Translator.swap ("FiLayerForm.showEditor");
	public static final String HIDE_EDITOR = Translator.swap ("FiLayerForm.hideEditor");

	private FmModel model;
	private FmInitialParameters settings;

	private JTable table1;
	private FmLayerFormTableModel1 table1Model;

	private JTable table2;
	private FmLayerFormTableModel2 table2Model;

	private Collection<Listener> listeners;
	private JButton add;
	private JButton remove;
	private JButton clear;

	private boolean mute;

	// We search layers with height between layerHeight0 and layerHeight1
	// and bottom height between layerBottomHeight0 and layerBottomHeight1
	// all unspecified values are ignored
	private JTextField layerHeight0;
	private JTextField layerHeight1;
	private JTextField layerBottomHeight0;
	private JTextField layerBottomHeight1;

	private JTextField percentageSum;

	private boolean creationMode;			// creation / edition
	private boolean withShowHideEditor;		// if edition, with / without button below
	private JButton showHideEditor;
	private LinePanel showHideEditorLine;

	private ColumnPanel part1;
	private ColumnPanel part2;
	private JSplitPane split;

	private Collection<FmLayer> layers; // The final layers


	/**	Constructor 1 - creation mode: a FiLayerForm for creating a new
	*	FiLayerSet interactively.
	*/
	public FmLayerFormFromDB (FmModel model) {

		this.model = model;
		settings = model.getSettings();

		this.creationMode = true;
		this.withShowHideEditor = false;

		table1Model = new FmLayerFormTableModel1 ();
		table1Model.addTableModelListener (this);

		table2Model = new FmLayerFormTableModel2 ();
		table2Model.addTableModelListener (this);

		prepareUI ();
		refreshUI ();
		updateTable1 ();

	}

	/**	Constructor 2 - edition: a FiLayerForm for editing an existing FiLayerSet.
	*	We are editing: creationMode is false. If withShowHideEditor is true, show the
	*	showHideEditor button.
	*/
	public FmLayerFormFromDB (FmModel model, FmLayerSetFromDB layerSet, boolean withShowHideEditor) {

		this.model = model;
		settings = model.getSettings();

		this.creationMode = false;
		this.withShowHideEditor = withShowHideEditor;

		table1Model = new FmLayerFormTableModel1 ();
		table1Model.addTableModelListener (this);

		table2Model = new FmLayerFormTableModel2 ();
		table2Model.addTableModelListener (this);

		prepareUI ();
		refreshUI ();
		updateTable1 ();	// first load : no specific criteria

		setLayerSet (layerSet);

    }

	/**	Set the given layerSet in the form.
	*/
	public void setLayerSet (FmLayerSetFromDB layerSet) {
		// Copy the layers of the layerSet to be edited into the table2Model
		table2Model.setLayers (layerSet.getFmLayers ());
	}

	/**	Called when table2 is edited.
	*/
	public void tableChanged (TableModelEvent e) {
		if (e.getSource ().equals (table2Model)) {
			updateCoverFractionSum ();
			
			if (mute) {return;}
			tellSomethingHappened (null);
		}
	}

	/**	Add button.
	*/
	private void addAction () {
		int[] selectedRows = table1.getSelectedRows ();
		if (selectedRows.length == 0) {return;}

		for (int cpt = 0; cpt < selectedRows.length; cpt++) {
			// consider sorting
			selectedRows[cpt] = table1.convertRowIndexToModel (selectedRows[cpt]);
			// selection is now in terms of the underlying TableModel
			try {
				table2Model.addLayer ((FmLayer) table1Model.getLayer (selectedRows[cpt]).copy ());
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		}
		tellSomethingHappened (null);
	}

	/**	Remove button.
	*/
	private void removeAction () {
		int[] selectedRows = table2.getSelectedRows ();
		if (selectedRows.length == 0) {return;}
		int sel = 0;
		for (int cpt = 0; cpt < selectedRows.length; cpt++) {
			// consider sorting
			selectedRows[cpt] = table2.convertRowIndexToModel (selectedRows[cpt]);
			// selection is now in terms of the underlying TableModel
			sel = selectedRows[cpt];
			table2Model.removeLayer (table2Model.getLayer (sel));
		}
		// reselect some layer
		try {
			if (sel > table2Model.getRowCount () - 1) {sel = table2Model.getRowCount () - 1;}
			table2.getSelectionModel ().setSelectionInterval (sel, sel);
		} catch (Exception e) {
			table2.getSelectionModel ().setSelectionInterval (0, 0);
		}
		tellSomethingHappened (null);
	}

	/**	Clear button.
	*/
	private void clearAction () {
		table2Model.clear ();
		tellSomethingHappened (null);
	}

	/**	Show / Hide editor button.
	*/
	private void showHideEditorAction () {
		if (showHideEditor.getText ().equals (SHOW_EDITOR)) {
			showHideEditor.setText (HIDE_EDITOR);
			if (table1Model.getRowCount () == 0) {
				updateTable1 ();	// first load : no specific criteria
			}

		} else {
			showHideEditor.setText (SHOW_EDITOR);
		}
		refreshUI ();
	}

	/**	A button was hit or enter was hit in a filter field.
	*/
	public void actionPerformed (ActionEvent e) {
		if (e.getSource ().equals (add)) {
			addAction ();
		} else if (e.getSource ().equals (remove)) {
			removeAction ();
		} else if (e.getSource ().equals (clear)) {
			clearAction ();
		} else if (e.getSource ().equals (showHideEditor)) {
			showHideEditorAction ();
		} else {
			updateTable1 ();
		}
	}

	/**	Tests correctness of the form.
	*/
	public boolean isCorrect () {

		// Check all user entries, in case of trouble, tell him and return false
		Collection<FmLayer> layers = getLayers ();

		// No layers added
		if (layers == null || layers.isEmpty ()) {
			MessageDialog.print (this,
					Translator.swap ("FiLayerForm.fiLayerForm-pleaseAddAtLeastOneLayerInTheResultTable"));
			return false;
		}

		// If table is under edition, finish edition -> TROUBLE
		//~ if (table2.isEditing ()) {
			//~ MessageDialog.print (this,
					//~ Translator.swap ("FiLayerForm.pleaseFinishTableEdition"));
			//~ return false;
		//~ }

		// Check characteristic size >= 0
		for (FiLayer layer : layers) {
			if (layer.getCharacteristicSize () < 0) {
				MessageDialog.print (this,
						Translator.swap ("FiLayerForm.fiLayerForm-allCharacteristicSizesMustBeGreaterOrEqualToZero"));
				return false;
			}
		}

		// Check fraction > 0
		double sum = 0;
		for (FiLayer layer : layers) {
			if (layer.getCoverFraction () <= 0) {
				MessageDialog.print (this,
						Translator.swap ("FiLayerForm.fiLayerForm-allCoversMustBeGreaterThanZero"));
				return false;
			}
			sum += layer.getCoverFraction ();
		}

		// Check percentage sum <= 1
		if (sum > 1) {
			MessageDialog.print (this,
					Translator.swap ("FiLayerForm.fiLayerForm-sumOfTheCoversMustBeLowerOrEqualTo1"));
			return false;
		}
		for (FmLayer layer : layers) {
			try {
				if (layer.getMoisture (0, FiParticle.LIVE) < 0 || layer.getMoisture (0, FiParticle.DEAD) < 0) {
					MessageDialog.print (this, Translator
							.swap ("FiLayerForm.fiLayerForm-allMoistureMustBeGreaterOrEqualToZero"));
					return false;
				}
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		}

		return true;
	}

	/**
	 * Count the coverfraction column and cumulate
	 */
	private void updateCoverFractionSum () {
		if (percentageSum == null) {return;}	// too early
		double sum = 0;
		for (FiLayer layer : table2Model.getLayers ()) {
			sum += layer.getCoverFraction ();
		}
		percentageSum.setText (""+sum);
	}

	/**	Get the filter low threshold value for height.
	*/
	private double getHeight0 () {
		// if not set, return 0
		if (layerHeight0.getText ().trim ().length () == 0) {return 0;}
		try {
			if (!Check.isDouble (layerHeight0.getText ().trim ())) {throw new Exception ();}
			double v = Check.doubleValue (layerHeight0.getText ().trim ());
			if (v >= 0) {return v;}		// if value is ok, return it directly
		} catch (Exception e) {}
		MessageDialog.print (this, Translator.swap ("FiLayerForm.fiLayerForm-layerHeight0ShouldBeAPositiveNumber"));
		return -1;
	}

	/**	Get the filter high threshold value for height.
	*/
	private double getHeight1 () {
		// if not set, return max possible value
		if (layerHeight1.getText ().trim ().length () == 0) {return Double.MAX_VALUE;}
		try {
			if (!Check.isDouble (layerHeight1.getText ().trim ())) {throw new Exception ();}
			double v = Check.doubleValue (layerHeight1.getText ().trim ());
			if (v >= 0) {return v;}		// if value is ok, return it directly
		} catch (Exception e) {}
		MessageDialog.print (this, Translator.swap ("FiLayerForm.fiLayerForm-layerHeight1ShouldBeAPositiveNumber"));
		return -1;
	}

	/**	Get the filter low threshold value for bottom height.
	*/
	private double getBottomHeight0 () {
		// if not set, return 0
		if (layerBottomHeight0.getText ().trim ().length () == 0) {return 0;}
		try {
			if (!Check.isDouble (layerBottomHeight0.getText ().trim ())) {throw new Exception ();}
			double v = Check.doubleValue (layerBottomHeight0.getText ().trim ());
			if (v >= 0) {return v;}		// if value is ok, return it directly
		} catch (Exception e) {}
		MessageDialog.print (this, Translator.swap ("FiLayerForm.fiLayerForm-layerBottomHeight0ShouldBeAPositiveNumber"));
		return -1;
	}

	/**	Get the filter high threshold value for bottom bottom height.
	*/
	private double getBottomHeight1 () {
		// if not set, return max possible value
		if (layerBottomHeight1.getText ().trim ().length () == 0) {return Double.MAX_VALUE;}
		try {
			if (!Check.isDouble (layerBottomHeight1.getText ().trim ())) {throw new Exception ();}
			double v = Check.doubleValue (layerBottomHeight1.getText ().trim ());
			if (v >= 0) {return v;}		// if value is ok, return it directly
		} catch (Exception e) {}
		MessageDialog.print (this, Translator.swap ("FiLayerForm.fiLayerForm-layerBottomHeight1ShouldBeAPositiveNumber"));
		return -1;
	}

	/**	Load layers from the fuel database buffers in the table1 according to
	*	the current bottomHeight / height filters.
	*/
	public void updateTable1 () {

		if (!isEditorShown ()) {return;}

		// Get the current selection criteria
		double h0 = getHeight0 ();
		double h1 = getHeight1 ();
		double bh0 = getBottomHeight0 ();
		double bh1 = getBottomHeight1 ();
		// if trouble in the 4 layers upper, they sent a message
		// to the user and returned -1 -> just return
		if (h0 < 0 || h1 < 0 || bh0 < 0 || bh1 < 0) {return;}

		table1Model.clear ();
		try {
				//~ if (table1Model.getRowCount () == 0) {}
			
			// Get the FiLayerSyntheticDataBaseData instances
			Map<Long,FmLayerSyntheticDataBaseData> allLayers = model.getLayerSyntheticMap ();

			for (FmLayerSyntheticDataBaseData candidate : new TreeSet<FmLayerSyntheticDataBaseData> (allLayers.values ())) {
			//~ for (FiLayerSyntheticDataBaseData candidate : allLayers.values ()) {

				// Filters
				double height = candidate.getHeight ();
				double bottomHeight = candidate.getBottomHeight ();
				if (height < h0 || height > h1) {continue;}
				if (bottomHeight < bh0 || bottomHeight > bh1) {continue;}

				

				// Additional properties
				double characteristicSize = 0d;
				double percentage = 1d;
				double liveMoisture;
				int spatialGroup = 0;
				if (height <= FiConstants.HEIGHT_THRESHOLD) {
					liveMoisture = FiConstants.SHRUB_MOISTURE;
				} else {
					liveMoisture = FiConstants.TREE_MOISTURE;
				}
				double deadMoisture = FiConstants.DEAD_MOISTURE;
				// Add layer in table1
				
				FmLayerFromDB layer = new FmLayerFromDB(candidate, percentage,
						characteristicSize, spatialGroup, liveMoisture,
						deadMoisture);
				table1Model.addLayer (layer);
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayerForm.updateTable1 ()", "Error while reading Fuel database", e);
		}
	}

	/**	Accessor for the layer layers in table2: the current 'layerSet'.
	*/
	public Collection<FmLayer> getLayers () {
		// Stop current edition if needed
		if (table2.isEditing ()) {
			mute = true;
			table2.getCellEditor ().stopCellEditing ();
			mute = false;
		}

		List<FmLayer> resultLayers = new ArrayList<FmLayer> ();;
		for (FmLayer layer : table2Model.getLayers ()) {
			resultLayers.add (layer);
		}
		return resultLayers;
	}

	/**	ListenedTo interface. Add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new ArrayList<Listener> ();}
		listeners.add (l);
	}

	/**	ListenedTo interface. Remove a listener to this object.
	*/
	public void removeListener (Listener l) {
		if (listeners == null) {return;}
		listeners.remove (l);
	}

	/**	ListenedTo interface. Notify all the listeners by calling their somethingHappened (listenedTo, param) method.
	*/
	public void tellSomethingHappened (Object param) {
		if (listeners == null) {return;}
		for (Listener l : listeners) {
			l.somethingHappened (this, param);	// param may be null
		}
	}

	/**	Initialize the GUI.
	*/
	public void prepareUI () {

		// Show/hide editor button layer
		showHideEditorLine = new LinePanel ();
		showHideEditorLine.addGlue ();
		showHideEditorLine.add (new JLabel (Translator.swap ("FiLayerForm.toEditTheLayerSet")+" : "));
		showHideEditor = new JButton (SHOW_EDITOR);
		showHideEditor.addActionListener (this);
		showHideEditorLine.add (showHideEditor);
		showHideEditorLine.addStrut0 ();

		// Part1. LayerSet editor
		part1 = new ColumnPanel (0, 0);

		// 1.1 Selection criteria
		ColumnPanel criteriaPanel = new ColumnPanel ();

		// Top height thresholds
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiLayerForm.layerHeightBetWeen")+" : ", 200));
		layerHeight0 = new JTextField (3);
		layerHeight0.addActionListener (this);
		l1.add (layerHeight0);

		l1.add (new JWidthLabel (Translator.swap ("FiLayerForm.and"), 20));
		layerHeight1 = new JTextField (3);
		layerHeight1.addActionListener (this);
		l1.add (layerHeight1);
		l1.addStrut0 ();
		criteriaPanel.add (l1);

		// Bottom height thresholds
		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FiLayerForm.layerBottomHeightBetWeen")+" : ", 200));
		layerBottomHeight0 = new JTextField (3);
		layerBottomHeight0.addActionListener (this);
		l2.add (layerBottomHeight0);

		l2.add (new JWidthLabel (Translator.swap ("FiLayerForm.and"), 20));
		layerBottomHeight1 = new JTextField (3);
		layerBottomHeight1.addActionListener (this);
		l2.add (layerBottomHeight1);
		l2.addStrut0 ();
		criteriaPanel.add (l2);

		criteriaPanel.addStrut0 ();

		// 1.2 Table1 panel
		JPanel table1Panel = new JPanel (new BorderLayout ());
		table1Panel.setBorder (BorderFactory.createTitledBorder (Translator.swap (
				"FiLayerForm.table1")));

		// Table1
		table1 = new JTable (table1Model);
		table1.setAutoCreateRowSorter (true);
		TableColumn col = table1.getColumnModel ().getColumn (0);
		col.setPreferredWidth (150);
		JScrollPane scroll1 = new JScrollPane (table1);
			scroll1.setMinimumSize (new Dimension (100, 100));	// fc - 27.10.2009
		
		table1Panel.add (criteriaPanel, BorderLayout.NORTH);
		table1Panel.add (scroll1, BorderLayout.CENTER);

		// 1.3 Edit buttons
		LinePanel l10 = new LinePanel ();
		l10.addGlue ();

			add = new JButton (Translator.swap ("FiLayerForm.add"));
			add.addActionListener (this);
			l10.add (add);

			remove = new JButton (Translator.swap ("FiLayerForm.remove"));
			remove.addActionListener (this);
			l10.add (remove);

			clear = new JButton (Translator.swap ("FiLayerForm.clear"));
			clear.addActionListener (this);
			l10.add (clear);
			l10.addGlue ();

		part1.add (table1Panel);
		part1.add (l10);
		part1.setMinimumSize (new Dimension (100, 200));

		// Part2. LayerSet
		part2 = new ColumnPanel (0, 0);

		// 2.1 Table2 panel
		JPanel table2Panel = new JPanel (new BorderLayout ());
		table2Panel.setBorder (BorderFactory.createTitledBorder (Translator.swap (
				"FiLayerForm.table2")));

		// Table2
		table2 = new JTable (table2Model);
		table2.setAutoCreateRowSorter (true);
		col = table2.getColumnModel ().getColumn (0);
		col.setPreferredWidth (150);
		JScrollPane scroll2 = new JScrollPane (table2);
			scroll2.setMinimumSize (new Dimension (100, 100));	// fc - 27.10.2009
		table2Panel.add (scroll2, BorderLayout.CENTER);

		// 2.2 Percentage sum + manual
		ColumnPanel bottom = new ColumnPanel (0, 0);

		LinePanel l8 = new LinePanel ();
		l8.addGlue ();
		l8.add (new JLabel (Translator.swap ("FiLayerForm.percentagesSum")+" : "));
		percentageSum = new JTextField (4);
		percentageSum.setPreferredSize (new Dimension (30, percentageSum.getPreferredSize ().height));
		percentageSum.setEditable (false);
		l8.add (percentageSum);
		l8.addStrut0 ();
		bottom.add (l8);

		if (creationMode) {
			LinePanel l9b = new LinePanel ();
			l9b.add (new JLabel ("<html>"
					+Translator.swap ("FiLayerForm.usage")
					+"</html>"));
			l9b.addStrut0 ();
			bottom.add (l9b);
		}

		part2.add (table2Panel);
		part2.add (bottom);
		part2.setMinimumSize (new Dimension (100, 150));

    }

	/**	Returns true if the layerSet editor is shown.
	*/
	private boolean isEditorShown () {
		return creationMode ||
				(withShowHideEditor
					&& showHideEditor != null
					&& showHideEditor.getText ().equals (HIDE_EDITOR));
    }

	/**	Updates the UI according to current context.
	*/
	private void refreshUI () {
		removeAll ();

		// Layout according to current options
		// - if the editor is required, use a split pane
		// - otherwise only the layerSet table
		setLayout (new BorderLayout ());

		// Edition mode: editor part is hidden, a button to show/hide it
		if (withShowHideEditor) {
			add (showHideEditorLine, BorderLayout.NORTH);
		}

		// If the editor is/can be opened, use a split pane
		if (isEditorShown ()) {
			split = new JSplitPane (JSplitPane.VERTICAL_SPLIT , part1, part2);
			split.setResizeWeight (0.5);
			split.setContinuousLayout (true);
			split.setBorder (null);
			add (split, BorderLayout.CENTER);
			setPreferredSize (new Dimension (200, 450));

		} else {
			// If no editor at all, no split pane
			add (part2, BorderLayout.CENTER);
			setPreferredSize (new Dimension (200, 250));
		}

		revalidate ();
	}

}
