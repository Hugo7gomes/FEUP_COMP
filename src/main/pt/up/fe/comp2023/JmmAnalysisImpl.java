package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JmmAnalysisImpl implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        if (TestUtils.getNumReports(jmmParserResult.getReports(), ReportType.ERROR) > 0L) {
            return null;
        }
        if (jmmParserResult.getRootNode() == null) {
            return null;
        }

        JmmNode node = jmmParserResult.getRootNode();

        List<Report> reports = new ArrayList<>();

        MySymbolTable symbolTable = new MySymbolTable();
        symbolTable.create(jmmParserResult);

        ExpressionAnalyser expressionAnalyser = new ExpressionAnalyser(symbolTable,reports);
        expressionAnalyser.visit(node,null);

        return new JmmSemanticsResult(jmmParserResult, symbolTable, reports);


    }
}
