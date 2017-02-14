grammar Soql;

@header {
package org.talend.components.salesforce.soql.parser;
}

// Fragments

fragment A : 'a' | 'A' ;
fragment B : 'b' | 'B' ;
fragment C : 'c' | 'C' ;
fragment D : 'd' | 'D' ;
fragment E : 'e' | 'E' ;
fragment F : 'f' | 'F' ;
fragment G : 'g' | 'G' ;
fragment H : 'h' | 'H' ;
fragment I : 'i' | 'I' ;
fragment J : 'j' | 'J' ;
fragment K : 'k' | 'K' ;
fragment L : 'l' | 'L' ;
fragment M : 'm' | 'M' ;
fragment N : 'n' | 'N' ;
fragment O : 'o' | 'O' ;
fragment P : 'p' | 'P' ;
fragment Q : 'q' | 'Q' ;
fragment R : 'r' | 'R' ;
fragment S : 's' | 'S' ;
fragment T : 't' | 'T' ;
fragment U : 'u' | 'U' ;
fragment V : 'v' | 'V' ;
fragment W : 'w' | 'W' ;
fragment X : 'x' | 'X' ;
fragment Y : 'y' | 'Y' ;
fragment Z : 'z' | 'Z' ;

// Lexer rules

WS : [ \t\r\n]+ -> channel(HIDDEN) ;
COMMA : ',' ;
DOT : '.' ;
LPAR : '(' ;                     // left parenthesis
RPAR : ')' ;                     // right parenthesis

FROM : F R O M ;
SELECT : S E L E C T ;
NAME : [a-zA-Z_$] [a-zA-Z_$0-9]* ;
ANYCHAR : . ;

// Parser rules
query : selectClause fromClause (anythingClause)? ;
selectClause : SELECT fieldList (COMMA subqueryList)? ;
fromClause : FROM object ;
anythingClause : (anything)+ ;

fieldList : field (COMMA field)* ;
subqueryList : subquery (COMMA subquery)* ;
field : (objectPrefix)? NAME ;
object : (objectPrefix)? NAME ;
objectPrefix : (NAME DOT)+ ;
subquery : LPAR subSelectClause fromClause RPAR ;
subSelectClause : SELECT fieldList ;
anyword : (ANYCHAR)+ ;


anything : 
             NAME 
		   | anyword
		   | COMMA
		   | DOT
		   | LPAR
		   | RPAR
		   | BY
;

