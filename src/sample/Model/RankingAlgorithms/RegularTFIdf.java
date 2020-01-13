package sample.Model.RankingAlgorithms;

import org.apache.commons.lang3.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RegularTFIdf extends IRankingAlgorithm{
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
    private sample.Model.RankingAlgorithms.BM25RankingAlgorithm.IdfFormula useFormala;
    private double possibleConst;

    /**
     * constructor - receives meta data about the entire corpus.
     * also receives an ENUM for the Idf Formula to use.
     * @param totalNumOfDocs
     * @param avgDocLength
     * @param idfFormula
     */
    public RegularTFIdf(int totalNumOfDocs, double avgDocLength, sample.Model.RankingAlgorithms.BM25RankingAlgorithm.IdfFormula idfFormula) {
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
        double bParam = 0.9; // 1 > bParam > 0
        double kParam = 1.8; // 1 < kParam < Math.inifinity
        double termWeightInFormula = 0.15;
        double tfIDFWeightInFormula = 1 - termWeightInFormula;
        for (int i = 0; i < allDocsToTermsValues.size(); i++) {
            String docName = allDocsToTermsValues.get(i).left;
            double[] docToTermValues = allDocsToTermsValues.get(i).right;
            double termFreqInDoc = (int)docToTermValues[0]; //term_freq
            double termWeightInDoc = docToTermValues[1]; // term weight
            double numberOfDocsContainingTerm = docToTermValues[3]; // number of docs containing term
            double docLength = docToTermValues[4];
            double curr_score = 0;
            try{
                curr_score = docScores.get(docName);
            }catch (Exception e){
                curr_score = 0;
            }

            double innerNom = termFreqInDoc;
            double innerDinom = docLength/this.avgDocLength;

            curr_score += termWeightInFormula*(termWeightInDoc)+tfIDFWeightInFormula*(termFreqInDoc* Math.log10((this.totalNumOfDocs)/(numberOfDocsContainingTerm+1)));
            docScores.put(docName, curr_score);
        }

        return docScores;
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

