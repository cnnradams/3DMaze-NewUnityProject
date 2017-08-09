package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

	public String fileName;

	public static void main(String[] args) {
		
		long in = System.currentTimeMillis();
		ArrayList<Floor> input = getInput("src/resources/in.txt");
		long inE = System.currentTimeMillis()-in;
		long pather = System.currentTimeMillis();
		ArrayList<Coordinate> path = pathFind(input);
		long patherE = System.currentTimeMillis()-pather;
		long out = System.currentTimeMillis();
		output("src/resources/out.txt", maze, path);
		long outE = System.currentTimeMillis()-out;
		
		System.out.println("Read Time: " + inE + "ms");
		System.out.println("Pathfind Time: " + patherE + "ms");
		System.out.println("Output: " + outE + "ms");
		System.out.println("   Replace Time: " + oSplit1 + "ms");
		System.out.println("   Write Time: " + oSplit2 + "ms");
		System.out.println("Total Time: " + (inE + patherE + outE) + "ms");
	}

	public static ArrayList<Floor> getInput(String fileName) {
		try {
			
			BufferedReader input = Files.newBufferedReader(Paths.get(fileName));
			ArrayList<Floor> layers = new ArrayList<Floor>();
			Floor level = new Floor();
			int yLevel = 0;
			String inputLine;
			while ((inputLine = input.readLine()) != null) {
				
				if (inputLine.length() != 0) {
					ArrayList<Character> charList = new ArrayList<Character>();
					for (int i = 0; i < inputLine.length(); i++) {
						Character c = inputLine.charAt(i);
						if (c == 'S' || c == 'z') {
							level.startPos = new Coordinate(i, yLevel);
						}
						if (c == 'X' || c == 'Z') {
							level.endPos = new Coordinate(i, yLevel);
						}
						charList.add(inputLine.charAt(i));
					}
					yLevel++;
					level.add(charList);
				} else {
					yLevel = 0;
					layers.add(level);
					level = new Floor();
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

	static ArrayList<Floor> maze;

	public static ArrayList<Coordinate> pathFind(ArrayList<Floor> mazer) {
		maze = mazer;
		ArrayList<Coordinate> finalPath = new ArrayList<Coordinate>();
		for (int z = 0; z < maze.size(); z++) {
			openList = new ArrayList<Point>();
			closedList = new ArrayList<Point>();
			Point startPoint = new Point(null, maze.get(z).startPos, 0);
			closedList.add(startPoint);
			TryAddOpens(startPoint, maze.get(z).endPos, z);

			boolean done = false;
			Point finalsq = null;
			while (!done) {
				Point least = null;
				for (Point p : openList) {
					if (least == null || p.cost < least.cost) {
						least = p;
					}
					if (p.coords.x == maze.get(z).endPos.x && p.coords.y == maze.get(z).endPos.y) {
						done = true;
						finalsq = p;
						break;
					}
				}
				openList.remove(least);
				closedList.add(least);
				TryAddOpens(least, maze.get(z).endPos, z);
				if ((least.coords.x == maze.get(z).endPos.x && least.coords.y == maze.get(z).endPos.y) || openList.size() == 0) {
					done = true;
					finalsq = least;
					if((least.coords.x != maze.get(z).endPos.x || least.coords.y != maze.get(z).endPos.y)) {
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
			
			for (int i = finalPathBackwards.size() - 1; i >= 0; i--) {
				finalPath.add(finalPathBackwards.get(i));
			}
			

		}
		return finalPath;
	}

	private static void TryAddOpens(Point p, Coordinate finish, int z) {
		if (maze.get(z).get(p.coords.y).get(p.coords.x + 1) != '#') {
			int cost = Math.abs(((p.coords.x + 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x + 1, p.coords.y, z), cost));
		}
		if (maze.get(z).get(p.coords.y).get(p.coords.x - 1) != '#') {
			int cost = Math.abs(((p.coords.x - 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x - 1, p.coords.y, z), cost));
		}
		if (maze.get(z).get(p.coords.y + 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y + 1) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x, p.coords.y + 1, z), cost));
		}
		if (maze.get(z).get(p.coords.y - 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y - 1) - finish.y));
			TryAddOpen(new Point(p, new Coordinate(p.coords.x, p.coords.y - 1, z), cost));
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
	static long oSplit1;
	static long oSplit2;
	public static void output(String file, ArrayList<Floor> map, ArrayList<Coordinate> path) {
		oSplit1 = System.currentTimeMillis();
		for(int i = 0; i < path.size(); i++) {
			if(map.get(path.get(i).z).get(path.get(i).y).get(path.get(i).x) == ' ') {
				map.get(path.get(i).z).get(path.get(i).y).set(path.get(i).x, 'P');
			}
		}
		oSplit1 = System.currentTimeMillis() - oSplit1;

		try {
		oSplit2 = System.currentTimeMillis();
		BufferedWriter out = Files.newBufferedWriter(Paths.get(file));
		for(int z = 0; z < map.size(); z++) {
			for(int y = 0; y < map.get(z).size(); y++) {
				for(int x = 0; x < map.get(z).get(y).size(); x++) {
					out.write(map.get(z).get(y).get(x));
				}
				out.newLine();
			}
			out.newLine();
		}
		
		out.close();
		oSplit2 = System.currentTimeMillis() - oSplit2;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
