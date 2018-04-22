package edu.zuo.setree.export;

import java.io.*;
import java.util.*;

import acteve.symbolic.integer.Expression;
import com.sun.org.apache.xpath.internal.operations.Bool;
import edu.zuo.setree.datastructure.CallSite;
import edu.zuo.setree.datastructure.StateNode;
import edu.zuo.setree.JSON.JSON;
import soot.Body;
import soot.Immediate;

class PrintStack<E> extends Stack<E> {
	private boolean isPrint;

	public PrintStack() {
		isPrint=false;
	}

	public E push(E item) {
		isPrint = false;
		return super.push(item);
	}

	public synchronized E pop(String s, PrintWriter printWriter) {
//		if(!isPrint) {
//			isPrint = true;
//			printWriter.print(s+": ");
//			for(int i=0; i< elementCount; i++){
//				printWriter.print(elementData[i]+", ");
//			}
//			printWriter.println();
//		}
		return super.pop();
	}
}

public class Exporter {
	
	public static final File outFile = new File("set.conditional");
	public static final File stateNodeFile = new File("stateNode.json");
	public static final File consEdgeGraphFile = new File("consEdgeGraph");
	public static final File var2indexMapFile = new File("var2indexMap");

	private static Map<String,PrintStack<Integer>> constraintEdgeMap = new LinkedHashMap<>();
	private static Map<String, Integer> var2indexMap = new LinkedHashMap<>();
	
