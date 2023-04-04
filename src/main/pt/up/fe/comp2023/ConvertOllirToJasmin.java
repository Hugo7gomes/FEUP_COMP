package pt.up.fe.comp2023;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Method;

public class ConvertOllirToJasmin {

    private ClassUnit classUnit;

    public ConvertOllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }
    public String toJasmin() {
        this.classUnit.buildVarTables();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(".class public " ).append(classUnit.getClassName()).append("\n");

        String superClass = classUnit.getSuperClass();
        if (superClass != null) {
            stringBuilder.append(".super " ).append(superClass).append("\n");
        } else {
            stringBuilder.append(".super java/lang/Object").append("\n\n");
        }

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
            stringBuilder.append(buildMethod(method));
        }

        return null;
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

    public String buildMethod(Method method, LabelTracker labelTracker){
        StringBuilder stringBuilder = new StringBuilder();
        AccessModifiers accessModifier = method.getMethodAccessModifier();
        StringBuilder access = new StringBuilder();

        if(accessModifier != AccessModifiers.DEFAULT){
            access.append(accessModifier.name().toLowerCase()).append(" ");
        }
        stringBuilder.append(".method " ).append(access);
        if (method.isStaticMethod()) {
            access.append("static ");
        }

        stringBuilder.append(method.getMethodName()).append(" ");

        

        stringBuilder.append(MyJasminUtils.getType(this.classUnit, method.getReturnType()));

        stringBuilder.append("\n");
        stringBuilder.append(".limit stack 100\n");
        stringBuilder.append(".limit locals 100\n");

        stringBuilder.append("\n\n\n");

        stringBuilder.append(".end method\n");

        return stringBuilder.toString();
    }

}
