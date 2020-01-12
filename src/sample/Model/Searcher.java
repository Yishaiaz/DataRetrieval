package sample.Model;

import com.google.common.io.Resources;
import com.medallia.word2vec.Word2VecExamples;
import com.medallia.word2vec.Word2VecModel;
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
import java.util.*;


/**
 * responsible to pass the Ranker parsed query and get from it RankedDocumentStructure with relevant docs.
 * extract top50 and write to result file.
 */
public class Searcher {
    private final String resultPath;
    String corpusPath;
    String dictionaryPath;
    String postingFilesPath;
    ArrayList<String> dictionaryContent = new ArrayList<>();
    private int Max_Additional_Terms_API = 4;
    private boolean isStemming=false;

    public Searcher(String corpusPath, String postingFilesPath,boolean isStemming,String resultPath) {
        this.corpusPath=corpusPath;
        this.resultPath=resultPath;
        if (!isStemming) {
            this.dictionaryPath = corpusPath + File.separator + "DictionaryNoStemming.txt"; //that's where we save the dictionary.
            this.postingFilesPath = postingFilesPath+File.separator+"notStemmingPostingFile.txt";
        } else if (isStemming) {
            this.dictionaryPath = corpusPath + File.separator + "DictionaryStemming.txt";
            this.postingFilesPath = postingFilesPath+File.separator+"stemmingPostingFile.txt";
        }
    }
    public void search(Document query, boolean isStemming,Boolean withSemantic) {
        /** will hold the result ->relevant documents in decreasing order.*/
        RankedDocumentsStructure rankedDocumentsStructure ;
        String pathToDocsInfo="";
        if (isStemming==false)
         pathToDocsInfo = Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt";
        else{
             pathToDocsInfo = Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoStemming.txt";
        }
        long numOfDocs = 0;

        try {
            numOfDocs = Files.lines(Paths.get(pathToDocsInfo)).count();
                Ranker ranker = new Ranker(dictionaryPath, postingFilesPath, pathToDocsInfo, (int) numOfDocs, 250);

            if (withSemantic) {
              //  Document queryWithSemantic = useSemanticTreat(query, isStemming);
                Document queryWithSemantic = useSemanticTreatOffline(query, isStemming);
                rankedDocumentsStructure=ranker.rankDocsForQuery(queryWithSemantic.parsedTerms,query.DocNo);

            } else {//without semantic treat.
                rankedDocumentsStructure = ranker.rankDocsForQuery(query.parsedTerms, query.DocNo);
            }
            rankedDocumentsStructure.onlyBest50(); // leave only best 50 docs.
            writeResultsToFile(rankedDocumentsStructure); //write final results to file
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * write ranked results to file.
     * required format : query_id, iter, docno, rank, sim, run_id
     */
    public void writeResultsToFile(RankedDocumentsStructure rankedDocumentStructure) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultPath + File.separator + "results.txt",true));

            for (RankedDocumentsStructure.Pair pair : rankedDocumentStructure.topRankedDocs) {
                Double rank = pair.getScore();
                //format requested for Track_Eval program
                StrBuilder line=new StrBuilder();
                line.append(rankedDocumentStructure.queryId+" "); //queryId
                line.append("0 "); //ignore
                line.append(pair.getDocId());
                line.append(rank+" ");
                line.append("42.38 "); //Double , ignore.
                line.append("mt"); // some name, ignore.
                bufferedWriter.write(line.toString()+ System.lineSeparator());
            }
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document useSemanticTreatOffline(Document query, boolean isStemming) throws IOException, com.medallia.word2vec.Searcher.UnknownWordException {
        this.isStemming = isStemming;
        readDictionary(); //read dictionary to memory.
        ArrayList<String> termsFromAPI = new ArrayList<>(); //hold all terms from API
        String pathToModelFiletxt= Resources.getResource("word2vec.c.output.model.txt").getPath();
        Word2VecModel semanticModel=Word2VecModel.fromTextFile(new File(pathToModelFiletxt));

        for (String key : query.parsedTerms.termsEntries.keySet()) {
            if (key.contains(" ")) //we will not check terms contain more then one word
                continue;
            com.medallia.word2vec.Searcher semanticSearcher = semanticModel.forSearch();
            List<com.medallia.word2vec.Searcher.Match> matches = semanticSearcher.getMatches(key, 3);
            for (com.medallia.word2vec.Searcher.Match match : matches) {
                String similarWord = match.match();
                double distance=match.distance();
                if (distance>0.95){
                    if (isStemming) similarWord = stem(similarWord);
                    boolean isExistInDic = dictionaryContent.contains(similarWord);
                    if (!isExistInDic)  // try capital term
                        isExistInDic = dictionaryContent.contains(similarWord.toUpperCase());
                    if (!isExistInDic)// the term isn't in the corpus
                        continue;

                    //check if new term not already in query' terms or in API array.
                    if (query.parsedTerms.termsEntries.get(similarWord) == null && !termsFromAPI.contains(similarWord)) {
                        termsFromAPI.add(similarWord);
                    } else continue;
            }
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


    /**
     * connect to DataMuse API and send terms from query .
     * API return synonyms.
     * insert only synonyms that exist in dictionary .
     * @param query
     * @param isStemming
     * @return
     * @throws IOException
     */
    public Document useSemanticTreat(Document query, boolean isStemming) throws IOException {
        this.isStemming=isStemming;
        readDictionary(); //read dictionary to memory.
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
     * @param
     */
    public void readDictionary() {
        File dictionary = new File(dictionaryPath );

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
