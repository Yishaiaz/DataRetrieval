package sample.Model;


import sample.Model.TasksPoolsRunners.MergeTasksPool;
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
        try
        {
            this.pool.poolAccessSemaphore.acquire();
            String[] myPaths = this.pool.recieveTempFileTask();
            if (myPaths==null){
                this.pool.poolAccessSemaphore.release();
                return;
            }
            System.out.println(String.format("the tasker: %s got the paths: %s, %s", name, myPaths[0], myPaths[1]));
            this.pool.poolAccessSemaphore.release();
            Thread.sleep(1000);
        }

        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}

public class tries {
    static final int MAX_T = 3;

    public static void main(String[] args) {
        //TESTING THE STEMMER:
        String path = "/Users/yishaiazabary/Desktop/University/שנה ד/TestForDataRetrievelMultiThread";
        MergeTasksPool t1 = new MergeTasksPool(path);

        Runnable tasker1 = new Task("task1", t1);
        Runnable tasker2 = new Task("task2", t1);
        Runnable tasker3 = new Task("task3", t1);
        Runnable tasker4 = new Task("task4", t1);
        Runnable tasker5 = new Task("task5", t1);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
        pool.execute(tasker1);
        pool.execute(tasker2);
        pool.execute(tasker3);
        pool.execute(tasker4);
        pool.execute(tasker5);


        pool.shutdown();
    }

}





