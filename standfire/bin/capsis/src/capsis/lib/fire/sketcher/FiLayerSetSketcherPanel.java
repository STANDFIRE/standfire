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

package capsis.lib.fire.sketcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.border.Border;

import jeeb.lib.sketch.util.SmartColor;
import jeeb.lib.util.ColoredButton;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;

/**
 * FiLayerSetSketcherPanel: a configuration panel for FiLayerSetSketcher
 * 
 * @author F. de Coligny - february 2007
 */
public class FiLayerSetSketcherPanel extends InstantPanel {
	private FiLayerSetSketcher sketcher;

	private JCheckBox filled;
	private JCheckBox extrude;
	// ~ private JCheckBox reverseNormalsOrientation;
	private ColoredButton lineColor;

	// ~ private FiLayerSetSketcherParams s;

	/**
	 * Constructor.
	 */
	public FiLayerSetSketcherPanel(FiLayerSetSketcher sketcher) {
		super(sketcher);
		// ~ s = sketcher.getParams ();
		this.sketcher = sketcher;
		createUI();
	}

	/**
	 * InstantPanel interface.
	 */
	@Override
	public boolean isCorrect() {
		// 1. controls
		// ~ if (!Check.isInt (labelFrequency.getText ().trim ())) {
		// ~ MessageDialog.promptError (Translator.swap
		// ("FiLayerSetSketcher.labelFrequencyShouldBeAnInt"));
		// ~ return false ;
		// ~ }

		// 2. all controls ok, report new configuration
		sketcher.filled = filled.isSelected();
		sketcher.extrude = extrude.isSelected();
		// ~ sketcher.reverseNormalsOrientation =
		// reverseNormalsOrientation.isSelected ();

		return true;
	}

	/**
	 * Called when something changes in config (ex: a check box was changed...)
	 * It will notify the Drawer listener.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(lineColor)) {
			Color newColor = JColorChooser.showDialog(this, Translator.swap("FiLayerSetSketcherPanel.chooseAColor"),
					lineColor.getColor());
			if (newColor != null) {
				sketcher.lineColor = new SmartColor(newColor);
				lineColor.colorize(newColor);
			}
		}

		super.actionPerformed(e);
		sketcher.store();
	}

	/**
	 * Synchronize the radio buttons / check box
	 */
	// ~ private void synchronizeOptions () {
	// ~ }

	/**
	 * Initializes the GUI.
	 */
	private void createUI() {
		Border etched = BorderFactory.createEtchedBorder();

		// ~ ColumnPanel part1 = new ColumnPanel (Translator.swap
		// ("FiLayerSetSketcher.title"), 0, 0);
		// fc - 8.9.2008 - lighter design, underlining
		ColumnPanel part1 = new ColumnPanel(0, 0);
		part1.setMargin(5); // fc - 8.9.2008 - lighter design, underlining
		part1.add(LinePanel.getTitle1(Translator.swap("FiLayerSetSketcher.title")));

		part1.newLine();

		// ~ ColumnPanel part1 = new ColumnPanel (0, 0);
		// ~ JPanel grid1 = new JPanel (new GridLayout (1, 1));
		// ~ grid1.add (part1);
		// ~ JPanel north1 = new JPanel (new BorderLayout ());
		// ~ north1.add (grid1, BorderLayout.NORTH);

		// label
		// ~ ColumnPanel p1 = new ColumnPanel (0, 0);
		// ~ Border b = BorderFactory.createTitledBorder (etched,
		// Translator.swap ("FiLayerSetSketcher.title"));
		// ~ p1.setBorder (b);
		// ~ part1.add (p1);

		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator.swap("FiLayerSetSketcher.lineColor") + " : ", 150));
		lineColor = new ColoredButton(sketcher.lineColor);
		lineColor.setToolTipText("RGB : " + sketcher.lineColor.getRed() + ", " + sketcher.lineColor.getGreen() + ", "
				+ sketcher.lineColor.getBlue());
		lineColor.addActionListener(this);
		l3.add(lineColor);
		l3.addGlue();
		part1.add(l3);

		LinePanel l1 = new LinePanel();
		filled = new JCheckBox(Translator.swap("FiLayerSetSketcher.filled"));
		filled.setSelected(sketcher.filled);
		filled.addActionListener(this);
		l1.add(filled);
		l1.addGlue();

		part1.add(l1);

		LinePanel l4 = new LinePanel();
		extrude = new JCheckBox(Translator.swap("FiLayerSetSketcher.extrude"));
		extrude.setSelected(sketcher.extrude);
		extrude.addActionListener(this);
		l4.add(extrude);
		l4.addGlue();

		part1.add(l4);

		// ~ LinePanel l2 = new LinePanel ();
		// ~ reverseNormalsOrientation = new JCheckBox (Translator.swap
		// ("FiLayerSetSketcher.reverseNormalsOrientation"));
		// ~ reverseNormalsOrientation.setSelected
		// (sketcher.reverseNormalsOrientation);
		// ~ reverseNormalsOrientation.addActionListener (this);
		// ~ l2.add (reverseNormalsOrientation);
		// ~ l2.addGlue ();

		// ~ part1.add (l2);

		setLayout(new BorderLayout());
		add(part1, BorderLayout.NORTH);

		// ~ synchronizeOptions ();

	}

}
