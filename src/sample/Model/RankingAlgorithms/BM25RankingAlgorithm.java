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
        double bParam = 0.3; // best b = 0.3
        double kParam = 1.4; // 1 < kParam < Math.inifinity
        double termWeightInFormula = 0.1;
        double tfIDFWeightInFormula = 1 - termWeightInFormula;
        for (int i = 0; i < allDocsToTermsValues.size(); i++) {
            String docName = allDocsToTermsValues.get(i).left;
            double[] docToTermValues = allDocsToTermsValues.get(i).right;
            double termFreqInDoc = docToTermValues[0]; //term_freq
            double termWeightInDoc = docToTermValues[1]/2; // term weight
            double numberOfDocsContainingTerm = docToTermValues[3]; // number of docs containing term
            double docLength = docToTermValues[4];
            double curr_score = 0;
            try{
                curr_score = docScores.get(docName);
            }catch (Exception e){
                curr_score = 0;
            }

            double innerNom = (kParam+1)*termFreqInDoc;
            double innerDinom = termFreqInDoc + kParam*(1- bParam + bParam* docLength/this.avgDocLength);

            curr_score += termWeightInFormula*(termWeightInDoc)+tfIDFWeightInFormula*((innerNom / innerDinom) *
                    Math.log10((this.totalNumOfDocs-numberOfDocsContainingTerm +0.5)/(numberOfDocsContainingTerm+0.5)));
            docScores.put(docName, curr_score);
        }

        return docScores;
        // result= 108 ; params: b = 0.5,k = 1.4, weightInFormula=0.1
        // result= 106 ; params: b = 0.75,k = 1.4, weightInFormula=0.1
        // result=  108; params: b = 0.1,k = 1.4, weightInFormula=0.1
        // result=  111; params: b = 0.3,k = 1.4, weightInFormula=0.1
        // result=  109; params: b = 0.4,k = 1.4, weightInFormula=0.1

        // result=  109; params: b = 0.3,k = 1.2, weightInFormula=0.1
        // result=  109; params: b = 0.3,k = 1.6, weightInFormula=0.1
        // result=  105; params: b = 0.3,k = 4, weightInFormula=0.1
        // result=  109; params: b = 0.3,k = 2, weightInFormula=0.1
        // result=  111; params: b = 0.3,k = 1.4, weightInFormula=0
        // result=  111; params: b = 0.3,k = 1.4, weightInFormula=0.15 with divide by 2
        //result = 111; params: b = 0.3,k = 1.4, weightInFormula=0.15 with divide by 2 removed. removed the 0.5 from the idf
        //result = ; params: b = 0.3,k = 1.4, weightInFormula=0.15 with divide by 2 removed. regular idf => N/n(qi)
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
                return Math.log((numberOfDocsContainingTerm)/ (this.totalNumOfDocs ));
            case FLOORCONST:
                return Math.max(this.possibleConst, Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
            case NONNEGATIVE:
                return Math.max(0,Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
        }
        return 0;
    }

}
