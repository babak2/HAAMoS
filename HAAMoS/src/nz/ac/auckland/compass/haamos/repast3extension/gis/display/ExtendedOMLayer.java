/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.gis.display;

//this is the modified version of OMLayer

import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;



import anl.repast.gis.OpenMapAgent;
import anl.repast.gis.data.OpenMapData;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.dataAccess.shape.EsriGraphic;
import com.bbn.openmap.dataAccess.shape.EsriPolygon;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.location.Location;
import com.bbn.openmap.omGraphics.OMArc;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;

import anl.repast.gis.display.PropertyWindow;

/**
 * Extends Repast OMLayer
 */

public class ExtendedOMLayer extends OMGraphicHandlerLayer implements DataBoundsProvider{//implements  ActionListener {


    List agentList;
    OMGraphicList currentList;
    Paint highlightFillPaint;
    Paint originalFillPaint;
     boolean waitingToMove;
     LatLonPoint moveFromPoint;
     OpenMapAgent agentToMove;
    ExtendedOpenMapDisplay omDisplay;
    Point moveToPoint; 
    OpenMapAgent movedAgent;
    OMGraphic movedOMG;
    String title;
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
     public ExtendedOMLayer(ExtendedOpenMapDisplay omDisplay, String title) {
         this.omDisplay = omDisplay;
         this.title = title;
     }
     
     
     //Babak
     public void disposeMe() {
    	 
    	 this.agentList.clear();
    	 this.agentList = null;
    	 
    	 if (omDisplay != null)  {
    		// omDisplay.dispose();
    		 omDisplay= null;
    	 }
    	 
    	 if (currentList != null)
    		 currentList.clear();
    	 
    	 if (movedAgent != null)
    		 movedAgent = null;
    	 
    	 if (agentToMove != null)
    		 agentToMove = null;
    	 
    	 this.finalize();
    	     	 
     }
     
     /**
      * set the list of OpenMapAgents used by the layer
      * 
      * @param gisAgents
      */
    public void setAgentList(List omAgents) {
        this.agentList = omAgents;
    }
    
    
    /**
     * 
     * 
     * @param omg
     * @return OpenMapAgent
     */
    public OpenMapAgent getAgentFromOMGraphic(OMGraphic omg) {
        int index = currentList.indexOf(omg);
        return (OpenMapAgent)agentList.get(index);
    }
    
    /**
     * find agent closet to x,y location
     * 
     * @param x
     * @param y
     * @return OpenMapAgent
     */
    public OpenMapAgent findClosestAgent(int x, int y) {
        OMGraphic omg = currentList.findClosest(x,y);
        return this.getAgentFromOMGraphic(omg);
    }
    
    /**
     * find agent closet to  Point
     * 
     * @param p
     * @return OpenMapAgent
     */
    public OpenMapAgent findClosestAgent(java.awt.Point p) {
        OMGraphic omg = currentList.findClosest(p.x, p.y);
        return this.getAgentFromOMGraphic(omg);
        
    }
    
    /**
     * find agent closet to  Point within limit
     * 
     * @param p
     * @return OpenMapAgent
     */
    public OpenMapAgent findClosestAgent(java.awt.Point p, float limit) {
        OMGraphic omg = currentList.findClosest(p.x, p.y, limit);
        return this.getAgentFromOMGraphic(omg);
    }
    
    /**
     * find agent closet to x,y  location within limit
     * 
     * @param p
     * @return OpenMapAgent
     */
    public OpenMapAgent findClosestAgent(int x, int y, float limit) {
        OMGraphic omg = currentList.findClosest(x,y, limit);
        return this.getAgentFromOMGraphic(omg);
    }
  
    public synchronized OMGraphicList prepare() {
        
        OMGraphicList omList = new OMGraphicList();
        Collection gisAgents = this.getAgentList();
        if (gisAgents != null ) {
            for (Iterator iter = gisAgents.iterator(); iter.hasNext();) {
                OpenMapAgent agent = (OpenMapAgent)iter.next();
                OMGraphic omg = agent.getOMGraphic();
                omg.setFillPaint(((OpenMapAgent)agent).getFillPaint());
                omList.add(omg);
            }
        }
        
        this.setList(omList);
        Projection proj = getProjection(); 

        // go through the omg list and make sure the colors et al are set properly
        currentList = getList();
        
        // if the layer hasn't been added to the MapBean 
        // the projection could be null.
        if (currentList != null && proj != null) {
            currentList.generate(proj);
        }
        
        return currentList;
    }


