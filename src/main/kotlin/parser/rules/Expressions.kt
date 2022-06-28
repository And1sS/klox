package parser.rules

import ast.AssignmentExpression
import ast.Expression
import ast.LabelExpression
import ast.UnaryOperatorExpression
import parser.Combiner
import parser.NodeToken
import parser.Rule
import parser.andRule
import parser.orRule
import parser.toOperatorTypeAndOperand
import parser.validateGrammar

// expression -> assignment
val expressionRule = Rule { ctx ->
    assignmentRule.match(ctx)
}

// TODO: add parenthesized AST node
// parenthesized -> "(" expression ")"
private val parenthesizedRule: Rule =
    andRule(leftParenRule, expressionRule, rightParenRule) { it[1] }

// primary -> NUMBER | STRING | "true" | "false" | "nil" | "this" | parenthesized | identifier
val primaryExpressionRule = orRule(
    numberLiteralRule,
    stringLiteralRule,
    trueRule,
    falseRule,
    nilRule,
    thisKeywordRule,
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
private val equalityRule: Rule =
    binaryOperatorRule(comparisonRule, orRule(bangEqualRule, equalEqualRule))

// logicAnd -> equality ( "and" equality )*
private val logicAndRule: Rule = binaryOperatorRule(equalityRule, andKeywordRule)

// logicOr -> logicAnd ( "or" logicAnd )*
private val logicOrRule: Rule = binaryOperatorRule(logicAndRule, orKeywordRule)

private val intermediateAssignmentRule: Rule = Rule { ctx ->
    assignmentRule.match(ctx)
}

// assignment -> ( call "=" assignment ) | logicOr
private val assignmentRule: Rule = orRule(
    andRule(callRule, equalRule, intermediateAssignmentRule) { assignmentCombiner(it) },
    logicOrRule
)

private val assignmentCombiner: Combiner = { tokens ->
    val (labelToken, _, exprToken) = tokens
    validateGrammar(labelToken is NodeToken && labelToken.node is LabelExpression)
    validateGrammar(exprToken is NodeToken && exprToken.node is Expression)

    NodeToken(AssignmentExpression(labelToken.node, exprToken.node))
}
