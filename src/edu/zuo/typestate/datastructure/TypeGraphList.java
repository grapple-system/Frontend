package edu.zuo.typestate.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.zuo.setree.client.IntraMain;
import soot.Local;
import soot.SootMethod;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class TypeGraphList {
	public int state = 0; // normal:0; laststmt:1
	public List<TypeGraph> ltg = new ArrayList<TypeGraph>();
//	public Map<String, List<TransEdge>> typegraph_map = new HashMap<String, List<TransEdge>>();
//	public void addTypeGraph(String varname){
//		if(!typegraph_map.containsKey(varname))
//			typegraph_map.put(varname, new ArrayList<TransEdge>());
//	}
	
	public TypeGraphList(Block block){
		Chain<Local> locals = block.getBody().getLocals();
		for (Local local : locals) {
			String classname = local.getType().toString();
			if (IntraMain.allClass.contains(classname) && !local.getName().startsWith("$")) {
				if(getTypeGraph(local.getName()+local.hashCode()) == null){
					TypeGraph tg = new TypeGraph(classname, local.getName()+local.hashCode());
					addTypeGraph(tg);
				}
			}
		}
	}
	
	public TypeGraph getTypeGraph(String varname){
		for(TypeGraph tg : ltg){
			if(tg.varname.equals(varname))
				return tg;
		}
		return null;
	}
	
	public void addTypeGraph(TypeGraph tg){
		if(!ltg.contains(tg))
			ltg.add(tg);
	}
	
	public void addTypeGraphList(TypeGraphList add_tgl){
		for(TypeGraph mytg : this.ltg){
			TypeGraph add_tg = add_tgl.getTypeGraph(mytg.varname);
			assert(add_tg != null);
			mytg.addTypeGraph(add_tg);
		}
	}
	
	public void addConstraintGraphList(ConstraintGraphList add_cgl){
		for(TypeGraph mytg : this.ltg){
			ConstraintGraph add_cg = add_cgl.getConstraintGraph(mytg.varname);
			assert(add_cg != null);
			mytg.addConstraintGraph(add_cg);
		}
	}
	
	public void transMethod(String var, String method, String starthash, String endhash){
		for(TypeGraph tg : ltg){
			if(tg.varname.equals(var)){
				if(state==0)
					tg.transMethod(method, starthash, endhash);
				else
					tg.transMethodCon(method, starthash, endhash);
				transAllExcept(tg, starthash, endhash);
				break;
			}				
		}
	}
	
	public void transAllExcept(TypeGraph tg, String starthash, String endhash){
		for(TypeGraph t : ltg){
			if(!t.equals(tg)){
				if(state == 0)
					t.transAll(starthash, endhash);
				else
					t.transAllCon(starthash, endhash);
			}
		}
	}
	
	public void transAll(int starthash, int endhash){
		transAll(Integer.toString(starthash), Integer.toString(endhash));
	}
	
	public void transMethod(String var, String method, int starthash, int endhash){
		transMethod(var, method, Integer.toString(starthash), Integer.toString(endhash));
	}
	
	public void transAll(String starthash, String endhash){
		for(TypeGraph tg : ltg){
			if(state == 0)
				tg.transAll(starthash, endhash);
			else
				tg.transAllCon(starthash, endhash);
		}
	}
	
	public void simplifyGraph(){
		for(TypeGraph tg : ltg){
			tg.simplifyGraph();
		}
	}
	
	public Set<String> getVars(int index){
		Set<String> vars = new HashSet<String>();
		for(TypeGraph tg : ltg){
			vars.addAll(tg.getVars(index));
		}
		return vars;
	}
	
	public void printDot(String filepath){
		for(TypeGraph tg : ltg){
			//String filepath = path+soot_method.getDeclaringClass().getName()+"."+soot_method.getName()+"."+tg.varname+".txt";
			tg.printDot(filepath);
		}
	}
}
