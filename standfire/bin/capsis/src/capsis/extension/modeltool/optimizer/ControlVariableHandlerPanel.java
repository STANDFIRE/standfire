/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package capsis.extension.modeltool.optimizer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.gui.dnd.DnDPanel;
import capsis.extension.modeltool.optimizer.BoundableVariable.MessageID;

/**
 * The ControlVariableHandlerPanel class is the GUI of ControlVariableHandler.
 * @author Mathieu Fortin - May 2014
 */
public final class ControlVariableHandlerPanel extends REpiceaPanel implements ActionListener {

	protected final ControlVariableHandler caller;
	protected JFormattedNumericField minField;
	protected JFormattedNumericField maxField;
	private JButton delete;
	
	protected ControlVariableHandlerPanel(ControlVariableHandler caller) {
		super();
		this.caller = caller;
		minField = NumberFormatFieldFactory.createNumberFormatField (Type.Double, Range.All, true);
		minField.setColumns(10);
		maxField = NumberFormatFieldFactory.createNumberFormatField (Type.Double, Range.All, true);
		maxField.setColumns(10);
		delete = UIControlManager.createCommonButton(UIControlManager.CommonControlID.Cancel);
		delete.setText("");
		
		setLayout(new BorderLayout());
		Border etchedBorder = BorderFactory.createEtchedBorder();
		this.setBorder(BorderFactory.createTitledBorder(etchedBorder, caller.wrapperLinker.toString()));
		
		refreshInterface();
	}

	@Override
	public void refreshInterface() {
		if (Double.isInfinite (caller.getMinimum())) {
			minField.setText("");
		} else {
			minField.setText(((Double) caller.getMinimum()).toString());
		}
		if (Double.isInfinite(caller.getMaximum())) {
			maxField.setText("");
		} else {
			maxField.setText(((Double) caller.getMaximum()).toString());
		}
		removeAll();
		if (caller.getContainer() != null) {
			JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			add(centerPanel, BorderLayout.CENTER);
			centerPanel.add(caller.getContainer());
		}
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(leftPanel, BorderLayout.WEST);
		leftPanel.add(Box.createHorizontalStrut(5));
		leftPanel.add(UIControlManager.getLabel(MessageID.Minimum));
		leftPanel.add(Box.createHorizontalStrut(5));
		leftPanel.add(minField);
		leftPanel.add(Box.createHorizontalStrut(5));
		leftPanel.add(new JLabel("<"));
		leftPanel.add(Box.createHorizontalStrut(5));

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(rightPanel, BorderLayout.EAST);
		rightPanel.add(Box.createHorizontalStrut(5));
		rightPanel.add(new JLabel("<"));
		rightPanel.add(Box.createHorizontalStrut(5));
		rightPanel.add(UIControlManager.getLabel(MessageID.Maximum));
		rightPanel.add(Box.createHorizontalStrut(5));
		rightPanel.add(maxField);
		rightPanel.add(Box.createHorizontalStrut(5));
		rightPanel.add(delete);
		rightPanel.add(Box.createHorizontalStrut(5));

//		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		add(panel, BorderLayout.EAST);
//		panel.add(UIControlManager.getLabel(MessageID.Minimum));
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(minField);
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(UIControlManager.getLabel(MessageID.Maximum));
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(maxField);
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(delete);
//		panel.add(Box.createHorizontalStrut(5));
	}

	@Override
	public void listenTo () {
		minField.addNumberFieldListener(caller);
		maxField.addNumberFieldListener(caller);
		delete.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore () {
		minField.removeNumberFieldListener(caller);
		maxField.removeNumberFieldListener(caller);
		delete.removeActionListener(this);
	}
		
	protected boolean isFieldAcceptable() {
//		if (caller.isValid()) {
//			setBorder(BorderFactory.createBevelBorder (BevelBorder.LOWERED));
//			return true;
//		} else {
//			setBorder(BorderFactory.createEtchedBorder (Color.RED, Color.GRAY));
//			return false;
//		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource ().equals(delete)) {
			DnDPanel panel = (DnDPanel) CommonGuiUtility.getParentComponent(this, DnDPanel.class);
			panel.removeSubpanel(caller);
		}
	}


}
