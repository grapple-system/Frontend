package edu.zuo.setree.client;

import java.util.Map;

import edu.zuo.setree.execution.Executor;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class IntraMain extends BodyTransformer {

	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		// TODO Auto-generated method stub
		Executor intra_executor = new Executor();
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
