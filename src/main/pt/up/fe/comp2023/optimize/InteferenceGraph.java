package pt.up.fe.comp2023.optimize;

import java.util.Set;

public class InteferenceGraph {
    private final Set<RegisterNode> vars;
    private final Set<RegisterNode> params;

    public InteferenceGraph(Set<RegisterNode> vars, Set<RegisterNode> params) {
        this.vars = vars;
        this.params = params;
    }

    public void addEdge(RegisterNode node1, RegisterNode node2) {
        node1.addEdge(node2);
        node2.addEdge(node1);
    }

    public void removeEdge(RegisterNode node1, RegisterNode node2) {
        node1.removeEdge(node2);
        node2.removeEdge(node1);
    }

    public Set<RegisterNode> getVars() {
        return vars;
    }

    public Set<RegisterNode> getParams() {
        return params;
    }

    public int countVisibleNodes() {
        int count = 0;
        for (RegisterNode node : vars) {
            if (node.isVisible())
                count++;
        }
        return count;
    }

}
