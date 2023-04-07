package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;


import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ExpressionAnalyser extends PreorderJmmVisitor<String, Type> {

    private MySymbolTable symbolTable;
    private List<Report> reports;


    public ExpressionAnalyser(MySymbolTable table, List<Report> reports){
        this.symbolTable = table;
        this.reports = reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("BinaryOp",this::dealWithBinaryOp);
        addVisit("This", this::dealWithThis);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("Boolean",this::dealWithBoolean);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Length", this::dealWithLengthMethod);
        addVisit("Indexing", this::dealWithArrayIndexing);
        addVisit("Parenthesis", this::dealWithParenthesis);

    }

    //Deal with parenthesis
    private Type dealWithParenthesis(JmmNode jmmNode, String s) {
        //return type of child
        return visit(jmmNode.getJmmChild(0),"");
    }

    //Deal with array indexing
    private Type dealWithArrayIndexing(JmmNode jmmNode, String s) {
        JmmNode leftChild = jmmNode.getJmmChild(0);
        JmmNode rightChild = jmmNode.getJmmChild(1);

        Type leftType = visit(leftChild, "");
        Type rightType = visit(rightChild,"");

        //Check if leftChild is not an array
        if(!leftType.isArray()){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Indexing error, " + leftChild.get("name") + " is not an array"));
            return new Type("error", false);
        }

        //Check if type of index is not int
        if(rightType.getName() != "int"){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Indexing error,index is not int"));
            return new Type("error", false);
        }

        return new Type("int", false);
    }

    //Deal with length Method
    private Type dealWithLengthMethod(JmmNode jmmNode, String s) {
        JmmNode leftChild = jmmNode.getJmmChild(0);
        //visit LeftChild
        Type leftChildType = visit(leftChild,"");

        //Check if leftChild is not an array
        if(!leftChildType.isArray()){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "LengthMethod error, " + leftChild.getJmmChild(0).get("name") + " is not an array"));
            return new Type("error", false);
        }
        return new Type("int", false);
    }

    private Type dealWithInteger(JmmNode jmmNode, String s) {
        return new Type("int", false);
    }

    private Type dealWithBoolean(JmmNode jmmNode, String s) {
        return new Type("boolean", false);
    }

    //Deal with Identifier
    private Type dealWithIdentifier(JmmNode jmmNode, String s) {
        //Get identifier name
        String name = jmmNode.get("value");
        //Get parent
        JmmNode parent = jmmNode.getJmmParent();

        //Get parent until find a methodDeclaration or ImportDeclaration
        while (!parent.getKind().equals("methodDeclaration") && !parent.getKind().equals("importDeclaration")){
            parent = parent.getJmmParent();
        }

        if(parent.getKind().equals("methodDeclaration")){
            String methodName = parent.get("name");

            //Get local variables from method
            List<Symbol> locals = symbolTable.getLocalVariables("methodName");
            //Check if identifier is a local variables
            for (Symbol l : locals){
                if(l.getName() == name){
                    return l.getType();
                }
            }

            //Get params from method
            List<Symbol> parameters  = symbolTable.getParameters("methodName");
            //Check if identifier is a parameter
            for (Symbol p: parameters){
                if(p.getName() == name){
                    return p.getType();
                }
            }

            //Get fields from class
            List<Symbol> fields = symbolTable.getFields();
            //Check if identifier is a field
            for(Symbol f:fields){
                if(f.getName() == name){
                    //Do I need to verify if the method is main? Fields cannot be used in main method?
                    return  f.getType();
                }
            }

            //No imports or Variable not in the imports.
            //Do I need to check superClassName ??
            if(symbolTable.getImports() == null || !symbolTable.getImports().contains(name)){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Variable " + name + " not declared"));
            }
        }
        //Dummy return
        return new Type("error", false);
    }

    private Type dealWithThis(JmmNode jmmNode, String s) {
        JmmNode parent = jmmNode.getJmmParent();

        while(!parent.getKind().equals("methodDeclaration") && !parent.getKind().equals("importDeclaration")){
            parent  = parent.getJmmParent();
        }

        if(parent.getKind().equals("methodDeclaration")){
            if(parent.get("methodName") == "main"){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "This cannot be used in main method"));
            }
        }

        return new Type(this.symbolTable.getClassName(),false);
    }


    private Type dealWithBinaryOp(JmmNode jmmNode, String s) {
        JmmNode leftChild = jmmNode.getChildren().get(0);
        JmmNode rightChild = jmmNode.getChildren().get(1);

        String op = jmmNode.get("op");

        Type leftType= visit(leftChild,"");
        Type rightType = visit(rightChild, "");

        //Check different types
        if(leftType.getName() != rightType.getName()){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Operands have different types in " + op + "operation" ));
        }//check if left or right is an array
        else if(leftType.isArray() || rightType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Array cannot be used in " + op + "operation"));
        }//check if leftType is not int in + - / * operations
        else  if (!leftType.getName().equals("int") && (op.equals("+") || op.equals("-") || op.equals("/") || op.equals("*") || op.equals("<"))){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Operation" + op + "cant support" + leftType.getName() + "type, expected integer"));
        }//check if leftType is not boolean in && operations
        else if(!leftType.getName().equals("boolean") && (op.equals("&&"))){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Operation" + op + "cant support" + leftType.getName() + "type, expected boolean"));
        }else{
            switch (op) {
                case "+", "-", "/", "*" -> {
                    return new Type("int", false);
                }
                case "<" , "&&" -> {
                    return  new Type("boolean",false);
                }
            }
        }
        //O que tenho que retornar em caso de nao erro???
        return new Type(leftType.getName(), leftType.isArray());

    }


}
