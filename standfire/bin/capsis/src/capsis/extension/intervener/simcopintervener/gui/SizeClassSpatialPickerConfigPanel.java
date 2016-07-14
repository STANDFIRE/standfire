package capsis.extension.intervener.simcopintervener.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AlgorithmType;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AutoThinTargetValueType;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AverageDistanceComputationMethod;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.SizeClassType;
import capsis.extension.intervener.simcopintervener.picker.PickerStand;
import capsis.extension.intervener.simcopintervener.picker.PickerTree;
import capsis.extension.intervener.simcopintervener.picker.SimcopTreePicker;
import capsis.extension.intervener.simcopintervener.utilities.Tools;

/**
 * 
 * @author thomas.bronner@gmail.com
 */
public class SizeClassSpatialPickerConfigPanel extends JPanel {

	private PickerStand scene;
	private double targetDensity;
	private int targetNumberOfTrees;
	private String sizeClassesWheightsString;
	private List<Double> sizeClassesFactors;
	private String distanceFactorsString;
	private boolean doSecondPass;
	AlgorithmType algoType = AlgorithmType.JMORandomSizeClassPick;
	private boolean wheightByFrequencies;
	private List<Double> distanceFactors;
	private AverageDistanceComputationMethod method = AverageDistanceComputationMethod.HexMesh;
	private SizeClassType sizeClassType = SizeClassType.DiameterClass;
	private AutoThinTargetValueType targetValueType = AutoThinTargetValueType.DensityAfterThinning;
	private boolean integerBounds;

	public SizeClassSpatialPickerConfigPanel () {
		initComponents ();
	}

