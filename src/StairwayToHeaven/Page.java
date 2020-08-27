package StairwayToHeaven;

import java.io.*;
import java.text.*;
import java.util.*;

public class Page implements Serializable {

	private static final long serialVersionUID = 1L;
	private int maxSize, clustKeyIdx;
	private String pageID, file;
	private Vector<Tuple> tuples;

	public Page(int maxSize, String path, int tupleSize, int clustKeyIdx) throws IOException {
		Long key = DBApp.getPageIDGenerator();
		pageID = key.toString();
		this.file = path + '/' + pageID + ".class";
		this.maxSize = maxSize;
		this.clustKeyIdx = clustKeyIdx;
		this.tuples = new Vector<>();
		writePage();
	}

	public Vector getTuples() {
		return tuples;
	}

	public Boolean isFull() {
		return tuples.size() == maxSize;
	}

	public Boolean isMax() {
		return tuples.size() > maxSize;
	}

	public Boolean isEmpty() {
		return tuples.isEmpty();
	}

	public void addHere(Tuple t, int h) throws IOException {
		tuples.add(h, t);
		// writePage();
	}

	public int size() {
		return tuples.size();
	}

	public void add(Tuple t) throws IOException {
		tuples.add(t);
//		writePage();
	}

	public String getFile() {
		return this.file;
	}

