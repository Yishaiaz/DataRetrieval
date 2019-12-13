package sample.Model.DataStructures;

import java.util.HashMap;
/**
 *This structure holds Hash Map .
 * key is term in lower case
 * value is TermHashMapEntry
 * every Document has this TermHashMapDataStructure that holds all his terms.
 */
public class TermHashMapDataStructure {
    public HashMap<String, TermHashMapEntry> termsEntries = new HashMap<>();

    public TermHashMapDataStructure() {}
    /**
     * change value to new low case value when so far term apper only as Upper case and now we saw it in
     * lowwer case
     * @param newValue
     * @param weight
     */
    public void updateValueToLowCastValue(String newValue, double weight) {
        String key = newValue.toLowerCase();
        TermHashMapEntry entry = termsEntries.get(key);
        TermHashMapEntry newEntry = new TermHashMapEntry(newValue, weight);
        newEntry.setTF(entry.getTF());
        newEntry.termLocations = entry.termLocations;
        termsEntries.replace(key, entry, newEntry);
    }
    /**
     * insert new TermHashMapEntry to Hash Map termsEntries.
     * * @param s is term
     * @param iDFLocation location of term
     * @param weight weight of term
     */
    public void insert(String s, int iDFLocation, double weight) {
        String entryValue = s;
        String key = s.toLowerCase();
        // Term exist in structure.
        if (termsEntries.containsKey(key)) {
            TermHashMapEntry entry = termsEntries.get(key);
            // if we found term in low case while his current value is in upper case-
            //change to low case.
            if (isStringLowerCase(s) && !entry.getValue().equals(s))
                updateValueToLowCastValue(s, weight);

            termsEntries.get(key).addLocation(iDFLocation);
        } else {
            // in case its new Term in Structure.

            //in case word start with uppercase.
            if (Character.isUpperCase(s.charAt(0)))
                s=s.toUpperCase();

            TermHashMapEntry newEntry = new TermHashMapEntry(s, weight);
            newEntry.addLocation(iDFLocation);
            termsEntries.put(key, newEntry);
        }
    }

    private static boolean isStringLowerCase(String str) {

        //convert String to char array
        char[] charArray = str.toCharArray();

        for (int i = 0; i < charArray.length; i++) {

            //if any character is not in lower case, return false
            if (!Character.isLowerCase(charArray[i]))
                return false;
        }

        return true;
    }

    public void SaveToQueue() {
        return;
    }

    public int  howManyUniqTerms(){
        return termsEntries.size();
    }

    public int getMaxTf(){
        int max=0;
       for (TermHashMapEntry entry: termsEntries.values()){
           if (entry.getTF()>max) {
               max = entry.getTF();
           }
       }
           return max;
    }
}
