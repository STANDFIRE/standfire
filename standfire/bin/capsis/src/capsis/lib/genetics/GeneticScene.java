/* 
* The Genetics library for Capsis4
* 
* Copyright (C) 2002-2004  Ingrid Seynave, Christian Pichot
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
package capsis.lib.genetics;

import java.util.Collection;

//import ventoug.model.VtgStand;
import capsis.kernel.Step;

/**	GeneticScene is an interface for scenes containing Genotypable objects.
*	@see VtgStand AlsStand etc...
*	@author F. de Coligny - november 2004
*/
public interface GeneticScene  {
	
	public Genotypable getGenotypable (int id);
	
	public Collection getGenotypables ();
	
	public Step getStep ();
	
	public int getDate ();
	
	public boolean isInitialScene ();
}
