package StairwayToHeaven;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBApp implements Serializable {
	private static final long serialVersionUID = 1L;
	private Vector<String> database;
	private String metafile, configfile, DBpath;
	private static long pageIDGenerator;
	private long offset;
	private transient Properties prop;
	private transient CSV meta;

	/**
	 * Creates a new instance of the database if the database is is already present
	 * it loads it
	 */

	public DBApp() {
		metafile = "data/" + "metadata" + ".csv";
		configfile = "config/" + "DBApp" + ".properties";
		DBpath = "data";
		init();
	}

	/**
	 * initializes the metadata file and the configuration file set the maximum rows
	 * count in page and node size in the configuration file
	 */

	public void init() {
		try {
			initDirectories();
			meta = new CSV(metafile);
			setProp();
			read();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * retrieves the names of files of the tables in the database
	 * 
	 * @throws IOException            in case of I/O error
	 * @throws ClassNotFoundException in case class is not found
	 */

	private void read() throws IOException, ClassNotFoundException {
		File f = new File("data/DBApp.class");
		if (f.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			DBApp db = (DBApp) ois.readObject();
			database = db.database;
			pageIDGenerator = db.offset;
			ois.close();
		} else {
			database = new Vector<>();
		}
	}

	private void readCSV() throws Exception {
		meta = new CSV(metafile);
	}

	public Vector<String> getTableNames() {
		return this.database;
	}

	/**
	 * stores the filenames of all tables as a vector object
	 * 
	 * @throws IOException in case of I/O error
	 */

	private void write() throws IOException {
		offset = pageIDGenerator + 1;
		File f = new File("data/DBApp.class");
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this);
		oos.close();

	}

	/**
	 * loads the configuration file if present if not it is generated
	 * 
	 * @throws IOException
	 */

	private void setProp() throws IOException {
		prop = new Properties();
		if (new File(configfile).exists()) {
			FileReader reader = new FileReader(configfile);
			prop.load(reader);
		} else {
			prop.put("MaximumRowsCountinPage", "13");
			prop.put("NodeSize", "15");
			File config = new File(configfile);
			config.createNewFile();
			FileOutputStream fos = new FileOutputStream(config);
			prop.store(fos, "");
		}
	}

	/**
	 * Creates a new directory in a specific relative path
	 *
	 * @param DirectoryPath indicates the path of the directory to be created
	 */

	private void createDirectory(String DirectoryPath) {
		File f = new File(DirectoryPath);
		f.mkdirs();
	}

	/**
	 * initializes all necessary directories if not already initialized
	 */

	private void initDirectories() {
		if (!new File("data").exists())
			createDirectory("data");
		if (!new File("config").exists())
			createDirectory("config");
		if (!new File("classes").exists())
			createDirectory("classes");
	}

	/**
	 * retrieves the corresponding table in the database
	 *
	 * @param idx specifies the index of the table to be retrieved
	 * @return Table object after reading it from file
	 * @throws Exception
	 */

	private Table getTable(int idx) throws Exception {
		String filename = database.get(idx) + ".class";
		File f = new File(DBpath + '/' + filename);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		Table t = (Table) ois.readObject();
		t.readCSV();
		ois.close();
		return t;
	}

	/**
	 * creates a new table with a unique table name
	 *
	 * @param strTableName           specifies the unique table name
	 * @param strClusteringKeyColumn specifies the column that will be indexed later
	 *                               and table is sorted upon
	 * @param htblColNameType        specifies all columns present in the table and
	 *                               their corresponding data type
	 * @throws DBAppException if the table already exists an error will be thrown if
	 *                        the data types of columns are not supported error will
	 *                        be thrown throws any I/O error
	 */

	public void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws DBAppException {
		for (String s : database) {
			if (s.equals(strTableName)) {
				throw new DBAppException("Table already exists");
			}
		}

		for (Map.Entry<String, String> mp : htblColNameType.entrySet()) {
			if (!supportedDataType(mp.getValue())) {
				throw new DBAppException("Data Type is wrong");
			}
		}

		try {
			meta.addTableToCSV(strTableName, strClusteringKeyColumn, htblColNameType);

			int maxPageSize = Integer.parseInt(prop.getProperty("MaximumRowsCountinPage"));
			int tupleSize = meta.getTupleSize(strTableName);
			int clustKeyIdx = meta.findClustKeyIdx(strTableName);
			new Table(strTableName, DBpath, maxPageSize, tupleSize, clustKeyIdx, meta, strClusteringKeyColumn);
			database.add(strTableName);
			write();
		} catch (IOException e) {
			throw new DBAppException(e.getMessage());
		}
	}

	/**
	 * checks if the dataType is supported by the program or not
	 *
	 * @param dataType specifies the dataType to be checked
	 * @return boolean indicating whether supported or not
	 */

	private boolean supportedDataType(String dataType) {
		TreeSet<String> ts = new TreeSet<>();
		ts.add("java.lang.Integer");
		ts.add("java.lang.String");
		ts.add("java.lang.Double");
		ts.add("java.lang.Boolean");
		ts.add("java.util.Date");
		ts.add("java.awt.Polygon");
		ts.add("StairwayToHeaven.Polygon");
		return ts.contains(dataType);
	}

	/**
	 * returns the index of the table with specific index in the database
	 *
	 * @param strTableName specifies the table name
	 * @return the index of the table if the table is not found -1 will be returned
	 */

	private int findTableIdx(String strTableName) {
		for (int i = 0; i < database.size(); i++) {
			if (database.get(i).equals(strTableName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * checks if the given tuple contains no null values
	 *
	 * @param t the tuple to be checked
	 * @return boolean whether the tuple contains null values or not
	 */

	private boolean validTuple(Tuple t) {
		for (int i = 0; i < t.size(); i++) {
			if (t.get(i) == null)
				return false;
		}
		return true;
	}

	/**
	 * inserts a valid tuple in the table in it's correct position according to the
	 * clustering key index
	 *
	 * @param strTableName     name of the table to insert tuple in it
	 * @param htblColNameValue values in the tuple as objects and their
	 *                         corresponding columns
	 * @throws DBAppException for table not found, column not found incompatible
	 *                        type,missing input and I/O errors
	 */

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		int idx = findTableIdx(strTableName);
		if (idx == -1) {
			throw new DBAppException("Table not found");
		}

		int start = meta.getTablePos(strTableName);
		int sz = meta.getTupleSize(strTableName);
		int clustKeyIdx = meta.findClustKeyIdx(strTableName);
		Tuple tuple = new Tuple(sz, clustKeyIdx);
		String ck = "";
		for (Map.Entry<String, Object> mp : htblColNameValue.entrySet()) {
			int pos = meta.findColIdx(strTableName, mp.getKey());
			String type = mp.getValue().getClass().getName();

			if (pos == -1) {
				throw new DBAppException("Coloumn not found");
			}
//			System.out.println(type);
//			System.out.println(meta.arr.get(pos).ColoumnType);
			if (!type.equals(meta.arr.get(pos).ColoumnType)) {
				if (!(type.equals("java.lang.String")
						&& meta.arr.get(pos).ColoumnType.equals("StairwayToHeaven.Polygon"))) {
					throw new DBAppException("Incompatible data type");
				}
			}
			if (pos - start == clustKeyIdx) {
				ck = "" + mp.getValue();
			}
			if (type.equals("java.lang.String") && meta.arr.get(pos).ColoumnType.equals("StairwayToHeaven.Polygon")) {
				Polygon p = new StairwayToHeaven.Polygon((String) mp.getValue());
				tuple.put(pos - start, p);
			} else {
				tuple.put(pos - start, mp.getValue());
			}
		}

		if (!validTuple(tuple)) {
			throw new DBAppException("Missing inputs");
		}

		try {
			Table t = getTable(idx);
			if (t.clustringKeyIndexed()) {
				String ss = t.getClustKeyName();
				TreeMap<String, String> tm = t.getIndexCoulmn();
				int pos = meta.findColIdx(strTableName, ss);
				String k = meta.arr.get(pos).ColoumnType;
				Ref cur = new Ref(0, 0);
				// System.out.println("search in");
				if (k.equals("java.lang.Integer")) {
					BPTree<Integer> b = t.getBPTree(ss);
					// System.out.println(b);
					// System.out.println((Integer) tuple.get(pos - start));
					cur = b.lowerBound((Integer) tuple.get(pos - start));
				} else if (k.equals("java.lang.String")) {
					BPTree<String> b = t.getBPTree(ss);
					cur = b.lowerBound((String) tuple.get(pos - start));
				} else if (k.equals("java.lang.Double")) {
					BPTree<Double> b = t.getBPTree(ss);
					cur = b.lowerBound((Double) tuple.get(pos - start));
				} else if (k.equals("java.util.Date")) {
					BPTree<Date> b = t.getBPTree(ss);
					cur = b.lowerBound((Date) tuple.get(pos - start));
				} else if (k.equals("StairwayToHeaven.Polygon")) {
					RTree<Polygon> b = t.getRTree(ss);
					cur = b.lowerBound((Polygon) tuple.get(pos - start));
				}
				if (cur == null)
					cur = new Ref(0, 0);
				else {
					cur = new Ref(cur.getPage(), cur.getIndexInPage() + 1);
				}
				// System.out.println(cur);
				for (String s : tm.keySet()) {
					pos = meta.findColIdx(strTableName, s);
					k = meta.arr.get(pos).ColoumnType;
					if (k.equals("java.lang.Integer")) {
						BPTree<Integer> b = t.getBPTree(s);
						b.insertO_E((Integer) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.lang.String")) {
						BPTree<String> b = t.getBPTree(s);
						b.insertO_E((String) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.lang.Double")) {
						BPTree<Double> b = t.getBPTree(s);
						b.insertO_E((Double) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.util.Date")) {
						BPTree<Date> b = t.getBPTree(s);
						b.insertO_E((Date) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("StairwayToHeaven.Polygon")) {
						RTree<Polygon> b = t.getRTree(ss);
						b.insertO_E((Polygon) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()),
								((Polygon) tuple.get(pos - start)).getPoints());
					}
				}
				// System.out.println("in method");
				// System.out.println(t.printindecies());
				tuple.setRef(cur.getPage(), cur.getIndexInPage());
				t.insertAt(tuple, cur.getPage(), cur.getIndexInPage());
			} else {
				TreeMap<String, String> tm = t.getIndexCoulmn();
				int[] a = t.Correctplace(ck);
				Ref cur = new Ref(a[0], a[1]);
//				System.out.println("here?");
				for (String s : tm.keySet()) {
					int pos = meta.findColIdx(strTableName, s);
					String k = meta.arr.get(pos).ColoumnType;
					if (k.equals("java.lang.Integer")) {
						BPTree<Integer> b = t.getBPTree(s);
						b.insertO_E((Integer) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.lang.String")) {
						BPTree<String> b = t.getBPTree(s);
						b.insertO_E((String) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.lang.Double")) {
						BPTree<Double> b = t.getBPTree(s);
						b.insertO_E((Double) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("java.util.Date")) {
						BPTree<Date> b = t.getBPTree(s);
						b.insertO_E((Date) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()));
					} else if (k.equals("StairwayToHeaven.Polygon")) {
						RTree<Polygon> b = t.getRTree(s);
						b.insertO_E((Polygon) tuple.get(pos - start), new Ref(cur.getPage(), cur.getIndexInPage()),
								((Polygon) tuple.get(pos - start)).getPoints());
					}
				}
				tuple.setRef(cur.getPage(), cur.getIndexInPage());
				t.insertAt(tuple, cur.getPage(), cur.getIndexInPage());
			}
			write();
		} catch (Exception e) {
			throw new DBAppException(e.getMessage());
		}
	}

	/**
	 * update all records with specific clustering key
	 *
	 * @param strTableName     the tableName of the table that will be updated
	 * @param strClusteringKey the clustering key to be searched for and if found
	 *                         the corresponding tuple will be updated
	 * @param htblColNameValue the new values of specific columns and their
	 *                         corresponding values as objects
	 * @throws DBAppException for table not found, no record with this key, column
	 *                        is not found, incompatible types and I/O errors
	 */

	public void updateTable(String strTableName, String strClusteringKey, Hashtable<String, Object> htblColNameValue)
			throws DBAppException {
		int idx = findTableIdx(strTableName);
		int start = meta.getTablePos(strTableName);
		if (idx == -1) {
			throw new DBAppException("Table doesn't exist");
		}

		try {
			Table t = getTable(idx);
			int[] pls = new int[2];
			if (t.clustringKeyIndexed()) {
				String ss = t.getClustKeyName();
				TreeMap<String, String> tm = t.getIndexCoulmn();
				int pos = meta.findColIdx(strTableName, ss);
				String k = meta.arr.get(pos).ColoumnType;
				Ref cur = new Ref(0, 0);
				if (k.equals("java.lang.Integer")) {
					BPTree<Integer> b = t.getBPTree(ss);
					cur = b.lowerBound(Integer.parseInt(strClusteringKey));
				} else if (k.equals("java.lang.String")) {
					BPTree<String> b = t.getBPTree(ss);
					cur = b.lowerBound(strClusteringKey);
				} else if (k.equals("java.lang.Double")) {
					BPTree<Double> b = t.getBPTree(ss);
					cur = b.lowerBound(Double.parseDouble(strClusteringKey));
				} else if (k.equals("java.util.Date")) {
					BPTree<Date> b = t.getBPTree(ss);
					cur = b.lowerBound(new SimpleDateFormat("yyyy-mm-dd").parse(strClusteringKey));
				} else if (k.equals("StairwayToHeaven.Polygon")) {
					RTree<Polygon> b = t.getRTree(ss);
					Polygon x = new Polygon(strClusteringKey);
					cur = b.lowerBound(x);
				}
				if (cur == null)
					cur = new Ref(0, 0);
				pls[0] = cur.getPage();
				pls[1] = cur.getIndexInPage();
			} else {
				pls = t.Correctplace(strClusteringKey);
			}
			// System.out.println(Arrays.toString(pls));
			Object[] rep = new Object[t.getTupleSize()];
			for (Map.Entry<String, Object> mp : htblColNameValue.entrySet()) {
				int pos = meta.findColIdx(strTableName, mp.getKey());
				String type = mp.getValue().getClass().getName();
				if (pos == -1) {
					throw new DBAppException("Coloumn is not found");
				}
				if (!type.equals(meta.arr.get(pos).ColoumnType)) {
					throw new DBAppException("Incompatible data type");
				}
				rep[pos - start] = mp.getValue();
			}
			// System.out.println("here");
			t.updateAt(pls[0], pls[1], rep, strClusteringKey);
		} catch (Exception e) {
			throw new DBAppException(e.getMessage());
		}
	}

	/**
	 * deletes tuples with the specific values
	 *
	 * @param strTableName     the table to be deleted from
	 * @param htblColNameValue all values as objects that should be present in the
	 *                         tuple that will be deleted
	 * @throws DBAppException
	 */

	public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		int idx = findTableIdx(strTableName);
		if (idx == -1) {
			throw new DBAppException("Table doesn't exist");
		}

		try {
			Table t = getTable(idx);
			ArrayList<Integer> upd = new ArrayList<>();
			String key = null;
			TreeMap<String, String> tm = t.getIndexCoulmn();
			for (Map.Entry<String, Object> mp : htblColNameValue.entrySet()) {
				if (tm.containsKey(mp.getKey())) {
					key = mp.getKey();
					break;
				}
			}
		//	System.out.println(key);
			if (key != null) {
				// System.out.println(1);
				Vector<Ref> del = new Vector<>();
				int pos = meta.findColIdx(strTableName, key);
				String k = meta.arr.get(pos).ColoumnType;
				if (k.equals("java.lang.Integer")) {
					BPTree<Integer> b = t.getBPTree(key);
					del = b.getBucket((Integer) htblColNameValue.get(key));
				} else if (k.equals("java.lang.String")) {
					BPTree<String> b = t.getBPTree(key);
					del = b.getBucket((String) htblColNameValue.get(key));
				} else if (k.equals("java.lang.Double")) {
					BPTree<Double> b = t.getBPTree(key);
					del = b.getBucket((Double) htblColNameValue.get(key));
				} else if (k.equals("java.util.Date")) {
					BPTree<Date> b = t.getBPTree(key);
					del = b.getBucket((Date) htblColNameValue.get(key));
				} else if (k.equals("StairwayToHeaven.Polygon")) {
					RTree<Polygon> b = t.getRTree(key);
					Polygon x = new Polygon((String) htblColNameValue.get(key));
					del = b.getBucket(x);
				}
			//	System.out.println(del);
				if (del == null)
					return;
				// System.out.println(del);
				int start = meta.getTablePos(strTableName);
				TreeSet<Integer>[] adj = new TreeSet[t.size()];

				for (int i = 0; i < del.size(); i++) {
					int pa = del.get(i).getPage();
					int id = del.get(i).getIndexInPage();
					if (adj[pa] != null)
						adj[pa].add(id);
					else {
						adj[pa] = new TreeSet<>();
						adj[pa].add(id);
					}
				}
			//	 System.out.println(Arrays.toString(adj));
				for (int i = 0; i < t.size(); i++) {
					if (adj[i] == null)
						continue;
					int shift = 0;
					for (int y : adj[i]) {
						int pa = i;
						int id = y - shift;
						int counter = 0;
						Page p = t.getPage(pa);
						for (Map.Entry<String, Object> mp : htblColNameValue.entrySet()) {
							pos = meta.findColIdx(strTableName, mp.getKey());
							String type = mp.getValue().getClass().getName();
							if (pos == -1) {
								throw new DBAppException("Coloumn is not found");
							}
							if (meta.arr.get(pos).ColoumnType.equals("StairwayToHeaven.Polygon")) {
								if (!type.equals("java.lang.String")) {
									throw new DBAppException("Incompatible data type");
								}
								Polygon x=new Polygon((String)mp.getValue());
						//		System.out.println(x);
								if (p.get(id).get(pos - start) != null
										&& ((Polygon)p.get(id).get(pos - start)).equalsp(x.getPoints()))
									counter++;
					//		System.out.println(counter);	
							} else {
								if (!type.equals(meta.arr.get(pos).ColoumnType)) {
									throw new DBAppException("Incompatible data type");
								}
								if (p.get(id).get(pos - start) != null
										&& p.get(id).get(pos - start).equals(mp.getValue()))
									counter++;
							}
						}
						if (counter == htblColNameValue.size()) {
							upd.add(pa);
							Ref cur = p.get(id).getRef();
							// System.out.println(cur);
							for (String s : tm.keySet()) {
								pos = meta.findColIdx(strTableName, s);
								k = meta.arr.get(pos).ColoumnType;
								if (k.equals("java.lang.Integer")) {
									BPTree<Integer> b = t.getBPTree(s);
									b.deleteO_E((Integer) p.get(id).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.lang.String")) {
									BPTree<String> b = t.getBPTree(s);
									b.deleteO_E((String) p.get(id).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.lang.Double")) {
									BPTree<Double> b = t.getBPTree(s);
									b.deleteO_E((Double) p.get(id).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.util.Date")) {
									BPTree<Date> b = t.getBPTree(s);
									b.deleteO_E((Date) p.get(id).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("StairwayToHeaven.Polygon")) {
									RTree<Polygon> b = t.getRTree(s);
									b.deleteO_E((Polygon) p.get(id).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()),
											((Polygon) p.get(id).get(pos - start)).getPoints());
								}
							}
							p.remove(id);
							shift++;
						}
					}
				}
				// System.out.println(upd);
			} else {
				int start = meta.getTablePos(strTableName);
				for (int i = 0; i < t.size(); i++) {
					Page p = t.getPage(i);
					for (int j = 0; j < p.size(); j++) {
						int counter = 0;
						for (Map.Entry<String, Object> mp : htblColNameValue.entrySet()) {
							int pos = meta.findColIdx(strTableName, mp.getKey());
							pos = meta.findColIdx(strTableName, mp.getKey());
							String type = mp.getValue().getClass().getName();
							if (pos == -1) {
								throw new DBAppException("Coloumn is not found");
							}
							if (meta.arr.get(pos).ColoumnType.equals("StairwayToHeaven.Polygon")) {
								if (!type.equals("java.lang.String")) {
									throw new DBAppException("Incompatible data type");
								}
								Polygon x=new Polygon((String)mp.getValue());
								if (p.get(j).get(pos - start) != null
										&& ((Polygon)p.get(j).get(pos - start)).equalsp(x.getPoints()))
									counter++;
							} else {
								if (!type.equals(meta.arr.get(pos).ColoumnType)) {
									throw new DBAppException("Incompatible data type");
								}
								if (p.get(j).get(pos - start) != null
										&& p.get(j).get(pos - start).equals(mp.getValue()))
									counter++;
							}
						}
						if (counter == htblColNameValue.size()) {
							upd.add(i);
							Ref cur = p.get(j).getRef();
							for (String s : tm.keySet()) {
								int pos = meta.findColIdx(strTableName, s);
								String k = meta.arr.get(pos).ColoumnType;
								if (k.equals("java.lang.Integer")) {
									BPTree<Integer> b = t.getBPTree(s);
									b.deleteO_E((Integer) p.get(j).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.lang.String")) {
									BPTree<String> b = t.getBPTree(s);
									b.deleteO_E((String) p.get(j).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.lang.Double")) {
									BPTree<Double> b = t.getBPTree(s);
									b.deleteO_E((Double) p.get(j).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("java.util.Date")) {
									BPTree<Date> b = t.getBPTree(s);
									b.deleteO_E((Date) p.get(j).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()));
								} else if (k.equals("StairwayToHeaven.Polygon")) {
									RTree<Polygon> b = t.getRTree(s);
									b.deleteO_E((Polygon) p.get(j).get(pos - start),
											new Ref(cur.getPage(), cur.getIndexInPage()),
											((Polygon) p.get(j).get(pos - start)).getPoints());
								}
							}
							p.remove(j);
							j--;
						}
					}
				}
			}

			int st = Integer.MAX_VALUE;
			for (int i = 0; i < t.size(); i++) {
				Page p = t.getPage(i);
				if (p.isEmpty()) {
					String filename = p.getFile();
					File f = new File(filename);
					f.delete();
					t.remove(i);
					st = Math.min(st, i);
					i--;
				}
			}
			// System.out.println(st);

			int start = meta.getTablePos(strTableName);
			if (st != Integer.MAX_VALUE) {
				for (int i = st; i < t.size(); i++) {
					Page p = t.getPage(i);
					int ii = i;
					for (int j = 0; j < p.size(); j++) {
						Tuple tuple = p.get(j);
						for (String s : tm.keySet()) {
							int pos = meta.findColIdx(strTableName, s);
							String k = meta.arr.get(pos).ColoumnType;
							if (k.equals("java.lang.Integer")) {
								BPTree<Integer> b = t.getBPTree(s);
								b.updateKey((Integer) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.lang.String")) {
								BPTree<String> b = t.getBPTree(s);
								b.updateKey((String) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.lang.Double")) {
								BPTree<Double> b = t.getBPTree(s);
								b.updateKey((Double) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.util.Date")) {
								BPTree<Date> b = t.getBPTree(s);
								b.updateKey((Date) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("StairwayToHeaven.Polygon")) {
								RTree<Polygon> b = t.getRTree(s);
								b.updateKey((Polygon) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j),
										((Polygon) tuple.get(pos - start)).getPoints());
								// tuple.setRef(ii, j);
							}
						}
						tuple.setRef(ii, j);
					}
					p.writePage();
				}
			} else {
				for (int i = 0; i < upd.size(); i++) {
					Page p = t.getPage(upd.get(i));
					int ii = upd.get(i);
					for (int j = 0; j < p.size(); j++) {
						Tuple tuple = p.get(j);
						for (String s : tm.keySet()) {
							int pos = meta.findColIdx(strTableName, s);
							String k = meta.arr.get(pos).ColoumnType;
							if (k.equals("java.lang.Integer")) {
								BPTree<Integer> b = t.getBPTree(s);
								// System.out.println(tuple.getRef());
								b.updateKey((Integer) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.lang.String")) {
								BPTree<String> b = t.getBPTree(s);
								b.updateKey((String) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.lang.Double")) {
								BPTree<Double> b = t.getBPTree(s);
								b.updateKey((Double) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("java.util.Date")) {
								BPTree<Date> b = t.getBPTree(s);
								b.updateKey((Date) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j));
								// tuple.setRef(ii, j);
							} else if (k.equals("StairwayToHeaven.Polygon")) {
								RTree<Polygon> b = t.getRTree(s);
								b.updateKey((Polygon) tuple.get(pos - start), tuple.getRef(), new Ref(ii, j),
										((Polygon) tuple.get(pos - start)).getPoints());

							}

						}
						tuple.setRef(ii, j);
					}
					p.writePage();
				}
			}
			t.writeTable();
		} catch (Exception e) {
			throw new DBAppException(e.getMessage());
		}
	}

	public void createBTreeIndex(String strTableName, String strColName) throws DBAppException {
		int idx = findTableIdx(strTableName);
		if (idx == -1) {
			throw new DBAppException("no table with this name");
		}
		try {
			Table t = getTable(idx);
			if (t.containsIndex(strColName)) {
				throw new DBAppException("there is indexing on this column");
			} else {
				int sz = Integer.parseInt(prop.getProperty("NodeSize"));
				int pos = meta.findColIdx(strTableName, strColName);
				int start = meta.getTablePos(strTableName);
				String k = meta.arr.get(pos).ColoumnType;
				String fileName = "data/" + strTableName + strColName + ".class";
				if (k.equals("java.lang.Integer")) {
					BPTree<Integer> tree = new BPTree<Integer>(sz, fileName);
					for (int i = 0; i < t.size(); i++)
						for (int j = 0; j < t.getPage(i).size(); j++)
							tree.insertO_E((Integer) t.getPage(i).get(j).get(pos - start), new Ref(i, j));

					t.addIndex(strColName, fileName);
				} else if (k.equals("java.lang.String")) {
					BPTree<String> tree = new BPTree<String>(sz, fileName);
					for (int i = 0; i < t.size(); i++)
						for (int j = 0; j < t.getPage(i).size(); j++)
							tree.insertO_E((String) t.getPage(i).get(j).get(pos - start), new Ref(i, j));
					t.addIndex(strColName, fileName);
				} else if (k.equals("java.lang.Double")) {
					BPTree<Double> tree = new BPTree<Double>(sz, fileName);
					for (int i = 0; i < t.size(); i++)
						for (int j = 0; j < t.getPage(i).size(); j++)
							tree.insertO_E((Double) t.getPage(i).get(j).get(pos - start), new Ref(i, j));
					t.addIndex(strColName, fileName);
				} else if (k.equals("java.util.Date")) {
					BPTree<Date> tree = new BPTree<Date>(sz, fileName);
					for (int i = 0; i < t.size(); i++)
						for (int j = 0; j < t.getPage(i).size(); j++)
							tree.insertO_E((Date) t.getPage(i).get(j).get(pos - start), new Ref(i, j));
					t.addIndex(strColName, fileName);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createRTreeIndex(String strTableName, String strColName) throws DBAppException {
		int idx = findTableIdx(strTableName);
		if (idx == -1) {
			throw new DBAppException("no table with this name");
		}
		try {
			Table t = getTable(idx);
			if (t.containsIndex(strColName)) {
				throw new DBAppException("there is indexing on this column");
			} else {
				int pos = meta.findColIdx(strTableName, strColName);
				int start = meta.getTablePos(strTableName);
				String k = meta.arr.get(pos).ColoumnType;
				int sz = Integer.parseInt(prop.getProperty("NodeSize"));
				String fileName = "data/" + strTableName + strColName + ".class";
				if (k.equals("StairwayToHeaven.Polygon")) {
					RTree<Polygon> tree = new RTree<Polygon>(sz, fileName);
					for (int i = 0; i < t.size(); i++)
						for (int j = 0; j < t.getPage(i).size(); j++)
							tree.insertO_E((Polygon) t.getPage(i).get(j).get(pos - start), new Ref(i, j),
									((Polygon) t.getPage(i).get(j).get(pos - start)).getPoints());
					t.addIndex(strColName, fileName);
				} else {
					throw new DBAppException("this column is not a polygon");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
		try {
			readCSV();
			TreeSet<Ref> res = new TreeSet<>();
			for (int i = 0; i < arrSQLTerms.length; i++) {
				TreeSet<Ref> cur = search(arrSQLTerms[i]);
				res = combine(res, cur, (i == 0 ? "OR" : strarrOperators[i - 1]));
			}

			Vector<Tuple> ret = convert(res, arrSQLTerms[0]._strTableName);
			return ret.iterator();
		} catch (Exception e) {
			throw new DBAppException(e.getMessage());
		}
	}

	private TreeSet<Ref> search(SQLTerm sqlTerm) throws Exception {
		if (meta.isIndexed(sqlTerm._strTableName, sqlTerm._strColumnName)) {
			int pos = meta.findColIdx(sqlTerm._strTableName, sqlTerm._strColumnName);
			if (meta.arr.get(pos).ColoumnType.equals("StairwayToHeaven.Polygon"))
				return searchByRTree(sqlTerm);
			return searchByBPTree(sqlTerm);
		}

		if (meta.isClustKey(sqlTerm._strTableName, sqlTerm._strColumnName)) {
			return BinarySearch(sqlTerm);
		}

		return searchLinear(sqlTerm);
	}

	@SuppressWarnings("unchecked")
	private TreeSet<Ref> searchByRTree(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		String col = sqlTerm._strColumnName;
		Polygon key = (Polygon) (sqlTerm._objValue);
		TreeSet<Ref> ret = new TreeSet<>();
		switch (sqlTerm._strOperator) {
		case "<":
			ret.addAll(t.getRTree(col).lowerThan(key));
			return ret;
		case "<=":
			ret.addAll(t.getRTree(col).lowerThanOrEqual(key));
			return ret;
		case "=":
			ret.addAll(t.getRTree(col).getBucket(key));
			return ret;
		case ">=":
			ret.addAll(t.getRTree(col).biggerThanOrEqual(key));
			return ret;
		case ">":
			ret.addAll(t.getRTree(col).biggerThan(key));
			return ret;
		case "!=":
			ret.addAll(t.getRTree(col).lowerThan(key));
			ret.addAll(t.getRTree(col).biggerThan(key));
			return ret;
		default:
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	private TreeSet<Ref> searchByBPTree(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		String col = sqlTerm._strColumnName;
		Comparable key = (Comparable) sqlTerm._objValue;
		TreeSet<Ref> ret = new TreeSet<>();
		switch (sqlTerm._strOperator) {
		case "<":
			ret.addAll(t.getBPTree(col).lowerThan(key));
			return ret;
		case "<=":
			ret.addAll(t.getBPTree(col).lowerThanOrEqual(key));
			;
			return ret;
		case "=":
			ret.addAll(t.getBPTree(col).getBucket(key));
			return ret;
		case ">=":
			ret.addAll(t.getBPTree(col).biggerThanOrEqual(key));
			return ret;
		case ">":
			ret.addAll(t.getBPTree(col).biggerThan(key));
			return ret;
		case "!=":
			ret.addAll(t.getBPTree(col).lowerThan(key));
			ret.addAll(t.getBPTree(col).biggerThan(key));
			return ret;
		default:
			return ret;
		}
	}

	private TreeSet<Ref> BinarySearch(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		TreeSet<Ref> ret = new TreeSet<>();
		Ref r = new Ref(-1, -1);
		switch (sqlTerm._strOperator) {
		case "<":
			r = floor(sqlTerm);
			if (r.getPage() == -1 || r.getIndexInPage() == -1) {
				return ret;
			}
			for (int i = 0; i <= r.getPage(); i++) {
				Page p = t.getPage(i);
				for (int j = 0; j < p.size(); j++) {
					ret.add(new Ref(i, j));
					if (new Ref(i, j).equals(r))
						return ret;
				}
			}
			break;
		case "<=":
			r = lower(sqlTerm);
			if (r.getPage() == -1 || r.getIndexInPage() == -1) {
				return ret;
			}
			for (int i = 0; i <= r.getPage(); i++) {
				Page p = t.getPage(i);
				for (int j = 0; j < p.size(); j++) {
					ret.add(new Ref(i, j));
					if (new Ref(i, j).equals(r))
						return ret;
				}
			}
			break;
		case ">=":
			r = upper(sqlTerm);

			if (r.getPage() == -1 || r.getIndexInPage() == -1) {
				return ret;
			}

			for (int i = r.getPage(); i < t.size(); i++) {
				Page p = t.getPage(i);
				int j = (i == r.getPage() ? r.getIndexInPage() : 0);
				for (; j < p.size(); j++) {
					ret.add(new Ref(i, j));
				}
			}
			break;
		case ">":
			r = ceil(sqlTerm);
			if (r.getPage() == -1 || r.getIndexInPage() == -1) {
				return ret;
			}

			for (int i = r.getPage(); i < t.size(); i++) {
				Page p = t.getPage(i);
				int j = (i == r.getPage() ? r.getIndexInPage() : 0);
				for (; j < p.size(); j++) {
					ret.add(new Ref(i, j));
				}
			}

			break;
		case "=":
			Ref le = upper(sqlTerm);
			Ref ri = lower(sqlTerm);

			if (ri.getPage() == -1 || ri.getIndexInPage() == -1) {
				return ret;
			}
			if (le.getPage() == -1 || le.getIndexInPage() == -1) {
				return ret;
			}

			for (int i = le.getPage(); i <= ri.getPage(); i++) {
				Page p = t.getPage(i);
				int j = (i == le.getPage() ? le.getIndexInPage() : 0);
				for (; j < p.size(); j++) {
					ret.add(new Ref(i, j));
					if (new Ref(i, j).equals(r))
						return ret;
				}
			}
			break;
		case "!=":
			ret = searchLinear(sqlTerm);
			break;
		}

		return ret;
	}

	private Ref lower(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		Object o = sqlTerm._objValue;
		int idx = t.findLastClustKeyInstance(o);
		if (idx == -1)
			return new Ref(-1, -1);
		Page p = t.getPage(idx);
		int start = 0, end = p.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			if (p.get(mid).compareToClust(o) <= 0) {
				ans = mid;
				start = mid + 1;
			} else {
				end = mid - 1;
			}
		}
		return new Ref(idx, ans);
	}

	private Ref floor(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		Object o = sqlTerm._objValue;
		int idx = t.findLastClustKeyInstance(o);
		while (idx >= 0 && compare(t.getPage(idx).firstKeyValue(), o) == 0)
			idx--;
		if (idx == -1)
			return new Ref(-1, -1);
		Page p = t.getPage(idx);
		int start = 0, end = p.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			if (p.get(mid).compareToClust(o) < 0) {
				ans = mid;
				start = mid + 1;
			} else {
				end = mid - 1;
			}
		}
		return new Ref(idx, ans);

	}

	private Ref upper(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		Object o = sqlTerm._objValue;
		int idx = t.findFirstClustKeyInstance(o);
		if (idx == -1)
			return new Ref(-1, -1);
		Page p = t.getPage(idx);
		int start = 0, end = p.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			if (p.get(mid).compareToClust(o) >= 0) {
				ans = mid;
				end = mid - 1;
			} else {
				start = mid + 1;
			}
		}
		return new Ref(idx, ans);
	}

	private Ref ceil(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		Object o = sqlTerm._objValue;
		int idx = t.findFirstClustKeyInstance(o);
		while (idx > -1 && idx < t.size() && compare(t.getPage(idx).lastKeyValue(), o) == 0)
			idx++;
		if (idx < 0 || idx >= t.size())
			return new Ref(-1, -1);
		Page p = t.getPage(idx);
		int start = 0, end = p.size() - 1, ans = -1;
		while (start <= end) {
			int mid = (start + end) >> 1;
			if (p.get(mid).compareToClust(o) > 0) {
				ans = mid;
				end = mid - 1;
			} else {
				start = mid + 1;
			}
		}
		return new Ref(idx, ans);
	}

	private TreeSet<Ref> searchLinear(SQLTerm sqlTerm) throws Exception {
		Table t = getTable(findTableIdx(sqlTerm._strTableName));
		int idx = meta.findColIdxInTable(sqlTerm._strTableName, sqlTerm._strColumnName);
		TreeSet<Ref> st = new TreeSet<>();
		Object o = sqlTerm._objValue;
		for (int i = 0; i < t.size(); i++) {
			Page p = t.getPage(i);
			for (int j = 0; j < p.size(); j++) {
				if (valid(o, p.get(j).get(idx), sqlTerm._strOperator)) {
					st.add(new Ref(i, j));
				}
			}
		}
		return st;
	}

	private boolean valid(Object o1, Object o2, String operator) {
		int comp = compare(o1, o2);
		switch (operator) {
		case "<":
			return comp < 0;
		case "<=":
			return comp <= 0;
		case ">=":
			return comp >= 0;
		case ">":
			return comp > 0;
		case "=":
			return comp == 0;
		case "!=":
			return comp != 0;
		default:
			return false;
		}
	}

	private int compare(Object t2, Object t1) {
		String type = t2.getClass().getName();
		if (type.equals("java.lang.Integer")) {
			return ((Integer) t1).compareTo((Integer) t2);
		} else if (type.equals("java.lang.String")) {
			System.out.println("here");
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

	private TreeSet<Ref> combine(TreeSet<Ref> a, TreeSet<Ref> b, String operation) throws DBAppException {
		TreeSet<Ref> ret = new TreeSet<Ref>();
		if (operation.equals("OR")) {
			ret.addAll(a);
			ret.addAll(b);
			return ret;
		}

		if (operation.equals("AND")) {
			for (Ref r : a) {
				if (b.contains(r))
					ret.add(r);
			}
			return ret;
		}

		if (operation.equals("XOR")) {
			for (Ref r : a) {
				if (!b.contains(r))
					ret.add(r);
			}
			for (Ref r : b) {
				if (!a.contains(r))
					ret.add(r);
			}
			return ret;
		}

		throw new DBAppException("operation is not supported");
	}

	public Vector<Tuple> convert(TreeSet<Ref> st, String TableName) throws Exception {
		int idx = findTableIdx(TableName);
		Table t = getTable(idx);
		Vector<Tuple> ret = new Vector<>();
		for (Ref r : st) {
			ret.add(t.getTuple(r));
		}
		return ret;
	}

	public static Long getPageIDGenerator() {
		return ++pageIDGenerator;
	}

	public static void main(String[] args) throws Exception {
		String strTableName = "Student";
		DBApp dbApp = new DBApp();
		Hashtable htblColNameType = new Hashtable();
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		htblColNameType.put("geometry", "StairwayToHeaven.Polygon");
//	//	dbApp.createTable( strTableName, "geometry", htblColNameType );
		// dbApp.createRTreeIndex(strTableName, "geometry");
		// dbApp.createBTreeIndex(strTableName, "id");
//		System.out.println(dbApp.getTable(0).printindecies());

		Hashtable htblColNameValue = new Hashtable();
//		for (int i = 0; i < 50; i++) {
//			Random r = new Random();
//			String name = "";
//			for (int j = 0; j < 3; j++)
//				name += ("" + ((char) ('a' + r.nextInt(26))));
////			System.out.println(name + " " + i);
//			htblColNameValue = new Hashtable();
//			double u = 0;
//	//		if (i < 5)
//				u = 0.1;
////	System.out.println(i);
//
//			htblColNameValue.put("gpa", new Double(u));
//			htblColNameValue.put("id", new Integer(i));
//			htblColNameValue.put("name", new String(name));
//			if(i<20)
//			htblColNameValue.put("geometry", new String("(1,2),(3,4)"));
//			else
//				htblColNameValue.put("geometry", new String("(1,2),(3,4),(8,9),(2,1)"));
//			dbApp.insertIntoTable(strTableName, htblColNameValue);
//		}

//		SQLTerm[] arrSQLTerms = new SQLTerm[2];
//		arrSQLTerms[0] = new SQLTerm();
//		arrSQLTerms[0]._strTableName = "Student";
//		arrSQLTerms[0]._strColumnName = "geometry";
//		arrSQLTerms[0]._strOperator = "=";
//		arrSQLTerms[0]._objValue = new StairwayToHeaven.Polygon(new String("(2,3),(4,5)"));
//
//		arrSQLTerms[1] = new SQLTerm();
//		arrSQLTerms[1]._strTableName = "Student";
//		arrSQLTerms[1]._strColumnName = "id";
//		arrSQLTerms[1]._strOperator = "<";
//		arrSQLTerms[1]._objValue = new Integer(60);
//
//		String Operations[] = new String[1];
//		Operations[0] = "AND";
//
//		Iterator<Tuple> it = dbApp.selectFromTable(arrSQLTerms, Operations);
//		while (it.hasNext()) {
//			System.out.println(it.next());
//		}

//		for (int i = 0; i < 10; i++) {
//			htblColNameValue = new Hashtable();
//			htblColNameValue.put("id", new Integer(i));
//			dbApp.deleteFromTable(strTableName, htblColNameValue);
//			System.out.println("ompa");
//			System.out.println(dbApp.getTable(0).printindecies());
//		}
		htblColNameValue = new Hashtable();
		htblColNameValue.put("gpa", new Double(100));
		dbApp.updateTable(strTableName,"(1,2),(3,4),(8,9),(2,1)", htblColNameValue);;
		System.out.println(dbApp.getTable(0).size() + " size");
		for (int i = 0; i < dbApp.getTable(0).size(); i++) {
			for (int j = 0; j < dbApp.getTable(0).getPage(i).size(); j++)
				System.out.println(
						dbApp.getTable(0).getPage(i).get(j).get(2) + " " + dbApp.getTable(0).getPage(i).get(j).get(1)
								+ " " + dbApp.getTable(0).getPage(i).get(j).get(0));
		}

		System.out.println(dbApp.getTable(0).printindecies());
	}

}
