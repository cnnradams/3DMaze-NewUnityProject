package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public String fileName;

	public static void main(String[] args) {
		getInput("b");
	}

	public static Character[][][] getInput(String fileName) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(new File("src/resources/in.txt")));
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
			input.close();
			Character[][][] stringArray = layers.stream()
					.map(u1 -> u1.stream().map(u2 -> u2.toArray(new Character[0])).toArray(Character[][]::new))
					.toArray(Character[][][]::new);
			return stringArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String pathFind(Character[][][] maze) {
		for(int z = 0; z < maze.length; z++) {
			Coordinate startPos = null;
			Coordinate endPos = null;
				for(int y = 0; y < maze[z].length; y++) {
					for(int x = 0; x < maze[z][y].length; x++) {
						if(maze[z][y][x] == 'S' || maze[z][y][x] == 'z') {
							startPos = new Coordinate(x,y);
						}
						if(maze[z][y][x] == 'X' || maze[z][y][x] == 'Z') {
							endPos = new Coordinate(x,y);
						}
					}
				}
				
				ArrayList<Coordinate> openList = new ArrayList<Coordinate>();
				openList.add(startPos);
				
				
				
				
				
				
				
			}
		return null;
	}
}
