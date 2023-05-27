package pt.up.fe.comp2023.optimize;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;

public class ConstantFoldingVisitor extends AJmmVisitor<String, String> {

    private boolean changed = false;

    public boolean hasChanged(){
        return changed;
    }

    @Override
    protected void buildVisitor() {
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("UnaryOp", this::dealWithUnaryOp);
        setDefaultVisit(this::defaultVisit);
    }

    private String dealWithUnaryOp(JmmNode jmmNode, String dummy) {
        JmmNode child = jmmNode.getChildren().get(0);
        if(child.getKind().equals("Boolean")){
            boolean value = Boolean.parseBoolean(child.get("value"));
            JmmNode newNode = new JmmNodeImpl("Boolean");
            String newValue;
            if(value){
                newValue = "false";
            }else{
                newValue = "true";
            }
            newNode.put("value", newValue);
            replaceNode(jmmNode, newNode);
        }
        return "";
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String dummy) {
        JmmNode left = jmmNode.getChildren().get(0);
        JmmNode right = jmmNode.getChildren().get(1);
        visit(left);
        visit(right);

        String op = jmmNode.get("op");
        if(left.getKind().equals("Integer") && right.getKind().equals("Integer")){
            int leftValue = Integer.parseInt(left.get("value"));
            int rightValue = Integer.parseInt(right.get("value"));
            int result = 0;
            boolean resultBool = false;
            switch (op){
                case "+":
                    result = leftValue + rightValue;
                    break;
                case "-":
                    result = leftValue - rightValue;
                    break;
                case "*":
                    result = leftValue * rightValue;
                    break;
                case "/":
                    result = leftValue / rightValue;
                    break;
                case "<":
                    resultBool = leftValue < rightValue;
                    break;
            }
            JmmNode newNode;
            if(op.equals("<")){
                newNode = new JmmNodeImpl("Boolean");
                newNode.put("value", String.valueOf(resultBool));
            }else{
                newNode = new JmmNodeImpl("Integer");
                newNode.put("value", String.valueOf(result));
            }
            replaceNode(jmmNode, newNode);
            changed = true;
        }
        else if(left.getKind().equals("Boolean") && right.getKind().equals("Boolean")){
            if(op.equals("&&")){
                JmmNode newNode;
                newNode = new JmmNodeImpl("Boolean");
                boolean result = left.get("value").equals("1") && right.get("value").equals("1");
                newNode.put("value", Boolean.toString(result));
                replaceNode(jmmNode, newNode);
            }
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

    private String defaultVisit(JmmNode jmmNode, String dummy) {
        for(JmmNode child : jmmNode.getChildren()){
            visit(child, dummy);
        }
        return "";
    }
}
