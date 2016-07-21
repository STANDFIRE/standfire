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

package capsis.extension.treelogger.geolog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.REpiceaTranslator;
import capsis.commongui.util.Helper;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.util.DiaUtil;

/**	
 * GeoLogDialog : dialog box for GeoLog
 * @author F. Mothe - January 2006
 * @author M. Fortin - January 2013 (refactoring)
 */
public class GeoLogTreeLoggerParametersDialog extends TreeLoggerParametersDialog<GeoLogLogCategory> implements ActionListener {
	
	static {
		UIControlManager.setTitle (GeoLogTreeLoggerParametersDialog.class, "GeoLog Tree Logger", "Module de billonnage GeoLog");
		Translator.addBundle("capsis.extension.treelogger.geolog.GeoLog");

		try {
			Method callHelp = Helper.class.getMethod("helpFor", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"/capsis/extension/treelogger/geolog/GeoLogTreeLoggerParametersDialog";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(GeoLogTreeLoggerParametersDialog.class, helper);
		} catch (Exception e) {}
	}

	private JCheckBox recordResults;
	private JCheckBox exportResults;
	private JTextField discInterval_m;
	private JTextField precisionLength_m;
	private JTextField randomSeed;


	/**	
	 * Default constructor.
	 */
	protected GeoLogTreeLoggerParametersDialog(Window parent, GeoLogTreeLoggerParameters params) {
		super(parent, params);
		mnSpecies.setEnabled(false);
//		mnLogGrade.setEnabled(false);
		Dimension minDim = new Dimension(750, 500);
		setMinimumSize(minDim);
		setSize(minDim);
	}


	@Override
	protected void instantiateVariables(TreeLoggerParameters<GeoLogLogCategory> params) {
		super.instantiateVariables (params);
		recordResults = new JCheckBox();
		exportResults = new JCheckBox();
		discInterval_m = new JTextField ();
		precisionLength_m = new JTextField ();
		randomSeed = new JTextField ();
	}
	
	
	
	@Override
	protected GeoLogTreeLoggerParameters getTreeLoggerParameters() {
		return (GeoLogTreeLoggerParameters) params;
	}
	
	
	protected void updateConfig() {
		try {
			double discInterval_m_ = DiaUtil.checkedDoubleValue (
					discInterval_m.getText (),
					"GeoLogDialog.discInterval_m", true, true, false);
			double precisionLength_m_ = DiaUtil.checkedDoubleValue (
					precisionLength_m.getText (),
					"GeoLogDialog.precisionLength_m", true, true, true);
			long randomSeed_ = DiaUtil.checkedLongValue (
					randomSeed.getText (), // -1 means new seed for each simulation
					"GeoLogDialog.randomSeed", true, false, false);

			// Here, all checks are ok: update the starter and set valid
			getTreeLoggerParameters().setRecordResultsEnabled(recordResults.isSelected());
			getTreeLoggerParameters().setExportResultsEnabled(exportResults.isSelected());
			// values of the two check boxes are saved into properties
			Settings.setProperty("GeoLogDialog.recordResults", recordResults.isSelected());
			Settings.setProperty("GeoLogDialog.exportResults", exportResults.isSelected());
			GeoLogTreeLoggerParameters.discInterval_m = discInterval_m_;
			getTreeLoggerParameters().setPrecisionLength_m (precisionLength_m_);
			GeoLogTreeLoggerParameters.randomSeed = randomSeed_;
			//		params.setSelectedProducts (selectedLogCategories); already changed since this is a reference
			getTreeLoggerParameters().pleaseOpenDialog = false;
		} catch (DiaUtil.CheckException e) {
			System.out.println ("GeoLogDialog : okAction () Exception" + e);
		}
	}

	
	
	@Override
	public void reset() {
		super.reset();
		recordResults.setSelected(true);		// reset to true by default
		exportResults.setSelected(true);		// reset to true by default
	}


	@Override
	public void refreshInterface() {
		super.refreshInterface ();
		discInterval_m.setText("" + GeoLogTreeLoggerParameters.discInterval_m);
		precisionLength_m.setText("" + getTreeLoggerParameters().getPrecisionLength_m ());
		randomSeed.setText("" + GeoLogTreeLoggerParameters.randomSeed);
	}
	
	
	public static  JPanel bordurePanel (JComponent center, int xSpace, int ySpace) {
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
	}
	
	@Override
	protected void settingsAction () {
		// TODO Auto-generated method stub
	}

}
