package sample.Model.Indexer;

import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocIndexer {

    String postingFilePath="";
    public DocIndexer(String postingFilePath) {
        this.postingFilePath=postingFilePath;

    }

    //this function receives document and put into file all terms inside it.
    public void indexOneDoc(Document doc) {
        try {
            //Whatever the file path is.
            File statText = new File(postingFilePath +File.pathSeparator+ doc.DocNo + ".txt");
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            TermHashMapDataStructure termStructure = doc.parsedTerms;
            for (String key : termStructure.termsEntries.keySet()) {
                w.write(key);
                int tf = termStructure.termsEntries.get(key).getTF();
                w.write("|" + doc.DocNo + "|" + tf + '\n');

            }

            w.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file " + doc.DocNo);
        }
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

                //    System.out.println(filesPaths.size());

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
            File merged = new File(postingFilePath+File.pathSeparator+ "merged" + ".txt");
            FileWriter fileWriter = new FileWriter(merged.getPath());
            PrintWriter out = new PrintWriter(fileWriter);
            Iterator it1 = br1.lines().iterator();
            Iterator it2 = br2.lines().iterator();
            //line1 - current line in doc1. term1 is the term value
            String line1 = (String) it1.next();
            String term1 = findTerm(line1);
            //line2 - current line in doc2 . term2 is the term value
            String line2 = (String) it2.next();
            String term2 = findTerm(line2);
            while (it1.hasNext() && it2.hasNext()) {
                //in case its same term
                if (term1.compareTo(term2) == 0) {
                    out.write(line1);
                    int indexOfMetaData = line2.indexOf("|");
                    out.write(line2.substring(indexOfMetaData) + "\n");
                    line1 = (String) it1.next();
                    term1 = findTerm((line1));
                    line2 = (String) it2.next();
                    term2 = findTerm((line2));


                } else if (!it1.hasNext() || term1.compareTo(term2) > 0) {
                    out.write(line2 + "\n");
                    line2 = (String) it2.next();
                    term2 = findTerm(line2);
                } else if (!it2.hasNext() || term1.compareTo(term2) < 0) {
                    out.write(line1 + "\n");
                    line1 = (String) it1.next();
                    term1 = findTerm(line1);
                }
                out.flush();
            }

            //delete original files
            File file1=new File (path1);
            file1.delete();
            File file2=new File (path2);
            file2.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String findTerm(String line) {
        int indexOfEnd = line.indexOf('|');
        return line.substring(0, indexOfEnd);
    }

    public void sortDocument(String path) throws IOException {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String inputLine = "";
        List<String> lineList = new ArrayList<String>();
        while (true) {
            try {
                if (!((inputLine = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            lineList.add(inputLine);
        }
        fileReader.close();

        Collections.sort(lineList, String.CASE_INSENSITIVE_ORDER);
        FileWriter fileWriter = new FileWriter(path);
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);
        }


        for (String outputLine : lineList) {
            out.println(outputLine);
        }
        out.flush();
        out.close();
        fileWriter.close();

    }

}




