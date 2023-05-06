package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.OperationType;

public class MyJasminInstruction {

    private static String registerInstruction(String instruction, int register) {
        if(register >= 0 && register <= 3) {
            return instruction + "_" + register + "\n";
        }
        return instruction + " " + register + "\n";
    }

    public static String pop() {
        return "\tpop\n";
    }

    public static String dup() {
        return "\tdup\n";
    }

    public static String iload(int register) {
        return registerInstruction("\tiload", register);
    }

    public static String istore(int register) {
        return registerInstruction("\tistore", register);
    }

    public static String aload(int register) {
        return registerInstruction("\taload", register);
    }

    public static String astore(int register) {
        return registerInstruction("\tastore", register);
    }

    public static String iaload() {
        return "\tiaload\n";
    }

    public static String iastore() {
        return "\tiastore\n";
    }

    public static String ireturn() {
        return "\tireturn\n";
    }

    public static String areturn() {
        return "\tareturn\n";
    }

    public static String iconst(int value) {
        if(value == -1) {
            return "\ticonst_m1\n";
        }
        if(value >= 0 && value <= 5) {
            return "\ticonst_" + value + "\n";
        }
        if (value >= -128 && value <= 127) {
            return "\tbipush " + value + "\n";
        }
        if (value >= -32768 && value <= 32767) {
            return "\tsipush " + value + "\n";
        }
        return "\tldc " + value + "\n";
    }

    public static String newArray() {
        return "\tnewarray int\n";
    }

    public static String newObject(String className) {
        return "\tnew " + className + "\n";
    }

    public static String iinc(int register, String value) {
        return "\tiinc " + register + " " + value + "\n";
    }

    public static String arithOp(OperationType op) {
        switch (op) {
            case ADD -> {
                return "\tiadd\n";
            }
            case SUB -> {
                return "\tisub\n";
            }
            case MUL -> {
                return "\timul\n";
            }
            case DIV -> {
                return "\tidiv\n";
            }
            case ANDB -> {
                return "\tiand\n";
            }
        }
        return "";
    }

    public static String invokeStaticOp(String className, String methodName, String argsTypes, String returnType) {
        return "\tinvokestatic " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String invokeVirtualOp(String className, String methodName, String argsTypes, String returnType) {
        return "\tinvokevirtual " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String invokeSpecialOp(String className, String methodName, String argsTypes, String returnType) {
        return "\tinvokespecial " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String fieldOp(String type, String className, String fieldName, String fieldType) {
        return "\t" + type + " " + className + "/" + fieldName + " " + fieldType + "\n";
    }

    public static String goTo(String label){
        return "\tgoto " + label + "\n";
    }

    public static String ifne(String label){
        return "\tifne " + label + "\n";
    }

    public static String iflt(String label){
        return "\tiflt " + label + "\n";
    }

    public static String ifIcmplt(String label){
        return "\tif_icmplt " + label + "\n";
    }


}
