package interpreter.astTraversals.semantic

import ast.AssignmentExpression
import ast.BinaryOperatorExpression
import ast.Expression
import ast.CallExpression
import ast.Literal
import ast.ResolvedIdentifierExpression
import ast.UnaryOperatorExpression
import ast.UnresolvedIdentifierExpression
import exception.EvaluationException
import interpreter.Environment

fun resolveExpression(expr: Expression, evaluationEnvironment: Environment): Expression =
    when (expr) {
        is Literal -> expr
        is UnresolvedIdentifierExpression -> evaluationEnvironment.resolveVariable(expr)
        is UnaryOperatorExpression -> resolveUnaryOperatorExpression(expr, evaluationEnvironment)
        is BinaryOperatorExpression -> resolveBinaryOperatorExpression(expr, evaluationEnvironment)
        is AssignmentExpression -> resolveAssignmentExpression(expr, evaluationEnvironment)
        is CallExpression -> resolveFunctionCallExpression(expr, evaluationEnvironment)
        // this branch shouldn't have been reached
        is ResolvedIdentifierExpression ->
            throw EvaluationException("Trying to evaluate unresolved variable")
    }

private fun resolveFunctionCallExpression(
    expr: CallExpression,
    evaluationEnvironment: Environment
): CallExpression {
    val functionExpression = resolveExpression(expr.function, evaluationEnvironment)
    val arguments: List<Expression> = expr.arguments
        .map { arg -> resolveExpression(arg, evaluationEnvironment) }

    return CallExpression(functionExpression, arguments)
}

private fun resolveAssignmentExpression(
    expr: AssignmentExpression,
    evaluationEnvironment: Environment
): AssignmentExpression {
    require(expr.identifier is UnresolvedIdentifierExpression) {
        "This branch shouldn't have been reached"
    }
    return AssignmentExpression(
        identifier = evaluationEnvironment.resolveVariable(expr.identifier),
        expr = resolveExpression(expr.expr, evaluationEnvironment)
    )
}

fun resolveBinaryOperatorExpression(
    expression: BinaryOperatorExpression,
    evaluationEnvironment: Environment
): BinaryOperatorExpression =
    BinaryOperatorExpression(
        operatorType = expression.operatorType,
        lhs = resolveExpression(expression.lhs, evaluationEnvironment),
        rhs = resolveExpression(expression.rhs, evaluationEnvironment)
    )

fun resolveUnaryOperatorExpression(
    expression: UnaryOperatorExpression,
    evaluationEnvironment: Environment
): UnaryOperatorExpression =
    UnaryOperatorExpression(
        expression.operatorType,
        resolveExpression(expression.expr, evaluationEnvironment)
    )

