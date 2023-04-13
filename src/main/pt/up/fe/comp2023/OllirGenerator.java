package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;


import java.util.List;

public class OllirGenerator extends AJmmVisitor<Integer, String> {
    private final SymbolTable symbolTable;
    private final StringBuilder codeOllir;

    public OllirGenerator(SymbolTable symbolTable) {

        this.codeOllir = new StringBuilder();
        this.symbolTable = symbolTable;
        buildVisitor();
    }

    private String dealWithProgram(JmmNode program, Integer aux) {
        for(String imports: symbolTable.getImports()){
            codeOllir.append("import ").append(imports).append(";\n");
        }
        System.out.println(codeOllir.toString());
        for(JmmNode child: program.getChildren()){
            if(!child.getKind().equals("Import")){
                visit(child);
            }
        }
        codeOllir.append("}");
        return "";
    }

/*    private Integer dealWithImport(JmmNode importNode, Integer aux) {
        for(String imports: symbolTable.getImports()){
            codeOllir.append("import ").append(imports).append(";\n");
        }
        return 0;
    }*/

    private String dealWithClass(JmmNode classNode, Integer aux) {
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
        return "";
    }

    private String emptyConstructor(){
        final String emptyConstructor = ".construct " +
                symbolTable.getClassName() +
                "().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n\n";
        return emptyConstructor;
    }


    private String dealWithMethod(JmmNode method, Integer integer) {
        String methodName = method.get("methodName");
        codeOllir.append(".method public");
        if(methodName.equals("main")) codeOllir.append(" static");
        codeOllir.append(" ").append(methodName).append("(");
        List<Symbol> parameters = symbolTable.getParameters(methodName);
        StringBuilder parametersCode = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Symbol parameter = parameters.get(i);
            parametersCode.append(parameter.getName()).append(".").append(getCode(parameter.getType()));
            if (i < parameters.size() - 1) {
                parametersCode.append(", ");
            }
        }
        System.out.println(parametersCode);
        codeOllir.append(parametersCode).append(").").append(getCode(symbolTable.getReturnType(methodName))).append(" {\n");

        for(JmmNode child: method.getChildren()){
            if(!child.getKind().equals("Type")) visit(child, integer);
        }
        if(methodName.equals("main")) codeOllir.append("ret.V;\n");
        codeOllir.append("}\n");
        return "";
    }

    private String dealWithDeclaration(JmmNode jmmNode, Integer integer) {
            System.out.println(jmmNode.getJmmParent());
            if(!jmmNode.getJmmParent().getKind().equals("Class")) {
                return "";
            }
            codeOllir.append(".field private ");
            String fieldName = jmmNode.get("value");
            String typeName = jmmNode.getChildren().get(0).get("name");
            codeOllir.append(fieldName).append(".").append(getTypeOllir(typeName)).append(";\n");
            System.out.println(codeOllir.toString());
            return "";
    }

    private String dealWithReturn(JmmNode jmmNode, Integer integer) {
            JmmNode returnNode = jmmNode.getChildren().get(0);
            System.out.println(jmmNode);
            Type returnType = getType(returnNode);
            if(returnType.getName().equals("int")){
                dealWithInteger(returnNode, integer);
            }else if(returnType.getName().equals("boolean")) {
                dealWithBoolean(returnNode, integer);
            }else{
                dealWithIdentifier(jmmNode, integer);
            }

        return "";
    }

    private String dealWithInteger(JmmNode nodeFinal, Integer integer) {
        if(nodeFinal.getKind().equals("Integer")){
            codeOllir.append("ret.i32 ").append(nodeFinal.get("value")).append(".i32;\n");
        }
        return "";
    }


    private String dealWithBoolean(JmmNode nodeFinal, Integer integer) {
        if(nodeFinal.getKind().equals("Boolean")){
            codeOllir.append("ret.bool ").append(nodeFinal.get("value")).append(".bool;\n");
        }
        return "";
    }

    private String dealWithIdentifier(JmmNode jmmNode, Integer integer) {
        Type returnType = symbolTable.getReturnType(jmmNode.getJmmParent().get("methodName"));
        codeOllir.append("ret.").append(getCode(returnType)).append(" ").append(jmmNode.get("value")).append(".").append(getCode(returnType)).append(";\n");
        return "";
    }

    public String getCode() {
        return codeOllir.toString();
    }


    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        //addVisit("Import", this::dealWithImport);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
        addVisit("Declaration", this::dealWithDeclaration);
        addVisit("Return", this::dealWithReturn);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBoolean);
        addVisit("Identifier", this::dealWithIdentifier);
    }




    //alterar isto, colocar no ficheiro auxiliar, mas quando coloco nao funciona, nao sei porquê


    public static String getCode(Symbol symbol){
        return symbol.getName() + "." + getCode(symbol.getType());
    }

    public static String getCode(Type type){
        StringBuilder code = new StringBuilder();
        if(type.isArray()){
            code.append("array.");
        }
        String ollirName = getTypeOllir(type.getName());
        code.append(ollirName);
        return code.toString();
    }

    public static String getTypeOllir(String type){
        switch (type){
            case "int":
                return "i32";
            case "void":
                return "V";
            case "boolean":
                return "bool";
            default:
                return type;
        }
    }

    public static String getCode(String value, Type type){
        return value + "." + getCode(type);
    }




    public static Type getType(JmmNode node) {
        return new Type(node.getKind(), node.getAttributes().contains("isArray") && node.get("isArray").equals("true"));
    }


}

