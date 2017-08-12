package main;

import java.util.ArrayList;

public class Floor {
	public ArrayList<ArrayList<Character>> floor;
	public ArrayList<Coordinate> stairs;
	public Coordinate sPoint;
	public Coordinate xPoint;
	public Floor() {
		stairs = new ArrayList<Coordinate>();
		floor = new ArrayList<ArrayList<Character>>();
	}
	public void add(ArrayList<Character> addition) {
		floor.add(addition);
	}
	public int size() {
		return floor.size();
	}
	public ArrayList<Character> get(int index) {
			return floor.get(index);
	}
}
