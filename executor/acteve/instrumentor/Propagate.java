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
import acteve.symbolic.integer.SymbolicDouble;
import acteve.symbolic.integer.SymbolicFloat;
import acteve.symbolic.integer.SymbolicInteger;
import acteve.symbolic.integer.SymbolicLong;
import acteve.symbolic.integer.SymbolicRef;
import acteve.symbolic.integer.UnaryOperator;
import acteve.symbolic.integer.operation.NEGATION;
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


public class Propagate extends AbstractStmtSwitch {
	
	private Map<Local, Expression> localsMap;

	public Propagate(){
		this.localsMap = new LinkedHashMap<Local, Expression>();
	}
	
	public Propagate(Map<Local, Expression> localsM){
		this.localsMap = localsM;
	}


    private void printOutLocalsMap() {
		// TODO Auto-generated method stub
		System.out.println(localsMap.size());
		for(Local local: localsMap.keySet()){
			System.out.println(local.toString() + " -- " + localsMap.get(local).toString());
		}
		System.out.println();
	}


	private void handleInvokeExpr(Stmt s)
	{
//		InvokeExpr ie = s.getInvokeExpr();
//		List symArgs = new ArrayList();
//		SootMethod callee = ie.getMethod();
//		int subSig = methSubsigNumberer.getOrMakeId(callee);
//		
//		//pass the subsig of the callee
//		symArgs.add(IntConstant.v(subSig));
//		
//		List args = new ArrayList();
//		if (ie instanceof InstanceInvokeExpr) {
//			Immediate base = (Immediate) ((InstanceInvokeExpr) ie).getBase();
//			args.add(base);
//			//symArgs.add(symLocalfor(base));
//			symArgs.add(NullConstant.v());
//		}
//		for (Iterator it = ie.getArgs().iterator(); it.hasNext();) {
//			Immediate arg = (Immediate) it.next();
//			args.add(arg);
//			symArgs.add(addSymLocationFor(arg.getType()) ? symLocalfor(arg) : NullConstant.v());
//		}
//
//		G.invoke(G.staticInvokeExpr(G.argPush[symArgs.size()-1], symArgs));
//
//		if (s instanceof AssignStmt) {
//			Local retValue = (Local) ((AssignStmt) s).getLeftOp();
//			if(addSymLocationFor(retValue.getType())) {
//				G.editor.insertStmtAfter(G.jimple.newAssignStmt(symLocalfor(retValue),
//																G.staticInvokeExpr(G.retPop, IntConstant.v(subSig))));
//				
//				SootMethod modelInvoker = ModelMethodsHandler.getModelInvokerFor(callee);
//				if (modelInvoker != null) {
//					G.editor.insertStmtAfter(G.jimple.newInvokeStmt(G.staticInvokeExpr(modelInvoker.makeRef(), args)));
//				}
//			}
//		}
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
	}

