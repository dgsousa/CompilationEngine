import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import utils.Utils;
import static tokenutils.TokenUtils.*;

public class CompilationEngine {
    String className;
    int ifCount = 0;
    int whileCount = 0;
    SymbolTable symbolTable = new SymbolTable();
    
    private String getSegment(String name) throws Exception {
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
            throw new Exception ("COMPILATION ERROR - VARIABLE NOT FOUND");
        }
    }

    private String convertKeyword(String name) throws Exception {
        if(name.equals("true")) {
            return "constant 1\nneg";
        } else if(name.equals("false")) {
            return "constant 0";
        } else if(name.equals("null")) {
            return "constant 0";
        } else if(name.equals("this")) {
            return "pointer 0";
        } else {
            throw new Exception ("COMPILATION ERROR - KEYWORD CONSTANT NOT FOUND");
        }
    }

    private Boolean isInstance(String name) {
        int index = this.symbolTable.indexOf(name);
        return index != -1;
    }

    private void compileVarDec(List<String> tokens) throws Exception {
        String name;
        String kind;
        String type;
        int commaIndex;
        if(!getTokenString(tokens.get(0)).equals("var")) {
            throw new Exception ("Syntax Error - Missing 'var' declaration\n");
        } else if(!(getTokenType(tokens.get(1)).equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            throw new Exception ("Syntax Error - Missing type\n");
        } else if(!getTokenType(tokens.get(2)).equals("identifier")) {
            throw new Exception ("Syntax Error - Missing var name\n");
        } else if(!getTokenString(tokens.get(tokens.size() - 1)).equals(";")) {
            throw new Exception ("Syntax Error - Missing terminal token ';'\n");
        }

        commaIndex = getTokenIndex(tokens, ",");

        kind = getTokenString(tokens.get(0));
        type = getTokenString(tokens.get(1));
        name = getTokenString(tokens.get(2));
        symbolTable.define(name, type, kind);


        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            commaIndex += 2;
        }
    }

    private String compileSubroutineCall(List<String> tokens) throws Exception {
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
        }

        return "Invalid Format - cannot parse subroutineCall\n";
    }


    private String compileExpressionList(List<String> tokens) throws Exception {
        String expressionListString = "";
        int topLevelCommaIndex;
        int nextTopLevelCommaIndex;

        if(tokens.size() > 0) {
            topLevelCommaIndex = getTopLevelCommaIndex(tokens);

            expressionListString += (
                compileExpression(tokens.subList(0, topLevelCommaIndex))
            );

            while(topLevelCommaIndex < tokens.size() && getTokenString(tokens.get(topLevelCommaIndex)).equals(",")) {
                nextTopLevelCommaIndex = getTopLevelCommaIndex(tokens.subList(topLevelCommaIndex + 1, tokens.size())) + topLevelCommaIndex + 1;
                expressionListString += compileExpression(tokens.subList(topLevelCommaIndex + 1, nextTopLevelCommaIndex));
                topLevelCommaIndex = nextTopLevelCommaIndex;
            }
        }

        return expressionListString;
    }

    private String compileExpression(List<String> tokens) throws Exception {
        String expressionString = "";
        int opIndex = getTopLevelOpIndex(tokens.subList(0, tokens.size()));
        int nextOpIndex;

        if(opIndex == 0 && Utils.isUnaryOp(getTokenString(tokens.get(opIndex)))) {
            return compileTerm(tokens);
        }

        if(opIndex > 0) {
            expressionString += compileTerm(tokens.subList(0, opIndex));
        }

        while(opIndex < tokens.size() && Utils.isOp(getTokenString(tokens.get(opIndex)))) {
            nextOpIndex = getTopLevelOpIndex(tokens.subList(opIndex + 1, tokens.size())) + opIndex + 1;
            expressionString += (
                compileTerm(tokens.subList(opIndex + 1, nextOpIndex)) +
                Utils.getOpCode(getTokenString(tokens.get(opIndex))) + "\n"
            );
            opIndex = nextOpIndex;
        }

        return expressionString;
    }

    private String compileTerm(List<String> tokens) throws Exception {
        String name;
        String stringConstant;
        String stringPushCommands = "";
        int length;
        if(tokens.size() == 0) {
            return "";
        }
        String firstTokenType = getTokenType(tokens.get(0));
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenString;
        String lastTokenString;
        // intergerConstant | stringConstant | keywordConstant || varName
        if(tokens.size() == 1) {
            if(!(firstTokenType.equals("integerConstant")
                || firstTokenType.equals("stringConstant")
                || firstTokenType.equals("keyword")
                || firstTokenType.equals("identifier"))) {
                throw new Exception ("Syntax Error - Wrong Token Type\n" + tokens + "\n");
            } else if(firstTokenType.equals("identifier")) {
                return "push " + getSegment(firstTokenString) + "\n";
            } else if(firstTokenType.equals("integerConstant")) {
                return "push constant " + firstTokenString + "\n";
            } else if(firstTokenType.equals("stringConstant")) {
                stringConstant = firstTokenString;
                length = stringConstant.length();
                for(int i = 0; i < length; i++) {
                    stringPushCommands += (
                        "push constant " + ((int) stringConstant.charAt(i)) + "\n" +
                        "call String.appendChar 2\n"
                    );
            }
                return (
                    "push constant " + length + "\n" +
                    "call String.new 1\n" +
                    stringPushCommands
                );
            } else if(firstTokenType.equals("keyword")) {
                return "push " + convertKeyword(firstTokenString) + "\n";
            }
        }

        secondTokenString = getTokenString(tokens.get(1));
        lastTokenString = getTokenString(tokens.get(tokens.size() - 1));

        // varName [ expression ]
        if(firstTokenType.equals("identifier") && secondTokenString.equals("[") && lastTokenString.equals("]")) {
            return (
                "push " + getSegment(firstTokenString) + "\n" +
                compileExpression(tokens.subList(2, tokens.size() - 1)) +
                "add\n" +
                "pop pointer 1\n" +
                "push that 0\n"
            );
        }

        // subRoutineCall
        if(firstTokenType.equals("identifier") && (secondTokenString.equals("(") || secondTokenString.equals("."))) {
            return compileSubroutineCall(tokens);
        }

        // ( expression )
        if(firstTokenString.equals("(") && lastTokenString.equals(")")) {
            return compileExpression(tokens.subList(1, tokens.size() - 1));
        }

        // unaryOp term
        if(Utils.isUnaryOp(firstTokenString)) {
            return (
                compileTerm(tokens.subList(1, tokens.size())) +
                Utils.getUnaryOpCode(firstTokenString) + "\n"
            );
        }
        
        throw new Exception ("Invalid Format - Tokens do not correspond to valid term\n" + tokens + "\n");
    }

    private String compileLet(List<String> tokens) throws Exception {
        int equalsIndex = getTokenIndex(tokens, "=");
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenString = getTokenString(tokens.get(1));
        String secondTokenType = getTokenType(tokens.get(1));
        String thirdTokenString = getTokenString(tokens.get(2));
        String lastTokenString = getTokenString(tokens.get(tokens.size() - 1));
        if(!firstTokenString.equals("let")) {
            throw new Exception ("Syntax Error - Must have let declaration\n");
        } else if(!secondTokenType.equals("identifier")) {
            throw new Exception ("Syntax Error - Let statement missing name\n");
        } else if(!lastTokenString.equals(";")) {
            throw new Exception ("Syntax Error - Let statement missing terminal declaration ';'\n");
        } else if(!(thirdTokenString.equals("[") || thirdTokenString.equals("="))) {
            throw new Exception ("Syntax Error - Missing either [ or = ");
        }

        // let varName [ expression ]
        if(equalsIndex != 2) {
            return (
                compileExpression(tokens.subList(equalsIndex + 1, tokens.size() - 1)) +
                "push " + getSegment(secondTokenString) + "\n" +
                compileExpression(tokens.subList(3, equalsIndex - 1)) +
                "add\n" +
                "pop pointer 1\n" +
                "pop that 0\n"
            );
        }

        return (
            compileExpression(tokens.subList(equalsIndex + 1, tokens.size() - 1)) +
            "pop " + getSegment(secondTokenString) + "\n"
        );
    }

    private String compileIf(List<String> tokens) throws Exception {
        int localIfCount = this.ifCount;
        int parenIndex = getTopLevelParenIndex(tokens);
        int ifBracketIndex = getTopLevelBracketIndex(tokens);
        int elseIndex = getTopLevelElseIndex(tokens);
        String ifString;
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenString = getTokenString(tokens.get(1));
        String closeParentTokenString = getTokenString(tokens.get(parenIndex));
        String firstBracketTokenString = getTokenString(tokens.get(parenIndex + 1));
        String lastBracketTokenString = getTokenString(tokens.get(ifBracketIndex));
        if(!firstTokenString.equals("if")) {
            throw new Exception ("Syntax Error - Must have if declaration\n");
        } else if(!secondTokenString.equals("(")) {
            throw new Exception ("Syntax Error - if statement missing symbol '('\n");
        } else if(!closeParentTokenString.equals(")")) {
            throw new Exception ("Syntax Error - if statement missing symbol ')'\n");
        } else if(!firstBracketTokenString.equals("{")) {
            throw new Exception ("Syntax Error - if statement missing symbol '{'\n" + tokens + "\n");
        } else if(!lastBracketTokenString.equals("}")) {
            throw new Exception ("Syntax Error - if statement missing symbol '}'\n" + tokens + "\n");
        }

        this.ifCount += 1;

        ifString = (
            compileExpression(tokens.subList(2, parenIndex)) +
            "not\n" +
            "if-goto IF_TRUE" + localIfCount + "\n" +
            compileStatements(tokens.subList(parenIndex + 2, ifBracketIndex)) +
            "goto IF_FALSE" + localIfCount + "\n" +
            "label IF_TRUE" + localIfCount + "\n"
        );

        if(elseIndex < tokens.size() && getTokenString(tokens.get(elseIndex)).equals("else")) {
            ifString += compileStatements(tokens.subList(elseIndex + 2, tokens.size() - 1));
        }
        
        ifString += "label IF_FALSE" + localIfCount + "\n";

        return ifString;
    }

    private String compileWhile(List<String> tokens) throws Exception {
        String whileString = "";
        int localWhileCount = this.whileCount;
        int parenIndex = getTopLevelParenIndex(tokens);
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenString = getTokenString(tokens.get(1));
        String parenTokenString = getTokenString(tokens.get(parenIndex));
        String bracketTokenString = getTokenString(tokens.get(parenIndex + 1));
        String lastTokenString = getTokenString(tokens.get(tokens.size() - 1));

        if(!firstTokenString.equals("while")) {
            throw new Exception ("Syntax Error - Must have while declaration\n");
        } else if(!secondTokenString.equals("(")) {
            throw new Exception ("Syntax Error - while statement missing symbol '('\n");
        } else if(!parenTokenString.equals(")")) {
            throw new Exception ("Syntax Error - while statement missing symbol ')'\n");
        } else if(!bracketTokenString.equals("{")) {
            throw new Exception ("Syntax Error - while statement missing symbol '{'\n");
        } else if(!lastTokenString.equals("}")) {
            throw new Exception ("Syntax Error - while statement missing symbol '}'\n");
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

        return whileString;
    }

    private String compileDo(List<String> tokens) throws Exception {
        String compiledSubroutineCall;
        int parenIndex = getTokenIndex(tokens, ")");
        String firstTokenString = getTokenString(tokens.get(0));
        String lastTokenString = getTokenString(tokens.get(tokens.size() - 1));

        if(!firstTokenString.equals("do")) {
            throw new Exception ("Syntax Error - Must have do declaration\n");
        } else if(!lastTokenString.equals(";")) {
            throw new Exception ("Syntax Error - while statement missing symbol ';'\n");
        }

        compiledSubroutineCall = compileSubroutineCall(tokens.subList(1, tokens.size() - 1));
        
        return (
            compiledSubroutineCall +
            "pop temp 0\n"
        );
    }

    private String compileReturn(List<String> tokens) throws Exception {
        String returnString = "push constant 0\n";
        String firstTokenString = getTokenString(tokens.get(0));
        String lastTokenString = getTokenString(tokens.get(tokens.size() - 1));

        if(!firstTokenString.equals("return")) {
            throw new Exception ("Syntax Error - Must have return declaration\n");
        } else if(!lastTokenString.equals(";")) {
            throw new Exception ("Syntax Error - return statement missing symbol ';'\n");
        }

        if(tokens.size() > 2) {
            returnString = compileExpression(tokens.subList(1, tokens.size() - 1));
        }

        return (
            returnString +
            "return\n"
        );
    }

    // needs syntax validation?
    private String compileStatements(List<String> tokens) throws Exception {
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

    private String compileSubroutineDec(List<String> tokens) throws Exception {
        int subroutineBodyIndex = getTokenIndex(tokens, "{");
        int varDecIndex = subroutineBodyIndex + 1;
        int statementsIndex = subroutineBodyIndex + 1;
        int endVarDecIndex;
        int numLocalVars;
        int numFields;
        String subroutineName;
        String compiledSubroutine;
        String compiledStatements;
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenType = getTokenType(tokens.get(1));
        String thirdTokenType = getTokenType(tokens.get(2));

        if(!isSubroutineDec(firstTokenString)) {
            throw new Exception ("Syntax Error - should be Subroutine declaration");
        } else if(!(secondTokenType.equals("keyword") || getTokenType(tokens.get(1)).equals("identifier"))) {
            throw new Exception ("Syntax Error - should be keyword or identifier");
        } else if(!thirdTokenType.equals("identifier")) {
            throw new Exception ("Syntax Error - token should be identifier");
        }

        symbolTable.startSubroutine();
        if(firstTokenString.equals("method")) {
            symbolTable.define("this", this.className, "argument");
        }
        
        compileParameterList(new ArrayList<String>(tokens.subList(4, subroutineBodyIndex - 1)));

        if(getTokenString(tokens.get(varDecIndex)).equals("var")) {
            while(varDecIndex < tokens.size() && getTokenString(tokens.get(varDecIndex)).equals("var")) {
                endVarDecIndex = getTokenIndex(tokens.subList(varDecIndex, tokens.size()), ";") + varDecIndex;
                compileVarDec(tokens.subList(varDecIndex, endVarDecIndex + 1));
                varDecIndex = endVarDecIndex + 1;
            }
            statementsIndex = varDecIndex;
        }

        compiledStatements = compileStatements(tokens.subList(statementsIndex, tokens.size() - 1));

        subroutineName = getTokenString(tokens.get(2));
        numLocalVars = symbolTable.getVarCount("var");
        numFields = symbolTable.getVarCount("field");
        compiledSubroutine = "function " + this.className + "." + subroutineName + " " + numLocalVars + "\n";
        if(firstTokenString.equals("constructor")) {
            compiledSubroutine += (
                "push constant " + numFields + "\n" +
                "call Memory.alloc 1\n" +
                "pop pointer 0\n"
            );
        } else if(firstTokenString.equals("method")) {
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

    // only needs to update the symbol table, no code generated
    // needs syntax validation?
    private void compileParameterList(List<String> tokens) {
        String kind;
        String type;
        String name;
        int commaIndex;
        int nextCommaIndex;

        if(tokens.size() > 0) {
            commaIndex = getTokenIndex(tokens, ",");
            kind = "argument";
            type = getTokenString(tokens.get(0));
            name = getTokenString(tokens.get(1));
            symbolTable.define(name, type, kind);

            while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
                nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
                type = getTokenString(tokens.get(commaIndex + 1));
                name = getTokenString(tokens.get(commaIndex + 2));
                symbolTable.define(name, type, kind);
                commaIndex = nextCommaIndex;
            }
        }
    }

    // only needs to update symbol table, no code generated
    private void compileClassVarDec(List<String> tokens) throws Exception {
        int commaIndex;
        int nextCommaIndex;
        String kind;
        String type;
        String name;
        String firstTokenString = getTokenString(tokens.get(0));
        String secondTokenString = getTokenString(tokens.get(1));
        String secondTokenType = getTokenType(tokens.get(1));
        String thirdTokenString = getTokenString(tokens.get(2));
        String lastTokenString = getTokenString(tokens.get(tokens.size() - 1));
        if(!isClassVarDec(firstTokenString)) {
            throw new Exception("INVALID FORMAT - missing class variable declaration");
        } else if(!(secondTokenType.equals("keyword") || secondTokenType.equals("identifier"))) {
            throw new Exception ("INVALID FORMAT - class variable declaration missing type field");
        } else if(!lastTokenString.equals(";")) {
            throw new Exception ("INVALID FORMAT - class variable declaration missing terminal token ';'");
        }

        commaIndex = getTokenIndex(tokens, ",");

        kind = firstTokenString;
        type = secondTokenString;
        name = thirdTokenString;
        symbolTable.define(name, type, kind);

        while(commaIndex < tokens.size() && getTokenString(tokens.get(commaIndex)).equals(",")) {
            nextCommaIndex = getTokenIndex(tokens.subList(commaIndex + 1, tokens.size()), ",") + commaIndex + 1;
            name = getTokenString(tokens.get(commaIndex + 1));
            symbolTable.define(name, type, kind);
            commaIndex = nextCommaIndex;
        }
    }

    private String compileClass(List<String> tokens) throws Exception {
        int lastClassVarDecIndex;
        int nextClassVarDecIndex;
        int lastSubroutineIndex;
        int nextSubroutineIndex;
        String compiledClass;
        String firstToken = tokens.get(0);
        String secondTokenType = getTokenType(tokens.get(1));
        String thirdTokenType = getTokenType(tokens.get(2));
        String lastTokenType = getTokenType(tokens.get(tokens.size() - 1));
        if(!firstToken.equals("<keyword>class</keyword>")) {
            throw new Exception ("Syntax Error - No 'class' token");
        } else if(!secondTokenType.equals("identifier")) {
            throw new Exception ("Syntax Error - No class identifier");
        } else if(!thirdTokenType.equals("symbol")) {
            throw new Exception ("Syntax Error - Missing token '{' on class assignment");
        } else if(!lastTokenType.equals("symbol")) {
            throw new Exception ("Syntax Error - Missing token '}' on class assignment");
        }

        this.className = getTokenString(tokens.get(1));
        lastClassVarDecIndex = getClassVarDecIndex(tokens.subList(3, tokens.size() - 1)) + 3;
        lastSubroutineIndex = getSubroutineIndex(tokens.subList(3, tokens.size() - 1)) + 3;

        compiledClass = "";
        
        while(lastClassVarDecIndex < lastSubroutineIndex && isClassVarDec(getTokenString(tokens.get(lastClassVarDecIndex)))) {
            nextClassVarDecIndex = getClassVarDecIndex(tokens.subList(lastClassVarDecIndex + 1, lastSubroutineIndex)) + lastClassVarDecIndex + 1;
            compileClassVarDec(tokens.subList(lastClassVarDecIndex, nextClassVarDecIndex));
            lastClassVarDecIndex = nextClassVarDecIndex;
        }

        while(lastSubroutineIndex < tokens.size() && isSubroutineDec(getTokenString(tokens.get(lastSubroutineIndex)))) {
            nextSubroutineIndex = getSubroutineIndex(tokens.subList(lastSubroutineIndex + 1, tokens.size() - 1)) + lastSubroutineIndex + 1;
            compiledClass += compileSubroutineDec(tokens.subList(lastSubroutineIndex, nextSubroutineIndex));
            lastSubroutineIndex = nextSubroutineIndex;
        }

        return compiledClass;
    }

    public List<String> compile(List<String> tokens) throws Exception {
        String firstToken = tokens.get(0);
        String lastToken = tokens.get(tokens.size() - 1);
        if(!firstToken.equals("<tokens>") || !lastToken.equals("</tokens>")) {
            throw new Exception("Syntax Error - missing wrapping 'token'");
        } else {
            String compiledVMCode = compileClass(tokens.subList(1, tokens.size() - 1));
            List<String> vmCode = Arrays.asList(compiledVMCode.split("\n"));
            return vmCode;
        }
    }
}