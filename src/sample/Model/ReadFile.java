package sample.Model;

import sample.Model.Parser.DocParser;
import sample.Model.Parser.IParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;


public class ReadFile {
    private final String corpusPath;
    private final IParser parser;
    private final HashSet<String> STOP_WORD_BAG;

    public ReadFile(String corpusPath, HashSet<String> stopWords, HashSet<String> months) {
        this.corpusPath = corpusPath;
        this.parser = new DocParser(false, stopWords, months);
        this.STOP_WORD_BAG = stopWords;
//        readStopWordsFile();
    }

    public void run(){

    }



    //  prepare file for parsing by extract fields and create object of doc.
    public void prepareDocToParse(String path) {
        BufferedReader br = null;
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
                    line = br.readLine();
                }
                else{
                    // we're inside a doc, read it all
                    if (line.equals("</DOC>")){
                        //we've reached the end of the doc.
                        this.parser.Parse(fullDoc);
                        line = br.readLine();
                    }
                    else{
                        fullDoc += " "+line;
                        line = br.readLine();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
