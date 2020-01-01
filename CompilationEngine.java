import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CompilationEngine {
    String className;
    int ifCount = 0;
    int whileCount = 0;
    SymbolTable symbolTable = new SymbolTable();
    HashMap<String, String> opCodes = new HashMap<String, String>() {{
        put("+", "add");
        put("-", "sub");
        put("*", "call Math.multiply 2");
        put("/", "call Math.divide 2");
        put("&amp;", "and");
        put("|", "or");
        put("=", "eq");
        put("&gt;", "gt");
        put("&lt;", "lt");
    }};
    HashMap<String, String> unaryOpCodes = new HashMap<String, String>() {{
        put("~", "not");
        put("-", "neg");
    }};

    public String getSegment(String name) {
        String kind = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);
        if(kind.equals("var")) {
            return "local " + index;
        } else if(kind.equals("argument")) {
            return "argument " + index;
        } else if(kind.equals("static")) {
            return "static " + index;
        } else if(kind.equals("field")) {
            return "this " + index;
        } else {
            return "COMPILATION ERROR - VARIABLE NOT FOUND";
        }
    }

    public String convertKeyword(String name) {
        if(name.equals("true")) {
            return "constant 1\nneg";
        } else if(name.equals("false")) {
            return "constant 0";
        } else if(name.equals("null")) {
            return "constant 0";
        } else if(name.equals("this")) {
            return "pointer 0";
        } else {
            return "COMPILATION ERROR - KEYWORD CONSTANT NOT FOUND";
        }
    }

    public Boolean isInstance(String name) {
        int index = this.symbolTable.indexOf(name);
        return index != -1;
    }

    public String getOpCode(String op) {
        return opCodes.get(op);
    }

    public String getUnaryOpCode(String op) {
        return unaryOpCodes.get(op);
    }

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

    public int getTopLevelBracketIndex(List<String> tokens) {
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

    public int getTopLevelElseIndex(List<String> tokens) {
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

    public int countNumArgs(List<String> tokens) {
        int topLevelCommaIndex;
        int numArgs = 0;

        if(tokens.size() > 0) {
            numArgs = 1;
            topLevelCommaIndex = getTopLevelCommaIndex(tokens);
            while(topLevelCommaIndex < tokens.size() && getTokenString(tokens.get(topLevelCommaIndex)).equals(",")) {
                numArgs += 1;
                topLevelCommaIndex = getTopLevelCommaIndex(tokens.subList(topLevelCommaIndex + 1, tokens.size())) + topLevelCommaIndex + 1;
            }
        }
        return numArgs;
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
            "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n"
        );

        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            varDecString += tokens.get(commaIndex) + "\n";
            varDecString += "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n";
            commaIndex += 2;
        }
        
        varDecString += tokens.get(tokens.size() - 1) + "\n";
        varDecString += "</varDec>\n";
        return varDecString;
    }

    public String compileSubroutineCall(List<String> tokens) {
        int numArgs;
        String subroutineName;
        String type;
        String compiledExpressionList;
        List<String> expressionSublist;

        // someSubroutine.somemethodOrFunction()
        if(getTokenType(tokens.get(0)).equals("identifier") && getTokenString(tokens.get(1)).equals(".") ) {
            expressionSublist = tokens.subList(4, tokens.size() - 1);
            numArgs = countNumArgs(expressionSublist);
            subroutineName = getTokenString(tokens.get(0)) + "." + getTokenString(tokens.get(2));
            compiledExpressionList = compileExpressionList(expressionSublist);
            if(isInstance(getTokenString(tokens.get(0)))) {
                type = this.symbolTable.typeOf(getTokenString(tokens.get(0)));
                return (
                    "push " + getSegment(getTokenString(tokens.get(0))) + "\n" +
                    compiledExpressionList +
                    "call " + type + "." + getTokenString(tokens.get(2)) + " " + (numArgs + 1) + "\n"
                );
            }
            return (
                compiledExpressionList +
                "call " + subroutineName + " " + numArgs + "\n"
            );
            // return (
            //     "<identifier>\n" +
            //     " name: " + getTokenString(tokens.get(0)) + "\n" +
            //     " category: subroutine\n" +
            //     " isStandard: false\n" +
            //     " index: 0\n" +
            //     " usage: call\n" +
            //     "</identifier>\n" +
            //     tokens.get(1) + "\n" +
            //     compileSubroutineCall(tokens.subList(2, tokens.size()))
            // );

        // someSubroutine()
        } else if(getTokenType(tokens.get(0)).equals("identifier") && getTokenString(tokens.get(1)).equals("(") && getTokenString(tokens.get(tokens.size() - 1)).equals(")")) {
            expressionSublist = tokens.subList(2, tokens.size() - 1);
            numArgs = countNumArgs(expressionSublist);
            subroutineName = this.className + "." + getTokenString(tokens.get(0)); // if no className, use current className
            compiledExpressionList = compileExpressionList(expressionSublist);
            return (
                "push pointer 0\n" +
                compiledExpressionList +
                "call " + subroutineName + " " + (numArgs + 1) + "\n"
            );

            // return (
            //     "<identifier>\n" +
            //     " name: " + getTokenString(tokens.get(0)) + "\n" +
            //     " category: subroutine\n" +
            //     " isStandard: false\n" +
            //     " index: 0\n" +
            //     " usage: call\n" +
            //     "</identifier>\n" +
            //     tokens.get(1) + "\n" +
            //     compileExpressionList(tokens.subList(2, tokens.size() - 1)) +
            //     tokens.get(tokens.size() - 1) + "\n"
            // );
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
        String expressionListString = "";
        int topLevelCommaIndex;
        int nextTopLevelCommaIndex;
        
        // expressionListString = "<expressionList>\n";

        if(tokens.size() > 0) {
            topLevelCommaIndex = getTopLevelCommaIndex(tokens);

            expressionListString += (
                compileExpression(tokens.subList(0, topLevelCommaIndex))
            );

            while(topLevelCommaIndex < tokens.size() && getTokenString(tokens.get(topLevelCommaIndex)).equals(",")) {
                nextTopLevelCommaIndex = getTopLevelCommaIndex(tokens.subList(topLevelCommaIndex + 1, tokens.size())) + topLevelCommaIndex + 1;
                expressionListString += (
                    // tokens.get(topLevelCommaIndex) + "\n" +
                    compileExpression(tokens.subList(topLevelCommaIndex + 1, nextTopLevelCommaIndex))
                );
                topLevelCommaIndex = nextTopLevelCommaIndex;
            }
        }

        // expressionListString += "</expressionList>\n";

        return expressionListString;
    }

    public String compileExpression(List<String> tokens) {
        String expressionString = "";
        int opIndex = getTopLevelOpIndex(tokens.subList(0, tokens.size()));
        int nextOpIndex;

        if(opIndex == 0 && isUnaryOp(getTokenString(tokens.get(opIndex)))) {
            return compileTerm(tokens);
            // return (
            //     "<expression>\n" +
            //     compileTerm(tokens) +
            //     "</expression>\n"
            // );
        }

        // expressionString = "<expression>\n";

        if(opIndex > 0) {
            expressionString += compileTerm(tokens.subList(0, opIndex));
        }

        while(opIndex < tokens.size() && isOp(getTokenString(tokens.get(opIndex)))) {
            nextOpIndex = getTopLevelOpIndex(tokens.subList(opIndex + 1, tokens.size())) + opIndex + 1;
            expressionString += (
                compileTerm(tokens.subList(opIndex + 1, nextOpIndex)) +
                getOpCode(getTokenString(tokens.get(opIndex))) + "\n"
            );
            // expressionString += (
            //     tokens.get(opIndex) + "\n" +
            //     compileTerm(tokens.subList(opIndex + 1, nextOpIndex))
            // );
            opIndex = nextOpIndex;
        }

        // expressionString += (
        //     "</expression>\n"
        // );

        return expressionString;
    }

    public String compileTerm(List<String> tokens) {
        String name;
        if(tokens.size() == 0) {
            return ""; // "<term>\n</term>\n";
        }
        String firstTokenType = getTokenType(tokens.get(0));
        // intergerConstant | stringConstant | keywordConstant || varName
        if(tokens.size() == 1) {
            if(!(firstTokenType.equals("integerConstant")
                || firstTokenType.equals("stringConstant")
                || firstTokenType.equals("keyword")
                || firstTokenType.equals("identifier"))) {
                return "Invalid Format - Wrong Token Type\n" + tokens + "\n";
            } else if(firstTokenType.equals("identifier")) {
                return "push " + getSegment(getTokenString(tokens.get(0))) + "\n";
                // return (
                //     "<term>\n" +
                //     "<identifier>\n" + symbolTable.getSymbolString(name, "call") + "</identifier>\n" +
                //     "</term>\n"
                // );
            } else if(firstTokenType.equals("integerConstant")) {
                return "push constant " + getTokenString(tokens.get(0)) + "\n";
            } else if(firstTokenType.equals("stringConstant")) {
                return "STRING CONSTANT TERMS NOT IMPLEMENTED YET";
            } else if(firstTokenType.equals("keyword")) {
                return "push " + convertKeyword(getTokenString(tokens.get(0))) + "\n";
            }
            // return (
            //     "<term>\n" +
            //     tokens.get(0) + "\n" +
            //     "</term>\n"
            // );  
        }

        // varName [ expression ]
        if(getTokenType(tokens.get(0)).equals("identifier")
            && getTokenString(tokens.get(1)).equals("[")
            && getTokenString(tokens.get(tokens.size() - 1)).equals("]")) {
            name = getTokenString(tokens.get(0));
            return (
                "<term>\n" +
                "<identifier>\n" + symbolTable.getSymbolString(name, "call") + "</identifier>\n" +
                tokens.get(1) + "\n" +
                compileExpression(tokens.subList(2, tokens.size() - 1)) +
                tokens.get(tokens.size() - 1) + "\n" +
                "</term>\n" 
            );
        }

        // subRoutineCall
        if(getTokenType(tokens.get(0)).equals("identifier")
            && (getTokenString(tokens.get(1)).equals("(") || getTokenString(tokens.get(1)).equals("."))) {
            return compileSubroutineCall(tokens);
            // return (
            //     "<term>\n" +
            //     compileSubroutineCall(tokens) +
            //     "</term>\n" 
            // );
        }

        // ( expression )
        if(getTokenString(tokens.get(0)).equals("(") && getTokenString(tokens.get(tokens.size() - 1)).equals(")")) {
            return compileExpression(tokens.subList(1, tokens.size() - 1));
            // return (
            //     "<term>\n" +
            //     tokens.get(0) + "\n" +
            //     compileExpression(tokens.subList(1, tokens.size() - 1)) +
            //     tokens.get(tokens.size() - 1) + "\n" +
            //     "</term>\n"
            // );
        }

        // unaryOp term
        if(isUnaryOp(getTokenString(tokens.get(0)))) {
            return (
                compileTerm(tokens.subList(1, tokens.size())) +
                getUnaryOpCode(getTokenString(tokens.get(0))) + "\n"
            );
            // return (
            //     "<term>\n" +
            //     tokens.get(0) + "\n" +
            //     compileTerm(tokens.subList(1, tokens.size())) + // only clip off the first token
            //     "</term>\n"
            // );
        }
        
        return "Invalid Format - Tokens do not correspond to valid term\n" + tokens + "\n";
    }

    public String compileLet(List<String> tokens) {
        String letString = "";
        // String name;
        int equalsIndex = getTokenIndex(tokens, "=");
        if(!getTokenString(tokens.get(0)).equals("let")) {
            return "Syntax Error - Must have let declaration\n";
        } else if(!getTokenType(tokens.get(1)).equals("identifier")) {
            return "Syntax Error - Let statement missing name\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - Let statement missing terminal declaration ';'\n";
        }

        // name = getTokenString(tokens.get(1));

        // letString += (
        //     "<letStatement>\n" +
        //     tokens.get(0) + "\n" +
        //     "<identifier>\n" + symbolTable.getSymbolString(name, "call") + "</identifier>" // tokens.get(1) + "\n"
        // );

        if(!(getTokenString(tokens.get(2)).equals("[") || getTokenString(tokens.get(2)).equals("="))) {
            return "Syntax Error - Missing either [ or = ";
        }

        // let varName [ expression ]
        // if(equalsIndex != 2) {
        //     letString += (
        //         tokens.get(2) + "\n" +
        //         compileExpression(tokens.subList(3, equalsIndex - 1)) +
        //         tokens.get(equalsIndex - 1) + "\n"
        //     );
        // }

        // letString += tokens.get(equalsIndex) + "\n";
        // letString += compileExpression(tokens.subList(equalsIndex + 1, tokens.size() - 1));
        // letString += tokens.get(tokens.size() - 1) + "\n";
        // letString += "</letStatement>\n";

        letString += (
            compileExpression(tokens.subList(equalsIndex + 1, tokens.size() - 1)) +
            "pop " + getSegment(getTokenString(tokens.get(1))) + "\n"
        );

        return letString;
    }

    public String compileIf(List<String> tokens) {
        String ifString = "";
        int localIfCount = this.ifCount;
        int parenIndex = getTopLevelParenIndex(tokens);
        int ifBracketIndex = getTopLevelBracketIndex(tokens);
        int elseIndex = getTopLevelElseIndex(tokens);
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

        this.ifCount += 1;

        ifString += (
            compileExpression(tokens.subList(2, parenIndex)) +
            "not\n" +
            "if-goto IF_TRUE" + localIfCount + "\n" +
            compileStatements(tokens.subList(parenIndex + 2, ifBracketIndex)) +
            "goto IF_FALSE" + localIfCount + "\n" +
            "label IF_TRUE" + localIfCount + "\n"
        );
        
        // ifString += (
        //     "<ifStatement>\n" +
        //     tokens.get(0) + "\n" +
        //     tokens.get(1) + "\n" +
        //     compileExpression(tokens.subList(2, parenIndex)) +
        //     tokens.get(parenIndex) + "\n" +
        //     tokens.get(parenIndex + 1) + "\n" +
        //     compileStatements(tokens.subList(parenIndex + 2, ifBracketIndex)) +
        //     tokens.get(ifBracketIndex) + "\n"
        // );

        if(elseIndex < tokens.size() && getTokenString(tokens.get(elseIndex)).equals("else")) {
            ifString += compileStatements(tokens.subList(elseIndex + 2, tokens.size() - 1));

            // ifString += (
            //     tokens.get(elseIndex) + "\n" +
            //     tokens.get(elseIndex + 1) + "\n" +
            //     compileStatements(tokens.subList(elseIndex + 2, tokens.size() - 1)) +
            //     tokens.get(tokens.size() - 1) + "\n"
            // );
        }
        
        ifString += "label IF_FALSE" + localIfCount + "\n";

        // ifString += "</ifStatement>\n";
        return ifString;
    }

    public String compileWhile(List<String> tokens) {
        String whileString = "";
        int localWhileCount = this.whileCount;
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

        this.whileCount += 1;

        whileString = (
            "label WHILE_FALSE" + localWhileCount + "\n" +
            compileExpression(tokens.subList(2, parenIndex)) +
            "not\n" +
            "if-goto WHILE_TRUE" + localWhileCount + "\n" +
            compileStatements(tokens.subList(parenIndex + 2, tokens.size() - 1)) +
            "goto WHILE_FALSE" + localWhileCount + "\n" +
            "label WHILE_TRUE" + localWhileCount + "\n"
        );

        // whileString = (
        //     "<whileStatement>\n" +
        //     tokens.get(0) + "\n" +
        //     tokens.get(1) + "\n" +
        //     compileExpression(tokens.subList(2, parenIndex)) +
        //     tokens.get(parenIndex) + "\n" +
        //     tokens.get(parenIndex + 1) + "\n" +
        //     compileStatements(tokens.subList(parenIndex + 2, tokens.size() - 1)) +
        //     tokens.get(tokens.size() - 1) + "\n" +
        //     "</whileStatement>\n"
        // );

        return whileString;
    }

    public String compileDo(List<String> tokens) {
        // String doString;
        String compiledSubroutineCall;
        int parenIndex = getTokenIndex(tokens, ")");

        if(!getTokenString(tokens.get(0)).equals("do")) {
            return "Syntax Error - Must have do declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - while statement missing symbol ';'\n";
        }

        compiledSubroutineCall = compileSubroutineCall(tokens.subList(1, tokens.size() - 1));

        // doString = (
        //     "<doStatement>\n" +
        //     tokens.get(0) + "\n" +
        //     compileSubroutineCall(tokens.subList(1, tokens.size() - 1)) +
        //     tokens.get(tokens.size() - 1) + "\n" +
        //     "</doStatement>\n"
        // );
        
        return (
            compiledSubroutineCall +
            "pop temp 0\n"
        );
    }

    public String compileReturn(List<String> tokens) {
        String returnString = "push constant 0\n";

        if(!getTokenString(tokens.get(0)).equals("return")) {
            return "Syntax Error - Must have return declaration\n";
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            return "Syntax Error - return statement missing symbol ';'\n";
        }

        // returnString += (
        //     "<returnStatement>\n" +
        //     tokens.get(0) + "\n"
        // );

        if(tokens.size() > 2) {
            returnString = compileExpression(tokens.subList(1, tokens.size() - 1));
        }

        // returnString += (
        //     tokens.get(tokens.size() - 1) + "\n" +
        //     "</returnStatement>\n"
        // );

        return (
            returnString +
            "return\n"
        );
    }

    public String compileStatements(List<String> tokens) {
        String compiledStatements = "";
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

        return compiledStatements;
    }

    public String compileSubroutineDec(List<String> tokens) {
        int subroutineBodyIndex = getTokenIndex(tokens, "{");
        int varDecIndex = subroutineBodyIndex + 1;
        int statementsIndex = subroutineBodyIndex + 1;
        int endVarDecIndex;
        int numLocalVars;
        int numFields;
        String subroutineName;
        String compiledSubroutine;
        String compiledStatements;

        if(!isSubroutineDec(getTokenString(tokens.get(0)))) {
            return "Syntax Error - should be Subroutine declaration";
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            return "Syntax Error - should be keyword or identifier";
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            return "Syntax Error - token should be identifier";
        }

        symbolTable.startSubroutine();
        if(getTokenString(tokens.get(0)).equals("method")) {
            symbolTable.define("this", this.className, "argument");
        }
        
        // compiledSubroutine = (
            // "<subroutineDec>\n" +
            // tokens.get(0) + "\n" +
            // tokens.get(1) + "\n" +
            // "<identifier>\n" +
            // " name: " + getTokenString(tokens.get(2)) + "\n" +
            // " category: subroutine\n" +
            // " isStandard: false\n" +
            // " index: 0\n" +
            // " usage: definition\n" +
            // "</identifier>\n" +
            // tokens.get(3) + "\n" +
            compileParameterList(new ArrayList<String>(tokens.subList(4, subroutineBodyIndex - 1))); // + 
            // tokens.get(subroutineBodyIndex - 1) + "\n" +
            // "<subroutineBody>\n" +
            // tokens.get(subroutineBodyIndex) + "\n"
        // );

        if(getTokenString(tokens.get(varDecIndex)).equals("var")) {
            while(varDecIndex < tokens.size() && getTokenString(tokens.get(varDecIndex)).equals("var")) {
                endVarDecIndex = getTokenIndex(tokens.subList(varDecIndex, tokens.size()), ";") + varDecIndex;
                /* compiledSubroutine += */ compileVarDec(tokens.subList(varDecIndex, endVarDecIndex + 1));
                varDecIndex = endVarDecIndex + 1;
            }
            statementsIndex = varDecIndex;
        }

        compiledStatements = compileStatements(tokens.subList(statementsIndex, tokens.size() - 1)); // clip off terminal '}'

        // compiledSubroutine += tokens.get(tokens.size() - 1) + "\n";
        // compiledSubroutine += "</subroutineBody>\n";
        // compiledSubroutine += "</subroutineDec>\n";
        subroutineName = getTokenString(tokens.get(2));
        numLocalVars = symbolTable.getVarCount("var");
        numFields = symbolTable.getVarCount("field");
        compiledSubroutine = "function " + this.className + "." + subroutineName + " " + numLocalVars + "\n";
        if(getTokenString(tokens.get(0)).equals("constructor")) {
            compiledSubroutine += (
                "push constant " + numFields + "\n" +
                "call Memory.alloc 1\n" +
                "pop pointer 0\n"
            );
        } else if(getTokenString(tokens.get(0)).equals("method")) {
            compiledSubroutine += (
                "push argument 0\n" +
                "pop pointer 0\n"
            );
        }
        return (
            "\n" +
            compiledSubroutine +
            compiledStatements
        );
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
                "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n"
            );

            while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
                nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
                type = getTokenString(tokens.get(commaIndex + 1));
                name = getTokenString(tokens.get(commaIndex + 2));
                symbolTable.define(name, type, kind);
                parameterListString += (
                    tokens.get(commaIndex) + "\n" +
                    tokens.get(commaIndex + 1) + "\n" +
                    "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n"
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
            "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n"
        );

        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            classVarDecString += tokens.get(commaIndex) + "\n";
            classVarDecString += "<identifier>\n" + symbolTable.getSymbolString(name, "definition") + "</identifier>\n";
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

        this.className = getTokenString(tokens.get(1));
        lastClassVarDecIndex = getClassVarDecIndex(tokens.subList(3, tokens.size() - 1)) + 3;
        lastSubroutineIndex = getSubroutineIndex(tokens.subList(3, tokens.size() - 1)) + 3;

        compiledClass = ""; // (
        //     "<class>\n" +
        //     tokens.get(0) + "\n" +
        //     "<identifier>\n" +
        //     " name: " + getTokenString(tokens.get(1)) + "\n" +
        //     " category: class\n" +
        //     " isStandard: false\n" +
        //     " index: 0\n" +
        //     " usage: definition\n" +
        //     "</identifier>\n" +
        //     tokens.get(2) + "\n"
        // );
        
        while(lastClassVarDecIndex < lastSubroutineIndex && isClassVarDec(getTokenString(tokens.get(lastClassVarDecIndex)))) {
            nextClassVarDecIndex = getClassVarDecIndex(tokens.subList(lastClassVarDecIndex + 1, lastSubroutineIndex)) + lastClassVarDecIndex + 1;
            /* compiledClass  += */ compileClassVarDec(tokens.subList(lastClassVarDecIndex, nextClassVarDecIndex));
            lastClassVarDecIndex = nextClassVarDecIndex;
        }

        while(lastSubroutineIndex < tokens.size() && isSubroutineDec(getTokenString(tokens.get(lastSubroutineIndex)))) {
            nextSubroutineIndex = getSubroutineIndex(tokens.subList(lastSubroutineIndex + 1, tokens.size() - 1)) + lastSubroutineIndex + 1;
            compiledClass += compileSubroutineDec(tokens.subList(lastSubroutineIndex, nextSubroutineIndex));
            lastSubroutineIndex = nextSubroutineIndex;
        }

        // compiledClass += (
        //     tokens.get(tokens.size() - 1) + "\n" +
        //     "</class>"
        // );

        return compiledClass;
    }

    public List<String> compile(List<String> tokens) throws Exception {
        if(!tokens.get(0).equals("<tokens>") || !tokens.get(tokens.size() - 1).equals("</tokens>")) {
            return new ArrayList<String>();
        } else {
            String compiledVMCode = compileClass(tokens.subList(1, tokens.size() - 1));
            List<String> vmCode = Arrays.asList(compiledVMCode.split("\n"));
            return vmCode;
        }
    }
}