package main;

public class Point {

	public Point parent;
	public int cost;
	public Coordinate coords;
	public Point(Point parent, Coordinate coords, int cost) {
		this.parent = parent;
		this.coords = coords;
		this.cost = cost;
	}
}