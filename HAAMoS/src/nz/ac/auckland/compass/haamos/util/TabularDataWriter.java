package nz.ac.auckland.compass.haamos.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import nz.ac.auckland.compass.haamos.base.Const;
import nz.ac.uoa.sgges.babak.HAAMoS.HAAMoSModel;

import uchicago.src.sim.engine.IController;
import uchicago.src.sim.util.SimUtilities;

import ViolinStrings.Strings;

/**
 * Holds data in tabular format - a vector of vectors. Also provides methods
 * for printing the data to a file.
 */

public class TabularDataWriter implements Serializable{
	
	public class TabularDataIterator implements Iterator{
	    private Iterator i;

	    TabularDataIterator(){
	      i = data.iterator();
	    }

	    /**
	     * Returns <tt>true</tt> if the iteration has more elements. (In other
	     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	     * rather than throwing an exception.)
	     *
	     * @return <tt>true</tt> if the iterator has more elements.
	     */
	    public boolean hasNext() {
	      return i.hasNext();
	    }

	    /**
	     * Returns the next element in the iteration.
	     *
	     * @return the next element in the iteration.
	     * @exception NoSuchElementException iteration has no more elements.
	     */
	    public Object next_test() {
	    	List v = (List) i.next();
	    	StringBuffer out = new StringBuffer();
	    	for (int j = 0; j < v.size(); j++) {
	    		out.append(v.get(j).toString());
	    		out.append(lineSeparator);
	    	}
	    	//out.append(lineSeparator);
	    	return out.toString();
	    }
	    
	    /**
	     * Returns the next element in the iteration.
	     *
	     * @return the next element in the iteration.
	     * @exception NoSuchElementException iteration has no more elements.
	     */
	    public Object next() {
	      List v = (List) i.next();
	      StringBuffer out = new StringBuffer();
	      for (int j = 0; j < v.size(); j++) {
	        if (j == 0)
	          out.append(v.get(j).toString());
	        else
	          out.append(delimiter + v.get(j).toString());
	      }
	      out.append(lineSeparator);
	      return out.toString();
	    }

	    /**
	     *
	     * Removes from the underlying collection the last element returned by the
	     * iterator (optional operation).  This method can be called only once per
	     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
	     * the underlying collection is modified while the iteration is in
	     * progress in any way other than by calling this method.
	     *
	     * @exception UnsupportedOperationException if the <tt>remove</tt>
	     *		  operation is not supported by this Iterator.

	     * @exception IllegalStateException if the <tt>next</tt> method has not
	     *		  yet been called, or the <tt>remove</tt> method has already
	     *		  been called after the last call to the <tt>next</tt>
	     *		  method.
	     */
	    public void remove() {
	      i.remove();
	    }
	}

  protected String fileName;
  protected boolean writeHeader = true;
  
  
  // A vector of vectors
  private ArrayList data = new ArrayList();
  //private String header = "\"tick\"";
  private String header = "";

  //private String modelHeader;

  private String delimiter = ",";
  private String lineSeparator = "\n";

  /**
   * Constructs a new SimData object from the model header, model and batch flag
   * @param modelHeader
   * the file header.
   */

  //public FlexiDataOutput(String modelHeader){
  public TabularDataWriter(String sFileName){
   // this.modelHeader = modelHeader;
	fileName=sFileName;
    lineSeparator = System.getProperty("line.separator");
  }

  public String getHeader() {
    return header;
  }

 /* public String getModelHeader() {
    return modelHeader;
  } */

  /**
   * Sets the column delimiter. Data is written out in tabular format
   * where the columns are separated by the specified delimiter.
   *
   * @param delim the new delimiter
   */
  public void setDelimiter(String delim) {
    header = Strings.change(header, delimiter, delim);
    delimiter = delim;
  }

  /**
   * Adds the specified String to the header associated with this data
   */
  public void addToHeader(String s) {
    if (header.length() == 0) {
      header += "\"" + s + "\"";
    }else if(s.equalsIgnoreCase("run")){
      header = "\"" + s + "\"" + delimiter + header;
    } else {
      header += delimiter + "\"" + s + "\"";
    }
  }

  /**
   * Adds the specified list to the header associated with this data
   */
  public void addToHeader(List l) {
    ListIterator li = l.listIterator();
    while (li.hasNext()) {
      String s = (String) li.next();
      if (header.length() == 0) {
        header += s;
      } else {
        header += delimiter + s;
      }
    }
  }

  /**
   * Add a vector of data to this SimData.
   * @param v the data to add.
   */
  public void addData(List v) {
    data.add(v);
  }


