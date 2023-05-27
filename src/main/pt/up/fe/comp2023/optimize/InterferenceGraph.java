package pt.up.fe.comp2023.optimize;

import java.util.Set;

public record InterferenceGraph(Set<RegisterNode> vars, Set<RegisterNode> params) {

    public void addEdge(RegisterNode node1, RegisterNode node2) {
        node1.addEdge(node2);
        node2.addEdge(node1);
    }

    public void removeEdge(RegisterNode node1, RegisterNode node2) {
        node1.removeEdge(node2);
        node2.removeEdge(node1);
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
