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

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Extends Repast RangeWidget 
 */

public class ExtendedRangeWidget extends JPanel implements IExtendedPropertyWidget {

	private String propertyName;
	private JSlider slider;
	private JTextField field;
	private int min, max;
	private ArrayList listeners = new ArrayList();
	//private boolean noError = true;

	public ExtendedRangeWidget(int min, int max, int tickSpacing) {
		super(new FlowLayout());
		slider = new JSlider(min, max);
		field = new JTextField(String.valueOf(max).length());
		add(slider);
		add(field);
		this.min = min;
		this.max = max;
		slider.setMajorTickSpacing(tickSpacing);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (evt.getSource() == field) {
					// change the value to the fields value
					// check for non-integer value.
					setValue(new Integer(field.getText()));
				} else {
					// change the field to the slider value
					field.setText(String.valueOf(slider.getValue()));
					if (slider.getValueIsAdjusting() == false) fireActionPerformed();
				}
			}
		});

		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//if (noError) {
				setSlider();
				setValue(new Integer(field.getText()));
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
			Integer i = new Integer(field.getText());
			setValue(i);
		} catch (NumberFormatException ex) {
			Integer i = (Integer)getValue();
			field.setText(i.toString());
			setValue(i);

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
		int iVal = ((Integer)val).intValue();
		iVal = iVal < min ? min : iVal > max ? max : iVal;
		field.setText(String.valueOf(iVal));
		slider.setValue(iVal);
	}

	public Object getValue() {
		return new Integer(slider.getValue());
	}

	public void requestFocus() {
		slider.requestFocus();
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
		slider.setEnabled(b);
		field.setEnabled(b);
	}
	
	public void setToolTipText(String text) {
		  slider.setToolTipText(text);
		  field.setToolTipText(text);
		  
	  }
}


