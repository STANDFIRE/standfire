package fireparadox.gui.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import jeeb.lib.util.Check;
import jeeb.lib.util.ListenedTo;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;


/**
 * FiVoxelParticleLayerCageEditor : LAYER Particles voxel edition for CAGE method
 *
 * @author I. Lecomte - January 2010
 */
public class FmVoxelParticleLayerCageEditor extends FmVoxelParticleCageEditor implements ListenedTo    {


	//to store EDGE voxel particles parameters
	private int xEdgeMax, yEdgeMax, zEdgeMax;
	private long   [][][][][] edgeParticleId;			//particle id 	for each particle and each type of EDGE voxel
	private long   [][][][][] edgeBiomassId;			//biomass  id 	for each particle and each type of EDGE voxel
	private double [][][][][] edgeBiomass;				//biomass 		for each particle and each type of EDGE voxel
	private double [][][][][] edgeOldBiomass;			//old biomass  	for each particle and each type of EDGE voxel
	private boolean edge;

	/**	Constructors */
	public FmVoxelParticleLayerCageEditor (FmModel _model, FmDBShape _shape, int _xMax, int _yMax, int _zMax,
											int _xEdgeMax, int _yEdgeMax, int _zEdgeMax) {

		super (_model, _shape,  _xMax,  _yMax,  _zMax);

		xEdgeMax = _xEdgeMax;
		yEdgeMax = _yEdgeMax;
		zEdgeMax = _zEdgeMax;

		//load values and create UI
		if (shape != null) {
			shapeId = shape.getShapeId();
			razVoxelParticles ();
			razEdgeVoxelParticles();
			loadVoxelParticles ();
			loadEdgeVoxelParticles ();
			createPanel ();
		}

	 }


	/**	Load particles biomasses values in the Jtable
	 */
	protected void loadValues () {

		tableModel.clear();

		if (colorSelected != 0)  {
			for (int np=0; np < nbParticle; np++) {
				if (particleName[np] != null)
				if (!edge) {
					tableModel.add (particleName[np], biomass[np][iSel][jSel][kSel][0],
													biomass[np] [iSel][jSel][kSel][1]);
				}
				else {
					tableModel.add (particleName[np], edgeBiomass[np][iSel][jSel][kSel][0],
													edgeBiomass[np] [iSel][jSel][kSel][1]);
				}
			}
		}
	}

	/**	Save particles biomasses values from the Jtable
	 */
	public void saveValues () {

		boolean update = false;


		//no color for this voxel means no biomass allowed
		if (nbParticle > 0) {
			for (int np=0; np < nbParticle; np++) {
				if (particleName[np] != null) {

					String alive = (String)  tableModel.getValueAt(np, 1);
					Double biomassAlive = -9d;
					if (Check.isDouble (alive)) {
						biomassAlive = Check.doubleValue (alive);
						if (biomassAlive < 0) biomassAlive = -9d;
					}

					String dead = (String)  tableModel.getValueAt(np, 2);
					Double biomassDead = -9d;
					if (Check.isDouble (dead)) {
						biomassDead = Check.doubleValue (dead);
						if (biomassDead < 0)  biomassDead = -9d;
					}

					if (!edge) {
						if (biomass[np][iSel][jSel][kSel][0] != biomassAlive)
							update = true;
						if (biomass[np][iSel][jSel][kSel][1] != biomassDead)
							update = true;
					}
					else {
						if (edgeBiomass[np][iSel][jSel][kSel][0] != biomassAlive)
							update = true;
						if (edgeBiomass[np][iSel][jSel][kSel][1] != biomassDead)
							update = true;
					}

				}

			}
		}

		if (update) {

			tellSomethingHappened (1);		//for HISTORY an undo redo

			if (nbParticle > 0) {
				for (int np=0; np < nbParticle; np++) {
					if (particleName[np] != null) {

						String alive = (String)  tableModel.getValueAt(np, 1);
						Double biomassAlive = -9d;
						if (Check.isDouble (alive)) {
							biomassAlive = Check.doubleValue (alive);
							if (biomassAlive < 0) biomassAlive = -9d;
						}

						String dead = (String)  tableModel.getValueAt(np, 2);
						Double biomassDead = -9d;
						if (Check.isDouble (dead)) {
							biomassDead = Check.doubleValue (dead);
							if (biomassDead < 0)  biomassDead = -9d;
						}
						if (!edge) {
							biomass[np][iSel][jSel][kSel][0] = biomassAlive;
							biomass[np][iSel][jSel][kSel][1] = biomassDead;
						}
						else {
							edgeBiomass[np][iSel][jSel][kSel][0] = biomassAlive;
							edgeBiomass[np][iSel][jSel][kSel][1] = biomassDead;
						}
					}
				}
			}

		}

	}