	public static void run(StateNode root, Body mb) {
		//for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 0);
		System.out.println("\n");
		
		//export symbolic execution tree info to output file
		System.out.println("Exporting...");
		export(root, mb);
		System.out.println("Finish exporting!!!");
		
	}
	
	
	private static void export(StateNode root, Body mb) {
		PrintWriter out = null;
		PrintWriter stateNodeOut = null;
		PrintWriter consEdgeGraphOut = null;
		PrintWriter var2indexMapOut = null;
		try {
			if(!outFile.exists()) {
				outFile.createNewFile();
			}
			if(!stateNodeFile.exists()) {
				stateNodeFile.createNewFile();
			}
			if(!consEdgeGraphFile.exists()) {
				consEdgeGraphFile.createNewFile();
			}
			if(!var2indexMapFile.exists()) {
				var2indexMapFile.createNewFile();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
			stateNodeOut = new PrintWriter(new BufferedWriter(new FileWriter(stateNodeFile, true)));
			consEdgeGraphOut = new PrintWriter(new BufferedWriter(new FileWriter(consEdgeGraphFile, true)));
			var2indexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(var2indexMapFile, true)));

			//Print Function Signature
			out.println(mb.getMethod().getSignature());
            stateNodeOut.println(mb.getMethod().getSignature());
			consEdgeGraphOut.println(mb.getMethod().getSignature());
            var2indexMapOut.println(mb.getMethod().getSignature());

            // Recursive
			recursiveExport(root, 0, out, stateNodeOut, consEdgeGraphOut);
			constraintEdgeMap.clear();
			var2indexMap.clear();
			recursiveconsEdgeGraph(root, 0, consEdgeGraphOut);
			//printConstraintEdge(constraintEdgeOut);
			printVar2indexMap(var2indexMapOut);


			out.println();
			stateNodeOut.println();
			consEdgeGraphOut.println();
			var2indexMapOut.println();

			out.close();
			stateNodeOut.close();
			consEdgeGraphOut.close();
			var2indexMapOut.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void putVar2indexMap(String s){
		if(!var2indexMap.containsKey(s)){
			var2indexMap.put(s, var2indexMap.size());
		}
	}
	
	private static void recursiveExport(StateNode root, int index, PrintWriter out, PrintWriter stateNodeOut, PrintWriter constraintEdgeOut) {
		//termination
		if(root == null) {
		    stateNodeOut.println(JSON.toJsonSet("stateNode",null));
			return;
		}
		
		//export operation
		if(root.getConditional() != null) {
			out.println(index + ":" + root.getConditional().toString());
		}

		//export stateNode
        printStateNode(root, stateNodeOut);


		//recursive operation 
		recursiveExport(root.getTrueChild(), 2 * index + 1, out, stateNodeOut, constraintEdgeOut);
		recursiveExport(root.getFalseChild(), 2 * index + 2, out, stateNodeOut, constraintEdgeOut);
	}

	private static void recursiveconsEdgeGraph(StateNode root, int index, PrintWriter consEdgeGraphOut) {
		if(root == null){
			return;
		}
		System.out.println("----------"+index+"----------");
		Set<String> Vars = root.getPegIntra_blockVars();
		// push
		for(String s: Vars){
			putVar2indexMap(index+"."+s);
			if(!constraintEdgeMap.containsKey(s)) {
				constraintEdgeMap.put(s, new PrintStack<Integer>());
			}else if (constraintEdgeMap.get(s).size() != 0){
				int start = constraintEdgeMap.get(s).peek();
				consEdgeGraphOut.println(var2indexMap.get(start+"."+s)+", "+var2indexMap.get(index+"."+s)+", ["+start+", "+index+"]");
			}
			constraintEdgeMap.get(s).push(index);
			//System.out.print(index);
		}
		//
		consEdgeGraphOut.print(root.getPeg_intra_block().toString(var2indexMap, index));
		//recursive operation
		recursiveconsEdgeGraph(root.getTrueChild(), 2 * index + 1, consEdgeGraphOut);
		recursiveconsEdgeGraph(root.getFalseChild(), 2 * index + 2, consEdgeGraphOut);
		// pop
		for(String s: Vars){
            constraintEdgeMap.get(s).pop(s, consEdgeGraphOut);
			//System.out.print(index);
		}
	}

//	private static void printConstraintEdge(PrintWriter constraintEdgeOut) {
//		for(String str: constraintEdgeMap.keySet()){
//			constraintEdgeOut.print(str+": ");
//			List<Integer> list = constraintEdgeMap.get(str);
//			for(Integer index: list){
//				constraintEdgeOut.print(index.toString()+", ");
//			}
//			constraintEdgeOut.println();
//		}
//	}

	//root != null
	private static void printStateNode(StateNode root, PrintWriter stateNodeOut) {
	    List<String> stringList = new ArrayList<>();
        //print conditional
        if(root.getConditional() != null) {
            stringList.add(JSON.toJson("conditional",root.getConditional().toString()));
        }else {
            stringList.add(JSON.toJson("conditional", null));
        }
        //print callSites
        if(root.getCallsites() != null) {
            List<String> callSiteStringList = new ArrayList<>();
            List<CallSite> callSiteList = root.getCallsites();
            for(int i=0;i<callSiteList.size();i++){
                callSiteStringList.add(getCallSite(callSiteList.get(i)));
            }
            stringList.add(JSON.toJsonArray("callsites", callSiteStringList));
        }else {
            stringList.add(JSON.toJson("callsites", null));
        }
        //print returnExpr
        if(root.getReturnExpr() != null) {
            stringList.add(JSON.toJson("returnExp", root.getReturnExpr().toString()));
        }else {
            stringList.add(JSON.toJson("returnExp", null));
        }
        stateNodeOut.println(JSON.toJsonSet("stateNode", stringList));
    }

    private static void printVar2indexMap(PrintWriter var2indexMapOut) {
		for(String s: var2indexMap.keySet()){
			var2indexMapOut.println(var2indexMap.get(s) + " : " + s);
		}
	}

    private static String getCallSite(CallSite callSite) {
	    List<String> stringList = new ArrayList<>();
	    //print signature
	    if(callSite.getSignature() != null) {
            stringList.add(JSON.toJson("signature",callSite.getSignature()));
        }else {
            stringList.add(JSON.toJson("signature",null));
        }
        //print callee
        stringList.add(JSON.toJson("callee",callSite.getCalleeString()));
	    //print argumentsMap
        List<String> argStringList = new ArrayList<>();
        for (Immediate im : callSite.getArgumentsMap().keySet()) {
            Expression expr = callSite.getArgumentsMap().get(im);
            if(im != null && im != null) {
                argStringList.add(JSON.toJson(im.toString()+" = "+expr.exprString()));
            }
        }
        if(argStringList.size() != 0){
            stringList.add(JSON.toJsonArray("argumentsMap",argStringList));
        }else {
            stringList.add(JSON.toJson("argumentsMap",null));
        }
        //print retVar
        if(callSite.getRetVar() != null) {
            stringList.add(JSON.toJson("retVar",callSite.getRetVar().toString()));
        }else {
            stringList.add(JSON.toJson("retVar",null));
        }
        return JSON.toJsonSet(stringList);
    }


	/** print out state information
	 * @param root
	 * @param id
	 */
	private static void printOutInfo(StateNode root, int id) {
		// TODO Auto-generated method stub
		if(root == null){
			return;
		}
		System.out.println(id + ": " + root.toString());
		printOutInfo(root.getTrueChild(), 2 * id + 1);
		printOutInfo(root.getFalseChild(), 2 * id + 2);
	}

}
