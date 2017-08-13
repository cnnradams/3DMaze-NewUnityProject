package main;

/**
 * 
 * Stores an X, Y, Z value in an object,
 * allowing for a custom hash code and equals
 * to speed up LinkedHashMap .contains() in the path finding
 * 
 * @author Stealth
 *
 */
public class Coordinate {

	public int x;
	public int y;
	public int z;
	
	public Coordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Creates a simple hash code so if x, y, z are equal they are put in a bucket for instant contains()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}
	
	/**
	 * If the coordinates are the same return true
	 */
	@Override
	public boolean equals(Object obj) {
		return ((Coordinate)obj).x == x && ((Coordinate)obj).y == y && ((Coordinate)obj).z == z;
	}
}
