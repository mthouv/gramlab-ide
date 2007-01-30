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
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import fr.umlv.unitex.process.*;

/**
 * This class describes the lexicon-grammar conversion frame.
 * 
 * @author S�bastien Paumier
 *  
 */
public class LexiconGrammarFrame extends JInternalFrame {

	static LexiconGrammarFrame frame;
	JTextField grfName = new JTextField();
	JTextField resultName = new JTextField();
	JTextField subgraphName = new JTextField();

	private LexiconGrammarFrame() {
		super("Compile Lexicon-Grammar to GRF", false, true);
		setContentPane(constructPanel());
		pack();
		setResizable(false);
		setVisible(false);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Initializes the frame
	 *  
	 */
	private static void init() {
		frame = new LexiconGrammarFrame();
		UnitexFrame.addInternalFrame(frame);
	}

	/**
	 * Shows the frame
	 *  
	 */
	public static void showFrame() {
		if (frame == null) {
			init();
		}
		frame.setVisible(true);
		try {
			frame.setSelected(true);
			frame.setIcon(false);
		} catch (java.beans.PropertyVetoException e2) {
			e2.printStackTrace();
		}
	}

	private JPanel constructPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(true);
		panel.add(constructUpPanel(), BorderLayout.NORTH);
		panel.add(constructDownPanel(), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPanel(JLabel label, JTextField textField,
			JButton button) {
		JPanel p = new JPanel(new GridLayout(2, 1));
		p.setOpaque(true);
		p.add(label);
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.setOpaque(true);
		tmp.add(textField, BorderLayout.CENTER);
		tmp.add(button, BorderLayout.EAST);
		p.add(tmp);
		return p;
	}

	private JPanel constructUpPanel() {
		JPanel upPanel = new JPanel(new GridLayout(3, 1));
		upPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		grfName.setPreferredSize(new Dimension(280, 20));
		resultName.setPreferredSize(new Dimension(280, 20));
		subgraphName.setPreferredSize(new Dimension(280, 20));
		Action setGrfAction = new AbstractAction("Set...") {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dialogBox=Config.getGraphDialogBox(false);
				dialogBox.setDialogType(JFileChooser.OPEN_DIALOG);
				int returnVal = dialogBox.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					// we return if the user has clicked on CANCEL
					return;
				}
				System.out.println(dialogBox.getSelectedFile());
				System.out.println(dialogBox.getSelectedFile()
						.getAbsolutePath());
				frame.grfName.setText(dialogBox.getSelectedFile()
						.getAbsolutePath());
			}
		};
		JButton setGrfName = new JButton(setGrfAction);
		Action setResultAction = new AbstractAction("Set...") {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dialogBox=Config.getGraphDialogBox(false);
				dialogBox.setDialogType(JFileChooser.SAVE_DIALOG);
				int returnVal = dialogBox.showSaveDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					// we return if the user has clicked on CANCEL
					return;
				}
				String s = dialogBox.getSelectedFile()
						.getAbsolutePath();
				if (!s.endsWith(".grf"))
					s = s + ".grf";
				frame.resultName.setText(s);
			}
		};
		JButton setResultName = new JButton(setResultAction);
		Action setSubgraphAction = new AbstractAction("Set...") {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dialogBox=Config.getGraphDialogBox(false);
				dialogBox.setDialogType(JFileChooser.SAVE_DIALOG);
				int returnVal = dialogBox.showSaveDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					// we return if the user has clicked on CANCEL
					return;
				}
				String s = dialogBox.getSelectedFile()
						.getAbsolutePath();
				if (!s.endsWith(".grf"))
					s = s + ".grf";
				frame.subgraphName.setText(s);
			}
		};
		JButton setSubgraphName = new JButton(setSubgraphAction);
		JPanel a = createPanel(new JLabel("Reference Graph (in GRF format):"),
				grfName, setGrfName);
		JPanel b = createPanel(new JLabel("Resulting GRF grammar:"),
				resultName, setResultName);
		JPanel c = createPanel(new JLabel("Name of produced subgraphs:"),
				subgraphName, setSubgraphName);
		upPanel.add(a);
		upPanel.add(b);
		upPanel.add(c);
		return upPanel;
	}

	private JPanel constructDownPanel() {
		JPanel downPanel = new JPanel(new GridLayout(1, 2));
		downPanel.setOpaque(true);
		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
			}
		};
		JButton CANCEL = new JButton(cancelAction);
		Action okAction = new AbstractAction("Compile") {
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Table2GrfCommand command = new Table2GrfCommand()
								.table(new File(LexiconGrammarTableFrame.getTableName()))
								.parametrizedGraph(new File(frame.grfName.getText()))
								.resultMainGraph(new File(frame.resultName.getText()));
						if (!frame.resultName.getText().equals("")) {
							command = command.subgraphName(new File(frame.subgraphName.getText()));
						}
						frame.setVisible(false);
						new ProcessInfoFrame(command, true, null);
					}
				});
			}
		};
		JButton OK = new JButton(okAction);
		JPanel left = new JPanel();
		left.setBorder(new EmptyBorder(10, 50, 10, 20));
		left.setLayout(new BorderLayout());
		left.add(CANCEL, BorderLayout.CENTER);
		JPanel right = new JPanel();
		right.setBorder(new EmptyBorder(10, 20, 10, 50));
		right.setLayout(new BorderLayout());
		right.add(OK, BorderLayout.CENTER);
		downPanel.add(left);
		downPanel.add(right);
		return downPanel;
	}

}