package sample.Model;

public class Document {
    public String DocNo;
    public String date;
    public String headline;
    StringBuilder text;


    public Document(String docNo, String date, StringBuilder text) {
        DocNo = docNo;
        this.date = date;
        this.text = text;
    }
}
