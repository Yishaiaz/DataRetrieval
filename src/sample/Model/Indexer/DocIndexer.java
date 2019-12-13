package sample.Model.Indexer;

import org.apache.commons.lang3.StringUtils;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;
import sample.Model.FilesMerger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class that write temp posting files.
 */
public class DocIndexer {
    private int MAX_T = 20;
    public static int indexForTempFiles = 0;
    String postingFilePath = "";
    boolean stemming;

    public DocIndexer(String postingFilePath,boolean withStemming) {
        this.postingFilePath = postingFilePath;
        this.stemming=withStemming;

    }

    /**
     * index all docs in given docsContainer and write the out come to a file .
     * @param docsContainer
     */

    //this function receives document and put into file all terms inside it.
    public void indexChuckDocs(ArrayList<Document> docsContainer) {

        HashMap<String, String> valuesOfChunck = new HashMap<>();

        for (Document doc : docsContainer) {

            TermHashMapDataStructure termStructure = doc.parsedTerms;
            for (String key : termStructure.termsEntries.keySet()) {
                String value = termStructure.termsEntries.get(key).getValue();
                int tf = termStructure.termsEntries.get(key).getTF();
                double weight = termStructure.termsEntries.get(key).getWeight();
                // term appeared first time in this chunk.


                //term already in valuesOfChunck. need to append <> segment of this doc .
                if (valuesOfChunck.containsKey(value)) {
                    valuesOfChunck.replace(value, valuesOfChunck.get(value), writeSegmentToPostingFileInFormat(valuesOfChunck.get(value), doc.getDocNo(), tf, weight));
                }

                //in case value start with upper case and this term already exist in lower case
                //need to save in lower case
                else if (Character.isUpperCase(value.charAt(0)) && valuesOfChunck.containsKey(value.toLowerCase())) {

                    String info = valuesOfChunck.get(key.toLowerCase());
                    valuesOfChunck.replace(value.toLowerCase(), valuesOfChunck.get(value.toLowerCase()), writeSegmentToPostingFileInFormat(info, doc.getDocNo(), tf, weight));

                }
                //in case value start with lower case and this term already exist in upper case
                //need to save in lower case
                else if (Character.isLowerCase(value.charAt(0)) && valuesOfChunck.containsKey(value.toUpperCase())) {

                    String info = valuesOfChunck.get(value.toUpperCase());
                    valuesOfChunck.remove(value.toUpperCase());
                    valuesOfChunck.put(value.toLowerCase(), writeSegmentToPostingFileInFormat(info, doc.getDocNo(), tf, weight));

                }
                // in case value not exist at all , first time
                else if (!valuesOfChunck.containsKey(value))
                    valuesOfChunck.put(value, writeSegmentToPostingFileInFormat("", doc.getDocNo(), tf, weight));

            }

        }

        //  StrBuilder contentOfFile=new StrBuilder();
        //write everything to file.
        File statText = new File(postingFilePath + File.separator + indexForTempFiles + ".txt");
        indexForTempFiles++;
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(statText);

            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            for (String term : valuesOfChunck.keySet()) {
                int df = countMatches(valuesOfChunck.get(term), '<');

                w.write(term + "|" + df + "|" + valuesOfChunck.get(term)+System.lineSeparator());
            }
            w.close();
            osw.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * this function help to add segment to line in posting file .
     * segment = <docID, tf, weight>
     * @param mainLine original line that I want to append to it
     * @param docId
     * @param tf
     * @param weight
     * @return
     */
        public String writeSegmentToPostingFileInFormat(String mainLine ,String docId,int tf,double weight){
        String ans=StringUtils.removeEnd(mainLine,"\n") + "<" + docId + " ," + tf + "," + Double.toString(weight) + ">";
        return ans;
    }

    /**
     * how many times char c exist in str
     * @param str
     * @param c
     * @return
     */
    public int countMatches(String str, char c) {
        int count = 0;

        for (char ch : str.toCharArray()) {
            if (ch == c) {
                count++;
            }
        }
        return count;
    }


    /**
     * This function add to 'filesPath' list with files paths.
     * @param path
     * @return
     */
    public static ArrayList<String> getListOfFilesPaths(String path) {  //"C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus"
        ArrayList<String> filesPaths= new ArrayList<>();
        File corpusFolder = new File(path);

        //check if path exist
        if (corpusFolder != null) {
            try (Stream<Path> walk = Files.walk(Paths.get(path))) {

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

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Path doesn't exist.");
        return filesPaths;
    }

    /**
     * this function merge all files in posting path- until left one merged file.
     */
    public void mergeFiles() {
        ArrayList<String> paths=getListOfFilesPaths(postingFilePath);
        paths.removeIf(path-> (path.endsWith("stemmingPostingFile.txt")|| path.endsWith("notStemmingPostingFile.txt")));
        paths.sort(new FileSizeCompare());
        ArrayList<FilesMerger> mergers = new ArrayList<>();
        while (paths.size()>1){
            ExecutorService executorService = Executors.newFixedThreadPool(MAX_T);

            int numberOfTaskers = 0;
            int numberOfFiles = 0;
            while(numberOfTaskers<MAX_T){
                if ((paths.size()- numberOfTaskers*2)>1){
                    String path1=paths.get(numberOfFiles);
                    String path2=paths.get(numberOfFiles + 1);
                    mergers.add(new FilesMerger(path1, path2, postingFilePath));
                }
                numberOfTaskers+=1;
                numberOfFiles+=2;
            }
            for (FilesMerger takser :
                    mergers) {
                executorService.execute(takser);
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                System.out.println(e.getCause());
            }

            paths=getListOfFilesPaths(postingFilePath);
            paths.removeIf(path-> (path.endsWith("stemmingPostingFile.txt")|| path.endsWith("notStemmingPostingFile.txt")));
            Collections.sort(paths, new FileSizeCompare());
        }

        File mergedFile = new File (getListOfFilesPaths(postingFilePath).get(0));
        //change file name depend if including stemming or not.
        if (stemming){
            String pathToMergeFile=getListOfFilesPaths(postingFilePath).get(0);
            File f= new File(postingFilePath+File.separator+"stemmingPostingFile.txt");
            mergedFile.renameTo(f);
        }
        else{
            String pathToMergeFile=getListOfFilesPaths(postingFilePath).get(0);
            File f= new File(postingFilePath+File.separator+"notStemmingPostingFile.txt");
            mergedFile.renameTo(f);
        }
    }

    class FileSizeCompare implements Comparator<String> {
        /**
         *compere two files by their weights.
         * because we want merge smaller files first.
         * @param o1
         * @param o2
         * @return
         */

        @Override
        public int compare(String o1, String o2) {
            File file1 = new File(o1);
            File file2 = new File(o2);
            return file1.length() > file2.length() ? 1 : file1.length()< file2.length() ? -1 : 0  ;
        }
    }

}