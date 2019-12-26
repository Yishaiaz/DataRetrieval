package sample.Model;

import java.util.*;

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
    public void insert(String docId,double score) {
        //if document already exist in documents
        if (documents.containsKey(docId)) {
            double newScore = documents.get(docId) + score;
            documents.replace(docId, newScore);
        }
        // if document not exist in documents.
        else {
            documents.put(docId, score);
        }
    }

    /**
     * given value , return key that satisfied <key,value> in documents.
     * @param value
     * @return
     */
    private String getKeyWithSpecificValue(double value){
        for (String curKey: documents.keySet()){
            if (documents.get(curKey).equals(value)){
               documents.remove(curKey);
               return curKey;
            }

        }
        return null;
    }

    /**
     * place only top 50 relevant docs in 'documents'
     */
    public void  onlyBest50(){
        HashMap<String, Double> topDocuments = new HashMap<>(); //will contain top documents
        List<Double> scores = new ArrayList<>(); //contain all scores from documents.
        scores.addAll(documents.values());
        Collections.sort(scores,Collections.reverseOrder()); // sort in decanting
        for (int i=0; i<50; i++){
            double curScore=scores.get(i);
            String key=getKeyWithSpecificValue(curScore);
            topDocuments.put(key,curScore);
        }
        documents=topDocuments;
    }
}
