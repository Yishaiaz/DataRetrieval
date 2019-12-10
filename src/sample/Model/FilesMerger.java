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
    /**
     * a runnable implementation of the file merging process.
     * receives two paths for the posting files to merge.
     * uses two "pointers" one on each document in order to keep everything sorted without the need to resort.
     */
    public static int indexForMergeFiles=0;
    private String firstPath;
    private String secondPath;
    private String postingFilePath;

    /**
     * constructor
     * @param firstPath - String, the full path to the first file to merge
     * @param secondPath - String, the full path to the second file to merge
     * @param postingFilePath - String, the full path to the DIRECTORY where the new merged file should be written to.
     */
    public FilesMerger(String firstPath, String secondPath, String postingFilePath){
        this.firstPath = firstPath;
        this.secondPath = secondPath;
        this.postingFilePath = postingFilePath;
    }

    /**
     * all functionality is wrapped in a try/catch block in order to avoid any issues.
     * run() sorts both documents separately, then using a BufferedReader object reads both documents line by line.
     * keeps the merged document sorted.
     * writes the merged document using BufferedWriter object.
     */
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
            BufferedWriter out = new BufferedWriter(fileWriter);

            String line1 = br1.readLine();
            String term1 = StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));

            String line2 = br2.readLine();
            String term2 =StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
            while (line1!=null && line2 != null) {
                //in case its same term
                if (CASE_INSENSITIVE_ORDER.compare(term1,term2) == 0) {
                    try{
                        line1= line1.substring(line1.indexOf("<"),line1.lastIndexOf(">")+1);
                        line2=line2.substring(line2.indexOf("<"),line2.lastIndexOf(">")+1);
                        String temp= StringUtils.remove(line1, "\n")+StringUtils.remove(line2,"\n");
                        //count total df
                        int df= StringUtils.countMatches(temp,"<");
                        //out.write(term1+"|"+df+"|"+line1+line2+"\n");
                        out.write(term1+"|"+df+"|"+temp+System.lineSeparator());


                        line1 = br1.readLine();
                        term1 = StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));

                        line2 = br2.readLine();
                        term2 =StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
                    }catch (Exception e){

                    }
                }
                else if (!(line1!=null) ||  CASE_INSENSITIVE_ORDER.compare(term1, term2)> 0) {
                    out.write(line2 + System.lineSeparator());

                    line2 = br1.readLine();
                    term2 = StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
                } else if (!(line2!=null) || CASE_INSENSITIVE_ORDER.compare(term2, term1)> 0) {
                    out.write(line1 + System.lineSeparator());

                    line1 = br2.readLine();
                    term1 =  StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
                }

            }
            // in case doc2 end and doc1 not
            if (!(line2!=null) && (line1!=null)){
                while((line1!=null)) {
                    out.write(line1 + System.lineSeparator());
                    line1 = br1.readLine();
                    term1 =  StringUtils.substring(line1, 0, StringUtils.indexOf(line1,'|'));
                }
            }

            // in case doc1 end and doc2 not
            if (!(line1!=null) && (line1!=null)){
                while((line2!=null)) {
                    out.write(line2 + System.lineSeparator());
                    line2 = br2.readLine();
                    term2 =  StringUtils.substring(line2, 0, StringUtils.indexOf(line2,'|'));
                }
            }
            out.close();
            br1.close();
            br2.close();
            //delete original files
            try{
                File file1=new File (path1);
                file1.delete();
            }catch(Exception e){
            }
            try{
                File file2=new File (path2);
                file2.delete();
            }catch(Exception e){
            }

        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    /**
     * private method, receives a full path to a txt document, reads it, and sorts it
     * using a custom comparator object. @see RatingComparator inner class.
     * catches all exeptions internally
     * @param path - String, full path to a .txt file
     */
    private void sortDocument(String path) throws IOException {

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
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
            BufferedWriter out = new BufferedWriter(fileWriter);
            for (String outputLine : lineList) {
                out.write(outputLine + System.lineSeparator());
            }
            out.close();
        }catch (StringIndexOutOfBoundsException e){
            System.out.println(e.getMessage());
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
}
