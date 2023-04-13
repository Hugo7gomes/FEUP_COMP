package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.MySymbolTable;
import pt.up.fe.comp2023.semantics.ExpressionAnalyser;


import java.util.List;

public class StatementAnalyser extends AJmmVisitor<String, Type> {

    private MySymbolTable symbolTable;
    private List<Report> reports;

    public StatementAnalyser(MySymbolTable table, List<Report> reports){
        this.symbolTable = table;
        this.reports = reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Stmt", this::dealWithStmt);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("IfElseStmt", this::dealConditionalStmt);
        addVisit("WhileStmt", this::dealConditionalStmt);
        addVisit("ArrayAssignment", this::dealWithArrayAssignment);
        addVisit("ExprStmt",this::dealWithExpression);
    }

    private Type dealWithExpression(JmmNode jmmNode, String s) {
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        return expressionAnalyser.visit(jmmNode.getJmmChild(0));
    }

    private Type dealWithArrayAssignment(JmmNode jmmNode, String s) {
        return null;
    }

    private Type dealWithStmt(JmmNode jmmNode, String s) {
        //Visit all children
        for(JmmNode child: jmmNode.getChildren()){
            visit(child);
        }
        return null;
    }

    //Deal with While and If conditional expression
    private Type dealConditionalStmt(JmmNode jmmNode, String s) {
        //Get conditional expression node
        JmmNode child = jmmNode.getJmmChild(0);
        //Create expressionAnalyser
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        //Visit node and get Type
        Type childType = expressionAnalyser.visit(child, "");
        //Checks if type is not boolean
        if(!childType.getName().equals("boolean")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Conditional expression needs to return a boolean" ));
            return new Type("error", false);
        }
        return childType;

    }


    private Type dealWithAssignment(JmmNode jmmNode, String s) {
        String var = jmmNode.get("var");
        //Get assignee expression node
        JmmNode child = jmmNode.getJmmChild(0);
        //Create expressionAnalyser
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        //Visit node and get Type
        Type childType = expressionAnalyser.visit(child, "");

        Type varType = new Type("", false);

        JmmNode parent = jmmNode.getJmmParent();
        while(!parent.getKind().equals("Method")){
            parent = parent.getJmmParent();
        }

        String methodName = parent.get("methodName");

        //Get fields
        List<Symbol> fields = symbolTable.getFields();
        //Check if var is a field
        if(fields != null){
            for(Symbol f: fields){
                if(f.getName().equals(var)){
                    varType = f.getType();
                    break;
                }
            }
        }

        //Get List of local variables
        List<Symbol> locals = symbolTable.getLocalVariables(methodName);
        //check if var is a local variable
        if(locals != null){
            for(Symbol l :locals){
                if(l.getName().equals(var)){
                    varType = l.getType();
                    break;
                }
            }
        }

        //Get List of parameters of the method
        List<Symbol> parameters  = symbolTable.getParameters(methodName);
        //check if var is a parameter
        if(parameters != null){
            for(Symbol p:parameters){
                if(p.getName().equals(var)){
                    varType = p.getType();
                }
            }
        }


        //Checks if varType equals Super class and child type equals current class
        if((varType.getName().equals(symbolTable.getSuper()) && childType.getName().equals(symbolTable.getClassName()))){
            return childType;
        }
        //Checks if both types are imported
        else if(symbolTable.getImports().contains(varType.getName()) && symbolTable.getImports().contains(childType.getName())){
            return childType;
        }
        //Checks if assignee and assigner have different types
        else if(!varType.getName().equals(childType.getName())){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Type of the assignee must be compatible with the assigned"));
            return new Type("errorType", false);
        }

        return childType;

    }
}
