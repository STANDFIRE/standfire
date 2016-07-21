package capsis.lib.economics2.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import jeeb.lib.util.Translator;

public class EconomicTablePopUp extends JPopupMenu {

	public static final int INSERT_LINE_AFTER = 20;
	public static final int REMOVE_LINE = 21;
	public static final int REORDER = 22;


	private JTable table;
	private ActionListener listener;


	public EconomicTablePopUp (JTable table, ActionListener listener) {
		super ();
		this.table = table;
		this.listener = listener;

		// INSERT_LINE_BEFORE
		JMenuItem insertLineBefore = new JMenuItem (
				Translator.swap ("EconomicTablePopUp.insertLineAfter"),
				EconomicTablePopUp.INSERT_LINE_AFTER);
		insertLineBefore.addActionListener (listener);
		add (insertLineBefore);

		// REMOVE_LINE
		JMenuItem removeLine = new JMenuItem (
				Translator.swap ("EconomicTablePopUp.removeLine"),
				EconomicTablePopUp.REMOVE_LINE);
		removeLine.addActionListener (listener);
		add (removeLine);
		
		// reorder
		JMenuItem insertLineAfter = new JMenuItem (
				Translator.swap ("EconomicTablePopUp.reorder"),
				EconomicTablePopUp.REORDER);
		insertLineAfter.addActionListener (listener);
		add (insertLineAfter);


	}

}
