grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDeclaration)* classDeclaration EOF;

importDeclaration
    : ('import' ID ('.'ID)* ';') #Import;

classDeclaration
    : 'class' name=ID ('extends' extendsName=ID)? '{'(varDeclaration)* (methodDeclaration)*'}' #Class;

varDeclaration
    : type value=ID';' #Declaration;

methodDeclaration
    : ('public')? type value=ID '(' (type value=ID (',' type value=ID)*)? ')' '{' (varDeclaration)*
    (statement)* 'return' expression ';' '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '['']' ID ')' '{' (varDeclaration)* (statement)* '}';

type
    : 'int' '['']' #ArrayType
    | 'boolean' #BooleanType
    | 'int' #IntegerType
    | 'String' #StringType
    | ID #IdentifierType;

statement
    : '{'(statement)*'}' #Stmt
    | 'if' '(' expression ')' statement 'else' statement #IfElseStmt
    | 'while' '(' expression ')' statement #WhileStmt
    | expression ';' #ExprStmt
    | ID '=' expression ';' #Assignment
    | ID '[' expression ']' '=' expression ';' #Assignment;

expression
    : '('expression')' #Parenthesis
    | expression '[' expression ']' #Indexing
    | expression'.'op='length' #Method
    | expression'.'name=ID'('(expression(','expression)*)?')' #Method
    | op='!'expression #UnaryOp
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op='<' expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | 'new' 'int' '['expression']' #Instantiation
    | 'new' ID'('')' #Instantiation
    | value=INTEGER #Integer
    | value='true' #Boolean
    | value='false' #Boolean
    | value=ID #Identifier
    | value='this' #This;

