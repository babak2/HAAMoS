/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.agents;

import nz.ac.auckland.compass.haamos.base.Const.LOC_PREF;
import nz.ac.auckland.compass.haamos.space.SpatialArea;

/**
 * An interface for defining individual/ household agents' preferences signatures. 
 * Household agents in <em> HAAMoS </em> should implement these methods.
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public interface IPreference {
	public LOC_PREF getLocationPref();
	public double getToleranceThresholdPref();
	public int getSearchLimit();
	
	public void setLocationPref(LOC_PREF locPref);
	public void setToleranceThresholdPref(double t);
	public void setSearchLimit(int l);


}
