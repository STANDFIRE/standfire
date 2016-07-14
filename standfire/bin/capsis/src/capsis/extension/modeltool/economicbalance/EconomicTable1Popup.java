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
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import jeeb.lib.util.Translator;

/**
 * EconomicTable1Popup is a JPopupMenu.
 *
 * @author F. de Coligny - december 2002
 */
public class EconomicTable1Popup extends JPopupMenu {

	private boolean economicInterventionLine;

	public static final int EXPENSE = 10;
	public static final int INCOME = 11;

	public static final int INSERT_INTERVENTION_BEFORE = 1;
	public static final int INSERT_INTERVENTION_AFTER = 2;
	public static final int REMOVE_INTERVENTION = 3;
	public static final int INSERT_LINE = 4;
	public static final int REMOVE_LINE = 5;

	private JTable table;
	private ActionListener listener;


	public EconomicTable1Popup (JTable table, ActionListener listener,
			boolean IncomePerDiameterRangeDialogue, String noneKey,
			Vector expenseKeys, Vector incomeKeys, Set candidateDates) {

		super ();
		this.table = table;
		this.listener = listener;
		this.economicInterventionLine = economicInterventionLine;

		// Expense submenu contains at least one entry : "[None]" (fr: "[Aucune]")
		JMenu expense = new JMenu (
				Translator.swap ("EconomicTable1Popup.expense")
				);
		add (expense);

		JMenuItem n = new JMenuItem (noneKey, EXPENSE);	// text is the function name (translated)
		n.addActionListener (listener);
		expense.add (n);

		for (Iterator i = expenseKeys.iterator (); i.hasNext ();) {
			String key = (String) i.next ();
			JMenuItem item = new JMenuItem (key, EXPENSE);	// text is the function name (translated)
			item.addActionListener (listener);
			expense.add (item);
		}

		// Income submenu contains at least one entry : "[None]" (fr: "[Aucune]")
		JMenu income = new JMenu (
				Translator.swap ("EconomicTable1Popup.income"));
		add (income);

		n = new JMenuItem (noneKey, INCOME);	// text is the function name (translated)
		n.addActionListener (listener);
		income.add (n);

		for (Iterator i = incomeKeys.iterator (); i.hasNext ();) {
			String key = (String) i.next ();
			JMenuItem item = new JMenuItem (key, INCOME);	// text is the function name (translated)
			item.addActionListener (listener);
			income.add (item);
		}

		addSeparator ();

		// INSERT_INTERVENTION_BEFORE
		JMenu insertInterventionBefore = new JMenu (
				Translator.swap ("EconomicTable1Popup.insertInterventionBefore"));
		add (insertInterventionBefore);

		for (Iterator i = candidateDates.iterator (); i.hasNext ();) {
			String date = (String) i.next ();
			JMenuItem item = new JMenuItem (date, INSERT_INTERVENTION_BEFORE);	// text is the candidate date
			item.addActionListener (listener);
			insertInterventionBefore.add (item);
		}

		// INSERT_INTERVENTION_AFTER
		JMenuItem insertInterventionAfter = new JMenuItem (
				Translator.swap ("EconomicTable1Popup.insertInterventionAfter"),
				EconomicTable1Popup.INSERT_INTERVENTION_AFTER);
		insertInterventionAfter.addActionListener (listener);
		insertInterventionAfter.setEnabled (false);	// temporary, maybe enabled only for last step
		//~ add (insertInterventionAfter);	// fc - 23.1.2003 - temporary desactivation (maybe not proposed to user)

		// REMOVE_INTERVENTION
		JMenuItem removeIntervention = new JMenuItem (
				Translator.swap ("EconomicTable1Popup.removeIntervention"),
				EconomicTable1Popup.REMOVE_INTERVENTION);
		removeIntervention.addActionListener (listener);
		if (!economicInterventionLine) {removeIntervention.setEnabled (false);}
		add (removeIntervention);

		addSeparator ();

		// INSERT_LINE
		JMenuItem insertLine = new JMenuItem (
				Translator.swap ("EconomicTable1Popup.insertLine"),
				EconomicTable1Popup.INSERT_LINE);
		insertLine.addActionListener (listener);
		insertLine.setEnabled (false);	// temporary, not yet in order
		add (insertLine);

		// REMOVE_LINE
		JMenuItem removeLine = new JMenuItem (
				Translator.swap ("EconomicTable1Popup.removeLine"),
				EconomicTable1Popup.REMOVE_LINE);
		removeLine.addActionListener (listener);
		removeLine.setEnabled (false);	// temporary, not yet in order
		add (removeLine);


	}

}
