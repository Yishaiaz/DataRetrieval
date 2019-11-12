package sample.Model.DataStructures;

import java.util.ArrayList;

public class TermHashMapEntry {
    private String value;
    private int TF = 0;


    public ArrayList<Integer> termLocations = new ArrayList<>();

    public TermHashMapEntry(String value) {
        this.value = value;
    }

    public void addLocation(int location) {
        TF++;
        termLocations.add(location);
    }

    public String getValue() {
        return value;
    }

    public int getTF() {
        return TF;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTF(int TF) {
        this.TF = TF;
    }
}
