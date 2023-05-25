package pt.up.fe.comp2023.optimize;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodLivenessAllocation {

    private final Method method;
    private final OllirResult ollirResult;

    private ArrayList<Set<String>> defined;
    private ArrayList<Set<String>> used;
    private ArrayList<Set<String>> inAlive;
    private ArrayList<Set<String>> outAlive;
    private ArrayList<Node> nodeOrder;

    public MethodLivenessAllocation(Method method, OllirResult ollirResult) {
        this.method = method;
        this.ollirResult = ollirResult;
    }

    private void dfsOrderNodes(Node node, ArrayList<Node> visited) {

        if (node == null) return;
        if (visited.contains(node)) return;
        if (this.nodeOrder.contains(node)) return;
        if (node instanceof Instruction instruction && !method.getInstructions().contains(instruction)) return;

        visited.add(node);

        for (Node successor : node.getSuccessors()) {
            dfsOrderNodes(successor, visited);
        }

        this.nodeOrder.add(node);

    }

    private void orderNodes() {
        Node node = method.getBeginNode();
        this.nodeOrder = new ArrayList<>();
        ArrayList<Node> visited = new ArrayList<>();
        dfsOrderNodes(node, visited);

    }

    private void addUseDefSet(Node node, Element element, ArrayList<Set<String>> useDefSet){
        int index = nodeOrder.indexOf(node);
        if(element instanceof ArrayOperand arrayOperand){
            for(Element elem: arrayOperand.getIndexOperands()){
                addUseDefSet(node, elem, useDefSet);
            }
            useDefSet.get(index).add(arrayOperand.getName());
        }
        if(element instanceof Operand operand){
            ElementType elementType = operand.getType().getTypeOfElement();
            if(elementType.equals(ElementType.THIS))
                useDefSet.get(index).add(operand.getName());
        }
    }


    private void useDefAlgorithm(Node node) {
        useDefAlgorithm(node, null);
    }

    public void useDefAlgorithm(Node node, Node parentNode){

        if(node == null) return;
        Node useDefNode;
        if(parentNode == null){
            useDefNode = node;
        } else {
            useDefNode = parentNode;
        }

        if(node.getNodeType().equals(NodeType.BEGIN) || node.getNodeType().equals(NodeType.END)){
            return;
        }

        switch(node.getClass().getSimpleName()){
            case "AssignInstruction" -> {
                AssignInstruction instruction = (AssignInstruction) node;
                addUseDefSet(useDefNode, instruction.getDest(), defined);
                useDefAlgorithm(instruction.getRhs(), node);
            }
            case "BinaryOpInstruction" -> {
                BinaryOpInstruction instruction = (BinaryOpInstruction) node;
                addUseDefSet(useDefNode, instruction.getLeftOperand(), used);
                addUseDefSet(useDefNode, instruction.getRightOperand(), used);
            }
            case "CallInstruction" -> {
                CallInstruction instruction = (CallInstruction) node;
                addUseDefSet(useDefNode, instruction.getFirstArg(), used);
                ArrayList<Element> operands = instruction.getListOfOperands();
                if(operands != null){
                    for(Element operand: operands){
                        addUseDefSet(useDefNode, operand, used);
                    }
                }
            }
            case "GetFieldInstruction" -> {
                GetFieldInstruction instruction = (GetFieldInstruction) node;
                addUseDefSet(useDefNode, instruction.getFirstOperand(), used);
            }
            case "PutFieldInstruction" -> {
                PutFieldInstruction instruction = (PutFieldInstruction) node;
                addUseDefSet(useDefNode, instruction.getFirstOperand(), used);
                addUseDefSet(useDefNode, instruction.getThirdOperand(), used);
            }
            case "ReturnInstruction" -> {
                ReturnInstruction instruction = (ReturnInstruction) node;
                addUseDefSet(useDefNode, instruction.getOperand(), used);
            }
            case "UnaryOpInstruction" -> {
                UnaryOpInstruction instruction = (UnaryOpInstruction) node;
                addUseDefSet(useDefNode, instruction.getOperand(), used);
            }
            case "SingleOpInstruction" -> {
                SingleOpInstruction instruction = (SingleOpInstruction) node;
                addUseDefSet(useDefNode, instruction.getSingleOperand(), used);
            }
        }
    }

    public void inOutAlgorithm(){
        orderNodes();
        inAlive = new ArrayList<>();
        outAlive = new ArrayList<>();
        defined = new ArrayList<>();
        used = new ArrayList<>();

        for(int i = 0; i < nodeOrder.size(); i++){
            inAlive.add(new HashSet<>());
            outAlive.add(new HashSet<>());
            defined.add(new HashSet<>());
            used.add(new HashSet<>());
            useDefAlgorithm(nodeOrder.get(i));
        }

        boolean changedLiveness;
        do {
            changedLiveness = false;
            for(int i = 0; i < nodeOrder.size(); i++){
                Node node = nodeOrder.get(i);
                Set<String> in = new HashSet<>(inAlive.get(i));
                Set<String> out = new HashSet<>(outAlive.get(i));

                outAlive.get(i).clear();


                for(Node successor: node.getSuccessors()){
                    int index = nodeOrder.indexOf(successor);
                    if(index == -1) continue;
                    Set<String> inAliveSuccessorIndex = inAlive.get(index);

                    outAlive.get(i).addAll(inAliveSuccessorIndex);
                }

                inAlive.get(i).clear();
                Set<String> outDefDiff = new HashSet<>(outAlive.get(i));
                outDefDiff.removeAll(defined.get(i));
                outDefDiff.addAll(used.get(i));

                inAlive.get(i).addAll(outDefDiff);

                changedLiveness = !in.equals(inAlive.get(i)) || !out.equals(outAlive.get(i));

            }
        } while(changedLiveness);

    }

    //==================================================================================================================

    public void getInteferenceGraph(){
        Set<String> vars = new HashSet<>();
        Set<String> params = new HashSet<>();
        Set<String> varTableVariables = this.method.getVarTable().keySet();

        List<String> paramNames = new ArrayList<>();
        List<Element> methodParams = this.method.getParams();
        for(Element param: methodParams){
            if(param instanceof Operand operand){
                params.add(operand.getName());
                paramNames.add(operand.getName());
            }
        }

        for(String variable: varTableVariables){
            if(paramNames.contains(variable)){
                params.add(variable);
            } else if (variable.equals("this")) vars.add(variable);
        }

    }

}
