/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.space;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;

import uchicago.src.collection.BaseMatrix;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;
//import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Displayable;
import uchicago.src.sim.gui.Display2D;
import uchicago.src.sim.gui.DisplayConstants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
//import java.lang.Double;
//import javax.swing.BorderFactory;
//import javax.swing.border.*;

import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Category;

import com.bbn.openmap.dataAccess.shape.EsriPolygon;
import com.bbn.openmap.dataAccess.shape.EsriPolygonList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import anl.repast.gis.GisAgent;
import anl.repast.gis.OpenMapAgent;

import cern.colt.list.FloatArrayList;

import org.apache.commons.collections.MultiHashMap;


import nz.ac.auckland.compass.haamos.agents.Household;
import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.base.Const.LOC_PREF;
import nz.ac.uoa.sgges.babak.HAAMoS.HAAMoSModel;

import java.util.*;

/**
 * This class represents one possible form of spatial unit that corresponds to the SNZ Area Unit and contains census-based population.
 * <p> In this model, the exact locations of households within area units (AU) are not specified (matter of concern).  
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class AreaUnit implements OpenMapAgent, GisAgent{

	
	class History {
		public long moveOut_g1=0;
		public long moveOut_g2=0;
		public long moveIn_g1=0;
		public long moveIn_g2=0;
		
	}

	OMGraphic omGraphic;
	EsriPolygon polygon;
	EsriPolygonList polygonList;
	int gisAgentIndex;
	int[] neighborsArray;//used to find which polygons are surrounding it

	private WeakHashMap map_groupPopInfoAgent;

	double vacantSpace;
	double initialVacantSpace;
	boolean firstInitialization;
	
	ArrayList<History> historyList = new ArrayList<History>();

	int au_id;
	String au_name;
	int ta_id;
	String ta_name;

	int ur_pop_91;
	int ur_pop_96;
	int ur_pop_01;
	int ur_pop_06;

	int e_euro_91;
	int e_asian_91;
	int e_pac_91;
	int e_maori_91;

	int e_euro_96;
	int e_asian_96;
	int e_pac_96;
	int e_maori_96;

	int e_euro_01;
	int e_asian_01;
	int e_pac_01;
	int e_maori_01;

	int e_euro_06;
	int e_asian_06;
	int e_pac_06;
	int e_maori_06;


	private static int IDNumber = 0;
	public String name = "Region";

	
	private static GeoSpace geoSpace = null;
	private static HAAMoSModel model=null;

	
	private static ColorMap colorMapArray[];    
	private int ethnicGroupsPopArray[]; //Array to keep different ethnic group population
	private int ethnicGroupsSESArray[]; //Array to keep different ethnic group SES
	private int ethnicGroupID; //can also be used for group color
	private boolean isNull=true;


	public AreaUnit(){	
		ethnicGroupsPopArray = new int [model.nbOfEthnicGroups];
		firstInitialization=true;
	}
	
	public void updateHistory(String inOrOut, int gIndex){
		if (inOrOut == "in") {
			if (gIndex == 0)
				((History)historyList.get((int)model.getTickCount()-1)).moveIn_g1++;
			else ((History)historyList.get((int)model.getTickCount()-1)).moveIn_g2++;
		}
		else { // out
			if (gIndex == 0)
				((History)historyList.get((int)model.getTickCount()-1)).moveOut_g1++;
			else ((History)historyList.get((int)model.getTickCount()-1)).moveOut_g2++;
			
		}
		
	}


	public int getTA_ID(){
		return ta_id;
	}
	public void setTA_ID(int id){
		ta_id = id;
	}

	public String getTA_NAME(){
		return ta_name;
	}
	public void setTA_NAME(String name){
		ta_name = name;
	}
	
	public int getAU_ID(){
		return au_id;
	}
	public void setAU_ID(int id){
		au_id = id;
	}

	public String getAU_NAME(){
		return au_name;
	}
	public void setAU_NAME(String name){
		au_name = name;
	}




	public int getUR_POP_91(){
		return ur_pop_91;
	}
	public void setUR_POP_91(int pop){
		ur_pop_91 = pop;
	}

	public int getUR_POP_96(){
		return ur_pop_96;
	}
	public void setUR_POP_96(int pop){
		ur_pop_96 = pop;
	}

	public int getUR_POP_01(){
		return ur_pop_01;
	}
	public void setUR_POP_01(int pop){
		ur_pop_01 = pop;
	}

	public int getUR_POP_06(){
		return ur_pop_06;
	}
	public void setUR_POP_06(int pop){
		ur_pop_06 = pop;
	}

	//-----------------------------------------

	public int getE_euro_91() {
		return e_euro_91;
	}

	public void setE_euro_91(int e_euro_91) {
		this.e_euro_91 = e_euro_91;
	}

	public int getE_asian_91() {
		return e_asian_91;
	}

	public void setE_asian_91(int e_asian_91) {
		this.e_asian_91 = e_asian_91;
	}

	public int getE_pac_91() {
		return e_pac_91;
	}

	public void setE_pac_91(int e_pac_91) {
		this.e_pac_91 = e_pac_91;
	}

	public int getE_maori_91() {
		return e_maori_91;
	}

	public void setE_maori_91(int e_maori_91) {
		this.e_maori_91 = e_maori_91;
	}

	public int getE_euro_96() {
		return e_euro_96;
	}

	public void setE_euro_96(int e_euro_96) {
		this.e_euro_96 = e_euro_96;
	}

	public int getE_asian_96() {
		return e_asian_96;
	}

	public void setE_asian_96(int e_asian_96) {
		this.e_asian_96 = e_asian_96;
	}

	public int getE_pac_96() {
		return e_pac_96;
	}

	public void setE_pac_96(int e_pac_96) {
		this.e_pac_96 = e_pac_96;
	}

	public int getE_maori_96() {
		return e_maori_96;
	}

	public void setE_maori_96(int e_maori_96) {
		this.e_maori_96 = e_maori_96;
	}

	public int getE_euro_01() {
		return e_euro_01;
	}

	public void setE_euro_01(int e_euro_01) {
		this.e_euro_01 = e_euro_01;
	}

	public int getE_asian_01() {
		return e_asian_01;
	}

	public void setE_asian_01(int e_asian_01) {
		this.e_asian_01 = e_asian_01;
	}

	public int getE_pac_01() {
		return e_pac_01;
	}

	public void setE_pac_01(int e_pac_01) {
		this.e_pac_01 = e_pac_01;
	}

	public int getE_maori_01() {
		return e_maori_01;
	}

	public void setE_maori_01(int e_maori01) {
		this.e_maori_01 = e_maori01;
	}

	public int getE_euro_06() {
		return e_euro_06;
	}

	public void setE_euro_06(int e_euro_06) {
		this.e_euro_06 = e_euro_06;
	}

	public int getE_asian_06() {
		return e_asian_06;
	}

	public void setE_asian_06(int e_asian_06) {
		this.e_asian_06 = e_asian_06;
	}

	public int getE_pac_06() {
		return e_pac_06;
	}

	public void setE_pac_06(int e_pac_06) {
		this.e_pac_06 = e_pac_06;
	}

	public int getE_maori_06() {
		return e_maori_06;
	}

	public void setE_maori_06(int e_maori_06) {
		this.e_maori_06 = e_maori_06;
	}


	public Paint getFillPaint() {

		int majGroupInd = getMajGroupIndex();

		switch (majGroupInd) { 
		case Const.G_Vacant: 
			return Const.G_Vacancy_Color; // means there was no maj. No population 
		case Const.G1_EURO:
			return Const.G1_EURO_Color; 
		case Const.G2_ASIAN:
			return Const.G2_ASIAN_Color; 
		case Const.G3_PACIFIC:
			return Const.G3_PACIFIC_Color; 
		case Const.G4_MAORI:
			return Const.G4_MAORI_Color; 
		case -2:   // means there are more than one group claiming majority
			return Const.G_NoMajority_Color;  //
		}
		return Color.BLACK;  //if none (default/problem)
	}


	public OMGraphic getOMGraphic() {
		return this.omGraphic;
	}


	public void setOMGraphic(OMGraphic omg) {
		this.omGraphic=omg;
		if (omg instanceof EsriPolygon) {

			this.polygon = (EsriPolygon)omg;	
		}
		if (omg instanceof 	EsriPolygonList) {

			this.polygonList = (EsriPolygonList)omg;	
			
		}

	}

	public int getGisAgentIndex() {
		return gisAgentIndex;
	}

	public void setGisAgentIndex(int index) {
		this.gisAgentIndex = index;
	}

	public String[] gisPropertyList() {
		String sStartYear = model.getStartYearInString();
		
		switch (model.nbOfEthnicGroups) { 
		case 2: 
			String[] props2= {"AU_ID","getAU_ID", "AU_NAME","getAU_NAME", "AU_Pop", "getArealUnitPop",
					"G1_Euro_cur", "getG1_Euro_Pop", "G2_Asian_cur", "getG2_Asian_Pop",
					"Vacancy_cur", "getVacantSpace", 
					"G1_Euro_ini", "getE_euro_"+sStartYear, "G2_Asian_ini", "getE_asian_"+sStartYear,
					"Vacancy_ini", "getInitialVacantSpace"};
			return props2;
		case 3:
			String[] props3= {"AU_ID","getAU_ID", "AU_NAME","getAU_NAME", "AU_Pop", "getArealUnitPop",
					"G1_Euro_cur", "getG1_Euro_Pop", "G2_Asian_cur", "getG2_Asian_Pop",
					"G3_Pac_cur", "getG3_Pac_Pop", "Vacancy_cur", "getVacantSpace", 
					"G1_Euro_ini", "getE_euro_"+sStartYear, "G2_Asian_ini", "getE_asian_"+sStartYear,
					"G3_Pac_ini", "getE_pac_"+sStartYear,"Vacancy_ini", "getInitialVacantSpace"};
			return props3;
		case 4:
			String[] props4= {"AU_ID","getAU_ID", "AU_NAME","getAU_NAME", "AU_Pop", "getArealUnitPop",
					"G1_Euro_cur", "getG1_Euro_Pop", "G2_Asian_cur", "getG2_Asian_Pop",
					"G3_Pac_cur", "getG3_Pac_Pop","G4_Maori_cur", "getG4_Maori_Pop",
					"Vacancy_cur", "getVacantSpace", "G1_Euro_ini", "getE_euro_"+sStartYear, 
					"G2_Asian_ini", "getE_asian_"+sStartYear,"G3_Pac_ini", "getE_pac_"+sStartYear,
					"G4_Maori_ini", "getE_maori_"+sStartYear,"Vacancy_ini", "getInitialVacantSpace"};
			return props4;
		}

		String[] propsDefault= {"AU_ID","getAU_ID", "AU_NAME","getAU_NAME", "AU_Pop", "getArealUnitPop",
				"G1_Euro_Pop", "getG1_Euro_Pop", "G2_Asian_Pop", "getG2_Asian_Pop",
				"Vacancy", "getVacantSpace"};
		return propsDefault;
	}
	

	public void setNeighbors(int[] neighbors) {
		this.neighborsArray = neighbors;

		String neigbourString ="";
		for (int i = 0;i<Array.getLength(neighbors);i++){
			neigbourString = neigbourString + " , "+ neighbors[i];
		}
		if (model.debug)
			System.out.println("GisRegionAgent " + gisAgentIndex + " has " + neigbourString + " as neighbours");
	}


	/* 
	 * @return array of neighbour's ID
	 * @see also anl.repast.gis.GisAgent#getNeighbors()
	 */
	public int[] getNeighbors() {
		return neighborsArray;
	}
	
	/**
	 * @return size of neighbour array (number of neighbours)
	 */
	public int getNeighborsSize() {
		return getNeighbors().length;
	}
		
	public Vector getNeighborsAsVector(){
	  int[] neighbors = this.getNeighbors();	
	  Vector neighborVect = new Vector();
	  String neigbourString ="";
	 for (int i=0; i<neighbors.length;i++) {
		 neighborVect.add(neighbors[i]); 
		 neigbourString = neigbourString + " , "+ neighbors[i];
	 }
	  System.out.println("getNeighborsAsVector() GisRegionAgent " + gisAgentIndex + " has " + neigbourString + " as neighbours");

	 return neighborVect;
	}

	 public OMPoint getMidPointForPopInfoIndicator(){

		 if (polygon != null) {
			 float[] latlonarray = polygon.getLatLonArray();
			 float minLat=99999;
			 float maxLat=-99999;
			 float minLon=99999;
			 float maxLon=-99999;
			 for (int i = 0;i<latlonarray.length;i++){
				 if (i%2==0){
					 if (latlonarray[i]<minLat){
						 minLat=latlonarray[i];
					 }
					 if (latlonarray[i]>maxLat){
						 maxLat=latlonarray[i];
					 }
				 }
				 else {
					 if (latlonarray[i]<minLon){
						 minLon=latlonarray[i];
					 }
					 if (latlonarray[i]>maxLon){
						 maxLon=latlonarray[i];
					 }
				 }
			 }


			 float midLatDegrees;
			 float midLonDegrees;

			 float midLat = (minLat+maxLat)/2;

			 float midLon = (minLon+maxLon)/2;

			 midLatDegrees=(float)Math.toDegrees(midLat);
			 midLonDegrees=(float)Math.toDegrees(midLon);
			 return new OMPoint(midLatDegrees, midLonDegrees); 

		 }
		 if (model.debug)
			 System.out.println("Not Ploygon, but PolygonList: "+this.getGisAgentIndex()+" "+this.getAU_NAME());

		 return null; 
	 }

	 public float getExtendForGroupPop(int gNb){
		 double auPop = getArealUnitPop();
		 double auVac = getVacantSpace();
		 double auPopAndVac= auPop+auVac;
		 double groupPopProp =0;			

		 if (auPopAndVac >0) {
			 if (gNb == Const.G_Vacant)
				 groupPopProp =auVac/auPopAndVac;
			 else{
				 int groupPop = getGroupPop(gNb);
				 groupPopProp = groupPop/auPopAndVac;
			 }
		 }

		 double extend360 = (groupPopProp *360);
		 return (float)extend360;

	 }


	 public void createAndInitializePopSizeIndicatorAgents(){

		 OMPoint midPoint = this.getMidPointForPopInfoIndicator();

		 if (midPoint != null) {
			 float extendVal;
			 float start=0f;

			 map_groupPopInfoAgent = new WeakHashMap(model.nbOfEthnicGroups+1);

			 for (int gNb = 0; gNb < model.nbOfEthnicGroups; gNb++) { 
				 extendVal = getExtendForGroupPop(gNb);
				 GroupPopInfo newGroupInfoPieAgent = new GroupPopInfo(gNb, this,(float)midPoint.getLat(),(float)midPoint.getLon(),Const.DefaultRadiusSizeOfGroupPopIndicator,start, extendVal);
				 geoSpace.worldGisAUgroupPopInfoAgentList.add(newGroupInfoPieAgent);
				 map_groupPopInfoAgent.put(gNb, newGroupInfoPieAgent);
				 start= start+extendVal;					
			 }
			 extendVal = getExtendForGroupPop(Const.G_Vacant);
			 GroupPopInfo newGroupInfoPieAgent = new GroupPopInfo(Const.G_Vacant, this,(float)midPoint.getLat(),(float)midPoint.getLon(),Const.DefaultRadiusSizeOfGroupPopIndicator,start, extendVal);
			 geoSpace.worldGisAUgroupPopInfoAgentList.add(newGroupInfoPieAgent);
			 map_groupPopInfoAgent.put(Const.G_Vacant, newGroupInfoPieAgent);

		 }
	 }

	 public void updateGroupInfoAgents() {
		 if (this.map_groupPopInfoAgent != null){
			 float extendVal;
			 float start=0f;
			 for (int gNb = 0; gNb < model.nbOfEthnicGroups; gNb++) { 
				 extendVal = getExtendForGroupPop(gNb);
				 GroupPopInfo groupInfoPie= (GroupPopInfo)map_groupPopInfoAgent.get(gNb);
				 groupInfoPie.setStart(start);
				 groupInfoPie.setExtend(extendVal);
				 start= start+extendVal;					
			 }

			 extendVal = getExtendForGroupPop(Const.G_Vacant);
			 GroupPopInfo groupInfoPie= (GroupPopInfo)map_groupPopInfoAgent.get(Const.G_Vacant);
			 groupInfoPie.setStart(start);
			 groupInfoPie.setExtend(extendVal);
		 }

		 else {
			 if (model.debug)
				 System.out.println("GisAUAgent::updateGroupInfoAgents: No groupAgent associated with this AU: "+
						 this.getAU_NAME()+", Ind: "+this.getGisAgentIndex()+", ID: "+this.getAU_ID());
		 }

	 }

	 public String printGisIndexID(){
		 return "AU-" + this.gisAgentIndex;
	 }


	 public void printInfo(){
		 
		 System.out.println(", GIS ID: "+ printGisIndexID()+ ", AU_ID: "+this.getAU_ID() + ", AU Name: "+this.getAU_NAME());
		 double totalPop =  getArealUnitPop(); 
		 System.out.println("totalPop: "+ (int) totalPop+", consists of:");
		 for (int i = 0; i < model.nbOfEthnicGroups; i++) {
			 System.out.println("  G["+(i+1)+"]: Pop:"+ ethnicGroupsPopArray[i]+ " (Rel%"+ ethnicGroupsPopArray[i]/totalPop+ "),(Abs%"+ ethnicGroupsPopArray[i]/geoSpace.getWorldPop(Const.TA_All));

		 }
		 System.out.println("  Vacant space: "+ this.getVacantSpace()+ " (Rel%"+ (this.getVacantSpace()/this.getArealUnitPop()+")"));
		 
		ArrayList<AreaUnit> neighbourList =  geoSpace.getNeighborsAsArrayList(this);

		 System.out.println("  Nb of neighbours: "+ neighbourList.size());

		 System.out.println("---end----");
	 } 
	 
	 public void printShortInfo(){
			 
		 System.out.print("AU-GIS-ID= "+ this.gisAgentIndex+ ", AU_ID= "+this.getAU_ID());
		 System.out.print(", AU-Name= "+ this.getAU_NAME());
		 System.out.print(", TA-ID= "+ this.getTA_ID());
		 double totalPop =  getArealUnitPop(); 
		 System.out.print(", totalPop= "+ this.getArealUnitPop());
		 for (int i = 0; i < model.nbOfEthnicGroups; i++) {
			 System.out.print(", G["+(i+1)+"]: Pop:"+ ethnicGroupsPopArray[i]);
		 }
		 System.out.print(", Vac= "+ this.getVacantSpace());
		 		 
		ArrayList<AreaUnit> neighbourList =  geoSpace.getNeighborsAsArrayList(this);

		System.out.println(", Nb of neighbours= "+ neighbourList.size());

	 } 


	 public double getCo_ethnicPercentage(int gId){
		 double res =0;
		 if (getArealUnitPop() >0)
			 res = ethnicGroupsPopArray[gId]/getArealUnitPop();
		 return res;
	 }
	 
	 public double getUnlikePop(int unlikeGgroupId) {

		 double unlikePop =0;
		 for (int i = 0; i < model.nbOfEthnicGroups; i++) {
			 if (i != unlikeGgroupId)
				 unlikePop += getGroupPop (i);
		 }
		 return unlikePop;

	 }
	 
	 public double getLikeDivUnlike(int gId) {
		 double like = ethnicGroupsPopArray[gId];
		 double unlike = getUnlikePop(gId);
		 return like/unlike;
	 }

	 public double getArealUnitPop() {
		 double totPop =0;
		 for (int i = 0; i < model.nbOfEthnicGroups; i++) {
			 totPop += getGroupPop (i);
		 }
		 return totPop;
	 }
	 
	 public  double getArealUnitPopPlusVacancy() {
		 
		return getArealUnitPop() + this.getVacantSpace();
	 }

	 public int getG1_Euro_Pop(){
		 return getGroupPop(Const.G1_EURO);
		 //return this.ethnicGroupsPopArray[Const.G1_EURO];
	 }
	 public int getG2_Asian_Pop(){
		 return getGroupPop(Const.G2_ASIAN);
	 }
	 public int getG3_Pac_Pop(){
		 return getGroupPop(Const.G3_PACIFIC);
	 }
	 public int getG4_Maori_Pop(){
		 return getGroupPop(Const.G4_MAORI);
	 }

	 public int getMajGroupIndex() {
		 int bigestPopSoFar = Integer.MIN_VALUE;
		 boolean isNoMajorityGroups = false;  //there is no single majority group --> No majority group
		 int gInd = -1;
		 for (int i = 0; i < model.nbOfEthnicGroups; i++) {
			 if ( getGroupPop(i) == bigestPopSoFar)
				 isNoMajorityGroups = true;
			 if ( getGroupPop(i) > bigestPopSoFar){
				 bigestPopSoFar = getGroupPop(i);
				 gInd = i;
			 }
		 }
		 if (isNoMajorityGroups)
			 return Const.G_NoMaj;
		 return gInd;
	 }

	 public  void setGroupPop (int groupNb, int pop){
		 ethnicGroupsPopArray [groupNb] = pop;
		 isNull=false;
	 }

	 public int getGroupPop (int groupNb){
		 return ethnicGroupsPopArray [groupNb];
	 }

	 public void setGroupSES (int groupNb, int ses){
		 ethnicGroupsSESArray [groupNb] = ses;
	 }

	 public int getGroupSES (int groupNb){
		 return ethnicGroupsSESArray [groupNb];
	 }
	 
	 public void addOneToAU_Poulation(int groupNb) {
		 setGroupPop(groupNb, getGroupPop(groupNb)+1);
		 setVacantSpace(getVacantSpace()-1);
		 		 
	 }
	 
	 public void reduceOneFromAU_Poulation(int groupNb) {
		 setGroupPop(groupNb, getGroupPop(groupNb)-1);
		 setVacantSpace(getVacantSpace()+1);
	 }

	 public  double  getVacantSpace() {
		 return vacantSpace;
	 }

	 public double getInitialVacantSpace() {
		 return initialVacantSpace;
	 }

	 public  void setVacantSpace(double vac) {
		 vacantSpace=vac;
		 if (firstInitialization){
			 initialVacantSpace = vac;
			 firstInitialization = false;
		 }
	 }

	 public ColorMap getColorMap (int groupNb){
		 return colorMapArray[groupNb];
	 }

	 public static void setColorMap (int groupColorNb, ColorMap cm){

		 colorMapArray[groupColorNb] = cm;
	 }

	 public static void setModel (HAAMoSModel m){
		 model = m;
	 }

	 public static void setGeographicalSpace (GeoSpace gs){
		 geoSpace = gs;
	 }

	 public boolean isNull() {
		 return isNull;
	 }


	 private boolean isMovePossible(int px, int py) {
		 return false;
	 }

	 private double getTuroverForGroupIndex(int gIndex) {
		 
		 double movPopTurnoverP = 0.0;
		 
		 switch (gIndex) { 
		 case 0: 
			 movPopTurnoverP = model.getTurnoverG1();
			 break;
		 case 1: 
			 movPopTurnoverP = model.getTurnoverG2();
			 break;
		 case 2: 
			 movPopTurnoverP = model.getTurnoverG3();
			 break;
		 case 3: 
			 movPopTurnoverP = model.getTurnoverG4();
			 break;
		 } 
		 
		 return  movPopTurnoverP;
	 }
	 
	 private double getTolerenceForGroupIndex(int gIndex) {
		 
		 Double movPopTolerancePercentage = -1.00;

		 switch (gIndex) { 
		 case 0: 
			 movPopTolerancePercentage = model.getTolerancePrefG1();
			 break;
		 case 1: 
			 movPopTolerancePercentage = model.getTolerancePrefG2();
			 break;
		 case 2: 
			 movPopTolerancePercentage = model.getTolerancePrefG3();
			 break;
		 case 3: 
			 movPopTolerancePercentage = model.getTolerancePrefG4();
			 break;
		 } 
		 
		 return movPopTolerancePercentage;
		 
	 }
	 
    private double getLocationPreferenceForGroupIndex(int gIndex) {
    	Double anyLocPrefP = -1.00;
		 switch (gIndex) {
		 case 0: 
			 anyLocPrefP = model.getAnyLocPrefPG1();
			 break;
		 case 1: 
			 anyLocPrefP = model.getAnyLocPrefPG2();
			 break;
		 case 2: 
			 anyLocPrefP = model.getAnyLocPrefPG3();
			 break;
		 case 3: 
			 anyLocPrefP = model.getAnyLocPrefPG4();
			 break;
		 } 
		 
		 return anyLocPrefP;
		 
	 }


    private  void poolMovingPop(boolean isPoolSizeZero) {
    	    	
    	historyList.add(new History());

    	for (int gIndex=0; gIndex<model.nbOfEthnicGroups; gIndex++){
    		int groupInitialPop = ethnicGroupsPopArray[gIndex];
    		double movPopTurnoverP = getTuroverForGroupIndex(gIndex);

    		double movPopTolerancePercentage = getTolerenceForGroupIndex(gIndex);
    	
    		int movPop =0;
    		if (groupInitialPop > 0) {
    			if (movPopTurnoverP != 0) {
    				movPop = Random.binomial.nextInt(groupInitialPop, movPopTurnoverP);		
    				if (movPop > groupInitialPop)
    					movPop = groupInitialPop;
    			}

    			if (isPoolSizeZero){
        			int similarInPool = geoSpace.howmanyHHlikeThis(this.getGisAgentIndex(), gIndex);
        			movPop = movPop - similarInPool;  //deduct those hh already intend to move
        			
        			if (movPop <0)
        				movPop=0;
        		}
    			
    			
    			double anyLocPrefP = getLocationPreferenceForGroupIndex(gIndex);

    			int movPopWithLocPrefAnywhere =0;

    			if ((movPop != 0) && (anyLocPrefP >0)) {
    				if (anyLocPrefP == 1.0)
    					movPopWithLocPrefAnywhere = movPop;
    				else {
    					
    				
    					movPopWithLocPrefAnywhere = Random.binomial.nextInt(movPop, anyLocPrefP); // use Bionomial
    				}
    			}
    			

    			for (int i=0; i<movPop; i++) {
    			
    				Household hh = new Household(gIndex, this);
    				
    				hh.setCreationTime(this.model.getTickCount());
    				hh.setToleranceThresholdPref(movPopTolerancePercentage);    				

    				if (i<movPopWithLocPrefAnywhere) {
    					hh.setLocationPref(LOC_PREF.GLOBAL);
    					int searchLimit = Random.poisson.nextInt(Const.SEARCH_LIMIT_FACTOR);

    					if (searchLimit ==0)
    						searchLimit = 1;
    						    					    					
    					hh.setSearchLimit(searchLimit);
    					
    				}
    				else { //they prefer local/neighbourhood move
    					
    					hh.setLocationPref(LOC_PREF.LOCAL);
    					int searchLimit = geoSpace.getNeighborsAsArrayList(this).size();
    					hh.setSearchLimit(searchLimit);
    				}
    				
    				geoSpace.poolList.add(hh);
    			
    			}
    		}
    	}
    }
	 
	
	 public void step(boolean isPoolSizeZero){		

		 poolMovingPop(isPoolSizeZero);
		 if (model.isWorldViewDisplayed)
			 this.updateGroupInfoAgents();
	 }

	 
	 public void nullifyMe() {

		 if (omGraphic != null) {
			 this.omGraphic.clearAttributes();
			 this.omGraphic=null;
		 }
		 
		 if (polygon != null) {
			 this.polygon.clearAttributes();
			 this.polygon=null;
		 }
		 
		 if (polygonList != null) {
			 this.polygonList.clear();
			 this.polygonList=null;
		 }

		 if (historyList != null) {
			 this.historyList.clear();
			 this.historyList =null;
		 }
		 
		 if (map_groupPopInfoAgent !=null) {
			 map_groupPopInfoAgent.clear();
			 map_groupPopInfoAgent=null;
		 }
		 
		 neighborsArray=null;
	
		 
	 }

}
