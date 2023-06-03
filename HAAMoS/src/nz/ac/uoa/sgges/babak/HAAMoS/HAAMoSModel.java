/**
 *  <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */

package nz.ac.uoa.sgges.babak.HAAMoS;


import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.auckland.compass.haamos.io.CSV_FileWriter;
import nz.ac.auckland.compass.haamos.io.ShapefileFilterAndChooser;
import nz.ac.auckland.compass.haamos.repast3extension.gis.display.ExtendedOMLayer;
import nz.ac.auckland.compass.haamos.repast3extension.gis.display.ExtendedOpenMapDisplay;
import nz.ac.auckland.compass.haamos.repast3extension.util.ExtendedRangePropertyDescriptor;
import nz.ac.auckland.compass.haamos.repast3extension.util.TextFieldPropertyDescriptor;
import nz.ac.auckland.compass.haamos.space.AreaUnit;
import nz.ac.auckland.compass.haamos.space.GeoSpace;
import nz.ac.auckland.compass.haamos.space.GroupPopInfo;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.analysis.NumericDataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Legend;

import uchicago.src.sim.util.Random;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.IController;
import uchicago.src.sim.gui.DisplayConstants;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.ActionGroup;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.reflector.BooleanPropertyDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Integer;
import uchicago.src.sim.util.ProbeUtilities;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.event.MouseInputAdapter;

import anl.repast.gis.OpenMapAgent;

import com.bbn.openmap.gui.OpenMapFrame;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

import flanagan.math.PsRandom; // from Flanagan
import umontreal.iro.lecuyer.probdist.*;  //From SSJ  - iro.umontreal
import jode.expr.ThisOperator;
import jsim.variate.*;  //from JSIM  - uGeorgia
import monarc.dist.LogNormalDistribution;  //Monarc Caltech

import cern.colt.Arrays;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import cern.jet.random.Binomial;

import java.util.Comparator;


/**
 * <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 *  
 * <em>HAAMoS</em> is <em>Hybrid Aggregate Agent-based Microsimulation of Segregation</em> model.
 * <p>It has been developed using <em>Repast</em> libraries, as part of <em> Marsden Project (MoSC)</em> 
 * at <em> COMPASS Research Centre, University of Auckland </em>
 * and described in the following PhD thesis: 
 * <p><em> 'Using a Hybrid Model for Investigating Residential Segregation: An Empirical and Simulation-based Study' </em> 
 * 
 * <p>
 * <p> <code>HAAMoSModel</code> is the main class, extending SimModelImpl. 
 * In part, it {@link #buildModel()}, {@link #buildSchedule()} and {@link #buildDisplay()}
 * <p>For running the model in debug or verbose mode, set the constant parameters accordingly (@see IS_BEBUG & IS_VERBOSE in {@link Const} class)
 * 
 * @author Babak Mahdavi Ardestani
 * @version $Revision: 2.1    $Date: 2011/09/18
 * 
 */
public class HAAMoSModel extends SimModelImpl {

	static HAAMoSModel model;
	
	JFileChooser fileChooser;
	public boolean isBatch = Const.IS_BATCH_MODE;
	public boolean isWorldViewDisplayed = Const.WorldDisplayMode;
	
	public static String shapefileSourceFile = null;
	public static String neighbourhoodGALSourceFile = null;
	
	ExtendedOpenMapDisplay omDisplay;
	public int startYear = Const.YEAR_1991;

	private Legend userLegend;
	ExtendedOMLayer omLayer1;
	ExtendedOMLayer omLayer2;

	
	public static boolean debug = Const.IS_DEBUG;
	
	public boolean createRandomlyOn = Const.CreateRandomlyOnDefault;
	
	
	private boolean noMajorityExposureOn = false;  
	protected boolean readFromShapefileOn = true;
	
	public boolean betterLocSearchPrefOn = Const.BetterLocSearchPrefOnDefault;

	public int nType = Const.MOORE;
	public int orientation = Const.RANDOM;
	
	ExtendedRangePropertyDescriptor pdVacancyPC;
	ListPropertyDescriptor pdOrientation;
	RangePropertyDescriptor pdMaxPopPerAU;
	ExtendedRangePropertyDescriptor pdClusteringP;
	ExtendedRangePropertyDescriptor pdIsolationP;
	ExtendedRangePropertyDescriptor pdUnevenessP;
	ExtendedRangePropertyDescriptor pdFlowInImm;
	TextFieldPropertyDescriptor pdProportionG1;
	TextFieldPropertyDescriptor pdProportionG2;
	TextFieldPropertyDescriptor pdProportionG3;
	TextFieldPropertyDescriptor pdProportionG4;
	TextFieldPropertyDescriptor pdAnyLocPrefPG3;

	TextFieldPropertyDescriptor pdTurnoverG3;
	TextFieldPropertyDescriptor pdTurnoverG4;
	
	TextFieldPropertyDescriptor pdTolerancePrefG3;
	TextFieldPropertyDescriptor pdAnyLocPrefPG4;
	TextFieldPropertyDescriptor pdTolerancePrefG4;
	TextFieldPropertyDescriptor pdSnapshotInterval;
	BooleanPropertyDescriptor pdNoMajorityExposureOn;
	BooleanPropertyDescriptor pdReadFromShapefileOn;

	private GeoSpace geoSpace;
	public Schedule schedule;

	public double maxPopPerAU = Const.MaxHouseholdsPerRegionByDefault;
	public double flowInImm = Const.FlowInImmByDefault;
	public double flowOutEmi = Const.FlowOutEmiByDefault;

	public int vacancyPC = Const.VacacnyPercentageByDefault;  //percentage of empty by default

	public int numHappyAgents;
	public OpenSequenceGraph happinessGraph; 
	boolean displayHappiness = true;

	public int totalNumberOfAUs;
	public int nbOfEthnicGroups = Const.NbOfEthnicGroupByDefault;

	public int proportionG1 = 0;
	public int proportionG2 = 0;
	public int proportionG3 = 0;
	public int proportionG4 = 0;
	
	protected double turnoverG1 = Const.TurnoverPbyDefaultforGroup1;
	protected double turnoverG2 = Const.TurnoverPbyDefaultforGroup2;
	protected double turnoverG3 = Const.TurnoverPbyDefaultforGroup3;
	protected double turnoverG4 = Const.TurnoverPbyDefaultforGroup4;

	protected double anyLocPrefPG1 = Const.AnyLocPrefPbyDefaultforGroup1;
	protected double anyLocPrefPG2 = Const.AnyLocPrefPbyDefaultforGroup2;
	protected double anyLocPrefPG3 = Const.AnyLocPrefPbyDefaultforGroup3;
	protected double anyLocPrefPG4 = Const.AnyLocPrefPbyDefaultforGroup4;
	
	protected double tolerancePrefG1 = Const.TolerancePrefByDefaultforGroup1;
	protected double tolerancePrefG2 = Const.TolerancePrefByDefaultforGroup2;
	protected double tolerancePrefG3 = Const.TolerancePrefByDefaultforGroup3;
	protected double tolerancePrefG4 = Const.TolerancePrefByDefaultforGroup4;	

	protected double p_persistG1 = Const.P_Persist_Default_G1;
	protected double p_persistG2 = Const.P_Persist_Default_G2;
	protected double p_persistG3 = Const.P_Persist_Default_G3;
	protected double p_persistG4 = Const.P_Persist_Default_G4;
		
	public int clusteringP = Const.ClusteringPbyDefault;
	public int isolationP = Const.IsolationPbyDefault;
	public int unevenessP = Const.UnevenessPbyDefault;
	
	public Vector flowInImmP = new Vector();
	public Vector flowOutEmiP = new Vector();
	public double maxP = 1;

	private OpenSequenceGraph  dissimilarityGraph; 
	boolean displayDissimilarityGraph = true;
	
	private OpenSequenceGraph  moranIGraph; 
	
	private OpenSequenceGraph  hGraph; 
	boolean displayHGraph = true;

	private OpenSequenceGraph  vacGraph; 
	boolean displayVacGraph = Const.DisplayGraph_Vacancy;

	private OpenSequenceGraph  majCountGraph; 
	boolean displayMajCountGraph = Const.DisplayGraph_MajCount;
	
	private OpenSequenceGraph  isolationIndexGraph; 
	
	private OpenSequenceGraph  poolSizeGraph; 
	boolean displayPoolSizeGraph = Const.DisplayGraph_PoolSize;
	
	private OpenSequenceGraph  vacPropGraph; 
    boolean displayVacPropGraph = Const.DisplayGraph_VacancyProportion;
	
	protected Controller controller; //temp
	static public IController iController = null;
	protected DataRecorder dataRecorder;
	
	protected int stopAtTick = -1;
	private int snapshotInterval = Const.DefaultSnapshotPerTickInterval;
	
	private OpenHistogram minorityDistHistogram; //temp
	DoubleArrayList minPopList; //temp

	
	public class RegionPop {
		int minPop;
		int majPop;
		public RegionPop (int min, int maj){
			minPop = min;
			majPop = maj;
		}
	}
	
	public class RegionPopComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			RegionPop rp1 = (RegionPop) o1;
			RegionPop rp2 = (RegionPop) o2;
			int res =0;
			
