/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2015  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.Border;

import repicea.gui.REpiceaPanel;
import capsis.extension.modeltool.optimizer.ObjectiveFunctionPanel.MessageID;

public class IndicatorListPanel extends REpiceaPanel {

	protected final IndicatorList caller;
	
	protected IndicatorListPanel(IndicatorList caller) {
		this.caller = caller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Border etchedBorder = BorderFactory.createEtchedBorder();
		
		setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.Constaints_Indicators.toString()));
	}
	
	
	@Override
	public void refreshInterface() {
		removeAll();
		add(Box.createVerticalStrut(5));
		
		for (Indicator indicator : caller) {
			add(indicator.getUI());
			add(Box.createVerticalStrut(5));
		}
	}

	@Override
	public void listenTo() {}

	@Override
	public void doNotListenToAnymore() {}

}
