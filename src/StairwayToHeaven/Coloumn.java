package StairwayToHeaven;

public class Coloumn {
    String TableName;
    String ColoumnName;
    String ColoumnType;
    boolean Key;
    boolean Indexed;

    public Coloumn(String[] info) {
        this.TableName = info[0];
        this.ColoumnName = info[1];
        this.ColoumnType = info[2];
        this.Key = info[3].equals("True");
        this.Indexed = info[4].equals("True");

    }

    public String toString() {
        return TableName + "," + ColoumnName + "," + ColoumnType + "," + (Key ? "True" : "False") + "," + (Indexed ? "True" : "False");
    }
}
