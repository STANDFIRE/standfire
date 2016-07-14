/*
 * This file is part of the LERFoB modules for Capsis4.
 *
 * Copyright (C) 2009-2014 UMR 1092 (AgroParisTech/INRA) 
 * Contributors Jean-Francois Dhote, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Frederic Mothe,
 * Laurent Saint-Andre, Ingrid Seynave, Mathieu Fortin.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import capsis.commongui.util.Helper;
import capsis.extension.intervener.RdiAutoThinner.RDIAutoThinnerSettings;
import capsis.extension.modeltool.scenariorecorder.IntervenerDialog;
import capsis.gui.MainFrame;
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.GCoppiceProvider;

/**
 * RdiAutoThinnerDialog - Dialog box to input the parameters for RdiAutoThinner
 * @author F. Mothe, G. LeMoguedec - May 2010
 * @author Mathieu Fortin (refactoring) - May 2014
 */
public class RdiAutoThinnerDialog extends AmapDialog implements IntervenerDialog, ActionListener, ChangeListener {

    private static final long serialVersionUID = 20100520L;

    private static int HistoNbClasses = 50;
    
    /**
     * The HistoPanel class handles the diameter distributions before and after harvesting.
     * @author F. Mothe, G. LeMoguedec - May 2010
     * @author Mathieu Fortin (refactoring) - May 2014
     */
    private class HistoPanel extends JPanel {

        private HistoPanel() {}

        private void paintStandData(Graphics g, double[] hist, double maxY, Color color, int width) {
            g.setColor(color);
            for (int i = 0; i < hist.length; ++i) {
                int height = (int) ((double) hist[i] / maxY * getHeight());
                if (hist[i] > 0) {
                    height = Math.max(height, 1);
                }
                int x = (int) ((double) i / hist.length * getWidth()) - width / 2;
                int y = getHeight() - height;
                g.fillRect(x, y, width, height);
            }
        }

        @Override
        public Dimension getPreferredSize() {return new Dimension(100, 100);}

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            
            double[] preHarvestHist = RdiAutoThinnerDialog.this.caller.scoredTreeCollection.retrieveHistogram (true, HistoNbClasses);
            double[] postHarvestHist = RdiAutoThinnerDialog.this.caller.scoredTreeCollection.retrieveHistogram (false, HistoNbClasses);

            double maxY = findMaxY(preHarvestHist);
            int nbBars = HistoNbClasses;		// variable nbBars is useless
            int maxWidth = getWidth() / Math.max(nbBars, 1);
            
            paintStandData(g, preHarvestHist, maxY, Color.blue, (int) (.9 * maxWidth));	// true : before thinning
            paintStandData(g, postHarvestHist, maxY, Color.green, (int) (.7 * maxWidth));	// false : after thinning
        }

        private double findMaxY(double[] array) {
        	double maxY = 1d;
        	if (array != null) {
        		for (int i = 0; i < array.length; i++) {
        			if (array[i] > maxY) {
        				maxY = array[i];
        			}
        		}
        	}
        	return maxY;
        }
    }
    
    
    private final JButton ok;
    private final JButton help;
    private final JButton cancel;
    private JSlider objectiveRdiSlider;
    private JSlider thinningCoefSlider;
    private JSlider coppiceGSlider;//TB 2012 coppice basal area support for FTChene
    
    // TODO : replace these labels by editable JTextField:
    private JLabel objectiveRdiText;
    private JLabel thinningCoefText;
    private JLabel coppiceGText;
    private JTable resultTable;
    private HistoPanel histoPanel;
    private RdiAutoThinner.RDIAutoThinnerSettings newSettings;
    
    private final RdiAutoThinner caller;
    
    
    private double m_initialRdi;
    
    // Results data:
    private RdiAutoThinnerStandData initialStandData;
    private RdiAutoThinnerStandData postHarvestStandData;
    private RdiAutoThinnerStandData removedStemData;
