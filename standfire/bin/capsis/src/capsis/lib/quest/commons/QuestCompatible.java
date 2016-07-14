package capsis.lib.quest.commons;

import repicea.simulation.treelogger.LoggableTree;

/**
 * Trees compatible with the Quest library must implement this interface.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public interface QuestCompatible extends LoggableTree {

	/**
	 * Returns the species of this tree according to the Quest library. E;g.
	 * QuestSpecies.BLACK_SPRUCE. See QuestSpecies.
	 */
	public QuestSpecies getQuestSpecies();
	
	

	
}
