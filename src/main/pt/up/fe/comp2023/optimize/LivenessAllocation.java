package pt.up.fe.comp2023.optimize;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class LivenessAllocation {

    private final OllirResult ollirResult;
    private ArrayList<MethodLivenessAllocation> methodLivenessAllocations;
    private final ArrayList<Method> methods;

    public LivenessAllocation(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.methodLivenessAllocations = new ArrayList<>();
        this.methods = ollirResult.getOllirClass().getMethods();
    }

    public void inOutAlgorithm() {
        ollirResult.getOllirClass().buildCFGs();

        for(Method method: methods){
            MethodLivenessAllocation methodLivenessAllocation = new MethodLivenessAllocation(method, ollirResult);
            methodLivenessAllocation.inOutAlgorithm();
            methodLivenessAllocations.add(methodLivenessAllocation);
        }
    }

    public void interferenceGraph() {
        for(MethodLivenessAllocation methodLivenessAllocation: methodLivenessAllocations){
            methodLivenessAllocation.setInterferenceGraph();
        }
    }

    public void colorInterferenceGraph() {
        for(MethodLivenessAllocation methodLivenessAllocation: methodLivenessAllocations){
            int numRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            methodLivenessAllocation.colorInterferenceGraph(numRegisters);

        }
    }

    public void registerAlloc() {
        for(MethodLivenessAllocation method: methodLivenessAllocations){
            HashMap<String, Descriptor> variableTable = method.getMethod().getVarTable();
            for(RegisterNode node: method.getInterferenceGraph().vars()){
                variableTable.get(node.getName()).setVirtualReg(node.getRegister());
            }
            for(RegisterNode node: method.getInterferenceGraph().params()){
                variableTable.get(node.getName()).setVirtualReg(node.getRegister());
            }
            if(variableTable.get("this") != null){
                variableTable.get("this").setVirtualReg(0);
            }

        }
    }
}
