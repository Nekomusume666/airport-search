import javax.tools.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

class main {

    //Мейн
    public static void main(String[] args) throws IOException {
        functionCaller();
    }

    //Выборка
    public  static ArrayList<String> selection;

    //Ввод фильтра или завершение программы
    public static void functionCaller() throws IOException {
        selection = new ArrayList<>();

        Scanner scan = new Scanner(System.in);
        System.out.println("Введите фильтр (!quit - выход)\n");
        String filter = scan.nextLine();
        if (filter.equals("!quit")) System.exit(0);
//        scan.close();
        if (filter.trim().equals("")){
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/airports.csv"));

            String record;

            while ((record = reader.readLine()) != null) {
                selection.add(record);
            }
            //вызов поиска по буквам
            search();
        }else {

            String[] subFilter;
            //работа над фильтром
            subFilter = delimeterAnalyser(filter);
            try {
                readStrings(filter, subFilter);
            } catch (IOException e) {
                System.out.println("ошибка 1");
                functionCaller();
            }

            //вызов поиска по буквам
            search();
        }
        functionCaller();
    }

    ///

    public static void search(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите начало слова(!quit - выход)\n");
        String word = scan.nextLine();
        if (!word.equals("!quit")) {
            int len = word.length();
            word = word.toLowerCase();
            int count = 0;

//            if (word.trim().equals("")){
//                System.out.println("Количество найденных строк: " + count);
//                System.out.println("Время, затраченное на поиск: " + 0 + " мс");
//                search();
//            }


            long start = System.currentTimeMillis();
            for (String record :
                    selection) {
                String[] subRec = record.split(",");
//            System.out.println(record);
//            System.out.println(subRec[1]);
                if (subRec[1].length() < len + 1) continue;

                String subStr = subRec[1].substring(1, len + 1).toLowerCase();
                if (subStr.equals(word)) {
                    count++;
                    System.out.println(subRec[1] + "[" + record + "]");
                }
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            System.out.println("Количество найденных строк: " + count);
            System.out.println("Время, затраченное на поиск: " + time + " мс");
            search();
        } else return;
    }

    ///


    //Разделение фильтра на простые состовляющие по логическим знакам. Простая состовляющая: column[1]>10
    public static String[] delimeterAnalyser(String filter){
        String[] subFilter;
        subFilter = filter.split("[&]{1,2}|[|]{1,2}|[,]|[\\\\(]|[\\\\)]");
//        System.out.println(Arrays.toString(subFilter));
        return subFilter;
    }


    //Открытие файла и считывание одной строки
    public static void readStrings(String filter, String[] subFilter) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/java/airports.csv"));

        String record;

        while ((record = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(record, ",");
            int index = 0;
            String[] tokens = new String[tokenizer.countTokens()];
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                tokens[index] = token;
                index++;
                // добавление токенов про одну запись
            }
            // обработка токена

            ArrayList<Boolean> checked = checking(subFilter, tokens);
            System.out.println(Arrays.toString(checked.toArray()));
            String boolString = replaceWithBooleans(filter, checked);
            System.out.println(boolString);

            boolean result = evaluateBooleanExpression(boolString);
            System.out.println(result);
            if (result){
                selection.add(record);
            }


            //if какой то

//            System.out.println(Arrays.toString(tokens));
            //след запись
        }
        reader.close();

    }

