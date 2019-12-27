import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CompilationEngine {
    SymbolTable symbolTable = new SymbolTable();
    

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

    public Boolean isUnaryOp(String token) {
        return (
            token.equals("-")
            || token.equals("~")
        );
    }

    public Boolean isOp(String token) {
        return (
            token.equals("+")
            || token.equals("-")
            || token.equals("*")
            || token.equals("/")
            || token.equals("&amp;")
            || token.equals("|")
            || token.equals("&lt;")
            || token.equals("&gt;")
            || token.equals("=")
            || token.equals("~")
        );
    }

    public int getTopLevelOpIndex(List<String> tokens) {
        int counter = 0;
        int parenCount = 0;
        while(counter < tokens.size() && !(isOp(getTokenString(tokens.get(counter))) && parenCount == 0)) {
            if(getTokenString(tokens.get(counter)).equals("(")) {
                parenCount += 1;
            }
            if(getTokenString(tokens.get(counter)).equals(")")) {
                parenCount -= 1;
            }
            counter++;
        }
        return counter;
    }

    public int getTopLevelParenIndex(List<String> tokens) {
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

    public Boolean isClassVarDec(String token) {
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
        || token.equals("while")
        || token.equals("do")
        || token.equals("return");
    }

    public int getClassVarDecIndex(List<String> tokens) {
        int counter = 0;
        while(counter < tokens.size() && !isClassVarDec(getTokenString(tokens.get(counter)))) {
            counter++;
        }
        return counter;
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
        String name;
        String kind;
        String type;
        int commaIndex;
        if(!getTokenString(tokens.get(0)).equals("var")) {
            return "Syntax Error - Missing 'var' declaration\n";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "Syntax Error - Missing type\n";
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            return "Syntax Error - Missing var name\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - Missing terminal token ';'\n";
        }

        commaIndex = getTokenIndex(tokens, ",");

        kind = getTokenString(tokens.get(0));
        type = getTokenString(tokens.get(1));
        name = getTokenString(tokens.get(2));
        symbolTable.define(name, type, kind);

        varDecString = (
            "<varDec>\n" + 
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n"
        );

        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            varDecString += tokens.get(commaIndex) + "\n";
            varDecString += "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n";
            commaIndex += 2;
        }
        
        varDecString += tokens.get(tokens.size() - 1) + "\n";
        varDecString += "</varDec>\n";
        return varDecString;
    }

    public String compileSubroutineCall(List<String> tokens) {
        if(getTokenType(tokens.get(0)).equals("identifier") && getTokenString(tokens.get(1)).equals(".") ) {
            return (
                tokens.get(0) + "\n" +
                tokens.get(1) + "\n" +
                compileSubroutineCall(tokens.subList(2, tokens.size()))
            );
        }

        if(getTokenType(tokens.get(0)).equals("identifier")
            && getTokenString(tokens.get(1)).equals("(")
            && getTokenString(tokens.get(tokens.size() - 1)).equals(")")) {
            return (
                tokens.get(0) + "\n" +
                tokens.get(1) + "\n" +
                compileExpressionList(tokens.subList(2, tokens.size() - 1)) +
                tokens.get(tokens.size() - 1) + "\n"
            );
        }
        return "Invalid Format - cannot parse subroutineCall\n";
    }

    public int getTopLevelCommaIndex(List<String> tokens) {
        int counter = 0;
        int parenCounter = 0;
        while(counter < tokens.size() && !(getTokenString(tokens.get(counter)).equals(",") && parenCounter == 0)) {
            if(getTokenString(tokens.get(counter)).equals("(")) {
                parenCounter += 1;
            } else if(getTokenString(tokens.get(counter)).equals(")")) {
                parenCounter -= 1;
            }
            counter++;
        }
        return counter;
    }

    public String compileExpressionList(List<String> tokens) {
        String expressionListString;
        int topLevelCommaIndex;
        int nextTopLevelCommaIndex;
        
        expressionListString = "<expressionList>\n";

        if(tokens.size() > 0) {
            topLevelCommaIndex = getTopLevelCommaIndex(tokens);

            expressionListString += (
                compileExpression(tokens.subList(0, topLevelCommaIndex))
            );

            while(topLevelCommaIndex < tokens.size() && getTokenString(tokens.get(topLevelCommaIndex)).equals(",")) {
                nextTopLevelCommaIndex = getTopLevelCommaIndex(tokens.subList(topLevelCommaIndex + 1, tokens.size())) + topLevelCommaIndex + 1;
                expressionListString += (
                    tokens.get(topLevelCommaIndex) + "\n" +
                    compileExpression(tokens.subList(topLevelCommaIndex + 1, nextTopLevelCommaIndex))
                );
                topLevelCommaIndex = nextTopLevelCommaIndex;
            }
        }

        expressionListString += "</expressionList>\n";
        return expressionListString;
    }

    public String compileExpression(List<String> tokens) {
        String expressionString = "";
        int opIndex = getTopLevelOpIndex(tokens.subList(0, tokens.size()));
        int nextOpIndex;

        if(opIndex == 0 && isUnaryOp(getTokenString(tokens.get(opIndex)))) {
            return (
                "<expression>\n" +
                compileTerm(tokens) +
                "</expression>\n"
            );
        }

        expressionString = "<expression>\n";

        if(opIndex > 0) {
            expressionString += compileTerm(tokens.subList(0, opIndex));
        }

        while(opIndex < tokens.size() && isOp(getTokenString(tokens.get(opIndex)))) {
            nextOpIndex = getTopLevelOpIndex(tokens.subList(opIndex + 1, tokens.size())) + opIndex + 1;
            expressionString += (
                tokens.get(opIndex) + "\n" +
                compileTerm(tokens.subList(opIndex + 1, nextOpIndex))
            );
            opIndex = nextOpIndex;
        }

        expressionString += (
            "</expression>\n"
        );

        return expressionString;
    }

    public String compileTerm(List<String> tokens) {
        if(tokens.size() == 0) {
            return "<term>\n</term>\n";
        }
        String firstTokenType = getTokenType(tokens.get(0));
        // intergerConstant | stringConstant | keywordConstant || varName
        if(tokens.size() == 1) {
            if(!(firstTokenType.equals("integerConstant")
                || firstTokenType.equals("stringConstant")
                || firstTokenType.equals("keyword")
                || firstTokenType.equals("identifier"))) {
                return "Invalid Format - Wrong Token Type\n" + tokens + "\n";
            }
            return (
                "<term>\n" +
                tokens.get(0) + "\n" +
                "</term>\n"
            );  
        }

        // varName [ expression ]
        if(getTokenType(tokens.get(0)).equals("identifier")
            && getTokenString(tokens.get(1)).equals("[")
            && getTokenString(tokens.get(tokens.size() - 1)).equals("]")) {
            return (
                "<term>\n" +
                tokens.get(0) + "\n" +
                tokens.get(1) + "\n" +
                compileExpression(tokens.subList(2, tokens.size() - 1)) +
                tokens.get(tokens.size() - 1) + "\n" +
                "</term>\n" 
            );
        }

        // subRoutineCall
        if(getTokenType(tokens.get(0)).equals("identifier")
            && (getTokenString(tokens.get(1)).equals("(") || getTokenString(tokens.get(1)).equals("."))) {
            return (
                "<term>\n" +
                compileSubroutineCall(tokens) +
                "</term>\n" 
            );
        }

        // ( expression )
        if(getTokenString(tokens.get(0)).equals("(") && getTokenString(tokens.get(tokens.size() - 1)).equals(")")) {
            return (
                "<term>\n" +
                tokens.get(0) + "\n" +
                compileExpression(tokens.subList(1, tokens.size() - 1)) +
                tokens.get(tokens.size() - 1) + "\n" +
                "</term>\n"
            );
        }

        // unaryOp term
        if(isOp(getTokenString(tokens.get(0)))) {
            return (
                "<term>\n" +
                tokens.get(0) + "\n" +
                compileTerm(tokens.subList(1, tokens.size())) + // only clip off the first token
                "</term>\n"
            );
        }
        
        return "Invalid Format - Tokens do not correspond to valid term\n" + tokens + "\n";
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
                compileExpression(tokens.subList(3, equalsIndex - 1)) +
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
        String ifString = "";
        int parenIndex = getTopLevelParenIndex(tokens);
        int ifBracketIndex = getTokenIndex(tokens, "}");
        int elseIndex = getTokenIndex(tokens, "else");
        
        if(!getTokenString(tokens.get(0)).equals("if")) {
            return "Syntax Error - Must have if declaration\n";
        } else if(!getTokenString(tokens.get(1)).equals("(")) {
            return "Syntax Error - if statement missing symbol '('\n";
        } else if(!getTokenString(tokens.get(parenIndex)).equals(")")) {
            return "Syntax Error - if statement missing symbol ')'\n";
        } else if(!getTokenString(tokens.get(parenIndex + 1)).equals("{")) {
            return "Syntax Error - if statement missing symbol '{'\n" + tokens + "\n";
        } else if(!getTokenString(tokens.get(ifBracketIndex)).equals("}")) {
            return "Syntax Error - if statement missing symbol '}'\n" + tokens + "\n";
        }
        
        ifString += (
            "<ifStatement>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            compileExpression(tokens.subList(2, parenIndex)) +
            tokens.get(parenIndex) + "\n" +
            tokens.get(parenIndex + 1) + "\n" +
            compileStatements(tokens.subList(parenIndex + 2, ifBracketIndex)) +
            tokens.get(ifBracketIndex) + "\n"
        );

        if(elseIndex < tokens.size() && getTokenString(tokens.get(elseIndex)).equals("else")) {
            ifString += (
                tokens.get(elseIndex) + "\n" +
                tokens.get(elseIndex + 1) + "\n" +
                compileStatements(tokens.subList(elseIndex + 2, tokens.size() - 1)) +
                tokens.get(tokens.size() - 1) + "\n"
            );
        }

        ifString += "</ifStatement>\n";

        return ifString;
    }

    public String compileWhile(List<String> tokens) {
        String whileString = "";
        int parenIndex = getTopLevelParenIndex(tokens);

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
            "</whileStatement>\n"
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
            "<returnStatement>\n" +
            tokens.get(0) + "\n"
        );

        if(tokens.size() > 2) {
            returnString += compileExpression(tokens.subList(1, tokens.size() - 1));
        }

        returnString += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</returnStatement>\n"
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

        symbolTable.startSubroutine();

        compiledSubroutine = (
            "<subroutineDec>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            "<identifier>\n" +
            " name: " + getTokenString(tokens.get(2)) + "\n" +
            " category: subroutine\n" +
            " isStandard: true\n" +
            " index: 0\n" +
            " usage: definition\n" +
            "</identifier>\n" +
            tokens.get(3) + "\n" +
            compileParameterList(new ArrayList<String>(tokens.subList(4, subroutineBodyIndex - 1))) + 
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

    public String compileParameterList(List<String> tokens) {
        String parameterListString;
        String kind;
        String type;
        String name;
        int commaIndex;
        int nextCommaIndex;
        
        parameterListString = "<parameterList>\n";

        if(tokens.size() > 0) {
            commaIndex = getTokenIndex(tokens, ",");
            kind = "argument";
            type = getTokenString(tokens.get(0));
            name = getTokenString(tokens.get(1));
            symbolTable.define(name, type, kind);

            parameterListString += (
                tokens.get(0) + "\n" +
                "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n"
            );

            while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
                nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
                type = getTokenString(tokens.get(commaIndex + 1));
                name = getTokenString(tokens.get(commaIndex + 2));
                symbolTable.define(name, type, kind);
                parameterListString += (
                    tokens.get(commaIndex) + "\n" +
                    tokens.get(commaIndex + 1) + "\n" +
                    "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n"
                );
                commaIndex = nextCommaIndex;
            }
        }

        parameterListString += "</parameterList>\n";
        return parameterListString;
    }

    public String compileClassVarDec(List<String> tokens) {
        int commaIndex;
        int nextCommaIndex;
        String classVarDecString;
        String kind;
        String type;
        String name;
        if(!isClassVarDec(getTokenString(tokens.get(0)))) {
            return "INVALID FORMAT - missing class variable declaration\n";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "INVALID FORMAT - class variable declaration missing type field";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "INVALID FORMAT - class variable declaration missing terminal token ';'";
        }

        commaIndex = getTokenIndex(tokens, ",");

        kind = getTokenString(tokens.get(0));
        type = getTokenString(tokens.get(1));
        name = getTokenString(tokens.get(2));
        symbolTable.define(name, type, kind);

        classVarDecString = (
            "<classVarDec>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n"
        );

        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            classVarDecString += tokens.get(commaIndex) + "\n";
            classVarDecString += "<identifier>\n" + symbolTable.getSymbolString(name) + "</identifier>\n";
            commaIndex = nextCommaIndex;
        }

        classVarDecString += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</classVarDec>\n"
        );

        return classVarDecString;
    }

    public String compileClass(List<String> tokens) {
        int lastClassVarDecIndex;
        int nextClassVarDecIndex;
        int lastSubroutineIndex;
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

        lastClassVarDecIndex = getClassVarDecIndex(tokens.subList(3, tokens.size() - 1)) + 3;
        lastSubroutineIndex = getSubroutineIndex(tokens.subList(3, tokens.size() - 1)) + 3;

        compiledClass = (
            "<class>\n" +
            tokens.get(0) + "\n" +
            tokens.get(1) + "\n" +
            "<identifier>\n" +
            " name: " + getTokenString(tokens.get(1)) + "\n" +
            " category: class\n" +
            " isStandard: true\n" +
            " index: 0\n" +
            " usage: definition\n" +
            "</identifier>\n"
        );
        
        while(lastClassVarDecIndex < lastSubroutineIndex && isClassVarDec(getTokenString(tokens.get(lastClassVarDecIndex)))) {
            nextClassVarDecIndex = getClassVarDecIndex(tokens.subList(lastClassVarDecIndex + 1, lastSubroutineIndex)) + lastClassVarDecIndex + 1;
            compiledClass += compileClassVarDec(tokens.subList(lastClassVarDecIndex, nextClassVarDecIndex));
            lastClassVarDecIndex = nextClassVarDecIndex;
        }

        while(lastSubroutineIndex < tokens.size() && isSubroutineDec(getTokenString(tokens.get(lastSubroutineIndex)))) {
            nextSubroutineIndex = getSubroutineIndex(tokens.subList(lastSubroutineIndex + 1, tokens.size() - 1)) + lastSubroutineIndex + 1;
            compiledClass += compileSubroutineDec(tokens.subList(lastSubroutineIndex, nextSubroutineIndex));
            lastSubroutineIndex = nextSubroutineIndex;
        }

        compiledClass += (
            tokens.get(tokens.size() - 1) + "\n" +
            "</class>"
        );

        return compiledClass;
    }

    public List<String> compile(List<String> tokens) throws Exception {
        if(!tokens.get(0).equals("<tokens>") || !tokens.get(tokens.size() - 1).equals("</tokens>")) {
            return new ArrayList<String>();
        } else {
            String compiledTokenString = compileClass(tokens.subList(1, tokens.size() - 1));
            List<String> compiledTokens = Arrays.asList(compiledTokenString.split("\n"));
            return compiledTokens;
        }
    }
}