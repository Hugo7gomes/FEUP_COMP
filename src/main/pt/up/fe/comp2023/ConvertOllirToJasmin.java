package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;

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

        return null;
    }

    public String buildField(Field field){
        StringBuilder stringBuilder = new StringBuilder();

    }

}
