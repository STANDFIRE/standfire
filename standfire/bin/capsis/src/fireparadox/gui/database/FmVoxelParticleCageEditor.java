package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;


/**
 * FiVoxelParticleCageEditor : Particles voxel edition for CAGE method
 *
 * @author I. Lecomte - October 2009
 */
public class FmVoxelParticleCageEditor extends JPanel implements ListenedTo    {

	protected FmModel model;

	//Table to display particles and biomasses values
	protected JScrollPane scroll;
	protected ColumnPanel colTable;
	protected JTable resultTable;
	protected FiVoxelParticleTableModel tableModel;

	//fuel information
	protected FmDBShape shape;						//fuel object
	protected long shapeId;							//fuel id in the database
	protected boolean isVoxels;

	//Entry fields for voxel particles update
	protected Vector particleList;
	protected Collection particles;
	protected int nbParticleMax = 30;
	protected int nbParticle;
	protected int xMax, yMax, zMax;
	protected int iSel, jSel, kSel;
	protected int colorSelected = 99;

	//to store voxel particles parameters
	protected String [] particleName;			//name    for each  voxel and each particle
	protected long   [][][][][] particleId;			//particle id 	for each particle and each type of voxel
	protected long   [][][][][] biomassId;			//biomass  id 	for each particle and each type of voxel
	protected double [][][][][] biomass;				//biomass 		for each particle and each type of voxel
	protected double [][][][][] oldBiomass;			//old biomass  	for each particle and each type of voxel

	// ListenedTo interface
	protected HashSet<Listener> listeners;

	/**	Constructors */

	public FmVoxelParticleCageEditor (FmModel _model, FmDBShape _shape, int _xMax, int _yMax, int _zMax) {

		model = _model;

		shape = _shape;
		xMax = _xMax;
		yMax = _yMax;
		zMax = _zMax;

		iSel = -1;
		jSel = -1;
		kSel = -1;

	}
	public FmVoxelParticleCageEditor (FmModel _model, FmDBShape _shape, int _xMax, int _yMax, int _zMax,
									double [][][][][] _biomass, Vector _particleList) {

		model = _model;

		shape = _shape;
		xMax = _xMax;
		yMax = _yMax;
		zMax = _zMax;


		iSel = -1;
		jSel = -1;
		kSel = -1;


		//load values and create UI
		if (shape != null) {
			shapeId = shape.getShapeId();
			razVoxelParticles ();
			loadVoxelParticles ();
			if ((!isVoxels) && (_biomass != null)) {
				biomass = _biomass;
				particleList = _particleList;
				initFrom2D();
			}
			createPanel ();
		}
	 }

	/**	JTABLE creation
	 */
	protected void createTable () {
		tableModel = new FiVoxelParticleTableModel ();
	}

