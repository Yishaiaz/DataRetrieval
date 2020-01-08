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
    /**
     * Class for ranking documents using an IAlgorithm implemented class.
     * This implementation uses BM25 ranking algorithm implementation, slightly
     * different from what we learned in class.
     *
     */
    private IRankingAlgorithm rankingAlgorithm;
    private String pathToDictionary;
    private String pathToPosting;
    private String pathToDocInfo;
    private int totalNumOfDocs;
    private double docsAvgLength;
    private HashMap<Integer, String> lineInMemoryHash = new HashMap<>();// contains line number, and the line string value
    private HashMap<String, Integer> docsMaxTFMemoryHash = new HashMap<>();// contains docID, Doc max tf integer
    private HashMap<String, Map <Pair<String, String>, ArrayList>> termsMapToDocsDataInMemoryHash = new HashMap<>();// contains docID, Doc max tf integer
    private BufferedReader termDictionaryReader;
    private BufferedReader termPostingReader;
    private int postingLineCtr = 0;


    /**
     * constructor - receives the paths to all necessary files,
     * such as dictionary, posting, and documents' info.
     * it also receives the total number of documents in the corpus,
     * and the avg document length
     * now initializes the dictionary reader, this helps avoid re-reading the entire dictionary every term.
     * @important this means it must receive in method rankDocsForQuery a SORTED TermHashMapDataStructure.
     * @param pathToDictionary - String
     * @param pathToPosting - String
     * @param pathToDocInfo - String
     * @param totalNumOfDocs - int
     * @param docsAvgLength - double
     */
    public Ranker(String pathToDictionary, String pathToPosting, String pathToDocInfo, int totalNumOfDocs, double docsAvgLength) throws Exception{
        this.pathToDictionary = pathToDictionary;
        this.pathToPosting = pathToPosting;
        this.pathToDocInfo = pathToDocInfo;
        this.totalNumOfDocs = totalNumOfDocs;
        this.docsAvgLength = docsAvgLength;
        try{
            this.termDictionaryReader = new BufferedReader(new FileReader(pathToDictionary));
            this.termPostingReader = new BufferedReader(new FileReader(pathToPosting));
        }catch(IOException e){
            throw new Exception("can't load dictionary");
        }
    }

    /**
     * the main public function, receives a TermHasHMapDataStructure as
     * a container for all the query terms.
     * it extracts the termIDs', retrieves the relevant information for each term-doc
     * pair, using private functions that accesses the posting, dictionary and doc info files.
     * it returns a  RankedDocumentsStructure instance, containing all the scores for each doc.
     * @param queryTerms - TermHashMapDataStructure
     * @param queryID - String
     * @return RankedDocumentsStructure
     */
    public RankedDocumentsStructure rankDocsForQuery(TermHashMapDataStructure queryTerms, String queryID){
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
        IRankingAlgorithm BM25Ranker = new BM25RankingAlgorithm(this.totalNumOfDocs, this.docsAvgLength, BM25RankingAlgorithm.IdfFormula.OKAPIREGULAR);
        ArrayList<Pair<String, double[]>> relatedDocsToTermValues = new ArrayList<>();
        //for each possible term name
        for (int j = 0; j < allQueryRelatedDocsID.size(); j++) {
            String currDocToProcess = allQueryRelatedDocsID.get(j);
            // for each of the terms we found
            for (int k = 0; k < termToDocData.size(); k++) {
                Pair<String,String>[] termToDocsPairs = termToDocData.get(k).keySet().toArray(new Pair[0]);
                // for each of the Dod, term we found for that term.
                for (int l = 0; l < termToDocsPairs.length; l++) {
                    if(termToDocsPairs[l].left.equals(currDocToProcess)){
                        double[] singleTermValues = this.getValuesAsDoubleArray(termToDocData.get(k).get(termToDocsPairs[l]).toArray());
                        Pair<String, double[]> singleDocAllSingleTermsValues = new Pair<>(currDocToProcess,singleTermValues) ;
                        relatedDocsToTermValues.add(singleDocAllSingleTermsValues);
                    }
                }
            }
        }
        Map<String, Double> docsRanking = BM25Ranker.rank(relatedDocsToTermValues);
        RankedDocumentsStructure ans = new RankedDocumentsStructure(queryID);
        Set<String> keySet = docsRanking.keySet();
        for (int j = 0; j < docsRanking.size(); j++) {
            ans.insert(keySet.toArray(new String[0])[j], docsRanking.get(keySet.toArray(new String[0])[j]));
        }
        return ans;
    }



    /**
     *  extracts from an ArrayList<Pair<DocID, TermID>, ArrayList>
     *  only the documents' IDs.
     * @param allData
     * @return
     */
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

    /**
     *  using private function, collects all the information on a
     *  each doc-term pair.
     * @param termsIds
     * @return
     */
    private ArrayList<Map<Pair<String, String>, ArrayList>>  collectAllTermsToDocData(String[] termsIds){
        ArrayList<Map<Pair<String, String>, ArrayList>> allDataAllTerms = new ArrayList<>();
        for (String termID :
                termsIds) {
            Map<Pair<String, String>, ArrayList> singleTermAllData =  this.collectTermToDocData(termID);
            if(singleTermAllData!=null) {
                allDataAllTerms.add(singleTermAllData);
            }
        }
        return allDataAllTerms;
    }

    /**
     * using private functions, collects all the information
     * on a doc-term pair, i.e. it extracts for term-doc the TF, weight paramter,
     * location & total term appearances in the corpus.
     * returns a Map instance with the keys being Pair object of <DocID, TermID>
     * and the values is an ArrayList of Doubles.
     * @param termIdentifier - String
     * @return Map<Pair<String, String>, ArrayList>
     */
    private Map<Pair<String, String>, ArrayList> collectTermToDocData(String termIdentifier){
        if(this.termsMapToDocsDataInMemoryHash.containsKey(termIdentifier)){
            return this.termsMapToDocsDataInMemoryHash.get(termIdentifier);
        }
        BufferedReader br = this.termDictionaryReader;
        try{
//            br = new BufferedReader(new FileReader(this.pathToDictionary));
            String line = br.readLine();
            while(line!=null){
                String[] splitLine = StringUtils.split(line, ",");
                // if we haven't found the term in our dictionary
                if(StringUtils.startsWith(splitLine[0].toLowerCase(), String.valueOf((char)(termIdentifier.toCharArray()[0]+1)))){
                    br.close();
                    this.termDictionaryReader = new BufferedReader(new FileReader(this.pathToDictionary));
                    return null;
                }
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
                        double docsMaxTF = (double)getDocMaxTF(docID);
                        valuesOfTermToDoc.add(docsMaxTF==-1? 1: docsMaxTF); //docsAvgLength
                        docToTerm.put(new Pair<>(docID, termIdentifier), valuesOfTermToDoc);
                    }
                    // docID - TermID :
                    this.termsMapToDocsDataInMemoryHash.put(termIdentifier, docToTerm);
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
        if(this.lineInMemoryHash.containsKey(pointerToLine)){
            return lineInMemoryHash.get(pointerToLine);
        }
        BufferedReader bufferedReader = this.termPostingReader;
        try{
//            bufferedReader = new BufferedReader(new FileReader(this.pathToPosting));
            String line = bufferedReader.readLine();
            int currentLineNum = this.postingLineCtr;
            while(line!=null){
                if(currentLineNum==pointerToLine){
                    this.lineInMemoryHash.put(pointerToLine, line);
                    this.postingLineCtr = currentLineNum + 1;
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
        if(this.docsMaxTFMemoryHash.containsKey(docID)){
            return this.docsMaxTFMemoryHash.get(docID);
        }
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.pathToDocInfo));
            String line = bufferedReader.readLine();
            while(line!=null){
                if (StringUtils.startsWith(line, docID)){
                    this.docsMaxTFMemoryHash.put(docID, Integer.parseInt(StringUtils.split(line," ")[2]));
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


}
