package pt.up.fe.comp2023.jasmin;


import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;

public class MyJasminUtils {

    // Returns the Jasmin type string of a given OLLIR type.
    public static String getType(ClassUnit context, Type type) {
        StringBuilder typeBuilder = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();
        if (elementType == ElementType.ARRAYREF){
            typeBuilder.append("[");
            elementType = ((ArrayType) type).getArrayType();
        }
        switch (elementType) {
            case INT32 -> typeBuilder.append("I");
            case BOOLEAN -> typeBuilder.append("Z");
            case STRING -> typeBuilder.append("Ljava/lang/String;");
            case VOID -> typeBuilder.append("V");
            case OBJECTREF -> {
                assert type instanceof ClassType;
                String className = ((ClassType) type).getName();
                typeBuilder.append("L").append(getQualifiedName(context, className)).append(";");
            }
            default -> throw new NotImplementedException("Type not implemented: " + elementType);
        }

        return typeBuilder.toString();
    }

    // Returns the concatenated argument types of a method in Jasmin format.
    public static String argTypes(ArrayList<Element> args, Method method){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(");
        for(Element arg: args){
            stringBuilder.append(getType(method.getOllirClass(), arg.getType()));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    // Returns the fully qualified class name in the Jasmin format given the class name and its imports.
    public static String getQualifiedName(ClassUnit context, String className) {
        for (String importString : context.getImports()) {
            var split = importString.split("\\.");
            String lastImportName;

            if (split.length == 0) {
                lastImportName = importString;
            } else {
                lastImportName = split[split.length - 1];
            }
            if (lastImportName.equals(className)) {
                return importString.replace('.', '/');
            }
        }
        return context.getClassName().replace("\\.", "/");
    }

    // Returns the virtual register of a given element in the OLLIR code.
    public static int getRegister(Element element, Method method){
        HashMap<String, Descriptor> varTable = method.getVarTable();
        return varTable.get(((Operand) element).getName()).getVirtualReg();
    }

    // Generates the Jasmin code to load an element from an array operand.
    public static String getArray(Element element, Method method){
        int arrayReg = getRegister(element, method);
        Element first = ((ArrayOperand) element).getIndexOperands().get(0);
        int firstReg = getRegister(first, method);

        return MyJasminInstruction.aload(arrayReg) + MyJasminInstruction.iload(firstReg);
    }

    public static int getValue(Element element, String valueSign) {
        String literalValue = valueSign + ((LiteralElement) element).getLiteral();
        return Integer.parseInt(literalValue);
    }
}
