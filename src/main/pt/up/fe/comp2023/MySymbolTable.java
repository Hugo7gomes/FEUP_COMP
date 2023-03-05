package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor ;
import pt.up.fe.comp.jmm.ast.JmmNode ;

import java.util.*;

public class MySymbolTable implements SymbolTable {

    private List<String> imports = new ArrayList<>();
    private String className;
    private String superClassName;
    private List<String> classMethods = new ArrayList<>();
    private List<Symbol> classFields = new ArrayList<>();
    private Map<String, List<Symbol>> methodParams = new HashMap<>();
    private Map<String, Type> methodReturnTypes = new HashMap<>();
    private Map<String, List<Symbol>> localVariables = new HashMap<>();

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuper(String superClassName) {
        this.superClassName = superClassName;
    }

    public void setClassMethods(List<String> classMethods){
        this.classMethods =classMethods;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public void addMethod(String methodName, List<Symbol> params, Type returnType) {
        methodParams.put(methodName, params);
        methodReturnTypes.put(methodName, returnType);
        classMethods.add(methodName);
        localVariables.put(methodName, new ArrayList<Symbol>());
    }

    public void addLocalVariable(String methodName, Symbol variable){
        List<Symbol> variables = localVariables.get(methodName);
        variables.add(variable);
    }


    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superClassName;
    }

    @Override
    public List<Symbol> getFields() {
        return classFields;
    }

    @Override
    public List<String> getMethods() {
        return classMethods;
    }

    @Override
    public Type getReturnType(String s) {
        return methodReturnTypes.get(s);
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return methodParams.get(s);
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return localVariables.get(s);
    }
}