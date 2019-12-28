package sample.Model;

public class Searcher {


    public void search(Document query) {
        RankedDocumentsStructure rankedDocumentsStructure=new RankedDocumentsStructure(query.getDocNo());

    }

    /**
     * write ranked results to file.
     */
    public void writeResultsToFile(){

    }
}
