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
		if(!isPrint) {
			isPrint = true;
			printWriter.print(s+": ");
			for(int i=0; i< elementCount; i++){
				printWriter.print(elementData[i]+", ");
			}
			printWriter.println();
		}
		return super.pop();
	}
}

public class Exporter {
	
	public static final File outFile = new File("set.conditional");
	public static final File stateNodeFile = new File("stateNode.json");
	public static final File constraintEdgeFile = new File("constraintEdge");

	private static Map<String,PrintStack<Integer>> constraintEdgeMap = new LinkedHashMap<>();
	
	public static void run(StateNode root, Body mb) {
		//for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 1);
		System.out.println("\n");
		
		//export symbolic execution tree info to output file
		System.out.println("Exporting...");
		export(root, mb);
		System.out.println("Finish exporting!!!");
		
	}
	
	
	private static void export(StateNode root, Body mb) {
		PrintWriter out = null;
		PrintWriter stateNodeOut = null;
		PrintWriter constraintEdgeOut = null;
		try {
			if(!outFile.exists()) {
				outFile.createNewFile();
			}
			if(!stateNodeFile.exists()) {
				stateNodeFile.createNewFile();
			}
			if(!constraintEdgeFile.exists()) {
				constraintEdgeFile.createNewFile();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
			stateNodeOut = new PrintWriter(new BufferedWriter(new FileWriter(stateNodeFile, true)));
			constraintEdgeOut = new PrintWriter(new BufferedWriter(new FileWriter(constraintEdgeFile, true)));

			out.println(mb.getMethod().getSignature());
            stateNodeOut.println(mb.getMethod().getSignature());
            constraintEdgeOut.println(mb.getMethod().getSignature());

			recursiveExport(root, 0, out, stateNodeOut, constraintEdgeOut);
			constraintEdgeMap.clear();
			recursiveConstraintEdge(root, 0, constraintEdgeOut);
			//printConstraintEdge(constraintEdgeOut);


			out.println();
			stateNodeOut.println();
			constraintEdgeOut.println();

			out.close();
			stateNodeOut.close();
			constraintEdgeOut.close();
		}
		catch(IOException e) {
			e.printStackTrace();
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

	private static void recursiveConstraintEdge(StateNode root, int index, PrintWriter constraintEdgeOut) {
		if(root == null){
			return;
		}
		Set<String> Vars = root.getPegIntra_blockVars();
		// push
		for(String s: Vars){
			if(!constraintEdgeMap.containsKey(s)) {
				constraintEdgeMap.put(s, new PrintStack<Integer>());
			}
			constraintEdgeMap.get(s).push(index);
			//System.out.print(index);
		}
		//recursive operation
		recursiveConstraintEdge(root.getTrueChild(), 2 * index + 1, constraintEdgeOut);
		recursiveConstraintEdge(root.getFalseChild(), 2 * index + 2, constraintEdgeOut);
		// pop
		for(String s: Vars){
            constraintEdgeMap.get(s).pop(s, constraintEdgeOut);
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
		printOutInfo(root.getTrueChild(), 2 * id);
		printOutInfo(root.getFalseChild(), 2 * id + 1);
	}

}
