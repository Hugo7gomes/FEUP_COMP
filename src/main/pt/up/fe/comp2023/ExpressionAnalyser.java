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
        addVisit("UnaryOp", this::dealWithUnaryOp);
        addVisit("NewIntArray", this::dealWithNewIntArray);
        addVisit("NewObject", this::dealWithNewObject);
        addVisit("MethodCall", this::dealWithMethodCall);

    }

    private Type dealWithMethodCall(JmmNode jmmNode, String s) {
        String methodName = jmmNode.get("name");
        JmmNode classCall = jmmNode.getJmmChild(0);
        Type classType = visit(classCall,"");

        //The class calling the method is the current class
        if(classType.getName().equals(symbolTable.getClassName())){
            //verify if method exist
            if(symbolTable.getMethods().contains(methodName)){
                //verify arguments type
                List<Symbol> methodParams = symbolTable.getParameters(methodName);
                for (int i = 1; i < jmmNode.getNumChildren(); i++){
                    Type argType = visit(jmmNode.getJmmChild(i),"");
                    // (i-1) because parameters index start at 0 and children that corresponds to arguments start at 1
                    if(!methodParams.get(i - 1).getType().equals(argType)){
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Error in argument"+ jmmNode.getJmmChild(i).get("name") + "type" ));
                        return new Type("error", false);
                    }
                }
            }//checks if current class extends a super class
            else if(!(symbolTable.getSuper() != null && symbolTable.getImports().contains(symbolTable.getSuper()))){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Method doesnt exist"));
                return new Type("error", false);
            }
        }else{
            //checks if class is imported assume method is being called correctly
            if(!symbolTable.getImports().contains(classType.getName())){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Class not imported"));
                return new Type("error", false);
            }
        }
        return symbolTable.getReturnType(methodName);

    }

    private Type dealWithNewObject(JmmNode jmmNode, String s) {
        return new Type(jmmNode.get("name"),false);
    }

    private Type dealWithNewIntArray(JmmNode jmmNode, String s) {
        JmmNode child =jmmNode.getJmmChild(0);
        Type childType = visit(child,"");

        if(!childType.getName().equals("int")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Array initialization requires integer size"));
        }
        return  new Type("int", true);
    }

    private Type dealWithUnaryOp(JmmNode jmmNode, String s) {
        JmmNode child =jmmNode.getJmmChild(0);
        Type childType = visit(child,"");

        //Checks if child type is boolean
        if(!childType.getName().equals("boolean")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), "Only boolean type can be used with not operator"));
            //return new Type("error", false);
        }

        return new Type("boolean",false);

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
