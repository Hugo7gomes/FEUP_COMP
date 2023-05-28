package pt.up.fe.comp2023.jasmin;

import java.util.HashSet;
import java.util.Set;

public class MyLimitController {

    // A Set data structure to hold the register values.
    private Set<Integer> registers;

    // Maximum stack size observed during operations
    private int maxStackSize;

    // Current stack size during operations
    private int runningStackSize;

    // A constant to add error margin to the maximum stack size
    private final int errorMargin;

    // Default constructor for the MyLimitController class
    public MyLimitController() {
        this.registers = new HashSet<>();
        this.maxStackSize = 0;
        this.runningStackSize = 0;
        this.errorMargin = 2;
    }

    // Method to get the stack limit, adding the error margin to the max stack size
    public int getStackLimit() {
        return maxStackSize += errorMargin;
    }

    // Method to get the size of registers, i.e., the number of unique registers
    public int getLocalsLimit() {
        return registers.size();
    }

    // Method to update the running stack size by adding the value parameter to it.
    // It also updates the maxStackSize if runningStackSize exceeds the current maxStackSize
    public void updateStack(int value) {
        this.runningStackSize += value;
        if (this.runningStackSize > this.maxStackSize) {
            this.maxStackSize = this.runningStackSize;
        }
    }

    // Method to add a register to the set of registers
    public void updateRegister(int register) {
        this.registers.add(register);
    }

    // Method to add a number of registers to the set of registers
    public void updateRegisters(int size){
        for (int i = 0; i < size; i++) {
            this.registers.add(i);
        }
    }

    // Method to reset the stack size and clear the set of registers
    public void resetStack() {
        this.runningStackSize = 0;
        this.maxStackSize = 0;
        this.registers.clear();
    }
}