			if (rp1.minPop < rp2.minPop)
				res = 1;
			else if (rp1.minPop == rp2.minPop)
				res = 0;
			else if (rp1.minPop > rp2.minPop)  //order Ascendantly
				res = -1;
			return res;
		}
		public boolean 	equals(Object obj) {
	
			System.out.println("Comparator.equals is called");
		    
		    int res = this.compare(this, obj);
		    if (res == 0)
		    	return true;
		    else return false;
		}
	}

	class D_G1vG2_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All, Const.G1_EURO,Const.G2_ASIAN);
			return res;
		}
	} 
	
	class D_G1vG3_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G3_PACIFIC);
			return res;
		}
	} 
	
	class D_G1vG4_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G4_MAORI);
			return res;
		}
	} 
	
	class D_G2vG3_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN,Const.G3_PACIFIC);
			return res;
		}
	} 
	class D_G2vG4_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN,Const.G4_MAORI);
			return res;
		}
	} 
	class D_G3vG4_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getDindex(Const.TA_All,Const.G3_PACIFIC,Const.G4_MAORI);
			return res;
		}
	} 
		
	class MI_G1_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G1_EURO);
			return res;
		}
	} 
	
	class MI_G2_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G2_ASIAN);
			return res;
		}
	} 
	
	class MI_G3_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G3_PACIFIC);
			return res;
		}
	} 
	
	class MI_G4_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G4_MAORI);
			return res;
		}
	} 
	
	class H_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_All);
			return res;
		}
	} 
	class H_All_Census_Seq implements Sequence {
		public double getSValue() {
			double timeTick = getController().getCurrentTime();
			double res=0;
			if (timeTick <= Const.CENSUS_DATA_INTRO_SPAN )
			   res = geoSpace.getH_census(Const.TA_All, Const.CENSUS_DATA_INTRO_SPAN );
			else res = geoSpace.getH(Const.TA_All);
			return res;
		}
	} 
	class H_04Rodney_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_4_Rodney);
			return res;
		}
	} 
	class H_05NorthShore_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_5_NorthShore);
			return res;
		}
	} 
	class H_06Waitakere_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_6_Waitakere);
			return res;
		}
	} 
	class H_07Auckland_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_7_Auckland);
			return res;
		}
	} 
	class H_08Manukau_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_8_Manukau);
			return res;
		}
	} 
	class H_09Papkura_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_9_Papakura);
			return res;
		}
	} 
	class H_10Franklin_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getH(Const.TA_10_Franklin);
			return res;
		}
	} 
		
	class IsolationIndexSequence implements Sequence {
		public double getSValue() {
			double res = geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G1_EURO); 
			return res;
		}
	}  
	
	class II_G1_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G1_EURO); 
			return res;
		}
	}  
	
	class II_G2_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G2_ASIAN); 
			return res;
		}
	}  
	
	class II_G3_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G3_PACIFIC); 
			return res;
		}
	}  
	class II_G4_All_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI, Const.G4_MAORI); 
			return res;
		}
	}  
	
	class PoolListSize_Seq implements Sequence {
		public double getSValue() {
			double res = geoSpace.poolList.size();
			return res;
		}
	} 
	
	
	class D_G1vG2_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_All,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	
	class D_G1vG2_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_4_Rodney,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	
	class D_G1vG2_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_5_NorthShore,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	
	class D_G1vG2_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_6_Waitakere,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class D_G1vG2_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class D_G1vG2_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_8_Manukau,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class D_G1vG2_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_9_Papakura,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class D_G1vG2_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class D_G1vG3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G1vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class D_G2vG3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class D_G2vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class D_G3vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getDindex(Const.TA_10_Franklin,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	//-------------------------------------
	
	class II_G1_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	
	class II_G1vG2_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G2vG1_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	
	class II_G2vG3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	
	class II_G3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G3vG1_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	
	class II_G3vG4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class II_G4_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	
	class II_G4vG1_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_All_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	
	class II_G1_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	
	class II_G1vG2_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2vG1_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	
	class II_G2vG3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	
	class II_G3vG4_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class II_G4vG1_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	
	class II_G4vG3_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_4_Rodney,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	class II_G1vG2_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2vG1_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	class II_G2vG3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	
	class II_G3vG4_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	class II_G4vG1_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_5_NorthShore,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	
	class II_G1vG2_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2vG1_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	class II_G2vG3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	class II_G3vG4_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	class II_G4vG1_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_6_Waitakere,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	class II_G1vG2_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2vG1_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	class II_G2vG3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	class II_G3vG4_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	class II_G4vG1_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	
	class II_G1vG2_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	class II_G2vG1_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	
	
	class II_G2vG3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	
	
	class II_G3vG4_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	
	class II_G4vG1_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_8_Manukau,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	

	class II_G1_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	
	class II_G1vG2_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	class II_G2vG1_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	
	class II_G2vG3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	class II_G3vG4_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	class II_G4vG1_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_9_Papakura,Const.G4_MAORI, Const.G4_MAORI);
	    }
	  }
	
	
	class II_G1_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G1_EURO,Const.G1_EURO);
	    }
	  }
	class II_G2_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G2_ASIAN,Const.G2_ASIAN);
	    }
	  }
	
	class II_G3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G3_PACIFIC,Const.G3_PACIFIC);
	    }
	  }
	class II_G4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G4_MAORI,Const.G4_MAORI);
	    }
	  }
	class II_G1vG2_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G1_EURO,Const.G2_ASIAN);
	    }
	  }
	
	class II_G1vG3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G1_EURO, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G1vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G1_EURO, Const.G4_MAORI);
	    }
	  }
	
	class II_G2vG1_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G2_ASIAN, Const.G1_EURO);
	    }
	  }
	
	class II_G2vG3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G2_ASIAN, Const.G3_PACIFIC);
	    }
	  }
	
	class II_G2vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G2_ASIAN, Const.G4_MAORI);
	    }
	  }
	class II_G3vG1_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G3_PACIFIC, Const.G1_EURO);
	    }
	  }
	class II_G3vG2_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G3_PACIFIC, Const.G2_ASIAN);
	    }
	  }
	class II_G3vG4_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G3_PACIFIC, Const.G4_MAORI);
	    }
	  }
	class II_G4vG1_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G4_MAORI, Const.G1_EURO);
	    }
	  }
	class II_G4vG2_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G4_MAORI, Const.G2_ASIAN);
	    }
	  }
	class II_G4vG3_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getIsolationIndex(Const.TA_10_Franklin,Const.G4_MAORI, Const.G3_PACIFIC);
	    }
	  }
	
	
	class MI_G1_All_NDS implements NumericDataSource {
				
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_All, Const.G1_EURO);			
		}
	}
	class MI_G2_All_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_All, Const.G2_ASIAN);
		}
	}

	class MI_G3_All_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G3_PACIFIC);
		}
	}

	class MI_G4_All_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G4_MAORI);
		}
	}
	
	class MI_G1_04Rodney_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_4_Rodney, Const.G1_EURO);
		}
	}
	class MI_G2_04Rodney_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_4_Rodney, Const.G2_ASIAN);
		}
	}

	class MI_G3_04Rodney_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_4_Rodney,Const.G3_PACIFIC);
		}
	}

	class MI_G4_04Rodney_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_4_Rodney,Const.G4_MAORI);
		}
	}
	
	class MI_G1_05NorthShore_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_5_NorthShore, Const.G1_EURO);
		}
	}
	class MI_G2_05NorthShore_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_5_NorthShore, Const.G2_ASIAN);
		}
	}

	class MI_G3_05NorthShore_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_5_NorthShore,Const.G3_PACIFIC);
		}
	}

	class MI_G4_05NorthShore_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_5_NorthShore,Const.G4_MAORI);
		}
	}
	
	class MI_G1_06Waitakere_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_6_Waitakere, Const.G1_EURO);
		}
	}
	class MI_G2_06Waitakere_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_6_Waitakere, Const.G2_ASIAN);
		}
	}

	class MI_G3_06Waitakere_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_6_Waitakere,Const.G3_PACIFIC);
		}
	}

	class MI_G4_06Waitakere_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_6_Waitakere,Const.G4_MAORI);
		}
	}
	
	class MI_G1_07Auckland_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_7_Auckland, Const.G1_EURO);
		}
	}
	class MI_G2_07Auckland_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_7_Auckland, Const.G2_ASIAN);
		}
	}

	class MI_G3_07Auckland_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_7_Auckland,Const.G3_PACIFIC);
		}
	}

	class MI_G4_07Auckland_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_7_Auckland,Const.G4_MAORI);
		}
	}
	
	class MI_G1_08Manukau_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_8_Manukau, Const.G1_EURO);
		}
	}
	class MI_G2_08Manukau_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_8_Manukau, Const.G2_ASIAN);
		}
	}

	class MI_G3_08Manukau_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_8_Manukau,Const.G3_PACIFIC);
		}
	}

	class MI_G4_08Manukau_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_8_Manukau,Const.G4_MAORI);
		}
	}
	
	class MI_G1_09Papakura_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_9_Papakura, Const.G1_EURO);
		}
	}
	class MI_G2_09Papakura_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_9_Papakura, Const.G2_ASIAN);
		}
	}

	class MI_G3_09Papakura_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_9_Papakura,Const.G3_PACIFIC);
		}
	}

	class MI_G4_09Papakura_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_9_Papakura,Const.G4_MAORI);
		}
	}
	
	class MI_G1_10Franklin_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_10_Franklin, Const.G1_EURO);
		}
	}
	class MI_G2_10Franklin_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_10_Franklin, Const.G2_ASIAN);
		}
	}

	class MI_G3_10Franklin_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_10_Franklin,Const.G3_PACIFIC);
		}
	}

	class MI_G4_10Franklin_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getGMIandCalculateLMI(Const.TA_10_Franklin,Const.G4_MAORI);
		}
	}

	class H_All_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_All);
	    	//System.out.println("H: "+res);
	      return res;
	    }
	  }
	class H_04Rodney_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_4_Rodney);
	    	//System.out.println("H: "+res);
	      return res;
	    }
	  }
	class H_05NorthShore_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_5_NorthShore);
	    	//System.out.println("H: "+res);
	      return res;
	    }
	  }
	class H_06Waitakere_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_6_Waitakere);
	      return res;
	    }
	  }
	class H_07Auckland_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_7_Auckland);
	      return res;
	    }
	  }
	class H_08Manukau_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_8_Manukau);
	      return res;
	    }
	  }
	
	class H_09Papakura_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_9_Papakura);
	      return res;
	    }
	  }
	class H_10Franklin_NDS implements NumericDataSource {
	    public double execute() {
	    	double res = geoSpace.getH(Const.TA_10_Franklin);
	      return res;
	    }
	  }
	
	class Isolation_index_G2_NumericDataSource implements NumericDataSource {
	    public double execute() {
	      return geoSpace.getSW_IsolationIndex(Const.TA_All, Const.G2_ASIAN);  //enter min index
	    }
	  }
	
	class RngSeed_NDS implements NumericDataSource {
	    public double execute() {
	      return controller.getRandomSeed();
	    }
	  }
	
	
	
	class PopSize_All implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_All);
		}
	}
	
	class MajCount_G1_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G1_EURO);
		}
	}
	class MajCount_G2_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G4_MAORI);
			
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_All, Const.G_NoMaj);
		}
	}
	
	
	class MajCount_G1_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G1_EURO);
		}
	}
	class MajCount_G2_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G4_MAORI);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_5_NorthShore, Const.G_NoMaj);
		}
	}
	
	
	
	class MajCount_G1_06Wtakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G1_EURO);
		}
	}
	class MajCount_G2_06Wtakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_06Wtakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_06Wtakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G4_MAORI);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_06Wtakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_6_Waitakere, Const.G_NoMaj);
		}
	}
	
	
	class MajCount_G1_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G1_EURO);
		}
	}
	class MajCount_G2_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G4_MAORI);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_7_Auckland, Const.G_NoMaj);
		}
	}
	
	
	class MajCount_G1_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G1_EURO);
		}
	}
	class MajCount_G2_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G4_MAORI);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_8_Manukau, Const.G_NoMaj);
		}
	}
	
	class MajCount_G1_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G1_EURO);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G1_EURO);
		}
	}
	class MajCount_G2_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G2_ASIAN);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G2_ASIAN);
		}
	}
	class MajCount_G3_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G3_PACIFIC);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G3_PACIFIC);
		}
	}
	
	class MajCount_G4_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G4_MAORI);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G4_MAORI);
		}
	}
	
	class MajCount_None_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G_NoMaj);
		}
		public double getSValue() {
		  return geoSpace.getNbOfAUWhereEthnicGroupIsMajority(Const.TA_9_Papakura, Const.G_NoMaj);
		}
	}
		
	
	class VacantSize_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_All);
		}
		public double getSValue() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_All);
		}
	}
	
	class PopSize_05NorthShore implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_5_NorthShore);
		}
	}
	class VacantSize_05NorthShore implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_5_NorthShore);
		}
	}
	

	class PopSize_06Waitakere implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_6_Waitakere);
		}
	}
	
	class VacantSize_06Waitakere implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_6_Waitakere);
		}
	}
	
	class PopSize_07Auckland implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_7_Auckland);
		}
	}
	class VacantSize_07Auckland implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_7_Auckland);
		}
	}
	
	class PopSize_08Manukau implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_8_Manukau);
		}
	}
	class VacantSize_08Manukau implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_8_Manukau);
		}
	}
	class PopSize_09Papakura implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldPop(Const.TA_9_Papakura);
		}
	}
	class VacantSize_09Papakura implements NumericDataSource {
		public double execute() {
			return geoSpace.getWorldVacantSpaceSize(Const.TA_9_Papakura);
		}
	}
	
	
	class PopSize_G1_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G1_EURO);
		}
	}
	class PopSize_G2_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_All,Const.G4_MAORI);
		}
	}
	
	class PopSize_G1_05NorthShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G1_EURO);
		}
	}
	class PopSize_G2_05NorthShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_05NorthShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_05NorthShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_5_NorthShore,Const.G4_MAORI);
		}
	}
	
	class PopSize_G1_06Waitakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G1_EURO);
		}
	}
	class PopSize_G2_06Waitakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_06Waitakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_06Waitakere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_6_Waitakere,Const.G4_MAORI);
		}
	}
	
	class PopSize_G1_07Auckland implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G1_EURO);
		}
	}
	class PopSize_G2_07Auckland implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_07Auckland implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_07Auckland implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_7_Auckland,Const.G4_MAORI);
		}
	}
	
	class PopSize_G1_08Manukau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G1_EURO);
		}
	}
	class PopSize_G2_08Manukau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_08Manukau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_08Manukau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_8_Manukau,Const.G4_MAORI);
		}
	}
	
	class PopSize_G1_09Papakura implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G1_EURO);
		}
	}
	class PopSize_G2_09Papakura implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G2_ASIAN);
		}
	}
	class PopSize_G3_09Papakura implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G3_PACIFIC);
		}
	}
	class PopSize_G4_09Papakura implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPopSiseForGroup(Const.TA_9_Papakura,Const.G4_MAORI);
		}
	}
	
	//-------------------POOL Sizes:
	class PoolListSize_NDS implements NumericDataSource {
		public double execute() {
			return geoSpace.getPoolListSize();
		}
	} 
	
	class PoolPopSize_G1 implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPoolPopSize4Group(Const.G1_EURO);
		}
		public double getSValue() {
			return geoSpace.getPoolPopSize4Group(Const.G1_EURO);
		}
	}
	class PoolPopSize_G2 implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPoolPopSize4Group(Const.G2_ASIAN);
		}
		public double getSValue() {
			return geoSpace.getPoolPopSize4Group(Const.G2_ASIAN);
		}
	}
	class PoolPopSize_G3 implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPoolPopSize4Group(Const.G3_PACIFIC);
		}
		public double getSValue() {
			return geoSpace.getPoolPopSize4Group(Const.G3_PACIFIC);
		}
	}
	class PoolPopSize_G4 implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getPoolPopSize4Group(Const.G4_MAORI);
		}
		public double getSValue() {
			return geoSpace.getPoolPopSize4Group(Const.G4_MAORI);

		}
		
	}
	
	class VacVsPopSizeProp_All implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_All);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_All);
		}
	}
	
	class VacVsPopSizeProp_05NShore implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_5_NorthShore);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_5_NorthShore);
		}
	}
	
	class VacVsPopSizeProp_06Wtkere implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_6_Waitakere);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_6_Waitakere);
		}
	}
	
	class VacVsPopSizeProp_07Akl implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_7_Auckland);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_7_Auckland);
		}
	}
	
	class VacVsPopSizeProp_08Mkau implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_8_Manukau);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_8_Manukau);
		}
	}
	
	class VacVsPopSizeProp_09Papak implements NumericDataSource, Sequence {
		public double execute() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_9_Papakura);
		}
		public double getSValue() {
			return geoSpace.getVacVsPopSizeProp(Const.TA_9_Papakura);
		}
	}
	
	
	class MinorityDistValueBinDataSource implements BinDataSource{  //temp
		public double getBinValue(Object o) {
			int index = (Integer)o;
			return (double)minPopList.get(index);
		}
	}
		
	protected MouseInputAdapter mouseAdapter = new MouseInputAdapter() {

		public void mouseClicked(MouseEvent evt) {
			
			System.out.println("mouse Clicked");
		}
		
	};
	public HAAMoSModel() {
		DisplayConstants.CELL_WIDTH = 40;
		DisplayConstants.CELL_HEIGHT = 20;
		DisplayConstants.CELL_DEPTH = 20;
	}

	public String getName(){
		return "HAAMoS";
	}
	
	public String[] getInitParam(){
		String[] initParams = {"ReadFromShapefileOn","StartYear","NType","NbOfEthnicGroups", "RegionSize", "MaxPopPerAU", "ProportionG1", "ProportionG2","ProportionG3","ProportionG4","TurnoverG1","TurnoverG2","TurnoverG3","TurnoverG4","AnyLocPrefPG1","AnyLocPrefPG2","AnyLocPrefPG3", "AnyLocPrefPG4", "TolerancePrefG1", "TolerancePrefG2",  "TolerancePrefG3", "TolerancePrefG4", "P_PersistG1", "P_PersistG2", "P_PersistG3", "P_PersistG4", "FlowInImm", "FlowInImmP","FlowOutEmi","FlowOutEmiP","BetterLocSearchPrefOn","CreateRandomlyOn", "NoMajorityExposureOn","ClusteringP","IsolationP","UnevenessP","Orientation","VacancyPercentage", "StopAtTick", "MeanTF", "SnapshotInterval"};

		return initParams;
	}

	public String getStartYearInString(){
		switch (this.startYear) { 
		case Const.YEAR_1991: 
			return Const.sYEAR_1991;
		case Const.YEAR_1996: 
			return Const.sYEAR_1996;
		case Const.YEAR_2001: 
			return Const.sYEAR_2001;
		case Const.YEAR_2006: 
			return Const.sYEAR_2006;
		}
		return null;
	}
	
	public String getTA_NameInStringForID(int taID){
		switch (taID) { 
		case Const.TA_All: 
			return "All";
		case Const.TA_4_Rodney: 
			return "Rodney";
		case Const.TA_5_NorthShore: 
			return "NorthShore";
		case Const.TA_6_Waitakere: 
			return "Waitakere";
		case Const.TA_7_Auckland: 
			return "Auckland";
		case Const.TA_8_Manukau: 
			return "Manukau";
		case Const.TA_9_Papakura: 
			return "Papakura";
		case Const.TA_10_Franklin: 
			return "Franklin";
		}
		return null;
	}

	public void setup(){
		if (Const.IS_VERBOSE)
			System.out.println("Running setup");
		
		System.gc();

		if (iController == null) {

			iController = (IController) getController();
			if (iController.isGUI()) {
				controller = (Controller) getController();
				JButton snapshotButton = new JButton("Snapshot");
				snapshotButton.setToolTipText("Takes a snapshot");
				snapshotButton.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						takeSnapshot();
					}
				});
				controller.addButton(snapshotButton);
			}
		}
		
		if(Const.IsWriteDebugOutputToFile)	{
			String debugOutputFile = Const.BaseDebugOutputFileName+"_r"+iController.getRunCount()+".txt";
				File file  = new File(debugOutputFile);		
				File backupFile = new File(debugOutputFile+"bak.txt");
				if(file.exists()) {
					// Rename file (or directory)
					boolean success = file.renameTo(backupFile);
				}
				
				PrintStream printStream;
				try {
					printStream = new PrintStream(new FileOutputStream(file));
					System.setOut(printStream);
					System.out.println("'isDebugOutputToFile' is true. If you do not want the output to be directed to file, set it to false");
					System.out.println("----------------------------------------------------------------------------------------------------");

				} catch (FileNotFoundException e) {
					System.err.println("Couldn't find file with name " + debugOutputFile);
					System.err.println("Therefore cannot re-direct System.out to this file ");
				}						    
		}
		

		if (geoSpace != null) {
			if (geoSpace.worldGisArealUnitAgentList != null) {
				geoSpace.worldGisArealUnitAgentList.clear();
				geoSpace.worldGisArealUnitAgentList = null;
			}

			if (geoSpace.poolList != null)	{	
				geoSpace.poolList.clear();
				geoSpace.poolList = null;
			}
			
			if (geoSpace.worldGisAUgroupPopInfoAgentList !=null) {
				geoSpace.worldGisAUgroupPopInfoAgentList.clear();
				geoSpace.worldGisAUgroupPopInfoAgentList =null;
			}
			
			if (geoSpace.gisOpenMapData != null)	{
				geoSpace.gisOpenMapData = null;
			}
						
			geoSpace = null;
		}

		if (schedule != null)
			schedule = null;
		
		schedule = new Schedule(1);
		
		if (dissimilarityGraph != null){
			dissimilarityGraph.dispose();
			dissimilarityGraph = null;
		}
		
		if (moranIGraph != null){
			moranIGraph.dispose();
			moranIGraph = null;
		}
		
		if (hGraph != null){
			hGraph.dispose();
			hGraph = null;
		}
		
		if (vacGraph != null){
			vacGraph.dispose();
			vacGraph = null;
		}
				
		if (vacPropGraph != null){
			vacPropGraph.dispose();
			vacPropGraph = null;
		}
		
		if (majCountGraph != null){
			majCountGraph.dispose();
			majCountGraph = null;
		}
		
		
		if (isolationIndexGraph != null){
			isolationIndexGraph.dispose();
			isolationIndexGraph = null; 
		}
		
		if (poolSizeGraph != null){
			poolSizeGraph.dispose();
			poolSizeGraph = null;
		}		

		Controller.ALPHA_ORDER = false;
				
		if (getController().getRunCount() == 1) {

			Hashtable h0 = new Hashtable();
			h0.put(new Integer(Const.YEAR_1991), "1991");
			h0.put(new Integer(Const.YEAR_1996), "1996");
			h0.put(new Integer(Const.YEAR_2001), "2001");
			h0.put(new Integer(Const.YEAR_2006), "2006");
			ListPropertyDescriptor pdStartYear = new ListPropertyDescriptor("startYear", h0);
			descriptors.put("StartYear", pdStartYear);


			Hashtable h1 = new Hashtable();
			h1.put(new Integer(Const.VON_NEUMANN), "VON NEUMANN");
			h1.put(new Integer(Const.MOORE), "MOORE");
			ListPropertyDescriptor pdNType = new ListPropertyDescriptor("NType", h1);
			descriptors.put("NType", pdNType);
			pdNType.getWidget().setEnabled(false);

			Hashtable h2 = new Hashtable();
			h2.put(new Integer(Const.RANDOM), "Random");
			h2.put(new Integer(Const.NORTH), "North");
			h2.put(new Integer(Const.SOUTH), "South");
			h2.put(new Integer(Const.WEST), "West");
			h2.put(new Integer(Const.EAST), "East");
			h2.put(new Integer(Const.CENTER), "Center");
			h2.put(new Integer(Const.CORNER_NE), "Corner_NE");
			h2.put(new Integer(Const.CORNER_NW), "Corner_NW");
			h2.put(new Integer(Const.CORNER_SE), "Corner_SE");
			h2.put(new Integer(Const.CORNER_SW), "Corner_SW");
			pdOrientation = new ListPropertyDescriptor("Orientation", h2);
			descriptors.put("Orientation", pdOrientation);
			if (!createRandomlyOn) 
				pdOrientation.getWidget().setEnabled(false);

			Vector v = new Vector();

			v.addElement(new Integer(2));
			v.addElement(new Integer(3));
			v.addElement(new Integer(4));

			ListPropertyDescriptor lpd = new ListPropertyDescriptor("NbOfEthnicGroups",v);
			descriptors.put("NbOfEthnicGroups", lpd);

			pdMaxPopPerAU = new RangePropertyDescriptor("MaxPopPerAU", (int)Const.MaxHouseholdsPerRegionByDefault, 5000, 1000);
			descriptors.put("MaxPopPerAU", pdMaxPopPerAU);
			if (readFromShapefileOn) 
				pdMaxPopPerAU.getWidget().setEnabled(false);

			pdFlowInImm = new ExtendedRangePropertyDescriptor("FlowInImm", (int)Const.FlowInImmByDefault, (int)maxPopPerAU/10, 50);
			descriptors.put("FlowInImm", pdFlowInImm);
			pdFlowInImm.getWidget().setToolTipText("Nb of in-Migration");

			ExtendedRangePropertyDescriptor pdFlowOutEmi = new ExtendedRangePropertyDescriptor("FlowOutEmi", (int)Const.FlowOutEmiByDefault, (int)maxPopPerAU/10, 50);
			descriptors.put("FlowOutEmi", pdFlowOutEmi);
			pdFlowOutEmi.getWidget().setToolTipText("Nb of out-Migration");

			pdVacancyPC = new ExtendedRangePropertyDescriptor("VacancyPercentage", 0, 95, 15);
			descriptors.put("VacancyPercentage", pdVacancyPC);
			pdVacancyPC.getWidget().setToolTipText("Vacancy percentage");

			pdClusteringP = new 	ExtendedRangePropertyDescriptor("ClusteringP", 0, 10, 1);
			descriptors.put("ClusteringP", pdClusteringP);
			pdClusteringP.getWidget().setToolTipText("Clustering Prob: choose 0 <--Low  High -->10 (All)");
			if (!createRandomlyOn) 
				pdClusteringP.getWidget().setEnabled(false);

			pdIsolationP = new 	ExtendedRangePropertyDescriptor("IsolationgP", 0, 10, 1);
			descriptors.put("IsolationP", pdIsolationP);
			pdIsolationP.getWidget().setToolTipText("Isolation Prob.: choose 0 <--Low  High -->10");
			if (!createRandomlyOn) 
				pdIsolationP.getWidget().setEnabled(false);

			pdUnevenessP = new 	ExtendedRangePropertyDescriptor("UnevenessP", 0, 10, 1);
			descriptors.put("UnevenessP", pdUnevenessP);
			pdUnevenessP.getWidget().setToolTipText("Uneveness Prob.: choose 0 <--Low  High -->10");
			if (!createRandomlyOn) 
				pdUnevenessP.getWidget().setEnabled(false);

			flowInImmP.add(new Double(Const.FlowInImmPbyDefaultforGroup1));
			flowInImmP.add(new Double(Const.FlowInImmPbyDefaultforGroup2));

			flowOutEmiP.add(new Double(Const.FlowOutEmiPbyDefaultforGroup1));
			flowOutEmiP.add(new Double(Const.FlowOutEmiPbyDefaultforGroup2));

			pdProportionG1 = new TextFieldPropertyDescriptor("ProportionG1");
			descriptors.put("ProportionG1", pdProportionG1);
			if (readFromShapefileOn) 
				pdProportionG1.getWidget().setEnabled(false);
			pdProportionG1.getWidget().setToolTipText("type the proportion of pop for group #1 (Euro)");

			pdProportionG2 = new TextFieldPropertyDescriptor("ProportionG2");
			descriptors.put("ProportionG2", pdProportionG2);
			if (readFromShapefileOn) 
				pdProportionG2.getWidget().setEnabled(false);
			pdProportionG2.getWidget().setToolTipText("type the proportion of pop for group #2 (Asian)");

			pdProportionG3 = new TextFieldPropertyDescriptor("ProportionG3");
			descriptors.put("ProportionG3", pdProportionG3);
			pdProportionG3.getWidget().setEnabled(false);
			pdProportionG3.getWidget().setToolTipText("type the proportion of pop for group #3 (Pacific)");

			pdProportionG4 = new TextFieldPropertyDescriptor("ProportionG4");
			descriptors.put("ProportionG4", pdProportionG4);
			pdProportionG4.getWidget().setEnabled(false);
			pdProportionG4.getWidget().setToolTipText("type the proportion of pop for group #4 (Maori)");

			pdAnyLocPrefPG3 = new TextFieldPropertyDescriptor("AnyLocPrefPG3");
			descriptors.put("AnyLocPrefPG3", pdAnyLocPrefPG3);
			pdAnyLocPrefPG3.getWidget().setEnabled(false);
			pdAnyLocPrefPG3.getWidget().setToolTipText("type Any Location Preference Probability for group #3 (Pacific)");

			pdAnyLocPrefPG4 = new TextFieldPropertyDescriptor("AnyLocPrefPG4");
			descriptors.put("AnyLocPrefPG4", pdAnyLocPrefPG4);
			pdAnyLocPrefPG4.getWidget().setEnabled(false);
			pdAnyLocPrefPG4.getWidget().setToolTipText("type Any Location Preference Probability for group #4 (Maori)");

			pdTolerancePrefG3 = new TextFieldPropertyDescriptor("TolerancePrefG3");
			descriptors.put("TolerancePrefG3", pdTolerancePrefG3);
			pdTolerancePrefG3.getWidget().setEnabled(false);
			pdTolerancePrefG3.getWidget().setToolTipText("type Tolerance Preference for group #3 (Pacific)");

			pdTurnoverG3 = new TextFieldPropertyDescriptor("TurnoverG3");
			descriptors.put("TurnoverG3", pdTurnoverG3);
			pdTurnoverG3.getWidget().setEnabled(false);
			pdTurnoverG3.getWidget().setToolTipText("type Turnover for group #3 (Pacific)");

			pdTurnoverG4 = new TextFieldPropertyDescriptor("TurnoverG4");
			descriptors.put("TurnoverG4", pdTurnoverG4);
			pdTurnoverG4.getWidget().setEnabled(false);
			pdTurnoverG4.getWidget().setToolTipText("type Turnover for group #4 (Maori)");

			pdTolerancePrefG4 = new TextFieldPropertyDescriptor("TolerancePrefG4");
			descriptors.put("TolerancePrefG4", pdTolerancePrefG4);
			pdTolerancePrefG4.getWidget().setEnabled(false);
			pdTolerancePrefG4.getWidget().setToolTipText("type Tolerance Preference for group #4 (Maori)");

			pdSnapshotInterval = new TextFieldPropertyDescriptor("SnapshotInterval");
			descriptors.put("SnapshotInterval", pdSnapshotInterval);
			pdSnapshotInterval.getWidget().setToolTipText("type snapshot interval per tick");

			pdNoMajorityExposureOn = new BooleanPropertyDescriptor("NoMajorityExposureOn", false);
			descriptors.put("NoMajorityExposureOn", pdNoMajorityExposureOn);
			pdNoMajorityExposureOn.getWidget().setEnabled(false);

			pdReadFromShapefileOn = new BooleanPropertyDescriptor("ReadFromShapefileOn", false);
			descriptors.put("ReadFromShapefileOn", pdReadFromShapefileOn);

			switch (nbOfEthnicGroups) { 

			case 2: 
				proportionG1=Const.TG2_ProportionbyDefaultforGroup1;
				proportionG2= Const.TG2_ProportionbyDefaultforGroup2;		
				break;
			case 3: 
				proportionG1=Const.TG3_ProportionbyDefaultforGroup1;
				proportionG2= Const.TG3_ProportionbyDefaultforGroup2;	
				proportionG3= Const.TG3_ProportionbyDefaultforGroup3;		
				break;
			case 4:  
				proportionG1=Const.TG4_ProportionbyDefaultforGroup1;
				proportionG2= Const.TG4_ProportionbyDefaultforGroup2;	
				proportionG3= Const.TG4_ProportionbyDefaultforGroup3;	
				proportionG4= Const.TG4_ProportionbyDefaultforGroup4;	

				break;
			}

			modelManipulator.addButton( "About this model", new ActionListener() {
				public void actionPerformed ( ActionEvent evt ) {
					System.out.println("About this model");
				}
			} );

			modelManipulator.addButton( "Kill HAAMoS! (Exit)", new ActionListener() {
				public void actionPerformed ( ActionEvent evt ) {
					System.exit(0);
				}
			} ); 

		}
		
		if (displayDissimilarityGraph) {
			dissimilarityGraph = new OpenSequenceGraph("Index of Dissimilarity (D)", this);
			dissimilarityGraph.setYRange(Const.D_GRAPH_Y_AXIS_MIN_RANGE, Const.D_GRAPH_Y_AXIS_MAX_RANGE);
			
			dissimilarityGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			dissimilarityGraph.setAxisTitles("Tick/Time/Year", "Value");
			dissimilarityGraph.addSequence("D(g1:g2)", new D_G1vG2_All_Seq());
			if (Const.IS_BATCH_MODE) {
				if (nbOfEthnicGroups>2) {
					dissimilarityGraph.addSequence("D(g1:g3)", new D_G1vG3_All_Seq());
					dissimilarityGraph.addSequence("D(g2:g3)", new D_G2vG3_All_Seq());

				}
				if (nbOfEthnicGroups>3) {
					dissimilarityGraph.addSequence("D(g1:g4)", new D_G1vG4_All_Seq());
					dissimilarityGraph.addSequence("D(g2:g4)", new D_G2vG4_All_Seq());
					dissimilarityGraph.addSequence("D(g3:g4)", new D_G3vG4_All_Seq());
				}
			}
			dissimilarityGraph.display();
		}
		
		if (Const.DisplayGraph_DoMeasurmentForMI) {
			moranIGraph = new OpenSequenceGraph("Moran's I", this);	
			moranIGraph.setYRange(Const.MORAN_I_GRAPH_Y_AXIS_MIN_RANGE, Const.MORAN_I_GRAPH_Y_AXIS_MAX_RANGE);
			moranIGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			moranIGraph.setAxisTitles("Tick/Time/Year", "Value");
			moranIGraph.addSequence("Moran's I (g1)", new MI_G1_All_Seq());
			moranIGraph.addSequence("Moran's I (g2)", new MI_G2_All_Seq());
		
			if (Const.IS_BATCH_MODE) {
				if (nbOfEthnicGroups>2)
					moranIGraph.addSequence("Moran's I (g3)", new MI_G3_All_Seq());
				if (nbOfEthnicGroups>3)
					moranIGraph.addSequence("Moran's I (g4)", new MI_G4_All_Seq());
			}
			
			moranIGraph.display();
		}
		
		if (displayMajCountGraph) {
			majCountGraph = new OpenSequenceGraph("Maj Count (J)", this);			
			majCountGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			majCountGraph.setAxisTitles("Tick/Time/Year", "Value");
			majCountGraph.addSequence("Euro", new MajCount_G1_All());	
			majCountGraph.addSequence("Asian", new MajCount_G2_All());	
			majCountGraph.addSequence("Pac", new MajCount_G3_All());	
			majCountGraph.addSequence("Maori", new MajCount_G4_All());	
			majCountGraph.addSequence("None", new MajCount_None_All());	

			majCountGraph.display();
		}	
		
		if (displayHGraph) {
			hGraph = new OpenSequenceGraph("H", this);	
			hGraph.setYRange(Const.H_GRAPH_Y_AXIS_MIN_RANGE, Const.H_GRAPH_Y_AXIS_MAX_RANGE);

			hGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			hGraph.setYAutoExpand(false);
			hGraph.setAxisTitles("Tick/Time/Year", "Value");
			hGraph.addSequence("H ", new H_All_Seq());	
			if (Const.DisplayGraph_H_censusSeries)
				hGraph.addSequence("H census", new H_All_Census_Seq());		
			hGraph.display();
		}	
		
		if (displayVacGraph) {
			vacGraph = new OpenSequenceGraph("Vacancy total (v)", this);			
			vacGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			vacGraph.setAxisTitles("Tick/Time/Year", "Value");
			vacGraph.addSequence("Vac", new VacantSize_All());	
			vacGraph.display();
		}	
		
		if (Const.DisplayGraph_DoMeasurmentForII) {
			isolationIndexGraph = new OpenSequenceGraph("Isolation Index", this);	
			isolationIndexGraph.setYRange(0.0, 1.0);
			isolationIndexGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			isolationIndexGraph.setAxisTitles("Tick/Time/Year", "Value");
			isolationIndexGraph.addSequence("II (g1)", new II_G1_All_Seq());
			isolationIndexGraph.addSequence("II (g2)", new II_G2_All_Seq());	
			
			if (Const.IS_BATCH_MODE) {
				if (nbOfEthnicGroups>2)
					isolationIndexGraph.addSequence("II (g3)", new II_G3_All_Seq());
				if (nbOfEthnicGroups>3)
					isolationIndexGraph.addSequence("II (g4)", new II_G4_All_Seq());	
			}
			
			isolationIndexGraph.display();
		}  
		
		if (displayPoolSizeGraph) {
			poolSizeGraph = new OpenSequenceGraph("PoolSize Graph (W)", this);
			poolSizeGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			poolSizeGraph.setYRange(0, this.maxPopPerAU/10);
			poolSizeGraph.setAxisTitles("Tick/Time/Year", "Value");
			poolSizeGraph.addSequence("W", new PoolListSize_Seq());
			
			if (Const.IS_BATCH_MODE) {
				poolSizeGraph.addSequence("W_G1", new PoolPopSize_G1());
				poolSizeGraph.addSequence("W_G2", new PoolPopSize_G2());
				if (nbOfEthnicGroups>2)
					poolSizeGraph.addSequence("W_G3", new PoolPopSize_G3());
				if (nbOfEthnicGroups>3)
					poolSizeGraph.addSequence("W_G4", new PoolPopSize_G4());
			}
			
			poolSizeGraph.display();
		}
		
		if (displayVacPropGraph) {
			vacPropGraph = new OpenSequenceGraph("VacProp Graph", this);
			vacPropGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
			vacPropGraph.setYRange(0, 0.10);
			vacPropGraph.setAxisTitles("Tick/Time/Year", "Value");
			vacPropGraph.addSequence("vacProp_all", new VacVsPopSizeProp_All());
			vacPropGraph.addSequence("vacProp_NShor", new VacVsPopSizeProp_05NShore());
			vacPropGraph.addSequence("vacProp_Wtkere", new VacVsPopSizeProp_06Wtkere());
			vacPropGraph.addSequence("vacProp_Akl", new VacVsPopSizeProp_07Akl());
			vacPropGraph.addSequence("vacProp_Mkau", new VacVsPopSizeProp_08Mkau());
			vacPropGraph.addSequence("vacProp_Papak", new VacVsPopSizeProp_09Papak());
	
			vacPropGraph.display();
		}


	} //end of setup


	private void printMeasuresAtTime0() {
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(9);
				
		CSV_FileWriter fw = new CSV_FileWriter("measureValsAtTime0_.csv", false);

		switch (this.nbOfEthnicGroups) { 
		case 2: 
			System.out.println("D (All, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_All, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Rodney, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_4_Rodney, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (NorthShore, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_5_NorthShore, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Waitakere, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_6_Waitakere, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Auckland, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_7_Auckland, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Manukau, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_8_Manukau, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Papakaura, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_9_Papakura, Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Franklin, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_10_Franklin, Const.G1_EURO,Const.G2_ASIAN));

			System.out.println("H: all: "+ geoSpace.getH(Const.TA_All));
			System.out.println("H: Rodney: "+ geoSpace.getH(Const.TA_4_Rodney));
			System.out.println("H: NorthShore: "+ geoSpace.getH(Const.TA_5_NorthShore));
			System.out.println("H: Waitakere: "+ geoSpace.getH(Const.TA_6_Waitakere));
			System.out.println("H: Auckland: "+ geoSpace.getH(Const.TA_7_Auckland));
			System.out.println("H: Manukau: "+ geoSpace.getH(Const.TA_8_Manukau));
			System.out.println("H: Papakaura: "+ geoSpace.getH(Const.TA_9_Papakura));
			System.out.println("H: Franklin: "+ geoSpace.getH(Const.TA_10_Franklin));
			
			System.out.println("MI (All, Euro): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G1_EURO));
			System.out.println("MI (All, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G2_ASIAN));
			System.out.println("II (All, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_All, Const.G1_EURO));
			System.out.println("II (All, Asian): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G2_ASIAN));		
		break;
		case 3: 
			System.out.println("D (All, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (All, g1-Euro, g3-Pacific): "+geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G3_PACIFIC));
			System.out.println("D (All, g2-Asian, g3-Pacific): "+geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN,Const.G3_PACIFIC));

			System.out.println("H: all: "+ geoSpace.getH(Const.TA_All));
			System.out.println("H: Rodney: "+ geoSpace.getH(Const.TA_4_Rodney));
			System.out.println("H: NorthShore: "+ geoSpace.getH(Const.TA_5_NorthShore));
			System.out.println("H: Waitakere: "+ geoSpace.getH(Const.TA_6_Waitakere));
			System.out.println("H: Auckland: "+ geoSpace.getH(Const.TA_7_Auckland));
			System.out.println("H: Manukau: "+ geoSpace.getH(Const.TA_8_Manukau));
			System.out.println("H: Papakaura: "+ geoSpace.getH(Const.TA_9_Papakura));
			System.out.println("H: Franklin: "+ geoSpace.getH(Const.TA_10_Franklin));
			
			System.out.println("MI (All, Euro): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G1_EURO));
			System.out.println("MI (All, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G2_ASIAN));
			System.out.println("MI (All, Pacific): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G3_PACIFIC));
			System.out.println("II (All, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G1_EURO));
			System.out.println("II (All, Asian): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G2_ASIAN));
			System.out.println("II (All, Pacific): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G3_PACIFIC));
			break;
		case 4: 
			System.out.println("D (All, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (All, g1-Euro, g3-Pacific): "+geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G3_PACIFIC));
			System.out.println("D (All, g1-Euro, g4-Maori): "+geoSpace.getDindex(Const.TA_All,Const.G1_EURO,Const.G4_MAORI));
			System.out.println("D (All, g2-Asian, g3-Pacific): "+geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN,Const.G3_PACIFIC));
			System.out.println("D (All, g2-Asian, g4-Maori): "+geoSpace.getDindex(Const.TA_All,Const.G2_ASIAN,Const.G4_MAORI));
			System.out.println("D (All, g4-Maori, g3-Pacific): "+geoSpace.getDindex(Const.TA_All,Const.G4_MAORI,Const.G3_PACIFIC));
			
			System.out.println("D (Aukl, g1-Euro, g2-Asian): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO,Const.G2_ASIAN));
			System.out.println("D (Aukl, g1-Euro, g3-Pacific): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO,Const.G3_PACIFIC));
			System.out.println("D (Aukl, g1-Euro, g4-Maori): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G1_EURO,Const.G4_MAORI));
			System.out.println("D (Aukl, g2-Asian, g3-Pacific): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G2_ASIAN,Const.G3_PACIFIC));
			System.out.println("D (Aukl, g2-Asian, g4-Maori): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G2_ASIAN,Const.G4_MAORI));
			System.out.println("D (Aukl, g4-Maori, g3-Pacific): "+geoSpace.getDindex(Const.TA_7_Auckland,Const.G4_MAORI,Const.G3_PACIFIC));

			System.out.println("H: all: "+ geoSpace.getH(Const.TA_All));
			System.out.println("H: Rodney: "+ geoSpace.getH(Const.TA_4_Rodney));
			System.out.println("H: NorthShore: "+ geoSpace.getH(Const.TA_5_NorthShore));
			System.out.println("H: Waitakere: "+ geoSpace.getH(Const.TA_6_Waitakere));
			System.out.println("H: Auckland: "+ geoSpace.getH(Const.TA_7_Auckland));
			System.out.println("H: Manukau: "+ geoSpace.getH(Const.TA_8_Manukau));
			System.out.println("H: Papakaura: "+ geoSpace.getH(Const.TA_9_Papakura));
			System.out.println("H: Franklin: "+ geoSpace.getH(Const.TA_10_Franklin));
			
			System.out.println("MI (All, Euro): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G1_EURO));
			System.out.println("MI (All, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G2_ASIAN));
			System.out.println("MI (All, Pacific): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G3_PACIFIC));
			System.out.println("MI (All, Maori): "+geoSpace.getGMIandCalculateLMI(Const.TA_All,Const.G4_MAORI));
			System.out.println("MI (Auckland, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_7_Auckland,Const.G2_ASIAN));
			System.out.println("MI (Manukau, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_8_Manukau,Const.G2_ASIAN));
			System.out.println("MI (NorthShore, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_5_NorthShore,Const.G2_ASIAN));
			System.out.println("MI (Rodney, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_4_Rodney,Const.G2_ASIAN));
			System.out.println("MI (Franklin, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_10_Franklin,Const.G2_ASIAN));
			System.out.println("MI (Papakaura, Asian): "+geoSpace.getGMIandCalculateLMI(Const.TA_9_Papakura,Const.G2_ASIAN));
			
			System.out.println("II (All, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G1_EURO));
			System.out.println("II (All, Asian): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G2_ASIAN));
			System.out.println("II (All, Pacific): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G3_PACIFIC));
			System.out.println("II (All, Maori): "+geoSpace.getSW_IsolationIndex(Const.TA_All,Const.G4_MAORI));
			
			System.out.println("II (Rodney, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_4_Rodney,Const.G1_EURO));
			System.out.println("II (NorthShore, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_5_NorthShore,Const.G1_EURO));
			System.out.println("II (Waitekere, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_6_Waitakere,Const.G1_EURO));
			System.out.println("II (Auckland, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_7_Auckland,Const.G1_EURO));
			System.out.println("II (Manukau, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_8_Manukau,Const.G1_EURO));
			System.out.println("II (Papakaura, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_9_Papakura,Const.G1_EURO));
			System.out.println("II (Franklin, Euro): "+geoSpace.getSW_IsolationIndex(Const.TA_10_Franklin,Const.G1_EURO));
			
			System.out.println("II B (All, Euro, Euro): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G1_EURO));
			System.out.println("II B (All, Euro, Asian): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G2_ASIAN));
			System.out.println("II B (All, Euro, Pacific): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G3_PACIFIC));
			System.out.println("II B (All, Euro, Maori): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G1_EURO, Const.G4_MAORI));
			System.out.println("II B (All, Asian, Asian): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G2_ASIAN));
			System.out.println("II B (All, Asian, Pacific): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G3_PACIFIC));
			System.out.println("II B (All, Asian, Maori): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G2_ASIAN, Const.G4_MAORI));
			System.out.println("II B (All, Pacific, Pacific): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G3_PACIFIC));
			System.out.println("II B (All, Pacific, Maori): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G3_PACIFIC, Const.G4_MAORI));
			System.out.println("II B (All, Maori, Maori): "+geoSpace.getIsolationIndex(Const.TA_All,Const.G4_MAORI, Const.G4_MAORI));
			
			System.out.println("II B (Acukl, Euro, Euro): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G1_EURO));
			System.out.println("II B (Acukl, Euro, Asian): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G2_ASIAN));
			System.out.println("II B (Acukl, Euro, Pacific): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G3_PACIFIC));
			System.out.println("II B (Acukl, Euro, Maori): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G1_EURO, Const.G4_MAORI));
			System.out.println("II B (Acukl, Asian, Asian): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G2_ASIAN));
			System.out.println("II B (Acukl, Asian, Pacific): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G3_PACIFIC));
			System.out.println("II B (Acukl, Asian, Maori): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G2_ASIAN, Const.G4_MAORI));
			System.out.println("II B (Acukl, Pacific, Pacific): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G3_PACIFIC));
			System.out.println("II B (Acukl, Pacific, Maori): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G3_PACIFIC, Const.G4_MAORI));
			System.out.println("II B (Acukl, Maori, Maori): "+geoSpace.getIsolationIndex(Const.TA_7_Auckland,Const.G4_MAORI, Const.G4_MAORI));	
			break;			
		}
		
		fw.close();
	}
	
	private boolean chooseShapeFile(){
		boolean isFileChosen = false;

		fileChooser = new JFileChooser();

		fileChooser.setFileFilter(new ShapefileFilterAndChooser());

		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		int returnVal = fileChooser.showOpenDialog(new OpenMapFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			shapefileSourceFile= fileChooser.getSelectedFile().getAbsolutePath();
			System.out.println("Shapefile opened:  "+shapefileSourceFile);
		}
		else {
			System.out.println("Could not open file.  Does it exist?");
			shapefileSourceFile=null;
		}
		if (shapefileSourceFile!=null){
			String neigbourhoodInFile;
			neighbourhoodGALSourceFile = shapefileSourceFile;
			neighbourhoodGALSourceFile = neighbourhoodGALSourceFile.substring(0, neighbourhoodGALSourceFile.length()-3) + "GAL";
			System.out.println("GAL file expected: " + neighbourhoodGALSourceFile);
			isFileChosen= true;
		}
		return isFileChosen;
	}

	
	private int estimateNbOfRegionsToRecieveMinorityByIsolationP(){
		int nbOfRegionsToRecieve = 0;

		switch (isolationP) { 

		case 10: 
			nbOfRegionsToRecieve = 1; // concenterated in one region (if possible)
			break;
		case 0: 
			nbOfRegionsToRecieve = this.totalNumberOfAUs;  // in All region, no isolation
			break;
		default:  //casees 1-9
			double p = (10-isolationP)/10.0;
		    nbOfRegionsToRecieve = Random.binomial.nextInt( this.totalNumberOfAUs, p);
		    break;
		}

		return nbOfRegionsToRecieve;

	}
	
	private double getConvertedUnevenessP(){
		double p = 0;

		switch (unevenessP) { 

		case 10: 
			p = 0.9999; // concenterated in one region (if possible)
			break;
		case 0: 
			p = 0.0999;  // 
			break;
		default:  //casees 1-9
			p = unevenessP/10.0;
		    break;
		}
		return p;
	}
	
	private void displayMinorityDistHistogram (DoubleArrayList minPopList){  //temp
		if (minorityDistHistogram != null){
			minorityDistHistogram.dispose();
	    }
		minorityDistHistogram = null;
		
		ArrayList indexList = new ArrayList();
		
		for (int i=0; i< minPopList.size(); i++){
			indexList.add(new Integer(i));
		}
		System.out.println();  
		
		minorityDistHistogram = new OpenHistogram("Minority Distr.", minPopList.size(), (int)Descriptive.min(minPopList));
		minorityDistHistogram.display();
		minorityDistHistogram.createHistogramItem("Minority Distr.",indexList,new MinorityDistValueBinDataSource());
		minorityDistHistogram.step();
	
	}


	private void setHHsColorMaps() {
		
		ColorMap blueColorMap = new ColorMap();
		for (int i = 1; i < Const.RangeOfColorShades; i++){
			blueColorMap.mapColor(i, 0, 0, i/63.0);
		}
		AreaUnit.setColorMap(0,blueColorMap);
		
		ColorMap redColorMap = new ColorMap();
		for (int i = 1; i < Const.RangeOfColorShades; i++){
			redColorMap.mapColor(i, i/63.0, 0, 0);
		}
		AreaUnit.setColorMap(1,redColorMap);

		ColorMap greenColorMap = new ColorMap();
		for (int i = 1; i < Const.RangeOfColorShades; i++){
			greenColorMap.mapColor(i, 0, i/63.0, 0);
		}
		AreaUnit.setColorMap(2,greenColorMap);
		
		ColorMap aColorMap = new ColorMap();
		for (int i = 1; i < Const.RangeOfColorShades; i++){
			 aColorMap.mapColor(i, 	ColorMap.black);
		}
		
		AreaUnit.setColorMap(3,aColorMap);

	}

	
	public Schedule getSchedule(){
		return schedule;
	}

	public void step() {
		System.out.println("HAAMoSModel: Step - super.step()");
	}

	public boolean getReadFromShapefileOn() {
		return readFromShapefileOn;
	}

	public void setReadFromShapefileOn(boolean readOn) {
		readFromShapefileOn = readOn;
		
		switch (this.nbOfEthnicGroups) { 
		case 2: 
			if (readOn) {
				pdProportionG1.getWidget().setEnabled(false);
				pdProportionG2.getWidget().setEnabled(false);
			}else {
				pdProportionG1.getWidget().setEnabled(true);
				pdProportionG2.getWidget().setEnabled(true);
				
			}
			break;
		case 3: 
			if (readOn) {
				pdProportionG1.getWidget().setEnabled(false);
				pdProportionG2.getWidget().setEnabled(false);
				pdProportionG3.getWidget().setEnabled(false);
			}else {
				pdProportionG1.getWidget().setEnabled(true);
				pdProportionG2.getWidget().setEnabled(true);
				pdProportionG3.getWidget().setEnabled(true);
				
			}
			break;
		case 4: 
			if (readOn) {
				pdProportionG1.getWidget().setEnabled(false);
				pdProportionG2.getWidget().setEnabled(false);
				pdProportionG3.getWidget().setEnabled(false);
				pdProportionG4.getWidget().setEnabled(false);
			}else {
				pdProportionG1.getWidget().setEnabled(true);
				pdProportionG2.getWidget().setEnabled(true);
				pdProportionG3.getWidget().setEnabled(true);
				pdProportionG4.getWidget().setEnabled(true);
			}
			break;	
         } //end of switch
			
		if (readOn) 
			this.pdMaxPopPerAU.getWidget().setEnabled(false);
		else 
			pdMaxPopPerAU.getWidget().setEnabled(true);			
	} 
	
	public boolean getBetterLocSearchPrefOn() {
		return betterLocSearchPrefOn;
	}

	public void setBetterLocSearchPrefOn(boolean betterOn) {
		betterLocSearchPrefOn = betterOn;
		
	} 

	public boolean getCreateRandomlyOn() {
		return createRandomlyOn;
	}

	public void setCreateRandomlyOn(boolean randOn) {
		createRandomlyOn = randOn;
				
		if (randOn==false)
			pdClusteringP.getWidget().setEnabled(false);
		else pdClusteringP.getWidget().setEnabled(true);
		
		if (randOn==false)
			pdIsolationP.getWidget().setEnabled(false);
		else pdIsolationP.getWidget().setEnabled(true);
		
		if (randOn==false)
			pdUnevenessP.getWidget().setEnabled(false);
		else pdUnevenessP.getWidget().setEnabled(true);
		
		if (randOn==false)
			pdOrientation.getWidget().setEnabled(false);
		else pdOrientation.getWidget().setEnabled(true);
		
		if (randOn==false)
			pdNoMajorityExposureOn.getWidget().setEnabled(false);
		else pdNoMajorityExposureOn.getWidget().setEnabled(true);
		
		
	} 


	private void setModelBasedOnGroupNb(int newNb) {
		switch (newNb) { 
		case 2:  
			
			pdProportionG3.getWidget().setEnabled(false);	
			pdAnyLocPrefPG3.getWidget().setEnabled(false);
			pdTurnoverG3.getWidget().setEnabled(false);
			pdTolerancePrefG3.getWidget().setEnabled(false);
			pdProportionG4.getWidget().setEnabled(false);		
			pdAnyLocPrefPG4.getWidget().setEnabled(false);
			pdTurnoverG4.getWidget().setEnabled(false);
			pdTolerancePrefG4.getWidget().setEnabled(false);
			
			flowInImmP.clear();
			flowInImmP.add(new Double(0.5));
			flowInImmP.add(new Double(0.5));
			
			flowOutEmiP.clear();
			flowOutEmiP.add(new Double(0.5));
			flowOutEmiP.add(new Double(0.5));
			
			 proportionG1=Const.TG2_ProportionbyDefaultforGroup1;
			 proportionG2= Const.TG2_ProportionbyDefaultforGroup2;		
			
			if (displayDissimilarityGraph) {
				dissimilarityGraph.dispose();
				dissimilarityGraph = new OpenSequenceGraph("Index of Dissimilarity (D)", this);
				dissimilarityGraph.setYRange(Const.D_GRAPH_Y_AXIS_MIN_RANGE, Const.D_GRAPH_Y_AXIS_MAX_RANGE);
				dissimilarityGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				dissimilarityGraph.setAxisTitles("Tick/Time/Year", "Value");
				dissimilarityGraph.addSequence("D(g1:g2)", new D_G1vG2_All_Seq());
				dissimilarityGraph.display();
			}
			
			if (Const.DisplayGraph_DoMeasurmentForMI) {
				moranIGraph.dispose();
				moranIGraph = new OpenSequenceGraph("Moran's I", this);	
				moranIGraph.setYRange(Const.MORAN_I_GRAPH_Y_AXIS_MIN_RANGE, Const.MORAN_I_GRAPH_Y_AXIS_MAX_RANGE);
				moranIGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				moranIGraph.setAxisTitles("Tick/Time/Year", "Value");
				moranIGraph.addSequence("Moran's I (g1)", new MI_G1_All_Seq());		
				moranIGraph.addSequence("Moran's I (g2)", new MI_G2_All_Seq());				
				moranIGraph.display();
			}
			
			if (Const.DisplayGraph_DoMeasurmentForII) {
				isolationIndexGraph.dispose();
				isolationIndexGraph = new OpenSequenceGraph("Isolation Index", this);
				isolationIndexGraph.setYRange(0.0, 1.0);
				isolationIndexGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);

				isolationIndexGraph.setAxisTitles("Tick/Time/Year", "Value");
				isolationIndexGraph.addSequence("II (g1)", new II_G1_All_Seq());
				isolationIndexGraph.addSequence("II (g2)", new II_G2_All_Seq());			
				isolationIndexGraph.display();
			}  
			
		break;
		
		case 3:
			
			if (createRandomlyOn) 
				pdProportionG3.getWidget().setEnabled(true);	
			pdAnyLocPrefPG3.getWidget().setEnabled(true);
			pdTurnoverG3.getWidget().setEnabled(true);
			pdTolerancePrefG3.getWidget().setEnabled(true);
			pdProportionG4.getWidget().setEnabled(false);
			pdAnyLocPrefPG4.getWidget().setEnabled(false);
			pdTurnoverG4.getWidget().setEnabled(false);
			pdTolerancePrefG4.getWidget().setEnabled(false);
		
			
			flowInImmP.clear();
			flowInImmP.add(new Double(0.33)); 
			flowInImmP.add(new Double(0.33));
			flowInImmP.add(new Double(0.33));
			
			flowOutEmiP.clear();
			flowOutEmiP.add(new Double(0.33)); 
			flowOutEmiP.add(new Double(0.33));
			flowOutEmiP.add(new Double(0.33));
			
			proportionG1=Const.TG3_ProportionbyDefaultforGroup1;
			proportionG2= Const.TG3_ProportionbyDefaultforGroup2;	
			proportionG3= Const.TG3_ProportionbyDefaultforGroup3;		

			dissimilarityGraph.dispose();
			if (displayDissimilarityGraph) {
				dissimilarityGraph = new OpenSequenceGraph("Index of Dissimilarity (D)", this);
				dissimilarityGraph.setYRange(Const.D_GRAPH_Y_AXIS_MIN_RANGE, Const.D_GRAPH_Y_AXIS_MAX_RANGE);
				dissimilarityGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				dissimilarityGraph.setAxisTitles("Tick/Time/Year", "Value");
				dissimilarityGraph.addSequence("D(g1:g2)", new D_G1vG2_All_Seq());
				dissimilarityGraph.addSequence("D(g1:g3)", new D_G1vG3_All_Seq());
				dissimilarityGraph.addSequence("D(g2:g3)", new D_G2vG3_All_Seq());				
				dissimilarityGraph.display();
			}
			if (Const.DisplayGraph_DoMeasurmentForMI) {
				moranIGraph.dispose();
				moranIGraph = new OpenSequenceGraph("Moran's I", this);	
				moranIGraph.setYRange(Const.MORAN_I_GRAPH_Y_AXIS_MIN_RANGE, Const.MORAN_I_GRAPH_Y_AXIS_MAX_RANGE);
				moranIGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				moranIGraph.setAxisTitles("Tick/Time/Year", "Value");
				moranIGraph.addSequence("Moran's I (g1)", new MI_G1_All_Seq());		
				moranIGraph.addSequence("Moran's I (g2)", new MI_G2_All_Seq());		
				moranIGraph.addSequence("Moran's I (g3)", new MI_G3_All_Seq());		
				moranIGraph.display();
			}
			if (Const.DisplayGraph_DoMeasurmentForII) {

				isolationIndexGraph.dispose();
				isolationIndexGraph = new OpenSequenceGraph("Isolation Index", this);
				isolationIndexGraph.setYRange(0.0, 1.0);
				isolationIndexGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				isolationIndexGraph.setAxisTitles("Tick/Time/Year", "Value");
				//isolationIndexGraph.addSequence("Isolation Index", new IsolationIndexSequence());
				isolationIndexGraph.addSequence("II (g1)", new II_G1_All_Seq());
				isolationIndexGraph.addSequence("II (g2)", new II_G2_All_Seq());
				isolationIndexGraph.addSequence("II (g3)", new II_G3_All_Seq());			

				isolationIndexGraph.display();
			}  


		break;
		case 4:
			
			if (createRandomlyOn) { 
				pdProportionG3.getWidget().setEnabled(true); //??
				pdAnyLocPrefPG3.getWidget().setEnabled(true); //??
			}
			pdAnyLocPrefPG3.getWidget().setEnabled(true);
			pdTurnoverG3.getWidget().setEnabled(true);
			pdTolerancePrefG3.getWidget().setEnabled(true);
			//pdProportionG3.getWidget().setEnabled(true);

			pdAnyLocPrefPG4.getWidget().setEnabled(true);
			pdTurnoverG4.getWidget().setEnabled(true);
			pdTolerancePrefG4.getWidget().setEnabled(true);
			
			flowInImmP.clear();
			flowInImmP.add(new Double(0.25)); 
			flowInImmP.add(new Double(0.25));
			flowInImmP.add(new Double(0.25));
			flowInImmP.add(new Double(0.25));
			
			flowOutEmiP.clear();
			flowOutEmiP.add(new Double(0.25)); 
			flowOutEmiP.add(new Double(0.25));
			flowOutEmiP.add(new Double(0.25));
			flowOutEmiP.add(new Double(0.25));

			proportionG1=Const.TG4_ProportionbyDefaultforGroup1;
			proportionG2= Const.TG4_ProportionbyDefaultforGroup2;	
			proportionG3= Const.TG4_ProportionbyDefaultforGroup3;	
			proportionG4= Const.TG4_ProportionbyDefaultforGroup4;	

			
			dissimilarityGraph.dispose();
			if (displayDissimilarityGraph) {
				dissimilarityGraph = new OpenSequenceGraph("Index of Dissimilarity (D)", this);
				dissimilarityGraph.setYRange(Const.D_GRAPH_Y_AXIS_MIN_RANGE, Const.D_GRAPH_Y_AXIS_MAX_RANGE);
				dissimilarityGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
				dissimilarityGraph.setAxisTitles("Tick/Time/Year", "Value");
				dissimilarityGraph.addSequence("D(g1:g2)", new D_G1vG2_All_Seq());
				dissimilarityGraph.addSequence("D(g1:g3)", new D_G1vG3_All_Seq());
				dissimilarityGraph.addSequence("D(g2:g3)", new D_G2vG3_All_Seq());	
				dissimilarityGraph.addSequence("D(g1:g4)", new D_G1vG4_All_Seq());
				dissimilarityGraph.addSequence("D(g2:g4)", new D_G2vG4_All_Seq());		
				dissimilarityGraph.addSequence("D(g3:g4)", new D_G3vG4_All_Seq());		
				dissimilarityGraph.display();
			}
			if (Const.DisplayGraph_DoMeasurmentForMI){
				moranIGraph.dispose();
				if (Const.DisplayGraph_DoMeasurmentForMI) {
					moranIGraph = new OpenSequenceGraph("Moran's I", this);	
					moranIGraph.setYRange(Const.MORAN_I_GRAPH_Y_AXIS_MIN_RANGE, Const.MORAN_I_GRAPH_Y_AXIS_MAX_RANGE);
					moranIGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
					moranIGraph.setAxisTitles("Tick/Time/Year", "Value");
					moranIGraph.addSequence("Moran's I (g1)", new MI_G1_All_Seq());		
					moranIGraph.addSequence("Moran's I (g2)", new MI_G2_All_Seq());		
					moranIGraph.addSequence("Moran's I (g3)", new MI_G3_All_Seq());	
					moranIGraph.addSequence("Moran's I (g4)", new MI_G4_All_Seq());	
					moranIGraph.display();
				}
			}
			if (Const.DisplayGraph_DoMeasurmentForII){

				isolationIndexGraph.dispose();
				if (Const.DisplayGraph_DoMeasurmentForII) {
					isolationIndexGraph = new OpenSequenceGraph("Isolation Index", this);
					isolationIndexGraph.setYRange(0.0, 1.0);
					isolationIndexGraph.setXRange(0, Const.DEFAULT_X_AXIS_GRAPH_MAX_RANGE);
					isolationIndexGraph.setAxisTitles("Tick/Time/Year", "Value");
					//isolationIndexGraph.addSequence("Isolation Index", new IsolationIndexSequence());
					isolationIndexGraph.addSequence("II (g1)", new II_G1_All_Seq());
					isolationIndexGraph.addSequence("II (g2)", new II_G2_All_Seq());
					isolationIndexGraph.addSequence("II (g3)", new II_G3_All_Seq());	
					isolationIndexGraph.addSequence("II (g4)", new II_G4_All_Seq());			
					isolationIndexGraph.display();
				}  
			}

		break;
		}
		
		ProbeUtilities.updateModelProbePanel();
	}

	public void setNbOfEthnicGroups(int newNb) {
		nbOfEthnicGroups = newNb;
		setModelBasedOnGroupNb(newNb);
		setReadFromShapefileOn(this.readFromShapefileOn);
	}
	
	public int getNbOfEthnicGroups() {
		return nbOfEthnicGroups;
	}
	
	public void setNoMajorityExposureOn(boolean iOn) {
		noMajorityExposureOn = iOn;
	}
	
	public boolean getNoMajorityExposureOn() {
		return noMajorityExposureOn;
	}

	public void setStartYear(int y) {
		startYear = y;
	}

	public int getStartYear() {
		return startYear;
	}
	
	public void setNType(int type) {
		nType = type;
	}

	public int getNType() {
		return nType;
	}
	
	public void setOrientation(int d) {
		orientation = d;
	}

	public int getOrientation() {
		return orientation;
	}

	public int getMaxPopPerAU() {
		return (int)maxPopPerAU;
	}

	public void setMaxPopPerAU(int m) {
		this.maxPopPerAU = m;
	}

	public int getFlowInImm() {
		return (int)flowInImm;
	}

	public void setFlowInImm(int i) {
		this.flowInImm = i;
	}

	public int getFlowOutEmi() {
		return (int)flowOutEmi;
	}

	public void setFlowOutEmi(int i) {
		this.flowOutEmi = i;
	}
	
	
	public void setP_PersistG1(double val) {
		this.p_persistG1 = val;
	}
	public double getP_PersistG1() {
		return this.p_persistG1;
	}
	
	public void setP_PersistG2(double val) {
		this.p_persistG2 = val;
	}
	public double getP_PersistG2() {
		return this.p_persistG2;
	}	
	
	public void setP_PersistG3(double val) {
		this.p_persistG3 = val;
	}
	public double getP_PersistG3() {
		return this.p_persistG3;
	}
	
	public void setP_PersistG4(double val) {
		this.p_persistG4 = val;
	}
	public double getP_PersistG4() {
		return this.p_persistG4;
	}

	public int getVacancyPercentage() {
		return vacancyPC;
	}

	public void setVacancyPercentage(int vacProp) {
		this.vacancyPC = vacProp;
	}

	
	public int getClusteringP() {
		return clusteringP;
	}

	public void setClusteringP(int v) {
		this.clusteringP = v;
	}
	
	public int getIsolationP() {
		return isolationP;
	}

	public void setIsolationP(int v) {
		this.isolationP = v;
	}
	
	public int getUnevenessP() {
		return unevenessP;
	}

	public void setUnevenessP(int v) {
		this.unevenessP = v;
	}
		
	public long getRandomSeed() {
		return controller.getRandomSeed();
	}
	
	public void setProportionG1(int val) {
		proportionG1 = val;	
	}
	
	public int getProportionG1() {
		return proportionG1;
	}
	
	public void setProportionG2(int val) {
		proportionG2 = val;	
	}
	
	public int getProportionG2() {
		return proportionG2;
	}
	
	public void setProportionG3(int val) {
		proportionG3 = val;	
	}
	
	public int getProportionG3() {
		return proportionG3;
	}
	public void setProportionG4(int val) {
		proportionG4 = val;	
	}
	
	public int getProportionG4() {
		return proportionG4;
	}
	
	public void setAnyLocPrefPG1(double val) {
		anyLocPrefPG1 = val;
		
	}

	public double getAnyLocPrefPG1() {
		return anyLocPrefPG1;
	}
	
	public void setAnyLocPrefPG2(double val) {
		anyLocPrefPG2 = val;
		
	}

	public double getAnyLocPrefPG2() {
		return anyLocPrefPG2;
	}
	
	public void setAnyLocPrefPG3(double val) {
		anyLocPrefPG3 = val;
	}

	public double getAnyLocPrefPG3() {
		
		return anyLocPrefPG3;
	}
	
	public void setAnyLocPrefPG4(double val) {
		anyLocPrefPG4 = val;
		
	}

	public double getAnyLocPrefPG4() {
		return anyLocPrefPG4;
	}
	
	//------------------------------------
	
	public void setTolerancePrefG1(double val) {
		tolerancePrefG1 = val;
	}

	public double getTolerancePrefG1() {
		return tolerancePrefG1;
	}
	
	public void setTolerancePrefG2(double val) {
		tolerancePrefG2 = val;
	}

	public double getTolerancePrefG2() {

		return tolerancePrefG2;
	}
	
	public void setTolerancePrefG3(double val) {
		tolerancePrefG3 = val;
	}

	public double getTolerancePrefG3() {

		return tolerancePrefG3;
	}
	
	public void setTolerancePrefG4(double val) {
		tolerancePrefG4 = val;
	}

	public double getTolerancePrefG4() {
		return tolerancePrefG4;
	}
	
	//--------------------
	public void setTurnoverG1(double val) {
		this.turnoverG1 = val;
	}
	public double getTurnoverG1() {

		return this.turnoverG1;
	}
	
	public void setTurnoverG2(double val) {
		this.turnoverG2 = val;
	}
	public double getTurnoverG2() {

		return this.turnoverG2;
	}
	
	public void setTurnoverG3(double val) {
		this.turnoverG3 = val;
	}
	public double getTurnoverG3() {
		return this.turnoverG3;
	}
	
	public void setTurnoverG4(double val) {
		this.turnoverG4 = val;
	}
	public double getTurnoverG4() {

		return this.turnoverG4;
	}
	
	public void setFlowInImmP(String somePs) {
		flowInImmP.clear();
		StringTokenizer tok = new StringTokenizer(somePs, ", ");
		double totalNum =0;
		while (tok.hasMoreTokens()) {
			String num = tok.nextToken();
			num = num.trim();
			try {
				Double p = new Double(num);
				totalNum = totalNum + p.doubleValue();
				if (p.doubleValue() > maxP) {
					System.out.println("FlowInImm probablity is out of limit: "+p.doubleValue()+" > "+maxP);

				}
				flowInImmP.add(p);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("FlowInImm probabilities must be delimited by ','");
			}
		}
		if (totalNum<0.99)
			System.out.println("FlowInImm probabliies must add up to ~ 1.00 (100%), currently: " +totalNum);

	}

	public String getFlowInImmP() {
		String retVal = "";
		for (int i = 0; i < flowInImmP.size(); i++) {
			Double j = (Double)flowInImmP.elementAt(i);
			retVal += j.doubleValue() + ", ";
		}

		if (retVal.length() > 0) {
			retVal = retVal.substring(0, retVal.length()- 2);
		}

		return retVal;
	}

	public void setFlowOutEmiP(String somePs) {
		flowOutEmiP.clear();
		StringTokenizer tok = new StringTokenizer(somePs, ", ");
		double totalNum =0;
		while (tok.hasMoreTokens()) {
			String num = tok.nextToken();
			num = num.trim();
			try {
				Double p = new Double(num);
				totalNum = totalNum + p.doubleValue();
				if (p.doubleValue() > maxP) {
					System.out.println("FlowOutEmi probablity is out of limit: "+p.doubleValue()+" > "+maxP);

				}
				flowOutEmiP.add(p);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("FlowOutEmi probabilities must be delimited by ','");
			}
		}
		if (totalNum<0.99)
			System.out.println("FlowOutEmi probabliies must add up to ~ 1.00 (100%), currently: " +totalNum);

	}

	public String getFlowOutEmiP() {
		String retVal = "";
		for (int i = 0; i < flowOutEmiP.size(); i++) {
			Double j = (Double)flowOutEmiP.elementAt(i);
			retVal += j.doubleValue() + ", ";
		}

		if (retVal.length() > 0) {
			retVal = retVal.substring(0, retVal.length()- 2);
		}

		return retVal;
	}

	
	public void setStopAtTick(int val) {
		stopAtTick = val;
		schedule.scheduleActionAt(stopAtTick, this, "stop", Schedule.LAST);
	}

	public int getStopAtTick() {
		return stopAtTick;
	}
	
	
	public void setSnapshotInterval(int val) {
		snapshotInterval = val;
	}

	public int getSnapshotInterval() {
		return snapshotInterval;
	}

	public void callCalculateLQs() {
		//System.out.println("callCalculateQLs,@t= "+this.getTickCount());

		try{
			geoSpace.calculateLQ(Const.TA_All, Const.G1_EURO);
			geoSpace.calculateLQ(Const.TA_All, Const.G2_ASIAN);
			geoSpace.calculateLQ(Const.TA_All, Const.G3_PACIFIC);
			geoSpace.calculateLQ(Const.TA_All, Const.G4_MAORI);
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}


	public void takeSnapshot(){

		try{
			if (isWorldViewDisplayed)
				omDisplay.takeSnapshot();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			dissimilarityGraph.takeSnapshot();
			
			hGraph.takeSnapshot();
			
			if (Const.DisplayGraph_DoMeasurmentForMI)
				moranIGraph.takeSnapshot();
			
			if (Const.DisplayGraph_DoMeasurmentForII)
				isolationIndexGraph.takeSnapshot();
	
			if (displayVacGraph)
				vacGraph.takeSnapshot();
			
			if (displayMajCountGraph)
				majCountGraph.takeSnapshot();
		
			if (displayPoolSizeGraph)
				poolSizeGraph.takeSnapshot();
			
			if(displayVacPropGraph)
				vacPropGraph.takeSnapshot();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public long getRunCount () {  //BMA
		return getController().getRunCount();
	}

	public void closeLocalWriters() {
		if (Const.IS_VERBOSE)
			System.out.println("closeLocalWriters");
		geoSpace.closeLocalWriters();
	}

	private void setSnapshotRecording(){

		if (isWorldViewDisplayed)
			omDisplay.setSnapshotFileName("HAAMoS_IMG_Model_r"+getController().getRunCount()+"_t");
		
		dissimilarityGraph.setSnapshotFileName("HAAMoS_IMG_D_Graph_r"+getController().getRunCount()+"_t");
		
		if (Const.DisplayGraph_DoMeasurmentForMI)
			moranIGraph.setSnapshotFileName("HAAMoS_IMG_MoranI_Graph_r"+getController().getRunCount()+"_t");
		
		hGraph.setSnapshotFileName("HAAMoS_IMG_H_Graph_r"+getController().getRunCount()+"_t");
		
		if (displayVacGraph)
			vacGraph.setSnapshotFileName("HAAMoS_IMG_Vac_Graph_r"+getController().getRunCount()+"_t");
		
		if (displayMajCountGraph)
			majCountGraph.setSnapshotFileName("HAAMoS_IMG_Maj_Graph_r"+getController().getRunCount()+"_t");

		if (Const.DisplayGraph_DoMeasurmentForII)
			isolationIndexGraph.setSnapshotFileName("HAAMoS_IMG_IsolationI_Graph_r"+getController().getRunCount()+"_t");
		
		if (displayPoolSizeGraph)
			poolSizeGraph.setSnapshotFileName("HAAMoS_IMG_PoolSize_Graph_r"+getController().getRunCount()+"_t");

		if (displayVacPropGraph)
			vacPropGraph.setSnapshotFileName("HAAMoS_IMG_VacPropSize_Graph_r"+getController().getRunCount()+"_t");
	}
		
	
	private void initializeDataRecorderForSegMeasures_2Var_MetroArea() {
		
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_All", new H_All_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_All", new MI_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_All", new MI_G2_All_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_All", new D_G1vG2_All_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_All", new II_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_All", new II_G1vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_All", new II_G2vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_All", new II_G2_All_NDS(),-1,4);
		}
	}
	
	private void initializeDataRecorderForSegMeasures_2Var_Add2otherTAs() {
		//---Rodney
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Rodny", new H_04Rodney_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Rodny", new MI_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Rodny", new MI_G2_04Rodney_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Rodny", new D_G1vG2_04Rodney_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Rodny", new II_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Rodny", new II_G1vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Rodny", new II_G2vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Rodny", new II_G2_04Rodney_NDS(),-1,4);
		}

		//---Franklin
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Frank", new H_10Franklin_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Frank", new MI_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Frank", new MI_G2_10Franklin_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Frank", new D_G1vG2_10Franklin_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Frank", new II_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Frank", new II_G1vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Frank", new II_G2vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Frank", new II_G2_10Franklin_NDS(),-1,4);
		}

	}
	private void initializeDataRecorderForSegMeasures_2Var_5DistrictTAs() {
		//---NorthShore
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_NShore", new H_05NorthShore_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_NShore", new MI_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_NShore", new MI_G2_05NorthShore_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_NShore", new D_G1vG2_05NorthShore_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_NShore", new II_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_NShore", new II_G1vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_NShore", new II_G2vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_NShore", new II_G2_05NorthShore_NDS(),-1,4);
		}
		//--Waitakere
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Wtkere", new H_06Waitakere_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Wtkere", new MI_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Wtkere", new MI_G2_06Waitakere_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Wtkere", new D_G1vG2_06Waitakere_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Wtkere", new II_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Wtkere", new II_G1vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Wtkere", new II_G2vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Wtkere", new II_G2_06Waitakere_NDS(),-1,4);
		}
		//---Akl
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Akl", new H_07Auckland_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Akl", new MI_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Akl", new MI_G2_07Auckland_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Akl", new D_G1vG2_07Auckland_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Akl", new II_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Akl", new II_G1vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Akl", new II_G2vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Akl", new II_G2_07Auckland_NDS(),-1,4);
		}
		
		//---Manukau 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Mkau", new H_08Manukau_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Mkau", new MI_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Mkau", new MI_G2_08Manukau_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Mkau", new D_G1vG2_08Manukau_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Mkau", new II_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Mkau", new II_G1vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Mkau", new II_G2vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Mkau", new II_G2_08Manukau_NDS(),-1,4);
		}
		
		//---Papakura 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Papak", new H_09Papakura_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Papak", new MI_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Papak", new MI_G2_09Papakura_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD)
			dataRecorder.addNumericDataSource("D_1v2_Papak", new D_G1vG2_09Papakura_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Papak", new II_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Papak", new II_G1vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Papak", new II_G2vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Papak", new II_G2_09Papakura_NDS(),-1,4);
		}

	}
	
	//===
	private void initializeDataRecorderForSegMeasures_3Var_MetroArea() {
		
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_All", new H_All_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_All", new MI_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_All", new MI_G2_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_All", new MI_G3_All_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_All", new D_G1vG2_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_All", new D_G1vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_All", new D_G2vG3_All_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_All", new II_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_All", new II_G1vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_All", new II_G1vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_All", new II_G3vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_All", new II_G3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_All", new II_G3vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_All", new II_G2vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_All", new II_G2vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_All", new II_G2_All_NDS(),-1,4);
		}

	}
	private void initializeDataRecorderForSegMeasures_3Var_Add2otherTAs() {
		//---Rodney
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Rodny", new H_04Rodney_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Rodny", new MI_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Rodny", new MI_G2_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Rodny", new MI_G3_04Rodney_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Rodny", new D_G1vG2_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Rodny", new D_G1vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Rodny", new D_G2vG3_04Rodney_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Rodny", new II_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Rodny", new II_G1vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Rodny", new II_G1vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Rodny", new II_G3vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Rodny", new II_G3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Rodny", new II_G3vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Rodny", new II_G2vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Rodny", new II_G2vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Rodny", new II_G2_04Rodney_NDS(),-1,4);
		}

		//---Franklin
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Frank", new H_10Franklin_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Frank", new MI_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Frank", new MI_G2_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Frank", new MI_G3_10Franklin_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Frank", new D_G1vG2_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Frank", new D_G1vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Frank", new D_G2vG3_10Franklin_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Frank", new II_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Frank", new II_G1vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Frank", new II_G1vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Frank", new II_G3vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Frank", new II_G3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Frank", new II_G3vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Frank", new II_G2vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Frank", new II_G2vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Frank", new II_G2_10Franklin_NDS(),-1,4);
		}

	}
	private void initializeDataRecorderForSegMeasures_3Var_5DistrictTAs() {
		//---NorthShore
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_NShore", new H_05NorthShore_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_NShore", new MI_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_NShore", new MI_G2_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_NShore", new MI_G3_05NorthShore_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_NShore", new D_G1vG2_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_NShore", new D_G1vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_NShore", new D_G2vG3_05NorthShore_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_NShore", new II_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_NShore", new II_G1vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_NShore", new II_G1vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_NShore", new II_G3vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_NShore", new II_G3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_NShore", new II_G3vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_NShore", new II_G2vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_NShore", new II_G2vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_NShore", new II_G2_05NorthShore_NDS(),-1,4);
		}
		//--Waitakere
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Wtkere", new H_06Waitakere_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Wtkere", new MI_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Wtkere", new MI_G2_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Wtkere", new MI_G3_06Waitakere_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Wtkere", new D_G1vG2_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Wtkere", new D_G1vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Wtkere", new D_G2vG3_06Waitakere_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Wtkere", new II_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Wtkere", new II_G1vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Wtkere", new II_G1vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Wtkere", new II_G3vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Wtkere", new II_G3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Wtkere", new II_G3vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Wtkere", new II_G2vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Wtkere", new II_G2vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Wtkere", new II_G2_06Waitakere_NDS(),-1,4);
		}
		//---Akl
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Akl", new H_07Auckland_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Akl", new MI_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Akl", new MI_G2_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Akl", new MI_G3_07Auckland_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Akl", new D_G1vG2_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Akl", new D_G1vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Akl", new D_G2vG3_07Auckland_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Akl", new II_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Akl", new II_G1vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Akl", new II_G1vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Akl", new II_G3vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Akl", new II_G3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Akl", new II_G3vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Akl", new II_G2vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Akl", new II_G2vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Akl", new II_G2_07Auckland_NDS(),-1,4);
		}
		
		//---Manukau 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Mkau", new H_08Manukau_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Mkau", new MI_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Mkau", new MI_G2_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Mkau", new MI_G3_08Manukau_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Mkau", new D_G1vG2_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Mkau", new D_G1vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Mkau", new D_G2vG3_08Manukau_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Mkau", new II_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Mkau", new II_G1vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Mkau", new II_G1vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Mkau", new II_G3vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Mkau", new II_G3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Mkau", new II_G3vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Mkau", new II_G2vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Mkau", new II_G2vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Mkau", new II_G2_08Manukau_NDS(),-1,4);
		}

		//---Papakura 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Papak", new H_09Papakura_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Papak", new MI_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Papak", new MI_G2_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Papak", new MI_G3_09Papakura_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Papak", new D_G1vG2_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Papak", new D_G1vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Papak", new D_G2vG3_09Papakura_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Papak", new II_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Papak", new II_G1vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Papak", new II_G1vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Papak", new II_G3vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Papak", new II_G3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Papak", new II_G3vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Papak", new II_G2vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Papak", new II_G2vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Papak", new II_G2_09Papakura_NDS(),-1,4);
		}
	
		
	}

	private void initializeDataRecorderForSegMeasures_4Var_MetroArea() {
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_All", new H_All_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_All", new MI_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_All", new MI_G2_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_All", new MI_G3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_All", new MI_G4_All_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_All", new D_G1vG2_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_All", new D_G1vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_All", new D_G1vG4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_All", new D_G2vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_All", new D_G2vG4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_All", new D_G3vG4_All_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_All", new II_G1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_All", new II_G1vG4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_All", new II_G1vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_All", new II_G1vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_All", new II_G4vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_All", new II_G4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_All", new II_G4vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_All", new II_G4vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_All", new II_G3vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_All", new II_G3vG4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_All", new II_G3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_All", new II_G3vG2_All_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_All", new II_G2vG1_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_All", new II_G2vG4_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_All", new II_G2vG3_All_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_All", new II_G2_All_NDS(),-1,4);
		}
	
	}
	private void initializeDataRecorderForSegMeasures_4Var_Add2otherTAs() {
		//---Rodney
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Rodny", new H_04Rodney_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Rodny", new MI_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Rodny", new MI_G2_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Rodny", new MI_G3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Rodny", new MI_G4_04Rodney_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Rodny", new D_G1vG2_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Rodny", new D_G1vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Rodny", new D_G1vG4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Rodny", new D_G2vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Rodny", new D_G2vG4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Rodny", new D_G3vG4_04Rodney_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Rodny", new II_G1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Rodny", new II_G1vG4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Rodny", new II_G1vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Rodny", new II_G1vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Rodny", new II_G4vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Rodny", new II_G4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Rodny", new II_G4vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Rodny", new II_G4vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Rodny", new II_G3vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Rodny", new II_G3vG4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Rodny", new II_G3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Rodny", new II_G3vG2_04Rodney_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Rodny", new II_G2vG1_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Rodny", new II_G2vG4_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Rodny", new II_G2vG3_04Rodney_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Rodny", new II_G2_04Rodney_NDS(),-1,4);
		}

		//---Franklin
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Frank", new H_10Franklin_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Frank", new MI_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Frank", new MI_G2_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Frank", new MI_G3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Frank", new MI_G4_10Franklin_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Frank", new D_G1vG2_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Frank", new D_G1vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Frank", new D_G1vG4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Frank", new D_G2vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Frank", new D_G2vG4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Frank", new D_G3vG4_10Franklin_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Frank", new II_G1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Frank", new II_G1vG4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Frank", new II_G1vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Frank", new II_G1vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Frank", new II_G4vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Frank", new II_G4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Frank", new II_G4vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Frank", new II_G4vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Frank", new II_G3vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Frank", new II_G3vG4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Frank", new II_G3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Frank", new II_G3vG2_10Franklin_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Frank", new II_G2vG1_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Frank", new II_G2vG4_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Frank", new II_G2vG3_10Franklin_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Frank", new II_G2_10Franklin_NDS(),-1,4);
		}
		
	}
	
	private void initializeDataRecorderForSegMeasures_4Var_5DistrictTAs() {
		//---NorthShore
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_NShore", new H_05NorthShore_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_NShore", new MI_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_NShore", new MI_G2_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_NShore", new MI_G3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_NShore", new MI_G4_05NorthShore_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_NShore", new D_G1vG2_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_NShore", new D_G1vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_NShore", new D_G1vG4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_NShore", new D_G2vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_NShore", new D_G2vG4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_NShore", new D_G3vG4_05NorthShore_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_NShore", new II_G1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_NShore", new II_G1vG4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_NShore", new II_G1vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_NShore", new II_G1vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_NShore", new II_G4vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_NShore", new II_G4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_NShore", new II_G4vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_NShore", new II_G4vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_NShore", new II_G3vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_NShore", new II_G3vG4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_NShore", new II_G3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_NShore", new II_G3vG2_05NorthShore_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_NShore", new II_G2vG1_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_NShore", new II_G2vG4_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_NShore", new II_G2vG3_05NorthShore_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_NShore", new II_G2_05NorthShore_NDS(),-1,4);
		}

		//--Waitakere
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Wtkere", new H_06Waitakere_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Wtkere", new MI_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Wtkere", new MI_G2_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Wtkere", new MI_G3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Wtkere", new MI_G4_06Waitakere_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Wtkere", new D_G1vG2_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Wtkere", new D_G1vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Wtkere", new D_G1vG4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Wtkere", new D_G2vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Wtkere", new D_G2vG4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Wtkere", new D_G3vG4_06Waitakere_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Wtkere", new II_G1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Wtkere", new II_G1vG4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Wtkere", new II_G1vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Wtkere", new II_G1vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Wtkere", new II_G4vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Wtkere", new II_G4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Wtkere", new II_G4vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Wtkere", new II_G4vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Wtkere", new II_G3vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Wtkere", new II_G3vG4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Wtkere", new II_G3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Wtkere", new II_G3vG2_06Waitakere_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Wtkere", new II_G2vG1_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Wtkere", new II_G2vG4_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Wtkere", new II_G2vG3_06Waitakere_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Wtkere", new II_G2_06Waitakere_NDS(),-1,4);
		}

		//---Akl
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Akl", new H_07Auckland_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Akl", new MI_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Akl", new MI_G2_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Akl", new MI_G3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Akl", new MI_G4_07Auckland_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Akl", new D_G1vG2_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Akl", new D_G1vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Akl", new D_G1vG4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Akl", new D_G2vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Akl", new D_G2vG4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Akl", new D_G3vG4_07Auckland_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Akl", new II_G1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Akl", new II_G1vG4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Akl", new II_G1vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Akl", new II_G1vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Akl", new II_G4vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Akl", new II_G4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Akl", new II_G4vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Akl", new II_G4vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Akl", new II_G3vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Akl", new II_G3vG4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Akl", new II_G3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Akl", new II_G3vG2_07Auckland_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Akl", new II_G2vG1_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Akl", new II_G2vG4_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Akl", new II_G2vG3_07Auckland_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Akl", new II_G2_07Auckland_NDS(),-1,4);
		}
	
		//---Manukau 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Mkau", new H_08Manukau_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Mkau", new MI_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Mkau", new MI_G2_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Mkau", new MI_G3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Mkau", new MI_G4_08Manukau_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Mkau", new D_G1vG2_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Mkau", new D_G1vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Mkau", new D_G1vG4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Mkau", new D_G2vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Mkau", new D_G2vG4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Mkau", new D_G3vG4_08Manukau_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Mkau", new II_G1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Mkau", new II_G1vG4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Mkau", new II_G1vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Mkau", new II_G1vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Mkau", new II_G4vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Mkau", new II_G4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Mkau", new II_G4vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Mkau", new II_G4vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Mkau", new II_G3vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Mkau", new II_G3vG4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Mkau", new II_G3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Mkau", new II_G3vG2_08Manukau_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Mkau", new II_G2vG1_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Mkau", new II_G2vG4_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Mkau", new II_G2vG3_08Manukau_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Mkau", new II_G2_08Manukau_NDS(),-1,4);
		}
			
		
		//---Papakura 
		if (Const.OutputFile_DoMeasurmentForH)
			dataRecorder.addNumericDataSource("H_Papak", new H_09Papakura_NDS(),-1,4);

		if (Const.OutputFile_DoMeasurmentForMI) {
			dataRecorder.addNumericDataSource("MI_1_Papak", new MI_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_2_Papak", new MI_G2_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_3_Papak", new MI_G3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("MI_4_Papak", new MI_G4_09Papakura_NDS(),-1,4);
		}

		if (Const.OutputFile_DoMeasurmentForD) {
			dataRecorder.addNumericDataSource("D_1v2_Papak", new D_G1vG2_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v3_Papak", new D_G1vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_1v4_Papak", new D_G1vG4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v3_Papak", new D_G2vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_2v4_Papak", new D_G2vG4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("D_3v4_Papak", new D_G3vG4_09Papakura_NDS(),-1,4);
		}
		
		if (Const.OutputFile_DoMeasurmentForII) {
			dataRecorder.addNumericDataSource("II_1_Papak", new II_G1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v4_Papak", new II_G1vG4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v3_Papak", new II_G1vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_1v2_Papak", new II_G1vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_4v1_Papak", new II_G4vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4_Papak", new II_G4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v3_Papak", new II_G4vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_4v2_Papak", new II_G4vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_3v1_Papak", new II_G3vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v4_Papak", new II_G3vG4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3_Papak", new II_G3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_3v2_Papak", new II_G3vG2_09Papakura_NDS(),-1,4);

			dataRecorder.addNumericDataSource("II_2v1_Papak", new II_G2vG1_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v4_Papak", new II_G2vG4_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2v3_Papak", new II_G2vG3_09Papakura_NDS(),-1,4);
			dataRecorder.addNumericDataSource("II_2_Papak", new II_G2_09Papakura_NDS(),-1,4);
		}

	}
	
	private void initializeDataRecorderForPopSize(int nbOfGroups, int nbOfTAs) {

		dataRecorder.addNumericDataSource("Pop_All", new PopSize_All());
		dataRecorder.addNumericDataSource("Pop_G1_All", new PopSize_G1_All());
		dataRecorder.addNumericDataSource("Pop_G2_All", new PopSize_G2_All());
		if (nbOfGroups > 2)
			dataRecorder.addNumericDataSource("Pop_G3_All", new PopSize_G3_All());
		if (nbOfGroups > 3)
			dataRecorder.addNumericDataSource("Pop_G4_All", new PopSize_G4_All());


		if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
			//---NorthShore
			dataRecorder.addNumericDataSource("Pop_NShore", new PopSize_05NorthShore());
			dataRecorder.addNumericDataSource("Pop_G1_NShore", new PopSize_G1_05NorthShore());
			dataRecorder.addNumericDataSource("Pop_G2_NShore", new PopSize_G2_05NorthShore());
			if (nbOfGroups > 2)
				dataRecorder.addNumericDataSource("Pop_G3_NShore", new PopSize_G3_05NorthShore());
			if (nbOfGroups > 3)
				dataRecorder.addNumericDataSource("Pop_G4_NShore", new PopSize_G4_05NorthShore());

			//--Waitakere

			dataRecorder.addNumericDataSource("Pop_Wtkere", new PopSize_06Waitakere());
			dataRecorder.addNumericDataSource("Pop_G1_Wtkere", new PopSize_G1_06Waitakere());
			dataRecorder.addNumericDataSource("Pop_G2_Wtkere", new PopSize_G2_06Waitakere());
			if (nbOfGroups > 2)
				dataRecorder.addNumericDataSource("Pop_G3_Wtkere", new PopSize_G3_06Waitakere());
			if (nbOfGroups > 3)
				dataRecorder.addNumericDataSource("Pop_G4_Wtkere", new PopSize_G4_06Waitakere());

			//---Ak

			dataRecorder.addNumericDataSource("Pop_Akl", new PopSize_07Auckland());
			dataRecorder.addNumericDataSource("Pop_G1_Akl", new PopSize_G1_07Auckland());
			dataRecorder.addNumericDataSource("Pop_G2_Akl", new PopSize_G2_07Auckland());
			if (nbOfGroups > 2)
				dataRecorder.addNumericDataSource("Pop_G3_Akl", new PopSize_G3_07Auckland());
			if (nbOfGroups > 3)
				dataRecorder.addNumericDataSource("Pop_G4_Akl", new PopSize_G4_07Auckland());

			//---Manukau 

			dataRecorder.addNumericDataSource("Pop_Mkau", new PopSize_08Manukau());
			dataRecorder.addNumericDataSource("Pop_G1_Mkau", new PopSize_G1_08Manukau());
			dataRecorder.addNumericDataSource("Pop_G2_Mkau", new PopSize_G2_08Manukau());
			if (nbOfGroups > 2)
				dataRecorder.addNumericDataSource("Pop_G3_Mkau", new PopSize_G3_08Manukau());
			if (nbOfGroups > 3)
				dataRecorder.addNumericDataSource("Pop_G4_Mkau", new PopSize_G4_08Manukau());

			//---Papakura 

			dataRecorder.addNumericDataSource("Pop_Papak", new PopSize_09Papakura());
			dataRecorder.addNumericDataSource("Pop_G1_Papak", new PopSize_G1_09Papakura());
			dataRecorder.addNumericDataSource("Pop_G2_Papak", new PopSize_G2_09Papakura());
			if (nbOfGroups > 2)
				dataRecorder.addNumericDataSource("Pop_G3_Papak", new PopSize_G3_09Papakura());
			if (nbOfGroups > 3)
				dataRecorder.addNumericDataSource("Pop_G4_Papak", new PopSize_G4_09Papakura());
			
			if (nbOfTAs > 5) {
				
				/*
				 * @TODO:  the other two TA (for total of 7) can be added here: 
				 */
			}
		}
		
	}
	
	private void initializeDataRecorderForVacancySize(int nbOfGroups, int nbOfTAs) {
		
		//---Vacancies
		dataRecorder.addNumericDataSource("Vac_All", new VacantSize_All());
		
		if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
			dataRecorder.addNumericDataSource("Vac_NShore", new VacantSize_05NorthShore());
			dataRecorder.addNumericDataSource("Vac_Wtkere", new VacantSize_06Waitakere());
			dataRecorder.addNumericDataSource("Vac_Akl", new VacantSize_07Auckland());
			dataRecorder.addNumericDataSource("Vac_Mkau", new VacantSize_08Manukau());
			dataRecorder.addNumericDataSource("Vac_Papak", new VacantSize_09Papakura());
						
			if (nbOfTAs > 5) {
				
				/*
				 * @TODO:  the other two TA (for total of 7) can be added here: 
				 */
			}
		}
	}
	
	private void initializeDataRecorderForVacancyProportions(int nbOfGroups, int nbOfTAs) {
		
		//---Vacancies proportions (Vac vs PopSize)
		dataRecorder.addNumericDataSource("VProp_All", new VacVsPopSizeProp_All());
		
		if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
			dataRecorder.addNumericDataSource("VProp_NShore", new VacVsPopSizeProp_05NShore());
			dataRecorder.addNumericDataSource("VProp_Wtkere", new VacVsPopSizeProp_06Wtkere());
			dataRecorder.addNumericDataSource("VProp_Akl", new VacVsPopSizeProp_07Akl());
			dataRecorder.addNumericDataSource("VProp_Mkau", new VacVsPopSizeProp_08Mkau());
			dataRecorder.addNumericDataSource("VProp_Papak", new VacVsPopSizeProp_09Papak());
						
			if (nbOfTAs > 5) {
				
				/*
				 * @TODO:  the other two TA (for total of 7) can be added here: 
				 */
			}
		}
	}

	
	private void initializeDataRecorderForMajorityCounts(int nbOfGroups, int nbOfTAs) {
		
		dataRecorder.addNumericDataSource("J_G1_All", new MajCount_G1_All());
		dataRecorder.addNumericDataSource("J_G2_All", new MajCount_G2_All());
		dataRecorder.addNumericDataSource("J_G3_All", new MajCount_G3_All());
		dataRecorder.addNumericDataSource("J_G4_All", new MajCount_G4_All());
		dataRecorder.addNumericDataSource("J_GNM_All", new MajCount_None_All());
		
		if (Const.OutputFile_DoMeasurmentForDistrictTAs) {

			dataRecorder.addNumericDataSource("J_G1_NShore", new MajCount_G1_05NShore());
			dataRecorder.addNumericDataSource("J_G2_NShore", new MajCount_G2_05NShore());
			dataRecorder.addNumericDataSource("J_G3_NShore", new MajCount_G3_05NShore());
			dataRecorder.addNumericDataSource("J_G4_NShore", new MajCount_G4_05NShore());
			dataRecorder.addNumericDataSource("J_GNM_NShore", new MajCount_None_05NShore());

			dataRecorder.addNumericDataSource("J_G1_Wtkere", new MajCount_G1_06Wtakere());
			dataRecorder.addNumericDataSource("J_G2_Wtkere", new MajCount_G2_06Wtakere());
			dataRecorder.addNumericDataSource("J_G3_Wtkere", new MajCount_G3_06Wtakere());
			dataRecorder.addNumericDataSource("J_G4_Wtkere", new MajCount_G4_06Wtakere());
			dataRecorder.addNumericDataSource("J_GNM_Wtkere", new MajCount_None_06Wtakere());

			dataRecorder.addNumericDataSource("J_G1_Akl", new MajCount_G1_07Akl());
			dataRecorder.addNumericDataSource("J_G2_Akl", new MajCount_G2_07Akl());
			dataRecorder.addNumericDataSource("J_G3_Akl", new MajCount_G3_07Akl());
			dataRecorder.addNumericDataSource("J_G4_Akl", new MajCount_G4_07Akl());
			dataRecorder.addNumericDataSource("J_GNM_Akl", new MajCount_None_07Akl());

			dataRecorder.addNumericDataSource("J_G1_Mkau", new MajCount_G1_08Mkau());
			dataRecorder.addNumericDataSource("J_G2_Mkau", new MajCount_G2_08Mkau());
			dataRecorder.addNumericDataSource("J_G3_Mkau", new MajCount_G3_08Mkau());
			dataRecorder.addNumericDataSource("J_G4_Mkau", new MajCount_G4_08Mkau());
			dataRecorder.addNumericDataSource("J_GNM_Mkau", new MajCount_None_08Mkau());

			dataRecorder.addNumericDataSource("J_G1_Papak", new MajCount_G1_09Papak());
			dataRecorder.addNumericDataSource("J_G2_Papak", new MajCount_G2_09Papak());
			dataRecorder.addNumericDataSource("J_G3_Papak", new MajCount_G3_09Papak());
			dataRecorder.addNumericDataSource("J_G4_Papak", new MajCount_G4_09Papak());
			dataRecorder.addNumericDataSource("J_GNM_Papak", new MajCount_None_09Papak());
			
			if (nbOfTAs > 5) {

				/*
				 * @TODO:  the other two TA (for total of 7) can be added here: 
				 */
			}
		}
	}
	
	private void initializeDataRecorderForPoolSize(int nbOfGroups) {
		dataRecorder.addNumericDataSource("W", new PoolListSize_NDS());
		dataRecorder.addNumericDataSource("W_G1", new PoolPopSize_G1());
		dataRecorder.addNumericDataSource("W_G2", new PoolPopSize_G2());
		if (nbOfGroups > 2)
			dataRecorder.addNumericDataSource("W_G3", new PoolPopSize_G3());
		if (nbOfGroups > 3)
			dataRecorder.addNumericDataSource("W_G4", new PoolPopSize_G4());
	}
	

	protected void initializeDataRecorder() {
		
		dataRecorder = new DataRecorder("./HAAMoS_Measures_Output.csv", this);
			
		switch (this.nbOfEthnicGroups) { 
		case 2: 
			initializeDataRecorderForSegMeasures_2Var_MetroArea();
			if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
				if (geoSpace.totalNbOfTAs ==5) {
					initializeDataRecorderForSegMeasures_2Var_5DistrictTAs();
				}
				if (geoSpace.totalNbOfTAs >5) {
					initializeDataRecorderForSegMeasures_2Var_Add2otherTAs();
				}
			}
			break;
			
		case 3: 
			initializeDataRecorderForSegMeasures_3Var_MetroArea();
			if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
				if (geoSpace.totalNbOfTAs ==5) {
					initializeDataRecorderForSegMeasures_3Var_5DistrictTAs();
				}
				if (geoSpace.totalNbOfTAs >5) {
					initializeDataRecorderForSegMeasures_3Var_Add2otherTAs();
				}
			}
			
			break;
		case 4:
			initializeDataRecorderForSegMeasures_4Var_MetroArea();
			if (Const.OutputFile_DoMeasurmentForDistrictTAs) {
				if (geoSpace.totalNbOfTAs ==5) {
					initializeDataRecorderForSegMeasures_4Var_5DistrictTAs();
				}
				if (geoSpace.totalNbOfTAs >5) {
					initializeDataRecorderForSegMeasures_4Var_Add2otherTAs();
				}
			}
			
			break;
		}		
		
		
		if (Const.OutputFile_DoMeasurmentForPopSize) 
			initializeDataRecorderForPopSize(nbOfEthnicGroups, geoSpace.totalNbOfTAs);
		
		if (Const.OutputFile_DoMeasurmentForVacacnySize)
			initializeDataRecorderForVacancySize(nbOfEthnicGroups, geoSpace.totalNbOfTAs);
		
		if (Const.OutputFile_DoMeasurmentForVacacnyProportion)
			initializeDataRecorderForVacancyProportions(nbOfEthnicGroups, geoSpace.totalNbOfTAs);

		if (Const.OutputFile_DoMeasurmentForMajorityCountSize)
			initializeDataRecorderForMajorityCounts(nbOfEthnicGroups, geoSpace.totalNbOfTAs);
		
		if (Const.OutputFile_DoMeasurmentForPoolSize)
			this.initializeDataRecorderForPoolSize(nbOfEthnicGroups);
		
	}
	
	/**
	 * Module for initialisation of the model display.
	 */
	public void buildDisplay(){

		if (getController().getRunCount() == 1) {
			if (Const.IS_VERBOSE)
				System.out.println("Running BuildDisplay");
			
			if (isWorldViewDisplayed) {

				if (omDisplay == null) {
					omDisplay = new ExtendedOpenMapDisplay("HAAMoS Model");
					omDisplay.setModel(this);
				}
			}
			else System.out.println("World/City View Display is OFF");

			//create a new legend

			if (Const.DisplayGraph_Legend) {

				userLegend = new Legend("Legend");

				userLegend.addLegend("Area Unit Color:", 0, null, false, 0, 0);
				userLegend.addLegend(" Majority G1: European ", Legend.SQUARE, Const.G1_EURO_Color, false);
				userLegend.addLegend(" Majority G2: Asian ", Legend.SQUARE, Const.G2_ASIAN_Color, false);
				if (this.nbOfEthnicGroups >2)
					userLegend.addLegend(" Majority G3: Pacific ", Legend.SQUARE, Const.G3_PACIFIC_Color, false);
				if (this.nbOfEthnicGroups >3)
					userLegend.addLegend(" Majority G4: Maori ", Legend.SQUARE, Const.G4_MAORI_Color, false);
				userLegend.addLegend(" Totally Vacant ", Legend.SQUARE, Const.G_Vacancy_Color, false);
				userLegend.addLegend(" No Majority ", Legend.SQUARE, Const.G_NoMajority_Color, false);

				//leg.addLegend("Residents", Legend.SQUARE, Color.BITMASK, false, 1, 1);
				userLegend.addLegend("Population Size Color:", 0, null,false, 0, 0);
				userLegend.addLegend(" G1: European ", Legend.CIRCLE, Const.G1_EURO_Color, false);
				userLegend.addLegend(" G2: Asian ", Legend.CIRCLE, Const.G2_ASIAN_Color, false);
				if (this.nbOfEthnicGroups >2)
					userLegend.addLegend(" G3: Pacific ", Legend.CIRCLE, Const.G3_PACIFIC_Color, false);
				if (this.nbOfEthnicGroups >3)
					userLegend.addLegend(" G4: Maori ", Legend.CIRCLE, Const.G4_MAORI_Color, false);
				userLegend.addLegend(" Vacant Space ", Legend.CIRCLE, Const.G_Vacancy_Color, false);
				userLegend.display();
			}


			boolean areAllGisArealUnitsOMA=true;	
			Iterator it = geoSpace.worldGisArealUnitAgentList.iterator();

			while(it.hasNext()){
				if (it.next()instanceof OpenMapAgent==false){
					areAllGisArealUnitsOMA=false;
				}	
			}
			if (isWorldViewDisplayed) {
				if (areAllGisArealUnitsOMA){			

					omLayer1 = omDisplay.addLayer(geoSpace.worldGisArealUnitAgentList, "Areal Units layer");
					omDisplay.centerMap(geoSpace.gisOpenMapData.getCenter(this.shapefileSourceFile));
					//omDisplay.setMapScale(310000);	// on Asus
					omDisplay.setMapScale(300000);	// on Asus, (both Artificial and Realistic)


					//omDisplay.setMapScale(225000);// on big screen
					Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

					//omDisplay.getFrame().setSize(screenDim.width-5, screenDim.height-50); // on big screen?	
					omDisplay.getFrame().setSize(screenDim.width-500, screenDim.height); // on Asus (Realistic)
					//omDisplay.getFrame().setSize(screenDim.width-400, screenDim.height); // on Asus (Artificial)


					omDisplay.getFrame().setLocation(0, 0);
					
					if (debug) {
						System.out.println("omDisplay.getProjection: "+ omDisplay.getProjection());
						System.out.println("omDisplay.getMapPanel: "+ omDisplay.getMapPanel());
						System.out.println("omDisplay.getFrame: "+ omDisplay.getFrame());
						System.out.println("gisData.getExtents: "+ geoSpace.gisOpenMapData.getExtents(this.shapefileSourceFile));
					}

				}
				else {
					System.out.println("HAAMoSmodel.buildDisplay():: Problem in dispaly: not all GISArealUnits");
				}

			}


			if (isWorldViewDisplayed)
				omLayer2 = omDisplay.addLayer(geoSpace.worldGisAUgroupPopInfoAgentList, "AU Group Pop Info layer");

		}

		setSnapshotRecording();
	}

	/**
	 * Module for initialisation of the scheduling.
	 */
	public void buildSchedule(){
		if (Const.IS_VERBOSE)
			System.out.println("Running BuildSchedule");

		class MovingProcessSchedule extends BasicAction {
			public void execute() {
				
				int tickTime = (int) model.getTickCount();
				
				ArrayList areaUnitList = geoSpace.getWorldAreaUnitListCopy();
				
				boolean isPoolSizeZero = false;
				
				if (geoSpace.poolList.size() > 0) {
					isPoolSizeZero = true;
				}
				
				for(int i =0; i < areaUnitList.size(); i++){
					AreaUnit ra = (AreaUnit)areaUnitList.get(i);
					ra.step(isPoolSizeZero); //in each step those regions pool their moving HHs
				}
				     		
				if (flowOutEmi>0)
					geoSpace.flowOut(flowOutEmi);

				if (flowInImm>0) {
					if (Const.IS_INFLOW_BASED_ON_CENSUS) {
						geoSpace.calculatePopGrowth_AdjVacancy_FlowOut_FlowIn_BasedOnCensus_TA_based(tickTime, Const.CENSUS_DATA_INTRO_SPAN);

					}
					else 
						geoSpace.poolFlowInPop(flowInImm);  //@TODO: this needs to be checked & corrected if used (currenlty not used)
				}

				geoSpace.placeFromPool();  
			
				if (isWorldViewDisplayed) {
					omDisplay.updateLayer(geoSpace.worldGisArealUnitAgentList, "Areal Units layer");//updates the layer
					omDisplay.updateLayer(geoSpace.worldGisAUgroupPopInfoAgentList, "AU Group Pop Info layer");
					omDisplay.updateDisplay();
				}
				
			}
		}

		class IndexOfDissimilarityUpdateSchedule extends BasicAction {
			public void execute(){
				dissimilarityGraph.step();
			}
		}
	
		
		class MoranI_Measure_updateSchedule extends BasicAction {
			public void execute(){
				moranIGraph.step();
			}
		}

		
		class H_Measure_updateSchedule extends BasicAction {
			public void execute(){
				hGraph.step();
			}
		}
		
		class Vac_Measure_updateSchedule extends BasicAction {
			public void execute(){
				vacGraph.step();
			}
		}
		class MajCount_Measure_updateSchedule extends BasicAction {
			public void execute(){
				majCountGraph.step();
			}
		}
	
		
		class IsolationIndex_Measure_updateSchedule extends BasicAction {
			public void execute(){
				isolationIndexGraph.step();
			}
		} 

		class PoolSizeUpdateSchedule extends BasicAction {
			public void execute(){
				poolSizeGraph.step();
			}
		}
		
		class VacPropUpdateSchedule extends BasicAction {
			public void execute(){
				vacPropGraph.step();
			}
		}
		
		class RecordData extends BasicAction {
			public void execute(){
				  dataRecorder.record();
			}
		}
     
		RecordData recordData =null;
		if (Const.IS_RECORD_CSV_DATA){
			recordData = new RecordData();
			if (Const.IS_RECORD_CSV_DATA_ONLY_AT_T0)
				recordData.execute(); // to get data (e.g. measurments) recorded at t=0
			else if (Const.IS_RECORD_CSV_DATA_AT_T0_ANYWAY)
				recordData.execute(); 
		}
		schedule.scheduleActionAt(stopAtTick, this, "stop", Schedule.LAST);
				
		if (Const.OutputFile_DoMeasurmentForLQ)
			callCalculateLQs();

		ActionGroup seq_Schedules = new ActionGroup(ActionGroup.SEQUENTIAL);
		
		seq_Schedules.addAction(new MovingProcessSchedule()); //for build cenus measure, comment this
		
		if (Const.DisplayGraph_DoMeasurmentForMI)
			seq_Schedules.addAction(new MoranI_Measure_updateSchedule());
		
		seq_Schedules.addAction(new H_Measure_updateSchedule());
		
		if (this.displayVacGraph)
			seq_Schedules.addAction(new Vac_Measure_updateSchedule());
		
		if (displayVacPropGraph)
			seq_Schedules.addAction(new VacPropUpdateSchedule());
		
		if (displayMajCountGraph)
			seq_Schedules.addAction(new MajCount_Measure_updateSchedule());

		if (displayPoolSizeGraph)
			seq_Schedules.addAction(new PoolSizeUpdateSchedule());
		
		seq_Schedules.addAction(new IndexOfDissimilarityUpdateSchedule());
		
		if (Const.DisplayGraph_DoMeasurmentForII)
			seq_Schedules.addAction(new IsolationIndex_Measure_updateSchedule());

		if ((Const.IS_RECORD_CSV_DATA)&& (!Const.IS_RECORD_CSV_DATA_ONLY_AT_T0))
			seq_Schedules.addAction(recordData);

		if (Const.OutputFile_DoMeasurmentForLQ)
			schedule.scheduleActionBeginning(0,this, "callCalculateLQs");
		
		schedule.scheduleActionBeginning(0, seq_Schedules); // putting 1 generates the same results
		schedule.scheduleActionAtInterval(this.getSnapshotInterval(), this, "takeSnapshot", Schedule.LAST); //every 1 interval take snapshot
		
		schedule.scheduleActionAtEnd(this, "takeSnapshot");
		
		if (Const.IS_RECORD_CSV_DATA) {
			schedule.scheduleActionAtEnd(dataRecorder, "writeToFile");
			schedule.scheduleActionAtEnd(this, "closeLocalWriters");
		}
		
	} 
	
	private void initializeRandomDistributions() {
		
		Random.createBinomial((int)maxPopPerAU, 0.5);  //necessary
		Random.createPoisson(Const.SEARCH_LIMIT_FACTOR);  //necessary

		Random.createHyperGeometric((int)maxPopPerAU, (int)maxPopPerAU/2, 10);  //necessary
		Random.createNormal(0, 1);  // may not be necessary (some of calls to next* does not require prior creation of dist)
		
		Random.createNegativeBinomial((int)maxPopPerAU, 0.5);  //necessary
		
	}
	
	public void nullifyAndFreeMemory(){
		
		System.out.println("nullifyAndFreeMemory");
		
	    for (AreaUnit au: geoSpace.worldGisArealUnitAgentList) {
	    	au.nullifyMe();
	    }
	
	    for (GroupPopInfo gpi: geoSpace.worldGisAUgroupPopInfoAgentList) {
	    	gpi.nullifyMe();
	    }
	    
		
		geoSpace.worldGisArealUnitAgentList.clear();
		geoSpace.worldGisArealUnitAgentList = null;
		geoSpace.poolList.clear();
		geoSpace.poolList = null;		
		geoSpace.worldGisAUgroupPopInfoAgentList.clear();
		geoSpace.worldGisAUgroupPopInfoAgentList = null;				
		schedule = null;
		geoSpace = null;
		iController = null;
		
		this.shapefileSourceFile=null;
		this.neighbourhoodGALSourceFile=null;
		
		
		if (omLayer1 != null) {
			omLayer1.clearListeners();
			omLayer1.removeAll();
			omLayer1.disposeMe();
			omLayer1 = null;
		}
		
		if (omLayer2 != null) {
			omLayer2.clearListeners();
			omLayer2.removeAll();
			omLayer2.disposeMe();
			omLayer2 = null;
		}
		
		
		if (omDisplay!=null){

			omDisplay.dispose();
			omDisplay = null;

		}

		if (userLegend!=null){
			userLegend.dispose();
			userLegend =null;
		}
			
		if (dissimilarityGraph != null){
			dissimilarityGraph.dispose();
			dissimilarityGraph =null;
		}
		
		if (moranIGraph != null){
			moranIGraph.dispose();
			moranIGraph=null;
		}
		
		if (hGraph != null){
			hGraph.dispose();
			hGraph = null;
		}
		
		if (vacGraph != null){
			vacGraph.dispose();
			vacGraph = null;
		}
		
		if (majCountGraph != null){
			majCountGraph.dispose();
			majCountGraph = null;
		}
			
		if (isolationIndexGraph != null){
			isolationIndexGraph.dispose();
			isolationIndexGraph = null; 
		}
		
		if (poolSizeGraph != null){
			poolSizeGraph.dispose();
			poolSizeGraph = null;
		}		
		
		if (vacPropGraph != null){
			vacPropGraph.dispose();
			vacPropGraph = null;
		}
		
		model.clearPropertyListeners();
		model.clearMediaProducers();
		
        Runtime runTime = Runtime.getRuntime();
		
		long heapSize = runTime.totalMemory();
		long freeMemory = runTime.freeMemory();
		
		runTime.gc(); //call garbage collector
		heapSize = runTime.totalMemory();
		freeMemory = runTime.freeMemory();
		
	}
	
	
	/**
	 * Module for initialisation of the model.
	 */
	public void buildModel(){
		if (Const.IS_VERBOSE)
			System.out.println("Running BuildModel");

		initializeRandomDistributions();
				
		Runtime runTime = Runtime.getRuntime();
		
		long heapSize = runTime.totalMemory();
		long freeMemory = runTime.freeMemory();
		
		runTime.gc(); //call garbage collector
		heapSize = runTime.totalMemory();
		freeMemory = runTime.freeMemory();
		
		boolean isFileChosen = false;
		if (!this.isBatch) {  //if not batch run, then let user to select and open shapefile
		   isFileChosen = chooseShapeFile();
		}
		else { 
			
			shapefileSourceFile = Const.BatchModeFilenamePath;
			
			if (Const.IS_VERBOSE)
				System.out.println("Shapefile opened for batch:  "+shapefileSourceFile);
			String neigbourhoodInFile;
			neighbourhoodGALSourceFile = shapefileSourceFile;
			neighbourhoodGALSourceFile = neighbourhoodGALSourceFile.substring(0, neighbourhoodGALSourceFile.length()-3) + "GAL";
			if (Const.IS_VERBOSE)
				System.out.println("GAL file expected for batch: " + neighbourhoodGALSourceFile);
			isFileChosen = true;
		}
		
		if (Const.IS_VERBOSE) {
			if (!(flowInImm >0)) {
				System.out.println(">> --- NO Immigration/Growth");
			}
			else {
				System.out.print(">> +++ With Immigration/Growth");
				System.out.print(", & VacRate (if uniform used and not census)= "+Const.INFLOW_CENSUS_BASED_VACANCY_RATE_FOR_ALL+ ", MinVac= "+ Const.MinVacancyNumber);
				if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM) {
					System.out.println(", applied randomly with probability = "+ Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);
				}
				else System.out.println();
				System.out.println("  >> Immigrant has preference for specific TA?: "+ Const.IS_IMMIGRANT_USE_TA_PREFERENCE);
				System.out.println("  >> Is Immigration/Growth based on census?: "+ Const.IS_INFLOW_BASED_ON_CENSUS);
				System.out.println("  >> NB of years census data is spaned = "+ Const.CENSUS_DATA_INTRO_SPAN);
				System.out.println("  >> IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM ?: "+ Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM);
				if (Const.IS_INFLOW_CENSUS_BASED_VACANCY_RATE_RANDOM ) {
					System.out.println("   >>> INFLOW_CENSUS_BASED_VACANCY_RATE_FOR_ALL = "+ Const.INFLOW_CENSUS_BASED_VACANCY_RATE_FOR_ALL);
					System.out.println("   >>> INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION = "+ Const.INFLOW_CENSUS_BASED_VACANCY_RATE_PROPABILITY_PRECISION);
				}
				
				System.out.println("  >> Growth SNZ projection case (if used) = "+ Const.SNZ_GROWTH_PROJ_CASE);
				
			}

			System.out.println(">> Household agents search limit mean = "+ Const.SEARCH_LIMIT_FACTOR);
			System.out.println(">> is Morans'I calcualation using proportion?: "+Const.IS_MoranI_USE_PROPORTION);
			System.out.println(">> SD used in random (Normal) guessing/estimating of co-ethnic% calcualation: "+Const.Realtor_SD_RandomCoEthnicEstimation);

			System.out.println(">> is setting Batch Mode?: "+ Const.IS_BATCH_MODE);
		}
			
		
		geoSpace = new GeoSpace(this);
		AreaUnit.setModel(this);
		AreaUnit.setGeographicalSpace(geoSpace);
			
		if (isFileChosen) {
			
			geoSpace.createAndInitializeArealUnitAgents();
			
			if (!readFromShapefileOn) { 
				geoSpace.initializeCityManually();
			}	
			
			if (Const.IS_INFLOW_BASED_ON_CENSUS && (flowInImm>0))
				geoSpace.initializeVacancyBasedOnCensusRate(Const.CENSUS_DATA_INTRO_SPAN);
			else 		
				geoSpace.initinalizeVacancy(); 

			if (isWorldViewDisplayed)
				geoSpace.createGroupPopInfoAgents();
			
			
		}
				
		if (Const.IS_RECORD_CSV_DATA)
			initializeDataRecorder();
	}
	
	
	public void begin(){

		buildModel();		
		buildSchedule();  
		buildDisplay();    
				
	}
	

	public static void main(String[] args) {
		if (Const.IS_VERBOSE)
			System.out.println("main():: argsLength: " +args.length+ " "+Arrays.toString(args));	
		SimInit init = new SimInit();
		 model = new HAAMoSModel();
		init.loadModel(model, "", false);

	} 

}
