/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
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

package capsis.extension.modeltool.economicbalance;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import jeeb.lib.util.Translator;

/**
 * EconomicTable2Popup is a JPopupMenu.
 *
 * @author C. Orazio - january 2003
 */
public class EconomicTable2Popup extends JPopupMenu {

	public static final int INSERT_LINE_BEFORE = 20;
	public static final int INSERT_LINE_AFTER = 21;
	public static final int REMOVE_LINE = 22;

	private JTable table;
	private ActionListener listener;


	public EconomicTable2Popup (JTable table, ActionListener listener) {
		super ();
		this.table = table;
		this.listener = listener;

		// INSERT_LINE_BEFORE
		JMenuItem insertLineBefore = new JMenuItem (
				Translator.swap ("EconomicTable2Popup.insertLineBefore"),
				EconomicTable2Popup.INSERT_LINE_BEFORE);
		insertLineBefore.addActionListener (listener);
		add (insertLineBefore);

		// INSERT_LINE_AFTER
		JMenuItem insertLineAfter = new JMenuItem (
				Translator.swap ("EconomicTable2Popup.insertLineAfter"),
				EconomicTable2Popup.INSERT_LINE_AFTER);
		insertLineAfter.addActionListener (listener);
		add (insertLineAfter);

		// REMOVE_LINE
		JMenuItem removeLine = new JMenuItem (
				Translator.swap ("EconomicTable2Popup.removeLine"),
				EconomicTable2Popup.REMOVE_LINE);
		removeLine.addActionListener (listener);
		add (removeLine);


	}

}
