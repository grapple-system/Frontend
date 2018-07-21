package edu.zuo.typestate.datastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.zuo.setree.client.IntraMain;
import soot.Local;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class ConstraintGraphList {
	public List<ConstraintGraph> cgl = new ArrayList<ConstraintGraph>();	
	
	public ConstraintGraphList(Block block){
		Chain<Local> locals = block.getBody().getLocals();
		for (Local local : locals) {
			String classname = local.getType().toString();
			if (IntraMain.allClass.contains(classname) && !local.getName().startsWith("$")) {
				ConstraintGraph cg = new ConstraintGraph(local.getName());
				addConstraintGraph(cg);
			}
		}
	}
	
	public ConstraintGraph getConstraintGraph(String varname){
		for(ConstraintGraph cg : cgl){
			if(cg.varname.equals(varname))
				return cg;
		}
		return null;
	}
	
	public void addConstraintGraph(ConstraintGraph cg){
		if(!cgl.contains(cg))
			cgl.add(cg);
	}
	
	public void addTemp(String varname, String[] te){
		for(ConstraintGraph cg : cgl){
			if(cg.varname.equals(varname))
				cg.addTemp(te);
		}
	}
	
	public void temp2Constraint(String start, String end, int startnode, int endnode){
		for(ConstraintGraph cg : cgl){
			cg.temp2Constraint(start, end, startnode, endnode);
		}
	}
	
	public void temp2Constraint(int start, int end, int startnode, int endnode){
		temp2Constraint(Integer.toString(start), Integer.toString(end), startnode, endnode);
	}
	
	public void clearTemp(){
		for(ConstraintGraph cg : cgl){
			cg.clearTemp();
		}
	}
	
	public Set<String> getVars(){
		Set<String> vars = new HashSet<String>();
		for(ConstraintGraph cg : cgl){
			vars.addAll(cg.getVars());
		}
		return vars;
	}
}
