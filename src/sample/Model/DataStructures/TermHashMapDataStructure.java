package sample.Model.DataStructures;

import java.util.ArrayList;
import java.util.HashMap;


public class TermHashMapDataStructure {
    public class TermHashMapEntry {
        public String value;
        public int TF=0;
        public ArrayList<Integer> termLocations = new ArrayList<>();

        public TermHashMapEntry(String value) {
            this.value = value;
        }

        public void addLocation(int location){
            TF++;
            termLocations.add(location);
        }
    }

    public String value;
    public int TF=0;
    public HashMap<String, TermHashMapEntry> termsEntries = new HashMap<>();

    public TermHashMapDataStructure() {

    }
    public void insert(String s, int iDFLocation){
        String entryValue = s;
        String key = s.toLowerCase();
        if (termsEntries.containsKey(key)){
            // אם אני בפנים, תעדכן לפי הצורך
            TermHashMapEntry entry = termsEntries.get(key);
//            entry.update();
        }else{

        }
    }

    public void SaveToQueue(){
        return;
    }
}