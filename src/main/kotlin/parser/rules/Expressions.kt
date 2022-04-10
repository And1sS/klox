package parser.rules

import parser.NodeToken
import parser.Rule
import parser.ast.UnaryOperatorExpression
import parser.andRule
import parser.orRule
import parser.toOperatorTypeAndOperand

// expression -> equality
val expressionRule = Rule { ctx ->
    equalityRule.match(ctx)
}

// TODO: add parenthesized AST node
// "(" expression ")"
private val parenthesizedRule: Rule = andRule(leftParenRule, expressionRule, rightParenRule) { it[1] }

// primaryExpression -> NUMBER | STRING | "true" | "false" | "nil" | parenthesizedRule | identifier
private val primaryExpressionRule = orRule(
    numberLiteralRule,
    stringLiteralRule,
    trueRule,
    falseRule,
    nilRule,
    parenthesizedRule,
    identifierRule
)

private val intermediateUnaryRule = Rule { ctx ->
    andRule(orRule(minusRule, bangRule), unaryOperatorRule) { tokens ->
        tokens
            .toOperatorTypeAndOperand()
            .let { (type, operand) -> UnaryOperatorExpression(type, operand) }
            .let(::NodeToken)
    }.match(ctx)
}
// unaryOperator -> ( "!" | "-" ) unaryOperator | primaryExpression
private val unaryOperatorRule: Rule = orRule(intermediateUnaryRule, primaryExpressionRule)

// factor -> unary ( ( "/" | "*" ) unary )*
private val factorRule: Rule = binaryOperatorRule(unaryOperatorRule, orRule(starRule, slashRule))

// term -> factor ( ( "-" | "+" ) factor )*
private val termRule: Rule = binaryOperatorRule(factorRule, orRule(plusRule, minusRule))

// comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
private val comparisonRule: Rule = binaryOperatorRule(
    termRule,
    orRule(greaterRule, greaterEqualRule, lessRule, lessEqualRule)
)

// equality -> comparison ( ( "!=" | "==" ) comparison )*
private val equalityRule: Rule = binaryOperatorRule(comparisonRule, orRule(bangEqualRule, equalEqualRule))