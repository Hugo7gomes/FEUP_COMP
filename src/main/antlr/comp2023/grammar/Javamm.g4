grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0] | [1-9][0-9]*;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
COMMENT : ('/*' .*? '*/'| '//' ~[\r\n]*) -> skip;

program
    : (importDeclaration)* classDeclaration EOF;

importDeclaration
    : ('import' name=ID ('.'values+=ID)* ';') #Import;

classDeclaration
    : 'class' name=ID ('extends' extendsName=ID)? '{'(varDeclaration)* (methodDeclaration)*'}' #Class;

varDeclaration
    : ('public')? ('private')? type value=ID';' #Declaration;

methodDeclaration
    : ('public')? type methodName=ID '(' (type params+=ID (',' type params+=ID)*)? ')' '{' (varDeclaration)*
    (statement)* 'return' expression ';' '}' #Method
    | ('public')? 'static' 'void' methodName='main' '(' 'String' '['']' args=ID ')' '{' (varDeclaration)* (statement)* '}'#Method;

type locals [boolean isArray = false]
    : name='int' ('['']'{$isArray = true;})?
    | name='boolean'
    | name='int'
    | name='String'
    | name=ID;

statement
    : '{'(statement)*'}' #Stmt
    | 'if' '(' expression ')' statement 'else' statement #IfElseStmt
    | 'while' '(' expression ')' statement #WhileStmt
    | expression ';' #ExprStmt
    | var=ID '=' expression ';' #Assignment
    | var=ID '[' expression ']' '=' expression ';' #ArrayAssignment;

expression
    : '('expression')' #Parenthesis
    | expression '[' expression ']' #Indexing
    | expression'.'op='length' #Length
    | expression'.'name=ID'('(expression(','expression)*)?')' #MethodCall
    | op='!'expression #UnaryOp
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op='<' expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | 'new' 'int' '['expression']' #NewIntArray
    | 'new' name=ID'('')' #NewObject
    | value=INTEGER #Integer
    | value='true' #Boolean
    | value='false' #Boolean
    | value=ID #Identifier
    | value='this' #This;

