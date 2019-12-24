import java.util.List;
import java.util.ArrayList;

public class CompilationEngine {
    public String getTokenType(String token) {
        String[] tokenArr = token.split(">");
        return tokenArr[0].substring(1, tokenArr[0].length());
    }

    public String getTokenString(String token) {
        String[] tokenArr = token.split(">");
        return tokenArr[1].split("<")[0];
    }

    public Boolean isVarDec(String token) {
        return token.equals("static")
        || token.equals("field");
    }

    public Boolean isSubroutineDec(String token) {
        return token.equals("constructor")
        || token.equals("function")
        || token.equals("method");
    }

    public Boolean isStatementDec(String token) {
        return token.equals("let")
        || token.equals("if")
        || token.equals("else")
        || token.equals("while")
        || token.equals("do")
        || token.equals("return");
    }

    public int getNextSubroutineIndex(List<String> tokens, int beginIndex) {
        int counter = beginIndex;
        System.out.println(tokens);
        while(!isSubroutineDec(getTokenString(tokens.get(counter))) && counter < tokens.size() - 1) {
            counter++;
        }
        return counter;
    }

    public int getSubroutineBodyIndex(List<String> tokens, int beginIndex) {
        int counter = beginIndex;
        while(!getTokenString(tokens.get(counter)).equals("{") && counter < tokens.size()) {
            counter++;
        }
        return counter;
    }

    public int getVarDecIndex(List<String> tokens, int beginIndex) {
        int counter = beginIndex;
        while(!getTokenString(tokens.get(counter)).equals("var") && counter < tokens.size() - 1) {
            counter++;
        }
        return counter;
    }

    public int getParenIndex(List<String> tokens, int beginIndex) {
        int counter = beginIndex;
        while(!getTokenString(tokens.get(counter)).equals(")") && counter < tokens.size() - 1) {
            counter++;
        }
        return counter;
    }

    public int getStatementsIndex(List<String> tokens, int beginIndex, Boolean initial) {
        int counter = beginIndex;
        int blockCount = 0;
        int insideBlock = initial ? 1 : 0;
        String currentToken = getTokenString(tokens.get(counter));
        while(counter < tokens.size() && !(isStatementDec(currentToken) && blockCount == insideBlock)) {
            if(currentToken.equals("{")) {
                blockCount += 1;
            }
            if(currentToken.equals("}")) {
                blockCount -= 1;
            }
            // System.out.println("counter: " + counter);
            // System.out.println("currentToken: " + currentToken);
            // System.out.println("blockCount: " + blockCount);
            counter++;
            if(counter < tokens.size() - 1) {
                currentToken = getTokenString(tokens.get(counter));
            }
        }
        return counter;
    }

    public int getEqualsIndex(List<String> tokens, int beginIndex) {
        int counter = beginIndex;
        String currentToken = getTokenString(tokens.get(counter));
        while(!currentToken.equals("=") && counter < tokens.size() - 1) {
            counter++;
            currentToken = getTokenString(tokens.get(counter));
        }
        return counter;
    }

    public String compileVarDec(List<String> tokens) {
        String varDecString = "";
        int commaIndex = 3;
        if(!getTokenString(tokens.get(0)).equals("var")) {
            return "Syntax Error - Missing 'var' declaration\n";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "Syntax Error - Missing type\n";
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            return "Syntax Error - Missing var name\n";
        }

        varDecString = (
            "<varDec>\n" + 
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            tokens.get(2) + "\n"
        );

        while(getTokenString(tokens.get(commaIndex)).equals(",")) {
            varDecString += tokens.get(commaIndex) + "\n";
            if(!getTokenType(tokens.get(commaIndex + 1)).equals("identifier")) {
                return "Syntax Error - Missing var Name\n";
            } else {
                varDecString += tokens.get(commaIndex + 1) + "\n";
            }
            commaIndex += 2;
        }
        
        varDecString += tokens.get(tokens.size() - 1) + "\n";
        varDecString += "</varDec>\n";
        return varDecString;
    }

    public String compileExpression(List<String> tokens) {
        return "EXPRESSION\n";
    }

    public String compileLet(List<String> tokens) {
        String letString = "";
        int equalsIndex = getEqualsIndex(tokens, 0);
        if(!getTokenString(tokens.get(0)).equals("let")) {
            return "Syntax Error - Must have let declaration\n";
        } else if(!getTokenType(tokens.get(1)).equals("identifier")) {
            return "Syntax Error - Let statement missing name\n";
        }

        letString += (
            "<letStatement>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n"
        );

        if(!(getTokenString(tokens.get(2)).equals("[") || getTokenString(tokens.get(2)).equals("="))) {
            return "Syntax Error - Missing either [ or = ";
        }

        if(equalsIndex != 2) {
            letString += (
                tokens.get(2) + "\n" +
                compileExpression(tokens.subList(3, equalsIndex - 2)) +
                tokens.get(equalsIndex - 1) + "\n"
            );
        }

        letString += tokens.get(equalsIndex) + "\n";
        letString += compileExpression(tokens.subList(equalsIndex + 1, tokens.size() - 1));
        letString += tokens.get(tokens.size() - 1) + "\n";
        letString += "</letStatement>\n";

        return letString;
    }

