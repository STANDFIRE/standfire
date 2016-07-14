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

package capsis.lib.fire.intervener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;

/**
 * This dialog box is used to set FuelMoistureAdjuster parameters in interactive
 * context.
 * 
 * 
 * 
 * @author F. Pimont - sept 2011
 */
public class FuelMoistureAdjusterDialog extends AmapDialog implements ActionListener, TableModelListener {

	private JTextField treeLiveMoisture;
	double previousTreeLiveMoisture;
	double previousTreeDeadMoisture;
	double previousTreeLiveTwigMoisture;

	private JTextField treeDeadMoisture;
	private JTextField treeLiveTwigMoisture;

	private JTable table;
	private FuelMoistureFormTableModel tableModel;

	private JPanel scroll; // to show the form inside

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	private ButtonGroup rdGroup;

	private Collection<FmLayerSet> layerSets;
	private Collection<String> layerSetNames;
	private Map<String, FmLayerSet> layerSetMap = new HashMap<String, FmLayerSet>();

	public FuelMoistureAdjusterDialog(double tlm, double tdm, double tltm, Collection<FmLayerSet> layerSets) {
		// Map<FmLayerSet, Double> shrubLiveMoisture,
		// Map<FmLayerSet, Double> shrubDeadMoisture,
		// Map<FmLayerSet, Double> herbLiveMoisture,
		// Map<FmLayerSet, Double> herbDeadMoisture) {
		super();
		this.previousTreeLiveMoisture = tlm;
		this.previousTreeDeadMoisture = tdm;
		this.previousTreeLiveTwigMoisture = tltm;
		this.layerSets = layerSets;
		tableModel = new FuelMoistureFormTableModel();
		tableModel.addTableModelListener(this);
		tableModel.setLayerSets(layerSets);
		layerSetNames = new Vector<String>();
		for (FmLayerSet ls : layerSets) {
			String name = ls.getName();
			layerSetNames.add(name);
			layerSetMap.put(name, ls);
		}
		// System.out.println("layersetthinnerdiaglog after :" +
		// layerSets.size());

		createUI();
		setTitle(Translator.swap("FuelMoistureAdjusterDialog"));
		setModal(true);
		// location is set by AmapDialog
		pack(); // uses component's preferredSize
		show();

	}

	/**
	 * Accessor for context.
	 */
	public double getTreeLiveMoisture() {
		return Check.doubleValue(treeLiveMoisture.getText().trim());
	}

	public double getTreeDeadMoisture() {
		return Check.doubleValue(treeDeadMoisture.getText().trim());
	}

	public double getTreeLiveTwigMoisture() {
		return Check.doubleValue(treeLiveTwigMoisture.getText().trim());
	}

	//
	// Action on ok button.
	//
	private void okAction() {
		// Checks...
		if (!Check.isDouble(treeLiveMoisture.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeLiveMoistureMustBeANumberGreaterThanZero"));
			return;
		}

		double _treeLiveMoisture = Check.doubleValue(treeLiveMoisture.getText().trim());
		if (_treeLiveMoisture < 0.0) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeLiveMoistureMustBeANumberGreaterThanZero"));
			return;
		}
		if (!Check.isDouble(treeDeadMoisture.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeDeadMoistureMustBeANumberGreaterThanZero"));
			return;
		}

		double _treeDeadMoisture = Check.doubleValue(treeDeadMoisture.getText().trim());
		if (_treeDeadMoisture < 0.0) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeDeadMoistureMustBeANumberGreaterThanZero"));
			return;
		}
		if (!Check.isDouble(treeLiveTwigMoisture.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeLiveeTwigMoistureMustBeANumberGreaterThanZero"));
			return;
		}

		double _treeLiveTwigMoisture = Check.doubleValue(treeLiveTwigMoisture.getText().trim());
		if (_treeLiveTwigMoisture < 0.0) {
			MessageDialog.print(this,
					Translator.swap("FuelMoistureAdjusterDialog.treeLiveTwigMoistureMustBeANumberGreaterThanZero"));
			return;
		}
		// Check layers in table
		for (FmLayerSet ls : layerSets) {
			for (FmLayer ll : ls.getFmLayers()) {

				try {
					if (ll.getMoisture(0, FiParticle.LIVE) < 0 || ll.getMoisture(0, FiParticle.DEAD) < 0) {
						MessageDialog
								.print(this,
										Translator
												.swap("FuelMoistureAdjusterDialog.layerMoistureMustBeANumberBetweenZeroAndHundredInLayerSet"
														+ ls.getName()));
						return;
					}
				} catch (Exception e) {
					// TODO FP Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		setValidDialog(true);
	}

	//
	// Action on cancel button.
	//
	private void cancelAction() {
		setValidDialog(false);
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			cancelAction();
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	//
	// Create the dialog box user interface.
	//
	private void createUI() {

		// 1. Util panel
		ColumnPanel panel = new ColumnPanel();

		LinePanel l1 = new LinePanel();
		l1.add(new JLabel(Translator.swap("FuelMoistureAdjusterDialog.adjustingTreeMoisture") + " :"));
		l1.addGlue();
		panel.add(l1);
		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator.swap("FuelMoistureAdjusterDialog.treeLiveMoisture") + " :", 160));
		treeLiveMoisture = new JTextField(5);
		treeLiveMoisture.setText("" + (this.previousTreeLiveMoisture));
		l2.add(treeLiveMoisture);
		l2.addStrut0();
		panel.add(l2);
		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator.swap("FuelMoistureAdjusterDialog.treeDeadMoisture") + " :", 160));
		treeDeadMoisture = new JTextField(5);
		treeDeadMoisture.setText("" + (this.previousTreeDeadMoisture));
		l3.add(treeDeadMoisture);
		l3.addStrut0();
		panel.add(l3);
		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator.swap("FuelMoistureAdjusterDialog.treeLiveTwigMoisture") + " :", 160));
		treeLiveTwigMoisture = new JTextField(5);
		treeLiveTwigMoisture.setText("" + (this.previousTreeLiveTwigMoisture));
		l4.add(treeLiveTwigMoisture);
		l4.addStrut0();
		panel.add(l4);

		panel.addGlue();

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.NORTH);

		// Table
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder(Translator.swap("FuelMoistureForm.table")));

		table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setPreferredWidth(300);
		JScrollPane scroll = new JScrollPane(table);
		scroll.setMinimumSize(new Dimension(300, 100)); // fc - 27.10.2009
		tablePanel.add(scroll, BorderLayout.CENTER);

		getContentPane().add(tablePanel, BorderLayout.CENTER);
		// part2.add (tablePanel);
		// part2.add (bottom);
		// part2.setMinimumSize (new Dimension (100, 150));

		// 2. control panel (ok cancel help);
		JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ok = new JButton(Translator.swap("Shared.ok"));
		cancel = new JButton(Translator.swap("Shared.cancel"));
		help = new JButton(Translator.swap("Shared.help"));
		pControl.add(ok);
		pControl.add(cancel);
		pControl.add(help);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
		getContentPane().add(pControl, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);

	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getSource().equals(tableModel)) {

			// if (mute) {return;}
			tellSomethingHappened(null);
		}
	}

}
