package sample.Model.Parser;

import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.Document;
import sample.Model.Number;
import sample.Model.Term;
import sample.Model.Word;
import sun.font.TrueTypeFont;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeMath.round;

public class DocParser implements IParser{
    private Boolean wordStemming;
    private HashSet<String> stopWords;
    private HashSet<String> months;
//    private Document doc;

    public DocParser(Boolean wordStemming, HashSet<String> stopWords, HashSet<String> months) {
        this.wordStemming = wordStemming;
        this.stopWords = stopWords;
        this.months = months;
        //todo: receive stemming boolean paramter
    }


    @Override
    public void Parse(String fullDoc) throws Exception {
        TermHashMapDataStructure termHashMapDataStructure = new TermHashMapDataStructure();
        if(this.wordStemming){
            //todo: fullDoc to stemmer
//            fullDoc = stemmer.stem(fullDoc);
        }
        String[] docData = getDocData(fullDoc);
        String treatedFullDoc =""; // NOT IN USE YET
        Document doc = new Document(docData[0], docData[1], docData[2]);
        //todo: remove unnecessary tags e.g. <F..>
        //todo: remove unnecessary chars : '[' , ']' ,'"', '?' '...'
        String[] docText = fullDoc.substring(fullDoc.indexOf("<TEXT>")+6, fullDoc.indexOf("</TEXT>")).split(" | \n ");


        int textIterator=0;
        int termLocation = 0;
        while(textIterator<docText.length){
            // ignore here from any invalid entry
            if (docText[textIterator].equals("")){
                textIterator+=1;
                continue;
            }
            //remove all ',' if exists at the end of the word
            if(docText[textIterator].endsWith(",")){
                docText[textIterator] = docText[textIterator].replaceAll(",", "");
            }// if the word/number is comprised of ',' remove them.  THIS IS FOR NUMBERS ONLY
            else if(isANumber(docText[textIterator])==-1 && isANumber(docText[textIterator].replaceAll(",",""))!=-1){
                docText[textIterator] = docText[textIterator].replaceAll(",", "");
            }

            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringNumberBuilder = new StringBuilder();
            ////////////////// RANGES ////////////////////
            if(docText[textIterator].contains("-") && docText[textIterator].split("-").length > 1){
                //term-term / word-word-word
                String[] seperated = docText[textIterator].split("-");
                if (seperated.length>=3){
                    if(isANumber(seperated[0]) == -1 && isANumber(seperated[1]) == -1 && isANumber(seperated[2]) == -1){
                        //word-word-word pass as single term
                        termHashMapDataStructure.insert(docText[textIterator], termLocation);
                        termLocation+=1;
                    }
                    else{
                        //not eligible for a range, split and pass as differnet terms
                        for (int i = 0; i < seperated.length; i++) {
                            termHashMapDataStructure.insert(seperated[i], termLocation);
                            termLocation+=1;
                        }
                    }
                    textIterator+=1;
                }else if(seperated.length==2){
                    // word-word number-word word-number number number
                    termHashMapDataStructure.insert(docText[textIterator],termLocation);
                    termLocation+=1;
                    if(isANumber(seperated[0]) != -1){
                        //number-number divide into two number and add them as terms
                        termHashMapDataStructure.insert(seperated[0],termLocation);
                        termLocation+=1;
                    }if (isANumber(seperated[1]) != -1){
                        termHashMapDataStructure.insert(seperated[1],termLocation);
                        termLocation+=1;
                    }
                    textIterator+=1;
                }
            }else if(textIterator+3 < docText.length &&
                    docText[textIterator].toLowerCase().equals("between") &&
                    docText[textIterator+2].toLowerCase().equals("and") &&
                    (isANumber(docText[textIterator+1]) != -1 && isANumber(docText[textIterator+3]) != -1)){
                    // BETWEEN value and value
                double firstNum = isANumber(docText[textIterator+1]);
                double secondNum = isANumber(docText[textIterator+3]);
                stringBuilder.append(firstNum);
                stringBuilder.append("-");
                stringBuilder.append(secondNum);
                termHashMapDataStructure.insert(stringBuilder.toString(), termLocation);
                termLocation+=1;
                textIterator+=4;
            }
            ////////////////// DATES ///////////////////// this is for the MM DD / MM YYYY format, NOT the DD MM format
            else if(this.months.contains(docText[textIterator].replaceAll("\\.|,",""))){
                //look for the DD or YYYY
                if(textIterator+1 < docText.length &&  isANumber(docText[textIterator+1]) != -1){
                    // found a full date
                    String monthRep = this.monthIntoNumber(docText[textIterator]);
                    double isNextANumber = isANumber(docText[textIterator+1]);
                    // check if its DD or YYYY
                    //assume years will be larger than days.
                    if(isNextANumber<=31){
                        //DD format
                        stringBuilder.append(monthRep);
                        //check if the number is below 10 to add 0
                        stringBuilder.append("-");
                        if(isNextANumber<10){
                            stringBuilder.append("0");
                            stringBuilder.append((int)(isNextANumber));
                        }else{
                            stringBuilder.append((int)isNextANumber);
                        }
                        textIterator+=2;
                    }else if(isNextANumber>=1000 && isNextANumber<=9999){ //todo ask roiki, this is very shady.
                        // YYYY format
                        stringBuilder.append((int)isNextANumber);
                        stringBuilder.append("-");
                        stringBuilder.append(monthRep);
                        textIterator+=2;
                    }else{
                        //the number has nothing to do with the month name, pass as a regular word
                        stringBuilder.append(docText[textIterator]);
                        textIterator+=1;
                    }
                }else{
                    //this is just the month, pass a regular word term
                    stringBuilder.append(docText[textIterator]);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringBuilder.toString(), termLocation);
                termLocation+=1;
            }

            /////////////////numbers, percentages and prices/////////////////
            ////DOLLAR NUMBERS/////
            else if(docText[textIterator].startsWith("$")){
                //remove the '$' sign
                String replace_with = docText[textIterator].replace("$","");
                docText[textIterator] = replace_with;
                //evaluate the number and transform if necessary
                if(textIterator+1<docText.length){
                    if(docText[textIterator+1].toLowerCase().equals("million")){
                        try{
//                            $price million
                            double num = Double.parseDouble(docText[textIterator])*Math.pow(10,6);
                            String toConcat = this.transformNumber(num, false);
                            stringNumberBuilder.append(toConcat);
                            textIterator+=2;
                        }catch (Exception e){
                            // wasn't a number
                            System.out.println(e);
                            textIterator+=1;
                        }

                    }else if(docText[textIterator+1].toLowerCase().equals("billion")){
                        try{
//                            $price billion
                            double num = Double.parseDouble(docText[textIterator])*Math.pow(10,9);
                            String toConcat = this.transformNumber(num, false);
                            stringNumberBuilder.append(toConcat);
                            textIterator+=2;
                        }catch (Exception e){
                            // wasn't a number
                            System.out.println(e);
                            textIterator+=1;
                        }
                    }else{
                        // $price
                        stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator]), false));
                        textIterator+=1;
                    }
                }else{
                    // $price [end of text]
                    stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator]), false));
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(docText[textIterator].contains("%")){
                //Number%
                stringNumberBuilder.append(docText[textIterator]);
                textIterator+=1;
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if((docText[textIterator].contains("bn") || docText[textIterator].contains("m"))&&startsWithNum(docText[textIterator])){
                // Number[m/bn]
                String replace_with="";
                double num=1;
                String toConcat="";
                //removing the units
                if (docText[textIterator].contains("bn")){
                    replace_with = docText[textIterator].replace("bn","");
                    num = Math.pow(10,9);
                }else{
                    replace_with = docText[textIterator].replace("m","");
                    num = Math.pow(10,6);
                }
                try{
                    // it's a number for sure
                    num = Double.parseDouble(replace_with)*num;
                    if(textIterator+1< docText.length && docText[textIterator+1].toLowerCase().equals("dollars")){
                        toConcat = this.transformNumber(num, false);
                        // price[m/bn] dollars
                        textIterator+=2;

                    }else{
                        // number[m/bn] dollars
                        toConcat = this.transformNumber(num, true);
                        textIterator+=1;
                    }
                    stringNumberBuilder.append(toConcat);
                }
                catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;

            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("percent") || docText[textIterator+1].toLowerCase().equals("percentage") )){
                // Number percent /percentage
                String replace_with="";
                double num=1;
                String toConcat="";
                try{
                    // it's a number for sure
                    num = Double.parseDouble(docText[textIterator])*num;
                    toConcat = this.transformNumber(num, true);
                    // price m/bn dollars
                    textIterator+=2;
                    stringNumberBuilder.append(toConcat);
                    stringNumberBuilder.append("%");
                }catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(textIterator+1 < docText.length &&
                    (docText[textIterator+1].contains("/") &&
                    startsWithNum(docText[textIterator+1])) &&
                    isANumber(docText[textIterator])!=-1){
                // Number fraction dollars
                stringNumberBuilder.append(isANumber(docText[textIterator]));
                stringNumberBuilder.append(" ");
                stringNumberBuilder.append(docText[textIterator+1]);
                if(textIterator+2 < docText.length && docText[textIterator+2].toLowerCase().equals("dollars") ) {
                    stringNumberBuilder.append(" Dollars");
                    textIterator+=3;
                }else{
                    textIterator+=2;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("thousand"))){
                // Number Thousand
                String replace_with="";
                double num=1000;
                String toConcat="";
                try{
                    // it's a number for sure
                    num = Double.parseDouble(docText[textIterator])*num;
                    toConcat = this.transformNumber(num, true);
                    // price m/bn dollars
                    textIterator+=2;
                    stringNumberBuilder.append(toConcat);
                }catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("dollars"))){
                // Number Dollars
                String replace_with="";
                double num=1;
                String toConcat="";
                try{
                    // it's a number for sure
                    num = Double.parseDouble(docText[textIterator])*num;
                    toConcat = this.transformNumber(num, false);
                    // price m/bn dollars
                    textIterator+=2;
                    stringNumberBuilder.append(toConcat);
                }catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("m") || docText[textIterator+1].toLowerCase().equals("bn") )){
                // Number m/bn
                String replace_with="";
                double num=1;
                String toConcat="";
                //removing the units
                if (docText[textIterator+1].contains("bn")){
                    num = Math.pow(10,9);
                }else{
                    num = Math.pow(10,6);
                }
                try{
                    // it's a number for sure
                    num = Double.parseDouble(docText[textIterator])*num;
                    if(docText[textIterator+2].toLowerCase().equals("dollars")){
                        toConcat = this.transformNumber(num, false);
                        // price m/bn dollars
                        textIterator+=3;

                    }else{
                        // number m/bn dollars
                        toConcat = this.transformNumber(num, true);
                        textIterator+=2;
                    }
                    stringNumberBuilder.append(toConcat);
                }
                catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("million") ||
                    docText[textIterator+1].toLowerCase().equals("billion") ||
                    docText[textIterator+1].toLowerCase().equals("trillion"))){
                //number million/billion
                String replace_with="";
                double num=1;
                String toConcat="";
                if (docText[textIterator+1].contains("trillion")){
                    num = Math.pow(10,12);
                }else if(docText[textIterator+1].contains("billion")){
                    num = Math.pow(10,9);
                }else{
                    num = Math.pow(10,6);
                }
                try{
                    // it's a number for sure
                    num = Double.parseDouble(docText[textIterator])*num;
                    if(textIterator+3 < docText.length && docText[textIterator+2].toLowerCase().equals("u.s.") && docText[textIterator+3].toLowerCase().equals("dollars")){
                        //price million/billion u.s. dollars
                        toConcat = this.transformNumber(num, false);
                        textIterator+=4;

                    }else{
                        // number million/billion
                        toConcat = this.transformNumber(num, true);
                        textIterator+=2;
                    }
                    stringNumberBuilder.append(toConcat);
                }
                catch (NumberFormatException e){
                    // NOT A NUMBER
                    System.out.println(e);
                    textIterator+=1;
                }
                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;
            }else if(isANumber(docText[textIterator])!=-1){
                //this is a number
//                String num = this.transformNumber(Double.parseDouble(docText[textIterator]), true);
                double num = isANumber(docText[textIterator]);
//                stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator]), true));
                // checks if there is a month name after it, meaning it is a date.
                if(textIterator+1 < docText.length &&
                        months.contains(docText[textIterator+1].replaceAll("\\.|,",""))){
                    //checks if it is a YYYY or a DD
                    if(num<=31){
                        // its DD
                        stringNumberBuilder.append(this.monthIntoNumber(docText[textIterator+1].replaceAll(", | . ", "")));
                        stringNumberBuilder.append("-");
                        stringNumberBuilder.append(this.daysInDatesFormatter(num));
                    }else if(num>=1000 && num<=9999){
                        // its YYYY
                        stringNumberBuilder.append(this.monthIntoNumber(docText[textIterator+1].replaceAll(", | . ", "")));
                        stringNumberBuilder.append("-");
                        stringNumberBuilder.append(num);
                    }
                    textIterator+=2;
                }else{
                    //here should be all the rest of the cases of just a number
                    stringNumberBuilder.append(num);
                    textIterator+=1;
                }

                termHashMapDataStructure.insert(stringNumberBuilder.toString(), termLocation);
                termLocation+=1;

            }else {
                //not a number/percent/price/range/date for sure
                //todo remove things here like special characters - the regex = .replaceAll("\\.|,","")
                Pattern regex = Pattern.compile("[^A-Za-z0-9]");
                String word = docText[textIterator].replaceAll(regex.toString(), "");
                termHashMapDataStructure.insert(word, termLocation);
                termLocation+=1;
                textIterator += 1;
            }
        }