    /**
     * Designate a list of OMGraphics as selected.
     */
    public void select(OMGraphicList list) {
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (selectedList == null) {
                    selectedList = new OMGraphicList();
                }

                OMGraphic omg = (OMGraphic)it.next();
                if (omg instanceof OMGraphicList && !((OMGraphicList)omg).isVague()) {
                    select((OMGraphicList)omg);
                } else {
                    selectedList.add(omg);
                }
            }
        }
    }

    
    /**
     * Designate a list of OMGraphics as deselected.
     */
    public void deselect(OMGraphicList list) {
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext() && selectedList != null) {
                OMGraphic omg = (OMGraphic)it.next();
                if (omg instanceof OMGraphicList && !((OMGraphicList)omg).isVague()) {
                    deselect((OMGraphicList)omg);
                } else {
                    selectedList.remove(omg);
                }
            }
        }
    }


   
    ////// Reactions

    /**
     * Fleeting change of appearance for mouse movements over an OMGraphic. 
     */
    public void highlight(OMGraphic omg) {
        originalFillPaint = omg.getFillPaint();
        omg.setFillPaint(highlightFillPaint);
        omg.select();
        omg.generate(getProjection());
        repaint();
    }

    /**
     * Notification to set OMGraphic to normal appearance.
     */
    public void unhighlight(OMGraphic omg) {
       
        omg.setFillPaint(originalFillPaint);
        omg.deselect();
        omg.generate(getProjection());
        repaint();
    }

    /**
     * Query asking if an OMGraphic is selectable, or able to be
     * moved, deleted or otherwise modified. Responding true to this
     * method may cause select() to be called (depends on the
     * MapMouseInterpertor) so the meaning depends on what the layer
     * does in select.
     */
    public boolean isSelectable(OMGraphic omg) {
        return true;
    }

    /**
     * If applicable, should return a tool tip for the OMGraphic.
     * Return null if nothing should be shown.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        OpenMapAgent agent = (OpenMapAgent)agentList.get(currentList.indexOf(omg));
        String [] props = agent.gisPropertyList();
        
       
        Class clazz = agent.getClass();
        StringBuffer s = new StringBuffer("<HTML><BODY>");
        if (props != null && props.length > 0) {
        try {
            
             for (int i=0;i<props.length;i+=2) {
                 Method method = clazz.getMethod(props[i+1], null);
                 s.append("<b>" + props[i] + "</b>&nbsp;&nbsp;" + method.invoke(agent, null) + "<BR>");
             }
             
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (IllegalArgumentException e) {
           
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
        s.append("</BODY></HTML>");
        }
        return s.toString();
    }

    /**
     * If applicable, should return a short, informational string
     * about the OMGraphic to be displayed in the
     * InformationDelegator.  Return null if nothing should be
     * displayed.
     */
    public String getInfoText(OMGraphic omg) {
        OpenMapAgent agent = (OpenMapAgent)agentList.get(currentList.indexOf(omg));
        Class clazz = agent.getClass();
        Method method;
             
                  try {
                    method = clazz.getMethod("getGisAgentIndex", null);
                    return ("" + method.invoke(agent, null));
                    
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
              return "";
        
    }

    /**
     * Return a java.util.List containing input for a JMenu with
     * contents applicable to a popup menu for a location over an
     * OMGraphic.
     * @return a List containing options for the given OMGraphic.
     * Return null or empty list if there are no options.
     */
    public java.util.List getItemsForOMGraphicMenu(OMGraphic omg) {
       ArrayList list = new ArrayList();
       //System.out.println("ExtendedOMLayer: getItemsForOMGraphicMenu called");
       JMenuItem propertiesMenuItem = new JMenuItem("Properties");
       PropertiesAction propertiesAction = new PropertiesAction("Properties", omg, this.currentList, this.getAgentList());
       propertiesMenuItem.setAction(propertiesAction);
        list.add(propertiesMenuItem);
        
        JMenuItem separator = new JMenuItem();
        list.add(separator);
        /*
        if ((omg instanceof OMPoly) == false) { // can't move OMPoly
            JMenuItem moveMenuItem = new JMenuItem("Move Agent");
            MoveAction moveAction = new MoveAction("Move Agent", omg, this);
            moveMenuItem.setAction(moveAction);
            list.add(moveMenuItem);
        }
        JMenuItem cancelMoveMenuItem = new JMenuItem("Cancel Move Agent");
        CancelMoveAction cancelMoveAction = new CancelMoveAction("Cancel Move Agent", this);
        cancelMoveMenuItem.setAction(cancelMoveAction);
        list.add(cancelMoveMenuItem);
        
        
        
        JMenuItem undoMoveMenuItem = new JMenuItem("Undo Move Agent");
        UndoMoveAction undoMoveAction = new UndoMoveAction("Undo Move Agent", this);
        undoMoveMenuItem.setAction(undoMoveAction);
        list.add(undoMoveMenuItem);  */
        
        
        return list;
    }

    /**
     * returns a list of JMenuItems when the user right clicks on the map 
     * in a location where there is no OMGraphic (no agent)
     * 
     */
    public java.util.List getItemsForMapMenu(MapMouseEvent mme) {
        ArrayList list = new ArrayList();
        System.out.println("HaamosOMLayer: getItemsForMapMenu -- called");
        
        JMenuItem cancelMoveMenuItem = new JMenuItem("Cancel Move Agent");
        CancelMoveAction cancelMoveAction = new CancelMoveAction("Cancel Move Agent", this);
        cancelMoveMenuItem.setAction(cancelMoveAction);
        list.add(cancelMoveMenuItem);
        
        JMenuItem undoMoveMenuItem = new JMenuItem("Undo Move Agent");
        UndoMoveAction undoMoveAction = new UndoMoveAction("Undo Move Agent", this);
        undoMoveMenuItem.setAction(undoMoveAction);
        list.add(undoMoveMenuItem);
        
        return list;
    }
    
    public boolean receivesMapEvents(){
        return true;
    }
    
    
    public LatLonPoint getCenter(float []  extents) {
	      //extents: the lat/lon extent of the EsriGraphicList contents, 
	      //returned as  miny, minx, maxy maxx in order of the array.
	    LatLonPoint center = new LatLonPoint();
	      
	    center.setLatitude((extents[0] + extents[2]) / 2);
	    center.setLongitude((extents[1] + extents[3]) / 2);
	    return center;
	  }
	  
  public float [] getExtents(OMGraphic omg) {
	      if (omg instanceof EsriGraphic) {
	          return ((EsriGraphic)omg).getExtents();
	      }
	      else if (omg instanceof OMPoint) {
	          float points [] = new float[2];
	          points[0] = ((OMPoint)omg).getLat();
	          points[1] = ((OMPoint)omg).getLon();
	          return points;
	      }
	         else if (omg instanceof OMPoly) {
	             EsriPolygon ep =EsriPolygon.convert((OMPoly)omg);
	             return ep.getExtents();
	         }
	        
	         return null;
	  }
  
  public void setWaitingToMove(boolean waiting) {
      this.waitingToMove = waiting;
  }
  
  public void setAgentToMove(OpenMapAgent agent){
      agentToMove = agent;
  }
  
  public OpenMapAgent getAgentToMove() {
      return agentToMove;
  }

  
  public void setMoveFromPoint(LatLonPoint point){
      this.moveFromPoint = point;
  }
  
  public LatLonPoint getMoveFromPoint(OMGraphic omg) {
      LatLonPoint llp = null;
      if (omg instanceof OMArc) {
          llp = ((OMArc)omg).getCenter();
      }
      else if (omg instanceof Location) {
          llp = new LatLonPoint(((Location)omg).lat, ((Location)omg).lon);
      }
      else {
           llp =  this.getCenter(this.getExtents(omg));
      }
      return llp;
  }
  
    public static void main(String[] args) {
    }

    public void undoMoveAgent() {
        
        Class clazz = movedAgent.getClass();
       
        Method method;
        //OMGraphic omg = null;
        try {
            method = clazz.getMethod("getOMGraphic", null);
            method.invoke(movedAgent, null);
            
            this.moveAgent(moveFromPoint);
            
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
    }
    
   
    public void moveAgent(LatLonPoint llp) {
        this.moveAgent(omDisplay.latLonToPoint(llp));
    }
    
    public void moveAgent(Point p) {
        this.moveAgent(p.x, p.y);
    }
    
    public void moveAgent(int x, int y) {
        OpenMapAgent agent = this.getAgentToMove();
         movedAgent = agent;
         movedOMG = null;
        Class clazz = agent.getClass();
        Method method;
        try {
            method = clazz.getMethod("getOMGraphic", null);
            OMGraphic omg = (OMGraphic) method.invoke(agent, null);
            
            movedOMG = omg;
            
            if (omg instanceof OMPoint) {
                LatLonPoint llp = this.omDisplay.xyToLatLon(x,y);
                ((OMPoint)omg).setLat(llp.getLatitude());
                ((OMPoint)omg).setLon(llp.getLongitude());
         
                Object [] params = new Object[1];
                params[0] = omg;
                Class [] classParams = new Class[1];
                classParams[0] = OMGraphic.class;
                method = clazz.getMethod("setOMGraphic", classParams);//, (Class[]) params);
                method.invoke(agent, params);
                this.doPrepare();
            }
            else if (omg instanceof OMPoly) {
                return;
                /*
                int index = currentList.indexOf(omg);
                System.out.println("== left clcik - move Poly");
                LatLonPoint llp = this.omDisplay.xyToLatLon(x,y);
                int [] origXs = ((OMPoly)omg).getXs();
                int [] origYs = ((OMPoly)omg).getYs();
                float [] latLons = ((OMPoly)omg).getLatLonArray();
                if (latLons == null) System.out.println("LatLon Array is null");
                LatLonPoint centerLLP = this.getCenter(this.getExtents(omg));
                
                // go through the list of lat lon points
                for (int i=0; i< latLons.length; i+=2) {
                    latLons[i] = latLons[i] + (centerLLP.getLatitude() - llp.getLatitude());
                    latLons[i+1] = latLons[i+1] + (centerLLP.getLongitude() - llp.getLongitude());
                }
                ((OMPoly)omg).setLocation(latLons, OMGraphic.RADIANS);//DECIMAL_DEGREES);
                currentList.setOMGraphicAt(omg, index);
//                ((OMPoly)omg).setVisible(true);
                for (int j=0; j<latLons.length; j++) {
                    System.out.print("" + latLons[j] + "  ");
                }
                System.out.println("");
   */             
            //    System.out.println("new poly location: " + ((OMPoly)omg).getLat() + "   " + ((OMPoly)omg).getLon());
          /*      java.awt.Point center = this.omDisplay.latLonToPoint(llp);
                int [] newXs = new int[origXs.length];
                for (int i=0; i<origXs.length; i++) {
                    newXs[i] = origXs[i] + (center.x - x);
                }
                int [] newYs = new int[origYs.length];
                for (int i=0; i<origYs.length; i++) {
                    newYs[i] = origYs[i] + (center.y - y);
                }
//                ((OMPoly)omg).setLat(llp.getLatitude());
  //              ((OMPoly)omg).setLon(llp.getLongitude());
               ((OMPoly)omg).setLocation(newXs, newYs);
//                ((OMPoly)omg).setYs(newYs);
              */ 
/*                Object [] params = new Object[1];
                params[0] = omg;
                Class [] classParams = new Class[1];
                classParams[0] = OMGraphic.class;
                method = clazz.getMethod("setOMGraphic", classParams);//, (Class[]) params);
                method.invoke(agent, params);
                
                method = clazz.getMethod("getOMGraphic", null);//, (Class[]) params);
                OMGraphic omg2 = (OMGraphic) method.invoke(agent, null);
                if (omg2 == null) {
                    System.out.println("omg 2 is null");
                }
                else System.out.println("omg 2 NOT null");
                
                System.out.println("omg2 latlon points =======");
                float [] latLons2 = ((OMPoly)omg2).getLatLonArray();
                for (int j=0; j<latLons2.length; j++) {
                    System.out.print("" + latLons2[j] + "  ");
                }
                //omg.regenerate(this.getProjection());
                this.doPrepare();
                this.prepare();*/
            }
            else if (omg instanceof OMArc) {
                LatLonPoint llp = this.omDisplay.xyToLatLon(x,y);
                ((OMArc)omg).setLatLon(llp.getLatitude(), llp.getLongitude());
         
                Object [] params = new Object[1];
                params[0] = omg;
                Class [] classParams = new Class[1];
                classParams[0] = OMGraphic.class;
                method = clazz.getMethod("setOMGraphic", classParams);//, (Class[]) params);
                method.invoke(agent, params);
                this.doPrepare();
            }
            else if (omg instanceof Location) {
                LatLonPoint llp = this.omDisplay.xyToLatLon(x,y);
                ((Location)omg).setLocation(llp.getLatitude(), llp.getLongitude());
         
                Object [] params = new Object[1];
                params[0] = omg;
                Class [] classParams = new Class[1];
                classParams[0] = OMGraphic.class;
                method = clazz.getMethod("setOMGraphic", classParams);
                method.invoke(agent, params);
                this.doPrepare();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
    }
    
    public void setMoveToPoint(Point p) {
        this.moveToPoint = p;
    }
    
    public boolean leftClick(MapMouseEvent evt) {
        if (waitingToMove == true) {
            waitingToMove = false;
            setMoveToPoint(new Point(evt.getX(), evt.getY()));
            moveAgent(evt.getX(), evt.getY());
        }
        return true;
    }
   
    public boolean mouseOver(MapMouseEvent evt) {
     
      return true;
    }
    
    public Paint getHighlightFillPaint() {
        return highlightFillPaint;
    }
    public void setHighlightFillPaint(Paint highlightFillPaint) {
        this.highlightFillPaint = highlightFillPaint;
    }
  
public List getAgentList() {
    return agentList;
}



public void ActionPerformed(ActionEvent evt) {
}


public DataBounds getDataBounds() {
   
    OpenMapData gisData = OpenMapData.getInstance();
    float [] extents = gisData.getExtents(gisData.buildEsriGraphicList(this.getAgentList()));
    DataBounds dataBounds = new DataBounds((double)extents[1], (double)extents[0], (double)extents[3], (double)extents[2]);
    return dataBounds;
}

public String getName() {
    return this.title;
}
}

    
class CancelMoveAction extends AbstractAction {
    OMGraphic omg;
    ExtendedOMLayer layer;
    
    public CancelMoveAction(String name, ExtendedOMLayer layer) {
        super(name);
        this.layer = layer;
    }
    
    public void actionPerformed(ActionEvent e) {
        layer.setWaitingToMove(false);
    }
    
}
    


class MoveAction extends AbstractAction {
    OMGraphic omg;
    ExtendedOMLayer layer;
    
    public MoveAction(String name, OMGraphic omg, ExtendedOMLayer layer) {
        super(name);
        this.omg = omg;
        this.layer = layer;
    }
    
    public void actionPerformed(ActionEvent e) {
        LatLonPoint fromLLP = layer.getMoveFromPoint(omg);
        
        
       layer.setWaitingToMove(true);
       layer.setAgentToMove(layer.getAgentFromOMGraphic(omg));
       layer.setMoveFromPoint(fromLLP);
    }
    
}


class UndoMoveAction extends AbstractAction {
    OMGraphic omg;
    ExtendedOMLayer layer;
    
    public UndoMoveAction(String name, ExtendedOMLayer layer) {
        super(name);
        this.layer = layer;
    }
    
    public void actionPerformed(ActionEvent e) {
       layer.undoMoveAgent();
    }
    
}

class PropertiesAction extends  AbstractAction {
OMGraphic omg;
OMGraphicList omList;
Collection agentList;

    public PropertiesAction(String name, OMGraphic omg, OMGraphicList omList, Collection agentList) {
        super(name);
        this.omg = omg;
        this.omList = omList;
        this.agentList = agentList;
    }
    public void actionPerformed(ActionEvent e) {
        
        OpenMapAgent agent = (OpenMapAgent)((ArrayList) agentList).get(omList.indexOf(omg));
        ExtendedPropertyWindow propWindow = new ExtendedPropertyWindow(agent);
        propWindow.setVisible(true);
    }
    
}
