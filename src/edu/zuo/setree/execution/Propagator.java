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

package edu.zuo.setree.execution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import acteve.symbolic.A3TInstrumented;
import acteve.symbolic.A3TNative;
import acteve.symbolic.integer.BinaryOperator;
import acteve.symbolic.integer.BooleanBinaryOperator;
import acteve.symbolic.integer.DoubleBinaryOperator;
import acteve.symbolic.integer.DoubleConstant;
import acteve.symbolic.integer.DoubleUnaryOperator;
import acteve.symbolic.integer.Expression;
import acteve.symbolic.integer.FloatBinaryOperator;
import acteve.symbolic.integer.FloatConstant;
import acteve.symbolic.integer.FloatUnaryOperator;
import acteve.symbolic.integer.IntegerBinaryOperator;
import acteve.symbolic.integer.IntegerConstant;
import acteve.symbolic.integer.IntegerUnaryOperator;
import acteve.symbolic.integer.LongBinaryOperator;
import acteve.symbolic.integer.LongConstant;
import acteve.symbolic.integer.LongUnaryOperator;
import acteve.symbolic.integer.RefConstant;
import acteve.symbolic.integer.SymbolicDouble;
import acteve.symbolic.integer.SymbolicFloat;
import acteve.symbolic.integer.SymbolicInteger;
import acteve.symbolic.integer.SymbolicLong;
import acteve.symbolic.integer.SymbolicRef;
import acteve.symbolic.integer.UnaryOperator;
import acteve.symbolic.integer.operation.NEGATION;
import acteve.symbolic.integer.operation.Operations;
import edu.zuo.setree.datastructure.CallSite;
import edu.zuo.setree.datastructure.StateNode;
import soot.Scene;
import soot.ShortType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.Immediate;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PrimType;
import soot.RefType;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.Unit;
import soot.VoidType;
import soot.ArrayType;
import soot.IntType;
import soot.IntegerType;
import soot.SootMethodRef;
import soot.FastHierarchy;
import soot.FloatType;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LengthExpr;
import soot.jimple.NegExpr;
import soot.jimple.EqExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.options.Options;
import soot.util.Chain;
import soot.PatchingChain;
import soot.jimple.ConditionExpr;
import soot.jimple.IfStmt;
import soot.jimple.NullConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.UnopExpr;
import soot.jimple.LookupSwitchStmt;
import soot.tagkit.SourceFileTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.BytecodeOffsetTag;
import soot.toolkits.graph.PostDominatorAnalysis;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;


public class Propagator extends AbstractStmtSwitch {
	
//	private Map<Local, Expression> localsMap;
	
	private final StateNode stateNode;
	

	public Propagator(StateNode sNode){
		this.stateNode = sNode;
//		this.localsMap = sNode.getState().getLocalsMap();
	}
	

//    private void printOutLocalsMap() {
//		System.out.println(localsMap.size());
//		for(Local local: localsMap.keySet()){
//			System.out.println(local.toString() + " -- " + localsMap.get(local).toString());
//		}
//		System.out.println();
//	}


	public void caseInvokeStmt(InvokeStmt is){
		InvokeExpr ie = is.getInvokeExpr();
		handleInvokeExpr(ie, null);
	}
	
