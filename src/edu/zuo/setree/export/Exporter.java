package edu.zuo.setree.export;

import java.io.*;
import java.util.*;

import acteve.symbolic.integer.*;
//import com.sun.org.apache.xpath.internal.operations.Bool;
import edu.zuo.setree.datastructure.CallSite;
import edu.zuo.setree.datastructure.StateNode;
import edu.zuo.typestate.datastructure.ConstraintEdge;
import edu.zuo.typestate.datastructure.ConstraintGraph;
import edu.zuo.typestate.datastructure.TransEdge;
import edu.zuo.typestate.datastructure.TypeGraph;

import edu.zuo.setree.JSON.JSON;
import soot.*;

public class Exporter {

	public static final File outFile = new File("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/set.conditional");
	public static final File stateNodeFile = new File("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/stateNode.json");
	public static final File conditionalSmt2File = new File("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/conditionalSmt2");
	public static final File consEdgeGraphFile = new File("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/consEdgeGraph");
	public static final File var2indexMapFile = new File("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/var2indexMap");


	private static Map<String, Stack<Integer>> constraintEdgeMap = new LinkedHashMap<>();
	private static Map<String, Integer> var2indexMap = new LinkedHashMap<>();

	public static void run(StateNode root, Body mb) {
		// for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 0);
		System.out.println("\n");

		// export symbolic execution tree info to output file
		System.out.println("Exporting...");
		export(root, mb);
		System.out.println("Finish exporting!!!");

	}

	private static void export(StateNode root, Body mb) {
		PrintWriter out = null;
		PrintWriter stateNodeOut = null;
		PrintWriter consEdgeGraphOut = null;
		PrintWriter var2indexMapOut = null;
		PrintWriter conditionalSmt2Out = null;
		try {
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
			if (!stateNodeFile.exists()) {
				stateNodeFile.createNewFile();
			}
			if (!conditionalSmt2File.exists()) {
				conditionalSmt2File.createNewFile();
			}
			if (!consEdgeGraphFile.exists()) {
				consEdgeGraphFile.createNewFile();
			}
			if (!var2indexMapFile.exists()) {
				var2indexMapFile.createNewFile();
			}
			
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
			stateNodeOut = new PrintWriter(new BufferedWriter(new FileWriter(stateNodeFile, true)));
			conditionalSmt2Out = new PrintWriter((new BufferedWriter(new FileWriter(conditionalSmt2File, true))));
			consEdgeGraphOut = new PrintWriter(new BufferedWriter(new FileWriter(consEdgeGraphFile, true)));
			var2indexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(var2indexMapFile, true)));

			// Print Function Signature
			out.println(mb.getMethod().getSignature());
			stateNodeOut.println(mb.getMethod().getSignature());
			conditionalSmt2Out.println(mb.getMethod().getSignature());
			consEdgeGraphOut.println(mb.getMethod().getSignature());
			var2indexMapOut.println(mb.getMethod().getSignature());

			// Recursive
			System.out.println("Exporting set.conditional...");
			recursiveExport(root, 0, out);
			System.out.println("Exporting conditionalSmt2...");
			recursiveConditionalSmt2(root, 0, conditionalSmt2Out);
			System.out.println("Exporting stateNode.json...");
			recursiveStateNode(root, 0, stateNodeOut);
			//
			constraintEdgeMap.clear();
			var2indexMap.clear();
			System.out.println("Exporting consEdgeGraph...");
			// Change by pan: each may has 0-x graphs, so it need to traversal
			// all graphs
			for (ConstraintGraph consGraph : root.getConstraintGraphList().cgl) {
				String varname = consGraph.varname;
				consEdgeGraphOut.println();
				consEdgeGraphOut.println(":"+varname);
				recursiveConsEdgeGraph(root, varname, consEdgeGraphOut);
			}
			// recursiveConsEdgeGraph(root, 0, consEdgeGraphOut);
			System.out.println("Exporting var2indexMap...");
			printVar2indexMap(var2indexMapOut);

			// Output End
			out.println();
			stateNodeOut.println();
			conditionalSmt2Out.println();
			consEdgeGraphOut.println();
			var2indexMapOut.println();

			// Output close
			out.close();
			stateNodeOut.close();
			conditionalSmt2Out.close();
			consEdgeGraphOut.close();
			var2indexMapOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void putVar2indexMap(String s) {
		if (!var2indexMap.containsKey(s)) {
			var2indexMap.put(s, var2indexMap.size());
		}
	}

