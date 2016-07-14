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

package capsis.extension.modeltool.castaneaclimateviewer;

//import nz1.model.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

import capsis.commongui.ProjectFileAccessory;
import capsis.commongui.util.Helper;
import capsis.extension.DialogModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.AbstractSettings;
import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.lib.castanea.FmClimate;
import capsis.lib.castanea.FmClimateDay;
import capsis.lib.castanea.FmClimateReader;
import capsis.lib.castanea.FmSettings;


/**	A Wood Quality Workshop.
*	@author D. Pont - december 2005
*/
public class CastaneaClimateViewer extends DialogModelTool implements ActionListener, ItemListener {
	
	static public final String AUTHOR="F. de Coligny, H. Davi";
	static public final String VERSION="1.1.2";
	
	
	static {
		Translator.addBundle("capsis.extension.modeltool.castaneaclimateviewer.CastaneaClimateViewer");
	}

	private static final int INITIAL_WIDTH = 700;
	private static final int INITIAL_HEIGHT = 600;

	private Step step;
	private FmSettings sets;
	private FmClimate climate;

	private JTextField fileName;	// climate file
	private JButton browse;		// browse climate file
	//~ private JScrollPane scroll;
	//~ private JList list;
	private String currentGraphName;

	private JComboBox graphList;
	private JComboBox timeStep;
	private JComboBox variableName;
	private JTextField dateMin0;
	private JTextField dateMin1;
	private JTextField dateMax0;
	private JTextField dateMax1;

	private double latitude;
	private double longitude;

	private JButton addGraph;

	private JTabbedPane tabs;
	private JButton closeGraph;

	private JButton close;	// after confirmation
	private JButton help;


	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public CastaneaClimateViewer () {		
		super();
	}
	
	@Override
	public void init(GModel m, Step s){

		try {
			step = s;

			sets = (FmSettings) step.getProject ().getModel ().getSettings ();
			latitude = sets.latitude;
			longitude = sets.longitude;
			
			setTitle (Translator.swap ("CastaneaClimateViewer"));
			createUI ();

			setSize (INITIAL_WIDTH, INITIAL_HEIGHT);
			//pack ();	// sets the size
			setVisible (true);
			setModal (false);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "CastaneaClimateViewer.c ()", e.toString (), e);
		}

	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {
		try {
			GModel m = (GModel) referent;
			AbstractSettings s = m.getSettings ();
			if (s instanceof FmSettings) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "CastaneaClimateViewer.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}



	/**	Called when selection changed in table
	*/
	public void itemStateChanged (ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if (evt.getSource ().equals (graphList)) {
				graphListAction ();
			} else if (evt.getSource ().equals (timeStep)) {
				timeStepAction ();
			}
		}
	}

	private void browseAction() {
		JFileChooser chooser = new JFileChooser (
				Settings.getProperty ("castanea.climate.viewer.inventory.path", (String)null));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		//chooser.setFileSelectionMode ();
		int returnVal = chooser.showOpenDialog (this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String n = chooser.getSelectedFile ().toString ();
			Settings.setProperty ("castanea.climate.viewer.inventory.path", n);
			fileName.setText (n);

			try {
				FmClimateReader reader = new FmClimateReader (n);
				reader.interpret (sets, latitude);

				climate = reader.getClimate ();
				GraphFactory.initMap (climate);
				updateGraphList ();

				//~ updateCombo (littleFileName);
			} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.wrongClimateFileName"));
				return;
			}


		}
	}

