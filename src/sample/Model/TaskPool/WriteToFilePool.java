package sample.Model.TaskPool;

import sun.awt.Mutex;

import java.util.Stack;
import java.util.concurrent.Semaphore;

public class WriteToFilePool {
    public static Semaphore poolAccessSemaphore=new Semaphore(3);
    private Stack<String> contentFilesStack;

    public WriteToFilePool() {
        this.contentFilesStack = new Stack<>();
    }

    public boolean areTasksLeft() {
        return contentFilesStack.size()>0;
    }

    public String receiveTempFileTask() {
            return contentFilesStack.pop();

    }

    public void addContentToStack(String content){
        contentFilesStack.push(content);
    }
}
