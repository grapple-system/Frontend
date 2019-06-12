package edu.zuo.setree.intergraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.zuo.typestate.datastructure.CallSite;

public class inline_interGraph {
	public static final File consEdgeGraphFile = new File("intraOutput/consEdgeGraph");
	public static final File var2indexMapFile = new File("intraOutput/var2indexMap");
	public static final File conditionalSmt2File = new File("intraOutput/conditionalSmt2");
	public static final File callInfoFile = new File("intraOutput/callInfoFile");
	public static final File calledVarFile = new File("intraOutput/calledVarFile");
	public static final File methodReturnFile = new File("intraOutput/methodReturnFile");
	public static int funcIndex = -1;

	private static Map<pair, Integer> pair2indexMap = new LinkedHashMap<>();
	// <func.varstr, varindex>
	private static Map<String, Integer> varStr2varindexMap = new LinkedHashMap<>();
	private static Map<Integer, String> index2varMap = new LinkedHashMap<>();
	private static Map<String, Integer> func2indexMap = new LinkedHashMap<>();
	private static Map<Integer, Map<String, Integer>> funcParamReturn = new LinkedHashMap<>();
	private static List<String> calledVarList = new ArrayList<String>();
	private static Map<String, List<CallSite>> callSiteMap = new LinkedHashMap<>();
	private static Map<Integer, List<Integer>> meturnReturnMap = new LinkedHashMap<>();

	/**
	 * each line of pair2varMapFile is (funcIndex,inFuncVarIndex) :
	 * outFuncVarIndex
	 */
	public static final File pair2indexMapFile = new File("interOutput/pair2indexMap");

	public static final File varStr2varindexMapFile = new File("interOutput/varStr2varindexMap");

	/**
	 * each line of index2varMapFile is outFuncVarIndex :
	 * funcIndex.inFuncVarIndex.nodeIndex.varName
	 */
	public static final File index2varMapFile = new File("interOutput/index2varMap");

	/**
	 * each line of func2indexMapFile is funcIndex : funcName
	 */
	public static final File func2indexMapFile = new File("interOutput/func2indexMap");

	/**
	 * each line of interGraphFile is outFuncVarIndex outFuncVarIndex label
	 * constraint label: a|e|l|n|o|p|r|s constraint: [T]|[pair,pair] pair:
	 * (funcIndex, nodeIndex)
	 *
	 * [Assign] a [Load] l [New] n [Callee] c [Param] p [Return] r [Store] s
	 * other e
	 */
	public static final File interGraphFile = new File("interOutput/interGraph");

	/**
	 * each line of interSmt2File is (funcIndex, nodeIndex):constraintString
	 */
	public static final File interSmt2File = new File("interOutput/interSmt2");

	public static final File callSiteFile = new File("interOutput/callSite");

