package edu.zuo.setree.datastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acteve.symbolic.integer.Expression;
import soot.Local;

public class StateNode {
	
	//state map containing <variable, symbolic expression> mapping
	private final Map<Local, Expression> localsMap;
	
	//symbolic conditional
	private Expression conditional;
	
	//list of call-sites ahead of the node
	private List<CallSite> callsites;
	
	//formal symbolic return if applicable (only for the node containing return statement) 
	private Expression returnExpr;

	
	//two children
	private StateNode trueChild;
	private StateNode falseChild;
	
	
	public StateNode(){
		this.localsMap = new LinkedHashMap<Local, Expression>();
		
		this.conditional = null;
		this.callsites = null;
		this.returnExpr = null;

		this.trueChild = null;
		this.falseChild = null;
	}
	
	public StateNode(StateNode parentNode){
		localsMap = new LinkedHashMap<Local, Expression>(parentNode.localsMap);
		
		this.conditional = null;
		this.callsites = null;
		this.returnExpr = null;

		this.trueChild = null;
		this.falseChild = null;
	}
	
//	public StateNode(Map<Local, Expression> parentMap){
//		localsMap = new LinkedHashMap<Local, Expression>(parentMap);
//		
//		this.conditional = null;
//		this.callsites = null;
//		this.returnExpr = null;
//
//		this.trueChild = null;
//		this.falseChild = null;
//	}

	public int getCallSiteIndex() {
		return this.callsites.size();
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

	public Expression getConditional() {
		return conditional;
	}

	public void setConditional(Expression conditional) {
		this.conditional = conditional;
	}
	
//	public Map<Local, Expression> getLocalsMap() {
//		return localsMap;
//	}
	
	public void putToLocalsMap(Local l, Expression expr) {
		this.localsMap.put(l, expr);
	}
	
	public Expression getFromLocalsMap(Local l) {
		return this.localsMap.get(l);
	}

	public void addCallSite(CallSite cs) {
		if(this.callsites == null) {
			this.callsites = new ArrayList<CallSite>();
		}
		this.callsites.add(cs);
	}
	
	public Expression getReturnExpr() {
		return returnExpr;
	}

	public void setReturnExpr(Expression returnExpr) {
		this.returnExpr = returnExpr;
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
