package sample.Model;


import org.apache.commons.lang3.StringUtils;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.TasksPoolsRunners.MergeTasksPool;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


class Task implements Runnable
{
    private String name;
    private MergeTasksPool pool;
    public Task(String name, MergeTasksPool pool)
    {   this.name = name;
        this.pool = pool;
    }

    // Prints task name and sleeps for 1s
    // This Whole process is repeated 5 times
    public void run()
    {
        while(this.pool.areTasksLeft()){
            try
            {
                this.pool.poolAccessSemaphore.acquire();
                String[] myPaths = this.pool.recieveTempFileTask();
                if (myPaths==null){
                    this.pool.poolAccessSemaphore.release();
                    return;
                }
                this.pool.poolAccessSemaphore.release();
                //here you should preform a task.
                System.out.println(String.format("the tasker: %s got the paths: %s, %s", name, myPaths[0], myPaths[1]));
                Thread.sleep(1000);
            }

            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }
}

public class tries {
    static final int MAX_T = 3;
    private Timestamp convert(String day, String month, String year){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(year+"-"+month+"-"+day);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return timestamp;
        }catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        //TESTING THE STEMMER:
//        String day = "04";
//        String month = "11";
//        String year = "1997";
//        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            Date parsedDate = dateFormat.parse(year+"-"+month+"-"+day);
//            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
//        } catch(Exception e) { //this generic but you can control another types of exception
//            // look the origin of excption
//        }

//        Timestamp ts = new Timestamp()

//        TESTING RANKER
        //building a fake termHashMap
//        TermHashMapDataStructure fakeTermHashMap = new TermHashMapDataStructure();
//        String[] someTerms = {"bus", "tall"};
//        int ctr = 0;
//        for (String termName :
//                someTerms) {
//            fakeTermHashMap.insert(termName, ctr, 1);
//            ctr+=1;
//        }
//
//
//        String pathToDictionary = "/Users/yishaiazabary/Desktop/University/שנה ד/Engine/DataRetrieval/sample files to work with/DictionaryNoStemming.txt";
//        String pathToPosting = "/Users/yishaiazabary/Desktop/University/שנה ד/Engine/DataRetrieval/sample files to work with/notStemmingPostingFile.txt";
//        String pathToDocsInfo = "/Users/yishaiazabary/Desktop/University/שנה ד/Engine/DataRetrieval/sample files to work with/DocsInfoNoStemming.txt";
//        Ranker ranker;
//        try{
//            ranker = new Ranker(pathToDictionary, pathToPosting, pathToDocsInfo, 500000, 250);
//            RankedDocumentsStructure rm = ranker.rankDocsForQuery(fakeTermHashMap, "asdfasdf");
//            System.out.println(rm);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
////        ranker.fakeRankQuery(someTerms);
//        System.out.println("s");
//        char c = 'f';
//        System.out.println(String.valueOf((char)(c+1)));
        String line = "termName|5|<LA050989-0101 ,1,1.0><LA100190-0025 ,1,1.0><LA122690-0116 ,1,1.0><LA061790-0171 ,1,1.0><LA022490-0118 ,1,1.0>";
        line = StringUtils.substring(line, StringUtils.indexOf(line, "|<")+1);
        int termOccurSum = 0;
        for (String sa :
                StringUtils.split(line, "<>")) {
            termOccurSum += Integer.parseInt(StringUtils.split(sa, ",")[1]);
        }
        System.out.println(termOccurSum);
//        String pathToIndex = "d:\\documents\\users\\benbsaha\\Downloads\\post";
//        String pathToDictionaryDirectory = "d:\\documents\\users\\benbsaha\\Downloads\\post";
//        boolean isStemming = true;
//
//
//
//        DictionaryParser dictPars = new DictionaryParser(pathToIndex, pathToDictionaryDirectory, isStemming);
//        dictPars.run();


    }

}





