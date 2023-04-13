package pt.up.fe.comp2023.jasmin;


import org.specs.comp.ollir.ElementType;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import org.specs.comp.ollir.*;

public class MyJasminUtils {
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
                typeBuilder.append("L").append(getClassName(context, className)).append(";");
            }
            default -> throw new NotImplementedException("Type not implemented: " + elementType);
        }

        return typeBuilder.toString();
    }

    public static String getClassName(ClassUnit context, String className) {
        if(className.equals("this")) {
            return context.getClassName();
        }
        for(String importName : context.getImports()) {
            if(importName.endsWith(className)) {
                return importName.replaceAll("\\.", "/");
            }
        }

        return className;
    }
}
