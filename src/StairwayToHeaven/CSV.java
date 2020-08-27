package StairwayToHeaven;


import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class CSV {
	Vector<Coloumn> arr;
	String filePath;
	
	public CSV(String filePath) throws IOException { 
		this.filePath = filePath;
		File f = new File(filePath);
		if(!f.exists()) {
			f.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		arr = new Vector<>();
		String line;
		while ((line = br.readLine()) != null) {
			String[] info = line.split(",");
			arr.add(new Coloumn(info));
			
		}
	}
	public void addTableToCSV(String strTableName, String strClusteringKeyColumn , Hashtable<String,String> htblColNameType) throws IOException { // add table to csv
		for(Map.Entry<String,String> mp : htblColNameType.entrySet()){
			String info[] = new String[5];
			info[0] = strTableName;
			info[1] = mp.getKey();
			info[2] = mp.getValue();
			if(info[2].equals("java.awt.Polygon")) {
				info[2] = "StairwayToHeaven.Polygon";
			}
			info[3] = strClusteringKeyColumn.equals(mp.getKey()) ? "True" : "No";
			info[4] = "False";
			arr.add(new Coloumn(info));
		}
		writeToCSV();
	}


	public void writeToCSV() throws IOException {
		FileWriter csvWriter = new FileWriter(filePath);
		StringBuilder  x = new StringBuilder();
		for(Coloumn c : arr) {
			x.append(c).append("\n");
		}
		csvWriter.append(x).flush();
		csvWriter.close();
	}
	
	public int findColIdx(String TableName, String ColName){
		int start = getTablePos(TableName);
		int end = start + getTupleSize(TableName);
		for(int i = start; i < end; i++){
			if(TableName.equals(arr.get(i).TableName) && ColName.equals(arr.get(i).ColoumnName)){
				return i;
			}
		}
		return -1;
	}
	
	
	public int findColIdxInTable(String TableName, String ColName) {
		return findColIdx(TableName,ColName) - getTablePos(TableName);
	}

	public int findClustKeyIdx(String TableName){
		int start = getTablePos(TableName);
		int end = start + getTupleSize(TableName);
		for(int i = start; i < end; i++){
			if(arr.get(i).Key){
				return i;
			}
		}
		return -1;
	}


	public int getTablePos(String strTableName) {
		for(int i = 0; i< arr.size(); i++){
			if(arr.get(i).TableName.equals(strTableName))
				return i;
		}
		return -1;
	}

	public int getTupleSize(String TableName) {
		int cnt = 0;
		for(Coloumn  c: arr){
			if(c.TableName.equals(TableName)) cnt++;
		}
		return cnt;
	}
	
	public boolean isClustKey(String TableName, String ColName) {
		Coloumn c = arr.get(findColIdx(TableName,ColName));
		return c.Key;
	}
	
	public boolean isIndexed(String TableName, String ColName) {
		Coloumn c = arr.get(findColIdx(TableName,ColName));
		return c.Indexed;
	}
	
	public void indexCol(String TableName, String ColName) throws IOException {
		Coloumn c = arr.get(findColIdx(TableName,ColName));
		c.Indexed = true;
		writeToCSV();
	}

}
