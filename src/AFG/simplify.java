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
	
	public void createGraph(){
		
	}
	
}
