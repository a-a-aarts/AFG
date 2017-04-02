package AFG;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.Arrays;
import java.util.regex.*;

public class Parser {
	
	public static List<Line> fileToLines(String file) throws ParserConfigurationException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Pattern p = Pattern.compile("^(\\d+):(.*)$");
		List<Line> lines = new ArrayList<>();
		Files.lines(Paths.get(file)).forEach(l -> {
			
			try{
				Matcher m = p.matcher(l);
				if (m.find())
				{
					List<Point> points = new ArrayList<>();
					Document document = builder.parse(new InputSource(new StringReader(m.group(2))));
					String pointstring = document.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
					//get points
					Arrays.stream(pointstring.split(" ")).forEach(x -> {
						String[] xy = x.split(",");
						points.add(new Point(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
					});
					Line line = new Line(points, Integer.parseInt(m.group(1)));
					//add meta data
				    for(int i = 0; i < points.size(); i++){
				    	points.get(i).index = i;
				    	points.get(i).line = line;
				    }
				    lines.add(line);
				}
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		});
		return lines;
	}
	
	public static List<Point> fileToPoints(String file) throws ParserConfigurationException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Pattern p = Pattern.compile("^(//d+):(.*)$");
		List<Point> points = new ArrayList<>();
		Files.lines(Paths.get(file)).forEach(l -> {
			
			try{
				Matcher m = p.matcher(l);
				if (m.find())
				{
					Document document = builder.parse(new InputSource(new StringReader(m.group(2))));
					String pointstring = document.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
					//get points
						String[] xy = pointstring.split(",");
						points.add(new Point(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
				}
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		});
		return points;
	}
	
}
