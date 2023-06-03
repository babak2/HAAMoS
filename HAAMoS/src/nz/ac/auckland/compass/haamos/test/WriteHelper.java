/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.test;

import nz.ac.auckland.compass.haamos.io.CSV_FileWriter;


/**
 * Manages multiple writer streams ...
 * @author Babak Mahdavi Ardestani
 */


public class WriteHelper {
	
	//static CSVWriter outputFile;
	static CSV_FileWriter outputFile1;
	static CSV_FileWriter outputFile2;
	static CSV_FileWriter outputFile3;
	static CSV_FileWriter outputFile4;
	static CSV_FileWriter outputFile5;

	/**
	 * This methods writes the parameters passed by arguments into CSV file format.
	 * @param fileName
	 * @param C
	 * @param NC
	 * @param B
	 * @param D
	 * @param S
	 * @param e
	 * @param k
	 */
	/*static public void writeOutput(CascadeContext mainContext, String fileName, boolean addInfoHeader, double[] valArr) {
		int [] ts_arr = new int[mainContext.ticksPerDay];

		for (int i=0; i<ts_arr.length; i++){
			ts_arr[i] = i;	
		}
		String resFileName = fileName+mainContext.getDayCount()+".csv";

		CSVWriter res = new CSVWriter(resFileName, false);

		if (addInfoHeader) {
			
			res.appendText("Random seed= "+mainContext.getRandomSeedValue());
			res.appendText("Number of Prosumers= "+mainContext.getTotalNbOfProsumers());
			res.appendText("ProfileBuildingPeriod= "+Consts.AGGREGATOR_PROFILE_BUILDING_PERIODE);
			res.appendText("TrainingPeriod= "+Consts.AGGREGATOR_TRAINING_PERIODE);
			res.appendText("REEA on?= "+Consts.AGG_RECO_REEA_ON);
			res.appendText("ColdAppliances on?= "+Consts.HHPRO_HAS_COLD_APPL);
			res.appendText("WetAppliances on?= "+Consts.HHPRO_HAS_WET_APPL);
			res.appendText("ElectSpaceHeat on?= "+Consts.HHPRO_HAS_ELEC_SPACE_HEAT);
			res.appendText("ElectWaterHeat on?= "+Consts.HHPRO_HAS_ELEC_WATER_HEAT);
			res.appendText("");
			
			res.appendText("Timeslots:");
			res.appendRow(ts_arr);
		}
		
		res.appendText("B:");
		res.appendRow(valArr);
	
		res.close(); 
		
	} */
	
	private static CSV_FileWriter getOutputFile(int fileNb) {
		
		CSV_FileWriter outputFileToReturn = null;
		
		switch (fileNb) {
		   case 1:  outputFileToReturn = outputFile1;
		   break;
		   case 2:  outputFileToReturn = outputFile2;
		   break;
		   case 3:  outputFileToReturn = outputFile3;
		   break;
		   case 4:  outputFileToReturn = outputFile4;
		   break;
		   case 5:  outputFileToReturn = outputFile5;
		   break;
		}
		
		return outputFileToReturn;
	}
	
	
	public static void writeData(int fileNb, double[] valArr) {
		getOutputFile(fileNb).appendRow(valArr);
		//outputFile.appendRow(valArr);
	}
	
	public static void writeData(int fileNb, int[] valArr) {
		getOutputFile(fileNb).appendRow(valArr);
	}
	
	
	public static void writeData(int fileNb, String[] valArr) {
		getOutputFile(fileNb).appendRow(valArr);
		//outputFile.appendRow(valArr);
	}
	
	public static void writeText(int fileNb, String text) {
		getOutputFile(fileNb).appendText(text);
		//outputFile.appendText(text);

	}
	
	public static void writeColHeaders(int fileNb, String[] colsNames) {
		getOutputFile(fileNb).writeColHeaders(colsNames);
		//outputFile.writeColHeaders(colsNames);

	}
	
	public static void initialize(int fileNb, String filename) {
		
		/*if (RunEnvironment.getInstance().isBatch()) {
			try {
			    BufferedWriter outputFile = new BufferedWriter(new FileWriter(filename, true));
			} catch (IOException e) { }
			
		}
		else outputFile = new CSVWriter(filename+".csv", true); */
		
		switch (fileNb) {
		   case 1:  outputFile1 = new CSV_FileWriter(filename+".csv", true);
		   break;
		   case 2:  outputFile2 = new CSV_FileWriter(filename+".csv", true);
		   break;
		   case 3:  outputFile3 = new CSV_FileWriter(filename+".csv", true);
		   break;
		   case 4:  outputFile4 = new CSV_FileWriter(filename+".csv", true);
		   break;
		   case 5:  outputFile5 = new CSV_FileWriter(filename+".csv", true);
		   break;
		}
		

	}
	
	public static void close(int fileNb) {
		getOutputFile(fileNb).close();
	}
	
	


}
