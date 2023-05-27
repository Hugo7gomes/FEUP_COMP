package pt.up.fe.comp2023.jasmin;
import org.specs.comp.ollir.OperationType;
import java.util.Objects;

public class MyJasminInstruction {

    public static MyLimitController limitController = new MyLimitController();

    private static String registerInstruction(String instruction, int register) {
        limitController.updateRegister(register);
        if(register >= 0 && register <= 3) {
            return instruction + "_" + register + "\n";
        }
        return instruction + " " + register + "\n";
    }

    public static String pop() {
        limitController.updateStack(-1);
        return "\tpop\n";
    }

    public static String dup() {
        limitController.updateStack(1);
        return "\tdup\n";
    }

    public static String iload(int register) {
        limitController.updateStack(1);
        return registerInstruction("\tiload", register);
    }

    public static String istore(int register) {
        limitController.updateStack(-1);
        return registerInstruction("\tistore", register);
    }

    public static String aload(int register) {
        limitController.updateStack(1);
        return registerInstruction("\taload", register);
    }

    public static String astore(int register) {
        limitController.updateStack(-1);
        return registerInstruction("\tastore", register);
    }

    public static String iaload() {
        limitController.updateStack(-1);
        return "\tiaload\n";
    }

    public static String iastore() {
        limitController.updateStack(-3);
        return "\tiastore\n";
    }

    public static String ireturn() {
        return "\tireturn\n";
    }

    public static String areturn() {
        return "\tareturn\n";
    }

    public static String iconst(int value) {
        limitController.updateStack(1);
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
        limitController.updateStack(1);
        return "\tnew " + className + "\n";
    }

    public static String iinc(int register, int value) {
        return "\tiinc " + register + " " + value + "\n";
    }

    public static String arithOp(OperationType op) {
        limitController.updateStack(-1);
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

    public static String invokeStaticOp(String className, String methodName, String argsTypes, String returnType, int argsSize) {
        limitController.updateStack(-argsSize);

        return "\tinvokestatic " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String invokeVirtualOp(String className, String methodName, String argsTypes, String returnType, int argsSize) {
        limitController.updateStack(-argsSize);

        return "\tinvokevirtual " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String invokeSpecialOp(String className, String methodName, String argsTypes, String returnType, int argsSize) {
        limitController.updateStack(-argsSize);

        return "\tinvokespecial " + className + "/" + methodName + argsTypes +  returnType + "\n";
    }

    public static String fieldOp(String type, String className, String fieldName, String fieldType) {
        int stackDiff = Objects.equals(type, "get") ? 1 : -2;
        limitController.updateStack(stackDiff);
        return "\t" + type + " " + className + "/" + fieldName + " " + fieldType + "\n";
    }

    public static String goTo(String label){
        return "\tgoto " + label + "\n";
    }

    public static String ifne(String label){
        limitController.updateStack(-1);
        return "\tifne " + label + "\n";
    }

    public static String iflt(String label){
        limitController.updateStack(-1);
        return "\tiflt " + label + "\n";
    }

    public static String ifge(String label){
        limitController.updateStack(-1);
        return "\tifge " + label + "\n";
    }

    public static String ifIcmplt(String label){
        limitController.updateStack(-2);
        return "\tif_icmplt " + label + "\n";
    }

    public static String ifIcmpge(String label){
        limitController.updateStack(-2);
        return "\tif_icmpge " + label + "\n";
    }

    public static String arrayLength(){
        return "\tarraylength\n";
    }

}
