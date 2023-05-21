package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.ollir.OllirGenerator;

import java.util.Collections;

public class Optimizer implements JmmOptimization{
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        System.out.println(semanticsResult.getRootNode().toTree());
        OllirGenerator ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());
        String code = ollirGenerator.getCode();
        System.out.println(code);
        return new OllirResult(semanticsResult, code, Collections.emptyList());
    }
}