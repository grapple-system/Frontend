package edu.zuo.typestate.datastructure;

public class TransEdge {
	public Point end;
	public Point start;
	
	public void addEnd(Point base) {
		// TODO Auto-generated method stub
		this.end = base;
	}
	
	public void addStart(Point arg) {
		// TODO Auto-generated method stub
		this.start = new Point(arg.getName(), arg.getHashcode());
	}
	
	public String callStr(){
		return start.getName()+"("+start.getHashcode()+")";
	}
	
	public String receiveStr(){
		return end.getName()+"("+end.getHashcode()+")";
	}
	
	public String printDot(){
		String str = start.getName()+"_"+start.getHashcode()+" -> "+end.getName()+"_"+end.getHashcode()+"\r\n";
		return str;
	}
}
