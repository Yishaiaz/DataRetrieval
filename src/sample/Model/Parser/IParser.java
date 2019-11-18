package sample.Model.Parser;

import sample.Model.Document;

import java.io.BufferedReader;

public interface IParser {

    public Document Parse(String fullDoc) throws Exception;

}
