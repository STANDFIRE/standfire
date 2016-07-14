package fireparadox.gui.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import jeeb.lib.util.Check;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBShape;

/**
 * FiVoxelParticleEditor : Particles voxel EDITOR panel
 *
 * @author I. Lecomte - October 2009
 */
public class FmVoxelParticleEditor extends FmVoxelParticlePanel  implements ListenedTo  {


	//to store voxel particles parameters
	protected long   [][][] particleId;			//particle id 	for each particle and each type of voxel
	protected long   [][][] biomassId;			//biomass  id 	for each particle and each type of voxel
	protected double [][][] oldBiomass;			//old biomass  	for each particle and each type of voxel

	// ListenedTo interface
	private HashSet<Listener> listeners;


	/**	Constructors */
	public FmVoxelParticleEditor (FmModel _model,  FmDBShape _sample, int _nbTypeMax) {

		super (_model, _sample, _nbTypeMax);

	 }
	/**	JTABLE creation
	 */
	@Override
	protected void createTable () {
		tableModel = new FiVoxelParticleTableModel(true);	//editable
	}

	/**	Adding a new particle
	 */
	public void addParticleAction () {

		saveValues ();

		FmParticlesAdding dial  = new FmParticlesAdding (model,  particleList);
		Vector <String> newParticleList = dial.getNewParticleList() ;

		//For each voxels
		for (Iterator t = newParticleList.iterator(); t.hasNext ();) {
			String name = (String) t.next();

			//fill particle name list with new names
			if (!particleList.contains (name)) {
				particleList.add (name);
				particleName[nbParticle] = name;
				nbParticle++;
			}
		}

		loadValues ();
	}

	/**	Save particles biomasses values from the Jtable
	 */
	public void saveValues () {

		boolean update = false;



		//no color for this voxel means no biomass allowed
		if ((nbParticle > 0) && (typeSelected > 0)) {
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

					if (biomass[np][typeSelected][0] != biomassAlive)
						update = true;
					if (biomass[np][typeSelected][1] != biomassDead)
						update = true;

				}
			}
		}

		if (update) {

			tellSomethingHappened (1);

			//no color for this voxel means no biomass allowed
			if ((nbParticle > 0) && (typeSelected > 0)) {
				for (int np=0; np < nbParticle; np++) {
					if (particleName[np] != null) {

						String alive = (String)  tableModel.getValueAt(np, 1);
						Double biomassAlive = -9d;
						if (Check.isDouble (alive)) {
							biomassAlive = Check.doubleValue (alive);
							if (biomassAlive < 0)   biomassAlive = -9d;
						}



						String dead = (String)  tableModel.getValueAt(np, 2);
						Double biomassDead = -9d;
						if (Check.isDouble (dead)) {
							biomassDead = Check.doubleValue (dead);
							if (biomassDead < 0)  biomassDead = -9d;
						}

						biomass[np][typeSelected][0] = biomassAlive;
						biomass[np][typeSelected][1] = biomassDead;
					}
				}
			}
		}
	}

	/**	RAZ particles for all types of voxels in the fuel
	 */
	@Override
	protected void razVoxelParticles () {
		particleName = new String [nbParticleMax];
		particleId = new long [nbParticleMax][nbTypeMax][2];
		biomassId = new long [nbParticleMax][nbTypeMax][2];
		biomass = new double [nbParticleMax][nbTypeMax][2];
		oldBiomass = new double [nbParticleMax][nbTypeMax][2];

		for (int part=0; part <nbParticleMax; part++) {
			particleName[part] = null;
			for (int type=0; type <nbTypeMax; type++) {
				particleId [part][type][0] = -1;
				biomassId [part][type][0] = -1;
				particleId [part][type][1] = -1;
				biomassId [part][type][1] = -1;
				biomass [part][type][0] = -9;
				biomass [part][type][1] = -9;
				oldBiomass [part][type][0] = -9;
				oldBiomass [part][type][1] = -9;

			}
		}
	}

	/*
	* STORE biomaas values in TABLES
	* COnvert kilos to grammes and rounding to 3 decimals
	*/
	@Override
	protected void storeBiomass (FmDBParameter parameter, int part, int indexType, long partId, boolean alive) {

		String parameterName = parameter.getName();
		if (parameterName.equals("Biomass")) {
			double val = parameter.getValue();
			long bid = parameter.getId();
			if (val > 0) {
				val = val * 1000.0;
				val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
			}


			if (alive) {
				biomass [part][indexType][0] = val;
				oldBiomass [part][indexType][0] = val;
				biomassId [part][indexType][0] = bid;
				particleId [part][indexType][0] = partId;
			}
			else {
				biomass [part][indexType][1] = val;
				oldBiomass [part][indexType][1] = val;
				biomassId [part][indexType][1] = bid;
				particleId [part][indexType][1] = partId;

			}
		}
	}


	public long   [][][] getParticleId () {
		return particleId;
	}
	public long   [][][] getBiomassId() {
		return biomassId;
	}

	/**
	 * Return biomass for each type and particle
	 */
	@Override
	public double [][][] getNewBiomass () {
		return biomass;
	}
	public double [][][] getOldBiomass() {
		return oldBiomass;
	}
	public void setBiomass (double [][][] _biomass) {
		biomass = _biomass;
	}

	/**	Add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new HashSet<Listener> ();}
		listeners.add (l);
	}

	/**	Remove a listener to this object.
	*/
	public void removeListener (Listener l) {
		if (listeners == null) {return;}
		listeners.remove (l);
	}

	/**	Notify all the listeners by calling their somethingHappened (listenedTo, param) method.
	*/
	public void tellSomethingHappened (Object evt) {
		if (listeners == null) {return;}

		for (Listener l : listeners) {
			try {
				l.somethingHappened (this, evt);
			} catch (Exception e) {
				Log.println (Log.ERROR, "FiVoxel2DPanel.tellSomethingHappened ()",
						"listener caused the following exception, passed: "+l, e);
			}
		}
	}

}
