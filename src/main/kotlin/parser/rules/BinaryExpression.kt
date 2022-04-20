package parser.rules

import parser.Combiner
import parser.CompositeToken
import parser.MatchResult
import parser.Matched
import parser.NodeToken
import parser.Rule
import parser.Unmatched
import parser.andRule
import ast.BinaryOperatorExpression
import ast.Expression
import parser.toOperatorTypeAndOperand
import parser.validateGrammar
import parser.zeroOrMoreRule

// binaryOperator -> operand (operator operand)*
fun binaryOperatorRule(operandRule: Rule, operatorRule: Rule): Rule =
    binaryOperatorRule(operandRule, operatorRule, defaultBinaryOperatorCombiner)

fun binaryOperatorRule(operandRule: Rule, operatorRule: Rule, combiner: Combiner): Rule = Rule { ctx ->
    andRule(operandRule, zeroOrMoreRule(andRule(operatorRule, operandRule)))
        .match(ctx)
        .map { matched ->
            validateGrammar(matched.token is CompositeToken)
            Matched(combiner(matched.token.tokens), matched.newCtx)
        }
}

private val defaultBinaryOperatorCombiner: Combiner = { tokens ->
    var processed: Expression = (tokens[0] as? NodeToken)?.node as? Expression
        ?: throw RuntimeException("Invalid grammar: operator argument is not an expression")

    tokens
        .drop(1)
        .chunked(2)
        .forEach {
            val (type, operand) = it.toOperatorTypeAndOperand()
            processed = BinaryOperatorExpression(type, processed, operand)
        }

    NodeToken(processed)
}


private fun MatchResult.map(mapper: (Matched) -> MatchResult): MatchResult =
    when (this) {
        is Unmatched -> Unmatched
        is Matched -> mapper(this)
    }