	public void caseIdentityStmt(IdentityStmt is)
	{
		Local lhs = (Local) is.getLeftOp();
		Value rhs = ((DefinitionStmt) is).getRightOp();
		String rhs_name = rhs.toString();

		
		if (rhs instanceof ParameterRef) {
			Expression sym_para = null;
			int index = ((ParameterRef) rhs).getIndex();
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
				localsMap.put(lhs, sym_para);
			}
			else if(rhs.getType() instanceof RefLikeType){
				sym_para = new SymbolicRef(rhs_name, null);
//				localsMap.put(lhs, sym_para);
			}
			else{
				System.err.println("unexpected type!!!");
			}
			
		}
		else if(rhs instanceof ThisRef){
			Expression sym = new SymbolicRef(rhs_name, null);
			localsMap.put(lhs, sym);
		}
		else{
			assert(rhs instanceof CaughtExceptionRef);
		}
		
	}

	void handleBinopStmt(Local leftOp, BinopExpr binExpr)
	{
		Immediate op1 = (Immediate) binExpr.getOp1();
        Immediate op2 = (Immediate) binExpr.getOp2();
		
		if(!(op1.getType() instanceof PrimType) || !(op2.getType() instanceof PrimType))
			return;

		Expression symOp1 = op1 instanceof Constant ? getConstant((Constant) op1) : localsMap.get((Local) op1);
		Expression symOp2 = op2 instanceof Constant ? getConstant((Constant) op2) : localsMap.get((Local) op2);
		
		BinaryOperator binop = getBinaryOperator(binExpr);
		Expression rightOp_sym = binop.apply(symOp1, symOp2);
		localsMap.put(leftOp, rightOp_sym);
	}
	
	private BinaryOperator getBinaryOperator(BinopExpr binExpr) {
		// TODO Auto-generated method stub
		String binExprSymbol = binExpr.getSymbol();
		
		Type binType = binExpr.getType();
		assert(binType == binExpr.getOp1().getType());
		
		if(binType instanceof IntType || binType instanceof ShortType || binType instanceof CharType || binType instanceof ByteType){
			return new IntegerBinaryOperator(binExprSymbol);
		}
		else if(binType instanceof LongType){
			return new LongBinaryOperator(binExprSymbol);
		}
		else if(binType instanceof FloatType){
			return new FloatBinaryOperator(binExprSymbol);
		}
		else if(binType instanceof DoubleType){
			return new DoubleBinaryOperator(binExprSymbol);
		}
		else if(binType instanceof BooleanType){
			return new BooleanBinaryOperator(binExprSymbol);
		}
		else{
			System.err.println("wrong type: " + binType.toString());
		}
		
		return null;
	}

	Expression getConstant(Constant operand){
		assert(operand.getType() instanceof PrimType);
		
		Type constType = operand.getType();
		if(constType instanceof IntType){
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
		else{
			System.err.println("wrong type: " + constType.toString());
		}
		return null;
	}
	

	void handleNegStmt(Local leftOp, NegExpr negExpr)
	{
		Immediate op = (Immediate) negExpr.getOp();
		if(!(op.getType() instanceof PrimType)){
			return;
		}
		
		Expression symOp = op instanceof Constant ? getConstant((Constant) op) : localsMap.get((Local) op);
		UnaryOperator unaop = getUnaryOperator(negExpr);
		Expression rightOp_sym = unaop.apply(symOp);
		localsMap.put(leftOp, rightOp_sym);
	}
	
	private UnaryOperator getUnaryOperator(UnopExpr unaryExpr) {
//		String unaryExprSymbol = unaryExpr instanceof NegExpr? "-" : "lengthOf";
		
		String unaryExprSymbol = "-";
		Type unaryType = unaryExpr.getType();
		assert(unaryType == unaryExpr.getOp().getType());
		
		if(unaryType instanceof IntType || unaryType instanceof ShortType || unaryType instanceof CharType || unaryType instanceof ByteType){
			return new IntegerUnaryOperator(unaryExprSymbol);
		}
		else if(unaryType instanceof LongType){
			return new LongUnaryOperator(unaryExprSymbol);
		}
		else if(unaryType instanceof FloatType){
			return new FloatUnaryOperator(unaryExprSymbol);
		}
		else if(unaryType instanceof DoubleType){
			return new DoubleUnaryOperator(unaryExprSymbol);
		}
		else if(unaryType instanceof BooleanType){
			return new NEGATION(unaryExprSymbol);
		}
		else{
			System.err.println("wrong type: " + unaryType.toString());
		}
		
		return null;
	}

	void handleSimpleAssignStmt(Local leftOp, Immediate rightOp)
	{
		Expression exp;
		if(rightOp instanceof Constant){
			exp = getConstant((Constant) rightOp);
		}
		else{
			assert(rightOp instanceof Local);
			exp = localsMap.get((Local) rightOp);
		}
		localsMap.put(leftOp, exp);
	}

	void handleStoreStmt(FieldRef leftOp, Immediate rightOp) 
	{
//		Immediate base;
//		if (leftOp instanceof StaticFieldRef) {
//			base = NullConstant.v();
//		} else {
//			base = (Local) ((InstanceFieldRef) leftOp).getBase();
//		}
//
//		SootField fld = leftOp.getField();
//		if (!Main.isInstrumented(fld.getDeclaringClass())) 
//			return;
//
//		if(addSymLocationFor(fld.getType())) {
//			SootField fld_sym = fieldsMap.get(fld);
//			assert fld_sym != null : fld + " " + fld.getDeclaringClass();
//			FieldRef leftOp_sym;
//			if (leftOp instanceof StaticFieldRef) {
//				leftOp_sym = G.staticFieldRef(fld_sym.makeRef());
//			} else {
//				leftOp_sym = G.instanceFieldRef(base, fld_sym.makeRef());
//			}
//			G.assign(leftOp_sym, symLocalfor(rightOp));
//		} 
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
			Expression exp;
			if (op instanceof Constant) {
				exp = getConstant((Constant) op);
			} 
			else {
				assert(op instanceof Local);
				exp = localsMap.get((Local) op);
			}
			localsMap.put(leftOp, exp);
		}
	}

	void handleArrayLoadStmt(Local leftOp, ArrayRef rightOp)
	{
//		Local base = (Local) rightOp.getBase();
//		Immediate index = (Immediate) rightOp.getIndex();
//		
//		Local base_sym = localsMap.get(base);
//		Local leftOp_sym = localsMap.get(leftOp);
//		if(base_sym != null) {
//			Immediate index_sym = index instanceof Constant ? NullConstant.v() : localsMap.get((Local) index);
//			Type[] paramTypes = new Type[]{G.EXPRESSION_TYPE, G.EXPRESSION_TYPE, base.getType(), IntType.v()};
//			SootMethodRef ref = G.symOpsClass.getMethod(G.arrayGetMethodName, Arrays.asList(paramTypes)).makeRef();
//			G.assign(leftOp_sym, G.staticInvokeExpr(ref, Arrays.asList(new Immediate[]{base_sym, index_sym, base, index})));
//		} else if(leftOp_sym != null){
//			G.assign(leftOp_sym, NullConstant.v());
//		}
//		if (doRW()) {
//			if (rwKind == RWKind.ID_FIELD_WRITE || rwKind == RWKind.EXPLICIT_WRITE)
//				G.invoke(G.staticInvokeExpr(G.readArray, base, index));
//        }
	}

	void handleArrayLengthStmt(Local leftOp, LengthExpr rightOp)
	{
		
	}

	void handleArrayStoreStmt(ArrayRef leftOp, Immediate rightOp)
	{
//		Local base = (Local) leftOp.getBase();
//		Immediate index = (Immediate) leftOp.getIndex();
//
//		Local base_sym = localsMap.get(base);
//		if(base_sym != null){
//			Immediate index_sym = index instanceof Constant ? NullConstant.v() : localsMap.get((Local) index);
//			
//			Immediate rightOp_sym = rightOp instanceof Constant ? NullConstant.v() : localsMap.get((Local) rightOp);
//			
//			Type[] paramTypes = new Type[]{G.EXPRESSION_TYPE, G.EXPRESSION_TYPE, G.EXPRESSION_TYPE,
//										   base.getType(), IntType.v(), ((ArrayType) base.getType()).baseType};
//			SootMethodRef ref = G.symOpsClass.getMethod(G.arraySetMethodName, Arrays.asList(paramTypes)).makeRef();
//			G.invoke(G.staticInvokeExpr(ref, Arrays.asList(new Immediate[]{base_sym, index_sym,
//																		   rightOp_sym, base, index, rightOp})));
//		}
//		if (doRW()) {
//			if (rwKind == RWKind.ID_FIELD_WRITE || rwKind == RWKind.EXPLICIT_WRITE)
//            	G.invoke(G.staticInvokeExpr(G.writeArray, base, index));
//			else if (rwKind == RWKind.ONLY_WRITE)
//            	G.invoke(G.staticInvokeExpr(G.only_write, IntConstant.v(-1)));
//        }
//		
	}

	void handleInstanceOfStmt(Local leftOp, InstanceOfExpr expr)
	{

	}


}

