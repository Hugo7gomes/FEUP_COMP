package pt.up.fe.comp2023.optimize;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import java.util.ArrayList;
import java.util.HashMap;

public class LivenessAllocation {

    private final OllirResult ollirResult;

    // A list to store the result of the liveness analysis for each method
    private ArrayList<MethodLivenessAllocation> methodLivenessAllocations;

    // A list to store all the methods present in the class
    private final ArrayList<Method> methods;

    public LivenessAllocation(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.methodLivenessAllocations = new ArrayList<>();
        this.methods = ollirResult.getOllirClass().getMethods();
    }

    // Method to compute the 'inAlive' and 'outAlive' sets for each instruction in each method
    public void inOutAlgorithm() {
        ollirResult.getOllirClass().buildCFGs();

        for(Method method: methods){
            MethodLivenessAllocation methodLivenessAllocation = new MethodLivenessAllocation(method, ollirResult);
            methodLivenessAllocation.inOutAlgorithm();
            methodLivenessAllocations.add(methodLivenessAllocation);
        }
    }

    // Method to create an interference graph for each method, where each node is a variable
    // and an edge between two nodes means the corresponding variables are live at the same time
    public void interferenceGraph() {
        for(MethodLivenessAllocation methodLivenessAllocation: methodLivenessAllocations){
            methodLivenessAllocation.setInterferenceGraph();
        }
    }

    // Method to color the interference graph. Each color corresponds to a register,
    // and two nodes that have an edge between them must have different colors
    public void colorInterferenceGraph() {
        for(MethodLivenessAllocation methodLivenessAllocation: methodLivenessAllocations){
            int numRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            methodLivenessAllocation.colorInterferenceGraph(numRegisters);

        }
    }

    // Method to allocate registers for variables based on the coloring of the interference graph
    public void registerAlloc() {
        for(MethodLivenessAllocation method: methodLivenessAllocations){
            HashMap<String, Descriptor> variableTable = method.getVariableTable();

            for(RegisterNode regNode: method.getInterferenceGraph().vars()){
                variableTable.get(regNode.getName()).setVirtualReg(regNode.getRegister());
            }

            for(RegisterNode regNode: method.getInterferenceGraph().params()){
                variableTable.get(regNode.getName()).setVirtualReg(regNode.getRegister());
            }

            if(variableTable.get("this") != null){
                variableTable.get("this").setVirtualReg(0);
            }

        }
    }
}
