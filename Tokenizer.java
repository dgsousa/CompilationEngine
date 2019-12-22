import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Tokenizer {
    List<String> tokens = new ArrayList<String>() {{
        add("<tokens>");
    }};

    HashMap<String, String> symbolMap = new HashMap<String, String>() {{
        put("{", "{");
        put("}", "}");
        put("(", "(");
        put(")", ")");
        put("[", "[");
        put("]", "]");
        put(".", ".");
        put(",", ",");
        put(";", ";");
        put("+", "+");
        put("-", "-");
        put("*", "*");
        put("/", "/");
        put("&", "&amp;");
        put("|", "|");
        put("\\", "\\");
        put("<", "&lt;");
        put(">", "&gt;");
        put("=", "=");
        put("~", "~");
    }};

    HashMap<String, Boolean> keywordMap = new HashMap<String, Boolean>() {{
        put("class", true);
        put("constructor", true);
        put("function", true);
        put("method", true);
        put("field", true);
        put("static", true);
        put("var", true);
        put("int", true);
        put("char", true);
        put("boolean", true);
        put("void", true);
        put("true", true);
        put("false", true);
        put("null", true);
        put("this", true);
        put("let", true);
        put("do", true);
        put("if", true);
        put("else", true);
        put("while", true);
        put("return", true);
    }}; 

    public Boolean isSymbol(String string) {
        return symbolMap.containsKey(string);
    }

    public Boolean isKeyword(String string) {
        return keywordMap.containsKey(string);
    }

    public void addStringToken(String token) {
        tokens.add("<stringConstant>" + token + "</stringConstant>");
    }

    public void addToken(String token) {
        if(token.chars().allMatch(Character::isDigit)) {
            tokens.add("<integerConstant>" + token + "</integerConstant>");
        } else if(isSymbol(token)) {
            tokens.add("<symbol>" + symbolMap.get(token) + "</symbol>");
        } else if(isKeyword(token)) {
            tokens.add("<keyword>" + token + "</keyword>");
        } else {
            tokens.add("<identifier>" + token + "</identifier>");
        }
    }

    public List<String> tokenizeLine(String line) {
        String current = "";
        String stringLit = "";
        int length = line.length();
        int counter = 0;
        while(counter < length) {
            char currChar = line.charAt(counter);
            if(currChar == '\"') {
                counter++;
                currChar = line.charAt(counter);
                while(currChar != '\"') {
                    current = current + currChar;
                    counter++;
                    currChar = line.charAt(counter);
                }
                addStringToken(current);
                current = "";
            } else if(currChar == ' ' || currChar == '\t') {
                if(current.length() > 0) {
                    addToken(current);
                }
                current = "";
            } else if(isSymbol("" + currChar)) {
                if(current.length() > 0) {
                    addToken(current);
                }
                addToken("" + currChar);
                current = "";
            } else {
                current = current + currChar;
            }
            counter++;
        }
        return Arrays.asList(current.split(" "));
    }

    public List<String> tokenize(List<String> contents) {
        contents
            .stream()
            .forEach(line -> tokenizeLine(line));
        
        tokens.add("</tokens>");
        return tokens;
    }
}