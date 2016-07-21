/*
* The ifn library for Capsis4
*
* Copyright (C) 2006 J-L Cousin, M-D Van Damme
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

package capsis.lib.ifnutil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JTextField;


public class RateField extends JTextField {
	
	/**
	   * Interface Listener
	   * 
	   */
	  public interface RateListener {
		  void isRate(float f);
		  void isNotRate(String s);
	  }
	  
	  /**
	   * 
	   */
	  public class RateListenerSupport {
		  private Collection<RateListener> listeners = new ArrayList<RateListener>();
		  
		  public void addRateListener(RateListener listener){
			  listeners.add(listener);
		  }
		  
		  public void removeRateListener(RateListener listener){
			  listeners.remove(listener);
		  }
		  
		  public void fireIsRate(float f){
			  for(RateListener listener : listeners){
				  listener.isRate(f);
			  }
		  }
		  
		  public void fireIsNotRate(String s){
			  for(RateListener listener : listeners){
				  listener.isNotRate(s);
			  }
		  }
	  }
	  
	  /**
	   * 
	   */
	  private RateListenerSupport listenerSupport = new RateListenerSupport();
	  
	  /**
	   * 
	   *
	   */
	  public RateField (){
		  addFocusListener(new FocusListener(){
			 public void focusGained(FocusEvent e){
				 
			 }
			 public void focusLost(FocusEvent e){
				 valid();
			 }
		  });
		  
		  addActionListener(new ActionListener (){
			 public void actionPerformed(ActionEvent e){
				 
			 }
		  });
		  setColumns(4);
	  }
	  
	  /**
	   * 
	   *
	   */
	  protected void valid() {
		  String currentText = getText();
		  try {
			  float f = Float.parseFloat(currentText);
			  if(f >= 0 && f <= 1)
				  fireIsRate(f);
			  else fireIsNotRate(currentText);
		  } catch(NumberFormatException e){
			  fireIsNotRate(currentText);
		  }
	  }
	  
	  /**
	   * 
	   * @param listener
	   */
	  public void addRateListener(RateListener listener){
		  listenerSupport.addRateListener(listener);
	  }
	  
	  /**
	   * 
	   * @param listener
	   */
	  public void removeRateListener(RateListener listener){
		  listenerSupport.removeRateListener(listener);
	  }
	  
	  /**
	   * 
	   * @param f
	   */
	  protected void fireIsRate(float f){
		  listenerSupport.fireIsRate(f);
	  }
	  
	  /**
	   * 
	   * @param s
	   */
	  protected void fireIsNotRate(String s){
		  listenerSupport.fireIsNotRate(s);
	  }
}
