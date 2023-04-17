package pt.up.fe.comp2023.ollir;

public class OllirCodeStruct {
    public String prefixCode;
    public String value;

    public OllirCodeStruct(String prefixCode, String value) {
        this.prefixCode = prefixCode;
        this.value = value;
    }

    public OllirCodeStruct() {
        this.prefixCode = "";
        this.value = "";
    }

}
