package parser.rules

import parser.Combiner
import parser.NodeToken
import parser.Rule
import parser.andRule
import parser.ast.AssignmentExpression
import parser.ast.Expression
import parser.ast.IdentifierExpression
import parser.ast.UnaryOperatorExpression
import parser.orRule
import parser.toOperatorTypeAndOperand
import parser.validateGrammar

// expression -> assignment
val expressionRule = Rule { ctx ->
    assignmentRule.match(ctx)
}

// TODO: add parenthesized AST node
// "(" expression ")"
private val parenthesizedRule: Rule = andRule(leftParenRule, expressionRule, rightParenRule) { it[1] }

// primaryExpression -> NUMBER | STRING | "true" | "false" | "nil" | parenthesizedRule | identifier
val primaryExpressionRule = orRule(
    numberLiteralRule,
    stringLiteralRule,
    trueRule,
    falseRule,
    nilRule,
    parenthesizedRule,
    identifierRule
)

// hack to overcome circular dependency
private val intermediateUnaryRule = Rule { ctx ->
    andRule(orRule(minusRule, bangRule), unaryOperatorRule) { tokens ->
        tokens
            .toOperatorTypeAndOperand()
            .let { (type, operand) -> UnaryOperatorExpression(type, operand) }
            .let(::NodeToken)
    }.match(ctx)
}

// unaryOperator -> ( "!" | "-" ) unaryOperator | call
private val unaryOperatorRule: Rule = orRule(intermediateUnaryRule, callRule)

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

// logicAnd -> equality ( "or" equality )*
private val logicAndRule: Rule = binaryOperatorRule(equalityRule, andKeywordRule)

// logicOr -> logicAnd ( "or" logicAnd )*
private val logicOrRule: Rule = binaryOperatorRule(logicAndRule, orKeywordRule)

private val intermediateAssignmentRule: Rule = Rule { ctx ->
    assignmentRule.match(ctx)
}

// assignment -> ( identifier "=" assignment ) | logicOr
private val assignmentRule: Rule = orRule(
    andRule(identifierRule, equalRule, intermediateAssignmentRule) { assignmentCombiner(it) },
    logicOrRule
)

private val assignmentCombiner: Combiner = { tokens ->
    val (identifierToken, _, exprToken) = tokens
    validateGrammar(identifierToken is NodeToken && identifierToken.node is IdentifierExpression)
    validateGrammar(exprToken is NodeToken && exprToken.node is Expression)

    NodeToken(AssignmentExpression(identifierToken.node, exprToken.node))
}
