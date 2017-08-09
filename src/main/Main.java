package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public String fileName;

	public static void main(String[] args) {
		
		ArrayList<ArrayList<ArrayList<Character>>> input = getInput("src/resources/in.txt");
		long start = (long)System.currentTimeMillis();
		
		pathFind(input);
		long elapsed =(long)System.currentTimeMillis()-(long)start;
		System.out.println(elapsed);
		
		
	}

	public static ArrayList<ArrayList<ArrayList<Character>>> getInput(String fileName) {
		try {
			
			BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
			ArrayList<ArrayList<ArrayList<Character>>> layers = new ArrayList<ArrayList<ArrayList<Character>>>();
			ArrayList<ArrayList<Character>> level = new ArrayList<ArrayList<Character>>();
			String inputLine;
			while ((inputLine = input.readLine()) != null) {
				if (inputLine.length() != 0) {
					ArrayList<Character> charList = new ArrayList<Character>();
					for (int i = 0; i < inputLine.length(); i++) {
						charList.add(inputLine.charAt(i));
					}
					level.add(charList);
				} else {
					layers.add(level);
					level = new ArrayList<ArrayList<Character>>();
				}
			}
			layers.add(level);
			
			input.close();
			
			
			
			return layers;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static ArrayList<Point> openList;
	static ArrayList<Point> closedList;

	static ArrayList<ArrayList<ArrayList<Character>>> maze;

	public static String pathFind(ArrayList<ArrayList<ArrayList<Character>>> mazer) {
		maze = mazer;
		for (int z = 0; z < maze.size(); z++) {
			System.out.println("Layer " + z);
			Coordinate startPos = null;
			Coordinate endPos = null;
			for (int y = 0; y < maze.get(z).size(); y++) {
				for (int x = 0; x < maze.get(z).get(y).size(); x++) {
					if (maze.get(z).get(y).get(x) == 'S' || maze.get(z).get(y).get(x) == 'z') {
						startPos = new Coordinate(x, y);
					}
					if (maze.get(z).get(y).get(x) == 'X' || maze.get(z).get(y).get(x) == 'Z') {
						endPos = new Coordinate(x, y);
					}
				}
			}
			System.out.println("Start: " + startPos.x + "," + startPos.y);
			openList = new ArrayList<Point>();
			closedList = new ArrayList<Point>();
			Point startPoint = new Point(null, startPos, 0);
			closedList.add(startPoint);
			TryAddOpens(startPoint, endPos, z);

			boolean done = false;
			Point finalsq = null;
			while (!done) {
				Point least = null;
				for (Point p : openList) {
					if (least == null || p.cost < least.cost) {
						least = p;
					}
					if (p.coords.x == endPos.x && p.coords.y == endPos.y) {
						done = true;
						finalsq = p;
						break;
					}
				}
				openList.remove(least);
				closedList.add(least);
				TryAddOpens(least, endPos, z);
				if ((least.coords.x == endPos.x && least.coords.y == endPos.y) || openList.size() == 0) {
					done = true;
					finalsq = least;
					if((least.coords.x != endPos.x || least.coords.y != endPos.y)) {
						System.out.println("No Path!");
						return null;
					}
				}
			}
			ArrayList<Coordinate> finalPathBackwards = new ArrayList<Coordinate>();
			finalPathBackwards.add(finalsq.coords);
			boolean donePath = false;
			while (!donePath) {
				finalsq = finalsq.parent;
				if (finalsq != null) {
					finalPathBackwards.add(finalsq.coords);
				} else {
					donePath = true;
				}
			}
			ArrayList<Coordinate> finalPath = new ArrayList<Coordinate>();
			for (int i = finalPathBackwards.size() - 1; i >= 0; i--) {
				finalPath.add(finalPathBackwards.get(i));
			}

			for (Coordinate c : finalPath) {
				System.out.println(c.x + "," + c.y);
			}

		}
		return null;
	}

	private static void TryAddOpens(Point p, Coordinate finish, int z) {
		if (maze.get(z).get(p.coords.y).get(p.coords.x + 1) != '#') {
			int cost = Math.abs(((p.coords.x + 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x + 1, p.coords.y), cost));
		}
		if (maze.get(z).get(p.coords.y).get(p.coords.x - 1) != '#') {
			int cost = Math.abs(((p.coords.x - 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x - 1, p.coords.y), cost));
		}
		if (maze.get(z).get(p.coords.y + 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y + 1) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x, p.coords.y + 1), cost));
		}
		if (maze.get(z).get(p.coords.y - 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y - 1) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x, p.coords.y - 1), cost));
		}
	}

	private static void TryAddOpen(Point p) {
		for (Point g : closedList) {
			if (p.coords.x == g.coords.x && p.coords.y == g.coords.y) {
				return;
			}
		}
		boolean onOpenList = false;
		for (Point g : openList) {
			if (g.coords.x == p.coords.x && g.coords.y == p.coords.y) {
				if (g.cost > p.cost) {
					openList.remove(g);
					break;
				} else {
					onOpenList = true;
					break;
				}
			}
		}
		if (!onOpenList) {
			openList.add(p);
		}
	}
}
