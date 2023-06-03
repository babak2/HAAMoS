/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */

package nz.ac.auckland.compass.haamos.repast3extension.util;

import uchicago.src.reflector.PropertyDescriptor;
import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.reflector.RangeWidget;

/**
 * Extends Repast RangePropertyDescriptor 
 */

public class ExtendedRangePropertyDescriptor  extends RangePropertyDescriptor {
	IExtendedPropertyWidget xWidget;
	public ExtendedRangePropertyDescriptor(String name, int min, int max, int tickSpacing)
	{
		super(name, min, max, tickSpacing);
		//min = Math.min(min, max);
		//max = Math.max(min, max);

		xWidget = new ExtendedRangeWidget(min, max, tickSpacing);
		//super.widget.s
	}
	
	public IExtendedPropertyWidget getWidget() {
		return xWidget;
	}

}
