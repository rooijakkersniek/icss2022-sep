grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: variableAssignment* stylerule+ EOF;

variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR value SEMICOLON;

stylerule: tag OPEN_BRACE statement* CLOSE_BRACE;
declaration: variable COLON value SEMICOLON;

tag: ID_IDENT | CLASS_IDENT | LOWER_IDENT | CAPITAL_IDENT;
variable: CAPITAL_IDENT | LOWER_IDENT;
value: expr;

expr:  expr MUL expr | expr PLUS expr | expr MIN expr | basic;
basic: TRUE | FALSE | PIXELSIZE | PERCENTAGE | SCALAR | COLOR | variable;

ifclause: IF BOX_BRACKET_OPEN expr BOX_BRACKET_CLOSE OPEN_BRACE statement* CLOSE_BRACE elseclause?;
elseclause: ELSE OPEN_BRACE statement* CLOSE_BRACE;
statement: declaration | variableAssignment | ifclause;


