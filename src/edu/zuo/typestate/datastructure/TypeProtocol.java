package edu.zuo.typestate.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeProtocol {
	public String classname;
	public String[] allStates;
	public Map<String, List<String>> statetrans = new HashMap<String, List<String>>();
	
	public void addTrans(String method, String statetran){
		if(statetrans.containsKey(method)){
			List<String> lst = statetrans.get(method);
			lst.add(statetran);
			statetrans.put(method, lst);
		}else{
			List<String> lst = new ArrayList<String>();
			lst.add(statetran);
			statetrans.put(method, lst);
		}
	}
}