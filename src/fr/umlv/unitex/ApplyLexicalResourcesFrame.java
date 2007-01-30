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
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import fr.umlv.unitex.process.*;

/**
 * This class defines the "Apply Lexical Resources" frame, accessible from the
 * "Text" menu of Unitex. This frame shows two dictionary list. The first
 * contains all the ".bin" files found in the directory
 * <code>(user dir)/(current language dir)/Dela</code>. The second gives all
 * the ".bin" files found in the directory
 * <code>(system dir)/(current language dir)/Dela</code>. The user can select
 * the dictionaries that will be applied to the text. The "Clear" button reset
 * the selection to the empty selection. The "Default" button reset the
 * selection to the default selection. The "Set Default" button put the current
 * selection as the default one. The "Apply Selected Resources" button launch
 * the application of the selected dictionaries to the current corpus, which is
 * made by calling the <code>Dico</code> program through the creation of a
 * <code>ProcessInfoFrame</code> object.
 * 
 * @author S�bastien Paumier
 */
public class ApplyLexicalResourcesFrame extends JInternalFrame {

	static ApplyLexicalResourcesFrame frame;
	JList userDicList;
	JList systemDicList;
	MyTextArea credits; 
    JScrollPane scrollCredits;
    String noCreditMessage="No available description for the dictionary \"";

	
	private ApplyLexicalResourcesFrame() {
		super("Lexical Resources", true, true);
		setContentPane(constructMainPanel());
		pack();
		setVisible(false);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Initializes the frame.
	 *  
	 */
	private static void init() {
		frame = new ApplyLexicalResourcesFrame();
		UnitexFrame.addInternalFrame(frame);
	}

	/**
	 * Shows the frame.
	 *  
	 */
	public static void showFrame() {
		if (frame == null) {
			init();
		}
		if (frame.isVisible()) {
			return;
		}
		frame.refreshDicLists();
		frame.setVisible(true);
		try {
			frame.setSelected(true);
			frame.setIcon(false);
		} catch (java.beans.PropertyVetoException e2) {
			e2.printStackTrace();
		}

	}

	private JPanel constructMainPanel() {
		JPanel main = new JPanel(new BorderLayout());
		main.setOpaque(true);
		main.add(constructInfoPanel(),BorderLayout.NORTH);
		main.add(constructDicPanel(), BorderLayout.CENTER);
		main.add(constructButtonsPanel(), BorderLayout.SOUTH);
		main.setPreferredSize(new Dimension(390,460));
		return main;
	}

	private JPanel constructInfoPanel() {
		JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(5,5,5,5));
		JTextArea text=new JTextArea("Select the dictionaries to be applied. You can sort them one by one using the arrows. Note that system dictionaries are given to the Dico program before the user ones.");
		text.setFont(new JLabel().getFont());
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setBackground(panel.getBackground());
		panel.add(text,BorderLayout.CENTER);
		return panel;
	}

	/**
	 * This method takes a dictionary list that comes from a configuration
	 * file and merges it with the list on dictionaries that are 
	 * actually present on the disk. If a dictionary is found in the list
	 * and not on the disk, it is removed from the list. If one is found on
	 * the disk and not in the list, it is appended at the end of the list.
	 * If a dictionary is present in both list and disk, nothing is done.
	 * @param list
	 * @param dicOnDisk
	 * @return the new list
	 */
	private Vector merge(Vector list, Vector dicOnDisk) {
		if (list==null) {
			// if the list is empty, then we put in it all the
			// dictionaries that are on the disk
			return dicOnDisk;
		}
		// first, we remove every element of the list that does
		// appear on disk
		int i=0;
		while (i<list.size()) {
			if (!dicOnDisk.contains((list.elementAt(i)))) {
				list.remove(i);
			} else {
				i++;
			}
		}
		// then, we look for dictionaries that are on disk but not in
		// the list
		while (!dicOnDisk.isEmpty()) {
			String dic=(String)dicOnDisk.remove(0);
			if (!list.contains(dic)) {
				list.addElement(dic);
			}
		}
		return list;
	}

