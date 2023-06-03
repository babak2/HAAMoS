/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.agents;

import java.math.BigDecimal;
import java.util.Iterator;

import uchicago.src.sim.util.Random;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;

import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.base.Const.LOC_PREF;
import nz.ac.auckland.compass.haamos.space.AreaUnit;
import nz.ac.auckland.compass.haamos.space.GeoSpace;
import nz.ac.auckland.compass.haamos.test.WriteHelper;
import nz.ac.auckland.compass.haamos.util.SortComparatorUtils;
import nz.ac.uoa.sgges.babak.HAAMoS.HAAMoSModel;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.ObjectArrayList;
import cern.jet.stat.Descriptive;

/**
 * Realtor is currently a model mechanism rather than an agent, <em> per se </em>
 * Notwithstanding, as such, it may be still used as proxy to act as a single centralised discriminatory agency (e.g. organisation/ agency discriminatory practice, ...).
 * <p> TODO:  As a 'placeholder', further extensions of this model can eventually include realtor agents in realistic fashion throughout the neighbourhoods and metropolitan area.   
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class Realtor {


	public class VirtualAreaUnit {
		private int ethnicGroupsPopArray[] = new int [model.nbOfEthnicGroups];

		public AreaUnit ref_AU;
		public Household ref_HH;		//int pop_g1;

		public double getCo_ethnicPC(int gId){
			double res =0;
			if (getArealUnitPop() >0)
				res = ethnicGroupsPopArray[gId]/getArealUnitPop();
			return res;
		}

		public double getCo_ethnicPC(){
			double res =0;
			if (getArealUnitPop() >0)
				res = ethnicGroupsPopArray[ref_HH.groupIndex]/getArealUnitPop();
			return res;
		}

		public  double getArealUnitPop() {
			double totPop =0;
			for (int i = 0; i < model.nbOfEthnicGroups; i++) {
				totPop += getGroupPop(i);
			}
			return totPop;
		}

		public  int getGroupPop (int groupNb){
			return ethnicGroupsPopArray [groupNb];
		}

		public  void setGroupPop (int groupNb, int pop){
			ethnicGroupsPopArray [groupNb] = pop;
		}

		public void printVAUinfo(){

			System.out.print("t-time: "+ model.schedule.getCurrentTime());
			System.out.print(", VAU-->GIS-ID= "+ ref_AU.getGisAgentIndex());
			System.out.println(", VAU-getCo_ethnicPC: "+ getCo_ethnicPC());
		} 
	}


  ///-------------------------------------
	GeoSpace geoSpace; 
	public HAAMoSModel model;

	ObjectArrayList  candidateAUList; // to keep a list of those candidate regions
	DoubleArrayList candidateAU_coEthnicPClist; //to keep list of co_ethnic Percentage of potential/candidate regions

	ArrayList<AreaUnit> neighborArrList = new ArrayList<AreaUnit>(20);

   //---------------------------------------------

	private static int movedToMatchAU_g1=0;
	private static int movedToMatchAU_g2=0;
	private static int movedToMatchAU_g3=0;
	private static int movedToMatchAU_g4=0;
	
	private static int last_movedToMatchAU_g1=0;
	private static int last_movedToMatchAU_g2=0;
	private static int last_movedToMatchAU_g3=0;
	private static int last_movedToMatchAU_g4=0;

	private static double movedToBestAU_g1=0;
	private static double movedToBestAU_g2=0;
	private static double movedToBestAU_g3=0;
	private static double movedToBestAU_g4=0;

	private static double last_movedToBestAU_g1=0;
	private static double last_movedToBestAU_g2=0;
	private static double last_movedToBestAU_g3=0;
	private static double last_movedToBestAU_g4=0;

	private static double remainedInSameAU_g1=0;
	private static double remainedInSameAU_g2=0;
	private static double remainedInSameAU_g3=0;
	private static double remainedInSameAU_g4=0;
	
	private static double last_remainedInSameAU_g1=0;
	private static double last_remainedInSameAU_g2=0;
	private static double last_remainedInSameAU_g3=0;
	private static double last_remainedInSameAU_g4=0;

	private static double newImmiSettledMatch_g1=0;
	private static double newImmiSettledMatch_g2=0;
	private static double newImmiSettledMatch_g3=0;
	private static double newImmiSettledMatch_g4=0;
	
	private static double last_newImmiSettledMatch_g1=0;
	private static double last_newImmiSettledMatch_g2=0;
	private static double last_newImmiSettledMatch_g3=0;
	private static double last_newImmiSettledMatch_g4=0;
		
	private static double newImmiSettledBest_g1=0;
	private static double newImmiSettledBest_g2=0;
	private static double newImmiSettledBest_g3=0;
	private static double newImmiSettledBest_g4=0;
	
	private static double last_newImmiSettledBest_g1=0;
	private static double last_newImmiSettledBest_g2=0;
	private static double last_newImmiSettledBest_g3=0;
	private static double last_newImmiSettledBest_g4=0;
	
	private static double cum_match_T_really_bigger_g1 =0;
	private static double cum_match_T_really_bigger_g2 =0;
	private static double cum_match_T_really_bigger_g3 =0;
	private static double cum_match_T_really_bigger_g4 =0;

	private static double cum_match_T_really_not_bigger_g1 =0;
	private static double cum_match_T_really_not_bigger_g2 =0;
	private static double cum_match_T_really_not_bigger_g3 =0;
	private static double cum_match_T_really_not_bigger_g4 =0;

	private static double last_match_T_really_bigger_g1 =0;
	private static double last_match_T_really_bigger_g2 =0;
	private static double last_match_T_really_bigger_g3 =0;
	private static double last_match_T_really_bigger_g4 =0;
	
	private static double last_match_T_really_not_bigger_g1 =0;
	private static double last_match_T_really_not_bigger_g2 =0;
	private static double last_match_T_really_not_bigger_g3 =0;
	private static double last_match_T_really_not_bigger_g4 =0;

	
	private void updatePlacementStat(int caseNb, int gIndex) {

		switch (caseNb) { 
		case 0: 
			switch (gIndex) { // find matching place (turnovered)
			case 0:  movedToMatchAU_g1++; break;
			case 1:  movedToMatchAU_g2++; break;
			case 2:  movedToMatchAU_g3++; break;
			case 3:  movedToMatchAU_g4++; break;
			} 
			break;
	
		case 1: // moved to best place  (turnovered)
			switch (gIndex) { 
			case 0:  movedToBestAU_g1++; break;
			case 1:  movedToBestAU_g2++; break;
			case 2:  movedToBestAU_g3++; break;
			case 3:  movedToBestAU_g4++; break;

			} 
			break;

		case 2: // remained where they were  (turnovered)
			switch (gIndex) { 
			case 0:  remainedInSameAU_g1++; break;
			case 1:  remainedInSameAU_g2++; break;
			case 2:  remainedInSameAU_g3++; break;
			case 3:  remainedInSameAU_g4++; break;
			} 
			break;

		case 3: // new Immigrant settled in matching spot
			switch (gIndex) { 
			case 0:  newImmiSettledMatch_g1++; break;
			case 1:  newImmiSettledMatch_g2++; break;
			case 2:  newImmiSettledMatch_g3++; break;
			case 3:  newImmiSettledMatch_g4++; break;
			} 
			break;
			
		case 4: // new Immigrant settled in best location
			switch (gIndex) { 
			case 0:  newImmiSettledBest_g1++; break;
			case 1:  newImmiSettledBest_g2++; break;
			case 2:  newImmiSettledBest_g3++; break;
			case 3:  newImmiSettledBest_g4++; break;
			} 
			break;
		} 
	}
	
	
	private void updateFoundMatchStat(int gIndex, boolean wasBigger) {

		switch (gIndex) { 
		case 0: 
			if (wasBigger) 
				cum_match_T_really_bigger_g1++;
			else cum_match_T_really_not_bigger_g1++;
			break;
		case 1: 
			if (wasBigger) 
				cum_match_T_really_bigger_g2++;
			else cum_match_T_really_not_bigger_g2++;
			break;
		case 2: 
			if (wasBigger) 
				cum_match_T_really_bigger_g3++;
			else cum_match_T_really_not_bigger_g3++;
			break;

		case 3: 
			if (wasBigger) 
				cum_match_T_really_bigger_g4++;
			else cum_match_T_really_not_bigger_g4++; 
			break;

		} 
	}
	
private static void updateHistoryLastTime(int numOfGroup) {
		
		switch (numOfGroup) { 
		case 2: 
			
			last_match_T_really_bigger_g1 =cum_match_T_really_bigger_g1;
			last_match_T_really_bigger_g2 =cum_match_T_really_bigger_g2;
			
			last_match_T_really_not_bigger_g1 = cum_match_T_really_not_bigger_g1;
			last_match_T_really_not_bigger_g2 = cum_match_T_really_not_bigger_g2;
			
			last_movedToMatchAU_g1 = movedToMatchAU_g1;
			last_movedToMatchAU_g2 = movedToMatchAU_g2;
			
			last_movedToBestAU_g1 = movedToBestAU_g1;
			last_movedToBestAU_g2 = movedToBestAU_g2;
			
			last_remainedInSameAU_g1 = remainedInSameAU_g1;
			last_remainedInSameAU_g2 = remainedInSameAU_g2;
			break;
			
		case 4: 
			last_match_T_really_bigger_g1 =cum_match_T_really_bigger_g1;
			last_match_T_really_bigger_g2 =cum_match_T_really_bigger_g2;
			last_match_T_really_bigger_g3 =cum_match_T_really_bigger_g3;
			last_match_T_really_bigger_g4 =cum_match_T_really_bigger_g4;

			last_match_T_really_not_bigger_g1 = cum_match_T_really_not_bigger_g1;
			last_match_T_really_not_bigger_g2 = cum_match_T_really_not_bigger_g2;
			last_match_T_really_not_bigger_g3 = cum_match_T_really_not_bigger_g3;
			last_match_T_really_not_bigger_g4 = cum_match_T_really_not_bigger_g4;
			
			last_movedToMatchAU_g1 = movedToMatchAU_g1;
			last_movedToMatchAU_g2 = movedToMatchAU_g2;
			last_movedToMatchAU_g3 = movedToMatchAU_g3;
			last_movedToMatchAU_g4 = movedToMatchAU_g4;
			
			last_movedToBestAU_g1 = movedToBestAU_g1;
			last_movedToBestAU_g2 = movedToBestAU_g2;
			last_movedToBestAU_g3 = movedToBestAU_g3;
			last_movedToBestAU_g4 = movedToBestAU_g4;
			
			last_remainedInSameAU_g1 = remainedInSameAU_g1;
			last_remainedInSameAU_g2 = remainedInSameAU_g2;
			last_remainedInSameAU_g3 = remainedInSameAU_g3;
			last_remainedInSameAU_g4 = remainedInSameAU_g4;
			
			last_newImmiSettledMatch_g1 = newImmiSettledMatch_g1;
			last_newImmiSettledMatch_g2 = newImmiSettledMatch_g2;
			last_newImmiSettledMatch_g3 = newImmiSettledMatch_g3;
			last_newImmiSettledMatch_g4 = newImmiSettledMatch_g4;
			
			last_newImmiSettledBest_g1 = newImmiSettledBest_g1;
			last_newImmiSettledBest_g2 = newImmiSettledBest_g2;
			last_newImmiSettledBest_g3 = newImmiSettledBest_g3;
			last_newImmiSettledBest_g4 = newImmiSettledBest_g4;
			
			break;
		}
		
	}
	
	private static void printPlacementStat() {

		System.out.println("# of HH moved to best AU (g1)= "+movedToBestAU_g1);
		System.out.println("# of HH moved to best AU (g2)= "+movedToBestAU_g2);

		double movedToBestAU_all = movedToBestAU_g1+movedToBestAU_g2;
		System.out.println("# of HH moved to best AU (g1+g2)= "+(movedToBestAU_g1+movedToBestAU_g2));


		System.out.println("# of HH remained in same AU (g1)= "+remainedInSameAU_g1);
		System.out.println("# of HH remained in same AU (g2)= "+remainedInSameAU_g2);
		double remainedInSameAU_all = remainedInSameAU_g1+remainedInSameAU_g2;
		System.out.println("# of HH remained in same AU (g1+g2)= "+(remainedInSameAU_g1+remainedInSameAU_g2));

		double ratio = remainedInSameAU_all/movedToBestAU_all;        
		double ratio_g1 = remainedInSameAU_g1/movedToBestAU_g1;
		double ratio_g2 = remainedInSameAU_g2/movedToBestAU_g2;


		System.out.println("ratio of remained/moved (all) = "+ ratio);
		System.out.println("ratio of remained/moved (g1) = "+ ratio_g1);
		System.out.println("ratio of remained/moved (g2) = "+ ratio_g2);

		System.out.println("-----------------------------------");

	}

	private static void printPlacementStat4Groups() {

		System.out.println("# of HH moved to best AU (g1)= "+movedToBestAU_g1);
		System.out.println("# of HH moved to best AU (g2)= "+movedToBestAU_g2);
		System.out.println("# of HH moved to best AU (g3)= "+movedToBestAU_g3);
		System.out.println("# of HH moved to best AU (g4)= "+movedToBestAU_g4);

		double movedToBestAU_all = movedToBestAU_g1+movedToBestAU_g2+movedToBestAU_g3+movedToBestAU_g4;
		System.out.println("------Total moved (g1+g2+g3+g4)= "+movedToBestAU_all);

		System.out.println("# of HH remained in same AU (g1)= "+remainedInSameAU_g1);
		System.out.println("# of HH remained in same AU (g2)= "+remainedInSameAU_g2);
		System.out.println("# of HH remained in same AU (g3)= "+remainedInSameAU_g3);
		System.out.println("# of HH remained in same AU (g4)= "+remainedInSameAU_g4);

		double remainedInSameAU_all = remainedInSameAU_g1+remainedInSameAU_g2+remainedInSameAU_g3+remainedInSameAU_g4;
		System.out.println("---- Total remained (g1+g2+g3+4)= "+remainedInSameAU_all);

		System.out.println("# of new imm HH settled (g1)= "+ newImmiSettledMatch_g1);
		System.out.println("# of new imm HH settled (g2)= "+ newImmiSettledMatch_g2);
		System.out.println("# of new imm HH settled (g3)= "+ newImmiSettledMatch_g3);
		System.out.println("# of new imm HH settled (g4)= "+ newImmiSettledMatch_g4);

		double newImmiSettled_all = newImmiSettledMatch_g1+newImmiSettledMatch_g2+newImmiSettledMatch_g3+newImmiSettledMatch_g4;
		System.out.println("---- Total new immi (g1+g2+g3+4)= "+newImmiSettled_all);

		System.out.println("+++Total (moved +remaind+ new)= "+ (movedToBestAU_all+remainedInSameAU_all+newImmiSettled_all));

		double ratio = remainedInSameAU_all/movedToBestAU_all;
		double ratio_g1 = remainedInSameAU_g1/movedToBestAU_g1;
		double ratio_g2 = remainedInSameAU_g2/movedToBestAU_g2;
		double ratio_g3 = remainedInSameAU_g3/movedToBestAU_g3;
		double ratio_g4 = remainedInSameAU_g4/movedToBestAU_g4;

		System.out.println("ratio of remained/moved= "+ ratio);
		System.out.println("ratio of remained/moved (g1) = "+ ratio_g1);
		System.out.println("ratio of remained/moved (g2) = "+ ratio_g2);
		System.out.println("ratio of remained/moved (g3) = "+ ratio_g3);
		System.out.println("ratio of remained/moved (g4) = "+ ratio_g4);

		System.out.println("-----------------------------------");
	}

	public static void printToFilePlacementStat2Groups(double tickTime) {
		
		WriteHelper.writeText(1,"-----------------------stat (2 groups)-----------------------tick: "+tickTime);

		double curr_placedTmatchingCount_g1=0;
		double curr_placedTmatchingCount_g2=0;
		double curr_movedToBestAU_g1=0;
		double curr_movedToBestAU_g2=0;
		double curr_remainedInSameAU_g1=0;
		double curr_remainedInSameAU_g2=0;

		if (tickTime >1) { //build this time stat
			curr_placedTmatchingCount_g1 = movedToMatchAU_g1 - last_movedToMatchAU_g1;
			curr_placedTmatchingCount_g2 = movedToMatchAU_g2 - last_movedToMatchAU_g2;
			curr_movedToBestAU_g1 = movedToBestAU_g1 - last_movedToBestAU_g1;
			curr_movedToBestAU_g2 = movedToBestAU_g2 - last_movedToBestAU_g2;
			curr_remainedInSameAU_g1=  remainedInSameAU_g1 - last_remainedInSameAU_g1;
			curr_remainedInSameAU_g2=  remainedInSameAU_g2 - last_remainedInSameAU_g2;
		}
		
		
		WriteHelper.writeText(1,"cum # of HH moved to MATCHING AU (g1), "+movedToMatchAU_g1);
		WriteHelper.writeText(1, "cum # of HH moved to MATCHING AU (g2), "+movedToMatchAU_g2);
		double movedToMatchingAU_all = movedToMatchAU_g1+movedToMatchAU_g2;
		WriteHelper.writeText(1,"+++cum Total MATCHING moved (g1+g2), "+movedToMatchingAU_all);
		
		WriteHelper.writeText(1,"curr # of HH moved to MATCHING AU (g1), "+curr_placedTmatchingCount_g1);
		WriteHelper.writeText(1, "curr # of HH moved to MATCHING AU (g2), "+curr_placedTmatchingCount_g2);
		double curr_movedToMatchingAU_all = curr_placedTmatchingCount_g1+curr_placedTmatchingCount_g2;
		WriteHelper.writeText(1,"---curr Total MATCHING moved (g1+g2), "+curr_movedToMatchingAU_all);
		
		//--------
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum # of HH moved to BEST AU (g1), "+movedToBestAU_g1);
		WriteHelper.writeText(1, "cum # of HH moved to BEST AU (g2), "+movedToBestAU_g2);
		double movedToBestAU_all = movedToBestAU_g1+movedToBestAU_g2;
		WriteHelper.writeText(1,"+++cum Total BEST moved (g1+g2), "+movedToBestAU_all);
		
		//WriteHelper.writeText(1,"-");
		WriteHelper.writeText(1,"curr # of HH moved to BEST AU (g1), "+curr_movedToBestAU_g1);
		WriteHelper.writeText(1, "curr # of HH moved to  AU (g2), "+curr_movedToBestAU_g2);
		double curr_movedToBestAU_all = curr_movedToBestAU_g1+curr_movedToBestAU_g2;
		WriteHelper.writeText(1,"---curr Total BEST moved (g1+g2), "+curr_movedToBestAU_all);
		
		//--------

		WriteHelper.writeText(1,"");

		WriteHelper.writeText(1, "cum # of HH REMAINED  in same AU (g1), "+remainedInSameAU_g1);
		WriteHelper.writeText(1,"cum # of HH REMAINED in same AU (g2), "+remainedInSameAU_g2);
		double remainedInSameAU_all = remainedInSameAU_g1+remainedInSameAU_g2;
		WriteHelper.writeText(1,"+++ cum Total REMAINED (g1+g2), "+remainedInSameAU_all);
		
		//WriteHelper.writeText(1,"-");
		WriteHelper.writeText(1, "curr # of HH REMAINED in same AU (g1), "+curr_remainedInSameAU_g1);
		WriteHelper.writeText(1,"curr # of HH REMAINED in same AU (g2), "+curr_remainedInSameAU_g2);
		double curr_remainedInSameAU_all = curr_remainedInSameAU_g1+curr_remainedInSameAU_g2;
		WriteHelper.writeText(1,"--- curr Total REMAINED (g1+g2), "+curr_remainedInSameAU_all);
		
		//-----------------
		WriteHelper.writeText(1,"");
        double all = movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all;
        double all_g1 = movedToMatchAU_g1+movedToBestAU_g1+remainedInSameAU_g1;
        double all_g2 = movedToMatchAU_g2+movedToBestAU_g2+remainedInSameAU_g2;
        WriteHelper.writeText(1,"cum Total (moved(matching+best) +remaind), "+ (movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all));
        WriteHelper.writeText(1,"     cum Total moved g1, "+ all_g1);
        WriteHelper.writeText(1,"     cum Total moved g2,"+ all_g2);

		WriteHelper.writeText(1,"-");
        double curr_all = curr_movedToMatchingAU_all+curr_movedToBestAU_all+curr_remainedInSameAU_all;
        double curr_all_g1 = curr_placedTmatchingCount_g1+curr_movedToBestAU_g1+curr_remainedInSameAU_g1;
        double curr_all_g2 = curr_placedTmatchingCount_g2+curr_movedToBestAU_g2+curr_remainedInSameAU_g2;
        WriteHelper.writeText(1,"curr Total (moved(matching+best) +remaind), "+ (curr_movedToMatchingAU_all+curr_movedToBestAU_all+curr_remainedInSameAU_all));
        WriteHelper.writeText(1,"     curr Total moved g1, "+ curr_all_g1);
        WriteHelper.writeText(1,"     curr Total moved g2,"+ curr_all_g2);
        
        //-----------------------
        
		double ratio_remained_all = remainedInSameAU_all/all;
		double ratio_remained_g1 = remainedInSameAU_g1/all_g1;
		double ratio_remained_g2 = remainedInSameAU_g2/all_g2;
		
		double ratio_match_g1 = movedToMatchAU_g1/all_g1;
		double ratio_match_g2 = movedToMatchAU_g2/all_g2;
		
		double ratio_best_g1 = movedToBestAU_g1/all_g1;
		double ratio_best_g2 = movedToBestAU_g2/all_g2;		
		
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g1), "+ ratio_remained_g1);
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g2), "+ ratio_remained_g2);		
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g1), "+ ratio_match_g1);
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g2), "+ ratio_match_g2);		
		WriteHelper.writeText(1,"cum ratio of BEST/all (g1), "+ ratio_best_g1);
		WriteHelper.writeText(1,"cum ratio of BEST/all (g2), "+ ratio_best_g2);
		
		
		double curr_ratio_remained_all = curr_remainedInSameAU_all/curr_all;
		double curr_ratio_remained_g1 = curr_remainedInSameAU_g1/curr_all_g1;
		double curr_ratio_remained_g2 = curr_remainedInSameAU_g2/curr_all_g2;
		
		double curr_ratio_match_g1 = curr_placedTmatchingCount_g1/curr_all_g1;
		double curr_ratio_match_g2 = curr_placedTmatchingCount_g2/curr_all_g2;
		
		double curr_ratio_best_g1 = curr_movedToBestAU_g1/curr_all_g1;
		double curr_ratio_best_g2 = curr_movedToBestAU_g2/curr_all_g2;
		WriteHelper.writeText(1,"-");
		WriteHelper.writeText(1,"curr ratio of REMAINED/all (g1), "+ curr_ratio_remained_g1);
		WriteHelper.writeText(1,"curr ratio of REMAINED/all (g2), "+ curr_ratio_remained_g2);		
		WriteHelper.writeText(1,"curr ratio of MATCH/all (g1), "+ curr_ratio_match_g1);
		WriteHelper.writeText(1,"curr ratio of MATCH/all (g2), "+ curr_ratio_match_g2);		
		WriteHelper.writeText(1,"curr ratio of BEST/all (g1), "+ curr_ratio_best_g1);
		WriteHelper.writeText(1,"curr ratio of BEST/all (g2), "+ curr_ratio_best_g2);
		
		//--------------------------
	
		printToFileGoodvBadGuessesStat(2, tickTime);

		updateHistoryLastTime(2);
					
	}
	
	
	public static void printToFilePlacementStat4Groups(double tickTime) {
			
		WriteHelper.writeText(1,"-----------------------stat (4 groups)-----------------------tick: "+tickTime);

		double curr_placedTmatchingCount_g1=0;
		double curr_placedTmatchingCount_g2=0;
		double curr_placedTmatchingCount_g3=0;
		double curr_placedTmatchingCount_g4=0;
		
		double curr_movedToBestAU_g1=0;
		double curr_movedToBestAU_g2=0;		
		double curr_movedToBestAU_g3=0;
		double curr_movedToBestAU_g4=0;
		
		double curr_remainedInSameAU_g1=0;
		double curr_remainedInSameAU_g2=0;		
		double curr_remainedInSameAU_g3=0;
		double curr_remainedInSameAU_g4=0;
		
		double curr_imm_moveToMatchAU_g1=0;
		double curr_imm_moveToMatchAU_g2=0;
		double curr_imm_moveToMatchAU_g3=0;
		double curr_imm_moveToMatchAU_g4=0;
		
		double curr_imm_moveToBestAU_g1=0;
		double curr_imm_moveToBestAU_g2=0;
		double curr_imm_moveToBestAU_g3=0;
		double curr_imm_moveToBestAU_g4=0;


		if (tickTime >1) { //build this time stat
			curr_placedTmatchingCount_g1 = movedToMatchAU_g1 - last_movedToMatchAU_g1;
			curr_placedTmatchingCount_g2 = movedToMatchAU_g2 - last_movedToMatchAU_g2;
			curr_placedTmatchingCount_g3 = movedToMatchAU_g3 - last_movedToMatchAU_g3;
			curr_placedTmatchingCount_g4 = movedToMatchAU_g4 - last_movedToMatchAU_g4;
			
			curr_movedToBestAU_g1 = movedToBestAU_g1 - last_movedToBestAU_g1;
			curr_movedToBestAU_g2 = movedToBestAU_g2 - last_movedToBestAU_g2;
			curr_movedToBestAU_g3 = movedToBestAU_g3 - last_movedToBestAU_g3;
			curr_movedToBestAU_g4 = movedToBestAU_g4 - last_movedToBestAU_g4;
			
			curr_remainedInSameAU_g1=  remainedInSameAU_g1 - last_remainedInSameAU_g1;
			curr_remainedInSameAU_g2=  remainedInSameAU_g2 - last_remainedInSameAU_g2;
			curr_remainedInSameAU_g3=  remainedInSameAU_g3 - last_remainedInSameAU_g3;
			curr_remainedInSameAU_g4=  remainedInSameAU_g4 - last_remainedInSameAU_g4;
			
			curr_imm_moveToMatchAU_g1 = newImmiSettledMatch_g1 - last_newImmiSettledMatch_g1;
			curr_imm_moveToMatchAU_g2 = newImmiSettledMatch_g2 - last_newImmiSettledMatch_g2;
			curr_imm_moveToMatchAU_g3 = newImmiSettledMatch_g3 - last_newImmiSettledMatch_g3;
			curr_imm_moveToMatchAU_g4 = newImmiSettledMatch_g4 - last_newImmiSettledMatch_g4;

			curr_imm_moveToBestAU_g1 = newImmiSettledBest_g1 - last_newImmiSettledBest_g1;
			curr_imm_moveToBestAU_g2 = newImmiSettledBest_g2 - last_newImmiSettledBest_g2;
			curr_imm_moveToBestAU_g3 = newImmiSettledBest_g3 - last_newImmiSettledBest_g3;
			curr_imm_moveToBestAU_g4 = newImmiSettledBest_g4 - last_newImmiSettledBest_g4;

		}
		
		
		WriteHelper.writeText(1,"cum # of HH moved to MATCHING AU (g1), "+movedToMatchAU_g1);
		WriteHelper.writeText(1,"cum # of HH moved to MATCHING AU (g2), "+movedToMatchAU_g2);
		WriteHelper.writeText(1,"cum # of HH moved to MATCHING AU (g3), "+movedToMatchAU_g3);
		WriteHelper.writeText(1,"cum # of HH moved to MATCHING AU (g4), "+movedToMatchAU_g4);
		
		double movedToMatchingAU_all = movedToMatchAU_g1+movedToMatchAU_g2+ movedToMatchAU_g3+movedToMatchAU_g4;
		WriteHelper.writeText(1,"+++cum Total MATCHING moved (g1+g2+g3+g4), "+movedToMatchingAU_all);
		
		//WriteHelper.writeText(1,"-");
		WriteHelper.writeText(1,"curr # of HH moved to MATCHING AU (g1), "+curr_placedTmatchingCount_g1);
		WriteHelper.writeText(1,"curr # of HH moved to MATCHING AU (g2), "+curr_placedTmatchingCount_g2);
		WriteHelper.writeText(1,"curr # of HH moved to MATCHING AU (g3), "+curr_placedTmatchingCount_g3);
		WriteHelper.writeText(1,"curr # of HH moved to MATCHING AU (g4), "+curr_placedTmatchingCount_g4);
		
		double curr_movedToMatchingAU_all = curr_placedTmatchingCount_g1+curr_placedTmatchingCount_g2+curr_placedTmatchingCount_g3+curr_placedTmatchingCount_g4;
		WriteHelper.writeText(1,"---curr Total MATCHING moved (g1+g2+g3+g4), "+curr_movedToMatchingAU_all);
		
		//--------
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum # of HH moved to BEST AU (g1), "+movedToBestAU_g1);
		WriteHelper.writeText(1,"cum # of HH moved to BEST AU (g2), "+movedToBestAU_g2);
		WriteHelper.writeText(1,"cum # of HH moved to BEST AU (g3), "+movedToBestAU_g3);
		WriteHelper.writeText(1,"cum # of HH moved to BEST AU (g4), "+movedToBestAU_g4);
		
		double movedToBestAU_all = movedToBestAU_g1+movedToBestAU_g2+movedToBestAU_g3+movedToBestAU_g4;
		WriteHelper.writeText(1,"+++cum Total BEST moved (g1+g2+g3+g4), "+movedToBestAU_all);
		
		//WriteHelper.writeText(1,"-");
		WriteHelper.writeText(1,"curr # of HH moved to BEST AU (g1), "+curr_movedToBestAU_g1);
		WriteHelper.writeText(1,"curr # of HH moved to BEST AU (g2), "+curr_movedToBestAU_g2);
		WriteHelper.writeText(1,"curr # of HH moved to BEST AU (g3), "+curr_movedToBestAU_g3);
		WriteHelper.writeText(1,"curr # of HH moved to BEST AU (g4), "+curr_movedToBestAU_g4);
		double curr_movedToBestAU_all = curr_movedToBestAU_g1+curr_movedToBestAU_g2 + curr_movedToBestAU_g3+curr_movedToBestAU_g4;
		WriteHelper.writeText(1,"---curr Total BEST moved (g1+g2+g3+g4), "+curr_movedToBestAU_all);
		
		//--------

		WriteHelper.writeText(1,"");

		WriteHelper.writeText(1,"cum # of HH REMAINED in same AU (g1), "+remainedInSameAU_g1);
		WriteHelper.writeText(1,"cum # of HH REMAINED in same AU (g2), "+remainedInSameAU_g2);
		WriteHelper.writeText(1,"cum # of HH REMAINED in same AU (g3), "+remainedInSameAU_g3);
		WriteHelper.writeText(1,"cum # of HH REMAINED in same AU (g4), "+remainedInSameAU_g4);
		double remainedInSameAU_all = remainedInSameAU_g1+remainedInSameAU_g2+remainedInSameAU_g3+remainedInSameAU_g4;
		WriteHelper.writeText(1,"+++cum Total REMAINED (g1+g2+g3+g4), "+remainedInSameAU_all);
		
		WriteHelper.writeText(1,"curr # of HH REMAINED in same AU (g1), "+curr_remainedInSameAU_g1);
		WriteHelper.writeText(1,"curr # of HH REMAINED in same AU (g2), "+curr_remainedInSameAU_g2);
		WriteHelper.writeText(1,"curr # of HH REMAINED in same AU (g3), "+curr_remainedInSameAU_g3);
		WriteHelper.writeText(1,"curr # of HH REMAINED in same AU (g4), "+curr_remainedInSameAU_g4);
		
		double curr_remainedInSameAU_all = curr_remainedInSameAU_g1+curr_remainedInSameAU_g2+curr_remainedInSameAU_g3+curr_remainedInSameAU_g4;
		WriteHelper.writeText(1,"---curr Total REMAINED (g1+g2+g3+g4), "+curr_remainedInSameAU_all);
		
		
		//--------------

		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum # of imm HH moved to MATCH AU (g1), "+newImmiSettledMatch_g1);
		WriteHelper.writeText(1,"cum # of imm HH moved to MATCH AU (g2), "+newImmiSettledMatch_g2);
		WriteHelper.writeText(1,"cum # of imm HH moved to MATCH AU (g3), "+newImmiSettledMatch_g3);
		WriteHelper.writeText(1,"cum # of imm HH moved to MATCH AU (g4), "+newImmiSettledMatch_g4);

		double newImmMovedToMatchAU_all = newImmiSettledMatch_g1+newImmiSettledMatch_g2+newImmiSettledMatch_g3+newImmiSettledMatch_g4;
		WriteHelper.writeText(1,"+++cum Total IMM MATCH moved (g1+g2+g3+g4), "+newImmMovedToMatchAU_all);

		WriteHelper.writeText(1,"curr # of imm HH moved to MATCH AU (g1), "+curr_imm_moveToMatchAU_g1);
		WriteHelper.writeText(1,"curr # of imm HH moved to MATCH AU (g2), "+curr_imm_moveToMatchAU_g2);
		WriteHelper.writeText(1,"curr # of imm HH moved to MATCH AU (g3), "+curr_imm_moveToMatchAU_g3);
		WriteHelper.writeText(1,"curr # of imm HH moved to MATCH AU (g4), "+curr_imm_moveToMatchAU_g4);
		double curr_imm_movedToMatchAU_all = curr_imm_moveToMatchAU_g1+curr_imm_moveToMatchAU_g2 + curr_imm_moveToMatchAU_g3+curr_imm_moveToMatchAU_g4;
		WriteHelper.writeText(1,"---curr Total IMM MATCH moved (g1+g2+g3+g4), "+curr_imm_movedToMatchAU_all);

		//--------------

		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum # of imm HH moved to BEST AU (g1), "+newImmiSettledBest_g1);
		WriteHelper.writeText(1,"cum # of imm HH moved to BEST AU (g2), "+newImmiSettledBest_g2);
		WriteHelper.writeText(1,"cum # of imm HH moved to BEST AU (g3), "+newImmiSettledBest_g3);
		WriteHelper.writeText(1,"cum # of imm HH moved to BEST AU (g4), "+newImmiSettledBest_g4);

		double newImmMovedToBestAU_all = newImmiSettledBest_g1+newImmiSettledBest_g2+newImmiSettledBest_g3+newImmiSettledBest_g4;
		WriteHelper.writeText(1,"+++cum Total IMM BEST moved (g1+g2+g3+g4), "+newImmMovedToBestAU_all);

		WriteHelper.writeText(1,"curr # of imm HH moved to BEST AU (g1), "+curr_imm_moveToBestAU_g1);
		WriteHelper.writeText(1,"curr # of imm HH moved to BEST AU (g2), "+curr_imm_moveToBestAU_g2);
		WriteHelper.writeText(1,"curr # of imm HH moved to BEST AU (g3), "+curr_imm_moveToBestAU_g3);
		WriteHelper.writeText(1,"curr # of imm HH moved to BEST AU (g4), "+curr_imm_moveToBestAU_g4);
		double curr_imm_movedToBestAU_all = curr_imm_moveToBestAU_g1+curr_imm_moveToBestAU_g2 + curr_imm_moveToBestAU_g3+curr_imm_moveToBestAU_g4;
		WriteHelper.writeText(1,"---curr Total IMM BEST moved (g1+g2+g3+g4), "+curr_imm_movedToBestAU_all);


		//-----------------
		WriteHelper.writeText(1," +++CUM");
        double all_turnovered = movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all;
        double all_turnovered_g1 = movedToMatchAU_g1+movedToBestAU_g1+remainedInSameAU_g1;
        double all_turnovered_g2 = movedToMatchAU_g2+movedToBestAU_g2+remainedInSameAU_g2;
        double all_turnovered_g3 = movedToMatchAU_g3+movedToBestAU_g3+remainedInSameAU_g3;
        double all_turnovered_g4 = movedToMatchAU_g4+movedToBestAU_g4+remainedInSameAU_g4;
        WriteHelper.writeText(1,"cum Total (moved(matching+best) +remaind), "+ (movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all));
        WriteHelper.writeText(1,"     cum Total moved g1,"+ all_turnovered_g1);
        WriteHelper.writeText(1,"     cum Total moved g2,"+ all_turnovered_g2);
        WriteHelper.writeText(1,"     cum Total moved g3,"+ all_turnovered_g3);
        WriteHelper.writeText(1,"     cum Total moved g4,"+ all_turnovered_g4);
        

		WriteHelper.writeText(1," ---curr");
        double curr_turnover_all = curr_movedToMatchingAU_all+curr_movedToBestAU_all+curr_remainedInSameAU_all;
        double curr_turnover_all_g1 = curr_placedTmatchingCount_g1+curr_movedToBestAU_g1+curr_remainedInSameAU_g1;
        double curr_turnover_all_g2 = curr_placedTmatchingCount_g2+curr_movedToBestAU_g2+curr_remainedInSameAU_g2;
        double curr_turnover_all_g3 = curr_placedTmatchingCount_g3+curr_movedToBestAU_g3+curr_remainedInSameAU_g3;
        double curr_turnover_all_g4 = curr_placedTmatchingCount_g4+curr_movedToBestAU_g4+curr_remainedInSameAU_g4;

        WriteHelper.writeText(1,"curr Total (moved(matching+best) +remaind), "+ (curr_movedToMatchingAU_all+curr_movedToBestAU_all+curr_remainedInSameAU_all));
        WriteHelper.writeText(1,"     curr Total moved g1,"+ curr_turnover_all_g1);
        WriteHelper.writeText(1,"     curr Total moved g2,"+ curr_turnover_all_g2);
        WriteHelper.writeText(1,"     curr Total moved g3,"+ curr_turnover_all_g3);
        WriteHelper.writeText(1,"     curr Total moved g4,"+ curr_turnover_all_g4);
        
        
        WriteHelper.writeText(1," +++CUM");
        double all_imm = newImmMovedToMatchAU_all+newImmMovedToBestAU_all;
        double all_imm_g1 = newImmiSettledMatch_g1+newImmiSettledBest_g1;
        double all_imm_g2 = newImmiSettledMatch_g2+newImmiSettledBest_g2;
        double all_imm_g3 = newImmiSettledMatch_g3+newImmiSettledBest_g3;
        double all_imm_g4 = newImmiSettledMatch_g4+newImmiSettledBest_g4;

        WriteHelper.writeText(1,"cum Total IMM (moved(matching+best) , "+ all_imm);
        WriteHelper.writeText(1,"     cum Total imm moved g1,"+ all_imm_g1);
        WriteHelper.writeText(1,"     cum Total imm moved g2,"+ all_imm_g2);
        WriteHelper.writeText(1,"     cum Total imm moved g3,"+ all_imm_g3);
        WriteHelper.writeText(1,"     cum Total imm moved g4,"+ all_imm_g4);
        

		WriteHelper.writeText(1," ---curr");
        double curr_imm_all = curr_imm_movedToMatchAU_all+curr_imm_movedToBestAU_all;
        double curr_imm_all_g1 = curr_imm_moveToMatchAU_g1+curr_imm_moveToBestAU_g1;
        double curr_imm_all_g2 = curr_imm_moveToMatchAU_g2+curr_imm_moveToBestAU_g2;
        double curr_imm_all_g3 = curr_imm_moveToMatchAU_g3+curr_imm_moveToBestAU_g3;
        double curr_imm_all_g4 = curr_imm_moveToMatchAU_g4+curr_imm_moveToBestAU_g4;

        WriteHelper.writeText(1,"curr Total IMM moved(matching+best), "+  curr_imm_all);
        WriteHelper.writeText(1,"     curr Total imm moved g1,"+ curr_imm_all_g1);
        WriteHelper.writeText(1,"     curr Total imm moved g2,"+ curr_imm_all_g2);
        WriteHelper.writeText(1,"     curr Total imm moved g3,"+ curr_imm_all_g3);
        WriteHelper.writeText(1,"     curr Total imm moved g4,"+ curr_imm_all_g4);
             
        //-----------------------
        
		double ratio_remained_all_g1 = remainedInSameAU_all/all_turnovered;
		double ratio_remained_g1 = remainedInSameAU_g1/all_turnovered_g1;
		double ratio_remained_g2 = remainedInSameAU_g2/all_turnovered_g2;
		double ratio_remained_g3 = remainedInSameAU_g3/all_turnovered_g3;
		double ratio_remained_g4 = remainedInSameAU_g4/all_turnovered_g4;
		
		double ratio_match_g1 = movedToMatchAU_g1/all_turnovered_g1;
		double ratio_match_g2 = movedToMatchAU_g2/all_turnovered_g2;
		double ratio_match_g3 = movedToMatchAU_g3/all_turnovered_g3;
		double ratio_match_g4 = movedToMatchAU_g4/all_turnovered_g4;

		double ratio_best_g1 = movedToBestAU_g1/all_turnovered_g1;
		double ratio_best_g2 = movedToBestAU_g2/all_turnovered_g2;	
		double ratio_best_g3 = movedToBestAU_g3/all_turnovered_g3;
		double ratio_best_g4 = movedToBestAU_g4/all_turnovered_g4;		
	
		double ratio_imm_match_g1 = newImmiSettledMatch_g1/all_imm_g1;
		double ratio_imm_match_g2 = newImmiSettledMatch_g2/all_imm_g2;
		double ratio_imm_match_g3 = newImmiSettledMatch_g3/all_imm_g3;
		double ratio_imm_match_g4 = newImmiSettledMatch_g4/all_imm_g4;
		
		double ratio_imm_best_g1 = newImmiSettledBest_g1/all_imm_g1;
		double ratio_imm_best_g2 = newImmiSettledBest_g2/all_imm_g2;
		double ratio_imm_best_g3 = newImmiSettledBest_g3/all_imm_g3;
		double ratio_imm_best_g4 = newImmiSettledBest_g4/all_imm_g4;
		
		WriteHelper.writeText(1," +++CUM");
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g1), "+ ratio_remained_g1);
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g2), "+ ratio_remained_g2);	
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g3), "+ ratio_remained_g3);
		WriteHelper.writeText(1,"cum ratio of REMAINED/all (g4), "+ ratio_remained_g4);	
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g1), "+ ratio_match_g1);
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g2), "+ ratio_match_g2);	
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g3), "+ ratio_match_g3);
		WriteHelper.writeText(1,"cum ratio of MATCH/all (g4), "+ ratio_match_g4);
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum ratio of BEST/all (g1), "+ ratio_best_g1);
		WriteHelper.writeText(1,"cum ratio of BEST/all (g2), "+ ratio_best_g2);
		WriteHelper.writeText(1,"cum ratio of BEST/all (g3), "+ ratio_best_g3);
		WriteHelper.writeText(1,"cum ratio of BEST/all (g4), "+ ratio_best_g4);
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum ratio of IMM MATCH/all (g1), "+ ratio_imm_match_g1);
		WriteHelper.writeText(1,"cum ratio of IMM MATCH/all (g2), "+ ratio_imm_match_g2);	
		WriteHelper.writeText(1,"cum ratio of IMM MATCH/all (g3), "+ ratio_imm_match_g3);
		WriteHelper.writeText(1,"cum ratio of IMM MATCH/all (g4), "+ ratio_imm_match_g4);
		WriteHelper.writeText(1,"");
		WriteHelper.writeText(1,"cum ratio of IMM BEST/all (g1), "+ ratio_imm_best_g1);
		WriteHelper.writeText(1,"cum ratio of IMM BEST/all (g2), "+ ratio_imm_best_g2);
		WriteHelper.writeText(1,"cum ratio of IMM BEST/all (g3), "+ ratio_imm_best_g3);
		WriteHelper.writeText(1,"cum ratio of IMM BEST/all (g4), "+ ratio_imm_best_g4);
			
		if (tickTime >1) { //build this time stat

			double curr_ratio_remained_all = curr_remainedInSameAU_all/curr_turnover_all;
			double curr_ratio_remained_g1 = curr_remainedInSameAU_g1/curr_turnover_all_g1;
			double curr_ratio_remained_g2 = curr_remainedInSameAU_g2/curr_turnover_all_g2;
			double curr_ratio_remained_g3 = curr_remainedInSameAU_g3/curr_turnover_all_g3;
			double curr_ratio_remained_g4 = curr_remainedInSameAU_g4/curr_turnover_all_g4;

			double curr_ratio_match_g1 = curr_placedTmatchingCount_g1/curr_turnover_all_g1;
			double curr_ratio_match_g2 = curr_placedTmatchingCount_g2/curr_turnover_all_g2;
			double curr_ratio_match_g3 = curr_placedTmatchingCount_g3/curr_turnover_all_g3;
			double curr_ratio_match_g4 = curr_placedTmatchingCount_g4/curr_turnover_all_g4;

			double curr_ratio_best_g1 = curr_movedToBestAU_g1/curr_turnover_all_g1;
			double curr_ratio_best_g2 = curr_movedToBestAU_g2/curr_turnover_all_g2;
			double curr_ratio_best_g3 = curr_movedToBestAU_g3/curr_turnover_all_g3;
			double curr_ratio_best_g4 = curr_movedToBestAU_g4/curr_turnover_all_g4;

			double curr_ratio_imm_match_g1 = curr_imm_moveToMatchAU_g1/curr_imm_all_g1;
			double curr_ratio_imm_match_g2 = curr_imm_moveToBestAU_g2/curr_imm_all_g2;
			double curr_ratio_imm_match_g3 = curr_imm_moveToBestAU_g3/curr_imm_all_g3;
			double curr_ratio_imm_match_g4 = curr_imm_moveToBestAU_g4/curr_imm_all_g4;

			double curr_ratio_imm_best_g1 = curr_imm_moveToBestAU_g1/curr_imm_all_g1;
			double curr_ratio_imm_best_g2 = curr_imm_moveToBestAU_g2/curr_imm_all_g2;
			double curr_ratio_imm_best_g3 = curr_imm_moveToBestAU_g3/curr_imm_all_g3;
			double curr_ratio_imm_best_g4 = curr_imm_moveToBestAU_g4/curr_imm_all_g4;

			WriteHelper.writeText(1," ---curr");
			WriteHelper.writeText(1,"curr ratio of REMAINED/all (g1), "+ curr_ratio_remained_g1);
			WriteHelper.writeText(1,"curr ratio of REMAINED/all (g2), "+ curr_ratio_remained_g2);	
			WriteHelper.writeText(1,"curr ratio of REMAINED/all (g3), "+ curr_ratio_remained_g3);
			WriteHelper.writeText(1,"curr ratio of REMAINED/all (g4), "+ curr_ratio_remained_g4);
			WriteHelper.writeText(1,"");
			WriteHelper.writeText(1,"curr ratio of MATCH/all (g1), "+ curr_ratio_match_g1);
			WriteHelper.writeText(1,"curr ratio of MATCH/all (g2), "+ curr_ratio_match_g2);		
			WriteHelper.writeText(1,"curr ratio of MATCH/all (g3), "+ curr_ratio_match_g3);
			WriteHelper.writeText(1,"curr ratio of MATCH/all (g4), "+ curr_ratio_match_g4);	
			WriteHelper.writeText(1,"");
			WriteHelper.writeText(1,"curr ratio of BEST/all (g1), "+ curr_ratio_best_g1);
			WriteHelper.writeText(1,"curr ratio of BEST/all (g2), "+ curr_ratio_best_g2);
			WriteHelper.writeText(1,"curr ratio of BEST/all (g3), "+ curr_ratio_best_g3);
			WriteHelper.writeText(1,"curr ratio of BEST/all (g4), "+ curr_ratio_best_g4);
			WriteHelper.writeText(1,"");
			WriteHelper.writeText(1,"curr ratio of IMM MATCH/all (g1), "+ curr_ratio_imm_match_g1);
			WriteHelper.writeText(1,"curr ratio of IMM MATCH/all (g2), "+ curr_ratio_imm_match_g2);		
			WriteHelper.writeText(1,"curr ratio of IMM MATCH/all (g3), "+ curr_ratio_imm_match_g3);
			WriteHelper.writeText(1,"curr ratio of IMM MATCH/all (g4), "+ curr_ratio_imm_match_g4);	
			WriteHelper.writeText(1,"");
			WriteHelper.writeText(1,"curr ratio of IMM BEST/all (g1), "+ curr_ratio_imm_best_g1);
			WriteHelper.writeText(1,"curr ratio of IMM BEST/all (g2), "+ curr_ratio_imm_best_g2);
			WriteHelper.writeText(1,"curr ratio of IMM BEST/all (g3), "+ curr_ratio_imm_best_g3);
			WriteHelper.writeText(1,"curr ratio of IMM BEST/all (g4), "+ curr_ratio_imm_best_g4);
		}
		
		printToFileGoodvBadGuessesStat(4, tickTime);
		
		// ---------------History keep info of the last time 
		updateHistoryLastTime(4);
	}
	
	private static void printToFileGoodvBadGuessesStat(int numOfGroups, double tickTime) {
		
		WriteHelper.writeText(1,"");	

        double cum_ratio_badGuessVcorrectGuess_g1 = cum_match_T_really_not_bigger_g1/cum_match_T_really_bigger_g1;
        double cum_ratio_badGuessVcorrectGuess_g2 = cum_match_T_really_not_bigger_g2/cum_match_T_really_bigger_g2;
        
        WriteHelper.writeText(1,"cum correct guess g1, "+ cum_match_T_really_bigger_g1);
		WriteHelper.writeText(1, "cum bad guess    g1, "+ cum_match_T_really_not_bigger_g1);
		WriteHelper.writeText(1,"      cum total good+bad guess g1 , "+ (cum_match_T_really_bigger_g1+cum_match_T_really_not_bigger_g1));
    	WriteHelper.writeText(1, "cum ratio (bad guess) / (correct guess) g1, "+ cum_ratio_badGuessVcorrectGuess_g1);
    	
		
		if (tickTime >1) {

			double curr_ratio_badGuess_to_correctGuess_g1=0;			
			double curr_T_really_not_bigger_g1=0;			
			double curr_T_really_bigger_g1=0;
			
			curr_T_really_not_bigger_g1 = cum_match_T_really_not_bigger_g1 - last_match_T_really_not_bigger_g1;
			curr_ratio_badGuess_to_correctGuess_g1 = curr_T_really_not_bigger_g1/curr_T_really_bigger_g1;
						
			WriteHelper.writeText(1," ---curr");				
			WriteHelper.writeText(1,"curr correct guess g1, "+ curr_T_really_bigger_g1);
			WriteHelper.writeText(1,"curr bad guess     g1, "+ curr_T_really_not_bigger_g1);
			WriteHelper.writeText(1,"      curr total good+bad guess g1 , "+ (curr_T_really_bigger_g1+curr_T_really_not_bigger_g1));
			WriteHelper.writeText(1, "curr ratio (bad guess) / (correct guess) g1, "+ curr_ratio_badGuess_to_correctGuess_g1);
		}
			
    	
        WriteHelper.writeText(1,"cum correct guess g2, "+ cum_match_T_really_bigger_g2);
 		WriteHelper.writeText(1, "cum bad guess    g2, "+ cum_match_T_really_not_bigger_g2);
 		WriteHelper.writeText(1,"      cum total good+bad guess g2 , "+ (cum_match_T_really_bigger_g2+cum_match_T_really_not_bigger_g2));
     	WriteHelper.writeText(1, "cum ratio (bad guess) / (correct guess) g2, "+ cum_ratio_badGuessVcorrectGuess_g2);

		
		if (tickTime >1) {

			double curr_ratio_badGuess_to_correctGuess_g2=0;
			double curr_T_really_not_bigger_g2=0;
			double curr_T_really_bigger_g2=0;
		
			curr_T_really_not_bigger_g2 = cum_match_T_really_not_bigger_g2 - last_match_T_really_not_bigger_g2;
			curr_T_really_bigger_g2 = cum_match_T_really_bigger_g2 - last_match_T_really_bigger_g2;
			curr_ratio_badGuess_to_correctGuess_g2 = curr_T_really_not_bigger_g2/curr_T_really_bigger_g2;
			
			WriteHelper.writeText(1," -----curr");				
			WriteHelper.writeText(1,"curr correct guess g2, "+ curr_T_really_bigger_g2);
			WriteHelper.writeText(1,"curr bad guess     g2, "+ curr_T_really_not_bigger_g2);
			WriteHelper.writeText(1,"      curr total good+bad guess g2 , "+ (curr_T_really_bigger_g2+curr_T_really_not_bigger_g2));
			WriteHelper.writeText(1, "curr ratio (bad guess) / (correct guess) g2, "+ curr_ratio_badGuess_to_correctGuess_g2);
		}
				
		
		if (numOfGroups == 4) { //if there are 4 groups 
			
	        double cum_ratio_badGuessVcorrectGuess_g3 = cum_match_T_really_not_bigger_g3/cum_match_T_really_bigger_g3;
	        double cum_ratio_badGuessVcorrectGuess_g4 = cum_match_T_really_not_bigger_g4/cum_match_T_really_bigger_g4;
			
	        WriteHelper.writeText(1,"");	
	        WriteHelper.writeText(1, "cum correct guess g3, "+ cum_match_T_really_bigger_g3);
			WriteHelper.writeText(1, "cum bad guess     g3, "+ cum_match_T_really_not_bigger_g3);
			WriteHelper.writeText(1,"      cum total good+bad guess g3 , "+ (cum_match_T_really_bigger_g3+cum_match_T_really_not_bigger_g3));
	        WriteHelper.writeText(1, "cum ratio (bad guess) / (correct guess) g3, "+ cum_ratio_badGuessVcorrectGuess_g3);
	        
	        if (tickTime >1) {

		    	double curr_ratio_badGuess_to_correctGuess_g3=0;				
				double curr_T_really_not_bigger_g3=0;				
				double curr_T_really_bigger_g3=0;
				
				curr_T_really_not_bigger_g3 = cum_match_T_really_not_bigger_g3 - last_match_T_really_not_bigger_g3;
				curr_T_really_bigger_g3 = cum_match_T_really_bigger_g3 - last_match_T_really_bigger_g3;
				curr_ratio_badGuess_to_correctGuess_g3 = curr_T_really_not_bigger_g3/curr_T_really_bigger_g3;
			
				WriteHelper.writeText(1," -----curr");	
				WriteHelper.writeText(1, "curr correct guess g3, "+ curr_T_really_bigger_g3);
				WriteHelper.writeText(1, "curr bad guess     g3, "+ curr_T_really_not_bigger_g3);
				WriteHelper.writeText(1,"      curr total good+bad guess g3 , "+ (curr_T_really_bigger_g3+curr_T_really_not_bigger_g3));
				WriteHelper.writeText(1, "curr ratio (bad guess) / (correct guess) g3, "+ curr_ratio_badGuess_to_correctGuess_g3);
	        }

			WriteHelper.writeText(1,"");	
	        WriteHelper.writeText(1, "cum correct guess g4, "+ cum_match_T_really_bigger_g4);
			WriteHelper.writeText(1, "cum bad guess     g4, "+ cum_match_T_really_not_bigger_g4);
			WriteHelper.writeText(1,"      cum total good+bad guess g4 , "+ (cum_match_T_really_bigger_g4+cum_match_T_really_not_bigger_g4));
			WriteHelper.writeText(1, "cum ratio (bad guess) / (correct guess) g4, "+ cum_ratio_badGuessVcorrectGuess_g4);


			if (tickTime >1) {

				double curr_ratio_badGuess_to_correctGuess_g4=0;				
				double curr_T_really_not_bigger_g4=0;				
				double curr_T_really_bigger_g4=0;
				
				curr_T_really_not_bigger_g4 = cum_match_T_really_not_bigger_g4 - last_match_T_really_not_bigger_g4;
				curr_T_really_bigger_g4 = cum_match_T_really_bigger_g4 - last_match_T_really_bigger_g4;
				curr_ratio_badGuess_to_correctGuess_g4 = curr_T_really_not_bigger_g4/curr_T_really_bigger_g4;
			
				WriteHelper.writeText(1," -----curr");	
				WriteHelper.writeText(1, "curr correct guess g4, "+ curr_T_really_bigger_g4);
				WriteHelper.writeText(1, "curr bad guess     g4, "+ curr_T_really_not_bigger_g4);
				WriteHelper.writeText(1,"      curr total good+bad guess g4 , "+ (curr_T_really_bigger_g4+curr_T_really_not_bigger_g4));
				WriteHelper.writeText(1, "curr ratio (bad guess) / (correct guess) g4, "+ curr_ratio_badGuess_to_correctGuess_g4);
			}
			
		}
		
	}
	
  private static void printPlacementStat2Groups_old(double tickTime) {
		
		System.out.println("---------------stat (2 groups)-----------------tick: "+tickTime);

		System.out.println("# of HH moved to matching AU (g1)= "+movedToMatchAU_g1);
		System.out.println("# of HH moved to matching AU (g2)= "+movedToMatchAU_g2);

		double movedToMatchingAU_all = movedToMatchAU_g1+movedToMatchAU_g2;
		System.out.println("------Total matching moved (g1+g2)= "+movedToMatchingAU_all);

		System.out.println("# of HH moved to best AU (g1)= "+movedToBestAU_g1);
		System.out.println("# of HH moved to best AU (g2)= "+movedToBestAU_g2);

		double movedToBestAU_all = movedToBestAU_g1+movedToBestAU_g2;
		System.out.println("------Total best moved (g1+g2)= "+movedToBestAU_all);


		System.out.println("# of HH remained in same AU (g1)= "+remainedInSameAU_g1);
		System.out.println("# of HH remained in same AU (g2)= "+remainedInSameAU_g2);


		double remainedInSameAU_all = remainedInSameAU_g1+remainedInSameAU_g2;
		System.out.println("---- Total remained (g1+g2)= "+remainedInSameAU_all);

        double all = movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all;
        double all_g1 = movedToMatchAU_g1+movedToBestAU_g1+remainedInSameAU_g1;
        double all_g2 = movedToMatchAU_g2+movedToBestAU_g2+remainedInSameAU_g2;
		System.out.println("+++Total (moved(matching+best) +remaind)= "+ (movedToMatchingAU_all+movedToBestAU_all+remainedInSameAU_all));
		System.out.println("+++Total g1 ="+ all_g1);
		System.out.println("+++Total g2 ="+ all_g2);
			
		double ratio_remained_all_g1 = remainedInSameAU_all/all;
		double ratio_remained_g1 = remainedInSameAU_g1/all_g1;
		double ratio_remained_g2 = remainedInSameAU_g2/all_g2;
		
		double ratio_match_g1 = movedToMatchAU_g1/all_g1;
		double ratio_match_g2 = movedToMatchAU_g2/all_g2;
		
		double ratio_best_g1 = movedToBestAU_g1/all_g1;
		double ratio_best_g2 = movedToBestAU_g2/all_g2;
		
		System.out.println("ratio of remained/all (g1)= "+ ratio_remained_g1);
		System.out.println("ratio of remained/all (g2)= "+ ratio_remained_g2);
		
		System.out.println("ratio of match/all (g1)= "+ ratio_match_g1);
		System.out.println("ratio of match/all (g2)= "+ ratio_match_g2);
		
		System.out.println("ratio of best/all (g1)= "+ ratio_best_g1);
		System.out.println("ratio of best/all (g2)= "+ ratio_best_g2);

		System.out.println();	
		System.out.println("smaple IS  > exact = "+ cum_match_T_really_bigger_g2);
		System.out.println("smaple NOT > exact = "+ cum_match_T_really_not_bigger_g2);
		System.out.println(" total = "+ (cum_match_T_really_bigger_g2+cum_match_T_really_not_bigger_g2));

	}
	
	
	private double getRandomEstimateOfCo_EthnicPC_normal(double mean) {
		
		double sd = Const.Realtor_SD_RandomCoEthnicEstimation;

		double res = Random.normal.nextDouble(mean,sd);
		
		if (res < 0)
			res=0;

		return res;
	}


	/**
	 * This method accepts as parameters a sorted list of AUs (based on vacancy, from high [up] to low/zero [down])
	 * and a givenVac number (obtained randomly). It then iterates through the AU list and adds up the vacancy of each
	 * AU until the accumulative vacancy is bigger or equal to givenVac. When the latter is true, then the last AU 
	 * is returned. 
	 * The  modified version of this method makes sure that an AU with no (zero) vacancy is not considered. In other
	 * words, it stops after observing an AU with no vacancy (since the list is sorted, we can be sure the rest of 
	 * the list contains AUs with zero vacancy), and that AU with no vacancy is ignored (i.e. the last/previous AU
	 * before hitting the AU with zero vacancy would be returned). 
	 * 
	 * However, the possibility of returning an AU with zero vacancy still exists. if the passed auList contains a single AU with no vacancy 
	 * or all AUs have no vacancy, then the single AU element (or first encounter among many) is returned. 
	 *  
	 * @param auList
	 * @param givenVac
	 * @return AreaUnit
	 */
	private AreaUnit getAUforVacancy(AbstractList<AreaUnit> auList, int givenVac) {

		if (auList.isEmpty()) {
			System.err.println("Realtor:: getAUforVacancy: auList passed as parameter is empty");
		}
		boolean last=false;
		Iterator<AreaUnit> it = auList.iterator();
		int accVac=0;
		AreaUnit currentAU =null;
		AreaUnit lastAU= null;
		while (it.hasNext()  && !last) {
			currentAU = it.next();
			if(currentAU.getVacantSpace()<=0) {
				last = true;
			}else {
				accVac += currentAU.getVacantSpace();
				lastAU= currentAU;
				if (accVac >= givenVac) {
					last = true;
				}
			}
		}

		/*
		 * if the auList contained one single AU with no vacancy or all the AUs had no vacancy, 
		 * the lastAU will be null which in this case, we will return the first encounter kept by currentAU
		 * instead of returning a null. 
		 */
		if (lastAU != null)  
			return lastAU;
		else return currentAU;
	}
	
	private int getTotalVacancy(AbstractList<AreaUnit> auList) {
		int totalVac=0;
		for (int i=0; i<auList.size(); i++) {
			totalVac+=auList.get(i).getVacantSpace();
		}	
		return totalVac;
	}	
	

	private AreaUnit pick_a_Neighbour(AbstractList<AreaUnit> neighList) {
		int randomVac = Random.uniform.nextIntFromTo(0, getTotalVacancy(neighList));
		return getAUforVacancy(neighList, randomVac);	
	}
	
	
	private boolean genericPlacementLoop_VacSensitive_OnTheFly_Persist(AbstractList<AreaUnit> auList, AreaUnit auLosingHH, Household movingHH, double hhTolerancePref, LOC_PREF locPref) {

		boolean isMoveCompleted = false;
		double auCandidateSampleCoethnicPC=0;
		
		int maxSearchVisitNb = movingHH.getSearchLimit();

		boolean isNewImmigrant= false;

		if (auLosingHH == null) { // new immigrant to the city

			isNewImmigrant = true;
			int randomVac = Random.uniform.nextIntFromTo(0, getTotalVacancy(auList));					
			auLosingHH = getAUforVacancy(auList, randomVac);	 //assign him randomly a location he may settle to start with

		}

		AreaUnit bestAUsoFar=auLosingHH;
		double auCandidateExactCoethnicPC = auLosingHH.getCo_ethnicPercentage(movingHH.groupIndex);
		
		double bestCoethnicPCsoFar =0;
    	bestCoethnicPCsoFar =	getRandomEstimateOfCo_EthnicPC_normal(auCandidateExactCoethnicPC);

		double p_persist = 0;
		
		switch (movingHH.groupIndex) { 
		case Const.G1_EURO: 
			p_persist = model.getP_PersistG1();
			break;
		case Const.G2_ASIAN: 
			p_persist = model.getP_PersistG2();
			break;
		case Const.G3_PACIFIC: 
			p_persist = model.getP_PersistG3();
			break;
		case Const.G4_MAORI: 
			p_persist = model.getP_PersistG4();
			break;
		} 

		AreaUnit candidateAU = auLosingHH;

		AbstractList <AreaUnit> neighList;
		if (locPref == LOC_PREF.GLOBAL) {			
			/*
			 * Even with movingHH willing to move globally, we start locally (within the neighbourhood),
			 * starting with the immediate neighbours of the AU where HH is currently resides (auLossingHH).
			 */

			neighList =  geoSpace.getNeighborsAsArrayList(auLosingHH); 

			/*
			 * We may decide not to add auLosingHH to the list of its neighbours. 
			 * This is because it is (already/currently) in the auList (passed as parameter), so
			 * it can be basically selected again (when p_persist exhausts [see below]). 
			 * However, if we add it, it would be likely to be picked up again, which basically means that
			 * the higher possibly exist that movingHH ends up remaining where it resides. 
			 * 
			 * On the other hand, but having only its neighbours and not adding itself (which has certainly vacant space), 
			 * there might be a situation where its neighbours have no vacancy. In this case, by entering into while loop,
			 * we may never comes out of it (unless, the section which capture no vacancy situation will pick up another 
			 * candiateAU.   
			 */
			neighList.add(auLosingHH);
			//neighList.add(bestAUsoFar);

			Collections.sort(neighList, SortComparatorUtils.AU_VAC_TOPDOWN_DESCENDING_ORDER);	

		}
		/*
		 * The move is local, so the neighbours are already known and passed as parameter (auList).
		 * Currently, the auList includes the AU where movingHH lives (needs to be check from caller method).  
		 */
		else neighList= auList; 

		while ((maxSearchVisitNb > 0) && !isMoveCompleted) {

			candidateAU = this.pick_a_Neighbour(neighList);
		
			if (candidateAU.getVacantSpace()>0) {

				neighborArrList.clear();
				neighborArrList =  geoSpace.getNeighborsAsArrayList(candidateAU);
				auCandidateExactCoethnicPC = candidateAU.getCo_ethnicPercentage(movingHH.groupIndex);
				
				auCandidateSampleCoethnicPC =	getRandomEstimateOfCo_EthnicPC_normal(auCandidateExactCoethnicPC);

				if (auCandidateSampleCoethnicPC > hhTolerancePref) {  //orig @TODO later try >=

					bestAUsoFar = candidateAU;
					isMoveCompleted = true;	
	
					if (bestAUsoFar.getGisAgentIndex() != auLosingHH.getGisAgentIndex()) {
						if (!isNewImmigrant) {
							if (auCandidateExactCoethnicPC> hhTolerancePref) {  // orig @TODO later try >=
								updateFoundMatchStat(movingHH.groupIndex, true);
							}
							else updateFoundMatchStat(movingHH.groupIndex, false);
						}
			
					}
					
				}

				else {
					if (auCandidateSampleCoethnicPC >= bestCoethnicPCsoFar) { // orig @TODO later try >=

						bestCoethnicPCsoFar = auCandidateSampleCoethnicPC;
						bestAUsoFar = candidateAU;
					}
				
					double randomPersist  = Random.uniform.nextDoubleFromTo(0, 1);

					if ((randomPersist > p_persist) && (!isMoveCompleted)){

						if (locPref == LOC_PREF.GLOBAL) {

							int randomVac = Random.uniform.nextIntFromTo(0, getTotalVacancy(auList));					
							candidateAU = getAUforVacancy(auList, randomVac);	
							neighList =  geoSpace.getNeighborsAsArrayList(candidateAU);
							/*
							 * we have to add the found candidateAU to the list of neighList, as this list
							 * will be used again at the beginning of the while loop to pick up a candidate and
							 * we do not want to exclude current pick from being selected. 
							 */
							neighList.add(candidateAU);  

							Collections.sort(neighList, SortComparatorUtils.AU_VAC_TOPDOWN_DESCENDING_ORDER);
						}
					}
				}

				maxSearchVisitNb--;
				//System.out.println("maxSearchVisitNb= "+ maxSearchVisitNb);
			}
			else  { //Candidate AU had had no vacancy, let choose another one
				/*
				 * an AU with no vacancy can be picked up if there is no other option available
				 * This can happen particularly if the previous section above [after if (randomPersist > p_persist)],
				 * a chosen candidate is not add to the neighList (a chosen candidate has certainly vacancy, but 
				 * its neighbour[s] may not). If the candidate AU has no vacancy, its maxSearchVistNb will never change, 
				 * and it will remain indefinitely in the while loop.  
				 * By adding chosen candidate to the neighList, this section of code will be unlikely 
				 * to be enter/used (it can be remove, or kept as precaution measure).  
				 */

				System.out.println("*******Realtor:: [Local mover case] Candidate AU has no vancacy, pick up another one");

						
				if (Const.IS_IMMIGRANT_USE_TA_PREFERENCE) {
					maxSearchVisitNb--;
				}
			}
		}
			

		if ((!isNewImmigrant ) && (bestAUsoFar.getGisAgentIndex() != auLosingHH.getGisAgentIndex())) { //make sure the move has been done


			bestAUsoFar.addOneToAU_Poulation(movingHH.groupIndex);
			auLosingHH.reduceOneFromAU_Poulation(movingHH.groupIndex); 
			if (isMoveCompleted) {
				updatePlacementStat(0, movingHH.groupIndex); //moved to matching place
			}
			else {
				isMoveCompleted = true;		
				updatePlacementStat(1, movingHH.groupIndex); //moved to best place
				
			}

		}
		else {
			if (isNewImmigrant) {  // was new immigrant who just settled 
				if (isMoveCompleted) {
					updatePlacementStat(3, movingHH.groupIndex); // new immigrant settled (match)
				}
				else updatePlacementStat(4, movingHH.groupIndex); // new immigrant settled (best)

				bestAUsoFar.addOneToAU_Poulation(movingHH.groupIndex);
				isMoveCompleted = true;	
				
			}
			else {  // not new immigrant, but a moving agent who did not find a suitable place --> remain where he is
				
				updatePlacementStat(2, movingHH.groupIndex); // remained in the same AU
				isMoveCompleted = true;	
			
			}

		}

		if (!isMoveCompleted) {
			//if (movingHH.hhID == 2213352) {

			System.out.println("£isMoveCompleted= "+isMoveCompleted+ "maxSearchVisitNb= "+ maxSearchVisitNb);
			System.out.println("bestAUsoFar.getGisAgentIndex() != auLosingHH.getGisAgentIndex()= "+ (bestAUsoFar.getGisAgentIndex() != auLosingHH.getGisAgentIndex()));
			movingHH.printInfo();

		}

		return isMoveCompleted; 

	} 



	public boolean genericHHPlacement_VacSensitive_OnTheFly(Household movingHH) {

		boolean moveCompleted = false;
		AreaUnit auLosingHH = null;
		boolean isInitialPlacementConditionPassed = false; 

		if (movingHH.auID == -1) {
			isInitialPlacementConditionPassed = true;
		}
		else {
			auLosingHH = (AreaUnit) movingHH.getReferenceToAU();
			/*
			 * The following condition may cause some agent will not go through the placement.
			 * This has only been observed only once when the turnover for Euro (large pop) was set to extremely high value [0.90]
			 * causing many moving agents (mix with flow-out process) which would leave some AU to be free
			 * from a specific group, while having a moving agent registered there. 
			 */
			if (auLosingHH.getGroupPop(movingHH.groupIndex) > 0) 
				isInitialPlacementConditionPassed = true;
		}

		if (isInitialPlacementConditionPassed) { //@TODO remove this later (not necessary anymore)

			AbstractList<AreaUnit> auList=null;
			LOC_PREF locPref = movingHH.getLocationPref();

			if (movingHH.auID == -1)  {  //immigrant (new comer) which is global mover at the same time		
				if (Const.IS_IMMIGRANT_USE_TA_PREFERENCE) {
					auList = geoSpace.getWorldAreaUnitListCopyForTA(movingHH.getPreferredTA_ID());
				} else 	auList = geoSpace.getWorldAreaUnitListCopy(); //like GLOBAL case (below)
			}
			else if (locPref == LOC_PREF.GLOBAL)  { //Global
				auList = geoSpace.getWorldAreaUnitListCopy(); 
			}
			else {  // LOC_PREF.LOCAL
				auList =  geoSpace.getNeighborsAsArrayList(auLosingHH);				
				auList.add(auLosingHH);	
			}

			Collections.sort(auList, SortComparatorUtils.AU_VAC_TOPDOWN_DESCENDING_ORDER);
			
			double hhTolerancePref = movingHH.getToleranceThresholdPref();

			moveCompleted = genericPlacementLoop_VacSensitive_OnTheFly_Persist(auList, auLosingHH, movingHH, hhTolerancePref, locPref);

			if (!moveCompleted) {
				movingHH.increaseTimeSpentInPool();
			}

		}
		if (!moveCompleted) {
			System.out.println("Realtor:: move not completed by following hh: ");
			geoSpace.printInfoOnHouseholdAndItsNeighbours(movingHH);
		}

		return moveCompleted;		
	} 
	
	public Realtor (GeoSpace gs, HAAMoSModel m) {
		geoSpace = gs;
		model = m;

		candidateAUList = new ObjectArrayList(); 
		candidateAU_coEthnicPClist = new DoubleArrayList(); 
		
		if (Const.IsWritePlacementStatToFile)
			WriteHelper.initialize(1, Const.BasePlacementStatFileName+model.getRunCount());

	}



}
