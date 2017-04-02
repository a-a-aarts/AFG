package AFG;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class simplify {
	List<Line> lines;
	List<Point> Points;
	
	Map<Line, List<Point>> lineToBoundingBox;
	
	public void organize(){
		//collection of all points
		List<Point> allPoints = Stream.concat(
			Points.stream(),
			lines.stream().flatMap(x -> x.points.stream()))
				.collect(Collectors.toList());
		
		lineToBoundingBox = new HashMap<>();
		//for each line
		lines.parallelStream().forEach(l -> {
			double xmin = Double.POSITIVE_INFINITY;
			double ymin = Double.POSITIVE_INFINITY;
			double xmax = Double.NEGATIVE_INFINITY;
			double ymax = Double.NEGATIVE_INFINITY;
			
			for (Point p : l.points){
				if (p.x < xmin) xmin = p.x;
				if (p.y < ymin) ymin = p.x;
				if (p.x > xmax) xmax = p.x;
				if (p.y > ymax) ymax = p.x;
			}
			
			final double fxmin = xmin;
			final double fymin = ymin;
			final double fxmax = xmax;
			final double fymax = ymax;
			
			//add correct points to line bounding box
			lineToBoundingBox.put(
					 l
					,allPoints.stream().filter(p -> fxmin <= p.x && p.x <= fxmax
												 &&	fymin <= p.y && p.y <= fymax ).collect(Collectors.toList()));
		});
	}
	
	//creates the graph
	//NOTE: does not take into account lines that rotate more than 360%
	public void createGraph(){
		//for each line
		lines.parallelStream().forEach(l -> {
			//for each point on the line
			l.points.stream().forEach(p -> {
				//only relevant if we are not the last 2 points
				if (p.index < l.points.size() - 2)
				{
					//calc lenghts
					Map<Point, Double> lengths = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.collect(Collectors.toMap(x -> x, x -> getLength(p, x)));
					//calc angles
					Map<Point, Double> angles = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.collect(Collectors.toMap(x -> x, x -> getAngle(p, x)));
					
					Point p1 = null;
					TreeMap<Pair, Point> sortedangle = new TreeMap<>((e1, e2) -> angleCompare(e1.a, e2.a, e1.x, e2.x));
					//sort points on leght
					List<Point> sortedlength = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.sorted((e1, e2) -> Double.compare(lengths.get(e1), lengths.get(e2)))
						.collect(Collectors.toList());
					//insert in order of length and sort on angle
					for (Point px : sortedlength){
						sortedangle.put(new Pair(angles.get(px), px.x), px);
						//intialize
						if(p1 == null) {
							if (px.index == p.index + 1) {
								p1 = px;
							}
						}
						//main part
						else{
							//we only do something if it is the next point in the line
							if (px.index == p1.index + 1) {
								//determine bigger and smaller point
								Point pb = null;
								Point ps = null;
								if(angleCompare(angles.get(px), angles.get(p1), px.x, p1.x) == 1) {
									pb = px;
									ps = p1;
								}
								else {
									pb = p1;
									ps = px;
								}
								Collection<Point> needToCheck;
								//diference between the angles might be bigger than 2 if it is out domain is plit
								if(Math.abs(angles.get(px) - angles.get(p1)) < 2) {
									needToCheck = sortedangle.subMap(new Pair(angles.get(ps), ps.x), false, new Pair(angles.get(pb), pb.x), false).values();
								}
								//domain is split
								else {
									needToCheck = Stream.of( sortedangle.headMap(new Pair(angles.get(ps), ps.x)).values()
														   , sortedangle.tailMap(new Pair(angles.get(pb), pb.x ), false).values())
											.flatMap(Collection::stream)
											.collect(Collectors.toList());
								}
								
								//see which points are actualy in the problem set using line intersections
								p1 = px;
							}
						}
					}
				}
			});
		});
	}
	
	private int angleCompare(double a1, double a2, double x1, double x2){
		return Double.compare(a1, a2) != 0  
		   		? Double.compare(a1, a2)
		   		: Double.compare(x1, x2);
	}
	
	private double getLength(Point p1, Point p2){
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		return Math.sqrt(x * x + y * y);
	}
	
	private double getAngle(Point p1, Point p2){
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		double invlength = 1 / Math.sqrt(x * x + y * y);
		x = invlength * x;
		y = invlength * y;
		return 2 + (y > 0 ? x + (x > 0 ? 1 : -1) : x);
	}
	
}
