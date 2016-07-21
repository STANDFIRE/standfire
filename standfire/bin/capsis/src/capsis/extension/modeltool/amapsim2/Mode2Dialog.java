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

package capsis.extension.modeltool.amapsim2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.gui.MainFrame;
import capsis.kernel.Step;

/**
 * Mode2Dialog is a dialog box to enter parameters for the related request.
 *
 * @author F. de Coligny - november 2002
 */
public class Mode2Dialog extends AmapDialog implements ActionListener {

	private Mode2 request;
	private Step step;

	// User data : default values, changeable by user
	private JTextField messageId;	// not changeable
	private JTextField species;

	private JTextField numberOfTreesToBeSimulated;	// Just for user info (Mode2 : 1 for 1)

	private JCheckBox storeLineTree;
	private JCheckBox storeMtg;
	private JCheckBox storeBranches;
	private JCheckBox storeCrownLayers;
	private JCheckBox storePolycyclism;

		private JCheckBox storeTrunkShape;			// 19.1.2004

	private JTextField surface;
	private JTextField numberOfTreesInStand;	// for density
	private JTextField basalArea;
	private JTextField fertilityHDom;
	private JTextField fertilityAge;
	private JTextField coeffAge;

	private JCheckBox useAge;	// for management
	private JCheckBox useH;	// for management
	private JCheckBox useD;	// for management
	private JCheckBox useCrown;	// fc - 5.2.2004

	// Control panel
	private JButton ok;
	private JButton cancel;
	private JButton help;


