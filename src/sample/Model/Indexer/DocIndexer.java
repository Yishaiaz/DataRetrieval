package sample.Model.Indexer;

import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;

import java.io.*;

public class DocIndexer {

    public DocIndexer() {

    }

    public void index(Document doc){
        try {
            //Whatever the file path is.
            File statText = new File("C:\\Users\\Sahar Ben Baruch\\Desktop\\aba\\"+doc.DocNo+".txt");
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            TermHashMapDataStructure termStructure= doc.parsedTerms;
            for (String key : termStructure.termsEntries.keySet()) {
                w.write(key);
                int tf=termStructure.termsEntries.get(key).getTF();
                w.write("|"+doc.DocNo+"|"+tf+'\n');

            }

            w.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file "+doc.DocNo);
        }
    }


    }