	// put all vars of node into map
	private static void putVar2indexMap(StateNode node, String varName) {
		Set<String> vars = node.getTypeStateVars();
		for (String var : vars) {
			putVar2indexMap(varName + "." + var);
		}
	}

	private static void recursiveExport(StateNode root, int index, PrintWriter out) {
		// termination
		if (root == null) {
			return;
		}

		// export operation
		if (root.getConditional() != null) {
			// this str has changed!
			out.println(root.index + ":" + root.getConditional().toSmt2String());
		}

		// recursive operation
		recursiveExport(root.getTrueChild(), 2 * index + 1, out);
		recursiveExport(root.getFalseChild(), 2 * index + 2, out);
	}

	private static void recursiveConditionalSmt2(StateNode root, int index, PrintWriter conditionalSmt2Out) {
		// termination
		if (root == null) {
			return;
		}

		// export operation
		if (root.getConditional() != null) {
			conditionalSmt2Out.println(root.index + ":" + root.getConditional().toSmt2String());
			List<CallSite> callSites = root.getCallsites();
			if (callSites != null) {
				for (CallSite cs : callSites) {
					Map<Immediate, Expression> map = cs.getArgumentsMap();
					conditionalSmt2Out.print(root.index + ":#c#" + cs.getRetSym() + "#" + cs.getSignature());
					for (Immediate im : map.keySet()) {
						conditionalSmt2Out.print("#" + map.get(im).toSmt2String());
					}
					conditionalSmt2Out.println();
				}
			}
		}
		if (root.getReturnExpr() != null) {
			conditionalSmt2Out.println(root.index + ":#r#" + root.getReturnExpr().toSmt2String());
		}
		if (root.index == 0) {
			Map<Local, Expression> localExpressionMap = root.getLocalsMap();
			conditionalSmt2Out.print(root.index + ":#p");
			for (Local l : localExpressionMap.keySet()) {
				String expr = localExpressionMap.get(l).toSmt2String();
				if (!expr.contains(" ") && expr.contains("@para")) {
					conditionalSmt2Out.print("#" + localExpressionMap.get(l).toSmt2String());
				}
			}
			conditionalSmt2Out.println();
		}

		// recursive operation
		recursiveConditionalSmt2(root.getTrueChild(), 2 * index + 1, conditionalSmt2Out);
		recursiveConditionalSmt2(root.getFalseChild(), 2 * index + 2, conditionalSmt2Out);
	}

	private static void recursiveStateNode(StateNode root, int index, PrintWriter stateNodeOut) {
		// termination
		if (root == null) {
			return;
		}

		// export stateNode
		List<String> stringList = new ArrayList<>();
		// print conditional
		if (root.getConditional() != null) {
			stringList.add(JSON.toJson("conditional", root.getConditional().toString()));
		} else {
			stringList.add(JSON.toJson("conditional", null));
		}
		// print callSites
		if (root.getCallsites() != null) {
			List<String> callSiteStringList = new ArrayList<>();
			List<CallSite> callSiteList = root.getCallsites();
			for (int i = 0; i < callSiteList.size(); i++) {
				callSiteStringList.add(getCallSite(callSiteList.get(i)));
			}
			stringList.add(JSON.toJsonArray("callsites", callSiteStringList));
		} else {
			stringList.add(JSON.toJson("callsites", null));
		}
		// print returnExpr
		if (root.getReturnExpr() != null) {
			stringList.add(JSON.toJson("returnExp", root.getReturnExpr().toString()));
		} else {
			stringList.add(JSON.toJson("returnExp", null));
		}
		stateNodeOut.println(JSON.toJsonSet("stateNode", stringList));

		// recursive operation
		recursiveStateNode(root.getTrueChild(), 2 * index + 1, stateNodeOut);
		recursiveStateNode(root.getFalseChild(), 2 * index + 2, stateNodeOut);
	}

