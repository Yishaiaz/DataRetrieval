package sample.Model.TasksPoolsRunners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MergeTasksPool {
    private int ctrUntilListUpdate = 5;
    static public Semaphore poolAccessSemaphore= new Semaphore(1);
    private Stack<String> toMergePath;
    private String tempFilesPath;

    public MergeTasksPool(String tempFilesDirectoryPath) {
        this.tempFilesPath = tempFilesDirectoryPath;
        this.toMergePath = this.getCurrentFilesInDir(tempFilesDirectoryPath);
    }
    private Stack<String> getCurrentFilesInDir(String tempFilesDirectoryPath){
        Stack<String> toMergePaths = new Stack<>();
        File tempFilesFolder = new File(tempFilesDirectoryPath);
        //check if path exist
        if (tempFilesFolder != null) {
            try (Stream<Path> walk = Files.walk(Paths.get(tempFilesDirectoryPath))) {
                List<String> tempFiles = walk.filter(Files::isRegularFile)
                        .map(x -> x.toString()).collect(Collectors.toList());
                toMergePaths.addAll(tempFiles);
                //    System.out.println(filesPaths.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Path doesn't exist.");

        return toMergePaths;
    }


    public String[] recieveTempFileTask(){
        String[] tasks = new String[2];
        tasks[0] = this.toMergePath.pop();
        tasks[1] = this.toMergePath.pop();
        this.ctrUntilListUpdate-=1;
        if (this.ctrUntilListUpdate == 0 || this.toMergePath.size()<10){
            this.ctrUntilListUpdate = 5;
            this.toMergePath = this.getCurrentFilesInDir(this.tempFilesPath);
            if(this.toMergePath.size()<=1){
                return null;
            }
        }
        return tasks;
    }
}
