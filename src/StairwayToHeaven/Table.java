package StairwayToHeaven;

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.*;

public class Table implements Serializable {
	private static final long serialVersionUID = 1L;
	private static String metafile = "data/metadata.csv";
	private int maxSize, tupleSize, clustKeyIdx;
	private String tableName, file, path, clustKeyName;
	private Vector<String> pages; // stores the file of the page to be accessed immediately.
	private TreeMap<String, String> indexCoulmn;
	private transient CSV meta;

	public Table(String tableName, String path, int maxSize, int tupleSize, int clustKeyIdx, CSV meta,
			String clustKeyName) throws IOException {
		this.tupleSize = tupleSize;
		this.clustKeyIdx = clustKeyIdx;
		this.tableName = tableName;
		this.path = path;
		this.file = path + "/" + this.tableName + ".class";
		this.maxSize = maxSize;
		this.pages = new Vector<>();
		this.indexCoulmn = new TreeMap<>();
		this.meta = meta;
		this.clustKeyName = clustKeyName;
		writeTable();
	}

	public int pageSize() {
		return maxSize;
	}

	public void readCSV() throws Exception {
		meta = new CSV(metafile);
	}

	public int size() {
		return pages.size();
	}

	public String printindecies() throws Exception {
		String s = "";
		for (String sk : indexCoulmn.keySet()) {
			int pos = meta.findColIdx(tableName, sk);
			String k = meta.arr.get(pos).ColoumnType;
			s += sk + "\n";
			if (k.equals("java.lang.Integer")) {
				BPTree<Integer> b = getBPTree(sk);
				s += b.toString() + "\n";
			} else if (k.equals("java.lang.String")) {
				BPTree<String> b = getBPTree(sk);
				s += b.toString() + "\n";
			} else if (k.equals("java.lang.Double")) {
				BPTree<Double> b = getBPTree(sk);
				s += b.toString() + "\n";
			} else if (k.equals("java.util.Date")) {
				BPTree<Date> b = getBPTree(sk);
				s += b.toString() + "\n";
			} else if (k.equals("StairwayToHeaven.Polygon")) {
				RTree<Polygon> b = getRTree(sk);
				s += b.toString() + "\n";
			}
		}
		return s;
	}

	public String getClustKeyName() {
		return clustKeyName;
	}

	public boolean containsIndex(String strColName) {
		return indexCoulmn.containsKey(strColName);
	}

	public boolean clustringKeyIndexed() {
		if (indexCoulmn.containsKey(clustKeyName))
			return true;
		return false;
	}

	public void addIndex(String strColName, String fileName) throws IOException {
		indexCoulmn.put(strColName, fileName);
		meta.indexCol(tableName, strColName);
		writeTable();
	}

	public TreeMap getIndexCoulmn() {
		return indexCoulmn;
	}

