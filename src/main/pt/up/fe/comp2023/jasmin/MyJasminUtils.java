package pt.up.fe.comp2023.jasmin;


import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

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
                typeBuilder.append("L").append(getQualifiedName(context, className)).append(";");
            }
            default -> throw new NotImplementedException("Type not implemented: " + elementType);
        }

        return typeBuilder.toString();
    }

    public static String getQualifiedName(ClassUnit context, String className) {
        for (String importString : context.getImports()) {
            var splitImports = importString.split("\\.");
            String lastName;
            if (splitImports.length == 0) {
                lastName = importString;
            } else {
                lastName = splitImports[splitImports.length - 1];
            }
            if (lastName.equals(className)) {
                return importString.replace('.', '/');
            }
        }
        return context.getClassName().replace("\\.", "/");
    }
}
