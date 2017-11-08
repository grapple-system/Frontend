package edu.zuo.pegraph.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.ClassConstant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StringConstant;

public class PegIntra {
	
	private SootMethod soot_method;
	
	//formal parameters
	private List formal_paras = new ArrayList<Value>();
	
	//formal returns
	private Value formal_return;

	//call sites
	private Map callSites = new HashMap<InvokeExpr, CallSite>();
	
	
	//edges
	private Map local2Local = new HashMap<Local, HashSet<Local>>();
	
	private Map obj2Local = new HashMap<Value, HashSet<Local>>();
	
	private Map local2Field = new HashMap<Local, HashSet<Value>>();
	
	private Map field2Local = new HashMap<Value, HashSet<Local>>();
	

	public PegIntra(SootMethod sm){
		this.soot_method = sm;
	}


	public void addJavaClassObj2Local(Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public CallSite createCallSite(InvokeExpr ie) {
		// TODO Auto-generated method stub
		return null;
	}



	public void setFormalReturn(Value v) {
		// TODO Auto-generated method stub
		if(v instanceof Local){
			
		}
		else if (v instanceof StringConstant){
			
			
		}
		else if(v instanceof ClassConstant){
			
		}
		else{
			System.err.println("error!!!");
		}
	}
	

	public void addFormalParameter(Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addLocal2ArrayRef(Local rhs, ArrayRef lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addStringConst2ArrayRef(StringConstant rhs, ArrayRef lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addClassConst2ArrayRef(ClassConstant rhs, ArrayRef lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addLocal2FieldRef(Local rhs, FieldRef lhs) {
		// TODO Auto-generated method stub
		if (lhs instanceof InstanceFieldRef) {
			
		}
		else{
			
		}
	}


	public void addStringConst2FieldRef(StringConstant rhs, FieldRef lhs) {
		// TODO Auto-generated method stub
		if (lhs instanceof InstanceFieldRef) {
			
		}
		else{
			
		}
	}


	public void addClassConst2FieldRef(ClassConstant rhs, FieldRef lhs) {
		// TODO Auto-generated method stub
		if (lhs instanceof InstanceFieldRef) {
			
		}
		else{
			
		}
	}


	public void addLocal2Local(Local rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addStringConst2Local(StringConstant rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addClassConst2Local(ClassConstant rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addNewExpr2Local(NewExpr rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addNewArrayExpr2Local(NewArrayExpr rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addNewMultiArrayExpr2Local(NewMultiArrayExpr rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}


	public void addField2Local(FieldRef rhs, Local lhs) {
		// TODO Auto-generated method stub
		if (rhs instanceof InstanceFieldRef) {
			
		}
		else{
			
		}
	}


	public void addArrayRef2Local(ArrayRef rhs, Local lhs) {
		// TODO Auto-generated method stub
		
	}



	public class CallSite{
		
		private List actual_args = new ArrayList<Value>();
		
		private Local actural_return;
		
		public CallSite(){
			
		}

		public void addReceiver(Local base) {
			// TODO Auto-generated method stub
			
		}

		public void setActualReturn(Local lhs) {
			// TODO Auto-generated method stub
			this.actural_return = lhs;
		}

		public void addArg(Value arg) {
			// TODO Auto-generated method stub
			if(arg instanceof Local){

			}
			else if(arg instanceof StringConstant){
				
			}
			else if(arg instanceof ClassConstant){
				
			}
			else{
				System.err.println("error!!!");
			}
		}
		
	}
	
}
