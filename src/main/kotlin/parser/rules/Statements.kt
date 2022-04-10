package parser.rules

import parser.ast.Expression
import parser.ast.ExpressionStatement
import parser.NodeToken
import parser.ast.PrintStatement
import parser.Rule
import parser.andRule
import parser.orRule

// printStatement -> "print" expression ";"
val printStatementRule: Rule = andRule(printRule, expressionRule, semicolonRule) { tokens ->
    val (_, expressionToken, _) = tokens
    require(expressionToken is NodeToken && expressionToken.node is Expression) {
        "Invalid grammar"
    }
    NodeToken(PrintStatement(expressionToken.node))
}

// expressionStatement -> expression ";"
val expressionStatementRule: Rule = andRule(expressionRule, semicolonRule) { tokens ->
    val (expressionToken, _) = tokens
    require(expressionToken is NodeToken && expressionToken.node is Expression) {
        "Invalid grammar"
    }
    NodeToken(ExpressionStatement(expressionToken.node))
}

// statement -> expressionStatement | printStatement
val statementRule: Rule = orRule(expressionStatementRule, printStatementRule)