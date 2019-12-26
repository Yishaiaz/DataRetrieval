import sample.Model.DataStructures.TermHashMapEntry;

import java.util.HashMap;

public class RankedDocumentsStructure {
    public HashMap<String, Double> documents = new HashMap<>();
    public String queryId;

    public RankedDocumentsStructure(String queryId) {
        this.queryId=queryId;
    }

    /**
     * if document already exist in documents structure , sum new score to exist score
     * else add entry <docId,score>
     * @param docId
     * @param score
     */
    public void insert(String docId,double score){
        //if document already exist in documents
        if (documents.containsKey(docId)){
            double newScore=documents.get(docId)+score;
            documents.replace(docId,newScore);
        }
        // if document not exist in documents.
        else{
            documents.put(docId,score);
        }
    }
}
