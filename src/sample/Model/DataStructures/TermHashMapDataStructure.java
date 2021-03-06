package sample.Model.DataStructures;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * This class help to hold all terms of one document. do it using HashMap
 */
public class TermHashMapDataStructure {
    public TreeMap<String, TermHashMapEntry> termsEntries = new TreeMap<>();

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
        if(StringUtils.equals(s, "!")){
            return;
        }
        if(StringUtils.startsWith(s,"-")){
            s = StringUtils.remove(s, "-");
        }
        if(s.length()<=1){
            return;
        }
        s = StringUtils.removeStartIgnoreCase(s, " ");
        s = StringUtils.removeStartIgnoreCase(s, "'");
        s = s.replaceFirst("^0+(?!$)", "");

        String key = s.toLowerCase();
        // Term exist in structure.
        if (termsEntries.containsKey(key)) {
            TermHashMapEntry entry = termsEntries.get(key);
            // if we found term in low case while his current value is in upper case-
            //change to low case.
            if (isStringLowerCase(s) && !entry.getValue().equals(s))
                updateValueToLowCastValue(s, weight);

            termsEntries.get(key).addLocation(iDFLocation);
        } else {     // in case its new Term in Structure.

            //in case word start with uppercase.
            if (Character.isUpperCase(s.charAt(0)))
                s=s.toUpperCase();

            TermHashMapEntry newEntry = new TermHashMapEntry(s, weight);
            newEntry.addLocation(iDFLocation);
            termsEntries.put(key, newEntry);
        }
    }

    /**
     * checks if all characters in str are in lower case.
     * @param str
     * @return
     */
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

    /**
     *
     * @return how many unique terms are in one doc
     */
    public int  howManyUniqTerms(){
        return termsEntries.size();
    }

    /**
     *
     * @return The term that most frequent in docs
     */
    public int getMaxTf(){
        int max=0;
       for (TermHashMapEntry entry: termsEntries.values()){
           if (entry.getTF()>max) {
               max = entry.getTF();
           }
       }
           return max;
    }

    /**
     * this function return only entities from doc
     * @return
     */
    public HashMap <String,Integer> getOnlyEntities(){
        HashMap<String,Integer> entities=new HashMap<>();
        for (String term: termsEntries.keySet()){
            if (termsEntries.get(term).getWeight()==1.7){
                entities.put(termsEntries.get(term).getValue(), termsEntries.get(term).getTF());
            }
        }
        return entities;
    }
}
