package pt.up.fe.comp2023.optimize;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;

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
        return "";
    }

    private String dealWithIfStmt(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        JmmNode expression = jmmNode.getJmmChild(0);
        JmmNode ifStmts = jmmNode.getJmmChild(1);
        JmmNode elseStmts = jmmNode.getJmmChild(2);

        if(expression.getKind().equals("Boolean")){
            Boolean expressionValue = Boolean.valueOf(expression.get("value"));
            if(expressionValue){
                visit(ifStmts, constMap);
            }else{
                visit(elseStmts, constMap);
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
            changed = true;
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
        //Remove the node from the parent and add the new node in the same position
        parent.removeJmmChild(index);
        parent.add(newNode, index);
        //Set the parent of the new node
        newNode.setParent(parent);
    }

    private String dealWithAssignment(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        //Get the expression
        JmmNode expression = jmmNode.getChildren().get(0);
        //Check if the expression is a constant
        if(expression.getKind().equals("Boolean") || expression.getKind().equals("Integer")){
            //If it is, add it to the map
            constMap.put(jmmNode.get("var"), expression);
        }else{
            //If it isn't, visit it
            visit(expression, constMap);
        }
        return "";
    }

    private String dealWithMethod(JmmNode jmmNode, HashMap<String, JmmNode> constMap) {
        //Create a new map for the method
        HashMap<String, JmmNode> methodMap = new HashMap<>();
        //Visit method children with the new map
        for(JmmNode child : jmmNode.getChildren()){
            visit(child, methodMap);
        }
        return "";
    }

    public boolean hasChanged(){
        return changed;
    }
}
