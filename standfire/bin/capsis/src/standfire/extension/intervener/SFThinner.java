package standfire.extension.intervener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import jeeb.lib.util.autoui.SimpleAutoDialog;
import jeeb.lib.util.autoui.annotations.AutoUI;
import jeeb.lib.util.autoui.annotations.Editor;
import standfire.model.SFModel;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;


/**	SFThinner is an example of thinning tool for Standfire. 
 * 
 * 	@author F. Pimont - September 2013
 */
@AutoUI(title="StandfireThinner", translation="SFThinner")
public class SFThinner implements Intervener, GroupableIntervener, Automatable {
	
	static {
		Translator.addBundle ("standfire.extension.intervener.SFThinner");
	}
	
	public static final String NAME = "SFThinner";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "F. Pimont";
	public static final String DESCRIPTION = "SFThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	@Ignore
	private boolean constructionCompleted = false;  // if cancel in interactive mode, false
	@Ignore
	private GScene scene;  // Reference scene: will be altered by apply ()
	@Ignore
	private GModel model;
	@Ignore
	protected Collection<Tree> concernedTrees;

	@Editor
	protected double selectionDbh = 20;  // cm, cut no trees above this dbh
	
	

	/**	Default constructor.
	 */
	public SFThinner () {}

	
	/** Script constructor.
	 */
	public SFThinner (List<Integer> treeIdsToBeCut) {
		
		this.selectionDbh = selectionDbh;

		constructionCompleted = true;
		
	}
	
	
	/** Init the thinner on a given scene.
	 */
	@Override
	public void init(GModel model, Step s, GScene scene, Collection c) {
		this.scene = scene;	// this is referentScene.getInterventionBase ();
		this.model = model;
		
		if (c == null) {
			concernedTrees = (Collection<Tree>) ((TreeList) scene).getTrees ();  // all trees
		} else {
			concernedTrees = c;  // restrict to the given collection
		}
		
		constructionCompleted = true;
	}
	
	
	/**	Open a dialog to tune the thinner.	
	 */
	@Override
	public boolean initGUI() throws Exception {
		// Interactive start
		SimpleAutoDialog dlg = new SimpleAutoDialog (this, false);  // false: no extra buttons
		dlg.setVisible(true);

		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// Valid -> ok was hit and all checks were ok
			constructionCompleted = true;
		}
		dlg.dispose ();
		return constructionCompleted;
		
	}

	
	/**	Extension dynamic compatibility mechanism.
	 *	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof SFModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "SFThinner.matchWith ()", 
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	
	/**	GroupableIntervener interface. This intervener acts on trees,
	 *	tree groups can be processed.
	 */
	public String getGrouperType () {return Group.TREE;}


	/**	These checks are done at the beginning of apply ().
	 * 	They are run in interactive AND script mode.
	 */
	public boolean isReadyToApply () {
		
		if (!constructionCompleted) {
			// If cancel on dialog in interactive mode -> constructionCompleted = false
			Log.println (Log.ERROR, "SFThinner.isReadyToApply ()",
					"constructionCompleted is false. SFThinner is not appliable.");
			return false;
		}
		
		if (selectionDbh <= 0) {
			Log.println (Log.ERROR, "SFThinner.isReadyToApply ()",
					"Wrong selectionDbh: "+selectionDbh+". SFThinner is not appliable.");
			return false;
		}
		
		return true;
	}

	
	/**	Makes the action: thinning.
	 */
	public Object apply () throws Exception {
		// Check if apply is possible
		if (!isReadyToApply ()) {
			throw new Exception ("SFThinner.apply () - Wrong input parameters, see Log");
		}

		scene.setInterventionResult (true);

		// iterate and cut
		for (Iterator i = concernedTrees.iterator (); i.hasNext ();) {
			Tree t = (Tree) i.next ();
			
			// do we cut this tree ?
			if (t.getDbh () <= selectionDbh) {  // yes
				// remove the tree
				i.remove ();
				((TreeList) scene).removeTree (t);
				// remember this tree has been cut
				((TreeList) scene).storeStatus (t, "cut");
				
			}
			
		}

		return scene;
	}

	
	/**	Intervener
	 */
	@Override
	public void activate() {
		// not used
		
	}

	

}

