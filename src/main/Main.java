package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * 
 * @author NewUnityProject Team
 *
 * Main class for 3D maze pathfinding, 
 * including timings and file input/output
 * Created using A* but in three dimensions
 */
public class Main {

	// Root folder outputs so it works in a JAR
	public static final String INPUT_FILE_NAME = "in.txt";
	public static final String OUTPUT_FILE_NAME = "out.txt";

	// TODO: Make the quickOutput function what the judges want to see
	public static final boolean QUICK_OUTPUT = true;

	public static void main(String[] args) {

		long inputTimingStart = System.currentTimeMillis();
		
		// Gets input as a 3 dimensional array of chars
		ArrayList<ArrayList<ArrayList<Character>>> input = getInput(INPUT_FILE_NAME);
		
		long inputTimingEnd = System.currentTimeMillis() - inputTimingStart;

		new LinkedHashSet<Integer>().contains(1);
		long pathfindTimingStart = System.currentTimeMillis();
		
		// Works with the input to create a list of coordinates you would need to travel to to pathfind
		ArrayList<Coordinate> path = pathFind(input);
		
		long pathfindTimingEnd = System.currentTimeMillis() - pathfindTimingStart;
		
		
		long outputTimingStart = System.currentTimeMillis();
		
		// Works with the coordinate path to quickly place ups, rights, lefts, and downs into the file
		if (QUICK_OUTPUT)
			quickOutput(OUTPUT_FILE_NAME, path);
		
		// Prints the whole maze but with each coordinate in the path turned into a P
		else
			output(OUTPUT_FILE_NAME, input, path);
		
		long outputTimingEnd = System.currentTimeMillis() - outputTimingStart;

		System.out.println("Input read from " + INPUT_FILE_NAME + ", Output written to " + OUTPUT_FILE_NAME + ".\n");
		
		// Prints time taken for each step and total in miliseconds
		System.out.println("Read Time: " + inputTimingEnd + "ms");
		System.out.println("Pathfind Time: " + pathfindTimingEnd + "ms");
		System.out.println("Output Time: " + outputTimingEnd + "ms");
		System.out.println("Total Time: " + ((inputTimingEnd + pathfindTimingEnd + outputTimingEnd)) + "ms");
	}

	// Global starting and ending position of the mazer (S and X)
	static Coordinate startPos;
	static Coordinate endPos;