  /**
   * Get the data stored in this SimData object and clear this object.  This is the
   * preferred way of recording data.  Get the data as a String and return it to the
   * recorder object.
   * @return The data currently in the SimData object.
   */
  public String getData(){
    StringBuffer out = new StringBuffer();
    // write all the data and clear the vector

    for (int i = 0; i < data.size(); i++) {
      List v = (List) data.get(i);
      for (int j = 0; j < v.size(); j++) {
        if (j == 0)
          out.append(v.get(j).toString());
        else
          out.append(delimiter + v.get(j).toString());
      }
      out.append(lineSeparator);
    }
    data.clear();
    return out.toString();
  }

  public void clearData(){
    this.data.clear();
  }

  public Iterator iterator(){
    return new TabularDataIterator();
  }
  
  
  ///++++++++++++++++++++Down from here: LocalDataRecorder
  /**
   * Writes the recorded data out to a file in tabular format. This also
   * does a flush on the data itself (i.e. the data is no longer stored by
   * repast and exists only in the file). Identical to write().
   */
  public void writeToFile() {
    BufferedWriter out = null;
    try {
      if (writeHeader) {
        renameFile();
        out = new BufferedWriter(new FileWriter(fileName, true));
       
        writeHeader = false;
      }

      if (out == null)
        out = new BufferedWriter(new FileWriter(fileName, true));

      for (int i=0; i<((ArrayList)data.get(0)).size();i++) {
    	  for(int j=0; j<data.size(); j++) {
    		  out.write(((ArrayList) data.get(j)).get(i).toString());
    		  out.append(delimiter);
        	  
          }
    	  out.newLine();
    	  
      }
      /*
      Iterator i = this.iterator();
    	  
      while(i.hasNext()){
        out.write((String) i.next());
      } */
      out.flush();
      out.close();
    } catch (IOException ex) {
      SimUtilities.showError("Unable to write data to file", ex);
      ex.printStackTrace();
      try {
        out.flush();
        out.close();
      } catch (Exception ex1) {
      }
      System.exit(0);
    }
    this.clearData();
  }
  
  
  ///++++++++++++++++++++Down from here: LocalDataRecorder
  /**
   * Writes the recorded data out to a file in tabular format. This also
   * does a flush on the data itself (i.e. the data is no longer stored by
   * repast and exists only in the file). Identical to write().
   */
  public void writeToFile_old() {
    BufferedWriter out = null;
    try {
      if (writeHeader) {
        renameFile();
        out = new BufferedWriter(new FileWriter(fileName, true));
        //out.write(data.getModelHeader());
       // out.newLine();
        out.newLine();
        out.write(this.getHeader());
        out.newLine();
       
        writeHeader = false;
      }

      if (out == null)
        out = new BufferedWriter(new FileWriter(fileName, true));

      Iterator i = this.iterator();
      while(i.hasNext()){
        out.write((String) i.next());
      }
      out.flush();
      out.close();
    } catch (IOException ex) {
      SimUtilities.showError("Unable to write data to file", ex);
      ex.printStackTrace();
      try {
        out.flush();
        out.close();
      } catch (Exception ex1) {
      }
      System.exit(0);
    }
    this.clearData();
  }
  
  private void renameFile() throws IOException {
	    File oldFile = new File(fileName);
	    fileName = oldFile.getCanonicalPath();

	    if (oldFile.exists()) {
	      int x = 1;
	      File newFile;
	      String newName = fileName;
	      String lastPart = "";

	      if (fileName.indexOf(".") != -1) {
	        int index = fileName.lastIndexOf(".");
	        newName = fileName.substring(0, index);
	        lastPart = fileName.substring(index, fileName.length());
	      }
	     // IController iController = (IController) getController();
	      if (Const.IS_BATCH_MODE) {
	        newName += "_r"+HAAMoSModel.iController.getRunCount()+".bak";
	        //newName += ".bak";

	      }
	      

	      do {
	        newFile = new File(newName + x + lastPart);
	        x++;
	      } while (newFile.exists());
	      oldFile.renameTo(newFile);
	      oldFile.delete();
	    }
	  }
  
  /**
   * Writes the recorded data out to a file in tabular format. This also
   * does a flush on the data itself (i.e. the data is no longer stored by
   * repast and exists only in the file). Identical to writeToFile().
   */
  public void write() {
    writeToFile();
  }

  /**
   * Writes any ending matter to the file. Used internally during a batch run
   * to write the ending time of the entire batch. A model would not
   * typically call this method.
   */

  public void writeEnd() {
    BufferedWriter out = null;
    try {
      // has not written anything out yet
      if (writeHeader) {
        writeToFile();
      }

      out = new BufferedWriter(new FileWriter(fileName, true));
      Date date = new Date();
      String dateTime = DateFormat.getDateTimeInstance().format(date);
      out.newLine();
      out.write("End Time: " + dateTime);
      out.flush();
      out.close();
    } catch (IOException ex) {
      SimUtilities.showError("Unable to write footer to file", ex);
      ex.printStackTrace();
      try {
        out.flush();
        out.close();
      } catch (Exception ex1) {
      }
      System.exit(0);
    }
  }

  
    
   
  
}


