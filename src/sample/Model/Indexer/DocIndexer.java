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

import static java.lang.String.CASE_INSENSITIVE_ORDER;


public class DocIndexer {
    int MAX_T = 10;
    public static int indexForTempFiles = 0;
    public static int indexForMergeFiles = 0;
    String postingFilePath = "";

    // WriteToFilePool writeToFilePool;
    public DocIndexer(String postingFilePath/*,WriteToFilePool writeToFilePool*/) {
        this.postingFilePath = postingFilePath;
        //this.writeToFilePool=writeToFilePool;

    }

    //this function receives document and put into file all terms inside it.
    public void indexChuckDocs(ArrayList<Document> docsContainer) {
        //     try {

        //<Term, postingString>
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

                w.write(term + "|" + df + "|" + valuesOfChunck.get(term));
            }
            w.close();
            osw.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //contentOfFile.append(term+"|"+ df+"|"+valuesOfChunck.get(term));

        //  writeToFilePool.addContentToStack(contentOfFile.toString());


//        } catch (IOException e) {
//            System.err.println("Problem writing to the files "+ docsContainer.get(0).getDocNo() +" to "+ docsContainer.get(docsContainer.size()-1).getDocNo() );
//        }
    }

    //this function help to add segment to line in posting file .
    //segment = <docID, tf, weight>
    public String writeSegmentToPostingFileInFormat(String mainLine ,String docId,int tf,double weight){
        //todo: אולי צריך פה lineseperator
        String ans=mainLine.replaceAll("\n","")+"<" + docId + " ," + tf + "," + Double.toString(weight) + ">" + '\n';
        return ans;
    }

    public int countMatches(String str, char c) {
        int count = 0;

        for (char ch : str.toCharArray()) {
            if (ch == c) {
                count++;
            }
        }
        return count;
    }


    // This function add to 'filesPath' list with files paths.
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

    public void mergeTwoDocuments(String path1, String path2) {
        try {
            sortDocument(path1);
            sortDocument(path2);
            BufferedReader br1 = null;
            BufferedReader br2 = null;
            br1 = new BufferedReader(new InputStreamReader(new FileInputStream(path1), "UTF-8"));
            br2 = new BufferedReader(new InputStreamReader(new FileInputStream(path2), "UTF-8"));
            File merged = new File(postingFilePath+File.separator+ "merged"+indexForMergeFiles +".txt");
            indexForMergeFiles++;
            FileWriter fileWriter = new FileWriter(merged.getPath());
            PrintWriter out = new PrintWriter(fileWriter);
            Iterator it1 = br1.lines().iterator();
            Iterator it2 = br2.lines().iterator();
            //line1 - current line in doc1. term1 is the term value
            String line1 = (String) it1.next();
            String term1 = StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
            //line2 - current line in doc2 . term2 is the term value
            String line2 = (String) it2.next();
            String term2 =StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
            while (it1.hasNext() && it2.hasNext()) {
                //in case its same term
                if (CASE_INSENSITIVE_ORDER.compare(term1,term2) == 0) {

//                    if (Character.isUpperCase(term1.charAt(0)) &&(Character.isLowerCase(term2.charAt(0))))
//                            term1=term1.toLowerCase();
//
////                    if((Character.isUpperCase(term2.charAt(0)) &&(Character.isLowerCase(term1.charAt(0
// ))) ))
////                        term2=term2.toLowerCase();


                    // extract from line1 and line2 only <....> parts
                    line1= line1.substring(line1.indexOf("<"),line1.lastIndexOf(">")+1);
                    line2=line2.substring(line2.indexOf("<"),line2.lastIndexOf(">")+1);
                    String temp= line1.replaceAll("\n","")+line2;
                    //count total df
                    int df= countMatches(temp,'<');
                    //out.write(term1+"|"+df+"|"+line1+line2+"\n");
                    out.write(term1+"|"+df+"|"+temp+"\n");
//                    System.out.println(term1+"|"+df+"|"+temp+"\n");

                    //int indexOfMetaData = line2.indexOf("<");
                    //out.write(line2.substring(indexOfMetaData) + "\n");
                    line1 = (String) it1.next();
                   // term1 = findTerm((line1));
                    term1 = StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
                    line2 = (String) it2.next();
                    term2 =StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));


                } else if (!it1.hasNext() ||  CASE_INSENSITIVE_ORDER.compare(term1, term2)> 0) {
                    out.write(line2 + "\n");
//                    System.out.println(line2 + "\n");
                    line2 = (String) it2.next();
                    term2 = StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
                } else if (!it2.hasNext() || CASE_INSENSITIVE_ORDER.compare(term2, term1)> 0) {
                    out.write(line1 + "\n");
//                    System.out.println(line1 + "\n");
                    line1 = (String) it1.next();
                    term1 =  StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
                }

            }

            // in case doc2 end and doc1 not
            if (!it2.hasNext() && it1.hasNext()){
                while(it1.hasNext()) {
                    out.write(line1 + "\n");
                    line1 = (String) it1.next();
                    term1 =  StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
                }
            }

            // in case doc1 end and doc2 not
            if (!it1.hasNext() && it2.hasNext()){
                while(it2.hasNext()) {
                    out.write(line2 + "\n");
                    line2 = (String) it2.next();
                    term2 =  StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
                }
            }

            out.flush();
            out.close();
            br1.close();
            br2.close();
            //delete original files
            File file1=new File (path1);
            file1.delete();
            if (file1.exists())
                System.out.println("not good, "+ path1);

            File file2=new File (path2);
            file2.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    // this function extract the term from line in file (term came before '|')
//    //todo: אין למה לקרוא לפונציה הזאת כ"כ הרבה פעמים, עדיף להכניס את לקוד עצמו (פריימים מיותרים)
//    private String findTerm(String line) {
//        String check = StringUtils.substring(line, 0, StringUtils.indexOf(line,'|'));
//        int indexOfEnd = line.indexOf('|');
//        return line.substring(0, indexOfEnd);
//    }

    public void sortDocument(String path) throws IOException {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String inputLine = "";
        ArrayList<String> lineList = new ArrayList<>();
        while (true) {
            try {
                if (!((inputLine = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            lineList.add(inputLine);
        }
        fileReader.close();

        Collections.sort(lineList,new RatingCompare());
        FileWriter fileWriter = new FileWriter(path);
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);
        }
        out.flush();
        out.close();
        fileWriter.close();

    }

    public void mergeFiles() {
        ArrayList<String> paths=getListOfFilesPaths(postingFilePath);
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
            Collections.sort(paths, new FileSizeCompare());
        }
    }


    class RatingCompare implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            o1=o1.substring(0,o1.indexOf('|'));
            o2=o2.substring(0,o2.indexOf('|'));
            return  (CASE_INSENSITIVE_ORDER.compare(o1, o2));

        }
    }
    class FileSizeCompare implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            File file1 = new File(o1);
            File file2 = new File(o2);
            return file1.length() > file2.length() ? 1 : 0 ;
//            o1=o1.substring(0,o1.indexOf('|'));
//            o2=o2.substring(0,o2.indexOf('|'));
//            return  (CASE_INSENSITIVE_ORDER.compare(o1, o2));

        }
    }

}