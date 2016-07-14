/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003 Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTable;

import jeeb.lib.util.ColoredPanel;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.HatchedPaint;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.RGBManager;
import jeeb.lib.util.RGBManagerClasses;
import jeeb.lib.util.RGBManagerGradient;
import jeeb.lib.util.RGBManagerValues;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import samsara2.model.Samsa2DecayedTree;
import samsara2.model.Samsa2Stand;
import samsara2.model.Samsa2Tree;
import capsis.commongui.projectmanager.StepButton;
import capsis.defaulttype.RectangularPlot;
import capsis.defaulttype.SquareCell;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.Cell;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Plot;
import capsis.kernel.Step;
import capsis.util.TreeHeightComparator;
import capsis.util.methodprovider.SVSamCell;
import capsis.util.methodprovider.SVSamSapling;
import capsis.util.methodprovider.SVSamSettings;
import capsis.util.methodprovider.SVSamTree;

/**
 * SVSamsara is a cartography simple viewer for trees with coordinates. It draws
 * the trees within the cells. It's based on SVSimple. Compatibility: the models
 * are compatible if their settings object implements the SVSamSettings
 * interface, their tree description implements SVSamTree and their cell
 * description implements SVSamCell. The stand must be a TreeList and the plot a
 * RectangularPlot. The interfaces upper are located in
 * capsis.util.methodprovider.
 * 
 * @author B. Courbaud - july 1999
 */
public class SVSamsara extends SVSimple {

	// Reviewed by fc - october 2010 - RGBManagers

	static public String AUTHOR = "B. Courbaud";
	static public String VERSION = "2.0";

	static {
		Translator.addBundle("capsis.extension.standviewer.SVSamsara");
	}

	public static final int MARGIN = 20;

	// Radiation colors
	public static final Color LIGHT1 = new Color(0, 0, 0); // Color.BLACK;
	public static final Color LIGHT2 = new Color(100, 100, 100);
	public static final Color LIGHT3 = new Color(150, 150, 150);
	public static final Color LIGHT4 = new Color(200, 200, 200);
	public static final Color LIGHT5 = new Color(255, 255, 255); // Color.WHITE;

	// Filled crown colors
	public static final Color COLORSP0L1 = new Color(0, 128, 0); // species 0
																	// highest
																	// layer
	public static final Color COLORSP0L2 = new Color(0, 170, 0);
	public static final Color COLORSP0L3 = new Color(0, 213, 0);
	public static final Color COLORSP0L4 = new Color(0, 255, 0); // species 0
																	// lowest
																	// layer
	public static final Color COLORSP1L1 = new Color(0, 0, 128);
	public static final Color COLORSP1L2 = new Color(0, 0, 170);
	public static final Color COLORSP1L3 = new Color(0, 0, 213);
	public static final Color COLORSP1L4 = new Color(0, 0, 255);
	public static final Color COLORSP2L1 = new Color(128, 0, 0);
	public static final Color COLORSP2L2 = new Color(170, 0, 0);
	public static final Color COLORSP2L3 = new Color(213, 0, 0);
	public static final Color COLORSP2L4 = new Color(255, 0, 0);

	public static final Color BLUEBERRY_COLOR = new Color(0, 0, 128); // dark
																		// blue

	// Transparent crown colors
	private Color colorT0; // = new Color (0, 170, 0, alpha);
	private Color colorT1; // = new Color (0, 0, 170, alpha);
	private Color colorT2; // = new Color (170, 0, 0, alpha);

	private int speciesNumber;
	private JCheckBox ckCellLines;
	private JCheckBox ckAscendingSort;
	private ButtonGroup rdGroup1;
	private JRadioButton rdCrownNone;
	private JRadioButton rdCrownOutlined;
	private JRadioButton rdCrownFilled;
	private JRadioButton rdCrownTransparent;

	private JCheckBox grayScale; // fc-28.4.2014

	private ButtonGroup cellColorGroup;
	private JRadioButton showLight;
	private JRadioButton showRegeneration;
	private JCheckBox showSaplings; // fc-7.11.2013
	private JCheckBox showDMH; // fc-30.10.2014 Dendro micro habitats
	private JCheckBox showRecentDead; // fc-30.10.2014
	private JSlider alphaSlider;
	private JCheckBox ckShowLegend;
	private Font tableFont = new JTable().getFont();

	private RGBManagerValues<SVSamCell> regeRGBManager;
	private RGBManagerClasses<SVSamCell> lightRGBManager;
	private RGBManagerGradient<SVSamCell> blueberryRGBManager;

	private Set<String> propKeys;
	private Map<ButtonModel, RGBManager> cellRGBMap;

	private static Map<String, int[]> propKeysRGBs;
	static {
		propKeysRGBs = new HashMap<String, int[]>();
		propKeysRGBs.put("Herbs", new int[] { 0, 128, 0 }); // dark green
		propKeysRGBs.put("Blueberry", new int[] { 0, 0, 128 }); // dark blue
		propKeysRGBs.put("Raspberry", new int[] { 128, 0, 0 }); // dark red
		propKeysRGBs.put("GrouseIndex", new int[] { 128, 64, 0 }); // dark brown
	}

	/** Init function */
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init(model, s, but);

		SVSamSettings set = (SVSamSettings) stand.getStep().getProject().getModel().getSettings();
		speciesNumber = set.getSpeciesNumber();

		SVSamCell c = (SVSamCell) s.getScene().getPlot().getFirstCell();
		propKeys = c.getPropKeys();

