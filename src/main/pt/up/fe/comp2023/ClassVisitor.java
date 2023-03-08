package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClassVisitor extends AJmmVisitor<Void,Void> {
    private String className;
    private String superClassName;

    protected void buildVisitor() {
        addVisit ("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);

    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return superClassName;
    }
    private Void dealWithProgram(JmmNode jmmNode, Void v) {
        for(JmmNode child: jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "Class")) {
                visit(child, null);
                return null;
            }
        }
        return null;
    }

    private Void dealWithClass (JmmNode jmmNode, Void v) {
        className = jmmNode.get("name");
        if(jmmNode.getAttributes().contains("extendsName")){
            superClassName = jmmNode.get("extendsName");
        }
        else {
            superClassName = null;
        }


        return null;
    }

}
