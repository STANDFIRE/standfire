/*
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
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
package capsis.lib.samsaralight;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Neighbour;
import capsis.defaulttype.RectangularPlot;
import capsis.defaulttype.ShiftItem;
import capsis.defaulttype.SquareCell;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.util.Point2D;

/**
 * SLModel - implementation of the Samsara light model.
 * 
 * @author B. Courbaud, N. Dones, M. Jonard, G. Ligot, F. de Coligny - October 2008 / June 2012
 */
public class SLModel implements Serializable {

	private SLBeamSet beamSet;
	private SLSettings slSettings;
	private double cellInitialEnergySum;
	

	// A map to help calculate PotentialEnergy
	private Map<SLLightableTree,Double> potCurrentEnergyMap;

	/**
	 * Constructor
	 */
	public SLModel () {
		slSettings = new SLSettings ();
	}

	/**
	 * This method should be called at init time, once the main parameters file name is set in
	 * SLSettings.
	 */
	public void init () throws Exception {

		// Before calling init (), a correct fileName must be set in slSettings
		try {
			SLSettingsLoader loader = new SLSettingsLoader (slSettings.fileName);
			loader.interpret (slSettings);
		} catch (Exception e) {
			Log.println (Log.ERROR, "SLModel.init ()", "Could not load the settings file: " + slSettings, e);
			throw e;
		}

		// Create beamSet with SLBeamSetFactory
		if (getSettings ().writeStatusDispatcher) StatusDispatcher.print (Translator.swap ("MountModel.creatingBeamSet"));

		if (slSettings.getMontlyRecords () != null) {
			beamSet = SLBeamSetFactory.getMonthlyBeamSet (slSettings);
		} else if (slSettings.getHourlyRecord () != null) {
			beamSet = SLBeamSetFactory.getHourlyBeamSet (slSettings);
		} else {
			throw new Exception (
					"SLModel.init() could not create the beamSet, please provide monthlyBeamSet or hourlyBeamSet in SLSettings");
		}

	}