	/**
	 * Refreshes the two dictionary lists.
	 */
	void refreshDicLists() {
		Vector userListDef;
		Vector systemListDef;
		Vector userList;
		Vector systemList;
		
		userListDef = loadDicList(new File(Config
				.getUserCurrentLanguageDir(), "user_dic.def"));
		systemListDef = loadDicList(new File(Config
				.getUserCurrentLanguageDir(), "system_dic.def"));
		userList = loadDicList(new File(Config
				.getUserCurrentLanguageDir(), "user_dic_list.txt"));
		Vector userDicOnDisk = getDicList(new File(Config
		.getUserCurrentLanguageDir(), "Dela"));
		userList=merge(userList,userDicOnDisk);
		
			systemList = loadDicList(new File(
					Config.getUserCurrentLanguageDir(), "system_dic_list.txt"));
		Vector systemDicOnDisk = getDicList(new File(Config
				.getUnitexCurrentLanguageDir(), "Dela"));
		systemList=merge(systemList,systemDicOnDisk);

			//userDicList.setListData(userList);
		setContent(userDicList,userList);
		//systemDicList.setListData(systemList);
		setContent(systemDicList,systemList);
		userDicList.clearSelection();
		systemDicList.clearSelection();
		setDefaultSelection(userDicList, userListDef);
		setDefaultSelection(systemDicList, systemListDef);
		credits.setText("");
	}

	private JPanel constructCreditsPanel() {
		JPanel p=new JPanel(new BorderLayout());
		p.add(new JLabel( "Right-click a dictionary to get information about it :"),BorderLayout.NORTH);
		credits=new MyTextArea();
		credits.setWrapStyleWord(true);
		credits.setLineWrap(true);
		credits.setEditable(false);
		scrollCredits=new JScrollPane(credits);
		scrollCredits
		.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCredits
		.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		p.add(scrollCredits,BorderLayout.CENTER);
		return p;
	}

	private JSplitPane constructDicPanel() {
		JSplitPane p=new JSplitPane(JSplitPane.VERTICAL_SPLIT,constructDicListPanel(),constructCreditsPanel());
		p.setDividerLocation(250);
		return p;
	}

	
	private void setContent(JList list, Vector dics) {
		DefaultListModel model=new DefaultListModel();
		int size=dics.size();
		for (int i=0;i<size;i++) {
			model.addElement(dics.elementAt(i));
		}
		list.setModel(model);
	}

