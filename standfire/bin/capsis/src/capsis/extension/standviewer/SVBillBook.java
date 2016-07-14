/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
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

package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.projectmanager.StepButton;
import capsis.extension.AbstractStandViewer;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.economics.BBCashFlow;
import capsis.lib.economics.BBCashFlowLine;
import capsis.lib.economics.BillBookCompatible;
import capsis.lib.economics.BillBookLine;
import capsis.lib.economics.CurrencyRenderer;

/**	SVBillBook builds a billbook of the projects with BillBookCompatible stands
* 
*	@author F. de Coligny, O. Pain - november 2007
*/
public class SVBillBook extends AbstractStandViewer implements ActionListener {
	
	static public String AUTHOR = "F. de Coligny, O. Pain";
	static public String VERSION = "1.0";
	

	private static class BillBookTableModel extends AbstractTableModel {
		private ArrayList data;
		private String[] columnNames = {
				Translator.swap ("SVBillBook.rotationOrder"),
				Translator.swap ("SVBillBook.date"),
				Translator.swap ("SVBillBook.name"),
				Translator.swap ("SVBillBook.detail"),
				Translator.swap ("SVBillBook.expenseType"),
				Translator.swap ("SVBillBook.quantity"),
				Translator.swap ("SVBillBook.unit"),
				Translator.swap ("SVBillBook.unitPrice"),
				Translator.swap ("SVBillBook.fuelConsumption"), 
				Translator.swap ("SVBillBook.total")};
		public BillBookTableModel (Collection<BBCashFlowLine> lines) {
			data = new ArrayList (lines);
		}
		public Class getColumnClass (int col) {
			if (col == 7 || col == 9) {return Currency.class;} 
			return data == null ? null : getValueAt (0, col).getClass ();
		}
		public int getColumnCount () {return columnNames.length;}
		public String getColumnName (int col) {
			return columnNames[col];
		}
		public int getRowCount () {return data == null ? 0 : data.size ();}
		public Object getValueAt (int row, int col) {
			BBCashFlowLine line = (BBCashFlowLine) data.get (row);
			switch (col) {
				case 0: return line.getBillBookRotationOrder ();
				case 1: return line.getBillBookYear ();
				case 2: return line.getBillBookOperation ();
				case 3: return line.getBillBookDetail ();
				case 4: return Translator.swap (line.getBillBookType ());
				case 5: return line.getBillBookQuantity ();
				case 6: return Translator.swap (line.getBillBookQuantityUnit ());
				case 7: return line.getBillBookUnitPrice ();
				case 8: return line.getBillBookTotalFuelConsumption ();
					
				case 9: return line.getCashFlow ();	// fc + op - 20.3.2008
			}
			return "";
		}
		
		
	}
	

	static {
		Translator.addBundle("capsis.extension.standviewer.SVBillBook");
	} 
	private JCheckBox includeAnnualAndVariableCosts;	// fc + op - 7.5.2008
	private JScrollPane scroll;
	
	
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
	
		try {			
			createUI ();
			update ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVBillBook.c ()", "Error in constructor", e);
			throw e;	// propagate
		}
	}
	
	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			Step rootStep = (Step) m.getProject ().getRoot ();
			GScene rootStand = rootStep.getScene ();
			if (!(rootStand instanceof BillBookCompatible)) {return false;}
			return true;
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVBillBook.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}


	
	/**	Update the viewer on a given step button
	*/
	public void update (StepButton sb) {
		super.update (sb);		// computes boolean sameStep
		update ();	// for example
	}

	/**	Update the viewer with the current step button
	*/
	public void update () {
		super.update ();
		if (sameStep) {return;}
		
		// Retrieve Steps from root to this step
		Collection<BillBookLine> lines = new ArrayList<BillBookLine> ();
		Vector steps = step.getProject ().getStepsFromRoot (step);
		
		BBCashFlow cf = new BBCashFlow (steps, includeAnnualAndVariableCosts.isSelected ());
		
			
		BillBookTableModel model = new BillBookTableModel (cf.getLines ());
		//~ BillBookTableModel model = new BillBookTableModel (lines);
		
		JTable table = new JTable (model);
		table.setDefaultEditor (Object.class, null);
		
		table.setDefaultRenderer (Currency.class, new CurrencyRenderer ());
		
		table.getColumnModel ().getColumn (0).setPreferredWidth (5);
		table.getColumnModel ().getColumn (1).setPreferredWidth (5);
		table.getColumnModel ().getColumn (2).setPreferredWidth (200);
		table.getColumnModel ().getColumn (3).setPreferredWidth (150);
		table.getColumnModel ().getColumn (4).setPreferredWidth (20);
		table.getColumnModel ().getColumn (5).setPreferredWidth (20);
		table.getColumnModel ().getColumn (6).setPreferredWidth (10);
		table.getColumnModel ().getColumn (7).setPreferredWidth (20);
		table.getColumnModel ().getColumn (8).setPreferredWidth (20);
		table.getColumnModel ().getColumn (9).setPreferredWidth (50);
		
		scroll.getViewport ().setView (table);
	}

	/**	Create the user interface
	*/
	public void createUI () {
		setLayout (new BorderLayout ());
		
		LinePanel l0 = new LinePanel ();
		//~ l0.add (new JLabel (Translator.swap ("SVFinancialReport.projectReportTable")+" : "));

		boolean includeCosts;
		includeCosts = Settings.getProperty ("bill.book.include.annual.and.variable.costs", false);
		
		includeAnnualAndVariableCosts = new JCheckBox (
				Translator.swap ("SVBillBook.includeAnnualAndVariableCosts"), includeCosts);
		includeAnnualAndVariableCosts.addActionListener (this);
		l0.add (includeAnnualAndVariableCosts);
		l0.addGlue ();
		add (l0, BorderLayout.NORTH);

		scroll = new JScrollPane ();
		add (scroll, BorderLayout.CENTER);
		
	}

	/**	Used for the settings buttons.
	*/
	public void actionPerformed (ActionEvent evt) {
		update ();
	}
	

}

