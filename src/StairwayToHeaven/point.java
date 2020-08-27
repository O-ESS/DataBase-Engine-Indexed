package StairwayToHeaven;

import java.io.Serializable;

public class point implements Comparable<point>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int x, y;

	public point(int x, int y) {
		// TODO Auto-generated constructor stub
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return x + " " + y;
	}

	@Override
	public int compareTo(point o) {
		// TODO Auto-generated method stub
		if (x == o.x)
			return y - o.y;
		return x - o.x;
	}

}