	/**	Request contains default values to be changed by user.
	*/
	public Mode2Dialog (Mode2 request, Step step) {
		super (MainFrame.getInstance ());
		this.request = request;
		this.step = step;

		setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
		addWindowListener (new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
				escapePressed ();
			}
		});

		createUI ();

		pack ();
		setModal (true);
		
		setVisible (true);

	}

	private void okAction () {
		// checks here
		String id = messageId.getText ().trim ();

		// species is mandatory
		if (Check.isEmpty (species.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.speciesMustBeSpecified"));
			return;
		}

		// surface > 0
		if (Check.isEmpty (surface.getText ().trim ())
				|| !Check.isDouble (surface.getText ().trim ())
				|| Check.doubleValue (surface.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.surfaceMustBePositive"));
			return;
		}

		// numberOfTreesInStand > 0 (for density)
		if (Check.isEmpty (numberOfTreesInStand.getText ().trim ())
				|| !Check.isInt (numberOfTreesInStand.getText ().trim ())
				|| Check.intValue (numberOfTreesInStand.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.numberOfTreesInStandMustBePositive"));
			return;
		}

		// basal area > 0
		if (Check.isEmpty (basalArea.getText ().trim ())
				|| !Check.isDouble (basalArea.getText ().trim ())
				|| Check.doubleValue (basalArea.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.basalAreaMustBePositive"));
			return;
		}

		// fertilityHDom > 0f
		if (Check.isEmpty (fertilityHDom.getText ().trim ())
				|| !Check.isDouble (fertilityHDom.getText ().trim ())
				|| Check.doubleValue (fertilityHDom.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.fertilityHDomMustBePositive"));
			return;
		}

		// fertilityAge > 0f
		if (Check.isEmpty (fertilityAge.getText ().trim ())
				|| !Check.isDouble (fertilityAge.getText ().trim ())
				|| Check.doubleValue (fertilityAge.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode2Dialog.fertilityAgeMustBePositive"));
			return;
		}

		// coeffAge : no controls yet
		// coeffAge : no controls yet
		// coeffAge : no controls yet


		// set data in request and set validation
		updateRequest ();

		setValidDialog (true);
	}

	private void updateRequest () {

		//request.dataLength = 0;		// deferred calculation (see Mode1)
		//request.messageId 			// already set (see Mode2)
		//request.requestType = 1;		// already set (see Mode2)

		request.species = species.getText ().trim ();

		request.storeLineTree = storeLineTree.isSelected ();
		request.storeMtg = storeMtg.isSelected ();
		request.storeBranches = storeBranches.isSelected ();
		request.storeCrownLayers = storeCrownLayers.isSelected ();
		request.storePolycyclism = storePolycyclism.isSelected ();

			request.storeTrunkShape = storeTrunkShape.isSelected ();		// 19.1.2004

		request.surface = (float) Check.doubleValue (surface.getText ().trim ());
		request.numberOfTreesInStand = Check.intValue (numberOfTreesInStand.getText ().trim ());	// for density
		request.basalArea = (float) Check.doubleValue (basalArea.getText ().trim ());
		request.fertilityHDom = (float) Check.doubleValue (fertilityHDom.getText ().trim ());
		request.fertilityAge = (float) Check.doubleValue (fertilityAge.getText ().trim ());
		request.coeffAge = (float) Check.doubleValue (coeffAge.getText ().trim ());

		request.useAge = useAge.isSelected ();
		request.ageMean = 0f;
		request.ageStandardDeviation = 0f;
		request.ageMin = 0f;
		request.ageMax = 0f;
		request.ageg = 0f;
		request.ageDom = 0f;

		request.useH = useH.isSelected ();
		request.HMean = 0f;
		request.HStandardDeviation = 0f;
		request.HMin = 0f;
		request.HMax = 0f;
		request.Hg = 0f;
		request.HDom = 0f;

		request.useD = useD.isSelected ();
		request.DMean = 0f;
		request.DStandardDeviation = 0f;
		request.DMin = 0f;
		request.DMax = 0f;
		request.Dg = 0f;
		request.DDom = 0f;

		// Do not scratch default values for trees in mode 2
		//
		//~ request.numberOfTrees = 0;
		//~ request.trees = new Vector ();
		
		// If crown constraint is not requested, 
		// Discard all individual crown description if provided by 
		// the underlying module tree desciption
		// fc - 5.2.2004
		//
		if (!useCrown.isSelected ()) {
			for (Iterator i = request.trees.iterator (); i.hasNext ();) {
				Mode1.TreeDesc t = (Mode1.TreeDesc) i.next ();
				t.finalCrownBaseHeight = -1;
				t.finalCrownDiameter = -1;
			}
		}
		
	}

	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			escapePressed ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**
	 * Called on Escape. Redefinition of method in AmapDialog.
	 */
	protected void escapePressed () {
		request.setCanceled (true);
		setValidDialog (false);
	}

	// Initializes the dialog's GUI.
	//
	private void createUI () {
		Border etched = BorderFactory.createEtchedBorder ();

		ColumnPanel part1 = new ColumnPanel ();

		part1.add (createTextFieldLine ("messageId", messageId = new JTextField (10), ""+request.messageId));
		messageId.setEditable (false);
		part1.add (createTextFieldLine ("species", species = new JTextField (10), ""+request.species));

		// Just for info (mode 2 : 1 for 1 tree)
		//
		part1.add (createTextFieldLine ("numberOfTreesToBeSimulated", numberOfTreesToBeSimulated = new JTextField (10), ""+request.numberOfTrees));
		numberOfTreesToBeSimulated.setEditable (false);

		// checkBoxes
		part1.add (createCheckBoxLine ("storeLineTree", storeLineTree = new JCheckBox (), request.storeLineTree));
		part1.add (createCheckBoxLine ("storeMtg", storeMtg = new JCheckBox (), request.storeMtg));
		part1.add (createCheckBoxLine ("storeBranches", storeBranches = new JCheckBox (), request.storeBranches));
		part1.add (createCheckBoxLine ("storeCrownLayers", storeCrownLayers = new JCheckBox (), request.storeCrownLayers));
		part1.add (createCheckBoxLine ("storePolycyclism", storePolycyclism = new JCheckBox (), request.storePolycyclism));

			part1.add (createCheckBoxLine ("storeTrunkShape", storeTrunkShape = new JCheckBox (), request.storeTrunkShape));	// 19.1.2004

		part1.add (createTextFieldLine ("surface", surface = new JTextField (10), ""+request.surface));
		part1.add (createTextFieldLine ("numberOfTreesInStand", numberOfTreesInStand = new JTextField (10), ""+request.numberOfTreesInStand));
		part1.add (createTextFieldLine ("basalArea", basalArea = new JTextField (10), ""+request.basalArea));
		part1.add (createTextFieldLine ("fertilityHDom", fertilityHDom = new JTextField (10), ""+request.fertilityHDom));
		part1.add (createTextFieldLine ("fertilityAge", fertilityAge = new JTextField (10), ""+request.fertilityAge));
		part1.add (createTextFieldLine ("coeffAge", coeffAge = new JTextField (10), ""+request.coeffAge));

		// Age
		ColumnPanel sub1 = new ColumnPanel ();
		sub1.add (createCheckBoxLine ("useAge", useAge = new JCheckBox (), request.useAge));
		sub1.addStrut0 ();
		part1.add (sub1);

		// H
		ColumnPanel sub2 = new ColumnPanel ();
		sub2.add (createCheckBoxLine ("useH", useH = new JCheckBox (), request.useH));
		sub2.addStrut0 ();
		part1.add (sub2);

		// D
		ColumnPanel sub3 = new ColumnPanel ();
		sub3.add (createCheckBoxLine ("useD", useD = new JCheckBox (), request.useD));
		sub3.addStrut0 ();
		part1.add (sub3);

		// Crown - fc - 5.2.2004
		boolean moduleProvidesCrown = true;
		for (Iterator i = request.trees.iterator (); i.hasNext ();) {
			Mode1.TreeDesc t = (Mode1.TreeDesc) i.next ();
			if (t.finalCrownBaseHeight <= 0 || t.finalCrownDiameter <= 0) {
				moduleProvidesCrown = false;
				break;
			}
		}
		
		ColumnPanel sub4 = new ColumnPanel ();
		sub4.add (createCheckBoxLine ("useCrown", useCrown = new JCheckBox (), request.useCrown));
		useCrown.setEnabled (moduleProvidesCrown);	// If module does not provide crown, checkbox is disabled
		
useCrown.setEnabled (false);		// Jeff - 6.2.2004 - unused by AMAPsim server
		
		sub4.addStrut0 ();
		part1.add (sub4);

		part1.addGlue ();

		JScrollPane pane1 = new JScrollPane (part1);
		pane1.setPreferredSize (new Dimension (500, 400));

		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (pane1, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

		setTitle (Translator.swap ("Mode2Dialog"));
	}

	// ex : part1.add (createTextFieldLine ("species", species = new JTextField (10)));
	//
	private LinePanel createTextFieldLine (String code, JTextField textField, String value) {
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("Mode2Dialog."+code)+" :", 200));
		textField.setText (value);
		l1.add (textField);
		l1.addStrut0 ();
		return l1;
	}

	//
	//
	private LinePanel createCheckBoxLine (String code, JCheckBox checkBox, boolean selected) {
		LinePanel l1 = new LinePanel ();
		checkBox.setText (Translator.swap ("Mode2Dialog."+code));
		checkBox.setSelected (selected);	// fc - 13.10.2003
		l1.add (checkBox);
		l1.addGlue ();
		return l1;
	}

}