    public String compileIf(List<String> tokens) {
        // String ifString = "";
        // int parenIndex = getParenIndex(tokens, 0);

        // if(!getTokenString(tokens.get(0)).equals("if")) {
        //     return "Syntax Error - Must have if declaration\n";
        // } else if(!getTokenString(tokens.get(1)).equals("(")) {
        //     return "Syntax Error - if statement missing symbol '('\n";
        // } else if(!getTokenString(tokens.get(parenIndex)).equals(")")) {
        //     return "Syntax Error - if statement missing symbol ')'\n";
        // } else if(!getTokenString(tokens.get(parenIndex + 1)).equals("{")) {
        //     return "Syntax Error - if statement missing symbol '{'"
        // }

        // ifString += (
        //     "<ifStatement>\n" +
        //     tokens.get(0) + "\n" +
        //     tokens.get(1) + "\n" +
        //     compileExpression(tokens.subList(2, parenIndex)) +
        //     tokens.get(parenIndex) + "\n" +
        //     tokens.get(parenIndex + 1) + "\n" +
        //     compileStatements(tokens.subList(parenIndex + 2, ))
        // );

        // ifString += tokens.get(tokens.size() - 1) + "\n";
        // ifString += "</ifStatement>\n";

        // return ifString;
        return "ifStatement";
    }

    public String compileWhile(List<String> tokens) {
        String whileString = "";
        int parenIndex = getParenIndex(tokens, 0);

        if(!getTokenString(tokens.get(0)).equals("while")) {
            return "Syntax Error - Must have while declaration\n";
        } else if(!getTokenString(tokens.get(1)).equals("(")) {
            return "Syntax Error - while statement missing symbol '('\n";
        } else if(!getTokenString(tokens.get(parenIndex)).equals(")")) {
            return "Syntax Error - while statement missing symbol ')'\n";
        } else if(!getTokenString(tokens.get(parenIndex + 1)).equals("{")) {
            return "Syntax Error - while statement missing symbol '{'\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals("}")) {
            return "Syntax Error - while statement missing symbol '}'\n";
        }

        whileString = (
            "<whileStatement>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            compileExpression(tokens.subList(2, parenIndex)) +
            tokens.get(parenIndex) + "\n" +
            tokens.get(parenIndex + 1) + "\n" +
            compileStatements(tokens.subList(parenIndex + 2, tokens.size() - 1)) +
            tokens.get(tokens.size() - 1) + "\n" +
            "</whileStatement>"
        );

