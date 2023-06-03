/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.util;

import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

/**
 * Extends Repast TextFieldWidget 
 */

public class TextFieldWidget extends JTextField implements IExtendedPropertyWidget {

  String propertyName = null;

  public TextFieldWidget(int cols) {
    super(cols);
  }

  public void setPropertyName(String propName) {
    propertyName = propName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setValue(Object value) {
    if (value == null) {
      setText("Null");
      setEnabled(false);
    } else {
      setText(value.toString());
    }
  }
  
  public void setEnabled(boolean b) {
	  super.setEnabled(b);
  }

  public Object getValue() {
    return getText();
  }

  public void addActionListener(ActionListener l) {
    super.addActionListener(l);
    addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        fireActionPerformed();
      }
    });
  }
  
  public void setToolTipText(String text) {
	  super.setToolTipText(text);
  }

}
