package sample.Model.Parser;

import org.apache.commons.lang3.text.StrBuilder;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

public class DocParser implements IParser{
    private Boolean wordStemming;
    private HashSet<String> stopWords;
    private HashSet<String> months;
    private Stemmer stemmer;
//    private Document doc;

    public DocParser(Boolean wordStemming, HashSet<String> stopWords, HashSet<String> months) {
        this.wordStemming = wordStemming;
        this.stopWords = stopWords;
        this.months = months;
        if(this.wordStemming){
            this.stemmer = new Stemmer();
        }
    }


    @Override
    public Document Parse(String fullDoc) throws Exception {
//        StringUtils stringUtils = new StringUtils();
        TermHashMapDataStructure termHashMapDataStructure = new TermHashMapDataStructure();
        String[] docData = getDocData(fullDoc);
//        fullDoc = StringUtils.remove(fullDoc, "");

        int textIterator=0;
        int termLocationIterator = 0;

        Document doc = new Document(docData[0], docData[1], docData[2], docData[3]);
        //add doc date as term
        if(!StringUtils.isEmpty(docData[1])){
            termHashMapDataStructure.insert(docData[1], termLocationIterator, 1.8);
            termLocationIterator+=1;
        }
        // adding doc title as a whole term
        if(!StringUtils.isEmpty(docData[2])){
            termHashMapDataStructure.insert(docData[2],termLocationIterator, 2);
            termLocationIterator+=1;
        }

        // adding doc title's words as seperate terms
        docData = initialBadCharacterRemoval(docData);
        for (String word :
                docData[2].split("\t | \n | ")) {
            word = word.replaceAll("[^\\d.]", "");
            if(StringUtils.equals(word, "")){
                continue;
            }else{
                termHashMapDataStructure.insert(word, termLocationIterator, 1.8);
                termLocationIterator+=1;
            }
        }
        //todo: remove unnecessary tags e.g. <F..>

        fullDoc = StringUtils.substring(fullDoc,StringUtils.indexOf(fullDoc, "<TEXT>")+6, StringUtils.indexOf(fullDoc, "</TEXT>"));
        String[] docText =fullDoc.split(" | \n | \t");
        long start = System.currentTimeMillis();
        docText = initialBadCharacterRemoval(docText);
//        System.out.println(String.format("time to clean %d", System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        while(textIterator<docText.length) {
            try {
                docText[textIterator] = StringUtils.remove(docText[textIterator]," ");
                // ignore here from any invalid entry
                if (StringUtils.equals(docText[textIterator], "") || StringUtils.equals(docText[textIterator], "\t") || StringUtils.equals(docText[textIterator], "\n") || StringUtils.equals(docText[textIterator], " ")) {
                    textIterator += 1;
                    continue;
                }

                StrBuilder stringBuilder = new StrBuilder();
                StrBuilder stringNumberBuilder = new StrBuilder();

                ////////////////// RANGES ////////////////////
                if (docText[textIterator].contains("-") && docText[textIterator].split("-").length > 1) {
                    //term-term / word-word-word
                    String[] seperated = docText[textIterator].split("-");
                    if (seperated.length >= 3) {
                        if (StringUtils.isNumeric(seperated[0])&&
                                StringUtils.isNumeric(seperated[1])&& StringUtils.isNumeric(seperated[2])) {
                            //word-word-word pass as single term
                            termHashMapDataStructure.insert(docText[textIterator], termLocationIterator, 1.5);
                            termLocationIterator += 1;
                        } else {
                            //not eligible for a range, split and pass as differnet terms
                            for (int i = 0; i < seperated.length; i++) {
                                if (!StringUtils.equals(seperated[i], "")){
                                    termHashMapDataStructure.insert(seperated[i], termLocationIterator, 1);
                                    termLocationIterator += 1;
                                }
                            }
                        }
                        textIterator += 1;
                    } else if (seperated.length == 2) {
                        // word-word number-word word-number number number
                        termHashMapDataStructure.insert(docText[textIterator], termLocationIterator, 1.5);
                        termLocationIterator += 1;
                        if (StringUtils.isNumeric(seperated[0]) && !StringUtils.equals(seperated[0], "")) {
                            //number-number divide into two number and add them as terms
                            termHashMapDataStructure.insert(seperated[0], termLocationIterator, 1.4);
                            termLocationIterator += 1;
                        }
                        if (StringUtils.isNumeric(seperated[1]) && !StringUtils.equals(seperated[1], "")) {
                            termHashMapDataStructure.insert(seperated[1], termLocationIterator, 1.4);
                            termLocationIterator += 1;
                        }
                        textIterator += 1;
                    }
                } else if (textIterator + 3 < docText.length &&
                        StringUtils.equals(StringUtils.lowerCase(docText[textIterator]), "between") &&
                        StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 2]), "and") &&
                        (StringUtils.isNumeric(docText[textIterator + 1])&& StringUtils.isNumeric(docText[textIterator + 3]))) {
                    // BETWEEN value and value
                    double firstNum = isANumber(docText[textIterator + 1]);
                    double secondNum = isANumber(docText[textIterator + 3]);
                    stringBuilder.append(firstNum);
                    stringBuilder.append("-");
                    stringBuilder.append(secondNum);
                    termHashMapDataStructure.insert(stringBuilder.toString(), termLocationIterator, 1.5);
                    termLocationIterator += 1;
                    textIterator += 4;
                }
                ////////////////// ENTITIES AND ACRONYMS /////
                else if (docText[textIterator].matches("(?:[a-zA-Z]\\.){2,}")) {
                    //Acronyms, if the word is char.char.char. ...
                    termHashMapDataStructure.insert(docText[textIterator], termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    textIterator += 1;
                } else if (textIterator + 2 < docText.length &&
                        Character.isUpperCase(docText[textIterator].charAt(0)) &&
                        StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "of") && (!StringUtils.equals(docText[textIterator + 2], "")) &&
                        Character.isUpperCase(docText[textIterator + 2].charAt(0))) {
                    // Emtities - Llll of Llll , capital letter at both words 1st and 3rd.
                    // adding each word as a seperate term
                    termHashMapDataStructure.insert(docText[textIterator], termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    termHashMapDataStructure.insert(docText[textIterator + 2], termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    // adding the entire entity as a term
                    stringBuilder.append(docText[textIterator]);
                    stringBuilder.append(" ");
                    stringBuilder.append(docText[textIterator + 1]);
                    stringBuilder.append(" ");
                    stringBuilder.append(docText[textIterator + 2]);
                    termHashMapDataStructure.insert(stringBuilder.toString(), termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    textIterator += 3;
                } else if (textIterator + 1 < docText.length &&
                        Character.isUpperCase(docText[textIterator].charAt(0)) && (!StringUtils.equals(docText[textIterator + 1], "")) &&
                        Character.isUpperCase(docText[textIterator + 1].charAt(0))) {
                    // Emtities - Llll Llll , capital letter at both words 1st and 2rd.
                    // adding each word as a seperate term
                    termHashMapDataStructure.insert(docText[textIterator], termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    termHashMapDataStructure.insert(docText[textIterator + 1], termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    // adding the entire entity as a term
                    stringBuilder.append(docText[textIterator]);
                    stringBuilder.append(" ");
                    stringBuilder.append(docText[textIterator + 1]);
                    termHashMapDataStructure.insert(stringBuilder.toString(), termLocationIterator, 1.7);
                    termLocationIterator += 1;
                    textIterator += 2;
                }

                ////////////////// DATES ///////////////////// this is for the MM DD / MM YYYY format, NOT the DD MM format
                else if (this.months.contains(StringUtils.remove(docText[textIterator],"\\.|,"))) {
                    //look for the DD or YYYY
                    if (textIterator + 1 < docText.length &&
                            StringUtils.isNumeric(docText[textIterator + 1])) {
                        // found a full date
                        String monthRep = this.monthIntoNumber(docText[textIterator]);
                        double isNextANumber = isANumber(docText[textIterator + 1]);
                        // check if its DD or YYYY
                        //assume years will be larger than days.
                        if (isNextANumber <= 31) {
                            //DD format
                            stringBuilder.append(monthRep);
                            //check if the number is below 10 to add 0
                            stringBuilder.append("-");
                            if (isNextANumber < 10) {
                                stringBuilder.append("0");
                                stringBuilder.append((int) (isNextANumber));
                            } else {
                                stringBuilder.append((int) isNextANumber);
                            }
                            textIterator += 2;
                        } else if (isNextANumber >= 1000 && isNextANumber <= 9999) {
                            // YYYY format
                            stringBuilder.append((int) isNextANumber);
                            stringBuilder.append("-");
                            stringBuilder.append(monthRep);
                            textIterator += 2;
                        } else {
                            //the number has nothing to do with the month name, pass as a regular word
                            stringBuilder.append(docText[textIterator]);
                            textIterator += 1;
                        }
                    } else {
                        //this is just the month, pass a regular word term
                        stringBuilder.append(docText[textIterator]);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringBuilder.toString(), termLocationIterator, 1.5);
                    termLocationIterator += 1;
                }

                /////////////////numbers, percentages and prices/////////////////
                ////DOLLAR NUMBERS/////
                else if (docText[textIterator].startsWith("$")) {
                    //remove the '$' sign
                    String replace_with = docText[textIterator].replace("$", "");
                    docText[textIterator] = replace_with;
                    //evaluate the number and transform if necessary
                    if ((docText[textIterator].contains("bn") || docText[textIterator].contains("m") || docText[textIterator].contains("B") || docText[textIterator].contains("M"))
                            && startsWithNum(docText[textIterator])) {
                        if (StringUtils.endsWith(docText[textIterator], "bn") ||
                                StringUtils.endsWith(docText[textIterator], "B") ||
                                StringUtils.endsWith(docText[textIterator], "b")) {
                            try {
                                docText[textIterator] = docText[textIterator].replace("bn", "");
                                docText[textIterator] = docText[textIterator].replace("B", "");
                                docText[textIterator] = docText[textIterator].replace("b", "");
                                double num = Double.parseDouble(docText[textIterator].replaceAll(",", "")) * Math.pow(10, 9);
                                String toConcat = this.transformNumber(num, false);
                                stringNumberBuilder.append(toConcat);
                                textIterator += 1;
                            } catch (Exception e) {
                                textIterator += 1;
                            }
                        } else if (StringUtils.endsWith(docText[textIterator], "m") ||
                                StringUtils.endsWith(docText[textIterator], "M")) {
                            try {
                                docText[textIterator] = docText[textIterator].replace("m", "");
                                docText[textIterator] = docText[textIterator].replace("M", "");
                                double num = Double.parseDouble(docText[textIterator].replaceAll(",", "")) * Math.pow(10, 6);
                                String toConcat = this.transformNumber(num, false);
                                stringNumberBuilder.append(toConcat);
                                textIterator += 1;
                            } catch (Exception e) {
                                textIterator += 1;
                            }
                        }
                    } else if (textIterator + 1 < docText.length) {
                        if (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "million")) {
                            try {
    //                            $price million
                                double num = Double.parseDouble(docText[textIterator].replaceAll(",", "")) * Math.pow(10, 6);
                                String toConcat = this.transformNumber(num, false);
                                stringNumberBuilder.append(toConcat);
                                textIterator += 2;
                            } catch (Exception e) {
                                // wasn't a number
    //                            System.out.println(e);
                                textIterator += 1;
                            }

                        } else if (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "billion")) {
                            try {
    //                            $price billion
                                double num = Double.parseDouble(docText[textIterator].replaceAll(",", "")) * Math.pow(10, 9);
                                String toConcat = this.transformNumber(num, false);
                                stringNumberBuilder.append(toConcat);
                                textIterator += 2;
                            } catch (Exception e) {
                                // wasn't a number
    //                            System.out.println(e);
                                textIterator += 1;
                            }
                        } else {
                            // $price
                            stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator].replaceAll(",", "")), false));
                            textIterator += 1;
                        }
                    } else {
                        // $price [end of text]
                        docText[textIterator] = docText[textIterator].replaceAll("[^\\d.]", "");
                        stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator].replaceAll(",", "")), false));
                        textIterator += 1;
                    }
                    if (!StringUtils.equals(stringNumberBuilder.toString(), "")) {
                        termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.3);
                        termLocationIterator += 1;
                    }
                } else if (docText[textIterator].contains("%")) {
                    //Number%
                    stringNumberBuilder.append(docText[textIterator]);
                    textIterator += 1;
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.3);
                    termLocationIterator += 1;
                }else if (StringUtils.isNumeric(docText[textIterator])&&
                        textIterator + 1 < docText.length &&
                        (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "percent") ||
                        StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "percentage"))) {
                    // Number percent /percentage
                    String replace_with = "";
                    double num = 1;
                    String toConcat = "";
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(docText[textIterator]) * num;
                        toConcat = this.transformNumber(num, true);
                        // price m/bn dollars
                        textIterator += 2;
                        stringNumberBuilder.append(toConcat);
                        stringNumberBuilder.append("%");
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
    //                    System.out.println(e);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.3);
                    termLocationIterator += 1;
                }else if ((docText[textIterator].contains("bn") || docText[textIterator].contains("m")) && startsWithNum(docText[textIterator])) {
                    // Number[m/bn]
                    String replace_with = "";
                    double num = 1;
                    String toConcat = "";
                    //removing the units
                    if (docText[textIterator].contains("bn")) {
                        replace_with = docText[textIterator].replace("bn", "");
                        num = Math.pow(10, 9);
                    } else {
                        replace_with = docText[textIterator].replace("m", "");
                        num = Math.pow(10, 6);
                    }
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(replace_with) * num;
                        if (textIterator + 1 < docText.length &&
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "dollars")) {
                            toConcat = this.transformNumber(num, false);
                            // price[m/bn] dollars
                            textIterator += 2;

                        } else {
                            // number[m/bn] dollars
                            toConcat = this.transformNumber(num, true);
                            textIterator += 1;
                        }
                        stringNumberBuilder.append(toConcat);
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
                        //                    System.out.println(e);
                        textIterator += 1;
                    }
                    if (!StringUtils.equals(stringNumberBuilder.toString(), "")){
                        termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.1);
                        termLocationIterator += 1;
                    }

                }else if (StringUtils.isNumeric(docText[textIterator])&&
                        textIterator + 1 < docText.length &&
                        (docText[textIterator + 1].contains("/") &&
                                startsWithNum(docText[textIterator + 1])) &&
                        StringUtils.isNumeric(docText[textIterator])) {
                    // Number fraction dollars
                    stringNumberBuilder.append(isANumber(docText[textIterator]));
                    stringNumberBuilder.append(" ");
                    stringNumberBuilder.append(docText[textIterator + 1]);
                    if (textIterator + 2 < docText.length &&
                            StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 2]), "dollars")) {
                        stringNumberBuilder.append(" Dollars");
                        textIterator += 3;
                    } else {
                        textIterator += 2;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.1);
                    termLocationIterator += 1;
                } else if (StringUtils.isNumeric(docText[textIterator])&& textIterator + 1 < docText.length &&
                        (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "thousand"))) {
                    // Number Thousand
                    String replace_with = "";
                    double num = 1000;
                    String toConcat = "";
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(docText[textIterator]) * num;
                        toConcat = this.transformNumber(num, true);
                        // price m/bn dollars
                        textIterator += 2;
                        stringNumberBuilder.append(toConcat);
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
    //                    System.out.println(e);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.1);
                    termLocationIterator += 1;
                } else if (StringUtils.isNumeric(docText[textIterator]) && textIterator + 1 < docText.length &&
                        (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "dollars"))) {
                    // Number Dollars
                    String replace_with = "";
                    double num = 1;
                    String toConcat = "";
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(docText[textIterator]) * num;
                        toConcat = this.transformNumber(num, false);
                        // price m/bn dollars
                        textIterator += 2;
                        stringNumberBuilder.append(toConcat);
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
    //                    System.out.println(e);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.3);
                    termLocationIterator += 1;
                } else if (StringUtils.isNumeric(docText[textIterator])&& textIterator + 1 < docText.length &&
                        (StringUtils.equals(docText[textIterator + 1].toLowerCase(), "m") ||
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "bn"))) {
                    // Number m/bn
                    String replace_with = "";
                    double num = 1;
                    String toConcat = "";
                    //removing the units
                    if (docText[textIterator + 1].contains("bn")) {
                        num = Math.pow(10, 9);
                    } else {
                        num = Math.pow(10, 6);
                    }
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(docText[textIterator]) * num;
                        if (textIterator + 2 < docText.length &&
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 2]), "dollars")) {
                            toConcat = this.transformNumber(num, false);
                            // price m/bn dollars
                            textIterator += 3;

                        } else {
                            // number m/bn dollars
                            toConcat = this.transformNumber(num, true);
                            textIterator += 2;
                        }
                        stringNumberBuilder.append(toConcat);
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
    //                    System.out.println(e);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.1);
                    termLocationIterator += 1;
                } else if (StringUtils.isNumeric(docText[textIterator])&& textIterator + 1 < docText.length &&
                        (StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "million") ||
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "billion") ||
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 1]), "trillion"))) {
                    //number million/billion
                    String replace_with = "";
                    double num = 1;
                    String toConcat = "";
                    if (docText[textIterator + 1].contains("trillion")) {
                        num = Math.pow(10, 12);
                    } else if (docText[textIterator + 1].contains("billion")) {
                        num = Math.pow(10, 9);
                    } else {
                        num = Math.pow(10, 6);
                    }
                    try {
                        // it's a number for sure
                        num = Double.parseDouble(docText[textIterator]) * num;
                        if (textIterator + 3 < docText.length &&
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 2]), "u.s.") &&
                                StringUtils.equals(StringUtils.lowerCase(docText[textIterator + 3]), "dollars")) {
                            //price million/billion u.s. dollars
                            toConcat = this.transformNumber(num, false);
                            textIterator += 4;

                        } else {
                            // number million/billion
                            toConcat = this.transformNumber(num, true);
                            textIterator += 2;
                        }
                        stringNumberBuilder.append(toConcat);
                    } catch (NumberFormatException e) {
                        // NOT A NUMBER
    //                    System.out.println(e);
                        textIterator += 1;
                    }
                    termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, 1.1);
                    termLocationIterator += 1;
                } else if (StringUtils.isNumeric(docText[textIterator])) {
                    //this is a number
                    double num = isANumber(docText[textIterator]);
                    boolean isDate = false;
                    // checks if there is a month name after it, meaning it is a date.
                    if (textIterator + 1 < docText.length &&
                            months.contains(docText[textIterator + 1].replaceAll("\\.|,", ""))) {
                        //checks if it is a YYYY or a DD
                        if (num <= 31) {
                            // its DD
                            stringNumberBuilder.append(this.monthIntoNumber(docText[textIterator + 1].replaceAll(", | . ", "")));
                            stringNumberBuilder.append("-");
                            stringNumberBuilder.append(this.daysInDatesFormatter(num));
                            isDate = true;
                        } else if (num >= 1000 && num <= 9999) {
                            // its YYYY
                            stringNumberBuilder.append(this.monthIntoNumber(docText[textIterator + 1].replaceAll(", | . ", "")));
                            stringNumberBuilder.append("-");
                            stringNumberBuilder.append(num);
                            isDate = true;
                        }
                        textIterator += 2;
                    } else {
                        //here should be all the rest of the cases of just a number
                        stringNumberBuilder.append(num);
                        textIterator += 1;
                    }
                    if(StringUtils.equals(stringNumberBuilder.toString(), "")){
                        textIterator+=1;
                    }else{
                        termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocationIterator, isDate ? 1.3 : 1.1);
                        termLocationIterator += 1;
                    }


                } else {
                    //not a number/percent/price/range/date for sure
                    String word = docText[textIterator];
                    if(StringUtils.equals(word, "")){
                        textIterator+=1;
                        continue;
                    }
                    if (this.stopWords.contains(StringUtils.lowerCase(word))) {
                        //a stop word, ignore it.
                        textIterator += 1;
                        continue;
                    }
                    if (this.wordStemming) {
                        char[] wordInChars = word.toCharArray();
                        this.stemmer.add(wordInChars, wordInChars.length);
                        this.stemmer.stem();
                        word = this.stemmer.toString();
                        this.stemmer.resetStemer();

                    } else {
                        Pattern regex = Pattern.compile("[^A-Za-z0-9]");
                        docText[textIterator] = word.replaceAll(  regex.toString(), "");
                    }
                    if (this.stopWords.contains(StringUtils.lowerCase(word))) {
                        //a stop word, ignore it.
                        textIterator += 1;

                    } else {
                        if(StringUtils.equals(word, "") || StringUtils.equals(word, ".")  || StringUtils.equals(word, "-") || StringUtils.equals(word, ",") || StringUtils.equals(word, "+")){
                            textIterator+=1;
                        }else{
                            termHashMapDataStructure.insert(word, termLocationIterator, 1);
                            termLocationIterator += 1;
                            textIterator += 1;
                        }
                    }
                }
            }catch(Exception e){
                    textIterator+=1;
            }
        }
