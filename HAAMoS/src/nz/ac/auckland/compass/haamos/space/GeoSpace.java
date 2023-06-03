/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */

package nz.ac.auckland.compass.haamos.space;

/**
 */

import java.text.NumberFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import anl.repast.gis.data.FieldNameAndType;
import anl.repast.gis.data.OpenMapData;

import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DGrid;
//import uchicago.src.sim.space.Object2DHexagonalGrid;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;


import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.engine.SimModelImpl; 
import uchicago.src.sim.gui.Object2DDisplay;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.ObjectArrayList;
import cern.jet.stat.Descriptive;
import cern.colt.list.IntArrayList;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import javax.swing.event.MouseInputAdapter;

import nz.ac.auckland.compass.haamos.agents.Household;
import nz.ac.auckland.compass.haamos.agents.Realtor;
import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.base.Const.LOC_PREF;
import nz.ac.auckland.compass.haamos.io.CSV_FileWriter;
import nz.ac.auckland.compass.haamos.test.TestHelper;
import nz.ac.auckland.compass.haamos.util.SortComparatorUtils;
import nz.ac.auckland.compass.haamos.util.TabularDataWriter;
import nz.ac.uoa.sgges.babak.HAAMoS.HAAMoSModel;


class Position {
	int x;
	int y;
	public Position (int xPos, int yPos){
		x = xPos;
		y = yPos;
	}
}  

class TAPop {

	int nb_of_TA = 5;
	int ta_pop[][] = null;

	public TAPop (){
		ta_pop = new int[nb_of_TA][Const.MaxNbOfGroupsHandeledByModel];
	}

	public void addPop(int taIndex, int groupID, int pop) {
		ta_pop[taIndex][groupID] += pop;
	}

	public int getPop(int taIndex, int groupID) {
		return ta_pop[taIndex][groupID];
	}

	public int getTotalTAPop(int taIndex) {		
		int totalPop =0;

		for (int i=0; i<Const.MaxNbOfGroupsHandeledByModel; i++) {
			totalPop += ta_pop[taIndex][i];
		}

		return totalPop;
	}

	public int getTotalGroupPop(int groupID) {		
		int totalPop =0;

		for (int i=0; i<5; i++) {
			totalPop += ta_pop[i][groupID];
		}

		return totalPop;
	}

	public int getGrandTotalIn5TA() {		
		int grandTotalPop=0;

		for (int i=0; i<5; i++)
			grandTotalPop += getTotalTAPop(i);

		return grandTotalPop;		
	}

	private void setGroupPopToZeroInAllTAs(int groupID) {		
		for (int i=0; i<5; i++) {
			ta_pop[i][groupID]=0;
		}
	}

	private void adjustGroupPop(int groupID) {

		int negPopCount=0;
		int posPopCount=0;
		int negPopTotal=0;
		int posPopTotal=0;

		for (int i=0; i<5; i++) {
			if (ta_pop[i][groupID]<=0) {
				negPopTotal += ta_pop[i][groupID];
				negPopCount++;
			}
			else {

				posPopTotal += ta_pop[i][groupID];
			}
		}

		if (negPopTotal != 0) {
			int absNegPopTotal = Math.abs(negPopTotal);
			int j=0;
			while (absNegPopTotal > 0) {

				if (ta_pop[j%5][groupID]>0) {
					ta_pop[j%5][groupID] = ((ta_pop[j%5][groupID])-1);

					absNegPopTotal--;
				}
				j++;
			}

			for (int i=0; i<5; i++) {
				if (ta_pop[i][groupID]<0) {
					ta_pop[i][groupID] = 0;
				}
			}

		}

	}

	public void adjustPops() {	


		for (int i=0; i<Const.MaxNbOfGroupsHandeledByModel; i++) {			
			int groupTotPop = this.getTotalGroupPop(i);
			if (groupTotPop <=0) {

				setGroupPopToZeroInAllTAs(i);
			}
			else {
				adjustGroupPop(i);

			}
		}
	} 

}  

