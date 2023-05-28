package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class MyJasminInstructionBuilder {
    private final Method method;
    private final String superClass;
    private final MyLabelController labelController;

    public MyJasminInstructionBuilder(Method method, String superClass, MyLabelController labelController) {
        this.method = method;
        this.superClass = superClass;
        this.labelController = labelController;
    }

    // Method to build a jasmin instruction
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
            case GOTO -> ret = buildGoto((GotoInstruction) instruction);
            case BRANCH -> ret = buildBranch((CondBranchInstruction) instruction);
        }
        return ret;
    }

    // Generates the Jasmin instruction for loading a value from a given element, based on its type.
    // Throws a NotImplementedException if the element's type is not supported.
    private String loadOp(Element element) {
        ElementType type = element.getType().getTypeOfElement();

        if(element.isLiteral()) {
            return MyJasminInstruction.iconst(parseInt(((LiteralElement) element).getLiteral()));
        }

        if(type == ElementType.INT32 || type == ElementType.BOOLEAN){
            int reg = MyJasminUtils.getRegister(element, this.method);
            Descriptor descriptor = method.getVarTable().get(((Operand) element).getName());
            ElementType varType = descriptor.getVarType().getTypeOfElement();
            if (varType == ElementType.ARRAYREF){
                return MyJasminUtils.getArray(element, this.method) + MyJasminInstruction.iaload();
            }
            return MyJasminInstruction.iload(reg);
        }

        if(type == ElementType.ARRAYREF || type == ElementType.OBJECTREF || type == ElementType.THIS || type == ElementType.STRING){
            int reg = MyJasminUtils.getRegister(element, this.method);
            return MyJasminInstruction.aload(reg);
        }

        throw new NotImplementedException(element);
    }

    // Generates the Jasmin instruction for storing a value into a given element, based on its type.
    // Throws a NotImplementedException if the element's type is not supported.
    private String storeOp(Element element, String value){
        ElementType type = element.getType().getTypeOfElement();

        if(type == ElementType.INT32 || type == ElementType.BOOLEAN){
            int reg = MyJasminUtils.getRegister(element,this.method);
            Descriptor descriptor = method.getVarTable().get(((Operand) element).getName());
            ElementType varType = descriptor.getVarType().getTypeOfElement();
            if (varType == ElementType.ARRAYREF){
                return MyJasminUtils.getArray(element,this.method) + value + MyJasminInstruction.iastore();
            }
            return value + MyJasminInstruction.istore(reg);
        }

        if(type == ElementType.ARRAYREF || type == ElementType.OBJECTREF || type == ElementType.THIS || type == ElementType.STRING){
            int reg = MyJasminUtils.getRegister(element, this.method);
            return value + MyJasminInstruction.astore(reg);
        }

        throw new NotImplementedException(element);
    }

    // Generates the Jasmin instruction for accessing a field of a class,
    // based on the given field element, class element, and instruction type (GETFIELD or PUTFIELD).
    // Returns the generated instruction as a string.
    // Uses MyJasminUtils to obtain the field's type, class name, and qualified name.
    private String fieldOp(Element fieldElem, Element classElem, InstructionType type){
        String fieldInstruction, inst;
        if(type.equals(InstructionType.GETFIELD)){
            fieldInstruction = "getfield";
        }
        else {
            fieldInstruction = "putfield";
        }

        String fieldType = MyJasminUtils.getType(method.getOllirClass(), fieldElem.getType());
        String className = MyJasminUtils.getQualifiedName(method.getOllirClass(),((ClassType)classElem.getType()).getName());
        String fieldName = ((Operand)fieldElem).getName();

        inst = MyJasminInstruction.fieldOp(fieldInstruction, className, fieldName, fieldType);

        return inst;
    }

    // Builds the Jasmin instruction for returning a value from a method
    private String buildReturn(ReturnInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        if(!instruction.hasReturnValue()) return "\treturn\n";
        Element resultValue = instruction.getOperand();

        stringBuilder.append(loadOp(resultValue));

        Type returnType = method.getReturnType();
        if(returnType.getTypeOfElement() == ElementType.INT32 || returnType.getTypeOfElement() == ElementType.BOOLEAN){
            stringBuilder.append(MyJasminInstruction.ireturn());
        } else {
            stringBuilder.append(MyJasminInstruction.areturn());
        }

        return stringBuilder.toString();

    }

    // Builds the Jasmin instruction for assigning a value to a variable, based on the right-hand side expression
    private String buildAssign(AssignInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();

        Element leftHandMostSymbol = instruction.getDest();
        Instruction rightHandMostSymbol = instruction.getRhs();
        InstructionType instType = rightHandMostSymbol.getInstType();

        if(instType == InstructionType.BINARYOPER){
            BinaryOpInstruction binaryExpression = (BinaryOpInstruction) rightHandMostSymbol;
            OperationType sign = binaryExpression.getOperation().getOpType();

            if(sign == OperationType.ADD || sign == OperationType.SUB || sign == OperationType.MUL || sign == OperationType.DIV){
                String valueSign = "";
                if(sign == OperationType.SUB) valueSign = "-";
                int reg = MyJasminUtils.getRegister(leftHandMostSymbol, this.method);

                Element leftOperand = binaryExpression.getLeftOperand();
                Element rightOperand = binaryExpression.getRightOperand();

                if(!leftOperand.isLiteral() && rightOperand.isLiteral()){

                    int value = MyJasminUtils.getValue(rightOperand, valueSign);
                    if((value >= -128 && value <= 127) &&
                            Objects.equals(((Operand) leftOperand).getName(), ((Operand) leftHandMostSymbol).getName())){

                        return MyJasminInstruction.iinc(reg, value);
                    }

                } else if (leftOperand.isLiteral() && !rightOperand.isLiteral()){
                    int value = MyJasminUtils.getValue(leftOperand, valueSign);
                    if((value >= -128 && value <= 127)  && Objects.equals(((Operand) rightOperand).getName(), ((Operand) leftHandMostSymbol).getName())){
                        return MyJasminInstruction.iinc(reg, value);
                    }
                }
            }
        }

        String rightHandMostSymbolString = buildInstruction(rightHandMostSymbol);
        String result = storeOp(leftHandMostSymbol, rightHandMostSymbolString);
        stringBuilder.append(result);
        return stringBuilder.toString();
    }

    // Builds the Jasmin code for a CALL instruction based on the invocation type.
    private String buildCall(CallInstruction instruction){

        CallType invokeType = instruction.getInvocationType();
        String inst;
        switch (invokeType) {
            case arraylength -> {
                return loadOp(instruction.getFirstArg()) + MyJasminInstruction.arrayLength();
            }
            case NEW -> {
                StringBuilder stringBuilder = new StringBuilder();
                ElementType returnType = instruction.getReturnType().getTypeOfElement();
                if(returnType != ElementType.ARRAYREF){
                    String returnTypeName = ((ClassType)instruction.getReturnType()).getName();
                    inst = MyJasminInstruction.newObject(returnTypeName);
                    stringBuilder.append(inst).append(MyJasminInstruction.dup());
                }
                else {
                    inst = MyJasminInstruction.newArray();
                    stringBuilder.append(loadOp(instruction.getListOfOperands().get(0))).append(inst);
                }
                return stringBuilder.toString();
            }

            case invokestatic -> {
                StringBuilder stringBuilder = new StringBuilder();

                Operand operand = (Operand) instruction.getFirstArg();
                String classNameAux = operand.getName();
                String className = MyJasminUtils.getQualifiedName(method.getOllirClass(), classNameAux);

                String methodName = ((LiteralElement)instruction.getSecondArg()).getLiteral().replace("\"", "");

                ArrayList<Element> params = instruction.getListOfOperands();
                for (Element param : params) {
                    stringBuilder.append(loadOp(param));
                }

                String returnType = MyJasminUtils.getType(method.getOllirClass(), instruction.getReturnType());
                inst = MyJasminInstruction.invokeStaticOp(className, methodName,
                        MyJasminUtils.argTypes(params, this.method), returnType, params.size());

                stringBuilder.append(inst);
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
                String paramTypes = MyJasminUtils.argTypes(params, this.method);
                String returnType = MyJasminUtils.getType(method.getOllirClass(), instruction.getReturnType());

                if(callType == CallType.invokespecial)
                    inst = MyJasminInstruction.invokeSpecialOp(className, methodName, paramTypes, returnType, params.size());
                else inst = MyJasminInstruction.invokeVirtualOp(className, methodName, paramTypes, returnType, params.size());

                stringBuilder.append(inst);

                return stringBuilder.toString();
            }
            default -> throw new NotImplementedException(instruction);
        }
    }

    // Builds a binary operation instruction by loading the left and right operands, and applying the corresponding
    // arithmetic operator to them.
    private String buildBinaryOp (BinaryOpInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        OperationType opType = instruction.getOperation().getOpType();

        String leftOperandString = loadOp(leftOperand);
        String rightOperandString = loadOp(rightOperand);

        if(opType == OperationType.LTH || opType == OperationType.GTE) {
            stringBuilder.append(leftOperandString);
            String firstLabel = "", secondLabel = "";

            if(opType == OperationType.LTH){
                firstLabel = "LTH_" + this.labelController.nextLabel();
                secondLabel = "LTH_" + this.labelController.nextLabel();
            } else {
                firstLabel = "GTE_" + this.labelController.nextLabel();
                secondLabel = "GTE_" + this.labelController.nextLabel();
            }
            
            if (rightOperand.isLiteral() && ((LiteralElement) rightOperand).getLiteral().equals("0"))
                if(opType == OperationType.LTH)
                    stringBuilder.append(MyJasminInstruction.iflt(firstLabel));
                else stringBuilder.append(MyJasminInstruction.ifge(firstLabel));

            else {
                stringBuilder.append(rightOperandString);
                if(opType == OperationType.LTH)
                    stringBuilder.append(MyJasminInstruction.ifIcmplt(firstLabel));
                else stringBuilder.append(MyJasminInstruction.ifIcmpge(firstLabel));
            }

            stringBuilder.append(MyJasminInstruction.iconst(0));
            stringBuilder.append(MyJasminInstruction.goTo(secondLabel));
            stringBuilder.append(firstLabel).append(":\n");
            stringBuilder.append(MyJasminInstruction.iconst(1));
            stringBuilder.append(MyJasminInstruction.goTo(secondLabel));
            stringBuilder.append(secondLabel).append(":\n");

        } else {
            stringBuilder.append(leftOperandString);
            stringBuilder.append(rightOperandString);
            String inst = MyJasminInstruction.arithOp(opType);
            stringBuilder.append(inst);
        }

        return stringBuilder.toString();
    }

    // Builds a string representation of a unary operation instruction, which performs a unary operation on an operand.
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

    // Builds the Jasmin code for a PutFieldInstruction.
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

    // Builds the Jasmin code for a GetFieldInstruction.
    private String buildGetField(GetFieldInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();

        Element classElem = instruction.getFirstOperand();
        Element fieldElem = instruction.getSecondOperand();

        stringBuilder.append(loadOp(classElem));
        stringBuilder.append(fieldOp(fieldElem, classElem, InstructionType.GETFIELD));

        return stringBuilder.toString();
    }

    // Builds the Jasmin instruction for a SingleOpInstruction
    private String buildSingleOp(SingleOpInstruction instruction){
        Element operand = instruction.getSingleOperand();
        return loadOp(operand);
    }

    // Builds the Jasmin instruction for a GoToInstruction
    private String buildGoto(GotoInstruction instruction){
        return MyJasminInstruction.goTo(instruction.getLabel());
    }

    // Build the Jasmin code for a IfNeInstruction
    private String buildCondition(Instruction instruction, String label){
        return buildInstruction(instruction) + MyJasminInstruction.ifne(label);
    }

    // Builds the Jasmin code for a CondBranchInstruction
    private String buildBranch(CondBranchInstruction instruction){
        StringBuilder stringBuilder = new StringBuilder();
        Instruction cond = instruction.getCondition();
        String label = instruction.getLabel();
        InstructionType type = cond.getInstType();

        if(type == InstructionType.BINARYOPER) {
            Element leftOperand = ((BinaryOpInstruction) cond).getLeftOperand();
            Element rightOperand = ((BinaryOpInstruction) cond).getRightOperand();
            OperationType opType = ((BinaryOpInstruction) cond).getOperation().getOpType();

            if (opType == OperationType.LTH || opType == OperationType.GTE) {
                stringBuilder.append(loadOp(leftOperand));
                if (rightOperand.isLiteral() && ((LiteralElement) rightOperand).getLiteral().equals("0")) {

                    if (opType == OperationType.LTH)
                        stringBuilder.append(MyJasminInstruction.iflt(label));
                    else stringBuilder.append(MyJasminInstruction.ifge(label));

                } else {
                    stringBuilder.append(loadOp(rightOperand));
                    if(opType == OperationType.LTH)
                        stringBuilder.append(MyJasminInstruction.ifIcmplt(label));
                    else stringBuilder.append(MyJasminInstruction.ifIcmpge(label));
                }

            } else {
                stringBuilder.append(buildCondition(cond, label));
            }
        } else {
            stringBuilder.append(buildCondition(cond, label));
        }

        return stringBuilder.toString();
    }

}
