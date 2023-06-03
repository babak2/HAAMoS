/**
 * This file is part of <em>HAAMoS</em> model: ©Copyright 2011 Babak Mahdavi Ardestani <p>
 */
package nz.ac.auckland.compass.haamos.repast3extension.gis.display;


import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BoxLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import anl.repast.gis.GisAgent;

import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Color;

import javax.swing.*;
import java.util.Locale;
import java.awt.BorderLayout;

import nz.ac.auckland.compass.haamos.awt.SpringUtilities;
import nz.ac.auckland.compass.haamos.base.Const;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PiePlot;


class PieRenderer {
	private Color[ ] color;

	public PieRenderer(Color[ ] color)
	{
		this.color = color;
	}

	/**
	 * Set Method to set colors for pie sections based on our choice
	 * @param plot PiePlot of PieChart
	 * @param dataset PieChart DataSet
	 */
	public void setColor(PiePlot plot,
			DefaultPieDataset dataset)
	{
		List keys = dataset.getKeys();
		int aInt;

		for (int i =0; i <	keys.size(); i++)
		{
			aInt = i % this.color.length;
			plot.setSectionPaint(
					keys.get(i).toString(),this.color[aInt]);
		}
	}
}


/**
 * Extends Repast PropertyWindow 
 */

public class ExtendedPropertyWindow  extends JFrame implements ActionListener {

	Container container;
	GisAgent gisAgent;
	String [] props;
	String [] initialProps;
	//int nbOfGroupColorNeeded=0;
	//DefaultPieDataset currentPieDataset;
	//DefaultPieDataset initialPieDataset; 


	public ExtendedPropertyWindow() {

	}
	public ExtendedPropertyWindow(GisAgent agent) {
		super("GisAgent: ");// + agent.getGisAgentIndex());
		this.gisAgent = agent;


		props = gisAgent.gisPropertyList();

		//gisAgent.
		container = this.getContentPane();
		buildAgentInfoPanel();
		this.pack();
		this.setVisible(true);
		//show();
	}
	/**
	 * A debugging utility that prints to stdout the component's
	 * minimum, preferred, and maximum sizes.
	 */
	public static void printSizes(Component c) {
		System.out.println("minimumSize = " + c.getMinimumSize());
		System.out.println("preferredSize = " + c.getPreferredSize());
		System.out.println("maximumSize = " + c.getMaximumSize());
	}

	private Color[] getColors(int numberOfNeededColor) {

		switch (numberOfNeededColor) { 
		case 3: 
			Color[] colors3 = {Const.G1_EURO_Color, Const.G2_ASIAN_Color, Const.G_Vacancy_Color};
			return colors3;
		case 4:
			Color[] colors4 = {Const.G1_EURO_Color, Const.G2_ASIAN_Color, Const.G3_PACIFIC_Color,
					Const.G_Vacancy_Color};
			return colors4;
		case 5:
			Color[] colors5 = {Const.G1_EURO_Color, Const.G2_ASIAN_Color, Const.G3_PACIFIC_Color,
					Const.G4_MAORI_Color, Const.G_Vacancy_Color};
			return colors5;
		}

		Color[] colors = {Const.G1_EURO_Color, Const.G2_ASIAN_Color, Const.G3_PACIFIC_Color,
				Const.G4_MAORI_Color, Const.G_Vacancy_Color};
		return colors;

	}

	public JPanel buildAndGetPieChartPanel(DefaultPieDataset pieDataset, 
			int nbOfGroupColorNeeded,
			String chartTitle){
		/*DefaultPieDataset pieDataset1 = new DefaultPieDataset();
		pieDataset1.setValue("Euro", new Integer(40));
		pieDataset1.setValue("Asian", new Integer(30));
		pieDataset1.setValue("Pacific", new Integer(20));
		pieDataset1.setValue("Maori", new Integer(10));
		//JFreeChart chart = ChartFactory.createPieChart("Sample Pie Chart",pieDataset,true,true,false);
		 */
		//JFreeChart chart = ChartFactory.createPieChart("Sample Pie Chart",pieDataset,true,true,false);

		JFreeChart chart = ChartFactory.createPieChart
		//JFreeChart chart = ChartFactory.createRingChart
		(chartTitle,   // Title
				pieDataset,           // Dataset
				true,                 // Show legend  
				true,                 //tooltips
				false               
		); 

		/*
		JFreeChart chart = ChartFactory.createPieChart
		                     ("Current population percentage",   // Title
		                      pieDataset1,           // Dataset
		                      pieDataset2,           // Dataset
		                      1,
		                      true,
		                      true,                 // Show legend
		                      true,                 //tooltips
		                      Locale.getDefault(),
		                      true,                 
		                      false               
		                     ); */

		// JFreeChart chart = createChart(createDataset());		

		JPanel chartPanel = new ChartPanel(chart);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setSectionOutlinesVisible(false);
		plot.setNoDataMessage("No data available");

		// plot.setBackgroundAlpha(alpha)

		Color[] colors = getColors(nbOfGroupColorNeeded);

		/* Delegating the choice of color to an inner class */
		PieRenderer renderer = new PieRenderer(colors);
		renderer.setColor(plot, pieDataset);
		//this.setContentPane(chartPanel);
		//chartPanel.setMaximumSize(chartPanel.getPreferredSize());
		chartPanel.setPreferredSize(new Dimension(380,210));
		//chartPanel.setBackground(Color.YELLOW);
		//this.printSizes(chartPanel);
		return chartPanel;

	}

