package edu.zuo.typestate.datastructure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CallExecutor {
	public static List<CallInfo> callInfoList = new ArrayList<CallInfo>();
	private static List<CallInfo> finalcallInfoList = new ArrayList<CallInfo>();
	// <method: var, <returnstr,...>>
	public static Map<String, List<String>> methodReturn = new HashMap<String, List<String>>();
	public static int callNum = -1;

	public static void addRet(String method, ConstraintGraphList constraint_graph_list) {
		for (ConstraintGraph cg : constraint_graph_list.cgl) {
			String var = cg.varname;
			String key = method + ", " + var;
			for (ConstraintEdge ce : cg.constraintedges) {
				//String ret = var + "." + ce.getStartStr();
				String ret = var + "." + ce.getStartNode()+ "." +ce.getStart().getName()+ "_" +ce.getEnd().getHashcode();
				List<String> value = methodReturn.get(key);
				if (value == null) {
					value = new ArrayList<String>();
					value.add(ret);
					methodReturn.put(key, value);
				} else {
					assert(!value.contains(ret));
					value.add(ret);
					methodReturn.put(key, value);
				}
			}
		}
	}
	
	public static void initCallNum(File file){
		try {
			if(!file.exists()){
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write("-1\n");
				fw.close();
			}
			Scanner reader = new Scanner(file);
			String line = reader.nextLine();
			callNum = Integer.parseInt(line);
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void exitCallNum(File file){
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
			pw.println(callNum);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dealReturn() {
		for (CallInfo callinfo : callInfoList) {
			if (callinfo.callnodeIndex() == -2) {
				String key = callinfo.callMethod() + ", " + callinfo.callVar();
				List<String> value = methodReturn.get(key);
				assert(value != null);
				for (String ret : value) {
					String retstate = (ret.split("[.]")[2]).split("_")[0];
					if (retstate.equals(callinfo.receiveState())) {
						CallInfo newCallInfo = new CallInfo(callinfo);
						newCallInfo.changeCallStr(ret);
						finalcallInfoList.add(newCallInfo);
					}
				}
			} else {
				finalcallInfoList.add(callinfo);
			}
		}
	}

	public static void printCallInfo(String filepath) {
		File file = new File(filepath);
		try {
			if (!file.exists())
				file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for (CallInfo callinfo : finalcallInfoList) {
				assert(callinfo.callnodeIndex() != -2);
				pw.println(callinfo.print2str());
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//callInfoList.clear();
		//finalcallInfoList.clear();
	}

	public static void printMetReturn(String filepath) {
		File file = new File(filepath);
		try {
			if (!file.exists())
				file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for (Map.Entry<String, List<String>> entry : methodReturn.entrySet()) {
				String line = entry.getKey()+", ";
				for(String ret : entry.getValue()){
					line = line+ret+"\t";
				}
				pw.println(line);
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//methodReturn.clear();
	}
}
