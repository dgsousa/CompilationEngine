package tokenutils;

import java.util.List;
import utils.Utils;

public class TokenUtils {
    public static String getTokenType(String token) {
        String[] tokenArr = token.split(">");
        return tokenArr[0].substring(1, tokenArr[0].length());
    }

    public static String getTokenString(String token) {
        String[] tokenArr = token.split(">");
        return tokenArr[1].split("<")[0];
    }

    public static int getTokenIndex(List<String> tokens, String tokenString) {
        int counter = 0;
        while(counter < tokens.size() && !getTokenString(tokens.get(counter)).equals(tokenString)) {
            counter++;
        }
        return counter;
    }

    public static int getTopLevelOpIndex(List<String> tokens) {
        int counter = 0;
        int parenCount = 0;
        int squareBracketCount = 0;
        while(counter < tokens.size() && !(Utils.isOp(getTokenString(tokens.get(counter))) && parenCount == 0 && squareBracketCount == 0)) {
            if(getTokenString(tokens.get(counter)).equals("(")) {
                parenCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals(")")) {
                parenCount -= 1;
            }
            if(getTokenString(tokens.get(counter)).equals("[")) {
                squareBracketCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals("]")) {
                squareBracketCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public static int getTopLevelParenIndex(List<String> tokens) {
        int counter = 0;
        int openParenCount = 0;
        while(counter < tokens.size() && !(getTokenString(tokens.get(counter)).equals(")") && openParenCount == 1)) {
            if(getTokenString(tokens.get(counter)).equals("(")) {
                openParenCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals(")")) {
                openParenCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public static int getTopLevelBracketIndex(List<String> tokens) {
        int counter = 0;
        int openBracketCount = 0;
        while(counter < tokens.size() && !(getTokenString(tokens.get(counter)).equals("}") && openBracketCount == 1)) {
            if(getTokenString(tokens.get(counter)).equals("{")) {
                openBracketCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals("}")) {
                openBracketCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public static int getTopLevelElseIndex(List<String> tokens) {
        int counter = 0;
        int openBracketCount = 0;
        while(counter < tokens.size() && !(getTokenString(tokens.get(counter)).equals("else") && openBracketCount == 0)) {
            if(getTokenString(tokens.get(counter)).equals("{")) {
                openBracketCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals("}")) {
                openBracketCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public static Boolean isClassVarDec(String token) {
        return token.equals("static")
        || token.equals("field");
    }

    public static Boolean isSubroutineDec(String token) {
        return token.equals("constructor")
        || token.equals("function")
        || token.equals("method");
    }

    public static Boolean isStatementDec(String token) {
        return token.equals("let")
        || token.equals("if")
        || token.equals("while")
        || token.equals("do")
        || token.equals("return");
    }

    public static int getClassVarDecIndex(List<String> tokens) {
        int counter = 0;
        while(counter < tokens.size() && !isClassVarDec(getTokenString(tokens.get(counter)))) {
            counter++;
        }
        return counter;
    }

    public static int getSubroutineIndex(List<String> tokens) {
        int counter = 0;
        while(counter < tokens.size() && !isSubroutineDec(getTokenString(tokens.get(counter)))) {
            counter++;
        }
        return counter;
    }

    public static int countNumArgs(List<String> tokens) {
        int topLevelCommaIndex;
        int numArgs = 0;

        if(tokens.size() > 0) {
            numArgs = 1;
            topLevelCommaIndex = getTopLevelCommaIndex(tokens);
            while(topLevelCommaIndex < tokens.size() && TokenUtils.getTokenString(tokens.get(topLevelCommaIndex)).equals(",")) {
                numArgs += 1;
                topLevelCommaIndex = getTopLevelCommaIndex(tokens.subList(topLevelCommaIndex + 1, tokens.size())) + topLevelCommaIndex + 1;
            }
        }
        return numArgs;
    }

    public static int getStatementsIndex(List<String> tokens) {
        int counter = 0;
        int blockCount = 0;
        String currentToken;
        while(counter < tokens.size() && !(TokenUtils.isStatementDec(TokenUtils.getTokenString(tokens.get(counter))) && blockCount == 0)) {
            currentToken = TokenUtils.getTokenString(tokens.get(counter));
            if(currentToken.equals("{")) {
                blockCount += 1;
            }
            if(currentToken.equals("}")) {
                blockCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public static int getTopLevelCommaIndex(List<String> tokens) {
        int counter = 0;
        int parenCounter = 0;
        while(counter < tokens.size() && !(TokenUtils.getTokenString(tokens.get(counter)).equals(",") && parenCounter == 0)) {
            if(TokenUtils.getTokenString(tokens.get(counter)).equals("(")) {
                parenCounter += 1;
            } else if(TokenUtils.getTokenString(tokens.get(counter)).equals(")")) {
                parenCounter -= 1;
            }
            counter++;
        }
        return counter;
    }
}