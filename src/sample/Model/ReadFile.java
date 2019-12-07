package sample.Model;

import sample.Model.Indexer.DocIndexer;
import sample.Model.Parser.DocParser;
import sample.Model.Parser.IParser;
import sample.Model.TaskPool.WriteToFilePool;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;


public class ReadFile {
    private final String corpusPath;
    private final IParser parser;
    private final HashSet<String> STOP_WORD_BAG;
    public DocIndexer indexer;

    public ReadFile(String corpusPath, HashSet<String> stopWords, HashSet<String> months, boolean withStemming, String postingFilesPath, WriteToFilePool writeToFilePool) {
        this.corpusPath = corpusPath;
        this.parser = new DocParser(withStemming, stopWords, months);
        indexer=new DocIndexer(postingFilesPath,writeToFilePool);
        this.STOP_WORD_BAG = stopWords;
//        readStopWordsFile();
    }

    public void run(){

    }



    //  prepare file for parsing by extract fields and create object of doc.
    public void prepareDocToParse(String corpusPath,int containerSize) {
        long total_start_time = System.currentTimeMillis();
        ArrayList <Document> docsContainer= new ArrayList<>();
        BufferedReader br = null;
        StringBuilder fullDocStringBuilder = new StringBuilder();
//        String fullDoc = "";
        try {
            // stream to file
            br = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
            String line = br.readLine();
            while (line != null) {
                if(line.equals("") || line.equals("\n") || line.equals(" ")){
                    line = br.readLine();
                }
                else if (line.equals("<DOC>")){
                    fullDocStringBuilder.setLength(0);
                    line = br.readLine();
                }
                else{
                    // we're inside a doc, read it all
                    if (line.equals("</DOC>")){
                        //we've reached the end of the doc.
                        //with timer
                        long start_time = System.currentTimeMillis();
                        Document doc = this.parser.Parse(fullDocStringBuilder.toString());
                       // docsContainer not full yet
                        if (docsContainer.size()<containerSize-1)
                            docsContainer.add(doc);
                        // docs container full and ready for index
                        else {
                            docsContainer.add(doc);
                            this.indexer.indexChuckDocs(docsContainer);
                            docsContainer.clear();
                        }
//                        Document doc = this.parser.Parse(fullDoc);
//                        this.indexer.index(doc);

//                        System.out.println(String.format("Time to parse doc- %s, took: %d Ms", doc.getDocNo(), (System.currentTimeMillis() - start_time)));
                        line = br.readLine();
                    }
                    else{
//                        fullDoc += " "+line;
                        fullDocStringBuilder.append(line);
                        line = br.readLine();
                    }
                }
            }

            //if There are some docs left in docsContainer.
       if (docsContainer.size()>0)
           indexer.indexChuckDocs(docsContainer);



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Total parsing for single FILE took: %d Ms", (System.currentTimeMillis() - total_start_time)));
    }

}