	public void caseAssignStmt(AssignStmt as)
	{
		Value rightOp = as.getRightOp();
		Value leftOp = as.getLeftOp();

		if (rightOp instanceof BinopExpr) {
			handleBinopStmt((Local) leftOp, (BinopExpr) rightOp);
		}
		if (rightOp instanceof NegExpr) {
			handleNegStmt((Local) leftOp, (NegExpr) rightOp);
		}
		else if (leftOp instanceof FieldRef) {
			handleStoreStmt((FieldRef) leftOp, (Immediate) rightOp);
		}
		else if (rightOp instanceof FieldRef) {
			handleLoadStmt((Local) leftOp, (FieldRef) rightOp);
		}
		else if (leftOp instanceof ArrayRef) {
			handleArrayStoreStmt((ArrayRef) leftOp, (Immediate) rightOp);
		}
		else if (rightOp instanceof ArrayRef) {
			handleArrayLoadStmt((Local) leftOp, (ArrayRef) rightOp);
		}
		else if (rightOp instanceof LengthExpr) {
			handleArrayLengthStmt((Local) leftOp, (LengthExpr) rightOp);
		}
		else if (rightOp instanceof InstanceOfExpr) {
			handleInstanceOfStmt((Local) leftOp, (InstanceOfExpr) rightOp);
		}
		else if (rightOp instanceof CastExpr) {
			handleCastExpr((Local) leftOp, (CastExpr) rightOp);
		}
		else if (rightOp instanceof NewExpr) {
			handleNewStmt((Local) leftOp, (NewExpr) rightOp);
		}
		else if (rightOp instanceof NewArrayExpr) {
			handleNewArrayStmt((Local) leftOp, (NewArrayExpr) rightOp);
		}
		else if (rightOp instanceof NewMultiArrayExpr) {
			handleNewMultiArrayStmt((Local) leftOp, (NewMultiArrayExpr) rightOp);
		}
		else if (rightOp instanceof Immediate && leftOp instanceof Local) {
			handleSimpleAssignStmt((Local) leftOp, (Immediate) rightOp);
		}
		else if(rightOp instanceof InvokeExpr) {
			Local retValue = (Local) leftOp;
			handleInvokeExpr((InvokeExpr) rightOp, retValue);
		}
	}

	private void handleInvokeExpr(InvokeExpr ie, Local retValue) {
		CallSite callSite = new CallSite();
		
		//signature
		SootMethod calleeMethod = ie.getMethod();
		callSite.setSignature(calleeMethod.getSignature());
		
		//args: callee and arguments
		if(ie instanceof InstanceInvokeExpr) {
			Immediate base = (Immediate) ((InstanceInvokeExpr) ie).getBase();
			assert(!(base instanceof Constant));
			Expression expr = getMap((Local) base);
			callSite.setCallee(base, expr);
		}
		for(Iterator it = ie.getArgs().iterator(); it.hasNext();) {
			Immediate arg = (Immediate) it.next();
			Expression expr = arg instanceof Constant ? getConstant((Constant) arg) : getMap((Local) arg);
			callSite.putArgsMap(arg, expr);
		}
		
		if(retValue != null) {
			callSite.setRetVar(retValue);

			//add new symbolic local to localsMap
			Expression retSym = getRetSym(ie, retValue);
			putMap(retValue, retSym);
		}
		
		//add callsite to stateNode
		this.stateNode.addCallSite(callSite);
		
	}

