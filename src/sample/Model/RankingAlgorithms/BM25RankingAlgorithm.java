package sample.Model.RankingAlgorithms;

import org.apache.commons.lang3.Pair;

import java.util.*;

public class BM25RankingAlgorithm extends IRankingAlgorithm{
    /**
     * BM25RankingAlgorithm implements the interface IRankingAlgorithm.
     * it is an implementation of the OKAPI BM25 formula to calculate documents
     * relevancy for queries.
     * this class doesn't deal with documents, or terms, it is on a basis of
     * docID(as string) a term's specific information regarding that doc (it's appearances, specific weight etc.)
     * it also allows the user to use different OKAPI BM25 iterations, such as
     * FloorConstant, NonNegative @see https://en.wikipedia.org/wiki/Okapi_BM25
     *
     */
    private int totalNumOfDocs;
    private double avgDocLength;
    public enum IdfFormula {
        OKAPIREGULAR,
        FLOORCONST,
        NONNEGATIVE
    }
    private IdfFormula useFormala;
    private double possibleConst;

    /**
     * constructor - receives meta data about the entire corpus.
     * also receives an ENUM for the Idf Formula to use.
     * @param totalNumOfDocs
     * @param avgDocLength
     * @param idfFormula
     */
    public BM25RankingAlgorithm(int totalNumOfDocs, double avgDocLength, IdfFormula idfFormula) {
        this.totalNumOfDocs = totalNumOfDocs;
        this.avgDocLength = avgDocLength;
        this.useFormala = idfFormula;
        this.possibleConst = -0.01;
    }

    /**
     * receives all the doc-terms related data of the query.
     * extracts for each doc-term the data,
     * and uses private function to calculate the docs new score,
     * adds it up to the current score that the doc had until that doc-term pair.
     * @param allDocsToTermsValues - ArrayList<Pair<String DocID, double[] specificTermsData>>
     * @return Map<String DocID,Double Score>
     */
    @Override
    public Map<String, Double> rank(ArrayList<Pair<String, double[]>> allDocsToTermsValues) {
        Map<String, Double> docScores = new HashMap<String, Double>();
        for (int i = 0; i < allDocsToTermsValues.size(); i++) {
            String docName = allDocsToTermsValues.get(i).left;
            double[] docToTermValues = allDocsToTermsValues.get(i).right;
            double docNewScore = this.calcSingleTermScoreForDoc((int)docToTermValues[0],
                    docToTermValues[1],
                    docToTermValues[2],
                    (int)docToTermValues[3],
                    (int)docToTermValues[4]);
            if(docScores.containsKey(docName)){
                docScores.replace(docName, docScores.get(docName)+docNewScore);
            }
            else{
                docScores.put(docName, docNewScore);
            }
        }
        return docScores;
    }

    /**
     * calculates using private functions the Idf score
     * and returns its multiplications with the freq result.
     * @param termFreqInDoc - int
     * @param kParam - double our term-doc specific weight
     * @param bParam - double - a const
     * @param numberOfDocContainTerm - int
     * @param docLength -
     * @return
     */
    private double calcSingleTermScoreForDoc(int termFreqInDoc, double kParam, double bParam, int numberOfDocContainTerm, int docLength){
        double idfRes = calcIDF(numberOfDocContainTerm);
        double freq = ((termFreqInDoc) *(kParam + 1)/ (termFreqInDoc + kParam*((1-bParam)+bParam*(docLength/this.avgDocLength))));
        return idfRes > 0 ? idfRes * freq : freq;

    }

    /**
     * this function can calculate by three different formula's
     * the IDF. @see https://en.wikipedia.org/wiki/Okapi_BM25
     * @param numberOfDocsContainingTerm
     * @return
     */
    private double calcIDF(int numberOfDocsContainingTerm){
        switch(this.useFormala){
            case OKAPIREGULAR:
                return Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5));
            case FLOORCONST:
                return Math.max(this.possibleConst, Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
            case NONNEGATIVE:
                return Math.max(0,Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
        }
        return 0;
    }

}
