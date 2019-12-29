package sample.Model;

import org.apache.commons.lang3.StringUtils;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.DataStructures.TermHashMapEntry;
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
    double avgDocLength;

    public Ranker(String pathToDictionary, String pathToPosting, String pathToDocInfo, int totalNumOfDocs, double avgDocLength) {
        this.pathToDictionary = pathToDictionary;
        this.pathToPosting = pathToPosting;
        this.pathToDocInfo = pathToDocInfo;
        this.totalNumOfDocs = totalNumOfDocs;
        this.avgDocLength = avgDocLength;
    }

    /**
     *
     * @param queryTerms
     */
    public void rankQuery(TermHashMapDataStructure queryTerms){
        Iterator<Entry<String, TermHashMapEntry>> iterator = queryTerms.termsEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            // getting a single entry data
            Map.Entry<String, TermHashMapEntry> singleTerm = (Map.Entry<String, TermHashMapEntry>) iterator.next();
            String termID = singleTerm.getKey();
            String termValue = singleTerm.getValue().getValue();
            int termTF = singleTerm.getValue().getTF();
            double termWeight = singleTerm.getValue().getWeight();


//            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    public String testDicToPos(String termId){
        return this.DictionaryToPostings(termId);
    }
//todo: extract the following for each term/doc pair
//termFreqInDoc
//kparam
//Param
//numberOfDocContainTerm
//docLength
    private String DictionaryToPostings(String termIdentifier){
        BufferedReader br = null;
        // HashMap of <DocId, currentScore>
        HashMap<String, Double> currentDocsScore = new HashMap<>();
        try{
            br = new BufferedReader(new FileReader(this.pathToDictionary));
            String line = br.readLine();
            while(line!=null){
                String[] splitLine = StringUtils.split(line, ",");
                if (StringUtils.equals(termIdentifier, splitLine[0])){
                    int pointerToLine = Integer.parseInt(splitLine[3]); // extracting the pointer
                    String lineData = this.getPostingLine(pointerToLine);
                    String[] singleTermPostingData = StringUtils.split(lineData, "|");
                    int termTotalDocsTF = Integer.parseInt(singleTermPostingData[1]);
                    String[] termDocAppearanceData = StringUtils.split(singleTermPostingData[2], "<>");

                    // calc the score to each doc in termDocAppearanceData
                    for (int i = 0; i < termDocAppearanceData.length; i++) {
                        String[] singleDocData = StringUtils.split(termDocAppearanceData[i],",");
                        String docID = singleDocData[0];
                        int docTermTf = Integer.parseInt(singleDocData[1]);
                        double termWeightInDoc = Double.parseDouble(singleDocData[2]);
                    }
//                    Dictionary termScores = new Hashtable<>();

//                    termScores.put(termIdentifier, )
                    return lineData;
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
    // todo: now all that's left is to collect all the data and pass it to this function.


}
