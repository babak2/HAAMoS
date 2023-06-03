/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.util;

import uchicago.src.reflector.PropertyDescriptor;
import uchicago.src.reflector.RangeWidget;

/**
 * Displays GisArealUnitAgents population graphically (in a form of pie charts)
 */

public class DecimalRangePropertyDescriptor extends PropertyDescriptor {
	
	public DecimalRangePropertyDescriptor(String name, double min, double max, double tickSpacing){
		super(name);
		
		//min = Math.min(min, max);
	    //max = Math.max(min, max);
	    
	    super.widget = new DecimalRangeWidget(min, max, tickSpacing);
	}
}
