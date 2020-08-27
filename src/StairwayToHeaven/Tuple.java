package StairwayToHeaven;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tuple implements Serializable, Comparable<Tuple> {

	private static final long serialVersionUID = 1L;
	private Object[] tuple;
	private int clusteringKeyIdx;
	private String TouchDate;
	private Ref postion;

	public Tuple(int size, int clusteringKeyIdx) {
		tuple = new Object[size];
		this.clusteringKeyIdx = clusteringKeyIdx;
		updateTouchDate();
	}

	public String clusterKey() {
		return tuple[clusteringKeyIdx] + "";
	}

	public void setRef(int pageNo, int idx) {
		postion = new Ref(pageNo, idx);
	}

	public Ref getRef() {
		return postion;
	}

	public void put(int idx, Object value) {
		tuple[idx] = value;
		updateTouchDate();
	}

	public Object get(int idx) {
		return tuple[idx];
	}

	public String getTouchDate() {
		return TouchDate;
	}

	public int size() {
		return tuple.length;
	}

	public int compareTo(Tuple o) {
		Object t1 = this.tuple[clusteringKeyIdx];
		Object t2 = o.tuple[clusteringKeyIdx];

		String type = t1.getClass().getName();
		if (type.equals("java.lang.Integer")) { 
			return ((Integer) t1).compareTo((Integer) t2);
		} else if (type.equals("java.lang.String")) {
			return ((String) t1).compareTo((String) t2);
		} else if (type.equals("java.lang.Double")) {
			return ((Double) t1).compareTo((Double) t2);
		} else if (type.equals("java.util.Date")) {
			return ((Date) t1).compareTo((Date) t2);
		} else if (type.equals("StairwayToHeaven.Polygon")) {
			return ((Polygon) t1).compareTo((Polygon) t2);
		} else {
			return 0;
		}
	}
	
	public int compareToClust(Object t2) {
		Object t1 = this.tuple[clusteringKeyIdx];
		String type = t1.getClass().getName();
		if (type.equals("java.lang.Integer")) { 
			return ((Integer) t1).compareTo((Integer) t2);
		} else if (type.equals("java.lang.String")) {
			return ((String) t1).compareTo((String) t2);
		} else if (type.equals("java.lang.Double")) {
			return ((Double) t1).compareTo((Double) t2);
		} else if (type.equals("java.util.Date")) {
			return ((Date) t1).compareTo((Date) t2);
		} else if (type.equals("StairwayToHeaven.Polygon")) {
			return ((Polygon) t1).compareTo((Polygon) t2);
		} else {
			return 0;
		}
	}

	public int compareToClust(String clust) throws ParseException {
		Object t1 = this.tuple[clusteringKeyIdx];
		String t2 = clust;

		String type = t1.getClass().getName();
		if (type.equals("java.lang.Integer")) { 
			return ((Integer) t1).compareTo(Integer.parseInt(t2));
		} else if (type.equals("java.lang.String")) {
			return ((String) t1).compareTo((String) t2);
		} else if (type.equals("java.lang.Double")) {
			return ((Double) t1).compareTo(Double.parseDouble(t2));
		} else if (type.equals("java.util.Date")) {
			Date d2 = new SimpleDateFormat("yyyy-mm-dd").parse(t2);
			return ((Date) t1).compareTo(d2);
		} else {
			return 0;
		}
	}

	public void updateTouchDate() {
		TouchDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date(System.currentTimeMillis()));
	}

	public void update(Object[] rep) {
		for (int i = 0; i < rep.length; i++) {
			if (rep[i] != null) {
				tuple[i] = rep[i];
				updateTouchDate();
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Object o : tuple) {
			Class c = o.getClass();
			sb.append(c.cast(o).toString() + "  ");
		}
		return sb.toString();
	}
}
