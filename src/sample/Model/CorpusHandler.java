package sample.Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class mange all preparing search engine
 */
public class CorpusHandler {
    private String corpusPath;
    public String postingFilesPath="";
    private ArrayList<String> filesPaths; //list contains all paths in Corpus dir.
    public HashSet<String> stopWords = new HashSet<>();
    public HashSet<String> months = new HashSet<>();
    public ReadFile readFile;
    public boolean isStemming;

    public CorpusHandler(String corpusPath) {
        this.corpusPath = corpusPath;
        this.filesPaths = new ArrayList<>();
        this.stopWords = readStopWordsFile();
        this.months = new HashSet<String>(Arrays.asList(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER",
                "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December",
                "JAN", "FEB", "MAR", "APR", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC",
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
    }
    public void setPostingFilesPath(String postingFilesPath) {
        this.postingFilesPath = postingFilesPath;
    }

    /**
     * This function add to 'filesPath' list with files paths.
     */
    public void initListOfFilesPaths() {
        File corpusFolder = new File(corpusPath);

        //check if path exist
        if (corpusFolder != null) {
            try (Stream<Path> walk = Files.walk(Paths.get(corpusPath+"/corpus"))) {

                List<String> folders = walk.filter(Files::isDirectory)
                        .map(x -> x.toString()).collect(Collectors.toList());

                try (Stream<Path> walk2 = Files.walk(Paths.get(folders.get(0)))) {
                    //result contains all paths of files inside one folder
                    List<String> result = walk2.filter(Files::isRegularFile)
                            .map(x -> x.toString()).collect(Collectors.toList());

                    //insert all files' paths from folder to 'filesPath'
                    for (String r : result)
                        filesPaths.add(r);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                //    System.out.println(filesPaths.size());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Path doesn't exist.");
    }

    /**
     * This function start process of parsing to every path in corpus path
     * @param withStemming
     * @throws FileNotFoundException
     */
    public void findTextInDocs(boolean withStemming) throws FileNotFoundException {

        this.isStemming=withStemming;
        long start_time = System.currentTimeMillis();
         readFile = new ReadFile(this.corpusPath, this.stopWords, this.months,withStemming,postingFilesPath,"");
        File docsEntities = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "docsEntities");

        if (withStemming) {
            File docsInfoFile = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoStemming.txt");
        }
     else if (!withStemming){
            File docsInfoFile = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming");
            }

        for (String path : filesPaths) {
            if(path.endsWith(".DS_Store")){
                System.out.println("you and your mac");
            }
            else{
                readFile.prepareDocToParse(path,250);

            }
        }
        System.out.println(String.format("Total time without merging : %d minutes", (System.currentTimeMillis() - start_time)/60000));
        start_time = System.currentTimeMillis();
        readFile.indexer.mergeFiles();
        System.out.println(String.format("Total time to merge : %d seconds", (System.currentTimeMillis() - start_time)/1000));
        DictionaryParser dicParser = new DictionaryParser(postingFilesPath,corpusPath,withStemming);
        dicParser.run();

    }

    /**
     * this function reads stop words from '05 stop_words.txt' file
     * @return
     */
    private HashSet<String> readStopWordsFile(){
        HashSet<String> stopWords = new HashSet<>();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream(this.corpusPath+"/05 stop_words.txt"), "UTF-8"));
            String line = "";
            while(line != null){
                line = br.readLine();
                if (line != null){
                    stopWords.add(line);
//                    System.out.println(line);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
        return stopWords;
    }

    public void search(String queryPath,int withSemantic,String resultPath,boolean isStemming){
        try {
            readFile= new ReadFile(corpusPath,stopWords,months,isStemming,postingFilesPath,resultPath);
            readFile.prepareDocOfQueriesToParse(queryPath,withSemantic);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchFreeTyping(String query,int withSemantic,String resultPath,boolean isStemming)  {
        try {
            readFile= new ReadFile(corpusPath,stopWords,months,isStemming,postingFilesPath,resultPath);
            readFile.prepareFreeTypingQueryParse(query,withSemantic);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}