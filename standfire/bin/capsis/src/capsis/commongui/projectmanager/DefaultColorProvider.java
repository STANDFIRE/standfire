/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */

package capsis.commongui.projectmanager;

import java.util.List;

import jeeb.lib.util.ListMap;

/**
 * DefaultColorProvider provides user with colors. See
 * http://www.colorschemedesigner.com/ to choose nice color palettes.
 * 
 * @author F. de Coligny - September 2012
 */

public class DefaultColorProvider implements ColorProvider {

	// To init the userColors: one array per color bag
	static private String[][] defaultHexs = { { "850bee", "fb003a", "0f5eee", "339415", "ff00ff", "808080", "d7acfb",
			"ffacbf", "b4cdfa", "64e33c", "ffb3ff", "dadada" } };

	// "850bee" ,
	// "fb003a" ,
	// "0f5eee" ,
	// "339415" ,
	// "ff00ff" ,
	// "808080" ,
	// "d7acfb" ,
	// "ffacbf" ,
	// "b4cdfa" ,
	// "64e33c" ,
	// "ffb3ff" ,
	// "dadada"

	// Old values, changed on 15.10.2015
	// static private String[][] defaultHexs = { { "850BEE", "FB003A", "0F5EEE",
	// "339415", "DF9D1C", "118783" },
	// { "BF185C", "1A5E8F", "2C4F01", "3F0174", "7A001D", "022A74" } };

	static private ListMap<Integer, UserColor> userColors;

	private int nBags;

	/**
	 * Default constructor, relies on default colors.
	 */
	public DefaultColorProvider() {
		setColors(defaultHexs);
	}

	/**
	 * Inits the color provider by setting all its colors.
	 * 
	 * @param colors
	 *            : one color array per bag in the color provider: strings with
	 *            3 hexadecimal values inside (e.g. "06B2EC")
	 */
	public void setColors(String[][] colors) {
		// Create the userColors, set them in the bags
		userColors = new ListMap<Integer, UserColor>();

		for (int i = 0; i < colors.length; i++) {
			String[] bag = colors[i];
			for (int j = 0; j < bag.length; j++) {

				try {
					String hexCode = bag[j]; // may fail
					int[] rgb = hexStringToIntArray(hexCode);

					userColors.addObject(i, new UserColor(this, i, rgb[0], rgb[1], rgb[2]));
				} catch (Exception e) {
				} // no matter

			}

		}
		nBags = userColors.size();
	}

	/**
	 * Inits the color provider by setting all its colors.
	 * 
	 * @param colors
	 *            : one color array per bag in the color provider: values are
	 *            rgb
	 */
	public void setColors(int[][] colors) {
		// Create the userColors, set them in the bags
		userColors = new ListMap<Integer, UserColor>();

		for (int i = 0; i < colors.length; i++) {
			int[] bag = colors[i];
			for (int j = 0; j < bag.length; j += 3) {
				int v1 = bag[j];
				int v2 = bag[j + 1];
				int v3 = bag[j + 2];

				userColors.addObject(i, new UserColor(this, i, v1, v2, v3));
			}
		}
		nBags = userColors.size();

	}

	public static int[] hexStringToIntArray(String s) {
		int len = s.length();
		int[] data = new int[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (int) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Draws and returns a color in bag 0 (for convenience).
	 */
	@Override
	public UserColor getColor() {
		return getColor(0); // draw in first color bag
	}

	/**
	 * Draws and returns an available color in the given bag. If no available
	 * colors in the bag, returns a default color for this bag.
	 */
	@Override
	public UserColor getColor(int bag) {
		// Fix bag index if needed to avoid errors
		if (bag < 0)
			bag = 0;
		if (bag >= nBags)
			bag = bag % nBags;

		List<UserColor> ucs = userColors.get(bag);
		for (UserColor uc : ucs) {
			if (uc.isAvailable()) {
				uc.setAvailable(false);
				return uc; // return first available color in the bag
			}
		}

		// Could not find any available color in the bag
		// -> Set all of them available and return the first one
		for (UserColor uc : ucs) {
			uc.setAvailable(true);
		}

		UserColor uc = ucs.get(0);
		uc.setAvailable(false);

		return uc;

	}

}
