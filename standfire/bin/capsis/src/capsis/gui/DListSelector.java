/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Identifiable;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.UserDialog;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.kernel.GScene;
import capsis.util.NumberStringComparator;
import capsis.util.SmartListModel;

/**
 * Dialog box to select a sublist of Strings from an original list.
 *
 * @author F. de Coligny - may 2002
 */
public class DListSelector extends AmapDialog 
		implements ActionListener, ListDataListener {	//, ListSelectionListener
	private final static int LIST_HEIGHT = 10;
	private final static int CELL_WIDTH = 60;

	private String dlgTitle;
	private String dlgText;
	private String empty;
	private Vector fromVector;
	private Vector selectedVector;

	private JTextField fromNumber;
	private JTextField toNumber;
	private JList fromList;
	private JList toList;
	private SmartListModel fromModel;
	private SmartListModel toModel;
	private int fromSize;
	private int toSize;

	private JButton addButton;
	private JButton removeButton;
	private JButton addAllButton;
	private JButton removeAllButton;
	private JButton addGroupButton;	// fc - 16.4.2007 [for PhD]
	private JButton sortButton;

	private String type;

	private JButton ok;
	private JButton cancel;
	private JButton helpButton;

	private boolean singleSelection;


	/**	Constructor
	*/
	public DListSelector (String title, String text, Vector v) {
		this (title, text, v, false);	// default: multiple selection enabled
	}

	/**	Constructor
	*/
	public DListSelector (String title, String text, Vector v, boolean singleSelection) {
		super ();
		dlgTitle = title;
		dlgText = text;
		fromVector = v;
		this.singleSelection = singleSelection;	// fc - 28.9.2006
		
		selectedVector = new Vector ();
		empty = Translator.swap ("DListSelector.empty");
		
		createUI ();
		
		pack ();
		show ();

	}

	/**	Action on Ok button
	*/
	public void okAction () {
		// Check: ask a confirmation in some cases:
		// 1. toList is empty
		if (toModel.isEmpty ()) {
			JButton yesButton = new JButton (Translator.swap ("Shared.yes"));
			JButton noButton = new JButton (Translator.swap ("Shared.no"));
			Vector buttons = new Vector ();
			buttons.add (yesButton);
			buttons.add (noButton);
			// Prepare vector selected string items
			JButton choice = UserDialog.promptUser (this, Translator.swap ("Shared.confirm"),
					Translator.swap ("DListSelector.targetListIsEmpty")
					+"\n"+Translator.swap ("Shared.continue")+" ?",
					buttons, noButton);
			if (choice.equals (noButton)) {return;}
		}

		// 2. selection in fromList is multiple
		if (fromList.getSelectedIndices ().length > 1) {
			JButton yesButton = new JButton (Translator.swap ("Shared.yes"));
			JButton noButton = new JButton (Translator.swap ("Shared.no"));
			Vector buttons = new Vector ();
			buttons.add (yesButton);
			buttons.add (noButton);
			// Prepare vector selected string items
			JButton choice = UserDialog.promptUser (this, Translator.swap ("Shared.confirm"),
					Translator.swap ("DListSelector.selectionInFromListIsMultiple")
					+"\n"+Translator.swap ("Shared.continue")+" ?",
					buttons,
					noButton);
			if (choice.equals (noButton)) {return;}
		}

		// 3. selection in toList is multiple
		if (toList.getSelectedIndices ().length > 1) {
			JButton yesButton = new JButton (Translator.swap ("Shared.yes"));
			JButton noButton = new JButton (Translator.swap ("Shared.no"));
			Vector buttons = new Vector ();
			buttons.add (yesButton);
			buttons.add (noButton);
			// Prepare vector selected string items
			JButton choice = UserDialog.promptUser (this, Translator.swap ("Shared.confirm"),
					Translator.swap ("DListSelector.selectionInToListIsMultiple")
					+"\n"+Translator.swap ("Shared.continue")+" ?",
					buttons,
					noButton);
			if (choice.equals (noButton)) {return;}
		}

		// Prepare vector selected string items
		selectedVector = new Vector (toModel.getContents ());
		
		// Back to the caller
		setValidDialog (true);
		//fc	setVisible (false);
	}

	//	Manage list data changes
	//
	private void listChanged (ListDataEvent evt) {
		if (evt.getSource ().equals (fromModel)) {
			fromNumber.setText (""+fromModel.getSize ());
		} else if (evt.getSource ().equals (toModel)) {
			toNumber.setText (""+toModel.getSize ());
		}
		
		// Enable / disable buttons according to the current situation
		synchro ();
	}
	
	/**
	 * Enables / disables the action buttons (if lists are empty...)
	 * Proposed by S. Aid - fc-15.11.2011
	 */
	private void synchro () {
			
		addButton.setEnabled (!fromModel.isEmpty ());
		removeButton.setEnabled (!toModel.isEmpty ());
		addAllButton.setEnabled (!fromModel.isEmpty ());
		removeAllButton.setEnabled (!toModel.isEmpty ());
		addGroupButton.setEnabled (!fromModel.isEmpty ());
		
	}

	/**	ListDataListener
	*/
	public void contentsChanged (ListDataEvent evt) {listChanged (evt);}

	/**	ListDataListener
	*/
	public void intervalAdded (ListDataEvent evt) {listChanged (evt);}

	/**	ListDataListener
	*/
	public void intervalRemoved (ListDataEvent evt) {listChanged (evt);}

	/**	Redirection of events from panel buttons.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (toModel.contains (empty)) {toModel.remove (empty);}
		
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (addButton)) {
			addAction ();
		} else if (evt.getSource ().equals (removeButton)) {
			removeAction ();
		} else if (evt.getSource ().equals (addAllButton)) {
			addAllAction ();
		} else if (evt.getSource ().equals (removeAllButton)) {
			removeAllAction ();
		} else if (evt.getSource ().equals (sortButton)) {
			sortAction ();
		} else if (evt.getSource ().equals (addGroupButton)) {
			addGroupAction ();
		} else if (evt.getSource ().equals (helpButton)) {
			Helper.helpFor (this);
		}
	}

	/**	Set the two lists models mute or verbose.
	*/
	private void setModelsMute (boolean b) {
		fromModel.setMute (b);
		toModel.setMute (b);
	}

	/**	Add elements list1 -> list2
	*/
	public void addAction () {
		if (singleSelection) {

			Object[] values = toList.getSelectedValues ();
			if (values.length > 0) {	// possibly 0 or 1 value in toModel in singleSelection model
				setModelsMute (true);
				toModel.remove (values[0]);
				fromModel.add (values[0]);
				setModelsMute (false);
			}
			
		}	// fc - 28.9.2006
		
		// Find min selected index in fromList
		int[] tab = fromList.getSelectedIndices ();
		int iFrom = tab[0];

		// Process moves
		int i = 0;
		Object[] values = fromList.getSelectedValues ();
		if (values.length > 1) {setModelsMute (true);}
		for (i = 0; i < values.length; i++) {
			fromModel.remove (values[i]);
			toModel.add (values[i]);
		}
		if (values.length > 1) {setModelsMute (false);}

		// Deal with selections after moves in both lists
		if (!toModel.isEmpty ()) {
			toList.setSelectedIndex (toModel.getSize () - 1);
			toList.ensureIndexIsVisible (toList.getSelectedIndex ());
		}
		if (!fromModel.isEmpty ()) {
			if (iFrom < fromModel.getSize ()) {
				fromList.setSelectedIndex (iFrom);
			} else {
				fromList.setSelectedIndex (fromModel.getSize () - 1);
			}
			fromList.ensureIndexIsVisible (fromList.getSelectedIndex ());

		}
	}

	/**	Remove elements from list2
	*/
	public void removeAction () {
		// Find max selected index in toList
		int[] tab = toList.getSelectedIndices ();
		int iTo = tab[0];

		int i = 0;
		Object[] values = toList.getSelectedValues ();
		if (values.length > 1) {setModelsMute (true);}
		for (i = 0; i < values.length; i++) {
			toModel.remove (values[i]);
			fromModel.add (values[i]);
		}
		if (values.length > 1) {setModelsMute (false);}

		// Deal with selections after moves in both lists
		if (!fromModel.isEmpty ()) {
			fromList.setSelectedIndex (fromModel.getSize () - 1);
			fromList.ensureIndexIsVisible (fromList.getSelectedIndex ());
		}
		if (!toModel.isEmpty ()) {
			if (iTo < toModel.getSize ()) {
				toList.setSelectedIndex (iTo);
			} else {
				toList.setSelectedIndex (toModel.getSize () - 1);
			}
			toList.ensureIndexIsVisible (toList.getSelectedIndex ());
		}
	}

	/**	Add all element in list1 to list2	
	*/
	public void addAllAction () {
		int i = 0;
		Collection values = fromModel.getContents ();
		toModel.addAll (values);
		
		fromModel.clear ();
		if (!toModel.isEmpty ()) {
			toList.setSelectedIndex (toModel.getSize () - 1);
			toList.ensureIndexIsVisible (toList.getSelectedIndex ());
		}
	}


	/**	Remove all elements from list2
	*/
	public void removeAllAction () {
		int i = 0;
		Collection values = toModel.getContents ();
		fromModel.addAll (values);
		
		toModel.clear ();
		if (!fromModel.isEmpty ()) {
			fromList.setSelectedIndex (fromModel.getSize () - 1);
			fromList.ensureIndexIsVisible (fromList.getSelectedIndex ());
		}
	}


	/**	Add elements of a Group in list2
	*/
	public void addGroupAction () {	// fc - 16.4.2007
		GScene stand = Current.getInstance ().getStep ().getScene ();
		GroupBasedSelector dlg = new GroupBasedSelector (stand);
		if (dlg.isValidDialog ()) {	// valid
			Collection items = dlg.getSelectedItems ();
			System.out.println ("items: "+items );
			
			boolean shouldScroll = false;
			fromList.setSelectionInterval (0, 0);
			
			Collection ids = new ArrayList ();
			
			for (Iterator i = items.iterator (); i.hasNext ();) {
				Identifiable identifiable = (Identifiable) i.next ();
				
				String id = ""+identifiable.getId ();
				ids.add (id);
			}
			
			// fc - 17.4.2007 - if some ids were already in toModel, do not consider them again
			ids.removeAll (toModel.getContents ());
			
			fromModel.removeAll (ids);
			toModel.addAll (ids);
			
			sortAction ();
			
		} else {	// canceled
			
		}
		dlg.dispose ();
		
	}
	
	/**	Sort list1 and list2
	*/
	public void sortAction () {
		java.util.List fromValues = fromModel.getContents ();
		java.util.List toValues = toModel.getContents ();
		
		Comparator comparator = new NumberStringComparator ();
		
		try{	//cp - 30.11.2004
		Collections.sort (fromValues, comparator);
		Collections.sort (toValues, comparator);
		}	//cp - 30.11.2004
		catch (Exception e){};	//cp - 30.11.2004
		
		fromModel.clear ();
		fromModel.addAll (fromValues);
		
		toModel.clear ();
		toModel.addAll (toValues);
		
		if (!fromModel.isEmpty ()) {
			fromList.setSelectedIndex (0);
			fromList.ensureIndexIsVisible (fromList.getSelectedIndex ());
		}
		if (!toModel.isEmpty ()) {
			toList.setSelectedIndex (0);
			toList.ensureIndexIsVisible (toList.getSelectedIndex ());
		}
		
		//~ Object[] fromValues = fromModel.toArray ();
		//~ Object[] toValues = toModel.toArray ();

		//~ Comparator comparator = new NumberStringComparator ();
		//~ try {
			//~ // Try numeric sort
			//~ Arrays.sort (fromValues, comparator);
		//~ } catch (Exception e) {
			//~ Arrays.sort (fromValues);
		//~ }
		//~ //
		//~ try {
			//~ // Try numeric sort
			//~ Arrays.sort (toValues, comparator);
		//~ } catch (Exception e) {
			//~ Arrays.sort (toValues);
		//~ }

		//~ fromModel.clear ();
		//~ for (int i = 0; i < fromValues.length; i++) {
			//~ fromModel.addElement (String.valueOf (fromValues[i]));
		//~ }
		
		//~ toModel.clear ();
		//~ for (int i = 0; i < toValues.length; i++) {
			//~ toModel.addElement (String.valueOf (toValues[i]));
		//~ }
		
		//~ if (!fromModel.isEmpty ()) {
			//~ fromList.setSelectedIndex (0);
			//~ fromList.ensureIndexIsVisible (fromList.getSelectedIndex ());
		//~ }
		//~ if (!toModel.isEmpty ()) {
			//~ toList.setSelectedIndex (0);
			//~ toList.ensureIndexIsVisible (toList.getSelectedIndex ());
		//~ }
		
	}


	/**
	 * Processes actions on an item in the models list.
	 */
	//~ public void valueChanged (ListSelectionEvent evt) {
		//~ JList src = (JList) evt.getSource ();
	//~ }

	/**
	 * Initializes the dialog's GUI.
	 */
	private void createUI () {
		// used only if singleSelection
		LinePanel l0 = new LinePanel ();
		l0.add (new JLabel (Translator.swap ("DListSelector.singleSelection")));
		l0.addStrut0 ();
		
		LinePanel master = new LinePanel ();

		// 1. prepare from list
		fromModel = new SmartListModel ();

		try {
			for (Iterator i = fromVector.iterator (); i.hasNext ();) {
				String id = (String) i.next ();
				if (id != null) {
					fromModel.add (id);					
				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FIndividualSelectorConfigPanel.c ()",
					"Exception while preparing from list", e);
			return;
		}

		// 2. first column : prepare from list
		LinePanel l1 = new LinePanel ();
		l1.add (new JLabel (Translator.swap ("Shared.unSelected")+" : "));
		fromNumber = new JTextField (2);
		fromNumber.setEditable (false);
		l1.add (fromNumber);
		l1.addGlue ();
		
		fromList = new JList (fromModel);
		fromList.setSelectedIndex(0);
		fromList.setVisibleRowCount (LIST_HEIGHT);
		if (singleSelection) {
			fromList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		} else {
			fromList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		JScrollPane panFrom = new JScrollPane (fromList);
		JPanel aux1 = new JPanel (new BorderLayout ());
		aux1.add (l1, BorderLayout.SOUTH);
		aux1.add (panFrom, BorderLayout.CENTER);
		master.add (aux1);

		// 3. second column : buttons
		JPanel boxButtons = new JPanel ();
		boxButtons.setLayout (new BoxLayout (boxButtons, BoxLayout.Y_AXIS));

		ImageIcon icon = null;
		// fc - 6.11.2008
		icon = IconLoader.getIcon ("add-one_16.png");
		addButton = new JButton (icon);
		addButton.addActionListener (this);
		addButton.setToolTipText (Translator.swap ("DListSelector.add"));
		Tools.setSizeExactly (addButton, 24, 24);

		icon =  IconLoader.getIcon ("remove-one_16.png");
		removeButton = new JButton (icon);
		removeButton.addActionListener (this);
		removeButton.setToolTipText (Translator.swap ("DListSelector.remove"));
		Tools.setSizeExactly (removeButton, 24, 24);

		icon = IconLoader.getIcon ("add-all_16.png");
		addAllButton = new JButton (icon);
		addAllButton.addActionListener (this);
		addAllButton.setToolTipText (Translator.swap ("DListSelector.addAll"));
		Tools.setSizeExactly (addAllButton, 24, 24);

		icon = IconLoader.getIcon ("remove-all_16.png");
		removeAllButton = new JButton (icon);
		removeAllButton.addActionListener (this);
		removeAllButton.setToolTipText (Translator.swap ("DListSelector.removeAll"));
		Tools.setSizeExactly (removeAllButton, 24, 24);

		// fc - 16.4.2007
		icon = IconLoader.getIcon ("group_16.png");
		//~ icon = new IconLoader ("toolbarButtonGraphics/navigation/").getIcon (".gif");
		addGroupButton = new JButton (icon);
		addGroupButton.addActionListener (this);
		addGroupButton.setToolTipText (Translator.swap ("DListSelector.addGroup"));
		Tools.setSizeExactly (addGroupButton, 24, 24);

		icon = IconLoader.getIcon ("go-down_16.png");
		sortButton = new JButton (icon);
		sortButton.addActionListener (this);
		sortButton.setToolTipText (Translator.swap ("DListSelector.sort"));
		Tools.setSizeExactly (sortButton, 24, 24);

		boxButtons.add (addButton);
		boxButtons.add (removeButton);
		if (!singleSelection) {		// fc - 28.9.2006
			boxButtons.add (addAllButton);
			boxButtons.add (removeAllButton);
			boxButtons.add (addGroupButton);	// fc - 16.4.2007
		}
		boxButtons.add (sortButton);
		boxButtons.add (Box.createVerticalGlue ());
		master.add (boxButtons);

		// 3.1 ensure same width for all buttons.
		//     BoxLayout tries to enlarge all the components to the biggest's size if maximumSize allows it
		Component c[] = boxButtons.getComponents ();
		int wMax = 0;
		for (int i = 0; i < c.length; i++) {
			try {
				if (!(c[i] instanceof JButton)) {continue;}	// glue...
				JButton b = (JButton) c[i];
				if (b.getPreferredSize ().width > wMax) {
					wMax = b.getPreferredSize ().width;
				}
			} catch (Exception e) {
				Log.println (Log.WARNING, "DListSelector.createUI ()",
						"Trouble while resizing buttons : "+e.toString ());
			}
		}
		for (int i = 0; i < c.length; i++) {
			try {
				JButton b = (JButton) c[i];
				b.setMaximumSize (new Dimension (wMax, b.getPreferredSize ().height));
			} catch (Exception e) {}
		}

		// 4. third column : prepare to list
		toModel = new SmartListModel ();
		
		LinePanel l2 = new LinePanel ();
		l2.add (new JLabel (Translator.swap ("Shared.selected")+" : "));
		toNumber = new JTextField (2);
		toNumber.setEditable (false);
		l2.add (toNumber);
		l2.addGlue ();

		toList = new JList (toModel);
		toList.setSelectedIndex(0);
		toList.setVisibleRowCount (LIST_HEIGHT);
		toList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane panTo = new JScrollPane (toList);
		panTo.setPreferredSize (panFrom.getPreferredSize ());
		panFrom.setPreferredSize (panTo.getPreferredSize ());
		JPanel aux2 = new JPanel (new BorderLayout ());
		aux2.add (l2, BorderLayout.SOUTH);
		aux2.add (panTo, BorderLayout.CENTER);
		master.add (aux2);
		master.addStrut0 ();

		ColumnPanel aux = new ColumnPanel ();
		aux.add (master);
		JPanel orga = new JPanel (new BorderLayout ());
		orga.setLayout (new BorderLayout ());
		if (singleSelection) {
			orga.add (l0, BorderLayout.NORTH);
		}
		
		orga.add (aux, BorderLayout.CENTER);
		orga.setPreferredSize (new Dimension (300, 300));
		sortAction ();

		// 5. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		helpButton = new JButton (Translator.swap ("Shared.help"));
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (helpButton);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		helpButton.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (orga, "Center");
		getContentPane ().add (pControl, "South");

		fromModel.addListDataListener (this);
		toModel.addListDataListener (this);
		fromNumber.setText (""+fromModel.getSize ());
		toNumber.setText (""+toModel.getSize ());

		setTitle (dlgTitle);
		
		setModal (true);

	}

	public boolean getMarkOnly () {
		return false;
	}

	public Vector getSelectedVector () {	
		return selectedVector;
	}

}
