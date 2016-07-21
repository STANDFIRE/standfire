package capsis.lib.quest.commons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jeeb.lib.util.ListMap;
import jeeb.lib.util.Translator;
import capsis.lib.quest.knotviewer.QuestBlackSpruceKnotsBuilder;
import capsis.lib.quest.knotviewer.QuestKnotsBuilder;
import capsis.lib.quest.ringviewer.model.QuestBlackSpruceMOE;
import capsis.lib.quest.ringviewer.model.QuestBlackSpruceMOR;
import capsis.lib.quest.ringviewer.model.QuestModel;

/**
 * A species for the Quest library.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public class QuestSpecies {

	private static List<QuestSpecies> supportedSpecies; // available species
	private static ListMap<QuestSpecies, QuestModel> supportedModels; // species
																		// ->
	// list of available model
	public static final QuestSpecies BLACK_SPRUCE = new QuestSpecies("QuestSpecies.BLACK_SPRUCE", "BLACK_SPRUCE",
			new QuestBlackSpruceTaper(), new QuestBlackSpruceKnotsBuilder());
	// **declare here the new species**
	public static final QuestSpecies OTHER = new QuestSpecies("QuestSpecies.OTHER", "OTHER", null, null);

	static {
		Translator.addBundle("capsis.lib.quest.QuestLabels");

		// Declare all the species managed by QuEST
		supportedSpecies = new ArrayList<>();
		supportedSpecies.add(QuestSpecies.BLACK_SPRUCE);
		// **add here the new species in the list**

		// Declare all the species managed by QuEST
		supportedModels = new ListMap<>();
		supportedModels.addObject(QuestSpecies.BLACK_SPRUCE, new QuestBlackSpruceMOE());
		supportedModels.addObject(QuestSpecies.BLACK_SPRUCE, new QuestBlackSpruceMOR());
		// **add here the new models in the list (each new model must be created
		// in a new class like QuestBlackSpruceMOE)**

	}

	private String name; // e.g. "QuestSpecies.BLACK_SPRUCE"
	// CodeName: to select a species from an external file
	private String codeName; // e.g. "BLACK_SPRUCE"
	private QuestTaper taper;
	private QuestKnotsBuilder knotsBuilder;

	/**
	 * Constructor.
	 */
	public QuestSpecies(String name, String codeName, QuestTaper taper, QuestKnotsBuilder knotsBuilder) {
		this.name = name;
		this.codeName = codeName;
		this.taper = taper;
		this.knotsBuilder = knotsBuilder;
	}

	public String getName() {
		return name;
	}

	public String getCodeName() {
		return codeName;
	}

	public QuestTaper getTaper() {
		return taper;
	}

	public QuestKnotsBuilder getKnotsBuilder() {
		return knotsBuilder;
	}

	/**
	 * Returns the list of supported speciesNames
	 */
	public static List<QuestSpecies> getSupportedSpecies() {
		return supportedSpecies;
	}

	/**
	 * Returns the list of QuEST models available for the given species. Note:
	 * each model has a getName () function.
	 */
	public static List<QuestModel> getSupportedModels(QuestSpecies species) {
		return supportedModels.getObjects(species);
	}

	/**
	 * Returns a sentence with the species supported by QuEST (for comprehensive
	 * messages in case of trouble)
	 */
	public static String getSupportedSpeciesSentence() {
		StringBuffer b = new StringBuffer();

		for (Iterator<QuestSpecies> i = supportedSpecies.iterator(); i.hasNext();) {
			QuestSpecies species = i.next();
			String name = species.getName();
			b.append(Translator.swap(name));
			if (i.hasNext()) {
				b.append(", ");
			}
		}
		return b.toString();
	}

	/**
	 * If we have a species codeName from an input file, e.g. "BLACK_SPRUCE",
	 * this ethod returns the matching QuestSpecies. If no match, returns null.
	 * 
	 * @param codeName
	 *            : a QuestSpecies valid codeName, e.g. "BLACK_SPRUCE"
	 */
	static public QuestSpecies findSpecies(String codeName) {

		for (Iterator<QuestSpecies> i = supportedSpecies.iterator(); i.hasNext();) {
			QuestSpecies species = i.next();
			if (species.codeName.equals(codeName)) {
				return species;
			}
		}
		// Not found
		return null;
	}

	public String toString () {
		return codeName;
	}
	
}
