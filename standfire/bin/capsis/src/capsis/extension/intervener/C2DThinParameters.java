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

package capsis.extension.intervener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.util.SmartFlowLayout;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.HdomProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.RDIProvider;
import capsis.util.methodprovider.RdiProviderEnhanced.RdiTool;
import capsis.util.methodprovider.SHBProvider;
import capsis.util.methodprovider.TreeVProvider;

/**
 * C2DThinParameters - Dialog box to input the limit parameters for C2Thinner
 *
 * @author Ph. Dreyfus - March 2001 - October 2004
 */
@SuppressWarnings("serial")
public class C2DThinParameters extends AmapDialog implements ActionListener {

	private JTextField fldTargetKg;
	private double targetKg;
	private JTextField fldTargetStocking;
	private double targetStocking;
	private JTextField fldMiniKg;
	private int Sm;
	private JTextField fldNhaBefore;
	private JTextField fldGhaBefore;
	private JTextField fldVhaBefore;
	private JTextField fldRDIBefore;
	private JTextField fldSHBBefore;
	private JTextField fldNhaAfter;
	private JTextField fldGhaAfter;
	private JTextField fldVhaAfter;
	private JTextField fldRDIAfter;
	private JTextField fldSHBAfter;

	private JButton ok;
	private JButton cancel;
	private JButton help;

	private ButtonGroup rdGroup1;
	private JRadioButton rdN;
	private JRadioButton rdG;
	private JRadioButton rdV;
	private JRadioButton rdR;
	private JRadioButton rdS;

	private GScene stand;
	private GModel model;
	@SuppressWarnings("rawtypes")
	private Collection tc;

	private C2Thinner c2Thinner;
	
	protected MethodProvider mp;

	private NumberFormat formater1d;	// to control number of decimals
	private NumberFormat formater2d;	// to control number of decimals

// ------------------------------------------------------------------------------------------------------------------------------------->
//               Fonction de lancement du dialogue																		>
// ------------------------------------------------------------------------------------------------------------------------------------->
	@SuppressWarnings("rawtypes")
	public C2DThinParameters (GScene s, GModel m, C2Thinner c2Thinner) {
		super ();

		formater1d = NumberFormat.getInstance (Locale.US);
		formater1d.setMaximumFractionDigits (1);
		formater1d.setGroupingUsed (false);

		formater2d = NumberFormat.getInstance (Locale.US);
		formater2d.setMaximumFractionDigits (2);
		formater2d.setGroupingUsed (false);

		this.c2Thinner = c2Thinner;

		stand = s;
		model = m;

		Collection tc = null;	// fc - 24.3.2004
		try {
			tc = ((TreeCollection) stand).getTrees ();
		} catch (Exception e) {
			
		}	// fc - 24.3.2004
		
		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	private void okAction () {
		// Retrieve method provider
		mp = model.getMethodProvider ();
		
		if (Check.isEmpty (fldTargetStocking.getText ()) || Check.isEmpty (fldTargetKg.getText ()) ) {
			JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.fldTargetStockingAndfldKgMustNotBeEmpty"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
		} else {
			targetKg = Double.parseDouble (fldTargetKg.getText ());
			targetStocking = Double.parseDouble (fldTargetStocking.getText ());
			
			
			double rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, model, 0, 0);
			
			if (targetKg < 0 || targetKg > 1) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.KgShouldBeBetween0And1"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

			} else if (Sm == 0 && (int)targetStocking >= c2Thinner.getNhaBefore()) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			} else if (Sm == 1 && targetStocking >= c2Thinner.getGhaBefore()) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			} else if (Sm == 2 && targetStocking >= c2Thinner.getVhaBefore()) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
