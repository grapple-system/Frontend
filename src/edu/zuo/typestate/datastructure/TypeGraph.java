package edu.zuo.typestate.datastructure;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.zuo.setree.client.IntraMain;
import edu.zuo.setree.execution.Runner;
import soot.SootMethod;

public class TypeGraph {
	public String classname;
	public String varname;
	private String[] allStates;
	private List<TransEdge> transedges = new ArrayList<TransEdge>();
	private Map<String, int[]> stmtDegree = new HashMap<String, int[]>();

	public TypeGraph(String classname, String varname) {
		TypeProtocol tp = IntraMain.typemap.get(classname);
		allStates = tp.allStates;
		this.classname = classname;
		this.varname = varname;
	}

	public String[] getStates() {
		return allStates;
	}

	public void addTypeGraph(TypeGraph add_tg) {
		this.transedges.addAll(add_tg.transedges);
		Map<String, int[]> addDegree = add_tg.getDegree();
		for (Map.Entry<String, int[]> entry : addDegree.entrySet()) {
			String key = entry.getKey();
			int[] value = entry.getValue();
			assert(value.length == 2);
			if (this.stmtDegree.containsKey(key)) {
				int[] myvalue = this.stmtDegree.get(key);
				assert(myvalue.length == 2);
				myvalue[0] = myvalue[0] + value[0];
				myvalue[1] = myvalue[1] + value[1];
				this.stmtDegree.put(key, myvalue);
			} else {
				this.stmtDegree.put(key, value);
			}
		}
	}

	public void addConstraintGraph(ConstraintGraph conGraph) {
		for (ConstraintEdge ce : conGraph.constraintedges) {
			TransEdge te = new TransEdge();
			te.addStart(ce.getStart());
			te.addEnd(ce.getEnd());
			transedges.add(te);
			doDegree(te.start.getHashcode(), te.end.getHashcode());
		}
	}

	public Map<String, int[]> getDegree() {
		return stmtDegree;
	}

	public void addTransEdge(TransEdge te) {
		if (!transedges.contains(te))
			transedges.add(te);
	}

	public List<TransEdge> getTransEdges() {
		return transedges;
	}

	// change degree of the points of edge
	private void doDegree(String starthash, String endhash) {
		int[] degree;
		if (stmtDegree.containsKey(starthash)) {
			degree = stmtDegree.get(starthash);
		} else {
			degree = new int[] { 0, 0 };
		}
		degree[1]++;
		stmtDegree.put(starthash, degree);
		if (stmtDegree.containsKey(endhash)) {
			degree = stmtDegree.get(endhash);
		} else {
			degree = new int[] { 0, 0 };
		}
		degree[0]++;
		stmtDegree.put(endhash, degree);
	}

	// transmit all states from start to end
	public void transAll(String starthash, String endhash) {
		for (String state : allStates) {
			TransEdge te = new TransEdge();
			te.addStart(new Point(state, starthash));
			te.addEnd(new Point(state, endhash));
			transedges.add(te);
			doDegree(starthash, endhash);
		}
	}

	// transmit for method
	public void transMethod(String method, String starthash, String endhash) {
		TypeProtocol tp = IntraMain.typemap.get(classname);
		List<String> trans = tp.statetrans.get(method);
		for (String tran : trans) {
			TransEdge te = new TransEdge();
			te.addStart(new Point(tran.split(",")[0], starthash));
			te.addEnd(new Point(tran.split(",")[1], endhash));
			transedges.add(te);
			doDegree(starthash, endhash);
		}
	}

	// special for constraint
	public void transAllCon(String starthash, String endhash) {
		// Runner.constraint_graph_list.clearTemp();
		for (String state : allStates) {
			String[] te = new String[] { state, state };
			Runner.constraint_graph_list.addTemp(varname, te);
		}
	}

