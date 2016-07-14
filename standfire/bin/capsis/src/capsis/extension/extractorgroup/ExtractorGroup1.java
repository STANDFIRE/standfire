package capsis.extension.extractorgroup;

import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.Translator;
import capsis.extensiontype.ExtractorGroup;

/**
 * A group of data extractors in Capsis.
 */
public class ExtractorGroup1 extends ExtractorGroup {
	// reviewed fc-12.9.2014, fixed several bugs

	public static final String NAME = Translator
			.swap("ExtractorGroup1.standardCharts");
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "ExtractorGroup.description";

	static List<String> extractorClassNames;
	static {
		extractorClassNames = new ArrayList<String>();
		extractorClassNames.add("capsis.extension.dataextractor.DETimeG");
		extractorClassNames.add("capsis.extension.dataextractor.DETimeDdomDg");
		extractorClassNames.add("capsis.extension.dataextractor.DETimeHdomHg");
		extractorClassNames.add("capsis.extension.dataextractor.DEDbhClassN");
		extractorClassNames
				.add("capsis.extension.dataextractor.DEHeightClassN");
		extractorClassNames.add("capsis.extension.dataextractor.DETimeN");

	}

	/**
	 * Constructor.
	 */
	public ExtractorGroup1() {
		super();
	}

	/**
	 * Returns the list of extractors this group contains.
	 */
	public List<String> getExtractorClassNames() {
		return extractorClassNames;
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchWith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		return ExtractorGroup.matchWith(extractorClassNames, referent); // needed
	}

}
