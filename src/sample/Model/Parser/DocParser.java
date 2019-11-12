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
    }


    @Override
    public void Parse(String fullDoc) throws Exception {
//        HashMap<String, TermHashMapEntry> docTermsMap = new HashMap<>();
        String[] docData = getDocData(fullDoc);
        Document doc = new Document(docData[0], docData[1], docData[2]);
        //todo: remove unnecessary tags e.g. <F..>
        //todo: remove unnecessary chars : '[' , ']' ,'"', '?' '...'
        String[] docText = fullDoc.substring(fullDoc.indexOf("<TEXT>")+6, fullDoc.indexOf("</TEXT>")).split(" | \n");
        ArrayList<String> terms= new ArrayList<>();

        int textIterator=0;
        while(textIterator<docText.length){
            // ignore here from any invalid entry
            if (docText[textIterator].equals("")){
                textIterator++;
                continue;
            }
            //remove all ',' if exists at the end of the word
            if(docText[textIterator].endsWith(",")){
                docText[textIterator] = docText[textIterator].replaceAll(",", "");
            }

            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringNumberBuilder = new StringBuilder();

            ////////////////// DATES ///////////////////// this is for the MM DD / MM YYYY format, NOT the DD MM format
            if(this.months.contains(docText[textIterator])){
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
                terms.add(stringBuilder.toString());
            }
            // if the word/number is comprised of ',' remove them.  THIS IS FOR NUMBERS ONLY
            else if(docText[textIterator].contains(",")){
                docText[textIterator] = docText[textIterator].replaceAll(",", "");
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
                terms.add(stringNumberBuilder.toString());
            }else if(docText[textIterator].contains("%")){
                //Number%
                stringNumberBuilder.append(docText[textIterator]);
                textIterator+=1;
                terms.add(stringNumberBuilder.toString());
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
                terms.add(stringNumberBuilder.toString());

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
                terms.add(stringNumberBuilder.toString());
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].contains("/") && startsWithNum(docText[textIterator+1])) &&
                    textIterator+2 < docText.length && docText[textIterator+2].toLowerCase().equals("dollars") && isANumber(docText[textIterator])!=-1){
                // Number fraction dollars
                stringNumberBuilder.append(isANumber(docText[textIterator]));
                stringNumberBuilder.append(" ");
                stringNumberBuilder.append(docText[textIterator+1]);
                stringNumberBuilder.append(" Dollars");
                textIterator+=3;
                terms.add(stringNumberBuilder.toString());
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
                terms.add(stringNumberBuilder.toString());
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
                terms.add(stringNumberBuilder.toString());
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
                terms.add(stringNumberBuilder.toString());
            }else if(textIterator+1 < docText.length && (docText[textIterator+1].toLowerCase().equals("million") || docText[textIterator+1].toLowerCase().equals("billion"))){
                //number million/billion
                String replace_with="";
                double num=1;
                String toConcat="";
                if (docText[textIterator+1].contains("billion")){
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
                terms.add(stringNumberBuilder.toString());
            }else if(isANumber(docText[textIterator])!=-1){
                //this is a number
//                String num = this.transformNumber(Double.parseDouble(docText[textIterator]), true);
                double num = isANumber(docText[textIterator]);
//                stringNumberBuilder.append(this.transformNumber(Double.parseDouble(docText[textIterator]), true));
                // checks if there is a month name after it, meaning it is a date.
                if(textIterator+1 < docText.length && months.contains(docText[textIterator+1])){
                    //checks if it is a YYYY or a DD
                    if(num<=31){
                        // its DD
                        stringNumberBuilder.append(this.monthIntoNumber(docText[textIterator+1]));
                        stringNumberBuilder.append("-");
                        stringNumberBuilder.append(this.daysInDatesFormatter(num));
                    }else if(num>=1000 && num<=9999){
                        // its YYYY
                        stringNumberBuilder.append(docText[textIterator+1]);
                        stringNumberBuilder.append("-");
                        stringNumberBuilder.append(num);
                    }
                    textIterator+=2;
                }else{
                    //here should be all the rest of the cases of just a number
                    textIterator+=1;
                }

                terms.add(stringNumberBuilder.toString());

            }else{
                //not a number/percent/price for sure

                textIterator+=1;
            }
        }
//        System.out.println(fullDoc);
        // todo create a real Doc here
        for (int i = 0; i < terms.size(); i++) {
            System.out.println(terms.get(i));
        }
    }

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
    private double trimRegularToUnit(double num, String unit){
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        switch (unit){
            case "M":
            {
                return Double.parseDouble(df.format(num/Math.pow(10,6)));
            }
            case "B":
            {
                return Double.parseDouble(df.format(num/Math.pow(10,9)));
            }
            case "T":
            {
                return Double.parseDouble(df.format(num/Math.pow(10,12)));
            }

        }
        return 2;

    }
    private Boolean startsWithNum(String s){
        try{
            Double.parseDouble(s.substring(0,1));
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

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
