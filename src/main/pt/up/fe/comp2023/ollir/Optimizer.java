package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.ollir.OllirGenerator;
import pt.up.fe.comp2023.optimize.ConstantFoldingVisitor;
import pt.up.fe.comp2023.optimize.ConstantPropagationVisitor;

import java.util.Collections;
import java.util.HashMap;

public class Optimizer implements JmmOptimization{
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        OllirGenerator ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());
        String code = ollirGenerator.getCode();
        System.out.println(code);
        return new OllirResult(semanticsResult, code, Collections.emptyList());
    }

    @Override
    public  JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult){

        if(semanticsResult.getConfig().getOrDefault("optimize", "false").equals("false"))
            return semanticsResult;

        ConstantPropagationVisitor constantPropagationVisitor;
        ConstantFoldingVisitor constantFoldingVisitor;

        do{
            constantPropagationVisitor = new ConstantPropagationVisitor();
            constantFoldingVisitor = new ConstantFoldingVisitor();
            constantPropagationVisitor.visit(semanticsResult.getRootNode(), new HashMap<>());
            constantFoldingVisitor.visit(semanticsResult.getRootNode(),"");
        }while(constantPropagationVisitor.hasChanged() || constantFoldingVisitor.hasChanged());
        System.out.println("OPTIMIZED");
        System.out.println(semanticsResult.getRootNode().toTree());

        return semanticsResult;
    }
}