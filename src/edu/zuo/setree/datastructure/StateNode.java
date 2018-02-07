package edu.zuo.setree.datastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acteve.symbolic.integer.Expression;
import soot.Local;

public class StateNode {
	
	//state map containing <variable, symbolic expression> mapping
//	private State state;
	private Map<Local, Expression> localsMap;
	
	//symbolic conditional
	private Conditional conditional;
	
	//two children
	private StateNode trueChild;
	private StateNode falseChild;

	//list of call-sites ahead of the node
	private final List<CallSite> callsites;
	
	//formal symbolic return if applicable (only for the node containing return statement) 
	private Expression returnExpr;
	
	
	public StateNode(){
//		state = new State();
		this.localsMap = new LinkedHashMap<Local, Expression>();
		this.conditional = null;
		this.trueChild = null;
		this.falseChild = null;
		this.callsites = new ArrayList<CallSite>();
		this.returnExpr = null;
	}
	
	public StateNode(Map parentMap){
//		this.state = new State(s);
		localsMap = new LinkedHashMap<Local, Expression>(parentMap);
		this.conditional = null;
		this.trueChild = null;
		this.falseChild = null;
		this.callsites = new ArrayList<CallSite>();
		this.returnExpr = null;
	}


	public StateNode getTrueChild() {
		return trueChild;
	}

	public void setTrueChild(StateNode trueChild) {
		this.trueChild = trueChild;
	}

	public StateNode getFalseChild() {
		return falseChild;
	}

	public void setFalseChild(StateNode falseChild) {
		this.falseChild = falseChild;
	}

	public Conditional getConditional() {
		return conditional;
	}

	public void setConditional(Conditional conditional) {
		this.conditional = conditional;
	}
	
	public Map<Local, Expression> getLocalsMap() {
		return localsMap;
	}

	public void setLocalsMap(Map<Local, Expression> localsMap) {
		this.localsMap = localsMap;
	}
	
	public void addCallSite(CallSite cs) {
		this.callsites.add(cs);
	}
	
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Conditional: ");
		builder.append(this.getConditionalString());
		builder.append("\t");
		builder.append("State map: ");
		builder.append(this.localsMap.toString());
		
		return builder.toString();
	}
	
	public String getConditionalString() {
		return conditional == null ? "null" : conditional.toString();
	}
	

	
	
}