	/**
	 * Gets the maze as input from a .txt file
	 * 
	 * @param fileName Name of the file to pull from
	 * @return A 3 Dimensional array of each character in the file
	 */
	public static ArrayList<ArrayList<ArrayList<Character>>> getInput(String fileName) {
		
			try {
			
			// Opens the file in a reader so we can get the input
			BufferedReader input = Files.newBufferedReader(Paths.get(fileName));
			
			// Boolean used to skip double whitespace inbetween floors so the program 
			// doesn't think the second is a new floor when its really just blank 
			boolean previous = false;
			
			// Stores the current floor being read, increased whenever the program encounters an empty line
			int zLevel = 0;
			
			// Storage for the whole maze
			ArrayList<ArrayList<ArrayList<Character>>> layers = new ArrayList<ArrayList<ArrayList<Character>>>();
			
			// Storage for the singular floor, eventually added to the whole maze
			ArrayList<ArrayList<Character>> level = new ArrayList<ArrayList<Character>>();
			
			// The current Y level, increased after every line but reset when zLevel increases
			int yLevel = 0;
			
			// The line input from in.txt
			String inputLine;
			
				// Works through every single line until there are none left
				while ((inputLine = input.readLine()) != null) {

					// If there is content in the line
					if (inputLine.length() != 0) {
						
						// Since it's not whitespace, we can accept the next whitespace as a new floor
						previous = false;
						
						// Annoyingly enough you can't convert a String to an ArrayList<Character>,
						// so we have to do it manually (you can in java 8 but I dont like java 8 stuff)
						ArrayList<Character> charList = new ArrayList<Character>();
						
						// Loops through every char in the String
						for (int i = 0; i < inputLine.length(); i++) {
							Character c = inputLine.charAt(i);
							
							// If it's S or X you now know where the start and end of the maze is
							if (c == 'S')
								startPos = new Coordinate(i, yLevel, zLevel);
							else if (c == 'X') 
								endPos = new Coordinate(i, yLevel, zLevel);
							
							charList.add(inputLine.charAt(i));
						}
						
						// Add the row to the floor and then get ready for the next row
						level.add(charList);
						yLevel++;
					} else if (previous == true) {
						// Ignore the double blank row
						continue;
					} else {
						// Prepare for double blank row
						previous = true;
						
						// Reset y, get a new floor
						yLevel = 0;
						zLevel++;
						
						// Add the floor to the full array
						layers.add(level);
						level = new ArrayList<ArrayList<Character>>();
					}
				}
			
			// Add the last floor
			layers.add(level);

			input.close();

			return layers;
			
			} catch(IOException io) {
				System.out.println("Getting input from " + fileName + " failed: ");
				io.printStackTrace();
				System.exit(0);
			}

		// If something ever gets here I will quit programming forever
		return null;
	}
	static long inE2 = 0;
	/**
	 * This is where the pathfinding really happens (A*)
	 * 
	 * @param maze the 3d map
	 * @return the path to take
	 */
	public static ArrayList<Coordinate> pathFind(ArrayList<ArrayList<ArrayList<Character>>> maze) {
		
		// Open list to store potential paths
		OpenList openList = new OpenList();
		
		// Closed list to store actual path
		LinkedHashSet<Point> closedList = new LinkedHashSet<Point>();
		
		// Begins with the starting point that has no parent
		Point startPoint = new Point(null, startPos, 0);
		
		// Tiles we've checked around them for paths
		closedList.add(startPoint);
		
		// Tries to add its 6 neighbouring tiles to the open list for further pathfinding
		TryAddOpens(startPoint, closedList, openList, endPos, maze);

		// The final point
		Point finalP = null;
		
		// Loops until broken
		while (true) {
			
			// Its no longer a potential tile, it's now discovered
			 Point least = openList.remove();
			closedList.add(least);
			
			// If it made it to the target
			if (least.coords.equals(endPos)) {
				// We made it so stop looping now
				finalP = least;
				break;
			}
			
			// Sees where this cheap tile will lead
			TryAddOpens(least, closedList, openList, endPos, maze);
			
			// If least was the last in the openlist and found nothing tryable around it
			if (openList.size() == 0) {
				// Ran out of possible paths, we give up!
				System.out.println("No Path");
				System.exit(0);
			}
			
		}
		
		// Follows the parent chain of finalP all the way back to the start, the most efficient path
		ArrayList<Coordinate> finalPath = new ArrayList<Coordinate>();
		finalPath.add(finalP.coords);
		while (true) {
			finalP = finalP.parent;
			if (finalP != null)
				finalPath.add(0, finalP.coords);
			else
				break;
		}
		
		// We made it!
		return finalPath;
	}
	
