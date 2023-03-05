package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;

public class JavaCalcGenerator extends AJmmVisitor<String,String> {
    private String className;
    private Map<String,Integer> reads = new HashMap<>();
    private Map<String,Integer> writes = new HashMap<>();

    public JavaCalcGenerator() {

    }

    protected void buildVisitor() {
        addVisit ("Program", this :: dealWithProgram );
        addVisit ("Assignment", this :: dealWithAssignment );
        addVisit ("Integer", this :: dealWithLiteral );
        addVisit ("Identifier", this :: dealWithLiteral );
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ExprStmt", this::dealWithExprStmt);
        addVisit("Parenthesis", this::dealWithParenthesis);
    }

    private String dealWithProgram( JmmNode jmmNode , String s) {
        s = (s!= null ?s:"");
        String ret = s+" public class "+ "test" +" {\n";

        return ret;
    }

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        JmmNode expression = jmmNode.getChildren().get(0);
        writes.merge(jmmNode.get("var"),1,Integer::sum);
        String initialPart = s + "int " + jmmNode.get("var") + " = ";
        if(jmmNode.getChildren().get(0).getChildren().isEmpty() && jmmNode.getChildren().get(0).getKind().equals("Identifier")){
            reads.merge(jmmNode.getChildren().get(0).get("value"),1,Integer::sum);
        }
        return visit(expression,initialPart) + ";";
    }

    private String dealWithLiteral(JmmNode jmmNode, String s) {
        return s + jmmNode.get("value");
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        String res = s;
        JmmNode left = jmmNode.getChildren().get(0);
        if(jmmNode.getChildren().get(0).getKind().equals("Identifier")){
            reads.merge(String.valueOf(jmmNode.getChildren().get(0).get("value")),1,Integer::sum);
        }
        JmmNode right = jmmNode.getChildren().get(1);
        if(jmmNode.getChildren().get(1).getKind().equals("Identifier")){
            reads.merge(String.valueOf(jmmNode.getChildren().get(1).get("value")),1,Integer::sum);
        }
        String op = jmmNode.get("op");
        String operator = switch (op) {
            case "'('" -> " (";
            case "')'" -> ") ";
            case "'+'" -> " + ";
            case "'-'" -> " - ";
            case "'*'" -> " * ";
            case "'/'" -> " / ";
            case "'&&'" -> " && ";
            case "'<'" -> " < ";
            default -> jmmNode.get("op");
        };

        res = visit(left, res);
        res += operator;
        res = visit(right, res);
        return res;
    }
    private String dealWithExprStmt(JmmNode jmmNode, String s){
        String aux = "";
        for (JmmNode child: jmmNode.getChildren()){
            aux += visit(child,aux);
        }
        return s + "System.out.println(" + aux + ");";
    }

    private String dealWithParenthesis(JmmNode jmmNode, String s){
        String res = s + '(';
        JmmNode expr = jmmNode.getChildren().get(0);
        res = visit(expr,res);
        res += ')';

        return res;
    }
}
