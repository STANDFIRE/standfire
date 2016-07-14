package capsis.lib.organon;

import java.io.Serializable;
import java.util.Map;

/**
 * Organon: a convenient tools class to provide interestinf functions to the
 * subclasses.
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny -
 *         August 2014
 */
public class OrgTools {

	/**
	 * If the errorCode is not 0, add an entry in the given report. If errorMap
	 * is not null, a message is searched for the error code.
	 */
	protected void searchErrors(String variableName, int errorCode,
			Map<Integer, String> errorMap, StringBuffer report) {
		report.append("\n" + variableName + ":");

		boolean found = false;
		if (errorCode != 0) {
			found = true;
			report.append("\n error: (" + errorCode + ")");
			if (errorMap != null) {
				String errorMessage = errorMap.get(errorCode);
				if (errorMessage == null)
					errorMessage = "Message not found for code: " + errorCode;
				report.append(" " + errorMessage);
			}
		}

		if (!found)
			report.append(" OK");
	}

	/**
	 * If an error is found in the array, add an entry in the given report. If
	 * errorMap is not null, a message is searched for the error code.
	 */
	protected void searchErrorsI(String arrayName, int[] array,
			Map<Integer, String> errorMap, StringBuffer report) {
		report.append("\n" + arrayName + ":");
		boolean found = false;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0) {
				found = true;
				int fortranIndex = i + 1; // fortran index in [1,length]
				report.append("\n error " + fortranIndex + ": (" + array[i]
						+ ")");
				if (errorMap != null) {
					String errorMessage = errorMap.get(fortranIndex);
					if (errorMessage == null)
						errorMessage = "Message not found for code: "
								+ fortranIndex;
					report.append(" " + errorMessage);
				}
			}
		}
		if (!found)
			report.append(" OK");
	}

	/**
	 * If an error is found in the array, add an entry in the given report. If
	 * errorMap is not null, a message is searched for the error code.
	 */
	protected void searchErrorsIJ(String arrayName, int[] array,
			Map<Integer, String> errorMap, StringBuffer report) {
		report.append("\n" + arrayName + ":");
		boolean found = false;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0) {
				found = true;

				int errorCode = i / 2000 + 1;
				int treeId = i % 2000 + 1;
				// int treeId = i / 2000 + 1;
				// int errorCode = i % 2000 + 1;

				report.append("\n tree: " + treeId + " error " + errorCode
						+ ": (" + array[i] + ")");
				if (errorMap != null) {
					String errorMessage = errorMap.get(errorCode);
					if (errorMessage == null)
						errorMessage = "Message not found for code: "
								+ errorCode;
					report.append(" " + errorMessage);
				}
			}
		}
		if (!found)
			report.append(" OK");
	}

	/**
	 * Tool method, copies source in dest. Source can be smaller than dest.
	 */
	public void fillArray(int[] dest, int[] source) {
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
	}

	/**
	 * Tool method, copies source in dest. Source can be smaller than dest. 
	 */
	public void fillArray(float[] dest, float[] source) {
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
	}

	/**
	 * Tool method, copies source in dest. Source can be smaller than dest.
	 * Copies only for the given length.
	 */
	public void fillArray(int[] dest, int[] source, int length) {
		for (int i = 0; i < length; i++) {
			dest[i] = source[i];
		}
	}

	/**
	 * Tool method, copies source in dest. Source can be smaller than dest. Copies only for the given length.
	 */
	public void fillArray(float[] dest, float[] source, int length) {
		for (int i = 0; i < length; i++) {
			dest[i] = source[i];
		}
	}

	/**
	 * Tool method, prints the given array, restricted to the given length, into
	 * a String. If length is lower or equal to zero, prints the whole array.
	 */
	static public String head(int[] source) {
		return head(source, 6);
	}

	static public String head(int[] source, int len) {
		return head(source, len, " ", true);
	}

	static public String head(int[] source, int len, String separator,
			boolean withDots) {
		if (len <= 0 || len > source.length)
			len = source.length;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < len; i++) {
			b.append(source[i]);
			b.append(separator);
		}
		if (len < source.length && withDots)
			b.append("...");
		return b.toString();
	}

	/**
	 * Tool method, prints the given array, restricted to the given length, into
	 * a String. If length is lower or equal to zero, prints the whole array.
	 */
	static public String head(float[] source) {
		return head(source, 6);
	}

	static public String head(float[] source, int len) {
		return head(source, len, " ", true);
	}

	static public String head(float[] source, int len, String separator,
			boolean withDots) {
		if (len <= 0 || len > source.length)
			len = source.length;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < len; i++) {
			b.append(source[i]);
			b.append(separator);
		}
		if (len < source.length && withDots)
			b.append("...");
		return b.toString();
	}

}
