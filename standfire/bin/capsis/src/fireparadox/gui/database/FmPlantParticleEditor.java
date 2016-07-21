package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCheck;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;
import fireparadox.model.database.FmDBVoxel;

/**
 * FiPlantParticleEditor : Particles parameters for plants EDITOR
 *
 * @author I. Lecomte - October 2009
 */
public class FmPlantParticleEditor  extends AmapDialog implements  ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;	//to read database
	private FmDBUpdator bdUpdator;				//to update database

	//fuel information
	private FmDBPlant plant;					//plant object
	private long plantId;						//plant id in the database
	private LinkedHashMap<Long, FmDBPlant> plantMap;

	//Table to display particles and biomasses values
	private JScrollPane scroll;
	private ColumnPanel colTable;
	private JTable resultTable;
	private FiPlantParticleTableModel tableModel;

	//to store different particles
	private Vector<String> plantParticleNameList;
	private Collection plantParticles;
	private int nbParticleMax = 30;
	private int nbParticle;

	//to store different parameter
	private Vector<String> plantParamNameList;
	private Vector<String> plantParamDisplayList;
	private JComboBox paramComboBox;
	private int nbParamMax = 6;
	private int paramSelected;

	//to store plant particles parameters
	private long   [][][] particleIdTable;		//id of the particle for each particle/parameter/state
	private long   [][][] paramIdTable;			//id of the parameter for each particle/parameter/state
	private double [][][] newValues;			//new parameter value for each particle/parameter/state
	private double [][][] oldValues;			//old parameter value for each particle/parameter/state

	//to check parameter values
	private ArrayList<FmDBCheck> checkList;

	//Validation control
	private JButton save;
	private JButton cancel;
	private JButton help;


	/**	Constructors */
	public FmPlantParticleEditor (FmModel _model, FmDBPlant _plant, LinkedHashMap<Long, FmDBPlant> _plantMap) {

		model = _model;
		plant = _plant;
		plantMap = _plantMap;
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();


		//load values and create UI
		if (plant != null) {
			plantId = plant.getPlantId();
			loadPlantParticles ();
			createUI ();
			paramSelected = 0;
			loadValues () ;
		}

		show ();
	 }

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (save)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource ().equals (paramComboBox)) {
			selectParam ();
		}
	}

	/**	Parameter selection
	 */
	private void selectParam() {
		saveValues ();
		paramSelected = paramComboBox.getSelectedIndex();
		loadValues();
		repaint();
	}

	/**	JTABLE creation
	 */
	private void createTable () {
		tableModel = new FiPlantParticleTableModel(true);	//editable
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		/*********** plant info and param selector panel **************/
		ColumnPanel p1 = new ColumnPanel() ;

		FmPlantInfoPanel fuelInfoPanel = new FmPlantInfoPanel (plant);

		paramComboBox = new JComboBox ();
		paramComboBox.addItem ("-------------");
		for (Iterator i = plantParamDisplayList.iterator(); i.hasNext ();) {
			String name = (String) i.next();
			paramComboBox.addItem (name);
		}
		paramComboBox.addActionListener (this);

		p1.add(fuelInfoPanel);
		p1.add(paramComboBox);


		createTable ();
		colTable = new ColumnPanel (Translator.swap ("FiPlantParticleEditor.title"));
		resultTable= new JTable();
		resultTable.setAutoCreateColumnsFromModel(false);
		resultTable.setModel(tableModel);

		//Column text
		for (int k = 0; k < 4; k++) {
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
		resultTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

		scroll = new JScrollPane(resultTable);
		colTable.add (scroll);

		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		save = new JButton (Translator.swap ("FiPlantParticleEditor.validate"));
		save.addActionListener (this);
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (p1, BorderLayout.NORTH);
		getContentPane ().add (colTable, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlantParticleEditor.title"));

		setSize (300,400);

		setModal (true);

	}

	/**	Load particles biomasses values in the Jtable
	 */
	private void loadValues () {

		tableModel.clear();

		for (int part=0; part < nbParticle; part++) {
			String partName = plantParticleNameList.get (part);
			if (paramSelected > 0) {
					String paramName = plantParamDisplayList.get (paramSelected-1);

					tableModel.add (partName, paramName,
									newValues[part][paramSelected-1][0],
					newValues[part][paramSelected-1][1]);
			}
			else {
				for (int param=0; param < nbParamMax; param++) {
					String paramName = plantParamDisplayList.get (param);

					tableModel.add (partName, paramName,
									newValues[part][param][0],
									newValues[part][param][1]);
				}
			}
		}
	}

	/**	Save particles parameters values from the Jtable
	 */
	public void saveValues () {

		for (int part=0; part < nbParticle; part++) {

			if (paramSelected > 0) {
				newValues[part][paramSelected-1][0] = ((Double) tableModel.getValueAt(part, 2)). doubleValue();
				newValues[part][paramSelected-1][1] = ((Double) tableModel.getValueAt(part, 3)). doubleValue();
			}
			else {
				for (int param=0; param < nbParamMax; param++) {
					int index = param + (part * nbParamMax);
					newValues[part][param][0] = ((Double) tableModel.getValueAt(index, 2)). doubleValue();
					newValues[part][param][1] = ((Double) tableModel.getValueAt(index, 3)). doubleValue();
				}
			}
		}
	}

	/**	Load particles for the entire plant
	*/
	private void loadPlantParticles () {

		//load parameters list
		plantParamNameList= new Vector<String> ();
		plantParamNameList.add("MVR");
		plantParamNameList.add("SVR");
		plantParamNameList.add("AC");
		plantParamNameList.add("MC");
		plantParamNameList.add("HCV");
		plantParamNameList.add("Size");

		plantParamDisplayList= new Vector<String> ();
		plantParamDisplayList.add("MVR kg/m3");
		plantParamDisplayList.add("SVR m2/m3");
		plantParamDisplayList.add("AC g/100g");
		plantParamDisplayList.add("MC %");
		plantParamDisplayList.add("HCV KJ/kg");
		plantParamDisplayList.add("Size mm");


		//PLANT PARTICLES : RAZ table values and ids
		particleIdTable = new long [nbParticleMax][nbParamMax][2];
		paramIdTable = new long [nbParticleMax][nbParamMax][2];
		newValues = new double [nbParticleMax][nbParamMax][2];
		oldValues = new double [nbParticleMax][nbParamMax][2];

		for (int npa=0; npa<nbParticleMax; npa++) {
			for (int np=0; np<nbParamMax; np++) {
				particleIdTable[npa][np][0] = -1;
				particleIdTable[npa][np][1] = -1;
				paramIdTable[npa][np][0] = -1;
				paramIdTable[npa][np][1] = -1;
				oldValues[npa][np][0] = -9;
				oldValues[npa][np][1] = -9;
				newValues[npa][np][0] = -9;
				newValues[npa][np][1] = -9;
			}
		}



		//Load particles name list attached to the plant
		//particle are defined in shapes (biomass)

		Collection plantParticles = null;
		plantParticleNameList = new Vector<String> ();

		FmDBShape firstShape = null;
		FmDBShape sample = null;

		try {

			checkList = model.getCheckList();

			LinkedHashMap<Long, FmDBShape> listAllFuel = bdCommunicator.getPlantShapes (plant, 0);

			for (Iterator i = listAllFuel.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBShape f = listAllFuel.get(cle);
				if (firstShape == null) firstShape = f;
				if (f.getFuelType() >= 3) {
					sample = f;
				}

			}
			//if there is a sample, the particle list is attached to it
			if (sample != null)
				firstShape = bdCommunicator.getShapeVoxels (sample, false);
			else if (firstShape != null)
				firstShape = bdCommunicator.getShapeVoxels (firstShape, false);

			//if there is no shape, no particle !
			if (firstShape != null) {
				HashMap <Long, FmDBVoxel> voxelMap = firstShape.getVoxels();
				if (voxelMap != null) {

					//for each voxel
					for (Iterator v = voxelMap.keySet().iterator (); v.hasNext ();) {
						Object cle = v.next();
						FmDBVoxel voxel = voxelMap.get(cle);

						HashMap <Long, FmDBParticle> particleMap = voxel.getParticleMap();

						//for each voxel particle
						for (Iterator j = particleMap.keySet().iterator (); j.hasNext ();) {

							Object cle2 = j.next();
							//get particle info
							FmDBParticle particle = particleMap.get(cle2);
							String particleName = particle.getName();
							Long particleId = particle.getId();
							HashMap parameterMap = particle.getParameterMap();


							//fill particle name list for combo box
							if (!plantParticleNameList.contains (particleName)) {
								plantParticleNameList.add (particleName);
								nbParticle++;
							}
						}
					}

				}
			}

			//Load particles values attached to the plant
			plantParticles = bdCommunicator.getPlantParticles (plantId);
			if (plantParticles != null) {

				//for each plant particle
				for (Iterator j = plantParticles.iterator (); j.hasNext ();) {

					//get particle info
					FmDBParticle particle = (FmDBParticle) j.next ();
					String particleName = particle.getName();
					Long particleId = particle.getId();
					HashMap parameterMap = particle.getParameterMap();

					//fill particle name list for combo box
					if (!plantParticleNameList.contains (particleName)) {
						plantParticleNameList.add (particleName);
						nbParticle++;
					}
					int np  = plantParticleNameList.indexOf (particleName);

					//for each parameter stored for this plant particle
					for (Object o : parameterMap.values()) {
						FmDBParameter parameter = (FmDBParameter) o;

						String paramName = parameter.getName();
						Long paramId = parameter.getId();
						double val = parameter.getValue();
						int npa = plantParamNameList.indexOf (paramName);

						if ((np >=0) && (npa >= 0) && (np < nbParticleMax) && (npa < nbParamMax)) {

							//store values in local table
							if (particle.isAlive()) {
								particleIdTable[np][npa][0] = particleId;
								paramIdTable[np][npa][0] = paramId;
								newValues [np][npa][0] = val;
								oldValues [np][npa][0] = val;
							}
							else {
								particleIdTable[np][npa][1] = particleId;
								paramIdTable[np][npa][1] = paramId;
								newValues [np][npa][1] = val;
								oldValues [np][npa][1] = val;
							}
						}
					}
				}
			}


			//to get existing particle ids for empty parameters values
			for (int npa=0; npa<nbParticle; npa++) {
				for (int np=0; np<nbParamMax; np++) {
					if ((particleIdTable[npa][np][0] == -1) || (particleIdTable[npa][np][1] == -1)) {
						for (int p=0; p<nbParamMax; p++) {
							if ((particleIdTable[npa][np][0] == -1) && (particleIdTable[npa][p][0] != -1)) {
								particleIdTable[npa][np][0] = particleIdTable[npa][p][0];
							}
							if ((particleIdTable[npa][np][1] == -1) && (particleIdTable[npa][p][1] != -1)) {
								particleIdTable[npa][np][1] = particleIdTable[npa][p][1];
							}
						}
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantParticleEditor.loadValues() ", "error while opening FUEL data base", e);

		}
	}
	/**	check values with database threshold values
	*/
	private boolean checkValues () {

		for (int npa=0; npa<nbParticle; npa++) {
			for (int np=0; np<nbParamMax; np++) {

				String particleName = plantParticleNameList.get(npa);
				String paramName 	= plantParamNameList.get(np);


				for (FmDBCheck c : checkList) {



					if ((c.getParticleName().equals(particleName)) && (c.getParameterName().equals(paramName))) {
						if (newValues[npa][np][0] > 0) {
							if (newValues[npa][np][0] < c.getErrorMin())   {
								JOptionPane.showMessageDialog (this, particleName+":"+paramName+":"+Translator.swap ("FiPlantParticleEditor.aliveErrorMin")+"="+c.getErrorMin(),
								Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
								return false;
							}
							if (newValues[npa][np][0] > c.getErrorMax()) {
								JOptionPane.showMessageDialog (this, particleName+":"+paramName+":"+Translator.swap ("FiPlantParticleEditor.aliveErrorMax")+"="+c.getErrorMax(),
								Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
								return false;
							}
						}
						if (newValues[npa][np][1] > 0) {
							if (newValues[npa][np][1] < c.getErrorMin())   {
								JOptionPane.showMessageDialog (this, particleName+":"+paramName+":"+Translator.swap ("FiPlantParticleEditor.deadErrorMin")+"="+c.getErrorMin(),
								Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
								return false;
							}
							if (newValues[npa][np][1] > c.getErrorMax())   {
								JOptionPane.showMessageDialog (this, particleName+":"+paramName+":"+Translator.swap ("FiPlantParticleEditor.deadErrorMax")+"="+c.getErrorMax(),
								Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
								return false;
							}
						}
					}
				}
			}
		}

		return true;

	}
	/**	Validation
	 */
	private void validateAction () {

		saveValues ();

		if (checkValues ()) {

			boolean update = false;

			//PLANT Particles UPDATE or CREATION
			if (nbParticle > 0) {

				//for each value of the table
				for (int npa=0; npa<nbParticle; npa++) {
					for (int np=0; np<nbParamMax; np++) {

						//UPDATE ALIVE VALUE
						if (paramIdTable[npa][np][0] > 0) {
							if ((newValues[npa][np][0] != -9) && (newValues[npa][np][0] != oldValues[npa][np][0])) {
								try {
									bdUpdator.updateParameter (paramIdTable[npa][np][0], newValues[npa][np][0]);
									update = true;

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}
							}
						}

						//UPDATE DEAD VALUE
						if (paramIdTable[npa][np][1] > 0) {
							if ((newValues[npa][np][1] != -9) && (newValues[npa][np][1] != oldValues[npa][np][1])) {
								try {
									bdUpdator.updateParameter (paramIdTable[npa][np][1], newValues[npa][np][1]);
									update = true;

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}
							}
						}

						//DELETE  ALIVE VALUE
						if ((particleIdTable[npa][np][0] > 0) && (paramIdTable[npa][np][0] > 0)) {
							if ((newValues[npa][np][0] == -9) && (newValues[npa][np][0] != oldValues[npa][np][0])) { // ALIVE
								try {
									bdUpdator.removeProperty (particleIdTable[npa][np][0], paramIdTable[npa][np][0]);
									paramIdTable[npa][np][0] = -1;
									update = true;

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}
							}
						}

						//DELETE  DEAD VALUE
						if ((particleIdTable[npa][np][1] > 0) && (paramIdTable[npa][np][1] > 0)) {
							if ((newValues[npa][np][1] == -9) && (newValues[npa][np][1] != oldValues[npa][np][1])) { // DEAD
								try {
									bdUpdator.removeProperty (particleIdTable[npa][np][1], paramIdTable[npa][np][1]);
									paramIdTable[npa][np][1] = -1;
									update = true;

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}

							}
						}
						//autre test ???
						/*try {
							System.out.println("deletePlantParticle id="+particleIdTable[npa]);

							bdUpdator.deletePlantParticle (plantId, particleIdTable[npa]);
							particleIdTable[npa] = -1;

						} catch (Exception e) {
							Log.println (Log.ERROR, "FiPlantParticlePanel", "error while UPDATING data base", e);
						}*/

						//CREATE ALIVE VALUE
						if (paramIdTable[npa][np][0] < 0) {
							if (newValues[npa][np][0] != -9) {
								try {
									String newParticle 	= plantParticleNameList.get(npa);
									String newParam 	= plantParamNameList.get(np);

									//PARTICLE AND PARAM CREATION
									if (particleIdTable[npa][np][0] < 0) {
										long particleId = bdUpdator.createPlantParticle (plantId, newParticle, "Alive");
										//to get particle ids for empty parameters values
										for (int p=0; p<nbParamMax; p++) {
											if (particleIdTable[npa][p][0] < 0) {
												particleIdTable[npa][p][0] = particleId;
											}
										}

										long paramId = bdUpdator.addParticleParameter (particleId,  newParam, ""+newValues[npa][np][0]);
										paramIdTable[npa][np][0] = paramId;
										update = true;
									}
									//PARAM CREATION
									else {
										long particleId = particleIdTable[npa][np][0];
										long paramId = bdUpdator.addParticleParameter (particleId,  newParam, ""+newValues[npa][np][0]);
										paramIdTable[npa][np][0] = paramId;
										update = true;
									}
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}
							}
						}

						//CREATE DEAD VALUE
						if (paramIdTable[npa][np][1] < 0) {
							if (newValues[npa][np][1] != -9) {
								try {
									String newParticle 	= plantParticleNameList.get(npa);
									String newParam 	= plantParamNameList.get(np);

									//PARTICLE AND PARAM CREATION
									if (particleIdTable[npa][np][1] < 0) {
										long particleId = bdUpdator.createPlantParticle (plantId, newParticle, "Dead");

										//to get particle ids for empty parameters values
										for (int p=0; p<nbParamMax; p++) {
											if (particleIdTable[npa][p][1] < 0) {
												particleIdTable[npa][p][1] = particleId;
											}
										}

										long paramId = bdUpdator.addParticleParameter (particleId,  newParam, ""+newValues[npa][np][1]);
										paramIdTable[npa][np][1]  = paramId;
										update = true;
									}

									//PARAM CREATION
									else {
										long particleId = particleIdTable[npa][np][1];
										long paramId = bdUpdator.addParticleParameter (particleId,  newParam, ""+newValues[npa][np][1]);
										paramIdTable[npa][np][1]  = paramId;
										update = true;

									}
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlantParticleEditor", "error while UPDATING data base", e);
								}
							}
						}

					}
				}

			}

			if (update) {
				//NE MARCHE PAS !!!
				//FiPlantChoiceDialog particleEntry = new FiPlantChoiceDialog (model, plant, plantMap);
			}

			setValidDialog (true);
			setVisible (false);
		}

	 }

	/**
	 * FiPlantParticleTableModel : Internam classes for display particle in a JTABLE
	 */
	 private class ParticleData
		{
		public String p_name;
		public String p_param;
		public Double p_alive;
		public Double p_dead;

		public ParticleData (String name, String param, double alive, double dead)
		{
			p_name = new String (name);
			p_param = new String (param);
			p_alive = new Double (alive);
			p_dead = new Double (dead);
		}
	}

	private class ColumnData
	{
		public String m_titre;
		public ColumnData (String titre) {m_titre = titre;}
	}

	private class FiPlantParticleTableModel extends AbstractTableModel
	{
		public ColumnData columnNames [] = {
			new ColumnData(Translator.swap ("FiPlantParticleEditor.particle")),
			new ColumnData(Translator.swap ("FiPlantParticleEditor.param")),
			new ColumnData(Translator.swap ("FiPlantParticleEditor.alive")),
			new ColumnData(Translator.swap ("FiPlantParticleEditor.dead")),

		};

		private Vector particle_vector;
		private boolean isEditable;

		public FiPlantParticleTableModel (boolean _isEditable) {
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
				if (col == 1) return data.p_param;
				if (col == 2) return data.p_alive;
				if (col == 3) return data.p_dead;
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
			if (col == 1) data.p_param = String.valueOf(object);
			if (col == 2) data.p_alive = Double.valueOf(String.valueOf(object));
			if (col == 3) data.p_dead = Double.valueOf(String.valueOf(object));

			return;
		}

		public void add (String name, String param, double alive, double dead) {
								particle_vector.addElement(new ParticleData(name, param, alive, dead));
		}
		public void clear() {
			particle_vector.removeAllElements ();
			this.fireTableDataChanged ();
		}

	}

}
