package edu.zuo.setree.execution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acteve.instrumentor.LoopTransformer;
import acteve.instrumentor.SwitchTransformer;
import acteve.symbolic.integer.BinaryOperator;
import acteve.symbolic.integer.BooleanBinaryOperator;
import acteve.symbolic.integer.DoubleBinaryOperator;
import acteve.symbolic.integer.Expression;
import acteve.symbolic.integer.FloatBinaryOperator;
import acteve.symbolic.integer.IntegerBinaryOperator;
import acteve.symbolic.integer.LongBinaryOperator;
//import edu.zuo.pegraph.PEGGenerator_block;
//import edu.zuo.pegraph.datastructure.PegIntra_block;
import edu.zuo.setree.datastructure.Conditional;
import edu.zuo.setree.datastructure.StateNode;
import edu.zuo.setree.export.Exporter;
import edu.zuo.typestate.TSCGenerator;
import edu.zuo.typestate.datastructure.CallExecutor;
import edu.zuo.typestate.datastructure.ConstraintGraph;
import edu.zuo.typestate.datastructure.ConstraintGraphList;
import edu.zuo.typestate.datastructure.TypeGraph;
import edu.zuo.typestate.datastructure.TypeGraphList;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.Immediate;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.BinopExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.util.Chain;

public class Runner {

	private final StateNode root;
	private String dirPath;
	private String outPath;
	public static ConstraintGraphList constraint_graph_list;
	private final StateNode funcEntry;
	// private final StateNode funcExit;
	private List<Integer> passedNode = new ArrayList<Integer>();
	public static boolean hasInter = false;

	public Runner() {
		this.root = new StateNode();
		this.funcEntry = new StateNode();
		// this.funcExit = new StateNode();
	}

	public void run(Chain<SootClass> classes) {
		// synchronized (this) {
		for (SootClass klass : classes) {
			List<SootMethod> origMethods = klass.getMethods();
			for (SootMethod m : origMethods) {
				if (m.isConcrete()) {
					run(m.retrieveActiveBody());
				}
			}
		}
		// }
	}

	public void run(Body mb) {
		// synchronized (this) {
		System.out.println("\n\n");
		System.out.println("Method: " + mb.getMethod().getSubSignature().toString());
		System.out.println("---------------------------------------------------");
		
		if(mb.getMethod().getSubSignature().contains("isLeaseCheckerStarted")){
			System.out.println("next class");
		}

		if (!hasInterestVar(mb)) {
			hasInter = false;
			return;
		}
//		hasInter = true;

		// init the Directory to save the graph
		dirPath = "E:/Study/zuo_project/pepper_wef/pepper/sootOutput/interMet.txt";
		outPath = "E:/Study/zuo_project/pepper_wef/pepper/sootOutput/" + mb.getMethod().getDeclaringClass().toString()
				+ "." + mb.getMethod().getName();
		String regEx = "[`~!@#%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(outPath);
		outPath = m.replaceAll("").trim();
		File dirFile = new File(dirPath);
		File outFile = new File(outPath);
		try {
			if (!dirFile.exists())
				dirFile.createNewFile();
			if (!outFile.exists())
				outFile.mkdirs();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(dirFile, true)));
			pw.println(mb.getMethod().getSignature());
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// transform the body
		transform(mb.getMethod());

		// confirm that there's no loop at all before executing it
		// symbolically
		confirm_no_loop(mb);

		// add func exit
		// addExitNode(mb);

		// execute the body symbolically
		execute(mb);

		// print the constraint edges
		// constraint_graph.print(dirPath + "/constraint.txt");

		// separate func entry
		separateTS(mb);

