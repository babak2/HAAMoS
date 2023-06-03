/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.agents;


import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.base.Const.LOC_PREF;
import nz.ac.auckland.compass.haamos.space.AreaUnit;

/**
 * Household class encapsulating the moving household agents with specific (individual) preferences 
 * in search of new residence. 
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class Household implements IPreference{
	public int groupIndex;
	public int auID = -1;  //-1 if coming if new to the world
	private static long hhIDCounter=0;
	public long hhID;
	public int timeSpentInPool=0;
	
	private LOC_PREF locPref;
	double toleranceThresholdPref;
	int searchLimit;
	
   protected int preferredTA_ID;  //preferred TA ID
	//protected Hashtable prefHT;
	protected AreaUnit refernceToAU;  // NULL if new to the world 
	protected double creationTickTime;
	
	public Household(int gInd) {
		groupIndex = gInd;
		hhID = hhIDCounter++;
	}

	public Household(int gInd, AreaUnit au) {
		this(gInd);
		if (au != null) {
			this.auID = au.getGisAgentIndex();
		}
		else auID=-1;	
		//groupIndex = gInd;
		this.refernceToAU = au;
	}
	
	public Household(int gInd, AreaUnit au, double ctt) {
		this (gInd, au);
		//prefHT = p;
		this.creationTickTime = ctt;
	}

	
	public boolean isNewArrival ()  { //new arrival are initially homeless
		if (refernceToAU == null)
			return true;
		else return false;
	}
	
	public int getEthnicGroupID() {
		return this.groupIndex;
	}
	
	public void getEthnicGroupID (int id) {
		this.groupIndex = id;
	}
	
	public AreaUnit getReferenceToAU () { // change this to SpatialUNit (IAgent)
		return refernceToAU;
	}
	
	public void setReferenceToAU (AreaUnit au) {  // change this to SpatialUNit (IAgent)
		refernceToAU = au;
		auID = au.getGisAgentIndex();
	}
	
	public double getCreationTime() {
		return this.creationTickTime;
	}
	
	public void setCreationTime(double ct) {
		creationTickTime = ct;
	}
		
	
	public LOC_PREF getLocationPref() {
		return locPref;
	}
	
	public void setLocationPref(LOC_PREF l) {
		 locPref =l;
	}
	
	public double getToleranceThresholdPref() {
		return toleranceThresholdPref;
	}
	
	public void setToleranceThresholdPref(double t) {
		toleranceThresholdPref=t;
	}
	
	public int getSearchLimit() {
		return searchLimit;
	}
	
	public void setSearchLimit(int sl) {
		 searchLimit = sl;
	}
	
	public String getLocationPrefAsString() {
		
		if (locPref == LOC_PREF.GLOBAL)
			return "Global";
		else return "Local";
	}
	
	public void increaseTimeSpentInPool() {
		timeSpentInPool++;
	}
	
	public int getTimeSpentInPool() {
		return timeSpentInPool;
	}
	
	public void setPreferredTA_ID(int taID) {
		this.preferredTA_ID=taID;
	}
	
	public int getPreferredTA_ID() {
		return 	this.preferredTA_ID;
	}
	
	public void printInfo() {
		 System.out.print("hh_id: "+this.hhID);
		 System.out.print(", gIndex= "+ this.groupIndex);
		 System.out.print(", newInWorld= "+ this.isNewArrival());
		 System.out.print(", T= "+ this.getToleranceThresholdPref());
		 System.out.print(", L= "+ this.getLocationPrefAsString());
		 System.out.print(", SearchLimit= "+ this.getSearchLimit());
		 System.out.print(", TimeInPool= "+ this.getTimeSpentInPool());
		 
		 if (refernceToAU != null) {
			 System.out.print(", located on AU-"+this.auID);
			 System.out.print(", auName= "+refernceToAU.getAU_NAME());
			 System.out.println(", pop: "+refernceToAU.getArealUnitPop()+  
					 ", g1: "+refernceToAU.getGroupPop(Const.G1_EURO)+
					 ", g2: "+refernceToAU.getGroupPop(Const.G2_ASIAN)+
					 ", (g1+g2): "+(refernceToAU.getGroupPop(Const.G1_EURO)+refernceToAU.getGroupPop(Const.G2_ASIAN))+
					 ", vac: "+ refernceToAU.getVacantSpace() +
					 ", (g1+g2+vac): "+(refernceToAU.getGroupPop(Const.G1_EURO)+refernceToAU.getGroupPop(Const.G2_ASIAN) + refernceToAU.getVacantSpace())
					 );

			 // refernceToAU.report();
		 }
		 else {
			 System.out.println(", [newInWorld]: preferredTA_ID = "+this.getPreferredTA_ID());
		 }
		 
		
	}

}