		// A color manager for the regeneration on Samsara ground cells
		regeRGBManager = new RGBManagerValues<SVSamCell>(Translator.swap ("SVSamsara.regenerationColors")) {
//		regeRGBManager = new RGBManagerValues<SVSamCell>("Regeneration") {

			/**
			 * Add the values with their associated color and caption with
			 * addValue ().
			 */
			@Override
			public void init() {
				addValue(RGBManager.toRGB(Color.YELLOW), Translator.swap("SVSamsara.regeneration"), 1);
				addValue(RGBManager.toRGB(Color.WHITE), Translator.swap("SVSamsara.noRegeneration"), 0);
			}

			/**
			 * Returns 1 if there is regeneration on the cell or 0 if not.
			 */
			@Override
			public double getValue(SVSamCell e) {
				SVSamSettings set = (SVSamSettings) e.getPlot().getScene().getStep().getProject().getModel()
						.getSettings();
				int speciesNumber = set.getSpeciesNumber();

				for (int k = 0; k < speciesNumber; k++) {
					if (e.getSaplingNb(k) > 0) {
						return 1;
					}
				}
				return 0;
			}

		};

		// A color manager for the light on Samsara ground cells
		lightRGBManager = new RGBManagerClasses<SVSamCell>(Translator.swap ("SVSamsara.lightColors")) {
//		lightRGBManager = new RGBManagerClasses<SVSamCell>("Light") {

			/**
			 * Add the classes with their associated color and caption with
			 * addClass ().
			 */
			@Override
			public void init() {
				addClass(RGBManager.toRGB(LIGHT1), Translator.swap("SVSamsara.light1"), Double.MIN_VALUE, 6.25);
				addClass(RGBManager.toRGB(LIGHT2), Translator.swap("SVSamsara.light2"), 6.25, 12.5);
				addClass(RGBManager.toRGB(LIGHT3), Translator.swap("SVSamsara.light3"), 12.5, 25);
				addClass(RGBManager.toRGB(LIGHT4), Translator.swap("SVSamsara.light4"), 25, 50);
				addClass(RGBManager.toRGB(LIGHT5), Translator.swap("SVSamsara.light5"), 50, Double.MAX_VALUE);
			}

			/**
			 * Returns the amount of light on the given cell.
			 */
			@Override
			public double getValue(SVSamCell e) {
				return e.getRelativeHorizontalEnergy();
			}

		};

