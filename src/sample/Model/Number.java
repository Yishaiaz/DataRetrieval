package sample.Model;


public class Number extends Term {

    enum Unit {K, M, B}
    enum Mark { percent , Dollars};

    public Unit unit;
    public Mark mark;
    public Number(String content) {
        super(content);
    }

    public void prepareTerm() {

        content = content.replaceAll(",", "");

        // in case its price.
        if (content.contains("$")|| content.contains("Dollars") || content.contains("dollars") ){
            content=content.replace("$","");
            content=content.replaceAll("Dollars","");
            content=content.replaceAll("dollars","");
            content=content.replaceAll("U.S.","");
            mark=Mark.Dollars;
        }

        boolean hasUnitWord = false;
        // in case its number with unit .
        if (mark!=Mark.Dollars && (content.contains("Thousand")|| content.contains("thousand"))) {
            hasUnitWord = true;
            unit = Unit.K;
            content = content.replaceAll("Thousand", "");
            content = content.replaceAll("thousand", "");
        } else if (content.contains("Million") ||content.contains("million") || content.contains("m")) {
            hasUnitWord = true;
            unit = unit.M;
            content = content.replaceAll("Million", "");
            content = content.replaceAll("million", "");
            content = content.replaceAll("m", "");
        } else if (mark!=Mark.Dollars && (content.contains("Billion")|| content.contains("billion"))) {
            hasUnitWord = true;
            unit = unit.B;
            content = content.replaceAll("Billion", "");
            content = content.replaceAll("billion", "");

        }
            //in cast its '%'
        else if (content.contains("percent")|| content.contains("percentage") ||content.contains("%") ){
            content=content.replaceAll("percent","");
            content=content.replaceAll("percentage","");
            content=content.replaceAll("%","");
            mark = Mark.percent;
        }

        if (hasUnitWord)
            transformContentOfNumber();

        // in case its number without unit .
        if (!hasUnitWord && mark!= Mark.percent  && mark!=Mark.Dollars) {
            Double number = Double.valueOf(content);

            if (number > 1000 && number < 1000000)
                unit = Unit.K;
            else if (number > 1000000 && number < 1000000000)
                unit = Unit.M;
            else if (number > 1000000000)
                unit = Unit.B;
           transformContentOfNumber();
        }

        // follow price rules
        else if (mark==Mark.Dollars){
            if (content.contains("billion")|| content.contains("bn")){
                content=content.replaceAll("billion","");
                content=content.replaceAll("bn","");
                double value= Double.valueOf(content);
                value=value*1000000000;
                content=String.valueOf(value);
            }
            if (content.contains("trillion")) {
//                content=content.replaceAll("trillion","");
//                long value= Long.parseLong(content);
//                value= 1000000000000L *value;
//                content=String.valueOf(value);
            }

            if (content.contains("Thousand")|| content.contains("thousand")){
                content=content.replaceAll("Thousand","");
                content=content.replaceAll("thousand","");
                double value= Double.valueOf(content);
                value=value*1000;
                content=String.valueOf(value);

            }
            if (Double.valueOf(content)>=1000000)
                unit=Unit.M;

            transformContentOfNumber();
            }

        }



    public void transformContentOfNumber() {

        if (mark!= Mark.percent) {
            //dealing with shorten value

            Double value = Double.valueOf(content);
            // in case of K
            if (value >= 1000 && value < 1000000 && mark!=Mark.Dollars)
                value = value / 1000;
                //in case of M
            else if ((value >= 1000000 && value < 1000000000) /*|| mark==Mark.Dollars*/)
                value = value / 1000000;

                //in case of B
            else if (value > 1000000000){
              if (mark==Mark.Dollars)
                  value= value/1000000;
              else
                value = value / 1000000000;
            }


            // dealing with max 3 digit after "."
            if (value - Math.floor(value) == 0) {
                int intValue = value.intValue();
                content = String.valueOf(intValue);
            } else {
                content = String.valueOf(value);
                int idxDecimal = content.indexOf(".");
                if (content.length() - idxDecimal > 3)
                    content = content.substring(0, idxDecimal + 4);
                while (content.endsWith("0")) {
                    content = content.substring(0, content.length() - 1);
                    if (content.length() - 1 == idxDecimal) {
                        content = content.substring(0, content.length() - 1);
                        break;
                    }
                }

            }
        }
    }
}


