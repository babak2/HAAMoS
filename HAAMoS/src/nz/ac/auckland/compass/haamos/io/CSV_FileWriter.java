/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to create CSV (Comma Separated Values)files.
 * After creation of the class, the user can employ different methods to write or append
 * values (in columns) in the file. In most cases, the user should manually call the close method.
 * For convenience of writing one or two dimensional arrays rapidly, the closing is done 
 * automatically without allowing for further append.
 * Currently, the files are created in the user current directory. 
 * 
 * TODO: Can be expanded/modified (e.g. closure of file policy, file location options, etc).  
 * 
 * @author Babak Mahdavi Ardestani
 *
 */

public class CSV_FileWriter{
	FileWriter writer;
	String fileName;
	//TabularDataWriter dataMatrix;

	/**
	 * @param sFileName file name (as string) of the csv file to write data into 
	 * @param append boolean to indicate whether to append or rewrite the data 
	 */
	public CSV_FileWriter(String sFileName, boolean append){
		try	{
			writer = new FileWriter(sFileName, append);
			fileName = sFileName;
			//dataMatrix = new TabularDataWriter();

		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	public void writeColHeader(String colName) {
		try	{
			writer.append(colName);
			writer.append('\n');

			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	public void appendColHeader(String colName) {
		try	{
			writer.append(colName);

			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	public void writeColHeaders(String[] colNames) {
		try	{
			for (int i=0; i<colNames.length; i++){
				writer.append(colNames[i]);
				writer.append(',');
			}
			writer.append('\n');

			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	

	
	public void appendRow(float[]values) {
		try {
			for (int i=0; i<values.length; i++){
				writer.append(""+values[i]);
				writer.append(',');
				
			}
			writer.append('\n');
			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void appendRow(int[]values) {
		try {
			for (int i=0; i<values.length; i++){
				writer.append(""+values[i]);
				writer.append(',');
			}
			writer.append('\n');
			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void appendRow(double[] values) {
		try {
			for (int i=0; i<values.length; i++){
				writer.append(""+values[i]);
				//if (Consts.DEBUG) System.out.println("CSWriter:: value: "+values[i]);
				writer.append(',');
				
			}
			writer.append('\n');
			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void appendRow(String[] values) {
		try {
			for (int i=0; i<values.length; i++){
				writer.append(values[i]);
				//if (Consts.DEBUG) System.out.println("CSWriter:: value: "+values[i]);
				writer.append(',');
				
			}
			writer.append('\n');
			writer.flush();

		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void appendText(String text) {
		try {
			writer.append(text);
			writer.append('\n');
			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void appendHeaderAndCol(String header, String[]values) {
		try {
			writer.append(header);
			writer.append('\n');
			appendCol(values);
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	public void appendCol(String[]values) {
		try {
			for (int i=0; i<values.length; i++){
				writer.append(values[i]);
				writer.append('\n');
			}

			writer.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
		Usage: for example, IDs and values (written in the row and column of 2D array respectively) 
		it can be written in one shut, using this method.	
		 * @param values 2-D array containing cols and rows values
	 */

	public void appendCols(String[][]values) {
		try	{
			for (int i=0; i<values.length; i++){
				for (int j=0; j<values[0].length; j++) {
					writer.append(values[i][j]);
					writer.append(',');

				}
				writer.append('\n');
			}

			//writer.flush();
		}

		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public void appendCols(float[][]values) {
		try	{
			for (int i=0; i<values.length; i++){
				for (int j=0; j<values[0].length; j++) {
					writer.append(""+values[i][j]);
					writer.append(',');

				}
				writer.append('\n');
			}

			//writer.flush();
		}

		catch(IOException e) {
			e.printStackTrace();
		}

	}


	/**
		This method is used to close the file. 
		User should explicitly call this function after creating the file writer 
		and writing/appending the values. 
	 */

	public void close() {
		try	{
			writer.flush();
			writer.close();
			System.getProperty("user.dir");
			System.out.println("File created: "+System.getProperty("user.dir")+"\\"+fileName);
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	

}