//			} else if (Sm == 3 && targetStocking >= ((RDIProvider) mp).getRDI(model, 0, 0, null)) {
			} else if (Sm == 3 && targetStocking >= rdi) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			} else if (Sm == 4 && targetStocking <= c2Thinner.getSHBBefore()) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			} else if (Sm == 4 && Math.pow((10746.0/(((HdomProvider) mp).getHdom(stand,tc) * targetStocking)),2.0) < 5.0) {
						// This test does not work sometimes (when computed Kg is set to 1 ???)
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetSHBtooHigh"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

			} else {
				setValidDialog (true);
			}
		}
	}

	public void actionPerformed (ActionEvent evt) {
		if(rdN.isSelected()) Sm = 0;
		if(rdG.isSelected()) Sm = 1;
		if(rdV.isSelected()) Sm = 2;
		if(rdR.isSelected()) Sm = 3;
		if(rdS.isSelected()) Sm = 4;

		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (rdN) || evt.getSource ().equals (rdG) || evt.getSource ().equals (rdV) || evt.getSource ().equals (rdR) || evt.getSource ().equals (rdS)) {
				fldNhaAfter.setText ("");
				fldGhaAfter.setText ("");							
				fldVhaAfter.setText  ("");							
				fldRDIAfter.setText  ("");							
				fldSHBAfter.setText  ("");							
				fldTargetStocking.setText  ("");							
				fldMiniKg.setText  ("");							
				repaint();
				
		} else if (evt.getSource ().equals (fldTargetStocking) || evt.getSource ().equals (fldTargetKg)) {
			if (Check.isEmpty (fldTargetStocking.getText ()) || Check.isEmpty (fldTargetKg.getText ()) ) {
				JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.fldTargetStockingAndfldKgMustNotBeEmpty"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			} else {
				targetKg = Double.parseDouble (fldTargetKg.getText ());
				targetStocking = Double.parseDouble (fldTargetStocking.getText ());
				c2Thinner.setTargetKg(Math.min(0.99, targetKg));
				c2Thinner.setTargetStocking(targetStocking);
				c2Thinner.setSm(Sm);
				
				double rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, model, 0, 0);
				
				if (Sm == 0 && (int)targetStocking >= c2Thinner.getNhaBefore()) {
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				} else if (Sm == 1 && targetStocking >= c2Thinner.getGhaBefore()) {
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				} else if (Sm == 2 && targetStocking >= c2Thinner.getVhaBefore()) {
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
//				} else if (Sm == 3 && targetStocking >= ((RDIProvider) mp).getRDI(model, 0, 0, null)) {
				} else if (Sm == 3 && targetStocking >= rdi) {
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				} else if (Sm == 4 && targetStocking <= c2Thinner.getSHBBefore()) {
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetStockingShouldNotExceedCurrentStocking"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				} else if (Sm == 4 && Math.pow((10746.0/(((HdomProvider) mp).getHdom(stand,tc) * targetStocking)),2.0) < 5.0) {
					// This test does not work sometimes (when computed Kg is set to 1 ???)
					JOptionPane.showMessageDialog (this, Translator.swap ("C2Thinner.TargetSHBtooHigh"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

				} else {
					c2Thinner.computeThinning ();
					fldNhaAfter.setText (String.valueOf (c2Thinner.getNhaAfter ()));
					fldGhaAfter.setText (String.valueOf (formater1d.format(c2Thinner.getGhaAfter ())));
					fldVhaAfter.setText (String.valueOf (formater1d.format(c2Thinner.getVhaAfter ())));
					fldSHBAfter.setText (String.valueOf (formater1d.format(c2Thinner.getSHBAfter ())));
					fldTargetKg.setText (String.valueOf (formater2d.format(c2Thinner.getKg ())));
					fldMiniKg.setText (String.valueOf (formater2d.format(c2Thinner.getMiniKg ())));
					repaint();
				}
			}
		
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			System.err.println ("help");
		}
	}

	/**
	 * Initializes the GUI.
	 */
	private void createUI () {

		Box part1 = Box.createVerticalBox ();
		Border etched = BorderFactory.createEtchedBorder ();

		JPanel StockingVariable = new JPanel ();
			StockingVariable.setLayout (new FlowLayout (FlowLayout.LEFT));
			Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap ("C2Thinner.chooseStockingVariable"));
			StockingVariable.setBorder (b1);

			JPanel l1 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
			JPanel l2 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
			JPanel l3 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
			JPanel l4 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
			JPanel l5 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));

			rdN = new JRadioButton (Translator.swap ("C2Thinner.rdN"));
			rdG = new JRadioButton (Translator.swap ("C2Thinner.rdG"));
			rdV = new JRadioButton (Translator.swap ("C2Thinner.rdV"));
			rdR = new JRadioButton (Translator.swap ("C2Thinner.rdR"));
			rdS = new JRadioButton (Translator.swap ("C2Thinner.rdS"));
			rdN.addActionListener(this);
			rdG.addActionListener(this);
			rdV.addActionListener(this);
			rdR.addActionListener(this);
			rdS.addActionListener(this);

			rdGroup1 = new ButtonGroup ();
			rdGroup1.add (rdN);
			rdGroup1.add (rdG);
			rdGroup1.add (rdV);
			rdGroup1.add (rdR);
			rdGroup1.add (rdS);

			rdGroup1.setSelected (rdN.getModel (), true);

			l1.add (rdN);
			l2.add (rdG);
			l3.add (rdV);
			l4.add (rdR);
			l4.add (rdS);

			StockingVariable.add (l1);
			StockingVariable.add (l2);
			StockingVariable.add (l3);
			StockingVariable.add (l4);
			StockingVariable.add (l5);

			mp = model.getMethodProvider ();
			if (!(mp instanceof NProvider)) { rdN.setEnabled (false); }
			if (!(mp instanceof GProvider)) { rdG.setEnabled (false); }
			if (!(mp instanceof TreeVProvider)) { rdV.setEnabled (false); }
			if (!(mp instanceof RDIProvider)) { rdR.setEnabled (false); }
			if (!(mp instanceof SHBProvider)) { rdS.setEnabled (false); }

		JPanel beforeValues = new JPanel ();
			beforeValues.setLayout (new FlowLayout (FlowLayout.CENTER));
			Border b2 = BorderFactory.createTitledBorder (etched, Translator.swap ("C2Thinner.beforeValues"));
			beforeValues.setBorder (b2);

			beforeValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Nha"), 30));
			fldNhaBefore = new JTextField (6);
			beforeValues.add (fldNhaBefore);
			fldNhaBefore.setText (String.valueOf (c2Thinner.getNhaBefore()));
			fldNhaBefore.setEditable(false);

			beforeValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Gha"), 30));
			fldGhaBefore = new JTextField (4);
			beforeValues.add (fldGhaBefore);
			fldGhaBefore.setText (String.valueOf (formater1d.format(c2Thinner.getGhaBefore())));
			fldGhaBefore.setEditable(false);

			beforeValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Vha"), 30));
			fldVhaBefore = new JTextField (4);
			beforeValues.add (fldVhaBefore);
			fldVhaBefore.setText (String.valueOf (formater1d.format(c2Thinner.getVhaBefore())));
			fldVhaBefore.setEditable(false);

			beforeValues.add(new JWidthLabel (Translator.swap ("C2Thinner.RDI"), 30));
			fldRDIBefore = new JTextField (4);
			beforeValues.add (fldRDIBefore);
			fldRDIBefore.setText ("-");							// provisional
			fldRDIBefore.setEditable(false);

			beforeValues.add(new JWidthLabel (Translator.swap ("C2Thinner.SHB"), 30));
			fldSHBBefore = new JTextField (4);
			beforeValues.add (fldSHBBefore);
			fldSHBBefore.setText (String.valueOf (formater1d.format(c2Thinner.getSHBBefore())));
			fldSHBBefore.setEditable(false);

		JPanel afterValues = new JPanel ();
			afterValues.setLayout (new FlowLayout (FlowLayout.CENTER));
			Border b3 = BorderFactory.createTitledBorder (etched, Translator.swap ("C2Thinner.afterValues"));
			afterValues.setBorder (b3);

			afterValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Nha"), 30));
			fldNhaAfter = new JTextField (6);
			afterValues.add (fldNhaAfter);
			fldNhaAfter.setText ("");
			fldNhaAfter.setEditable(false);

			afterValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Gha"), 30));
			fldGhaAfter = new JTextField (4);
			afterValues.add (fldGhaAfter);
			fldGhaAfter.setText ("");
			fldGhaAfter.setEditable(false);

			afterValues.add(new JWidthLabel (Translator.swap ("C2Thinner.Vha"), 30));
			fldVhaAfter = new JTextField (4);
			afterValues.add (fldVhaAfter);
			fldVhaAfter.setText ("");
			fldVhaAfter.setEditable(false);

			afterValues.add(new JWidthLabel (Translator.swap ("C2Thinner.RDI"), 30));
			fldRDIAfter = new JTextField (4);
			afterValues.add (fldRDIAfter);
			fldRDIAfter.setText ("");
			fldRDIAfter.setEditable(false);

			afterValues.add(new JWidthLabel (Translator.swap ("C2Thinner.SHB"), 30));
			fldSHBAfter = new JTextField (4);
			afterValues.add (fldSHBAfter);
			fldSHBAfter.setText ("");
			fldSHBAfter.setEditable(false);


		JPanel Kg_TargetStocking = new JPanel ();
			Kg_TargetStocking.setLayout (new FlowLayout (FlowLayout.CENTER));
			Border b4 = BorderFactory.createTitledBorder (etched, Translator.swap ("C2Thinner.chooseValues_for_Stocking_and_Kg"));
			Kg_TargetStocking.setBorder (b4);

			Kg_TargetStocking.add(new JWidthLabel (Translator.swap ("C2Thinner.targetStocking"), 50));
			fldTargetStocking = new JTextField (8);
			Kg_TargetStocking.add (fldTargetStocking);
			fldTargetStocking.setText ("");							// TBC ??
			fldTargetStocking.addActionListener (this);

			Kg_TargetStocking.add(new JWidthLabel (Translator.swap ("C2Thinner.targetKg"), 15));
			fldTargetKg = new JTextField (5);
			Kg_TargetStocking.add (fldTargetKg);
			//fldTargetKg.setText ("0.8");
			fldTargetKg.setText ("");	          // TBC ???
			fldTargetKg.addActionListener (this);

			Kg_TargetStocking.add(new JWidthLabel (Translator.swap ("C2Thinner.miniKg"), 15));
			fldMiniKg = new JTextField (5);
			Kg_TargetStocking.add (fldMiniKg);
			fldMiniKg.setText ("");
			fldMiniKg.setEditable(false);
			
		
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
			pControl.add(new JWidthLabel (Translator.swap ("C2Thinner.howToDo"), 250));
			ok = new JButton (Translator.swap ("Shared.ok"));
			cancel = new JButton (Translator.swap ("Shared.cancel"));
			help = new JButton (Translator.swap ("Shared.help"));
			pControl.add (ok);
			pControl.add (cancel);
			pControl.add (help);
			ok.addActionListener (this);
			cancel.addActionListener (this);
			help.addActionListener (this);

		setDefaultButton (ok);	// from AmapDialog

		part1.add (StockingVariable);
		part1.add (beforeValues);
		part1.add (afterValues);
		part1.add (Kg_TargetStocking);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, "Center");
		getContentPane ().add (pControl, "South");

		setTitle (Translator.swap ("C2Thinner.thinningParameters"));
		
		setModal (true);
	}

	// Call in C2Thinner.java
	public double getTargetKg () { return targetKg;	}
	public double getTargetStocking () { return targetStocking;	}
	public int getSm () { return Sm;	}
}

