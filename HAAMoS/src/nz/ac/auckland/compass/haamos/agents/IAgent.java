/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.agents;

import nz.ac.auckland.compass.haamos.space.SpatialArea;

/**
 * An interface for defining agents' behaviour signatures. 
 * Agents in <em> HAAMoS </em> should implement these methods (behaviours).
 * 
 * @author Babak Mahdavi Ardestani
 *
 */
public interface IAgent {
	//public void setPreferences(Hashtable p);
	//public Hashtable getPreferences();
	public int getEthnicGroupID();
	public SpatialArea getRefToSpatialUnit();
	
	public void setEthnicGroupID(int id);
	public void setRefToSpatialUnit(SpatialArea u);
	
}
