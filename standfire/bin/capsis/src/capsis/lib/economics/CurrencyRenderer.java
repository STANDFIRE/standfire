package capsis.lib.economics;

import java.awt.Component;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**	CurrencyRenderer: a table cell renderer for columns containing Currency 
*	values in a JTable.
*	@author O. Pain - december 2007
*/
public class CurrencyRenderer extends DefaultTableCellRenderer implements Serializable {
	static private NumberFormat nf;
	static private Currency currency;
	
	public CurrencyRenderer () {
		super ();
		nf = NumberFormat.getInstance ();
		nf.setMinimumFractionDigits (2);
		nf.setMaximumFractionDigits (2);
		nf.setGroupingUsed (true);
		
		currency = Currency.getInstance (Locale.FRANCE);
	}
	
	
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		Component c = super.getTableCellRendererComponent (table, value, 
				isSelected, hasFocus, rowIndex, vColIndex);
		JLabel l = (JLabel) c;
		StringBuffer b = new StringBuffer (nf.format (value));
		b.append (' ');
		b.append (currency.getSymbol ());
		l.setText (b.toString ());
		l.setHorizontalAlignment (JLabel.RIGHT);
		return l;
	}
}
