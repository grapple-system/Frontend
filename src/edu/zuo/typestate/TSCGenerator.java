package edu.zuo.typestate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.zuo.setree.client.IntraMain;
import edu.zuo.typestate.datastructure.CallExecutor;
import edu.zuo.typestate.datastructure.CallInfo;
import edu.zuo.typestate.datastructure.Point;
import edu.zuo.typestate.datastructure.TypeGraph;
import edu.zuo.typestate.datastructure.TypeGraphList;
import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class TSCGenerator {
	private Block block;
	private TypeGraphList typegraph_list;
	private List<Local> interestLocal = new ArrayList<Local>();
	private Stmt laststmt;
	// private static List<CallInfo> callInfoList = new ArrayList<CallInfo>();
	private static List<String> calledVar = new ArrayList<String>();
	private String callNumFile = "E:/Study/zuo_project/pepper_wef/pepper/callNumFile.txt";
	public static boolean lastCall = false;

	public TSCGenerator(Block block, TypeGraphList typegraph_list) {
		this.block = block;
		this.typegraph_list = typegraph_list;
		laststmt = (Stmt) block.getTail();
	}

	public static void printCallInfo(String filepath) {
		File file = new File(filepath);
		try {
			if (!file.exists())
				file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for (CallInfo callinfo : CallExecutor.callInfoList) {
				pw.println(callinfo.print2str());
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printCalledVar(String filepath) {
		File file = new File(filepath);
		try {
			if (!file.exists())
				file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for (String calledvar : calledVar) {
				pw.println(calledvar);
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// calledVar.clear();
	}

	public void recordCallInfo(String callmethod, String receivemethod, int index, String var, String par, int callhash,
			int callNum) {
		TypeGraph tg = typegraph_list.getTypeGraph(var);
		assert(tg != null);
		String[] allStates = tg.getStates();
		for (String state : allStates) {
			String callStr = var + "." + index + "." + state + "_" + callhash;
			String receiveStr = par + "." + "0." + state + "_" + receivemethod;
			CallInfo callInfo = new CallInfo(callmethod, receivemethod, index, callStr, receiveStr, callNum);
			CallExecutor.callInfoList.add(callInfo);
			tg.doDegreeOut(Integer.toString(callhash));
			String calledvar = receivemethod + ":" + par;
			if (!calledVar.contains(calledvar))
				calledVar.add(calledvar);
		}
		for (String state : allStates) {
			String callStr = par + "." + "-2." + state + "_-" + receivemethod;
			String receiveStr = var + "." + index + "." + state + "_-" + callhash;
			CallInfo callInfo = new CallInfo(receivemethod, callmethod, -2, callStr, receiveStr, callNum);
			CallExecutor.callInfoList.add(callInfo);
			tg.doDegreeIn("-" + callhash);
		}
	}

	public void process(String dirPath, int nodeindex) {
		init();
		checkCalledMet();
		// write stmt with hashcode for checking
		File file = new File(dirPath + "/jimple.txt");
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
				fos = new FileOutputStream(file);
			} else {
				fos = new FileOutputStream(file, true);
			}
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			osw.write("block " + block.getIndexInMethod() + "\r\n");
			Stmt nowst, succst;
			Iterator<Unit> stmts = block.iterator();
			if (stmts.hasNext()) {
				nowst = (Stmt) stmts.next();
				succst = nowst;
				for (; stmts.hasNext();) {
					succst = (Stmt) stmts.next();
					osw.write(nowst.hashCode() + ":" + nowst.toString() + "\r\n");
					typegraph_list.state = 0;
					processStmt(nowst, succst, nodeindex);
					nowst = succst;
				}
				osw.write(succst.hashCode() + ":" + succst.toString() + "\r\n");
				typegraph_list.state = 0;
				processStmt(succst, succst, nodeindex);
			}
			osw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() {
		interestLocal.clear();
		Chain<Local> locals = block.getBody().getLocals();
		for (Local local : locals) {
			String classname = local.getType().toString();
			// if (IntraMain.allClass.contains(classname) &&
			// !local.getName().startsWith("$")) {
			if (IntraMain.allClass.contains(classname)) {
				interestLocal.add(local);
				// TypeGraph tg = new TypeGraph(classname, local.getName());
				// typegraph_list.addTypeGraph(tg);
			}
		}
	}

	private void processStmt(Stmt s, Stmt succst, int index) {
		lastCall = false;
		if (s == laststmt)
			typegraph_list.state = 1;
		if (s instanceof ReturnVoidStmt) {
			// typegraph_list.state = 0;
			typegraph_list.transAllReturn(Integer.toString(s.hashCode()), "-" + Integer.toString(s.hashCode()));
			return;
		}
		if (s instanceof ReturnStmt) {
			// typegraph_list.state = 0;
			typegraph_list.transAllReturn(Integer.toString(s.hashCode()), "-" + Integer.toString(s.hashCode()));
			return;
		}
		if (s instanceof GotoStmt) {
			assert(s == laststmt);
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s instanceof IfStmt) {
			// IfStmt ifs = (IfStmt) s;
			// Stmt target = ifs.getTarget();
			// typegraph_list.transAll(-s.hashCode(), target.hashCode());
			// typegraph_list.transAll(-s.hashCode(), succst.hashCode());
			assert(s == laststmt);
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s instanceof TableSwitchStmt) {
			// TableSwitchStmt tst = (TableSwitchStmt) s;
			// Unit defaulttarget = tst.getDefaultTarget();
			// typegraph_list.transAll(-s.hashCode(), defaulttarget.hashCode());
			// Iterator targets = tst.getTargets().iterator();
			// for (; targets.hasNext();) {
			// Unit target = (Unit) targets.next();
			// typegraph_list.transAll(-s.hashCode(), target.hashCode());
			// }
			assert(s == laststmt);
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s instanceof LookupSwitchStmt) {
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s instanceof MonitorStmt) {
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s instanceof RetStmt) {
			typegraph_list.transAllReturn(Integer.toString(s.hashCode()), "-" + Integer.toString(s.hashCode()));
			return;
		}
		if (s instanceof NopStmt) {
			typegraph_list.transAll(s.hashCode(), succst.hashCode());
			return;
		}
		if (s.containsInvokeExpr()) {
			InvokeExpr ie = s.getInvokeExpr();
			if (IntraMain.InterestMethod.contains(ie.getMethod().toString()) && ie instanceof InstanceInvokeExpr) {
				Local base = (Local) ((InstanceInvokeExpr) ie).getBase();
				// filewriter = new FileWriter(): $r0 =
				// FileWriter.init;filewriter = $r0;
				if (ie.toString().contains("<init>") && succst instanceof AssignStmt) {
					Value lhs = ((DefinitionStmt) succst).getLeftOp();
					Value rhs = ((DefinitionStmt) succst).getRightOp();
					if ((lhs instanceof Local && rhs instanceof Local
							&& ((Local) rhs).getName().equals(base.getName()))) {
						base = (Local) lhs;
					}
				}
				if (ie.toString().contains("newTcpPeer") && s instanceof AssignStmt) {
					Value lhs = ((DefinitionStmt) s).getLeftOp();
					Value rhs = ((DefinitionStmt) s).getRightOp();
					if (base.getName().contains("this") && lhs instanceof Local) {
						base = (Local) lhs;
					}
				}
				if (contains(base.getName())) {
					typegraph_list.transMethod(base.getName() + base.hashCode(), ie.getMethod().toString(),
							s.hashCode(), succst.hashCode());
				}
			} else {
				SootMethod receiver = ie.getMethod();
				List<String> args = new ArrayList<String>();
				List<String> rargs = new ArrayList<String>();
				for (int i = 0; i < ie.getArgCount(); i++) {
					Value arg = ie.getArg(i);
					if (arg instanceof Local && contains(((Local) arg).getName()) && receiver.hasActiveBody()) {
						Local rarg = receiver.getActiveBody().getParameterLocal(i);
						args.add(((Local) arg).getName() + arg.hashCode());
						rargs.add(rarg.getName() + rarg.hashCode());
					}
				}
				if (args.size() > 0) {
					CallExecutor.initCallNum(new File(callNumFile));
					CallExecutor.callNum++;
					for (int i = 0; i < args.size(); i++)
						recordCallInfo(block.getBody().getMethod().getSignature(), receiver.getSignature(), index,
								args.get(i), rargs.get(i), s.hashCode(), CallExecutor.callNum);
					if (s == laststmt) {
						lastCall = true;
						typegraph_list.state = 0;
						typegraph_list.transAll(s.hashCode(), -s.hashCode());
						typegraph_list.state = 1;
						typegraph_list.transAll(-s.hashCode(), succst.hashCode());
					} else if (!receiver.getSignature().contains("org.apache.zookeeper")) {
						typegraph_list.transAll(s.hashCode(), -s.hashCode());
						typegraph_list.transAll(-s.hashCode(), succst.hashCode());
					} else {
						typegraph_list.transAll(-s.hashCode(), succst.hashCode());
					}
					CallExecutor.exitCallNum(new File(callNumFile));
				} else
					typegraph_list.transAll(s.hashCode(), succst.hashCode());
			}
			return;
		}
		typegraph_list.transAll(s.hashCode(), succst.hashCode());
		return;
	}

	private void checkCalledMet() {
		SootMethod sm = block.getBody().getMethod();

		for (int i = 0; i < sm.getParameterCount(); i++) {
			Value arg = sm.getActiveBody().getParameterLocal(i);
			if (arg instanceof Local && contains(((Local) arg).getName())) {
				String calledvar = sm.getSignature() + ":" + ((Local) arg).getName() + arg.hashCode();
				if (!calledVar.contains(calledvar))
					calledVar.add(calledvar);
			}
		}
	}

	private boolean contains(String name) {
		for (Local local : interestLocal) {
			if (local.getName().equals(name))
				return true;
		}
		return false;
	}

	public void print(String path) {
		typegraph_list.printDot(path + "/" + block.getIndexInMethod() + ".txt");
	}
}
