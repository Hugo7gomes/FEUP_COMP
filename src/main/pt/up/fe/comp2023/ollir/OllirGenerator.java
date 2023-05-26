package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;


import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2023.ollir.OllirAuxFunctions.*;

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
                codeOllir.append(".").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName)));
            }
            codeOllir.append(";\n");
        }else if(returnNode.getKind().equals("This")){
            String returnType = OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName));
            codeOllir.append("ret.").append(returnType).append(" this.").append(returnType).append(";\n");

        }else {
            OllirCodeStruct code = visit(returnNode, methodName);
            if(!symbolTable.getReturnType(methodName).getName().equals("void")) {
                codeOllir.append(code.prefixCode);
                codeOllir.append("ret.").append(OllirAuxFunctions.getCode(symbolTable.getReturnType(methodName))).append(" ");
                codeOllir.append(code.value).append(";\n");
            }else{
                codeOllir.append(code.prefixCode);
                //codeOllir.append(code.value);
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
        String code;
        code = getType(jmmNode, methodName, jmmNode.get("value"));
        OllirCodeStruct ollirCodeStruct = isField(jmmNode, jmmNode.get("value"));
        if(!ollirCodeStruct.value.equals("") && code.equals("")){
            return ollirCodeStruct;
        }

        return new OllirCodeStruct("", code);
    }

    private OllirCodeStruct dealWithAssignment(JmmNode assignment, String methodName) {
        StringBuilder code = new StringBuilder();
        OllirCodeStruct ollirCodeField = isField(assignment, assignment.get("var"));
        String type = getType(assignment, methodName, assignment.get("var"));
        if(!ollirCodeField.value.equals("") && type.equals("")){
            OllirCodeStruct assignmentChild = visit(assignment.getJmmChild(0), methodName);
            type = extractType(ollirCodeField.value);
            codeOllir.append(assignmentChild.prefixCode);
            codeOllir.append("putfield(this, ").append(assignment.get("var")).append(".").append(type).append(", ").append(assignmentChild.value).append(").V;\n");
            return new OllirCodeStruct();
        }
        OllirCodeStruct ollirCodeRhs = visit(assignment.getJmmChild(0), methodName);
        codeOllir.append(ollirCodeRhs.prefixCode);
        String varType = extractType(type);

        if(assignment.getJmmChild(0).getKind().equals("NewObject")){
            codeOllir.append(type).append(" :=.").append(varType).append(" ").append(ollirCodeRhs.value).append(";\n");
            return new OllirCodeStruct(code.toString(), ollirCodeRhs.value);
        }
        codeOllir.append(type).append(" :=.").append(varType).append(" ").append(ollirCodeRhs.value).append(";\n");
        return new OllirCodeStruct(code.toString(), ollirCodeRhs.value);
    }

    private OllirCodeStruct dealWithBinaryOp(JmmNode jmmNode, String aux) {

        StringBuilder code = new StringBuilder();
        OllirCodeStruct ollirCodeLhs = visit(jmmNode.getJmmChild(0), aux);
        OllirCodeStruct ollirCodeRhs = visit(jmmNode.getJmmChild(1), aux);
        String type = operatorType(jmmNode.get("op"));
        String operator;
        if(jmmNode.getJmmParent().getKind().equals("WhileStmt")){
            operator = oppositeOperator(jmmNode.get("op")) + type;
        }else{
            operator = jmmNode.get("op") + type;
        }



        StringBuilder temp = new StringBuilder(nextTemp()).append(type);
        code.append(ollirCodeLhs.prefixCode);
        code.append(ollirCodeRhs.prefixCode);

        code.append(temp).append(" :=").append(type).append(" ").append(ollirCodeLhs.value).append(" ").append(operator).append(" ").append(ollirCodeRhs.value).append(";\n");


        return new OllirCodeStruct(code.toString(), temp.toString());
    }

    private OllirCodeStruct dealWithExprStmt(JmmNode ExprStmt, String methodName){
        OllirCodeStruct code = visit(ExprStmt.getJmmChild(0), methodName);
        codeOllir.append(code.prefixCode);
        codeOllir.append(code.value);
        return new OllirCodeStruct("", code.value);
    }

    private OllirCodeStruct dealWithMethodCall(JmmNode methodCall, String methodName) {
        StringBuilder code = new StringBuilder();
        StringBuilder prefixCode = new StringBuilder();
        OllirCodeStruct childCode = new OllirCodeStruct("", "");
        if (!methodCall.getJmmChild(0).getKind().equals("Identifier")) {
            childCode = visit(methodCall.getJmmChild(0), methodName);
        }
        OllirCodeStruct args = new OllirCodeStruct("", "");
        List<String> argsList = new ArrayList<>();
        if (methodCall.getChildren().size() > 1) {
            for (int i = 1; i < methodCall.getChildren().size(); i++) {
                JmmNode arguments = methodCall.getJmmChild(i);
                args = visit(arguments, methodName);
                prefixCode.append(args.prefixCode);
                argsList.add(args.value);
            }
            args.prefixCode = prefixCode.toString();
        }
        String identifierType;

        if (methodCall.getJmmChild(0).getKind().equals("Identifier")) {
           identifierType=  visit(methodCall.getJmmChild(0), methodName).value;
        }else{
            identifierType = childCode.value;
        }

        String returnType = "";


        if(!identifierType.equals("")){
            if(methodCall.getJmmChild(0).getKind().equals("Identifier")){
                returnType = "." + childClass(methodCall, methodCall.get("name"));
            }else{
                returnType = "." + extractType(childCode.value);
            }
            code.append("invokevirtual(").append(identifierType);

        }else{
            returnType = ".V";
            code.append("invokestatic(");
            code.append(methodCall.getJmmChild(0).get("value"));
        }

        code.append(", \"").append(methodCall.get("name")).append("\"");
        if(!args.value.equals("")){
            for(int i = 0; i < argsList.size(); i++){
                code.append(", ").append(argsList.get(i));
            }
        }
        code.append(")");
        if(!methodCall.getJmmParent().getKind().equals("Assignment")){

            if(methodCall.getJmmParent().getKind().equals("Indexing")){
                code.append(returnType);
                codeOllir.append(args.prefixCode);
                return new OllirCodeStruct("", code.toString());
            }

            if((!methodCall.getJmmParent().getKind().equals("ExprStmt")) && !returnType.equals(".V")){
                code.append(returnType).append(";\n");;
                String temp = nextTemp();
                childCode.value = temp + returnType;
                args.prefixCode = childCode.value +  " :=" + returnType + " " + code;
            }else{

                if(!returnType.equals(".V")){
                    returnType = "." + getTypeOllir(symbolTable.getReturnType(methodCall.get("name")).getName());
                }
                code.append(returnType).append(";\n");
                childCode.value = code.toString();
            }

            return new OllirCodeStruct(childCode.prefixCode + args.prefixCode, childCode.value);
        }

        JmmNode assignment = methodCall.getJmmParent();
        returnType = getType(assignment, methodName, assignment.get("var"));
        code.append(".").append(extractType(returnType));
        return new OllirCodeStruct(args.prefixCode, code.toString());
    }



    private OllirCodeStruct dealWithNewObject(JmmNode jmmNode, String methodName) {
        StringBuilder code = new StringBuilder();
        String temp = nextTemp() + "." + jmmNode.get("name");
        code.append(temp).append(" :=.").append(jmmNode.get("name"));
        code.append(" new(").append(jmmNode.get("name")).append(").").append(jmmNode.get("name")).append(";\n");
        code.append("invokespecial(").append(temp).append(", \"<init>\").V;\n");

        return new OllirCodeStruct(code.toString(), temp);
    }

    private OllirCodeStruct dealWithNewIntArray(JmmNode jmmNode, String s) {
        StringBuilder code = new StringBuilder();
        String typeParent = getType(jmmNode.getJmmParent(), s, jmmNode.getJmmParent().get("var"));
        String type =  "." + extractType(typeParent);
        StringBuilder prefixCode = new StringBuilder();
        String temp = nextTemp();
        String tempType = ".i32";
        OllirCodeStruct ollirCodeStruct = visit(jmmNode.getJmmChild(0), s);
        prefixCode.append(temp).append(tempType).append(" :=").append(tempType).append(" ").append(ollirCodeStruct.value).append(";\n");
        code.append("new(array, ").append(temp).append(tempType).append(")").append(type);
        return new OllirCodeStruct(prefixCode.toString(), code.toString());
    }

    private OllirCodeStruct dealWithIfElseStmt(JmmNode jmmNode, String s) {
        OllirCodeStruct condition = visit(jmmNode.getJmmChild(0), s);
        String thenJump = "THEN" +nextJump();
        String elseJump = "ENDIF" + nextJump();
        codeOllir.append(condition.prefixCode);
        codeOllir.append("if (")
                .append(condition.value)
                .append(") goto ").append(thenJump).append(";\n");

        OllirCodeStruct elseStmt = visit(jmmNode.getJmmChild(2), s);
        codeOllir.append(elseStmt.prefixCode);
        codeOllir.append("goto ")
                .append(elseJump)
                .append(";\n")
                .append(thenJump)
                .append(":\n");

        visit(jmmNode.getJmmChild(1), s);
        codeOllir.append(elseStmt.prefixCode);

        codeOllir.append(elseJump)
                .append(":\n");

        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithStmt(JmmNode jmmNode, String s) {
        for(JmmNode child : jmmNode.getChildren()){
            visit(child, s);
        }
        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithLength(JmmNode jmmNode, String s) {
        StringBuilder code = new StringBuilder();
        OllirCodeStruct identifier = visit(jmmNode.getJmmChild(0), s);
        String temp = nextTemp() + ".i32";
        code.append(temp)
                .append(":=.i32 arraylength(")
                .append(identifier.value)
                .append(").i32;\n");

        return new OllirCodeStruct(code.toString(), temp);
    }

    private OllirCodeStruct dealWithArrayAssignment(JmmNode jmmNode, String s) {
        OllirCodeStruct index = visit(jmmNode.getJmmChild(0), s);
        OllirCodeStruct value = visit(jmmNode.getJmmChild(1), s);
        String type = extractType(value.value);
        String nextTemp = nextTemp() + "." + type;
        codeOllir.append(nextTemp)
                .append(" :=.")
                .append(type)
                .append(" ")
                .append(index.value).append(";\n");

        codeOllir.append(jmmNode.get("var"))
                .append("[")
                .append(nextTemp)
                .append("].")
                .append(type)
                .append(" :=.")
                .append(type)
                .append(" ")
                .append(value.value)
                .append(";\n");

        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithIndexing(JmmNode jmmNode, String s) {
        OllirCodeStruct array = visit(jmmNode.getJmmChild(1), s);
        String tempArray = nextTemp() + ".i32";
        codeOllir.append(array.prefixCode);
        codeOllir.append(tempArray)
                .append(" :=.i32 ")
                .append(array.value)
                .append(";\n");
        OllirCodeStruct index = visit(jmmNode.getJmmChild(0), s);
        codeOllir.append(index.prefixCode);
        String type = "." + extractType(index.value).split("\\.")[1];
        String tempIndex = nextTemp() + type;
        codeOllir.append(tempIndex)
                .append(" :=")
                .append(type).append(" ");
        if(index.value.contains("$")){
            codeOllir.append(index.value.split("\\.")[1]);
        }else{
            codeOllir.append(index.value.split("\\.")[0]);
        }
        codeOllir.append("[")
                .append(tempArray)
                .append("]")
                .append(type)
                .append(";\n");

        return new OllirCodeStruct("", tempIndex);
    }

    private OllirCodeStruct dealWithWhileStmt(JmmNode jmmNode, String s) {
        OllirCodeStruct condition = visit(jmmNode.getJmmChild(0), s);
        String whileJump = "WHILE" + nextJump();
        String endWhileJump = "ENDWHILE" + nextJump();
        codeOllir.append(condition.prefixCode);
        codeOllir.append(whileJump)
                .append(":\n")
                .append("if (")
                .append(condition.value)
                .append(") goto ").append(endWhileJump).append(";\n");

        visit(jmmNode.getJmmChild(1), s);
        codeOllir.append("goto ")
                .append(whileJump)
                .append(";\n")
                .append(endWhileJump)
                .append(":\n");
        return new OllirCodeStruct();
    }

    private OllirCodeStruct dealWithThis(JmmNode jmmNode, String s) {
        return new OllirCodeStruct("", "this");
    }

    private OllirCodeStruct dealWithParenthesis(JmmNode jmmNode, String s) {
        return  visit(jmmNode.getJmmChild(0), s);

    }

    private OllirCodeStruct dealWithUnaryOp(JmmNode jmmNode, String s) {
        OllirCodeStruct child = visit(jmmNode.getJmmChild(0), s);
        codeOllir.append(child.prefixCode);
        String temp = nextTemp() + ".bool";
        codeOllir.append(temp)
                .append(" :=.")
                .append("bool")
                .append(" ")
                .append(jmmNode.get("op"))
                .append(".bool")
                .append(" ")
                .append(child.value)
                .append(";\n");
        return new OllirCodeStruct("", temp);
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

    private String extractType(String type){
        String [] typeSplitted = type.split("\\.");
        if(type.contains("array")){
            return typeSplitted[typeSplitted.length - 2] + "." + typeSplitted[typeSplitted.length - 1];
        }
        return typeSplitted[typeSplitted.length -1];
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

    private String childClass(JmmNode jmmNode, String methodName) {
        while(!jmmNode.getKind().equals("Class")){
            jmmNode = jmmNode.getJmmParent();
        }
        for(JmmNode child: jmmNode.getChildren()) {
            if (child.getKind().equals("Method") && child.get("methodName").equals(methodName)) {
                return OllirAuxFunctions.getCode(symbolTable.getReturnType(child.get("methodName")));
            }
        }
        return "";
    }

    private OllirCodeStruct isField(JmmNode jmmNode, String variableName){
        while(!jmmNode.getKind().equals("Class")){
            jmmNode = jmmNode.getJmmParent();
        }
        for(JmmNode child: jmmNode.getChildren()) {
            if (child.getKind().equals("Declaration") && child.get("value").equals(variableName)) {
                String temp = nextTemp();
                StringBuilder prefixCode = new StringBuilder();
                String type = OllirAuxFunctions.getCode(new Type(child.getJmmChild(0).get("name"), child.getJmmChild(0).get("isArray").equals("true")));
                prefixCode.append(temp).append(".").append(type).append(" :=.").append(type).append(" getfield(this, ").append(variableName).append(".").append(type).append(").").append(type).append(";\n");
                return new OllirCodeStruct(prefixCode.toString(), temp + "." + type);
            }
        }
        return new OllirCodeStruct();
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
        addVisit("Declaration", this::dealWithDeclaration);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBoolean);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ExprStmt", this::dealWithExprStmt);
        addVisit("MethodCall", this::dealWithMethodCall);
        addVisit("NewObject", this::dealWithNewObject);
        addVisit("NewIntArray", this::dealWithNewIntArray);
        addVisit("IfElseStmt", this::dealWithIfElseStmt);
        addVisit("Stmt", this::dealWithStmt);
        addVisit("Length", this::dealWithLength);
        addVisit("ArrayAssignment", this::dealWithArrayAssignment);
        addVisit("Indexing", this::dealWithIndexing);
        addVisit("WhileStmt", this::dealWithWhileStmt);
        addVisit("This", this::dealWithThis);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("UnaryOp", this::dealWithUnaryOp);
    }
}