	public void writeTable() throws IOException {
		File f = new File(file);
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this);
		oos.close();
	}

	public void remove(int idx) throws IOException {
		pages.remove(idx);
		writeTable();
	}

	public Page getPage(int idx) throws IOException, ClassNotFoundException {
		String file = pages.get(idx);
		File f = new File(file);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		Page p = (Page) ois.readObject();
		ois.close();
		return p;

	}

	public Page getLastPage() throws IOException, ClassNotFoundException {
		return getPage(pages.size() - 1);
	}

	public int getLastPageIdx() throws IOException, ClassNotFoundException {
		return pages.size() - 1;
	}

	public int getLastTubleIdx() throws IOException, ClassNotFoundException {
		return getPage(pages.size() - 1).size() - 1;
	}

	private void addPage() throws IOException {
		Page p = new Page(maxSize, path, tupleSize, clustKeyIdx);
		this.pages.add(p.getFile());
	}

	public void insert(Tuple t) throws IOException, ClassNotFoundException {
		if (pages.isEmpty()) { // add a page
			addPage();
		}
		Page lst = getLastPage();
		if (lst.isFull()) { // add page and update lst
			addPage();
			lst = getLastPage();
		}
		lst.add(t);
		lst.writePage();
		writeTable();
	}

	public void insertAt(Tuple t, int page, int idx) throws Exception {
		if (pages.isEmpty()) {
			addPage();
			Page lst = getLastPage();
			lst.add(t);
			lst.writePage();
			writeTable();
			return;
		}
		Page p = getPage(page);
		if (idx >= p.size()) {
			p.add(t);
		} else
			p.addHere(t, idx);

		int id = page + 1;
		int start = meta.getTablePos(tableName);
//		System.out.println(maxSize + " " + p.isMax() + " " + id + " " + pages.size());
		while (p.isMax()) {
			if (id == pages.size()) {
				addPage();
			}
			Page nxt = getPage(id);
			nxt.addHere(p.get(p.size() - 1), 0);
			p.remove(p.size() - 1);
			int i = id - 1;
			for (int j = 0; j < p.size(); j++) {
				Tuple tuple = p.get(j);
				for (String s : indexCoulmn.keySet()) {
					int pos = meta.findColIdx(tableName, s);
					String k = meta.arr.get(pos).ColoumnType;
					if (k.equals("java.lang.Integer")) {
						BPTree<Integer> b = getBPTree(s);
						b.updateKey((Integer) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
						// tuple.setRef(i, j);
						// System.out.println(tuple.getRef());
					} else if (k.equals("java.lang.String")) {
						BPTree<String> b = getBPTree(s);
						b.updateKey((String) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
						// tuple.setRef(i, j);
					} else if (k.equals("java.lang.Double")) {
						BPTree<Double> b = getBPTree(s);
						b.updateKey((Double) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
						// tuple.setRef(i, j);
						// System.out.println(tuple.getRef());
					} else if (k.equals("java.util.Date")) {
						BPTree<Date> b = getBPTree(s);
						b.updateKey((Date) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
						// tuple.setRef(i, j);
					} else if (k.equals("StairwayToHeaven.Polygon")) {
						RTree<Polygon> b = getRTree(s);
						b.updateKey((Polygon) tuple.get(pos - start), tuple.getRef(), new Ref(i, j),
								((Polygon) tuple.get(pos - start)).getPoints());
						// tuple.setRef(i, j);
					}
				}
				tuple.setRef(i, j);
			}
			p.writePage();
			nxt.writePage();
			p = nxt;
			id++;
		}
		int i = id - 1;
		for (int j = 0; j < p.size(); j++) {
			Tuple tuple = p.get(j);
			for (String s : indexCoulmn.keySet()) {
				int pos = meta.findColIdx(tableName, s);
				String k = meta.arr.get(pos).ColoumnType;
				// System.out.println(s + "stringgg");
				if (k.equals("java.lang.Integer")) {
					BPTree<Integer> b = getBPTree(s);
					b.updateKey((Integer) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
					// tuple.setRef(i, j);
					// System.out.println(tuple.getRef());
				} else if (k.equals("java.lang.String")) {
					BPTree<String> b = getBPTree(s);
					b.updateKey((String) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
					// tuple.setRef(i, j);
				} else if (k.equals("java.lang.Double")) {
					BPTree<Double> b = getBPTree(s);
					b.updateKey((Double) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
					// tuple.setRef(i, j);
					// System.out.println(tuple.getRef());
				} else if (k.equals("java.util.Date")) {
					BPTree<Date> b = getBPTree(s);
					b.updateKey((Date) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
					// tuple.setRef(i, j);
				} else if (k.equals("StairwayToHeaven.Polygon")) {
					RTree<Polygon> b = getRTree(s);
					b.updateKey((Polygon) tuple.get(pos - start), tuple.getRef(), new Ref(i, j),
							((Polygon) tuple.get(pos - start)).getPoints());
					// tuple.setRef(i, j);
				}
			}
			tuple.setRef(i, j);
		}
		p.writePage();
		writeTable();
	}

//	public void sort() throws Exception {
//
//		for (int i = pages.size() - 1; i >= 0; i--) {
//			Page p = getPage(i);
//
//			p.sort();
//			int start = meta.getTablePos(tableName);
//			for (int j = 0; j < p.size(); j++) {
//				Tuple tuple = p.get(j);
//				for (String s : indexCoulmn.keySet()) {
//					int pos = meta.findColIdx(tableName, s);
//					String k = meta.arr.get(pos).ColoumnType;
//					if (k.equals("java.lang.Integer")) {
//						BPTree<Integer> b = getBPTree(s);
//						b.updateKey((Integer) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
//					} else if (k.equals("java.lang.String")) {
//						BPTree<String> b = getBPTree(s);
//						b.updateKey((String) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
//					} else if (k.equals("java.lang.Double")) {
//						BPTree<Double> b = getBPTree(s);
//						b.updateKey((Double) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
//					} else if (k.equals("java.util.Date")) {
//						BPTree<Date> b = getBPTree(s);
//						b.updateKey((Date) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
//					} else if (k.equals("StairwayToHeaven.Polygon")) {
//						BPTree<Polygon> b = getBPTree(s);
//						b.updateKey((Polygon) tuple.get(pos - start), tuple.getRef(), new Ref(i, j));
//					}
//				}
//			}
//			if (i > 0) {
//				Page prev = getPage(i - 1);
//				Object t1 = p.firstKeyValue();
//				Object t2 = prev.lastKeyValue();
//				String type = t1.getClass().getName();
//				int comp = 0;
//				if (type.equals("java.lang.Integer")) {
//					comp = ((Integer) t1).compareTo((Integer) t2);
//				} else if (type.equals("java.lang.String")) {
//					comp = ((String) t1).compareTo((String) t2);
//				} else if (type.equals("java.lang.Double")) {
//					comp = ((Double) t1).compareTo((Double) t2);
//				} else if (type.equals("java.util.Date")) {
//					comp = ((Date) t1).compareTo((Date) t2);
//				} else if (type.equals("StairwayToHeaven.Polygon")) {
//					comp = ((Polygon) t1).compareTo((Polygon) t2);
//				} else {
//					// throw Exception
//					throw new DBAppException("Incompatible data type");
//				}
//
//				if (comp < 0) { // swap and sort page (i - 1)
//					Tuple temp1 = prev.lastElement();
//					Tuple temp2 = p.firstElement();
//					p.setFirstElement(temp1);
//					prev.setLastElement(temp2);
//					for (String s : indexCoulmn.keySet()) {
//						int pos = meta.findColIdx(tableName, s);
//						String k = meta.arr.get(pos).ColoumnType;
//						if (k.equals("java.lang.Integer")) {
//							BPTree<Integer> b = getBPTree(s);
//							b.updateKey((Integer) temp1.get(pos - start), temp1.getRef(), new Ref(i, 0));
//							b.updateKey((Integer) temp2.get(pos - start), temp2.getRef(),
//									new Ref(i - 1, prev.size() - 1));
//						} else if (k.equals("java.lang.String")) {
//							BPTree<String> b = getBPTree(s);
//							b.updateKey((String) temp1.get(pos - start), temp1.getRef(), new Ref(i, 0));
//							b.updateKey((String) temp2.get(pos - start), temp2.getRef(),
//									new Ref(i - 1, prev.size() - 1));
//						} else if (k.equals("java.lang.Double")) {
//							BPTree<Double> b = getBPTree(s);
//							b.updateKey((Double) temp1.get(pos - start), temp1.getRef(), new Ref(i, 0));
//							b.updateKey((Double) temp2.get(pos - start), temp2.getRef(),
//									new Ref(i - 1, prev.size() - 1));
//						} else if (k.equals("java.util.Date")) {
//							BPTree<Date> b = getBPTree(s);
//							b.updateKey((Date) temp1.get(pos - start), temp1.getRef(), new Ref(i, 0));
//							b.updateKey((Date) temp2.get(pos - start), temp2.getRef(), new Ref(i - 1, prev.size() - 1));
//						} else if (k.equals("StairwayToHeaven.Polygon")) {
//							BPTree<Polygon> b = getBPTree(s);
//							b.updateKey((Polygon) temp1.get(pos - start), temp1.getRef(), new Ref(i, 0));
//							b.updateKey((Polygon) temp2.get(pos - start), temp2.getRef(),
//									new Ref(i - 1, prev.size() - 1));
//						}
//					}
//				} else {
//					break;
//				}
//			}
//		}
//	}

	public int findFirstClustKeyInstance(String strClusteringKey)
			throws IOException, ClassNotFoundException, ParseException {
		// binary search according to clustering key
		int start = 0, end = pages.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			Page p = getPage(mid);
			int x = p.contains(strClusteringKey);
			if (x < 0) {
				end = mid - 1;
			} else if (x > 0) {
				start = mid + 1;
			} else { // page lies within
				ans = mid;
				end = mid - 1;
			}
		}
		return ans;
	}

	public int findFirstClustKeyInstance(Object ClusteringKey)
			throws IOException, ClassNotFoundException, ParseException {
		// find the idx of the first page with element >= clust
		int start = 0, end = pages.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			Page p = getPage(mid);
			int x = p.contains(ClusteringKey);
			if (x <= 0) {
				ans = mid;
				end = end - 1;
			} else {
				start = mid + 1;
			}
		}
		return ans;
	}

	public int findLastClustKeyInstance(Object ClusteringKey)
			throws IOException, ClassNotFoundException, ParseException {
		// find idx of last page with element <= clust
		int start = 0, end = pages.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			Page p = getPage(mid);
			int x = p.contains(ClusteringKey);
			if (x >= 0) {
				ans = mid;
				start = mid + 1;
			} else {
				end = mid - 1;
			}
		}
		return ans;
	}

	public int[] Correctplace(String strClusteringKey) throws IOException, ClassNotFoundException, ParseException {
		int[] ans = new int[2];
		if (pages.size() == 0)
			return ans;
		for (int i = 0; i < pages.size(); i++) {
			Page p = getPage(i);
			if (p.contains(strClusteringKey) > 0)
				continue;
			int lo = 0;
			int hi = p.size() - 1;
			ans[0] = i;
			while (lo <= hi) {
				int mid = (lo + hi) >> 1;
				if (p.get(mid).compareToClust(strClusteringKey) < 0) {
					lo = mid + 1;
				} else {
					hi = mid - 1;
					ans[1] = mid;
				}
			}
			return ans;
		}
		ans[0] = pages.size() - 1;
		ans[1] = getPage(pages.size() - 1).size();
		return ans;
	}

	public int getTupleSize() {
		return tupleSize;
	}

	public void update(int fst, Object[] rep, String clustKey) throws IOException, ClassNotFoundException {
		boolean flag = true;
		for (int i = fst; i < pages.size() && flag; i++) {
			Page p = getPage(i);
			flag &= p.replace(clustKey, rep);
		}
	}

	public void updateAt(int stp, int sti, Object[] rep, String clustKey) throws Exception {
		int flag = 0;
		int start = meta.getTablePos(tableName);
		// System.out.println(stp+" "+sti);
		for (int i = stp; i < pages.size() & flag < 2; i++) {
			Page p = getPage(i);
			for (int j = sti; j < p.size() & flag < 2; j++) {
				if (p.get(j).clusterKey().equals(clustKey)) {
					// System.out.println(i+" "+j);
					for (int o = 0; o < rep.length; o++) {
						if (rep[o] != null) {
							Object old = p.get(j).get(o);
							p.get(j).put(o, rep[o]);
							String s = meta.arr.get(o + start).ColoumnName;
							int pos = o + start;
							String k = meta.arr.get(pos).ColoumnType;
							if (indexCoulmn.containsKey(s)) {
								if (k.equals("java.lang.Integer")) {
									BPTree<Integer> b = getBPTree(s);
									b.deleteO_E((Integer) old, p.get(j).getRef());
									b.insertO_E((Integer) rep[o], p.get(j).getRef());
								} else if (k.equals("java.lang.String")) {
									BPTree<String> b = getBPTree(s);
									b.deleteO_E((String) old, p.get(j).getRef());
									b.insertO_E((String) rep[o], p.get(j).getRef());
								} else if (k.equals("java.lang.Double")) {
									BPTree<Double> b = getBPTree(s);
									b.deleteO_E((Double) old, p.get(j).getRef());
									b.insertO_E((Double) rep[o], p.get(j).getRef());
								} else if (k.equals("java.util.Date")) {
									BPTree<Date> b = getBPTree(s);
									b.deleteO_E((Date) old, p.get(j).getRef());
									b.insertO_E((Date) rep[o], p.get(j).getRef());
								} else if (k.equals("StairwayToHeaven.Polygon")) {
									RTree<Polygon> b = getRTree(s);
									b.deleteO_E((Polygon) old, p.get(j).getRef(), ((Polygon) old).getPoints());
									b.insertO_E((Polygon) rep[o], p.get(j).getRef(), ((Polygon) rep[o]).getPoints());
								}
							}
						}
					}
				} else
					flag++;
			}
			sti = 0;
			p.writePage();
		}
	}

	public Tuple getTuple(Ref r) throws ClassNotFoundException, IOException {
		return getPage(r.getPage()).get(r.getIndexInPage());
	}

	public BPTree getBPTree(String columnName) throws Exception {
		String fileName = indexCoulmn.get(columnName);
		File f = new File(fileName);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		BPTree t = (BPTree) ois.readObject();
		ois.close();
		return t;
	}

	public RTree<Polygon> getRTree(String columnName) throws Exception {
		String fileName = indexCoulmn.get(columnName);
		File f = new File(fileName);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		RTree<Polygon> t = (RTree<Polygon>) ois.readObject();
		ois.close();
		return t;
	}

}
