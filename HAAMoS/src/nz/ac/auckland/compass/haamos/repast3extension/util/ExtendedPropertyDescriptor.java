/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.util;

import uchicago.src.reflector.PropertyDescriptor;
import uchicago.src.reflector.PropertyWidget;

/**
 * Extends Repast PropertyDescripter 
 */

public abstract class ExtendedPropertyDescriptor extends PropertyDescriptor {
    IExtendedPropertyWidget xWidget;
	public ExtendedPropertyDescriptor(String name, IExtendedPropertyWidget widget) {
		super(name, widget);
		this.xWidget = widget;
	}
	
	public ExtendedPropertyDescriptor(String name) {
	    this(name, null);
	  }
	  
	
	public IExtendedPropertyWidget getWidget() {
	    return xWidget;
	  }
}