	/**	Changing the selected type of voxel
	 */
	protected void changeSelect (int _i, int _j, int _k, boolean _edge) {

		iSel = _i;
		jSel = _j;
		kSel = _k;
		edge = _edge;

		loadValues ();
		resultTable = new JTable(tableModel);
		scroll = new JScrollPane(resultTable);
		repaint();
	}


	/**	RAZ particles for all types of voxels in the fuel
	 */
	protected void razEdgeVoxelParticles () {

		edgeParticleId = new long [nbParticleMax][xEdgeMax][yEdgeMax][zEdgeMax][2];
		edgeBiomass = new double [nbParticleMax][xEdgeMax][yEdgeMax][zEdgeMax][2];
		edgeOldBiomass = new double [nbParticleMax][xEdgeMax][yEdgeMax][zEdgeMax][2];
		edgeBiomassId = new long [nbParticleMax][xEdgeMax][yEdgeMax][zEdgeMax][2];

		for (int np=0; np <nbParticleMax; np++) {

			particleName[np]= null;

			for (int i=0; i <xEdgeMax; i++) {
				for (int j=0; j <yEdgeMax; j++) {
					for (int k=0; k <zEdgeMax; k++) {

						edgeParticleId [np][i][j][k][0] = -1;
						edgeParticleId [np][i][j][k][1] = -1;

						edgeBiomassId [np][i][j][k][0] = -1;
						edgeBiomassId [np][i][j][k][1] = -1;
						edgeBiomass [np][i][j][k][0] = -9;
						edgeBiomass [np][i][j][k][1] = -9;
						edgeOldBiomass [np][i][j][k][0] = -9;
						edgeOldBiomass [np][i][j][k][1] = -9;

					}
				}
			}
		}
	}

	/**	Load particles for all types of voxels in the fuel
	 */
	protected void loadEdgeVoxelParticles () {


		HashMap edges = shape.getEdgeVoxels ();
		if (particleList == null) particleList= new Vector();


		//if there is already voxels in the shape
		if ((edges != null) && (edges.size() > 0)) {

			for (Iterator t = edges.keySet().iterator(); t.hasNext ();) {
				Object cle = t.next();
				FmDBVoxel voxel = (FmDBVoxel) edges.get(cle);

				//type of voxel determine index in tables
				int i = voxel.getI();
				int j = voxel.getJ();
				int k = voxel.getK();

				HashMap particleMap = voxel.getParticleMap();
				if (particleMap != null) {

					//for each particle
					for (Object o : particleMap.values()) {

						//store name in the list
						FmDBParticle particle = (FmDBParticle) o;
						String name = particle.getName();
						Long id = particle.getId();
						if (!particleList.contains (name)) {
							particleList.add (name);
							nbParticle++;

						}

						int np  = particleList.indexOf (name);
						if (np >=0) {

							//for each parameter
							HashMap parameterMap = particle.getParameterMap();
							for (Object o2 : parameterMap.values()) {
								FmDBParameter parameter = (FmDBParameter) o2;

								String parameterName = parameter.getName();
								if (parameterName.equals("Biomass")) {
									double val = parameter.getValue();
									long bid = parameter.getId();

									//convert to kilo and round to 3 decimals
									if (val > 0) {
										val = val * 1000.0;
										val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
									}

									particleName[np] = name;

									if (particle.isAlive()) {
										edgeBiomass [np][i][j][k][0] = val;
										edgeOldBiomass [np][i][j][k][0] = val;
										edgeBiomassId [np][i][j][k][0] = bid;
										edgeParticleId [np][i][j][k][0] = id;
									}
									else {
										edgeBiomass [np][i][j][k][1] = val;
										edgeOldBiomass [np][i][j][k][1] = val;
										edgeBiomassId [np][i][j][k][1] = bid;
										edgeParticleId [np][i][j][k][1] = id;
									}

								}
							}
						}
						else System.out.println("ne doit pas arriver!!!");
					}
				}

			}
		}

	}


	public long   [][][][][] getEdgeParticleId () {
		return edgeParticleId;
	}

	public long   [][][][][] getEdgeBiomassId() {
		return edgeBiomassId;
	}
	public double [][][][][] getNewEdgeBiomass() {
		return edgeBiomass;
	}
	public double [][][][][] getOldEdgeBiomass() {
		return edgeOldBiomass;
	}

	public void setEdgeBiomass (double [][][][][] _edgeBiomass) {
		edgeBiomass = _edgeBiomass;
	}

}
