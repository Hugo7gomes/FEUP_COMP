package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.MySymbolTable;

import java.util.List;
import java.util.Objects;

public class ProgramAnalyser extends AJmmVisitor<String, Type> {

    private MySymbolTable symbolTable;
    private List<Report> reports;

    public ProgramAnalyser(MySymbolTable table, List<Report> reports){
        this.symbolTable = table;
        this.reports = reports;
    }


    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
    }


    private Type dealWithMethod(JmmNode jmmNode, String s) {
        String methodName = jmmNode.get("methodName");
        List<JmmNode> children = jmmNode.getChildren();
        for(int i = 0; i < children.size(); i++){
            JmmNode child = children.get(i);
            Type childType = new Type("",false);
            switch (child.getKind()) {
                case "Stmt", "IfElseStmt", "WhileStmt", "ExprStmt", "Assignment", "ArrayAssignment" -> {
                    StatementAnalyser statementAnalyser = new StatementAnalyser(symbolTable, reports);
                    childType = statementAnalyser.visit(child, "");
                }
                case "Parenthesis", "Indexing", "Length", "MethodCall", "UnaryOp", "BinaryOp", "NewIntArray", "NewObject", "Integer", "Boolean", "Identifier", "This" -> {
                    ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable, reports);
                    childType = expressionAnalyser.visit(child, "");
                }
            }
            if(!methodName.equals("main") && i == children.size() - 1 ){
                //Assume return is correct
                if(childType.getName().equals("importCorrect")){
                    return new Type("importCorrect", false);
                }
                //Check if the returning types match
                else if(!childType.getName().equals(symbolTable.getReturnType(methodName).getName())){
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Return type is incorrect"));
                }
            }
        }
        return new Type("null", false);
    }


    private Type dealWithClass(JmmNode jmmNode, String s) {
        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Method")){
                visit(child, "");
            }
        }
        return new Type("null", false);
    }


    private Type dealWithProgram(JmmNode jmmNode, String s) {
        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Class")){
                visit(child,"");
            }
        }
        return new Type("null", false);
    }






}
