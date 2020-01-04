package utils;

import java.util.HashMap;

public class Utils {
    private static HashMap<String, String> opCodes = new HashMap<String, String>() {{
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
    private static HashMap<String, String> unaryOpCodes = new HashMap<String, String>() {{
        put("~", "not");
        put("-", "neg");
    }};

    public static String getOpCode(String op) {
        return opCodes.get(op);
    }

    public static String getUnaryOpCode(String op) {
        return unaryOpCodes.get(op);
    }

    public static Boolean isOp(String op) {
        return opCodes.containsKey(op) || unaryOpCodes.containsKey(op);
    }

    public static Boolean isUnaryOp(String op) {
        return unaryOpCodes.containsKey(op);
    }
}