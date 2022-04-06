package evaluator

import parser.BinaryOperatorExpression
import parser.Expression
import parser.UnaryOperatorExpression
import parser.Value

fun evaluate(ast: Expression): Value = when (ast) {
    is Value -> ast
    is UnaryOperatorExpression -> evaluateUnaryOperatorExpression(ast)
    is BinaryOperatorExpression -> evaluateBinaryOperatorExpression(ast)
    else -> TODO("Unimplemented")
}