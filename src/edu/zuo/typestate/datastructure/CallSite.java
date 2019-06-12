package edu.zuo.typestate.datastructure;

import java.util.ArrayList;
import java.util.List;

public class CallSite {
	public String callNum;
	public String callMethod;
	public String receiveMethod;
	public List<String> actualArgs = new ArrayList<String>();
	public List<String> actualReturns = new ArrayList<String>();
	
	public void addarg(String call, String receive, String actualarg){
		assert(callMethod.equals(call) && receiveMethod.equals(receive));
		if(!actualArgs.contains(actualarg))
			actualArgs.add(actualarg);
	}
	
	public void addreturn(String call, String receive, String actualreturn){
		assert(callMethod.equals(call) && receiveMethod.equals(receive));
		if(!actualReturns.contains(actualreturn))
			actualReturns.add(actualreturn);
	}
	
	public String getActualArgs(){
		String result = "<";
		for(String arg : actualArgs){
			result = result+arg+";";
		}
		result = result+">";
		return result;
	}
	
	public String getActualReturns(){
		String result = "<";
		for(String returns : actualReturns){
			result = result+returns+";";
		}
		result = result+">";
		return result;
	}
	
	public String print(){
		String call = callMethod;
		call = call.substring(callMethod.indexOf("<")+1, callMethod.lastIndexOf(">"));
		String receive = receiveMethod.substring(receiveMethod.indexOf("<")+1, receiveMethod.lastIndexOf(">"));
		String[] split = receive.split(": ");
		String receivemethod = split[1].substring(split[1].indexOf(" ")+1);
		return split[0]+"\t"+receivemethod+"\t"+getActualArgs()+"\t"+getActualReturns();
	}
}
