package pt.up.fe.comp2023.optimize;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class MethodLivenessAllocation {

    private final Method method;
    private final OllirResult ollirResult;

    private ArrayList<Set<String>> defined;
    private ArrayList<Set<String>> used;
    private ArrayList<Set<String>> inAlive;
    private ArrayList<Set<String>> outAlive;
    private ArrayList<Node> nodeOrder;
    private InterferenceGraph interferenceGraph;

    public MethodLivenessAllocation(Method method, OllirResult ollirResult) {
        this.method = method;
        this.ollirResult = ollirResult;
    }

    public InterferenceGraph getInterferenceGraph() {
        return interferenceGraph;
    }

    public Method getMethod() {
        return method;
    }

    public HashMap<String, Descriptor> getVariableTable() {
        return method.getVarTable();
    }

    // Method to perform Depth First Search to order the nodes for further processing
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

    // Helper method to order nodes
    private void orderNodes() {
        Node node = method.getBeginNode();
        this.nodeOrder = new ArrayList<>();
        ArrayList<Node> visited = new ArrayList<>();
        dfsOrderNodes(node, visited);
    }

    // Method to add elements to the use/define set
    private void addUseDefSet(Node node, Element element, ArrayList<Set<String>> useDefSet){
        int index = nodeOrder.indexOf(node);
        if(element instanceof ArrayOperand arrayOperand){
            for(Element elem: arrayOperand.getIndexOperands()){
                addUseDefSet(node, elem, used);
            }
            useDefSet.get(index).add(arrayOperand.getName());
        }
        if(element instanceof Operand operand){
            ElementType elementType = operand.getType().getTypeOfElement();
            if(!elementType.equals(ElementType.THIS))
                useDefSet.get(index).add(operand.getName());
        }
    }

    // Method to perform the use-define analysis on the given node
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
            case "OpCondInstruction" -> {
                OpCondInstruction instruction = (OpCondInstruction) node;
                List<Element> operands = instruction.getOperands();
                for (Element operand : operands) {
                    addUseDefSet(useDefNode, operand, used);
                }
            }
            case "SingleOpCondInstruction" -> {
                SingleOpCondInstruction instruction = (SingleOpCondInstruction) node;
                List<Element> operands = instruction.getOperands();
                for (Element operand : operands) {
                    addUseDefSet(useDefNode, operand, used);
                }
            }
        }
    }

    // Method to compute the in and out sets for each instruction
    public void inOutAlgorithm(){
        orderNodes();
        this.inAlive = new ArrayList<>();
        this.outAlive = new ArrayList<>();
        this.defined = new ArrayList<>();
        this.used = new ArrayList<>();
        for(int i = 0; i < nodeOrder.size(); i++){
            inAlive.add(new HashSet<>());
            outAlive.add(new HashSet<>());
            defined.add(new HashSet<>());
            used.add(new HashSet<>());
            useDefAlgorithm(nodeOrder.get(i), null);
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

                changedLiveness = changedLiveness || !in.equals(inAlive.get(i)) || !out.equals(outAlive.get(i));

            }
        } while(changedLiveness);

    }

    //=============================================

    // Helper method to get the name of an element
    private String getElemName(Element elem){
        if(elem instanceof Operand operand) return operand.getName();
        return null;
    }

    // Helper method to get the names of parameters
    private List<String> getParamNames(){
        List<String> paramNames = new ArrayList<>();
        List<Element> methodParams = this.method.getParams();

        for(Element param: methodParams){
            if(param instanceof Operand operand){
                String paramName = getElemName(param);
                paramNames.add(paramName);
            }
        }
        return paramNames;
    }

    // Method to create an interference graph where each node is a variable and an edge between
    // two nodes means the corresponding variables are live at the same time
    public void setInterferenceGraph(){
        Set<String> vars = new HashSet<>();
        Set<String> params = new HashSet<>();
        Set<String> varTableVariables = this.method.getVarTable().keySet();
        List<String> paramNames = getParamNames();

        for(String variable: varTableVariables){
            if(paramNames.contains(variable)){
                params.add(variable);
            } else if (!variable.equals("this")) vars.add(variable);
        }

        Set<RegisterNode> registerVars = new HashSet<>();
        Set<RegisterNode> registerParams = new HashSet<>();

        for(String node : vars){
            registerVars.add(new RegisterNode(node));
        }
        for(String node : params){
            registerParams.add(new RegisterNode(node));
        }

        this.interferenceGraph = new InterferenceGraph(registerVars, registerParams);

        for(RegisterNode varX: this.interferenceGraph.vars()){
            for(RegisterNode varY: this.interferenceGraph.vars()){
                if(varX.equals(varY)) continue;

                for(int i = 0; i < nodeOrder.size(); i++){
                    if(defined.get(i).contains(varX.getName()) && outAlive.get(i).contains(varY.getName())){
                        this.interferenceGraph.addEdge(varX, varY);
                    }
                }
            }
        }

    }

    //=============================================

    // Method to color the interference graph. Each color corresponds to a register, and two
    // nodes that have an edge between them must have different colors
    public void colorInterferenceGraph(int maxRegisters){
        Stack<RegisterNode> stack = new Stack<>();
        int registers = 0;

        while(interferenceGraph.countVisibleNodes() > 0){
            for(RegisterNode node: interferenceGraph.vars()){
                if(!node.isVisible()) continue;
                int numVisibleNeighbours = node.countVisibleNeighbors();
                if(numVisibleNeighbours < registers){
                    node.setVisible(false);
                    stack.push(node);
                } else registers++;
            }
        }

        if(maxRegisters > 0 && registers > maxRegisters) {
            String reportString = "Method " + this.method.getMethodName() + " requires " + registers + " registers, but only " + maxRegisters + " are available.";
            Report report = new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, reportString);
            ollirResult.getReports().add(report);

            throw new RuntimeException(reportString);
        }

        int startRegister = 1 + interferenceGraph.params().size();

        while(!stack.isEmpty()){
            RegisterNode node = stack.pop();
            for(int i = startRegister; i <= registers + startRegister; i++){
                if(!node.edgeHasRegister(i)) {
                    node.setRegister(i);
                    node.setVisible(true);
                    break;
                }
            }
            if(!node.isVisible()){
                String reportString = "Register allocation failed for method " + this.method.getMethodName() + ".";
                Report report = new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, reportString);
                ollirResult.getReports().add(report);

                throw new RuntimeException(reportString);
            }
        }

        // starts at 1, because register 0 is reserved for "this"
        int register = 1;
        for(RegisterNode node: interferenceGraph.params()){
            node.setRegister(register);
            register++;
        }
    }

}
