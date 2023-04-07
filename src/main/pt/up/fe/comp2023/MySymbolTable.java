package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor ;
import pt.up.fe.comp.jmm.ast.JmmNode ;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

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

    public void setClassFields(List<Symbol> fields) {
        this.classFields = fields;
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

    public void setMethodParams(Map<String, List<Symbol>> methodParams) {
        this.methodParams = methodParams;
    }

    public void setMethodReturnTypes(Map<String, Type> methodReturnTypes) {
        this.methodReturnTypes = methodReturnTypes;
    }

    public void setLocalVariables(Map<String, List<Symbol>> localVariables) {
        this.localVariables = localVariables;
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

    public String print(){
        StringBuilder res = new StringBuilder();
        res.append("Class - ").append(className).append("\n");
        res.append("Super - ").append(superClassName).append("\n");
        res.append("\n=======================\n\n");
        res.append("Imports - ").append("\n");
        for (String imp: imports) {
            res.append("\t").append(imp).append("\n");
        }
        res.append("\n=======================\n\n");
        res.append("Fields - ").append("\n");
        for(Symbol field: classFields){
            res.append("\t").append(field.getType().getName());
            if(field.getType().isArray()){
                res.append("[]");
            }
            res.append(" ").append(field.getName()).append("\n");
        }
        res.append("\n=======================\n\n");
        for (String method:classMethods){
            res.append("Method - ").append(method).append("\n");
            res.append("\tParams - \n");
            checkEmpty(res, method, methodParams);
            res.append("\tReturn - \n");
            if(methodReturnTypes.get(method).isArray()){
                res.append("\t\t").append(methodReturnTypes.get(method).getName()).append("[]\n");
            }
            else{
                res.append("\t\t").append(methodReturnTypes.get(method).getName()).append("\n");
            }

            res.append("\tLocal Variables - \n");
            checkEmpty(res, method, localVariables);
            res.append("\n-----------------------\n\n");

        }
        res.append("\n");
        return res.toString();
    }

    private void checkEmpty(StringBuilder res, String method, Map<String, List<Symbol>> list) {
        if(list.isEmpty()){
            res.append("\t\tNone\n");
        }
        for (Symbol localVariable: list.get(method)){
            res.append("\t\t").append(localVariable.getType().getName());
            if(localVariable.getType().isArray()){
                res.append("[]");
            }
            res.append(" ").append(localVariable.getName()).append("\n");
        }
    }

    public void create(JmmParserResult jmmParserResult){
        ClassVisitor classVisitor = new ClassVisitor ();
        classVisitor.visit(jmmParserResult.getRootNode());
        this.setClassName(classVisitor.getClassName());
        this.setSuper(classVisitor.getSuperClassName());

        ImportVisitor importVisitor = new ImportVisitor();
        importVisitor.visit(jmmParserResult.getRootNode());
        this.setImports(importVisitor.getImports());

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.visit(jmmParserResult.getRootNode());
        this.setClassMethods(methodVisitor.getClassMethods());
        this.setMethodParams(methodVisitor.getMethodsParams());
        this.setMethodReturnTypes(methodVisitor.getMethodsReturns());
        this.setLocalVariables(methodVisitor.getLocalVariables());

        FieldVisitor fieldVisitor = new FieldVisitor();
        fieldVisitor.visit(jmmParserResult.getRootNode());
        this.setClassFields(fieldVisitor.getClassFields());
    }
}