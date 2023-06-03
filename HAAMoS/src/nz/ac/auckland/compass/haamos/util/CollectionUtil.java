/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.util;

import java.util.ArrayList;
import java.util.Vector;

import nz.ac.auckland.compass.haamos.agents.Household;
import nz.ac.auckland.compass.haamos.space.AreaUnit;


/**
 * This is a utlitlity class for collection.
 * 
 * @author Babak Mahdavi Ardestani
 *
 */
public class CollectionUtil {
	
	public static void addAllToVectorWithoutDuplicate(Vector sourceVec, Vector toBeAddedVect){
		for (int i=0; i<toBeAddedVect.size(); i++) {				
			Object obj = toBeAddedVect.get(i);
			if (!sourceVec.contains(obj)) {
				sourceVec.add(obj);
			}
		}
	}
	
	
	public static void addAllToAUvectorWithoutDuplicate(Vector<AreaUnit> sourceVec, Vector<AreaUnit> toBeAddedVect){
		for (int i=0; i<toBeAddedVect.size(); i++) {				
			AreaUnit au = (AreaUnit) toBeAddedVect.get(i);
			if (!sourceVec.contains(au)) {
				sourceVec.add(au);
			}
		}
	}
	
	public static void addAllToArrayListWithoutDuplicate(ArrayList sourceList, ArrayList toBeAddedList){
		for (int i=0; i<toBeAddedList.size(); i++) {				
			Object obj = toBeAddedList.get(i);
			if (!sourceList.contains(obj)) {
				sourceList.add(obj);
			}
		}
	}
	
	public static void addAllToAUarrayListWithoutDuplicate(ArrayList<AreaUnit> sourceList, ArrayList<AreaUnit> toBeAddedList){
		for (int i=0; i<toBeAddedList.size(); i++) {				
			AreaUnit au = (AreaUnit) toBeAddedList.get(i);
			if (!sourceList.contains(au)) {
				sourceList.add(au);
			}
		}
	}
	
	
	
}
