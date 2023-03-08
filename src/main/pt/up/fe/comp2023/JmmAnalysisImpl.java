package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.ReportType;

import java.util.Collections;

public class JmmAnalysisImpl implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        if (TestUtils.getNumReports(jmmParserResult.getReports(), ReportType.ERROR) > 0L) {
            return null;
        } else if (jmmParserResult.getRootNode() == null) {
            return null;
        } else {
            MySymbolTable symbolTable = new MySymbolTable();

            ClassVisitor classVisitor = new ClassVisitor ();
            classVisitor.visit(jmmParserResult.getRootNode());
            symbolTable.setClassName(classVisitor.getClassName());
            symbolTable.setSuper(classVisitor.getSuperClassName());


            ImportVisitor importVisitor = new ImportVisitor();
            importVisitor.visit(jmmParserResult.getRootNode());
            symbolTable.setImports(importVisitor.getImports());

            MethodVisitor methodVisitor = new MethodVisitor();
            methodVisitor.visit(jmmParserResult.getRootNode());
            symbolTable.setClassMethods(methodVisitor.getClassMethods());
            symbolTable.setMethodParams(methodVisitor.getMethodsParams());
            symbolTable.setMethodReturnTypes(methodVisitor.getMethodsReturns());
            symbolTable.setLocalVariables(methodVisitor.getLocalVariables());

            FieldVisitor fieldVisitor = new FieldVisitor();
            fieldVisitor.visit(jmmParserResult.getRootNode());
            symbolTable.setClassFields(fieldVisitor.getClassFields());

            return new JmmSemanticsResult(jmmParserResult, symbolTable, Collections.emptyList());
        }

    }
}
