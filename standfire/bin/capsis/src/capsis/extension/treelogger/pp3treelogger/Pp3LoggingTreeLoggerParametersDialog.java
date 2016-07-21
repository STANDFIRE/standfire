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

package capsis.extension.treelogger.pp3treelogger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;
import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.REpiceaTranslator;
import capsis.commongui.util.Helper;

/**	Pp3LoggingDialog is a dialog box for Pp3Logging
*
*	@author C. Meredieu + T. Labb� - march 2006
*/

//public class Pp3LoggingDialog extends AmapDialog
//		implements ActionListener, ListSelectionListener {
public class Pp3LoggingTreeLoggerParametersDialog extends TreeLoggerParametersDialog implements ActionListener {

	static {
		Translator.addBundle("capsis.extension.treelogger.pp3treelogger.Pp3Logging");
		try {
			Method callHelp = Helper.class.getMethod("helpFor", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"capsis/extension/treelogger/pp3treelogger/Pp3LoggingTreeLoggerParametersDialog";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(Pp3LoggingTreeLoggerParametersDialog.class, helper);
		} catch (Exception e) {}
	}
	
	private JTextField fldTopGirth;
	private JTextField fldStumpHeight;
	private JTextField fldTop1Girth;
	private JTextField fldLog1Length;
	private JTextField fldTop2Girth;
	private JTextField fldLog2Length;
	private JTextField fldLog3Length;




	/*private Map < Integer, FgProductPanel > productPanels;
	private static Vector < FgProduct > selectedProducts;

	private JPanel panProduct;
	// private JList unselectedList;
	private JList selectedList;
	private JCheckBox recordResults;
	private JCheckBox exportResults;
	private JTextField discInterval_m;
	private JTextField precisionLength_m;

	private JButton upButton;
	private JButton downButton;*/

	//private JButton reset;		cm tl 31 01 2007 en attendant de le g�rer
	private JButton helpButton;

	/**	Default constructor.
	*/
	public Pp3LoggingTreeLoggerParametersDialog(Window parent, Pp3LoggingTreeLoggerParameters params) {
		super(parent, params);
	}

	@Override
	protected void instantiateVariables(TreeLoggerParameters params) {
		super.instantiateVariables(params);
		helpButton = UIControlManager.createCommonButton(CommonControlID.Help);
		fldTopGirth = new JTextField (5);
		fldStumpHeight = new JTextField (5);
		fldStumpHeight.setEnabled(false);
		fldLog1Length = new JTextField (5);
		fldTop1Girth = new JTextField (5);
		fldLog2Length = new JTextField (5);
		fldTop2Girth = new JTextField (5);
		fldLog3Length = new JTextField (5);
	}	

//	private void initialise (Pp3LoggingTreeLoggerParameters starter) {
//		this.params = starter;
//		createUI ();
//
//		// location is set by GDialog
//		pack ();	// validate (); does not work for this JDialog...
//		setVisible (true);
//	}

	/*private boolean areProductPanelsValid () {
		for (Iterator p = productPanels.keySet ().iterator (); p.hasNext ();) {
			FgProductPanel pan = productPanels.get (p.next ());
			if ( !pan.isValid () ) {
				System.out.println("Pp3LoggingDialog : IsValid == false for " + pan.getProductName ());
				return false;
			}
		}
		return true;
	}*/

	protected boolean checkConfig() {
		// Check request coherence
		//
		if (Check.isEmpty (fldTopGirth.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.topGirthIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		} else if (!Check.isDouble (fldTopGirth.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.topGirthIsNotFloat"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (Check.doubleValue (fldTopGirth.getText ()) < 0d) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.topGirthMustBeGreaterOrEqualTo0"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (Check.isEmpty (fldStumpHeight.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.stumpHeightIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		} else if (!Check.isDouble (fldStumpHeight.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.stumpHeightIsNotFloat"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (Check.doubleValue (fldStumpHeight.getText ()) < 0.30) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.stumpHeightMustBeGreaterOrEqualTo0.30"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (Check.isEmpty (fldLog1Length.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log1LengthIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		} else if (!Check.isDouble (fldLog1Length.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log1LengthIsNotFloat"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (Check.doubleValue (fldLog1Length.getText ()) < 0.10) {
			JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.logLengthMustBeGreaterOrEqualTo0.10"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (!Check.isEmpty (fldLog2Length.getText ())) {
			if (!Check.isDouble (fldLog2Length.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log2LengthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldLog2Length.getText ()) < 0.10) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.logLengthMustBeGreaterOrEqualTo0.10"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.isEmpty (fldTop1Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top1GirthIsEmpty"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			} else if (!Check.isDouble (fldTop1Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top1GirthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop1Girth.getText ()) <= Check.doubleValue (fldTopGirth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top1GirthIsTooSmall"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		} else if (!Check.isEmpty (fldTop1Girth.getText ())) {
			if (!Check.isDouble (fldTop1Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top1GirthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop1Girth.getText ()) <= Check.doubleValue (fldTopGirth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top1GirthIsTooSmall"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}

		if (!Check.isEmpty (fldLog3Length.getText ())) {
			if (!Check.isDouble (fldLog3Length.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log3LengthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldLog3Length.getText ()) < 0.10) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.logLengthMustBeGreaterOrEqualTo0.10"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.isEmpty (fldTop2Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsEmpty"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			} else if (!Check.isDouble (fldTop2Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop2Girth.getText ()) >= Check.doubleValue (fldTop1Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsTooBig"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop2Girth.getText ()) <= Check.doubleValue (fldTopGirth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsTooSmall"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.isEmpty (fldLog2Length.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log2LengthIsEmpty"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			} else if (!Check.isDouble (fldLog2Length.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.log2LengthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldLog2Length.getText ()) < 0.10) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.logLengthMustBeGreaterOrEqualTo0.10"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		} else if (!Check.isEmpty (fldTop2Girth.getText ())) {
			if (!Check.isDouble (fldTop2Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsNotFloat"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop2Girth.getText ()) <= Check.doubleValue (fldTopGirth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsTooSmall"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			if (Check.doubleValue (fldTop2Girth.getText ()) >= Check.doubleValue (fldTop1Girth.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("Pp3Logging.top2GirthIsTooBig"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}
		return true;

	}
	
	@Override
	protected Pp3LoggingTreeLoggerParameters getTreeLoggerParameters() {
		return (Pp3LoggingTreeLoggerParameters) this.params;
	}
	
	protected void updateConfig() {
		// 2. retrieve the collected data
		getTreeLoggerParameters().topGirth = Check.doubleValue (fldTopGirth.getText ());
		//stumpHeight = Check.doubleValue (fldStumpHeight.getText ());
		getTreeLoggerParameters().log1Length = Check.doubleValue (fldLog1Length.getText ());
		if (!Check.isEmpty (fldLog2Length.getText ())) getTreeLoggerParameters().log2Length = Check.doubleValue (fldLog2Length.getText ());
		else getTreeLoggerParameters().log2Length = getTreeLoggerParameters().log1Length;
		if (!Check.isEmpty (fldLog3Length.getText ())) getTreeLoggerParameters().log3Length = Check.doubleValue (fldLog3Length.getText ());
		else getTreeLoggerParameters().log3Length = getTreeLoggerParameters().log2Length;
		getTreeLoggerParameters().top3Girth = getTreeLoggerParameters().topGirth;
		if (!Check.isEmpty (fldTop2Girth.getText ())) getTreeLoggerParameters().top2Girth = Check.doubleValue (fldTop2Girth.getText ());
		else getTreeLoggerParameters().top2Girth = getTreeLoggerParameters().topGirth;
		if (!Check.isEmpty (fldTop1Girth.getText ())) getTreeLoggerParameters().top1Girth = Check.doubleValue (fldTop1Girth.getText ());
		else getTreeLoggerParameters().top1Girth = getTreeLoggerParameters().topGirth;

		/*if (!areProductPanelsValid ())
			return;

		try {
			double discInterval_m_ = DiaUtil.checkedDoubleValue (
					discInterval_m.getText (),
					"Pp3LoggingDialog.discInterval_m", true, true, false);
			double precisionLength_m_ = DiaUtil.checkedDoubleValue (
					precisionLength_m.getText (),
					"Pp3LoggingDialog.precisionLength_m", true, true, true);

			// Here, all checks are ok: update the starter and set valid
			starter.recordResults = recordResults.isSelected ();
			starter.exportResults = exportResults.isSelected ();
			starter.discInterval_m = discInterval_m_;
			starter.setPrecisionLength_m (precisionLength_m_);
			starter.setSelectedProducts (selectedProducts);
			starter.pleaseOpenDialog = false;
			setValidDialog (true);

		} catch (DiaUtil.CheckException e) {
			System.out.println("Pp3LoggingDialog : okAction () Exception" + e);
		}
		*/

//		setValidDialog (true);
	}

	@Override
	public void reset() {
		/*starter.setDefaultValues (true);
		recordResults.setSelected (starter.recordResults);
		exportResults.setSelected (starter.exportResults);
		discInterval_m.setText ("" + starter.discInterval_m);
		precisionLength_m.setText ("" + starter.getPrecisionLength_m ());
		for (Iterator p = productPanels.keySet ().iterator (); p.hasNext ();) {
			productPanels.get (p.next ()).reset ();
		}
		// create a local copy of starter.selectedProducts :
		selectedProducts = new Vector  < FgProduct > (
				starter.getSelectedProducts ());
		selectedList.setListData (selectedProducts);
		selectedList.setSelectedIndex (0);*/
	}

	//	ActionListener:
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(helpButton)) {
			helpAction();
		} else {
			super.actionPerformed (evt);		// ok and cancel buttons are defined and managed in the super class MF2013-01-17
		}

		/*
			System.out.print ("Produits :");
			for (int p = 0; p<selectedProducts.size (); p++) {
				FgProduct prod = selectedProducts.get (p);
				System.out.print (" " + p + "=" + prod.getName ());
			}
			System.out.println ();
		*/
//		if (evt.getSource ().equals (ok)) {
//			okAction ();
//		} else if (evt.getSource ().equals (cancel)) {
//			cancelAction();
//		/*} else if (evt.getSource ().equals (reset)) {		cm tl 31 01 2007 en attendant de le g�rer
//			resetAction ();*/
//		} else if (evt.getSource ().equals (help)) {
//			Helper.helpFor (this);
//		}
		/*else if (evt.getSource ().equals (upButton)) {
			moveSelectedProduct (true);
		} else if (evt.getSource ().equals (downButton)) {
			moveSelectedProduct (false);
		}*/
	}

	//	ListSelectionListener:
//	public void valueChanged (ListSelectionEvent evt) {
//		/*if (evt.getValueIsAdjusting ()) {return;}
//		if (evt.getSource ().equals (selectedList)) {
//			// Selection in selectedList
//			int ind = selectedList.getSelectedIndex ();
//			if (ind >= 0) {
//				FgProduct selectedProduct = selectedProducts.get (ind);
//				int id = selectedProduct.getId ();
//				changeProductPanel (id);
//				//selectedList.setToolTipText (selectedProduct.getName ());
//			}
//		}*/
//	}

	/*public static  JPanel bordurePanel (JComponent center, int xSpace, int ySpace) {
		JPanel bordure = new JPanel (new BorderLayout ());
		bordure.add (center, BorderLayout.CENTER);
		if (xSpace > 0) {
			bordure.add (Box.createHorizontalStrut (xSpace), BorderLayout.EAST);
			bordure.add (Box.createHorizontalStrut (xSpace), BorderLayout.WEST);
		}
		if (ySpace > 0) {
			bordure.add (Box.createVerticalStrut (ySpace), BorderLayout.NORTH);
			bordure.add (Box.createVerticalStrut (ySpace), BorderLayout.SOUTH);
		}
		return bordure;
	}*/

	
	@Override
	public void refreshInterface() {
		if (getTreeLoggerParameters().topGirth > 0) {
			fldTopGirth.setText (""+getTreeLoggerParameters().topGirth);
		}
		if (getTreeLoggerParameters().STUMP_HEIGHT > 0) {
			fldStumpHeight.setText (""+getTreeLoggerParameters().STUMP_HEIGHT);
		}
		if (getTreeLoggerParameters().top1Girth > 0) {
			fldTop1Girth.setText (""+getTreeLoggerParameters().top1Girth);
		}
		if (getTreeLoggerParameters().log1Length > 0) {
			fldLog1Length.setText (""+getTreeLoggerParameters().log1Length);
		}
		if (getTreeLoggerParameters().top2Girth > 0) {
			fldTop2Girth.setText (""+getTreeLoggerParameters().top2Girth);
		}
		if (getTreeLoggerParameters().log2Length > 0) {
			fldLog2Length.setText (""+getTreeLoggerParameters().log2Length);
		}
		if (getTreeLoggerParameters().log3Length > 0) {
			fldLog3Length.setText (""+getTreeLoggerParameters().log3Length);
		}
	}
	
	
	@Override
	protected void initUI() {

		Border etched = BorderFactory.createEtchedBorder ();

		Box box1 = Box.createVerticalBox ();
		Border bor1 = BorderFactory.createTitledBorder (etched, Translator.swap ("Pp3Logging.comment1"));
		box1.setBorder (bor1);
//		JPanel comment1Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
//		comment1Line.add (new JLabel (Translator.swap ("Pp3Logging.comment1")));

		JPanel topGirthLine = new JPanel (new FlowLayout (FlowLayout.LEFT));
		topGirthLine.add (new JWidthLabel (Translator.swap ("Pp3Logging.topGirth")+" :", 300));
		topGirthLine.add (fldTopGirth);

		JPanel stumpHeightLine = new JPanel (new FlowLayout (FlowLayout.LEFT));
		stumpHeightLine.add (new JWidthLabel (Translator.swap ("Pp3Logging.stumpHeight")+" :", 300));
		stumpHeightLine.add (fldStumpHeight);

		JPanel legendLine = new JPanel (new FlowLayout (FlowLayout.LEFT));
		legendLine.add (new JWidthLabel (Translator.swap ("Pp3Logging.log"), 100));
		legendLine.add (new JWidthLabel (Translator.swap ("Pp3Logging.logLength"), 120));
		legendLine.add (new JWidthLabel (Translator.swap ("Pp3Logging.logTopGirth"), 120));

		JPanel log1Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
		log1Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.firstLog"), 100));
		log1Line.add (fldLog1Length);
		log1Line.add (new JWidthLabel ("", 60));
		log1Line.add (fldTop1Girth);
		log1Line.add (new JWidthLabel ("", 10));
		log1Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.firstLogComment"), 100));

		JPanel log2Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
		log2Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.secondLog"), 100));
		log2Line.add (fldLog2Length);
		log2Line.add (new JWidthLabel ("", 60));
		log2Line.add (fldTop2Girth);
		log2Line.add (new JWidthLabel ("", 10));
		log2Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.firstLogComment"), 100));

		JPanel log3Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
		log3Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.otherLog"), 100));
		log3Line.add (fldLog3Length);
		log3Line.add (new JWidthLabel ("", 60));
		log3Line.add (new JWidthLabel (Translator.swap ("Pp3Logging.otherLogComment"), 100));

		JPanel comment2Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
		comment2Line.add (new JLabel (Translator.swap ("Pp3Logging.comment2")));
		JPanel comment3Line = new JPanel (new FlowLayout (FlowLayout.LEFT));
		comment3Line.add (new JLabel (Translator.swap ("Pp3Logging.comment3")));

//		box1.add (comment1Line);
		box1.add (topGirthLine);
		box1.add (stumpHeightLine);
		box1.add (legendLine);
		box1.add (log1Line);
		box1.add (log2Line);
		box1.add (log3Line);
		box1.add (comment2Line);
		box1.add (comment3Line);

		//	Reset ok cancel help buttons
		JPanel panControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
//		ok = new JButton (Translator.swap ("Shared.ok"));
//		cancel = new JButton (Translator.swap ("Shared.cancel"));
//		//reset = new JButton (Translator.swap ("Shared.reset"));  cm tl 31 01 2007 en attendant de le g�rer
//		help = new JButton (Translator.swap ("Shared.help"));
		panControl.add (ok);
		panControl.add (cancel);
		//panControl.add (reset); cm tl 31 01 2007 en attendant de le g�rer
		panControl.add (helpButton);
//		ok.addActionListener (this);
//		cancel.addActionListener (this);
//		//reset.addActionListener (this);  cm tl 31 01 2007 en attendant de le g�rer
//		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		//getContentPane ().add (panLists, BorderLayout.WEST);
		//getContentPane ().add (panProduct, BorderLayout.CENTER);
		getContentPane ().add (box1, BorderLayout.CENTER);
		getContentPane ().add (panControl, BorderLayout.SOUTH);

		// sets ok as default (see GDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

		setTitle (Translator.swap ("Pp3Logging"));
		
		pack();
		setMinimumSize(getSize());
		//
	}

	@Override
	public void listenTo() {
		cancel.addActionListener(this);
		ok.addActionListener(this);
		helpButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		cancel.removeActionListener(this);
		ok.removeActionListener(this);
		helpButton.removeActionListener(this);
	}


	@Override
	public void okAction() {
		if (checkConfig()) {
			setVisible(false);
			params.setParameterDialogCanceled(false);
			updateConfig();
		}
	}

	
	/*
	 * Useless for this class(non-Javadoc)
	 * @see repicea.simulation.treelogger.TreeLoggerParametersDialog#settingsAction()
	 */
	@Override
	protected void settingsAction () {}

	/*private void moveSelectedProduct (boolean up) {
		if (selectedList.isSelectionEmpty ()) {return;}
		int cur = selectedList.getSelectedIndex ();
		if ( (up && cur <= 0) || (!up && cur >= selectedProducts.size ()-1) ) {
			return ;
		}
		int newCur = up ? cur-1 : cur+1;

		swapSelectedProducts (cur, newCur);
		//selectedList.setListData (selectedProducts);
		selectedList.setSelectedIndex (newCur);
	}

	//	Swap 2 selected products within the local user list
	public void swapSelectedProducts (int order1, int order2) {
		FgProduct p1 = selectedProducts.get (order1);
		FgProduct p2 = selectedProducts.get (order2);
		if ( p1 != null && p2 != null ) {
			selectedProducts.set (order1, p2);
			selectedProducts.set (order2, p1);
		}
	}

	public void changeProductPanel (int idProduct) {
		FgProductPanel panel = productPanels.get (idProduct);

		panProduct.removeAll ();
		panProduct.add (panel, BorderLayout.CENTER);
		panProduct.revalidate ();
		panProduct.repaint ();
	}*/

	// For testing the dialog out of capsis:
//	public static void main (String[] args) {
//		new Pp3LoggingDialog (new JFrame(), );
//	}

}
