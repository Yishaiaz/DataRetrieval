package sample.Model;

import sample.Model.TaskPool.WriteToFilePool;
import sample.Model.TasksPoolsRunners.WriteToFileTask;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CorpusHandler {
    private String corpusPath;
    public String postingFilesPath="";
    private ArrayList<String> filesPaths; //list contains all paths in Corpus dir.
    public HashSet<String> stopWords = new HashSet<>();
    public HashSet<String> months = new HashSet<>();
    ExecutorService pool = Executors.newFixedThreadPool(3);
    WriteToFilePool writeToFilePool;

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

    // This function add to 'filesPath' list with files paths.
    public void initListOfFilesPaths() {  //"C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus"
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

    public void findTextInDocs(boolean withStemming) throws FileNotFoundException {
        writeToFilePool= new WriteToFilePool();
        WriteToFileTask tasker1= new WriteToFileTask("task1",writeToFilePool,postingFilesPath);
        WriteToFileTask tasker2= new WriteToFileTask("task2",writeToFilePool,postingFilesPath);
        WriteToFileTask tasker3= new WriteToFileTask("task3",writeToFilePool,postingFilesPath);


        pool.execute(tasker1);
        pool.execute(tasker2);
        pool.execute(tasker3);


        ReadFile readFile = new ReadFile(this.corpusPath, this.stopWords, this.months,withStemming,postingFilesPath,writeToFilePool);
        //send every file to ReadFile for preparation for parsing.
        for (String path : filesPaths) {
            if(path.endsWith(".DS_Store")){
                System.out.println("you and your mac");
            }
            else{
                readFile.prepareDocToParse(path,20);

            }
        }

        while(writeToFilePool.areTasksLeft()){}
        pool.shutdown();
        System.out.println("end");


    }
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

}