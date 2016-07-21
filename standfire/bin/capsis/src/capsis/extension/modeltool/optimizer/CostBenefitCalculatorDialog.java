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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import repicea.app.SettingMemory;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.REpiceaCellEditor;
import repicea.gui.components.REpiceaTable;
import repicea.gui.components.REpiceaTableModel;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.serial.Memorizable;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.extension.modeltool.optimizer.CostBenefitCalculator.PriceFunction;
import capsis.extension.modeltool.optimizer.CostRecord.CostType;
import capsis.extension.modeltool.optimizer.CostRecord.CostUnit;

class CostBenefitCalculatorDialog extends REpiceaDialog implements IOUserInterface, NumberFieldListener, OwnedWindow, ActionListener, TableModelListener, ItemListener {

	static {
		UIControlManager.setTitle(CostBenefitCalculatorDialog.class, "Economic parameters", "Param\u00E8tres \u00E9conomiques");
	}
	protected static enum MessageID implements TextableEnum {
		EconomicParameters("Economic parameters", "Param\u00E8tres \u00E9conomiques"),
		DiscountRateLabel("Discount rate", "Taux d'actualisation"),
		CostTitleLabel("Maintenance and harvest costs", "Co\u00FBts d'entretien et de r\u00E9colte"),
		MainLabel("Label", "Libell\u00E9"),
		TypeLabel("Type", "Type"), 
		UnitLabel("Unit", "Unit\u00E9"),
		AgeLabel("Age", "Age"),
		CostLabel("Cost", "Co\u00FBt"),
		Activated("Enabled", "Activ\u00E9"),
		CostBenefitCalculatorFileFilter("Cost benefit file (*.cbc)", "Fichier de co\u00FBts b\u00E9n\u00E9fices (*.cbc)"),
		AnnualFixedCostLabel("Annual fixed costs (\u20aC/ha/yr)", "Co\u00FBts fixes annuels (\u20aC/ha/an)"),
		PriceFunctionLabel("Price functions", "Fonction de prix"),
		PriceFunctionY("\u20aC/m\u00B3","\u20aC/m\u00B3"),
		EqualSign("=", "="),
		PlusSign("+", "+"),
		DiameterVariable("D", "D"),
		DiameterVariable2("D\u00B2", "D\u00B2"),
		VolumeVariable("V", "V"),
		VolumeVariable2("V\u00B2", "V\u00B2"),
		;
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	private final CostBenefitCalculator caller;
	private final JFormattedNumericField discountRateField;
//	private final JFormattedNumericField annualFixedCostsField;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final JMenuItem load;
	private final JMenuItem close;
	private final JMenuItem reset;
	private final REpiceaTableModel costsTableModel;
	private final JRadioButton chavetPriceFunctionButton;
	private final JRadioButton hehsmatolPriceFunctionButton;
	private final JRadioButton customizedDiameterBasedPriceFunctionButton;
	private final JRadioButton customizedVolumeBasedPriceFunctionButton;
	private final JFormattedNumericField a0Parameter;
	private final JFormattedNumericField a1Parameter;
	private final JFormattedNumericField a2Parameter;
	private final JFormattedNumericField b0Parameter;
	private final JFormattedNumericField b1Parameter;
	private final JFormattedNumericField b2Parameter;
	
	
	protected CostBenefitCalculatorDialog(CostBenefitCalculator costBenefitCalculator, Window parent) {
		super(parent);
		this.caller = costBenefitCalculator;

		cancelOnClose = false;

		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		close = UIControlManager.createCommonMenuItem(CommonControlID.Close);

		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);

		reset = UIControlManager.createCommonMenuItem(CommonControlID.Reset);

		ButtonGroup bg = new ButtonGroup();
		chavetPriceFunctionButton = new JRadioButton(PriceFunction.Chavet.toString());
		bg.add(chavetPriceFunctionButton);
		hehsmatolPriceFunctionButton = new JRadioButton(PriceFunction.Heshmatol.toString());
		bg.add(hehsmatolPriceFunctionButton);
		customizedDiameterBasedPriceFunctionButton = new JRadioButton(PriceFunction.CustomizedDiameter.toString());
		bg.add(customizedDiameterBasedPriceFunctionButton);
		customizedVolumeBasedPriceFunctionButton = new JRadioButton(PriceFunction.CustomizedVolume.toString());
		bg.add(customizedVolumeBasedPriceFunctionButton);
		
