package main;

/**
 * 
 * Object used by the 3D pathfinding, 
 * storing a position where to move there costs 'cost', 
 * along with the parent's cost
 * 
 * @author Stealth
 *
 */
public class Point {

	public Point parent;
	public int cost;
	public Coordinate coords;
	public Point(Point parent, Coordinate coords, int cost) {
		this.parent = parent;
		this.coords = coords;
		this.cost = cost;
	}

	/**
	 * Each Coordinate should make the Point unique, 
	 * therefore we can use Coordinate hash code
	 */
	@Override
	public int hashCode() {
		return coords.hashCode();
	}
	
	/**
	 * We can also use Coordinate's equals,
	 * since the coordinates is what defines the point.
	 */
	@Override
	public boolean equals(Object obj) {
		return coords.equals(((Point)obj).coords);
	}
}