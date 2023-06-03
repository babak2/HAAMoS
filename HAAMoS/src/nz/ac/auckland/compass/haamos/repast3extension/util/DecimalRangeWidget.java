/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.util;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import nz.ac.auckland.compass.haamos.awt.DecimalSlider;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uchicago.src.reflector.PropertyWidget;

public class DecimalRangeWidget extends JPanel implements PropertyWidget {
	 private String propertyName;
	  //private JSlider slider;
	  private DecimalSlider dSlider;
	  private JTextField field;
	  private double min, max;
	  private ArrayList listeners = new ArrayList();
	  //private boolean noError = true;
	  
	  public DecimalRangeWidget(double min, double max, double tickSpacing) {
	    super(new FlowLayout());
	    //slider = new JSlider(min, max);
	    dSlider = new DecimalSlider();
	    dSlider.setPrecision(1);
	    dSlider.setDecimalSliderMaximum(max);
	    dSlider.setDecimalSliderMinimum(min);
	    
	    
	    
	    field = new JTextField(String.valueOf(max).length());
	    //add(slider);
	    add(dSlider);
	    add(field);
	    this.min = min;
	    this.max = max;
	    //slider.setMajorTickSpacing(tickSpacing);
	    
	    dSlider.setDecimalSliderMajorTickSpacing(tickSpacing);
	    dSlider.setDecimalSliderMinorTickSpacing(tickSpacing);
	    
	    //slider.setPaintLabels(true);
	    //slider.setPaintTicks(true);
	    
	    dSlider.setPaintLabels(true);
	    dSlider.setPaintTicks(true);    

	    //slider.addChangeListener(new ChangeListener() {
	    dSlider.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent evt) {
	          if (evt.getSource() == field) {
	            // change the value to the fields value
	            // check for non-integer value.
	            //setValue(new Integer(field.getText()));
	            setValue(new Double(field.getText()));
	          } else {
	            // change the field to the slider value
	            //field.setText(String.valueOf(slider.getValue()));
	            field.setText(String.valueOf(dSlider.getValue()));
	            //if (slider.getValueIsAdjusting() == false) fireActionPerformed();
	            if (dSlider.getValueIsAdjusting() == false) fireActionPerformed();
	          }
	        }
	      });

	    field.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent evt) {
	          //if (noError) {
	          setSlider();
	          //setValue(new Integer(field.getText()));
	          setValue(new Double(field.getText()));
	          fireActionPerformed();
	            //}
	        }
	      });

	    field.addFocusListener(new FocusAdapter() {
	        public void focusLost(FocusEvent evt) {
	          //if (noError) {
	          setSlider();
	          fireActionPerformed();
	            // }
	        }
	      });
	  }

	  private void setSlider() {
	    try {
	      //Integer i = new Integer(field.getText());
	      Double i = new Double(field.getText());
	      setValue(i);  ///
	    } catch (NumberFormatException ex) {
	      //Integer i = (Integer)getValue();
	      Double i = (Double)getValue();
	      field.setText(i.toString());
	      setValue(i);  ////

	      /*
	      // we set noError to false here so that when we pop up the error
	      // dialog this doesn't recall this setSlider method when the
	      // text field loses focus.
	      noError = false;
	      SimUtilities.showError("Invalid Parameter for " + propertyName +
				     "\nRange properties must be integers", ex);
	      noError = true;
	      */
	    }
	  }
	    

	  public String getPropertyName() {
	    return propertyName;
	  }

	  public void setPropertyName(String name) {
	    propertyName = name;
	  }

	  public void setValue(Object val) {
	    //int iVal = ((Integer)val).intValue();
	    double iVal = ((Double)val).doubleValue();
	    iVal = iVal < min ? min : iVal > max ? max : iVal;
	    field.setText(String.valueOf(iVal));
	    //slider.setValue(iVal);
	    //dSlider.setValue(iVal);
	    dSlider.setDecimalSliderValue(iVal);
	  }

	  public Object getValue() {
	    //return new Integer(slider.getValue());
	    return new Double(dSlider.getValue());
	  }

	  public void requestFocus() {
	    //slider.requestFocus();
	    dSlider.requestFocus();
	  }
	  
	  public void addActionListener(ActionListener l) {
	    listeners.add(l);
	  }

	  private void fireActionPerformed() {
	    ArrayList list;
	    synchronized (listeners) {
	      list = (ArrayList)listeners.clone();
	    }

	    for (int i = 0; i < list.size(); i++) {
	      ActionListener l = (ActionListener)list.get(i);
	      l.actionPerformed(new ActionEvent(this, 0, "action"));
	    }
	  }
	  
	  public void setEnabled(boolean b) {
	    //slider.setEnabled(b);
	    dSlider.setEnabled(b);
	    field.setEnabled(b);
	  }

}