		discountRateField = NumberFormatFieldFactory.createNumberFormatField(5, 
				NumberFormatFieldFactory.Type.Double, 
				NumberFormatFieldFactory.Range.Positive,
				false);

//		annualFixedCostsField = NumberFormatFieldFactory.createNumberFormatField(5, 
//				NumberFormatFieldFactory.Type.Double, 
//				NumberFormatFieldFactory.Range.Positive,
//				false);

		Object[] columnNames = new Object[6];
		columnNames[0] = MessageID.MainLabel.toString();
		columnNames[1] = MessageID.TypeLabel.toString();
		columnNames[2] = MessageID.UnitLabel.toString();
		columnNames[3] = MessageID.AgeLabel.toString();
		columnNames[4] = MessageID.CostLabel.toString();
		columnNames[5] = MessageID.Activated.toString();
		
		costsTableModel = new REpiceaTableModel(columnNames);

		a0Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);
		a1Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);
		a2Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);
		b0Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);
		b1Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);
		b2Parameter = NumberFormatFieldFactory.createNumberFormatField(10,NumberFormatFieldFactory.Type.Double,NumberFormatFieldFactory.Range.All,false);

		synchronizeUIWithOwner();
		initUI();
		pack();
	}

	@Override
	public void listenTo() {
		discountRateField.addNumberFieldListener(this);
//		annualFixedCostsField.addNumberFieldListener(this);
		close.addActionListener(this);
		costsTableModel.addTableModelListener(this);
		reset.addActionListener(this);
		a0Parameter.addNumberFieldListener(this);
		a1Parameter.addNumberFieldListener(this);
		a2Parameter.addNumberFieldListener(this);
		b0Parameter.addNumberFieldListener(this);
		b1Parameter.addNumberFieldListener(this);
		b2Parameter.addNumberFieldListener(this);
		chavetPriceFunctionButton.addItemListener(this);
		hehsmatolPriceFunctionButton.addItemListener(this);
		customizedDiameterBasedPriceFunctionButton.addItemListener(this);
		customizedVolumeBasedPriceFunctionButton.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		discountRateField.removeNumberFieldListener(this);
//		annualFixedCostsField.removeNumberFieldListener(this);
		close.removeActionListener(this);
		costsTableModel.removeTableModelListener(this);
		reset.removeActionListener(this);
		a0Parameter.removeNumberFieldListener(this);
		a1Parameter.removeNumberFieldListener(this);
		a2Parameter.removeNumberFieldListener(this);
		b0Parameter.removeNumberFieldListener(this);
		b1Parameter.removeNumberFieldListener(this);
		b2Parameter.removeNumberFieldListener(this);
		chavetPriceFunctionButton.removeItemListener(this);
		hehsmatolPriceFunctionButton.removeItemListener(this);
		customizedDiameterBasedPriceFunctionButton.removeItemListener(this);
		customizedVolumeBasedPriceFunctionButton.removeItemListener(this);
	}

	@Override
	protected void initUI() {
		REpiceaIOFileHandlerUI.RefreshTitle(caller, this);
		
		getContentPane().setLayout(new BorderLayout());
		
		
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);
		
		JMenu file = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		mb.add(file);
		file.add(load);
		file.add(save);
		file.add(saveAs);
		file.add(close);
		
		JMenu edit = UIControlManager.createCommonMenu(CommonMenuTitle.Edit);
		mb.add(edit);
		edit.add(reset);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		
		JPanel subPanel1 = new JPanel();
		subPanel1.setLayout(new BoxLayout(subPanel1, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel1);

		subPanel1.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.EconomicParameters.toString()));
		subPanel1.add(Box.createVerticalStrut(5));
		subPanel1.add(OptimizerToolSettingsDialog.createPanel(MessageID.DiscountRateLabel, discountRateField));
		subPanel1.add(Box.createVerticalStrut(5));
