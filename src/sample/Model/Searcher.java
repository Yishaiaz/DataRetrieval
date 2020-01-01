package sample.Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Searcher {

    String dictionaryPath;
    String postingFilesPath;

    public Searcher(String corpusPath, String postingFilesPath) {
        this.dictionaryPath=corpusPath; //that's where we save the dictionary.
        this.postingFilesPath=postingFilesPath;
    }

    public void search(Document query) {

            RankedDocumentsStructure rankedDocumentsStructure=new RankedDocumentsStructure(query.getDocNo());
            String pathToDocsInfo= Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt";
        long numOfDocs = 0;

        try {
            numOfDocs = Files.lines(Paths.get(pathToDocsInfo)).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Ranker ranker = new Ranker(dictionaryPath,postingFilesPath,pathToDocsInfo,(int)numOfDocs,0);       ranker. rankQuery(query.parsedTerms);

        rankedDocumentsStructure.onlyBest50();
        writeResultsToFile(rankedDocumentsStructure);
    }

    /**
     * write ranked results to file.
     * required format : query_id, iter, docno, rank, sim, run_id
     *
     */
    public void writeResultsToFile(RankedDocumentsStructure rankedDocumentStructure ){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.dictionaryPath + File.separator + "results.txt"));

for (String key: rankedDocumentStructure.documents.keySet()){
    Double rank=rankedDocumentStructure.documents.get(key);
    //format requested for Track_Eval program
    bufferedWriter.write(rankedDocumentStructure.queryId+","+0+","+key+","+rank+","+42.38+","+"run"+System.lineSeparator());
}

bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
