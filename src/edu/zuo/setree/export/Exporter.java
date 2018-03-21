package edu.zuo.setree.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import acteve.symbolic.integer.Expression;
import edu.zuo.setree.datastructure.CallSite;
import edu.zuo.setree.datastructure.StateNode;
import edu.zuo.setree.JSON.JSON;
import soot.Body;
import soot.Immediate;

public class Exporter {
	
	public static final File outFile = new File("set.conditional");
	public static final File stateNodeFile = new File("stateNode.json");
	
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
		try {
			if(!outFile.exists()) {
				outFile.createNewFile();
			}
			if(!stateNodeFile.exists()) {
				stateNodeFile.createNewFile();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
			stateNodeOut = new PrintWriter(new BufferedWriter(new FileWriter(stateNodeFile, true)));
			
			out.println(mb.getMethod().getSignature());
            stateNodeOut.println(mb.getMethod().getSignature());

			recursiveExport(root, 1, out, stateNodeOut);
			out.println();
			stateNodeOut.println();
			
			out.close();
			stateNodeOut.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static void recursiveExport(StateNode root, int index, PrintWriter out, PrintWriter stateNodeOut) {
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
		recursiveExport(root.getTrueChild(), 2 * index, out, stateNodeOut);
		recursiveExport(root.getFalseChild(), 2 * index + 1, out, stateNodeOut);
	}

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
            stringList.add(JSON.toJsonArray("callSites", callSiteStringList));
        }else {
            stringList.add(JSON.toJson("callsites", null));
        }
        //print returnExpr
        if(root.getReturnExpr() != null) {
            stringList.add(JSON.toJson("returnExp", root.getReturnExpr().exprString()));
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
