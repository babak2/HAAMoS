/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */

package nz.ac.auckland.compass.haamos.space;

import java.awt.Color;
import java.awt.Paint;
import anl.repast.gis.OpenMapAgent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMArc;
import com.bbn.openmap.omGraphics.OMGraphic;
import java.awt.geom.Arc2D; 

import nz.ac.auckland.compass.haamos.base.Const;

/**
 * Displays GisArealUnitAgents population graphically (in a form of pie charts)
 * 
 * @author Babak Mahdavi Ardestani
 */

public class GroupPopInfo implements OpenMapAgent {


	int groupID;
	OMCircle omCircle;
	OMArc omArc;
	float start;
	float extend;
	AreaUnit refToAU;
	static int idNb=1;
	int agentIndex;


	public GroupPopInfo(int gID, AreaUnit au, float lat, float lon, float rad,  float s, float e) {
		this.setGisAgentIndex(GroupPopInfo.idNb);
		GroupPopInfo.idNb++;

		this.start = s;
		this.extend = e;
		omArc = new OMArc(lat, lon, rad,start,extend);
		omArc.setArcType(Arc2D.PIE);
		this.setRadius(rad);
		this.setGroupID(gID);
		this.refToAU = au;

	}


	public Paint getFillPaint() {		

		if (this.groupID==Const.G1_EURO) {
			return Const.G1_EURO_Color;
		}
		if (this.groupID==Const.G2_ASIAN) {
			return Const.G2_ASIAN_Color;
		}

		if (this.groupID==Const.G3_PACIFIC) {
			return Const.G3_PACIFIC_Color;
		}

		if (this.groupID==Const.G4_MAORI) {
			return Const.G4_MAORI_Color;
		}

		if (this.groupID==Const.G_Vacant) {
			return Const.G_Vacancy_Color;
		}


		return Color.BLACK;
	}

	public OMGraphic getOMGraphic() {
		return omArc;
	}

	public void setOMGraphic(OMGraphic omg) {
		if (omg instanceof OMCircle){
			this.omCircle= (OMCircle)omg;
		}

		if (omg instanceof OMArc){
			this.omArc= (OMArc)omg;
		}
	}
	

	public int getGisAgentIndex() {
		return agentIndex;
	}

	public void setGisAgentIndex(int index) {
		this.agentIndex=index;
	}

	public String[] gisPropertyList() {
		String[]props={"start","getStart", "Lat", "getLat", "Lon", "getLon"};
		return props;
	}

	public void setNeighbors(int[] neighbours) {
	}

	public int[] getNeighbors() {
		return null;
	}
	
	
	//------------------------

	public float getExtend(){
		return extend;
	}

	public void setExtend(float e) {
		omArc.setExtent(e);
		extend=e;
	}



	public float getStart() {
		return this.start;
	}

	public void setStart(float s) {
		omArc.setStart(s);
		start=s;
	}


	public float getLat() {
		return omArc.getLatLon().getLatitude();
	}


	public float getLon() {
		return omArc.getLatLon().getLongitude();
	}


	public float getRadius(){
		return omArc.getRadius();
	}


	public void setRadius(float r){
		omArc.setRadius(r);
	}

	public void setLat (float lat){
		omArc.setLatLon(lat,this.getLon());
	}

	public void setLon (float lon){
		omArc.setLatLon(this.getLat(),lon);
	}


	public int getGroupID(){
		return groupID;
	}

	public void setGroupID(int gID){
		this.groupID=gID;

	}
 
	public String toString(){
		return ("GroupPopInfoIndex id="+getGisAgentIndex() + "located in AU "+ refToAU.getAU_NAME());
	}

	
	public void nullifyMe() {
		this.omArc = null;
		this.omCircle=null;
	}


}
