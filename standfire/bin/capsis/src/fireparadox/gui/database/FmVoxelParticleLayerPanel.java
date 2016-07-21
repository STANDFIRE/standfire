package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;

/**
 * FiVoxelParticleLayerPanel : Particles voxel DISPLAY panel
 *
 * @author I. Lecomte - October 2009
 */
public class FmVoxelParticleLayerPanel extends FmVoxelParticlePanel   {


	//to store edge voxel particles parameters
	protected FmDBShape sampleEdge;					//fuel object
	protected long sampleEdgeId;
	protected int nbEdgeParticle;
	protected Vector edgeParticleList;
	protected String []     edgeParticleName;		//particle names for edges
	protected double [][][] edgeBiomass;			//biomass for each type of edge voxel and each particle


	protected boolean edgeSelected = false;

	/**	Constructors */
	public FmVoxelParticleLayerPanel (FmModel _model, FmDBShape _sample, FmDBShape _sampleEdge, int _nbTypeMax) {

		//connecting database
		model= _model;
		bdCommunicator = model.getBDCommunicator ();

		sample = _sample;
		sampleEdge = _sampleEdge;
		typeSelected = 0;
		nbTypeMax = _nbTypeMax;

		//load values for sample
		if (sample != null) {
			sampleId = sample.getShapeId();
			razVoxelParticles ();
			loadVoxelParticles ();

		}
		//load values for sample edge
		if (sampleEdge != null) {
			sampleEdgeId = sampleEdge.getShapeId();
			razEdgeVoxelParticles ();
			loadEdgeVoxelParticles ();

		}

		createPanel ();
		changeSelect (typeSelected, false);

	 }

	/**	JTABLE creation
	 */
	@Override
	protected void createTable () {
		tableModel = new FiVoxelParticleTableModel(false);	//not editable
	}

	/**	Initialize the GUI.
	*/
	@Override
	protected void createPanel () {

		createTable ();
		colTable = new ColumnPanel (Translator.swap ("FiVoxelParticlePanel.title"));
		resultTable= new JTable();
		resultTable.setAutoCreateColumnsFromModel(false);
		resultTable.setModel(tableModel);

		//Column text
		for (int k = 0; k < 3; k++) {
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

			if(k==0) {
				renderer.setHorizontalAlignment(SwingConstants.LEFT);
				TableColumn tcolumns = new TableColumn(k, 150, renderer, null);
				resultTable.addColumn(tcolumns);
			}
			else  {
				renderer.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn tcolumns = new TableColumn(k, 100, renderer, null);
				resultTable.addColumn(tcolumns);
			}
		}

		scroll = new JScrollPane(resultTable);
		colTable.add (scroll);
		this.add (colTable, BorderLayout.CENTER);
	}


	/**	Load particles biomasses values in the Jtable
	 */
	@Override
	public void loadValues () {

		tableModel.clear();

		//no color for this voxel means no biomass allowed
		if (typeSelected > 0)  {
			if (!edgeSelected) {
				for (int np=0; np < nbParticle; np++) {
					if (particleName[np] != null)
						tableModel.add (particleName[np], biomass[np][typeSelected][0],
														  biomass[np][typeSelected][1]);
				}
			}
			else {
				for (int np=0; np < nbEdgeParticle; np++) {
					if (edgeParticleName[np] != null)
						tableModel.add (edgeParticleName[np], edgeBiomass[np][typeSelected][0],
														  edgeBiomass[np][typeSelected][1]);
				}
			}
		}
	}


	/**	Changing the selected type of voxel
	 */
	@Override
	public void changeSelect (int _type, boolean _edge) {
		typeSelected = _type;
		edgeSelected = _edge;
		loadValues ();
		resultTable = new JTable(tableModel);
		scroll = new JScrollPane(resultTable);
		repaint();
	}

	/**	RAZ particles for all types of voxels in the fuel
	 */
	protected void razEdgeVoxelParticles () {

		edgeParticleName = new String [nbParticleMax];
		edgeBiomass = new double [nbParticleMax][nbTypeMax][2];
		for (int part=0; part <nbParticleMax; part++) {
			edgeParticleName[part] = null;
			for (int type=0; type <nbTypeMax; type++) {
				edgeBiomass [part][type][0] = -9;
				edgeBiomass [part][type][1] = -9;
			}
		}

	}

	protected void loadEdgeVoxelParticles () {

		try {

			sampleEdge = bdCommunicator.getShapeVoxels (sampleEdge, false);	//to get all shape info


			HashMap voxels = sampleEdge.getVoxels ();
			edgeParticleList = new Vector();


			//For each voxels
			if (voxels != null) {
				for (Iterator t = voxels.keySet().iterator(); t.hasNext ();) {
					Object cle = t.next();
					FmDBVoxel voxel = (FmDBVoxel) voxels.get(cle);

					//type of voxel determine index in tables
					if (voxel.getVoxelType() != null) {
						FmVoxelType type = voxel.getVoxelType();
						int indexType = type.getTypeIndex();
						if (indexType >= 0) {

							HashMap particleMap = voxel.getParticleMap();
							if (particleMap != null) {

								//for each particle
								for (Object o : particleMap.values()) {

									//Store name
									FmDBParticle particle = (FmDBParticle) o;
									String name = particle.getName();
									Long partId = particle.getId();
									Long id = particle.getId();
									if (!edgeParticleList.contains (name)) {
										edgeParticleList.add (name);
										nbEdgeParticle++;
									}

									int part  = edgeParticleList.indexOf (name);
									if (part >=0) {

										edgeParticleName[part] = name;

										//for each parameter
										HashMap parameterMap = particle.getParameterMap();
										for (Object o2 : parameterMap.values()) {
											FmDBParameter parameter = (FmDBParameter) o2;

											storeEdgeBiomass (parameter, part, indexType, partId, particle.isAlive());
										}
									}
								}
							}

						}
						else System.out.println("ne doit pas arriver!!!");
					}
				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiVoxelParticleLayerPanel.loadVoxels() ", "error while opening FUEL data base", e);
		}

	}

	/*
	* STORE biomaas values in TABLES
	* COnvert kilos to grammes and rounding to 3 decimals
	*/

	protected void storeEdgeBiomass (FmDBParameter parameter, int part, int indexType, long partId, boolean alive) {

		String parameterName = parameter.getName();
		if (parameterName.equals("Biomass")) {
			double val = parameter.getValue();
			if (val > 0) {
				val = val * 1000.0;
				val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
			}

			if (alive) {
				edgeBiomass [part][indexType][0] = val;
			}
			else {
				edgeBiomass [part][indexType][1] = val;
			}
		}
	}

	public double [][][] getNewEdgeBiomass () {
		return edgeBiomass;
	}


	public int getNbEdgeParticle () {
		return nbEdgeParticle;
	}


}
