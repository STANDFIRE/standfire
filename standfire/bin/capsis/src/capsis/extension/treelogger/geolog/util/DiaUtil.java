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

package capsis.extension.treelogger.geolog.util;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;

/**	DiaUtil : utility methods for Dialogs
*	All the methods are static
*
*	@author F. Mothe - february 2006
*/
public class DiaUtil {

	//	Size :

//	public static void freezeSize (JComponent component) {
//		Tools.setFixedSize (component, component.getPreferredSize ());
//	}

	public static void setWidth (JComponent component, int width) {
		Tools.setFixedSize (component, new Dimension (
				width, component.getPreferredSize ().height));
	}

	public static void setHeight (JComponent component, int height) {
		Tools.setFixedSize (component, new Dimension (
				component.getPreferredSize ().width, height));
	}

	//	Line arrangements :

	// Warning : does not return a JLabel !
	public static JComponent newTextWidth (String text, boolean translate,
			int width) {
		Box l1 = Box.createHorizontalBox ();
		if (translate) {
			text = Translator.swap (text);
		}
		JLabel label = new JLabel (text);
		l1.add (label);
		setWidth (l1, Math.max (width, l1.getPreferredSize ().width));
		//l1.setBorder (BorderFactory.createEtchedBorder ());
		return l1;
	}

	public static JComponent newTextAlone (String text, boolean translate,
			int width) {
		Box l1 = Box.createHorizontalBox ();
		if (text != null) {
			l1.add (newTextWidth (text, translate, width));
			// l1.add (Box.createHorizontalStrut (5));
		}
		l1.add (Box.createHorizontalGlue ());
		//l1.setBorder (BorderFactory.createEtchedBorder ());
		return l1;
	}

	public static JComponent newTextComponent (String text, int width,
			JComponent component) {
		// JPanel l1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box l1 = Box.createHorizontalBox ();
		if (text != null) {
			l1.add (newTextWidth (Translator.swap (text)+" :", false, width));
			l1.add (Box.createHorizontalStrut (5));
		}
//		freezeSize (component);
		l1.add (component);
		l1.add (Box.createHorizontalGlue ());
		//l1.setBorder (BorderFactory.createEtchedBorder ());
		//l1.setAlignmentX (Component.LEFT_ALIGNMENT);
		return l1;
	}

	static
	public < T > JComponent newTextComponent (String text,  int width,
			JTextField textField, int nbCol, T value) {
		textField.setColumns (nbCol);
		textField.setText ("" + value);
		return newTextComponent (text, width, textField);
	}

	public static JComponent newTextComponent (String text,  int width,
			JCheckBox checkBox, int unused, boolean value) {
		// Result : "text : [x]"
		return newTextCheckBox (text, width, checkBox, value);
	}

	public static JComponent newTextCheckBox (String text,  int width,
			JCheckBox checkBox, boolean value) {
		// Result : "text : [x]"
		return newTextCheckBox (text, width, checkBox, null, value);
	}

	public static JComponent newTextCheckBox (JCheckBox checkBox,
			String text, boolean value) {
		// Result : "[x] text"
		return newTextCheckBox (null, 0, checkBox, text, value);
	}

	public static JComponent newTextCheckBox (String textLeft, int width,
			JCheckBox checkBox, String textRight, boolean value) {
		// Result : "textLeft (length=width) : [x] textRight"
		checkBox.setSelected (value);
		if (textRight != null)
			checkBox.setText (Translator.swap (textRight));
		return newTextComponent (textLeft, width, checkBox);
	}

	//	Check value :

	public static class CheckException extends Exception {
		private static final long serialVersionUID = 20060318L;	// avoid java warning
		CheckException (String message) { super (message); }
	}

	public static int checkedIntegerValue (String s, String prefix,
			boolean checkInt, boolean checkPositive, boolean checkNotNul)
			throws CheckException
	{
		s = s.trim();
		String errorMsg = prefix + " : ";
		if (checkInt && !Check.isInt (s)) {
			errorMsg += Translator.swap ("DiaUtil.MustBeInteger");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		int value = Check.intValue (s);
		if (checkPositive && value < 0) {
			errorMsg += Translator.swap ("DiaUtil.MustBePositive");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		if (checkNotNul && value == 0) {
			errorMsg += Translator.swap ("DiaUtil.MustBeNotNull");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		return value;
	}

	public static long checkedLongValue (String s, String prefix,
			boolean checkLong, boolean checkPositive, boolean checkNotNul)
			throws CheckException
	{
		s = s.trim();
		String errorMsg = prefix + " : ";
		// if (checkLong && !Check.isLong (s)) {
		if (checkLong && !DiaUtil.isLong (s)) {
			errorMsg += Translator.swap ("DiaUtil.MustBeLong");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		//long value = Check.longValue (s);
		long value = DiaUtil.longValue (s);
		if (checkPositive && value < 0L) {
			errorMsg += Translator.swap ("DiaUtil.MustBePositive");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		if (checkNotNul && value == 0L) {
			errorMsg += Translator.swap ("DiaUtil.MustBeNotNull");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		return value;
	}

	public static double checkedDoubleValue (String s, String prefix,
			boolean checkDouble, boolean checkPositive, boolean checkNotNul)
			throws CheckException
	{
		s = s.trim();
		String errorMsg = prefix + " : ";
		if (checkDouble && !Check.isDouble (s)) {
			errorMsg += Translator.swap ("DiaUtil.MustBeDouble");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		double value = Check.doubleValue (s);
		if (checkPositive && value < 0) {
			errorMsg += Translator.swap ("DiaUtil.MustBePositive");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		if (checkNotNul && value == 0.) {
			errorMsg += Translator.swap ("DiaUtil.MustBeNotNull");
			MessageDialog.print (null, errorMsg);
			throw new CheckException (errorMsg);
		}
		return value;
	}

	// Proposal : add this to Capsis.Util.Check :
	/**	Return true if the String parameter is a long.
	*/
	public static boolean isLong (String s) {
		boolean r = false;
		try {
			long a = Long.valueOf (s.trim ()).longValue ();
			r = true;
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}
	/**	Return the long value of the String parameter, 0 if trouble.
	*/
	public static long longValue (String s) {
		long r = 0;
		try {
			r = Long.valueOf (s.trim ()).longValue ();
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}

}
