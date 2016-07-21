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

package capsis.extension.treelogger.geolog.util;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Vector;

/**	NumericRecord : a set of numeric values (double) used for exporting
*	and averaging records of data
*
*	@author F. Mothe - august 2006
*/

public class NumericRecord {
	private double recordWeight;
	protected Vector <Double> vars;

	// Constructors :

	// Unitialised record (no variables) :
	public NumericRecord () {
		this.vars = new Vector <Double> ();
		this.recordWeight = 0;
	}

	// Empty initialised record :
	public NumericRecord (int nbValues) {
		this ();
		for (int v = 0; v<nbValues; ++v) {
			vars.add (0.0);
		}
	}

	// Record initialised by yet computed values :
	public NumericRecord (Collection <Double> values, double weight) {
		this ();
		this.recordWeight = weight;
		vars.addAll (values);
	}

	// Appends values to the record as new variables :
	public void append (Collection <Double> values) {
		vars.addAll (values);
	}

	// Appends one value to the record as a new variable :
	public void append (double value) {
		vars.add (value);
	}

	// Performs the addition for each value (this.vars += rec.vars) :
	public void cumulate (NumericRecord rec, double weight) {
		cumulate (rec.vars, weight);
	}

	// Performs the addition for each value (this.vars += values) :
	public void cumulate (Collection <Double> values, double weight) {
		if (values.size () != getNbValues ()) {
			System.out.println ("NumericRecord : adding values of size " +
					values.size () + " to record of size " + vars.size () + "!!");
		}
		int nval=0;
		for (Double value : values) {
			double sum = vars.get (nval) + weight * value;
			vars.set (nval++, sum);
		}
		recordWeight += weight;
	}

	/*
	// Divides by recordWeight and reset recordWeight to 1 :
	public void average () {
		if (recordWeight > 0) {
			for (int nval=0; nval<vars.size (); ++nval) {
				double average = vars.get (nval) / recordWeight;
				vars.set (nval, average);
			}
			recordWeight = 1.0;
		}
	}

	// Returns a new averaged record :
	public NumericRecord getMeanRecord () {
		NumericRecord rec = new NumericRecord (vars, recordWeight);
		rec.average ();
		return rec;
	}
	*/

	public double getWeight () {
		return recordWeight;
	}

	public int getNbValues () {
		return vars.size ();
	}

	public Vector <Double> getValues () {
		return vars;
	}

	// Returns the variable values (beginning with a separator) :
	public String getValues (String sep, NumberFormat nf) {
		String s = "";
		for (Double d : vars) {
			s += sep + nf.format (d);
		}
		return s;
	}

}
