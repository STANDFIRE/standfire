package fireparadox.gui.database;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBPlant;

/** FiPlantInfoPanel : To display short plant information
 *
 * @author I. Lecomte - March 2008
 */
public class FmPlantInfoPanel extends JPanel  {

	private FmDBPlant plant;
	private long plantId;
	private String reference;
	private String teamName = "";
	private String siteName = "";
	private String specieName;
	private double height;
	private String origin = "";

	/**	Constructor.
	*/
	public FmPlantInfoPanel (FmDBPlant _plant)  {

		super ();
		plant = _plant;

		if (plant != null) {
			plantId = plant.getPlantId ();
			specieName = plant.getSpecie ();
			reference = plant.getReference();
			height = plant.getHeight ();
			if (plant.getTeam () != null)
				teamName = plant.getTeam (). getTeamCode();
			if (plant.getSite () != null)
				siteName = plant.getSite (). getSiteCode();
			origin = plant.getOrigin();
		}

		createUI ();
		show ();
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		this.setLayout(new FlowLayout (FlowLayout.LEFT)) ;

		// FUEL panel
		ColumnPanel fuelPanel = new ColumnPanel (Translator.swap ("FiPlantInfoPanel.title"));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.plantId")+" : "+plantId));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.team")+" : "+teamName));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.site")+" : "+siteName));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.species")+" : "+specieName));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.reference")+" : "+reference));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.height")+" : "+height));
		fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.origin")+" : "+origin));

		if (plant.isDeleted())
			fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.deleted")));
		if (plant.isValidated())
			fuelPanel.add (new JLabel (Translator.swap ("FiPlantInfoPanel.validated")));


		this.add (fuelPanel);
	}

}

