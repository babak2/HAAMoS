/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.io;


import javax.swing.filechooser.*;
import java.io.File;

public class ShapefileFilterAndChooser extends FileFilter {

	/** 
	 * True, whether the given file is accepted by this filter. 
	 * This filter accepts .shp files, that is, display them so they can be chosen.
	 * @param File to filter (for displaying, and selection).
	 * @return true if file is a directory or a shp file.
	 * 
	 * @author Babak Mahdavi Ardestani
	 * 
	 **/
	public boolean accept(File file) 	{
		
		boolean fileAccepted =false;
		
		String shapefileName = file.getName();
			
		if (shapefileName.endsWith("shp")) {
			return fileAccepted = true;
		}
		
		if (file.isDirectory()) {
			return fileAccepted = true;
		}

		return fileAccepted;
	}

	/**
	 * The description of this filter. 
	 * For example: "JPG and GIF Images" (in this case a Shapefile')
	 * @return file's description
	 **/
	public String getDescription() 	{
		return new String("Shapefile (.shp)");
	}
}