	/**
	 * Computes relative cell neighbourhoods (a brand new concept) for each beam in the beamSet. The
	 * trees which can intercept a beam are located in cells with their center located in a
	 * competition rectangle of length L (beam direction) + R (opposite direction) and width R
	 * (directions perpendicular to beam).
	 */
	public void computeRelativeCellNeighbourhoods (TreeList initStand, double treeMaxHeight, double cellWidth,
			double maxCrownRadius) throws Exception {

		// GL 22/06/2012 remove bottom azimut from method signature. I get it
		// from the slsettings.
		double bottomAzimut_rad = Math.toRadians (-slSettings.getPlotAspect_deg ()
				+ slSettings.getNorthToXAngle_cw_deg ());
		// bottomAzimut = Math.toRadians(bottomAzimut); // azimut of the maximum

		if (beamSet == null)
			throw new Exception ("SLModel: beamSet is null, check that SLModel.init () was called correctly");

		double Hmax = treeMaxHeight * 1.25; // max tree height (a little heigher
		// than the height at wich the
		// probability of mortality is max)

		// Slope line / x axis
		double width = cellWidth; // cell width (meters)
		double reach = (double) Math.min (initStand.getXSize (), initStand.getYSize ()); // max
		// reach
		// (meters)
		// : min
		// (plot
		// width,
		// plot
		// height)
		double cRadius = maxCrownRadius; // max crown radius (meters)

		// RectangularPlot plot = (RectangularPlot) initStand.getPlot(); // plot
		// slope
		// SLPlotLight plotLight = ((SLLightablePlot) plot).getPlotLight(); //
		// plot slope
		double slope_rad = Math.toRadians (slSettings.getPlotSlope_deg ()); // GL - modified from
		// (22/06/2012):
		// plotLight.getSlope();
		// // plot slope

		for (Iterator it = beamSet.getBeams ().iterator (); it.hasNext ();) {
			SLBeam b = (SLBeam) it.next ();
			int count = 0;

			double azimut = b.getAzimut_rad ();
			double hAngle = b.getHeightAngle_rad ();

			// double lateral = width/Math.sqrt(2);
			// Computes lateral = the boundary to add to the competition
			// rectangle to take into account cells center
			// instead of trees position.
			// The boundary depends on beam azimut.
			double azt;
			if (azimut < Math.PI / 4) {
				azt = azimut;
			} else if ((azimut >= Math.PI / 4) && (azimut < Math.PI / 2)) {
				azt = Math.PI / 2 - azimut;
			} else if ((azimut >= Math.PI / 2) && (azimut < 3 * Math.PI / 4)) {
				azt = azimut - Math.PI / 2;
			} else if ((azimut >= 3 * Math.PI / 4) && (azimut < Math.PI)) {
				azt = Math.PI - azimut;
			} else if ((azimut >= Math.PI) && (azimut < 5 * Math.PI / 4)) {
				azt = azimut - Math.PI;
			} else if ((azimut >= 5 * Math.PI / 4) && (azimut < 3 * Math.PI / 2)) {
				azt = 3 * Math.PI / 2 - azimut;
			} else if ((azimut >= 3 * Math.PI / 2) && (azimut < 7 * Math.PI / 4)) {
				azt = azimut - 3 * Math.PI / 2;
			} else if (azimut >= 7 * Math.PI / 4) {
				azt = 2 * Math.PI - azimut;
			} else {
				azt = 0;
			}

			double lateral = width / Math.sqrt (2) * Math.sin (azt + Math.PI / 4);

			// Beam width = max lateral distance from the beam to a cell center
			// able to own a tree which can intercept the beam.
			double R = cRadius + lateral;

			// Beam reach maximum distance along the beam beyond which the cells
			// cannot own trees which can intercept the beam (too high).
			double L = Hmax / (Math.tan (hAngle) + Math.cos (azimut - bottomAzimut_rad) * Math.tan (slope_rad))
					+ lateral;

			double sinA = Math.sin (azimut);
			double cosA = Math.cos (azimut);

			// Coordinates of the four corners of the competition rectangle.
			double x1 = R * sinA + L * cosA;
			double y1 = L * sinA - R * cosA;
			double x2 = L * cosA - R * sinA;
			double y2 = L * sinA + R * cosA;
			double x3 = R * (sinA - cosA);
			double y3 = -R * (sinA + cosA);
			double x4 = -R * (sinA + cosA);
			double y4 = R * (cosA - sinA);

			double xMin = Math.min (x1, x2);
			xMin = Math.min (xMin, x3);
			xMin = Math.min (xMin, x4);

			double xMax = Math.max (x1, x2);
			xMax = Math.max (xMax, x3);
			xMax = Math.max (xMax, x4);

			double yMin = Math.min (y1, y2);
			yMin = Math.min (yMin, y3);
			yMin = Math.min (yMin, y4);

			double yMax = Math.max (y1, y2);
			yMax = Math.max (yMax, y3);
			yMax = Math.max (yMax, y4);

			// Round of xMin, xMax, yMin, yMax with ceil or floor as necessary.
			// -> an xCenter relatively to target
			int aux = (int) Math.floor (xMax / width); // xMax > 0 : round to
			// maximum int < xMax
			xMax = aux * width;
			aux = (int) Math.floor (yMax / width);
			yMax = aux * width;
			aux = (int) Math.ceil (xMin / width); // xMin < 0 : round to minimum
			// int > xMin
			xMin = aux * width;
			aux = (int) Math.ceil (yMin / width);
			yMin = aux * width;

			// Creates an array of cell indexes between extreme competition
			// rectangle coordinates.
			int iMax = (int) ((yMax - yMin) / width + 0.5) + 1; // i e [0, iMax]
			// : iMax+1
			// elements
			int jMax = (int) ((xMax - xMin) / width + 0.5) + 1; // j e [0, jMax]
			// : jMax+1
			// elements

			Point2D.Double[][] tabCell = new Point2D.Double[iMax][jMax];
			int i = 0;
			int j = 0;
			iMax = 0;
			jMax = 0;

			for (double y = yMax; y >= yMin; y -= width) {
				for (double x = xMin; x <= xMax; x += width) {
					// System.out.print ("x="+x+" i="+i+" j="+j+" . ");

					tabCell[i][j] = new Point2D.Double (x, y); // a candidate
					// cell (its
					// center)
					j++;
				}
				if (j - 1 > jMax) {
					jMax = j - 1;
				}
				j = 0;
				i++;
			}
			iMax = i - 1;

			// Explores the array of cell indexes to extract the cells which are
			// located
			// inside the competition rectangle (depending of the azimut).
			for (i = 0; i <= iMax; i++) {
				for (j = 0; j <= jMax; j++) {
					double x = tabCell[i][j].x;
					double y = tabCell[i][j].y;
					if ((x * sinA - y * cosA < R) && (x * cosA + y * sinA < L) && (-x * sinA + y * cosA < R)
							&& (x * cosA + y * sinA > -R)) {

						// Line index.
						int I = (int) (-y / width);
						// Column index.
						int J = (int) (x / width);
						// ~ b.addNeighbourCell (new Point (I, J));
						b.addSite (new Point (I, J)); // fc - 10.10.2008
						count++;
						// Log.print (tabCell [i][j]+" ");
					}
				}
			}
		}
	}