	private JPanel constructDicListPanel() {
		JPanel dicListPanel = new JPanel(new GridLayout(1, 2));
		
		JPanel userButtonsPanel=new JPanel(null);
		userButtonsPanel.setLayout(new BoxLayout(userButtonsPanel,BoxLayout.Y_AXIS));
		final JButton userUpButton=new JButton("\u25B2");
		userUpButton.setEnabled(false);
		userUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index=userDicList.getSelectedIndex();
				if (index<=0) {
					// this case should not happen
					return;
				}
				DefaultListModel model=(DefaultListModel)userDicList.getModel();
				Object o=model.remove(index);
				model.insertElementAt(o,index-1);
				userDicList.setSelectedIndex(index-1);
				saveListToFile(userDicList,new File(Config
						.getUserCurrentLanguageDir(), "user_dic_list.txt"));
			}
		});
		final JButton userDownButton=new JButton("\u25BC");
		userDownButton.setEnabled(false);
		userDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index=userDicList.getSelectedIndex();
				if (index==-1 || index==userDicList.getModel().getSize()-1) {
					// this case should not happen
					return;
				}
				DefaultListModel model=(DefaultListModel)userDicList.getModel();
				Object o=model.remove(index);
				model.insertElementAt(o,index+1);
				userDicList.setSelectedIndex(index+1);
				saveListToFile(userDicList,new File(Config
						.getUserCurrentLanguageDir(), "user_dic_list.txt"));
			}
		});
		
		userButtonsPanel.add(userUpButton);
		userButtonsPanel.add(userDownButton);
		
		userDicList = new JList();
		userDicList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int min=userDicList.getMinSelectionIndex();
				int max=userDicList.getMaxSelectionIndex();
				boolean enabledUp=(min>0) && (min==max);
				userUpButton.setEnabled(enabledUp);
				boolean enabledDown=(min!=-1) && (max<userDicList.getModel().getSize()-1) 
					&& (min==max);
				userDownButton.setEnabled(enabledDown);
			}
		});
		MouseListener userDicListener=new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()!=MouseEvent.BUTTON1) {
					int index = userDicList.locationToIndex(e.getPoint());
					String s=(String)(userDicList.getModel().getElementAt(index));
					if (index!=-1) {
						String s2=Util.getFileNameWithoutExtension(s);
						File f=new File(new File(Config.getUserCurrentLanguageDir(),"Dela"),s2+".txt");
						if (f.exists()) {
							try {
								credits.load(f);
							} catch (IllegalArgumentException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						else {
							credits.setText(noCreditMessage+s+"\"");
						}
					}
					else {credits.setText("");}
				}
			}
		};
		userDicList.addMouseListener(userDicListener);

		JPanel systemButtonsPanel=new JPanel(null);
		systemButtonsPanel.setLayout(new BoxLayout(systemButtonsPanel,BoxLayout.Y_AXIS));
		final JButton systemUpButton=new JButton("\u25B2");
		systemUpButton.setEnabled(false);
		systemUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index=systemDicList.getSelectedIndex();
				if (index<=0) {
					// this case should not happen
					return;
				}
				DefaultListModel model=(DefaultListModel)systemDicList.getModel();
				Object o=model.remove(index);
				model.insertElementAt(o,index-1);
				systemDicList.setSelectedIndex(index-1);
				saveListToFile(systemDicList,new File(Config
						.getUserCurrentLanguageDir(), "system_dic_list.txt"));
			}
		});
		final JButton systemDownButton=new JButton("\u25BC");
		systemDownButton.setEnabled(false);
		systemDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index=systemDicList.getSelectedIndex();
				if (index==-1 || index==systemDicList.getModel().getSize()-1) {
					// this case should not happen
					return;
				}
				DefaultListModel model=(DefaultListModel)systemDicList.getModel();
				Object o=model.remove(index);
				model.insertElementAt(o,index+1);
				systemDicList.setSelectedIndex(index+1);
				saveListToFile(systemDicList,new File(Config
						.getUserCurrentLanguageDir(), "system_dic_list.txt"));
			}
		});
		
		systemButtonsPanel.add(systemUpButton);
		systemButtonsPanel.add(systemDownButton);
		
		systemDicList = new JList(new DefaultListModel());
		systemDicList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int min=systemDicList.getMinSelectionIndex();
				int max=systemDicList.getMaxSelectionIndex();
				boolean enabledUp=(min>0) && (min==max);
				systemUpButton.setEnabled(enabledUp);
				boolean enabledDown=(min!=-1) && (max<systemDicList.getModel().getSize()-1) 
					&& (min==max);
				systemDownButton.setEnabled(enabledDown);
			}
		});
		MouseListener systemDicListener=new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()!=MouseEvent.BUTTON1) {
					int index = systemDicList.locationToIndex(e.getPoint());
					String s=(String)(systemDicList.getModel().getElementAt(index));
					if (index!=-1) {
						String s2=Util.getFileNameWithoutExtension(s);
						File f=new File(new File(Config.getUnitexCurrentLanguageDir(),"Dela"),s2+".txt");
						if (f.exists()) {
							try {
								credits.load(f);
							} catch (IllegalArgumentException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						else {
							credits.setText(noCreditMessage+s+"\"");
						}
					}
					else {credits.setText("");}
				}
			}
		};
		systemDicList.addMouseListener(systemDicListener);
		userDicList.setBorder(BorderFactory.createLoweredBevelBorder());
		systemDicList.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel userPanel = new JPanel(new BorderLayout());
		JPanel systemPanel = new JPanel(new BorderLayout());
		userPanel.setBorder(new TitledBorder("User resources"));
		systemPanel.setBorder(new TitledBorder("System resources"));
		JScrollPane scroll_1 = new JScrollPane(userDicList);
		scroll_1
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_1
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		JScrollPane scroll_2 = new JScrollPane(systemDicList);
		scroll_2
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_2
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		userPanel.add(scroll_1,BorderLayout.CENTER);
		userPanel.add(userButtonsPanel,BorderLayout.EAST);
		systemPanel.add(scroll_2,BorderLayout.CENTER);
		systemPanel.add(systemButtonsPanel,BorderLayout.EAST);
		dicListPanel.add(userPanel);
		dicListPanel.add(systemPanel);
		return dicListPanel;
	}

	private JPanel constructButtonsPanel() {
		JPanel buttons = new JPanel(new GridLayout(1, 4));
		Action clearAction = new AbstractAction("Clear") {
			public void actionPerformed(ActionEvent e) {
				frame.userDicList.clearSelection();
				frame.systemDicList.clearSelection();
				credits.setText("");
			}
		};
		JButton clearButton = new JButton(clearAction);
		Action defaultAction = new AbstractAction("Default") {
			public void actionPerformed(ActionEvent e) {
				frame.refreshDicLists();
				credits.setText("");
			}
		};
		JButton defaultButton = new JButton(defaultAction);
		Action setDefaultAction = new AbstractAction("Set Default") {
			public void actionPerformed(ActionEvent e) {
				frame.saveDefaultDicLists();
			}
		};
		JButton setDefaultButton = new JButton(setDefaultAction);
		buttons.add(clearButton);
		buttons.add(defaultButton);
		buttons.add(setDefaultButton);
		buttons.add(constructGoButton());    
		return buttons;
	}

	private JButton constructGoButton() {
		Action goAction = new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
				// post pone code
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						MultiCommands commands;
						if(Config.isAgglutinativeLanguage()){
							commands = getRunCmdForAgglutinativeLanguages();
						} else {
							commands = getRunCmdForNonAgglutinativeLanguages();
						}
						if(commands.numberOfCommands() == 0) return;
						
						TextDicFrame.hideFrame();
						new ProcessInfoFrame(commands, true,
								new ApplyLexicalResourcesDo());
					}
				});
			}
		};
		return new JButton(goAction);
	}
	
	
	
	
	/**
	 * Builds the command lines for applying dictionaries for
	 * non agglutinative languages.
	 * @return a <code>MultiCommands</code> object that contains the command lines
	 */
	MultiCommands getRunCmdForNonAgglutinativeLanguages()
	{
		MultiCommands commands = new MultiCommands();

		Object[] userSelection = userDicList.getSelectedValues();
		Object[] systemSelection = systemDicList.getSelectedValues();
		if ((userSelection == null || userSelection.length == 0)
				&& (systemSelection == null || systemSelection.length == 0)) {
			// if there is no dic selected, we do nothing
			return commands;
		}
		DicoCommand cmd = new DicoCommand().snt(
				Config.getCurrentSnt()).alphabet(
						Config.getAlphabet());
		if (systemSelection != null	&& systemSelection.length != 0) {
			for (int i = 0; i < systemSelection.length; i++) {
				cmd = cmd.systemDictionary((String) systemSelection[i]);
			}
		}
		if (userSelection != null && userSelection.length != 0) {
			for (int i = 0; i < userSelection.length; i++) {
				cmd = cmd.userDictionary((String) userSelection[i]);
			}
		}
		commands.addCommand(cmd);
		// sorting DLF
		SortTxtCommand sortCmd = new SortTxtCommand().file(
				new File(Config.getCurrentSntDir(), "dlf"))
				.saveNumberOfLines(new File(Config.getCurrentSntDir(),"dlf.n"));
		if (Config.getCurrentLanguage().equals("Thai")) {
			sortCmd = sortCmd.thai();
		} else {
			sortCmd = sortCmd.sortAlphabet();
		}
		commands.addCommand(sortCmd);
		// sorting DLC
		SortTxtCommand sortCmd2 = new SortTxtCommand().file(
				new File(Config.getCurrentSntDir(), "dlc"))
				.saveNumberOfLines(new File(
						Config.getCurrentSntDir(),"dlc.n"));
		if (Config.getCurrentLanguage().equals("Thai")) {
			sortCmd2 = sortCmd2.thai();
		} else {
			sortCmd2 = sortCmd2.sortAlphabet();
		}
		commands.addCommand(sortCmd2);
		// sorting ERR
		SortTxtCommand sortCmd3 = new SortTxtCommand().file(
				new File(Config.getCurrentSntDir(), "err"))
				.saveNumberOfLines(new File(
						Config.getCurrentSntDir(),"err.n"));
		if (Config.getCurrentLanguage().equals("Thai")) {
			sortCmd3 = sortCmd3.thai();
		} else {
			sortCmd3 = sortCmd3.sortAlphabet();
		}
		commands.addCommand(sortCmd3);
		return (commands);
	}
	
	
	/**
	 * Builds the command lines for applying dictionaries for
	 * agglutinative languages.
	 * @return a <code>MultiCommands</code> object that contains the command lines
	 */

	MultiCommands getRunCmdForAgglutinativeLanguages()
	{
		MultiCommands commands = new MultiCommands();
		Object[] userSelection = userDicList.getSelectedValues();
		Object[] systemSelection = systemDicList.getSelectedValues();
		if ((userSelection == null || userSelection.length == 0)
		&& (systemSelection == null || systemSelection.length == 0)) {
	// if there is no dic selected, we do nothing
			return commands;
		}

		File usrlistDicFiles = new File(
				new File(Config
						.getUserCurrentLanguageDir(), "Dela"),"usrlist.lst");
		File syslistDicFiles = new File(
				new File(Config
						.getUnitexCurrentLanguageDir(), "Dela"),"syslist.lst");
		frame.saveSelectionToFile(frame.userDicList,usrlistDicFiles);
		frame.saveSelectionToFile(frame.systemDicList,syslistDicFiles);
		
		ConsultDicCommand consultkr = new ConsultDicCommand()
		.listFile(syslistDicFiles)
		.listFile(usrlistDicFiles)
		.sequencesMorph(new File(Config.getCurrentSntDir(),"tokensjm.txt"));

		Txt2Fst2KrCommand getmorph = new Txt2Fst2KrCommand();
		getmorph.creation(new File(Config.getCurrentSntDir(),"tokens.txt"));
		
		Txt2Fst2KrCommand getmorph1 = new Txt2Fst2KrCommand();
		getmorph1.getsentence(1,new File(Config.getCurrentSntDir(),"tokens.txt"));
		
		Jamo2SylCommand getSylSeqMorphs = new Jamo2SylCommand()
		.decodage(new File(Config.getCurrentSntDir(),"seqMorphs.txt"));
		Jamo2SylCommand getSylMorpheme = new Jamo2SylCommand()
		.decodage(new File(Config.getCurrentSntDir(),"Morphemes.txt"));
		File curSntDir  = Config.getCurrentSntDir();		

		
		SortMorphCommand getsearchResult = new SortMorphCommand()
			.tokenF(new File(curSntDir,"tokens.txt"))
			.seqMorpF(new File(curSntDir,"seqMorphssyl.txt"))
			.txtCodeF(new File(curSntDir,"Morphemessyl.txt"))
		;
		
//		CommandGen getsearchResult = new CommandGen("sortmorph");
//		getsearchResult.txtCodeF(new File(curSntDir,"text.cod"));
//		getsearchResult.tokenF(new File(curSntDir,"tokens.txt"));
//		getsearchResult.seqMorpF(new File(curSntDir,"seqMorphssyl.txt"));
		Jamo2SylCommand getSylSentence = new Jamo2SylCommand()
		    .decodage(new File(Config.getCurrentSntDir(),"sentence.fst2"));
		Fst2GrfCommand phraseGrfcmd = new Fst2GrfCommand().automaton(
		        new File(Config.getCurrentSntDir(),
		                "sentencesyl.fst2")
				).sentence(1).font("Gulim");
		
//		CommandGen firstPhraseGrf = new CommandGen("fst2grfkr");
//		firstPhraseGrf.dirs(new File(Config.getCurrentSntDir(),"sentencesyl.fst2"));
//		firstPhraseGrf.element();
		// set mode to morphem
	
		commands.addCommand(consultkr);
		commands.addCommand(getmorph);
		commands.addCommand(getmorph1);
		commands.addCommand(getSylSentence);
		commands.addCommand(phraseGrfcmd);
		commands.addCommand(getSylSeqMorphs);
		commands.addCommand(getSylMorpheme);
		commands.addCommand(getsearchResult);
		
		
		return(commands);
	}
	/**
	 * Gets a list of all ".bin" and ".fst2" files found in a directory.
	 * 
	 * @param dir
	 *            the directory to be scanned
	 * @return a <code>Vector</code> containing file names.
	 */
	public Vector getDicList(File dir) {
	
		Vector v = new Vector();
		if (!dir.exists())
			return v;
		File files_list[] = dir.listFiles();
		if(Config.isAgglutinativeLanguage()){
			for (int i = 0; i < files_list.length; i++) {
				String name = files_list[i].getAbsolutePath();
				
				if (!files_list[i].isDirectory()
						&& (name.endsWith(".mtb") || name.endsWith(".MTB"))) {
					v.add(files_list[i].getName());
				}
			}
			
		} else {
			for (int i = 0; i < files_list.length; i++) {
				String name = files_list[i].getAbsolutePath();
				
				if (!files_list[i].isDirectory()
						&& (name.endsWith(".bin") || name.endsWith(".BIN")
							|| name.endsWith(".fst2")  || name.endsWith(".FST2"))) {
					v.add(files_list[i].getName());
				}
			}

		}
		return v;
	}

	/**
	 * Loads a dictionary list.
	 * 
	 * @param name
	 *            the name of a file containing one ".bin" file name per line
	 * @return a <code>Vector</code> containing file names.
	 */
	public Vector loadDicList(File name) {
		Vector v;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(name));
		} catch (FileNotFoundException e) {
			return null;
		}
		try {
			String s;
			v = new Vector();
			while ((s = br.readLine()) != null) {
				v.add(s);
			}
			br.close();
		} catch (IOException e) {
			return null;
		}
		return v;
	}

	/**
	 * Selects in a list all files contained in a file name list. IMPORTANT:
	 * this method does not clear the <code>JList</code>. You must call first
	 * the <code>JList.clearSelection()</code> method.
	 * 
	 * @param list
	 *            the <code>JList</code> that contains file names.
	 * @param v
	 *            the <code>Vector</code> containing a list of the file name
	 *            to be selected.
	 */
	public void setDefaultSelection(JList list, Vector v) {
		int[] indices = new int[100];
		int i = 0;
		if (v == null)
			return;
		ListModel model = list.getModel();
		while (!v.isEmpty()) {
			String s = (String) (v.remove(0));
			int index = getElementIndex(model, s);
			if (index != -1) {
				indices[i++] = index;
			}
		}
		if (i != 0) {
			int[] res = new int[i];
			for (int j = 0; j < i; j++) {
				res[j] = indices[j];
			}
			list.setSelectedIndices(res);
		}
	}

	/**
	 * Looks for a file name in a <code>ListModel</code>.
	 * 
	 * @param model
	 *            the <code>ListModel</code>
	 * @param s
	 *            the file name
	 * @return the position in the <code>ListModel</code> if the file name
	 *         were found, -1 otherwise
	 */
	public int getElementIndex(ListModel model, String s) {
		if (model == null)
			return -1;
		int l = model.getSize();
		for (int i = 0; i < l; i++) {
			if (s.equals(model.getElementAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Saves the current file selections as the default selections. The
	 * selections are stored in the text files
	 * <code>(user dir)/(current language dir)/user_dic.def</code> and
	 * <code>(user dir)/(current language dir)/system_dic.def</code>.
	 *  
	 */
	public void saveDefaultDicLists() {
		saveSelectionToFile(userDicList, new File(Config
				.getUserCurrentLanguageDir(), "user_dic.def"));
		saveSelectionToFile(systemDicList, new File(Config
				.getUserCurrentLanguageDir(), "system_dic.def"));			
	}

	/**
	 * Saves a file selection into a text file, storing one file name per line.
	 * Only selected items of the <code>JList</code> are taken into account.
	 * 
	 * @param list
	 *            the file list
	 * @param file
	 *            the output file
	 */
	public void saveSelectionToFile(JList list, File file) {
		Object[] selection = list.getSelectedValues();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < selection.length; i++) {
				String s = ((String) selection[i]) + "\n";
				bw.write(s, 0, s.length());
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Saves a file selection into a text file, storing one file name per line.
	 * Only selected items of the <code>JList</code> are taken into account.
	 * 
	 * @param list
	 *            the file list
	 * @param file
	 *            the output file
	 */
	public void saveListToFile(JList list, File file) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			ListModel model=list.getModel();
			int size=model.getSize();
			for (int i = 0; i < size; i++) {
				String s = model.getElementAt(i)+"\n";
				bw.write(s, 0, s.length());
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	class ApplyLexicalResourcesDo extends ToDoAbstract {
		public void toDo() {
			TextDicFrame.loadTextDic(Config.getCurrentSntDir(),false);
			File morphlist = new File(Config.getCurrentSntDir(),"morph_by_freq.txt");
			if(morphlist.exists()){
				MorphemeFrame.loadMorphemes(morphlist);
			}
		}
	}

}