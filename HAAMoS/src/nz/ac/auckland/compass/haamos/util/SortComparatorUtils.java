/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.util;

import java.util.Comparator;

import nz.ac.auckland.compass.haamos.agents.Household;
import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.space.AreaUnit;


/**
 * Utility class consisting of comparator implementations of different object/class 
 * which compare two arguments for order (to be used by Collection.sort method). 
 * For an ascending order, for example, the compare method of the Comparator interface should return 
 * a negative integer, zero, or a positive integer as the first argument
 * is less than, equal to, or greater than the second.
 * @author Babak Mahdavi Ardestani
 */

public class SortComparatorUtils {
	
   public static final Comparator<Household>
	HH_ID_TOPDOWN_DESCENDING_ORDER =  new Comparator<Household>() {
		public int compare(Household hh1, Household hh2) {
			int res=0;
			if (hh2.hhID < hh1.hhID)
				res=-1;
			else if (hh2.hhID > hh1.hhID)
				res = 1;
			return res;
		}
	}; 
	
	public static final Comparator<Household>
		HH_ID_BOTTOMUP_ASCENDING_ORDER =  new Comparator<Household>() {
			public int compare(Household hh1, Household hh2) {
				int res=0;
				if (hh2.hhID < hh1.hhID)
					res=1;
				else if (hh2.hhID > hh1.hhID)
					res = -1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_GIS_ID_TOPDOWN_DESCENDING_ORDER =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getGisAgentIndex() < au1.getGisAgentIndex())
					res=-1;
				else if (au2.getGisAgentIndex()  > au1.getGisAgentIndex() )
					res = 1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_GIS_ID_BOTTOMUP_ASCENDING_ORDER =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getGisAgentIndex() < au1.getGisAgentIndex())
					res=1;
				else if (au2.getGisAgentIndex()  > au1.getGisAgentIndex() )
					res = -1;
				return res;
			}
		}; 
		
		
		public static final Comparator<AreaUnit>
		AU_VAC_TOPDOWN_DESCENDING_ORDER =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getVacantSpace() < au1.getVacantSpace())
					res=-1;
				else if (au2.getVacantSpace()  > au1.getVacantSpace() )
					res = 1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_VAC_BOTTOMUP_ASCENDING_ORDER =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getVacantSpace() < au1.getVacantSpace())
					res=1;
				else if (au2.getVacantSpace() > au1.getVacantSpace())
					res = -1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G1_LOW2HIGH_ASCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G1_EURO) < au1.getCo_ethnicPercentage(Const.G1_EURO))
					res= 1;
				else if (au2.getCo_ethnicPercentage(Const.G1_EURO) > au1.getCo_ethnicPercentage(Const.G1_EURO))
					res = -1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G2_LOW2HIGH_ASCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G2_ASIAN) < au1.getCo_ethnicPercentage(Const.G2_ASIAN))
					res= 1;
				else if (au2.getCo_ethnicPercentage(Const.G2_ASIAN) > au1.getCo_ethnicPercentage(Const.G2_ASIAN))
					res = -1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G3_LOW2HIGH_ASCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G3_PACIFIC) < au1.getCo_ethnicPercentage(Const.G3_PACIFIC))
					res= 1;
				else if (au2.getCo_ethnicPercentage(Const.G3_PACIFIC) > au1.getCo_ethnicPercentage(Const.G3_PACIFIC))
					res = -1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G4_LOW2HIGH_ASCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G4_MAORI) < au1.getCo_ethnicPercentage(Const.G4_MAORI))
					res= 1;
				else if (au2.getCo_ethnicPercentage(Const.G4_MAORI) > au1.getCo_ethnicPercentage(Const.G4_MAORI))
					res = -1;
				return res;
			}
		}; 
		
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G1_HIGH2LOW_DESCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G1_EURO) < au1.getCo_ethnicPercentage(Const.G1_EURO))
					res= - 1;
				else if (au2.getCo_ethnicPercentage(Const.G1_EURO) > au1.getCo_ethnicPercentage(Const.G1_EURO))
					res = 1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G2_HIGH2LOW_DESCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G2_ASIAN) < au1.getCo_ethnicPercentage(Const.G2_ASIAN))
					res= - 1;
				else if (au2.getCo_ethnicPercentage(Const.G2_ASIAN) > au1.getCo_ethnicPercentage(Const.G2_ASIAN))
					res = 1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G3_HIGH2LOW_DESCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G3_PACIFIC) < au1.getCo_ethnicPercentage(Const.G3_PACIFIC))
					res= - 1;
				else if (au2.getCo_ethnicPercentage(Const.G3_PACIFIC) > au1.getCo_ethnicPercentage(Const.G3_PACIFIC))
					res = 1;
				return res;
			}
		}; 
		
		public static final Comparator<AreaUnit>
		AU_ETHNIC_PROP_VAL_G4_HIGH2LOW_DESCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getCo_ethnicPercentage(Const.G4_MAORI) < au1.getCo_ethnicPercentage(Const.G4_MAORI))
					res= - 1;
				else if (au2.getCo_ethnicPercentage(Const.G4_MAORI) > au1.getCo_ethnicPercentage(Const.G4_MAORI))
					res = 1;
				return res;
			}
		}; 
		
		//---------
		
		public static final Comparator<AreaUnit>
		AU_TA_ID_LOW2HIGH_ASCENDING =  new Comparator<AreaUnit>() {
			public int compare(AreaUnit au1, AreaUnit au2) {
				int res=0;
				if (au2.getTA_ID() < au1.getTA_ID())
					res= 1;
				else if (au2.getTA_ID() > au1.getTA_ID())
					res = -1;
				return res;
			}
		}; 
		
		
	/*public static final Comparator<BOD>
	SO_SUBMITTED_BO_TOPDOWN_ASCENDING_ORDER =	 new Comparator<BOD>() {
		public int compare(BOD bod1, BOD bod2) {
			int res=0;
			if (bod2.getSubmittedBO() < bod1.getSubmittedBO())
				res=1;
			else if (bod2.getSubmittedBO() > bod1.getSubmittedBO())
				res = -1;
			return res;
		}
	};  */
	

}
