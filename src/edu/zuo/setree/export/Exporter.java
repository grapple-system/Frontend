package edu.zuo.setree.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.zuo.setree.datastructure.StateNode;
import soot.Body;

public class Exporter {
	
	public static final File outFile = new File("set.conditional");
	
	public static void run(StateNode root, Body mb) {
		//for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 1);
		System.out.println("\n");
		
		//export symbolic execution tree info to output file
		System.out.println("Exporting...");
		export(root, mb);
		System.out.println("Finish exporting!!!");
		
	}
	
	
	private static void export(StateNode root, Body mb) {
		PrintWriter out = null;
		try {
			if(!outFile.exists()) {
				outFile.createNewFile();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));
			
			out.println(mb.getMethod().getSignature());
			recursiveExport(root, 1, out);
			out.println();
			
			out.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static void recursiveExport(StateNode root, int index, PrintWriter out) {
		//termination
		if(root == null) {
			return;
		}
		
		//export operation
		if(root.getConditional() != null) {
			out.println(index + ":" + root.getConditional().toString());
		}
		
		//recursive operation 
		recursiveExport(root.getTrueChild(), 2 * index, out);
		recursiveExport(root.getFalseChild(), 2 * index + 1, out);
	}

	

	/** print out state information
	 * @param root
	 * @param id
	 */
	private static void printOutInfo(StateNode root, int id) {
		// TODO Auto-generated method stub
		if(root == null){
			return;
		}
		System.out.println(id + ": " + root.toString());
		printOutInfo(root.getTrueChild(), 2 * id);
		printOutInfo(root.getFalseChild(), 2 * id + 1);
	}

}
