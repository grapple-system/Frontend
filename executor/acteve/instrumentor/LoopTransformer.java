/*
  Copyright (c) 2011,2012, 
   Saswat Anand (saswat@gatech.edu)
   Mayur Naik  (naik@cc.gatech.edu)
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met: 
  
  1. Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer. 
  2. Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution. 
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  
  The views and conclusions contained in the software and documentation are those
  of the authors and should not be interpreted as representing official policies, 
  either expressed or implied, of the FreeBSD Project.
*/

package acteve.instrumentor;

import soot.SootMethod;
import soot.Unit;
import soot.Body;
import soot.Local;
import soot.Immediate;
import soot.jimple.TableSwitchStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.IfStmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.GotoStmt;
import soot.jimple.EqExpr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class LoopTransformer {
	
    public static final Jimple jimple = Jimple.v();

    public static final BodyEditor editor = new BodyEditor();

	private LoopTransformer() { }

	public static void transform(SootMethod method) {		
		Body body = method.retrieveActiveBody();		
		editor.newBody(body, method);
		
		LoopNestTree loopNestTree = new LoopNestTree(body);
		for(Loop loop: loopNestTree) {
			System.err.println("\nLoop ===>");
			System.err.println(loop.getLoopStatements());
			Stmt head = loop.getHead();
			System.err.println("Loop head: " + head.toString());
			Stmt backJump = loop.getBackJumpStmt();
			System.err.println("Loop backjump: " + backJump.toString());
			
			for(Stmt exit: loop.getLoopExits()) {
				System.err.println("Loop exit: " + exit.toString());
				System.err.println("Loop targets: " + loop.targetsOfLoopExit(exit).toString());
			}
			
			//infinite loop
			if(loop.loopsForever()) {
				throw new RuntimeException("unexcepted infinite loop!!!");
			}
			
			//get the target of the loop exit
			Stmt target = getTargetOfLoopExits(loop);
			
			//redirect backJump to exit target
			redirectBackJump(loop, target, method);
			
		}

	}


    private static void redirectBackJump(Loop loop, Stmt target, SootMethod method) {
    	Chain stmts = method.getActiveBody().getUnits().getNonPatchingChain();
    	
    	//unroll the loop one more time
//    	while (editor.hasNext()) {
//    		Stmt s = editor.next();
//    		//System.out.println(">> " + s);
//    		
//    	}
    	
    	
    	//redirect backJump
		Stmt backJump = loop.getBackJumpStmt();
		if(backJump instanceof IfStmt) {
			System.err.println("BackJump is IfStmt: " + backJump);
			
		}
		else if(backJump instanceof GotoStmt) {
			System.err.println("BackJump is GotoStmt: " + backJump);
			
		}
		else {
			System.err.println("BackJump is OtherStmt: " + backJump);
			GotoStmt newGoto = jimple.newGotoStmt(target);
//			stmts.insertAfter(newGoto, backJump);
		}
	}

    private static Stmt getTargetOfLoopExits(Loop loop) {
    	assert(!loop.getLoopExits().isEmpty());
    	Stmt header = loop.getHead();
    	Stmt backJump = loop.getBackJumpStmt();
    	//while loop
    	if(header instanceof IfStmt) {
    		assert(loop.getLoopExits().contains(header));
    		assert(loop.targetsOfLoopExit(header).size() == 1);
    		return loop.targetsOfLoopExit(header).iterator().next();
    	}
    	
    	//do-while loop
    	if(backJump instanceof IfStmt) {
    		
    	}
    	
    	return null;
    }
    
//	private static Stmt getTargetOfLoopExits(Loop loop) {
//		//get all the targets of the loop exits
//		assert(!loop.getLoopExits().isEmpty());
//		Set<Stmt> targets = new HashSet<Stmt>();
//		for(Stmt exit: loop.getLoopExits()) {
//			targets.addAll(loop.targetsOfLoopExit(exit));
//		}
//		
//		//filter out GotoStmt
//		for(Iterator<Stmt> it = targets.iterator(); it.hasNext();) {
//			Stmt target = it.next();
//			if(target instanceof GotoStmt) {
//				it.remove();
//				Stmt finalTarget = getGotoTarget((GotoStmt)target);
//				targets.add(finalTarget);
//			}
//		}
//		
//		//return the exit target
//		assert(targets.size() > 0);
//		if(targets.size() > 1) {
//			System.err.println("Multiple exit targets: " + targets);
//		}
//		return targets.iterator().next();
//		
//	}

	private static Stmt getGotoTarget(GotoStmt it) {
		// TODO Auto-generated method stub
		Stmt target = (Stmt) it.getTarget();
		while(target instanceof GotoStmt) {
			target = (Stmt)((GotoStmt) target).getTarget();
		}
		return target;
	}

}