	public boolean getAndCheckValues () {
		String name = getClass ().getName ();
		try {
			if (targetValueType == AutoThinTargetValueType.DensityAfterThinning) {
				targetDensity = Double.valueOf (tfTargetDensity.getText ());
				if (targetDensity <= 0) { throw new Exception (); }
			} else {
				targetNumberOfTrees = Integer.valueOf (tfTargetNumberOfTrees.getText ());
				if (targetNumberOfTrees <= 0) { throw new Exception (); }
			}
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog (this, Translator.swap ("wrongNumberToThin"), name, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		sizeClassesWheightsString = tfSizeClassesWheightsString.getText ();
		// decode string
		try {
			sizeClassesFactors = Tools.decodeDoubleListString (sizeClassesWheightsString);
			Settings.setProperty ("simcopIntervenerSizeClassesWheights", sizeClassesWheightsString);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog (this, Translator.swap ("wrongSizeClassPonderationString"), name, JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// distance factor must correspond to volume classes concerned by distance rule
		try {
			distanceFactorsString = tfDistanceFactor.getText ();
			distanceFactors = Tools.decodeDoubleListString (distanceFactorsString);
			for (Double d : distanceFactors) {
				if (d < 0d && d > 1d) { throw new Exception (); }
			}
			wheightByFrequencies = cbRelativeFrequencies.isSelected ();
			integerBounds = cbIntegerBounds.isSelected ();
			doSecondPass = cbDoSecondPass.isSelected ();
			algoType = (AlgorithmType) cbAlgoType.getSelectedItem ();
			if (rbDiameterClass.isSelected ()) {
				sizeClassType = SizeClassType.DiameterClass;
			} else if (rbVolumeClass.isSelected ()) {
				sizeClassType = SizeClassType.VolumeClass;
			}
			Settings.setProperty ("simcopIntervenerDistanceFactor", distanceFactorsString);
					
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("wrongDistanceFactors"));
			return false;
		}
		method = (AverageDistanceComputationMethod) cbAvgDistMethod.getSelectedItem ();
		return true;
	}

	public Collection<PickerTree> getTrees () throws Exception {
		Collection pickedTrees = null;
		if (getAndCheckValues ()) {
			ArrayList<PickerTree> simcopTrees = new ArrayList<PickerTree> ();
			for (PickerTree tree : scene.getPickerTrees ()) {
				// if (tree instanceof PickerTree) {
				if (!tree.isMarked ()) {
					simcopTrees.add ((PickerTree) tree);
				}
				// } else {
				// throw new Exception (
				// "SizeClassSpatialPickerConfigPanel.getTrees (): found a Tree which is not a PickerTree");
				// }
			}
			SimcopTreePicker picker;
			if (targetValueType == AutoThinTargetValueType.DensityAfterThinning) {
				picker = new SimcopTreePicker (simcopTrees, scene, targetDensity, method);
			} else {
				picker = new SimcopTreePicker (simcopTrees, scene, targetNumberOfTrees, method);
			}
			picker.methodJMOMPMerge (algoType, sizeClassesFactors, distanceFactors, sizeClassType, integerBounds, wheightByFrequencies, doSecondPass);
			pickedTrees = picker.getPickedTrees ();
			String logMessages = picker.getLogMessages ();
			if (logMessages != null) {
				MessageDialog.print (this, logMessages);
			}
		}
		return pickedTrees;
	}

	private void manageTargetType () {
		if (rbTargetDensity.isSelected ()) {
			targetValueType = AutoThinTargetValueType.DensityAfterThinning;
			tfTargetDensity.setEnabled (true);
			tfTargetNumberOfTrees.setEnabled (false);
		} else {
			targetValueType = AutoThinTargetValueType.NbTreesToPickPerHa;
			tfTargetDensity.setEnabled (false);
			tfTargetNumberOfTrees.setEnabled (true);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings ("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgSizeType = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();
        bgTarget = new javax.swing.ButtonGroup();
        cbAlgoType = new javax.swing.JComboBox();
        lblTarget = new javax.swing.JLabel();
        tfTargetDensity = new javax.swing.JTextField();
        tfTargetNumberOfTrees = new javax.swing.JTextField();
        lblSizeClassesWheightsString = new javax.swing.JLabel();
        tfSizeClassesWheightsString = new javax.swing.JTextField();
        lblDistanceFactor = new javax.swing.JLabel();
        tfDistanceFactor = new javax.swing.JTextField();
        lblAvgDistMethod = new javax.swing.JLabel();
        cbAvgDistMethod = new javax.swing.JComboBox();
        cbRelativeFrequencies = new javax.swing.JCheckBox();
        lblSizeClassType = new javax.swing.JLabel();
        cbDoSecondPass = new javax.swing.JCheckBox();
        rbVolumeClass = new javax.swing.JRadioButton();
        rbDiameterClass = new javax.swing.JRadioButton();
        rbTargetDensity = new javax.swing.JRadioButton();
        rbTargetNumberOfTree = new javax.swing.JRadioButton();
        cbIntegerBounds = new javax.swing.JCheckBox();

        jTextField1.setText("jTextField1");

        cbAlgoType.setModel(new DefaultComboBoxModel(AlgorithmType.values()));

        lblTarget.setText(Translator.swap("thinIntensity"));

        tfTargetDensity.setText("500");
        tfTargetDensity.setMinimumSize(new java.awt.Dimension(50, 20));
        tfTargetDensity.setPreferredSize(new java.awt.Dimension(50, 20));

        tfTargetNumberOfTrees.setText("200");
        tfTargetNumberOfTrees.setEnabled(false);
        tfTargetNumberOfTrees.setMinimumSize(new java.awt.Dimension(50, 20));
        tfTargetNumberOfTrees.setPreferredSize(new java.awt.Dimension(50, 20));

        lblSizeClassesWheightsString.setText(Translator.swap("sizeClassesWheights"));

        tfSizeClassesWheightsString.setText("5;4;3;2;1");
        tfSizeClassesWheightsString.setMinimumSize(new java.awt.Dimension(50, 20));
        tfSizeClassesWheightsString.setPreferredSize(new java.awt.Dimension(70, 20));

        lblDistanceFactor.setText(Translator.swap("sizeClassesFactors"));

        tfDistanceFactor.setText("0;0;0;0.5;0.8");
        tfDistanceFactor.setMinimumSize(new java.awt.Dimension(50, 20));
        tfDistanceFactor.setPreferredSize(new java.awt.Dimension(70, 20));

        lblAvgDistMethod.setText(Translator.swap("averageDistanceMethod"));

        cbAvgDistMethod.setModel(new DefaultComboBoxModel(AverageDistanceComputationMethod.values()));

        cbRelativeFrequencies.setText(Translator.swap("useRelativeFrequencies"));

        lblSizeClassType.setText(Translator.swap("sizeClassType"));

        cbDoSecondPass.setSelected(true);
        cbDoSecondPass.setText(Translator.swap("secondPass"));

        bgSizeType.add(rbVolumeClass);
        rbVolumeClass.setText(Translator.swap("volumeClasses"));

        bgSizeType.add(rbDiameterClass);
        rbDiameterClass.setSelected(true);
        rbDiameterClass.setText(Translator.swap("diameterClasses"));

        bgTarget.add(rbTargetDensity);
        rbTargetDensity.setSelected(true);
        rbTargetDensity.setText(Translator.swap("densityAfterThin"));
        rbTargetDensity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTargetDensityActionPerformed(evt);
            }
        });

        bgTarget.add(rbTargetNumberOfTree);
        rbTargetNumberOfTree.setText(Translator.swap("toRemoveNHa"));
        rbTargetNumberOfTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTargetNumberOfTreeActionPerformed(evt);
            }
        });