//        System.out.println(fullDoc);
        // todo create a real Doc here - done, check with sahar.
        doc.setParsedTerms(termHashMapDataStructure);
        for (int i = 0; i < termHashMapDataStructure.termsEntries.size(); i++) {
            System.out.println(termHashMapDataStructure.termsEntries.get(i));
        }
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
        int startIndex = fullDoc.indexOf("<DOCNO>")+7;
        int endIndex = fullDoc.indexOf("</DOCNO>");
        String docNo = fullDoc.substring(startIndex, endIndex).replace(" ", "");
        //DATE1 tag info
        startIndex = fullDoc.indexOf("<DATE1>")+7;
        endIndex = fullDoc.indexOf("</DATE1>");
        String docDate = fullDoc.substring(startIndex, endIndex).replace(" ", "");
        //DATE1 tag info
        startIndex = fullDoc.indexOf("<TI>")+4;
        endIndex = fullDoc.indexOf("</TI>");
        String docTI = this.trimRedundantSpaces(fullDoc.substring(startIndex, endIndex));
        String[] docData = new String[3];
        docData[0] = docNo;
        docData[1] = docDate;
        docData[2] = docTI;
        return docData;
    }

    /**
     * if needed a private function that removes all the spaces leading to
     * a character and keeps the rest of the string
     * (including the spaces appearing after the first character).
     *
     * e.g. trimRedundantSpaces("   hello world") => "hello world"
     * @param s
     * @return
     */
    private String trimRedundantSpaces(String s){
        String trimmedString="";
        StringBuilder sB = new StringBuilder();
        Boolean stopTrim = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c!=' '){
                sB.append(c);
                stopTrim = true;
            }
            else{
                if(stopTrim){
                  sB.append(c) ;
                }
            }
        }
        trimmedString = sB.toString();
        return trimmedString;
    }

    /**
     * checks if the first char of the string is a number
     * @param s
     * @return
     */
    private Boolean startsWithNum(String s){
        try{
            Double.parseDouble(s.substring(0,1));
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
        StringBuilder sb = new StringBuilder();
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
        StringBuilder stringBuilder = new StringBuilder();
        s = s.toLowerCase().substring(0,3);
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
        StringBuilder stringBuilder = new StringBuilder();
        if(intNum<10){
            stringBuilder.append("0");
        }
        stringBuilder.append(intNum);
        return stringBuilder.toString();
    }
}
