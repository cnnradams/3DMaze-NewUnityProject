package main;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Combination of HashSet and PriorityQueue for
 * getting the cheapest node in O(1) instead of O(n)
 * but also contains() in O(1) instead of O(n)
 * 
 * @author Stealth
 *
 */
public class OpenList {

	// The lists to store
	public PriorityQueue<Point> priority;
	public HashSet<Point> set;
	
	public OpenList() {
		priority = new PriorityQueue<Point>(new CostComparator());
		set = new HashSet<Point>();
	}
	
	// The functions used by the pathfinding are here but doubled for both the PriorityQueue and HashSet
	public void add(Point p) {
		priority.add(p);
		set.add(p);
	}
	public boolean contains(Point p) {
		return set.contains(p);
	}
	
	public Point remove() {
		Point p = priority.remove();
		set.remove(p);
		return p;
	}
	
	public int size() {
		return priority.size();
	}

	// Sorts the PriorityQueue by the cheapest node
	public class CostComparator implements Comparator<Point>
	{
		@Override
		public int compare(Point o1, Point o2) {
			return o1.cost - o2.cost;
		}
	}
}
