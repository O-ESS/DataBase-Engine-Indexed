package StairwayToHeaven;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Polygon implements Comparable<Polygon>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name; 
	private java.awt.Polygon polygon;
	private TreeSet<point> points;

	public Polygon(String polygon) {
		points = new TreeSet<>();
		name = polygon;
		this.polygon = parsePolygon(polygon);
	}

	public java.awt.Polygon parsePolygon(String str) {
		java.awt.Polygon p = new java.awt.Polygon();
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens()) {
			String xx = st.nextToken();
			String yy = st.nextToken();
			xx = xx.substring(1);
			yy = yy.substring(0, yy.length() - 1);
			p.addPoint(Integer.parseInt(xx), Integer.parseInt(yy));
			points.add(new point(Integer.parseInt(xx), Integer.parseInt(yy)));
		}
		return p;
	}

	public java.awt.Polygon getPolygon() {
		return polygon;
	}

	public TreeSet getPoints() {
		return points;
	}
	public boolean equalsp(TreeSet<point> b) {
		if (points.size() != b.size())
			return false;
		TreeSet<point> x = (TreeSet) b.clone();
		for (point p : points) {
			point now = x.pollFirst();
			if (p.x != now.x || p.y != now.y)
				return false;
		}
		return true;
	}
	@Override
	public int compareTo(Polygon o) { // sort according to area
		int a1 = polygon.getBounds().height * polygon.getBounds().width;
		int a2 = o.getPolygon().getBounds().height * o.getPolygon().getBounds().width;
		return a1 - a2;
	}

	public String toString() {
		return name;
	}
}
