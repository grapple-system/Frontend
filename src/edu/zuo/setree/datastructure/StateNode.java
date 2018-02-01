package edu.zuo.setree.datastructure;

public class StateNode {
	
	private State state;
	
	private Conditional conditional;
	
	private StateNode trueChild;
	
	private StateNode falseChild;

	public StateNode(){
		state = new State();
		this.conditional = null;
		this.trueChild = null;
		this.falseChild = null;
	}
	
	public StateNode(State s){
		this.state = new State(s);
		this.conditional = null;
		this.trueChild = null;
		this.falseChild = null;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
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
	
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Conditional: ");
		builder.append(this.getConditionalString());
		builder.append("\t");
		builder.append("State map: ");
		builder.append(state.toString());
		
		return builder.toString();
	}
	
	public String getConditionalString() {
		return conditional == null ? "null" : conditional.toString();
	}
	

	
	
}
