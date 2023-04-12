package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
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
        addVisit("Import", this::dealWithImport);
        addVisit("Declaration", this::dealWithDeclaration);
        addVisit("Method", this::dealWithMethod);
    }

    private Type dealWithType(JmmNode jmmNode, String s) {
        return new Type("null", false);
    }

    private Type dealWithMethod(JmmNode jmmNode, String s) {
        for(JmmNode child: jmmNode.getChildren()){
            switch (child.getKind()){
                case "Stmt":
                case "IfElseStmt":
                case "WhileStmt":
                case "ExprStmt":
                case "Assignment":
                case "ArrayAssignment":
                    StatementAnalyser statementAnalyser = new StatementAnalyser(symbolTable,reports);
                    statementAnalyser.visit(child, "");
                    break;
                case "Parenthesis":
                case "Indexing":
                case "Length":
                case "MethodCall":
                case "UnaryOp":
                case "BinaryOp":
                case "NewIntArray":
                case "NewObject":
                case "Integer":
                case "Boolean":
                case "Identifier":
                case "This":
                    ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
                    Type type = expressionAnalyser.visit(child, "");
                    break;
            }
        }
        return new Type("null", false);
    }

    private Type dealWithDeclaration(JmmNode jmmNode, String s) {
        visit(jmmNode.getJmmChild(0), "");
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

    private Type dealWithImport(JmmNode jmmNode, String s) {
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
