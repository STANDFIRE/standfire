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
package capsis.lib.fire.exporter.firetec;

/**
 * NOT USED : previously used for visualization of export (see F Coligny)
 */

/**
 * CValue: a value in the cMap control SetMap
 * 
 * @author F. de Coligny - january 2008
 */
public class CValue {

	public int fi;
	public int fj;
	public int fk;
	public double contribution;

	public CValue(double fi, double fj, double fk, double contribution) {
		this.fi = (int) fi;
		this.fj = (int) fj;
		this.fk = (int) fk;
		this.contribution = contribution;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append('f');
		b.append('(');
		b.append(fi);
		b.append('-');
		b.append(fj);
		b.append('-');
		b.append(fk);
		b.append(')');
		b.append('-');
		b.append(contribution);
		return b.toString();
	}
}
