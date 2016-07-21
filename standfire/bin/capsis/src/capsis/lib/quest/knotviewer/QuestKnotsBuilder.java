package capsis.lib.quest.knotviewer;

import java.util.List;

import capsis.lib.quest.commons.QuestTaper;

/**
 * QuestKnotsBuilder computes the knots distribution and geometry.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public abstract class QuestKnotsBuilder {
	
	public abstract void execute (QuestTaper taper, List<Double> dbhs, List<Double> heights);
	
	public abstract List<QuestGU> getGUs();
	
}
