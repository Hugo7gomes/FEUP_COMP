package pt.up.fe.comp2023.optimize;

import java.util.Set;

public record InterferenceGraph(Set<RegisterNode> vars, Set<RegisterNode> params) {

    // Method to add an edge to the interference graph
    // An edge between two nodes indicates that the corresponding variables are live at the same time
    public void addEdge(RegisterNode node1, RegisterNode node2) {
        node1.addEdge(node2);
        node2.addEdge(node1);
    }

    // Method to remove an edge from the interference graph
    // If the edge is removed, it indicates that the corresponding variables are not live at the same time
    public void removeEdge(RegisterNode node1, RegisterNode node2) {
        node1.removeEdge(node2);
        node2.removeEdge(node1);
    }

    // Method to count the number of visible nodes in the interference graph
    // A visible node is a node that has not been colored yet
    public int countVisibleNodes() {
        int count = 0;
        for (RegisterNode node : vars) {
            if (node.isVisible())
                count++;
        }
        return count;
    }
}