	/**
	 * Implementation of the Samsara light model
	 */
	public void processLighting (TreeList stand) {

		RectangularPlot plot = (RectangularPlot) stand.getPlot ();
		// SLPlotLight plotLight = ((SLLightablePlot) plot).getPlotLight(); //
		// remove GL : 22/06/2012

		// GL : 22/06/2012 - modified from plotLight.getSlope(); samsaSettings.plotSlope;
		double slope_rad = Math.toRadians (slSettings.getPlotSlope_deg ());

		// GL : 22/06/2012 : modified from : plotLight.getCellSlopeArea();
		double cellHorizontalSurface = plot.getCellWidth () * plot.getCellWidth ();
		double cellSurface = cellHorizontalSurface / Math.cos (slope_rad);

		// azimut of the vector orthogonal to the ground in the x,y system
		double bottomAzimut = Math.toRadians (-slSettings.getPlotAspect_deg () + slSettings.getNorthToXAngle_cw_deg ());

		// double bottomAzimut = plotLight.getBottomAzimut(); // GL removed the
		// 22/06/2012 - azimut of vector othogonal to slope

		if (getSettings ().writeStatusDispatcher) System.out.println ("SLModel processLighting() : cellSurface = " + cellSurface);

		// Reset energy for target cells before their enlightment.
		for (Iterator i = plot.getCells ().iterator (); i.hasNext ();) {
			SquareCell c = (SquareCell) i.next ();
			SLCellLight cellLight = ((SLLightableCell) c).getCellLight ();
			cellLight.resetEnergy ();
		}

		// Reset energy and impact number for all trees before new computation
		// (necessary when trees have grown).
		// for (Iterator i = stand.getTrees().iterator(); i.hasNext();) {
		// Tree t = (Tree) i.next();T
		// SLTreeLight treeLight = ((SLLightableTree) t).getTreeLight();
		// treeLight.resetEnergy();
		// treeLight.resetPotCrownEnergy();
		// treeLight.resetPotEnergy();
		// treeLight.resetImpactNumber();
		// }

		// fc - for test - you may leave this here.
		int maxSize = 0;
		int maxCcSize = 0;

		// Define the cells to enlight, optional optimization, GL, 25/06/2012
		Collection<SquareCell> targetCells = new ArrayList<SquareCell> ();
		targetCells = ((SLLightableScene) stand).getCellstoEnlight ();
		if (targetCells == null) targetCells = plot.getCells ();

		if(! slSettings.isSensorLightOnly ()){
			// For each target cell to enlight.
			for (SquareCell cell : targetCells) {

				// SquareCell cell = (SquareCell) c.next();
				SLCellLight cellLight = ((SLLightableCell) cell).getCellLight ();

				if(slSettings.writeStatusDispatcher){ //gl 5-6-2013
					// User wait message.
					String msg = "";
					try {
						Step step = stand.getStep ();
						Project scenario = step.getProject ();
						msg = Translator.swap ("SamsaModel.scenario") + " " + scenario.getName () + " ";
					} catch (Exception e) {
						// This is not an error: the scene is not supposed to be tied to a Step in
						// processLighting ()
						// fc-14.2.2013 -> if failure, does not matter at all, it is only for a message (see
						// below)
						// Log.println(Log.ERROR, "SLModel.processLighting ()",
						// "Structural error: could not get Project from scene", e);
					}
	
					msg += Translator.swap ("SamsaModel.step") + " " + stand.getCaption () // step is being
							// build
							+ " " + Translator.swap ("SamsaModel.lightingForCell") + " " + cell.getPosition ();
					StatusDispatcher.print (msg);
				}

				double cellTotalEnergy = 0;
				double cellDirectEnergy = 0;
				double cellDiffuseEnergy = 0;
				double cellTotalHorizontalEnergy = 0; // GL 3/10/2012
				double cellDiffuseHorizontalEnergy = 0; // GL 3/10/2012
				double cellDirectHorizontalEnergy = 0; // GL 3/10/2012

				//			cellInitialEnergySum = 0; // --ND 04-2010 (commented by GL see in the beamset)

				// For each beam of the sky.
				for (SLBeam beam : beamSet.getBeams ()) {

					// Before each target cell enlightment, reset beam energy to
					// initial value
					beam.resetEnergy ();

					Collection<Neighbour> neighbours = beam.getNeighbours (cell, true); // true: only
					// neighbour
					// cells with
					// trees

					Vector vctI = new Vector ();

					for (Neighbour neighbour : neighbours) {
						SquareCell cCell = (SquareCell) neighbour.cell;
						ShiftItem shift = neighbour.shift; // the shift manages the
						// torus approaches to
						// work on virtual
						// infinite plots

						if (!cCell.isEmpty ()) {

							// For each tree in the concurrent cells.
							for (Iterator t = cCell.getTrees ().iterator (); t.hasNext ();) {
								Tree tree = (Tree) t.next ();
								// Computes interception characteristics for a
								// targetCell / a beam/ a tree (average distance
								// from target cell / pathLength / competitor tree)

								Set<SLInterceptionItem> items = intercept (cellLight.getConnectedCell ().getXCenter (), 
										cellLight.getConnectedCell ().getYCenter (), 
										cellLight.getConnectedCell ().getZCenter (), 
										beam, tree, shift); // modified by GL at Feb 2013

								// System.out.println("SLModel, Turbid medium, interception items "+((items
								// == null) ? "null" : ""+items.size ()));

								if (items != null) {
									vctI.addAll (items);
								}

							}
						}
					}

					// These counters are useful and take no time (measured fc).
					if (vctI.size () > maxSize) {
						maxSize = vctI.size ();
					}
					if (neighbours.size () > maxCcSize) {
						maxCcSize = neighbours.size ();
					}

					// fc - tested : use directly tabI instead of vctI : NOT faster
					// and tab dimension pb.
					SLInterceptionItem[] tabI = new SLInterceptionItem[vctI.size ()];
					vctI.copyInto (tabI);

					// Interception items are sorted from farthest to nearest tree
					// from the target cell.
					// It is necessary to compute progressive energy loss of the
					// beam from canopy top to canopy bottom.
					Arrays.sort (tabI);

					// Test order, checked on 28.6.2012, correct (fc)
					// if (testOrder) {
					// System.out.println("SLModel Test order...");
					// for (int i = 0; i < tabI.length; i++) {
					// System.out.println("SLModel "+tabI[i]);
					// }
					// System.out.println("...Test order SLModel");
					// testOrder = false; // only once
					// }

					// Reduce beam energy for each tree interception,
					// increase tree energy at the same time and
					// add the remaining beam energy to the target cell.

					// Compute initial energy in MJ, reaching a cell parallel to
					// slope without interception.
					double hAngle = beam.getHeightAngle_rad ();
					double azimut = beam.getAzimut_rad ();
					double beamEnergy = beam.getInitialEnergy (); // beam energy in
					// MJ/m2 on a plane orthogonal to beam direction

					// Projection of energy on plane parallel to slope in MJ/m2.
					double scalar = Math.cos (slope_rad) * Math.sin (hAngle) + Math.sin (slope_rad) * Math.cos (hAngle)
							* Math.cos (azimut - bottomAzimut);
					double initEnergy = scalar * beamEnergy;
					//				cellInitialEnergySum += initEnergy; // --ND 04-2010

					// Projection of energy on a horizontal plane in MJ/m2. (GL - 3 Oct. 2012)
					double scalarHorizontal = Math.cos (0) * Math.sin (hAngle) + Math.sin (0) * Math.cos (hAngle)
							* Math.cos (azimut - bottomAzimut);
					double initEnergyHorizontal = scalarHorizontal * beamEnergy;

					// From MJ/m2 to MJ.
					initEnergy = initEnergy * cellSurface;
					initEnergyHorizontal = initEnergyHorizontal * cellHorizontalSurface;
					double currentEnergy = initEnergy;
					double currentHorizontalEnergy = initEnergyHorizontal;

					// To calculate PotentialEnergy, see lower
					potCurrentEnergyMap = new HashMap<SLLightableTree,Double> ();

					Set<Tree> treeMemory = new HashSet<Tree> ();

					// For each competing tree.
					for (int i = 0; i < tabI.length; i++) {
						SLInterceptionItem item = tabI[i];

						Tree t = item.getTree ();
						SLLightableTree lightableTree = (SLLightableTree) t;
						SLTreeLight treeLight = lightableTree.getTreeLight ();
						SLTreePart treePart = item.getTreePart ();

						if (treePart instanceof SLTrunk) {

							if (beam.isDirect ()) {
								treePart.addDirectEnergy (currentEnergy);
							} else {
								treePart.addDiffuseEnergy (currentEnergy);
							}
							// LATER treePart.addPotentialEnergy(e)

							currentEnergy = 0;
							currentHorizontalEnergy = 0;

						} else {

							double potCurrentEnergy = getPotCurrentEnergy (lightableTree, initEnergy);

							// Crown part
							if (slSettings.turbidMedium) {

								double leafAreaDensity = ((SLCrownPart) treePart).getLeafAreaDensity ();
								double extinctionCoef = ((SLCrownPart) treePart).getExtinctionCoefficient ();
								// if the coef = 0.5, it corresponds to a
								// spherical leaf angle distribution

								double clumpingFactor = 1.0;
								double interceptedE = currentEnergy
										* (1 - Math.exp (-extinctionCoef * clumpingFactor * leafAreaDensity
												* item.getPathLength ()));

								if (beam.isDirect ()) {
									treePart.addDirectEnergy (interceptedE);
								} else {
									treePart.addDiffuseEnergy (interceptedE);
								}

								// System.out.println("SLModel, Turbid medium, added "+interceptedE+" MJ to tree "+t.getId

								currentEnergy -= interceptedE;
								currentHorizontalEnergy -= currentHorizontalEnergy
										* (1 - Math.exp (-extinctionCoef * clumpingFactor * leafAreaDensity
												* item.getPathLength ()));

								double potCrownInterceptedE = potCurrentEnergy
										* (1 - Math.exp (-extinctionCoef * clumpingFactor * leafAreaDensity
												* item.getPathLength ()));
								treePart.addPotentialEnergy (potCrownInterceptedE); // IT'S
								// WRONG

								decrementPotCurrentEnergy (lightableTree, potCrownInterceptedE);

								// porous envelop
							} else {

								if (!treeMemory.contains (t)) { //since the crown openness concern the entire crown and not a crownpart, we need to attenuate the energy only once.
									double transmissivity = lightableTree.getCrownTransmissivity ();

									double interceptedE = (1 - transmissivity) * currentEnergy;

									if (beam.isDirect ()) {
										treePart.addDirectEnergy (interceptedE);
									} else {
										treePart.addDiffuseEnergy (interceptedE);
									}

									// System.out.println("SLModel, Porous envelop, added "+interceptedE+" MJ to tree "+t.getId
									// ());

									currentEnergy -= interceptedE;
									currentHorizontalEnergy -= currentHorizontalEnergy * (1 - transmissivity);

									double potCrownInterceptedE = (1 - transmissivity) * potCurrentEnergy;
									treePart.addPotentialEnergy (potCrownInterceptedE); // IT'S WRONG (TODO)

									decrementPotCurrentEnergy (lightableTree, potCrownInterceptedE);

									treeMemory.add (t);
								}
							}
						}

					}

					// Cell energy is in MJ/m2 and projected on a plane parallel to the slope
					cellTotalEnergy += currentEnergy / cellSurface;
					cellTotalHorizontalEnergy += currentHorizontalEnergy / cellHorizontalSurface;

					if (beam.isDirect ()) {
						cellDirectEnergy += currentEnergy / cellSurface;
						cellDirectHorizontalEnergy += currentHorizontalEnergy / cellHorizontalSurface;
					} else {
						cellDiffuseEnergy += currentEnergy / cellSurface;
						cellDiffuseHorizontalEnergy += currentHorizontalEnergy / cellHorizontalSurface;
					}

					//				System.out.println ("SLModel - beam azimut " + beam.getAzimut_rad () + "- beam elevation " +beam.getHeightAngle_rad ()
					//						+ "- initial energy " + beam.getInitialEnergy () + " - cell energy (MJ/M2 horiz) " + cellTotalEnergy
					//						+ " - nb intercepted items " + vctI.size ());

				} // end of the loop going through every beams

				
				cellLight.setAboveCanopyHorizontalEnergy((float) (beamSet.getHorizontalDiffuse() + beamSet.getHorizontalDirect()));
				
				cellLight.setTotalHorizontalEnergy((float) cellTotalHorizontalEnergy);
				cellLight.setDirectHorizontalEnergy((float) cellDirectHorizontalEnergy);
				cellLight.setDiffuseHorizontalEnergy((float) cellDiffuseHorizontalEnergy);
				
				cellLight.setDirectSlopeEnergy ((float) cellDirectEnergy);
				cellLight.setDiffuseSlopeEnergy ((float) cellDiffuseEnergy);
				cellLight.setTotalSlopeEnergy ((float) cellTotalEnergy);

				double relativeSlopeEnergy = 100 * cellTotalEnergy
						/ (beamSet.getSlopeDiffuse () + beamSet.getSlopeDirect ());
				double relativeSlopeDiffuseEnergy = 100 * cellDiffuseEnergy / beamSet.getSlopeDiffuse ();
				double relativeSlopeDirectEnergy = 100 * cellDirectEnergy / beamSet.getSlopeDirect ();

				if (relativeSlopeEnergy > 100) {
					System.out.println ("SLModel processLighting() : relative slope energy was over 100% : "
							+ relativeSlopeEnergy);
					relativeSlopeEnergy = 100;
				}
				if (relativeSlopeDiffuseEnergy > 100) relativeSlopeDiffuseEnergy = 100;
				if (relativeSlopeDirectEnergy > 100) relativeSlopeDirectEnergy = 100;

				cellLight.setRelativeSlopeEnergy ((float) relativeSlopeEnergy);
				cellLight.setRelativeDiffuseSlopeEnergy ((float) relativeSlopeDiffuseEnergy);
				cellLight.setRelativeDirectSlopeEnergy ((float) relativeSlopeDirectEnergy);

				double relativeHorizontalEnergy = 100 * cellTotalHorizontalEnergy
						/ (beamSet.getHorizontalDiffuse () + beamSet.getHorizontalDirect ());
				double relativeHorizontalDiffuseEnergy = 100 * cellDiffuseHorizontalEnergy
						/ beamSet.getHorizontalDiffuse ();
				double relativeHorizontalDirectEnergy = 100 * cellDirectHorizontalEnergy / beamSet.getHorizontalDirect ();

				if (relativeHorizontalEnergy > 100) {
					System.out.println ("SLModel processLighting() : relative horizontal energy was over 100% : "
							+ relativeHorizontalEnergy);
					relativeHorizontalEnergy = 100;
				}
				if (relativeHorizontalDiffuseEnergy > 100) relativeHorizontalDiffuseEnergy = 100;
				if (relativeHorizontalDirectEnergy > 100) relativeHorizontalDirectEnergy = 100;

				cellLight.setRelativeHorizontalEnergy ((float) relativeHorizontalEnergy);
				cellLight.setRelativeDiffuseHorizontalEnergy ((float) relativeHorizontalDiffuseEnergy);
				cellLight.setRelativeDirectHorizontalEnergy ((float) relativeHorizontalDirectEnergy);

				//			Log.println ("SamsaraLight","SLModel.processLighting() - cell light - " + cellLight.getConnectedCell ().toString () +
				//					" PACL = " + cellLight.getRelativeHorizontalEnergy () + " (x " + cellLight.getConnectedCell ().getXCenter () + " y " + cellLight.getConnectedCell ().getYCenter () + ")");
			}
		}


		//----------------------------------------------
		// For each vitual sensors to enlight (GL Feb 2013, May 2013)
		List<SLSensor> sensors = ((SLLightableScene) stand).getSensors ();
		
		Log.println ("SamsaraLight","--------------------------------------");
		Log.println ("SamsaraLight","SLModel : Sensor computation");
		Log.println ("SamsaraLight","--------------------------------------");
		
		if(sensors != null){
			for(SLSensor sensor : sensors){
				
				if(slSettings.writeStatusDispatcher){ //gl 5-6-2013
					// User wait message.
					String msg = "";
					msg += Translator.swap ("SamsaModel.step") + " " + stand.getCaption () // step is being
							+ " " + "compute light for the virtual sensor ID =" + " " + sensor.getId ();
					StatusDispatcher.print (msg);
				}

				//Initialization
				sensor.resetEnergy ();
				double sensorTotalHorizontalEnergy = 0; 
				double sensorDiffuseHorizontalEnergy = 0; 
				double sensorDirectHorizontalEnergy = 0; 
				double sensorTotalSlopeEnergy = 0;
				double sensorDiffuseSlopeEnergy = 0;
				double sensorDirectSlopeEnergy = 0;

				int interceptedItemCount = 0;

				//Find the cells corresponding to the sensors
				if (sensor.getBelowCell() == null){
					SquareCell squareCell = plot.getCell (sensor.getX (), sensor.getY());
					if (squareCell == null) {Log.println (Log.ERROR,"SLModel.processLighting","One sensor does not belong to any cell, you must correct its coordinates");}
					sensor.setBelowCell (squareCell); // needed to compute z coordinate of the sensor!
					if (squareCell.getZCenter () > sensor.getZ ()) Log.println (Log.WARNING,"SLModel.processLighting","The sensor " + sensor.getId () + " is below the grid !");
				}

				// For each beam of the sky.
				for (SLBeam beam : beamSet.getBeams ()) {
					beam.resetEnergy ();
					Collection<Neighbour> neighbours = beam.getNeighbours (sensor.getBelowCell(), true); // true = only neighbour cells with trees
					//GL, TODO, could be optimized for the sensors (e.g. HMAX replaced by HMAX - HSensor)

					//list of every item by the beam
					Vector interceptedItems = new Vector();

					for (Neighbour neighbour : neighbours) {
						SquareCell cCell = (SquareCell) neighbour.cell;
						ShiftItem shift = neighbour.shift; // the shift manages the torus approaches to work on virtual infinite plots

						if (!cCell.isEmpty ()) {

							// For each tree in the concurrent cells.
							for (Iterator t = cCell.getTrees ().iterator (); t.hasNext ();) {
								Tree tree = (Tree) t.next ();

								// Computes interception characteristics for a
								// target virtual sensor / a beam/ a tree (average distance
								// from target cell / pathLength / competitor tree)
								Set<SLInterceptionItem> items = intercept (sensor.getX(), sensor.getY (),	sensor.getZ (), beam, tree, shift); 

								if(items != null) { interceptedItems.addAll (items);}

							}
						}
					}

					//count
					interceptedItemCount += interceptedItems.size ();

					// fc - tested : use directly tabI instead of interceptedItems : NOT faster
					// and tab dimension pb.
					SLInterceptionItem[] tabI = new SLInterceptionItem[interceptedItems.size ()];
					interceptedItems.copyInto (tabI);

					// Interception items are sorted from farthest to nearest tree
					// from the target cell. 
					// It is necessary to compute progressive energy loss of the
					// beam from canopy top to canopy bottom.
					Arrays.sort (tabI);

					//light computations
					double hAngle = beam.getHeightAngle_rad ();
					double azimut = beam.getAzimut_rad ();
					double beamEnergy = beam.getInitialEnergy (); // beam energy in MJ/m2 on a plane orthogonal to beam direction
					double scalarSlope = Math.cos (slope_rad) * Math.sin (hAngle) + Math.sin (slope_rad) * Math.cos (hAngle) * Math.cos (azimut - bottomAzimut);
					double scalarHorizontal = Math.cos (0) * Math.sin (hAngle) + Math.sin (0) * Math.cos (hAngle) * Math.cos (azimut - bottomAzimut);
					double initEnergySlope = scalarSlope * beamEnergy;
					double initEnergyHorizontal = scalarHorizontal * beamEnergy;
					double currentHorizontalEnergy = initEnergyHorizontal; // beam energy in MJ/m2 on a horizontal plane
					double currentSlopeEnergy = initEnergySlope; // beam energy in MJ/M2 along the slope

					//remember intercepted tree if the porous envelop algo was used
					Set<Tree> treeMemory = new HashSet<Tree> ();

					// For each competing tree.
					for (int i = 0; i < tabI.length; i++) {
						SLInterceptionItem item = tabI[i];

						Tree t = item.getTree ();
						SLLightableTree lightableTree = (SLLightableTree) t;
						SLTreeLight treeLight = lightableTree.getTreeLight ();
						SLTreePart treePart = item.getTreePart ();

						//trunk
						if (treePart instanceof SLTrunk) {
							currentHorizontalEnergy = 0;
							currentSlopeEnergy = 0;
						
						// Crown part
						} else {
							//turbid medium
							if (slSettings.turbidMedium) {

								double leafAreaDensity = ((SLCrownPart) treePart).getLeafAreaDensity ();
								double extinctionCoef = ((SLCrownPart) treePart).getExtinctionCoefficient ();
								double clumpingFactor = 1.0;
								double interceptedFraction = (1 - Math.exp (-extinctionCoef * clumpingFactor * leafAreaDensity * item.getPathLength ()));
								double interceptedEHorizontal = currentHorizontalEnergy * interceptedFraction;
								double interceptedESlope = currentSlopeEnergy * interceptedFraction;	
								
								currentHorizontalEnergy -= interceptedEHorizontal;
								currentSlopeEnergy -= interceptedESlope;
								
							// porous envelop
							} else {
								if (!treeMemory.contains (t)) { //since the crown openess concern the entire crown and not a crownpart, we need to attenuate the energy only once.

									double transmissivity = lightableTree.getCrownTransmissivity ();
									
									double interceptedEHorizontal = (1 - transmissivity) * currentHorizontalEnergy;
									double interceptedESlope = (1 - transmissivity) * currentSlopeEnergy;
									
									currentHorizontalEnergy -= interceptedEHorizontal;
									currentSlopeEnergy -= interceptedESlope;

									treeMemory.add (t);
								}
							}
						}
					}

					//cumulate the energy for every beam
					sensorTotalHorizontalEnergy += currentHorizontalEnergy; //MJ/M2
					sensorTotalSlopeEnergy += currentSlopeEnergy; 			//MJ/M2
					
					if (beam.isDirect ()) {
						sensorDirectHorizontalEnergy += currentHorizontalEnergy; //MJ/M2
						sensorDirectSlopeEnergy += currentSlopeEnergy; 			//MJ/M2
					} else {
						sensorDiffuseHorizontalEnergy += currentHorizontalEnergy; 	//MJ/M2
						sensorDiffuseSlopeEnergy += currentSlopeEnergy; 			//MJ/M2
					}	

					//				Log.println ("SamsaraLight2", "SLModel - sensor id = " + sensor.getId () + " beam azimut " + beam.getAzimut_rad () + "- beam elevation " +beam.getHeightAngle_rad ()
					//						+ "- initial energy " + beam.getInitialEnergy () + " - sensor energy (horizonthal MJ/M2) " + sensorTotalHorizontalEnergy
					//						+ " - nb intercepted items " + interceptedItems.size ());

				} //end of the loop going through every beam

				//sensor energy
				sensor.setAboveCanopyHorizontalEnergy(beamSet.getHorizontalDiffuse () + beamSet.getHorizontalDirect ());
				
				sensor.setTotalHorizontalEnergy (sensorTotalHorizontalEnergy);
				sensor.setDiffuseHorizontalEnergy (sensorDiffuseHorizontalEnergy);
				sensor.setDirectHorizontalEnergy (sensorDirectHorizontalEnergy);
				
				sensor.setTotalSlopeEnergy (sensorTotalSlopeEnergy);
				sensor.setDiffuseSlopeEnergy (sensorDiffuseSlopeEnergy);
				sensor.setDirectSlopeEnergy (sensorDirectSlopeEnergy);
				
				sensor.setRelativeHorizontalTotalEnergy (sensorTotalHorizontalEnergy/(beamSet.getHorizontalDiffuse ()+beamSet.getHorizontalDirect ())*100);
				sensor.setRelativeHorizontalDiffuseEnergy (sensorDiffuseHorizontalEnergy/beamSet.getHorizontalDiffuse ()*100);
				sensor.setRelativeHorizontalDirectEnergy (sensorDirectHorizontalEnergy/beamSet.getHorizontalDirect ()*100);

				Log.println ("SamsaraLight","SLModel.processLighting() - sensor light - ID = " + sensor.getId () + " height = " + sensor.getHeight ()
						+" PACL = " + sensor.getRelativeHorizontalTotalEnergy () + " intercepted items nb " + interceptedItemCount + " (x " + sensor.getX () + " y " + sensor.getY () + ")");}
		}
		Log.println ("SamsaraLight","--------------------------------------");
	}