//        System.out.println(String.format("time to parse %d", System.currentTimeMillis() - start));
        //adding it into the Document object.
        doc.setParsedTerms(termHashMapDataStructure);
        return doc;
    }

    /**
     * detects all meta data documents' data and parses it. returns a 1D 3 sized array of:
     * DOCNO - the documents' number
     * DATE1 - the documents' date
     * TI - the documents' header
     * @param fullDoc
     * @return
     */
    private String[] getDocData(String fullDoc){
    //DOCNO tag info
        int startIndex = StringUtils.indexOf(fullDoc, "<DOCNO>")+7;
        int endIndex = StringUtils.indexOf(fullDoc, "</DOCNO>");
        String docNo;
        String docDate;
        String docTI;
        if (endIndex<0 || startIndex-7 < 0){
            docNo = "";
        }else{
            docNo = StringUtils.substring(fullDoc, startIndex, endIndex).replace(" ", "");
        }

        //DATE1 tag info
        startIndex = StringUtils.indexOf(fullDoc, "<DATE1>")+7;
        endIndex = StringUtils.indexOf(fullDoc, "</DATE1>");
        if (endIndex<0 || startIndex-7 < 0){
            startIndex = StringUtils.indexOf(fullDoc, "<DATE>")+6;
            endIndex = StringUtils.indexOf(fullDoc, "</DATE>");
            if (endIndex<0 || startIndex-7 < 0){
                docDate = "";
            }else{
                docDate = StringUtils.substring(fullDoc, startIndex, endIndex).replace(" ", "");
            }
        }else{
            docDate = StringUtils.substring(fullDoc, startIndex, endIndex).replace(" ", "");
        }
        //DATE1 tag info
        startIndex = StringUtils.indexOf(fullDoc, "<TI>")+4;
        endIndex = StringUtils.indexOf(fullDoc, "</TI>");
        if (endIndex<0 || startIndex-7 < 0){
            docTI = "";
        }else{
            docTI = StringUtils.trim(StringUtils.substring(fullDoc,startIndex, endIndex));
        }
        String[] docData = new String[4];
        docData[0] = docNo;
        docData[1] = docDate;
        docData[2] = docTI;
        docData[3] = String.valueOf(fullDoc.length());
        return docData;
    }

    /**
     * checks if the first char of the string is a number
     * @param s
     * @return
     */
    private Boolean startsWithNum(String s){
        try{
            Double.parseDouble(StringUtils.substring(s,0,1));
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    /**
     *  receives a number and a boolean indicating whether this is a price term.
     *  acts according to the rules and concatenates the unit and measure accordingly.
     * @param s
     * @param regNumber
     * @return String
     */
    private String transformNumber(double s, boolean regNumber){
        StrBuilder sb = new StrBuilder();
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

//        s =  Double.parseDouble(df.format(s/Math.pow(10,12)));
        if (regNumber){
            double divisionByK = Double.parseDouble(df.format(s/Math.pow(10,3)));
            double divisionByM = Double.parseDouble(df.format(s/Math.pow(10,6)));
            double divisionByB = Double.parseDouble(df.format(s/Math.pow(10,9)));
            if (divisionByB>=1){
                sb.append(divisionByB);
                sb.append("B");
            }else if(divisionByM>=1){
                sb.append(divisionByM);
                sb.append("M");
            }else if(divisionByK>=1){
                sb.append(divisionByK);
                sb.append("K");
            }else{
                sb.append(Double.parseDouble(df.format(s)));
            }
        }else{
            double divisionByM = Double.parseDouble(df.format(s/Math.pow(10,6)));
            double divisionByB = Double.parseDouble(df.format(s/Math.pow(10,9)));
            if (divisionByB>=1){
                sb.append(divisionByB*Math.pow(10,3));
                sb.append(" M Dollars");
            }else if(divisionByM>=1){
                sb.append(divisionByM);
                sb.append(" M Dollars");
            }else{
                sb.append(Double.parseDouble(df.format(s)));
                sb.append(" Dollars");
            }
        }
        return sb.toString();
    }

    /**
     * returns -1 if not a number, returns the number otherwise.
     * @param s
     * @return
     */
    private double isANumber(String s){
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        double num;
        try{
            s = df.format(Double.parseDouble(s));
            num = Double.parseDouble(s);
        }catch(Exception e){
            num = -1;
        }
        return num;
    }

    /**
     * receives a string month, trims it to the first 3 letters (e.g. January=>jan, etc.)
     * matches it to the month's numerical presentation and returns it.
     * @param s - the months name (e.g. [January, JAN, Jan, jan] all applicable)
     * @return String
     */
    private String monthIntoNumber(String s){
        StrBuilder stringBuilder = new StrBuilder();
        s = StringUtils.substring(StringUtils.lowerCase(s), 0,3);
        switch (s){
            case "jan":{
                stringBuilder.append("01");
                break;
            }
            case "feb":{
                stringBuilder.append("02");
                break;
            }
            case "mar":{
                stringBuilder.append("03");
                break;
            }
            case "apr":{
                stringBuilder.append("04");
                break;
            }
            case "may":{
                stringBuilder.append("05");
                break;
            }
            case "jun":{
                stringBuilder.append("06");
                break;
            }
            case "jul":{
                stringBuilder.append("07");
                break;
            }
            case "aug":{
                stringBuilder.append("08");
                break;
            }
            case "sep":{
                stringBuilder.append("09");
                break;
            }
            case "oct":{
                stringBuilder.append("10");
                break;
            }
            case "nov":{
                stringBuilder.append("11");
                break;
            }
            case "dec":{
                stringBuilder.append("12");
                break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * transforms a days representing number to a string.
     * E.g.:
     * daysInDatesFormatter(15) => "15"
     * daysInDatesFormatter(5) => "05"
     * @param num
     * @return String
     */
    private String daysInDatesFormatter(double num){
        int intNum = (int) num;
        StrBuilder stringBuilder = new StrBuilder();
        if(intNum<10){
            stringBuilder.append("0");
        }
        stringBuilder.append(intNum);
        return stringBuilder.toString();
    }

    /**
     * removes from all strings in a given array (docText) "bad characters" that we know we have no
     * need for. returns the string array.
     * @param docText String[]
     * @return String[]
     */
    private String[] initialBadCharacterRemoval(String[] docText){
//        ArrayList
        int textIterator =0;
        while(textIterator<docText.length){
            docText[textIterator] = StringUtils.removeEnd(docText[textIterator], ".");
            docText[textIterator] = StringUtils.removeEnd(docText[textIterator], "?");
            docText[textIterator] = StringUtils.removeEnd(docText[textIterator], ",");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "[");
            docText[textIterator] = StringUtils.remove(docText[textIterator], " ");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "]");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "]");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "(");
            docText[textIterator] = StringUtils.remove(docText[textIterator], ")");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "*");
            docText[textIterator] = StringUtils.remove(docText[textIterator], ":");
            docText[textIterator] = StringUtils.remove(docText[textIterator], ";");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "<");
            docText[textIterator] = StringUtils.remove(docText[textIterator], ">");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "=");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "'");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "\"");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "/");
            docText[textIterator] = StringUtils.remove(docText[textIterator], "|");
            // if the word/number is comprised of ',' remove them.  THIS IS FOR NUMBERS ONLY
            if(StringUtils.isNumeric(docText[textIterator])&& StringUtils.isNumeric(docText[textIterator].replaceAll(",",""))){
                docText[textIterator] = docText[textIterator].replaceAll(",", "");
            }
            textIterator++;
        }
        return docText;
    }
}
