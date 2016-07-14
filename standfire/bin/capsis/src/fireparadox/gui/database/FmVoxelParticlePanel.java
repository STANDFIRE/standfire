package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.util.HashMap;
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
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;

/**
 * FiVoxelParticlePanel : Particles voxel DISPLAY panel
 *
 * @author I. Lecomte - October 2009
 */
public class FmVoxelParticlePanel extends JPanel   {

	protected FmModel model;
	protected FmDBCommunicator bdCommunicator;		//to read database

	//Table to display particles and biomasses values
	protected JScrollPane scroll;
	protected ColumnPanel colTable;
	protected JTable resultTable;
	protected FiVoxelParticleTableModel tableModel;

	//fuel information
	protected FmDBShape sample;						//fuel object
	protected long sampleId;							//fuel id in the database

	//Entry fields for voxel particles update
	protected int nbParticleMax = 30;
	protected int nbTypeMax;
	protected int typeSelected;					//0=no 1=top 2=center 3=bottom

	//to store voxel particles parameters
	protected int nbParticle;
	protected Vector particleList;
	protected String []     particleName;		//name of each particle
	protected double [][][] biomass;			//biomass for each type of voxel and each particle


	public FmVoxelParticlePanel () {}

	/**	Constructors */
	public FmVoxelParticlePanel (FmModel _model, FmDBShape _sample, int _nbTypeMax) {

		//connecting database
		model= _model;
		bdCommunicator = model.getBDCommunicator ();

		sample = _sample;
		typeSelected = 0;
		nbTypeMax = _nbTypeMax;

		//load values and create UI
		if (sample != null) {
			sampleId = sample.getShapeId();
			razVoxelParticles ();
			loadVoxelParticles ();
			createPanel ();
		}
	 }

	/**	JTABLE creation
	 */
	protected void createTable () {
		tableModel = new FiVoxelParticleTableModel(false);	//not editable
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
	public void loadValues () {

		tableModel.clear();

		//no color for this voxel means no biomass allowed
		if (typeSelected > 0)  {
			for (int np=0; np < nbParticle; np++) {
				if (particleName[np] != null)
					tableModel.add (particleName[np], biomass[np][typeSelected][0],
													  biomass[np][typeSelected][1]);
			}
		}
	}

	/**	Changing the selected type of voxel
	 */
	public void changeSelect (int _type, boolean _edge) {
		typeSelected = _type;
		loadValues ();
		resultTable = new JTable(tableModel);
		scroll = new JScrollPane(resultTable);
		repaint();
	}


	/**	RAZ particles for all types of voxels in the fuel
	 */
	protected void razVoxelParticles () {
		particleName = new String [nbParticleMax];
		biomass = new double [nbParticleMax][nbTypeMax][2];
		for (int part=0; part <nbParticleMax; part++) {
			particleName[part] = null;
			for (int type=0; type <nbTypeMax; type++) {
				biomass [part][type][0] = -9;
				biomass [part][type][1] = -9;
			}
		}
	}


	/**	Load particles for all types of voxels in the fuel
	 */
	protected void loadVoxelParticles () {

		try {

			sample = bdCommunicator.getShapeVoxels (sample, false);	//to get all shape info


			HashMap voxels = sample.getVoxels ();
			particleList= new Vector();


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
									if (!particleList.contains (name)) {
										particleList.add (name);
										nbParticle++;
									}

									int part  = particleList.indexOf (name);
									if (part >=0) {

										particleName[part] = name;

										//for each parameter
										HashMap parameterMap = particle.getParameterMap();
										for (Object o2 : parameterMap.values()) {
											FmDBParameter parameter = (FmDBParameter) o2;

											storeBiomass (parameter, part, indexType, partId, particle.isAlive());
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
			Log.println (Log.ERROR, "FiVoxelParticlePanel.loadVoxels() ", "error while opening FUEL data base", e);
		}

	}

	/*
	* STORE biomass values in TABLES
	* COnvert kilos to grammes and rounding to 3 decimals
	*/
	protected void storeBiomass (FmDBParameter parameter, int part, int indexType, long partId, boolean alive) {

		String parameterName = parameter.getName();
		if (parameterName.equals("Biomass")) {
			double val = parameter.getValue();
			if (val > 0) {
				val = val * 1000.0;
				val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
			}
			if (alive) {
				biomass [part][indexType][0] = val;
			}
			else {
				biomass [part][indexType][1] = val;
			}
		}
	}

	/**
	 * Return biomass for each type and particle
	 */
	public double [][][] getNewBiomass () {
		return biomass;
	}

	public int getNbParticle () {
		return nbParticle;
	}

	/**
	 * Return biomass for each type and particle
	 */
	public String [] getParticleNames () {
		return particleName;
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
		protected boolean isEditable;

		public FiVoxelParticleTableModel (boolean _isEditable) {
			particle_vector = new Vector();
			isEditable = _isEditable;
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
			if ((isEditable) && (col > 0)) return true;
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
							particle_vector.addElement(new ParticleData (name, alive, dead));
		}
		public void clear() {
			particle_vector.removeAllElements ();
			this.fireTableDataChanged ();
		}

	}

}
