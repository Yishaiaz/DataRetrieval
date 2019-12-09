package sample.Model.Indexer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;
import sample.Model.TaskPool.WriteToFilePool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class DocIndexer {
    public static int index=0;
    String postingFilePath="";
   // WriteToFilePool writeToFilePool;
    public DocIndexer(String postingFilePath/*,WriteToFilePool writeToFilePool*/) {
        this.postingFilePath=postingFilePath;
        //this.writeToFilePool=writeToFilePool;

    }

    //this function receives document and put into file all terms inside it.
    public void indexChuckDocs(ArrayList<Document> docsContainer) {
   //     try {

            //<Term, postingString>
            HashMap<String, String> valuesOfChunck=new HashMap<>();

            for (Document doc: docsContainer) {

                TermHashMapDataStructure termStructure = doc.parsedTerms;
                for (String key : termStructure.termsEntries.keySet()) {
                    String value=termStructure.termsEntries.get(key).getValue();
                    int tf = termStructure.termsEntries.get(key).getTF();
                    double weight = termStructure.termsEntries.get(key).getWeight();
                    // term appeared first time in this chunk.


                    //term already in valuesOfChunck. need to append <> segment of this doc .
                     if (valuesOfChunck.containsKey(value)){
                        valuesOfChunck.replace(value,valuesOfChunck.get(value),writeSegmentToPostingFileInFormat(valuesOfChunck.get(value),doc.getDocNo(),tf,weight));
                    }

                    //in case value start with upper case and this term already exist in lower case
                    //need to save in lower case
                     else if (Character.isUpperCase(value.charAt(0)) && valuesOfChunck.containsKey( value.toLowerCase())){

                         String info=valuesOfChunck.get(key.toLowerCase());
                         valuesOfChunck.replace(value.toLowerCase(),valuesOfChunck.get(value.toLowerCase()),writeSegmentToPostingFileInFormat(info,doc.getDocNo(),tf,weight));

                    }
                     //in case value start with lower case and this term already exist in upper case
                     //need to save in lower case
                    else if (Character.isLowerCase(value.charAt(0)) && valuesOfChunck.containsKey( value.toUpperCase())){

                        String info= valuesOfChunck.get(value.toUpperCase());
                        valuesOfChunck.remove(value.toUpperCase());
                         valuesOfChunck.put(value.toLowerCase(),writeSegmentToPostingFileInFormat(info,doc.getDocNo(),tf,weight));

                    }
                    // in case value not exist at all , first time
                    else if (!valuesOfChunck.containsKey(value))
                        valuesOfChunck.put(value, writeSegmentToPostingFileInFormat("", doc.getDocNo(), tf, weight));

                }

            }

          //  StrBuilder contentOfFile=new StrBuilder();
            //write everything to file.
            File statText = new File(postingFilePath +File.separator+ index + ".txt");
            index++;
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(statText);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            for (String term : valuesOfChunck.keySet()){
                int df= countMatches(valuesOfChunck.get(term),'<');
                try {
                    w.write(term+"|"+ df+"|"+valuesOfChunck.get(term)+System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //contentOfFile.append(term+"|"+ df+"|"+valuesOfChunck.get(term));
            }
          //  writeToFilePool.addContentToStack(contentOfFile.toString());


        try {
            w.close();
            osw.close();
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

//        } catch (IOException e) {
//            System.err.println("Problem writing to the files "+ docsContainer.get(0).getDocNo() +" to "+ docsContainer.get(docsContainer.size()-1).getDocNo() );
//        }
    }

    //this function help to add segment to line in posting file .
    //segment = <docID, tf, weight>
    public String writeSegmentToPostingFileInFormat(String mainLine ,String docId,int tf,double weight){
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
            File merged = new File(postingFilePath+File.separator+ "merged"+hashCode() +".txt");
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
                    term1 = findTerm((line1));
                    line2 = (String) it2.next();
                    term2 = findTerm((line2));


                } else if (!it1.hasNext() ||  CASE_INSENSITIVE_ORDER.compare(term1, term2)> 0) {
                    out.write(line2 + "\n");
//                    System.out.println(line2 + "\n");
                    line2 = (String) it2.next();
                    term2 = findTerm(line2);
                } else if (!it2.hasNext() || CASE_INSENSITIVE_ORDER.compare(term2, term1)> 0) {
                    out.write(line1 + "\n");
//                    System.out.println(line1 + "\n");
                    line1 = (String) it1.next();
                    term1 = findTerm(line1);
                }

            }

            // in case doc2 end and doc1 not
            if (!it2.hasNext() && it1.hasNext()){
                while(it1.hasNext()) {
                    out.write(line1 + "\n");
                    line1 = (String) it1.next();
                    term1 = findTerm(line1);
                }
            }

            // in case doc1 end and doc2 not
            if (!it1.hasNext() && it2.hasNext()){
                while(it2.hasNext()) {
                    out.write(line2 + "\n");
                    line2 = (String) it2.next();
                    term2 = findTerm(line2);
                }
            }

            out.flush();
            //delete original files
            File file1=new File (path1);
            file1.delete();
            File file2=new File (path2);
            file2.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


// this function extract the term from line in file (term came before '|')
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


    class RatingCompare implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            o1=o1.substring(0,o1.indexOf('|'));
            o2=o2.substring(0,o2.indexOf('|'));
            return  (CASE_INSENSITIVE_ORDER.compare(o1, o2));

        }
    }

}