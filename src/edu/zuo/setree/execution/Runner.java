package edu.zuo.setree.execution;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import acteve.instrumentor.LoopTransformer;
import acteve.instrumentor.SwitchTransformer;
import edu.zuo.setree.datastructure.StateNode;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ConditionExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.util.Chain;

public class Runner {
	
	public void run(Chain<SootClass> classes){
		for (SootClass klass : classes) {
			List<SootMethod> origMethods = klass.getMethods();
			for (SootMethod m : origMethods) {
				if (m.isConcrete()) {
					run(m.retrieveActiveBody());
				}
			}
		}
	}
	
	
	public void run(Body mb){
		System.out.println("\n\n");
		System.out.println("Method: " + mb.getMethod().getSubSignature().toString());
		System.out.println("---------------------------------------------------");
		
		//transform the body
		transform(mb.getMethod());
		
		//confirm that there's no loop at all before executing it symbolically
		confirm_no_loop(mb);
		
		//execute the body symbolically
		execute(mb);
		
	}
	
	private void confirm_no_loop(Body mb) {
		// TODO Auto-generated method stub
		LoopNestTree loopNestTree = new LoopNestTree(mb);
		
		if(!loopNestTree.isEmpty()) {
			throw new RuntimeException("Unexpected loops existing!!!");
		}
	}


	private void execute(Body mb) {
		BriefBlockGraph cfg = new BriefBlockGraph(mb);
		System.out.println("\n\nCFG before executing ==>>");
		System.out.println(cfg.toString());
		
		List<Block> entries = cfg.getHeads();
		filterEntries(entries);
		
		for(Block entry: entries){
			executeSingleEntry(entry);
		}
	}


	private void executeSingleEntry(Block entry) {
		StateNode root = new StateNode();
		traverseCFG(entry, root);
		
		//for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 1);
		System.out.println("\n");
	}
	
	
	
	private void transform(SootMethod method) {
		BriefBlockGraph cfg = new BriefBlockGraph(method.getActiveBody());
		System.out.println("\nCFG before transforming ==>>");
		System.out.println(cfg.toString());
		
		//switch transform: transform lookupswitch and tableswitch into if
		SwitchTransformer.transform(method);
		
		//loop transform: unroll the loop twice
		LoopTransformer.transform(method);
	}

	
	/** print out state information
	 * @param root
	 * @param id
	 */
	private void printOutInfo(StateNode root, int id) {
		// TODO Auto-generated method stub
		if(root == null){
			return;
		}
		System.out.println(id + ": " + root.getState().toString());
		printOutInfo(root.getTrueChild(), 2 * id);
		printOutInfo(root.getFalseChild(), 2 * id + 1);
	}

	
	/** filter out the entry blocks which are catching exceptions
	 * @param entries
	 */
	private void filterEntries(List<Block> entries) {
		// TODO Auto-generated method stub
		for(Iterator<Block> it = entries.listIterator(); it.hasNext();){
			Block b = it.next();
			if(b.getHead() instanceof IdentityStmt && ((IdentityStmt)b.getHead()).getRightOp() instanceof CaughtExceptionRef){
				it.remove();
			}
		}
//		System.out.println(entries.size());
		assert(entries.size() == 1);
	}

	
	private void traverseCFG(Block block, StateNode node) {
		//propagate the execution symbolically
		operate(block, node);
		
		//branching
		List<Block> succs = block.getSuccs();
		if(succs.size() == 2){
			//branch
			assert(block.getTail() instanceof IfStmt);
			
			//set conditional
			IfStmt ifstmt = (IfStmt) block.getTail();
//			ifstmt.getCondition();
			setConditional(ifstmt, node);
			
			
			StateNode nTrue = new StateNode(node.getState());
			node.setTrueChild(nTrue);
			traverseCFG(succs.get(0), nTrue);
			
			StateNode nFalse = new StateNode(node.getState());
			node.setFalseChild(nFalse);
			traverseCFG(succs.get(1), nFalse);
		}
		else if(succs.size() == 1){
			//fall-through
			traverseCFG(succs.get(0), node);
		}
		else if(succs.size() == 0){
			//end

		}
		else if(succs.size() > 2){
			//error
			System.err.println("unexpected case!!!");
		}
	}

	
	private void setConditional(IfStmt ifstmt, StateNode node) {
		// TODO Auto-generated method stub
		ConditionExpr conditionExpr = (ConditionExpr) ifstmt.getCondition();
		
		System.out.println("Condition expression ==>>");
		System.out.println(conditionExpr.toString());
		System.out.println();
		
	}


	private void operate(Block block, StateNode node) {
		// TODO Auto-generated method stub
		Propagator p = new Propagator(node.getState().getLocalsMap());
		for(Iterator<Unit> it = block.iterator(); it.hasNext();){
			Stmt stmt = (Stmt) it.next();
//			//for debugging
//			System.out.println(stmt);
			stmt.apply(p);
		}
	}

//	/** get the unique entry block starting with Parameter or This rather than CaughtException
//	 * @param cfg
//	 * @return
//	 */
//	private Block getEntryBlock(BriefBlockGraph cfg) {
//		// TODO Auto-generated method stub
//		List<Block> entries = cfg.getHeads();
//		if(entries.size() == 1){
//			return entries.get(0);
//		}
//		
//		for(Block b: entries){
//			if(b.getHead() instanceof IdentityStmt && ((IdentityStmt)b.getHead()).getRightOp() instanceof CaughtExceptionRef){
//				continue;
//			}
//			return b;
//		}
//		return null;
//	}
	
	

}