	public static void genMapTSC() {
		try {
			if (!consEdgeGraphFile.exists()) {
				System.out.println("Error: consEdgeGraph file not exists.");
				return;
			}
			if (!var2indexMapFile.exists()) {
				System.out.println("Error: var2indexMap file not exists.");
				return;
			}
			if (!calledVarFile.exists()) {
				System.out.println("Error: calledVar file not exists.");
				return;
			}
			Scanner consEdgeGraphInput = new Scanner(consEdgeGraphFile);
			Scanner var2indexMapInput = new Scanner(var2indexMapFile);
			Scanner calledVarListInput = new Scanner(calledVarFile);
			// Scanner callInfoFileInput = new Scanner(callInfoFile);
			int varIndex = 0;
			while (var2indexMapInput.hasNextLine()) {
				String line = var2indexMapInput.nextLine();
				// System.out.println("#"+line+"#");
				if (line.length() == 0) {

				} else if (line.startsWith("<")) {
					++funcIndex;
					func2indexMap.put(line, funcIndex);
				} else {
					String[] tokens = line.split(" : ");
					int right = Integer.parseInt(tokens[0]);
					pair2indexMap.put(new pair(funcIndex, right), varIndex);
					varStr2varindexMap.put(funcIndex + "." + tokens[1], right);
					index2varMap.put(varIndex, funcIndex + "." + tokens[0] + "." + tokens[1]);
					varIndex++;
				}
			}

			funcIndex = -1;
			while (consEdgeGraphInput.hasNextLine()) {
				String line = consEdgeGraphInput.nextLine();
				if (line.length() == 0) {

				} else if (line.startsWith("<")) {
					++funcIndex;
					funcParamReturn.put(funcIndex, new LinkedHashMap<String, Integer>());
				} else if (line.startsWith(":")) {
					continue;
				} else {
					String[] tokens = line.split(", ");
					if (tokens.length == 2) {
						int i = Integer.parseInt(tokens[0]);
						funcParamReturn.get(funcIndex).put(tokens[1], i);
					}
				}
			}

			while (calledVarListInput.hasNextLine()) {
				String line = calledVarListInput.nextLine();
				calledVarList.add(line);
			}
			consEdgeGraphInput.close();
			var2indexMapInput.close();
			calledVarListInput.close();
			// callInfoFileInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printMapTSC() {
		PrintWriter pair2indexMapOut = null;
		PrintWriter index2varMapOut = null;
		PrintWriter func2indexMapOut = null;
		PrintWriter varStr2varindexMapOut = null;
		try {
			if (!pair2indexMapFile.exists()) {
				pair2indexMapFile.createNewFile();
			}
			if (!index2varMapFile.exists()) {
				index2varMapFile.createNewFile();
			}
			if (!func2indexMapFile.exists()) {
				func2indexMapFile.createNewFile();
			}
			if (!varStr2varindexMapFile.exists()) {
				varStr2varindexMapFile.createNewFile();
			}

			pair2indexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(pair2indexMapFile, true)));
			index2varMapOut = new PrintWriter(new BufferedWriter(new FileWriter(index2varMapFile, true)));
			func2indexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(func2indexMapFile, true)));
			varStr2varindexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(varStr2varindexMapFile, true)));

			for (pair p : pair2indexMap.keySet()) {
				pair2indexMapOut.println(p.toString() + " : " + pair2indexMap.get(p));
			}
			for (Integer i : index2varMap.keySet()) {
				index2varMapOut.println(i + " : " + index2varMap.get(i));
			}
			for (String f : func2indexMap.keySet()) {
				func2indexMapOut.println(func2indexMap.get(f).toString() + " : " + f);
			}
			for (String s : varStr2varindexMap.keySet()) {
				varStr2varindexMapOut.println(varStr2varindexMap.get(s) + " : " + s);
			}
			pair2indexMapOut.close();
			index2varMapOut.close();
			func2indexMapOut.close();
			varStr2varindexMapOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void formalReturns() {
		try {
			Scanner methodReturnFileInput = new Scanner(methodReturnFile);
			while (methodReturnFileInput.hasNextLine()) {
				String line = methodReturnFileInput.nextLine();
				String[] tokens = line.split(", ");
				int methodIndex = func2indexMap.get(tokens[0]);
				List<Integer> formalReturns = null;
				if (meturnReturnMap.containsKey(methodIndex))
					formalReturns = meturnReturnMap.get(methodIndex);
				else
					formalReturns = new ArrayList<Integer>();
				String[] methodreturn = tokens[2].split("\t");
				for (String ret : methodreturn) {
					int varIndex = varStr2varindexMap.get(methodIndex + "." + ret);
					int i1 = pair2indexMap.get(new pair(methodIndex, varIndex));
					if (!formalReturns.contains(i1))
						formalReturns.add(i1);
				}
				meturnReturnMap.put(methodIndex, formalReturns);
			}
			methodReturnFileInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dealCallSite() {
		PrintWriter callSiteOut = null;
		try {
			if (!callSiteFile.exists()) {
				callSiteFile.createNewFile();
			}
			callSiteOut = new PrintWriter(new BufferedWriter(new FileWriter(callSiteFile, true)));
			Scanner callInfoFileInput = new Scanner(callInfoFile);
			while (callInfoFileInput.hasNextLine()) {
				String line = callInfoFileInput.nextLine();
				String[] tokens = line.split(", ");
				assert(tokens.length == 5);
				String callNum = tokens[0];
				int callMethodIndex = func2indexMap.get(tokens[1]);
				int receiveMethodIndex = func2indexMap.get(tokens[3]);
				int callVarIndex = varStr2varindexMap.get(callMethodIndex + "." + tokens[2]);
				int receiveVarIndex = varStr2varindexMap.get(receiveMethodIndex + "." + tokens[4]);
				int i1 = pair2indexMap.get(new pair(callMethodIndex, callVarIndex));
				int i2 = pair2indexMap.get(new pair(receiveMethodIndex, receiveVarIndex));
				pair p1 = new pair(callMethodIndex, Integer.parseInt(tokens[2].split("[.]")[1]));
				pair p2 = new pair(receiveMethodIndex, Integer.parseInt(tokens[4].split("[.]")[1]));
				String receiveIndex = tokens[4].split("[.]")[1];
				if (receiveIndex.equals("0")) {
					callSiteOut
							.println(callNum + "\t" + i1 + "\t" + i2 + "\te\t" + p1.toString() + "," + p2.toString());
					String callMethod = tokens[1];
					List<CallSite> value = null;
					CallSite thiscallsite = null;
					if (callSiteMap.containsKey(callMethod))
						value = callSiteMap.get(callMethod);
					else
						value = new ArrayList<CallSite>();
					for (CallSite callsite : value) {
						if (callsite.callNum.equals(callNum))
							thiscallsite = callsite;
					}
					if (thiscallsite != null) {
						thiscallsite.addarg(callMethod, tokens[3], String.valueOf(i1));
					} else {
						thiscallsite = new CallSite();
						thiscallsite.callNum = callNum;
						thiscallsite.callMethod = callMethod;
						thiscallsite.receiveMethod = tokens[3];
						thiscallsite.addarg(callMethod, tokens[3], String.valueOf(i1));
						value.add(thiscallsite);
					}
					callSiteMap.put(callMethod, value);
				} else {
					callSiteOut
							.println(callNum + "\t" + i1 + "\t" + i2 + "\te\t" + p1.toString() + "," + p2.toString());
					String callMethod = tokens[3];
					List<CallSite> value = null;
					CallSite thiscallsite = null;
					if (callSiteMap.containsKey(callMethod))
						value = callSiteMap.get(callMethod);
					else
						value = new ArrayList<CallSite>();
					for (CallSite callsite : value) {
						if (callsite.callNum.equals(callNum))
							thiscallsite = callsite;
					}
					if (thiscallsite != null) {
						thiscallsite.addreturn(callMethod, tokens[1], String.valueOf(i2));
					} else {
						thiscallsite = new CallSite();
						thiscallsite.callNum = callNum;
						thiscallsite.callMethod = callMethod;
						thiscallsite.receiveMethod = tokens[1];
						thiscallsite.addarg(callMethod, tokens[1], String.valueOf(i2));
						value.add(thiscallsite);
					}
					callSiteMap.put(callMethod, value);
				}
			}

			callInfoFileInput.close();
			callSiteOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printGraphTSC() {
		String funcName = "";
		String calledVar = "";
		boolean edges = false;
		String formalPars = "Formal Parameters: [";
		String formalRets = "Formal Returns: [";
		try {
			Scanner consEdgeGraphInput = new Scanner(consEdgeGraphFile);
			funcIndex = -1;
			PrintWriter interGraphOut = null;
			if (!interGraphFile.exists()) {
				interGraphFile.createNewFile();
			}
			interGraphOut = new PrintWriter(new BufferedWriter(new FileWriter(interGraphFile, true)));
			while (consEdgeGraphInput.hasNextLine()) {
				String line = consEdgeGraphInput.nextLine();
				System.out.println("#" + line + "#");
				if (line.length() == 0) {

				} else if (line.startsWith("<")) {
					if (funcIndex != -1) {
						interGraphOut.println("]");
						formalPars = formalPars + "]";
						interGraphOut.println(formalPars);
						formalPars = "Formal Parameters: [";
						interGraphOut.println(formalRets);
						formalRets = "Formal Returns: [";
						interGraphOut.println("");
					}
					edges = false;
					funcName = line;
					++funcIndex;
					interGraphOut.println("------------------------IntraGraph------------------------");
					String[] sp = funcName.split(" ");
					String file = sp[0].substring(1, sp[0].lastIndexOf(":"));
					String method = sp[2].substring(0, sp[2].lastIndexOf(">"));
					interGraphOut.println("File: [" + file + "]");
					interGraphOut.println("Function: [" + method + "]");
					// deal callsite
					if (callSiteMap.containsKey(funcName)) {
						List<CallSite> value = callSiteMap.get(funcName);
						for (CallSite callsite : value) {
							interGraphOut.println("Call Sites: " + callsite.callNum + ": [" + callsite.print() + "]");
						}
					}
					// deal formal returns
					List<Integer> forret = meturnReturnMap.get(funcIndex);
					if (forret != null) {
						for (int ret : forret) {
							formalRets = formalRets + String.valueOf(ret) + "\t";
						}
					}
					formalRets = formalRets + "]";
				} else if (line.startsWith(":")) {
					calledVar = funcName + line;
					if (!edges) {
						interGraphOut.print("Edges: [");
						edges = true;
					}
					continue;
				} else {
					String[] tokens = line.split(", ");
					assert(tokens.length == 4);
					/*
					 * a, b, [x, y]
					 */
					System.out.println(4);
					int first_right = Integer.parseInt(tokens[0]);
					int second_right = Integer.parseInt(tokens[1]);
					pair p1 = new pair(funcIndex, first_right);
					pair p2 = new pair(funcIndex, second_right);
					int i1 = pair2indexMap.get(p1);
					int i2 = pair2indexMap.get(p2);
					int n1_right = Integer.parseInt(tokens[2].substring(1));
					int n2_right = Integer.parseInt(tokens[3].substring(0, tokens[3].length() - 1));
					pair p3 = new pair(funcIndex, n1_right);
					pair p4 = new pair(funcIndex, n2_right);
					if (n1_right == 0) {
						formalPars = formalPars + String.valueOf(i1) + "\t";
					}
					if (n1_right == 0 && index2varMap.get(i1).contains("uninit") && !calledVarList.contains(calledVar))
						interGraphOut.print(i1 + ";" + i2 + ";n;" + p3.toString() + "," + p4.toString() + "\t");
					else if (n1_right != n2_right)
						interGraphOut.print(i1 + ";" + i2 + ";e;" + p3.toString() + "," + p4.toString() + "\t");
					else
						interGraphOut.print(i1 + ";" + i2 + ";e;T\t");
				}
			}
			interGraphOut.println("]");
			formalPars = formalPars + "]";
			interGraphOut.println(formalPars);
			formalPars = "Formal Parameters: [";
			interGraphOut.println(formalRets);
			formalRets = "Formal Returns: [";
			interGraphOut.println("");

			consEdgeGraphInput.close();
			interGraphOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

    public static void printSmt2TSC() {
        try{
            if (!conditionalSmt2File.exists()) {
                System.out.println("Error: conditionalSmt2 file not exists.");
                return;
            }
            Scanner conditionalSmt2Input = new Scanner(conditionalSmt2File);
            PrintWriter interSmt2Out = null;
            if (!interSmt2File.exists()){
                interSmt2File.createNewFile();
            }
            interSmt2Out = new PrintWriter(new BufferedWriter(new FileWriter(interSmt2File, true)));
            funcIndex = -1;
            while(conditionalSmt2Input.hasNextLine()){
                String line = conditionalSmt2Input.nextLine();
                if(line.length()==0){

                }else if(line.startsWith("<")){
                    ++funcIndex;
                }else {
                    String []tokens=line.split(":");
                    if(tokens[1].startsWith("#")){

                    }else {
                        String t = tokens[1].replace("$I", "$I$" + funcIndex).replace("$L", "$L$" + funcIndex).replace("$F", "$F$" + funcIndex).replace("$D", "$D$" + funcIndex).replace("$Z", "$Z$" + funcIndex).replace("$R", "$R$" + funcIndex).replace("$B", "$B$" + funcIndex);
                        interSmt2Out.println("(" + funcIndex + "," + tokens[0] + "):" + t);
                    }
                }
            }
            conditionalSmt2Input.close();
            interSmt2Out.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

	public static void main(String args[]) {
		genMapTSC();
		formalReturns();
		dealCallSite();
		printGraphTSC();
		printMapTSC();
		printSmt2TSC();
	}
}
