package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBSite;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;


/**	FiPlantChoiceDialog : Plant choice for particle parameter copy (unique individual)
*
*	@author I. Lecomte - march 2008
*/
public class FmPlantChoiceDialog extends AmapDialog implements ActionListener {

	private FmDBUpdator bdUpdator;				//to update database

	private FmModel model;
	private FmDBPlant plant;			//research form
	private FmDBTeam team;
	private FmDBSite site;
	private String species;
	private Long plantId;

	private LinkedHashMap<Long, FmDBPlant>  plantMap;
	private Vector<FmDBPlant>  listPlant;
    private JTable resultTable;
    private FiPlantChoiceTableModel tableModel;

	private JButton close;
	private JButton help;
	private JButton valid;


	/**	Constructor.
	*/
	public FmPlantChoiceDialog (FmModel _model, FmDBPlant _plant, LinkedHashMap<Long, FmDBPlant> _plantMap) {

		super ();
		model = _model;
		bdUpdator = model.getBDUpdator ();

		plant = _plant;
		plantMap = _plantMap;
		plantId = plant.getPlantId();

		team = plant.getTeam();
		species = plant.getSpeciesName();

		selectPlant ();

		createUI ();

		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (valid)) {
			validAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}



	/**	Plant update
	*/
	private void validAction () {

		int [] selRow  = resultTable.getSelectedRows ();

		for (int i=0; i<selRow.length ; i++) {
			int selectedRow = selRow[i];
			FmDBPlant plantSelected =  (listPlant.elementAt (selectedRow));

			long plantSelectedId = plantSelected.getPlantId();
			try {
				bdUpdator.copyPlantParticle (plantId,  plantSelectedId);
			} catch (Exception e) {
					Log.println (Log.ERROR, "FiPlantChoiceDialog", "error while UPDATING data base", e);
			}

		}
		setValidDialog (true);
		setVisible (false);
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {



		LinePanel ligResult = new LinePanel ();
		Border etchedRes = BorderFactory.createEtchedBorder ();
		Border bRes = BorderFactory.createTitledBorder (etchedRes, Translator.swap (
				"FiPlantChoiceDialog.resultBorder"));
		ligResult.setBorder (bRes);

		// 2.1 Result Table
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		JScrollPane listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (300,240));
		colTable.add (listSP);
		ligResult.add (colTable);



		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		valid = new JButton (Translator.swap ("FiPlantChoiceDialog.valid"));
		controlPanel.add (close);
		controlPanel.add (help);
		controlPanel.add (valid);
		close.addActionListener (this);
		help.addActionListener (this);
		valid.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (ligResult, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlantChoiceDialog.title"));

		setModal (true);
	}

	private void selectPlant () {

		listPlant = new Vector<FmDBPlant>();
		tableModel = new  FiPlantChoiceTableModel ();
		tableModel.clear();


		for (Iterator i = plantMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBPlant f = plantMap.get(cle);



			if (f != plant) {


				//check team
				FmDBTeam t = f.getTeam();

				if ((t != null) && (t.getTeamCode().equals(team.getTeamCode()))) {

					//check species
					String sp = f.getSpeciesName();
					if (sp.equals(species)) {

						String teamName = t.getTeamCode();
						String siteName = "";
						if (f.getSite()!=null) siteName = f.getSite().getSiteCode();
						String origin = f.getOrigin();
						Double height = f.getHeight();
						Double diameter = f.getCrownDiameter();
						listPlant.add (f);
						tableModel.add (teamName, siteName, species, height, diameter, origin);
					}

				}
			}
		}
	}





	/**
	 * FiVoxelFiVoxelParticleTableModel : Internam classes for display particle in a JTABLE
	 */
	 protected class PlantData
		{
		public boolean p_selected;
		public String p_team_name;
		public String p_site_name;
		public String p_species_name;
		public Double p_height;
		public Double p_diameter;
		public String p_origin;

		public PlantData (String team, String site, String species, double height, double diameter, String origin)
		{
			p_selected = false;
			p_team_name = new String (team);
			p_site_name = new String (site);
			p_species_name = new String (species);
			p_origin = new String (origin);
			p_height = new Double (height);
			p_diameter = new Double (diameter);
		}
	}

	protected class ColumnData
	{
		public String m_titre;
		public ColumnData (String titre) {m_titre = titre;}
	}

	protected class FiPlantChoiceTableModel extends AbstractTableModel
	{
		public ColumnData columnNames [] = {
			//new ColumnData(Translator.swap ("FiPlantTableModel.selection")),
			new ColumnData(Translator.swap ("FiPlantTableModel.team")),
			new ColumnData(Translator.swap ("FiPlantTableModel.site")),
			new ColumnData(Translator.swap ("FiPlantTableModel.species")),
			new ColumnData(Translator.swap ("FiPlantTableModel.height")),
			new ColumnData(Translator.swap ("FiPlantTableModel.diameter")),
			new ColumnData(Translator.swap ("FiPlantTableModel.origin")),

		};

		protected Vector plant_vector;

		public FiPlantChoiceTableModel () {
			plant_vector = new Vector();
			setDefaultData();
		}


		public void setDefaultData() {
			if (plant_vector == null) plant_vector = new Vector ();
			plant_vector.removeAllElements();
		}
		public int getRowCount() {return plant_vector == null ? 0 : plant_vector.size();}
		public int getColumnCount() {return columnNames.length;}
		@Override
		public String getColumnName(int column) {return columnNames[column].m_titre;}
		@Override
		public boolean isCellEditable(int row, int col) {
			//if (col == 0) return true;
			//else return false;
			return false;
		}

		public Object getValueAt(int row, int col) {
			if (row < 0 || row > getRowCount()) return "";
			PlantData data = (PlantData) plant_vector.elementAt(row);
				//if (col == 0) return data.p_selected;
				if (col == 0) return data.p_team_name;
				if (col == 1) return data.p_site_name;
				if (col == 2) return data.p_species_name;
				if (col == 3) return data.p_height;
				if (col == 4) return data.p_diameter;
				if (col == 5) return data.p_origin;

			return "";
		}
		@Override
		public void setValueAt(Object object, int row, int col) {
			//if (row < 0 || row > getRowCount()) return;
			//if (col > 0) return;
			//System.out.println("valeur="+object);

			return;
		}

		public void add (String team, String site, String species, double height, double diameter, String origin) {
								plant_vector.addElement(new PlantData(
										team, site, species, height, diameter, origin));
		}
		public void clear() {
			plant_vector.removeAllElements ();
			this.fireTableDataChanged ();
		}

	}
}