	/**
	 * Tries all 6 possible directions around a specific point, calls TryAddOpen on each
	 * 
	 * @param point The starting point
	 * @param closedList For TryAddOpen()
	 * @param openList For TryAddOpen()
	 * @param finish The end goal for calculating cost
	 * @param maze The 3d map of input
	 */
	private static void TryAddOpens(Point point, LinkedHashSet<Point> closedList, OpenList openList,
			Coordinate finish, ArrayList<ArrayList<ArrayList<Character>>> maze) {
		
		
			// Right, checks if in bounds and if not #
			if (maze.get(point.coords.z).get(point.coords.y).size() > point.coords.x + 1) {
				if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x + 1) != '#') {
					
					// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
					int cost = Math.abs(point.coords.x + 1 - finish.x) + Math.abs(point.coords.y - finish.y) + Math.abs(point.coords.z - finish.z);

					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x + 1, point.coords.y, point.coords.z), cost));
					
				}
			}
			
			// Left, checks if in bounds and if not #
			if (point.coords.x > 0) {
				if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x - 1) != '#') {
					
					// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
					int cost = Math.abs(point.coords.x - 1 - finish.x) + Math.abs(point.coords.y - finish.y) + Math.abs(point.coords.z - finish.z);
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x - 1, point.coords.y, point.coords.z), cost));
				}
			}
			
			// Down, checks if in bounds and if not #
			if (maze.get(point.coords.z).size() > point.coords.y + 1) {
				if (maze.get(point.coords.z).get(point.coords.y + 1).get(point.coords.x) != '#') {
					
					// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
					int cost = Math.abs(((point.coords.x) - finish.x))+ Math.abs(((point.coords.y + 1) - finish.y) + Math.abs(((point.coords.z) - finish.z)));
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y + 1, point.coords.z), cost));
				}
			}
			
			// Up, checks if in bounds and if not #
			if (point.coords.y > 0) {
				if (maze.get(point.coords.z).get(point.coords.y - 1).get(point.coords.x) != '#') {
					
					// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
					int cost = Math.abs(point.coords.x - finish.x) + Math.abs(point.coords.y - 1 - finish.y) + Math.abs(point.coords.z - finish.z);
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y - 1, point.coords.z), cost));
				}
			}
			
			// Through, checks if z
			if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x) == 'z') {
				
				// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
				int cost = Math.abs(point.coords.x - finish.x) + Math.abs(point.coords.y - finish.y) + Math.abs(point.coords.z - 1 - finish.z);
				
				// Tries to add to open list
				TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y, point.coords.z - 1), cost));
			}
			
			// Out, checks if Z
			if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x) == 'Z') {
				
				// Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
				int cost = Math.abs(point.coords.x - finish.x) + Math.abs(point.coords.y - finish.y) + Math.abs(point.coords.z + 1 - finish.z);
				
				// Tries to add to open list
				TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y, point.coords.z + 1), cost));
			}
	}

	/**
	 * Tries to add a point to the open list for further checking
	 * 
	 * @param closedList Things that can't go on the open list
	 * @param openList The current open list
	 * @param point The point we want to add
	 */
	private static void TryAddOpen(LinkedHashSet<Point> closedList, OpenList openList, Point point) {

		// If the closedList or openList has point we can't add it to openList
		if (closedList.contains(point) || openList.contains(point))
			return;
		else
			openList.add(point);
		
	}

	/**
	 * Now that we have a path we can output it
	 * 
	 * @param file The name of the output file
	 * @param map The 3D map
	 * @param path The path the pathfinding took
	 */
	public static void output(String file, ArrayList<ArrayList<ArrayList<Character>>> map, ArrayList<Coordinate> path) {

		// Turns any whitespace on the pathfind into Ps, keeps z Z etc.
		for (int i = 0; i < path.size(); i++) {
			Coordinate p = path.get(i);
			if (map.get(p.z).get(p.y).get(p.x) == ' ')
				map.get(p.z).get(p.y).set(p.x, 'P');
		}

		try {

			// Makes a byte array to instantly write to file
			byte[] bytes = new byte[map.get(0).get(0).size() * map.get(0).size() * map.size() + map.size()
					+ map.size() * map.get(0).size() + 3];

			// Keeps track of bytes
			int count = 0;

			// Loops through the whole map and adds it to the byte array, including pathfind stuff
			for (int z = 0; z < map.size(); z++) {
				for (int y = 0; y < map.get(z).size(); y++) {
					for (int x = 0; x < map.get(z).get(y).size(); x++) {
						bytes[count++] = (byte) map.get(z).get(y).get(x).charValue();
					}
					bytes[count++] = (byte) '\n';
				}
				bytes[count++] = (byte) '\n';
			}

			// Writes the byte array to file
			Files.write(Paths.get(file), bytes);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Just prints the path taken to file, instead of the whole map for efficiency purposes
	 * 
	 * @param file The file name
	 * @param path The path taken
	 */
	public static void quickOutput(String file, ArrayList<Coordinate> path) {
		
		// Creates a byte array with one byte allocated for each coordinate on the path
		byte[] bytes = new byte[path.size() * 2 - 3];
		
		// Stores the previous X and Y to see if it should go left, right, up, or down
		int previousX = path.get(0).x;
		int previousY = path.get(0).y;
		int previousZ = path.get(0).z;
		int count = 0;
		
		// Loops through the path and finds which direction the program took
		for (int i = 1; i < path.size(); i++) {

			if (previousY > path.get(i).y)
				bytes[count++] = 'u';
			else if (previousY < path.get(i).y)
				bytes[count++] = 'd';
			else if (previousX > path.get(i).x)
				bytes[count++] = 'l';
			else if (previousX < path.get(i).x)
				bytes[count++] = 'r';
			else if(previousZ > path.get(i).z)
				bytes[count++] = 'z';
			else if(previousZ < path.get(i).z)
				bytes[count++] = 'Z';

			if(count+1 < bytes.length)
				bytes[count++] = ',';
			
			previousX = path.get(i).x;
			previousY = path.get(i).y;
			previousZ = path.get(i).z;

		}
		try {
			Files.write(Paths.get(file), bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
