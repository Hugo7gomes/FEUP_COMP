package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class MyJasminBackend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        ClassUnit classUnit = ollirResult.getOllirClass();
        ConvertOllirToJasmin converter =  new ConvertOllirToJasmin(classUnit);
        String jasmin = converter.toJasmin();


        return null;
    }
}
