package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirAuxFunctions {
    private static int count = 0;
    private static int countJump = 0;

    public static String getCode(Symbol symbol){
        return symbol.getName() + "." + getCode(symbol.getType());
    }

    public static String getCode(Type type){
        StringBuilder code = new StringBuilder();
        if(type.isArray()){
            code.append("array.");
        }
        String ollirName = getTypeOllir(type.getName());
        code.append(ollirName);
        return code.toString();
    }

    public static String getTypeOllir(String type){
        switch (type){
            case "int", "Integer":
                return "i32";
            case "void":
                return "V";
            case "boolean":
                return "bool";
            default:
                return type;
        }
    }

    public static String getCode(String value, Type type){
        return value + "." + getCode(type);
    }




    public static Type getType(JmmNode node) {
        return new Type(node.getKind(), node.getAttributes().contains("isArray") && node.get("isArray").equals("true"));
    }

    public static String nextTemp(){
        return "t" + count++;
    }

    public static String nextJump(){
        return Integer.toString(countJump++);
    }

    public static String operatorType(String operator){
        switch (operator){
            case "+":
            case "-":
            case "*":
            case "/":
                return ".i32";
            case "&&":
            case "||":
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                return ".bool";
            default:
                return "unknown";
        }
    }

    public static String oppositeOperator(String operator){
        switch (operator){
            case "<":
                return ">=";
            case ">":
                return "<=";
            case "<=":
                return ">";
            case ">=":
                return "<";
            case "==":
                return "!=";
            case "!=":
                return "==";
            case "&&":
                return "||";
            case "||":
                return "&&";
            default:
                return "unknown";
        }
    }
}
