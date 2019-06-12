package edu.zuo.typestate.datastructure;

public class CallInfo {
	private int callNum;
	private String callMethod;
	private String receiveMethod;
	private int callNodeIndex;
	//private Point callPoint;
	//private Point receivePoint;
	//varname of call method. ex: index.var.state_hashcode
	private String callStr;
	//parname of receive method
	private String receiveStr;

	public CallInfo(String callmethod, String receivemethod, int index, String var, String par, int callNum) {
		this.callMethod = callmethod;
		this.receiveMethod = receivemethod;
		this.callNodeIndex = index;
		this.callStr = var;
		this.receiveStr = par;
		this.callNum = callNum;
		//this.callPoint = call;
		//this.receivePoint = receive;
	}
	
	public CallInfo(CallInfo callinfo){
		this.callMethod = callinfo.callMethod();
		this.receiveMethod = callinfo.receiveMethod();
		this.callNodeIndex = callinfo.callnodeIndex();
		this.callStr = callinfo.callStr();
		this.receiveStr = callinfo.receiveStr();
		this.callNum = callinfo.callNum();
	}
	
	public void changeCallStr(String callStr){
		this.callStr = callStr;
		this.callNodeIndex = Integer.parseInt(callStr.split("[.]")[1]);
	}

	public int callNum(){
		return callNum;
	}
	
	public String callVar() {
		String[] sa = callStr.split("[.]");
		return sa[0];
	}

	public String receiveVar() {
		return receiveStr.split("[.]")[0];
	}

	public String callState() {
		return (callStr.split("[.]")[2]).split("_")[0];
	}

	public String receiveState() {
		return (receiveStr.split("[.]")[2]).split("_")[0];
	}
	
	public String callHash() {
		return (callStr.split("[.]")[2]).split("_")[1];
	}

	public String receiveHash() {
		return (receiveStr.split("[.]")[2]).split("_")[1];
	}

	public String callStr() {
		return callStr;
	}

	public String receiveStr() {
		return receiveStr;
	}

	public int callnodeIndex() {
		return callNodeIndex;
	}

	public String callMethod() {
		return callMethod;
	}

	public String receiveMethod() {
		return receiveMethod;
	}
	
	public String print2str(){
		return callNum+", "+callMethod+", "+callStr+", "+receiveMethod+", "+receiveStr;
	}
}
