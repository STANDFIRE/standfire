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

package capsis.lib.fire.intervener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * FiIntervener with retention: superclass for intervener for which activity fuel can be retended (through a LayerSet)
 * 
 * @author F. Pimont - 19/11/2015
 */
public class FiIntervenerWithRetention  {
	protected Collection concernedTrees;	// Intervener will be ran on these trees only (maybe all, maybe a group)
	protected FiLayerSet concernedLayerSet;
	
	protected GScene stand; 
	protected GModel model; 
	
	protected boolean activityFuelRetention; // fuel transferred to a layerSet or removed
	private double residualFuelHeight; // height of the slash fuel, when not removed
	private double residualFuelCoverFraction; // coverFraction of the slash fuel, when not removed
	private double residualFuelCharacteristicSize; // clump of the slash fuel, when not removed
	private double residualFuelMoisture; // moisture content (in %)
	
	protected FiLayerSet layerSet;
	protected FiLayer layer;
	
	public final static double HEIGHT = 0.05; //m
	public final static double COVERFRACTION = 1.0; //fuel cover
	public final static double CHARACTERISTICSIZE = 0.5; // 50 CM
	public final static double MOISTURE = 5.0; //%

	
	public FiIntervenerWithRetention() {
	}
	
	/**
	 * for script mode
	 */
	public FiIntervenerWithRetention(boolean activityFuelRetention, double residualFuelHeight, double residualFuelCoverFraction, double residualFuelCharacteristicSize, double residualFuelMoisture) {
		this.activityFuelRetention = activityFuelRetention;
		this.residualFuelHeight = residualFuelHeight;
		this.residualFuelCoverFraction = residualFuelCoverFraction;
		this.residualFuelCharacteristicSize = residualFuelCharacteristicSize;
		this.residualFuelMoisture = residualFuelMoisture;
	}
	
	
	public void setResidualFuelProperties(FiIntervenerWithRetentionDialog dlg) {
		activityFuelRetention = dlg.isActivityFuelRetented();
		residualFuelHeight = dlg.getResidualFuelHeight();
		residualFuelCoverFraction = dlg.getResidualFuelCoverFraction();
		residualFuelCharacteristicSize = dlg.getResidualFuelCharacteristicSize();
		residualFuelMoisture = dlg.getResidualFuelMoisture();
	}
	/**
	 * Method to build the LayerSet in which Litter will be retented
	 * This method is called only when the model is a FiModel (since it should contains a fuelitem)
	 * @param fi
	 */
	public void buildLayerSet() {
		FiSpecies defaultSpecies = ((FiModel) model).getSettings().getSpeciesMap().get(FiSpecies.DEFAULT);
		layer = new FiLayer(FiLayer.LITTER, this.residualFuelHeight, 0d, residualFuelCoverFraction, residualFuelCharacteristicSize,0, defaultSpecies);
		 if (concernedLayerSet == null) { // The intervention deals with the collection of trees concernedTrees
			 layerSet = new FiLayerSet (((FiStand) stand).maxId);	
			((FiStand) stand).maxId++;
			 List<Vertex3d> vertices = new ArrayList<Vertex3d> ();
				vertices.add(new Vertex3d(stand.getOrigin()));
				vertices.add(new Vertex3d (stand.getOrigin().x + stand.getXSize(), stand.getOrigin().y, stand.getOrigin().z));
				vertices.add(new Vertex3d (stand.getOrigin().x + stand.getXSize(), stand.getOrigin().y+ stand.getYSize(), stand.getOrigin().z));
				vertices.add(new Vertex3d (stand.getOrigin().x, stand.getOrigin().y+ stand.getYSize(), stand.getOrigin().z));
				Polygon p = new Polygon (vertices);
				// unique layerSet for understoreyFuelOption==0 (SVS data for understorey);
				layerSet.updateToMatch (p);
		 } else {
			 layerSet = concernedLayerSet;
		 }
	}
}
