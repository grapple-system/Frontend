package edu.zuo.typestate.datastructure;

public class Point {
	private String name;
	private String hashcode;

	public Point(String name, String hashcode) {
		this.name = name;
		this.hashcode = hashcode;
	}

	public String getName() {
		return this.name;
	}

	public String getHashcode() {
		return this.hashcode;
	}

	public void setHashcode(String hashcode) {
		this.hashcode = hashcode;
	}

	public String print() {
		return name + "_" + hashcode;
	}

	public boolean equalPoint(Point p) {
		if (name.equals(p.getName()) && (hashcode.equals(p.getHashcode())))
			return true;
		return false;
	}
}
