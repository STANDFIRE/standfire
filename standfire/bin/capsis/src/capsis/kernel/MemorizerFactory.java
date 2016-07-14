/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.kernel;

import capsis.extension.memorizer.CompactMemorizer;
import capsis.extension.memorizer.DefaultMemorizer;
import capsis.extension.memorizer.FrequencyMemorizer;
import capsis.kernel.extensiontype.Memorizer;

/**	Memorizer factory creates memorizers.
 * 
 *	@author F. de Coligny, S. Dufour - october 2002, september 2010 
 */
public class MemorizerFactory {

	/**	Returns a Memorizer from the given code.
	 */
	static public Memorizer createMemorizer (String code) {
		if (code == null || 
				code.startsWith ("DefaultMemorizer")) {
			return createDefaultMemorizer ();
			
		} else if (code.startsWith ("CompactMemorizer")) {
			return createCompactMemorizer ();
			
		} else if (code.startsWith ("FrequencyMemorizer")) {
			// e.g. "FrequencyMemorizer f=5"
			int i = code.indexOf ("=");
			String f = code.substring (i+1);
			try {
				int freq = new Integer (f).intValue ();
				return createFrequencyMemorizer (freq);
			} catch (Exception e) {}
			return createFrequencyMemorizer (5);
		}
	
		return createDefaultMemorizer ();
	
	}
	
	/**	Returns an instance of DefaultMemorizer.
	 */
	static public Memorizer createDefaultMemorizer () {
		
		try {
			
			return new DefaultMemorizer();
			
		} catch (Exception e) {
			return null;
		}
	}
	

	/**	Returns an instance of CompactMemorizer.
	 */
	static public Memorizer createCompactMemorizer () {
		
		try {
			
			return new CompactMemorizer();
			
		} catch (Exception e) {
			return createDefaultMemorizer ();
		}
	}
	
	/**	Returns an instance of FrequencyMemorizer.
	 */
	static public Memorizer createFrequencyMemorizer (int freq) {
		
		try {
			return new FrequencyMemorizer(freq);
			
		} catch (Exception e) {
			return createDefaultMemorizer ();
		}
	}


}