	public void writePage() throws IOException {
		File f = new File(file);
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this);
		oos.close();
	}

	public Object firstKeyValue() {
		return tuples.get(0).get(clustKeyIdx);
	}

	public Object lastKeyValue() {
		return tuples.get(tuples.size() - 1).get(clustKeyIdx);
	}

	public Object elementKeyValue(int id) {
		return tuples.get(id).get(clustKeyIdx);
	}

	public Tuple get(int idx) {
		return tuples.get(idx);
	}

	public Tuple lastElement() {
		return tuples.lastElement();
	}

	public Tuple firstElement() {
		return tuples.firstElement();
	}

	public void setLastElement(Tuple t) throws IOException {
		tuples.set(tuples.size() - 1, t);
		writePage();
	}

	public void setFirstElement(Tuple t) throws IOException {
		tuples.set(0, t);
		writePage();
	}

	public void sort() throws IOException {
		Collections.sort(tuples);
		writePage();
	}

	public int contains(String strClustKey) throws ParseException {
		try {
		//	System.out.println(strClustKey);
			Object fst = firstKeyValue();
			Object lst = lastKeyValue();
			String type = fst.getClass().getName();
			///System.out.println(type);
			if (type.equals("java.lang.Integer")) {
				if (((Integer) (Integer.parseInt(strClustKey))).compareTo((Integer) fst) < 0)
					return -1;
				if (((Integer) (Integer.parseInt(strClustKey))).compareTo((Integer) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.String")) {
				if (strClustKey.compareTo((String) fst) < 0)
					return -1;
				if (strClustKey.compareTo((String) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.Double")) {
				if (((Double) (Double.parseDouble(strClustKey))).compareTo((Double) fst) < 0)
					return -1;
				if (((Double) (Double.parseDouble(strClustKey))).compareTo((Double) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.util.Date")) {
				Date fstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) fst);
				Date lstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) lst);
				Date clustKey = new SimpleDateFormat("yyyy-mm-dd").parse(strClustKey);
				if (clustKey.compareTo(fstdate) < 0)
					return -1;
				if (clustKey.compareTo(lstdate) > 0)
					return 1;
				return 0;
			} else if (type.equals("StairwayToHeaven.Polygon")) {
				StairwayToHeaven.Polygon clustKey = new StairwayToHeaven.Polygon(strClustKey);
				if (clustKey.compareTo((StairwayToHeaven.Polygon) fst) < 0)
					return -1;
				if (clustKey.compareTo((StairwayToHeaven.Polygon) lst) > 0)
					return 1;
				return 0;
			} else {
				return 0;
			}
		} catch (Exception e) {
			//System.out.println("the case");
			System.out.println("please insert a valid data type");
			return 0;
		}
	}
	
	public int contains(Object clustKey) throws ParseException {
		try {
			Object fst = firstKeyValue();
			Object lst = lastKeyValue();
			String type = fst.getClass().getName();
			if (type.equals("java.lang.Integer")) {
				if (((Integer) clustKey).compareTo((Integer) fst) < 0)
					return -1;
				if (((Integer) clustKey).compareTo((Integer) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.String")) {
				if (((String) clustKey).compareTo((String) fst) < 0)
					return -1;
				if (((String) clustKey).compareTo((String) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.Double")) {
				if (((Double) clustKey).compareTo((Double) fst) < 0)
					return -1;
				if (((Double) clustKey).compareTo((Double) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.util.Date")) {
				Date fstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) fst);
				Date lstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) lst);
				Date NclustKey = new SimpleDateFormat("yyyy-mm-dd").parse((String) clustKey);
				if (NclustKey.compareTo(fstdate) < 0)
					return -1;
				if (NclustKey.compareTo(lstdate) > 0)
					return 1;
				return 0;
			} else if (type.equals("StairwayToHeaven.Polygon")) {
				if (((StairwayToHeaven.Polygon) clustKey).compareTo((StairwayToHeaven.Polygon) fst) < 0)
					return -1;
				if (((StairwayToHeaven.Polygon) clustKey).compareTo((StairwayToHeaven.Polygon) lst) > 0)
					return 1;
				return 0;
			} else {
				return 0;
			}
		} catch (Exception e) {
			System.out.println("please insert a valid data type");
			return 0;
		}
	}
	
	
	
	public int check(String strClustKey, int id) throws ParseException {
		try {
			Object fst = elementKeyValue(id);
			Object lst = elementKeyValue(id);
			String type = fst.getClass().getName();
			if (type.equals("java.lang.Integer")) {
				if (((Integer) (Integer.parseInt(strClustKey))).compareTo((Integer) fst) < 0)
					return -1;
				if (((Integer) (Integer.parseInt(strClustKey))).compareTo((Integer) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.String")) {
				if (strClustKey.compareTo((String) fst) < 0)
					return -1;
				if (strClustKey.compareTo((String) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.lang.Double")) {
				if (((Double) (Double.parseDouble(strClustKey))).compareTo((Double) fst) < 0)
					return -1;
				if (((Double) (Double.parseDouble(strClustKey))).compareTo((Double) lst) > 0)
					return 1;
				return 0;
			} else if (type.equals("java.util.Date")) {
				Date fstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) fst);
				Date lstdate = new SimpleDateFormat("yyyy-mm-dd").parse((String) lst);
				Date clustKey = new SimpleDateFormat("yyyy-mm-dd").parse(strClustKey);
				if (clustKey.compareTo(fstdate) < 0)
					return -1;
				if (clustKey.compareTo(lstdate) > 0)
					return 1;
				return 0;
			} else if (type.equals("StairwayToHeaven.Polygon")) {
				StairwayToHeaven.Polygon clustKey = new StairwayToHeaven.Polygon(strClustKey);
				if (clustKey.compareTo((StairwayToHeaven.Polygon) fst) < 0)
					return -1;
				if (clustKey.compareTo((StairwayToHeaven.Polygon) lst) > 0)
					return 1;
				return 0;
			} else {
				return 0;
			}
		} catch (Exception e) {
			System.out.println("please insert a valid data type");
			return 0;
		}
	}

	public boolean replace(String clustKey, Object[] rep) throws IOException {
		boolean flag = false;
		for (Tuple t : tuples) {
			if (t.get(clustKeyIdx).toString().equals(clustKey)) {
				flag = true;
				t.update(rep);
			}
		}
		writePage();
		return flag;
	}

	public void remove(int idx) throws IOException {
		tuples.remove(idx);
		writePage();
	}

}
