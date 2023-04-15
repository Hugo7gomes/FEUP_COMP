package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class MyJasminInstructionBuilder {
    private final Method method;
    private final String superClass;

    public MyJasminInstructionBuilder(Method method, String superClass) {
        this.method = method;
        this.superClass = superClass;
    }

    //method to build a jasmin instruction
    public String buildInstruction(Instruction instruction){
        InstructionType inst = instruction.getInstType();
        String ret = "";
        switch (inst) {
            case ASSIGN -> ret =buildAssign((AssignInstruction) instruction);
            case CALL -> ret =buildCall((CallInstruction) instruction);
            case RETURN -> ret = buildReturn((ReturnInstruction) instruction);
            case PUTFIELD -> ret = buildPutField((PutFieldInstruction) instruction);
            case GETFIELD -> ret = buildGetField((GetFieldInstruction) instruction);
            case UNARYOPER -> ret = buildUnaryOp((UnaryOpInstruction) instruction);
            case BINARYOPER -> ret = buildBinaryOp((BinaryOpInstruction) instruction);
            case NOPER -> ret = buildSingleOp((SingleOpInstruction) instruction);
        }
        return ret;
    }

    //method to get a variable in the method's var table
    private Descriptor lookup(Element element){
        return method.getVarTable().get(((Operand) element).getName());
    }

    //method to get arguments types of a method
    private String argTypes(ArrayList<Element> args){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for(Element arg: args){
            stringBuilder.append(MyJasminUtils.getType(method.getOllirClass(), arg.getType()));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    //method to return the virtual register of an element
    private int register(Element element){
        return lookup(element).getVirtualReg();
    }

    //
    private String getArray(Element element){
        int arrayReg = register(element);
        Element index = ((ArrayOperand) element).getIndexOperands().get(0);
        int indexReg = register(index);
        return MyJasminInstruction.aload(arrayReg) + MyJasminInstruction.iload(indexReg);
    }

    private String loadOp(Element element) {
        ElementType type = element.getType().getTypeOfElement();

        if(element.isLiteral()) {
            return MyJasminInstruction.iconst(parseInt(((LiteralElement) element).getLiteral()));
        }

        if(type == ElementType.INT32 || type == ElementType.BOOLEAN){
            int reg = register(element);
            ElementType varType = lookup(element).getVarType().getTypeOfElement();
            if (varType == ElementType.ARRAYREF){
                return getArray(element) + MyJasminInstruction.iaload();
            }
            return MyJasminInstruction.iload(reg);
        }

        if(type == ElementType.ARRAYREF || type == ElementType.OBJECTREF || type == ElementType.THIS || type == ElementType.STRING){
            int reg = register(element);
            return MyJasminInstruction.aload(reg);
        }

        throw new NotImplementedException(element);
    }

    private String storeOp(Element element, String value){
        ElementType type = element.getType().getTypeOfElement();

        if(type == ElementType.INT32 || type == ElementType.BOOLEAN || type == ElementType.STRING){
            int reg = register(element);
            ElementType varType = lookup(element).getVarType().getTypeOfElement();
            if (varType == ElementType.ARRAYREF){
                return getArray(element) + value + MyJasminInstruction.iastore();
            }
            return value + MyJasminInstruction.istore(reg);
        }

        if(type == ElementType.ARRAYREF || type == ElementType.OBJECTREF || type == ElementType.THIS){
            int reg = register(element);
            return value + MyJasminInstruction.astore(reg);
        }

        throw new NotImplementedException(element);
    }

    private String buildReturn(ReturnInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        if(!instruction.hasReturnValue()) return "\treturn\n";
        Element resultValue = instruction.getOperand();
        stringBuilder.append(loadOp(resultValue));
        Type returnType = method.getReturnType();
        if(returnType.getTypeOfElement() == ElementType.INT32 || returnType.getTypeOfElement() == ElementType.BOOLEAN){
            stringBuilder.append("\tireturn\n");
        } else {
            stringBuilder.append("\tareturn\n");
        }
        return stringBuilder.toString();

    }
    private String buildAssign(AssignInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element leftHandMostSymbol = instruction.getDest();
        Instruction rightHandMostSymbol = instruction.getRhs();

        if(rightHandMostSymbol.getInstType() == InstructionType.BINARYOPER){
            BinaryOpInstruction binaryExpression = (BinaryOpInstruction) rightHandMostSymbol;
            OperationType sign = binaryExpression.getOperation().getOpType();
            if(sign == OperationType.ADD || sign == OperationType.SUB || sign == OperationType.MUL || sign == OperationType.DIV){
                String valueSign = "";
                if(sign == OperationType.SUB) valueSign = "-";
                int reg = register(leftHandMostSymbol);
                Element leftOperand = binaryExpression.getLeftOperand();
                Element rightOperand = binaryExpression.getRightOperand();
                if(!leftOperand.isLiteral() && rightOperand.isLiteral()){
                    if(Objects.equals(((Operand) leftOperand).getName(), ((Operand) leftHandMostSymbol).getName())){
                        String value = valueSign + ((LiteralElement) rightOperand).getLiteral();
                        return MyJasminInstruction.iinc(reg, value);
                    }
                }
                else if (leftOperand.isLiteral() && !rightOperand.isLiteral()){
                    if(Objects.equals(((Operand) rightOperand).getName(), ((Operand) leftHandMostSymbol).getName())){
                        String value = valueSign + ((LiteralElement) leftOperand).getLiteral();
                        return MyJasminInstruction.iinc(reg, value);
                    }
                }
            }
        }
        String rightHandMostSymbolString = buildInstruction(rightHandMostSymbol);
        String res = storeOp(leftHandMostSymbol, rightHandMostSymbolString);
        stringBuilder.append(res);

        return stringBuilder.toString();
    }

    private String buildCall(CallInstruction instruction){
        switch (instruction.getInvocationType()) {
            case NEW -> {
                StringBuilder stringBuilder = new StringBuilder();
                ElementType returnType = instruction.getReturnType().getTypeOfElement();
                if(returnType != ElementType.ARRAYREF){
                    String returnTypeName = ((ClassType)instruction.getReturnType()).getName();
                    stringBuilder.append(MyJasminInstruction.newObject(returnTypeName)).append(MyJasminInstruction.dup());
                }
                else {
                    stringBuilder.append(loadOp(instruction.getListOfOperands().get(0)));
                    stringBuilder.append(MyJasminInstruction.newArray());
                }
                return stringBuilder.toString();
            }
            case arraylength -> {
                return loadOp(instruction.getFirstArg()) + MyJasminInstruction.arrayLength();
            }

            case invokestatic -> {
                StringBuilder stringBuilder = new StringBuilder();
                CallType callType = instruction.getInvocationType();

                Operand operand = (Operand) instruction.getFirstArg();
                String classNameAux = operand.getName();
                String className = MyJasminUtils.getQualifiedName(method.getOllirClass(), classNameAux);

                String methodName = ((LiteralElement)instruction.getSecondArg()).getLiteral().replace("\"", "");

                Type returnType = instruction.getReturnType();

                ArrayList<Element> params = instruction.getListOfOperands();
                for (Element param : params) {
                    stringBuilder.append(loadOp(param));
                }

                stringBuilder.append(MyJasminInstruction.invokeOp(callType, className, methodName,argTypes(params), MyJasminUtils.getType(method.getOllirClass(), returnType)));
                return stringBuilder.toString();
            }

            case invokespecial, invokevirtual -> {
                StringBuilder stringBuilder = new StringBuilder();
                String className, classNameAux;

                if(method.isConstructMethod() && instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS){
                    stringBuilder.append("\taload_0\n");
                    stringBuilder.append("\tinvokenonvirtual ");
                    stringBuilder.append(this.superClass).append("/<init>()V").append("\n\treturn\n");

                    return stringBuilder.toString();
                }

                CallType callType = instruction.getInvocationType();
                stringBuilder.append(loadOp(instruction.getFirstArg()));

                ArrayList<Element> params = instruction.getListOfOperands();
                for (Element param : params) {
                    stringBuilder.append(loadOp(param));
                }

                ClassType classType = (ClassType) instruction.getFirstArg().getType();
                classNameAux = classType.getName();
                className = MyJasminUtils.getQualifiedName(method.getOllirClass(), classNameAux);

                String methodName = ((LiteralElement)instruction.getSecondArg()).getLiteral().replace("\"", "");
                Type returnType = instruction.getReturnType();

                stringBuilder.append(MyJasminInstruction.invokeOp(callType, className, methodName, argTypes(params), MyJasminUtils.getType(method.getOllirClass(), returnType)));

                return stringBuilder.toString();
            }
            default -> throw new NotImplementedException(instruction);
        }
    }

    private String buildBinaryOp (BinaryOpInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        OperationType opType = instruction.getOperation().getOpType();

        String leftOperandString = loadOp(leftOperand);
        String rightOperandString = loadOp(rightOperand);

        if(opType == OperationType.LTH){
            stringBuilder.append(leftOperandString);
        } else {
            stringBuilder.append(leftOperandString);
            stringBuilder.append(rightOperandString);
            stringBuilder.append(MyJasminInstruction.arithOp(opType));
        }

        return stringBuilder.toString();
    }

    private String buildUnaryOp(UnaryOpInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element operand = instruction.getOperand();
        OperationType opType = instruction.getOperation().getOpType();
        if(opType == OperationType.NOTB){
            stringBuilder.append(MyJasminInstruction.iconst(1));
            stringBuilder.append(loadOp(operand));
            stringBuilder.append(MyJasminInstruction.arithOp(OperationType.SUB));
        }
        return stringBuilder.toString();
    }

    private String fieldOp(Element fieldElem, Element classElem, InstructionType type){
        MyJasminInstruction.FieldInstructionType fieldInstruction;
        if(type.equals(InstructionType.GETFIELD)){
            fieldInstruction = MyJasminInstruction.FieldInstructionType.GETFIELD;
        }
        else {
            fieldInstruction = MyJasminInstruction.FieldInstructionType.PUTFIELD;
        }
        String className = MyJasminUtils.getQualifiedName(method.getOllirClass(),((ClassType)classElem.getType()).getName());
        String fieldName = ((Operand)fieldElem).getName();
        String fieldType = MyJasminUtils.getType(method.getOllirClass(), fieldElem.getType());
        return MyJasminInstruction.fieldOp(fieldInstruction, className, fieldName, fieldType);
    }

    private String buildPutField(PutFieldInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element classElem = instruction.getFirstOperand();
        Element fieldElem = instruction.getSecondOperand();
        Element valueElem = instruction.getThirdOperand();
        stringBuilder.append(loadOp(classElem));
        stringBuilder.append(loadOp(valueElem));
        stringBuilder.append(fieldOp(fieldElem, classElem, InstructionType.PUTFIELD));

        return stringBuilder.toString();
    }

    private String buildGetField(GetFieldInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element classElem = instruction.getFirstOperand();
        Element fieldElem = instruction.getSecondOperand();
        stringBuilder.append(loadOp(classElem));
        stringBuilder.append(fieldOp(fieldElem, classElem, InstructionType.GETFIELD));

        return stringBuilder.toString();
    }


    private String buildSingleOp(SingleOpInstruction instruction){
        Element operand = instruction.getSingleOperand();
        return loadOp(operand);
    }
}
