package sample.Model;

import java.util.*;

public class RankedDocumentsStructure {
    public String queryId;
    public HashMap<String, Double> documents = new HashMap<>();
    public ArrayList<Pair> topRankedDocs;


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
    public void onlyBest50(){
        List<Double> scores = new ArrayList<>(); //contain all scores from documents.
        scores.addAll(documents.values());
        Collections.sort(scores,Collections.reverseOrder()); // sort in decanting
        int size;
        if (scores.size()<50)
            size=scores.size();
        else
            size=50;
        topRankedDocs=new ArrayList<>();
        for (int i=0; i<size; i++){
            double curScore=scores.get(i);
            String key=getKeyWithSpecificValue(curScore);
            Pair pair=new Pair (key,curScore);
            topRankedDocs.add(pair);
        }
         Collections.sort(topRankedDocs,new rankingComperaor());

    }

    class Pair{
        String docId;
        Double score;
        Pair(String docId,Double score){
            this.docId=docId;
            this.score=score;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    class rankingComperaor implements Comparator<Pair> {
        /**
         *  compares between two strings according to CASE_INSENSITIVE_ORDER, default by Java.
         * @param o1 - String
         * @param o2 - String
         * @return int [>1,0,<-1]
         */
        @Override
        public int compare(Pair o1, Pair o2) {
          if(o1.getScore()<o2.getScore())
              return 1;
          else if(o1.getScore()>o2.getScore())
              return -1;
          else
              return 0;
        }
    }


}
