package edu.zuo.setree.client;

import java.util.Iterator;
import java.util.Map;

import edu.zuo.setree.execution.Runner;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class IntraMain extends BodyTransformer {

	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		// TODO Auto-generated method stub
		Runner intra_executor = new Runner();
		intra_executor.run(b);
	}
	
	
	public static void main(String[] args) {
		//set options
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("tag", "off");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.setree", new IntraMain()));

		
		/**args should be in the following format: "-cp path_of_classes_analyzed class_names"
		 * e.g., -cp E:\Workspace\ws_program\taintanalysis\bin\ InterTest HelloWorld
		 */
		soot.Main.main(args);
	}

}
