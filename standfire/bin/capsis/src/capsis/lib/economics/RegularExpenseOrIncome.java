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

import java.io.Serializable;

/**
 * 
 * 
 * @author Ch. Orazio - january 2003
 */
public class RegularExpenseOrIncome implements Serializable {
	
	private int fromDate;
	private int toDate;
	private String label;
	private double expense;
	private double income;
	
	public void setFromDate (int v) {fromDate = v;}
	public void setToDate (int v) {toDate = v;}
	public void setLabel (String v) {label = v;}
	public void setExpense (double v) {expense = v;}
	public void setIncome (double v) {income = v;}
		
	public int getFromDate () {return fromDate;}
	public int getToDate () {return toDate;}
	public String getLabel () {return label;}
	public double getExpense () {return expense;}
	public double getIncome () {return income;}
		
}

