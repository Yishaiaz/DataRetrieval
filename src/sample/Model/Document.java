package sample.Model;

import sample.Model.DataStructures.TermHashMapDataStructure;

/**
 * This class represent one Document
 */
public class Document {
    public String DocNo;
    public String date;
    public String headline;
    public TermHashMapDataStructure parsedTerms;
    public int length;


    public Document(String docNo, String date, String headline, String length) {
        this.DocNo = docNo;
        this.date = date;
        this.headline = headline;
        this.length= Integer.parseInt(length);
    }


    public String getDocNo() {
        return DocNo;
    }

    public void setDocNo(String docNo) {
        DocNo = docNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public TermHashMapDataStructure getParsedTerms() {
        return parsedTerms;
    }

    public void setParsedTerms(TermHashMapDataStructure parsedTerms) {
        this.parsedTerms = parsedTerms;
    }

    /**
     * @return how many uniq terms in doc
     */
    public int howManyUniqTermsInDoc(){
       return parsedTerms.howManyUniqTerms();
    }
}
