package sample.Model.DataStructures;

import java.util.ArrayList;
/**
 * This class is represent entry of term .
 * value- which is the term
 * tf - how many times term appears in doc
 * weight
 */
public class TermHashMapEntry {
    private String value;
    private int TF = 0;
    private double weight;


    public ArrayList<Integer> termLocations = new ArrayList<>();

    public TermHashMapEntry(String value, double weight) {
        this.value = value;
        this.weight = weight;
    }

    /**
     * increase location of term because its appears again in doc.
     * @param location
     */
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

    public double getWeight() {
        return weight;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTF(int TF) {
        this.TF = TF;
    }
}
