package sample.Model;

public abstract class Term {
    public String content;
    public int occurrence;

    public Term(String content) {
        this.content = content;
        this.occurrence = 0;
    }

    public abstract void  prepareTerm();
}
