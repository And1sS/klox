package parser.rules

import parser.Combiner
import parser.NodeToken
import parser.OptionalToken
import parser.Rule
import parser.ast.UnaryOperatorExpression
import parser.andRule
import parser.ast.AssignmentExpression
import parser.ast.Expression
import parser.ast.IdentifierExpression
import parser.orRule
import parser.toOperatorTypeAndOperand

// expression -> assignment
val expressionRule = Rule { ctx ->
    assignmentRule.match(ctx)
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

private val intermediateAssignmentRule: Rule = Rule { ctx ->
    assignmentRule.match(ctx)
}

// assignment -> ( identifier "=" assignment ) | equality
private val assignmentRule: Rule = orRule(
    andRule(identifierRule, equalRule, intermediateAssignmentRule) { assignmentCombiner(it) },
    equalityRule
)

private val assignmentCombiner: Combiner = { tokens ->
    val (identifierToken, _, exprToken) = tokens
    require(identifierToken is NodeToken && identifierToken.node is IdentifierExpression) {
        "Invalid grammar"
    }
    require(exprToken is NodeToken && exprToken.node is Expression) {
        "Invalid grammar"
    }
    NodeToken(AssignmentExpression(identifierToken.node, exprToken.node))
}