package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MethodVisitor extends AJmmVisitor<Void,Void> {

    private List<String> classMethods = new ArrayList<String>();

    private Map<String, List<Symbol>> methodsParams = new HashMap<>();

    private Map<String, Type> methodsReturns = new HashMap<>();

    private Map<String, List<Symbol>> localVariables = new HashMap<>();
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

    private Void dealWithMethod(JmmNode jmmNode, Void v) {
        String methodName = jmmNode.get("methodName");
        classMethods.add(methodName);

        List<Symbol> methodParams = new ArrayList<Symbol>();
        List<String> params = new ArrayList<String>();
        List<Symbol> localVars = new ArrayList<Symbol>();

        params.add(jmmNode.get("paramName"));
        List<String> otherParams = (List<String>) jmmNode.getObject("otherParams");
        for( String otherParam: otherParams){
            params.add(otherParam);
        }

        JmmNode childType = jmmNode.getChildren().get(0);
        String returnTypeName = childType.get("name");
        Boolean returnIsArray = (Boolean) childType.getObject("isArray");
        Type type = new Type(returnTypeName, returnIsArray);

        methodsReturns.put(methodName,type);

        List<JmmNode> children = jmmNode.getChildren();
        for(int i = 1; i < children.size()-1; i++){
            if(children.get(i).getKind().equals("Declaration")){
                JmmNode declaration = children.get(i);
                String name = declaration.get("value");
                String typeName = declaration.getChildren().get(0).get("name");
                Boolean isArray = (Boolean) declaration.getChildren().get(0).getObject("isArray");
                Type localType = new Type(typeName,isArray);
                Symbol symbol = new Symbol(localType,name);
                localVars.add(symbol);
            }
            else{
                String typeName = children.get(i).get("name");
                Boolean isArray = (Boolean) children.get(i).getObject("isArray");
                String paramName = params.get(i-1);
                Type paramType = new Type(typeName,isArray);
                Symbol symbol = new Symbol(paramType,paramName);
                methodParams.add(symbol);
            }

        }
        methodsParams.put(methodName,methodParams);
        localVariables.put(methodName,localVars);
        return null;
    }




}
