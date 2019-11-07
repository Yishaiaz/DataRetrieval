package sample.Model;

import java.io.*;

public class ReadFile {
    private final String corpusPath;

    public ReadFile(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    //  prepare file for parsing by extract fields and create object of doc.
    public void prepareDocToParse(String path) {

        BufferedReader br = null;
        String docNo = null;
        String date1;
        String t1;

        try {
            // stream to file
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = " ";

        while (line != null) {
            try {
                line = br.readLine();

                while (line != null && !line.equals("<DOC>"))
                    line = br.readLine();

                //check if we reach to the end of file.
                if (line == null)
                    break;

                StringBuilder text = new StringBuilder(); // for text field of current doc.
                //start new doc. define by "<DOC>"

                line = br.readLine();
                //extract DocNo
                if (line.startsWith("<DOCNO>")) {
                    docNo = line.replaceAll("<DOCNO>", "");
                    docNo = docNo.replaceAll("</DOCNO>", "");
                }
                while (!line.startsWith("<DATE1>")) {
                    line = br.readLine();
                }
                //extract Date1
                date1 = line.replaceAll("<DATE1>", "");
                date1 = date1.replaceAll("</DATE1>", "");

                while (!line.contains("<TEXT>")) {
                    line = br.readLine();
                }
                //extract Text
                line = br.readLine();

                while (!line.equals("</TEXT>")) {
                    text.append(line);
                    line = br.readLine();
                }

                //just for testing
                System.out.println(path);
                System.out.println(text);
                System.out.println("----------------");
                Document doc = new Document(docNo, date1, text);
                //parser.parse(doc);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

}