//    private double m_Kg;
    private static final int RESULTS_NB_STATES = 3;
    private static final int RESULTS_NB_VARIABLES = 5;
    // Sliders stuff:
    private static final int SLIDER_RDI_MAX = 100;	// 0 to 100
    private static final int SLIDER_RDI_NBTICKS = 5;
    private final static int SLIDER_COEF_MAX = 100;	// -100 to 100
    private static final int SLIDER_COEF_NBTICKS = 5;
    
    //TB 2012 coppice basal area support for FTChene
    private static final int SLIDER_COPPICE_G_NBTICKS = 5;
    private final NumberFormat nf;


    /**
     * Constructor
     */
    protected RdiAutoThinnerDialog(RdiAutoThinner autoThinner) {
        super(MainFrame.getInstance());	// dialog centered on capsis main frame
        activateLocationMemorization(getClass().getName());	// dialog location saved

        caller = autoThinner;
        
        this.nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(2);

        ok = UIControlManager.createCommonButton(CommonControlID.Ok);
        cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
        help = UIControlManager.createCommonButton(CommonControlID.Help);

        initialise();
        createUI();
        pack();
    }

    /**
     * Initialisations to perform before createUI
     */
    private void initialise() {
        initialStandData = caller.scoredTreeCollection.getInitialStandCharacteristics();
        postHarvestStandData = caller.scoredTreeCollection.getInitialStandCharacteristics();
        removedStemData = postHarvestStandData.getDifference(initialStandData);
        m_initialRdi = initialStandData.getRDI();
        if (m_initialRdi <= 0.) {
            m_initialRdi = 1E-5;	// m_initialRdi always > 0
        }

        if (caller.getSettings() == null) {
        	newSettings = new RDIAutoThinnerSettings(m_initialRdi);
        } else {
            double objectiveRdi = Math.min(Math.max(caller.getSettings().getObjectiveRdi(), 0.), m_initialRdi);
            double thinningCoef = Math.min(Math.max(caller.getSettings().getThinningCoefficient(), -1.), 1.);
        	newSettings = new RDIAutoThinnerSettings(objectiveRdi, thinningCoef);
        }
        MethodProvider mp = caller.model.getMethodProvider();
        if (mp instanceof GCoppiceProvider) {
        	newSettings.setCoppiceBasalAreaM2(((GCoppiceProvider) mp).getCoppiceG(caller.stand, null));
        }
    }


    private void updateSettings() {
        newSettings.setObjectiveRdi(sliderValueToRdi(objectiveRdiSlider.getValue()));
        newSettings.setThinningCoefficient(sliderValueToCoef(thinningCoefSlider.getValue()));
        //TB 2012 coppice basal area support for FTChene
        if (coppiceGSlider != null) {
            newSettings.setCoppiceBasalAreaM2(sliderValueToCoppiceG(coppiceGSlider.getValue()));
        }

    }

    private void updateResults() {
    	postHarvestStandData = caller.getFutureStandCharacteristics(newSettings);
    	removedStemData = postHarvestStandData.getDifference(initialStandData);
    }

    private void updateResultPanel() {
        resultTable.repaint();
        histoPanel.repaint();
    }

    /**
     * Action on ok button
     */
    private void okAction() {
        if (removedStemData.getN() <= 0. && newSettings.getCoppiceBasalAreaM2() == initialStandData.getCoppiceBasalAreaM2()) {
            JOptionPane.showMessageDialog(this,
                    Translator.swap("RdiAutoThinnerDialog.nothingToCut"),
                    Translator.swap("Shared.warning"), JOptionPane.WARNING_MESSAGE);
        } else {
            setValidDialog(true);		// the window is closed
        }
        return;
    }

    @Override // ActionListener
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource().equals(ok)) {
            okAction();
            caller.setSettings(newSettings);
        } else if (evt.getSource().equals(cancel)) {
            setValidDialog(false);
        } else if (evt.getSource().equals(help)) {
            Helper.helpFor(this);	// fc - 16.6.2003
        }
    }

    private void updateUI() {
        updateSettings();
        updateResults();
        Runnable doRun = new Runnable() {
			@Override
			public void run () {
				RdiAutoThinnerDialog.this.updateResultPanel();
			}
        };
        SwingUtilities.invokeLater(doRun);		// to ensure a slack in the interface response
    }
    
    
    @Override // ChangeListener
    public void stateChanged(ChangeEvent evt) {
    	if (evt.getSource () instanceof JSlider) {
            JSlider source = (JSlider) evt.getSource();
            if (source.equals(objectiveRdiSlider)) {
                objectiveRdiText.setText(rdiToString(sliderValueToRdi(source.getValue())));
                updateUI();
            } else if (source.equals(thinningCoefSlider)) {
                thinningCoefText.setText(coefToString(sliderValueToCoef(source.getValue())));
                updateUI();
            } else if (coppiceGSlider != null && coppiceGText != null && source.equals(coppiceGSlider)) {
                coppiceGText.setText(coefToString(sliderValueToCoef(source.getValue())));
                updateUI();
            }
    	}
    }

    /**
     * Initialize the GUI.
     */
    private void createUI() {

        Border etched = BorderFactory.createEtchedBorder();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        //~ Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap
        //~ ("RdiAutoThinnerDialog.thinningParameters") +" : ");
        //~ mainPanel.setBorder (b1);

        //~ {
        //~ JPanel pan = new JPanel (new FlowLayout (FlowLayout.LEFT));
        //~ pan.add (new JWidthLabel (Translator.swap ("RdiAutoThinnerDialog.objectiveRdi")+" :", 180));
        //~ objectiveRdi = new JTextField (5);
        //~ if (m_data.objectiveRdi >= 0.) {objectiveRdi.setText ("" + m_data.objectiveRdi);}
        //~ pan.add (objectiveRdi);
        //~ mainPanel.add (pan);
        //~ }

        //~ {
        //~ JPanel pan = new JPanel (new FlowLayout (FlowLayout.LEFT));
        //~ pan.add (new JWidthLabel (Translator.swap ("RdiAutoThinnerDialog.thinningCoef")+" :", 180));
        //~ thinningCoef = new JTextField (5);
        //~ if (m_data.thinningCoef >= 0.) {thinningCoef.setText ("" + m_data.thinningCoef);}
        //~ pan.add (thinningCoef);
        //~ mainPanel.add (pan);
        //~ }

        {
            Box box = Box.createVerticalBox();
            box.setBorder(BorderFactory.createTitledBorder(etched, Translator.swap("RdiAutoThinnerDialog.thinningParameters") + " : "));

            // objectiveRdiText :
            {
                JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
                //~ pan.add (new JLabel (Translator.swap (
                //~ "RdiAutoThinnerDialog.objectiveRdi")+" : "));
                pan.add(new JWidthLabel(Translator.swap(
                        "RdiAutoThinnerDialog.objectiveRdi") + " :", 150));

                objectiveRdiText = new JLabel(rdiToString(newSettings.getObjectiveRdi()));
                pan.add(objectiveRdiText);
                box.add(pan);
            }

            // objectiveRdiSlider :
            {
                int min = 0;
                int max = SLIDER_RDI_MAX;
                int nbTicks = SLIDER_RDI_NBTICKS;

                objectiveRdiSlider = new JSlider(JSlider.HORIZONTAL, min, max, rdiToSliderValue(newSettings.getObjectiveRdi()));
                objectiveRdiSlider.addChangeListener(this);
                objectiveRdiSlider.setMajorTickSpacing((max - min) / (nbTicks - 1));
                objectiveRdiSlider.setPaintTicks(true);
                objectiveRdiSlider.setPaintLabels(true);
                objectiveRdiSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                //objectiveRdiSlider.setFont (new Font ("Serif", Font.ITALIC, 15));
                objectiveRdiSlider.setInverted(true);	// because thinning intensity = 1 - rdi

                Hashtable<Integer, JComponent> table =
                        new Hashtable<Integer, JComponent>();
                for (int n = 0; n < nbTicks; ++n) {
                    int value = n * (max - min) / (nbTicks - 1) + min;
                    table.put(value, new JLabel(rdiToString(sliderValueToRdi(value))));
                }
                //~ for (int n = 0; n < SLIDER_RDI_NBTICKS; ++n) {
                //~ int value = n * SLIDER_RDI_MAX / (SLIDER_RDI_NBTICKS - 1);
                //~ table.put (value, new JLabel (rdiToString (sliderValueToRdi (value))));
                //~ }
                objectiveRdiSlider.setLabelTable(table);
            }

            box.add(objectiveRdiSlider);

            // thinningCoefText :
            {
                JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
                //~ pan.add (new JLabel (Translator.swap (
                //~ "RdiAutoThinnerDialog.thinningCoef")+" : "));
                pan.add(new JWidthLabel(Translator.swap("RdiAutoThinnerDialog.thinningCoef") + " :", 150));
                thinningCoefText = new JLabel(coefToString(newSettings.getThinningCoefficient()));
                pan.add(thinningCoefText);
                box.add(pan);
            }

            //box.add (Box.createVerticalStrut (10));	// space between sliders

            // thinningCoefSlider :
            {
                int min = -SLIDER_COEF_MAX;
                int max = +SLIDER_COEF_MAX;
                int nbTicks = SLIDER_COEF_NBTICKS;
                thinningCoefSlider = new JSlider(JSlider.HORIZONTAL, min, max,
                        coefToSliderValue(newSettings.getThinningCoefficient()));
                thinningCoefSlider.addChangeListener(this);
                thinningCoefSlider.setMajorTickSpacing((max - min) / (nbTicks - 1));
                thinningCoefSlider.setPaintTicks(true);
                thinningCoefSlider.setPaintLabels(true);
                thinningCoefSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                //thinningCoefSlider.setFont (new Font ("Serif", Font.ITALIC, 15));
                Hashtable<Integer, JComponent> table =
                        new Hashtable<Integer, JComponent>();
                for (int n = 0; n < nbTicks; ++n) {
                    int value = n * (max - min) / (nbTicks - 1) + min;
                    table.put(value, new JLabel(coefToString(sliderValueToCoef(value))));
                }
                thinningCoefSlider.setLabelTable(table);
            }
            box.add(thinningCoefSlider);


            //TB 2012 coppice basal area support for models with non individualized coppice G like FTChene
            MethodProvider mp = caller.model.getMethodProvider();
            if (mp instanceof GCoppiceProvider) {
                JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pan.add(new JWidthLabel(Translator.swap("RdiAutoThinnerDialog.coppiceGText") + " :", 150));
                coppiceGText = new JLabel(coefToString(5d));
                pan.add(coppiceGText);
                box.add(pan);
                int min = 0;
                int max = (int) (initialStandData.getCoppiceBasalAreaM2() * 100);
                int nbTicks = SLIDER_COPPICE_G_NBTICKS;
                coppiceGSlider = new JSlider(JSlider.HORIZONTAL, min, max, coppiceToSliderValue(newSettings.getCoppiceBasalAreaM2()));
                coppiceGSlider.addChangeListener(this);
                coppiceGSlider.setMajorTickSpacing((max - min) / (nbTicks - 1));
                coppiceGSlider.setPaintTicks(true);
                coppiceGSlider.setPaintLabels(true);
                coppiceGSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                Hashtable<Integer, JComponent> table = new Hashtable<Integer, JComponent>();
                for (int n = 0; n < nbTicks; ++n) {
                    int value = n * (max - min) / (nbTicks - 1) + min;
                    table.put(value, new JLabel(coppiceToString(sliderValueToCoppiceG(value))));
                }
                coppiceGSlider.setLabelTable(table);
                box.add(coppiceGSlider);
            }

            box.add(Box.createHorizontalStrut(300));	// minimal size of the sliders
            mainPanel.add(box);
        }

        {
            // Result panel
            Box resultPanel = Box.createVerticalBox();
            Border b2 = BorderFactory.createTitledBorder(etched,
                    Translator.swap("RdiAutoThinnerDialog.results"));
            resultPanel.setBorder(b2);

            histoPanel = new HistoPanel();
            histoPanel.setPreferredSize(new Dimension(0, 200));
            resultPanel.add(histoPanel);

            final String[] colNames = new String[RESULTS_NB_STATES + 1];
            colNames[0] = "";
            colNames[1] = Translator.swap("RdiAutoThinnerDialog.before");
            colNames[2] = Translator.swap("RdiAutoThinnerDialog.after");
            colNames[3] = Translator.swap("RdiAutoThinnerDialog.cut");

            int numberOfVariables = RESULTS_NB_VARIABLES;
            if (caller.model.getMethodProvider() instanceof GCoppiceProvider) {
            	numberOfVariables++;
            }
            
            final String[] rowNames = new String[numberOfVariables];
            rowNames[0] = Translator.swap("RdiAutoThinnerDialog.N");
            rowNames[1] = Translator.swap("RdiAutoThinnerDialog.G_m2");
            rowNames[2] = Translator.swap("RdiAutoThinnerDialog.Dg_cm");
            rowNames[3] = Translator.swap("RdiAutoThinnerDialog.rdi");
            rowNames[4] = Translator.swap("RdiAutoThinnerDialog.Kg");
            if (numberOfVariables == 6) {
                rowNames[5] = Translator.swap("RdiAutoThinnerDialog.coppice");
            }
            DefaultTableModel resultModel = new DefaultTableModel(colNames, numberOfVariables) {
            	
                private static final long serialVersionUID = 20100520L;

                public Object getValueAt(int row, int col) {
                    String value;
                    if (col == 0) {
                        value = rowNames[row];
                    }
                    else {
                        int state = col - 1;
                        RdiAutoThinnerStandData data;
                        switch (state) {
                            case 0:
                                data = initialStandData;
                                break;
                            case 1:
                                data = postHarvestStandData;
                                break;
                            case 2:
                                data = removedStemData;
                                break;
                            default:
                                data = null;
                        }
                        switch (row) {
                            case 0:
                                value = "" + (int) data.getN();
                                break;
                            case 1:
                                value = nf.format(data.getBasalAreaM2() / data.getAreaHa());
                                break;
                            case 2:
                                value = nf.format(data.getMeanQuadraticDiameterCm());
                                break;
                            case 3:
                                value = rdiToString(data.getRDI());
                                break;
                            case 4:
                                value = state == 1 ? nf.format(removedStemData.getThinningCoefficientKg()) : "";
                                break;
                            case 5:
                               	value = nf.format(data.getCoppiceBasalAreaM2());
                            	break;
                            default:
                                value = null;
                        }
                    }
                    return value;
                }
            };
            resultTable = new JTable(resultModel);
            //~ StringRenderer stringRenderer = new StringRenderer ();
            //~ resultTable.setDefaultRenderer (Object.class, stringRenderer);
            //~ resultTable.setDefaultRenderer (String.class, stringRenderer);
            //~ resultTable.setDefaultRenderer (Number.class, stringRenderer);
            //~ resultTable.setDefaultRenderer (Double.class, stringRenderer);
            //~ resultTable.setDefaultRenderer (Integer.class, stringRenderer);

            JScrollPane aux = new JScrollPane(resultTable);
            aux.setPreferredSize(new Dimension(150, resultTable.getRowHeight()
                    * (resultModel.getRowCount() + 2)));
            resultPanel.add(aux);
            //resultPanel.addStrut0 ();

            mainPanel.add(resultPanel);
        }

        JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pControl.add(ok);
        pControl.add(cancel);
        pControl.add(help);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        help.addActionListener(this);

        setDefaultButton(ok);	// from AmapDialog

        getContentPane().add(mainPanel);
        getContentPane().add(pControl, "South");

        setTitle(Translator.swap("RdiAutoThinnerDialog.title"));

        setModal(true);
    }

    // Helper methods for the sliders:
    private int rdiToSliderValue(double rdi) {
        // slider value ranges from 0 to SLIDER_RDI_MAX
        // rdi ranges from 0 to SLIDER_RDI_MAX
        // m_initialRdi is always > 0
        return (int) (rdi / m_initialRdi * SLIDER_RDI_MAX);
    }

    private double sliderValueToRdi(int value) {
        // slider value ranges from 0 to SLIDER_RDI_MAX
        // rdi ranges from 0 to SLIDER_RDI_MAX
        //~ return m_initialRdi * value / SLIDER_RDI_MAX;
        // To make sure that nothing will be cut for SLIDER_RDI_MAX :
        return value < SLIDER_RDI_MAX ? m_initialRdi * value / SLIDER_RDI_MAX : m_initialRdi;

    }

    private String rdiToString(double rdi) {
        // Use current locale: return String.format ("%5.3f", rdi);
        return nf.format(rdi);
    }

    private int coefToSliderValue(double coef) {
        // slider value ranges from -SLIDER_COEF_MAX to SLIDER_COEF_MAX
        // coef ranges from -1 to +1
        return (int) (coef * SLIDER_COEF_MAX);
    }

    private double sliderValueToCoef(int value) {
        // slider value ranges from -SLIDER_COEF_MAX to SLIDER_COEF_MAX
        // coef ranges from -1 to +1
        return (double) value / SLIDER_COEF_MAX;
    }

    private String coefToString(double coef) {
        return nf.format(coef);
    }

    //TB 2012 coppice basal area support for FTChene
    private int coppiceToSliderValue(double coppiceG) {
        return (int) (coppiceG * 100);
    }

    private double sliderValueToCoppiceG(int value) {
        return (double) value / 100;
    }

    private String coppiceToString(double coppiceG) {
        return nf.format(coppiceG);
    }

}
