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

package capsis.extension;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MemoPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.gui.DListSelector;
import capsis.gui.GrouperChooser;
import capsis.gui.StatusChooser;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.GrouperManager;

/**
 * Configuration panel for DataExtractors, single extractor configuration mode.
 * 
 * @see DEMultiConfPanel
 * 
 * @author F. de Coligny - march 2003
 */
public class DEConfigurationPanel extends ConfigurationPanel implements ActionListener {
	private AbstractDataExtractor ex;

	// treeIds
	protected JTextField treeIdsTF;
	protected JButton choose;

	// Groupers
	public GrouperChooser grouperChooser;

	// Status
	public StatusChooser statusChooser;

	/**
	 * Constructor, build user interface.
	 */
	public DEConfigurationPanel(Configurable c) {
		super(c);

		ex = (AbstractDataExtractor) c;

		ColumnPanel master = new ColumnPanel();

		// Tree ids selection
		//
		// 1.1 for TREE_IDS property
		// check if model manages individual trees
		// disable TREE_IDS property if not
		// [C. Meredieu, T. Labb�] lemoine model is stand level, no trees
		// fc - 12.10.2006
		if (ex.hasConfigProperty(AbstractDataExtractor.TREE_IDS)) {
			Step step = ex.getStep();
			GScene stand = step.getScene();
			boolean modelWithTrees = stand instanceof TreeCollection;
			ex.setPropertyEnabled(AbstractDataExtractor.TREE_IDS, modelWithTrees);
		}

		// 1.2 create a treeIds chooser
		if (ex.hasConfigProperty(AbstractDataExtractor.TREE_IDS)) {
			LinePanel l0 = new LinePanel();
			l0.add(new JLabel(Translator.swap("DEConfigurationPanel.concernedIndividuals") + " :"));
			String ids = "";
			for (Iterator i = ex.treeIds.iterator(); i.hasNext();) {
				String id = (String) i.next();
				ids += id;
				if (i.hasNext()) {
					ids += ", ";
				}
			}
			treeIdsTF = new JTextField(ids, 2);
			l0.add(treeIdsTF);
			choose = new JButton(Translator.swap("DEConfigurationPanel.choose"));
			choose.addActionListener(this);

			if (!ex.isPropertyEnabled(AbstractDataExtractor.TREE_IDS)) { // fc -
																			// 6.2.2004
				treeIdsTF.setEnabled(false);
				choose.setEnabled(false);
			}

			l0.add(choose);
			l0.addStrut0();

			master.add(l0);
		}

		// Individual tree / cell grouper : for this extractor only
		// fc - 6.5.2003
		//
		// ~ System.out.println
		// ("DEConfigurationPanel: ex.getIGrouperType () "+ex.getIGrouperType
		// ());
		if (ex.getIGrouperType() != null) {
			String type = ex.getIGrouperType();

			boolean checked = !ex.isCommonGrouper() && ex.isGrouperMode();
			GrouperManager gm = GrouperManager.getInstance();
			String selectedGrouperName = gm.removeNot(ex.getGrouperName());
			grouperChooser = new GrouperChooser(ex.getStep().getScene(), type, selectedGrouperName, ex.isGrouperNot(),
					true, checked);

			LinePanel l8 = new LinePanel();
			l8.add(grouperChooser);
			l8.addStrut0();
			master.add(l8);
		}

		// Choose trees status if available (Ex: alive / cut / dead...)
		// fc - 22.3.2004 / 22.4.2004
		//
		if (ex.hasConfigProperty(AbstractDataExtractor.STATUS)) {
			if (ex.getStep().getScene() instanceof TreeList) {
				TreeList stand = (TreeList) ex.getStep().getScene();
				statusChooser = new StatusChooser(stand.getStatusKeys(), ex.getStatusSelection());
				master.add(statusChooser);
			}
		}

		// General layout
		//
		if (master.getComponents().length != 0) {
			setLayout(new BorderLayout());
			add(master, BorderLayout.NORTH);
			// ~ add (new JScrollPane (master), BorderLayout.CENTER); //
			// JScrollPane: not here (fc)
		}
		
		
	}

	public GrouperChooser getGrouperChooser() {
		return grouperChooser;
	}

	public StatusChooser getStatusChooser() {
		return statusChooser;
	}

	/**
	 * Actions management.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == choose) {
			chooseIds();
		}
	}

	// Dialog to choose some ids.
	//
	private void chooseIds() {
		Vector<String> ids = new Vector<String>();
		Collection<? extends Tree> v = ((TreeCollection) ex.getStep().getScene()).getTrees();
		for (Tree t : v) {

			String id = "" + t.getId(); // String

			// fc - 19.9.2005 - if Numberable, show number in list
			if (t instanceof Numberable) {
				double number = ((Numberable) t).getNumber();
				// if (number <= 0) {continue;} // fc - correction for
				// C.Meredieu : wants these trees to be selectable here
				// (20.9.2005)
				id += " (number=" + number + ")";
			}

			ids.add(id);
		}

		// fc - 28.9.2006 - added singleSelection device
		boolean singleSelection = ex.isSingleIndividual();

		DListSelector dlg = new DListSelector(Translator.swap("DEConfigurationPanel.individualsSelection"),
				Translator.swap("DEConfigurationPanel.individualsSelectionText"), ids, singleSelection);

		if (dlg.isValidDialog()) {
			String txt = "";
			for (Iterator i = dlg.getSelectedVector().iterator(); i.hasNext();) {
				String id = (String) i.next();

				// fc - 19.9.2005 - discard "(number=12)" in case of id of
				// Numberable
				int space = id.indexOf(" ");
				if (space != -1) {
					id = id.substring(0, space);
				}
				txt += id;

				if (i.hasNext()) {
					txt += ", ";
				}
			}
			treeIdsTF.setText(txt);
		}
	}

	public boolean checksAreOk() {

		// fc - 22.4.2004 - at least one check needed
		//
		if (statusChooser != null) {
			if (!statusChooser.isChooserValid()) {
				MessageDialog.print(this, Translator.swap("Shared.chooseAtLeastOneStatus"));
				return false;
			}
		}

		// fc - 23.3.2004
		if (grouperChooser != null && grouperChooser.isGrouperAvailable()) {
			if (grouperChooser.getGrouperName() == null || grouperChooser.getGrouperName().equals("")) {
				MessageDialog.print(this, Translator.swap("DEMultiConfPanel.wrongGrouperName"));
				return false;
			}
		}

		return true;
	}

}
