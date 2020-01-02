package sample.Model;

import sample.Model.Indexer.DocIndexer;
import sample.Model.Parser.DocParser;
import sample.Model.Parser.IParser;
import sample.Model.TaskPool.WriteToFilePool;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * responsible for read corpse and parse it
 */
public class ReadFile {
    private final String corpusPath;
    private final IParser parser;
    private final HashSet<String> STOP_WORD_BAG;
    public DocIndexer indexer;
    public static int indexForDocInfo=0;
    public boolean withStemming;
    public Searcher searcher;

    public ReadFile(String corpusPath, HashSet<String> stopWords, HashSet<String> months, boolean withStemming, String postingFilesPath) {
        this.corpusPath = corpusPath;
        this.parser = new DocParser(withStemming, stopWords, months);
        indexer=new DocIndexer(postingFilesPath,withStemming);
        this.STOP_WORD_BAG = stopWords;
        this.withStemming=withStemming;
        searcher=new Searcher(corpusPath,postingFilesPath);
//        readStopWordsFile();
    }

    /**
     *  prepare file for parsing by extract fields and create object of doc.
     * @param corpusPath
     * @param containerSize  its how many docs we write to one temp file .
     */
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
                        Document doc = this.parser.Parse(fullDocStringBuilder.toString(),false);
                        File docInfo=null;
                        if (withStemming) {
                            docInfo = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoStemming.txt");
                        }
                        else{
                            docInfo=new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt");
                        }


                        FileWriter fw = new FileWriter(docInfo,true);
                        BufferedWriter w = new BufferedWriter(fw);
                        w.write(doc.DocNo+" "+doc.howManyUniqTermsInDoc()+" "+doc.parsedTerms.getMaxTf()+System.lineSeparator());

                        w.close();
                        fw.close();


                        // docsContainer not full yet
                        if (docsContainer.size()<containerSize-1)
                            docsContainer.add(doc);
                            // docs container full and ready for index
                        else {
                            docsContainer.add(doc);
                            this.indexer.indexChuckDocs(docsContainer);
                            docsContainer.clear();
                        }

                        line = br.readLine();
                    }
                    else{
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

    /**
     * prepare query to parse.
     * @param queryPath
     * @throws Exception
     */
    public void prepareDocOfQueriesToParse(String queryPath,Boolean withSemantic) throws Exception {
        BufferedReader br = null;
        StringBuilder fullDocStringBuilder = new StringBuilder();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(queryPath), "UTF-8"));
        String line = br.readLine();
        while (line!=null){
            if(line.equals("") || line.equals("\n") || line.equals(" ")){
                line = br.readLine();
            }
                else if (line.equals("<top>")){
            fullDocStringBuilder.setLength(0);
            line = br.readLine();
        }
        else {
                // we're inside a query, read it all
                if (line.equals("</top>")) {
                    //we've reached the end of the query.
                    Document query = this.parser.Parse(fullDocStringBuilder.toString(), true);
                    searcher.search(query,withStemming,withSemantic);
                    line=br.readLine();
                }
                else{
                    fullDocStringBuilder.append(line);
                    line = br.readLine();
                }
            }
        }

    }

    public void prepareFreeTypingQueryParse(String query,Boolean withSemantic) {
        try {
            Document queryDoc = null;
            queryDoc = this.parser.Parse(query, true);
            searcher.search(queryDoc,withStemming,withSemantic);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
