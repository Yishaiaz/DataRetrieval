package sample.Model.TasksPoolsRunners;

import sample.Model.TaskPool.WriteToFilePool;
import java.io.*;

public class WriteToFileTask implements Runnable{
    private String name;
    private WriteToFilePool pool;
    private String postingFilePath;
    public static int index=0;
    public WriteToFileTask(String name, WriteToFilePool pool,String postingFilePath)
    {   this.name = name;
        this.pool = pool;
        this.postingFilePath=postingFilePath;
    }

    @Override
    public void run() {
        while (true) {
                if (this.pool.areTasksLeft()) {
                    try {
                        this.pool.poolAccessSemaphore.acquire();
                        if (this.pool.areTasksLeft()) {
                            String content = this.pool.receiveTempFileTask();
                            this.pool.poolAccessSemaphore.release();
                            //write everything to file.
                            File statText = new File(postingFilePath + File.separator +index + ".txt");
                            FileOutputStream is = new FileOutputStream(statText);
                            OutputStreamWriter osw = new OutputStreamWriter(is);
                            Writer w = new BufferedWriter(osw);
                            w.write(content);
                            System.out.println(content.substring(0,30));
                            w.close();
                            osw.close();
                            is.close();
                            index++;

                        }

                    } catch (InterruptedException | FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

        }
    }
}
