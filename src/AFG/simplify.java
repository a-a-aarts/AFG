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
				//initial graph
				if(p.index + 1 < l.points.size())
				{
					p.linesegments.add(l.points.get(p.index + 1));
					p.ProblemPoints.put(l.points.get(p.index + 1), new ArrayList<Point>());
				}
				//only relevant if we are not the last 2 points
				if (p.index + 2 < l.points.size())
				{
					//calc lenghts
					Map<Point, Double> lengths = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.collect(Collectors.toMap(x -> x, x -> getLength(p, x)));
					//calc absolute angles
					Map<Point, Double> angles = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.collect(Collectors.toMap(x -> x, x -> getAngle(p, x)));
					
					Point p1 = null;
					TreeMap<Pair, Point> sortedangle = new TreeMap<>((e1, e2) -> pairCompare(e1, e2));
					
					Set<Point> intermediatepp = new HashSet<>();
					//sort points on leght
					List<Point> sortedlength = lineToBoundingBox.get(l).stream()
						.filter(x -> x != p)
						.sorted((e1, e2) -> Double.compare(lengths.get(e1), lengths.get(e2)))
						.collect(Collectors.toList());
					
					NavigableMap<Pair, Point> needToCheck1prev = new TreeMap<>((e1, e2) -> pairCompare(e1, e2));
					NavigableMap<Pair, Point> needToCheck2prev = new TreeMap<>((e1, e2) -> pairCompare(e1, e2));
					HashSet<Point> intermediate = new HashSet<>();
					
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
								final Point pb;
								final Point ps;
								if(angleCompare(angles.get(px), angles.get(p1), px.x, p1.x) == 1) {
									pb = px;
									ps = p1;
								}
								else {
									pb = p1;
									ps = px;
								}
								NavigableMap<Pair, Point> needToCheck1;
								NavigableMap<Pair, Point> needToCheck2;
								//diference between the angles might be bigger than 2 if it is, our domain is plit
								if(Math.abs(angles.get(px) - angles.get(p1)) < 2) {
									needToCheck1 = sortedangle.subMap(new Pair(angles.get(ps), ps.x), false, new Pair(angles.get(pb), pb.x), false);
									needToCheck2 = null;
									intermediate.addAll(needToCheck1.values().stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
									
								}
								//domain is split
								else {
									needToCheck1 = sortedangle.headMap(new Pair(angles.get(ps), ps.x), false);
									intermediate.addAll(needToCheck1.values().stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
									needToCheck2 = sortedangle.tailMap(new Pair(angles.get(pb), pb.x ), false);
									intermediate.addAll(needToCheck2.values().stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
								}
								
								intermediate.removeAll(getsublist(needToCheck1, needToCheck1prev)
										.stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
								if (needToCheck2prev != null) intermediate.removeAll(getsublist(needToCheck1, needToCheck2prev)
										.stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
								
								if (needToCheck2 != null) {
									intermediate.removeAll(getsublist(needToCheck2, needToCheck1prev)
											.stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
									if (needToCheck2prev != null) intermediate.removeAll(getsublist(needToCheck2, needToCheck2prev)
											.stream().filter(x -> intersects(p, x, pb, ps)).collect(Collectors.toList()));
								}
								intermediate.remove(px);
								
								needToCheck1prev = needToCheck1;
								needToCheck2prev = needToCheck2;
								
								p.linesegments.add(px);
								p.ProblemPoints.put(px, new ArrayList<>(intermediate));
								//advance to next point on line
								sortedangle.remove(new Pair(angles.get(p1), p1.x));
								p1 = px;
							}
						}
					}
				}
			});
		});
	}
	
	private Collection<Point> getsublist(NavigableMap<Pair, Point> a, NavigableMap<Pair, Point> b ){
		if(!a.isEmpty() && !b.isEmpty()){
			Pair upper = pairCompare(a.lastKey(), b.lastKey()) > 0 ? b.lastKey() : a.lastKey();
			Pair lower = pairCompare(a.firstKey(), b.firstKey()) < 0 ? b.firstKey() : a.firstKey();
		
			if (pairCompare(upper, lower) > 0) {
				return b.subMap(lower, true, upper, true).values();
			}
		}
		return new ArrayList<Point>();
		
	}
	
	private int pairCompare(Pair a, Pair b){
		return angleCompare(a.a, b.a, a.x, b.x);
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
	
	boolean intersects(Point p0, Point p1, Point p2, Point p3)
		{
		    double s1_x, s1_y, s2_x, s2_y;
		    s1_x = p1.x - p0.x;     s1_y = p1.y - p0.y;
		    s2_x = p3.x - p2.x;     s2_y = p3.y - p2.y;

		    double s, t;
		    s = (-s1_y * (p0.x - p2.x) + s1_x * (p0.y - p2.y)) / (-s2_x * s1_y + s1_x * s2_y);
		    t = ( s2_x * (p0.y - p2.y) - s2_y * (p0.x - p2.x)) / (-s2_x * s1_y + s1_x * s2_y);

		    return s >= 0 && s <= 1 && t >= 0 && t <= 1;
		}
	
}
