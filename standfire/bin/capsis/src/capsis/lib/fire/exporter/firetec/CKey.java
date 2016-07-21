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
 * CKey: a key in the cMap control SetMap
 * 
 * @author F. de Coligny - january 2008
 */
public class CKey implements Comparable {

	public String source; // source is either a FiPlant or a FiLayerSet
	public int ci;
	public int cj;
	public int ck;

	/**
	 * Constructor.
	 */
	public CKey(String source, int ci, int cj, int ck) {
		this.source = source;
		this.ci = ci;
		this.cj = cj;
		this.ck = ck;
	}

	/**
	 * Comparable interface.
	 */
	public int compareTo(Object other) {
		if (!(other instanceof CKey)) {
			return -1;
		}
		CKey o = (CKey) other;
		int c = o.source.compareTo(source);
		if (c < 0) {
			return 1;
		} else if (c > 0) {
			return -1;
		} else { // c == 0
			if (o.ci < ci) {
				return 1;
			} else if (o.ci > ci) {
				return -1;
			} else {
				if (o.cj < cj) {
					return 1;
				} else if (o.cj > cj) {
					return -1;
				} else {
					if (o.ck < ck) {
						return 1;
					} else if (o.ck > ck) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(source);
		b.append('-');
		b.append('c');
		b.append('(');
		b.append(ci);
		b.append('-');
		b.append(cj);
		b.append('-');
		b.append(ck);
		b.append(')');
		return b.toString();
	}
}
