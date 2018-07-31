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
import edu.zuo.typestate.datastructure.CallInfo;
import edu.zuo.typestate.datastructure.Point;
import edu.zuo.typestate.datastructure.TypeGraph;
import edu.zuo.typestate.datastructure.TypeGraphList;
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
	private static List<CallInfo> callInfoList = new ArrayList<CallInfo>();
	private static List<String> calledVar = new ArrayList<String>();

	public TSCGenerator(Block block, TypeGraphList typegraph_list) {
		this.block = block;
		this.typegraph_list = typegraph_list;
		laststmt = (Stmt) block.getTail();
	}
	
	public static void printCallInfo(String filepath){
		File file = new File(filepath);
		try {
			if(!file.exists())
			file.createNewFile();		
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
			for(CallInfo callinfo : callInfoList){
				pw.println(callinfo.print2str());
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void printCalledVar(String filepath){
		File file = new File(filepath);
		try {
			if(!file.exists())
			file.createNewFile();		
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
			for(String calledvar : calledVar){
				pw.println(calledvar);
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void recordCallInfo(String callmethod, String receivemethod, int index, String var, String par, int callhash) {
		TypeGraph tg = typegraph_list.getTypeGraph(var);
		assert(tg != null);
		String[] allStates = tg.getStates();
		for(String state : allStates){
			String callStr = var+"."+index+"."+state+"_"+callhash;
			String receiveStr = par+"."+"-1."+state+"_"+receivemethod;
			CallInfo callInfo = new CallInfo(callmethod, receivemethod, index, callStr, receiveStr);
			callInfoList.add(callInfo);
			String calledvar = receivemethod+":"+par;
			if(!calledVar.contains(calledvar))
				calledVar.add(calledvar);
		}
		for(String state : allStates){
			String callStr = par+"."+"-2."+state+"_-"+receivemethod;
			String receiveStr = var+"."+index+"."+state+"_-"+callhash;
			CallInfo callInfo = new CallInfo(receivemethod, callmethod, -2, callStr, receiveStr);
			callInfoList.add(callInfo);
		}
	}

	public void process(String dirPath, int nodeindex) {
		init();
		// write stmt with hashcode for checking
		File file = new File(dirPath + "/jimple.txt");
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
				file.createNewFile();// Èç¹ûÎÄ¼þ²»´æÔÚ£¬¾Í´´½¨¸ÃÎÄ¼þ
				fos = new FileOutputStream(file);// Ê×´ÎÐ´Èë»ñÈ¡
			} else {
				// Èç¹ûÎÄ¼þÒÑ´æÔÚ£¬ÄÇÃ´¾ÍÔÚÎÄ¼þÄ©Î²×·¼ÓÐ´Èë
				fos = new FileOutputStream(file, true);// ÕâÀï¹¹Ôì·½·¨¶àÁËÒ»¸ö²ÎÊýtrue,±íÊ¾ÔÚÎÄ¼þÄ©Î²×·¼ÓÐ´Èë
			}
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");// Ö¸¶¨ÒÔUTF-8¸ñÊ½Ð´ÈëÎÄ¼þ
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
			if (IntraMain.allClass.contains(classname) && !local.getName().startsWith("$")) {
				interestLocal.add(local);
				// TypeGraph tg = new TypeGraph(classname, local.getName());
				// typegraph_list.addTypeGraph(tg);
			}
		}
	}

	private void processStmt(Stmt s, Stmt succst, int index) {
		if (s == laststmt)
			typegraph_list.state = 1;
		if (s instanceof ReturnVoidStmt) {
			//typegraph_list.state = 0;
			typegraph_list.transAll(Integer.toString(s.hashCode()), "-"+block.getBody().getMethod().getSignature());
			return;
		}
		if (s instanceof ReturnStmt) {
			//typegraph_list.state = 0;
			typegraph_list.transAll(Integer.toString(s.hashCode()), "-"+block.getBody().getMethod().getSignature());
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
			typegraph_list.transAll(Integer.toString(s.hashCode()), "-"+block.getBody().getMethod().getSignature());
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
					if (lhs instanceof Local && rhs instanceof Local
							&& ((Local) rhs).getName().equals(base.getName())) {
						base = (Local) lhs;
					}
				}
				if (contains(base.getName())) {
					typegraph_list.transMethod(base.getName()+base.hashCode(), ie.getMethod().toString(), s.hashCode(),
							succst.hashCode());
				}
			} else {
				SootMethod receiver = ie.getMethod();
				List<String> args = new ArrayList<String>();
				List<String> rargs = new ArrayList<String>();
				for (int i = 0; i < ie.getArgCount(); i++) {
					Value arg = ie.getArg(i);
					if (arg instanceof Local && contains(((Local) arg).getName())) {
						Local rarg = receiver.getActiveBody().getParameterLocal(i);
						args.add(((Local) arg).getName()+arg.hashCode());
						rargs.add(rarg.getName()+rarg.hashCode());
					}
				}
				if (args.size() > 0) {
					for (int i = 0; i < args.size();i++)
						recordCallInfo(block.getBody().getMethod().getSignature(), receiver.getSignature(), index,
								args.get(i), rargs.get(i), s.hashCode());
					typegraph_list.transAll(s.hashCode(), -s.hashCode());
					typegraph_list.transAll(-s.hashCode(), succst.hashCode());
				} else
					typegraph_list.transAll(s.hashCode(), succst.hashCode());
			}
			return;
		}
		typegraph_list.transAll(s.hashCode(), succst.hashCode());
		return;
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
