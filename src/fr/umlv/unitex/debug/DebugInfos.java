/*
 * Unitex
 *
 * Copyright (C) 2001-2011 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
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

package fr.umlv.unitex.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import fr.umlv.unitex.Util;
import fr.umlv.unitex.graphrendering.GenericGraphBox;
import fr.umlv.unitex.io.GraphIO;

public class DebugInfos {

	public File concordIndFile=null;
	public ArrayList<String> graphNames=new ArrayList<String>();
	public ArrayList<File> graphs=new ArrayList<File>();
	public ArrayList<String> lines=new ArrayList<String>();
	public HashMap<Integer,GraphIO> graphIOMap=new HashMap<Integer,GraphIO>();

	
	public static DebugInfos loadConcordanceIndex(File html) {
		String concord_ind=Util.getFileNameWithoutExtension(html)+".ind";
		File f=new File(concord_ind);
		if (!f.exists()) return null;
		Scanner scanner=null;
		try {
			scanner=new Scanner(f,"UTF-16LE");
			String z=scanner.nextLine();
			if (z.startsWith("\uFEFF")) {
				z=z.substring(1);
			}
			if (!z.startsWith("#D")) {
				scanner.close();
				return null;
			}
			DebugInfos infos=new DebugInfos();
			infos.concordIndFile=f;
			int n=scanner.nextInt();
			scanner.nextLine();
			Pattern normalDelimiter=scanner.delimiter();
			while (n>0) {
				scanner.useDelimiter(""+(char)1);
				String s=scanner.next();
				infos.graphNames.add(s);
				scanner.useDelimiter(normalDelimiter);
				/* We skip the delimiter char # 1*/
				s=scanner.nextLine().substring(1);
				infos.graphs.add(new File(s));
				n--;
			}
			/* We skip the #[IMR] line */
			scanner.nextLine();
			while (scanner.hasNextLine()) {
				/* We skip the match coordinates */
				scanner.next();
				scanner.next();
				/* We skip the normal output part */
				scanner.useDelimiter(""+(char)1);
				scanner.next();
				scanner.useDelimiter(normalDelimiter);
				infos.lines.add(scanner.nextLine());
			}
			scanner.close();
			return infos;
		} catch (FileNotFoundException e) {
			return null;
		} catch (NoSuchElementException e2) {
			if (scanner!=null) scanner.close();
			return null;
		}
	}
	
	/**
	 * Note: n must be in [1;number of graphs]
	 */
	public GraphIO getGraphIO(int n) {
		GraphIO gio=graphIOMap.get(Integer.valueOf(n));
		if (gio==null) {
			/* If we try to load a graph for the first time,
			 * we check if it has been modified since the concordance was built.
			 * Once loaded, we use the cached version, so that we are sure
			 * to debug on the correct version, even if the graph has been changed
			 * while debugging
			 */
			File f=graphs.get(n-1);
			if (f.lastModified()>concordIndFile.lastModified()) {
				JOptionPane
	            .showMessageDialog(
	                    null,
	                    "File "+f.getAbsolutePath()+ " has been modified\n"+
	                    "since the concordance index was built. Cannot debug it.",
	                    "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			gio=GraphIO.loadGraph(f,false,false);
			if (gio==null) {
				JOptionPane
	            .showMessageDialog(
	                    null,
	                    "Cannot load graph "+f.getAbsolutePath(),
	                    "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			graphIOMap.put(Integer.valueOf(n),gio);
		}
		return gio;
	}


	public int getEpsilonLineInInitialState(int graph) {
		GraphIO gio=getGraphIO(graph);
		GenericGraphBox box=gio.boxes.get(0);
		return box.lines.indexOf("<E>");
	}
	
	
	public ArrayList<DebugDetails> getMatchDetails(int n,ArrayList<DebugDetails> d) {
		if (d==null) {
			d=new ArrayList<DebugDetails>();
		}
		d.clear();
		Scanner scanner=new Scanner(lines.get(n));
		scanner.useDelimiter(""+(char)2);
		while (scanner.hasNext()) {
			/* We skip the initial char #1 */
			String output=scanner.next().substring(1);
			scanner.useDelimiter(":");
			int graph=Integer.parseInt(scanner.next().substring(1));
			int box=scanner.nextInt();
			scanner.useDelimiter(""+(char)3);
			int line=Integer.parseInt(scanner.next().substring(1));
			scanner.useDelimiter(""+(char)4);
			String tag=scanner.next().substring(1);
			scanner.useDelimiter(""+(char)1);
			String matched=scanner.next().substring(1);
			d.add(new DebugDetails(tag,output,matched,graph,box,line,this));
			scanner.useDelimiter(""+(char)2);
		}
		scanner.close();
		DebugDetails tmp=d.get(0);
		if (tmp.box!=0) {
			/* If necessary, we add the initial state */
			DebugDetails tmp2=new DebugDetails("<E>","","",tmp.graph,0,getEpsilonLineInInitialState(tmp.graph),this);
			d.add(0,tmp2);
		}
		tmp=d.get(d.size()-1);
		if (tmp.box!=1) {
			/* If necessary, we add the final state */
			DebugDetails tmp2=new DebugDetails("","","",tmp.graph,1,0,this);
			d.add(tmp2);
		}
		if (!restore_E_steps(d)) {
			d.clear();
		}
		return d;
	}
	
	/**
	 * In debug mode, <E> with no output are compiled without debug 
	 * information, so that they cannot be present in debug concordance.
	 * So, this function is there to restore those <E> steps in graph
	 * exploration.
	 */
	private boolean restore_E_steps(ArrayList<DebugDetails> d) {
		for (int i=0;i<d.size()-1;i++) {
			DebugDetails src=d.get(i);
			DebugDetails dst=d.get(i+1);
			File f=graphs.get(src.graph-1);
			if (src.tag.equals("$![")) {
				/* Special case of a forbidden right context
				 * Such contexts should never be catched in 
				 * debug mode, so that the immediate next 
				 * tag should be the $] one
				 */
				if (dst.tag.equals("$]")) {
					/* We go on */
					continue;
				}
				JOptionPane
	            .showMessageDialog(
	                    null,
	                    "Unexpected non empty forbidden context between "+src.box+" and "+dst.box+ " in graph "+f.getAbsolutePath(),
	                    "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (src.graph!=dst.graph) {
				/* There cannot be a missing <E> if the
				 * graphs are different */
				continue;
			}
			GraphIO gio=getGraphIO(src.graph);
			if (gio==null) {
				return false;
			}
			GenericGraphBox srcBox=gio.boxes.get(src.box);
			GenericGraphBox dstBox=gio.boxes.get(dst.box);
			if (srcBox.transitions.contains(dstBox)) {
				/* Nothing to do if there is a transition */
				continue;
			}
			if (src.box==dst.box && src.line==dst.line) {
				/* If we are in the same line of the same box, it may be because
				 * there is only one tag in the line and a loop on the box,
				 * but it may also be because the line contains several tokens */
				String line=srcBox.lines.get(src.line);
				int pos=line.indexOf(src.tag);
				pos=line.indexOf(dst.tag,pos+src.tag.length());
				if (pos!=-1) {
					/* We are in the same line, nothing to do */
					continue;
				}
			}
			ArrayList<GenericGraphBox> visited=new ArrayList<GenericGraphBox>();
			ArrayList<Integer> path=new ArrayList<Integer>();
			if (!findEpsilonPath(0,srcBox,dstBox,visited,path,gio.boxes)) {
				JOptionPane
	            .showMessageDialog(
	                    null,
	                    "Cannot find <E> path between box "+src.box+" and "+dst.box+ " in graph "+f.getAbsolutePath(),
	                    "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (int j=0;j<path.size();j=j+2) {
				int box=path.get(j);
				int line=path.get(j+1);
				DebugDetails det=new DebugDetails("<E>","","",src.graph,box,line,this);
				i++;
				d.add(i,det);
			}
		}
		return true;
	}

	private boolean findEpsilonPath(int depth,GenericGraphBox current,
			GenericGraphBox dstBox, ArrayList<GenericGraphBox> visited,
			ArrayList<Integer> path,ArrayList<GenericGraphBox> boxes) {
		if (current.equals(dstBox) && depth>0) return true;
		if (visited.contains(current)) return false;
		visited.add(current);
		if (depth==0) {
			/* Special of the starting box */
			for (GenericGraphBox dest:current.transitions) {
				if (findEpsilonPath(depth+1,dest,dstBox,visited,path,boxes)) return true;
			}
			return false;
		}
		if (current.transduction!=null && current.transduction.length()>0) {
			/* Boxes with an output cannot be considered */
			return false;
		}
		if (current.lines.size()==0) {
			/* Case of a box only containing <E> */
			path.add(boxes.indexOf(current));
			path.add(0);
			for (GenericGraphBox dest:current.transitions) {
				if (findEpsilonPath(depth+1,dest,dstBox,visited,path,boxes)) return true;
			}
			path.remove(path.size()-1);
			path.remove(path.size()-1);
			return false;
		}
		for (int i=0;i<current.lines.size();i++) {
			if (current.lines.get(i).equals("<E>")) {
				/* The box line is a candidate */
				path.add(boxes.indexOf(current));
				path.add(i);
				for (GenericGraphBox dest:current.transitions) {
					if (findEpsilonPath(depth+1,dest,dstBox,visited,path,boxes)) return true;
				}
				path.remove(path.size()-1);
				path.remove(path.size()-1);
				/* An <E> is enough to go through a box, so we can stop */
				break;
			}
		}
		return false;
	}

}
