package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MethodVisitor extends AJmmVisitor<Void,Void> {

    private final List<String> classMethods = new ArrayList<String>();

    private final Map<String, List<Symbol>> methodsParams = new HashMap<>();

    private final Map<String, Type> methodsReturns = new HashMap<>();

    private final Map<String, List<Symbol>> localVariables = new HashMap<>();

    public List<String> getClassMethods() {
        return classMethods;
    }

    public Map<String, List<Symbol>> getMethodsParams(){
        return methodsParams;
    }

    public Map<String, Type> getMethodsReturns() {
        return methodsReturns;
    }

    public Map<String, List<Symbol>> getLocalVariables() {
        return localVariables;
    }

    protected void buildVisitor() {
        addVisit ("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
    }

    private Void dealWithProgram(JmmNode jmmNode, Void v) {
        for(JmmNode child: jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "Class")) {
                visit(child, null);
                return null;
            }
        }
        return null;
    }

    private Void dealWithClass (JmmNode jmmNode, Void v) {

        for(JmmNode child: jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "Method")) {
                visit(child, null);
            }
        }
        return null;
    }

    private Void addLocalVariable(JmmNode node, List<Symbol> localVars ){
        String name = node.get("value");
        String typeName = node.getChildren().get(0).get("name");
        Boolean isArray = (Boolean) node.getChildren().get(0).getObject("isArray");
        Type localType = new Type(typeName, isArray);
        Symbol symbol = new Symbol(localType, name);
        localVars.add(symbol);
        return null;
    }

    private Void dealWithMethod(JmmNode jmmNode, Void v) {
        String methodName = jmmNode.get("methodName");
        classMethods.add(methodName);

        List<Symbol> methodParams = new ArrayList<>();
        List<String> params;
        List<Symbol> localVars = new ArrayList<>();
        List<JmmNode> children = jmmNode.getChildren();

        if (methodName.equals("main")) {
            Type paramsType = new Type("String", true);
            String paramName = jmmNode.get("args");
            Symbol paramsSymbol = new Symbol(paramsType, paramName);
            methodParams.add(paramsSymbol);
            Type returnType = new Type("void", false);
            methodsReturns.put(methodName, returnType);

            for (JmmNode child : children) {
                if (child.getKind().equals("Declaration")) {
                    addLocalVariable(child, localVars);
                }
            }
        }
        else{
            params = (List<String>) jmmNode.getObject("params");
            JmmNode childType = jmmNode.getChildren().get(0);
            String returnTypeName = childType.get("name");
            Boolean returnIsArray = (Boolean) childType.getObject("isArray");
            Type type = new Type(returnTypeName, returnIsArray);

            methodsReturns.put(methodName, type);

            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).getKind().equals("Declaration")) {
                    JmmNode declaration = children.get(i);
                    addLocalVariable(declaration, localVars);

                } else if (children.get(i).getKind().equals("Type")) {
                    String typeName = children.get(i).get("name");
                    Boolean isArray = (Boolean) children.get(i).getObject("isArray");
                    String paramName = params.get(i - 1);
                    Type paramType = new Type(typeName, isArray);
                    Symbol symbol = new Symbol(paramType, paramName);
                    methodParams.add(symbol);
                }

            }
        }
        methodsParams.put(methodName, methodParams);
        localVariables.put(methodName, localVars);
        return v;
    }

}