        // System.out.println(whileString + "\n\n\n");
        return whileString;
    }

    public String compileDo(List<String> tokens) {
        String doString = "";
        int parenIndex = getParenIndex(tokens, 0);

        if(!getTokenString(tokens.get(0)).equals("do")) {
            return "Syntax Error - Must have do declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - while statement missing symbol ';'\n";
        }

        doString += (
            "<doStatement>\n" +
            tokens.get(0) + "\n" +
            "SUBROUTINECALL\n" +
            tokens.get(tokens.size() - 1) + "\n" +
            "</doStatement>\n"
        );
        
        // System.out.println(doString + "\n");
        return doString;
    }

    public String compileReturn(List<String> tokens) {
        String returnString = "";

        if(!getTokenString(tokens.get(0)).equals("return")) {
            return "Syntax Error - Must have return declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            System.out.println("return tokens: " + tokens);
            return "Syntax Error - return statement missing symbol ';'\n";
        }

        returnString += (
            "<return>\n" +
            tokens.get(0) + "\n"
        );

        if(tokens.size() > 2) {
            returnString += compileExpression(tokens.subList(1, tokens.size() - 1));
        }

        returnString += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</return>"
        );

        // System.out.println(returnString + "\n");
        return returnString;
    }

    public String compileStatements(List<String> tokens) {
        String compiledStatements = "<statements>\n";
        int statementsIndex = getStatementsIndex(tokens, 0, false);
        String currentStatementType = getTokenString(tokens.get(statementsIndex));
        int nextStatementsIndex;
        
        while(statementsIndex < tokens.size()) {
            nextStatementsIndex = getStatementsIndex(tokens, statementsIndex + 1, false);
            List<String> subList = tokens.subList(statementsIndex, nextStatementsIndex);
            if(currentStatementType.equals("let")) {
                compiledStatements += compileLet(subList);
            } else if(currentStatementType.equals("if")) {
                compiledStatements += compileIf(subList);
            } else if(currentStatementType.equals("while")) {
                compiledStatements += compileWhile(subList);
            } else if(currentStatementType.equals("do")) {
                compiledStatements += compileDo(subList);
            } else if(currentStatementType.equals("return")) {
                // System.out.println("statementsIndex: " + statementsIndex);
                // System.out.println("nextStatementsIndex: " + nextStatementsIndex);
                compiledStatements += compileReturn(subList);
            }
            statementsIndex = nextStatementsIndex;
            if(statementsIndex < tokens.size() - 1) {
                currentStatementType = getTokenString(tokens.get(statementsIndex));
            }
        }

        compiledStatements += "</statements>\n";
        return compiledStatements;
    }

    public String compileSubroutineDec(List<String> tokens) {
        int subroutineBodyIndex = getSubroutineBodyIndex(tokens, 0);
        int varDecIndex = getVarDecIndex(tokens, 0);
        int statementsIndex = getStatementsIndex(tokens, 0, true);
        int nextVarDecIndex;
        String compiledSubroutine = "";
        if(!isSubroutineDec(getTokenString(tokens.get(0)))) {
            return "Syntax Error - should be Subroutine declaration";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "Syntax Error - should be keyword or identifier";
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            return "Syntax Error - token should be identifier";
        }

        compiledSubroutine = (
            "<subroutineDec>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            tokens.get(2) + "\n" +
            tokens.get(3) + "\n" +
            "parameter list\n" + // compileParameterList(new ArrayList<String>(tokens.subList(4, subroutineBodyIndex - 1))) + "\n" + 
            tokens.get(subroutineBodyIndex - 1) + "\n" +
            "<subroutineBody>\n" +
            tokens.get(subroutineBodyIndex) + "\n"
        );

        while(varDecIndex < statementsIndex) {
            nextVarDecIndex = getVarDecIndex(tokens.subList(0, statementsIndex + 1), varDecIndex + 1);
            // System.out.println(varDecIndex);
            // System.out.println(nextVarDecIndex);
            compiledSubroutine += compileVarDec(tokens.subList(varDecIndex, nextVarDecIndex));
            varDecIndex = nextVarDecIndex;
        }

        compiledSubroutine += compileStatements(tokens.subList(statementsIndex, tokens.size() - 1));

        compiledSubroutine += tokens.get(tokens.size() - 1) + "\n";
        compiledSubroutine += "</subroutineBody>\n";
        compiledSubroutine += "</subroutineDec>\n";
        return compiledSubroutine;
    }

    // public String compileParameterList(List<String> tokens) {

    // }

    public String compileClass(List<String> tokens) {
        int subroutineBeginIndex = getNextSubroutineIndex(tokens, 0);
        int nextSubroutineIndex;
        String compiledClass = "";
        if(!tokens.get(0).equals("<keyword>class</keyword>")) {
            return "Syntax Error - No 'class' token";
        } else if(!getTokenType(tokens.get(1)).equals("identifier")) {
            return "Syntax Error - No class identifier";
        } else if(!getTokenType(tokens.get(2)).equals("symbol")) {
            return "Syntax Error - Missing token '{' on class assignment";
        } else if(!getTokenType(tokens.get(tokens.size() - 1)).equals("symbol")) {
            return "Syntax Error - Missing token '}' on class assignment";
        }

        compiledClass = (
            "<class>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            tokens.get(2) + "\n"
        );

        while(subroutineBeginIndex < tokens.size() - 1) {
            nextSubroutineIndex = getNextSubroutineIndex(tokens, subroutineBeginIndex + 1);
            // System.out.println(subroutineBeginIndex);
            // System.out.println(nextSubroutineIndex);
            compiledClass += compileSubroutineDec(tokens.subList(subroutineBeginIndex, nextSubroutineIndex));
            subroutineBeginIndex = nextSubroutineIndex;
        }

        compiledClass += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</class>"
        );

        // System.out.println(compiledClass);
        return compiledClass;
    }

    public List<String> compile(List<String> tokens) throws Exception {
        if(!tokens.get(0).equals("<tokens>") || !tokens.get(tokens.size() - 1).equals("</tokens>")) {
            System.out.println("Syntax Error - No 'tokens' token");
            return tokens;
        } else {
            String compiledTokenString = compileClass(new ArrayList<String>(tokens.subList(1, tokens.size() - 1)));
            // System.out.println("compiledTokenString: " + compiledTokenString);
            List<String> compiledTokens = new ArrayList<String>() {{
                compiledTokenString.split("\n");
            }};
            return compiledTokens;
        }
    }
}