	/**	Add a graph
	*/
	public void addGraphAction () {
		// Checks
		if (currentGraphName == null) {
			MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.pleaseSelectAGraphInTheList"));
			return;
		}

		int yyyyMin = -1;
		int dddMin = -1;
		int yyyyMax = -1;
		int dddMax = -1;

		// dateMin / max / 0 / 1 : if one is set, they must all be integers
		if (dateMin0.getText ().trim ().length () != 0
				|| dateMin1.getText ().trim ().length () != 0
				|| dateMax0.getText ().trim ().length () != 0
				|| dateMax1.getText ().trim ().length () != 0) {

			String dMin0 = dateMin0.getText ().trim ();
			if (!Check.isInt (dMin0)) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMin0MustBeAnIntegerWithFormatYYYY"));
				return;
			}
			String dMin1 = dateMin1.getText ().trim ();
			if (!Check.isInt (dMin1)) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMin1MustBeAnIntegerWithFormatDDDInTheRange0-366"));
				return;
			}
			String dMax0 = dateMax0.getText ().trim ();
			if (!Check.isInt (dMax0)) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMax0MustBeAnIntegerWithFormatYYYY"));
				return;
			}
			String dMax1 = dateMax1.getText ().trim ();
			if (!Check.isInt (dMax1)) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMax1MustBeAnIntegerWithFormatDDDInTheRange0-366"));
				return;
			}

			yyyyMin = Check.intValue (dMin0);
			dddMin = Check.intValue (dMin1);
			yyyyMax = Check.intValue (dMax0);
			dddMax = Check.intValue (dMax1);

			if (dddMin <= 0 || dddMin > 366) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMin1MustBeAnIntegerWithFormatDDDInTheRange0-366"));
				return;
			}
			if (dddMax <= 0 || dddMax > 366) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMax1MustBeAnIntegerWithFormatDDDInTheRange0-366"));
				return;
			}
			int n = (yyyyMax - yyyyMin) * 365 + dddMax - dddMin;
			if (n <= 0) {
				MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.dateMaxMustBeAfterDateMin"));
				return;
			}
		}

		// if min / max dates not set, process from file begin to its end
		if (yyyyMin == -1 || dddMin == -1 || yyyyMax == -1 || dddMax == -1) {
			FmClimateDay day = climate.getDay (0);
			yyyyMin = day.getYear ();
			dddMin = day.getDay ();
			day = climate.getDay (climate.getDays ().length - 1);
			yyyyMax = day.getYear ();
			dddMax = day.getDay ();
		}

		String ts = (String) timeStep.getSelectedItem ();
		String vn = (String) variableName.getSelectedItem ();

		//~ double latitudeValue = 0;
		//~ double longitudeValue = 0;
		// Latitude and longitude are needed if hourly time step
		//~ if (ts.equals (Graph.HOURLY)) {
			//~ String lat = latitude.getText ().trim ();
			//~ if (!Check.isDouble (lat)) {
				//~ MessageDialog.promptError (Translator.swap ("CastaneaClimateViewer.latitudeMustBeADoubleInDegrees"));
				//~ return;
			//~ }
			//~ latitudeValue = Check.doubleValue (lat);
			//~ String lon = longitude.getText ().trim ();
			//~ if (!Check.isDouble (lon)) {
				//~ MessageDialog.promptError (Translator.swap ("CastaneaClimateViewer.longitudeMustBeADoubleInDegrees"));
				//~ return;
			//~ }
			//~ longitudeValue = Check.doubleValue (lon);
		//~ }

			StringBuffer b = new StringBuffer ();
			b.append (climate.getLittleFileName ());
			b.append (" ");
			b.append (ts);
			b.append (" ");
			b.append (dddMin);
			b.append ('-');
			b.append (yyyyMin);
			b.append (" ");
			b.append (dddMax);
			b.append ('-');
			b.append (yyyyMax);

		try {
			Graph g = GraphFactory.getGraph (currentGraphName, vn, ts, yyyyMin, dddMin, yyyyMax, dddMax,
					latitude, longitude);
			showGraph (g, b.toString ());

		} catch (Exception e) {
			Log.println (Log.ERROR, "CastaneaClimateViewer.addGraphAction ()", "Graph creation error", e);
			MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.couldNotCreateTheGraphSeeLog"));
			return;

		}
	}

	/**
	*/
	public void showGraph (Graph g, String title) {
		try {

			//~ String title = g.getName ();
			String XTitle = g.getXName ();
			String YTitle = g.getYName ();
			JFreeChart chart = ChartFactory.createXYLineChart (title, XTitle, YTitle,
					g.getData (),
					PlotOrientation.VERTICAL,
					false,		// Legend
					false,
					false);

			XYPlot plot = (XYPlot) chart.getPlot();

			// Definition of serial's color
			plot.getRenderer ().setSeriesPaint (0, Color.BLUE);
			//~ plot.getRenderer ().setSeriesPaint (1, Color.red);
			//~ plot.getRenderer ().setSeriesPaint (1, Color.green);

			// Definition of back color of graph
			plot.setBackgroundPaint (Color.WHITE);

			// format of the legend
			LegendTitle legend = new LegendTitle (chart.getPlot());

			ChartPanel chartPanel = new ChartPanel (chart, true);

			//~ tabs.addTab (g.getName (), chartPanel);
			tabs.insertTab (g.getName (), null, chartPanel, null, 0);	// icon = null, tip = null, index = 0
			tabs.setSelectedIndex (0);
		} catch (Exception e) {
			Log.println (Log.ERROR, "CastaneaClimateViewer.showGraph ()", "Graph showing error", e);
			MessageDialog.print (this, Translator.swap ("CastaneaClimateViewer.couldNotShowTheGraphSeeLog"));
			return;

		}

	}

	/**	Update the list
	*/
	public void updateGraphList () {
		currentGraphName = null;
		Collection<String> names = GraphFactory.getGraphNames ();

		graphList.removeAllItems ();
		for (String n : names) {
			graphList.addItem (n);
		}

		// select first entry
		try {
			graphList.setSelectedIndex (0);
		} catch (Exception e) {}

		//~ list = new JList (new Vector (names));
		//~ scroll.getViewport ().setView (list);
		//~ list.addListSelectionListener (this);

		// Set the file extremes in the textfields
			FmClimateDay day = climate.getDay (0);
			dateMin0.setText (""+day.getYear ());
			dateMin1.setText (""+day.getDay ());
			day = climate.getDay (climate.getDays ().length - 1);
			dateMax0.setText (""+day.getYear ());
			dateMax1.setText (""+day.getDay ());

	}

	/**	graphList combo changed values: update other combos
	*/
	public void graphListAction () {
		currentGraphName = (String) graphList.getSelectedItem ();
System.out.println ("selection: "+currentGraphName);

		try {
			// we need a graph instance just to ask it its compatible timeSteps
			Graph g = GraphFactory.getGraph (currentGraphName, "", "", 0, 0, 0, 0, 0, 0);
			Collection<String> ts = g.getTimeSteps ();

			// Update the timeStep combo
			timeStep.removeAllItems ();
			for (String s : ts) {
				timeStep.addItem (s);
			}
			// select first entry
			try {
				timeStep.setSelectedIndex (0);
			} catch (Exception e) {}
		} catch (Exception e) {}
	}

	/**	timeStep combo changed values: update variableName combo values.
	*/
	public void timeStepAction () {
		variableName.removeAllItems ();

		String v = (String) timeStep.getSelectedItem ();
		if (v.equals (Graph.YEARLY)) {
			Collection<String> variableNames = FmClimateDay.dailyVariableNames;
			for (String vn : variableNames) {
				variableName.addItem (vn);
			}

		} else if (v.equals (Graph.MONTHLY)) {

		} else if (v.equals (Graph.DAILY)) {
			Collection<String> variableNames = FmClimateDay.dailyVariableNames;
			for (String vn : variableNames) {
				variableName.addItem (vn);
			}

		} else if (v.equals (Graph.HOURLY)) {
			Collection<String> variableNames = FmClimateDay.hourlyVariableNames;
			for (String vn : variableNames) {
				variableName.addItem (vn);
			}

		}
	}

	/**	Close the graph in the current tab
	*/
	public void closeGraphAction () {
		try {
			tabs.removeTabAt (tabs.getSelectedIndex ());
		} catch (Exception e) {
			// may happen if no graph to close
		}
	}

	/**	From ActionListener interface.
	*	Buttons management.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (browse)) {
			browseAction ();
		} else if (evt.getSource ().equals (graphList)) {
			graphListAction ();
		} else if (evt.getSource ().equals (timeStep)) {
			timeStepAction ();
		} else if (evt.getSource ().equals (close)) {
			escapePressed ();
		} else if (evt.getSource ().equals (addGraph)) {
			addGraphAction ();
		} else if (evt.getSource ().equals (closeGraph)) {
			closeGraphAction ();

		//~ // update the object viewer
		//~ } else if (evt.getSource ().equals (viewerCombo)) {
			//~ boolean ovChanged = true;
			//~ updateViewer (ovChanged);	// dispose currentOV and load the new one

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Called on Escape. Redefinition of method in AmapDialog : ask for user confirmation.
	*/
	protected void escapePressed () {
		if (Question.ask (MainFrame.getInstance (),
				Translator.swap ("CastaneaClimateViewer.confirm"), Translator.swap ("CastaneaClimateViewer.confirmClose"))) {
			dispose ();
		}
	}

	/**	User interface definition
	*/
	private void createUI () {

		// part1 contains the grouperChooser and the table
		LinePanel part0 = new LinePanel ();

		part0.add (new JLabel (Translator.swap ("CastaneaClimateViewer.climateFileName")+" : "));
		fileName = new JTextField ();
		part0.add (fileName);
		browse = new JButton (Translator.swap ("CastaneaClimateViewer.browse"));
		browse.addActionListener (this);
		part0.add (browse);
		part0.addStrut0 ();

		// left contains the list
		LinePanel left = new LinePanel ();

		//~ ColumnPanel c1 = new ColumnPanel ();
		//~ scroll = new JScrollPane (new JList ());
		//~ c1.add (new JScrollPane (scroll));
		//~ c1.addStrut0 ();

		//~ left.add (c1);

		ColumnPanel c2 = new ColumnPanel ();


		graphList = new JComboBox ();
		graphList.addActionListener (this);
		graphList.addItemListener (this);
		c2.add (graphList);


		//~ Vector v = new Vector ();
		//~ v.add (Graph.YEARLY);
		//~ v.add (Graph.MONTHLY);
		//~ v.add (Graph.DAILY);
		//~ v.add (Graph.HOURLY);
		timeStep = new JComboBox ();
		timeStep.addActionListener (this);
		timeStep.addItemListener (this);
		c2.add (timeStep);

		//~ v = new Vector (FmClimateDay.dailyVariableNames);
		variableName = new JComboBox ();
		c2.add (variableName);

		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("CastaneaClimateViewer.dateMin")+" : ", 90));
		dateMin0 = new JTextField (6);
		l0.add (dateMin0);
		l0.add (new JLabel (" / "));
		dateMin1 = new JTextField (4);
		l0.add (dateMin1);
		l0.addStrut0 ();
		c2.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("CastaneaClimateViewer.dateMax")+" : ", 90));
		dateMax0 = new JTextField (6);
		l1.add (dateMax0);
		l1.add (new JLabel (" / "));
		dateMax1 = new JTextField (4);
		l1.add (dateMax1);
		l1.addStrut0 ();
		c2.add (l1);

		//~ LinePanel l4 = new LinePanel ();
		//~ l4.add (new JWidthLabel (Translator.swap ("CastaneaClimateViewer.latitude")+" : ", 80));
		//~ latitude = new JTextField (5);
		//~ latitude.setText (""+45);		// correct this default latitude
		//~ l4.add (latitude);
		//~ l4.addStrut0 ();
		//~ c2.add (l4);

		//~ LinePanel l5 = new LinePanel ();
		//~ l5.add (new JWidthLabel (Translator.swap ("CastaneaClimateViewer.longitude")+" : ", 80));
		//~ longitude = new JTextField (5);
		//~ l5.add (longitude);
		//~ longitude.setText (""+12);		// correct this default longitude
		//~ l5.addStrut0 ();
		//~ c2.add (l5);

		LinePanel l2 = new LinePanel ();
		l2.addGlue ();
		addGraph = new JButton (Translator.swap ("CastaneaClimateViewer.addGraph"));
		addGraph.addActionListener (this);
		l2.add (addGraph);
		l2.addStrut0 ();
		c2.add (l2);

		c2.addGlue ();

		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (c2, BorderLayout.NORTH);
		left.add (aux);

		// right contains the graphs
		LinePanel right = new LinePanel ();

		JPanel aux2 = new JPanel (new BorderLayout ());

		tabs = new JTabbedPane (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		aux2.add (tabs, BorderLayout.CENTER);

		closeGraph = new JButton (Translator.swap ("CastaneaClimateViewer.closeGraph"));
		closeGraph.addActionListener (this);
		aux2.add (closeGraph, BorderLayout.SOUTH);

		right.add (aux2);

		// Control panel at the bottom: Close / Help
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		close = new JButton (Translator.swap ("Shared.close"));
		close.addActionListener (this);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (close);
		pControl.add (help);
		// set close as default (see AmapDialog)
		//~ close.setDefaultCapable (true);
		//~ getRootPane ().setDefaultButton (close);

		// layout parts
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent (left);
		split.setRightComponent (right);
		split.setResizeWeight (0.5);
		split.setOneTouchExpandable (true);
		split.setDividerLocation (INITIAL_WIDTH/2);

		getContentPane ().add (part0, BorderLayout.NORTH);
		getContentPane ().add (split, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);
	}


}