		// Create the option panel
		try {
			optionPanel = new ColumnPanel();

			// General options
			ckCellLines = new JCheckBox(Translator.swap("SVSamsara.cellLines"),
					((SVSamsaraSettings) settings).cellLines);

			LinePanel l1 = new LinePanel();
			l1.add(ckCellLines);
			l1.addGlue();
			optionPanel.add(l1);

			ckAscendingSort = new JCheckBox(Translator.swap("SVSamsara.ascendingSort"),
					((SVSamsaraSettings) settings).ascendingSort);

			LinePanel l2 = new LinePanel();
			l2.add(ckAscendingSort);
			l2.addGlue();
			optionPanel.add(l2);

			// ////////////////////////

			LinePanel l99 = new LinePanel();
			grayScale = new JCheckBox(Translator.swap("SVSamsara.grayScale"));
			grayScale.setSelected(Settings.getProperty("SVSamsara.grayScale", false));
			l99.add(grayScale);
			l99.addGlue();
			optionPanel.add(l99);

			// fc-7.11.2013
			showSaplings = new JCheckBox(Translator.swap("SVSamsara.showSaplings"));
			showSaplings.setSelected(Settings.getProperty("SVSamsara.showSaplings", true));
			LinePanel l17 = new LinePanel();
			l17.add(showSaplings);
			l17.addGlue();
			optionPanel.add(l17);

			// fc-30.10.2014
			showDMH = new JCheckBox(Translator.swap("SVSamsara.showDMH"));
			showDMH.setSelected(Settings.getProperty("SVSamsara.showDMH", true));
			LinePanel l18a = new LinePanel();
			l18a.add(showDMH);
			l18a.addGlue();
			optionPanel.add(l18a);

			// fc-30.10.2014
			showRecentDead = new JCheckBox(Translator.swap("SVSamsara.showRecentDead"));
			showRecentDead.setSelected(Settings.getProperty("SVSamsara.showRecentDead", true));
			LinePanel l18 = new LinePanel();
			l18.add(showRecentDead);
			l18.addGlue();
			optionPanel.add(l18);

			// //////////////////////

			// Crowns
			ColumnPanel crownPanel = new ColumnPanel(Translator.swap("SVSamsara.crown"));

			rdCrownNone = new JRadioButton(Translator.swap("SVSamsara.rdCrownNone"));
			LinePanel l10 = new LinePanel();
			l10.add(rdCrownNone);
			l10.addGlue();
			crownPanel.add(l10);

			rdCrownOutlined = new JRadioButton(Translator.swap("SVSamsara.rdCrownOutlined"));
			LinePanel l11 = new LinePanel();
			l11.add(rdCrownOutlined);
			// grayScale = new JCheckBox (Translator.swap
			// ("SVSamsara.grayScale"));
			// grayScale.setSelected (Settings.getProperty
			// ("SVSamsara.grayScale", false));
			// l11.add (grayScale);
			l11.addGlue();
			crownPanel.add(l11);

			rdCrownFilled = new JRadioButton(Translator.swap("SVSamsara.rdCrownFilled"));
			LinePanel l12 = new LinePanel();
			l12.add(rdCrownFilled);
			l12.addGlue();
			crownPanel.add(l12);

			rdCrownTransparent = new JRadioButton(Translator.swap("SVSamsara.rdCrownTransparent"));
			LinePanel l4 = new LinePanel();
			l4.add(rdCrownTransparent);
			alphaSlider = new JSlider(0, 255, ((SVSamsaraSettings) settings).alphaValue);
			alphaSlider.setMaximumSize(new Dimension(150, alphaSlider.getPreferredSize().height));
			l4.add(alphaSlider);
			l4.addGlue();
			crownPanel.add(l4);

			crownPanel.addStrut0();
			optionPanel.add(crownPanel);

			rdGroup1 = new ButtonGroup();
			rdGroup1.add(rdCrownNone);
			rdGroup1.add(rdCrownOutlined);
			rdGroup1.add(rdCrownFilled);
			rdGroup1.add(rdCrownTransparent);
			switch (((SVSamsaraSettings) settings).crownView) {
			case SVSamsaraSettings.NONE:
				rdGroup1.setSelected(rdCrownNone.getModel(), true);
				break;
			case SVSamsaraSettings.OUTLINED:
				rdGroup1.setSelected(rdCrownOutlined.getModel(), true);
				break;
			case SVSamsaraSettings.FILLED:
				rdGroup1.setSelected(rdCrownFilled.getModel(), true);
				break;
			case SVSamsaraSettings.TRANSPARENT:
				rdGroup1.setSelected(rdCrownTransparent.getModel(), true);
				break;
			}

			// Cells
			ColumnPanel cellPanel = new ColumnPanel(Translator.swap("SVSamsara.cell"));

			showLight = new JRadioButton(Translator.swap("SVSamsara.showLight"));
			LinePanel l15 = new LinePanel();
			l15.add(showLight);
			l15.addGlue();
			cellPanel.add(l15);

			showRegeneration = new JRadioButton(Translator.swap("SVSamsara.showRegeneration"));
			LinePanel l16 = new LinePanel();
			l16.add(showRegeneration);
			l16.addGlue();
			cellPanel.add(l16);

			cellColorGroup = new ButtonGroup();
			cellColorGroup.add(showLight);
			cellColorGroup.add(showRegeneration);
			// cellColorGroup.add (showSaplings);

			cellRGBMap = new HashMap<ButtonModel, RGBManager>();
			cellRGBMap.put(showLight.getModel(), lightRGBManager);
			cellRGBMap.put(showRegeneration.getModel(), regeRGBManager);

			if (propKeys != null) {
				for (String k : propKeys) {

					JRadioButton b = new JRadioButton(Translator.swap(k));
					cellColorGroup.add(b);
					LinePanel l20 = new LinePanel();
					l20.add(b);
					l20.addGlue();
					cellPanel.add(l20);

					// A color manager for the property (e.g."BlueBerry") in the
					// Samsara ground
					// cells
					// prop map
					RGBManagerGradient myRGBManager = new RGBManagerGradient<SVSamCell>(k) {

						// Initialize the gradient with addClass ().
						public void init() {
							int[] rgb = propKeysRGBs.get(title);
							if (rgb == null) {
								rgb = RGBManager.toRGB(Color.ORANGE);
							} // default color
							setGradient(toRGB(Color.WHITE), rgb, 0, 10, 5);

						}

						// Returns the amount of the selected prop on the given
						// cell (e.g.
						// BlueBerry).
						public double getValue(SVSamCell e) {
							double v = e.getProp(title); // -1 or v in [0, 10]
							// If no prop in this cell, v == -1, return 0
							// instead
							return v == -1 ? 0 : v;
						}

					};

					cellRGBMap.put(b.getModel(), myRGBManager);

				}
			}

			// Select the same option than last time
			String cellColor = Settings.getProperty("SVSamsara.cellColor", showLight.getText()); // default
																									// value
																									// =
																									// showLight
			for (Enumeration e = cellColorGroup.getElements(); e.hasMoreElements();) {
				AbstractButton b = (AbstractButton) e.nextElement();
				if (b.getText().equals(cellColor)) { // "Light",
														// "Regeneration"...
					b.setSelected(true);
					break;
				}
			}
			if (cellColorGroup.getSelection() == null) {
				showLight.setSelected(true);
			} // needed (text changes with language)

			// if (cellColor.equals("showLight")) {
			// showLight.setSelected (true);
			// } else if (cellColor.equals("showRegeneration")) {
			// showRegeneration.setSelected (true);
			// }

			// Do not memo this option in SVSamsaraSettings
			// switch (((SVSamsaraSettings) settings).cellView) {
			// case SVSamsaraSettings.LIGHT :
			// cellColorGroup.setSelected (showLight.getModel (), true);
			// break;
			// case SVSamsaraSettings.REGENERATION :
			// cellColorGroup.setSelected (showRegeneration.getModel (), true);
			// break;
			// }

			cellPanel.addStrut0();
			optionPanel.add(cellPanel);

			// Legend
			ckShowLegend = new JCheckBox(Translator.swap("SVSamsara.showLegend"),
					((SVSamsaraSettings) settings).showLegend);

			LinePanel l25 = new LinePanel();
			l25.add(ckShowLegend);
			l25.addGlue();
			optionPanel.add(l25);

			((ColumnPanel) optionPanel).addGlue();

			updateLegend();
			revalidate();

		} catch (Exception e) {
			Log.println(Log.ERROR, "SVSamsara.init ()", "Exception", e);
			throw e; // propagate
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {

		try {
			if (!SVSimple.matchWith(referent)) {
				return false;
			}
			GModel m = (GModel) referent;

			GScene stand = ((Step) m.getProject().getRoot()).getScene();
			Plot plot = stand.getPlot();
			if (!(stand instanceof TreeList)) {
				return false;
			}
			if (!(plot instanceof RectangularPlot)) {
				return false;
			}

			Collection cells = ((RectangularPlot) plot).getCells();
			if (cells == null || cells.isEmpty()) {
				return false;
			}
			Cell c1 = (Cell) cells.iterator().next();
			if (!(c1 instanceof SquareCell)) {
				return false;
			}
			if (!(c1 instanceof SVSamCell)) {
				return false;
			}

			Collection trees = ((TreeList) stand).getTrees();
			if (!trees.isEmpty()) { // if bare soil, we do not return false
				Tree t1 = (Tree) trees.iterator().next();
				if (!(t1 instanceof SVSamTree)) {
					return false;
				}
			}
			return true;

		} catch (Exception e) {
			Log.println(Log.ERROR, "SVSamsara.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	protected void retrieveSettings() {

		settings = new SVSamsaraSettings();

	}

	/**
	 * Processes options once the option panel edited and closed.
	 */
	protected void optionAction() {
		super.optionAction();

		((SVSamsaraSettings) settings).cellLines = ckCellLines.isSelected();
		((SVSamsaraSettings) settings).ascendingSort = ckAscendingSort.isSelected();
		((SVSamsaraSettings) settings).alphaValue = alphaSlider.getValue();

		int opt = 0;
		if (rdGroup1.getSelection().equals(rdCrownNone.getModel())) {
			opt = SVSamsaraSettings.NONE;
		} else if (rdGroup1.getSelection().equals(rdCrownOutlined.getModel())) {
			opt = SVSamsaraSettings.OUTLINED;
		} else if (rdGroup1.getSelection().equals(rdCrownFilled.getModel())) {
			opt = SVSamsaraSettings.FILLED;
		} else if (rdGroup1.getSelection().equals(rdCrownTransparent.getModel())) {
			opt = SVSamsaraSettings.TRANSPARENT;
		}
		((SVSamsaraSettings) settings).crownView = opt;

		// Memo the cellColor option: text of the selected radiobutton
		for (Enumeration e = cellColorGroup.getElements(); e.hasMoreElements();) {
			AbstractButton b = (AbstractButton) e.nextElement();
			if (b.isSelected()) {
				Settings.setProperty("SVSamsara.cellColor", b.getText()); // "Light",
																			// "Regeneration",
																			// "BlueBerry"...
			}
		}

		// if (showLight.isSelected ()) {
		// Settings.setProperty("SVSamsara.cellColor", "showLight");
		// } else if (showRegeneration.isSelected ()) {
		// Settings.setProperty("SVSamsara.cellColor", "showRegeneration");
		// }

		// Do not memo this option in SVSamsaraSettings
		// opt = 0;
		// if (cellColorGroup.getSelection ().equals (showLight.getModel ())) {
		// opt = SVSamsaraSettings.LIGHT;
		// } else if (cellColorGroup.getSelection ().equals
		// (showRegeneration.getModel ())) {
		// opt = SVSamsaraSettings.REGENERATION;
		// }
		// ((SVSamsaraSettings) settings).cellView = opt;

		((SVSamsaraSettings) settings).showLegend = ckShowLegend.isSelected();

		Settings.setProperty("SVSamsara.grayScale", grayScale.isSelected());
		Settings.setProperty("SVSamsara.showSaplings", showSaplings.isSelected());
		Settings.setProperty("SVSamsara.showDMH", showDMH.isSelected());
		Settings.setProperty("SVSamsara.showRecentDead", showRecentDead.isSelected());

		ExtensionManager.recordSettings(this);

		updateLegend();

	}

	/**
	 * Optional, do something before the trees are drawn.
	 */
	public Object[] preProcessTrees(Object[] trees, Rectangle.Double r) {

		// Sort the trees in height order
		if (((SVSamsaraSettings) settings).ascendingSort) {
			Arrays.sort(trees, new TreeHeightComparator());
		}

		// Prepare colors for drawTree ()
		int crownOption = ((SVSamsaraSettings) settings).crownView;

		if (crownOption == SVSamsaraSettings.TRANSPARENT) {
			int alpha = ((SVSamsaraSettings) settings).alphaValue;
			colorT0 = new Color(0, 170, 0, alpha);
			colorT1 = new Color(0, 0, 170, alpha);
			colorT2 = new Color(170, 0, 0, alpha);
		}
		return trees;
	}

	/**
	 * Method to draw a GCell within this viewer.
	 */
	public void drawCell(Graphics2D g2, Cell gcell, Rectangle.Double r) {

		SVSamCell cell = (SVSamCell) gcell;

		ButtonModel selection = cellColorGroup.getSelection();
		RGBManager man = cellRGBMap.get(selection);
		if (man != null)
			cell.setRGB(man.getRGB(cell));

		// if (showLight.isSelected ()) {
		//
		// cell.setRGB (lightRGBManager.getRGB (cell));
		//
		// } else if (showRegeneration.isSelected ()) {
		//
		// cell.setRGB (regeRGBManager.getRGB (cell));
		//
		// }

		Shape sh = gcell.getShape();
		Rectangle2D bBox = sh.getBounds2D();
		if (r.intersects(bBox)) {
			// if (showSaplings.isSelected ()) { // fc-7.11.2013
			// g2.setColor (Color.WHITE); // Saplings mode: cell background
			// stays white
			// } else {
			if (showLight.isSelected() || showRegeneration.isSelected()) { // fc-7.11.2013
				int[] rgb = cell.getRGB();
				g2.setColor(new Color(rgb[0], rgb[1], rgb[2]));
			}
			g2.fill(sh);
		}

		// Draw cell lines if required
		if (((SVSamsaraSettings) settings).cellLines) {
			g2.setColor(getCellColor());
			if (r.intersects(bBox)) {
				g2.draw(sh);
			}
		}

		// fc-7.11.2013
		// Draw the saplings if any in the cell
		Collection saplings = cell.getSaplings();
		if (showSaplings.isSelected() && saplings != null && !saplings.isEmpty()) {
			for (Object o : saplings) {
				SVSamSapling sap = (SVSamSapling) o;
				drawSapling(g2, sap, r);
			}
		}

	}

	/**
	 * Method to draw a sapling within this viewer.
	 */
	public void drawSapling(Graphics2D g2, SVSamSapling sap, Rectangle.Double r) { // fc-7.11.2013
		double size_m = 0.50; // m, size of the cross
		double half = size_m / 2d;

		double x = sap.getX();
		double y = sap.getY();
		int speCode = sap.getSpeciesCode();

		Line2D.Double l1 = new Line2D.Double(x - half, y, x + half, y);
		Line2D.Double l2 = new Line2D.Double(x, y - half, x, y + half);

		// A cross
		if (speCode == 0) {
			g2.setColor(toGrayScale(COLORSP0L4));
		} else if (speCode == 1) {
			g2.setColor(toGrayScale(COLORSP1L4));
		} else {
			g2.setColor(toGrayScale(COLORSP2L4));
		}
		g2.draw(l1);
		g2.draw(l2);

	}

	/**
	 * Method to draw a recent dead tree within this viewer.
	 */
	public void drawRecentDead(Graphics2D g2, Samsa2DecayedTree dt, Rectangle.Double r) {
		double dbh_m = dt.getDbh() / 100d;
		double size_m = 8 * dbh_m; // m, size of the diamond
		double half = size_m / 2d;

		double x = dt.getX();
		double y = dt.getY();
		int speCode = dt.getSpecies().getValue();

		// Line2D.Double l1 = new Line2D.Double (x - half, y, x, y + half);
		// Line2D.Double l2 = new Line2D.Double (x, y + half, x + half, y);
		// Line2D.Double l3 = new Line2D.Double (x + half, y, x, y - half);
		// Line2D.Double l4 = new Line2D.Double (x, y - half, x - half, y);

		GeneralPath diamond = new GeneralPath();
		diamond.moveTo(x - half, y);
		diamond.lineTo(x, y + half);
		diamond.lineTo(x + half, y);
		diamond.lineTo(x, y - half);
		diamond.closePath();

		g2.setColor(Color.WHITE);
		g2.fill(diamond);

		// A diamond
		if (speCode == 0) {
			g2.setColor(toGrayScale(COLORSP0L4));
		} else if (speCode == 1) {
			g2.setColor(toGrayScale(COLORSP1L4));
		} else {
			g2.setColor(toGrayScale(COLORSP2L4));
		}

		// Stroke normalStroke = g2.getStroke();
		//
		// BasicStroke stroke = new BasicStroke(2);
		// g2.setStroke(stroke);

		g2.draw(diamond);

		// g2.setStroke(normalStroke);

	}

	/**
	 * Method to draw a Spatialized Tree within this viewer.
	 */
	public void drawTree(Graphics2D g2, Tree t, Rectangle.Double r) {
		// Marked trees are considered dead by generic tools -> don't draw
		if (t.isMarked()) {
			return;
		}

		Spatialized s = (Spatialized) t; // fc - 10.4.2008

		// New rendering hints for clean outputs (for a paper)
		// g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);

		double width = 0.1; // 10 cm.
		double x = s.getX();
		double y = s.getY();

		// fc - 26.11.2008
		SVSamTree tree = (SVSamTree) t;

		int speCode = tree.getSpecies().getValue();

		// Colors were prepared in preProcess ()
		if (speCode == 0) {
			g2.setColor(toGrayScale(COLORSP0L1));
		} else if (speCode == 1) {
			g2.setColor(toGrayScale(COLORSP1L1));
		} else {
			g2.setColor(toGrayScale(COLORSP2L1));
		}

		// Shall we draw the trunk ?
		if (((SVSimpleSettings) settings).showDiameters) {
			width = tree.getDbh() / 100;

			Shape sh = new Ellipse2D.Double(x - width / 2, y - width / 2, width, width);
			Rectangle2D bBox = sh.getBounds2D();
			if (r.intersects(bBox)) {
				g2.fill(sh);
			}
		}

		// How to draw the crown ?
		width = 2 * tree.getCrownRadius();

		int crownOption = ((SVSamsaraSettings) settings).crownView;

		if (crownOption == SVSamsaraSettings.NONE) {
			// Do nothing for the crown.

		} else if (crownOption == SVSamsaraSettings.OUTLINED) {

			Shape sh = new Ellipse2D.Double(x - width / 2, y - width / 2, width, width);
			Rectangle2D bBox = sh.getBounds2D();
			if (r.intersects(bBox)) {
				if (grayScale.isSelected()) {
					g2.fill(sh); // fc-28.4.2014
					g2.setColor(g2.getColor().darker()); // fc-28.4.2014
					g2.draw(sh); // fc-28.4.2014
				} else {
					g2.draw(sh);
				}
			}

		} else if (crownOption == SVSamsaraSettings.FILLED) {
			// Colors were prepared in preProcess ()
			int layer = tree.getLayer();
			if (layer == 4) {
				if (speCode == 0) {
					g2.setColor(toGrayScale(COLORSP0L4));
				} else if (speCode == 1) {
					g2.setColor(toGrayScale(COLORSP1L4));
				} else {
					g2.setColor(toGrayScale(COLORSP2L4));
				}
			} else if (layer == 3) {
				if (speCode == 0) {
					g2.setColor(toGrayScale(COLORSP0L3));
				} else if (speCode == 1) {
					g2.setColor(toGrayScale(COLORSP1L3));
				} else {
					g2.setColor(toGrayScale(COLORSP2L3));
				}
			} else if (layer == 2) {
				if (speCode == 0) {
					g2.setColor(toGrayScale(COLORSP0L2));
				} else if (speCode == 1) {
					g2.setColor(toGrayScale(COLORSP1L2));
				} else {
					g2.setColor(toGrayScale(COLORSP2L2));
				}
			} else {
				if (speCode == 0) {
					g2.setColor(toGrayScale(COLORSP0L1));
				} else if (speCode == 1) {
					g2.setColor(toGrayScale(COLORSP1L1));
				} else {
					g2.setColor(toGrayScale(COLORSP2L1));
				}
			}

			Shape sh = new Ellipse2D.Double(x - width / 2, y - width / 2, width, width);
			Rectangle2D bBox = sh.getBounds2D();
			if (r.intersects(bBox)) {
				g2.fill(sh);
			}

			// Specific to Samsara2: mark trees with DMH // fc+bc-31.10.2014
			if (showDMH.isSelected()) {
				try {
					Samsa2Tree samsa2Tree = (Samsa2Tree) tree;
					if (samsa2Tree.getNumberOfDendroHabitats() > 0) {

						Color normalColor = g2.getColor();

						// Hachures
						Paint HATCH2 = new HatchedPaint(1, normalColor, 1, java.awt.Color.WHITE, -Math.PI / 4);
						g2.setPaint(HATCH2);
						g2.fill(sh);

						g2.setColor(normalColor);
					}
				} catch (Exception e) {
				} // ignored
			}

		} else if (crownOption == SVSamsaraSettings.TRANSPARENT) {

			// Colors were prepared in preProcess ()
			if (speCode == 0) {
				g2.setColor(toGrayScale(colorT0));
			} else if (speCode == 1) {
				g2.setColor(toGrayScale(colorT1));
			} else {
				g2.setColor(toGrayScale(colorT2));
			}

			Shape sh = new Ellipse2D.Double(x - width / 2, y - width / 2, width, width);
			Rectangle2D bBox = sh.getBounds2D();
			if (r.intersects(bBox)) {
				g2.fill(sh);
			}

			// Specific to Samsara2: mark trees with DMH // fc+bc-31.10.2014
			if (showDMH.isSelected()) {
				try {
					Samsa2Tree samsa2Tree = (Samsa2Tree) tree;
					if (samsa2Tree.getNumberOfDendroHabitats() > 0) {

						Color normalColor = g2.getColor();

						// Hachures
						Paint HATCH2 = new HatchedPaint(1, normalColor, 1, java.awt.Color.WHITE, -Math.PI / 4);
						g2.setPaint(HATCH2);
						g2.fill(sh);

						g2.setColor(normalColor);
					}
				} catch (Exception e) {
				} // ignored
			}

		}

		// A label
		drawLabel(g2, String.valueOf(tree.getId()), (float) x, (float) y);

	}

	/**
	 * Turns the given color into a gray scale color.
	 */
	private Color toGrayScale(Color color) {

		if (!grayScale.isSelected())
			return color;

		// fc-28.4.2014
		float[] rgb = color.getRGBColorComponents(null);

		// Works with three colors only -> 3 very contrasted grays
		float gray = 0;
		if (rgb[0] > rgb[1] && rgb[0] > rgb[2]) { // more red, other species
			gray = 0.8f; // clear gray
		} else if (rgb[1] > rgb[0] && rgb[1] > rgb[2]) { // more green, spruce
			gray = 0.4f; // 0.0f; //mid gray
		} else { // more blue, fir
			gray = 0.0f; // 0.4f; // black
		}

		// // Comission internationale de l'éclairage, recommandation 709,
		// couleurs 'vraies' ou
		// // 'naturelles' (wikipedia)
		// float gray = (float) (0.2125 * rgb[0] + 0.7154 * rgb[1] + 0.0721 *
		// rgb[2]);

		// // Comission internationale de l'éclairage, recommandation 601,
		// couleurs 'non linéaires'
		// // (wikipedia)
		// float gray = (float) (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 *
		// rgb[2]);

		return new Color(gray, gray, gray);

	}

	/**
	 * Draw the scale.
	 */
	protected void drawMore(Graphics2D g2, Rectangle.Double r, GScene stand) { // 26.11.2003
																				// -
																				// added
																				// visible
																				// rectangle
		Vertex3d origin = stand.getOrigin();

		SVSamSettings s = (SVSamSettings) stand.getStep().getProject().getModel().getSettings();
		double cellWidth = s.getPlotCellWidth();

		double x = origin.x;
		double y = origin.y + stand.getYSize() + cellWidth / 3; // scale above
																// the drawing
																// (better)
																// - fc
		double x1 = x + cellWidth;
		double y1 = y;

		g2.setColor(Color.BLACK);
		Shape scale = new Line2D.Double(x, y, x1, y1);

		double h = cellWidth / 10d;
		double y2 = y + h / 2d;
		double y3 = y - h / 2d;
		Shape border1 = new Line2D.Double(x, y2, x, y3);
		Shape border2 = new Line2D.Double(x1, y2, x1, y3);

		g2.draw(scale);
		g2.draw(border1);
		g2.draw(border2);
		g2.drawString("" + cellWidth + " m", (float) x, (float) y);

		// Draw recent dead trees

		// fc-30.10.2014
		try {
			Samsa2Stand s2Stand = (Samsa2Stand) stand;

			Collection recentDead = new ArrayList(s2Stand.getDeadWoodCompartment().getFreshDead());
			recentDead.addAll(s2Stand.getDeadWoodCompartment().getClass(0).getTrees());

			if (showRecentDead.isSelected() && !recentDead.isEmpty()) {
				for (Object o : recentDead) {
					Samsa2DecayedTree dt = (Samsa2DecayedTree) o;
					drawRecentDead(g2, dt, r);
				}
			}

		} catch (Exception e) {
		} // ignored (Samsa2 only)
	}

	private void updateLegend() {
		SVSamsaraSettings s = (SVSamsaraSettings) settings;
		if (!s.showLegend) {
			setLegend(null);
			return;
		}

		ColumnPanel legend = new ColumnPanel(2, 0);

		// Compass.
		SVSamSettings settings = (SVSamSettings) stepButton.getStep().getProject().getModel().getSettings();
		SamsaCompass compass = new SamsaCompass(settings.getPlotAspect(), settings.getBottomAzimut());

		legend.add(compass);

		// Crown layers colors.
		if (s.crownView == SVSamsaraSettings.FILLED) {

			ColumnPanel crownLegend = new ColumnPanel(Translator.swap("SVSamsara.crownLegend"), 0, 0);

			crownLegend.setOpaque(true);
			crownLegend.setBackground(Color.WHITE);

			LinePanel l0 = new LinePanel();
			l0.setBackground(Color.WHITE);

			JLabel lab0 = new JLabel(settings.getSpeciesName(0));

			l0.add(lab0);
			l0.addGlue();
			crownLegend.add(l0);

			LinePanel l1 = new LinePanel();
			l1.setBackground(Color.WHITE);

			ColoredPanel b1 = new ColoredPanel(toGrayScale(COLORSP0L1));
			l1.add(b1);
			JLabel lab1 = new JLabel(Translator.swap("SVSamsara.layer1"));
			lab1.setFont(tableFont);
			l1.add(lab1);
			l1.addGlue();
			crownLegend.add(l1);

			LinePanel l2 = new LinePanel();
			l2.setBackground(Color.WHITE);
			ColoredPanel b2 = new ColoredPanel(toGrayScale(COLORSP0L2));
			l2.add(b2);
			JLabel lab2 = new JLabel(Translator.swap("SVSamsara.layer2"));
			lab2.setFont(tableFont);
			l2.add(lab2);
			l2.addGlue();
			crownLegend.add(l2);

			LinePanel l3 = new LinePanel();
			l3.setBackground(Color.WHITE);
			ColoredPanel b3 = new ColoredPanel(toGrayScale(COLORSP0L3));
			l3.add(b3);
			JLabel lab3 = new JLabel(Translator.swap("SVSamsara.layer3"));
			lab3.setFont(tableFont);
			l3.add(lab3);
			l3.addGlue();
			crownLegend.add(l3);

			LinePanel l4 = new LinePanel();
			l4.setBackground(Color.WHITE);
			ColoredPanel b4 = new ColoredPanel(toGrayScale(COLORSP0L4));
			l4.add(b4);
			JLabel lab4 = new JLabel(Translator.swap("SVSamsara.layer4"));
			lab4.setFont(tableFont);
			l4.add(lab4);
			l4.addGlue();
			crownLegend.add(l4);

			if (speciesNumber > 1) {
				LinePanel l0b = new LinePanel();
				l0b.setBackground(Color.WHITE);

				JLabel lab0b = new JLabel(settings.getSpeciesName(1));

				l0b.add(lab0b);
				l0b.addGlue();
				crownLegend.add(l0b);

				LinePanel l1b = new LinePanel();
				l1b.setBackground(Color.WHITE);
				ColoredPanel b1b = new ColoredPanel(toGrayScale(COLORSP1L1));
				l1b.add(b1b);
				JLabel lab1b = new JLabel(Translator.swap("SVSamsara.layer1"));
				lab1b.setFont(tableFont);
				l1b.add(lab1b);
				l1b.addGlue();
				crownLegend.add(l1b);

				LinePanel l2b = new LinePanel();
				l2b.setBackground(Color.WHITE);
				ColoredPanel b2b = new ColoredPanel(toGrayScale(COLORSP1L2));
				l2b.add(b2b);
				JLabel lab2b = new JLabel(Translator.swap("SVSamsara.layer2"));
				lab2b.setFont(tableFont);
				l2b.add(lab2b);
				l2b.addGlue();
				crownLegend.add(l2b);

				LinePanel l3b = new LinePanel();
				l3b.setBackground(Color.WHITE);
				ColoredPanel b3b = new ColoredPanel(toGrayScale(COLORSP1L3));
				l3b.add(b3b);
				JLabel lab3b = new JLabel(Translator.swap("SVSamsara.layer3"));
				lab3b.setFont(tableFont);
				l3b.add(lab3b);
				l3b.addGlue();
				crownLegend.add(l3b);

				LinePanel l4b = new LinePanel();
				l4b.setBackground(Color.WHITE);
				ColoredPanel b4b = new ColoredPanel(toGrayScale(COLORSP1L4));
				l4b.add(b4b);
				JLabel lab4b = new JLabel(Translator.swap("SVSamsara.layer4"));
				lab4b.setFont(tableFont);
				l4b.add(lab4b);
				l4b.addGlue();
				crownLegend.add(l4b);
			}

			if (speciesNumber > 2) {
				LinePanel l0c = new LinePanel();
				l0c.setBackground(Color.WHITE);
				JLabel lab0c = new JLabel(Translator.swap("SVSamsara.species2"));
				l0c.add(lab0c);
				l0c.addGlue();
				crownLegend.add(l0c);

				LinePanel l1c = new LinePanel();
				l1c.setBackground(Color.WHITE);
				ColoredPanel b1c = new ColoredPanel(toGrayScale(COLORSP2L1));
				l1c.add(b1c);
				JLabel lab1c = new JLabel(Translator.swap("SVSamsara.layer1"));
				lab1c.setFont(tableFont);
				l1c.add(lab1c);
				l1c.addGlue();
				crownLegend.add(l1c);

				LinePanel l2c = new LinePanel();
				l2c.setBackground(Color.WHITE);
				ColoredPanel b2c = new ColoredPanel(toGrayScale(COLORSP2L2));
				l2c.add(b2c);
				JLabel lab2c = new JLabel(Translator.swap("SVSamsara.layer2"));
				lab2c.setFont(tableFont);
				l2c.add(lab2c);
				l2c.addGlue();
				crownLegend.add(l2c);

				LinePanel l3c = new LinePanel();
				l3c.setBackground(Color.WHITE);
				ColoredPanel b3c = new ColoredPanel(toGrayScale(COLORSP2L3));
				l3c.add(b3c);
				JLabel lab3c = new JLabel(Translator.swap("SVSamsara.layer3"));
				lab3c.setFont(tableFont);
				l3c.add(lab3c);
				l3c.addGlue();
				crownLegend.add(l3c);

				LinePanel l4c = new LinePanel();
				l4c.setBackground(Color.WHITE);
				ColoredPanel b4c = new ColoredPanel(toGrayScale(COLORSP2L4));
				l4c.add(b4c);
				JLabel lab4c = new JLabel(Translator.swap("SVSamsara.layer4"));
				lab4c.setFont(tableFont);
				l4c.add(lab4c);
				l4c.addGlue();
				crownLegend.add(l4c);
			}

			crownLegend.addStrut0();
			legend.add(crownLegend);

		}

		else { // other tree representations
			ColumnPanel crownLegend = new ColumnPanel(Translator.swap("SVSamsara.speciesLegend"), 0, 0);

			crownLegend.setOpaque(true);
			crownLegend.setBackground(Color.WHITE);

			LinePanel l1 = new LinePanel();
			l1.setBackground(Color.WHITE);
			ColoredPanel b1 = new ColoredPanel(toGrayScale(COLORSP0L1));
			l1.add(b1);

			JLabel lab1 = new JLabel(settings.getSpeciesName(0));

			lab1.setFont(tableFont);
			l1.add(lab1);
			l1.addGlue();
			crownLegend.add(l1);

			if (speciesNumber > 1) { // fc - 10.11.2005
				LinePanel l2 = new LinePanel();
				l2.setBackground(Color.WHITE);
				ColoredPanel b2 = new ColoredPanel(toGrayScale(COLORSP1L1));
				l2.add(b2);

				JLabel lab2 = new JLabel(settings.getSpeciesName(1));

				lab2.setFont(tableFont);
				l2.add(lab2);
				l2.addGlue();
				crownLegend.add(l2);
			} // else, may crash if one species only

			if (speciesNumber > 2) {
				LinePanel l3 = new LinePanel();
				l3.setBackground(Color.WHITE);
				ColoredPanel b3 = new ColoredPanel(toGrayScale(COLORSP2L1));
				l3.add(b3);
				JLabel lab3 = new JLabel(Translator.swap("SVSamsara.species2"));
				lab3.setFont(tableFont);
				l3.add(lab3);
				l3.addGlue();
				crownLegend.add(l3);
				crownLegend.addStrut0();
			}

			legend.add(crownLegend);

		}

		// Light / regeneration

		ButtonModel selection = cellColorGroup.getSelection();
		RGBManager man = cellRGBMap.get(selection);
		if (man != null) { // fc-7.11.2013 no manager when Saplings mode
			JComponent p = man.getCaption();
			p.setBackground(Color.WHITE);
			legend.add(p);
		}
		// if (showLight.isSelected ()) {
		//
		// JComponent p = lightRGBManager.getCaption ();
		// p.setBackground (Color.WHITE);
		//
		// legend.add (p);
		//
		// } else if (showRegeneration.isSelected ()) {
		//
		// JComponent p = regeRGBManager.getCaption ();
		// p.setBackground (Color.WHITE);
		//
		// legend.add (p);
		//
		// }

		// Saplings
		if (showSaplings.isSelected()) {
			LinePanel l100 = new LinePanel();
			l100.setBackground(Color.WHITE);
			JLabel l = new JLabel(Translator.swap("SVSamsara.saplings"));

			// g.fillRect (0, 0, h, h);

			int h = 12;
			BufferedImage img = new BufferedImage(h, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.setColor(toGrayScale(COLORSP1L2));
			// A cross
			double size_m = 10; // m, size of the cross
			double half = size_m / 2d;
			double x = half;
			double y = half;
			Line2D.Double l1 = new Line2D.Double(x - half, y, x + half, y);
			Line2D.Double l2 = new Line2D.Double(x, y - half, x, y + half);
			g2.draw(l1);
			g2.draw(l2);

			ImageIcon icon = new ImageIcon(img);
			l.setIconTextGap(4);
			l.setIcon(icon);
			l100.add(l);
			l100.addGlue();

			legend.add(l100);
		}

		// DMH
		if (showDMH.isSelected()) {
			LinePanel l101 = new LinePanel();
			l101.setBackground(Color.WHITE);
			JLabel l = new JLabel(Translator.swap("SVSamsara.DMH"));

			// g.fillRect (0, 0, h, h);

			int h = 12;
			BufferedImage img = new BufferedImage(h, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			
			double size_m = 10; // m, size of the diamond
			double half = size_m / 2d;
			double x = half;
			double y = half;
		
			Shape sh = new Ellipse2D.Double(x - half, y - half / 2, size_m, size_m);

			// Hachures
			Paint HATCH2 = new HatchedPaint(2, toGrayScale(COLORSP0L2), 2, java.awt.Color.WHITE, Math.PI / 4);
			g2.setPaint(HATCH2);
			g2.fill(sh);
			
			ImageIcon icon = new ImageIcon(img);
			l.setIconTextGap(4);
			l.setIcon(icon);

			l101.add(l);
			l101.addGlue();

			legend.add(l101);

		}

		// Recent Dead
		if (showRecentDead.isSelected()) {
			LinePanel l102 = new LinePanel();
			l102.setBackground(Color.WHITE);
			JLabel l = new JLabel(Translator.swap("SVSamsara.recentDead"));

			// g.fillRect (0, 0, h, h);

			int h = 12;
			BufferedImage img = new BufferedImage(h, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			double size_m = 10; // m, size of the diamond
			double half = size_m / 2d;
			double x = half;
			double y = half;
			GeneralPath diamond = new GeneralPath();
			diamond.moveTo(x - half, y);
			diamond.lineTo(x, y + half);
			diamond.lineTo(x + half, y);
			diamond.lineTo(x, y - half);
			diamond.closePath();

			g2.setColor(Color.WHITE);
			g2.fill(diamond);

			g2.setColor(toGrayScale(COLORSP2L2));
			g2.draw(diamond);

			ImageIcon icon = new ImageIcon(img);
			l.setIconTextGap(4);
			l.setIcon(icon);

			l102.add(l);
			l102.addGlue();

			legend.add(l102);

		}

		legend.addGlue();

		legend.setOpaque(true);
		legend.setBackground(Color.WHITE);

		JPanel aux = new JPanel(new BorderLayout());
		aux.add(legend, BorderLayout.NORTH);
		aux.setBackground(Color.WHITE);

		setLegend(aux);
	}

}
