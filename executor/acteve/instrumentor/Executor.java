package acteve.instrumentor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.util.Chain;

public class Executor extends AbstractStmtSwitch {
	
	public void execute(Chain<SootClass> classes){
		for (SootClass klass : classes) {
			List<SootMethod> origMethods = klass.getMethods();
			for (SootMethod m : origMethods) {
				if (!m.isConcrete())
					continue;

				execute(m);
			}
		}
	}
	
	private void execute(SootMethod method){
		BriefBlockGraph cfg = new BriefBlockGraph(method.getActiveBody());
		System.out.println(cfg.toString());
		
//		Block entryBlock = getEntryBlock(cfg);
		
//		Set<Block> set_visited = new HashSet<Block>();
		List<Block> entries = cfg.getHeads();
		filterEntries(entries);
		
		for(Block entry: entries){
			StateNode root = new StateNode();
			traverseCFG(entry, root);
		}
		
	}

	
	private void filterEntries(List<Block> entries) {
		// TODO Auto-generated method stub
		for(Iterator<Block> it = entries.listIterator(); it.hasNext();){
			Block b = it.next();
			if(b.getHead() instanceof IdentityStmt && ((IdentityStmt)b.getHead()).getRightOp() instanceof CaughtExceptionRef){
				it.remove();
			}
		}
		System.out.println(entries.size());
	}

	private void traverseCFG(Block entryBlock, StateNode node) {
		// TODO Auto-generated method stub
		operate(entryBlock, node);
		
//		Unit stmt = entryBlock.getTail();
//		if(stmt instanceof IfStmt){
//			((IfStmt) stmt).getTarget();
//		}
		
		List<Block> succs = entryBlock.getSuccs();
		if(succs.size() == 2){//branch
			StateNode nTrue = new StateNode(node.getState());
			node.setTrueChild(nTrue);
			traverseCFG(succs.get(0), nTrue);
			
			StateNode nFalse = new StateNode(node.getState());
			node.setFalseChild(nFalse);
			traverseCFG(succs.get(1), nFalse);
		}
		else if(succs.size() == 1){//fall-through
			traverseCFG(succs.get(0), node);
		}
		else if(succs.size() == 0){//end

		}
		else if(succs.size() > 2){//error
			
		}
	}

	private void operate(Block block, StateNode node) {
		// TODO Auto-generated method stub
		Propagate p = new Propagate(node.getState().getLocalsMap());
		for(Iterator<Unit> it = block.iterator(); it.hasNext();){
			Stmt stmt = (Stmt) it.next();
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