	/**	Initialize the GUI.
	*/
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
	protected void loadValues () {

		tableModel.clear();

		if (colorSelected != 0)  {
			for (int np=0; np < nbParticle; np++) {
				if (particleName[np] != null)
					tableModel.add (particleName[np], biomass[np][iSel][jSel][kSel][0],
													biomass[np] [iSel][jSel][kSel][1]);
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


					if (biomass[np][iSel][jSel][kSel][0] != biomassAlive)
						update = true;
					if (biomass[np][iSel][jSel][kSel][1] != biomassDead)
						update = true;

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

						biomass[np][iSel][jSel][kSel][0] = biomassAlive;
						biomass[np][iSel][jSel][kSel][1] = biomassDead;


					}
				}
			}

		}

	}

	/**	Changing the selected type of voxel
	 */
	public void changeSelect (int _i, int _j, int _k) {

		iSel = _i;
		jSel = _j;
		kSel = _k;

		loadValues ();
		resultTable = new JTable(tableModel);
		scroll = new JScrollPane(resultTable);
		repaint();
	}

	/**	Changing the selected type of voxel
	 */
	public void setColor (int _color) {
		colorSelected = _color;
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

	/**	RAZ particles for all types of voxels in the fuel
	 */
	protected void razVoxelParticles () {

		particleName = new String [nbParticleMax];
		biomass = new double [nbParticleMax][xMax][yMax][zMax][2];
		oldBiomass = new double [nbParticleMax][xMax][yMax][zMax][2];

		particleId = new long [nbParticleMax][xMax][yMax][zMax][2];
		biomassId = new long [nbParticleMax][xMax][yMax][zMax][2];

		for (int np=0; np <nbParticleMax; np++) {

			particleName[np]= null;

			for (int i=0; i <xMax; i++) {
				for (int j=0; j <yMax; j++) {
					for (int k=0; k <zMax; k++) {

						particleId [np][i][j][k][0] = -1;
						biomassId [np][i][j][k][0] = -1;
						particleId [np][i][j][k][1] = -1;
						biomassId [np][i][j][k][1] = -1;
						biomass [np][i][j][k][0] = -9;
						biomass [np][i][j][k][1] = -9;
						oldBiomass [np][i][j][k][0] = -9;
						oldBiomass [np][i][j][k][1] = -9;

					}
				}
			}
		}
	}

	/**	Load particles for all types of voxels in the fuel
	 */
	protected void loadVoxelParticles () {


		HashMap voxels = shape.getVoxels ();
		particleList= new Vector();
		isVoxels = false;

		//if there is already voxels in the shape
		if ((voxels != null) && (voxels.size() > 0)) {

			isVoxels = true;
			for (Iterator t = voxels.keySet().iterator(); t.hasNext ();) {
				Object cle = t.next();
				FmDBVoxel voxel = (FmDBVoxel) voxels.get(cle);

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
										biomass [np][i][j][k][0] = val;
										oldBiomass [np][i][j][k][0] = val;
										biomassId [np][i][j][k][0] = bid;
										particleId [np][i][j][k][0] = id;
									}
									else {
										biomass [np][i][j][k][1] = val;
										oldBiomass [np][i][j][k][1] = val;
										biomassId [np][i][j][k][1] = bid;
										particleId [np][i][j][k][1] = id;
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

	protected void initFrom2D() {
		nbParticle = 0;
		if (particleList!= null) {
			for (Iterator t = particleList.iterator(); t.hasNext ();) {
				String part = (String) t.next();
				particleName[nbParticle] = part;
				nbParticle++;
			}
		}
		else particleList= new Vector();

	}

	//Return the all particle for this type
	public Vector<String>  getVoxelParticle (int i, int j, int k) {
		Vector<String> voxelParticle = new Vector<String> ();
		for (int part=0; part<nbParticle; part++) {
			if ((biomass[i][j][k][part][0] >= 0) || (biomass[i][j][k][part][1] >= 0)) {
				String particleName 	= (String) particleList.get(part);
				voxelParticle.add (particleName);
			}
		}
		return voxelParticle;
	}

	public int getNbParticle () {
		return nbParticle;
	}
	public String   [] getParticleName () {
		return particleName;
	}
	public long   [][][][][] getParticleId () {
		return particleId;
	}
	public long   [][][][][] getBiomassId() {
		return biomassId;
	}
	public double [][][][][] getNewBiomass() {
		return biomass;
	}
	public double [][][][][] getOldBiomass() {
		return oldBiomass;
	}

	public void setBiomass (double [][][][][] _biomass) {
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

	/**
	 * FiVoxelFiVoxelParticleTableModel : Internam classes for display particle in a JTABLE
	 */
	 protected class ParticleData
		{
		public String p_name;
		public String p_alive;
		public String p_dead;

		public ParticleData (String name, double alive, double dead)
		{
			p_name = new String (name);
			Double d = new Double(alive);
			p_alive = new String (d.toString());
			d = new Double(dead);
			p_dead = new String (d.toString());
		}
	}

	protected class ColumnData
	{
		public String m_titre;
		public ColumnData (String titre) {m_titre = titre;}
	}

	protected class FiVoxelParticleTableModel extends AbstractTableModel
	{
		public ColumnData columnNames [] = {
			new ColumnData(Translator.swap ("FiVoxelParticlePanel.particle")),
			new ColumnData(Translator.swap ("FiVoxelParticlePanel.alive")),
			new ColumnData(Translator.swap ("FiVoxelParticlePanel.dead")),

		};

		protected Vector particle_vector;

		public FiVoxelParticleTableModel () {
			particle_vector = new Vector();
			setDefaultData();
		}


		public void setDefaultData() {
			if (particle_vector == null) particle_vector = new Vector ();
			particle_vector.removeAllElements();
		}
		public int getRowCount() {return particle_vector == null ? 0 : particle_vector.size();}
		public int getColumnCount() {return columnNames.length;}
		@Override
		public String getColumnName(int column) {return columnNames[column].m_titre;}
		@Override
		public boolean isCellEditable(int row, int col) {
			if (col > 0) return true;
			else return false;
		}

		public Object getValueAt(int row, int col) {
			if (row < 0 || row > getRowCount()) return "";
			ParticleData data = (ParticleData) particle_vector.elementAt(row);
				if (col == 0) return data.p_name;
				if (col == 1) return data.p_alive;
				if (col == 2) return data.p_dead;
			return "";
		}
		@Override
		public void setValueAt(Object object, int row, int col) {
			if (row < 0 || row > getRowCount()) return;
			if (!Check.isDouble(object.toString())) {
				return;
			}
			ParticleData data = (ParticleData) particle_vector.elementAt(row);
			if (col == 0) data.p_name = String.valueOf(object);
			if (col == 1) data.p_alive = String.valueOf(object);
			if (col == 2) data.p_dead = String.valueOf(object);

			return;
		}

		public void add (String name, double alive, double dead) {
								particle_vector.addElement(new ParticleData(
										name,alive, dead));
		}
		public void clear() {
			particle_vector.removeAllElements ();
			this.fireTableDataChanged ();
		}



	}

}
