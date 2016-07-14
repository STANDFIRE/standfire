/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
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
package fireparadox.extension.objectviewer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Disposable;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Namable;
import jeeb.lib.util.Translator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import capsis.extension.AbstractObjectViewer;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSeverity;

/**
 * A viewer to watch the damage of the trees after a fire.
 * 
 * @author F. de Coligny - September 2009
 */
public class FiSeverityOV extends AbstractObjectViewer implements Disposable, ActionListener, Namable {

	static {
		Translator.addBundle ("fireparadox.extension.objectviewer.FiSeverityOV");
	}
	static public final String NAME = Translator.swap ("FiSeverityOV");
	static public final String DESCRIPTION = Translator.swap ("FiSeverityOV.description");
	static public final String AUTHOR = "F. de Coligny";
	static public final String VERSION = "1.0";

	private JTextField numberOfKilledMatureTrees;
	private JTextField meanAgeOfKilledMatureTrees;
	private JPanel treeMortalityGraph;
	private JPanel cambiumMortalityGraph;
	private ChartPanel treeMortalityChartPanel;
	private ChartPanel cambiumMortalityChartPanel;
	private JLabel statusBar;

	/**
	 * Default constructor.
	 */
	public FiSeverityOV () {
	}

	@Override
	public void init (Collection s) throws Exception {
		try {
			createUI ();
			show (new ArrayList (s));

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSeverityOV.c ()", e.toString (), e);
			throw e;
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			// referent is a Collection, a candidate selection
			// Warning: there may be trees and cells in the referent collection
			// Do not match only if all the elements are trees of a given type
			// (cells also)

			// We match if the collection contains at least one FiPlant
			// WARNING: updateUI () will have to check if its severity is !=
			// null
			Collection candidateSelection = (Collection) referent;
			for (Object o : candidateSelection) {
				if (o instanceof FiPlant) { return true; }
			}
			return false;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSeverityOV.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**
	 * Disposable.
	 */
	public void dispose () {
	}

	/**
	 * ActionListener interface
	 */
	public void actionPerformed (ActionEvent evt) {
	}

	/**
	 * OVSelector framework.
	 */
	@Override
	public Collection show (Collection candidateSelection) {
		realSelection = updateUI (candidateSelection);
		// ~ System.out.println (""+getName
		// ()+".select candidateSelection "+candidateSelection.size ()
		// ~ +" realSelection "+realSelection.size ());
		return realSelection;
	}

	/**
	 * User interface definition.
	 */
	protected void createUI () {

		ColumnPanel main = new ColumnPanel ();

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiSeverityOV.numberOfKilledMatureTrees") + " : ", 200));
		numberOfKilledMatureTrees = new JTextField (3);
		l1.add (numberOfKilledMatureTrees);
		l1.addStrut0 ();
		main.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FiSeverityOV.meanAgeOfKilledMatureTrees") + " : ", 200));
		meanAgeOfKilledMatureTrees = new JTextField (3);
		l2.add (meanAgeOfKilledMatureTrees);
		l2.addStrut0 ();
		main.add (l2);
		main.addStrut0 ();

		treeMortalityGraph = new JPanel (new BorderLayout ()); // fc-16.11.2015 bigger graph
//		treeMortalityGraph = new JPanel ();

		// cambiumMortalityGraph = new JPanel();
		// main.add(cambiumMortalityGraph);

		LinePanel l100 = new LinePanel ();
		statusBar = new JLabel ();
		l100.add (statusBar);
		l100.addStrut0 ();

		setLayout (new BorderLayout ());
		add (main, BorderLayout.NORTH);
		add (treeMortalityGraph, BorderLayout.CENTER);
		add (l100, BorderLayout.SOUTH);

	}

	/**
	 * Shows subject, returns what it effectively shown.
	 */
	protected Collection updateUI (Collection subject) {
		try {
			if (subject == null) {
				subject = new ArrayList ();
			} // fc - 7.12.2007 - ovs should accept null subject

			// Extract the FiPlant with a severity != null
			Collection<FiPlant> plants = extractMaturePlants (subject);

			if (plants.isEmpty ()) {
				// No plants with damage: all textfields empty
				setProgramaticalyEnabled (false);
				statusBar.setText (Translator.swap ("FiSeverityOV.noDamage"));
				statusBar.setEnabled (true);
				numberOfKilledMatureTrees.setText ("");
			} else {
				// Found plants with damage: update textfields
				setProgramaticalyEnabled (true);

				statusBar.setText (Translator.swap ("FiSeverityOV.matureTreesWithDamage") + " : " + plants.size ());

				int treeKilledNumber = 0;
				int cambiumKilledNumber = 0;
				double meanAge = 0.0;
				// we create a map with the trees, the key is the floor of the
				// treeHeight
				Map<Integer,Integer> treeMortalityClasses = new HashMap<Integer,Integer> ();
				Map<Integer,Integer> treeClasses = new HashMap<Integer,Integer> ();
				for (FiPlant plant : plants) {
					int h = (int) Math.floor (plant.getHeight ());
					if (!treeClasses.containsKey (h)) {
						treeClasses.put (h, 0);
						treeMortalityClasses.put (h, 0);
					}
					treeClasses.put (h, treeClasses.get (h) + 1);
					FiSeverity severity = plant.getSeverity ();
					if (severity.getIsKilled ()) {
						treeKilledNumber++;
						meanAge += plant.getAge ();
						treeMortalityClasses.put (h, treeMortalityClasses.get (h) + 1);
					}
					if (severity.getCambiumIsKilled ()) {
						cambiumKilledNumber++;
					}
				}
				meanAge = meanAge / (treeKilledNumber);
				numberOfKilledMatureTrees.setText ("" + treeKilledNumber);
				meanAgeOfKilledMatureTrees.setText ("" + meanAge);
				updateGraphs (treeClasses, treeMortalityClasses);
			}

			Collection accurateSelection = plants;
			return accurateSelection; // shown trees

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSeverityOV.updateUI ()", "Caught an exception", e);
			// ~ statusBar.setText (Translator.swap
			// ("FiSeverityOV.couldNotOpenSketcherSeeLog"));
		}
		return Collections.EMPTY_LIST; // showed nothing
	}

	private Collection<FiPlant> extractMaturePlants (Collection c) throws Exception {
		// get a collection of plant with >=2m
		Collection plants = new ArrayList<FiPlant> ();
		for (Object o : c) {
			if (!(o instanceof FiPlant)) {
				continue;
			}
			FiPlant plant = (FiPlant) o;
			if (plant.getHeight () >= 2.0) {
				if (plant.getSeverity () == null) { throw new Exception ("FiSeverityOV: no severity for plant "
						+ plant.getId ()); }
				plants.add (plant);
			}
		}
		return plants;
	}

	public void setProgramaticalyEnabled (boolean v) {
		super.setEnabled (v);
		AmapTools.setEnabled (this, v);
	}

	private void updateGraphs (Map<Integer,Integer> t, Map<Integer,Integer> tm) {
		// create one serial
		XYSeries tSeries = new XYSeries (Translator.swap ("FiSeverityOV.allTrees"));
		tSeries.setNotify (false);
		for (int h : t.keySet ()) {
			tSeries.add (h + 0.5, t.get (h));
		}
		XYSeries tmSeries = new XYSeries (Translator.swap ("FiSeverityOV.allKilledTrees"));
		tmSeries.setNotify (false);
		for (int h : tm.keySet ()) {
			// for (int i = 0; i < 15; i++) {
			tmSeries.add (h + 0.5, tm.get (h));
			// tmSeries.add(i, i * i);
		}
		// create the dataset
		XYSeriesCollection dataSet = new XYSeriesCollection ();
		// add serials to dataset
		dataSet.addSeries (tSeries);
		dataSet.addSeries (tmSeries);
		// create the graph
		String title = Translator.swap ("FiSeverityOV.treeMortalityChartTitle");
		String XTitle = Translator.swap ("FiSeverityOV.treeMortalityChartXTitle");
		String YTitle = Translator.swap ("FiSeverityOV.treeMortalityChartYTitle");
		// JFreeChart tmChart = ChartFactory.createXYLineChart(title, XTitle,

		JFreeChart tmChart = ChartFactory.createXYLineChart (title, XTitle, YTitle, dataSet, PlotOrientation.VERTICAL,
				true, // Legend
				false, false);

		XYPlot plot = (XYPlot) tmChart.getPlot ();

		
		
		
		// fc-16.11.2015 font sizes can be tuned
		float bigFont = 18;
		float mediumFont = 16;
		float smallFont = 14;
		
		bigFont = 28;
		mediumFont = 24;
		smallFont = 24;
		
		// fc-16.11.2015 bigger fonts
		Font f0 = tmChart.getTitle().getFont().deriveFont(bigFont);
		tmChart.getTitle().setFont(f0);

		
		Axis domainAxis = plot.getDomainAxis();
		Font f1 = domainAxis.getTickLabelFont().deriveFont(mediumFont);
		domainAxis.setTickLabelFont(f1);
		
		Font f2 = domainAxis.getLabelFont().deriveFont(mediumFont);
		domainAxis.setLabelFont(f2);
		
		Axis rangeAxis = plot.getRangeAxis();
		Font f10 = rangeAxis.getTickLabelFont().deriveFont(mediumFont);
		rangeAxis.setTickLabelFont(f10);
		
		Font f12 = rangeAxis.getLabelFont().deriveFont(mediumFont);
		rangeAxis.setLabelFont(f12);
		
		LegendTitle legend = tmChart.getLegend();
		Font f4 = legend.getItemFont().deriveFont(smallFont);
		legend.setItemFont(f4);
		legend.setFrame(BlockBorder.NONE);
		legend.setPosition(RectangleEdge.TOP);
		// fc-16.11.2015
		
		float lineWidth = 2.0f;
		BasicStroke bs = new BasicStroke(lineWidth); 
		
		// Definition of serial's color
		plot.getRenderer ().setSeriesPaint (0, Color.blue);
		plot.getRenderer ().setSeriesPaint (1, Color.red);
		plot.getRenderer().setSeriesStroke(0, bs);
		plot.getRenderer().setSeriesStroke(1, bs);
		// plot.getRenderer ().setSeriesPaint (1,Color.green);

		// Definition of back color of graph
		plot.setBackgroundPaint (Color.white);

		// format of the legend
//		LegendTitle legend = new LegendTitle (tmChart.getPlot ());
		

		// rangeAxis = (NumberAxis) plot.getRangeAxis();

		// Fix the range for YAxis
		// rangeAxis.setUpperBound(35.0);

		if (treeMortalityChartPanel != null) {
			treeMortalityChartPanel.setChart (tmChart);
		} else {
			treeMortalityChartPanel = new ChartPanel (tmChart, true);
			treeMortalityGraph.add (treeMortalityChartPanel, BorderLayout.CENTER); // fc-16.11.2015
//			treeMortalityGraph.add (treeMortalityChartPanel); // fc
		}

		// Dimensions of graph

		// Do not set any size in ObjectViewers
		// ~ Dimension d = new Dimension(400, 400);
		// ~ treeMortalityChartPanel.setPreferredSize (d);

		/*
		 * title = Translator.swap("FiSeverityOV.treeMortalityChartTitle");
		 * XTitle = Translator.swap("FiSeverityOV.treeMortalityChartXTitle");
		 * YTitle = Translator.swap("FiSeverityOV.treeMortalityChartXTitle");
		 * JFreeChart cmChart = ChartFactory.createXYLineChart(title, XTitle,
		 * YTitle, dataSet, PlotOrientation.VERTICAL, false, // Legend false,
		 * false);
		 * 
		 * plot = (XYPlot) tmChart.getPlot();
		 * 
		 * // Definition of serial's color plot.getRenderer().setSeriesPaint(0,
		 * Color.blue); // plot.getRenderer ().setSeriesPaint (1,Color.red); //
		 * plot.getRenderer ().setSeriesPaint (1,Color.green);
		 * 
		 * // Definition of back color of graph
		 * plot.setBackgroundPaint(Color.white);
		 * 
		 * // format of the legend legend = new LegendTitle(cmChart.getPlot());
		 * 
		 * // rangeAxis = (NumberAxis) plot.getRangeAxis();
		 * 
		 * // Fix the range for YAxis // rangeAxis.setUpperBound(35.0);
		 * 
		 * if (cambiumMortalityChartPanel != null) {
		 * cambiumMortalityChartPanel.setChart(cmChart); } else {
		 * cambiumMortalityChartPanel = new ChartPanel(cmChart, true);
		 * treeMortalityGraph.add (cambiumMortalityChartPanel); // fcfcfc }
		 * 
		 * // Dimensions of graph cambiumMortalityChartPanel.setPreferredSize
		 * (d);
		 */
	}

}
