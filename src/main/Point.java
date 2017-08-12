package main;

import java.util.ArrayList;

public class Point {

	public Point parent;
	public int cost;
	public Coordinate coords;
	private boolean inUse;
	private int index;
	public Point(Point parent, Coordinate coords, int cost) {
		this.parent = parent;
		this.coords = coords;
		this.cost = cost;
	}
	public Point reset(Point parent, Coordinate coords, int cost) {
		this.parent = parent;
		this.coords = coords;
		this.cost = cost;
		return this;
	}
	private static ArrayList<Point> points = new ArrayList<Point>();
	public static Point getPoint(Point parent, Coordinate coords, int cost) {
		for(int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if(!p.inUse) {
				p.inUse = true;
				p.index = i;
				p.parent = parent;
				p.coords = coords;
				p.cost = cost;
				return p;
			}
		}
		Point newP = new Point(parent,coords,cost);
		newP.inUse = true;
		points.add(newP);
		return newP;
	}
	@Override
	public String toString() {
		return coords.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + coords.x;
		result = prime * result + coords.y;
		result = prime * result + coords.z;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		return coords.equals(((Point)obj).coords);
	}
}