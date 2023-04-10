package pt.up.fe.comp2023;

import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.FieldInstruction;
import org.specs.comp.ollir.OperationType;

public class MyJasminInstruction {

    public enum FieldInstructionType {
        GETFIELD,
        PUTFIELD,
    }

    private static String registerInstruction(String instruction, int register) {
        if(register >= 0 && register <= 3) {
            return instruction + "_" + register + "\n";
        }
        return instruction + " " + register + "\n";
    }

    public static String pop() {
        return "pop\n";
    }
    public static String dup() {
        return "dup\n";
    }

    public static String swap() {
        return "swap\n";
    }

    public static String iload(int register) {
        return registerInstruction("iload", register);
    }

    public static String istore(int register) {
        return registerInstruction("istore", register);
    }

    public static String aload(int register) {
        return registerInstruction("aload", register);
    }

    public static String astore(int register) {
        return registerInstruction("astore", register);
    }

    public static String iaload() {
        return "iaload\n";
    }

    public static String iastore() {
        return "iastore\n";
    }

    public static String iconst(int value) {
        if(value >= 0 && value <= 5) {
            return "iconst_" + value + "\n";
        }
        if(value == -1) {
            return "iconst_m1\n";
        }
        if (value >= -128 && value <= 127) {
            return "bipush " + value + "\n";
        }
        if (value >= -32768 && value <= 32767) {
            return "sipush " + value + "\n";
        }
        return "ldc " + value + "\n";
    }
    public static String arrayLength() {
        return "arraylength\n";
    }

    public static String newArray() {
        return "newarray int\n";
    }

    public static String newObject(String className) {
        return "new " + className + "\n";
    }

    public static String gotoLabel(String label) {
        return "goto " + label + "\n";
    }

    public static String ifeq(String label) {
        return "ifeq " + label + "\n";
    }

    public static String ifne(String label) {
        return "ifne " + label + "\n";
    }

    public static String iinc(int register, String value) {
        return "iinc " + register + " " + value + "\n";
    }

    public static String iflt(String label) {
        return "iflt " + label + "\n";
    }

    public static String ifle(String label) {
        return "ifle " + label + "\n";
    }

    public static String ifgt(String label) {
        return "ifgt " + label + "\n";
    }

    public static String ifge(String label) {
        return "ifge " + label + "\n";
    }

    public static String if_icmpeq(String label) {
        return "if_icmpeq " + label + "\n";
    }

    public static String if_icmpne(String label) {
        return "if_icmpne " + label + "\n";
    }

    public static String if_icmplt(String label) {
        return "if_icmplt " + label + "\n";
    }

    public static String if_icmple(String label) {
        return "if_icmple " + label + "\n";
    }

    public static String if_icmpgt(String label) {
        return "if_icmpgt " + label + "\n";
    }

    public static String if_icmpge(String label) {
        return "if_icmpge " + label + "\n";
    }

    public static String arithOp(OperationType op) {
        switch (op) {
            case ADD -> {
                return "iadd\n";
            }
            case SUB -> {
                return "isub\n";
            }
            case MUL, ANDB -> {
                return "imul\n";
            }
            case DIV -> {
                return "idiv\n";
            }
            case AND -> {
                return "iand\n";
            }
            case OR -> {
                return "ior\n";
            }
            case XOR -> {
                return "ixor\n";
            }
            case SHL -> {
                return "ishl\n";
            }
            case SHR -> {
                return "ishr\n";
            }
        }
        return "";
    }

    public static String returnOp() {
        return "ireturn\n";
    }

    public static String fieldOp(FieldInstructionType type, String className, String fieldName, String fieldType) {
        return type.toString().toLowerCase() + " " + className + "/" + fieldName + " " + fieldType + "\n";
    }

    public static String invokeOp(CallType callType, String className, String methodName, String argsTypes, int numArgs, String returnType) {
        return callType.toString().toLowerCase() + " " + className + "/" + methodName +
                "(" + argsTypes + ")" + returnType + "\n";
    }






}