	private Expression getRetSym(InvokeExpr ie, Local retValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public void caseIdentityStmt(IdentityStmt is)
	{
		Local lhs = (Local) is.getLeftOp();
		Value rhs = ((DefinitionStmt) is).getRightOp();
		
		if (rhs instanceof ParameterRef) {
			Expression sym_para = null;
			int index = ((ParameterRef) rhs).getIndex();
			String rhs_name = "@para" + index;
			
			if(rhs.getType() instanceof PrimType){
				//split the cases
				if(rhs.getType() instanceof BooleanType){
					sym_para = new SymbolicInteger(0, rhs_name, index);
				}
				else if(rhs.getType() instanceof CharType){
					sym_para = new SymbolicInteger(1, rhs_name, index);
				}
				else if(rhs.getType() instanceof ShortType){
					sym_para = new SymbolicInteger(2, rhs_name, index);
				}
				else if(rhs.getType() instanceof ByteType){
					sym_para = new SymbolicInteger(3, rhs_name, index);
				}
				else if(rhs.getType() instanceof IntType){
					sym_para = new SymbolicInteger(4, rhs_name, index);
				}
				else if(rhs.getType() instanceof LongType){
					sym_para = new SymbolicLong(rhs_name, index);
				}
				else if(rhs.getType() instanceof FloatType){
					sym_para = new SymbolicFloat(rhs_name, index);
				}
				else if(rhs.getType() instanceof DoubleType){
					sym_para = new SymbolicDouble(rhs_name, index);
				}
				putMap(lhs, sym_para);
			}
			else if(rhs.getType() instanceof RefLikeType){
				//TODO: for array or object
				sym_para = new SymbolicRef(rhs_name, null);
				putMap(lhs, sym_para);
			}
			else{
				System.err.println("unexpected type!!!");
			}
			
		}
		else if(rhs instanceof ThisRef){
			//TODO: for callee
			String rhs_name = "@this";
			Expression sym = new SymbolicRef(rhs_name, null);
			putMap(lhs, sym);
		}
		else{
			assert(rhs instanceof CaughtExceptionRef);
		}
		
	}

	private void putMap(Local var, Expression sym_expr) {
		stateNode.putToLocalsMap(var, sym_expr);
	}
	
	private Expression getMap(Local var) {
		return stateNode.getFromLocalsMap(var);
	}
	

	void handleBinopStmt(Local leftOp, BinopExpr binExpr)
	{
		Immediate op1 = (Immediate) binExpr.getOp1();
        Immediate op2 = (Immediate) binExpr.getOp2();
		
        //the operands of binary operation (except for object equal) must be primitive type!!!
		if(!(op1.getType() instanceof PrimType) || !(op2.getType() instanceof PrimType)) {
			return;
		}
		//TODO: deal with object equality later

		Expression symOp1 = op1 instanceof Constant ? getConstant((Constant) op1) : getMap((Local) op1);
		Expression symOp2 = op2 instanceof Constant ? getConstant((Constant) op2) : getMap((Local) op2);
		
		Expression rightOp_sym = getBinaryExpression(binExpr, symOp1, symOp2);
		putMap(leftOp, rightOp_sym);
	}

	
	public static Expression getBinaryExpression(BinopExpr binExpr, Expression symOp1, Expression symOp2) {
		String binExprSymbol = binExpr.getSymbol().trim();
		
		Type binType = binExpr.getOp1().getType();
		
		if(binType instanceof IntType || binType instanceof ShortType || binType instanceof CharType || binType instanceof ByteType){
			return getIntegerBinaryOperator(binExprSymbol).apply(symOp1, symOp2);
		}
		else if(binType instanceof LongType){
			return getLongBinaryOperator(binExprSymbol).apply(symOp1, symOp2);
		}
		else if(binType instanceof FloatType){
			return getFloatBinaryOperator(binExprSymbol).apply(symOp1, symOp2);
		}
		else if(binType instanceof DoubleType){
			return getDoubleBinaryOperator(binExprSymbol).apply(symOp1, symOp2);
		}
		else if(binType instanceof BooleanType){
			return getBooleanBinaryExpression(binExprSymbol, symOp1, symOp2);
		}
		else{
			System.err.println("wrong type: " + binType.toString());
		}
		
		return null;
	}


//	public static BinaryOperator getBinaryOperator(BinopExpr binExpr) {
//		String binExprSymbol = binExpr.getSymbol().trim();
//		
//		Type binType = binExpr.getOp1().getType();
////		assert(binType == binExpr.getOp2().getType());
//		
//		if(binType instanceof IntType || binType instanceof ShortType || binType instanceof CharType || binType instanceof ByteType){
//			return getIntegerBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof LongType){
//			return getLongBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof FloatType){
//			return getFloatBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof DoubleType){
//			return getDoubleBinaryOperator(binExprSymbol);
//		}
//		else if(binType instanceof BooleanType){
//			return getBooleanBinaryOperator(binExprSymbol);
//		}
//		else{
//			System.err.println("wrong type: " + binType.toString());
//		}
//		
//		return null;
//	}
	

	private static Expression getBooleanBinaryExpression(String binExprSymbol, Expression symOp1, Expression symOp2) {
		switch (binExprSymbol) {
		//equality
		case "==":
			return _eq(symOp1, symOp2);
		case "!=":
			return _ne(symOp1, symOp2);
			
		default:
			throw new RuntimeException("wrong boolean binary operator!!!");	
		}
	}

	private static Expression _ne(Expression symOp1, Expression symOp2) {
		if(symOp2 instanceof IntegerConstant){
			int seed = ((IntegerConstant) symOp2).seed;
			if(seed == 1)
				return Operations.v.negation().apply(symOp1);
			else if(seed == 0)
				return symOp1;
			else
				assert false;
		}
		throw new RuntimeException("Take care");
	}

	private static Expression _eq(Expression symOp1, Expression symOp2) {
		if(symOp2 instanceof IntegerConstant){
			int seed = ((IntegerConstant) symOp2).seed;
			if(seed == 1)
				return symOp1;
			else if(seed == 0)
				return Operations.v.negation().apply(symOp1);
			else
				assert false;
		}
		throw new RuntimeException("Take care");
	}


	private static BinaryOperator getDoubleBinaryOperator(String binExprSymbol) {
		switch (binExprSymbol) {
		//cmp	
		case Jimple.CMPL:
			return Operations.v.dcmpl();
		case Jimple.CMPG:
			return Operations.v.dcmpg();
		
		//algebraic	
		case "+":
			return Operations.v.dadd();
		case "-":
			return Operations.v.dsub();
		case "*":
			return Operations.v.dmul();
		case "/":
			return Operations.v.ddiv();
		case "%":
			return Operations.v.drem();
			
		default:
			throw new RuntimeException("wrong double binary operator!!!");
		}
	}


	private static BinaryOperator getLongBinaryOperator(String binExprSymbol) {
		switch (binExprSymbol) {
		//cmp	
		case Jimple.CMP:
			return Operations.v.lcmp();
			
		//algebraic
		case "+":
			return Operations.v.ladd();
		case "-":
			return Operations.v.lsub();
		case "*":
			return Operations.v.lmul();
		case "/":
			return Operations.v.ldiv();
		case "%":
			return Operations.v.lrem();
		
		//bitwise	
		case "|":
			return Operations.v.lor();
		case "&":
			return Operations.v.land();
		case "^":
			return Operations.v.lxor();
		case ">>":
			return Operations.v.lshl();
		case "<<":
			return Operations.v.lshr();
		case ">>>":
			return Operations.v.lushr();
			
		default:
			throw new RuntimeException("wrong long binary operator!!!");
		}
	}


	private static BinaryOperator getFloatBinaryOperator(String binExprSymbol) {
		switch (binExprSymbol) {
		//equality
		case "==":
			return Operations.v.req();
		case "!=":
			return Operations.v.rne();
		//cmp	
		case Jimple.CMPL:
			return Operations.v.fcmpl();
		case Jimple.CMPG:
			return Operations.v.fcmpg();
		
		//algebraic	
		case "+":
			return Operations.v.fadd();
		case "-":
			return Operations.v.fsub();
		case "*":
			return Operations.v.fmul();
		case "/":
			return Operations.v.fdiv();
		case "%":
			return Operations.v.frem();
			
		default:
			throw new RuntimeException("wrong float binary operator!!!");
		}
	}


	private static BinaryOperator getIntegerBinaryOperator(String binExprSymbol) {
		switch (binExprSymbol) {
		//equality
		case "==":
			return Operations.v.icmpeq();
		case "!=":
			return Operations.v.icmpne();
		case ">=":
			return Operations.v.icmpge();
		case ">":
			return Operations.v.icmpgt();
		case "<=":
			return Operations.v.icmple();
		case "<":
			return Operations.v.icmplt();
			
		//algebraic
		case "+":
			return Operations.v.iadd();
		case "-":
			return Operations.v.isub();
		case "*":
			return Operations.v.imul();
		case "/":
			return Operations.v.idiv();
		case "%":
			return Operations.v.irem();
		
		//bitwise	
		case "|":
			return Operations.v.ior();
		case "&":
			return Operations.v.iand();
		case "^":
			return Operations.v.ixor();
		case ">>":
			return Operations.v.ishl();
		case "<<":
			return Operations.v.ishr();
		case ">>>":
			return Operations.v.iushr();
			
		default:
			throw new RuntimeException("wrong integer binary operator!!!");
		}
	}


	public static Expression getConstant(Constant operand){
		Type constType = operand.getType();
		
		if(constType instanceof IntegerType){
			return IntegerConstant.get(((soot.jimple.IntConstant)operand).value);
		}
		else if(constType instanceof LongType){
			return LongConstant.get(((soot.jimple.LongConstant)operand).value);
		}
		else if(constType instanceof FloatType){
			return FloatConstant.get(((soot.jimple.FloatConstant)operand).value);
		}
		else if(constType instanceof DoubleType){
			return DoubleConstant.get(((soot.jimple.DoubleConstant)operand).value);
		}
		else if(constType instanceof RefLikeType) {
			return RefConstant.get(operand);
		}
		else{
			System.err.println("wrong type: " + constType.toString());
		}
		return null;
	}
	

	void handleNegStmt(Local leftOp, NegExpr negExpr)
	{
		Immediate op = (Immediate) negExpr.getOp();
		
		assert(op.getType() instanceof PrimType);
		if(!(op.getType() instanceof PrimType)){
			return;
		}
		
		Expression symOp = op instanceof Constant ? getConstant((Constant) op) : getMap((Local) op);
		UnaryOperator unaop = getNegOperator(negExpr);
		Expression rightOp_sym = unaop.apply(symOp);
		putMap(leftOp, rightOp_sym);
	}
	
	private UnaryOperator getNegOperator(UnopExpr unaryExpr) {
		Type unaryType = unaryExpr.getOp().getType();
		
		if(unaryType instanceof IntType || unaryType instanceof ShortType || unaryType instanceof CharType || unaryType instanceof ByteType){
			return Operations.v.ineg();
		}
		else if(unaryType instanceof LongType){
			return Operations.v.lneg();
		}
		else if(unaryType instanceof FloatType){
			return Operations.v.fneg();
		}
		else if(unaryType instanceof DoubleType){
			return Operations.v.dneg();
		}
		else if(unaryType instanceof BooleanType){
			return Operations.v.negation();
		}
		else{
			System.err.println("wrong type: " + unaryType.toString());
		}
		
		return null;
	}

	void handleSimpleAssignStmt(Local leftOp, Immediate rightOp)
	{
		Expression exp = rightOp instanceof Constant ? getConstant((Constant) rightOp) : getMap((Local) rightOp);
		putMap(leftOp, exp);
	}

	void handleStoreStmt(FieldRef leftOp, Immediate rightOp) 
	{
	}
	
	void handleLoadStmt(Local leftOp, FieldRef rightOp) 
	{
	}

	void handleNewStmt(Local leftOp, NewExpr rightOp)
	{
	}

	void handleNewArrayStmt(Local leftOp, NewArrayExpr rightOp)
	{
	}

	void handleNewMultiArrayStmt(Local leftOp, NewMultiArrayExpr rightOp)
	{
	}

	public void caseReturnStmt(ReturnStmt rs)
	{
//		Immediate retValue = (Immediate) rs.getOp();
	}

	public void caseReturnVoidStmt(ReturnStmt rs)
	{	
	}

	void handleCastExpr(Local leftOp, CastExpr rightOp)
	{
		Type type = rightOp.getCastType();
		if(type instanceof PrimType){
			Immediate op = (Immediate) rightOp.getOp();
			Expression exp = op instanceof Constant ? getConstant((Constant) op) : getMap((Local) op);
			putMap(leftOp, exp);
		}
		else {
			//TODO
		}
	}

	void handleArrayLoadStmt(Local leftOp, ArrayRef rightOp)
	{
	}

	void handleArrayLengthStmt(Local leftOp, LengthExpr rightOp)
	{
		
	}

	void handleArrayStoreStmt(ArrayRef leftOp, Immediate rightOp)
	{
	}

	void handleInstanceOfStmt(Local leftOp, InstanceOfExpr expr)
	{

	}


}

