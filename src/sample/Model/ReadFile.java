package sample.Model;

import sample.Model.Indexer.DocIndexer;
import sample.Model.Parser.DocParser;
import sample.Model.Parser.IParser;

import java.io.*;
import java.util.HashSet;


public class ReadFile {
    private final String corpusPath;
    private final IParser parser;
    private final HashSet<String> STOP_WORD_BAG;
    private DocIndexer indexer;

    public ReadFile(String corpusPath, HashSet<String> stopWords, HashSet<String> months) {
        this.corpusPath = corpusPath;
        this.parser = new DocParser(false, stopWords, months);
        indexer=new DocIndexer();
        this.STOP_WORD_BAG = stopWords;
//        readStopWordsFile();
    }

    public void run(){

    }



    //  prepare file for parsing by extract fields and create object of doc.
    public void prepareDocToParse(String path) {
        long total_start_time = System.currentTimeMillis();
        BufferedReader br = null;
        StringBuilder fullDocStringBuilder = new StringBuilder();
        String fullDoc = "";
        try {
            // stream to file
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
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
                         this.indexer.indexOneDoc(doc);
//                        Document doc = this.parser.Parse(fullDoc);
//                        this.indexer.index(doc);

                        System.out.println(String.format("Time to parse doc- %s, took: %d Ms", doc.getDocNo(), (System.currentTimeMillis() - start_time)));
                        line = br.readLine();
                    }
                    else{
//                        fullDoc += " "+line;
                        fullDocStringBuilder.append(line);
                        line = br.readLine();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Total parsing took: %d Ms", (System.currentTimeMillis() - total_start_time)));


    }

}
