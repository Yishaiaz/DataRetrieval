package sample.Model;

import org.apache.commons.lang3.Pair;
import org.apache.commons.lang3.StringUtils;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.DataStructures.TermHashMapEntry;
import sample.Model.RankingAlgorithms.BM25RankingAlgorithm;
import sample.Model.RankingAlgorithms.IRankingAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Ranker {
    IRankingAlgorithm rankingAlgorithm;
    String pathToDictionary;
    String pathToPosting;
    String pathToDocInfo;
    int totalNumOfDocs;
    double docMaxTF;

    public Ranker(String pathToDictionary, String pathToPosting, String pathToDocInfo, int totalNumOfDocs, double docMaxTF) {
        this.pathToDictionary = pathToDictionary;
        this.pathToPosting = pathToPosting;
        this.pathToDocInfo = pathToDocInfo;
        this.totalNumOfDocs = totalNumOfDocs;
        this.docMaxTF = docMaxTF;
    }

    /**
     *
     * @param queryTerms
     */
    public void rankDocsForQuery(TermHashMapDataStructure queryTerms){
        Iterator<Entry<String, TermHashMapEntry>> iterator = queryTerms.termsEntries.entrySet().iterator();
        String[] termIDs = new String[queryTerms.termsEntries.size()];
        int i = 0;
        while (iterator.hasNext()) {
            // getting a single entry data
            Map.Entry<String, TermHashMapEntry> singleTerm = (Map.Entry<String, TermHashMapEntry>) iterator.next();
            String termID = singleTerm.getKey();
            termIDs[i] = termID;
            i += 1;
        }
        ArrayList<Map<Pair<String, String>, ArrayList>> termToDocData = collectAllTermsToDocData(termIDs);
        ArrayList<String> allQueryRelatedDocsID = getAllDocsID(termToDocData);
        // preparing all docs to term calculation values
        IRankingAlgorithm BM25Ranker = new BM25RankingAlgorithm(this.totalNumOfDocs, this.docMaxTF, BM25RankingAlgorithm.IdfFormula.OKAPIREGULAR);
        ArrayList<Pair<String, double[]>> relatedDocsToTermValues = new ArrayList<>();
        //for each possible term name
        for (int j = 0; j < allQueryRelatedDocsID.size(); j++) {
            String currDocToProcess = allQueryRelatedDocsID.get(j);
            // for each of the terms we found
            for (int k = 0; k < termToDocData.size(); k++) {
                Pair<String,String>[] termToDocsPairs = termToDocData.get(k).keySet().toArray(new Pair[2]);
                // for each of the Dod, term we found for that term.
                for (int l = 0; l < termToDocsPairs.length; l++) {
                    if(termToDocsPairs[l].left.equals(currDocToProcess)){
                        double[] singleTermValues = this.getValuesAsDoubleArray(termToDocData.get(k).get(termToDocsPairs[l]).toArray());
                        Pair<String, double[]> singleDocAllSingleTermsValues = new Pair<>(currDocToProcess,singleTermValues) ;
//                        singleDocAllSingleTermsValues.left = currDocToProcess, singleTermValues);
                        relatedDocsToTermValues.add(singleDocAllSingleTermsValues);
                    }
                }
            }
        }
        System.out.println(BM25Ranker.rank(relatedDocsToTermValues));
    }

    /**
     * because java functions don't work as they say they should in the documentation.
     * @return
     */
    private double[] getValuesAsDoubleArray(Object[] values){
        double[] ans = new double[values.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = (double)(values[i]);
        }
        return ans;
    }

    private ArrayList<String> getAllDocsID(ArrayList<Map<Pair<String, String>, ArrayList>> allData){
        ArrayList<String> docsIDs = new ArrayList<>();
        for (Map<Pair<String, String>, ArrayList> termToDocData :
               allData) {
            for (Object docTermPair :
                    termToDocData.keySet().toArray()) {
                if(!docsIDs.contains(((Pair<String, String>)(docTermPair)).left)){
                    docsIDs.add(((Pair<String, String>)(docTermPair)).left);
                }
            }
        }
        return docsIDs;
    }

    private ArrayList<Map<Pair<String, String>, ArrayList>>  collectAllTermsToDocData(String[] termsIds){
        ArrayList<Map<Pair<String, String>, ArrayList>> allDataAllTerms = new ArrayList<>();
        for (String termID :
                termsIds) {
            Map<Pair<String, String>, ArrayList> singleTermAllData =  this.collectTermToDocData(termID);
            allDataAllTerms.add(singleTermAllData);
        }
        return allDataAllTerms;
    }

//termFreqInDoc
//kParam = term weight
//bParam - 0.75
//numberOfDocContainTerm - n(qi) counted
//docMaxTF
    private Map<Pair<String, String>, ArrayList> collectTermToDocData(String termIdentifier){
        BufferedReader br = null;
        // HashMap of <DocId, currentScore>
        HashMap<String, Double> currentDocsScore = new HashMap<>();
        try{
            br = new BufferedReader(new FileReader(this.pathToDictionary));
            String line = br.readLine();
            while(line!=null){
                String[] splitLine = StringUtils.split(line, ",");
                if (StringUtils.equals(termIdentifier, splitLine[0].toLowerCase())){
                    int pointerToLine = Integer.parseInt(splitLine[3]); // extracting the pointer
                    String lineData = this.getPostingLine(pointerToLine);
                    String[] singleTermPostingData = StringUtils.split(lineData, "|");
                    int termTotalDocsTF = Integer.parseInt(singleTermPostingData[1]);
                    String[] termDocAppearanceData = StringUtils.split(singleTermPostingData[2], "<>");
                    int numberOfDocContainTerm = termDocAppearanceData.length;

                    Map<Pair<String, String>, ArrayList> docToTerm = new HashMap<>();
                    // calc the score to each doc in termDocAppearanceData
                    for (int i = 0; i < termDocAppearanceData.length; i++) {
                        String[] singleDocData = StringUtils.split(termDocAppearanceData[i],",");
                        String docID = singleDocData[0];
                        int docTermTf = Integer.parseInt(singleDocData[1]);
                        double termWeightInDoc = Double.parseDouble(singleDocData[2]);
                        ArrayList<Double> valuesOfTermToDoc = new ArrayList<>();
                        valuesOfTermToDoc.add((double)docTermTf); //termFreqInDoc
                        valuesOfTermToDoc.add(termWeightInDoc); //kParam = term weight
                        valuesOfTermToDoc.add(0.75); //bParam - 0.75
                        valuesOfTermToDoc.add((double)numberOfDocContainTerm); //numberOfDocContainTerm - n(qi) counted
                        valuesOfTermToDoc.add((double) getDocMaxTF(docID)); //docMaxTF
                        docToTerm.put(new Pair<>(docID, termIdentifier), valuesOfTermToDoc);
                    }
                    // docID - TermID :
                    return docToTerm;
                }
                else{
                    line = br.readLine();
                }
            }

        }catch(IOException e){
            System.out.println(e);
        }
        return null;
    }

    /**
     * private function, retrieves the posting line from the posting file.
     * @param pointerToLine - int, the line's number (starts counting from zero)
     * @return
     */
    private String getPostingLine(int pointerToLine){
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new FileReader(this.pathToPosting));
            String line = bufferedReader.readLine();
            int currentLineNum = 0;
            while(line!=null){
                if(currentLineNum==pointerToLine){
                    return line;
                }
                line = bufferedReader.readLine();
                currentLineNum += 1;
            }

        }catch(IOException e){
            System.out.println(e);
        }
        return null;
    }
    private String[] getAllTermDocsData(String termDocDataAsString){
        return null;
    }

    /**
     * private function, retrieves the length of a document given its ID string from the DocInfo file.
     * @param docID - String, the doc's identification string
     * @return
     */
    private int getDocMaxTF(String docID){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.pathToDocInfo));
            String line = bufferedReader.readLine();
            while(line!=null){
                if (StringUtils.startsWith(line, docID)){
                    return Integer.parseInt(StringUtils.split(line," ")[2]);
                }
                else{
                    line = bufferedReader.readLine();
                }
            }
        }catch(IOException e){
            System.out.println(e);
            // returns -1 if there was an issue with reading the docs info file.
            return -1;
        }
        return -1; //wasn't found
    }


}
