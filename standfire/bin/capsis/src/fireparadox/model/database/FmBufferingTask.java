package fireparadox.model.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import capsis.lib.fire.fuelitem.FiSpecies;

import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.TicketDispenser;
import jeeb.lib.util.Translator;
import jeeb.lib.util.task.CancellableTask;

/**	FiBufferingTask loads information from the FireParadox database
*	to be managed in buffers.
*	It tells the listeners in the Event Dispatch Thread when it's over.
*
*	@author F. de Coligny - august 2009
*/
//~ public class FiBufferingTask extends Task<Integer,Void> implements ListenedTo {
public class FmBufferingTask extends CancellableTask<Integer,Void> implements ListenedTo {

	private Collection<Listener> listeners;
	private Integer rc;			// doInWorker () return code

	private TicketDispenser speciesIdDispenser;
	private FiSpecies speciesSpecimen;

	private Collection<FiSpecies> speciesList;
	private LinkedHashMap<Long,FmDBTeam> teamMap;
	private ArrayList<FmDBCheck> checkList;

	private Map<Long,FmPlantSyntheticData> plantSyntheticMap;
	private Map<Long,FmLayerSyntheticDataBaseData> layerSyntheticMap;


	/**	Constructor.
	*/
	public FmBufferingTask (String name, Listener listener,
			TicketDispenser speciesIdDispenser, FiSpecies speciesSpecimen) {
		super (name);
		this.speciesIdDispenser = speciesIdDispenser;
		this.speciesSpecimen = speciesSpecimen;

		// this listener will be called at the end
		// other listeners may be added with addListener ()
		if (listener != null) {
			addListener (listener);
		}

		// this task will take an indeterminate time
		setIndeterminate ();
	}

	/**	doFirstInEDT () is called in the init () method at the very beginning
	*	of doInBackground () and runs in the EDT before doInWorker ().
	*	Sets some GUI components in waiting mode (optional).
	*/
	@Override
	protected void doFirstInEDT () {}	// Do nothing here

	/**	doInWorker () runs in a worker thread.
	*	It does the long job apart the EDT.
	*/
	@Override
	protected Integer doInWorker () {
		try {
			StatusDispatcher.print (Translator.swap ("FiBufferingTask.loadingSpecies"));
			loadSpeciesLists ();
			
			if (Thread.interrupted ()) {throw new Exception ();}
			
			StatusDispatcher.print (Translator.swap ("FiBufferingTask.loadingTeams"));
			loadTeamMap ();
			
			if (Thread.interrupted ()) {throw new Exception ();}

			StatusDispatcher.print (Translator.swap ("FiBufferingTask.loadingChecks"));
			loadChecks ();
			
			if (Thread.interrupted ()) {throw new Exception ();}

			StatusDispatcher.print (Translator.swap ("FiBufferingTask.loadingPlantSyntheticData"));
			loadPlantSyntheticData ();
			
			if (Thread.interrupted ()) {throw new Exception ();}

			StatusDispatcher.print (Translator.swap ("FiBufferingTask.loadingLayerSyntheticData"));
			loadLayerSyntheticData ();
			
			if (Thread.interrupted ()) {throw new Exception ();}

			StatusDispatcher.print (Translator.swap ("FiBufferingTask.done"));
			rc = new Integer (0);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiBufferingTask.doInWorker ()", "Exception", e);
			rc = new Integer (1);	// error
		}

		return null;

	}

	/**	doInEDTafterWorker () runs in the EDT when doInWorker () is over.
	*/
	@Override
	protected void doInEDTafterWorker () {

		//~ if (rc != null && rc.equals (new Integer (0))) {
			// tell listeners that the work is over (param = return code, 0 = ok)
			if (rc == null) {rc = new Integer (1);}	// error / cancellation
//~ System.out.println ("FiBufferingTask doInEDTafterWorker rc = "+rc+" tellSomethingHappened ()...");
			tellSomethingHappened (rc);
		//~ }
		
	}

	/**	In case of cancellation, doInEDTifCancelled () runs in the 
	*	Event Dispatch Thread when the worker thread is done.
	*	Update the GUI at the end of a task cancellation.
	*/
	protected void doInEDTifCancelled () {

		// tell listeners that the work was cancelled (with WRONG return code)
		//~ rc = new Integer (1);	// error
		//~ tellSomethingHappened (rc);
		
	}
	
	
	/**	Add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new ArrayList<Listener> ();}
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
	public void tellSomethingHappened (Object param) {
		if (listeners == null) {return;}
		for (Listener l : listeners) {
			l.somethingHappened (this, param);
		}
	}

	/**	Constructs the species lists by connecting to the FireParadox database.
	*/
	private void loadSpeciesLists () throws Exception {

		speciesList = new ArrayList<FiSpecies> ();
		FmDBCommunicator bdCommunicator = FmDBCommunicator.getInstance ();
		speciesList = bdCommunicator.getSpecies (speciesIdDispenser, speciesSpecimen);
	}

	/**	Constructs the teamMap by connecting to the FireParadox database.
	*/
	private void loadTeamMap () throws Exception {
		//~ teamMap = new LinkedHashMap<Long,FiDBTeam> ();
		FmDBCommunicator bdCommunicator = FmDBCommunicator.getInstance ();
		teamMap = bdCommunicator.getTeams ();

	}

	/**	Constructs the teamMap by connecting to the FireParadox database.
	*/
	private void loadChecks () throws Exception {
		//~ teamMap = new LinkedHashMap<Long,FiDBTeam> ();
		FmDBCommunicator bdCommunicator = FmDBCommunicator.getInstance ();
		checkList =  bdCommunicator.getCheckList ();

	}


	/**	Constructs the plant synthetic data map by connecting to the
	*	FireParadox database.
	*/
	public void loadPlantSyntheticData () throws Exception {
		FmDBCommunicator bdCommunicator = FmDBCommunicator.getInstance ();
		plantSyntheticMap = bdCommunicator.getPlantSyntheticData (teamMap);

	}

	/**	Constructs the layer synthetic data map by connecting to the
	*	FireParadox database.
	*/
	public void loadLayerSyntheticData () throws Exception {
		FmDBCommunicator bdCommunicator = FmDBCommunicator.getInstance ();
		layerSyntheticMap = bdCommunicator.getLayerSyntheticData (teamMap);

	}

	public Collection<FiSpecies> getSpeciesList () {return speciesList;}
	public LinkedHashMap<Long,FmDBTeam> getTeamMap () {return teamMap;}
	public ArrayList<FmDBCheck> getCheckList () {return checkList;}
	public Map<Long,FmPlantSyntheticData> getPlantSyntheticMap () {return plantSyntheticMap;}
	public Map<Long,FmLayerSyntheticDataBaseData> getLayerSyntheticMap () {return layerSyntheticMap;}

}
