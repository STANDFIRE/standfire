/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.lib.economics;

import java.util.Collection;

/**
 * Lis of functions that must be added in a model to compute economical balance
 *
 * @author Ch. Orazio - january 2003
 */
public interface EconModel {

	public Collection getRegularExpenseOrIncomes ();
	public void addRegularExpenseOrIncomes (RegularExpenseOrIncome v);
	public void removeRegularExpenseOrIncomes (RegularExpenseOrIncome v);
	public double getActualizationRate ();
	public void setActualizationRate (double v);
	public int getEconomicModelStartingDate ();
	public void setEconomicModelStartingDate (int v);
	public String getEconomicBalanceParameters ();
	public void setEconomicBalanceParameters (String v);
	/*public HashMap getEconTool (String s);
	public void addEconTool (Object m, String s);
	public void removeEconTool (String s);
	public void initializeEconTools (String s);*/
}

