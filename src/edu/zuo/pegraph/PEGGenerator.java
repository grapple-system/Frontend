package edu.zuo.pegraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;

public class PEGGenerator extends BodyTransformer {

	@Override
	protected void internalTransform(Body arg0, String arg1, Map arg2) {
		// TODO Auto-generated method stub
		SootMethod method = arg0.getMethod();
		
		if (!method.hasActiveBody()) {
			method.retrieveActiveBody();
		}
		
		// first of all, flow edges are added by inspecting the statements in the
		// method one by one
		for (Iterator stmts = method.getActiveBody().getUnits().iterator(); stmts
				.hasNext();) {
			Stmt st = (Stmt) stmts.next();
			processStmt(st, method);
		}
		
	}
	
	/**
	 * Ignores certain types of statements, and calls addFlowEdges()
	 * 
	 * @param s
	 * @param sm
	 */
	private void processStmt(Stmt s, SootMethod sm) {
		if (s instanceof ReturnVoidStmt)
			return;
		if (s instanceof GotoStmt)
			return;
		if (s instanceof IfStmt)
			return;
		if (s instanceof TableSwitchStmt)
			return;
		if (s instanceof LookupSwitchStmt)
			return;
		if (s instanceof MonitorStmt)
			return;
		addFlowEdges(s, sm);
	}

	
	private void addFlowEdges(Stmt s, SootMethod sm) {

		if (s instanceof NopStmt) return;
		
		// call site
		if (s.containsInvokeExpr()) {
			InvokeExpr ie = s.getInvokeExpr();			
			
			// deals with return values (which matters only for AssignStmt
			if (s instanceof AssignStmt) {
				Local lhs = (Local) ((AssignStmt) s).getLeftOp();
				InvokeExpr iie = s.getInvokeExpr();
				SootMethod static_target = iie.getMethod();
				String sig = static_target.getSubSignature();
				String cls = static_target.getDeclaringClass().getName();
				// deals with certain special cases
				// and since they are special, the parameters of them are not
				// handled
				// TODO: read through this part
				if (sig.equals("java.lang.Object newInstance()")
						&& cls.equals("java.lang.Class")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());

					addObjEdge(rn, ln);

					return;
				}
				if (sig
						.equals("java.lang.Object newInstance(java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Constructor")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}
				if (static_target.getSignature().equals("<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm, lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
				}
				if (sig
						.equals("java.lang.Object invoke(java.lang.Object,java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Method")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}

				if (sig
						.equals("java.lang.Object newProxyInstance(java.lang.ClassLoader,java.lang.Class[],java.lang.reflect.InvocationHandler)")
						&& cls.equals("java.lang.reflect.Proxy")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}

				// ---

				Type rt = static_target.getReturnType();
				if (rt instanceof RefType || rt instanceof ArrayType) {
					SymbolicReturnedObject ro = new SymbolicReturnedObject(sm,
							rt);
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					// TODO: returnVars shouldn't be changed here
//					returnVars.add(ln);
					addSymbolicEdge(ro, ln);
				} // Otherwise, the return type is not a reference.

			}

			// ---

			// deals with parameters
			if (s.getInvokeExpr() instanceof InstanceInvokeExpr) {
				Local base = (Local) ((InstanceInvokeExpr) s.getInvokeExpr())
						.getBase();
				LocalVarNode lvn = NodeFactory.v(sm).makeLocalVarNode(sm, base);
				Assert.assertTrue(lvn != null);
				receivers.add(lvn);
			}

			// ---

			// adds the pair <call site, targets> to cs2Targets map
			List list = new ArrayList();
			resolveCall(s.getInvokeExpr(), list);
			cs2Targets.put(s, list);
			return;
		}

		// END call site handling

		// ---

		// case 1: return
		if (s instanceof ReturnStmt) {
			Value v = ((ReturnStmt) s).getOp();

			if (v instanceof Local && isTypeofInterest(v)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) v);
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addVarEdge(ln, rn);
				
				// TODO: add returnVars
				returnVars.add(ln);
			}

