/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */

package capsis.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import jeeb.lib.util.task.StatusBar;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.command.InterventionDialog;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.util.Helper;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import capsis.util.Grouper;
import capsis.util.GrouperManager;


/**
 * DIntervention is a dialog box to choose an intervener to be applied on a
 * step. Interveners are Capsis extensions.
 * 
 * @author F. de Coligny - november 2000
 */
public class DIntervention extends AmapDialog implements InterventionDialog, ActionListener, GrouperChooserListener,
		ListSelectionListener {

	// fc - 14.4.2006 - grouper chooser may throw an exception -> added
	// groupEnabled

	private GModel model;
	private Step step;
	private Intervener intervener;

	private String intervenerClassName;

	// we need 3 tables
	private Map<String, String> subTypeKey_subType; // subTypeKey -> to comboBox
													// -> retrieve subType
	private Map<String, Vector<String>> subType_extensionKeys; // from selected
																// subType,
																// matching
	// existing extensions keys
	private Map<String, String> extensionKey_classname; // when extension chosen
														// in list, get
	// className (and wait for ok)

	private boolean groupEnabled; // fc - 15.4.2006 - null error on IfnCA
	// private JComboBox grouperTypes;
	private GrouperChooser grouperChooser;

	private JComboBox interventionTypes;
	private JList intervenerList;
	// private JTextArea description;
	private JTextPane propertyPanel;

	private StatusBar statusBar;
	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**
	 * Default constructor.
	 */
	public DIntervention(JFrame frame, Step step) {
		super(frame);

		this.step = step;
		this.model = step.getProject().getModel();

		intervenerClassName = "";
		groupEnabled = true;

		searchInterveners(null);
		createUI();

		// fc-3.9.2012
		// setSize (new Dimension (500, 450));
		activateSizeMemorization(getClass().getName());
		activateLocationMemorization(getClass().getName());

		pack(); // computes size

		setVisible(true);

	}

	/**
	 * Return a non null vector. The returned vector is sorted (fc-19.5.2011)
	 */
	private Vector<String> getCurrentInterveners() {
		Vector<String> v = null;
		try {
			String subTypeKey = (String) interventionTypes.getSelectedItem();
			String subType = (String) subTypeKey_subType.get(subTypeKey);
			v = (Vector<String>) subType_extensionKeys.get(subType);
			v = new Vector(new TreeSet(v)); // sort
		} catch (Exception e) {
			// this may happen if no intervener is selected (grouperMode...)
			// fc - 22.9.2004 -> do not write to Log
			// ~ Log.println (Log.ERROR,
			// "DIntervention.getCurrentInterveners ()",
			// ~ "Exception while retrieving interveners");
			return new Vector<String>();
		}
		if (v == null) {
			return new Vector<String>();
		}
		return v;
	}

	// Manages add in subType_extensionKeys map : for each subType,
	// vector containing the matching extension keys.
	// An extension key is something translated by Translator.swap ().
	// It can appear on a gui.
	//
	private void addSubType_extensionKeys(String st, String ek) {

		Vector<String> extensionKeys = (Vector<String>) subType_extensionKeys.get(st);

		// no entry for subType, create one
		if (extensionKeys == null) {
			extensionKeys = new Vector<String>();
			extensionKeys.add(ek);
			subType_extensionKeys.put(st, extensionKeys);
			return;
		}

		// entry exists, add extensionKey in related collection
		extensionKeys.add(ek);
	}

	// Build interveners list (grouper is considered if activated)
	// grouperType may be null -> no grouper active
	//
	private void searchInterveners(String grouperType) {

		// Ask the ExtensionManager for interveners compatible with the model
		// behind the scenario
		ExtensionManager extMan = CapsisExtensionManager.getInstance();
		Collection<String> classNames = extMan.getExtensionClassNames(CapsisExtensionManager.INTERVENER, model);

		// Interveners may be categorized in subTypes
		subTypeKey_subType = new TreeMap<String, String>(); // fc - 18.2.2002 -
															// try to sort combo
		// box contents
		subType_extensionKeys = new HashMap<String, Vector<String>>();
		extensionKey_classname = new HashMap<String, String>();

		for (Iterator i = classNames.iterator(); i.hasNext();) {
			String className = (String) i.next();

			// if grouper active, keep only interveners which can deal with its
			// type - fc -
			// 22.9.2004
			if (groupEnabled && grouperType != null) {
				try {

					if (!(ExtensionManager.isInstanceOf(className, GroupableIntervener.class))) {
						continue;
					}
					GroupableIntervener interv = (GroupableIntervener) ExtensionManager
							.getExtensionPrototype(className);

					String intervType = interv.getGrouperType();

					if (!intervType.equals(grouperType)) {
						continue;
					}
				} catch (Exception e) {
					Log.println(Log.ERROR, "DIntervention.searchInterveners ()",
							"trying to know if intervener deals with grouper type " + grouperType, e);
				}
			}

			// 1. getExtensionName () loads class -> labels added to Translator
			String extensionKey = ExtensionManager.getName(className);

			// ~ ExtensionItem item = extMan.getExtensionItem (className); // fc
			// - 17.12.2007
			// ~ String subType = item.getSubType (); // fc - 17.12.2007

			// get intervener subType property - fc - 17.12.2007
			String subType = ExtensionManager.getStaticField(className, "SUBTYPE");
			if (subType == null) {
				subType = "Unknown";
			}

			// 2.subType label was updated during class load in previous
			// getExtensionName ()
			String subTypeKey = Translator.swap(subType);

			if (subTypeKey == null) {
				subTypeKey = "NoSubType";
				subType = "NosubType";
			}

			subTypeKey_subType.put(subTypeKey, subType);
			addSubType_extensionKeys(subType, extensionKey);
			extensionKey_classname.put(extensionKey, className);
		}

		// for (Iterator i = subTypeKey_subType.keySet ().iterator (); i.hasNext
		// ();) {
		// System.out.println ("type " + (String) i.next ());
		// }
		// for (Iterator i = extensionKey_classname.keySet ().iterator ();
		// i.hasNext ();) {
		// System.out.println ("name " + (String) i.next ());
		// }

	}

	// Action on ok
	//
	private void okAction() {
		// classic checks...

		if (intervenerClassName != null && intervenerClassName.length() != 0) {

			// Intervener class name is retrieved from Dintervention dialog
			String className = intervenerClassName;
			GScene fromStand = step.getScene();
			String grouperName = getGrouperName(); // fc - 22.9.2004 - maybe
													// null if no grouper
			// selected

			// This object is a copy of fromStand which step instance variable
			// is null !

			statusBar.print(Translator.swap("DIntervention.sceneCopy") + "...");

			GScene newStand = (GScene) fromStand.getInterventionBase();
			if (newStand == null) { // fc - 17.12.2003 - getInterventionBase can
									// return null if
				// trouble
				MessageDialog.print(this, Translator.swap("Intervention.interventionBaseError"));
				return;
			}
			statusBar.print(Translator.swap("DIntervention.sceneCopyDone"));

			// Load an extension of type : the one chosen by user

			// fc - 22.9.2004 - intervener may be ran on groups
			Collection indivs = null;

			if (groupEnabled && grouperName != null) {
				GrouperManager gm = GrouperManager.getInstance();
				Grouper g = gm.getGrouper(grouperName);
				indivs = Group.whichCollection(newStand, g.getType());

				indivs = g.apply(indivs, grouperName.toLowerCase().startsWith("not "));
			}

			try {
				intervener = (Intervener) CapsisExtensionManager.getInstance().instantiate(className);

				statusBar.print(Translator.swap("DIntervention.intervenerInit") + "...");
				intervener.init(step.getProject().getModel(), step, newStand, indivs);
				statusBar.print(Translator.swap("DIntervention.intervenerConfig") + "...");
				intervener.initGUI();

				// ~ reason = intervener.toString ();
			} catch (Exception e) {
				MessageDialog.print(this, Translator.swap("Intervention.intervenerCanNotBeLoaded"), e);
				Log.println(Log.ERROR, "Intervention.execute ()", "Exception caught:", e);
				return;
			}

			// Intervener has opened a dialog and configuration is correct
			// => close this dialog valid, the intervention will be processed in
			// command.Intervention
			//
			if (intervener.isReadyToApply()) {
				setValidDialog(true);
			} else {
				// user canceled the intervener dialog, he can choose another
				// intervener or cancel the whole intervention by canceling this
				// dialog
				statusBar.print(Translator.swap("DIntervention.chooseAnIntervener"));
			}

		}
	}

	// Changing grouper type updates the grouper chooser list
	//
	// private void grouperTypesAction () {
	// if (groupEnabled) {
	// grouperChooser.setType ((String) grouperTypes.getSelectedItem ());
	// }
	// }

	// Changing intervention type triggers
	// update on the intervener list
	//
	private void interventionTypesAction() {
		// ~ System.out.println ("interventionTypesAction ()...");
		intervenerList.setListData(getCurrentInterveners());
		Settings.setProperty("capsis.last.intervention.group", "" + interventionTypes.getSelectedItem()); // memo
		// for
		// next
		// time

		// Select first tool
		try {
			intervenerList.setSelectedIndex(0);
		} catch (Exception e) {
		}

	}

	/**
	 * Grouper changed in grouper chooser.
	 */
	public void grouperChanged(String grouperName) {
		if (!groupEnabled) {
			return;
		}

		// synchronizeGroups ();
		if (grouperName == null || grouperName.equals("")) {
			synchronizeCombos(null);
		} else {
			GrouperManager gm = GrouperManager.getInstance();
			Grouper newGrouper = gm.getGrouper(grouperName);

			synchronizeCombos(newGrouper.getType());
		}
	}

	// If grouper is changed, update interveners types and list
	//
	private void synchronizeCombos(String groupType) {
		if (!groupEnabled) {
			return;
		}

		searchInterveners(groupType);
		fillComboBox(interventionTypes, subTypeKey_subType.keySet());
		// ~ fillList (intervenerList, getCurrentInterveners ());
		interventionTypes.revalidate();
		interventionTypes.repaint();
		intervenerList.revalidate();
		intervenerList.repaint();
		try {
			intervenerList.setSelectedIndex(0); // list may be empty
		} catch (Exception e) {
			updatePropertyPanel();
		} // if empty, nothing more
	}

	// Enables / Disables all group panel coherently
	//
	// private void synchronizeGroups () {
	// if (!groupEnabled) { return; }
	// grouperTypes.setEnabled (grouperChooser.isGrouperSelected ());
	// }

	// Some button was hit
	//
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			setValidDialog(false);
			// } else if (groupEnabled && evt.getSource ().equals
			// (grouperTypes)) {
			// grouperTypesAction ();
		} else if (evt.getSource().equals(interventionTypes)) {
			interventionTypesAction();
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Process actions on an item in the models list.
	 */
	public void valueChanged(ListSelectionEvent evt) {

		updatePropertyPanel();

		// try {
		// JList src = (JList) evt.getSource ();
		// String humanKey = (String) src.getSelectedValue ();
		// intervenerClassName = (String) extensionKey_classname.get (humanKey);
		//
		// // user information
		// // descriptionKey = className (without package
		// // i.e;"little name)+".description"
		// String descriptionKey = AmapTools.getClassSimpleName
		// (intervenerClassName) + ".description";
		// updateDescription (Translator.swap (descriptionKey));
		// } catch (Exception e) {
		// } // can be an exception if change in combo and there was a selection
		// // in the list
	}

	private void updatePropertyPanel() {
		try {
			String humanKey = (String) intervenerList.getSelectedValue();
			intervenerClassName = (String) extensionKey_classname.get(humanKey);

			Settings.setProperty("capsis.last.selected.intervention", humanKey); // memo
																					// for
																					// next
																					// time

			CapsisExtensionManager.getInstance().getPropertyPanel(intervenerClassName, propertyPanel);

		} catch (Exception e) {
			// There can be an exception if change in combo and there was a
			// selection
		}
	}

	// // From ListSelectionListener interface.
	// // Called when selection changes on list 2 (selected filters).
	// //
	// private void updateDescription (String text) {
	// description.setText (text);
	// description.setToolTipText (text);
	// description.setCaretPosition (0);
	// }

	// Initialize the dialog's GUI.
	//
	private void createUI() {

		ColumnPanel groupPanel = new ColumnPanel();
		groupPanel.setBorder(BorderFactory.createTitledBorder(Translator.swap("DIntervention.groupRestriction")));

		try {
			// 2. Grouper type
			GScene stand = Current.getInstance().getStep().getScene();

			// fc-31.8.2012 there is now an integrated group type chooser in the
			// grouperChooser, remove the extra combo box here
			// Collection z = Group.getPossibleTypes (stand);
			// grouperTypes = new JComboBox (new Vector (z));
			// grouperTypes.addActionListener (this);

			// 3. grouperChooser
			LinePanel l10 = new LinePanel();
			// String type = (String) grouperTypes.getSelectedItem ();
			String type = "";

			grouperChooser = new GrouperChooser(stand, type, "", false, true, false);

			grouperChooser.addGrouperChooserListener(this);
			l10.add(grouperChooser);
			// l10.add (grouperTypes);

			groupPanel.add(l10);
			groupPanel.addStrut1();
		} catch (Exception e) {
			groupEnabled = false;
		}

		ColumnPanel interventionPanel = new ColumnPanel();
		interventionPanel.setBorder(BorderFactory.createTitledBorder(Translator
				.swap("DIntervention.selectAnIntervener")));

		// 5. Combo for interventionTypes
		LinePanel l2 = new LinePanel();

		Set keys = subTypeKey_subType.keySet();
		interventionTypes = new JComboBox(new Vector(keys));
		// Try to reset combo like last time it was used
		try {
			if (keys.contains(Settings.getProperty("capsis.last.intervention.group", ""))) {
				interventionTypes.setSelectedItem(Settings.getProperty("capsis.last.intervention.group", ""));
			}
		} catch (Exception e) {
		}
		interventionTypes.addActionListener(this);
		l2.add(new JWidthLabel(Translator.swap("DIntervention.interventionType") + " :", 200));
		l2.add(interventionTypes);
		l2.addStrut0();

		// 6. List of candidate interveners
		LinePanel l3 = new LinePanel();
		JWidthLabel lab = new JWidthLabel(Translator.swap("DIntervention.interventionMethod") + " :", 200);
		ColumnPanel component1 = new ColumnPanel();
		component1.add(lab);
		component1.addGlue();

		Vector interveners = getCurrentInterveners(); // according to type
														// comboBox

		intervenerList = new JList(interveners);
		intervenerList.addListSelectionListener(this);
		intervenerList.setVisibleRowCount(7);
		intervenerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		intervenerList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					okAction();
				}
			}
		});
		JScrollPane scroll = new JScrollPane(intervenerList);

		JPanel aux = new JPanel(new BorderLayout());
		aux.add(scroll, BorderLayout.CENTER);

		l3.add(component1);
		l3.add(aux);
		l3.addStrut0();

		// 7. Label "description"
		// LinePanel l4 = new LinePanel ();
		// l4.add (new JLabel (Translator.swap ("DIntervention.description") +
		// " :"));
		// l4.addGlue ();

		// 8. Selected intervener properties
		propertyPanel = new JTextPane() {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(100, 100);
			}
		};
		propertyPanel.setEditable(false);

		JScrollPane scrollpane = new JScrollPane(propertyPanel);

		// description = new JTextArea (4, 15);
		// description.setEditable (false);
		// description.setLineWrap (true);
		// description.setWrapStyleWord (true);
		// JScrollPane scrollpane = new JScrollPane (description);

		ColumnPanel c = new ColumnPanel();
		c.add(scrollpane);
		c.addStrut0();
		LinePanel l = new LinePanel();
		l.add(c);
		l.addStrut0();

		// Try to restore list selection
		try {
			if (interveners.contains(Settings.getProperty("capsis.last.selected.intervention", ""))) {
				boolean shouldScroll = true;
				intervenerList.setSelectedValue(Settings.getProperty("capsis.last.selected.intervention", ""),
						shouldScroll);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			try {
				intervenerList.setSelectedIndex(0);
			} catch (Exception e2) {
			} // list may be empty
		}
		// try {
		// intervenerList.setSelectedIndex (0); // list may be empty
		// } catch (Exception e) {
		// updatePropertyPanel ();
		// } // if empty, nothing more

		interventionPanel.add(l2);
		interventionPanel.add(l3);
		// interventionPanel.add (l4);
		interventionPanel.add(l);
		interventionPanel.addStrut1();

		// synchronizeGroups ();

		// 9. Control panel
		LinePanel pControl = new LinePanel();

		// StatusDispatcher will print in this status bar
		boolean withTaskManagerButton = false; // would not work properly in a
												// modal dialog
		statusBar = new StatusBar(withTaskManagerButton);
		// StatusDispatcher.addListener (statusBar);
		statusBar.print(Translator.swap("DIntervention.chooseAnIntervener"));
		pControl.add(statusBar);

		pControl.addGlue();

		ok = new JButton(Translator.swap("Shared.ok"));
		ImageIcon icon = IconLoader.getIcon("ok_16.png");
		ok.setIcon(icon);
		pControl.add(ok);

		cancel = new JButton(Translator.swap("Shared.cancel"));
		icon = IconLoader.getIcon("cancel_16.png");
		cancel.setIcon(icon);
		pControl.add(cancel);

		help = new JButton(Translator.swap("Shared.help"));
		icon = IconLoader.getIcon("help_16.png");
		help.setIcon(icon);
		pControl.add(help);

		pControl.addStrut0();

		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);

		// Set ok as default (see AmapDialog)
		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);

		getContentPane().setLayout(new BorderLayout());
		if (groupEnabled) {
			getContentPane().add(groupPanel, BorderLayout.NORTH);
		}
		getContentPane().add(interventionPanel, BorderLayout.CENTER); // description
		getContentPane().add(pControl, BorderLayout.SOUTH);

		setTitle(Translator.swap("DIntervention") + " - " + Current.getInstance().getStep().getCaption());

		setModal(true);
	}

	// Tool method : put a collection into a JComboBox
	// The related intervener list is also updated, see interventionTypesAction
	// ()
	//
	private void fillComboBox(JComboBox combo, Collection items) {
		combo.removeAllItems();
		Iterator i = items.iterator();
		while (i.hasNext()) {
			combo.addItem(i.next());
		}
	}

	public String getIntervenerClassName() {
		return intervenerClassName;
	}

	public Intervener getIntervener() { // fc - 1.10.2004
		return intervener;
	}

	public String getGrouperName() {
		if (groupEnabled) {
			return grouperChooser.isGrouperAvailable() ? grouperChooser.getGrouperName() : null;
		} else {
			return null;
		}
	}

}
