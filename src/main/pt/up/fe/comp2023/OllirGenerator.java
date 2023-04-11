package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final SymbolTable symbolTable;
    private final StringBuilder codeOllir;

    public OllirGenerator(SymbolTable symbolTable) {
        this.codeOllir = new StringBuilder();
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {

    }
}
