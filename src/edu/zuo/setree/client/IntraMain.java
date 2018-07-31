package edu.zuo.setree.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.zuo.setree.execution.Runner;
import edu.zuo.typestate.TSCGenerator;
import edu.zuo.typestate.datastructure.TypeProtocol;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

public class IntraMain extends BodyTransformer {
	public static Map<String, TypeProtocol> typemap = new HashMap<String, TypeProtocol>();
	public static List<String> allClass = new ArrayList<String>();
	public static List<String> InterestMethod = new ArrayList<String>();

	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		// TODO Auto-generated method stub
		Runner intra_executor = new Runner();
		intra_executor.run(b);
	}

	public static void init(String dirpath) {
		File dirFile = new File(dirpath);
		if (!dirFile.isDirectory()) {
			return;
		}
		String[] fileList = dirFile.list();
		for (int i = 0; i < fileList.length; i++) {
			// ±éÀúÎÄ¼þÄ¿Â¼
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(new FileInputStream(new File(dirpath + "/" + fileList[i])), "UTF-8"));
				TypeProtocol tp = new TypeProtocol();
				String line = br.readLine();
				tp.classname = line;
				allClass.add(line);
				line = br.readLine();
				tp.allStates = line.split(" ");
				while ((line = br.readLine()) != null) {
					String[] sp = line.split("@");
					tp.addTrans(sp[0], sp[1]);
					if (!InterestMethod.contains(sp[0]))
						InterestMethod.add(sp[0]);
				}
				typemap.put(tp.classname, tp);
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		String sp = "lib/";
		String dirpath = "protocal/";
		init(dirpath);
		// set the sootclass path
		File dirFile = new File(sp);
		String path = "";
		if (!dirFile.isDirectory()) {
			return;
		}
		String[] fileList = dirFile.list();
		for (int i = 0; i < fileList.length; i++) {
			// ±éÀúÎÄ¼þÄ¿Â¼
			String string = fileList[i];
			if (string.endsWith(".jar")) {
				path += sp + string + ";";
			}
		}
		path += sp + "test;";
		Scene.v().setSootClassPath(path);
		// set options
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("tag", "off");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		PackManager.v().getPack("jtp").add(new Transform("jtp.setree", new IntraMain()));

		/**
		 * args should be in the following format:
		 * "-cp path_of_classes_analyzed class_names" e.g., -cp
		 * E:\Workspace\ws_program\taintanalysis\bin\ InterTest HelloWorld
		 */
		//synchronized (IntraMain.class) {
			soot.Main.main(args);
			TSCGenerator.printCallInfo("intraOutput/callInfoFile");
			TSCGenerator.printCalledVar("intraOutput/calledVarFile");
		//}
	}

}