//		subPanel1.add(OptimizerToolSettingsDialog.createPanel(MessageID.AnnualFixedCostLabel, annualFixedCostsField));
//		subPanel1.add(Box.createVerticalStrut(5));
		
		subPanel1 = new JPanel();
		subPanel1.setLayout(new BoxLayout(subPanel1, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel1);

		
		subPanel1.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.PriceFunctionLabel.toString()));
		JPanel tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(10));
		tmpPanel.add(hehsmatolPriceFunctionButton);
		subPanel1.add(tmpPanel);
		subPanel1.add(Box.createVerticalStrut(5));
		
		tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(10));
		tmpPanel.add(chavetPriceFunctionButton);
		subPanel1.add(tmpPanel);
		subPanel1.add(Box.createVerticalStrut(5));
		
		tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(10));
		tmpPanel.add(customizedDiameterBasedPriceFunctionButton);
		subPanel1.add(tmpPanel);
		subPanel1.add(createFunctionPanel(a0Parameter, a1Parameter, a2Parameter, MessageID.DiameterVariable, MessageID.DiameterVariable2));
		subPanel1.add(Box.createVerticalStrut(10));

		tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(Box.createHorizontalStrut(10));
		tmpPanel.add(customizedVolumeBasedPriceFunctionButton);
		subPanel1.add(tmpPanel);
		subPanel1.add(createFunctionPanel(b0Parameter, b1Parameter, b2Parameter, MessageID.VolumeVariable, MessageID.VolumeVariable2));
		subPanel1.add(Box.createVerticalStrut(10));

		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new BoxLayout(subPanel2, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel2);

		subPanel2.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.CostTitleLabel.toString()));
		subPanel2.add(Box.createVerticalStrut(5));
		subPanel2.add(createTable(caller.costs));
		subPanel2.add(Box.createVerticalStrut(10));
	}
	
	private Component createFunctionPanel(JFormattedNumericField parm0, JFormattedNumericField parm1, JFormattedNumericField parm2, TextableEnum label1, TextableEnum label2) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(Box.createHorizontalStrut(30));
		panel.add(UIControlManager.getLabel(MessageID.PriceFunctionY));
		panel.add(UIControlManager.getLabel(MessageID.EqualSign));
		panel.add(parm0);
		panel.add(UIControlManager.getLabel(MessageID.PlusSign));
		panel.add(parm1);
		panel.add(UIControlManager.getLabel(label1));
		panel.add(UIControlManager.getLabel(MessageID.PlusSign));
		panel.add(parm2);
		panel.add(UIControlManager.getLabel(label2));
		panel.add(Box.createHorizontalStrut(5));
		return panel;
	}
	
	private Component createTable(List<CostRecord> recordList) {
		REpiceaTable costTable = new REpiceaTable(costsTableModel);
		costTable.setDefaultEditor(CostUnit.class, new REpiceaCellEditor(new JComboBox<CostUnit>(CostUnit.values()), costsTableModel));
		costTable.setDefaultEditor(CostType.class, new REpiceaCellEditor(new JComboBox<CostType>(CostType.values()), costsTableModel));
		costTable.setDefaultEditor(Integer.class, new REpiceaCellEditor(NumberFormatFieldFactory.createNumberFormatField(8, NumberFormatFieldFactory.Type.Integer, NumberFormatFieldFactory.Range.Positive, false), costsTableModel));
		costTable.setDefaultEditor(Double.class, new REpiceaCellEditor(NumberFormatFieldFactory.createNumberFormatField(8, NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.All, false), costsTableModel));
		
		JScrollPane scrollPane = new JScrollPane(costTable);
		Dimension dim = new Dimension(scrollPane.getPreferredSize().width + 10, 150);
		scrollPane.setPreferredSize(dim);
		return scrollPane;
	}
	
	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(discountRateField)) {
			caller.setDiscountRate(Double.parseDouble(discountRateField.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
//		} else if (e.getSource().equals(annualFixedCostsField)) {
//			caller.setAnnualFixedCosts(Double.parseDouble(annualFixedCostsField.getText()));
//			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(a0Parameter)) {
			caller.diameterBasedFunction.setParameterValue(0, Double.parseDouble(a0Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(a1Parameter)) {
			caller.diameterBasedFunction.setParameterValue(0, Double.parseDouble(a1Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(a2Parameter)) {
			caller.diameterBasedFunction.setParameterValue(0, Double.parseDouble(a2Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(b0Parameter)) {
			caller.volumeBasedFunction.setParameterValue(0, Double.parseDouble(b0Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(b1Parameter)) {
			caller.volumeBasedFunction.setParameterValue(0, Double.parseDouble(b1Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		} else if (e.getSource().equals(b2Parameter)) {
			caller.volumeBasedFunction.setParameterValue(0, Double.parseDouble(b2Parameter.getText()));
			firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);
		}
	}

	@Override
	public void synchronizeUIWithOwner() {
		REpiceaIOFileHandlerUI.RefreshTitle(caller, this);
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.CANADA);	// to ensure the digits are separed by a dot
		formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
		discountRateField.setText(((Double) caller.getDiscountRate()).toString());
//		annualFixedCostsField.setText(((Double) caller.getAnnualFixedCosts()).toString());
		costsTableModel.getDataVector().clear();
		for (CostRecord record : caller.costs) {
			costsTableModel.addRow(record.getRecord());
		}
		
		chavetPriceFunctionButton.setSelected(caller.selectedPriceFunction == PriceFunction.Chavet);
		hehsmatolPriceFunctionButton.setSelected(caller.selectedPriceFunction == PriceFunction.Heshmatol);
		customizedDiameterBasedPriceFunctionButton.setSelected(caller.selectedPriceFunction == PriceFunction.CustomizedDiameter);
		customizedVolumeBasedPriceFunctionButton.setSelected(caller.selectedPriceFunction == PriceFunction.CustomizedVolume);

		checkWhichFeaturesShouldBeEnabled();
	}

	private void checkWhichFeaturesShouldBeEnabled() {
		a0Parameter.setText(((Double) caller.diameterBasedFunction.getParameterValue(0)).toString());
		a0Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedDiameter);
		a1Parameter.setText(((Double) caller.diameterBasedFunction.getParameterValue(1)).toString());
		a1Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedDiameter);
		a2Parameter.setText(((Double) caller.diameterBasedFunction.getParameterValue(2)).toString());
		a2Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedDiameter);
		
		b0Parameter.setText(((Double) caller.volumeBasedFunction.getParameterValue(0)).toString());
		b0Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedVolume);
		b1Parameter.setText(((Double) caller.volumeBasedFunction.getParameterValue(1)).toString());
		b1Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedVolume);
		b2Parameter.setText(((Double) caller.volumeBasedFunction.getParameterValue(2)).toString());
		b2Parameter.setEnabled(caller.selectedPriceFunction == PriceFunction.CustomizedVolume);
	}

	@Override
	public Memorizable getWindowOwner() {return caller;}

	@Override
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(close)) {
			okAction();
		} else if (evt.getSource().equals(reset)) {
			caller.reset();
			doNotListenToAnymore();
			synchronizeUIWithOwner();
			listenTo();
			validate();
		}
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		REpiceaTableModel model = (REpiceaTableModel) arg0.getSource();
		caller.costs.clear();
		for (Object row : model.getDataVector()) {
			CostRecord cr = new CostRecord(((Vector) row).toArray());
			caller.costs.add(cr);
		}
		firePropertyChange(REpiceaAWTProperty.ActionPerformed, null, this);	
	}

	@Override
	public void postLoadingAction() {
		doNotListenToAnymore();
		synchronizeUIWithOwner();
		listenTo();
		validate();
	}

	@Override
	public void postSavingAction() {
		REpiceaIOFileHandlerUI.RefreshTitle(caller, this);
	}

	@Override
	public void okAction() {
		super.okAction();
		caller.saveProperties();
	}
	
	
	@Override
	public SettingMemory getSettingMemory() {
		return caller.settings;
	}


	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(chavetPriceFunctionButton)) {
			if (chavetPriceFunctionButton.isSelected()) {
				caller.selectedPriceFunction = PriceFunction.Chavet;
				checkWhichFeaturesShouldBeEnabled();
			}
		} else if (arg0.getSource().equals(hehsmatolPriceFunctionButton)) {
			if (hehsmatolPriceFunctionButton.isSelected()) {
				caller.selectedPriceFunction = PriceFunction.Heshmatol;
				checkWhichFeaturesShouldBeEnabled();
			}
		} else if (arg0.getSource().equals(customizedDiameterBasedPriceFunctionButton)) {
			if (customizedDiameterBasedPriceFunctionButton.isSelected()) {
				caller.selectedPriceFunction = PriceFunction.CustomizedDiameter;
				checkWhichFeaturesShouldBeEnabled();
			}
		} else if (arg0.getSource().equals(customizedVolumeBasedPriceFunctionButton)) {
			if (customizedVolumeBasedPriceFunctionButton.isSelected()) {
				caller.selectedPriceFunction = PriceFunction.CustomizedVolume;
				checkWhichFeaturesShouldBeEnabled();
			}
		}
	}

}
