package pt.up.fe.comp2023.optimize;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterNode {

    // Name of the node
    private final String name;

    // Boolean variable indicating whether the node is visible or not
    private boolean isVisible;

    // The register number assigned to this node
    private int register;

    // A list of nodes that this node has edges with
    private final ArrayList<RegisterNode> edges;

    public RegisterNode(String name) {
        this.name = name;
        this.isVisible = true; // Node is initially visible
        this.register = -1; // Node initially doesn't have any register assigned
        this.edges = new ArrayList<>();
    }

    // Returns the count of visible neighbors
    public int countVisibleNeighbors() {
        int count = 0;
        for (RegisterNode edge : edges) {
            if (edge.isVisible)
                count++;
        }
        return count;
    }

    // Adds an edge to this node
    public void addEdge(RegisterNode register) {
        edges.add(register);
    }

    // Removes an edge from this node
    public void removeEdge(RegisterNode node) {
        edges.remove(node);
    }

    // Returns the list of edges for this node
    public ArrayList<RegisterNode> getEdges() {
        return edges;
    }

    public String getName() {
        return name;
    }

    // Returns the name of the node
    public String setName(String name) {
        return name;
    }

    // Returns the register number of this node
    public int getRegister() {
        return register;
    }

    // Assigns a register number to this node
    public void setRegister(int register) {
        this.register = register;
    }

    // Returns whether the node is visible or not
    public boolean isVisible() {
        return isVisible;
    }

    // Sets the visibility of this node
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    // Returns true if any edge has the given register
    public boolean edgeHasRegister(int register) {
        for (RegisterNode edge : edges) {
            if (edge.getRegister() != -1 && edge.getRegister() == register)
                return true;
        }
        return false;
    }

    // Overrides the equals method for RegisterNode
    @Override
    public boolean equals (Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterNode node = (RegisterNode) o;
        return Objects.equals(name, node.name);
    }

}
