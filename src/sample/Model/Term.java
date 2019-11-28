package sample.Model;

public abstract class Term {
    public String content;
    public int occurrence;
    public double wight;

    public Term(String content) {
        this.content = content;
        this.occurrence = 0;
        this .wight=0;
    }

    public abstract void  prepareTerm();
}
