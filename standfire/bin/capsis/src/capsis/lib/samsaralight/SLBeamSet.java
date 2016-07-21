/* 
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
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
package capsis.lib.samsaralight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A light beam set. Contains SLBeam instances.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLBeamSet implements Serializable {

	private List<SLBeam> beams;

	// Potential diffuse energy coming from angles higher
	// than angleMin without interception on a horizontal surface in MJ/m2
	private float horizontalDiffuse;
	private float horizontalDirect;

	// Potential diffuse energy coming from angles higher than angleMin
	// refering to the slope without interception on a surface parallel to
	// the slope in MJ/m2
	private float slopeDiffuse;
	private float slopeDirect;

	/**
	 * Constructor.
	 */
	public SLBeamSet() {
		beams = new ArrayList<SLBeam>();
		horizontalDiffuse = 0;
		horizontalDirect = 0;
		slopeDiffuse = 0;
		slopeDirect = 0;
	}

	public void addBeam(SLBeam b) {
		beams.add(b);
	}

	public List<? extends SLBeam> getBeams() {
		return beams;
	}

	public double getHorizontalDiffuse() {
		return (double) horizontalDiffuse;
	}

	public void setHorizontalDiffuse(double e) {
		horizontalDiffuse = (float) e;
	}

	public double getHorizontalDirect() {
		return (double) horizontalDirect;
	}

	public void setHorizontalDirect(double e) {
		horizontalDirect = (float) e;
	}

	public double getSlopeDiffuse() {
		return (double) slopeDiffuse;
	}

	public void setSlopeDiffuse(double e) {
		slopeDiffuse = (float) e;
	}

	public double getSlopeDirect() {
		return (double) slopeDirect;
	}

	public void setSlopeDirect(double e) {
		slopeDirect = (float) e;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("SLBeamSet");
		b.append(" #beams: ");
		b.append(beams == null ? 0 : beams.size());
		return b.toString();
	}

	public String bigString() {
		StringBuffer b = new StringBuffer();
		b.append(toString());
		// beams
		b.append(", beams: \n");
		for (SLBeam beam : getBeams()) {

			b.append(beam.toString());
			b.append("\n");
		}
		return b.toString();
	}
	
	/**
	 * Adapted bigstring to produce a csv file in the console
	 * GL 30/04/2013
	 */
	public String bigCSVString() {
		StringBuffer b = new StringBuffer();
		//b.append(toString());
		// beams
		b.append("azimut" + ";" + "HeightAngle" + ";" + "Energy" + ";" + "isDirect" + "\n");
		for (SLBeam beam : getBeams()) {

			//b.append(beam.toString());
			b.append(beam.toCSVString ());
			b.append("\n");
		}
		return b.toString();
	}

}