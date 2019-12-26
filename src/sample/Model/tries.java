package sample.Model;


import sample.Model.TasksPoolsRunners.MergeTasksPool;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



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
        String day = "04";
        String month = "11";
        String year = "1997";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(year+"-"+month+"-"+day);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch(Exception e) { //this generic but you can control another types of exception
            // look the origin of excption
        }

//        Timestamp ts = new Timestamp()
    }

}