	private static void recursiveConsEdgeGraph(StateNode root, String varname, PrintWriter consEdgeGraphOut) {
		if (root == null) {
			return;
		}
		// if(passedNode.contains(root.index))
		// return;
		// passedNode.add(root.index);
		TypeGraph typeGraph = root.getTypegraphList().getTypeGraph(varname);
		assert(typeGraph != null);
		ConstraintGraph conGraph = root.getConstraintGraphList().getConstraintGraph(varname);
		putVar2indexMap(root, varname);
		for (TransEdge te : typeGraph.getTransEdges()) {
			consEdgeGraphOut.println(var2indexMap.get( varname + "." +root.index + "." + te.start.print()) + ", "
					+ var2indexMap.get( varname + "." +root.index + "." + te.end.print()) + ", [" + root.index + ", "
					+ root.index + "]");
		}
		for (ConstraintEdge ce : conGraph.constraintedges) {
			consEdgeGraphOut.println(var2indexMap.get( varname + "." +ce.getStartStr()) + ", "
					+ var2indexMap.get( varname + "." +ce.getEndStr()) + ", [" + ce.getStartNode()
					+ ", " + ce.getEndNode() + "]");
		}
		recursiveConsEdgeGraph(root.getTrueChild(), varname, consEdgeGraphOut);
		recursiveConsEdgeGraph(root.getFalseChild(), varname, consEdgeGraphOut);
	}

	private static void recursiveConsEdgeGraph(StateNode root, int index, PrintWriter consEdgeGraphOut) {
		if (root == null) {
			return;
		}
		// consEdgeGraphOut.println("----------"+index+"----------");
		Set<String> Vars = root.getPegIntra_blockVars();
		// push
		for (String s : Vars) {
			putVar2indexMap(index + "." + s);
			if (!constraintEdgeMap.containsKey(s)) {
				constraintEdgeMap.put(s, new Stack<Integer>());
			} else if (constraintEdgeMap.get(s).size() != 0) {
				int start = constraintEdgeMap.get(s).peek();
				consEdgeGraphOut.println(var2indexMap.get(start + "." + s) + ", " + var2indexMap.get(index + "." + s)
						+ ", [" + start + ", " + index + "]");
			}
			constraintEdgeMap.get(s).push(index);
			// System.out.print(index);
		}
		// params rets
		// consEdgeGraphOut.println(root.getPeg_intra_block().getCallSites().size());
		// consEdgeGraphOut.println("----in----");
		consEdgeGraphOut.print(root.getPeg_intra_block().toString(var2indexMap, index));
		// consEdgeGraphOut.println("----out----");
		List<CallSite> callSites = root.getCallsites();
		if (callSites != null) {
			for (CallSite cs : callSites) {
				// consEdgeGraphOut.println("#" + cs.getSignature());
			}
		}
		// recursive operation
		recursiveConsEdgeGraph(root.getTrueChild(), 2 * index + 1, consEdgeGraphOut);
		recursiveConsEdgeGraph(root.getFalseChild(), 2 * index + 2, consEdgeGraphOut);
		// pop
		for (String s : Vars) {
			constraintEdgeMap.get(s).pop();
			// System.out.print(index);
		}
	}

	private static void printVar2indexMap(PrintWriter var2indexMapOut) {
		for (String s : var2indexMap.keySet()) {
			var2indexMapOut.println(var2indexMap.get(s) + " : " + s);
		}
	}

	private static String getCallSite(CallSite callSite) {
		List<String> stringList = new ArrayList<>();
		// print signature
		if (callSite.getSignature() != null) {
			stringList.add(JSON.toJson("signature", callSite.getSignature()));
		} else {
			stringList.add(JSON.toJson("signature", null));
		}
		// print callee
		stringList.add(JSON.toJson("callee", callSite.getCalleeString()));
		// print argumentsMap
		List<String> argStringList = new ArrayList<>();
		for (Immediate im : callSite.getArgumentsMap().keySet()) {
			Expression expr = callSite.getArgumentsMap().get(im);
			if (im != null && im != null) {
				argStringList.add(JSON.toJson(im.toString() + " = " + expr.toString()));
			}
		}
		if (argStringList.size() != 0) {
			stringList.add(JSON.toJsonArray("argumentsMap", argStringList));
		} else {
			stringList.add(JSON.toJson("argumentsMap", null));
		}
		// print retVar
		if (callSite.getRetVar() != null) {
			stringList.add(JSON.toJson("retVar", callSite.getRetVar().toString()));
		} else {
			stringList.add(JSON.toJson("retVar", null));
		}
		return JSON.toJsonSet(stringList);
	}

	/**
	 * print out state information
	 * 

	 * @param root
	 * @param id
	 */
	private static void printOutInfo(StateNode root, int id) {
		// TODO Auto-generated method stub
		if(root == null){
			return;
		}
		System.out.println(id + ": " + root.toString());
		System.out.println("local2local size:"+root.getPeg_intra_block().getLocal2Local().size());
		printOutInfo(root.getFalseChild(), 2 * id);
		printOutInfo(root.getTrueChild(), 2 * id + 1);
	}

}
