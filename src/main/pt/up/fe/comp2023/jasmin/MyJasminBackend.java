package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyJasminBackend implements JasminBackend {

    ClassUnit classUnit = null;
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        try {
            this.classUnit = ollirResult.getOllirClass();
            this.classUnit.buildVarTables();
            this.classUnit.buildCFGs();
            this.classUnit.checkMethodLabels();

            String jasmin = new MyOllirToJasminConverter(this.classUnit).toJasmin();
            List<Report> reports = new ArrayList<>();

            if(ollirResult.getConfig().getOrDefault("debug", "false").equals("true")) {
                System.out.println("Jasmin:\n\n");
                System.out.println(jasmin);
            }

            System.out.println(jasmin);

            JasminResult jasminResult = new JasminResult(ollirResult, jasmin, reports);
            jasminResult.run();
            return jasminResult;

        } catch (OllirErrorException e){
            return new JasminResult(this.classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1,
                            "Jasmin generation exception: " + e.getMessage(), e)));
        }
    }
}
