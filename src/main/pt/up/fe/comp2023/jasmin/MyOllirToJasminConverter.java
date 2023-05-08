package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MyOllirToJasminConverter {

    private final ClassUnit classUnit;
    private String superClass;
    private final MyLabelController labelController;

    public MyOllirToJasminConverter(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.labelController = new MyLabelController();
    }
    public String toJasmin() {

        StringBuilder stringBuilder = new StringBuilder();

        // Class
        // .class  <access-spec> <class-name>
        stringBuilder.append(".class public " ).append(classUnit.getClassName()).append("\n");

        // Super class
        // .super  <class-name>
        this.superClass = classUnit.getSuperClass() != null ? classUnit.getSuperClass() : "java/lang/Object";
        stringBuilder.append(".super " ).append(superClass).append("\n");

        // Fields
        for(Field field: this.classUnit.getFields()){
            // .field <access-spec> <field-name> <descriptor>
            stringBuilder.append(buildField(field));
        }

        // Methods
        for(Method method: this.classUnit.getMethods()){
            // .method <access-spec> <method-spec>
            //     <statements>
            // .end method
            stringBuilder.append(buildMethodHeader(method));
            stringBuilder.append(buildMethodStatements(method));
            stringBuilder.append(".end method\n\n");
        }

        return stringBuilder.toString();
    }

    public String buildField(Field field){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".field ");

        // Field Access
        AccessModifiers accessModifier = field.getFieldAccessModifier();

        if(accessModifier != AccessModifiers.DEFAULT){
            stringBuilder.append(accessModifier.name().toLowerCase()).append(" ");
        }

        if (field.isStaticField()) {
            stringBuilder.append("static ");
        }
        if (field.isFinalField()) {
            stringBuilder.append("final ");
        }


        // Field Name
        stringBuilder.append(field.getFieldName()).append(" ");

        // Field Return Type
        stringBuilder.append(MyJasminUtils.getType(this.classUnit, field.getFieldType()));
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    public String buildMethodHeader(Method method){
        StringBuilder stringBuilder = new StringBuilder();

        // Method Access
        AccessModifiers accessModifier = method.getMethodAccessModifier();

        if(accessModifier == AccessModifiers.DEFAULT) {
            stringBuilder.append(".method public <init>(");

        } else {

            stringBuilder.append(".method ");
            stringBuilder.append(accessModifier.name().toLowerCase()).append(" ");
            if (method.isStaticMethod()) {
                stringBuilder.append("static ");
            }
            if (method.isFinalMethod()) {
                stringBuilder.append("final ");
            }

            // Method Name
            stringBuilder.append(method.getMethodName()).append("(");
        }

        // Method Parameters
        String params = method.getParams().stream()
                .map(param -> MyJasminUtils.getType(this.classUnit, param.getType()))
                .collect(Collectors.joining());

        stringBuilder.append(params).append(")");

        // Method Return Type
        stringBuilder.append(MyJasminUtils.getType(this.classUnit, method.getReturnType())).append("\n");

        return stringBuilder.toString();
    }

    public String buildMethodStatements(Method method){
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder methodBody = new StringBuilder();
        HashMap<String, Instruction> labelMap = method.getLabels();

        // Method Instructions
        MyJasminInstructionBuilder myJasminInstructionBuilder = new MyJasminInstructionBuilder(method, this.superClass, this.labelController);
        MyJasminInstruction.limitController.resetStack();
        MyJasminInstruction.limitController.updateRegisters(method.getParams().size() + 1);

        for (Instruction instruction: method.getInstructions()) {
            for(String label: labelMap.keySet()){
                if(labelMap.get(label).equals(instruction))
                    methodBody.append(label).append(":\n");
            }
            methodBody.append(myJasminInstructionBuilder.buildInstruction(instruction));

            if (instruction.getInstType() == InstructionType.CALL) {
                ElementType returnType = ((CallInstruction) instruction).getReturnType().getTypeOfElement();
                CallType callType = ((CallInstruction) instruction).getInvocationType();
                if (!method.isConstructMethod() && (returnType != ElementType.VOID || callType == CallType.invokespecial)) {
                    methodBody.append(MyJasminInstruction.pop());
                }
            }
        }

        // Method Stack and Local Limits
        if(method.getMethodAccessModifier() != AccessModifiers.DEFAULT){
            stringBuilder.append("\t.limit stack ").append(MyJasminInstruction.limitController.getStackLimit()).append("\n");
            stringBuilder.append("\t.limit locals ").append(MyJasminInstruction.limitController.getLocalsLimit()).append("\n");
        }
        stringBuilder.append(methodBody);

        return stringBuilder.toString();
    }
}
