/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.test;


import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.ObjectArrayList;

import nz.ac.auckland.compass.haamos.agents.Realtor.VirtualAreaUnit;
import nz.ac.auckland.compass.haamos.space.AreaUnit;

/**
 * Helping methods for easier testing of the model.
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class TestHelper {


	public static void printAUinfo(Vector auVect) {
		for (int i=0; i <auVect.size(); i++) {
			AreaUnit au = (AreaUnit) auVect.elementAt(i);
			System.out.println("id: "+au.getAU_ID()+", name: "+au.getAU_NAME());
		}
	}
	
	public static void printAUinfoByWhile(Vector auVect) {
		   Iterator it = auVect.iterator();
			
			while (it.hasNext()) {
				AreaUnit au = (AreaUnit) it.next();
				System.out.println("id: "+au.getAU_ID()+", name: "+au.getAU_NAME());
			}
		
	}
	public static void printAUList(AbstractList list) {
		   Iterator it = list.iterator();
			while (it.hasNext()) {
				AreaUnit au = (AreaUnit) it.next();
				//System.out.println("gisId: "+au.getGisAgentIndex()+", name: "+au.getAU_NAME());
				au.printShortInfo();
			}
	}
	
	public static void printAUListPropStat(AbstractList<AreaUnit> list) {
		
		  double totalProp_g1=0;
		  double totalProp_g2=0;
		  double totalProp_g3=0;
		  double totalProp_g4=0;

		  for (AreaUnit au : list ) {
			  //double auPop = au.getArealUnitPop();
			  //double auPop_g1 = au.getCo_ethnicPercentage(0);
			  System.out.print("au= "+au.getGisAgentIndex()+", Prop (g1, g2, g2, g4)=");
			  System.out.print(" "+ au.getCo_ethnicPercentage(0));
			  System.out.print(", "+ au.getCo_ethnicPercentage(1));
			  System.out.print(", "+ au.getCo_ethnicPercentage(2));
			  System.out.println(", "+ au.getCo_ethnicPercentage(3));
			  
			  totalProp_g1+= au.getCo_ethnicPercentage(0);
			  totalProp_g2+= au.getCo_ethnicPercentage(1);
			  totalProp_g3+= au.getCo_ethnicPercentage(2);
			  totalProp_g4+= au.getCo_ethnicPercentage(3);
		  }
		  
		  double nbOfAUs = list.size();
		  
		  double avgProp_g1 =  totalProp_g1/nbOfAUs;
		  double avgProp_g2 =  totalProp_g2/nbOfAUs;
		  double avgProp_g3 =  totalProp_g3/nbOfAUs;
		  double avgProp_g4 =  totalProp_g4/nbOfAUs;
		  
		  System.out.println("-----------------------");
		  System.out.println("AVG Prop (g1, g2, g3, g4) = "+ avgProp_g1+", "+avgProp_g2+
		    	                                           ", "+avgProp_g3+", "+avgProp_g4);	  
	}
	
	public static void printAUList4(AbstractList list) {
		
		for (int i=0; i <list.size(); i++) {
			AreaUnit au = (AreaUnit) list.get(i);
			System.out.println("id: "+au.getAU_ID()+", name: "+au.getAU_NAME());
		}
		   
		/*Iterator it = list.iterator();
			while (it.hasNext()) {
				AreaUnit au = (AreaUnit) it.next();
				System.out.println("id: "+au.getAU_ID()+", name: "+au.getAU_NAME());
			} */
	}
	
	
  public static void printVirtualAUList(ArrayList<VirtualAreaUnit> list) {
	  System.out.println("-print VirtualAreaUnit of size: "+list.size());
		for (int i=0; i <list.size(); i++) {
			VirtualAreaUnit vau = list.get(i);
			System.out.println("id: "+vau.ref_AU.getGisAgentIndex()+", HH-gID: "+vau.ref_HH.groupIndex+ ", coE%: "+vau.getCo_ethnicPC());
		}
		   

	}

	public static void printDoubleArrayList(DoubleArrayList list) {

		System.out.println("DoubleArraySize: "+list.size());
		for (int i=0; i <list.size(); i++) {
			System.out.println("val: "+list.get(i));
		}
	}
	
	
	public static void printAUsinObjectArrayList(ObjectArrayList list) {

		for (int i=0; i <list.size(); i++) {
			((AreaUnit)list.get(i)).printShortInfo();
		}
	}
	
	public static void printVAUsinObjectArrayList(ObjectArrayList list) {

		for (int i=0; i <list.size(); i++) {
			((VirtualAreaUnit)list.get(i)).printVAUinfo();
		}
	}

}
