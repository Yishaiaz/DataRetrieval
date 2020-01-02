package sample.Model;

import org.apache.commons.lang3.text.StrBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import sample.Model.DataStructures.TermHashMapEntry;
import sample.Model.Parser.Stemmer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Searcher {

    String dictionaryPath;
    String postingFilesPath;
    ArrayList<String> dictionaryContent = new ArrayList<>();
    private int Max_Additional_Terms_API = 4;

    public Searcher(String corpusPath, String postingFilesPath) {
        this.dictionaryPath = corpusPath; //that's where we save the dictionary.
        this.postingFilesPath = postingFilesPath;
    }

    public void search(Document query, boolean isStemming,Boolean withSemantic) {
        /** will hold the result ->relevant documents in decreasing order.*/
        RankedDocumentsStructure rankedDocumentsStructure = new RankedDocumentsStructure(query.getDocNo());
        String pathToDocsInfo = Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt";
        long numOfDocs = 0;

        try {
            numOfDocs = Files.lines(Paths.get(pathToDocsInfo)).count();
            Ranker ranker = new Ranker(dictionaryPath, postingFilesPath, pathToDocsInfo, (int) numOfDocs, 0);

            if (withSemantic) {
                Document queryWithSemantic = useSemanticTreat(query, isStemming);
                ranker.rankQuery(queryWithSemantic.parsedTerms);

            } else //without semantic treat.
                ranker.rankQuery(query.parsedTerms);

            rankedDocumentsStructure.onlyBest50(); // leave only best 50 docs.
            writeResultsToFile(rankedDocumentsStructure); //write final results to file
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write ranked results to file.
     * required format : query_id, iter, docno, rank, sim, run_id
     */
    public void writeResultsToFile(RankedDocumentsStructure rankedDocumentStructure) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.dictionaryPath + File.separator + "results.txt"));

            for (String key : rankedDocumentStructure.documents.keySet()) {
                Double rank = rankedDocumentStructure.documents.get(key);
                //format requested for Track_Eval program
                StrBuilder line=new StrBuilder();
                line.append(rankedDocumentStructure.queryId+","); //queryId
                line.append("0,"); //ignore
                line.append(key+",");
                line.append(rank+",");
                line.append("42.38,"); //Double , ignore.
                line.append("run"); // some name, ignore.
                bufferedWriter.write(line.toString()+ System.lineSeparator());
            }

            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document useSemanticTreat(Document query, boolean isStemming) throws IOException {
        readDictionary(isStemming); //read dictionary to memory.
        ArrayList<String> termsFromAPI=new ArrayList<>(); //hold all terms from API
        for (String key : query.parsedTerms.termsEntries.keySet()) {
            if(key.contains(" ")) //we will not check terms contain more then one word
                continue;
            URL url = new URL("https://api.datamuse.com/words?ml=" + key);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
           // BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));

            String json_str = "";
            String line = "";
            while ((line = in.readLine()) != null) {
                json_str = json_str + line;
            }
            in.close();
            JSONArray jsonArray = new JSONArray(json_str);
            jsonArray = new JSONArray(json_str);
            int countAdditionalTerms = 0;
            for (int k = 0; k < jsonArray.length(); k++) {
                JSONObject obj = (JSONObject) jsonArray.get(k);
                String api_term = (String) obj.get("word");
                Boolean isExistInDic = false;
                if (isStemming) api_term = stem(api_term);
                isExistInDic = dictionaryContent.contains(api_term);
                if (!isExistInDic)  // try capital term
                    isExistInDic = dictionaryContent.contains(api_term.toUpperCase());
                if (!isExistInDic)// the term isn't in the corpus
                    continue;

                //check if new term not already in query' terms or in API array.
                if (query.parsedTerms.termsEntries.get(api_term) == null && !termsFromAPI.contains(api_term)) {
                    termsFromAPI.add(api_term);
                    countAdditionalTerms++;

                } else continue;
                //save only the MAX_SYN_TERMS for one term from query
                if (countAdditionalTerms== Max_Additional_Terms_API)
                    break;
            }
        }

        //add all new terms to Query term , and return
        for (String newTerm: termsFromAPI){
            // we will give 1.6 weight to terms from API
            TermHashMapEntry entry = new TermHashMapEntry(newTerm, 1.6);
            query.parsedTerms.termsEntries.put(newTerm, entry);
        }
        return query;
    }

    private String stem(String synonymous_term) {

        String final_term = "";
        String[] split = synonymous_term.split(" ");
        for (String s : split
        ) {
            Stemmer stemmer = new Stemmer();
            stemmer.add(s.toCharArray(), s.length());
            stemmer.stem();
            final_term += stemmer.toString() + " ";
        }
        return final_term.substring(0, final_term.length() - 1);

    }

    /**
     * load dictionary to RAM
     * @param isStemming
     */
    public void readDictionary(boolean isStemming) {
        File dictionary = null;
        if (isStemming) {
            dictionary = new File(dictionaryPath + File.separator + "DictionaryStemming.txt");
        } else {
            dictionary = new File(dictionaryPath + File.separator + "DictionaryNoStemming.txt");
        }

        String str = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dictionary));

            while (true) {

                if (!((str = br.readLine()) != null && (!str.equals("")))) break;

                str = str.substring(0, str.lastIndexOf(',')); // remove from presentation pointer to line
                str = str.substring(0, str.lastIndexOf(',')); // remove from presentation df
                str = str.substring(0, str.lastIndexOf(','));//remove Total Corpus Appearances
                dictionaryContent.add(str);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
