package pt.up.fe.comp2023.optimize;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConstantPropagationVisitor extends AJmmVisitor<HashMap<String, JmmNode>, String> {
    private boolean changed = false;
    @Override
    protected void buildVisitor() {
        addVisit("Method", this::dealWithMethod);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("IfElseStmt", this::dealWithIfStmt);
        addVisit("WhileStmt", this::dealWithWhileStmt);

        setDefaultVisit(this::defaultVisit);
    }

    private String dealWithWhileStmt(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        JmmNode expression = jmmNode.getJmmChild(0);


        JmmNode stmts = jmmNode.getJmmChild(1);
        visit(stmts,constMap);
        for(JmmNode child: stmts.getChildren()){
            if(child.getKind().equals("Assignment")){
                constMap.remove(child.get("var"));
            }
        }
        System.out.println("ConstMap");
        System.out.println(constMap);
        visit(expression,constMap);

        return "";
    }

    private Set<String> getVariablesAssigned(JmmNode node){
        Set<String> variablesAssigned = new HashSet<>();
        for(JmmNode child : node.getChildren()){
            if(child.getKind().equals("Assignment")){
                variablesAssigned.add(child.get("var"));
            }
        }
        return variablesAssigned;
    }

    private String dealWithIfStmt(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        JmmNode expression = jmmNode.getJmmChild(0);
        visit(expression,constMap);

        JmmNode ifStmts = jmmNode.getJmmChild(1);
        visit(ifStmts,constMap);
        JmmNode elseStmts = jmmNode.getJmmChild(2);
        visit(elseStmts,constMap);

        for(JmmNode child : ifStmts.getChildren()){
            if(child.getKind().equals("Assignment")){
                constMap.remove(child.get("var"));
            }
        }

        for(JmmNode child : elseStmts.getChildren()){
            if(child.getKind().equals("Assignment")){
                constMap.remove(child.get("var"));
            }
        }

        return "";
    }

    private String defaultVisit(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        for(JmmNode child : jmmNode.getChildren()){
            visit(child, constMap);
        }
        return "";
    }


    private String dealWithIdentifier(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        JmmNode constant = constMap.get(jmmNode.get("value"));
        //Check if the identifier is in the map
        if(constant != null){
            //If it is, replace it with the constant
            JmmNode newNode = new JmmNodeImpl(constant.getKind());
            newNode.put("value", constant.get("value"));
            replaceNode(jmmNode, newNode);
            return "changed";
        }
        return "";
    }


    private void replaceNode(JmmNode oldNode, JmmNode newNode){
        //Get the parent of the node
        JmmNode parent = oldNode.getJmmParent();
        if(parent == null){
            return;
        }
        //Get the index of the node
        int index = parent.getChildren().indexOf(oldNode);
        parent.setChild(newNode,index);
        this.changed = true;
    }

    private String dealWithAssignment(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        //Get the expression
        JmmNode expression = jmmNode.getChildren().get(0);
        visit(expression,constMap);
        //Check if the expression is a constant
        if(expression.getKind().equals("Boolean") || expression.getKind().equals("Integer")){
            //If it is, add it to the map
            constMap.put(jmmNode.get("var"), expression);
        }else{
            constMap.remove(jmmNode.get("var"));
        }
        return "";
    }

    private String dealWithMethod(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        //Create a new map for the method
        HashMap<String, JmmNode> methodMap = new HashMap<>();
        //Visit method with the new map
        defaultVisit(jmmNode,methodMap);
        return "";
    }

    public boolean hasChanged(){
        return changed;
    }
}