	public void buildAgentInfoPanel() {

		int nbOfGroupColorNeeded=0;
		DefaultPieDataset currentPieDataset = new DefaultPieDataset();
		DefaultPieDataset initialPieDataset = new DefaultPieDataset();


		JPanel valuePanel = new JPanel(new SpringLayout());
		Class clazz = gisAgent.getClass();
		int rows =0;
		int cols =2;
		//int nbOfGroupColorNeeded=0;

		try {

			if (props != null && props.length > 0) {

				valuePanel.setLayout(new BoxLayout(valuePanel,BoxLayout.Y_AXIS));

				// first check that the user has not added gisAgentIndex to props list (is so don't display twice
				boolean indexCalled = false;
				for (int i=0;i<props.length;i+=2) {
					if (props[i+1].equalsIgnoreCase("getGisAgentIndex")) {
						indexCalled = true;
					}
				}

				if (!indexCalled) {
					// Agent ID: JTextArea
					JPanel idPanel = new JPanel();
					JLabel idLabel = new JLabel("Agent ID:");
					idPanel.add(idLabel);
					Method indexMethod = clazz.getMethod("getGisAgentIndex", null);
					JTextField idField = new JTextField("" + indexMethod.invoke(gisAgent, null));
					//idField.setEditable(false);
					//idField.setEnabled(false);
					//idField.setDisabledTextColor(Color.YELLOW);
					idPanel.add(idField);
					valuePanel.add(idPanel);
					rows++;
				}

				for (int i=0;i<props.length;i+=2) {
					//    Method method;

					JPanel propsPanel = new JPanel();


					propsPanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createEtchedBorder(1),
							propsPanel.getBorder()));


					JLabel propsLabel = new JLabel("" + props[i] + ":");
					propsPanel.add(propsLabel);
					Method propsMethod = clazz.getMethod(props[i+1], null);
					Object valObj= propsMethod.invoke(gisAgent, null);

					if (props[i].equals("G1_Euro_cur")) {
						currentPieDataset.setValue(props[i], (Number)valObj);
						nbOfGroupColorNeeded++;
					}
					if (props[i].equals("G2_Asian_cur")) {
						currentPieDataset.setValue(props[i], (Number)valObj);
						nbOfGroupColorNeeded++;
					}
					if (props[i].equals("G3_Pac_cur")) {
						currentPieDataset.setValue(props[i], (Number)valObj);
						nbOfGroupColorNeeded++;
					}
					if (props[i].equals("G4_Maori_cur")) {
						currentPieDataset.setValue(props[i], (Number)valObj);
						nbOfGroupColorNeeded++;
					}
					if (props[i].equals("Vacancy_cur")) {
						currentPieDataset.setValue(props[i], (Number)valObj);
						nbOfGroupColorNeeded++;
					}
					//---------------------Initial Values 

					if (props[i].equals("G1_Euro_ini")) {
						initialPieDataset.setValue(props[i], (Number)valObj);
						//propsLabel.setBackground(Color.pink);
						propsPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(Color.YELLOW),
								propsPanel.getBorder()));
					}
					if (props[i].equals("G2_Asian_ini")) {
						initialPieDataset.setValue(props[i], (Number)valObj);
						propsPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(Color.YELLOW),
								propsPanel.getBorder()));
					}
					if (props[i].equals("G3_Pac_ini")) {
						initialPieDataset.setValue(props[i], (Number)valObj);
						propsPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(Color.YELLOW),
								propsPanel.getBorder()));
					}
					if (props[i].equals("G4_Maori_ini")) {
						initialPieDataset.setValue(props[i], (Number)valObj);
						propsPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(Color.YELLOW),
								propsPanel.getBorder()));
					}
					if (props[i].equals("Vacancy_ini")) {
						initialPieDataset.setValue(props[i], (Number)valObj);
						propsPanel.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(Color.YELLOW),
								propsPanel.getBorder()));
					}

					JTextField propsField = new JTextField("" + propsMethod.invoke(gisAgent, null));
					//propsPanel.add(Box.createRigidArea(new Dimension(5,0)));
					propsPanel.add(propsField);
					valuePanel.add(propsPanel);
					rows++;

				} 

			}
			else { // props is null or length is zero  - at least show agent id
				JPanel idPanel = new JPanel();
				JLabel idLabel = new JLabel("Agent ID:");
				idPanel.add(idLabel);
				Method indexMethod = clazz.getMethod("getGisAgentIndex", null);
				JTextField idField = new JTextField("" + indexMethod.invoke(gisAgent, null));
				idPanel.add(idField);
				valuePanel.add(idPanel);
				rows++;
			}
			//  }catch (Exception e) {
			//    e.printStackTrace();
			// }
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		} catch (InvocationTargetException e4) {
			e4.printStackTrace(); 
		}

		SpringUtilities.makeCompactGrid(valuePanel, //parent
				rows, cols,
				3, 3,  //initX, initY
				3, 3); //xPad, yPad
		valuePanel.setOpaque(true); //content panes must be opaque


		JPanel mainPanel = new JPanel (new BorderLayout());
		//JPanel valuePanel = buildAndGetAgentInfoValuePanel();
		//prepareInitialPieDataset();
		mainPanel.add(valuePanel,BorderLayout.WEST);
		if (nbOfGroupColorNeeded>0){
			mainPanel.add(this.buildAndGetPieChartPanel(currentPieDataset, nbOfGroupColorNeeded, "Current population percentage"),BorderLayout.CENTER);
			mainPanel.add(this.buildAndGetPieChartPanel(initialPieDataset, nbOfGroupColorNeeded, "Initial population percentage"),BorderLayout.EAST);
		}
		//this.setContentPane(valuePanel);
		//this.printSizes(mainPanel);
		this.setContentPane(mainPanel);
		//return mainPanel;
		//container.add(generalPanel);


	}

	/*
	public void addAgentInfo() {


		Class clazz = gisAgent.getClass();
		//Object o = new Object();

		JPanel generalPanel = new JPanel();


		try {

			if (props != null && props.length > 0) {
				// panel to hold this all

				generalPanel.setLayout(new BoxLayout(generalPanel,BoxLayout.Y_AXIS));

				// first check that the user has not added gisAgentIndex to props list (is so don't display twice
				boolean indexCalled = false;
				for (int i=0;i<props.length;i+=2) {
					if (props[i+1].equalsIgnoreCase("getGisAgentIndex")) {
						indexCalled = true;
					}
				}

				if (!indexCalled) {
					// Agent ID: JTextArea
					JPanel idPanel = new JPanel();
					JLabel idLabel = new JLabel("Agent ID:");
					idPanel.add(idLabel);
					Method indexMethod = clazz.getMethod("getGisAgentIndex", null);
					JTextField idField = new JTextField("" + indexMethod.invoke(gisAgent, null));
					idPanel.add(idField);
					generalPanel.add(idPanel);
				}

				for (int i=0;i<props.length;i+=2) {
					//    Method method;



					JPanel propsPanel = new JPanel();
					propsPanel.setBorder(BorderFactory.createCompoundBorder(
			                   BorderFactory.createLineBorder(Color.red),
			                   propsPanel.getBorder()));
					JLabel propsLabel = new JLabel("" + props[i] + ":");
					propsPanel.add(propsLabel);
					Method propsMethod = clazz.getMethod(props[i+1], null);
					JTextField propsField = new JTextField("" + propsMethod.invoke(gisAgent, null));
					//propsPanel.add(Box.createRigidArea(new Dimension(5,0)));
					propsPanel.add(propsField);
					generalPanel.add(propsPanel);

				}

			}
			else { // props is null or length is zero  - at least show agent id
				JPanel idPanel = new JPanel();
				JLabel idLabel = new JLabel("Agent ID:");
				idPanel.add(idLabel);
				Method indexMethod = clazz.getMethod("getGisAgentIndex", null);
				JTextField idField = new JTextField("" + indexMethod.invoke(gisAgent, null));
				idPanel.add(idField);
				generalPanel.add(idPanel);
			}
			//  }catch (Exception e) {
			//    e.printStackTrace();
			// }
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		} catch (InvocationTargetException e4) {
			e4.printStackTrace(); 
		}


		container.add(generalPanel);

	}
	 */
	public static void main(String[] args) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