/**
 * Most spatial related operations, measurements and calculations are done by GeoSpace (as a 'manager' of spatial entity in the model, <em>HAAMoS</em>). 
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class GeoSpace {

	public OpenMapData gisOpenMapData = null;

	public ArrayList<AreaUnit> worldGisArealUnitAgentList = null;  
	public ArrayList<GroupPopInfo> worldGisAUgroupPopInfoAgentList = null;  //comprise all of them - world
	public HAAMoSModel model;

	private double worldPopForEachGroupArray[] = null; //Array

	public ArrayList<Household> poolList =null;

	private int sizeOfRegions;  
	public int totalNbOfTAs =0;

	private Realtor realtor;
	protected TabularDataWriter lmiWriter;
	protected TabularDataWriter lqWriter;

	//protected CSV_FileWriter lqFileWriter; 

	protected boolean lqNamesAndIDsWritten =false;
	protected boolean lmiNamesAndIDsWritten =false;


	public GeoSpace(HAAMoSModel m){

		model = m; 

		worldGisArealUnitAgentList = new ArrayList<AreaUnit>();
		worldGisAUgroupPopInfoAgentList = new ArrayList();

		poolList = new ArrayList<Household>();

		realtor = new Realtor(this, model);

		if (Const.OutputFile_DoMeasurmentForLMI) {
			lmiWriter = new TabularDataWriter("LMI.csv");
		}

		if (Const.OutputFile_DoMeasurmentForLQ) {
			lqWriter = new TabularDataWriter("LQ.csv");			
		}


	}

	//++++++++++++++++++++++++++++++++++

	static int countGlobal=0;

	public void printPoolContent() {

		ArrayList <Household> poolListCopy = new ArrayList<Household>(poolList);		
		Collections.sort(poolListCopy, SortComparatorUtils.HH_ID_BOTTOMUP_ASCENDING_ORDER);

		for (int i=0; i<poolListCopy.size(); i++) {
			Household hh = (Household)poolListCopy.get(i);

			countGlobal++;
			hh.printInfo();

		}

	}

	public void printInfoOnHouseholdAndItsNeighbours(Household hh) {
		hh.printInfo();
		AreaUnit hhAU = (AreaUnit) hh.getReferenceToAU();
		if (hhAU != null) {
			ArrayList<AreaUnit> neighbourList =  getNeighborsAsArrayList(hhAU);
			Iterator it = neighbourList.iterator();
			for (int i =0; i<neighbourList.size(); i++ ) {
				AreaUnit au = (AreaUnit) neighbourList.get(i);
				System.out.println("    auGisID: "+au.getGisAgentIndex()+
						", pop: "+au.getArealUnitPop()+  
						", g1: "+au.getGroupPop(Const.G1_EURO)+
						", g2: "+au.getGroupPop(Const.G2_ASIAN)+
						", (g1+g2): "+(au.getGroupPop(Const.G1_EURO)+au.getGroupPop(Const.G2_ASIAN))+
						", vac: "+ au.getVacantSpace() +
						", (g1+g2+vac): "+(au.getGroupPop(Const.G1_EURO)+au.getGroupPop(Const.G2_ASIAN) + au.getVacantSpace())
						);
			} 
		} 
	}

	public void printAllAUs() {

		System.out.println("world pop: "+ this.getWorldPop(Const.TA_All) + ", world vac: "+this.getWorldVacantSpaceSize(Const.TA_All));

		for (int i=0; i<this.worldGisArealUnitAgentList.size(); i++) {
			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			System.out.println("auGisID: "+au.getGisAgentIndex()+
					//", name: "+au.getAU_NAME()+
					", pop: "+au.getArealUnitPop()+  
					", g1: "+au.getGroupPop(Const.G1_EURO)+
					", g2: "+au.getGroupPop(Const.G2_ASIAN)+
					", (g1+g2): "+(au.getGroupPop(Const.G1_EURO)+au.getGroupPop(Const.G2_ASIAN))+
					", vac: "+ au.getVacantSpace() +
					", (g1+g2+vac): "+(au.getGroupPop(Const.G1_EURO)+au.getGroupPop(Const.G2_ASIAN) + au.getVacantSpace())
					);
		}
		System.out.println("----------------------------");

	}


	public void createAndInitializeArealUnitAgents (){

		gisOpenMapData = OpenMapData.getInstance();	

		try{
			FieldNameAndType[] nameTypes = gisOpenMapData.interrogate(model.shapefileSourceFile);

			for (int i=0; i<nameTypes.length; i++){
				if (model.debug)
					System.out.println("interrogate " + i + " field name: " + nameTypes[i].getFieldName()+ " type: " + nameTypes[i].getFieldType());

			}

			worldGisArealUnitAgentList.addAll(gisOpenMapData.createAgents(AreaUnit.class,model.shapefileSourceFile));

			gisOpenMapData.readNeighborhoodInfo(model.neighbourhoodGALSourceFile, worldGisArealUnitAgentList);

			model.totalNumberOfAUs=worldGisArealUnitAgentList.size();
			int numAgents = worldGisArealUnitAgentList.size();

			IntArrayList taID_List = new IntArrayList();
			for (int i=0; i<numAgents; i++){

				AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

				if (!taID_List.contains(au.getTA_ID())) {
					taID_List.add(au.getTA_ID());
				}

				if (model.debug) 
					System.out.println("GIS Urban Areal agent " + i + "; index: " + au.getGisAgentIndex()+"; AU_ID: "+au.getAU_ID()
							+ "; AU_NAME: "+au.getAU_NAME() + "; Euro_POP_91: "+au.getE_euro_91()
							+ "; Asian_POP_91: "+au.getE_asian_91());

				switch (model.startYear) { 
				case Const.YEAR_1991: 
					for (int gNb=0; gNb<model.nbOfEthnicGroups; gNb++){

						switch (gNb) {
						case Const.G1_EURO:
							au.setGroupPop(gNb, au.getE_euro_91());
							break;
						case Const.G2_ASIAN:
							au.setGroupPop(gNb, au.getE_asian_91());
							break;
						case Const.G3_PACIFIC:
							au.setGroupPop(gNb, au.getE_pac_91());
							break;
						case Const.G4_MAORI:
							au.setGroupPop(gNb, au.getE_maori_91());
							break;
						}
					}
					break;

				case Const.YEAR_1996:
					for (int gNb=0; gNb<model.nbOfEthnicGroups; gNb++){

						switch (gNb) {
						case Const.G1_EURO:
							au.setGroupPop(gNb, au.getE_euro_96());
							break;
						case Const.G2_ASIAN:
							au.setGroupPop(gNb, au.getE_asian_96());
							break;
						case Const.G3_PACIFIC:
							au.setGroupPop(gNb, au.getE_pac_96());
							break;
						case Const.G4_MAORI:
							au.setGroupPop(gNb, au.getE_maori_96());
							break;
						}
					}
					break;

				case Const.YEAR_2001:
					for (int gNb=0; gNb<model.nbOfEthnicGroups; gNb++){

						switch (gNb) {
						case Const.G1_EURO:
							au.setGroupPop(gNb, au.getE_euro_01());
							break;
						case Const.G2_ASIAN:
							au.setGroupPop(gNb, au.getE_asian_01());
							break;
						case Const.G3_PACIFIC:
							au.setGroupPop(gNb, au.getE_pac_01());
							break;
						case Const.G4_MAORI:
							au.setGroupPop(gNb, au.getE_maori_01());
							break;
						}
					}
					break;

				case Const.YEAR_2006:
					for (int gNb=0; gNb<model.nbOfEthnicGroups; gNb++){
						switch (gNb) {
						case Const.G1_EURO:
							au.setGroupPop(gNb, au.getE_euro_06());
							break;
						case Const.G2_ASIAN:
							au.setGroupPop(gNb, au.getE_asian_06());
							break;
						case Const.G3_PACIFIC:
							au.setGroupPop(gNb, au.getE_pac_06());
							break;
						case Const.G4_MAORI:
							au.setGroupPop(gNb, au.getE_maori_06());
							break;
						}
					}
					break;

				}

				totalNbOfTAs = taID_List.size();

				Class[] interfaces = worldGisArealUnitAgentList.get(i).getClass().getInterfaces();
				for (int j=0;j<interfaces.length;j++){
				}
			}
			totalNbOfTAs = taID_List.size();

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createGroupPopInfoAgents (){
		try{

			Iterator it = this.worldGisArealUnitAgentList.iterator();
			while (it.hasNext()){
				AreaUnit gisArealUnitAgent =  (AreaUnit)it.next();

				gisArealUnitAgent.createAndInitializePopSizeIndicatorAgents();
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		if (model.debug)
			System.out.println("GeoSpace: Total Number of Group Pop Info Agents created: " + this.worldGisAUgroupPopInfoAgentList.size()); 

	}
	/**
	 *  This method initialises (populate) the city with settings parameters read from the model 
	 *  instead of shapefile. 
	 *  In reality, the initialisation is done first from the shapefile (as default), but this method
	 *  will override that initialisation with values defined by the user/modeller.
	 *  These (values) are basically the proportions of each ethnic group and maxPop per AU. 
	 *  Depending of the chosen start year, this method will also set the population of each ethnic group
	 *  for the chosen start year by values calculated based on user inputted proportions.
	 *  The vacancy will be then applied as in the case of reading from shapefile, meanning 
	 *  that vacancy will be a proportion of total population of the AU defined by user, on top
	 *  of the maxPop. 
	 */
	public void initializeCityManually() {

		if (Const.IS_VERBOSE)
			System.out.println("GeoSpace:: initializeCityManually()");

		int maxPopPerAU = model.getMaxPopPerAU();
		int popG1=0;
		int popG2=0;
		int popG3=0;
		int popG4=0;

		popG1= (maxPopPerAU * model.proportionG1) /100;
		popG2= (maxPopPerAU * model.proportionG2) /100;

		if (model.nbOfEthnicGroups >2)
			popG3= ((maxPopPerAU+1) * model.proportionG3) /100; 

		if (model.nbOfEthnicGroups > 3)
			popG4= (maxPopPerAU * model.proportionG4) /100;

		for (int i=0; i<worldGisArealUnitAgentList.size(); i++){
			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			au.setGroupPop(Const.G1_EURO, popG1);
			au.setGroupPop(Const.G2_ASIAN, popG2);

			if (model.nbOfEthnicGroups > 2)
				au.setGroupPop(Const.G3_PACIFIC, popG3);
			if (model.nbOfEthnicGroups > 3)
				au.setGroupPop(Const.G4_MAORI, popG4);

			switch (model.startYear) { 
			case Const.YEAR_1991:
				au.setE_euro_91(popG1);
				au.setE_asian_91(popG2);
				if (model.nbOfEthnicGroups > 2)
					au.setE_pac_91(popG3);
				if (model.nbOfEthnicGroups > 3)
					au.setE_maori_91(popG4);	
				break;
			case Const.YEAR_1996:
				au.setE_euro_96(popG1);
				au.setE_asian_96(popG2);
				if (model.nbOfEthnicGroups > 2)
					au.setE_pac_96(popG3);
				if (model.nbOfEthnicGroups > 3)
					au.setE_maori_96(popG4);	
				break;
			case Const.YEAR_2001:
				au.setE_euro_01(popG1);
				au.setE_asian_01(popG2);
				if (model.nbOfEthnicGroups > 2)
					au.setE_pac_01(popG3);
				if (model.nbOfEthnicGroups > 3)
					au.setE_maori_01(popG4);		
				break;
			case Const.YEAR_2006:
				au.setE_euro_06(popG1);
				au.setE_asian_06(popG2);
				if (model.nbOfEthnicGroups > 2)
					au.setE_pac_06(popG3);
				if (model.nbOfEthnicGroups > 3)
					au.setE_maori_06(popG4);	

				break;
			}
		}	
	}

	///++++++++++++++++++++++++++++++++

	public Position getNextPosition(int cX, int cY, int iterationNb){

		System.out.println("getNexPostion: iterationNb: "+iterationNb);  
		int xPos=cX;
		int yPos=cY;
		switch (model.orientation) { 

		case Const.CENTER: 
			switch (iterationNb%4) { 
			case -1: 
				xPos = (sizeOfRegions/2);
				yPos = (sizeOfRegions/2);
				if (sizeOfRegions%2 ==0){
					xPos--;
					yPos--;
				}
				break; 
			case 0:  //as initial 
				xPos = cX-1;
				yPos = cY-1;
				break; 
			case 1: 
				xPos = cX+1;
				yPos = cY;
				break;
			case 2: 
				xPos = cX-1;
				yPos = cY+1;
				break;
			case 3: 
				xPos = cX+1;
				yPos = cY;

				break;
			}
			break;

		case Const.NORTH: 
			switch (iterationNb%2) { 
			case -1: 
				xPos = (sizeOfRegions/2);
				yPos = 0;
				if (sizeOfRegions%2 ==0){
					xPos--;
				}
				break;
			case 0:  //as initial
				xPos = cX-1;
				yPos = cY;
				break; 
			case 1: 
				//System.out.println("case 1");  
				xPos = cX+1;
				yPos = cY;
				break;
			}
			break;

		case Const.SOUTH: 

			switch (iterationNb%2) { 
			case -1: 
				xPos = (sizeOfRegions/2);
				yPos = sizeOfRegions-1;
				if (sizeOfRegions%2 ==0){
					xPos--;
				}
			case 0:  //as initial
				xPos = cX-1;
				yPos = cY;
				break; 
			case 1: 
				xPos = cX+1;
				yPos = cY;
				break;
			}
			break;

		case Const.WEST:
			switch (iterationNb%2) { 
			case -1: 
				xPos = 0;
				yPos = (sizeOfRegions/2);
				if (sizeOfRegions%2 ==0){
					yPos--;
				}
				break;
			case 0:  //as initial
				xPos = cX;
				yPos = cY-1;
				break; 
			case 1: 
				xPos = cX;
				yPos = cY+1;
				break;		
			}
			break;

		case Const.EAST: 
			switch (iterationNb%2) { 
			case -1: 
				xPos = sizeOfRegions-1;
				yPos = (sizeOfRegions/2);
				if (sizeOfRegions%2 ==0){
					xPos--;
				}
				break;
			case 0:  //as initial
				xPos = cX-1;
				yPos = cY;
				break; 
			case 1: 
				xPos = cX+1;
				yPos = cY;
				break;		
			}
			break;

		case Const.CORNER_NE: 
			xPos = sizeOfRegions-1;
			yPos = 0;			
			break;

		case Const.CORNER_NW: 
			xPos = 0;
			yPos = 0;			
			break;

		case Const.CORNER_SW: 
			xPos = 0;
			yPos = sizeOfRegions-1;			
			break;

		case Const.CORNER_SE: 
			xPos = sizeOfRegions-1;
			yPos = sizeOfRegions-1;			
			break;

		default:  //random
			xPos = Random.uniform.nextIntFromTo(0,sizeOfRegions-1);
			yPos = Random.uniform.nextIntFromTo(0,sizeOfRegions-1);;

		}

		return new Position(xPos,yPos);
	}


	private int estimateNbOfNeighborsToFillByVicinityF(int nbOfRegionsWithMin){
		//System.out.println("totalMinPresentInRegions: "+ nbOfRegionsWithMin);
		int neighborsToFill = 0;
		//int bla =0;
		//switch (bla) { 
		switch (model.clusteringP) { 

		case 10: 
			neighborsToFill = nbOfRegionsWithMin; //all
			break;
		case 0: 
			neighborsToFill = 0;  //
			break;
		default:  //casees 1-9
			double p = model.clusteringP/10.0;
			neighborsToFill = Random.binomial.nextInt( nbOfRegionsWithMin, p);
			break;
		}


		return neighborsToFill;

	}


	public  AreaUnit getAU4ID(int auID){
		Iterator it = worldGisArealUnitAgentList.iterator();
		boolean isAUfound = false;
		AreaUnit foundAU = null;
		while (it.hasNext() && (!isAUfound)) {
			AreaUnit anAU = (AreaUnit)it.next();
			if (auID == anAU.getGisAgentIndex()) {
				isAUfound = true;
				foundAU = anAU;
			}	   
		}
		return foundAU;
	}

	/*
	 * This vector version will be useful if thread would be use to 
	 * place households simulataniously (Vector is synchronized).
	 * Otherwise, use ArrayList version
	 */
	public Vector getNeighborsAsVectorOfGisAU(AreaUnit givenAU){
		int[] neighbors = givenAU.getNeighbors();	
		Vector neighborVect = new Vector();
		for (int i=0; i<neighbors.length;i++) {
			AreaUnit aNeighbor= getAU4ID(neighbors[i]);
			if (aNeighbor != null) {
				neighborVect.add(aNeighbor); 
			}
			else    System.err.println("GeoSpace:getNeighborsAsVectorOfGisAU(): AU not found for ID"); 
		}

		return neighborVect;
	} 

	public ArrayList<AreaUnit> getNeighborsAsArrayList(AreaUnit givenAU){
		int[] neighbors = givenAU.getNeighbors();	
		ArrayList<AreaUnit> neighborList = new ArrayList<AreaUnit>(neighbors.length);
		for (int i=0; i<neighbors.length;i++) {
			AreaUnit aNeighbor= getAU4ID(neighbors[i]);
			if (aNeighbor != null) {
				neighborList.add(aNeighbor); 
			}
			else    System.err.println("getNeighborsAsArrayList(): AU not found for ID"); 
		}

		return neighborList;
	}


	private ArrayList calculateFlowOutPopForEachGroup(int totalFlowOutPop) {
		ArrayList flowPopList = new ArrayList();
		int outPop = totalFlowOutPop;
		for (int gIndex=0; gIndex<model.nbOfEthnicGroups; gIndex++){

			Double flowOutEmiP = (Double) model.flowOutEmiP.get(gIndex);

			if (outPop != 0)
				outPop = Random.binomial.nextInt(totalFlowOutPop, flowOutEmiP.doubleValue());

			flowPopList.add(gIndex, new Integer(outPop));

		}
		return flowPopList;
	}

	private ArrayList calculateFlowInPopForEachGroup(int totalFlowInPop) {
		ArrayList flowPopList = new ArrayList();
		int inPop = totalFlowInPop;
		for (int gIndex=0; gIndex<model.nbOfEthnicGroups; gIndex++){

			Double flowInImmP = (Double) model.flowInImmP.get(gIndex);

			if (inPop != 0)
				inPop = Random.binomial.nextInt(totalFlowInPop, flowInImmP.doubleValue());

			flowPopList.add(gIndex, new Integer(inPop));

		}
		return flowPopList;
	}


	private void moveOutPop(int groupIndex, int groupOutPop){

		for (int hhIndex=0; hhIndex<groupOutPop; hhIndex++){
			SimUtilities.shuffle(worldGisArealUnitAgentList);
			Iterator outIt = worldGisArealUnitAgentList.iterator();
			boolean moveOutCompleted = false;
			while (outIt.hasNext() && (!moveOutCompleted)) {
				AreaUnit a = (AreaUnit) outIt.next();
				if (a.getGroupPop(groupIndex) >= 1) {
					a.reduceOneFromAU_Poulation(groupIndex);
					moveOutCompleted = true;					
				}
			}
			if (moveOutCompleted == false) {
				//System.out.println("****** Flow out Emigration impossible for group: "+groupIndex +" in this region.");
			}
		}
	}

	private void moveInPop(int groupIndex, int groupInPop){

		for (int hhIndex=0; hhIndex<groupInPop; hhIndex++){
			SimUtilities.shuffle(worldGisArealUnitAgentList);
			Iterator outIt = worldGisArealUnitAgentList.iterator();
			boolean moveInCompleted = false;
			while (outIt.hasNext() && (!moveInCompleted)) {
				AreaUnit a = (AreaUnit) outIt.next();

				if (a.getVacantSpace() >= 1) {
					a.addOneToAU_Poulation(groupIndex);
					moveInCompleted = true;					
				}

			}
			if (moveInCompleted == false) {
				//System.out.println("****** Flow In Immigration impossible for group: "+groupIndex +" in this region.");
			}
		}
	}

	public void flowIn(double inPop){
		ArrayList flowInPopList = calculateFlowInPopForEachGroup((int)inPop); 
		int inGroupPop;
		for (int inGroupIndex=0; inGroupIndex<model.nbOfEthnicGroups; inGroupIndex++){
			inGroupPop = ((Integer) flowInPopList.get(inGroupIndex)).intValue();
			moveInPop(inGroupIndex, inGroupPop);
		}
	}

	public void flowOut(double outPop){
		ArrayList flowOutPopList = calculateFlowOutPopForEachGroup((int)outPop); 
		int outGroupPop;
		for (int outGroupIndex=0; outGroupIndex<model.nbOfEthnicGroups; outGroupIndex++){
			outGroupPop = ((Integer) flowOutPopList.get(outGroupIndex)).intValue();
			moveOutPop(outGroupIndex, outGroupPop);
		}
	}

	public void poolFlowInPop(double inPop){

		ArrayList flowInPopList = calculateFlowInPopForEachGroup((int)inPop); 
		int inGroupPop;
		for (int inGroupIndex=0; inGroupIndex<model.nbOfEthnicGroups; inGroupIndex++){
			inGroupPop = ((Integer) flowInPopList.get(inGroupIndex)).intValue();
			Double flowInPopTolerancePercentage = -1.00;

			switch (inGroupIndex) { 
			case 0: 
				flowInPopTolerancePercentage = model.getTolerancePrefG1();
				break;
			case 1: 
				flowInPopTolerancePercentage = model.getTolerancePrefG2();
				break;
			case 2: 
				flowInPopTolerancePercentage = model.getTolerancePrefG3();
				break;
			case 3: 
				flowInPopTolerancePercentage = model.getTolerancePrefG4();
				break;
			} 

			for (int i=0; i<inGroupPop; i++){

				Household hh = new Household(inGroupIndex, null);
				hh.setCreationTime(model.getTickCount());
				hh.setToleranceThresholdPref(flowInPopTolerancePercentage);    

				hh.setLocationPref(LOC_PREF.GLOBAL);
				int searchLimit = Random.poisson.nextInt(Const.SEARCH_LIMIT_FACTOR);

				if (searchLimit ==0)
					searchLimit = 1;

				hh.setSearchLimit(searchLimit);
				poolList.add(hh);

			}
		}
	}


	public void placeFromPool() {


		SimUtilities.shuffle(poolList);
		Iterator it = poolList.iterator();

		Household hh;
		boolean isPlaced = false;

		while (it.hasNext()) {
			hh = (Household) it.next();
			isPlaced = realtor.genericHHPlacement_VacSensitive_OnTheFly(hh);

			if (isPlaced) {
				it.remove();

			}
		}

		if (Const.IsWritePlacementStatToFile) {
			if (model.nbOfEthnicGroups == 4)
				realtor.printToFilePlacementStat4Groups(model.getTickCount());  //+++++Print STAT 4 groups
			else realtor.printToFilePlacementStat2Groups(model.getTickCount());  //+++++Print STAT 2 groups

		}

		if ((poolList.size() != 0) && (Const.PrintPoolListSizeAtEachIteration)) {
			System.out.println("tick= "+ this.model.getTickCount()+ ", poolList at the end this iteration is not empty. Size: "+poolList.size());
			printPoolContent();
		}

	}

	private void calculateWorldPopForEachGroup(int taID){
		worldPopForEachGroupArray = new double [model.nbOfEthnicGroups]; 
		for (int i=0; i<model.nbOfEthnicGroups; i++){
			worldPopForEachGroupArray[i]=0.0;
		}
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				for (int i=0; i<model.nbOfEthnicGroups; i++){
					worldPopForEachGroupArray[i] = worldPopForEachGroupArray[i]+a.getGroupPop(i);
				}
			}
		}
	}

	private double[] calculateWorldPopForEachGroup_Safe(int taID){
		double [] worldPop4EachGroupArray = new double [model.nbOfEthnicGroups]; 
		for (int i=0; i<model.nbOfEthnicGroups; i++){
			worldPop4EachGroupArray[i]=0.0;
		}
		Iterator it = worldGisArealUnitAgentList.iterator();

		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				for (int i=0; i<model.nbOfEthnicGroups; i++){
					worldPop4EachGroupArray[i] = worldPop4EachGroupArray[i]+a.getGroupPop(i);
				}
			}
		}
		return worldPop4EachGroupArray;
	}

	public double getPoolPopSize4Group(int groupID){
		Iterator it = poolList.iterator();
		Household h;
		int totalOfThisGroup=0;
		while (it.hasNext()) {
			h = (Household) it.next();
			if (h.groupIndex == groupID)
				totalOfThisGroup++;
		}

		return totalOfThisGroup;

	} 
	public double getPopSiseForGroup(int taID, int groupID){
		double[] popSizeArr= calculateWorldPopForEachGroup_Safe(taID);
		return  popSizeArr[groupID];

	} 

	public double getWorldPopSize(){
		double worldPop =0;
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			worldPop = worldPop + a.getArealUnitPop();
		}
		return worldPop;

	} 

	public ArrayList<AreaUnit> getWorldAreaUnitListCopy() {

		return new ArrayList<AreaUnit>(worldGisArealUnitAgentList);

	} 

	public ArrayList<AreaUnit> getWorldAreaUnitListCopyForTA(int taID) {

		ArrayList<AreaUnit> auListForTA = new ArrayList<AreaUnit>(worldGisArealUnitAgentList.size());

		for (AreaUnit au : worldGisArealUnitAgentList) {
			if (au.getTA_ID() == taID)
				auListForTA.add(au);
		} 

		return auListForTA;

	} 


	public double getWorldVacantSpaceSize(int taID){
		double worldVacantSpaceSize =0;
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All))
				worldVacantSpaceSize = worldVacantSpaceSize + a.getVacantSpace();
		}
		return worldVacantSpaceSize;

	} 

	public double getWorldPop(int taID){
		double worldPop =0;
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All))
				worldPop = worldPop + a.getArealUnitPop();
		}
		return worldPop;

	}

	public double getVacVsPopSizeProp(int taID){

		double vacSpaces = getWorldVacantSpaceSize(taID);
		return vacSpaces/(getWorldPop(taID)-vacSpaces);	
	}


	public double getDindex(int taID, int gA, int gB){
		calculateWorldPopForEachGroup(taID);
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		double g1PopDivByTotalG1Pop = 0;  //maj gA
		double g2PopDivByTotalG2Pop = 0;  //min gB
		double dSum = 0.0;

		while (it.hasNext()) {

			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				if (worldPopForEachGroupArray[gA]==0){
					g1PopDivByTotalG1Pop =0;
				}
				else g1PopDivByTotalG1Pop = a.getGroupPop(gA)/worldPopForEachGroupArray[gA];

				if (worldPopForEachGroupArray[gB]==0){
					g2PopDivByTotalG2Pop = 0;
				}
				else 	g2PopDivByTotalG2Pop = a.getGroupPop(gB)/worldPopForEachGroupArray[gB];

				dSum = dSum + Math.abs( g2PopDivByTotalG2Pop - g1PopDivByTotalG1Pop); 

			}

		}

		return dSum/2;
	}

	private double moranI_x_bar(int taID, int gIndex) { //gIndex should be the index of variable of interest

		double sum_of_x=0;
		double m=0;
		Iterator it = this.worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				m++;
				if (a.getArealUnitPop()>0)
					if (Const.IS_MoranI_USE_PROPORTION)
						sum_of_x = sum_of_x +(a.getGroupPop(gIndex)/a.getArealUnitPop()); //based on proportion of pop size
					else sum_of_x = sum_of_x +a.getGroupPop(gIndex); //based on pop size
			}

		}

		double x_bar = sum_of_x/m;

		return x_bar;		

	}

	private double moranI_w(AreaUnit rXi, AreaUnit rXj) {

		if (rXi.getGisAgentIndex() == rXj.getGisAgentIndex()) {
			return 0;
		}

		ArrayList<AreaUnit> neighborsOfI = this.getNeighborsAsArrayList(rXi);

		if (neighborsOfI.contains(rXj))
			return 1;
		else return 0;

	}

	private double moranI_sum_of_sum(int taID, int gIndex) { //Sum Sum Wij(Xi-Xbar)(Xj-Xbar)

		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index
		double sum_of_sum = 0;

		for (int ix = 0; ix <worldGisArealUnitAgentList.size() ; ix++) {
			AreaUnit rXi = (AreaUnit)worldGisArealUnitAgentList.get(ix);
			double xi=0;
			if ((rXi.ta_id == taID)|| (taID==Const.TA_All)){

				if (rXi.getArealUnitPop()>0) {
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = rXi.getGroupPop(gIndex)/rXi.getArealUnitPop(); //based on proportion
					else xi = rXi.getGroupPop(gIndex); //based on pop size
				}

				for (int jx = 0; jx < worldGisArealUnitAgentList.size(); jx++) {
					AreaUnit rXj = (AreaUnit) worldGisArealUnitAgentList.get(jx);

					if ((rXj.ta_id == taID)|| (taID==Const.TA_All)){
						if (rXj.getArealUnitPop() >0) {
							double xj=0;
							if (Const.IS_MoranI_USE_PROPORTION)
								xj = rXj.getGroupPop(gIndex)/rXj.getArealUnitPop(); // based on proportion
							else  xj = rXj.getGroupPop(gIndex); //based on pop size

							double w = moranI_w(rXi,rXj);
							double one_iteration_sum = w *(xi - x_bar)*(xj - x_bar);
							sum_of_sum = sum_of_sum + one_iteration_sum;
						}	
					}
				}
			}
		}

		return sum_of_sum;
	}
	private double calculateNbOfTAforID(int taID){
		Iterator it = this.worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		int nbOfTA =0;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if (a.ta_id == taID)
				nbOfTA++;
		}
		return nbOfTA;

	}

	private double moranI_b2(int taID, int gIndex){
		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index
		double m =0; //number of AUs
		if (taID==Const.TA_All)
			m = worldGisArealUnitAgentList.size();
		else m= calculateNbOfTAforID(taID);

		double b2Result=0;
		Iterator it = this.worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				if (a.getArealUnitPop()>0) {
					double xi=0;
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = a.getGroupPop(gIndex)/a.getArealUnitPop(); //based on proportion size of pop
					else xi = a.getGroupPop(gIndex); //based on group pop size

					b2Result = b2Result + (Math.pow((xi - x_bar), 2.00)/m);
				}
			}
		}

		return b2Result;

	}


	private double moranI_S0(int taID, int gIndex) {

		double sum_of_w = 0;

		for (int ix = 0; ix < this.worldGisArealUnitAgentList.size(); ix++) {
			AreaUnit rXi = (AreaUnit) worldGisArealUnitAgentList.get(ix);	
			if ((rXi.ta_id == taID)|| (taID==Const.TA_All)){

				for (int jx = 0; jx < this.worldGisArealUnitAgentList.size(); jx++) {
					AreaUnit rXj = (AreaUnit) this.worldGisArealUnitAgentList.get(jx);

					if ((rXj.ta_id == taID)|| (taID==Const.TA_All)){
						double w = moranI_w(rXi, rXj);
						sum_of_w = sum_of_w + w;
					}
				}
			}
		}
		return sum_of_w;
	} 

	public double getGlobalMoranI(int taID, int gIndex) {  //based on Zhang & Lin (2007) paper

		double a = 1 / (moranI_S0(taID, gIndex) * moranI_b2(taID, gIndex));
		double b = moranI_sum_of_sum(taID, gIndex);

		return a*b;
	} 


	public double calculateLMIandReturnGMI(int taID, int gIndex) {  //based on Brown & Chung (2006) paper

		double b2 = moranI_b2(taID, gIndex);
		double S0 = moranI_S0(taID, gIndex);
		double a = 1/ (S0*b2);
		double sumOfLi=0;
		double Gi = 0; 
		double N =0; //number of AUs
		if (taID==Const.TA_All)
			N = worldGisArealUnitAgentList.size();
		else N= calculateNbOfTAforID(taID);

		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index

		for (int ix = 0; ix <worldGisArealUnitAgentList.size() ; ix++) {
			AreaUnit rXi = (AreaUnit)worldGisArealUnitAgentList.get(ix);
			double xi=0;
			if ((rXi.ta_id == taID)|| (taID==Const.TA_All)){

				if (rXi.getArealUnitPop()>0) {
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = rXi.getGroupPop(gIndex)/rXi.getArealUnitPop(); //based on proportion
					else xi = rXi.getGroupPop(gIndex); //based on pop size
				}
				double Li = 0;
				for (int jx = 0; jx < worldGisArealUnitAgentList.size(); jx++) {
					AreaUnit rXj = (AreaUnit) worldGisArealUnitAgentList.get(jx);

					if ((rXj.ta_id == taID)|| (taID==Const.TA_All)){
						if (rXj.getArealUnitPop() >0) {
							double xj=0;
							if (Const.IS_MoranI_USE_PROPORTION)
								xj = rXj.getGroupPop(gIndex)/rXj.getArealUnitPop(); // based on proportion
							else  xj = rXj.getGroupPop(gIndex); //based on pop size

							double w = moranI_w(rXi,rXj);
							double one_iteration_sum = w *(xj - x_bar);

							Li = Li + one_iteration_sum;

						}	
					}
				}
				Li= ((xi - x_bar)/(b2*S0))* Li;
				Li = Li *N;
				sumOfLi= sumOfLi + Li;

			}
		}
		// Gi = sumOfLi/S0;
		Gi = sumOfLi/N;
		return Gi;

	} 



	private double moranI_m2(int taID, int gIndex){
		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index
		double m =0; //number of AUs
		if (taID==Const.TA_All)
			m = worldGisArealUnitAgentList.size();
		else m= calculateNbOfTAforID(taID);

		double ZiExp2=0;
		double m2Result=0;
		Iterator it = this.worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				if (a.getArealUnitPop()>0) {
					double xi=0;
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = a.getGroupPop(gIndex)/a.getArealUnitPop(); //based on proportion size of pop
					else xi = a.getGroupPop(gIndex); //based on group pop size

					ZiExp2 = ZiExp2 + (Math.pow((xi - x_bar), 2.00));
				}
			}
		}
		m2Result = ZiExp2/m;

		return m2Result;

	}


	public double getGMIandCalculateLMI(int taID, int gIndex) { //Sum Sum Wij(Xi-Xbar)(Xj-Xbar); Anselin

		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index

		double m2= moranI_m2(taID, gIndex);
		double Zi = 0;
		double lMI =0; //local MI
		double gMI = 0; //global MI

		double N =0; //number of AUs
		if (taID==Const.TA_All)
			N = worldGisArealUnitAgentList.size();
		else N= calculateNbOfTAforID(taID);

		ArrayList v = new ArrayList();

		if ((!lmiNamesAndIDsWritten) && (taID==Const.TA_All) && (Const.OutputFile_DoMeasurmentForLMI)) {	 
			Iterator firstIter = worldGisArealUnitAgentList.iterator();
			ArrayList vAUid = new ArrayList();
			vAUid.add("AU_ID");
			ArrayList vAUname= new ArrayList();
			vAUname.add("AU_Name");
			ArrayList vTAname = new ArrayList();
			vTAname.add("TA_Name");
			for (int i = 0; i <worldGisArealUnitAgentList.size() ; i++) {
				AreaUnit ra = (AreaUnit)worldGisArealUnitAgentList.get(i);
				ra = (AreaUnit) firstIter.next();
				if ((ra.ta_id == taID)|| (taID==Const.TA_All)){

					vAUid.add(""+ra.au_id);
					vAUname.add(ra.au_name);
					vTAname.add(ra.ta_name);

				}
			}
			lmiWriter.addData(vAUid);
			lmiWriter.addData(vAUname);
			lmiWriter.addData(vTAname);
			lmiNamesAndIDsWritten=true;
		}

		NumberFormat nf = null;
		if (Const.OutputFile_DoMeasurmentForLMI){

			nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);

			if (taID==Const.TA_All) {		

				v.add("g"+(gIndex+1)+"_t"+(int)model.getTickCount());

			}
		}

		for (int ix = 0; ix <worldGisArealUnitAgentList.size() ; ix++) {
			AreaUnit rXi = (AreaUnit)worldGisArealUnitAgentList.get(ix);
			double xi=0;
			Zi =0;
			lMI =0;
			if ((rXi.ta_id == taID)|| (taID==Const.TA_All)){

				if (rXi.getArealUnitPop()>0) {
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = rXi.getGroupPop(gIndex)/rXi.getArealUnitPop(); //based on proportion
					else xi = rXi.getGroupPop(gIndex); //based on pop size

					Zi = (xi - x_bar);
					/*
					 * WARNING:
					 * when using proportion with Fossett, at t=0, the x_bar calculation would have 
					 * floating-point error so that Zi = xi- x_bar will not be zero but a small amount.
					 * The accumulations of these small Zi make it the way that GMI at the end is not
					 * zero but 1.
					 */

				}
				double SumWjiByZj = 0;

				Iterator it = this.worldGisArealUnitAgentList.iterator();
				AreaUnit rXj;
				while (it.hasNext()) {
					rXj = (AreaUnit) it.next();

					if ((rXj.ta_id == taID)|| (taID==Const.TA_All)){
						if (rXj.getArealUnitPop() >0) {
							double xj=0;
							if (Const.IS_MoranI_USE_PROPORTION)
								xj = rXj.getGroupPop(gIndex)/rXj.getArealUnitPop(); // based on proportion
							else  xj = rXj.getGroupPop(gIndex); //based on pop size

							double w = moranI_w(rXi,rXj);
							double one_iteration_sum = w *(xj - x_bar); // Zj =(xj - x_bar)
							SumWjiByZj = SumWjiByZj + one_iteration_sum;
						}	
					}
				}
				if (m2 !=0)
					lMI   = Zi*SumWjiByZj/m2;
				gMI = gMI + lMI;


				if ((Const.OutputFile_DoMeasurmentForLMI) && (taID==Const.TA_All)){

					nf.format(lMI);
					v.add(lMI);
				}

			}
		}

		double S0 = moranI_S0(taID, gIndex);
		if (taID==Const.TA_All) {	
			if (Const.OutputFile_DoMeasurmentForLMI){

				lmiWriter.addData(v);

			}
		}
		return gMI/S0;
	}

	public double getGMIandCalcualteLMI_old(int taID, int gIndex) { //Sum Sum Wij(Xi-Xbar)(Xj-Xbar); Arseline

		double x_bar = moranI_x_bar(taID, gIndex);  // check minority group index

		double m2= moranI_m2(taID, gIndex);
		double Zi = 0;
		double lMI =0; //local MI
		double gMI = 0; //global MI

		double N =0; //number of AUs
		if (taID==Const.TA_All)
			N = worldGisArealUnitAgentList.size();
		else N= calculateNbOfTAforID(taID);

		String valuesArray[][] = new String[worldGisArealUnitAgentList.size()][4];
		String colHeaders[] = {"AU_ID", "AU_Name","TA_Name","LMI_g"+gIndex+" _y"+model.startYear};
		NumberFormat nf = null;
		CSV_FileWriter fw = null;
		if (Const.OutputFile_DoMeasurmentForLMI){

			nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);

			if (taID==Const.TA_All) {		
				fw = new CSV_FileWriter("LMI_"+model.getTA_NameInStringForID(taID)+"_r"+model.iController.getRunCount()+"_g"+(gIndex+1)+" _y"+model.getStartYearInString()+"_t"+model.getTickCount()+".csv", false);
				fw.writeColHeaders(colHeaders);

			}
		}
		for (int ix = 0; ix <worldGisArealUnitAgentList.size() ; ix++) {
			AreaUnit rXi = (AreaUnit)worldGisArealUnitAgentList.get(ix);
			double xi=0;
			Zi =0;
			lMI =0;
			if ((rXi.ta_id == taID)|| (taID==Const.TA_All)){

				if (rXi.getArealUnitPop()>0) {
					if (Const.IS_MoranI_USE_PROPORTION)
						xi = rXi.getGroupPop(gIndex)/rXi.getArealUnitPop(); //based on proportion
					else xi = rXi.getGroupPop(gIndex); //based on pop size

					Zi = (xi - x_bar);
				}
				double SumWjiByZj = 0;

				Iterator it = this.worldGisArealUnitAgentList.iterator();
				AreaUnit rXj;
				while (it.hasNext()) {
					rXj = (AreaUnit) it.next();

					if ((rXj.ta_id == taID)|| (taID==Const.TA_All)){
						if (rXj.getArealUnitPop() >0) {
							double xj=0;
							if (Const.IS_MoranI_USE_PROPORTION)
								xj = rXj.getGroupPop(gIndex)/rXj.getArealUnitPop(); // based on proportion
							else  xj = rXj.getGroupPop(gIndex); //based on pop size

							double w = moranI_w(rXi,rXj);
							double one_iteration_sum = w *(xj - x_bar); // Zj =(xj - x_bar)
							SumWjiByZj = SumWjiByZj + one_iteration_sum;
						}	
					}
				}
				lMI   = Zi*SumWjiByZj/m2;
				gMI = gMI + lMI;
				if (Const.OutputFile_DoMeasurmentForLMI){

					valuesArray[ix][0] = ""+rXi.au_id;
					valuesArray[ix][1] = ""+rXi.au_name;
					valuesArray[ix][2] = ""+rXi.ta_name;

					nf.format(lMI);
					valuesArray[ix][3] = ""+lMI;
				}

			}
		}

		double S0 = moranI_S0(taID, gIndex);
		if (taID==Const.TA_All) {	
			if (Const.OutputFile_DoMeasurmentForLMI){

				fw.appendCols(valuesArray);
				fw.close();
			}
		}

		return gMI/S0;
	}



	/**
	 * Calculate Entropy (Metro. area/World Div./Ent.) - based on Reardon & Iceland
	 * @param taID ID of the TA (or MA) for which E is being calculated
	 * @return Entropy E for the given TA/ MA
	 */
	private double getE(int taID){  //Calculate Entropy (Metro. area/World Div./Ent.) - based on Reardon & Iceland
		double worldPop = getWorldPop(taID); //world population (all Metro. area)
		calculateWorldPopForEachGroup(taID);
		double entropy = 0;
		double groupPopWithinWorld =0;
		double groupPopPropWithinWorld =0;
		for (int i = 0; i < model.nbOfEthnicGroups; i++) {			
			groupPopWithinWorld = worldPopForEachGroupArray[i];
			groupPopPropWithinWorld = groupPopWithinWorld/worldPop;

			entropy = entropy + (groupPopPropWithinWorld * Math.log((1/groupPopPropWithinWorld)));
		}

		return entropy;

	}

	private double getEr(AreaUnit a){  //Calculate Er or Ei (Tract/AU Div./Ent.)- based on Reardon & Iceland

		double entropy = 0;
		for (int i = 0; i < model.nbOfEthnicGroups; i++) {			
			double groupPopWithinRegion = a.getGroupPop(i);
			double regionPop = a.getArealUnitPop();

			if (regionPop <=0)
				regionPop = 1;
			if (groupPopWithinRegion <=0)
				groupPopWithinRegion = 1;

			double groupPopPropWithinRegion = groupPopWithinRegion/regionPop;
			entropy =  entropy + (groupPopPropWithinRegion * Math.log((1/groupPopPropWithinRegion))); 
		}

		return entropy;
	}


	public  double getH_census(int taID, int censusDataIntroSpan) {
		double censusVal=0d;
		if (taID== Const.TA_All) {
			double periodBetweenCensuses = censusDataIntroSpan/3;
			double timeTick = model.getController().getCurrentTime();
			if (timeTick <= 1)
				censusVal = 0.1617d;
			if (timeTick <= periodBetweenCensuses)
				censusVal = 0.1426d;
			else if (timeTick <= (periodBetweenCensuses*2))
				censusVal = 0.1596d;
			else if (timeTick <= (periodBetweenCensuses*3))
				censusVal = 0.1671d;
			else    censusVal = 0.1671d;

		}
		return censusVal;

	}

	public synchronized double getH(int taID) {  //Based on Reardon, Iceland and M&D

		double T = getWorldPop(taID); //world population 
		double E = getE(taID);
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit a;
		double t; //region population
		double H = 0;
		while (it.hasNext()) {
			a = (AreaUnit) it.next();
			if ((a.ta_id == taID)|| (taID==Const.TA_All)){
				t = a.getArealUnitPop();
				double Er = getEr(a);
				H= H+ ((t* (E-Er)) / (T*E));
			}
		}
		return H;
	}


	private double SW_isolationIndex_Dividend(int taID, int groupIndex){  //calculate the top of the equation, gA being minority/black
		calculateWorldPopForEachGroup(taID);
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit ra;

		double a_minPopByRegion = 0;
		double b_regionPop = 0; 
		double A_worldMinPop = 0; 

		double sum = 0.0;

		A_worldMinPop = worldPopForEachGroupArray[groupIndex];

		while (it.hasNext()) {
			ra = (AreaUnit) it.next();
			if ((ra.ta_id == taID)|| (taID==Const.TA_All)){
				a_minPopByRegion = ra.getGroupPop(groupIndex);
				b_regionPop = ra.getArealUnitPop();
				if (b_regionPop > 0)
					sum = sum + (Math.pow(a_minPopByRegion, 2)/b_regionPop);
			}

		}
		sum = (1/A_worldMinPop) * sum;

		return sum;
	}
	
	private double SW_isolationIndex_Divisor(int taID, int groupIndex) {  //calculate bottom of the equation

		calculateWorldPopForEachGroup(taID);
		double A_worldMinPop = worldPopForEachGroupArray[groupIndex];
		double B_worldPop = getWorldPop(taID);

		return A_worldMinPop/B_worldPop;

	}



	public double getSW_IsolationIndex(int taID, int groupIndex) { // as defined by Shevky and Williams seen in Bell 1954

		double res = SW_isolationIndex_Dividend(taID, groupIndex)/ SW_isolationIndex_Divisor(taID, groupIndex);
		return res;		
	}


	public double getIsolationIndex(int taID, int g1Index, int g2Index) { // defined by Bell (see Seg. Analyzer App. help doc)

		calculateWorldPopForEachGroup(taID);
		Iterator it = worldGisArealUnitAgentList.iterator();
		AreaUnit ra;

		double xi;
		double X;
		double yi;
		double ti;

		double sum = 0.0;

		while (it.hasNext()) {
			ra = (AreaUnit) it.next();
			if ((ra.ta_id == taID)|| (taID==Const.TA_All)){
				xi = ra.getGroupPop(g1Index);
				X = worldPopForEachGroupArray[g1Index];

				double xiDivX=0;
				if (X>0)
					xiDivX = xi/X;

				yi = ra.getGroupPop(g2Index);
				ti = ra.getArealUnitPop();
				double yiDIVti=0;
				if (ti>0) 
					yiDIVti = yi/ti;

				sum = sum + (xiDivX *yiDIVti);		
			}
		}
		return sum;
	}

	public void calculateLQ_old(int taID, int gIndex) { // defined by Isar (see SegAnalyser)

		calculateWorldPopForEachGroup(taID);
		Iterator it = worldGisArealUnitAgentList.iterator();

		double xi;  //populaiton of group X in spatial/are unit i
		double X;  //populaiton of group X in metropolitain area
		double ti; //total population in spatial/area unit i
		double T; //total populaiton of the metropolitian area
		T = getWorldPop(taID); 

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		String valuesArray[][] = new String[worldGisArealUnitAgentList.size()][4];

		String colHeaders[] = {"AU_ID", "AU_Name","TA_Name","QL_g"+gIndex+" _y"+model.startYear};
		CSV_FileWriter fw = new CSV_FileWriter("QL_"+model.getTA_NameInStringForID(taID)+"_r"+model.iController.getRunCount()+"_g"+(gIndex+1)+" _y"+model.getStartYearInString()+" _t"+model.getTickCount()+".csv", false);
		fw.writeColHeaders(colHeaders);

		for (int i = 0; i <worldGisArealUnitAgentList.size() ; i++) {
			AreaUnit ra = (AreaUnit)worldGisArealUnitAgentList.get(i);
			ra = (AreaUnit) it.next();
			if ((ra.ta_id == taID)|| (taID==Const.TA_All)){
				xi = ra.getGroupPop(gIndex);
				X = worldPopForEachGroupArray[gIndex];

				double XdivT=0;
				if (T>0)
					XdivT = X/T;

				ti = ra.getArealUnitPop();
				double xiDIVti=0;
				if (ti>0) 
					xiDIVti = xi/ti;

				double lQL = xiDIVti / XdivT;

				valuesArray[i][0] = ""+ra.au_id;
				valuesArray[i][1] = ""+ra.au_name;
				valuesArray[i][2] = ""+ra.ta_name;
				nf.format(lQL);
				valuesArray[i][3] = ""+lQL;

			}
		}

		fw.appendCols(valuesArray);
		fw.close();
		//return sum;
	}

	public void calculateLQ(int taID, int gIndex) { // defined by Isar (see SegAnalyser)

		calculateWorldPopForEachGroup(taID);


		double xi;  //populaiton of group X in spatial/are unit i
		double X;  //populaiton of group X in metropolitain area
		double ti; //total population in spatial/area unit i
		double T; //total populaiton of the metropolitian area
		T = getWorldPop(taID); 

		ArrayList v = new ArrayList();

		if (!lqNamesAndIDsWritten) {
			Iterator firstIter = worldGisArealUnitAgentList.iterator();
			ArrayList vAUid = new ArrayList();
			vAUid.add("AU_ID");
			ArrayList vAUname= new ArrayList();
			vAUname.add("AU_Name");
			ArrayList vTAname = new ArrayList();
			vTAname.add("TA_Name");
			for (int i = 0; i <worldGisArealUnitAgentList.size() ; i++) {
				AreaUnit ra = (AreaUnit)worldGisArealUnitAgentList.get(i);
				ra = (AreaUnit) firstIter.next();
				if ((ra.ta_id == taID)|| (taID==Const.TA_All)){

					vAUid.add(""+ra.au_id);
					vAUname.add(ra.au_name);
					vTAname.add(ra.ta_name);

				}
			}
			lqWriter.addData(vAUid);
			lqWriter.addData(vAUname);
			lqWriter.addData(vTAname);
			lqNamesAndIDsWritten=true;
		}

		Iterator it = worldGisArealUnitAgentList.iterator();

		v.add("g"+(gIndex+1)+"_t"+(int)model.getTickCount());
		for (int i = 0; i <worldGisArealUnitAgentList.size() ; i++) {
			AreaUnit ra = (AreaUnit)worldGisArealUnitAgentList.get(i);
			ra = (AreaUnit) it.next();
			if ((ra.ta_id == taID)|| (taID==Const.TA_All)){
				xi = ra.getGroupPop(gIndex);
				X = worldPopForEachGroupArray[gIndex];

				double XdivT=0;
				if (T>0)
					XdivT = X/T;

				ti = ra.getArealUnitPop();
				double xiDIVti=0;
				if (ti>0) 
					xiDIVti = xi/ti;

				double lQL = xiDIVti / XdivT;
				v.add(lQL);
			}
		}
		lqWriter.addData(v);
	}

	public double getNbOfAUWhereEthnicGroupIsMajority(int taID, int gIndex) {

		Iterator it = worldGisArealUnitAgentList.iterator();
		double totalNumWhereGroupMajority = 0;
		for (int i = 0; i <worldGisArealUnitAgentList.size() ; i++) {
			AreaUnit ra = (AreaUnit)worldGisArealUnitAgentList.get(i);
			ra = (AreaUnit) it.next();
			if ((ra.ta_id == taID)|| (taID==Const.TA_All)){	

				if (ra.getMajGroupIndex() == gIndex)
					totalNumWhereGroupMajority++;
			}
		}
		return totalNumWhereGroupMajority;
	}

	protected  DoubleArrayList getVacantSpaceList() {
		DoubleArrayList vacantSpaceList = new DoubleArrayList(); 
		for (int i=0; i<worldGisArealUnitAgentList.size();i++) {
			vacantSpaceList.add(worldGisArealUnitAgentList.get(i).getVacantSpace());
		}
		return vacantSpaceList;
	}

	public  double getMaxVacantSpace(int i){
		DoubleArrayList vacantSpaceList = getVacantSpaceList();
		double max = Descriptive.max(vacantSpaceList);
		i = vacantSpaceList.indexOf(max);
		return max;
	}

	public  double getMinVacantSpace(){
		DoubleArrayList vacantSpaceList = getVacantSpaceList();
		return Descriptive.min(vacantSpaceList);
	}

	public ArrayList<AreaUnit> getAreaUnitList() {
		return worldGisArealUnitAgentList;
	}
	public synchronized int getPoolListSize(){
		return poolList.size();
	}

	public int howmanyHHlikeThis(int rID, int gInd){
		int totalLikeThis = 0;
		Iterator it = poolList.iterator();
		while (it.hasNext()) {
			Household hh = (Household) it.next();
			if ((hh.auID == rID) && (hh.groupIndex ==gInd)){

				totalLikeThis = totalLikeThis+1;

			}
		}
		return totalLikeThis;
	}

	public void closeLocalWriters(){

		if (Const.OutputFile_DoMeasurmentForLMI) {
			lmiWriter.writeToFile();
		}

		if (Const.OutputFile_DoMeasurmentForLQ) {
			lqWriter.writeToFile();
		}
	}



	/**
	 * This is temporary method to enforce the exact number of people flow-in into the city
	 */
	public void flowInPopInPoolBasedOnCensus(int timeTick, int censusDataIntroSpan){


		calculatePopGrowth_AddVacancyAndPoolPop(timeTick, censusDataIntroSpan);

	}

	public int get5TA_index(int taID) {

		int fiveTA_index=-2;
		switch (taID) { 
		case Const.TA_5_NorthShore: 
			fiveTA_index = Const.FIVE_TA_INDEX_1NorthShore;
			break;		

		case Const.TA_6_Waitakere: 
			fiveTA_index = Const.FIVE_TA_INDEX_2Waitakere;
			break;	

		case Const.TA_7_Auckland: 
			fiveTA_index = Const.FIVE_TA_INDEX_3Auckland;
			break;	

		case Const.TA_8_Manukau: 
			fiveTA_index = Const.FIVE_TA_INDEX_4Manukau;
			break;				 

		case Const.TA_9_Papakura: 
			fiveTA_index = Const.FIVE_TA_INDEX_5Papakura;
			break;	
		}

		return fiveTA_index;
	}

	public int get7TA_ID(int taIndex) {

		int sevenTA_ID=-2;

		switch (taIndex) { 
		case Const.FIVE_TA_INDEX_1NorthShore: 
			sevenTA_ID = Const.TA_5_NorthShore;
			break;		

		case Const.FIVE_TA_INDEX_2Waitakere: 
			sevenTA_ID = Const.TA_6_Waitakere;
			break;	

		case Const.FIVE_TA_INDEX_3Auckland: 
			sevenTA_ID = Const.TA_7_Auckland;
			break;	

		case Const.FIVE_TA_INDEX_4Manukau: 
			sevenTA_ID = Const.TA_8_Manukau;
			break;				 

		case Const.FIVE_TA_INDEX_5Papakura: 
			sevenTA_ID = Const.TA_9_Papakura;
			break;	
		}

		return sevenTA_ID;
	}

	private void outflowPop_TA_based(TAPop taImmigrantPop, int periodBetweenCensuses) {

		int totalEliminated_Euro=0;
		int totalEliminated_Asian=0;
		int totalEliminated_Pac=0;
		int totalEliminated_Mao=0;

		for (int taIndex =0; taIndex<5; taIndex++) {            	 
			for (int gIndex=0; gIndex<Const.MaxNbOfGroupsHandeledByModel; gIndex++) {
				/*
				 * the population change during census period was calcluated and stored in taImigrantPop 
				 * based on periodBetweenCensuses [5 or 10 or ... years]. 
				 * For projection period, the population change was calcualted on yearly base. So, it is assumed
				 * it is responsability of the caller of this method to make sure to send the correct periodBetweenCensuses
				 *  (e.g. for projected period with yearly population change set periodBetweenCensuses=1 before calling this method)
				 */
				int pop = taImmigrantPop.getPop(taIndex, gIndex)/periodBetweenCensuses;
				if (pop < 0) {				
					int nbOfAgentsToBeEliminated = Math.abs(pop);			

					ArrayList<AreaUnit> auList4TA = getWorldAreaUnitListCopyForTA(get7TA_ID(taIndex));

					while (nbOfAgentsToBeEliminated >0) {
						for (AreaUnit au : auList4TA) {
							if (nbOfAgentsToBeEliminated>0) {
								if (au.getGroupPop(gIndex)> 10) { // do not eliminate from AU pop with less than 10 agents of this group
									au.reduceOneFromAU_Poulation(gIndex);
									nbOfAgentsToBeEliminated--;
									switch (gIndex) { 
									case Const.G1_EURO: 
										totalEliminated_Euro++;
										break;	
									case Const.G2_ASIAN: 
										totalEliminated_Asian++;
										break;	
									case Const.G3_PACIFIC: 
										totalEliminated_Pac++;
										break;	
									case Const.G4_MAORI: 
										totalEliminated_Mao++;
										break;								 
									}
								}
							}
						}
					}
				}
			}
		}

		if (Const.IS_VERBOSE) {
			System.out.print(model.getTickCount()+ ": outflowPop_TA_based                  (Euro, Asian, Pac, Mao): "+  totalEliminated_Euro + ", " +totalEliminated_Asian+", "+totalEliminated_Pac+", "+totalEliminated_Mao);
			System.out.println("  [census span years: "+Const.CENSUS_DATA_INTRO_SPAN+"]");
		}

	}

	private void addNewPopToPoolForEachGroup_TA_based(TAPop taImmigrantPop, int periodBetweenCensuses) {

		double groupToleranceArray[] = new double[Const.MaxNbOfGroupsHandeledByModel];

		groupToleranceArray[0] = model.getTolerancePrefG1();
		groupToleranceArray[1] = model.getTolerancePrefG2();
		groupToleranceArray[2] = model.getTolerancePrefG3();
		groupToleranceArray[3] = model.getTolerancePrefG4();


		int nbOfAUinTAToSearch[] = {7, 7, 10, 8, 6};	// real values {53, 55, 104, 87, 17};	NS, WTK, Akl, Mka, Ppk

		int totalPopAdded_Euro=0;
		int totalPopAdded_Asian=0;
		int totalPopAdded_Pac=0;
		int totalPopAdded_Mao=0;


		for (int taIndex =0; taIndex<5; taIndex++) {            	 
			for (int gIndex=0; gIndex<Const.MaxNbOfGroupsHandeledByModel; gIndex++) {
				/*
				 * the population change during census period was calcluated and stored in taImigrantPop 
				 * based on periodBetweenCensuses [5 or 10 or ... years]. 
				 * For projection period, the population change was calcualted on yearly base. So, it is assumed
				 * it is responsability of the caller of this method to make sure to send the correct periodBetweenCensuses
				 *  (e.g. for projected period with yearly population change set periodBetweenCensuses=1 before calling this method)
				 */
				int pop = taImmigrantPop.getPop(taIndex, gIndex)/periodBetweenCensuses;
				if (pop > 0) {		
					for (int k=0; k<pop; k++) {

						switch (gIndex) { 
						case Const.G1_EURO: 
							totalPopAdded_Euro++;
							break;	
						case Const.G2_ASIAN: 
							totalPopAdded_Asian++;
							break;	
						case Const.G3_PACIFIC: 
							totalPopAdded_Pac++;
							break;	
						case Const.G4_MAORI: 
							totalPopAdded_Mao++;
							break;								 
						}	

						Household hh = new Household(gIndex, null);
						hh.setCreationTime(model.getTickCount());
						hh.setToleranceThresholdPref(groupToleranceArray[gIndex]);    
						hh.setLocationPref(LOC_PREF.GLOBAL);

						int searchLimit=0;
						if (Const.IS_IMMIGRANT_USE_TA_PREFERENCE) {
							searchLimit = Random.poisson.nextInt(nbOfAUinTAToSearch[taIndex]);
						}
						else searchLimit = Random.poisson.nextInt(Const.SEARCH_LIMIT_FACTOR);

						if (searchLimit ==0)
							searchLimit = 1;

						hh.setSearchLimit(searchLimit);

						hh.setPreferredTA_ID(get7TA_ID(taIndex));

						poolList.add(hh);

					}
				}

			}
		}

		if (Const.IS_VERBOSE) {
			System.out.print(model.getTickCount()+ ": addNewPopToPoolForEachGroup_TA_based (Euro, Asian, Pac, Mao): "+  totalPopAdded_Euro + ", " +totalPopAdded_Asian+", "+totalPopAdded_Pac+", "+totalPopAdded_Mao);
			System.out.println("  [census span years: "+Const.CENSUS_DATA_INTRO_SPAN+"]");
		}

	}


	/*
	 * This function does not currently produce desirable result. Extra Euro and lower than other groups.
	 * To verify the adjustmenet function
	 */
	public void calculatePopGrowth_AdjVacancy_AdjFlowIn_BasedOnCensus_TA_based(int timeTick, int censusDataIntroSpan){

		//System.out.println("calculatePopGrowth_AddVacancyAndPoolPop_TA_based: "+timeTick);

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int totalToAddPop_Euro=0;
		int totalToAddPop_Asian=0;
		int totalAddPop_Pac=0;
		int totalToAdPop_Mao=0;

		TAPop taImmigrantPop = new TAPop();

		int periodBetweenCensuses =  censusDataIntroSpan/3; // we have 3 censuses

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			int taID = au.getTA_ID(); 
			int fiveTA_index = this.get5TA_index(taID);

			double auCurrVac = au.getVacantSpace();

			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			double au_nbToIncrease_Euro= 0;
			double au_nbToIncrease_Asian=0;
			double au_nbToIncrease_Pac = 0;
			double au_nbToIncrease_Mao = 0;

			if (timeTick <= censusDataIntroSpan) {
				//System.out.println( " pop 96: "+ au.getE_euro_96());
				if (timeTick <= periodBetweenCensuses) {
					au_nbToIncrease_Euro = (au.getE_euro_96() - au.getE_euro_91())/periodBetweenCensuses;
					au_nbToIncrease_Asian = (au.getE_asian_96() - au.getE_asian_91())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_96() - au.getE_pac_91())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_96() - au.getE_maori_91())/periodBetweenCensuses;
				}
				else if (timeTick <= (periodBetweenCensuses*2)) {
					au_nbToIncrease_Euro = (au.getE_euro_01() - au.getE_euro_96())/periodBetweenCensuses;
					au_nbToIncrease_Asian = (au.getE_asian_01() - au.getE_asian_96())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_01() - au.getE_pac_96())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_01() - au.getE_maori_96())/periodBetweenCensuses;
				}
				else if (timeTick <= (periodBetweenCensuses*3)) {
					au_nbToIncrease_Euro = (au.getE_euro_06() - au.getE_euro_01())/periodBetweenCensuses;
					au_nbToIncrease_Asian = (au.getE_asian_06() - au.getE_asian_01())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_06() - au.getE_pac_01())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_06() - au.getE_maori_01())/periodBetweenCensuses;
				}

			}
			else { // after census periods (projections)

				switch (Const.SNZ_GROWTH_PROJ_CASE) {
				case LOW: 
					growthRate_Euro = getCensusProjectionGrowthRate_Low(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowthRate_Low(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowthRate_Low(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowthRate_Low(taID, Const.G4_MAORI);	
					break;
				case MED:
					growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	
					break;
				case HIGH:
					growthRate_Euro = getCensusProjectionGrowthRate_High(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowthRate_High(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowthRate_High(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowthRate_High(taID, Const.G4_MAORI);	
					break;
				}

				au_nbToIncrease_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
				au_nbToIncrease_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
				au_nbToIncrease_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
				au_nbToIncrease_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));

			}

			totalToAddPop_Euro += au_nbToIncrease_Euro;
			totalToAddPop_Asian += au_nbToIncrease_Asian;
			totalAddPop_Pac += au_nbToIncrease_Pac;
			totalToAdPop_Mao += au_nbToIncrease_Mao;

			taImmigrantPop.addPop(fiveTA_index, Const.G1_EURO, (int)au_nbToIncrease_Euro);
			taImmigrantPop.addPop(fiveTA_index, Const.G2_ASIAN, (int)au_nbToIncrease_Asian);		
			taImmigrantPop.addPop(fiveTA_index, Const.G3_PACIFIC, (int)au_nbToIncrease_Pac);
			taImmigrantPop.addPop(fiveTA_index, Const.G4_MAORI, (int)au_nbToIncrease_Mao);


			double au_nbToIncrease_total = au_nbToIncrease_Euro+ au_nbToIncrease_Asian+ 
					au_nbToIncrease_Pac+au_nbToIncrease_Mao;


			double auCurrentPop = au.getArealUnitPop();
			double auPopToBe = auCurrentPop + au_nbToIncrease_total;	

			double vacancyRate =0;

			if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM) {

				vacancyRate = getCensusBasedVacancyRatePercentageForTA_Randomly(taID); 
			}
			else vacancyRate = getCensusBasedVacancyRatePercentageForTA(taID, timeTick, Const.CENSUS_DATA_INTRO_SPAN);  

			int vacSpaceToAdd = (int)Math.round(((auPopToBe  * vacancyRate)/100));

			if (vacSpaceToAdd <0)
				vacSpaceToAdd=0; 

			au.setVacantSpace(vacSpaceToAdd);

		}

		double totalOfAll = totalToAddPop_Euro + totalToAddPop_Asian +	totalAddPop_Pac +
				totalToAdPop_Mao;

		taImmigrantPop.adjustPops();
		addNewPopToPoolForEachGroup_TA_based(taImmigrantPop, 1);

	} 

	public void calculatePopGrowth_AdjVacancy_FlowOut_FlowIn_BasedOnCensus_TA_based(int timeTick, int censusDataIntroSpan){


		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int ma_totalPopChanged_Euro=0;
		int ma_totalPopChanged_Asian=0;
		int ma_totalPopChanged_Pac=0;
		int ma_totalPopChanged_Mao=0;

		TAPop taImmigrantPop = new TAPop();

		int periodBetweenCensuses =  censusDataIntroSpan/3; // we have 3 censuses

		boolean isDoingGrowthProjection= false;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			int taID = au.getTA_ID(); 
			int fiveTA_index = this.get5TA_index(taID);

			double auCurrVac = au.getVacantSpace();

			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			int au_popChanged_Euro= 0;
			int au_popChanged_Asian=0;
			int au_popChanged_Pac = 0;
			int au_popChanged_Mao = 0;

			if (timeTick <= censusDataIntroSpan) {
				if (timeTick <= periodBetweenCensuses) {
					au_popChanged_Euro = (au.getE_euro_96() - au.getE_euro_91());
					au_popChanged_Asian = (au.getE_asian_96() - au.getE_asian_91());
					au_popChanged_Pac = (au.getE_pac_96() - au.getE_pac_91());
					au_popChanged_Mao = (au.getE_maori_96() - au.getE_maori_91());
				}
				else if (timeTick <= (periodBetweenCensuses*2)) {
					au_popChanged_Euro = (au.getE_euro_01() - au.getE_euro_96());
					au_popChanged_Asian = (au.getE_asian_01() - au.getE_asian_96());
					au_popChanged_Pac = (au.getE_pac_01() - au.getE_pac_96());
					au_popChanged_Mao = (au.getE_maori_01() - au.getE_maori_96());
				}
				else if (timeTick <= (periodBetweenCensuses*3)) {
					au_popChanged_Euro = (au.getE_euro_06() - au.getE_euro_01());
					au_popChanged_Asian = (au.getE_asian_06() - au.getE_asian_01());
					au_popChanged_Pac = (au.getE_pac_06() - au.getE_pac_01());
					au_popChanged_Mao = (au.getE_maori_06() - au.getE_maori_01());
				}

			}
			else { // after census periods (projections)

			switch (Const.SNZ_GROWTH_PROJ_CASE) {
			case LOW: 
				growthRate_Euro = getCensusProjectionGrowthRate_Low(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowthRate_Low(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowthRate_Low(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowthRate_Low(taID, Const.G4_MAORI);	
				break;
			case MED:
				growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	
				break;
			case HIGH:
				growthRate_Euro = getCensusProjectionGrowthRate_High(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowthRate_High(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowthRate_High(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowthRate_High(taID, Const.G4_MAORI);	
				break;
			}

			au_popChanged_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
			au_popChanged_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
			au_popChanged_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
			au_popChanged_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));

			/*
			 * the SNZ growth projection rate is yearly (as is calculated here too).
			 *  unlike the previous census-based period/ years which the change in population is based on census span year 
			 *  (that is why it is divided later by census span year [for vacancy, outflow and inflow] to calcualte it in yearly base)
			 *  This should not be done for the SNZ projected period as they are already in yearly base) .
			 *  Therefore (to adjust for yearly projection growth) we later set manaully  periodBetweenCensuses =1; (for census period this was either 5 [for 15 years] or 10, or ...)
			 */

			isDoingGrowthProjection=true;

			}

			ma_totalPopChanged_Euro += au_popChanged_Euro;
			ma_totalPopChanged_Asian += au_popChanged_Asian;
			ma_totalPopChanged_Pac += au_popChanged_Pac;
			ma_totalPopChanged_Mao += au_popChanged_Mao;

			taImmigrantPop.addPop(fiveTA_index, Const.G1_EURO, au_popChanged_Euro);
			taImmigrantPop.addPop(fiveTA_index, Const.G2_ASIAN, au_popChanged_Asian);		
			taImmigrantPop.addPop(fiveTA_index, Const.G3_PACIFIC, au_popChanged_Pac);
			taImmigrantPop.addPop(fiveTA_index, Const.G4_MAORI, au_popChanged_Mao);


			double au_popChanged_total = au_popChanged_Euro+ au_popChanged_Asian+ 
					au_popChanged_Pac+au_popChanged_Mao;


			double auCurrentPop = au.getArealUnitPop();

			double auPopToBe = 0;
			if (isDoingGrowthProjection)
				auPopToBe = auCurrentPop + au_popChanged_total;
			else auPopToBe = auCurrentPop + (au_popChanged_total/periodBetweenCensuses);

			double vacancyRate =0;

			if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM) {
				/*
				 * @TODO: the vacancy rate is obtained based on SNZ rates.
				 * Crrently some randomness is used, but rates are all the same
				 * throughout census years and beyond.  
				 */
				vacancyRate = getCensusBasedVacancyRatePercentageForTA_Randomly(taID); 
			}
			else vacancyRate = getCensusBasedVacancyRatePercentageForTA(taID, timeTick, Const.CENSUS_DATA_INTRO_SPAN);  
			// else vacancyRate = getCensusLikeVacancyRatePercentageForTA(taID, timeTick, Const.CENSUS_DATA_INTRO_SPAN);  

			int vacSpaceToAdd = (int)Math.round(((auPopToBe  * vacancyRate)/100));

			if (vacSpaceToAdd <0)
				vacSpaceToAdd=0; 

			au.setVacantSpace(vacSpaceToAdd);

		}

		double ma_totalOfAll = ma_totalPopChanged_Euro + ma_totalPopChanged_Asian +	ma_totalPopChanged_Pac +
				ma_totalPopChanged_Mao;



		if (isDoingGrowthProjection)
			periodBetweenCensuses = 1; //for projection period, the populaiton chanage was calculated yearly 

		outflowPop_TA_based(taImmigrantPop, periodBetweenCensuses);

		addNewPopToPoolForEachGroup_TA_based(taImmigrantPop, periodBetweenCensuses);

	}

	/*
	 * Even-adj version
	 */
	public void calculatePopGrowth_AdjVacancy_AdjFlowIn_BasedOnCensus_TA_based_Evenly(int timeTick, int censusDataIntroSpan){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int ma_totalPopChanged_Euro=0;
		int ma_totalPopChanged_Asian=0;
		int ma_totalPopChanged_Pac=0;
		int ma_totalPopChanged_Mao=0;

		TAPop taImmigrantPop = new TAPop();

		int periodBetweenCensuses =  censusDataIntroSpan; // periods are yearly as it is based on census 2006 - 1991

		//double test_total_au=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			int taID = au.getTA_ID(); 
			int fiveTA_index = this.get5TA_index(taID);

			double auCurrVac = au.getVacantSpace();

			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			int au_popChanged_Euro= 0;
			int au_popChanged_Asian=0;
			int au_popChanged_Pac = 0;
			int au_popChanged_Mao = 0;

			if (timeTick <= (periodBetweenCensuses*3)) {
				au_popChanged_Euro = (au.getE_euro_06() - au.getE_euro_91());
				au_popChanged_Asian = (au.getE_asian_06() - au.getE_asian_91());
				au_popChanged_Pac = (au.getE_pac_06() - au.getE_pac_91());
				au_popChanged_Mao = (au.getE_maori_06() - au.getE_maori_91());


			}
			else { // after census periods (projections)

				switch (Const.SNZ_GROWTH_PROJ_CASE) {
				case LOW: 
					growthRate_Euro = getCensusProjectionGrowthRate_Low(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowthRate_Low(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowthRate_Low(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowthRate_Low(taID, Const.G4_MAORI);	
					break;
				case MED:
					growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	
					break;
				case HIGH:
					growthRate_Euro = getCensusProjectionGrowthRate_High(taID, Const.G1_EURO);
					growthRate_Asian = getCensusProjectionGrowthRate_High(taID, Const.G2_ASIAN);
					growthRate_Pac = getCensusProjectionGrowthRate_High(taID, Const.G3_PACIFIC);
					growthRate_Mao = getCensusProjectionGrowthRate_High(taID, Const.G4_MAORI);	
					break;
				}


				/* @TODO (TO CHECK)
				 * the calculation for projection is currently based on the growth rate 
				 * (which may be based on 5 years or annual growth rate)
				 * this needs to be verified and make sure the growth is proportional to the 
				 * census data introduction span (period over which the census data are introduced/injected/used in the model)
				 */

				au_popChanged_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
				au_popChanged_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
				au_popChanged_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
				au_popChanged_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));

			}

			ma_totalPopChanged_Euro += au_popChanged_Euro;
			ma_totalPopChanged_Asian += au_popChanged_Asian;
			ma_totalPopChanged_Pac += au_popChanged_Pac;
			ma_totalPopChanged_Mao += au_popChanged_Mao;

			taImmigrantPop.addPop(fiveTA_index, Const.G1_EURO, au_popChanged_Euro);
			taImmigrantPop.addPop(fiveTA_index, Const.G2_ASIAN, au_popChanged_Asian);		
			taImmigrantPop.addPop(fiveTA_index, Const.G3_PACIFIC, au_popChanged_Pac);
			taImmigrantPop.addPop(fiveTA_index, Const.G4_MAORI, au_popChanged_Mao);


			double au_popChanged_total = au_popChanged_Euro+ au_popChanged_Asian+ 
					au_popChanged_Pac+au_popChanged_Mao;


			double auCurrentPop = au.getArealUnitPop();
			double auPopToBe = auCurrentPop + (au_popChanged_total/periodBetweenCensuses);	

			double vacancyRate =0;

			if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM) {
				/*
				 * @TODO: the vacancy rate is obtained based on SNZ rates.
				 * Crrently some randomness is used, but rates are all the same
				 * throughout census years and beyond.  
				 */
				vacancyRate = getCensusBasedVacancyRatePercentageForTA_Randomly(taID); 
				//vacancyRate = getVacancyRateUsingSameConst4All_Random();
			}
			else vacancyRate = getCensusBasedVacancyRatePercentageForTA(taID, timeTick, Const.CENSUS_DATA_INTRO_SPAN);  
			//  else vacancyRate = getCensusLikeVacancyRatePercentageForTA(taID, timeTick, Const.CENSUS_DATA_INTRO_SPAN);  

			int vacSpaceToAdd = (int)Math.round(((auPopToBe  * vacancyRate)/100));


			if (vacSpaceToAdd <0)
				vacSpaceToAdd=0; 

			au.setVacantSpace(vacSpaceToAdd);

		}

		double ma_totalOfAll = ma_totalPopChanged_Euro + ma_totalPopChanged_Asian +	ma_totalPopChanged_Pac +
				ma_totalPopChanged_Mao;


		taImmigrantPop.adjustPops();

		addNewPopToPoolForEachGroup_TA_based(taImmigrantPop, periodBetweenCensuses);

	}


	private void addHardcodedPoptoPoolForEachGroup(int euroPop, int asianPop, int pacPop, int maoPop){

		ArrayList flowInPopList = new ArrayList();
		int inGroupPop;
		flowInPopList.add(Const.G1_EURO, euroPop);
		flowInPopList.add(Const.G2_ASIAN, asianPop);
		flowInPopList.add(Const.G3_PACIFIC, pacPop);
		flowInPopList.add(Const.G4_MAORI, maoPop);

		for (int inGroupIndex=0; inGroupIndex<model.nbOfEthnicGroups; inGroupIndex++){
			inGroupPop = ((Integer) flowInPopList.get(inGroupIndex)).intValue();
			double flowInPopTolerancePercentage = -1.00;

			switch (inGroupIndex) { 
			case 0: 
				flowInPopTolerancePercentage = model.getTolerancePrefG1();
				break;
			case 1: 
				flowInPopTolerancePercentage = model.getTolerancePrefG2();
				break;
			case 2: 
				flowInPopTolerancePercentage = model.getTolerancePrefG3();
				break;
			case 3: 
				flowInPopTolerancePercentage = model.getTolerancePrefG4();
				break;
			} 

			for (int i=0; i<inGroupPop; i++){
				Household hh = new Household(inGroupIndex, null);
				hh.setCreationTime(model.getTickCount());
				hh.setToleranceThresholdPref(flowInPopTolerancePercentage);    

				hh.setLocationPref(LOC_PREF.GLOBAL);
				int searchLimit = Random.poisson.nextInt(Const.SEARCH_LIMIT_FACTOR);

				if (searchLimit ==0)
					searchLimit = 1;

				hh.setSearchLimit(searchLimit);

				poolList.add(hh);

			}
		}

	}


	private void hardcodedFlowInPopInPoolUntilCensus2006_moreRealistic(int timeTick, int censusDataIntroSpan){

		int nb_increase_Euro = getCensusPopIncreaseForGroupIDAndTime(Const.G1_EURO, timeTick);
		int nb_increase_Asian = getCensusPopIncreaseForGroupIDAndTime(Const.G2_ASIAN, timeTick);
		int nb_increase_Pac = getCensusPopIncreaseForGroupIDAndTime(Const.G3_PACIFIC, timeTick);
		int nb_increase_Mao = getCensusPopIncreaseForGroupIDAndTime(Const.G4_MAORI, timeTick);

		int nb_increase_all = nb_increase_Euro + nb_increase_Asian + nb_increase_Pac + nb_increase_Mao;

		addHardcodedVacancyUntilCensus2006(timeTick, nb_increase_all, censusDataIntroSpan);

		addHardcodedPoptoPoolForEachGroup (nb_increase_Euro, nb_increase_Asian, nb_increase_Pac, nb_increase_Mao);

	}

	/**
	 * This is temporary method to enforce the exact number of people flow-in into the city
	 */
	private void hardcodedFlowInPopInPoolUntilCensus2006(int timeTick){

		addHardcodedVacancy_randomly((984 + 10747 + 3376 + 1313 ));

		addHardcodedPoptoPoolForEachGroup (984, 10747, 3376, 1313);

	}


	/**
	 *  This methods initialise the vacancy in each AU
	 *  It is currently calculated as a percentage (defined by the user) of population in each AU
	 *  However, there is a minimal vacancy required (defined by Const.MinBasicVacancy).
	 *  If the calculated proportion of vacancy is less than Const.MinBasicVacancy, then 
	 *  the Const.MinBasicVacancy would be considered the vacancy.
	 *  This is crucial as some AUs at the beginning of simulation may have zero population. 
	 */
	
	public void initinalizeVacancy(){

		if (Const.IS_VERBOSE)
			System.out.println("GeoSpace:: initinalizeVacancy()");

		int vacancyPercentage = model.getVacancyPercentage();
		int numAgents = worldGisArealUnitAgentList.size();

		for (int i=0; i<numAgents; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);
			int auTotalPop = (int)au.getArealUnitPop();
			int vacSpace = (auTotalPop  * vacancyPercentage)/100;
			if (vacSpace < Const.MinVacancyNumber)
				vacSpace = Const.MinVacancyNumber;
			au.setVacantSpace(vacSpace);
		}
	}

	public void adjustVacancyBasedOnCensusRate(int timeTick, int censusDataIntroSpan){
		int numOfAUs = worldGisArealUnitAgentList.size();

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);
			int taID = au.getTA_ID();


			double vacancyRate = getCensusBasedVacancyRatePercentageForTA_Randomly(taID); //get contantly the average of last two cencus


			double auTotalPop = au.getArealUnitPop();
			int vacSpaceToAdd = (int)Math.round(((auTotalPop  * vacancyRate)/100));

			au.setVacantSpace(vacSpaceToAdd);

		}
	}


	public void initializeVacancyBasedOnCensusRate(int censusDataIntroSpan){

		if (Const.IS_VERBOSE)
			System.out.println("GeoSpace:: initializeVacancyBasedOnCensusRate()");

		int numOfAUs = worldGisArealUnitAgentList.size();

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);
			int taID = au.getTA_ID();

			double vacancyRate=0;

			if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM) {
				/*
				 * @TODO: the vacancy rate is obtained based on SNZ rates.
				 * Crrently some randomness is used, but rates are all the same
				 * throughout census years and beyond.  
				 */
				vacancyRate = getCensusBasedVacancyRatePercentageForTA_Randomly(taID); 
			}
			else vacancyRate = getCensusBasedVacancyRatePercentageForTA(taID, 0, Const.CENSUS_DATA_INTRO_SPAN);  


			double auTotalPop = au.getArealUnitPop();
			int vacSpace = (int)Math.round(((auTotalPop  * vacancyRate)/100));

			if (vacSpace < Const.MinVacancyNumber)
				vacSpace = Const.MinVacancyNumber;
			au.setVacantSpace(vacSpace);
		}
	}

	public void addHardcodedVacancy_randomly( int nbOfIncrease){

		int cumulativeAddedNb =0;
		int remainingToAdd= nbOfIncrease;
		int i=0;
		boolean round2= false;
		double worldPop = this.getWorldPop(Const.TA_All);

		SimUtilities.shuffle(worldGisArealUnitAgentList);
		while (remainingToAdd > 0)  {

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);
			double auTotalPop = au.getArealUnitPop();
			double pcShareOfTA = auTotalPop / worldPop;
			double shareOfTAForIncrease = pcShareOfTA * nbOfIncrease;
			int toAddHere=0;

			if (cumulativeAddedNb != nbOfIncrease) {
				if (round2)
					toAddHere = Random.uniform.nextIntFromTo(0, 30);
				else
					toAddHere = Random.binomial.nextInt((int)shareOfTAForIncrease, 0.99-cumulativeAddedNb/nbOfIncrease);
			}

			if (toAddHere > remainingToAdd) {
				toAddHere = remainingToAdd;
			}
			cumulativeAddedNb =+ toAddHere;
			remainingToAdd = remainingToAdd - toAddHere;

			double currentVacSpace = au.getVacantSpace();
			double vacToBe = currentVacSpace+toAddHere;
			au.setVacantSpace(vacToBe);

			if (i < worldGisArealUnitAgentList.size()-1)
				i++;
			else 
			{
				i=0;
				SimUtilities.shuffle(worldGisArealUnitAgentList);
				round2=true;
			}
		}

	}

	public void addHardcodedVacancyUntilCensus2006(int timeTick, int nbOfIncrease, int censusDataIntroSpan){
		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		double totalShareOfTA =0;
		double totalShareOfIncrease=0;
		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();
			double shareOfTA = auTotalPop / worldPop;
			totalShareOfTA += shareOfTA;
			double shareOfTAForIncrease = shareOfTA * nbOfIncrease;
			totalShareOfIncrease += shareOfTAForIncrease;
			double currentVacSpace = au.getVacantSpace();
			double vacToBe = currentVacSpace+shareOfTAForIncrease;
			double popToBe = auTotalPop + shareOfTAForIncrease;
			double currentVacRate = currentVacSpace/auTotalPop;
			double vacRateWithInflowPop = currentVacSpace/popToBe;

			int taID = au.getTA_ID(); 
			double vacRateToBe = getCensusBasedVacancyRatePercentageForTA(taID, timeTick, censusDataIntroSpan);

			int vacSpaceWithInflowConsideration = (int)Math.round(((popToBe  * vacRateToBe)/100));

			au.setVacantSpace(vacSpaceWithInflowConsideration);
		}
	}

	public void calculatePopGrowthAndAddVacancy(int timeTick){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int totalToAddPop_Euro=0;
		int totalToAddPop_Asian=0;
		int totalAddPop_Pac=0;
		int totalToAdPop_Mao=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();

			int taID = au.getTA_ID(); 

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			if (timeTick <=15) {
				growthRate_Euro = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G1_EURO);
				growthRate_Asian = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G4_MAORI);
			}
			else {

				growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	
			}


			double pop_Euro = au.getGroupPop(Const.G1_EURO);
			double pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double pop_Mao = au.getGroupPop(Const.G4_MAORI);

			int toAdd_Euro = (int)Math.round(((pop_Euro * growthRate_Euro)/100));
			totalToAddPop_Euro += toAdd_Euro;

			int toAdd_Asian = (int)Math.round(((pop_Asian * growthRate_Asian)/100));
			totalToAddPop_Asian += toAdd_Asian;

			int toAdd_Pac = (int)Math.round(((pop_Pac * growthRate_Pac)/100));
			totalAddPop_Pac += toAdd_Pac;

			int toAdd_Mao = (int)Math.round(((pop_Mao * growthRate_Mao)/100));
			totalToAdPop_Mao += toAdd_Mao;

		}

		System.out.println("TotalPopToAdd: "+  totalToAddPop_Euro + ", " +totalToAddPop_Asian+", "+totalAddPop_Pac+", "+totalToAdPop_Mao);
		addHardcodedVacancy_randomly(totalToAddPop_Euro+totalToAddPop_Asian+totalAddPop_Pac+totalToAdPop_Mao);

		addHardcodedPoptoPoolForEachGroup (totalToAddPop_Euro, totalToAddPop_Asian, totalAddPop_Pac, totalToAdPop_Mao);

	}

	public void calculateAndAddPopGrowth(int timeTick){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int totalToAddPop_Euro=0;
		int totalToAddPop_Asian=0;
		int totalAddPop_Pac=0;
		int totalToAdPop_Mao=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();

			int taID = au.getTA_ID(); 

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			if (timeTick <=15) {
				growthRate_Euro = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G1_EURO);
				growthRate_Asian = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusAvergeGrowthRatePerYear4TA4group(taID, Const.G4_MAORI);
			}
			else {

				growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	
			}


			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			int au_toAdd_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
			totalToAddPop_Euro += au_toAdd_Euro;

			int au_toAdd_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
			totalToAddPop_Asian += au_toAdd_Asian;

			int au_toAdd_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
			totalAddPop_Pac += au_toAdd_Pac;

			int au_toAdd_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));
			totalToAdPop_Mao += au_toAdd_Mao;

			int au_growth_Asian96_91 = (au.getE_asian_01() - au.getE_asian_91())/5;

			au.setGroupPop(Const.G1_EURO, ((int)au_pop_Euro+au_toAdd_Euro));
			au.setGroupPop(Const.G2_ASIAN, ((int)au_pop_Asian+au_toAdd_Asian));
			au.setGroupPop(Const.G3_PACIFIC, ((int)au_pop_Pac+au_toAdd_Pac));
			au.setGroupPop(Const.G4_MAORI, ((int)au_pop_Mao+au_toAdd_Mao));

		}

		System.out.println(timeTick+ ": calculateAndAddPopGrowth: total population change (Euro, Asian, Pac, Mao): "+  totalToAddPop_Euro + ", " +totalToAddPop_Asian+", "+totalAddPop_Pac+", "+totalToAdPop_Mao);

	}


	public void calculateAndAddPopGrowthDirectlyFromAU(int timeTick){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int totalToAddPop_Euro=0;
		int totalToAddPop_Asian=0;
		int totalAddPop_Pac=0;
		int totalToAdPop_Mao=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();

			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			int taID = au.getTA_ID(); 

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			double au_nbToIncrease_Euro= 0;
			double au_nbToIncrease_Asian=0;
			double au_nbToIncrease_Pac = 0;
			double au_nbToIncrease_Mao = 0;

			if (timeTick <=15) {
				//System.out.println( " pop 96: "+ au.getE_euro_96());
				if (timeTick <=5) {
					au_nbToIncrease_Euro = (au.getE_euro_96() - au.getE_euro_91())/5;
					au_nbToIncrease_Asian = (au.getE_asian_96() - au.getE_asian_91())/5;
					au_nbToIncrease_Pac = (au.getE_pac_96() - au.getE_pac_91())/5;
					au_nbToIncrease_Mao = (au.getE_maori_96() - au.getE_maori_91())/5;
				}
				else if (timeTick <=10) {
					au_nbToIncrease_Euro = (au.getE_euro_01() - au.getE_euro_96())/5;
					au_nbToIncrease_Asian = (au.getE_asian_01() - au.getE_asian_96())/5;
					au_nbToIncrease_Pac = (au.getE_pac_01() - au.getE_pac_96())/5;
					au_nbToIncrease_Mao = (au.getE_maori_01() - au.getE_maori_96())/5;
				}
				else if (timeTick <=15) {
					au_nbToIncrease_Euro = (au.getE_euro_06() - au.getE_euro_01())/5;
					au_nbToIncrease_Asian = (au.getE_asian_06() - au.getE_asian_01())/5;
					au_nbToIncrease_Pac = (au.getE_pac_06() - au.getE_pac_01())/5;
					au_nbToIncrease_Mao = (au.getE_maori_06() - au.getE_maori_01())/5;
				}

			}
			else {

				growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	

				au_nbToIncrease_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
				au_nbToIncrease_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
				au_nbToIncrease_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
				au_nbToIncrease_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));

			}

			totalToAddPop_Euro += au_nbToIncrease_Euro;
			totalToAddPop_Asian += au_nbToIncrease_Asian;
			totalAddPop_Pac += au_nbToIncrease_Pac;
			totalToAdPop_Mao += au_nbToIncrease_Mao;

			au.setGroupPop(Const.G1_EURO, ((int) (au_pop_Euro+au_nbToIncrease_Euro)));
			au.setGroupPop(Const.G2_ASIAN, ((int) (au_pop_Asian+au_nbToIncrease_Asian)));
			au.setGroupPop(Const.G3_PACIFIC, ((int) (au_pop_Pac+au_nbToIncrease_Pac)));
			au.setGroupPop(Const.G4_MAORI, ((int) (au_pop_Mao+au_nbToIncrease_Mao)));
		}

		System.out.println(timeTick+ ": calculateAndAddPopGrowthDirectlyFromAU: total population change (Euro, Asian, Pac, Mao): "+  totalToAddPop_Euro + ", " +totalToAddPop_Asian+", "+totalAddPop_Pac+", "+totalToAdPop_Mao);

	}

	public void calculatePopGrowth_AddVacancyAndPoolPop(int timeTick, int censusDataIntroSpan){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int periodBetweenCensuses =  censusDataIntroSpan/3; // we have 3 censuses

		int totalToAddPop_Euro=0;
		int totalToAddPop_Asian=0;
		int totalAddPop_Pac=0;
		int totalToAdPop_Mao=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();
			double auCurrVac = au.getVacantSpace();

			double au_pop_Euro = au.getGroupPop(Const.G1_EURO);
			double au_pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double au_pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double au_pop_Mao = au.getGroupPop(Const.G4_MAORI);

			int taID = au.getTA_ID(); 

			double growthRate_Euro= 0;
			double growthRate_Asian=0;
			double growthRate_Pac = 0;
			double growthRate_Mao = 0;

			double au_nbToIncrease_Euro= 0;
			double au_nbToIncrease_Asian=0;
			double au_nbToIncrease_Pac = 0;
			double au_nbToIncrease_Mao = 0;


			if (timeTick <= censusDataIntroSpan) {
				if (timeTick <= periodBetweenCensuses) {
					au_nbToIncrease_Euro = (au.getE_euro_96() - au.getE_euro_91())/periodBetweenCensuses;

					au_nbToIncrease_Asian = (au.getE_asian_96() - au.getE_asian_91())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_96() - au.getE_pac_91())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_96() - au.getE_maori_91())/periodBetweenCensuses;
				}
				else if (timeTick <= (periodBetweenCensuses*2)) {
					au_nbToIncrease_Euro = (au.getE_euro_01() - au.getE_euro_96())/periodBetweenCensuses;
					au_nbToIncrease_Asian = (au.getE_asian_01() - au.getE_asian_96())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_01() - au.getE_pac_96())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_01() - au.getE_maori_96())/periodBetweenCensuses;
				}
				else if (timeTick <= (periodBetweenCensuses*3)) {
					au_nbToIncrease_Euro = (au.getE_euro_06() - au.getE_euro_01())/periodBetweenCensuses;
					au_nbToIncrease_Asian = (au.getE_asian_06() - au.getE_asian_01())/periodBetweenCensuses;
					au_nbToIncrease_Pac = (au.getE_pac_06() - au.getE_pac_01())/periodBetweenCensuses;
					au_nbToIncrease_Mao = (au.getE_maori_06() - au.getE_maori_01())/periodBetweenCensuses;
				}

			}
			else { // after census periods (projections)

				growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
				growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
				growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
				growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);	

				/* @TODO (TO CHECK)
				 * the calculation for projection is currently based on the growth rate 
				 * (which may be based on 5 years or annual growth rate)
				 * this needs to be verified and make sure the growth is proportional to the 
				 * census data introduction span (period over which the census data are introduced/injected/used in the model)
				 */

				au_nbToIncrease_Euro = (int)Math.round(((au_pop_Euro * growthRate_Euro)/100));
				au_nbToIncrease_Asian = (int)Math.round(((au_pop_Asian * growthRate_Asian)/100));
				au_nbToIncrease_Pac = (int)Math.round(((au_pop_Pac * growthRate_Pac)/100));
				au_nbToIncrease_Mao = (int)Math.round(((au_pop_Mao * growthRate_Mao)/100));

			}

			totalToAddPop_Euro += au_nbToIncrease_Euro;
			totalToAddPop_Asian += au_nbToIncrease_Asian;
			totalAddPop_Pac += au_nbToIncrease_Pac;
			totalToAdPop_Mao += au_nbToIncrease_Mao;

			double au_nbToIncrease_total = au_nbToIncrease_Euro+ au_nbToIncrease_Asian+ 
					au_nbToIncrease_Pac+au_nbToIncrease_Mao;


			double totalVac = auCurrVac+au_nbToIncrease_total;
			if (totalVac <0)
				totalVac=0;

			au.setVacantSpace(totalVac);

		}

		if (totalToAddPop_Euro <0)
			totalToAddPop_Euro=0;

		System.out.print(timeTick+ ": calculatePopGrowth_AddVacancyAndPoolPop: total pop added (Euro, Asian, Pac, Mao): "+  totalToAddPop_Euro + ", " +totalToAddPop_Asian+", "+totalAddPop_Pac+", "+totalToAdPop_Mao);
		System.out.println(" [census span years: "+Const.CENSUS_DATA_INTRO_SPAN+"]");


		addHardcodedPoptoPoolForEachGroup (totalToAddPop_Euro, totalToAddPop_Asian, totalAddPop_Pac, totalToAdPop_Mao);

	}



	public void calculateAndAddHardcodedInflowOrGrowthAndVacancyAfterCensus2006(int timeTick, int censusDataIntroSpan){

		int numOfAUs = worldGisArealUnitAgentList.size();
		double worldPop = this.getWorldPop(Const.TA_All);

		int totalAddedPop_Euro=0;
		int totalAddedPop_Asian=0;
		int totalAddedPop_Pac=0;
		int totalAddedPop_Mao=0;

		for (int i=0; i<numOfAUs; i++){

			AreaUnit au = (AreaUnit)worldGisArealUnitAgentList.get(i);

			double auTotalPop = au.getArealUnitPop();

			int taID = au.getTA_ID(); 

			double vacRateToBe = getCensusBasedVacancyRatePercentageForTA(taID, timeTick, censusDataIntroSpan);

			double growthRate_Euro = getCensusProjectionGrowth_Med(taID, Const.G1_EURO);
			double growthRate_Asian = getCensusProjectionGrowth_Med(taID, Const.G2_ASIAN);
			double growthRate_Pac = getCensusProjectionGrowth_Med(taID, Const.G3_PACIFIC);
			double growthRate_Mao = getCensusProjectionGrowth_Med(taID, Const.G4_MAORI);

			double pop_Euro = au.getGroupPop(Const.G1_EURO);
			double pop_Asian = au.getGroupPop(Const.G2_ASIAN);
			double pop_Pac = au.getGroupPop(Const.G3_PACIFIC);
			double pop_Mao = au.getGroupPop(Const.G4_MAORI);

			int toAdd_Euro = (int)Math.round(((pop_Euro * growthRate_Euro)/100));
			totalAddedPop_Euro += toAdd_Euro;

			int toAdd_Asian = (int)Math.round(((pop_Asian * growthRate_Asian)/100));
			totalAddedPop_Asian += toAdd_Asian;

			int toAdd_Pac = (int)Math.round(((pop_Pac * growthRate_Pac)/100));
			totalAddedPop_Pac += toAdd_Pac;

			int toAdd_Mao = (int)Math.round(((pop_Mao * growthRate_Mao)/100));
			totalAddedPop_Mao += toAdd_Mao;

			int totalGroupsToAdd = toAdd_Euro + toAdd_Asian + toAdd_Pac + toAdd_Mao;

			double currentVacSpace = au.getVacantSpace();

			double popToBe = auTotalPop + totalGroupsToAdd;
			double currentVacRate = currentVacSpace/auTotalPop;
			double vacRateWithInflowPopGrowth = currentVacSpace/popToBe;
			int vacSpaceWithInflowGrowthConsideration = (int)Math.round(((popToBe  * vacRateToBe)/100));
			au.setVacantSpace(vacSpaceWithInflowGrowthConsideration);


		}


		addHardcodedPoptoPoolForEachGroup (totalAddedPop_Euro, totalAddedPop_Asian, totalAddedPop_Pac, totalAddedPop_Mao);

	}

	private double getVacancyRateUsingSameConst4All_Random() {

		model.getVacancyPercentage();
		return	Random.binomial.nextInt((int)Const.INFLOW_CENSUS_BASED_VACANCY_RATE_FOR_ALL, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);
	}

	/**
	 *  This returns the vacancy rate of each TA based on census data, but randomly (more or so close to census).
	 *  It uses the avarage of the two last censuses as base of calculation. 
	 *  @TODO currently the same values are used for all years (cencus period and beyond)
	 *  if we want to do it differently for each cencus period and for the period beyond census, then 
	 *  we need to expand this method (passing parameters such as timeTick would be necessary at that time)
	 *  
	 * @param taID  ID of the TA for which the vacancy rate is being sought 
	 * @return  vacancy rate of the passed TA as parameter
	 *  	
	 */

	private double getCensusBasedVacancyRatePercentageForTA_Randomly(int taID) {

		double vacancyRate = 0d;

		switch (taID) { 
		case Const.TA_4_Rodney:

			vacancyRate =  Random.binomial.nextInt((int)14.6, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;
		case Const.TA_5_NorthShore:

			vacancyRate =  Random.binomial.nextInt((int)5.4, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;

		case Const.TA_6_Waitakere:		
			vacancyRate =  Random.binomial.nextInt((int)5.8, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;
		case Const.TA_7_Auckland:

			vacancyRate =  Random.binomial.nextInt((int)7.8, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;

		case Const.TA_8_Manukau:

			vacancyRate =  Random.binomial.nextInt((int)4.9, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;

		case Const.TA_9_Papakura:

			vacancyRate =  Random.binomial.nextInt((int)5.4, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;

		case Const.TA_10_Franklin:

			vacancyRate =  Random.binomial.nextInt((int)8.9, Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);

			break;

		}

		return vacancyRate;

	}


	/**
	 *  This returns the vacancy rate of each TA based on census data.
	 *  It assumes the simulation starts with 1991 data.
	 *  Each census period is equal to periodBetweenCensuses =  entireCensusDataSpan/3;
	 *  so for time ticks of 
	 *  [0- periodBetweenCensuses [ correpsonds to 1991-96, 
	 *  [periodBetweenCensuses - periodBetweenCensuses*2[ correpsonds to 96-2001 and 
	 *  [periodBetweenCensuses*2 - periodBetweenCensuses*3[ correpsonds to 2001-2006 census data periods.
	 *  After time tick of greater than entireCensusDataSpan, an estimate average is applied. 
	 *  
	 * @param taID TA ID 
	 * @param timeTick Time tick at the time of call
	 * @param entireCensusDataSpan censuses span time period
	 * @return vacancy rate of the passed TA
	 *  	
	 */
	
	private double getCensusBasedVacancyRatePercentageForTA(int taID, int timeTick, int entireCensusDataSpan) {

		double vacancyRate = 0d;

		int periodBetweenCensuses =  entireCensusDataSpan/3; // we have 3 censuses


		switch (taID) { 
		case Const.TA_4_Rodney:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 12.8;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 14.9;

			}
			else if (timeTick <=(periodBetweenCensuses*3)) {
				vacancyRate = 14.7;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 14.6;
			}

			break;
		case Const.TA_5_NorthShore:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 4.1;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.5;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.2;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.4;
			}

			break;
		case Const.TA_6_Waitakere:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 4.1;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 6.1;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.5;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.8;
			}

			break;
		case Const.TA_7_Auckland:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 5.9;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 7.3;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 8.4;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 7.8;
			}

			break;

		case Const.TA_8_Manukau:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 3.6;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.1;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 4.6;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 4.9;
			}

			break;

		case Const.TA_9_Papakura:

			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 3.0;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.8;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.1;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.4;
			}
			break;

		case Const.TA_10_Franklin:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 7.3;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 9.4;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 8.8;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 8.9;
			}

			break;

		}

		return vacancyRate;

	}


	private double getCensusBasedVacancyRatePercentageForTA_Random(int taID, int timeTick, int entireCensusDataSpan) {

		double vacancyRate = 0d;

		int periodBetweenCensuses =  entireCensusDataSpan/3; // we have 3 censuses


		switch (taID) { 

		case Const.TA_5_NorthShore:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 4.1;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.5;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.2;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.4;
			}

			break;
		case Const.TA_6_Waitakere:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 4.1;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 6.1;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.5;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.8;
			}

			break;
		case Const.TA_7_Auckland:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 5.9;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 7.3;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 8.4;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 7.8;
			}

			break;

		case Const.TA_8_Manukau:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 3.6;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.1;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 4.6;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 4.9;
			}

			break;

		case Const.TA_9_Papakura:

			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 3.0;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 5.8;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 5.1;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 5.4;
			}
			break;

		case Const.TA_10_Franklin:
			if (timeTick <= periodBetweenCensuses) {
				vacancyRate = 7.3;
			}
			else if (timeTick<= (periodBetweenCensuses*2)) {
				vacancyRate = 9.4;

			}
			else if (timeTick <= (periodBetweenCensuses*3)) {
				vacancyRate = 8.8;

			}
			else if (timeTick > entireCensusDataSpan) {
				vacancyRate = 8.9;
			}

			break;

		}

		return vacancyRate;

	}


	private double getCensusLikeVacancyRatePercentageForTA(int taID, int timeTick, int entireCensusDataSpan) {

		System.out.println("getCensusLikeVacancyRatePercentageForTA");
		double vacancyRate = 0d;

		int periodBetweenCensuses =  entireCensusDataSpan/3; // we have 3 censuses


		switch (taID) { 

		case Const.TA_5_NorthShore:

			vacancyRate = 8;

			break;
		case Const.TA_6_Waitakere:

			vacancyRate = 5.5;

			break;
		case Const.TA_7_Auckland:

			vacancyRate = 10;

			break;

		case Const.TA_8_Manukau:

			vacancyRate = 2; 

			break;

		case Const.TA_9_Papakura:

			vacancyRate = 4.5;
			break;

		}

		return vacancyRate;

	}


	/**
	 *  This returns the growth rate projection (2006-21) of each ethnic group
	 *  in each TA based on census data projection (Low level).
	 *  
	 * @param taID  ID of the TA for which the projection rate is being sought
	 * @param groupID the ID of the ethnic group
	 * @return  lower rate of SNZ projection (for 2006-21)
	 */

	private double getCensusProjectionGrowthRate_Low(int taID, int groupID) {

		double growthRate = 0d;

		switch (taID) { 
		case Const.TA_4_Rodney:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.7;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.3;
				break;
			case Const.G3_PACIFIC:
				growthRate = 2.4;
				break;
			case Const.G4_MAORI:
				growthRate = 1.7;
				break;
			}

			break;
		case Const.TA_5_NorthShore:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -0.6;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.2;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.8;
				break;
			case Const.G4_MAORI:
				growthRate = 0.2;
				break;
			}
			break;
		case Const.TA_6_Waitakere:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -0.8;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.6;
				break;
			case Const.G3_PACIFIC:
				growthRate = 2;
				break;
			case Const.G4_MAORI:
				growthRate = 0.9;
				break;
			}
			break;
		case Const.TA_7_Auckland:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -0.5;
				break;
			case Const.G2_ASIAN:
				growthRate = 1.6;
				break;
			case Const.G3_PACIFIC:
				growthRate = -0.8;
				break;
			case Const.G4_MAORI:
				growthRate = -0.3;
				break;
			}

			break;

		case Const.TA_8_Manukau:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -2;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.6;
				break;
			case Const.G3_PACIFIC:
				growthRate = 2;
				break;
			case Const.G4_MAORI:
				growthRate = 0.7;
				break;
			}
			break;

		case Const.TA_9_Papakura:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -0.8;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.2;
				break;
			case Const.G3_PACIFIC:
				growthRate = 4.3;
				break;
			case Const.G4_MAORI:
				growthRate = 1.1;
				break;
			}
			break;

		case Const.TA_10_Franklin:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.4;
				break;
			case Const.G2_ASIAN:
				growthRate = 0.9;
				break;
			case Const.G3_PACIFIC:
				growthRate = 3.3;
				break;
			case Const.G4_MAORI:
				growthRate = 0.7;
				break;
			}
			break;

		}

		return growthRate;
	}

	/**
	 *  This returns the growth rate projection (2006-21) of each ethnic group
	 *  in each TA based on census data projection (Med level).
	 */
	private double getCensusProjectionGrowth_Med(int taID, int groupID) {

		double growthRate = 0d;

		switch (taID) { 
		case Const.TA_4_Rodney:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.7;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.1;
				break;
			case Const.G3_PACIFIC:
				growthRate = 3.6;
				break;
			case Const.G4_MAORI:
				growthRate = 2.5;
				break;
			}

			break;
		case Const.TA_5_NorthShore:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.4;
				break;
			case Const.G2_ASIAN:
				growthRate = 3.9;
				break;
			case Const.G3_PACIFIC:
				growthRate = 1.8;
				break;
			case Const.G4_MAORI:
				growthRate = 1.4;
				break;
			}
			break;
		case Const.TA_6_Waitakere:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.2;
				break;
			case Const.G3_PACIFIC:
				growthRate = 2.9;
				break;
			case Const.G4_MAORI:
				growthRate = 1.9;
				break;
			}
			break;
		case Const.TA_7_Auckland:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.6;
				break;
			case Const.G2_ASIAN:
				growthRate = 3.3;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.6;
				break;
			case Const.G4_MAORI:
				growthRate = 0.8;
				break;
			}

			break;

		case Const.TA_8_Manukau:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -0.6;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.2;
				break;
			case Const.G3_PACIFIC:
				growthRate = 3.0;
				break;
			case Const.G4_MAORI:
				growthRate = 1.7;
				break;
			}
			break;

		case Const.TA_9_Papakura:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.0;
				break;
			case Const.G3_PACIFIC:
				growthRate = 5.3;
				break;
			case Const.G4_MAORI:
				growthRate = 2.3;
				break;
			}
			break;

		case Const.TA_10_Franklin:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 2.8;
				break;
			case Const.G3_PACIFIC:
				growthRate = 4.3;
				break;
			case Const.G4_MAORI:
				growthRate = 1.7;
				break;
			}
			break;

		}

		return growthRate;

	}


	/**
	 *  This returns the growth rate projection (2006-21) of each ethnic group
	 *  in each TA based on census data projection (High level).
	 *  
	 * @param taID  ID of the TA for which the projection rate is being sought
	 * @param groupID the ID of the ethnic group
	 * @return  higher rate of SNZ projection (for 2006-21)
	 */
	private double getCensusProjectionGrowthRate_High(int taID, int groupID) {

		double growthRate = 0d;

		switch (taID) { 
		case Const.TA_4_Rodney:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 2.5;
				break;
			case Const.G2_ASIAN:
				growthRate = 5.5;
				break;
			case Const.G3_PACIFIC:
				growthRate = 4.7;
				break;
			case Const.G4_MAORI:
				growthRate = 3.1;
				break;
			}

			break;
		case Const.TA_5_NorthShore:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 5.3;
				break;
			case Const.G3_PACIFIC:
				growthRate = 2.7;
				break;
			case Const.G4_MAORI:
				growthRate = 2.5;
				break;
			}
			break;
		case Const.TA_6_Waitakere:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 5.6;
				break;
			case Const.G3_PACIFIC:
				growthRate = 3.8;
				break;
			case Const.G4_MAORI:
				growthRate = 2.8;
				break;
			}
			break;
		case Const.TA_7_Auckland:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.5;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.8;
				break;
			case Const.G3_PACIFIC:
				growthRate = 1.8;
				break;
			case Const.G4_MAORI:
				growthRate = 1.9;
				break;
			}

			break;

		case Const.TA_8_Manukau:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.7;
				break;
			case Const.G2_ASIAN:
				growthRate = 5.6;
				break;
			case Const.G3_PACIFIC:
				growthRate = 3.8;
				break;
			case Const.G4_MAORI:
				growthRate = 2.6;
				break;
			}
			break;

		case Const.TA_9_Papakura:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 1.3;
				break;
			case Const.G2_ASIAN:
				growthRate = 5.5;
				break;
			case Const.G3_PACIFIC:
				growthRate = 6.1;
				break;
			case Const.G4_MAORI:
				growthRate = 3.3;
				break;
			}
			break;

		case Const.TA_10_Franklin:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 2.2;
				break;
			case Const.G2_ASIAN:
				growthRate = 4.4;
				break;
			case Const.G3_PACIFIC:
				growthRate = 5.3;
				break;
			case Const.G4_MAORI:
				growthRate = 2.6;
				break;
			}
			break;

		}

		return growthRate;
	}


	/**
	 *  This returns the growth rate projection (2006-21) of each ethnic group
	 *  in each TA based on census data projection (Med level).
	 */
	private double getCensusAvergeGrowthRatePerYear4TA4group(int taID, int groupID) {

		double growthRate = 0d;

		switch (taID) { 

		case Const.TA_5_NorthShore:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.85;
				break;
			case Const.G2_ASIAN:
				growthRate = 0.99;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.68;
				break;
			case Const.G4_MAORI:
				growthRate = 0.72;
			}
			break;
		case Const.TA_6_Waitakere:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.64;
				break;
			case Const.G2_ASIAN:
				growthRate = 0.93;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.85;
				break;
			case Const.G4_MAORI:
				growthRate = 0.78;
				break;
			}
			break;
		case Const.TA_7_Auckland:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = 0.79;
				break;
			case Const.G2_ASIAN:
				growthRate = 1.09;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.77;
				break;
			case Const.G4_MAORI:
				growthRate = 0.60;
				break;
			}

			break;

		case Const.TA_8_Manukau:
			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -2.69;
				break;
			case Const.G2_ASIAN:
				growthRate = 1.05;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.99;
				break;
			case Const.G4_MAORI:
				growthRate = 0.8;
				break;
			}
			break;

		case Const.TA_9_Papakura:

			switch (groupID) {
			case Const.G1_EURO:
				growthRate = -2.52;
				break;
			case Const.G2_ASIAN:
				growthRate = 0.66;
				break;
			case Const.G3_PACIFIC:
				growthRate = 0.63;
				break;
			case Const.G4_MAORI:
				growthRate = 0.70;
				break;
			}
			break;


		}

		return growthRate;

	}



	/**
	 *  This returns the exact number of population increase in each year
	 *  for each ethnic group based on time (for years 1991-2006).
	 *  Every five years, the exact number of increase are known from census.
	 *  This methods holds this number and returns them the this number divided by 5
	 *  (that is, for exact increase number for each year). It is called at each
	 *  timeTick equivalant to one year. 
	 *   
	 * @param groupID
	 * @param timeTick
	 * @return
	 */
	private int getCensusPopIncreaseForGroupIDAndTime(int groupID, int timeTick) {

		int nbOfIncrease = 0;

		switch (groupID) { 
		case Const.G1_EURO:
			if (timeTick <5) {
				nbOfIncrease = 0;
			}
			else if (timeTick<10) {
				nbOfIncrease = 0;

			}
			else if (timeTick <15) {
				nbOfIncrease = Math.round(14758/5); // ~ 2951

			}
			break;
		case Const.G2_ASIAN:
			if (timeTick <5) {
				nbOfIncrease = Math.round(42429/5); // ~ 8486
			}
			else if (timeTick<10) {
				nbOfIncrease = Math.round(45998/5); // ~ 9200

			}
			else if (timeTick <15) {
				nbOfIncrease = Math.round(72771/5); // ~ 14554

			}
			break;

		case Const.G3_PACIFIC:
			if (timeTick <5) {
				nbOfIncrease = Math.round(9628/5); // ~ 1926
			}
			else if (timeTick<10) {
				nbOfIncrease = Math.round(22462/5); // ~ 4492

			}
			else if (timeTick <15) {
				nbOfIncrease = Math.round(18546/5); // ~ 3709

			}
			break;

		case Const.G4_MAORI:
			if (timeTick <5) {
				nbOfIncrease = Math.round(11766/5); // ~ 2353
			}
			else if (timeTick<10) {
				nbOfIncrease = Math.round(2136/5); // ~ 427

			}
			else if (timeTick <15) {
				nbOfIncrease = Math.round(5790/5); // ~ 1158

			}
			break;
		}

		return nbOfIncrease;

	}

}
