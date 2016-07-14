package fireparadox.model;

import java.util.Vector;

import jeeb.lib.util.Command;
import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.Tree;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.database.FmDBUpdatorScene;
import fireparadox.model.database.FmLayerFromDB;
import fireparadox.model.database.FmPlantFromDB;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;

/**	FiSceneDatabaseSaver saves the given scene in the database.
*	@author F. de Coligny, Boris Pezzatti - february 2010
*/
public class FmSceneDatabaseSaver implements Command {

	private FmStand scene;
	private String sceneName;
	private String ownerTeam;
	

	/**	Constructor.
	*/
	public FmSceneDatabaseSaver (FmStand scene, String sceneName, String ownerTeam) {
		super ();
		this.scene = scene;
		this.sceneName = sceneName;
		this.ownerTeam = ownerTeam;
		
	}

	/**	Saves into the database
	*	*** No graphic widget in this class, may run in script mode without GUI ! ***
	*	May return error codes != 0 if trouble
	*	e.g. 1: ownerTeam is unknown, 2: wrongSceneName, 3: scene is null...
	*/
	public int execute () {
		
		// Test if scene is null
		if (scene == null) {
			Log.println (Log.ERROR, "FiSceneDatabaseSaver.execute ()",
					"Scene is null, could not save in the database");
			return 3;
		}
		
		//... other tests (do the same than upper)

				
		//... prepare save request
		StringBuffer request = new StringBuffer ();
		request.append ("action=create_scene");
		request.append ("&name=");
		request.append (sceneName);
		request.append ("&owner=");
		request.append (ownerTeam);
		
		
		// Loop on plants to save them
		for (Tree t : scene.getTrees ()) {
			FiPlant p = (FiPlant) t;
			Long shapeId = -1l;
			if (p instanceof FmPlantFromDB) {
				shapeId = ((FmPlantFromDB) p).getShapeId();
			}
			request.append("&s=" + shapeId + "," + p.getX() + "," + p.getY()
					+ "," + p.getZ());
			
		}

		// Loop on layerSets and their layers to save them
		for (FiLayerSet layerSet : scene.getLayerSets ()) {
//		for (FmLayerSet layerSet : scene.getLayerSets ()) {
			
			request.append ("&l=");
			
			for (FmLayer l : ((FmLayerSet) layerSet).getFmLayers ()) {
				// add a ',' between the layers (not at first iteration)
				if (!request.substring (request.length () - 1).equals ("=")) {
					request.append (",");
				}
				Long shapeId = -1l;
				if (l instanceof FmLayerFromDB) {
					shapeId = ((FmLayerFromDB) l).getShapeId();
				}

				request.append (shapeId + "," + l.getCoverFraction ()); 
				try {
					request.append ("," + l.getMoisture (0, FiParticle.LIVE));
				} catch (Exception e) {
					// TODO FP Auto-generated catch block
					e.printStackTrace ();
				}
				try {
					request.append ("," + l.getMoisture (0, FiParticle.DEAD));
				} catch (Exception e) {
					// TODO FP Auto-generated catch block
					e.printStackTrace ();
				}	
				request.append (","+l.getCharacteristicSize ());	
				
			}
			request.append (";");
			
			// polygon
			for (Vertex3d v : layerSet.getVertices ()) {
				// add a ',' between the vertices (not at first iteration)
				if (!request.substring (request.length () - 1).equals (";")) {
					request.append (",");
				}
				request.append (""+v.x+","+v.y+","+v.z);
				
			}
			
			
		}
		
		
		//... save to the database
		String s = request.toString ();

		// only to check the request in the terminal, should be removed
		System.out.println ("FiSceneDatabaseSaver, request="+s);

		try{
			Vector resultList = FmDBUpdatorScene.getInstance().doPost(s);
			
			if (((String)(resultList.elementAt(0))).equals("ok")){
				return 0;	// ok
				
			}else if (((String)(resultList.elementAt(0))).equals("invalid_owner")){
				Log.println (Log.ERROR, "FiSceneDatabaseSaver.execute ()",
					"Team does not exist, could not save in the database");
				return 4;
			}else{
				Log.println (Log.ERROR, "FiSceneDatabaseSaver.execute ()",
						"Error during servlet execution, Could not save in the database");
				return 1;   //problem on the server
			}
				
		}catch (Exception e){
			Log.println (Log.ERROR, "FiSceneDatabaseSaver.execute ()",
					"Error during posting, could not save in the database");
			return 2;   //problem posting
		}
				
		
	}
			
}

