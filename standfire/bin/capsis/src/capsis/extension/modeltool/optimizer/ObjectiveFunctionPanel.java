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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.apache.commons.math.optimization.GoalType;

import repicea.gui.REpiceaPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The ObjectiveFunctionPanel class is the GUI of the ObjectiveFunction class.
 * @author Mathieu Fortin - October 2014
 */
class ObjectiveFunctionPanel extends REpiceaPanel implements ItemListener {

	
	protected static enum MessageID implements TextableEnum {

		Maximization_Minization("Goal - Maximizing/Minimizing", "Objectif - Maximisation/Minimisation"),
		Constaints_Indicators("Indicators and Constraints", "Indicateurs et contraintes"),
		Objective("Objective", "Objectif"),
		Minimization("Minimizing", "Minimisation"),
		Maximization("Maximizing", "Maximisation");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}

	
	private final JRadioButton minimizationButton;
	private final JRadioButton maximizationButton;
	private final ObjectiveFunction caller;

	
	
	
	
	protected ObjectiveFunctionPanel(ObjectiveFunction caller) {
		this.caller = caller;
		setLayout(new BorderLayout());
		
		Border etchedBorder = BorderFactory.createEtchedBorder();
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		add(subPanel, BorderLayout.NORTH);

		subPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.Maximization_Minization.toString()));

		minimizationButton = new JRadioButton(MessageID.Minimization.toString());
		maximizationButton = new JRadioButton(MessageID.Maximization.toString());
		ButtonGroup bg = new ButtonGroup();
		bg.add (minimizationButton);
		bg.add (maximizationButton);

		subPanel.add(Box.createVerticalStrut(5));

		JPanel tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(5));
		tmpPanel.add(minimizationButton);
		subPanel.add(tmpPanel);
		subPanel.add(Box.createVerticalStrut(5));
		
		tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(5));
		tmpPanel.add(maximizationButton);
		subPanel.add(tmpPanel);
		subPanel.add(Box.createVerticalStrut(5));
		
		add(caller.indicators.getUI(), BorderLayout.CENTER);
	}
	
	
	
	@Override
	public void refreshInterface() {
		maximizationButton.setSelected(caller.goal == GoalType.MAXIMIZE);
		minimizationButton.setSelected(caller.goal == GoalType.MINIMIZE);
		caller.indicators.getUI().refreshInterface();
	}

	@Override
	public void listenTo() {
		maximizationButton.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		maximizationButton.removeItemListener(this);
	}
	
	@Override
	public void itemStateChanged (ItemEvent evt) {
		if (evt.getSource().equals(maximizationButton)){
			if (maximizationButton.isSelected()) {
				caller.goal = GoalType.MAXIMIZE;
			} else {
				caller.goal = GoalType.MINIMIZE;
			}
		}
	}


}
