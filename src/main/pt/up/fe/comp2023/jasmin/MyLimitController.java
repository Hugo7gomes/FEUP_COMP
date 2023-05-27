package pt.up.fe.comp2023.jasmin;

import java.util.HashSet;
import java.util.Set;

public class MyLimitController {
    private Set<Integer> registers;
    private int maxStackSize;
    private int runningStackSize;
    private int nextStackSize;

    public MyLimitController() {
        this.registers = new HashSet<>();
        this.maxStackSize = 0;
        this.runningStackSize = 0;
    }

    public int getStackLimit() {
        return maxStackSize;
    }

    public int getLocalsLimit() {
        return registers.size();
    }

    public void updateStack(int value) {
        this.nextStackSize += value;
    }

    public void applyStackSize() {
        this.runningStackSize = this.nextStackSize;
        if (this.runningStackSize > this.maxStackSize) {
            this.maxStackSize = this.runningStackSize;
        }
    }

    public void updateRegister(int register) {
        this.registers.add(register);
    }

    public void updateRegisters(int size){
        for (int i = 0; i < size; i++) {
            this.registers.add(i);
        }
    }

    public void resetStack() {
        this.runningStackSize = 0;
        this.maxStackSize = 0;
        this.registers.clear();
    }






}