    //Проверка столбцов из записи на соответствие фильтру. На выходе получаем массив true, false, false и тд
    public static ArrayList<Boolean> checking(String[] subFilter, String[] tokens) throws IOException {
        ArrayList<Boolean> booleans = new ArrayList<>();

        for (int i = 0; i < subFilter.length; i++) {

            String[] parts = splitCondition(subFilter[i]);
//            System.out.println(Arrays.toString(parts));
            if (parts[1].equals("error")) continue;
            switch (parts[1]){
                case "<":
                    if (isNumeric(parts[2])) {
                        if (Integer.parseInt(tokens[findIndex(parts[0])]) < Integer.parseInt(parts[2].trim()))
                            booleans.add(true);
                        else booleans.add(false);
                    }
                    else{
                        System.out.println("Неправильно введен фильтр, попробуйте снова");
                        functionCaller();
                    }
                    break;
                case ">":
                    if (isNumeric(parts[2])) {
                        if (Integer.parseInt(tokens[findIndex(parts[0])]) > Integer.parseInt(parts[2].trim())) booleans.add(true);
                        else booleans.add(false);
                    }
                    else{
                        System.out.println("Неправильно введен фильтр, попробуйте снова");
                        functionCaller();
                    }
                    break;
                case "=":
                    if (isNumeric(parts[2])){
                        if (Integer.parseInt(tokens[findIndex(parts[0])]) == Integer.parseInt(parts[2].trim())) booleans.add(true);
                        else booleans.add(false);
                    }
                    else {
                        if (tokens[findIndex(parts[0])].equals(parts[2])) booleans.add(true);
                        else booleans.add(false);
                    }
                    break;
                case "<>":
                    if (isNumeric(parts[2])){
                        if (Integer.parseInt(tokens[findIndex(parts[0])]) != Integer.parseInt(parts[2].trim())) booleans.add(true);
                        else booleans.add(false);
                    }
                    else {
                        if (!tokens[findIndex(parts[0])].equals(parts[2])) booleans.add(true);
                        else booleans.add(false);
                    }
                    break;
            }
        }
        return booleans;
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    ///Разделение простой состовляющей фильтра на столбец, знак и значение
    public static String[] splitCondition(String expression) throws IOException {
        String[] error = new  String[5];


        error[1] = "error";
        if (!expression.equals(" ")) {

            String[] parts = expression.split("[<>|=]+");
//                parts[1] = parts[1].substring(1, parts[1].length() - 1);
            String[] result = new String[3];
            try {
                result[0] = parts[0];
                result[1] = expression.substring(parts[0].length(), expression.length() - parts[1].length());
                result[2] = parts[1];


            } catch (ArrayIndexOutOfBoundsException e){
                System.out.println("Вы неправильно ввели фильтр. Пример правильного фильтра: column[1]>10&column[5]=’GKA’");
                functionCaller();

            }
            return result;
        }else return  error;
    }

    //Поиск индекса, участвует в checking
    public static int findIndex(String part) throws IOException {
        int i = 0;
        part = part.trim();
        try {
            i = Integer.parseInt(part.substring(7,part.length() - 1));
        } catch (NumberFormatException e){
            System.out.println("Неправильно введен фильтр, попробуйте снова");
            functionCaller();
        }

        if (i < 1 || i > 14){
            System.out.println("Столбца с таким номером не существует, попробуйте снова");
            functionCaller();
        }

        return i - 1;
    }

    public static String replaceWithBooleans(String inputString, ArrayList<Boolean> checked) {
        Object[] booleanArray = checked.toArray();
        String[] splitString = inputString.split("(?=[&|()])|(?<=[&|()])");
        StringBuilder outputString = new StringBuilder();
        int booleanIndex = 0;
        for (String s : splitString) {
            if (s.equals("&") || s.equals("|") || s.equals("(") || s.equals(")")) {
                outputString.append(s);
                outputString.append(" ");
            } else {
                if (!s.equals(" ")) {
                    outputString.append(booleanArray[booleanIndex]);
                    outputString.append(" ");
                    booleanIndex++;
                }
            }
        }
        return outputString.toString();
    }

    ///

    public static boolean evaluateBooleanExpression(String input) {
        Stack<Boolean> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();
        String[] tokens = input.split(" ");
        for (String token : tokens) {
            if (isBoolean(token)) {
                operands.push(Boolean.parseBoolean(token));
            } else if (token.equals("(")) {
                operators.push(token.charAt(0));
            } else if (token.equals(")")) {
                while (operators.peek() != '(') {
                    char topOperator = operators.pop();
                    boolean operand2 = operands.pop();
                    boolean operand1 = operands.pop();
                    boolean result = performOperation(topOperator, operand1, operand2);
                    operands.push(result);
                }
                operators.pop();
            } else {
                char operator = token.charAt(0);
                while (!operators.isEmpty() && hasHigherPrecedence(operators.peek(), operator)) {
                    char topOperator = operators.pop();
                    boolean operand2 = operands.pop();
                    boolean operand1 = operands.pop();
                    boolean result = performOperation(topOperator, operand1, operand2);
                    operands.push(result);
                }
                operators.push(operator);
            }
        }
        while (!operators.isEmpty()) {
            char topOperator = operators.pop();
            boolean operand2 = operands.pop();
            boolean operand1 = operands.pop();
            boolean result = performOperation(topOperator, operand1, operand2);
            operands.push(result);
        }
        return operands.pop();
    }

    private static boolean isBoolean(String token) {
        return token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false");
    }

    private static boolean hasHigherPrecedence(char operator1, char operator2) {
        if (operator1 == '(' || operator1 == ')') {
            return false;
        } else if (operator1 == '!' && operator2 != '!') {
            return true;
        } else if ((operator1 == '&' || operator1 == '|') && operator2 == operator1) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean performOperation(char operator, boolean operand1, boolean operand2) {
        switch (operator) {
            case '!':
                return !operand2;
            case '&':
                return operand1 && operand2;
            case '|':
                return operand1 || operand2;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }


}