        cbIntegerBounds.setSelected(true);
        cbIntegerBounds.setText(Translator.swap("integerBounds"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblDistanceFactor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfDistanceFactor, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTarget)
                            .addComponent(cbIntegerBounds)
                            .addComponent(cbRelativeFrequencies)
                            .addComponent(lblSizeClassType)
                            .addComponent(cbDoSecondPass)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(rbTargetNumberOfTree)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(tfTargetNumberOfTrees, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(rbTargetDensity)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(tfTargetDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(rbDiameterClass)
                                    .addComponent(rbVolumeClass)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblSizeClassesWheightsString)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfSizeClassesWheightsString, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(115, 115, 115))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cbAlgoType, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(lblAvgDistMethod)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbAvgDistMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbAlgoType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lblTarget)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTargetDensity)
                    .addComponent(tfTargetDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTargetNumberOfTree)
                    .addComponent(tfTargetNumberOfTrees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSizeClassType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbDiameterClass)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbVolumeClass)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSizeClassesWheightsString)
                    .addComponent(tfSizeClassesWheightsString, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbRelativeFrequencies)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAvgDistMethod)
                    .addComponent(cbAvgDistMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDistanceFactor)
                    .addComponent(tfDistanceFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbIntegerBounds)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDoSecondPass)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void rbTargetDensityActionPerformed (java.awt.event.ActionEvent evt) {// GEN-FIRST:event_rbTargetDensityActionPerformed
		manageTargetType ();
	}// GEN-LAST:event_rbTargetDensityActionPerformed

	private void rbTargetNumberOfTreeActionPerformed (java.awt.event.ActionEvent evt) {// GEN-FIRST:event_rbTargetNumberOfTreeActionPerformed
		manageTargetType ();
	}// GEN-LAST:event_rbTargetNumberOfTreeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgSizeType;
    private javax.swing.ButtonGroup bgTarget;
    private javax.swing.JComboBox cbAlgoType;
    private javax.swing.JComboBox cbAvgDistMethod;
    private javax.swing.JCheckBox cbDoSecondPass;
    private javax.swing.JCheckBox cbIntegerBounds;
    private javax.swing.JCheckBox cbRelativeFrequencies;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblAvgDistMethod;
    private javax.swing.JLabel lblDistanceFactor;
    private javax.swing.JLabel lblSizeClassType;
    private javax.swing.JLabel lblSizeClassesWheightsString;
    private javax.swing.JLabel lblTarget;
    private javax.swing.JRadioButton rbDiameterClass;
    private javax.swing.JRadioButton rbTargetDensity;
    private javax.swing.JRadioButton rbTargetNumberOfTree;
    private javax.swing.JRadioButton rbVolumeClass;
    private javax.swing.JTextField tfDistanceFactor;
    private javax.swing.JTextField tfSizeClassesWheightsString;
    private javax.swing.JTextField tfTargetDensity;
    private javax.swing.JTextField tfTargetNumberOfTrees;
    // End of variables declaration//GEN-END:variables

	/**
	 * @return the scene
	 */
	public PickerStand getScene () {
		return scene;
	}

	/**
	 * @param scene the scene to set
	 */
	public void setScene (PickerStand scene) {
		this.scene = scene;

		tfDistanceFactor.setEnabled (scene.isSpatialized ()); // fc+mp-6.6.2014
		cbAvgDistMethod.setEnabled (scene.isSpatialized ()); // fc+mp-6.6.2014
		cbDoSecondPass.setEnabled (scene.isSpatialized ()); // fc+mp-6.6.2014
		
	}

	public AutoThinTargetValueType getTargetValueType () {
		return targetValueType;
	}

	public void setTargetValueType (AutoThinTargetValueType targetValueType) {
		this.targetValueType = targetValueType;
	}

	public int getTargetNumberOfTrees () {
		return targetNumberOfTrees;
	}

	public double getTargetDensity () {
		return targetDensity;
	}

	public boolean isIntegerBounds () {
		return integerBounds;
	}

	public String getSizeClassesWheightsString () {
		return sizeClassesWheightsString;
	}

	public List<Double> getSizeClassesFactors () {
		return sizeClassesFactors;
	}

	public List<Double> getDistanceFactors () {
		return distanceFactors;
	}

	public boolean isWheighedtByFrequencies () {
		return wheightByFrequencies;
	}

	public boolean isDoSecondPass () {
		return doSecondPass;
	}

	public SizeClassType getSizeClassType () {
		return sizeClassType;
	}

	public AverageDistanceComputationMethod getAverageDistanceComputationMethod () {
		return method;
	}

	public AlgorithmType getAlgoType () {
		return algoType;
	}

}
