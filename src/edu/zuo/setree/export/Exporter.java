package edu.zuo.setree.export;

import edu.zuo.setree.datastructure.StateNode;

public class Exporter {
	
	public static void export(StateNode root) {
		//for debugging
		System.out.println("STATE ==>>");
		printOutInfo(root, 1);
		System.out.println("\n");
		
		//
		recursiveExport(root, 1);
		
		
	}
	
	
	
	
	private static void recursiveExport(StateNode root, int index) {
		//termination
		if(root == null) {
			return;
		}
		
		//export operation
		//TODO
		
		
		//recursive operation 
		recursiveExport(root.getTrueChild(), 2 * index);
		recursiveExport(root.getFalseChild(), 2 * index + 1);
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
