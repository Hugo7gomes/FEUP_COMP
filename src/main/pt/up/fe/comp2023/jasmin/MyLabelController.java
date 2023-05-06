package pt.up.fe.comp2023.jasmin;

public class MyLabelController {
    private int labelCounter;

    public MyLabelController() {
        this.labelCounter = 0;
    }

    public String getLabel() {
        return "label" + this.labelCounter++;
    }

    public String getLabel(int label) {
        return "label" + label;
    }

    public int nextLabel() {
        return this.labelCounter++;
    }

    public void resetLabelCounter() {
        this.labelCounter = 0;
    }

}
