/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.base;

import java.awt.Color;

/**
 * Constant 'parameters' are declared here.
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class Const {	
	//*************MODEL************************
	
	public static boolean IS_DEBUG = false;
	public static boolean IS_VERBOSE = true;	

	public static boolean PrintPoolListSizeAtEachIteration = true;
	
	public static boolean IsWriteDebugOutputToFile = true;
	public static String BaseDebugOutputFileName = "DebugOutputFile";

	public static boolean IsWritePlacementStatToFile = true;
	public static String BasePlacementStatFileName = "PlacementSTAT_r";

	
	//************** Often Changed: ***************************
	
	public static boolean IS_RECORD_CSV_DATA = true;
	public static boolean IS_RECORD_CSV_DATA_ONLY_AT_T0 = false; 
	public static boolean IS_RECORD_CSV_DATA_AT_T0_ANYWAY = true; 

	public static final float DefaultRadiusSizeOfGroupPopIndicator = 0.004f;  // use this for realistic env. 
	//public static final float DefaultRadiusSizeOfGroupPopIndicator = 0.009f; //use this for artificial/grid env.
	
	public static final int SEARCH_LIMIT_FACTOR = 12;	//SLF 12
	public static final int CENSUS_DATA_INTRO_SPAN = 15;  // immigration/ vacancy introduced over 15, 30, ... ticks,
	
	//public static final double DEFAULT_X_AXIS_GRAPH_MAX_RANGE = 16; //31
	public static final double DEFAULT_X_AXIS_GRAPH_MAX_RANGE = 31;

	
    //public static final double MORAN_I_GRAPH_Y_AXIS_MIN_RANGE = 0.45;  
	public static final double MORAN_I_GRAPH_Y_AXIS_MIN_RANGE = -1;
	//public static final double MORAN_I_GRAPH_Y_AXIS_MAX_RANGE = 0.85; 
	public static final double MORAN_I_GRAPH_Y_AXIS_MAX_RANGE = 1; 	

	//public static final double D_GRAPH_Y_AXIS_MIN_RANGE = 0.2;  
	public static final double D_GRAPH_Y_AXIS_MIN_RANGE = 0;  
	//public static final double D_GRAPH_Y_AXIS_MAX_RANGE = 0.7;  
	public static final double D_GRAPH_Y_AXIS_MAX_RANGE = 1;  

	//public static final double H_GRAPH_Y_AXIS_MIN_RANGE = 0.14;  
	public static final double H_GRAPH_Y_AXIS_MIN_RANGE = 0;  
	//public static final double H_GRAPH_Y_AXIS_MAX_RANGE = 0.4; //0.18;  
	public static final double H_GRAPH_Y_AXIS_MAX_RANGE = 1;  
	
	public static final int MinVacancyNumber = 50; 
	public static boolean IS_INFLOW_BASED_ON_CENSUS = true;
	public static final boolean IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM = true;		
	public static final double INFLOW_CENSUS_BASED_VACANCY_RATE_FOR_ALL = 10.0;
	public static final double INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION = 0.80;
	public static final boolean IS_IMMIGRANT_USE_TA_PREFERENCE = true;	
	
	//public static SNZ_GROWTH_PROJ SNZ_GROWTH_PROJ_CASE = SNZ_GROWTH_PROJ.LOW;
	public static SNZ_GROWTH_PROJ SNZ_GROWTH_PROJ_CASE = SNZ_GROWTH_PROJ.MED;
	//public static SNZ_GROWTH_PROJ SNZ_GROWTH_PROJ_CASE = SNZ_GROWTH_PROJ.HIGH;


	//public static final double POP_TURNOVER_PROPABILITY_PRECISION = 0.99;
	//public static final double GLOBAL_POP_TURNOVER_PROPABILITY_PRECISION = 0.99;	
	//public static final double P_PERSIST = 0.50;

	public static boolean IS_BATCH_MODE = true;
	public static String BatchModeFilenamePath= "D:\\BMA\\PhD\\HAAMoS_input_Shapesfiles\\5TA_316AU_WGS.shp";
	//public static String BatchModeFilenamePath= "D:\\BMA\\PhD\\HAAMoS_input_Shapesfiles\\7Dist_361_WGS.shp";

	//public static String BatchModeFilenamePath= "D:\\BMA\\PhD\\Thesis_Sim\\Fossett\\New\\Babak_grid4x4.shp";

  //public static String BatchModeFilenamePath= "D:\\BMA\\PhD\\Thesis_Sim\\Fossett\\New\\Babak_grid17x17.shp";

  // public static String BatchModeFilenamePath= "D:\\BMA\\PhD\\Thesis_Sim\\Schelling\\New\\Babak_grid17x17.shp";
	
	
	/**
	 * Realtor (as mechanism) is in charge of placing a moving agents.
	 * The ethnic proportion (percentage) of the neighbourhood  (immediate adjacent are unit) is 
	 * calculated and compared with moving agent proportion preference if the following constant is 
	 * set to true. 
	 * If this will be set to false, then the ethnic proportion (%) of each individual potential
	 * area unit in the neighbourhood is used (and not all of them combined). 
	 */
	public static boolean Relator_UseNighbourhoodEthnicPC = true;
	public static double Realtor_SD_RandomCoEthnicEstimation = 0.07;
	
	public static final int DefaultSnapshotPerTickInterval = 30;
	//public static final int DefaultSnapshotPerTickInterval = 10;

	public static boolean WorldDisplayMode = true;
	
	public static boolean OutputFile_DoMeasurmentForDistrictTAs =true; //control measuring TAs
	public static boolean OutputFile_DoMeasurmentForH =true; 
	public static boolean OutputFile_DoMeasurmentForD =true;
	public static boolean OutputFile_DoMeasurmentForII =true;  // control measuring Isolation Index for output file
	public static boolean OutputFile_DoMeasurmentForMI =true; //control measuring Moran's I for output file
	public static boolean OutputFile_DoMeasurmentForPopSize = true;
	public static boolean OutputFile_DoMeasurmentForVacacnySize = true;
	public static boolean OutputFile_DoMeasurmentForVacacnyProportion = true;
	public static boolean OutputFile_DoMeasurmentForPoolSize = false;
	public static boolean OutputFile_DoMeasurmentForMajorityCountSize = true;

	public static boolean OutputFile_DoMeasurmentForLMI =false; //control measuring Local Moran's I for output file
	public static boolean OutputFile_DoMeasurmentForLQ =false; //control measuring Location Quotient for output file
	
	public static boolean DisplayGraph_DoMeasurmentForII =true;  // control measuring Isolation Index for displaying graph
	public static boolean DisplayGraph_DoMeasurmentForMI =true; //control measuring Moran's I for displaying graph
	public static boolean DisplayGraph_Vacancy =true; 
	public static boolean DisplayGraph_VacancyProportion =true; 
	public static boolean DisplayGraph_MajCount =true;
	public static boolean DisplayGraph_PoolSize = false;
	public static boolean DisplayGraph_H_censusSeries =true;
	public static boolean DisplayGraph_Legend =false;
	
	// if proportion of pop size in AU (instead of pop size) is used, following should be TRUE, & vis-versa
	public static boolean IS_MoranI_USE_PROPORTION = true;
	
	//****************************************************
	
	public static final int NbOfEthnicGroupByDefault = 2;
	public static final int MaxNbOfGroupsHandeledByModel = 4;
	
	public static final int VacacnyPercentageByDefault = 10;
	
	
	// Constants (unless major change to the model): 

	
	// Default Values
	//public static final int SizeOfRegionsByDefault = 12;   //i.e. 2x2
	//public static final int MaxSizeOfRegions = 18;   //i.e. 18x18
	public static final double MaxHouseholdsPerRegionByDefault = 2000;
	//public static final int MinorityPercentageByDefault = 20;
	//public static final int MaxNbOfEthnicGroupByDefault = 4;
	//public static final int PercentageOfTurnoverByDefault = 0;
	
	
	public static final int TG2_ProportionbyDefaultforGroup1 = 50;
	public static final int TG2_ProportionbyDefaultforGroup2 = 50;	
	
	public static final int TG3_ProportionbyDefaultforGroup1 = 60;
	public static final int TG3_ProportionbyDefaultforGroup2 = 20;	
	public static final int TG3_ProportionbyDefaultforGroup3 = 20;	
	
	public static final int TG4_ProportionbyDefaultforGroup1 = 40;
	public static final int TG4_ProportionbyDefaultforGroup2 = 20;	
	public static final int TG4_ProportionbyDefaultforGroup3 = 20;	
	public static final int TG4_ProportionbyDefaultforGroup4 = 20;	
	
	public static final double TolerancePrefByDefaultforGroup1 =  0.80; //g (0): minority // Euro
	public static final double TolerancePrefByDefaultforGroup2 =  0.20; //g2(1): majority  //Asian
	public static final double TolerancePrefByDefaultforGroup3 =  0.10; //Pacific
	public static final double TolerancePrefByDefaultforGroup4 =  0.10; //Maori
	
	public static final double TurnoverPbyDefaultforGroup1 = 0.10;
	public static final double TurnoverPbyDefaultforGroup2 = 0.10;
	public static final double TurnoverPbyDefaultforGroup3 = 0.10;
	public static final double TurnoverPbyDefaultforGroup4 = 0.10;
	
	public static final double AnyLocPrefPbyDefaultforGroup1 = 0.10;
	public static final double AnyLocPrefPbyDefaultforGroup2 = 1.00;
	public static final double AnyLocPrefPbyDefaultforGroup3 = 0.10;
	public static final double AnyLocPrefPbyDefaultforGroup4 = 0.10;
	
	public static final double P_Persist_Default_G1 = 0.50;
	public static final double P_Persist_Default_G2 = 0.50;
	public static final double P_Persist_Default_G3 = 0.50;
	public static final double P_Persist_Default_G4 = 0.50;	
		
	public static final double FlowInImmByDefault = 0;
	public static final double FlowOutEmiByDefault = 0;

	public static final double FlowInImmPbyDefaultforGroup1 = 0.09;
	public static final double FlowInImmPbyDefaultforGroup2 = 0.99; 
	public static final double FlowInImmPbyDefaultforGroup3 = 0.32;
	public static final double FlowInImmPbyDefaultforGroup4 = 0.12;

	public static final double FlowOutEmiPbyDefaultforGroup1 =  0.50;
	public static final double FlowOutEmiPbyDefaultforGroup2 = 0.50;
	public static final double FlowOutEmiPbyDefaultforGroup3 = 0.13;
	public static final double FlowOutEmiPbyDefaultforGroup4 = 0.14;
	
	//public static final double VicinityPbyDefault = 0.9999999;
	public static final int ClusteringPbyDefault = 0;
	public static final int IsolationPbyDefault = 0;
	public static final int UnevenessPbyDefault = 0;
	
	public static final boolean CreateRandomlyOnDefault = false;
	public static final boolean BetterLocSearchPrefOnDefault = false;
		
	//GeoSpace call to on GisArealUnit.setGroupPop(gID, pop) to set the population of each ethnic group
	//chaning the order of these following constant G# requires changing the GeoSpace section respectively 
	public static final int G1_EURO = 0;
	public static final int G2_ASIAN = 1;
	public static final int G3_PACIFIC = 2;
	public static final int G4_MAORI = 3;
	public static final int G_Vacant = -1;
	public static final int G_NoMaj = -2;
	
	public static final int YEAR_1991 = 1;
	public static final int YEAR_1996 = 2;
	public static final int YEAR_2001 = 3;
	public static final int YEAR_2006 = 4;
	
	public static final String sYEAR_1991 = "91";
	public static final String sYEAR_1996 = "96";
	public static final String sYEAR_2001 = "01";
	public static final String sYEAR_2006 = "06";
	
	public static final Color G1_EURO_Color = Color.BLUE;
	public static final Color G2_ASIAN_Color = Color.RED;
	public static final Color G3_PACIFIC_Color = Color.GREEN;
	public static final Color G4_MAORI_Color = Color.GRAY;
	public static final Color G_Vacancy_Color = Color.WHITE;
	public static final Color G_NoMajority_Color = Color.PINK;
	
	public static final int TA_All = -1;
	public static final int TA_4_Rodney = 4;      //District
	public static final int TA_5_NorthShore = 5;
	public static final int TA_6_Waitakere = 6;
	public static final int TA_7_Auckland = 7;
	public static final int TA_8_Manukau = 8;
	public static final int TA_9_Papakura = 9;    //District
	public static final int TA_10_Franklin = 10;  //District
	
	public static final int FIVE_TA_INDEX_1NorthShore = 0;
	public static final int FIVE_TA_INDEX_2Waitakere = 1;
	public static final int FIVE_TA_INDEX_3Auckland = 2;
	public static final int FIVE_TA_INDEX_4Manukau = 3;
	public static final int FIVE_TA_INDEX_5Papakura = 4;   
	
	
	public static enum SNZ_GROWTH_PROJ {
		LOW, MED, HIGH, 
	}
	

			
	public static final int VON_NEUMANN = 0;
	public static final int MOORE = 1;
	
	public static final int RANDOM = 0;
	public static final int NORTH = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final int EAST = 4;
	public static final int CENTER = 5;
	public static final int CORNER_NE = 6;
	public static final int CORNER_NW = 7;
	public static final int CORNER_SE = 8;
	public static final int CORNER_SW = 9;
	
	
	public static final int MooreNeighborhoodSize = 8;
	public static final int VonNeumannNeighborhoodSize = 4;
	
	public static int RangeOfColorShades = 63;

	/*public final static int UNIFORM = 0;
	public final static int POISSON = 1;
	public final static int POISSON_SLOW = 2;
	public final static int BIONOMIAL = 3;
	public final static int NORMAL = 4;
	public final static int LOGARITHMIC = 5;  */
	
	
	//***************Preferences****************
	
	//public static final String PREF_LOC = "location";
	//public static final String PREF_TOL = "tolerance";
	
	 //number of cycles (period) that agent looks for suitable place, after which it settles wherever 
	//public static final String PREF_LOC_SPAN_BEFORE_SETTLING = "span_before_settling";
	//public static final String PREF_LOC_MAXIMIZER_SEARCH_LIMIT = "loc_maximizer+search_limit";
	
	//public static final int PREF_LOC_ANYWHERE = 0;
	
	//public static final int PREF_LOC_NEIGHBOR = 1;
	
	//public static final int PREF_TOLERENCE = 1;
	
	public static enum LOC_PREF {
		LOCAL, GLOBAL, 
	}

}
