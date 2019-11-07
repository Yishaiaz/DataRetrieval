package sample.Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CorpusHandler {
    private String corpusPath;
    private ArrayList<String> filesPaths; //list contains all paths in Corpus dir.
    private ReadFile readFile;

    public CorpusHandler(String corpusPath) {
        this.corpusPath = corpusPath;
        this.filesPaths = new ArrayList<>();

    }

    // This function add to 'filesPath' list with files paths.
    public void initListOfFilesPaths() {  //"C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus"
        File corpusFolder = new File(corpusPath);

        //check if path exist
        if (corpusFolder != null) {
            try (Stream<Path> walk = Files.walk(Paths.get(corpusPath))) {

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

    public void findTextInDocs() throws FileNotFoundException {
        readFile = new ReadFile(corpusPath);
        //send every file to ReadFile for preparation for parsing.
        for (String path : filesPaths) {
            readFile.prepareDocToParse(path);
        }

    }


}


