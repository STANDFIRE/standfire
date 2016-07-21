/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2001-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.NonEditableTableModel;
import jeeb.lib.util.NumberRenderer;
import jeeb.lib.util.OVSelector;
import jeeb.lib.util.OVSelectorSource;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Speciable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.AbstractStandViewer;
import capsis.gui.GrouperChooser;
import capsis.gui.GrouperChooserListener;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.ExcelAdapter;
import capsis.util.Group;
import capsis.util.Grouper;
import capsis.util.GrouperManager;
import capsis.util.JWidthTextField;
import capsis.util.SwingWorker3;

/**
 * SVController is a tool for TreeList detailed analysis.
 * 
 * @author F. de Coligny - November 2003
 */
public class SVController extends AbstractStandViewer implements ActionListener, GrouperChooserListener,
		ListSelectionListener, OVSelectorSource {

	private static final long serialVersionUID = 1L;

	static public String NAME = "SVController";
	static public String DESCRIPTION = "SVController.description";
	static public String AUTHOR = "F. de Coligny";
	static public String VERSION = "1.2";


	/**
	 * A table row sorter with a memory, see resort ().
	 */
	private static class SpecificSorter extends TableRowSorter {

		private List<? extends RowSorter.SortKey> memoSortKeys;

		/** Constructor */
		public SpecificSorter (TableModel m) {
			super (m);
		}

		/** Memorize the sort keys when needed */
		public void toggleSortOrder (int column) {
			super.toggleSortOrder (column);
			memoSortKeys = getSortKeys ();
		}

		/** Resort if requested */
		public void resort () {
			if (memoSortKeys != null) {
				setSortKeys (memoSortKeys);
			}
			sort ();
		}

	}

	static {
		Translator.addBundle ("capsis.extension.standviewer.SVController");
	}

	private static final String[] COLUMN_NAMES = {Translator.swap ("SVController.id"),
			Translator.swap ("SVController.age"), Translator.swap ("SVController.dbh"),
			Translator.swap ("SVController.height"), Translator.swap ("SVController.number"),
			Translator.swap ("SVController.sceneStatus"), Translator.swap ("SVController.mark"),
			Translator.swap ("SVController.x"), Translator.swap ("SVController.y"), Translator.swap ("SVController.z"),
			Translator.swap ("SVController.species"),};

	private NumberFormat formater;

	private static final String BLANK = "";
	private static final String MARKED = "marked";

	private TreeCollection currentStand;
	private Grouper currentGrouper;
	private boolean currentGrouperNot;

	private SwingWorker3 task; // only one task at any time
	private GrouperChooser grouperChooser;

	private OVSelector ovSelector;
	protected boolean thisIsAReselection;

	private JTable table;
	private JScrollPane tablePane;
	private JTextField NField;
	private JTextField nLinField;
	private double N;
	private int nLin;
	private JTextArea descriptionField;
	private JScrollPane descriptionPane;
	private JButton helpButton;

	protected Collection<Integer> memoSelectionIds;
	protected Collection memoSelection;
	protected Collection effectiveSelection;
	protected boolean listenTableSelection;

	private boolean constructionTime;

	private Map<Integer,Tree> tableRow_tree;

	private SpecificSorter sorter;
	private NumberRenderer numberRenderer;

	/**
	 * Default constructor
	 */
	public SVController () {}

	@Override
	public void init (GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);

		sorter = null;

		formater = NumberFormat.getInstance (Locale.ENGLISH);
		formater.setGroupingUsed (false);
		formater.setMaximumFractionDigits (3);

		numberRenderer = new NumberRenderer (formater);

		try {
			constructionTime = true;

			tableRow_tree = new HashMap<Integer,Tree> ();

			createOVSelector (step.getScene ());

			currentStand = null;
			currentGrouper = null;
			currentGrouperNot = false;

			memoSelectionIds = new ArrayList<Integer> ();
			memoSelection = new ArrayList ();

			createUI ();
			constructionTime = false;
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVController.c ()", "Error in constructor", e);
			throw e; // propagate
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can
	 * deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
			Step step = (Step) ((GModel) referent).getProject ().getRoot ();
			if (!(step.getScene () instanceof TreeCollection)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "SVController.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**
	 * Update tool on new step button (StandViewer superclass)
	 */
	public void update (StepButton sb) {
		super.update (sb); // computes boolean sameStep
		// new stand, same grouper
		refresh ((TreeCollection) step.getScene (), currentGrouper, currentGrouperNot);
	}

	/**
	 * Update the viewer with currentStand (StandViewer superclass) (currentStand should be set
	 * before)
	 */
	public void update () {
		super.update ();
		// same stand, same grouper
		refresh ((TreeCollection) step.getScene (), currentGrouper, currentGrouperNot);
	}

	// For thread access
	public synchronized void setCurrentStand (TreeCollection stand) {
		currentStand = stand;
	}

	public synchronized void setCurrentGrouper (Grouper grouper) {
		currentGrouper = grouper;
	}

	public synchronized void setCurrentGrouperNot (boolean not) {
		currentGrouperNot = not;
	}

	public synchronized void setTable (TableModel tableModel, Collection<Integer> selectedIds) {

		this.table.setModel (tableModel);

		// To allow cut & paste with system clipboard (MS-Excel & others...)
		ExcelAdapter myAd = new ExcelAdapter (table);

		// Restore selection in table: same ids in column 0
		Collection<Integer> selectedIndices = new ArrayList<Integer> ();
		for (int i = 0; i < tableModel.getRowCount (); i++) {
			if (selectedIds.contains (tableModel.getValueAt (i, 0))) {
				selectedIndices.add (i);
			}
		}

		listenTableSelection = false; // fc - 14.12.2007
		this.table.getSelectionModel ().clearSelection ();
		for (int i : selectedIndices) {
			this.table.getSelectionModel ().addSelectionInterval (i, i);
		}
		listenTableSelection = true; // fc - 14.12.2007

		// Update sorter
		sorter.setModel (tableModel);
		// Restore sort order
		sorter.resort ();

		// table.getColumnModel ().getColumn (0).setCellRenderer
		// (numberRenderer);
		table.setDefaultRenderer (Double.class, numberRenderer);
		table.setDefaultRenderer (Integer.class, numberRenderer);

	}

	/**
	 * Enable / disable the gui during refresh thread work Must be called only from Swing thread (=>
	 * not synchronized)
	 */
	private void enableUI (boolean b) {
		table.setEnabled (b);
		table.getTableHeader ().revalidate ();
		helpButton.setEnabled (b);
	}

	/**
	 * Update the viewer with currentStand (StandViewer superclass) (currentStand should be set
	 * before)
	 */
	private void refresh (TreeCollection newStand, Grouper newGrouper, boolean newGrouperNot) {
		if (newStand == currentStand && newGrouper == currentGrouper && newGrouperNot == currentGrouperNot) return;

		// Interrupt previous task if needed
		if (task != null) {
			task.interrupt ();
		}

		final TreeCollection fStand = newStand;
		final Grouper fGrouper = newGrouper;
		final boolean fGrouperNot = newGrouperNot;
		final Collection<Integer> finalMemoSelectionIds = memoSelectionIds;

		task = new SwingWorker3 () {

			private TableModel tableModel;
			private boolean interrupted;

			public Object construct () { // Runs in new Thread
				interrupted = false;
				TreeCollection wStand = fStand;
				Collection wTrees = wStand.getTrees ();
				if (fGrouper != null && !fGrouper.equals ("")) {
					try {
						wTrees = fGrouper.apply (wStand.getTrees (), fGrouperNot);
					} catch (Exception e) {
						Log.println (Log.WARNING, "SVController.refresh ()", "Exception while applying grouper "
								+ fGrouper + " on stand " + wStand, e);
					}
				}
				setCurrentGrouper (fGrouper);
				setCurrentGrouperNot (fGrouperNot);
				setCurrentStand (wStand);

				// makeDataModel is the longest method (loop) if interrupted, it
				// returns null
				tableModel = makeDataModel (wTrees);

				if (tableModel == null) {
					System.out.println ("    SVController: thread interruption detected in construct");
					interrupted = true;
					return null;
				}

				return tableModel;
			}

			public void finished () { // Runs in Swing Thread when construct is
				// over
				if (interrupted) {
					System.out.println ("    SVController: thread interruption detected in finished");
					enableUI (true);
					return;
				}

				NField.setText ("" + formater.format (N));
				nLinField.setText ("" + nLin);

				setTable (tableModel, finalMemoSelectionIds);

				reselect ();

				enableUI (true);
			}
		};

		// Disable table during task execution
		if (table != null) {
			enableUI (false);
		}

		task.start ();
	}

	/**
	 * Build main user interface.
	 */
	private synchronized TableModel makeDataModel (Collection wTrees) {
		tableRow_tree.clear ();
		Object[] trees = wTrees.toArray ();

		String[] colNames = COLUMN_NAMES;
		Object[][] mat = new Object[trees.length][11];

		StringBuffer buffer = new StringBuffer ();

		nLin = trees.length; // number of lines
		N = 0; // number of trees (considering numberable interface)

		for (int i = 0; i < trees.length; i++) {

			if (Thread.interrupted ()) {
				System.out.println ("*** SVController: thread was interrupted in makeDataModel");
				return null;
			}

			Tree t = (Tree) trees[i];

			// This map contains row index (i) -> tree reference for each tree
			// in the table
			// i is the table model index (does not change on column sorting)
			tableRow_tree.put (i, t);

			mat[i][0] = new Integer (t.getId ());
			mat[i][1] = new Integer (t.getAge ());
			mat[i][2] = new Double (t.getDbh ());
			mat[i][3] = new Double (t.getHeight ());

			// Numberable: number
			try {
				Numberable s = (Numberable) t;
				mat[i][4] = new Double (s.getNumber ()); // fc - 22.8.2006 -
				// Numberable
				// returns double
				if (!t.isMarked ()) {
					N += s.getNumber ();
				}
			} catch (Exception e) {
				mat[i][4] = BLANK;
				if (!t.isMarked ()) {
					N += 1;
				}
			}

			// Scene status
			try {
				mat[i][5] = t.getStatusInScene ();
			} catch (Exception e) {
				mat[i][5] = BLANK;
			}

			// Mark
			if (t.isMarked ()) {
				mat[i][6] = MARKED;
			} else {
				mat[i][6] = BLANK;
			}

			// Spatialized: coordinates
			try {
				Spatialized s = (Spatialized) t;
				mat[i][7] = new Double (s.getX ());
				mat[i][8] = new Double (s.getY ());
				mat[i][9] = new Double (s.getZ ());
			} catch (Exception e) {
				mat[i][7] = BLANK;
				mat[i][8] = BLANK;
				mat[i][9] = BLANK;
			}

			// Speciable: Species
			try {
				Speciable s = (Speciable) t;
				buffer.setLength (0);
				buffer.append (s.getSpecies ().getName ());
				buffer.append (" (");
				buffer.append (s.getSpecies ().getValue ());
				buffer.append (")");
				mat[i][10] = buffer.toString ();
			} catch (Exception e) {
				mat[i][10] = BLANK;
			}

		}

		DefaultTableModel dataModel = new NonEditableTableModel (mat, colNames);

		return dataModel;
	}

	/**
	 * Called when selection changed in table
	 */
	public void valueChanged (ListSelectionEvent evt) {
		if (evt.getValueIsAdjusting ()) { return; }
		if (!listenTableSelection) { return; }

		ListSelectionModel sm = (ListSelectionModel) evt.getSource ();

		if (sm.isSelectionEmpty ()) { return; } // we are on the header

		// Retrieve tree id
		try {

			int viewRow = table.getSelectedRow ();
			int selectedRow = table.convertRowIndexToModel (viewRow);

			int pos = descriptionPane.getVerticalScrollBar ().getValue ();

			Tree tree = tableRow_tree.get (selectedRow);

			descriptionField.setText (tree.toString ());
			descriptionPane.getVerticalScrollBar ().setValue (pos);

			thisIsAReselection = false;
			select ();

		} catch (Exception e) {
			descriptionField.setText ("");
			descriptionPane.getVerticalScrollBar ().setValue (0);
		}
	}

	/**
	 * Used for the settings buttons.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (helpButton)) {
			Helper.helpFor (this);
		}
	}

	/**
	 * Grouper changed in grouper chooser.
	 */
	public void grouperChanged (String grouperName) {
		if (grouperName == null || grouperName.equals (BLANK)) {
			// Cancel grouper if one is set
			refresh ((TreeCollection) step.getScene (), null, false);
		} else {
			GrouperManager gm = GrouperManager.getInstance ();
			Grouper newGrouper = gm.getGrouper (grouperName);
			// Set new grouper
			refresh ((TreeCollection) step.getScene (), newGrouper, grouperName.toLowerCase ().startsWith ("not "));
		}
	}

	// When a selection occurs, tell the OVSelector
	private void select () {
		// if we arrive here during construction time... Ignore
		if (constructionTime) { return; }

		updateMemoSelection ();

		effectiveSelection = ovSelector.select (memoSelection, thisIsAReselection);
		thisIsAReselection = false;
	}

	/**
	 * When this viewer updates (ex: changes StepButtons), see if reselection must be triggered
	 * towards OVSelector
	 */
	public void reselect () {
		// when creating ovSelector and its combo box, an itemStateChangedEvent
		// is thrown and we arrive here... Ignore
		if (constructionTime) { return; }

		thisIsAReselection = true;
		select ();
	}

	/**
	 * If ovDialog is closed by user, remove any selection marks
	 */
	public void cancelSelection () {
		// ~ select (null, false);

		// fc - 12.9.2008
		// we can ignore selection canceling in the table, does not matter here
		// and makes it possible to reselect the same selection by clicking 2
		// times
		// on the enable button in OVSelector
	}

	/**
	 * Update the memoSelection collection from the table selection
	 */
	private void updateMemoSelection () {
		try {
			memoSelectionIds = new ArrayList<Integer> ();
			memoSelection = new ArrayList ();
			int[] selectedRows = table.getSelectedRows ();

			TableModel model = table.getModel ();
			for (int i = 0; i < selectedRows.length; i++) {
				int r = table.convertRowIndexToModel (selectedRows[i]);

				int treeId = ((Integer) model.getValueAt (r, 0)).intValue ();
				memoSelectionIds.add (treeId);

				Tree tree = tableRow_tree.get (r);
				memoSelection.add (tree);
				// ~ memoSelection.add (currentStand.getTree (treeId));

			}
		} catch (Exception e) {} // can happen if called early

	}

	/**
	 * Define what objects are candidate for selection by OVSelector Can be redefined in subclasses
	 * to select other objects
	 */
	protected void createOVSelector (GScene stand) {
		try {
			// Default : trees and cells if any
			Collection candidateObjects = new ArrayList (((TreeCollection) stand).getTrees ());
			JScrollPane targetScrollPane = null;
			GModel modelForVetoes = null;
			ExtensionManager extMan = CapsisExtensionManager.getInstance ();
			ovSelector = new OVSelector (extMan, this, candidateObjects, targetScrollPane, true, false, modelForVetoes);
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVController.createOVSelector ()", "Exception during OVSelector construction, wrote this error and passed", e);
		}
	}

	/**
	 * Build main user interface. CurrentStand is still null here.
	 */
	private void createUI () {

		System.out.println ("SVController.createUI ()...");

		ColumnPanel header = new ColumnPanel ();

		// N + GrouperChooser
		LinePanel l1 = new LinePanel ();
		NField = new JWidthTextField (5, 20, true, true); // min and max size
		// fixed
		NField.setEditable (false);
		l1.add (new JWidthLabel (Translator.swap ("SVController.numberOfTrees") + " :", 50));
		l1.add (NField);

		grouperChooser = new GrouperChooser (step.getScene (), Group.TREE, BLANK, false, true, false);

		grouperChooser.addGrouperChooserListener (this);
		l1.add (grouperChooser);
		l1.addStrut0 ();
		header.add (l1);

		// nLin + ObjectViewers chooser + help button
		LinePanel l2 = new LinePanel ();
		nLinField = new JWidthTextField (5, 20, true, true); // min and max size
		// fixed
		nLinField.setEditable (false);
		l2.add (new JWidthLabel (Translator.swap ("SVController.numberOfLines") + " :", 50));
		l2.add (nLinField);
		l2.add (ovSelector);

		// Help button
		ImageIcon icon = IconLoader.getIcon ("help_16.png");
		helpButton = new JButton (icon);
		Tools.setSizeExactly (helpButton);
		helpButton.setToolTipText (Translator.swap ("Shared.help"));
		helpButton.addActionListener (this);
		l2.add (helpButton);
		l2.addStrut0 ();

		header.add (l2);
		header.addStrut0 ();

		// Data model of the main table: empty at the beginning
		DefaultTableModel dataModel = new NonEditableTableModel (new String[1][10], COLUMN_NAMES);

		// Main table creation
		table = new JTable (dataModel);

		// Numbers rendering
		// table.getColumnModel ().getColumn (0).setCellRenderer
		// (numberRenderer);
		table.setDefaultRenderer (Double.class, numberRenderer);
		table.setDefaultRenderer (Integer.class, numberRenderer);

		// table.setAutoCreateRowSorter(true);
		sorter = new SpecificSorter (table.getModel ());
		table.setRowSorter (sorter);

		// Default: sort on column 0
		sorter.toggleSortOrder (0);

		table.setRowSelectionAllowed (true);
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

		table.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getSelectionModel ().addListSelectionListener (this);
		// To allow cut & paste with system clipboard (MS-Excel & others...)
		ExcelAdapter myAd = new ExcelAdapter (table);

		tablePane = new JScrollPane (table);

		// Tree description
		descriptionField = new JTextArea (3, 5); // 4 rows approximatively
		descriptionField.setEditable (false);
		descriptionField.setLineWrap (true);
		descriptionPane = new JScrollPane (descriptionField);

		setLayout (new BorderLayout ());
		add (header, BorderLayout.NORTH);
		add (tablePane, BorderLayout.CENTER);
		add (descriptionPane, BorderLayout.SOUTH);
	}

	/**
	 * Dispose the used resources (dialog box).
	 */
	public void dispose () {
		super.dispose ();
		ovSelector.dispose ();
	}

}