			if (v instanceof StringConstant) {
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addObjEdge(new StringConstNode(sm, (StringConstant) v), rn);
			}
			if (v instanceof ClassConstant) {
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addObjEdge(ClassConstNode.node, rn);
			}
			return;
		}

		// case 2: throw
		if (s instanceof ThrowStmt) {
			Local l = (Local) ((ThrowStmt) s).getOp();
			LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm, l);
			addVarEdge(ln, ExceptionVarNode.node);
			return;
		}

		Value lhs = ((DefinitionStmt) s).getLeftOp();
		Value rhs = ((DefinitionStmt) s).getRightOp();

		// case 3: IdentityStmt
		if (s instanceof IdentityStmt) {

			if (rhs instanceof CaughtExceptionRef) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addVarEdge(ExceptionVarNode.node, ln);
				if (exceptionObj == null)
					exceptionObj = new SymbolicExceptionObject(sm);
				// headObjects.add(exceptionObj);
				addSymbolicEdge(exceptionObj, ExceptionVarNode.node);
			}

			if ((rhs instanceof ThisRef || rhs instanceof ParameterRef)
					&& isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				SymbolicParamObject obj = new SymbolicParamObject(sm,
						(IdentityRef) rhs);
				headObjects.add(obj);
				addSymbolicEdge(obj, ln);
			}
			return;
		}

		if (s instanceof AssignStmt) {
			// case 4.1: lhs is array access
			if (lhs instanceof ArrayRef) {
				// if rhs is local
				if (rhs instanceof Local && isTypeofInterest(rhs)) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) rhs);

					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());

					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);

					addVarEdge(ln, node);
				}
				// rhs is a string constant
				if (rhs instanceof StringConstant) {

					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());
					addObjEdge(new StringConstNode(sm, (StringConstant) rhs),
							node);
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);

				}
				if (rhs instanceof ClassConstant) {
					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);
					addObjEdge(ClassConstNode.node, node);
				}
				return;
			}

			// case 4.2: lhs is a field access
			if (lhs instanceof FieldRef) {

				if (rhs instanceof Local && isTypeofInterest(rhs)) {

					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) rhs);
					if (lhs instanceof InstanceFieldRef) {
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(), l);
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);
						addVarEdge(ln, fn);						
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());

						addVarEdge(ln, gn);
					}

				}
				// if rhs is a string constant
				if (rhs instanceof StringConstant) {
					if (lhs instanceof InstanceFieldRef) {
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(),
								(Local) ((InstanceFieldRef) lhs).getBase());
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);

						addObjEdge(
								new StringConstNode(sm, (StringConstant) rhs),
								fn);
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());

						addObjEdge(
								new StringConstNode(sm, (StringConstant) rhs),
								gn);
					}
				}
				// if rhs is a class constant
				if (rhs instanceof ClassConstant) {
					if (lhs instanceof InstanceFieldRef) {
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(),
								(Local) ((InstanceFieldRef) lhs).getBase());
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);
						addObjEdge(ClassConstNode.node, fn);
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());
						addObjEdge(ClassConstNode.node, gn);
					}
				}
				return;
			}

			if (!isTypeofInterest(lhs))
				return;

			// case 4.3: local := local
			if (rhs instanceof Local && isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				LocalVarNode rhs_ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) rhs);
				addVarEdge(rhs_ln, ln);
				return;
			}

			// case 4.4.1: local := string const
			if (rhs instanceof StringConstant) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addObjEdge(new StringConstNode(sm, (StringConstant) rhs), ln);
				return;
			}
			// case 4.4.2: local := class const
			if (rhs instanceof ClassConstant) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addObjEdge(ClassConstNode.node, ln);
				return;
			}

			// case 4.5: local := new X
			if (rhs instanceof NewExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				AllocNode on = NodeFactory.v(sm).makeAllocNode(sm,
						(NewExpr) rhs);
				addObjEdge(on, ln);
				return;
			}

			// case 4.6: new array: e.g. x := new Y[5];
			if (rhs instanceof NewArrayExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				ArrayAllocNode an = NodeFactory.v(sm).makeArrayAllocNode(sm,
						(NewArrayExpr) rhs);
				addObjEdge(an, ln);
				return;
			}

			// case 4.7: new multi-dimensional array
			if (rhs instanceof NewMultiArrayExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				ArrayAllocNode an = NodeFactory.v(sm).makeArrayAllocNode(sm,
						(NewMultiArrayExpr) rhs);
				addObjEdge(an, ln);
				return;
			}

			// case 4.8: rhs is field access x.f or X.f
			if (rhs instanceof FieldRef && isTypeofInterest(rhs)) {

				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				if (rhs instanceof InstanceFieldRef) {

					Local l = (Local) ((InstanceFieldRef) rhs).getBase();
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							l);
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(sm,
							((FieldRef) rhs).getField(),
							(Local) ((InstanceFieldRef) rhs).getBase());
					fields.add(fn);
					addVarEdge(fn, ln);
				} else {
					GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(sm,
							((FieldRef) rhs).getField());
					SymbolicGlobalObject o = (SymbolicGlobalObject) global2symbolic
							.get(gn);
					if (o == null) {
						o = new SymbolicGlobalObject(sm, gn);
						global2symbolic.put(gn, o);

					}
					addSymbolicEdge(o, gn);
					addVarEdge(gn, ln);
				}
				return;
			}

			// case 4.9: cast

			if (rhs instanceof CastExpr && isTypeofInterest(rhs)) {
				Value y = ((CastExpr) rhs).getOp();
				// possibleTypes.add(lhs.getType());
				if (y instanceof Local && isTypeofInterest(y)) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					LocalVarNode rhs_ln = NodeFactory.v(sm).makeLocalVarNode(
							sm, (Local) y);
					addVarEdge(rhs_ln, ln);
				}
				if (y instanceof StringConstant) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					addObjEdge(new StringConstNode(sm, (StringConstant) y), ln);
				}
				if (y instanceof ClassConstant) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					addObjEdge(ClassConstNode.node, ln);
				}
				return;
			}

			// case 4.10: rhs is array reference
			if (rhs instanceof ArrayRef && isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				FieldVarNode vn = NodeFactory.v(sm).makeArrayElementVarNode(sm,
						(Local) ((ArrayRef) rhs).getBase());

				LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
						((Local) ((ArrayRef) rhs).getBase()));
				Set fields = (Set) vartofields.get(base);
				if (fields == null) {
					fields = new HashSet();
					vartofields.put(base, fields);
				}
				fields.add(vn);
				addVarEdge(vn, ln);
				return;
			}

			if (rhs instanceof BinopExpr) {
				return;
			}

			if (rhs instanceof UnopExpr) {
				return;
			}

			if (rhs instanceof InstanceOfExpr) {
				return;
			}

			return;

		} // AssignStmt

	}
	
	
	public static boolean isTypeofInterest(Value v) {
		return (v.getType() instanceof RefType || v.getType() instanceof ArrayType);
	}
}
