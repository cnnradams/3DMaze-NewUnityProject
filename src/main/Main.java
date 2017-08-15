package main;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
 * Created using A* but in three dimensions minus backwards distance calculations since it's too slow
 * Threads are not used because they are heavier to create/pool than just running the loop in the main thread
 * Object Pooling doesn't make a difference with how lightweight Point and Coordinate is
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
		System.out.println("Read Time: " + inputTimingEnd + "ms");
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
	    try (FileChannel inChannel = new RandomAccessFile(fileName, "r").getChannel()) {
            MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            
            ArrayList<ArrayList<ArrayList<Character>>> maze = new ArrayList<>();
            
            ArrayList<ArrayList<Character>> twoD = new ArrayList<>();
            ArrayList<Character> oneD = new ArrayList<>();
            
            
            while(buffer.hasRemaining()) {
                char c = (char)buffer.get();
                if(c == '\r');
                else if(c == '\n') {
                    if(oneD.size() == 0) {
                            // End of floor
                            if(twoD.size() != 0) {
                                maze.add(twoD);
                            }
                            twoD = new ArrayList<>(twoD.size());
    
                    }
                    else {
                        // End of line
                        if(oneD.size() != 0)
                            twoD.add(oneD);
                        oneD = new ArrayList<>(oneD.size());
                    }
                }
                else {
                    if (c == 'S')
                        startPos = new Coordinate(oneD.size(), twoD.size(), maze.size());
                    else if (c == 'X') 
                        endPos = new Coordinate(oneD.size(), twoD.size(), maze.size());
                    
                    oneD.add(c);
                }
            }
            
            if(oneD.size() != 0) {
                twoD.add(oneD);
            }
            
            if(twoD.size() != 0) {
                maze.add(twoD);
            }
            
            return maze;
            
	    } catch (IOException e) {
            e.printStackTrace();
        }
	    
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

					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x + 1, point.coords.y, point.coords.z), getCost(point,finish)));
					
				}
			}
			
			// Left, checks if in bounds and if not #
			if (point.coords.x > 0) {
				if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x - 1) != '#') {
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x - 1, point.coords.y, point.coords.z), getCost(point,finish)));
				}
			}
			
			// Down, checks if in bounds and if not #
			if (maze.get(point.coords.z).size() > point.coords.y + 1) {
				if (maze.get(point.coords.z).get(point.coords.y + 1).get(point.coords.x) != '#') {
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y + 1, point.coords.z), getCost(point,finish)));
				}
			}
			
			// Up, checks if in bounds and if not #
			if (point.coords.y > 0) {
				if (maze.get(point.coords.z).get(point.coords.y - 1).get(point.coords.x) != '#') {
					
					// Tries to add to open list
					TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y - 1, point.coords.z), getCost(point,finish)));
				}
			}
			
			// Through, checks if z
			if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x) == 'z') {

				// Tries to add to open list
				TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y, point.coords.z - 1), getCost(point,finish)));
			}
			
			// Out, checks if Z
			if (maze.get(point.coords.z).get(point.coords.y).get(point.coords.x) == 'Z') {
				
				// Tries to add to open list
				TryAddOpen(closedList, openList, new Point(point, new Coordinate(point.coords.x, point.coords.y, point.coords.z + 1), getCost(point,finish)));
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
		// Normally you would compare the cost of the contained point in the openList but in this case
        // where speed > perfect path and the map is rather tight its better to just see if it exists
		if (closedList.contains(point) || openList.contains(point))
			return;
		else
			openList.add(point);
		
	}

	/**
	 * Calculates cost based on direct distance to target (without pythagorean since there is no diagnol)
	 * @param point Current point
	 * @param finish End target
	 * @return Heuristic of the point
	 */
	public static int getCost(Point point, Coordinate finish) {
		// Multiplying the Z coordinate by 4 encourages the program to prioritize getting to the top floor ASAP, since the maze is rather large
		return Math.abs(point.coords.x - finish.x) + Math.abs(point.coords.y - finish.y) + (Math.abs(point.coords.z + 1 - finish.z) * 4);
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
