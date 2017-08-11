package main;

import java.util.Arrays;

public class Coordinate {

	public int x;
	public int y;
	public int z;
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Coordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		return ((Coordinate)obj).x == x && ((Coordinate)obj).y == y;
	}
}
