 /*
  * Unitex
  *
  * Copyright (C) 2001-2007 Universit� de Marne-la-Vall�e <unitex@univ-mlv.fr>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
  *
  */

package fr.umlv.unitex;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * This class describes a text frame that shows the results of dictionary
 * checkings.
 * 
 * @author S�bastien Paumier
 *  
 */
public class CheckResultFrame extends JInternalFrame {

  static CheckResultFrame frame;
	private MyTextArea text = new MyTextArea();

    
	private CheckResultFrame() {
		super("", true, true, true, true);
		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(true);
		top.setBorder(new EmptyBorder(2, 2, 2, 2));
		JScrollPane scroll = new JScrollPane(text);
		scroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel middle = new JPanel(new BorderLayout());
		middle.setOpaque(true);
		middle.setBorder(BorderFactory.createLoweredBevelBorder());
		middle.add(scroll, BorderLayout.CENTER);
		top.add(middle, BorderLayout.CENTER);
		setContentPane(top);
		pack();
		setBounds(100, 100, 600, 600);
		setVisible(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				close();
			}
		});
	}

	/**
	 * Initializes the frame.
	 *  
	 */
	private static void init() {
		frame = new CheckResultFrame();
		UnitexFrame.addInternalFrame(frame);
	}

	/**
	 * Loads a text file.
	 * 
	 * @param dela
	 *            the name of the text file
	 */
	public static void load(File dela) {
		if (frame == null) {
			init();
		}
		frame.text.setFont(Config.getCurrentTextFont());
		frame.text.setLineWrap(true);
		frame.text.setEditable(false);
		try {
			frame.text.load(dela);
		} catch (java.io.IOException e) {
			return;
		}
		frame.setTitle("Check Results");
		frame.setVisible(true);
		try {
			frame.setIcon(false);
			frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * Closes the frame.
	 *  
	 */
	public static void close() {
		if (frame == null) {
			return;
		}
		frame.setVisible(false);
		frame.text.setDocument(new PlainDocument());
		System.gc();
	}

}