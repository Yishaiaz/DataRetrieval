package sample.Model;

import org.apache.commons.lang3.StringUtils;
import sample.Model.Indexer.DocIndexer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class FilesMerger implements Runnable{
    public static int indexForMergeFiles=0;
    private String firstPath;
    private String secondPath;
    private String postingFilePath;

    public FilesMerger(String firstPath, String secondPath, String postingFilePath){
        this.firstPath = firstPath;
        this.secondPath = secondPath;
        this.postingFilePath = postingFilePath;
    }

    @Override
    public void run() {
        String path1 = this.firstPath;
        String path2 = this.secondPath;
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
//            e.printStackTrace();
        }
    }
    private void sortDocument(String path) throws IOException {

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
//            System.out.println(path);
            fileReader = new FileReader(path);
            bufferedReader =  new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            return;
        }

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
        try{
            Collections.sort(lineList,new RatingCompare());
            FileWriter fileWriter = new FileWriter(path);
            PrintWriter out = new PrintWriter(fileWriter);
            for (String outputLine : lineList) {
                out.println(outputLine);
            }
            out.flush();
            out.close();
            fileWriter.close();
        }catch (StringIndexOutOfBoundsException e){
            System.out.println("it went wrong here\n"+lineList);
//            System.out.printf("");
        }


    }
    private int countMatches(String str, char c) {
        int count = 0;

        for (char ch : str.toCharArray()) {
            if (ch == c) {
                count++;
            }
        }
        return count;
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
