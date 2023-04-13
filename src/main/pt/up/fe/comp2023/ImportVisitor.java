package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ImportVisitor extends AJmmVisitor<Void,Void> {

    private List<String> imports = new ArrayList<String>();

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImport);
    }

    private Void dealWithImport(JmmNode jmmNode, Void v) {
        String importName = jmmNode.get("name");
        List<String> importValues = (List<String>) jmmNode.getObject("values");
        StringBuilder finalString = new StringBuilder();
        finalString.append(importName);
        if(importValues != null) {
            for (String importValue : importValues) {
                finalString.append(".").append(importValue);
            }
        }
        imports.add(finalString.toString());
        return null;
    }

    private Void dealWithProgram(JmmNode jmmNode, Void v) {
        for(JmmNode child: jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "Class")) {
                return null;
            }
            visit(child,null);
        }

        return null;
    }
    public List<String> getImports() {
        return imports;
    }
}
