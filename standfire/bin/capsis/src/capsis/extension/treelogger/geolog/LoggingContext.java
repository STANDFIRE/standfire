/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.treelogger.geolog;

import java.util.HashMap;
import java.util.Map;

/**	
 * LoggingContext manage the logging process of a tree.
 * It reminds the context between each call to cutLog():
 * - height and length of the last log produced
 * - number of logs produced (considering several types of products) <br>
 * <br>
 * The logging context is now embedded in the GeoLogTreeData class (MF2011-11-30).
 * <br>
 * @author F. Mothe - January 2006
 * @author Mathieu Fortin - November 2011 (refactoring)
 */
public class LoggingContext {
	
	private double height;			// current position (m)
	private double length;			// length of the next log to cut (m)
//	private double cutLogLength;	// length of the previous log (if any) (m)
	private double totalHeight;		// total height of the tree (m)
	private double crownBaseHeight;	// crown base height of the tree (m)
	private int logCount;			// total number of logs

	// Map of number of logs by product type
	// key = ProductId, value = count
	private Map <Integer, Integer> logCountByProduct;

	/**
	 * Protected constructor.
	 * @param baseHeight the base of the tree (m)
	 * @param totalHeight the tree height (m)
	 * @param crownBaseHeight the crown base height (m)
	 */
	protected LoggingContext(double baseHeight, double totalHeight, double crownBaseHeight) {
		this.height = baseHeight;
		this.length = 0;
//		this.cutLogLength = 0;
		this.totalHeight = totalHeight;
		this.crownBaseHeight = crownBaseHeight;
		this.logCount = 0;
		this.logCountByProduct = new HashMap  <Integer, Integer> ();
	}
	
	
	protected void addProduct(int productId) {
		logCountByProduct.put(productId, 0);
	}

//	public int getNbProducts () {
//		return logCountByProduct.size ();
//	}

	//	Cut a log of given productId with length = this.length
	//	Update this.height and this.cutLogLength
	protected void cutLog(int productId) {
		height += length;
//		cutLogLength = length;
		length = 0;
		logCount++;

		// Add the product if not yet done :
		if (! logCountByProduct.containsKey (productId))
			addProduct (productId);
		
		logCountByProduct.put (productId, logCountByProduct.get (productId)+1);
	}

	//	Length of the next log to cut
	public void setLength(double length) {
		this.length = length;
	}

	//	Length available for the next log
	public double getAvailableLength (boolean acceptCrown) {
		return (acceptCrown ? totalHeight : crownBaseHeight) - height;
	}

	/**
	 * This method returns the length of the next log to be cut
	 * @return the length (m)
	 */
	public double getLength() {
		return length;
	}
	
//	//	Length of the last cut log
//	public double getCutLogLength () {
//		return cutLogLength;
//	}
	
	//	Top of the last cut log (= bottom of the next one)
	/**
	 * This method returns the height of the top section of the last log cut so far, that is the bottom of the next one.
	 * @return the height (m)
	 */
	public double getHeight() {
		return height;
	}

//	public void setHeight_TEMPO (double height) {
//		this.height = height;
//	}

//	//	Bottom of the last cut log
//	public double getCutLogBottomHeight () {
//		return height - cutLogLength;
//	}

	/**
	 * This method returns the number of logs produced so far.
	 * @return an integer
	 */
	protected int getLogCount() {
		return logCount;
	}

	//	Return the number of logs produced for the given productId
	public int getLogCount (int productId) {
		return logCountByProduct.containsKey (productId) 
				? logCountByProduct.get (productId) : 0;
	}
}
