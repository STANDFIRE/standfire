package fireparadox.model.database;

/**
 * FiPlantTable : A plant/shape description for sorted list displayed.
 *
 * @author Isabelle LECOMTE     - January 2010
 *
 */
public class FmPlantTable   implements Comparable {


    private long id;						// id in the database
	private String team;
    private String site;
	private String species;
	private String origin;						// virtual, calculated, measured ...
    private double height;						// height of the fuel in m (trunk+crown)
	private double diameter;					// crown diameter in m
	private boolean deleted;					// if true, the plant is desactivated
	private boolean validated;					// if true, the plant is validated

	/**
	 * Creates a simple instance of FiPlantTable -
	 */
	public FmPlantTable (long _id, String _team, String _site, String _species,
						double _height, double _diameter,
						String _origin, boolean _validated, boolean _deleted) {
		id = _id;
		team = _team;
		site = _site;
		species = _species;
		height = _height;
		diameter = _diameter;
		origin = _origin;
		validated = _validated;
		deleted = _deleted;
	}
	public long getId() {return id;}
	public String getTeam() {return team;}
	public String getSite() {return site;}
	public String getSpecies() {return species;}
	public double getHeight() {return height;}
	public double getDiameter() {return diameter;}

	/*
	* sort the list by team, site, species, height, diameter
	*/
   	public int compareTo(Object other) {
	  String team1 = ((FmPlantTable) other).getTeam();
      String team2 = this.getTeam();
      String site1 = ((FmPlantTable) other).getSite();
      String site2 = this.getSite();
      String species1 = ((FmPlantTable) other).getSpecies();
      String species2 = this.getSpecies();
	  double heigh1 = ((FmPlantTable) other).getHeight();
      double heigh2 = this.getHeight();
	  double diameter1 = ((FmPlantTable) other).getDiameter();
      double diameter2 = this.getDiameter();

      if (team1.compareTo(team2) > 0)  return -1;
      else if (team1.equals(team2))  {
		  if (site1.compareTo(site2) > 0)  return -1;
		  else if (site1.equals(site2)) 	{
			  if (species1.compareTo(species2) > 0)  return -1;
			  else if (species1.equals(species2)) {
				   if (heigh1 > heigh2)  return -1;
				   else if (heigh1 == heigh2) {
					   	if (diameter1 > diameter2)  return -1;
					   	else if (diameter1 == diameter2) return 0;
						else return 1;
				   }
				   else return 1;
			  }
			  else return 1;
		  }
     	 else return 1;
	 }
     else return 1;
   }

}


