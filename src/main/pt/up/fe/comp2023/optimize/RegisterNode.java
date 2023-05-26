package pt.up.fe.comp2023.optimize;

import java.util.ArrayList;

public class RegisterNode {
    private final String name;
    private boolean isVisible;
    private int register;
    private final ArrayList<RegisterNode> edges;

    public RegisterNode(String name) {
        this.name = name;
        this.isVisible = true;
        this.register = -1;
        this.edges = new ArrayList<>();
    }

    public int countVisibleNeighbors() {
        int count = 0;
        for (RegisterNode edge : edges) {
            if (edge.isVisible)
                count++;
        }
        return count;
    }

    public void addEdge(RegisterNode register) {
        edges.add(register);
    }

    public void removeEdge(RegisterNode node) {
        edges.remove(node);
    }

    public ArrayList<RegisterNode> getEdges() {
        return edges;
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        return name;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean edgeHasRegister(int register) {
        for (RegisterNode edge : edges) {
            if (edge.getRegister() == -1 || edge.getRegister() != register)
                return true;
        }
        return false;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterNode node = (RegisterNode) o;
        return node.getName().equals(this.getName());
    }


}
