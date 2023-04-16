package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2023.OllirAuxFunctions.nextTemp;

public class OllirGenerator extends AJmmVisitor<String, OllirCodeStruct> {
    private final SymbolTable symbolTable;
    private final StringBuilder codeOllir;

    public OllirGenerator(SymbolTable symbolTable) {
        this.codeOllir = new StringBuilder();
        this.symbolTable = symbolTable;
        buildVisitor();
    }


    private OllirCodeStruct dealWithProgram(JmmNode program, String aux) {
        for(String imports: symbolTable.getImports()){
            codeOllir.append("import ").append(imports).append(";\n");
        }
        for(JmmNode child: program.getChildren()){
            if(!child.getKind().equals("Import")){
                visit(child);
            }
        }
        codeOllir.append("}");
        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithClass(JmmNode classNode, String aux) {
        codeOllir.append(symbolTable.getClassName());
        if (symbolTable.getSuper() != null) {
            codeOllir.append(" extends ").append(symbolTable.getSuper());
        }
        codeOllir.append(" {\n");
        boolean constructor = false;
        for (JmmNode child : classNode.getChildren()) {
            if (!child.getKind().equals("Declaration") && !constructor) {
                codeOllir.append(emptyConstructor());
                constructor = true;
            }
            visit(child, aux);
        }
        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithMethod(JmmNode method, String aux) {
        String methodName = method.get("methodName");
        codeOllir.append(".method public");
        if(methodName.equals("main")) codeOllir.append(" static");
        codeOllir.append(" ").append(methodName).append("(");
        List<Symbol> parameters = symbolTable.getParameters(methodName);
        StringBuilder parametersCode = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Symbol parameter = parameters.get(i);
            parametersCode.append(parameter.getName()).append(".").append(OllirAuxFunctions.getCode(parameter.getType()));
            if (i < parameters.size() - 1) {
                parametersCode.append(", ");
            }
        }
        System.out.println(parametersCode);
        codeOllir.append(parametersCode).append(").").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName))).append(" {\n");


        int childIndex = 0;
        for(JmmNode child: method.getChildren()){
            if(method.getChildren().size() -1 == childIndex){
                dealWithReturn(child, methodName);
                break;
            }
            if(!child.getKind().equals("Type")) visit(child, methodName);
            childIndex++;
        }
        if(symbolTable.getReturnType(methodName).getName().equals("void")) codeOllir.append("ret.V;\n");
        codeOllir.append("}\n");
        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithDeclaration(JmmNode jmmNode, String aux) {
            if(!jmmNode.getJmmParent().getKind().equals("Class")) {
                return new OllirCodeStruct();
            }

            Type type = new Type(jmmNode.getJmmChild(0).get("name"), jmmNode.getJmmChild(0).get("isArray").equals("true"));
            codeOllir.append(".field private ").append(OllirAuxFunctions.getCode(new Symbol(type, jmmNode.get("value")))).append(";\n");
            return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithReturn(JmmNode returnNode, String methodName) {
            if(returnNode.getKind().equals("BinaryOp")){
                OllirCodeStruct code = visit(returnNode, methodName);
                codeOllir.append(code.prefixCode);
                codeOllir.append("ret.").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName))).append(" ");
                codeOllir.append(code.value);
                if(!code.value.contains(".")) {
                    codeOllir.append(".")
                            .append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName)));
                }
                codeOllir.append(";\n");
            }else {
                OllirCodeStruct code = visit(returnNode, methodName);
                if(!symbolTable.getReturnType(methodName).getName().equals("void")) {
                    codeOllir.append("ret.").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName))).append(" ");
                    codeOllir.append(code.value).append(";\n");
                }else{
                    codeOllir.append(code.value);
                }
            }

        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithInteger(JmmNode nodeFinal, String aux) {
        StringBuilder code = new StringBuilder();
        code.append(nodeFinal.get("value")).append(".i32");
        return new OllirCodeStruct("", code.toString());
    }

    private OllirCodeStruct dealWithBoolean(JmmNode nodeFinal, String aux) {
        StringBuilder code = new StringBuilder();
        code.append(nodeFinal.get("value")).append(".bool");
        return new OllirCodeStruct("", code.toString());
    }

    private OllirCodeStruct dealWithIdentifier(JmmNode jmmNode, String methodName) {
        Type returnType = symbolTable.getReturnType(methodName);
        StringBuilder code = new StringBuilder();
        int index;
        if((index = getParameters(methodName, jmmNode.get("value"))) != -1){
            code.append("$").append(index).append(".");
            code.append(jmmNode.get("value")).append(".").append(OllirAuxFunctions.getCode(returnType));
            return new OllirCodeStruct("", code.toString());
        }else{
            JmmNode methodNode = jmmNode.getJmmParent();
            while(!methodNode.getKind().equals("Method")){
                methodNode = methodNode.getJmmParent();
            }
            for(JmmNode child: methodNode.getChildren()){
                if(child.getKind().equals("Declaration") && child.get("value").equals(jmmNode.get("value"))){
                    code.append(jmmNode.get("value")).append(".").append(OllirAuxFunctions.getCode(new Type(child.getJmmChild(0).get("name"), child.getJmmChild(0).get("isArray").equals("true"))));
                    return new OllirCodeStruct("", code.toString());
                }
            }
        }
        return new OllirCodeStruct("", jmmNode.get("value"));
    }

    private OllirCodeStruct dealWithAssignment(JmmNode assignment, String methodName) {
        StringBuilder code = new StringBuilder();
        OllirCodeStruct ollirCodeRhs = visit(assignment.getJmmChild(0), methodName);
        codeOllir.append(ollirCodeRhs.prefixCode);
        int index;
        if((index = getParameters(methodName, assignment.get("var"))) != -1){
            codeOllir.append("$").append(index).append(".");
        }
        String type = getType(assignment, methodName, assignment.get("var"));

        codeOllir.append(type).append(" :=.").append(type.split("\\.")[1]).append(" ").append(ollirCodeRhs.value);
        if(assignment.getJmmChild(0).getKind().equals("MethodCall")){
            codeOllir.append(".").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName)));
        }
        codeOllir.append(";\n");
        return new OllirCodeStruct(code.toString(), ollirCodeRhs.value);
    }

    private OllirCodeStruct dealWithBinaryOp(JmmNode jmmNode, String aux) {
        StringBuilder code = new StringBuilder();
        OllirCodeStruct ollirCodeLhs = visit(jmmNode.getJmmChild(0), aux);
        OllirCodeStruct ollirCodeRhs = visit(jmmNode.getJmmChild(1), aux);
        String operator = jmmNode.get("op") + ".i32";
        StringBuilder temp = new StringBuilder(nextTemp()).append(".i32");
        code.append(ollirCodeLhs.prefixCode);
        code.append(ollirCodeRhs.prefixCode);

        code.append(temp).append(" :=.i32 ").append(ollirCodeLhs.value).append(" ").append(operator).append(" ").append(ollirCodeRhs.value).append(";\n");


        return new OllirCodeStruct(code.toString(), temp.toString());
    }

    private OllirCodeStruct dealWithExprStmt(JmmNode ExprStmt, String methodName) {
        JmmNode methodCall = ExprStmt;
        if(ExprStmt.getJmmChild(0).getKind().equals("MethodCall")){
            methodCall = ExprStmt.getJmmChild(0);
        }
        JmmNode identifier = methodCall.getJmmChild(0);
        OllirCodeStruct args = new OllirCodeStruct("", "");
        if(methodCall.getChildren().size() > 1){
            JmmNode arguments = methodCall.getJmmChild(1);
            args = visit(arguments, methodName);
        }
        String identifierType = getType(identifier, methodName, identifier.get("value"));;

        String returnType = new String();
        StringBuilder code = new StringBuilder();
        if(!identifierType.equals("")){
            code.append("invokevirtual(").append(identifierType);
        }else{
            code.append("invokestatic(");
            code.append(identifier.get("value"));
            returnType = ".V";
        }
        code.append(", \"").append(methodCall.get("name")).append("\"");
        if(!args.value.equals("")){
            code.append(", ").append(args.value);
        }
        code.append(")").append(returnType);
        if(!ExprStmt.getJmmParent().getKind().equals("Assignment")){
            code.append(";\n");
            codeOllir.append(args.prefixCode);
            codeOllir.append(code.toString());
            return new OllirCodeStruct();
        }else{
            return new OllirCodeStruct(args.prefixCode, code.toString());
        }
    }



    public String getCode() {
        return codeOllir.toString();
    }

    private String emptyConstructor(){
        return ".construct " +
                symbolTable.getClassName() +
                "().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n\n";
    }

    private int getParameters(String methodName, String varName){
        int parameterNumber = 1;
        for (Symbol parameter : symbolTable.getParameters(methodName)) {
            if(parameter.getName().equals(varName))
                return parameterNumber;
            parameterNumber++;
        }
        return -1;
    }

    private String getType(JmmNode jmmNode, String methodName, String variableName){
        StringBuilder result = new StringBuilder();
        while(!jmmNode.getKind().equals("Method")){
            jmmNode = jmmNode.getJmmParent();
        }
        for(JmmNode child: jmmNode.getChildren()) {
            if (child.getKind().equals("Declaration") && child.get("value").equals(variableName)) {
                result.append(variableName).append(".").append(OllirAuxFunctions.getCode(new Type(child.getJmmChild(0).get("name"), child.getJmmChild(0).get("isArray").equals("true"))));
                return result.toString();
            }
        }
        int index;
        if((index = getParameters(methodName, variableName)) != -1){
            result.append("$").append(index).append(".").append(variableName).append(".").append(OllirAuxFunctions.getCode(new Type(jmmNode.getJmmChild(index).get("name"), jmmNode.getJmmChild(index).get("isArray").equals("true"))));
            return result.toString();
        }
        return "";

    }


    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
        addVisit("Declaration", this::dealWithDeclaration);
        //addVisit("Return", this::dealWithReturn);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBoolean);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ExprStmt", this::dealWithExprStmt);
        addVisit("MethodCall", this::dealWithExprStmt);
    }




}