		// export the symbolic execution tree
		export(mb);
		// }
		// TSCGenerator.printCalledVar("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/calledVarFile");
		// CallExecutor.dealReturn();
		// CallExecutor.printCallInfo("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/callInfoFile");
		// CallExecutor.printMetReturn("E:/Study/zuo_project/pepper_wef/pepper/intraOutput/methodReturnFile");
	}

	// return true if method has interest var, such fileWriter
	private boolean hasInterestVar(Body mb) {
		BriefBlockGraph cfg = new BriefBlockGraph(mb);
		List<Block> entries = cfg.getHeads();
		TypeGraphList tgl = new TypeGraphList(entries.get(0));
		if (tgl.ltg.size() == 0)
			return false;
		return true;
	}

	// Add by pan. init entry node for typestate checking
	private void separateTS(Body mb) {
		funcEntry.index = 0;
		BriefBlockGraph cfg = new BriefBlockGraph(mb);
		List<Block> entries = cfg.getHeads();
		filterEntries(entries);
		assert(entries.size() == 1);
		Block entry = entries.get(0);
		TypeGraphList first_tgl = new TypeGraphList(entry);
		Map<Local, Expression> localExpressionMap = root.getLocalsMap();
		for (Local l : localExpressionMap.keySet()) {
			funcEntry.putToLocalsMap(l, localExpressionMap.get(l));
		}
		constraint_graph_list = new ConstraintGraphList(entry);
		funcEntry.setTrueChild(root);
		first_tgl.state = 1;
		first_tgl.transAll(mb.getMethod().getSignature(), Integer.toString(entry.getHead().hashCode()));
		first_tgl.state = 0;
		constraint_graph_list.temp2Constraint(mb.getMethod().getSignature(),
				Integer.toString(entry.getHead().hashCode()), funcEntry.index, root.index);
		constraint_graph_list.clearTemp();
		funcEntry.setConstraintGraphList(constraint_graph_list);
		funcEntry.setTypegraphList(new TypeGraphList(entry));
	}

	// Add exit node for each method
	// private void addExitNode(Body mb) {
	// funcExit.index = -2;
	// BriefBlockGraph cfg = new BriefBlockGraph(mb);
	// List<Block> entries = cfg.getHeads();
	// filterEntries(entries);
	// assert(entries.size() == 1);
	// Block entry = entries.get(0);
	// TypeGraphList first_tgl = new TypeGraphList(entry);
	// Map<Local, Expression> localExpressionMap = root.getLocalsMap();
	// for (Local l : localExpressionMap.keySet()) {
	// funcExit.putToLocalsMap(l, localExpressionMap.get(l));
	// }
	// funcExit.setTypegraphList(new TypeGraphList(entry));
	// funcExit.setConstraintGraphList(new ConstraintGraphList(entry));
	// }

	private void export(Body mb) {
		Exporter.run(funcEntry, mb);
	}

	private void confirm_no_loop(Body mb) {
		// TODO Auto-generated method stub
		LoopNestTree loopNestTree = new LoopNestTree(mb);
		List<Loop> need2Remove = new ArrayList<>();
		for (Loop l : loopNestTree) {
			if (l.getHead() instanceof IdentityStmt
					&& ((IdentityStmt) l.getHead()).getRightOp() instanceof CaughtExceptionRef) {
				need2Remove.add(l);
			}
		}
		loopNestTree.removeAll(need2Remove);
		if (!loopNestTree.isEmpty()) {
			throw new RuntimeException("Unexpected loops existing!!!");
		}
	}

	private void execute(Body mb) {
		BriefBlockGraph cfg = new BriefBlockGraph(mb);
		System.out.println("\n\nCFG before executing ==>>");
		System.out.println(cfg.toString());

		// List<Block> entries = cfg.getHeads();
		List<Block> entries = new ArrayList<Block>(cfg.getHeads());
		filterEntries(entries);

		assert(entries.size() == 1);
		Block entry = entries.get(0);
		root.index = 1;
		passedNode = new ArrayList<Integer>();
		passedNode.add(entry.getIndexInMethod());
		// recursive construct stateNode tree
		traverseCFG(entry, root);
	}

	private void transform(SootMethod method) {
		BriefBlockGraph cfg = new BriefBlockGraph(method.getActiveBody());
		System.out.println("\nCFG before transforming ==>>");
		System.out.println(cfg.toString());

		// switch transform: transform lookupswitch and tableswitch into if
		SwitchTransformer.transform(method);

		// loop transform: unroll the loop twice
		LoopTransformer.transform(method);
	}

	/**
	 * filter out the entry blocks which are catching exceptions
	 * 
	 * @param entries
	 */
	private void filterEntries(List<Block> entries) {
		// TODO Auto-generated method stub
		for (Iterator<Block> it = entries.listIterator(); it.hasNext();) {
			Block b = it.next();
			try {
				if (b.getHead() instanceof IdentityStmt
						&& ((IdentityStmt) b.getHead()).getRightOp() instanceof CaughtExceptionRef) {
					it.remove();
				}
			} catch (UnsupportedOperationException e) {
				System.out.println("UnsupportedOperationException");
			}
		}
		// System.out.println(entries.size());
		assert(entries.size() == 1);
	}

	private void traverseCFG(Block block, StateNode node) {
		// if(passedNode.contains(block.getIndexInMethod()))
		// return;
		// passedNode.add(block.getIndexInMethod());
		constraint_graph_list = new ConstraintGraphList(block);
		// propagate the execution symbolically
		operate(block, node);

		// branching
		List<Block> succs = block.getSuccs();
		if (succs.size() == 2) {
			// branch
			assert(block.getTail() instanceof IfStmt);

			// //set conditional
			// IfStmt ifstmt = (IfStmt) block.getTail();
			// setConditional(ifstmt, node);

			StateNode nTrue = new StateNode(node);
			nTrue.index = 2 * node.index + 1;
			node.setTrueChild(nTrue);
			constraint_graph_list.temp2Constraint(block.getTail().hashCode(), succs.get(1).getHead().hashCode(),
					node.index, nTrue.index);

			StateNode nFalse = new StateNode(node);
			nFalse.index = 2 * node.index;
			node.setFalseChild(nFalse);
			constraint_graph_list.temp2Constraint(block.getTail().hashCode(), succs.get(0).getHead().hashCode(),
					node.index, nFalse.index);
			constraint_graph_list.clearTemp();
			node.setConstraintGraphList(constraint_graph_list);

			traverseCFG(succs.get(1), nTrue);
			traverseCFG(succs.get(0), nFalse);
		} else if (succs.size() == 1) {
			// fall-through

			// warning: call_hashcode changed to temp.callhash: +/-hashcode
			if (TSCGenerator.lastCall) {
				constraint_graph_list.temp2Constraint(-block.getTail().hashCode(), succs.get(0).getHead().hashCode(),
						node.index, node.index);
			} else
				constraint_graph_list.temp2Constraint(block.getTail().hashCode(), succs.get(0).getHead().hashCode(),
						node.index, node.index);
			constraint_graph_list.clearTemp();
			node.addConstraintGraphList(constraint_graph_list);
			traverseCFG(succs.get(0), node);
		} else if (succs.size() == 0) {
			constraint_graph_list.temp2Constraint(Integer.toString(block.getTail().hashCode()),
					"-" + Integer.toString(block.getTail().hashCode()), node.index, -2);
			constraint_graph_list.clearTemp();
			CallExecutor.addRet(block.getBody().getMethod().getSignature(), constraint_graph_list);
			node.setConstraintGraphList(new ConstraintGraphList(block));
			// node.setTrueChild(funcExit);
			// end
		} else if (succs.size() > 2) {
			node.setConstraintGraphList(constraint_graph_list);
			// error
			System.err.println("unexpected case!!!");
		}
	}


	private void operate(Block block, StateNode node) {
		// ---------------------------------------------------------
		// generate symbolic execution graph (SEG)
		// synchronized (this) {
		Propagator p = new Propagator(node);
		for (Iterator<Unit> it = block.iterator(); it.hasNext();) {
			Stmt stmt = (Stmt) it.next();
			// //for debugging
			// System.out.println(stmt);
			stmt.apply(p);
		}

		TypeGraphList tgl = new TypeGraphList(block);
		TSCGenerator tscgenerator = new TSCGenerator(block, tgl);
		// warning: par add node index!
		tscgenerator.process(outPath, node.index);
		tscgenerator.print(outPath);
		node.addTypegraphList(tgl);
		// don't simplify
		// node.getTypegraphList().simplifyGraph();
		// node.setConStr(p.constraintstr);
		// }

	}

	// /** get the unique entry block starting with Parameter or This rather
	// than CaughtException
	// * @param cfg
	// * @return
	// */
	// private Block getEntryBlock(BriefBlockGraph cfg) {
	// // TODO Auto-generated method stub
	// List<Block> entries = cfg.getHeads();
	// if(entries.size() == 1){
	// return entries.get(0);
	// }
	//
	// for(Block b: entries){
	// if(b.getHead() instanceof IdentityStmt &&
	// ((IdentityStmt)b.getHead()).getRightOp() instanceof CaughtExceptionRef){
	// continue;
	// }
	// return b;
	// }
	// return null;
	// }

}