	/**
	 * Tool method to help calculate potential energy, we need to decrement a variable for each
	 * tree.
	 */
	private double getPotCurrentEnergy (SLLightableTree t, double initEnergy) {
		Double v = potCurrentEnergyMap.get (t);
		if (v == null) {
			v = initEnergy;
			potCurrentEnergyMap.put (t, v);
		}
		return v;
	}

	/**
	 * Tool method to help calculate potential energy, we need to decrement a variable for each
	 * tree.
	 */
	private void decrementPotCurrentEnergy (SLLightableTree t, double value) {
		Double v = potCurrentEnergyMap.get (t);
		if (v == null) return;
		double vp = v;
		vp -= value;
		potCurrentEnergyMap.put (t, vp);
	}

	/**
	 * Returns the characteristics of an interception for a target cell, a given beam and a given
	 * tree. If no interception by the tree, returns null. Interception points are at the same time
	 * elements of the beam and of the crown. An equation is solved and the solutions are the
	 * distances between the cell center and the interception points along the beam.
	 */
	public Set<SLInterceptionItem> intercept (double targetX, double targetY, double targetZ, SLBeam beam, Tree tree, ShiftItem shift) { // modified by GL at Feb 2013
		// change method signature
		Set<SLInterceptionItem> interceptionItems = new HashSet<SLInterceptionItem> ();

		Spatialized spa = (Spatialized) tree;
		SLLightableTree lightableTree = ((SLLightableTree) tree);
		SLTreeLight light = lightableTree.getTreeLight ();

		double xShift = shift.x - targetX; //The target coordinates will become the origine in the next computation
		double yShift = shift.y - targetY;
		double zShift = shift.z - targetZ;

		double elevation = beam.getHeightAngle_rad ();
		double azimuth = beam.getAzimut_rad ();

		for (SLCrownPart part : lightableTree.getCrownParts ()) {
			double[] r = part.intercept (xShift, yShift, zShift, elevation, azimuth);

			if (r == null) {
				// no interception
			} else {
				// This part intercepted the beam
				double pathLength = r[0];
				double distance = r[1];
				SLInterceptionItem item = new SLInterceptionItem (distance, pathLength, part, tree);
				interceptionItems.add (item);
			}
		}

		if (slSettings.trunkInterception) {
			SLTrunk trunk = lightableTree.getTrunk ();

			double[] r = trunk.intercept (xShift, yShift, zShift, elevation, azimuth);

			if (r == null) {
				// no interception
			} else {
				// This part intercepted the beam
				double pathLength = r[0];
				double distance = r[1];
				SLInterceptionItem item = new SLInterceptionItem (distance, pathLength, trunk, tree);
				interceptionItems.add (item);
			}

		}

		return interceptionItems;

	}

	public SLSettings getSettings () {
		return slSettings;
	}

	public SLBeamSet getBeamSet () {
		return beamSet;
	}

	// TO BE CHECKED with Benoit Courbaud
	public double getIncidentEnergy () {
		return cellInitialEnergySum;
	}

	/**
	 * Solves a quadratic equation, returns 0, 1 or 2 roots. Never returns null;
	 */
	static public double[] solveQuadraticEquation (double a, double b, double c) {
		double ro = b * b - 4 * a * c;
		if (ro > 0) { 				// 2 roots
			double root1 = (-b + Math.sqrt (ro)) / (2 * a);
			double root2 = (-b - Math.sqrt (ro)) / (2 * a);
			return new double[] {root1, root2};
		} else if (ro == 0) { 		// 1 root
			double root1 = (-b + Math.sqrt (ro)) / (2 * a);
			return new double[] {root1};
		} else { 					// no roots
			return new double[] {};
		}
	}

	// // TODO check the ordering of the interceptionItems from the farthest to the nearest.
	// // See processLighting () upper.
	// public static boolean testOrder = true;



}
