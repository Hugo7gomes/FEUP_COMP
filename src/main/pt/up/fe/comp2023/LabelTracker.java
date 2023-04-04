package pt.up.fe.comp2023;

public class LabelTracker {
    private int labelCounter = 0;
    public LabelTracker() {
        this.labelCounter = 0;
    }
    public int nextLabel() {
        return this.labelCounter++;
    }
}