	// special for constraint
	public void transMethodCon(String method, String starthash, String endhash) {
		// Runner.constraint_graph_list.clearTemp();
		TypeProtocol tp = IntraMain.typemap.get(classname);
		List<String> trans = tp.statetrans.get(method);
		for (String tran : trans) {
			String[] te = new String[2];
			te[0] = tran.split(",")[0];
			te[1] = tran.split(",")[1];
			Runner.constraint_graph_list.addTemp(varname, te);
		}
	}

	private void deduplication() {
		for (int i = 0; i < transedges.size(); i++) {
			for (int j = transedges.size() - 1; j > i; j--) {
				TransEdge e1 = transedges.get(i);
				TransEdge e2 = transedges.get(j);
				if (e1.start.equalPoint(e2.start) && e1.end.equalPoint(e2.end)) {
					try {
						int[] degree = stmtDegree.get(e1.start.getHashcode());
						degree[1]--;
						stmtDegree.put(e1.start.getHashcode(), degree);
						degree = stmtDegree.get(e1.end.getHashcode());
						degree[0]--;
						stmtDegree.put(e1.end.getHashcode(), degree);
						transedges.remove(j);
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void simplifyGraph() {
		int stateNum = allStates.length;
		if (transedges.size() <= stateNum) {
			return;
		}
		TransEdge[] now = new TransEdge[stateNum];
		TransEdge[] next = new TransEdge[stateNum];
		// Iterator<TransEdge> it = transedges.iterator();
		for (int i = 0; i < stateNum; i++) {
			now[i] = transedges.get(i);
		}
		for (int i = stateNum; i < transedges.size();) {
			for (int j = 0; j < stateNum; j++) {
				next[j] = transedges.get(i + j);
			}
			int equ = 0;
			for (; equ < stateNum; equ++) {
				if (!edgeDirect(now[equ], next[equ])) {
					break;
				}
			}
			if (equ == stateNum && singlepoint(now[0].end.getHashcode())) {
				for (int j = 0; j < stateNum; j++) {
					now[j].end.setHashcode(next[j].end.getHashcode());
					try {
						int[] degree = stmtDegree.get(next[j].start.getHashcode());
						degree[1]--;
						stmtDegree.put(next[j].start.getHashcode(), degree);
						degree = stmtDegree.get(next[j].end.getHashcode());
						degree[0]--;
						stmtDegree.put(next[j].end.getHashcode(), degree);
						transedges.remove(next[j]);
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			} else {
				for (int j = 0; j < stateNum; j++) {
					now[j] = next[j];
				}
				i = i + stateNum;
			}
		}
		deduplication();
	}

	private boolean singlepoint(String hashcode) {
		int stateNum = allStates.length;
		int[] degree = stmtDegree.get(hashcode);
		if (degree != null) {
			if (degree[0] <= stateNum && degree[1] <= stateNum)
				return true;
		}
		return false;
	}

	public boolean edgeDirect(TransEdge first, TransEdge sec) {
		if (!first.start.getName().equals(first.end.getName()))
			return false;
		if (!sec.start.getName().equals(sec.end.getName()))
			return false;
		if (!(first.end.equalPoint(sec.start)))
			return false;
		return true;
	}

	public Set<String> getVars(int index) {
		Set<String> vars = new HashSet<String>();
		for (TransEdge te : transedges) {
			vars.add(index+"."+te.start.print());
			vars.add(index+"."+te.end.print());
		}
		return vars;
	}

	public void printDot(String file_path) {
		simplifyGraph();
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		// file_path = file_path + sm.getDeclaringClass().getName() + "_" +
		// sm.getName()+".txt";
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("digraph " + file_path.substring(0, file_path.lastIndexOf('.')) + "{\r\n");
			for (TransEdge transedge : transedges) {
				// if
				// (!transedge.start.getName().equals(transedge.end.getName()))
				// {
				// fileWriter.write(transedge.start.getName() + "_" +
				// transedge.start.getHashcode() + " -> "
				// + transedge.start.getName() + "_" +
				// transedge.end.getHashcode() + "[color=\"white\"]"
				// + " \r\n");
				// }
				fileWriter.write(transedge.printDot());
			}
			fileWriter.write("} \r\n");
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
