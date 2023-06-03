/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.util;

import uchicago.src.reflector.PropertyDescriptor;
import uchicago.src.reflector.PropertyTextField;
import uchicago.src.reflector.PropertyWidget;
import java.awt.Dimension;

/**
 * Extends Repast TextFieldPropertyDescripter 
 */

public class TextFieldPropertyDescriptor extends ExtendedPropertyDescriptor {
	TextFieldWidget propTextField;
	public TextFieldPropertyDescriptor(String name)
	{
		super(name);
		//propTextField = new PropertyTextField(1);
		propTextField = new TextFieldWidget(1);
		//propTextField.setEnabled(false);

		super.widget = propTextField;
	}
	
	public void setPrefferedSize(int width, int height) {
		propTextField.setMaximumSize(new Dimension(width,height));
		
	}
	
	public IExtendedPropertyWidget getWidget() {
		return propTextField;
	}

}

