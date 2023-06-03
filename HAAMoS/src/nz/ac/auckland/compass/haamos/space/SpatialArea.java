/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.space;

import anl.repast.gis.GisAgent;
import anl.repast.gis.OpenMapAgent;

/**
 * Abstract class where spatial area units can descend from (defining/ declaring common characteristics).
 * 
 * @author Babak Mahdavi Ardestani
 */

public abstract class SpatialArea implements OpenMapAgent, GisAgent {
	
	protected int ethnicGroupsPopArray[]; //Array to keep different ethnic group population
		
	abstract public void report();

}
