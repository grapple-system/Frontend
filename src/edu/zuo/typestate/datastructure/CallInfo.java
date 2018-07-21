package edu.zuo.typestate.datastructure;

public class CallInfo {
	private String callMethod;
	private String receiveMethod;
	private int callNodeIndex;
	//private Point callPoint;
	//private Point receivePoint;
	//varname of call method. ex: index.var.state_hashcode
	private String callStr;
	//parname of receive method
	private String receiveStr;

	public CallInfo(String callmethod, String receivemethod, int index, String var, String par) {
		this.callMethod = callmethod;
		this.receiveMethod = receivemethod;
		this.callNodeIndex = index;
		this.callStr = var;
		this.receiveStr = par;
		//this.callPoint = call;
		//this.receivePoint = receive;
	}
	

	public String callVar() {
		return callStr.split(".")[1];
	}

	public String receiveVar() {
		return receiveStr.split(".")[1];
	}

	public String callHash() {
		return (callStr.split(".")[2]).split("_")[1];
	}

	public String receiveHash() {
		return (receiveStr.split(".")[2]).split("_")[1];
	}

	public String callStr() {
		return callStr.split(".")[2];
	}

	public String receiveStr() {
		return receiveStr.split(".")[2];
	}

	public int nodeIndex() {
		return callNodeIndex;
	}

	public String callMethod() {
		return callMethod;
	}

	public String receiveMethod() {
		return receiveMethod;
	}
	
	public String print2str(){
		return callMethod+","+callStr+","+receiveMethod+","+receiveStr;
	}
}
