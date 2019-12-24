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

    public int getTokenIndex(List<String> tokens, String token) {
        int counter = 0;
        while(counter < tokens.size() && !getTokenString(tokens.get(counter)).equals(token)) {
            counter++;
        }
        return counter;
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

    public int getSubroutineIndex(List<String> tokens) {
        int counter = 0;
        while(counter < tokens.size() && !isSubroutineDec(getTokenString(tokens.get(counter)))) {
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
        while(counter < tokens.size() && !getTokenString(tokens.get(counter)).equals("var")) {
            counter++;
        }

        return counter;
    }

    public int getStatementsIndex(List<String> tokens) {
        int counter = 0;
        int blockCount = 0;
        String currentToken;
        while(counter < tokens.size() && !(isStatementDec(getTokenString(tokens.get(counter))) && blockCount == 0)) {
            currentToken = getTokenString(tokens.get(counter));
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

    public String compileVarDec(List<String> tokens) {
        String varDecString = "";
        int commaIndex = 3;
        if(!getTokenString(tokens.get(0)).equals("var")) {
            return "Syntax Error - Missing 'var' declaration\n";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "Syntax Error - Missing type\n";
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            return "Syntax Error - Missing var name\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - Missing terminal token ';'\n";
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
        int equalsIndex = getTokenIndex(tokens, "=");
        if(!getTokenString(tokens.get(0)).equals("let")) {
            return "Syntax Error - Must have let declaration\n";
        } else if(!getTokenType(tokens.get(1)).equals("identifier")) {
            return "Syntax Error - Let statement missing name\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - Let statement missing terminal declaration ';'\n";
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
        int parenIndex = getTokenIndex(tokens, ")");

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

        return whileString;
    }

    public String compileDo(List<String> tokens) {
        String doString = "";
        int parenIndex = getTokenIndex(tokens, ")");

        if(!getTokenString(tokens.get(0)).equals("do")) {
            return "Syntax Error - Must have do declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - while statement missing symbol ';'\n";
        }

        doString += (
            "<doStatement>\n" +
            tokens.get(0) + "\n" +
            compileSubroutineCall(tokens.subList(1, tokens.size() - 1)) +
            tokens.get(tokens.size() - 1) + "\n" +
            "</doStatement>\n"
        );
        
        return doString;
    }

    public String compileReturn(List<String> tokens) {
        String returnString = "";

        if(!getTokenString(tokens.get(0)).equals("return")) {
            return "Syntax Error - Must have return declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
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
            "</return>\n"
        );

        return returnString;
    }

    public String compileStatements(List<String> tokens) {
        String compiledStatements = "<statements>\n";
        int statementsIndex = getStatementsIndex(tokens);
        String currentStatementType;
        int nextStatementsIndex;
        
        while(statementsIndex < tokens.size() && isStatementDec(getTokenString(tokens.get(statementsIndex)))) {
            currentStatementType = getTokenString(tokens.get(statementsIndex));
            nextStatementsIndex = getStatementsIndex(tokens.subList(statementsIndex + 1, tokens.size())) + statementsIndex + 1;
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
                compiledStatements += compileReturn(subList);
            }
            statementsIndex = nextStatementsIndex;
        }

        compiledStatements += "</statements>\n";
        return compiledStatements;
    }

    public String compileSubroutineDec(List<String> tokens) {
        int subroutineBodyIndex = getTokenIndex(tokens, "{");
        int varDecIndex = subroutineBodyIndex + 1;
        int statementsIndex = subroutineBodyIndex + 1;
        int endVarDecIndex;
        String compiledSubroutine;

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

        if(getTokenString(tokens.get(varDecIndex)).equals("var")) {
            while(varDecIndex < tokens.size() && getTokenString(tokens.get(varDecIndex)).equals("var")) {
                endVarDecIndex = getTokenIndex(tokens.subList(varDecIndex, tokens.size()), ";") + varDecIndex;
                compiledSubroutine += compileVarDec(tokens.subList(varDecIndex, endVarDecIndex + 1));
                varDecIndex = endVarDecIndex + 1;
            }
            statementsIndex = varDecIndex;
        }
        

        compiledSubroutine += compileStatements(tokens.subList(statementsIndex, tokens.size() - 1)); // clip off terminal '}'

        compiledSubroutine += tokens.get(tokens.size() - 1) + "\n";
        compiledSubroutine += "</subroutineBody>\n";
        compiledSubroutine += "</subroutineDec>\n";
        return compiledSubroutine;
    }

    // public String compileParameterList(List<String> tokens) {

    // }

    public String compileClass(List<String> tokens) {
        int lastSubroutineIndex = getSubroutineIndex(tokens);
        int nextSubroutineIndex;
        String compiledClass;
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

        while(lastSubroutineIndex < tokens.size() && isSubroutineDec(getTokenString(tokens.get(lastSubroutineIndex)))) {
            nextSubroutineIndex = getSubroutineIndex(tokens.subList(lastSubroutineIndex + 1, tokens.size() - 1)) + lastSubroutineIndex + 1;
            compiledClass += compileSubroutineDec(tokens.subList(lastSubroutineIndex, nextSubroutineIndex));
            lastSubroutineIndex = nextSubroutineIndex;
        }

        compiledClass += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</class>"
        );

        System.out.println(compiledClass);
        return compiledClass;
    }

    public List<String> compile(List<String> tokens) throws Exception {
        if(!tokens.get(0).equals("<tokens>") || !tokens.get(tokens.size() - 1).equals("</tokens>")) {
            System.out.println("Syntax Error - No 'tokens' token");
            return new ArrayList<String>();
        } else {
            String compiledTokenString = compileClass(new ArrayList<String>(tokens.subList(1, tokens.size() - 1)));
            List<String> compiledTokens = new ArrayList<String>() {{
                compiledTokenString.split("\n");
            }};
            return compiledTokens;
        }
    }
}