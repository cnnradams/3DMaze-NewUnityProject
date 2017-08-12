package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Main {

	public String fileName;

	public static boolean QUICK_OUTPUT = false;

	public static void main(String[] args) {
		long in = System.currentTimeMillis();
		ArrayList<Floor> input = getInput("in.txt");
		long inE = System.currentTimeMillis() - in;
		long pather = System.currentTimeMillis();
		ArrayList<Coordinate> path = pathFind(input);
		long patherE = System.currentTimeMillis() - pather;
		long out = System.currentTimeMillis();
		if (QUICK_OUTPUT)
			quickOutput("out.txt", path);
		else
			output("out.txt", maze, path);
		long outE = System.currentTimeMillis() - out;
		System.out.println("Read Time: " + inE + "ms");
		System.out.println("Pathfind Time: " + patherE + "ms");
		System.out.println("Output Time: " + outE + "ms");
		System.out.println("Total Time: " + ((inE + patherE + outE)) + "ms");
		System.out.println("Thread Time: " + (Main.inE / 1000000) + "ms " + (Main.inE2 / 1000000) + "ms");
	}
	
	static int startLevel;

	public static ArrayList<Floor> getInput(String fileName) {
		try {
			boolean previous = false;
			int zLevel = 0;
			BufferedReader input = Files.newBufferedReader(Paths.get(fileName));
			ArrayList<Floor> layers = new ArrayList<Floor>();
			Floor level = new Floor();
			int yLevel = 0;
			String inputLine;
			while ((inputLine = input.readLine()) != null) {

				if (inputLine.length() != 0) {
					previous = false;
					ArrayList<Character> charList = new ArrayList<Character>();
					for (int i = 0; i < inputLine.length(); i++) {
						Character c = inputLine.charAt(i);
						if (c == 'S') {
							level.sPoint = new Coordinate(i, yLevel);
							startLevel = zLevel;
						}
						if (c == 'X') {
							level.xPoint = new Coordinate(i, yLevel);
						}
						if (c == 'z' || c == 'Z') {
							level.stairs.add(new Coordinate(i, yLevel));
						}
						charList.add(inputLine.charAt(i));
					}
					yLevel++;
					level.add(charList);
				} else if(previous = true) {
					continue;
				} else {
					previous = true;
					yLevel = 0;
					zLevel++;
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

	static volatile ArrayList<Floor> maze;
	static volatile ArrayList<Coordinate> finalPath;

	public static ArrayList<Coordinate> pathFind(ArrayList<Floor> mazer) {

		maze = mazer;
		finalPath = new ArrayList<Coordinate>();

		DelegateFloors(startLevel, mazer.get(startLevel).sPoint, new ArrayList<Coordinate>());

		return finalPath;
	}

	public static void DelegateFloors(int z, Coordinate startPos, ArrayList<Coordinate> pastPath) {
		ArrayList<Thread> threads = new ArrayList<>(maze.size());

		for (int i = 0; i < maze.get(z).stairs.size(); i++) {
			if (!maze.get(z).stairs.get(i).equals(startPos)) {
				threads.add(new Thread(new FloorSolver(z, startPos, maze.get(z).stairs.get(i), pastPath)));
				threads.get(i).start();
			}
		}
		boolean done = false;
		while (!done) {
			done = true;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					done = false;
				} else {
					ArrayList<Coordinate> thisResult = result.get(thread);
					if (thisResult != null) {
						Coordinate endPoint = thisResult.get(thisResult.size() - 1);
						if (maze.get(z).get(endPoint.y).get(endPoint.x) == 'z') {
							DelegateFloors(z - 1, endPoint, thisResult);
						} else if (maze.get(z).get(endPoint.y).get(endPoint.x) == 'Z') {
							DelegateFloors(z + 1, endPoint, thisResult);
						} else if (maze.get(z).get(endPoint.y).get(endPoint.x) == 'X') {
							finalPath = thisResult;
							return;
						}
					}
				}
			}
		}
	}

	static HashMap<Thread, ArrayList<Coordinate>> result = new HashMap<>();

	public static class FloorSolver implements Runnable {

		ArrayList<Point> openList;
		LinkedHashSet<Point> closedList;
		ArrayList<Coordinate> pastPath;
		private final int z;
		private final Coordinate startPos;
		private final Coordinate endPos;

		public FloorSolver(int z, Coordinate startPos, Coordinate endPos, ArrayList<Coordinate> pastPath) {
			this.z = z;
			this.startPos = startPos;
			this.endPos = endPos;
			this.pastPath = pastPath;
		}

		@Override
		public void run() {
			openList = new ArrayList<Point>();
			closedList = new LinkedHashSet<Point>();
			Point startPoint = new Point(null, startPos, 0);
			closedList.add(startPoint);
			TryAddOpens(startPoint, closedList, openList, endPos, z);

			boolean done = false;
			Point finalsq = null;
			while (!done) {
				long in = System.nanoTime();
				Point least = null;
				for (Point p : openList) {
					if (least == null || p.cost < least.cost) {
						least = p;
					}
					if (p.coords == endPos) {
						done = true;
						finalsq = p;
						break;
					}
				}
				inE += System.nanoTime() - in;
				openList.remove(least);
				closedList.add(least);
				TryAddOpens(least, closedList, openList, endPos, z);
				long in2 = System.nanoTime();
				if ((least.coords.x == endPos.x && least.coords.y == endPos.y) || openList.size() == 0) {
					done = true;
					finalsq = least;
					if ((least.coords.x != endPos.x || least.coords.y != endPos.y)) {
						System.out.println("No Path!");
						result.put(Thread.currentThread(), null);
						return;
					}
				}
				inE2 += System.nanoTime() - in2;
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
			ArrayList<Coordinate> thisPath = new ArrayList<Coordinate>();
			thisPath.addAll(pastPath);
			for (int i = finalPathBackwards.size() - 1; i >= 0; i--) {
				thisPath.add(finalPathBackwards.get(i));
			}
			result.put(Thread.currentThread(), thisPath);
		}
	}

	private static void TryAddOpens(Point p, LinkedHashSet<Point> closedList, ArrayList<Point> openList,
			Coordinate finish, int z) {
		System.out.println(maze.get(z).floor);
		//System.out.println(maze.get(z).get(p.coords.y).contains(p.coords.x + 1));
		if (maze.get(z).get(p.coords.y).contains(p.coords.x + 1)) {
			if (maze.get(z).get(p.coords.y).get(p.coords.x + 1) == ' '
					|| maze.get(z).get(p.coords.y).get(p.coords.x + 1) == 'z'
					|| maze.get(z).get(p.coords.y).get(p.coords.x + 1) == 'Z'
					|| maze.get(z).get(p.coords.y).get(p.coords.x + 1) == 'S'
					|| maze.get(z).get(p.coords.y).get(p.coords.x + 1) == 'X') {
				int cost = Math.abs(((p.coords.x + 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
				TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x + 1, p.coords.y, z), cost));
			}
		}
		if (maze.get(z).get(p.coords.y).contains(p.coords.x - 1)) {
			if (maze.get(z).get(p.coords.y).get(p.coords.x - 1) == ' '
					|| maze.get(z).get(p.coords.y).get(p.coords.x - 1) == 'z'
					|| maze.get(z).get(p.coords.y).get(p.coords.x - 1) == 'Z'
					|| maze.get(z).get(p.coords.y).get(p.coords.x - 1) == 'S'
					|| maze.get(z).get(p.coords.y).get(p.coords.x - 1) == 'X') {
				int cost = Math.abs(((p.coords.x - 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
				TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x - 1, p.coords.y, z), cost));
			}
		}
		if (maze.get(z).floor.contains(p.coords.y + 1)) {
			if (maze.get(z).get(p.coords.y + 1).get(p.coords.x) == ' '
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'z'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'Z'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'S'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'X') {
				int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y + 1) - finish.y));
				TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x, p.coords.y + 1, z), cost));
			}
		}
		if (maze.get(z).floor.contains(p.coords.y - 1)) {
			if (maze.get(z).get(p.coords.y - 1).get(p.coords.x) == ' '
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'z'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'Z'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'S'
					|| maze.get(z).get(p.coords.y + 1).get(p.coords.x) == 'X') {
				int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y - 1) - finish.y));
				TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x, p.coords.y - 1, z), cost));
			}
		}
	}

	static long inE = 0;
	static long inE2 = 0;

	private static void TryAddOpen(LinkedHashSet<Point> closedList, ArrayList<Point> openList, Point p) {

		if (closedList.contains(p)) {
			return;
		}

		boolean onOpenList = false;
		int i = openList.indexOf(p);
		if (i != -1) {
			Point g = openList.get(i);
			if (g.cost > p.cost) {
				openList.remove(i);
			} else {
				onOpenList = true;
			}
		}
		if (!onOpenList) {
			openList.add(p);
		}

	}

	public static void output(String file, ArrayList<Floor> map, ArrayList<Coordinate> path) {
		for (int i = 0; i < path.size(); i++) {
			if (map.get(path.get(i).z).get(path.get(i).y).get(path.get(i).x) == ' ') {
				map.get(path.get(i).z).get(path.get(i).y).set(path.get(i).x, 'P');
			}
		}

		try {

			byte[] bytes = new byte[map.get(0).get(0).size() * map.get(0).size() * map.size() + map.size()
					+ map.size() * map.get(0).size()];

			int count = 0;

			for (int z = 0; z < map.size(); z++) {
				for (int y = 0; y < map.get(z).size(); y++) {
					for (int x = 0; x < map.get(z).get(y).size(); x++) {
						bytes[count++] = (byte) map.get(z).get(y).get(x).charValue();
					}
					bytes[count++] = (byte) '\n';
				}
				bytes[count++] = (byte) '\n';
			}

			Files.write(Paths.get(file), bytes);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void quickOutput(String file, ArrayList<Coordinate> path) {
		byte[] bytes = new byte[path.size()];
		int previousX = path.get(0).x;
		int previousY = path.get(0).y;
		boolean newL = false;
		bytes[0] = 'S';
		for (int i = 1; i < path.size(); i++) {
			if (path.get(i).x == -1 && path.get(i).y == -1) {
				bytes[i] = '\n';
				previousX = path.get(i).x;
				previousY = path.get(i).y;
				newL = true;
				continue;
			}
			if (newL) {
				bytes[i] = 'Z';
				newL = false;
				continue;
			}

			if (previousY > path.get(i).y)
				bytes[i] = 'u';
			else if (previousY < path.get(i).y)
				bytes[i] = 'd';
			else if (previousX > path.get(i).x)
				bytes[i] = 'l';
			else if (previousX < path.get(i).x)
				bytes[i] = 'r';
			previousX = path.get(i).x;
			previousY = path.get(i).y;
		}
		try {
			Files.write(Paths.get(file), bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
