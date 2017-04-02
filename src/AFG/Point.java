package AFG;

import java.util.*;

public class Point {
	
	public final double x;
	public final double y;
	
	public int index;
	public Line line;
	
	//the graph
	public List<Point> linesegments;
	public Map<Point,List<Point>> ProblemPoints;
	boolean active;
	
	public Point(double x, double y){
		this.x = x;
		this.y = y;
		index = -1;
		linesegments = new ArrayList<>();
		ProblemPoints = new HashMap<>();
	}
	

}
