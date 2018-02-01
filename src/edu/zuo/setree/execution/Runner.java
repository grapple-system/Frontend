package edu.zuo.setree.execution;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acteve.instrumentor.LoopTransformer;
import acteve.instrumentor.SwitchTransformer;
import acteve.symbolic.integer.BinaryOperator;
import acteve.symbolic.integer.BooleanBinaryOperator;
import acteve.symbolic.integer.DoubleBinaryOperator;
import acteve.symbolic.integer.Expression;
import acteve.symbolic.integer.FloatBinaryOperator;
import acteve.symbolic.integer.IntegerBinaryOperator;
import acteve.symbolic.integer.LongBinaryOperator;
import edu.zuo.setree.datastructure.Conditional;
import edu.zuo.setree.datastructure.StateNode;
import edu.zuo.setree.export.Exporter;
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
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.util.Chain;

public class Runner {
	
	private final StateNode root;
	
	public Runner() {
		this.root = new StateNode();
	}
	
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
		
		//export the symbolic execution tree
		export(mb);
	}
	
	private void export(Body mb) {
		Exporter.run(root, mb);
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
		
		assert(entries.size() == 1);
		Block entry = entries.get(0);
		traverseCFG(entry, root);
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

	
	/**set the conditional constraint in StateNode 
	 * @param ifstmt
	 * @param node
	 */
	private void setConditional(IfStmt ifstmt, StateNode node) {
		// TODO Auto-generated method stub
		ConditionExpr conditionExpr = (ConditionExpr) ifstmt.getCondition();
		
		System.out.println("Condition expression ==>>");
		System.out.println(conditionExpr.toString());
		System.out.println();
		
		Conditional conditional = getConditional(conditionExpr, node.getState().getLocalsMap()); 
		node.setConditional(conditional);
	}
	


	/**get the conditional expression
	 * @param conditionExpr
	 * @param localsMap
	 * @return
	 */
	private Conditional getConditional(ConditionExpr conditionExpr, Map<Local, Expression> localsMap) {
		Immediate op1 = (Immediate) conditionExpr.getOp1();
        Immediate op2 = (Immediate) conditionExpr.getOp2();
		
		assert((op1.getType() instanceof PrimType) && (op2.getType() instanceof PrimType));

		//TODO: deal with non-primitive constant
		Expression symOp1 = op1 instanceof Constant ? Propagator.getConstant((Constant) op1) : localsMap.get((Local) op1);
		Expression symOp2 = op2 instanceof Constant ? Propagator.getConstant((Constant) op2) : localsMap.get((Local) op2);
		
		BinaryOperator binop = Propagator.getBinaryOperator(conditionExpr);
		Expression constraint = binop.apply(symOp1, symOp2);
		
		return new Conditional(constraint);
	}

//	public static BinaryOperator getConditionOperator(ConditionExpr conditionExpr) {
//		// TODO Auto-generated method stub
//		String binExprSymbol = conditionExpr.getSymbol().trim();
//		
//		Type binType = conditionExpr.getType();
////		assert(binType == binExpr.getOp2().getType());
//		
//		if(binType instanceof IntType || binType instanceof ShortType || binType instanceof CharType || binType instanceof ByteType){
//			return new IntegerBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof LongType){
//			return new LongBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof FloatType){
//			return new FloatBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof DoubleType){
//			return new DoubleBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof BooleanType){
//			return new BooleanBinaryOperator(binExprSymbol);
//		}
//		else{
//			System.err.println("wrong type: " + binType.toString());
//		}
//		
//		return null;
//	}

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
