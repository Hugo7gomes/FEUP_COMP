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
        addVisit("IfElseStmt", this::dealIfConditionalStmt);
        addVisit("WhileStmt", this::dealWhileConditionalStmt);
        addVisit("ArrayAssignment", this::dealWithArrayAssignment);
        addVisit("ExprStmt",this::dealWithExpression);

    }


    private Type dealWithExpression(JmmNode jmmNode, String s) {
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        return expressionAnalyser.visit(jmmNode.getJmmChild(0));
    }

    private Type dealWithArrayAssignment(JmmNode jmmNode, String s) {
        String var = jmmNode.get("var");
        JmmNode childIndex = jmmNode.getJmmChild(0);
        JmmNode childAssignment = jmmNode.getJmmChild(1);

        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        //Get Index type
        Type indexType = expressionAnalyser.visit(childIndex);
        //Get assignment
        Type assignmentType = expressionAnalyser.visit(childAssignment);

        Type varType = new Type("", false);
        //Var type
        JmmNode parent = jmmNode.getJmmParent();
        while(!parent.getKind().equals("Method")){
            parent = parent.getJmmParent();
        }

        String methodName = parent.get("methodName");

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
        if(parameters != null && varType.getName().equals("")){
            for(Symbol p:parameters){
                if(p.getName().equals(var)){
                    varType = p.getType();
                }
            }
        }

        //Get fields
        List<Symbol> fields = symbolTable.getFields();
        //Check if var is a field
        if(fields != null &&  varType.getName().equals("")){
            for(Symbol f: fields){
                if(f.getName().equals(var)){
                    if(methodName.equals("main")){
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Fields cannot be used inside static method main"));
                    }
                    varType = f.getType();
                    break;
                }
            }
        }


        //Checks if variable is an array
        if(!varType.isArray()){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Array assignment variable not array"));
        }
        //Checks if index is an integer
        if(!indexType.getName().equals("int")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Index needs to be an integer"));
        }
        //Checks if assignee is an integer
        if(!assignmentType.getName().equals("int")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Assignment needs to be an integer"));
        }

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
    private Type dealWhileConditionalStmt(JmmNode jmmNode, String s) {
        //Get conditional expression node
        JmmNode child = jmmNode.getJmmChild(0);
        //Create expressionAnalyser
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        //Visit node and get Type
        Type childType = expressionAnalyser.visit(child, "");
        //Checks if type is not boolean
        if(!childType.getName().equals("boolean")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Conditional expression needs to return a boolean" ));
        }
        //Visit statement inside while
        visit(jmmNode.getJmmChild(1));
        return childType;

    }

    private Type dealIfConditionalStmt(JmmNode jmmNode, String s) {
        //Get conditional expression node
        JmmNode child = jmmNode.getJmmChild(0);
        //Create expressionAnalyser
        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        //Visit node and get Type
        Type childType = expressionAnalyser.visit(child, "");
        //Checks if type is not boolean
        if(!childType.getName().equals("boolean")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Conditional expression needs to return a boolean" ));
        }
        //Visit statement inside if
        visit(jmmNode.getJmmChild(1));
        //visit statement inside else
        visit(jmmNode.getJmmChild(2));
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
        if(parameters != null && varType.getName().equals("")){
            for(Symbol p:parameters){
                if(p.getName().equals(var)){
                    varType = p.getType();
                }
            }
        }

        //Get fields
        List<Symbol> fields = symbolTable.getFields();
        //Check if var is a field
        if(fields != null &&  varType.getName().equals("")){
            for(Symbol f: fields){
                if(f.getName().equals(var)){
                    if(methodName.equals("main")){
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Fields cannot be used inside static method main"));
                    }
                    varType = f.getType();
                    break;
                }
            }
        }

        System.out.println("VarType" + varType);
        System.out.println("Child Type" + childType);
        //Checks if varType equals Super class and child type equals current class
        if((varType.getName().equals(symbolTable.getSuper()) && childType.getName().equals(symbolTable.getClassName()))){
            return childType;
        }
        //Checks if both types are imported
        else if(symbolTable.getImports().contains(varType.getName()) && symbolTable.getImports().contains(childType.getName())){
            return childType;
        //Checks if
        }else if(childType.getName().equals("importCorrect")){
            return childType;
        }
        //Checks if assignee and assigner have different types
        else if(!varType.getName().equals(childType.getName())){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Type of the assignee must be compatible with the assigned"));
            return new Type("errorType", false);
        }

        return varType;

    }
}
