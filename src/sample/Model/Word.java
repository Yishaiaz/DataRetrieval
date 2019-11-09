package sample.Model;

public class Word extends Term {
    boolean isCapital;
    boolean isRange;
    boolean isDate;
    boolean isPersona;

    public Word(String content) {
        super(content);
    }

    public void prepareTerm(){

    }
}
