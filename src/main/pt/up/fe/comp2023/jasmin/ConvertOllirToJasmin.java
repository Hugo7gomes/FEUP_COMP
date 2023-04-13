package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.MyJasminInstruction;
import pt.up.fe.comp2023.jasmin.MyJasminInstructionBuilder;
import pt.up.fe.comp2023.jasmin.MyJasminUtils;

import java.util.HashMap;
import java.util.stream.Collectors;

public class ConvertOllirToJasmin {

    private final ClassUnit classUnit;

    public ConvertOllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }
    public String toJasmin() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(".class public " ).append(classUnit.getClassName()).append("\n");

        String superClass = classUnit.getSuperClass() != null ? classUnit.getSuperClass() : "java/lang/Object";
        stringBuilder.append(".super " ).append(superClass).append("\n");

        for(Field field: this.classUnit.getFields()){
            stringBuilder.append(buildField(field));
        }

        stringBuilder.append(".method public <init>()V\n");
        stringBuilder.append("aload_0\n");

        stringBuilder.append("invokenonvirtual ").append(superClass).append("/<init>()V\n");
        stringBuilder.append("return\n");
        stringBuilder.append(".end method\n");

        stringBuilder.append("\n\n\n");

        for(Method method: this.classUnit.getMethods()){
            stringBuilder.append(buildMethodHeader(method));
            stringBuilder.append(buildMethodStatements(method));
            stringBuilder.append(".end method\n");
        }

        return stringBuilder.toString();
    }

    public String buildField(Field field){
        StringBuilder stringBuilder = new StringBuilder();

        AccessModifiers accessModifier = field.getFieldAccessModifier();
        StringBuilder access = new StringBuilder();

        if(accessModifier != AccessModifiers.DEFAULT){
            access.append(accessModifier.name().toLowerCase()).append(" ");
        }
        if (field.isStaticField()) {
            access.append("static ");
        }
        if (field.isFinalField()) {
            access.append("final ");
        }

        stringBuilder.append(".field " ).append(access);
        stringBuilder.append(field.getFieldName()).append(" ");

        stringBuilder.append(MyJasminUtils.getType(this.classUnit, field.getFieldType()));

        if(field.isInitialized()) {
            stringBuilder.append(" = ").append(field.getInitialValue()).append("\n");
        } else {
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String buildMethodHeader(Method method){
        StringBuilder stringBuilder = new StringBuilder();
        AccessModifiers accessModifier = method.getMethodAccessModifier();
        StringBuilder access = new StringBuilder();

        if(accessModifier != AccessModifiers.DEFAULT){
            access.append(accessModifier.name().toLowerCase()).append(" ");
        }
        if (method.isStaticMethod()) {
            access.append("static ");
        }
        if (method.isFinalMethod()) {
            access.append("final ");
        }

        stringBuilder.append(".method " ).append(access);
        if (method.isStaticMethod()) {
            access.append("static ");
        }
        stringBuilder.append(method.getMethodName()).append("(");

        String params = method.getParams().stream()
                .map(param -> MyJasminUtils.getType(this.classUnit, param.getType()))
                .collect(Collectors.joining());

        stringBuilder.append(params).append(")").append(MyJasminUtils.getType(this.classUnit, method.getReturnType())).append("\n");

        return stringBuilder.toString();
    }

    public String buildMethodStatements(Method method){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".limit stack 99\n");
        stringBuilder.append(".limit locals 99\n");
        MyJasminInstructionBuilder myJasminInstructionBuilder = new MyJasminInstructionBuilder(method);

        HashMap<String, Instruction> labels = method.getLabels();

        for (Instruction instruction: method.getInstructions()) {
            for(String label: labels.keySet()){
                if(labels.get(label) == instruction){
                    stringBuilder.append(label).append(":\n");
                }
            }

            stringBuilder.append(myJasminInstructionBuilder.buildInstruction(instruction));

            if (instruction.getInstType() == InstructionType.CALL) {
                ElementType returnType = ((CallInstruction) instruction).getReturnType().getTypeOfElement();
                CallType callType = ((CallInstruction) instruction).getInvocationType();
                if (returnType != ElementType.VOID || callType == CallType.invokespecial) {
                    stringBuilder.append(MyJasminInstruction.pop());
                }
            }
        }

        return stringBuilder.toString();
    }
}
