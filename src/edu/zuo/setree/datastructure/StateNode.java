package edu.zuo.setree.datastructure;

public class StateNode {
	
	private State state;
	
	private Conditional conditional;
	
	private StateNode trueChild;
	
	private StateNode falseChild;

	public StateNode(){
		state = new State();
		this.trueChild = null;
		this.falseChild = null;
	}
	
	public StateNode(State s){
		this.state = new State(s);
//		this.trueChild = tC;
//		this.falseChild = fC;
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

	
	
}
