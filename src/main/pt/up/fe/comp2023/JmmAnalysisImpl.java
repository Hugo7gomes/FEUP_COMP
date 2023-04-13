package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2023.semantics.ExpressionAnalyser;
import pt.up.fe.comp2023.semantics.ProgramAnalyser;

import java.util.ArrayList;
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

        System.out.println(jmmParserResult.getRootNode().toTree());
        ProgramAnalyser programAnalyser = new ProgramAnalyser(symbolTable,reports);
        programAnalyser.visit(node, "");

        return new JmmSemanticsResult(jmmParserResult, symbolTable, reports);


    }
}
