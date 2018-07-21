package edu.zuo.typestate.datastructure;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.zuo.setree.client.IntraMain;

public class ConstraintGraph {
	public String varname;
	public List<ConstraintEdge> constraintedges = new ArrayList<ConstraintEdge>();
	private List<String[]> tempedges = new ArrayList<String[]>();
	
	public ConstraintGraph(String varname){
		this.varname = varname;
	}
	
	public void temp2Constraint(String start, String end, int startnode, int endnode){
		for(String[] te : tempedges){
			constraintedges.add(new ConstraintEdge(te, start, end, startnode, endnode));
		}
		//tempedges = new ArrayList<String[]>();
	}
	
	public void clearTemp(){
		tempedges.clear();
	}
	
	public void addTemp(String[] te){
		assert(te.length == 2);
		tempedges.add(te);
	}
	
	private void deduplication() {
		for (int i = 0; i < constraintedges.size(); i++) {
			for (int j = constraintedges.size() - 1; j > i; j--) {
				ConstraintEdge e1 = constraintedges.get(i);
				ConstraintEdge e2 = constraintedges.get(j);
				if (e1.printDot().equals(e2.printDot())) {
					try {
						constraintedges.remove(j);
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public Set<String> getVars(){
		Set<String> vars = new HashSet<String>();
		for(ConstraintEdge ce : constraintedges){
			vars.add(ce.getStartStr());
			vars.add(ce.getEndStr());
		}
		return vars;
	}
	
	public void print(String file_path){
		deduplication();
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("digraph " + file_path.substring(0, file_path.lastIndexOf('.')) + "{\r\n");
			for (ConstraintEdge constraintedge : constraintedges) {
				fileWriter.write(constraintedge.printDot());
			}
			fileWriter.write("} \r\n");
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
