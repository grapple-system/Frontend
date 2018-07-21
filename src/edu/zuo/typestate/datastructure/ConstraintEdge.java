package edu.zuo.typestate.datastructure;

public class ConstraintEdge {
	private Point end;
	private Point start;
	private int startnode;
	private int endnode;
	
	public ConstraintEdge(String[] tempedge, String starthash, String endhash, int startnode, int endnode){
		assert(tempedge.length == 2);
		start = new Point(tempedge[0], starthash);
		end = new Point(tempedge[1], endhash);
		this.startnode = startnode;
		this.endnode = endnode;
	}
	//print edge
	public String printDot(){
		String str = start.getName()+"_"+start.getHashcode()+" "+end.getName()+"_"+end.getHashcode()+" ["+startnode+", ]"+endnode+"\r\n";
		return str;
	}
	
	public Point getStart(){
		return start;
	}
	
	public Point getEnd(){
		return end;
	}
	//return start point string: var_hashcode
	public String getStartStr(){
		return startnode+"."+start.getName()+"_"+start.getHashcode();
	}
	//return end point string
	public String getEndStr(){
		return endnode+"."+end.getName()+"_"+end.getHashcode();
	}
	//return start node index
	public int getStartNode(){
		return startnode;
	}
	//return end node index
	public int getEndNode(){
		return endnode;
	}
}
