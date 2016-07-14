/*
 * Delaunay triangulation and Voronoi diagram library for Capsis4.
 *
 * Copyright (C) 2004 Alexandre Piboule.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.lib.delaunay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jeeb.lib.util.Vertex2d;
import regelight.model.RegCrown;
import capsis.util.Polygon2D;

/**
 * DelaunayDemoTriangulation2 - demonstration program for the library.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunayDemoTriangulation2 {

	public static DelaunayWindow myFrame;

	public static DelaunayVertex vert1;
	public static DelaunayVertex vert2;
	public static DelaunayVertex vert3;

	public static DelaunayTriangulation triang1;
	public static ArrayList delauTriangles;
	public static ArrayList voroDiagram;

	public static DelaunayVertex vert0;
	public static DelaunayTriangle tri1;
	public static JButton bt1;
	public static JButton bt2;
	public static JCheckBox delauCB;
	public static JCheckBox voroCB;
	public static JCheckBox savingCB;
	public static JCheckBox cleanCB;
	public static JCheckBox withinCB;
	public static JSpinner sp1;
	public static JSpinner sp2;
	public static MyPanel drawing;
	public static JLabel label1;
	public static JLabel label2;

	public static Random rnd;
	public static boolean showWithin;
	public static double dist;
	public static ArrayList verticesWithin = null;

	public static long currentTime, delay;



	static class DelaunayWindow extends JFrame implements ActionListener  {


		DelaunayWindow (String s) {
			super (s);

			addWindowListener (new WindowAdapter () {
								public void windowClosing (WindowEvent e) {
									System.exit (0);
								}});
			setBounds (300,50,500,650);

			rnd = new Random ();

			vert1 = new DelaunayVertex (null,200,200);


			triang1 = new DelaunayTriangulation ();

			triang1.init (50,50,350,350);
			triang1.setSavingMode (false);

			delauTriangles = (ArrayList) triang1.getTriangles ();
			voroDiagram = (ArrayList) triang1.getVoronoiDiagram ();


			JPanel globalPanel = new JPanel ();
			JPanel panel = new JPanel (new GridLayout (6,1));
			JPanel panel1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			JPanel panel2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			JPanel panel3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			JPanel panel4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			JPanel panel5 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			JPanel panel6 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			globalPanel.setLayout (new BorderLayout ());

			drawing = new MyPanel ();

			// insertion position setting
			drawing.addMouseListener (new MouseAdapter () {
				public void mouseClicked (MouseEvent e) {

				vert1.x = e.getX ();
				vert1.y = e.getY ();
				//label2.setText ("Blue plus: x="+vert1.x+" y="+vert1.y);

				drawing.repaint ();

			}});

			drawing.setMaximumSize (new Dimension (400,400));

			delauCB = new JCheckBox ("Delaunay Triangles", true);
			delauCB.addActionListener (this);
			voroCB = new JCheckBox ("Voronoi Diagram", false);
			voroCB.addActionListener (this);
			cleanCB = new JCheckBox ("Clean Voronoi ?", false);
			cleanCB.addActionListener (this);

			savingCB = new JCheckBox ("SavingMode", false);
			savingCB.addActionListener (this);


			delauCB.setSelected (true);
			voroCB.setSelected (false);
			savingCB.setSelected (true);


			bt1 = new JButton ("Add 1 point (blue plus)");
			bt1.addActionListener (this);
			bt2 = new JButton ("Add");
			bt2.addActionListener (this);
			sp1 = new JSpinner (new SpinnerNumberModel(new Integer (50), new Integer (1), new Integer (10000), new Integer (50)));
			sp1.setMaximumSize (sp1.getMinimumSize ());
			label1 = new JLabel ("Computation Time : ");
//			label2 = new JLabel ("Blue plus: x="+vert1.x+" y="+vert1.y);

			withinCB = new JCheckBox ("Show points within :", false);
			withinCB.addActionListener (this);
			sp2 = new JSpinner (new SpinnerNumberModel(new Double (50), new Double (0), new Double (500), new Double (5)));
			sp2.setMaximumSize (sp2.getMinimumSize ());




			panel5.add (delauCB);
			panel5.add (voroCB);
			panel5.add (cleanCB);
			panel6.add (savingCB);
			panel1.add (bt1);
//			panel1.add (label2);
			panel1.add (withinCB);
			panel1.add (sp2);
			panel1.add (new JLabel (" meters"));
			panel3.add (new JLabel ("N.B.: To move the insertion position (blue plus), click with the mouse"));
			panel2.add (bt2);
			panel2.add (sp1);
			panel2.add (new JLabel ("Random Points"));
			panel4.add (label1);
			panel.add (panel5);
			panel.add (panel1);
			panel.add (panel3);
			panel.add (panel2);
			panel.add (panel6);
			panel.add (panel4);

			globalPanel.add (new JLabel ("Démo :"), BorderLayout.NORTH);
			globalPanel.add (drawing, BorderLayout.CENTER);
			globalPanel.add (panel, BorderLayout.SOUTH);


			getContentPane ().add (globalPanel);
			setVisible (true);
		}

		public void actionPerformed (ActionEvent e) {


			// add only 1 vertex
			if (e.getSource ().equals (bt1)) {
				vert3 = new DelaunayVertex (null,vert1.x , vert1.y);
				triang1.addVertex (vert3);

				currentTime = System.currentTimeMillis ();
				triang1.doInsertion ();
				delay = System.currentTimeMillis () - currentTime;

				triang1.getVerticesNeighbors ();

				dist = ((Double) sp2.getValue ()).doubleValue ();
				verticesWithin = (ArrayList) vert3.getVerticesWithin (dist);


				label1.setText ("1 point added, Computation Time : "+delay+" ms");
				System.out.println ("1 point added, Computation Time : "+delay+" ms");
				drawing.repaint ();

			// add the specified number of vertices
			} else if (e.getSource ().equals (bt2)) {

				int i;
				for (i=0;i<((Integer) sp1.getValue ()).intValue ();i++) {
					triang1.addVertex (new DelaunayVertex (null,50+rnd.nextInt(301) , 50+rnd.nextInt(301)));
				}

				currentTime = System.currentTimeMillis ();
				triang1.doInsertion ();
				delay = System.currentTimeMillis () - currentTime;

				verticesWithin = null;

				label1.setText (i+" points added, Computation Time : "+delay+" ms");
				System.out.println (i+" points added, Computation Time : "+delay+" ms");
				drawing.repaint ();

			} else if (e.getSource ().equals (delauCB)) {
				drawing.repaint ();
			} else if (e.getSource ().equals (voroCB)) {
				drawing.repaint ();
			} else if (e.getSource ().equals (savingCB)) {
				triang1.setSavingMode (savingCB.isSelected ());
				drawing.repaint ();
			} else if (e.getSource ().equals (cleanCB)) {
				drawing.repaint ();
			} else if (e.getSource ().equals (withinCB)) {
				showWithin = withinCB.isSelected ();
				drawing.repaint ();
			}
		}

	}


	static class MyPanel extends JPanel {

		public void paint (Graphics g) {

			Graphics2D g2 = (Graphics2D) g;

			g2.setColor (new Color (255,255,255));

			g2.fillRect (0, 0, 1000, 1000);



			// Draw triangulation's Triangles (grey)
			if (delauCB.isSelected ()) {
				delauTriangles = (ArrayList) triang1.getTriangles ();

				if (savingCB.isSelected ()) {

					// draw destructed triangles for last inserted vertex (green)
					if (triang1.getLastDestructedTriangles ()!=null) {

							g2.setColor (new Color (220,255,220));
							for (int i=0; i<(triang1.getLastDestructedTriangles ()).size ();i++) {
								g2.fill (((DelaunayTriangle) ((ArrayList) triang1.getLastDestructedTriangles ()).get (i)).getShape ());
							}

							g2.setColor (new Color (0,150,0));
							for (int i=0; i<triang1.getLastDestructedTriangles ().size ();i++) {
								g2.draw (((DelaunayTriangle) ((ArrayList) triang1.getLastDestructedTriangles ()).get (i)).getShape ());
							}
					}


					// draw border triangles for last inserted vertex (blue)
					if (triang1.getLastBorderTriangles ()!=null) {

							g2.setColor (new Color (220,220,255));
							for (int i=0; i<triang1.getLastBorderTriangles ().size ();i++) {
								DelaunaySide sd = (DelaunaySide) ((ArrayList) triang1.getLastBorderTriangles ()).get (i);
								if (sd.tri!=null) {
									g2.fill (sd.tri.getShape ());
								}
							}
					}
				}

				g2.setColor (new Color (150,150,150));
				for (int i = 0;i<delauTriangles.size ();i++) {

					// draw Delaunay triangles (grey)
					g2.draw (((DelaunayTriangle) delauTriangles.get (i)).getShape ());
				}

				if (savingCB.isSelected ()) {

					// draw refTriangle (red)
					g2.setColor (new Color (255,150,150));
					g2.draw (triang1.getRefTriangle ().getShape ());
				}
			}



			// draw Voronoi diagram (black)
			if (voroCB.isSelected ()) {
				if (cleanCB.isSelected ()) {
					voroDiagram = (ArrayList) triang1.getCleanVoronoiDiagram ();
				} else {
					voroDiagram = (ArrayList) triang1.getVoronoiDiagram ();
				}


				g2.setColor (new Color (220,100,100));
				for (int i = 0;i<voroDiagram.size ();i++) {
					vert2 = ((DelaunayVertex) voroDiagram.get (i));
					ArrayList vorovert = (ArrayList) vert2.getVoroVertices ();
					Vector points = new Vector ();

					for (int j = 0;j<vorovert.size ();j++) {
						DelaunayVoroVertex dvv = (DelaunayVoroVertex) vorovert.get (j);
						points.add (new Vertex2d (dvv.x, dvv.y));
					}

					Vector result = RegCrown.get8SidedPolygon (vert2.x, vert2.y, points, false);

					if (!result.isEmpty ()) {
						try {
							Polygon2D poly = new Polygon2D (result);
							//g2.draw (poly.getShape ());
							for (int k=0;k<result.size ();k++) {
								Vertex2d vt01 = (Vertex2d) result.get (k);
								g2.drawLine ((int) vert2.x, (int) vert2.y, (int) vt01.x, (int) vt01.y);
							}
						} catch (Exception expt) {
						}
					}



				}

				g2.setColor (new Color (0,0,0));
				for (int i = 0;i<voroDiagram.size ();i++) {
					vert2 = ((DelaunayVertex) voroDiagram.get (i));

					// draw Voronoi polygon (black)
					if (!vert2.getVoroVertices ().isEmpty ()) {
						g2.draw (vert2.getVoroShape ());
					}

					// draw the vertex (black)
					g2.drawLine ((int) vert2.x-2, (int) vert2.y, (int) vert2.x+2, (int) vert2.y);
					g2.drawLine ((int) vert2.x, (int) vert2.y-2, (int) vert2.x, (int) vert2.y+2);
				}

			}


			// Draw vertices within specified distance (m)
			if ((showWithin) && (verticesWithin!=null)) {

				g2.setColor (new Color (255,100,100));

				g2.drawOval ((int) (vert3.x-dist), (int) (vert3.y-dist), (int) dist*2, (int) dist*2);

				for (int i = 0;i<verticesWithin.size();i++) {
					vert2 = (DelaunayVertex) verticesWithin.get (i);
					g2.drawLine ((int) vert2.x-3, (int) vert2.y, (int) vert2.x+3, (int) vert2.y);
					g2.drawLine ((int) vert2.x, (int) vert2.y-3, (int) vert2.x, (int) vert2.y+3);
				}
			}

			// Draw insertion position (blue plus)
			g2.setColor (new Color (100,100,255));

			g2.drawLine ((int) vert1.x-5, (int) vert1.y, (int) vert1.x+5, (int) vert1.y);
			g2.drawLine ((int) vert1.x, (int) vert1.y-5, (int) vert1.x, (int) vert1.y+5);


			// Draw Outline
			g2.setColor (new Color (255,100,255));
			ArrayList ls = (ArrayList) triang1.getOutlines ();


			boolean isIn = false;

			for (int l=0;l<ls.size ();l++) {
				DelaunayOutline ch = (DelaunayOutline) ls.get (l);
				g2.draw (ch.getShape ());

				if (ch.contains (vert1.x, vert1.y)) {
					isIn = true;
				}
			}

			System.out.println ("Blue cross in Outline Polygon = "+isIn);

		}
	}

	public static void main (String[] args) {

		myFrame = new DelaunayWindow ("Delaunay tringulation and Voronoi diagram demo");


	}

}
