package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;


import java.util.List;

public class StatementAnalyser extends PreorderJmmVisitor<String, Type> {

    private MySymbolTable symbolTable;
    private List<Report> reports;

    public StatementAnalyser(MySymbolTable table, List<Report> reports){
        this.symbolTable = table;
        this.reports = reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("IfElseStmt", this::dealConditionalStmt);
        addVisit("WhileStmt", this::dealConditionalStmt);
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
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Conditional expression needs to return a boolean" ));
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

        //Get fields
        List<Symbol> fields = symbolTable.getFields();
        //Check if var is a field
        for(Symbol f: fields){
            if(f.getName().equals(var)){
                varType = f.getType();
                break;
            }
        }

        JmmNode parent = jmmNode.getJmmParent();
        while(!parent.getKind().equals("MethodDeclaration")){
            parent = parent.getJmmParent();
        }

        //Get List of local variables
        List<Symbol> locals = symbolTable.getLocalVariables(parent.get("name"));
        //check if var is a local variable
        for(Symbol l :locals){
            if(l.getName().equals(var)){
                varType = l.getType();
                break;
            }
        }

        //Get List of parameters of the method
        List<Symbol> parameters  = symbolTable.getParameters(parent.get("name"));
        //check if var is a parameter
        for(Symbol p:locals){
            if(p.getName().equals(var)){
                varType = p.getType();
            }
        }

        if(!varType.getName().equals(childType.getName())){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Type of the assignee must be compatible with the assigned"));
            return new Type("error", false);
        }

        return childType;

    }
}
