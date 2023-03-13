package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        if(parserResult.getReports().size() > 0){
            System.out.print("Parsing errors:\n");
            for (var report : parserResult.getReports()){
                System.out.println(report);
            }
            return;
        }

        System.out.println(parserResult.getRootNode().toTree());

        TestUtils.noErrors(parserResult.getReports());

        MySymbolTable symbolTable = new MySymbolTable();

        ClassVisitor classVisitor = new ClassVisitor ();
        classVisitor.visit(parserResult.getRootNode());
        symbolTable.setClassName(classVisitor.getClassName());
        symbolTable.setSuper(classVisitor.getSuperClassName());


        ImportVisitor importVisitor = new ImportVisitor();
        importVisitor.visit(parserResult.getRootNode());
        symbolTable.setImports(importVisitor.getImports());

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.visit(parserResult.getRootNode());
        symbolTable.setClassMethods(methodVisitor.getClassMethods());
        symbolTable.setMethodParams(methodVisitor.getMethodsParams());
        symbolTable.setMethodReturnTypes(methodVisitor.getMethodsReturns());
        symbolTable.setLocalVariables(methodVisitor.getLocalVariables());

        FieldVisitor fieldVisitor = new FieldVisitor();
        fieldVisitor.visit(parserResult.getRootNode());
        symbolTable.setClassFields(fieldVisitor.getClassFields());

        System.out.println(symbolTable.print());

        // ... add remaining stages
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // parseArgs



        return config;
    }

}
