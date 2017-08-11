package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

	public String fileName;

	public static boolean QUICK_OUTPUT = false;
	public static void main(String[] args) {
		long in = System.currentTimeMillis();
		ArrayList<Floor> input = getInput("in.txt");
		long inE = System.currentTimeMillis()-in;
		long pather = System.currentTimeMillis();
		ArrayList<Coordinate> path = pathFind(input);
		long patherE = System.currentTimeMillis()-pather;
		long out = System.currentTimeMillis();
		if(QUICK_OUTPUT)
			quickOutput("out.txt", path);
		else
			output("out.txt", maze, path);
		long outE = System.currentTimeMillis()-out;
		
		System.out.println("Read Time: " + inE + "ms");
		System.out.println("Pathfind Time: " + patherE + "ms");
		System.out.println("Output: " + outE + "ms");
		System.out.println("Total Time: " + (inE + patherE + outE) + "ms");
         System.out.println("Thread Time: " + Main.inE + "ns " + Main.inE2 + "ns");
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

	static volatile ArrayList<Floor> maze;
	static volatile ArrayList<ArrayList<Coordinate>> finalPath;

	public static ArrayList<Coordinate> pathFind(ArrayList<Floor> mazer) {
		
		maze = mazer;
		finalPath = new ArrayList<ArrayList<Coordinate>>(maze.size());
		while(finalPath.size() < maze.size()) finalPath.add(new ArrayList<>());
		
		ArrayList<Thread> threads = new ArrayList<>(maze.size());
		
		for (int z = 0; z < maze.size(); z++) {
		    threads.add(new Thread(new FloorSolver(z)));
		    threads.get(z).start();
		}
		
		boolean done = false;
		while(!done) {
		    done = true;
		    for(Thread thread : threads) {
		        if(thread.isAlive()) {
		            done = false;
		            break;
		        }
		    }
		}
		
		ArrayList<Coordinate> allFloors = new ArrayList<>();
		
		for(ArrayList<Coordinate> solvedFloor : finalPath) {
		    allFloors.addAll(solvedFloor);
		}
		
		return allFloors;
	}
	
	public static class FloorSolver implements Runnable {

	    ArrayList<Point> openList;
	    ArrayList<Point> closedList;
	    
	    private final int z;
	    
	    public FloorSolver(int z) {
	        this.z = z;
	    }
	    
        @Override
        public void run() {
        	
            openList = new ArrayList<Point>();
            closedList = new ArrayList<Point>();
            Point startPoint = new Point(null, maze.get(z).startPos, 0);
            closedList.add(startPoint);
            TryAddOpens(startPoint, closedList, openList, maze.get(z).endPos, z);
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
                TryAddOpens(least, closedList, openList, maze.get(z).endPos, z);
                if ((least.coords.x == maze.get(z).endPos.x && least.coords.y == maze.get(z).endPos.y) || openList.size() == 0) {
                    done = true;
                    finalsq = least;
                    if((least.coords.x != maze.get(z).endPos.x || least.coords.y != maze.get(z).endPos.y)) {
                        System.out.println("No Path!");
                        System.exit(0);
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
                finalPath.get(z).add(finalPathBackwards.get(i));
            }
           
        }
	}

	private static void TryAddOpens(Point p, ArrayList<Point> closedList, ArrayList<Point> openList, Coordinate finish, int z) {
		
		if (maze.get(z).get(p.coords.y).get(p.coords.x + 1) != '#') {
			int cost = Math.abs(((p.coords.x + 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y)); 
			TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x + 1, p.coords.y, z), cost));
		}
	
		if (maze.get(z).get(p.coords.y).get(p.coords.x - 1) != '#') {
			int cost = Math.abs(((p.coords.x - 1) - finish.x)) + Math.abs(((p.coords.y) - finish.y));
			TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x - 1, p.coords.y, z), cost));
		}
		if (maze.get(z).get(p.coords.y + 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y + 1) - finish.y));
			TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x, p.coords.y + 1, z), cost));
		}
		if (maze.get(z).get(p.coords.y - 1).get(p.coords.x) != '#') {
			int cost = Math.abs(((p.coords.x) - finish.x)) + Math.abs(((p.coords.y - 1) - finish.y));
			TryAddOpen(closedList, openList, new Point(p, new Coordinate(p.coords.x, p.coords.y - 1, z), cost));
		}
		
	}
	static long inE = 0;
	static long inE2 = 0;
	private static void TryAddOpen(ArrayList<Point> closedList, ArrayList<Point> openList, Point p) {
		
		long in = System.nanoTime();
			if (closedList.contains(p)) {
				return;
			}
		
		 inE += System.nanoTime()-in;
         long in2 = System.nanoTime();
		boolean onOpenList = false;
		for (Point g : openList) {
			if (g.coords.x == p.coords.x && g.coords.y == p.coords.y) {
				if (g.cost > p.cost) {
					g.returnPoint();
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
		inE2 += System.nanoTime() - in2;
	}
	public static void output(String file, ArrayList<Floor> map, ArrayList<Coordinate> path) {
		for(int i = 0; i < path.size(); i++) {
			if(map.get(path.get(i).z).get(path.get(i).y).get(path.get(i).x) == ' ') {
				map.get(path.get(i).z).get(path.get(i).y).set(path.get(i).x, 'P');
			}
		}

		try {
		
		byte[] bytes = new byte[map.get(0).get(0).size() * map.get(0).size() * map.size() + map.size() + map.size() * map.get(0).size()];
		
		int count = 0;
		
		for(int z = 0; z < map.size(); z++) {
			for(int y = 0; y < map.get(z).size(); y++) {
				for(int x = 0; x < map.get(z).get(y).size(); x++) {
					bytes[count++] = (byte)map.get(z).get(y).get(x).charValue();
				}
				bytes[count++] = (byte)'\n';
			}
			bytes[count++] = (byte)'\n';
		}
		
		Files.write(Paths.get(file), bytes);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void quickOutput(String file, ArrayList<Coordinate> path) {
		byte[] bytes = new byte[path.size()];
		int previousX = path.get(0).x;
		int previousY = path.get(0).y;
		boolean newL = false;
		bytes[0] = 'S';
		for(int i = 1; i < path.size(); i++) {
			if(path.get(i).x == -1 && path.get(i).y == -1) {
				bytes[i] = '\n';
				previousX = path.get(i).x;
				previousY = path.get(i).y;
				newL = true;
				continue;
			}
			if(newL) {
				bytes[i] = 'Z';
				newL = false;
				continue;
			}
				
			if(previousY > path.get(i).y)
				bytes[i] = 'u';
			else if(previousY < path.get(i).y)
				bytes[i] = 'd';
			else if(previousX > path.get(i).x)
				bytes[i] = 'l';
			else if(previousX < path.get(i).x)
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
