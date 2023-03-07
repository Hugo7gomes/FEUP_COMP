package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FieldVisitor extends AJmmVisitor<Void,Void> {

    private List<Symbol> classFields = new ArrayList<Symbol>();
    public List<Symbol> getClassFields() {
        return classFields;
    }
    protected void buildVisitor() {
        addVisit ("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Declaration", this::dealWithDeclaration);
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
        for(JmmNode child: jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "Declaration")) {
                visit(child, null);
            }
        }
        return null;
    }

    private Void dealWithDeclaration(JmmNode jmmNode, Void v) {
        JmmNode child = jmmNode.getChildren().get(0);
        String typeName = child.get("name");
        Boolean isArray = (Boolean) child.getObject("isArray");
        String fieldName = jmmNode.get("value");
        Type type = new Type(typeName,isArray);
        Symbol symbol = new Symbol(type,fieldName);
        classFields.add(symbol);

        return null;
    }
}
